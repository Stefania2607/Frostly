package dao;

import frostly.exception.FrostlyException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestore della connessione JDBC realizzato con pattern GoF Singleton.
 * Ha la singola responsabilita' di aprire e fornire l'unica connessione JDBC all'applicazione.
 * La creazione e migrazione dello schema sono delegate a {@link SchemaDatabase}.
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
                SchemaDatabase.inizializzaSchemaEDati(this.connection, true);
            } else {
                this.connection = DriverManager.getConnection(FALLBACK_SQLITE_URL);
                SchemaDatabase.inizializzaSchemaEDati(this.connection, false);
            }
        } catch (SQLException mysqlEx) {
            try {
                this.connection = DriverManager.getConnection(FALLBACK_SQLITE_URL);
                SchemaDatabase.inizializzaSchemaEDati(this.connection, false);
            } catch (SQLException sqliteEx) {
                throw new FrostlyException("Errore durante l'apertura dell'unica connessione JDBC.", sqliteEx);
            }
        }
    }
}
