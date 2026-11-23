package dao;

import frostly.exception.FrostlyException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestore della connessione JDBC verso MySQL (Singleton).
 * La creazione e migrazione dello schema sono delegate a {@link SchemaDatabase}.
 */
public class ConnectionManager {

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

        // Eventuali credenziali locali (non versionate) sovrascrivono db.properties
        Path localProps = Paths.get("db.local.properties");
        if (Files.exists(localProps)) {
            try (InputStream localIn = Files.newInputStream(localProps)) {
                props.load(localIn);
            } catch (IOException ignored) {
                // Prosegue con i valori di db.properties
            }
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pwd = System.getenv("FROSTLY_DB_PASSWORD");
        if (pwd == null || pwd.isEmpty()) {
            pwd = props.getProperty("db.password", "");
        }

        if (url == null || url.trim().isEmpty()) {
            throw new FrostlyException("Configurazione database mancante: impostare 'db.url' in db.properties.");
        }

        try {
            this.connection = DriverManager.getConnection(url, user, pwd);
            SchemaDatabase.inizializzaSchemaEDati(this.connection);
        } catch (SQLException mysqlEx) {
            throw new FrostlyException(
                    "Impossibile connettersi al database MySQL configurato (" + url + "): " + mysqlEx.getMessage(),
                    mysqlEx);
        }
    }
}
