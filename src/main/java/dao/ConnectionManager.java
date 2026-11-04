package dao;

import frostly.exception.FrostlyException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gestore della connessione JDBC realizzato con pattern GoF Singleton.
 * Legge i parametri di connessione al database MySQL (DBeaver) dal file 'db.properties'
 * e mantiene UNA SOLA connessione aperta per tutta l'esecuzione dell'applicazione.
 */
public class ConnectionManager {

    private static final String FALLBACK_SQLITE_URL = "jdbc:sqlite:frostly_db.sqlite";
    private static ConnectionManager instance;
    private Connection connection;

    private ConnectionManager() throws FrostlyException {
        apriConnessione();
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
                apriConnessione();
            }
            return this.connection;
        } catch (SQLException e) {
            throw new FrostlyException("Connessione JDBC non disponibile.", e);
        }
    }

    private void apriConnessione() throws FrostlyException {
        Properties props = new Properties();
        try (InputStream in = ConnectionManager.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new FrostlyException("Impossibile leggere il file db.properties.", e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pwd = props.getProperty("db.password");

        try {
            if (url != null && !url.isEmpty()) {
                this.connection = DriverManager.getConnection(url, user, pwd);
                inizializzaTabelle(true);
            } else {
                this.connection = DriverManager.getConnection(FALLBACK_SQLITE_URL);
                inizializzaTabelle(false);
            }
        } catch (SQLException mysqlEx) {
            try {
                this.connection = DriverManager.getConnection(FALLBACK_SQLITE_URL);
                inizializzaTabelle(false);
            } catch (SQLException sqliteEx) {
                throw new FrostlyException("Errore durante l'apertura dell'unica connessione JDBC.", sqliteEx);
            }
        }
    }

    private void inizializzaTabelle(boolean isMySql) throws SQLException {
        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS utenti ("
                    + "email VARCHAR(64) PRIMARY KEY, "
                    + "nome VARCHAR(50) NOT NULL, "
                    + "cognome VARCHAR(50) NOT NULL, "
                    + "password VARCHAR(64) NOT NULL, "
                    + "ruolo VARCHAR(30) NOT NULL)");

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

            if (isMySql) {
                stmt.execute("INSERT IGNORE INTO utenti (email, nome, cognome, password, ruolo) VALUES "
                        + "('maestro@frostly.it', 'Guglielmo', 'Bianchi', 'admin123', 'MAESTRO_GELATAIO'),"
                        + "('mario@frostly.it', 'Mario', 'Rossi', 'pass123', 'DIPENDENTE'),"
                        + "('giulia@frostly.it', 'Giulia', 'Verdi', 'pass123', 'DIPENDENTE'),"
                        + "('luca@frostly.it', 'Luca', 'Neri', 'pass123', 'DIPENDENTE'),"
                        + "('elena@cliente.it', 'Elena', 'Conti', 'pass123', 'CLIENTE'),"
                        + "('marco@cliente.it', 'Marco', 'Fabbri', 'pass123', 'CLIENTE')");

                stmt.execute("INSERT IGNORE INTO turni (codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato) VALUES "
                        + "('TRN-01', 'Lunedi', '08:00', '14:00', 'mario@frostly.it', 'PUBBLICATO'),"
                        + "('TRN-02', 'Lunedi', '14:00', '20:00', 'giulia@frostly.it', 'PUBBLICATO'),"
                        + "('TRN-03', 'Martedi', '08:00', '14:00', 'mario@frostly.it', 'IN_EMERGENZA')");
            }
        }
    }
}
