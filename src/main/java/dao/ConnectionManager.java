package dao;

import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestore della connessione JDBC realizzato con pattern GoF Singleton.
 * Garantisce che venga aperta UNA SOLA connessione al DBMS per tutta l'esecuzione
 * dell'applicazione, come richiesto dalle regole architetturali del corso ISPW.
 */
public class ConnectionManager {

    private static final String DB_URL = "jdbc:sqlite:frostly_db.sqlite";
    private static ConnectionManager instance;
    private Connection connection;

    private ConnectionManager() throws FrostlyException {
        try {
            this.connection = DriverManager.getConnection(DB_URL);
            inizializzaTabelle();
        } catch (SQLException e) {
            throw new FrostlyException("Errore durante l'apertura dell'unica connessione JDBC.", e);
        }
    }

    public static synchronized ConnectionManager getInstance() throws FrostlyException {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() throws FrostlyException {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.connection = DriverManager.getConnection(DB_URL);
            }
            return this.connection;
        } catch (SQLException e) {
            throw new FrostlyException("Connessione JDBC non disponibile.", e);
        }
    }

    private void inizializzaTabelle() throws SQLException {
        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS turni ("
                    + "codice_turno VARCHAR(32) PRIMARY KEY, "
                    + "data_turno VARCHAR(20) NOT NULL, "
                    + "ora_inizio VARCHAR(10) NOT NULL, "
                    + "ora_fine VARCHAR(10) NOT NULL, "
                    + "email_dipendente VARCHAR(64) NOT NULL, "
                    + "stato VARCHAR(20) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS emergenze ("
                    + "codice_emergenza VARCHAR(32) PRIMARY KEY, "
                    + "codice_turno VARCHAR(32) NOT NULL, "
                    + "email_richiedente VARCHAR(64) NOT NULL, "
                    + "email_sostituto VARCHAR(64), "
                    + "motivazione VARCHAR(255) NOT NULL, "
                    + "stato VARCHAR(20) NOT NULL, "
                    + "nota_risposta VARCHAR(255))");

            stmt.execute("CREATE TABLE IF NOT EXISTS richieste_gusto ("
                    + "codice_richiesta VARCHAR(32) PRIMARY KEY, "
                    + "email_cliente VARCHAR(64) NOT NULL, "
                    + "nome_gusto VARCHAR(64) NOT NULL, "
                    + "ingrediente_principale VARCHAR(64) NOT NULL, "
                    + "quantita_kg DOUBLE NOT NULL, "
                    + "data_ritiro VARCHAR(20) NOT NULL, "
                    + "note_cliente VARCHAR(255), "
                    + "stato VARCHAR(20) NOT NULL, "
                    + "risposta_maestro VARCHAR(255))");
        }
    }
}