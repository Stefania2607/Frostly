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
 * Legge i parametri di connessione al database MySQL dal file 'db.properties'
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

            stmt.execute("CREATE TABLE IF NOT EXISTS menu_gusti ("
                    + "nome_gusto VARCHAR(64) PRIMARY KEY, "
                    + "descrizione VARCHAR(255) NOT NULL, "
                    + "allergeni VARCHAR(128) NOT NULL, "
                    + "prezzo_al_kg DOUBLE NOT NULL, "
                    + "disponibile BOOLEAN NOT NULL, "
                    + "categoria VARCHAR(40) NOT NULL DEFAULT 'Gelati', "
                    + "sottocategoria VARCHAR(40) NOT NULL DEFAULT 'Creme', "
                    + "immagine_url VARCHAR(500) NOT NULL DEFAULT '')");
            migraMenuGusti(stmt);

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

            // Menu iniziale: inserito solo se la tabella e' vuota (valido sia per MySQL sia per SQLite)
            boolean menuVuoto = true;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM menu_gusti")) {
                if (rs.next()) {
                    menuVuoto = rs.getInt(1) == 0;
                }
            }
            if (menuVuoto) {
                stmt.execute("INSERT INTO menu_gusti (nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria) VALUES "
                        + "('Pistacchio di Bronte DOP', 'Cremoso al pistacchio siciliano tostato', 'Latte, Frutta a guscio', 26.0, 1, 'Gelati', 'Creme'),"
                        + "('Fondente Ecuador 75%', 'Cioccolato intenso monorigine', 'Latte', 24.0, 1, 'Gelati', 'Cioccolato'),"
                        + "('Nocciola Piemonte IGP', 'Classico cremoso alle nocciole trilobate', 'Latte, Frutta a guscio', 25.0, 1, 'Gelati', 'Creme'),"
                        + "('Crema Antica del Maestro', 'Tuorli freschi, vaniglia Bourbon e scorza di limone', 'Latte, Uova', 22.0, 1, 'Gelati', 'Creme'),"
                        + "('Stracciatella Artigianale', 'Fiordilatte fresco con scaglie di fondente croccante', 'Latte', 23.0, 1, 'Gelati', 'Creme'),"
                        + "('Sorbetto Mango Alphonso', 'Sorbetto 100% frutta fresca senza latte', 'Senza Lattosio', 24.0, 1, 'Gelati', 'Frutta')");
            }
            inserisciProdottiEsempio(stmt);
        }
    }

    /**
     * Aggiunge le colonne categoria/sottocategoria/immagine_url ai database creati prima dell'introduzione
     * delle categorie e assegna una sottocategoria ragionevole ai gusti gia' presenti.
     */
    private void migraMenuGusti(Statement stmt) throws SQLException {
        boolean haCategoria = false;
        try (java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM menu_gusti LIMIT 1")) {
            java.sql.ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                if ("categoria".equalsIgnoreCase(md.getColumnName(i))) {
                    haCategoria = true;
                }
            }
        }
        if (haCategoria) {
            return;
        }
        stmt.execute("ALTER TABLE menu_gusti ADD COLUMN categoria VARCHAR(40) NOT NULL DEFAULT 'Gelati'");
        stmt.execute("ALTER TABLE menu_gusti ADD COLUMN sottocategoria VARCHAR(40) NOT NULL DEFAULT 'Creme'");
        stmt.execute("ALTER TABLE menu_gusti ADD COLUMN immagine_url VARCHAR(500) NOT NULL DEFAULT ''");
        stmt.execute("UPDATE menu_gusti SET sottocategoria = 'Cioccolato' WHERE nome_gusto LIKE 'Fondente%'");
        stmt.execute("UPDATE menu_gusti SET sottocategoria = 'Frutta' "
                + "WHERE nome_gusto LIKE 'Sorbetto%' OR nome_gusto LIKE 'Mango%'");
    }

    /** Prodotti di esempio per le categorie non gelato: inseriti una sola volta, se non ne esiste nessuno. */
    private void inserisciProdottiEsempio(Statement stmt) throws SQLException {
        try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM menu_gusti WHERE categoria <> 'Gelati'")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        stmt.execute("INSERT INTO menu_gusti (nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria) VALUES "
                + "('Caffe Espresso', 'Miscela 100% arabica tostata lentamente', 'Nessuno', 1.2, 1, 'Caffetteria', 'Caffe'),"
                + "('Cappuccino', 'Espresso con latte fresco montato a vapore', 'Latte', 1.8, 1, 'Caffetteria', 'Caffe'),"
                + "('Affogato al Caffe', 'Gelato fior di latte annegato nell''espresso caldo', 'Latte', 4.0, 1, 'Caffetteria', 'Specialita'),"
                + "('Cioccolata Calda Fondente', 'Densa, con cacao 70% e un tocco di panna', 'Latte', 3.5, 1, 'Caffetteria', 'Specialita'),"
                + "('Cantucci alle Mandorle', 'Biscotti toscani croccanti, ideali per il caffe', 'Glutine, Frutta a guscio, Uova', 5.0, 1, 'Biscotti', 'Secchi'),"
                + "('Baci di Dama', 'Frollini alla nocciola farciti di cioccolato', 'Glutine, Frutta a guscio, Latte', 6.0, 1, 'Biscotti', 'Secchi'),"
                + "('Crostata alla Marmellata', 'Frolla al burro con confettura di albicocche', 'Glutine, Uova, Latte', 2.8, 1, 'Crostate', 'Classiche'),"
                + "('Crostata ai Frutti di Bosco', 'Crema pasticcera e frutti di bosco freschi', 'Glutine, Uova, Latte', 3.2, 1, 'Crostate', 'Alla frutta'),"
                + "('Tisana Zenzero e Limone', 'Infuso caldo digestivo e rinfrescante', 'Nessuno', 2.5, 1, 'Tisane', 'Infusi'),"
                + "('Tisana ai Frutti Rossi', 'Infuso fruttato senza caffeina', 'Nessuno', 2.5, 1, 'Tisane', 'Infusi')");
    }
}
