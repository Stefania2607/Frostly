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

            stmt.execute("CREATE TABLE IF NOT EXISTS ordini_clienti ("
                    + "codice_ordine VARCHAR(32) PRIMARY KEY, "
                    + "email_cliente VARCHAR(64) NOT NULL, "
                    + "gusto_principale VARCHAR(128), "
                    + "peso_kg DOUBLE NOT NULL, "
                    + "data_ritiro VARCHAR(128) NOT NULL, "
                    + "stato VARCHAR(20) NOT NULL, "
                    + "prezzo_totale DOUBLE NOT NULL, "
                    + "descrizione_dettagliata VARCHAR(1000) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS notifiche_utenti ("
                    + "email_destinatario VARCHAR(64) NOT NULL, "
                    + "titolo VARCHAR(128) NOT NULL, "
                    + "messaggio VARCHAR(1000) NOT NULL, "
                    + "timestamp_notifica VARCHAR(64) NOT NULL)");

                        stmt.execute("CREATE TABLE IF NOT EXISTS ingredienti ("
                    + "nome VARCHAR(64) PRIMARY KEY, "
                    + "quantita_disponibile DOUBLE NOT NULL, "
                    + "soglia_minima DOUBLE NOT NULL, "
                    + "unita_misura VARCHAR(20) NOT NULL)");

            boolean ingredientiVuoti = true;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ingredienti")) {
                if (rs.next()) {
                    ingredientiVuoti = rs.getInt(1) == 0;
                }
            }
            if (ingredientiVuoti) {
                stmt.execute("INSERT INTO ingredienti (nome, quantita_disponibile, soglia_minima, unita_misura) VALUES "
                        + "('Latte Fresco Alta Qualita', 25.0, 10.0, 'Litri'),"
                        + "('Pasta Pistacchio di Bronte DOP', 6.0, 2.5, 'Kg'),"
                        + "('Cioccolato Fondente Ecuador 75%', 8.0, 3.0, 'Kg'),"
                        + "('Nocciola Piemonte IGP', 5.0, 2.0, 'Kg'),"
                        + "('Polpa di Mango Alphonso', 1.5, 2.0, 'Kg')");
            }

            boolean emergenzeVuote = true;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM emergenze")) {
                if (rs.next()) {
                    emergenzeVuote = rs.getInt(1) == 0;
                }
            }
            if (emergenzeVuote) {
                stmt.execute("INSERT INTO emergenze (codice_emergenza, codice_turno, email_richiedente, email_sostituto, motivazione, stato, nota_risposta) VALUES "
                        + "('EMG-01', 'TRN-03', 'mario@frostly.it', NULL, 'Visita medica improvvisa martedi mattina', 'IN_ATTESA', NULL)");
            }
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

    /** Prodotti di esempio del catalogo: garantisce che tutte le categorie e foto siano presenti nel DB. */
    private void inserisciProdottiEsempio(Statement stmt) throws SQLException {
        aggiornaFotoSeVuota(stmt, "Pistacchio di Bronte DOP", "1793962265284_nascita-gelato-pistacchio.jpg");
        aggiornaFotoSeVuota(stmt, "Fondente Ecuador 75%", "1793962340626_gelato-al-cioccolato-570x380.jpg");
        aggiornaFotoSeVuota(stmt, "Nocciola Piemonte IGP", "1793962399876_Screenshot_2026-11-06_alle_11.53.13.png");
        aggiornaFotoSeVuota(stmt, "Crema Antica del Maestro", "1793962432008_Screenshot_2026-11-06_alle_11.53.44.png");
        aggiornaFotoSeVuota(stmt, "Stracciatella Artigianale", "1793962480783_stracciatella-gelato-recipe-1-1200px.jpg");
        aggiornaFotoSeVuota(stmt, "Sorbetto Mango Alphonso", "1793962514184_Screenshot_2026-11-06_alle_11.55.07.png");

        inserisciSeAssente(stmt, "Fragola", "Sorbetto fresco alle fragole mature di bosco", "Senza Lattosio", 22.0, "Gelati", "Frutta", "fragola.png");
        inserisciSeAssente(stmt, "Menta", "Gelato rinfrescante alla menta piperita del Piemonte", "Latte", 22.0, "Gelati", "Frutta", "menta.png");
        inserisciSeAssente(stmt, "Limone", "Sorbetto artigianale ai limoni di Sorrento IGP", "Senza Lattosio", 22.0, "Gelati", "Frutta", "limone.png");
        inserisciSeAssente(stmt, "Melone Bianco", "Sorbetto delicato e profumato al melone bianco invernale", "Senza Lattosio", 23.0, "Gelati", "Frutta", "melone_bianco.png");
        inserisciSeAssente(stmt, "Lampone", "Sorbetto vellutato ai lamponi freschi di montagna", "Senza Lattosio", 24.0, "Gelati", "Frutta", "lampone.png");
        inserisciSeAssente(stmt, "Anguria", "Sorbetto dissetante al cuore di anguria estiva", "Senza Lattosio", 22.0, "Gelati", "Frutta", "anguria.png");
        inserisciSeAssente(stmt, "Liquirizia", "Gelato intenso alla liquirizia pura di Calabria DOP", "Latte", 24.0, "Gelati", "Frutta", "liquirizia.png");
        inserisciSeAssente(stmt, "Pesca", "Sorbetto morbido alle pesche gialle di vigna", "Senza Lattosio", 22.0, "Gelati", "Frutta", "pesca.png");
        inserisciSeAssente(stmt, "Albicocca", "Sorbetto solare alle albicocche del Vesuvio", "Senza Lattosio", 22.0, "Gelati", "Frutta", "albicocca.png");
        inserisciSeAssente(stmt, "Banana", "Cremoso naturale alla banana matura", "Latte", 22.0, "Gelati", "Frutta", "banana.png");
        inserisciSeAssente(stmt, "Banana al Cioccolato", "Gelato alla banana con variegatura e scaglie di cioccolato fondente", "Latte", 24.0, "Gelati", "Frutta", "banana_cioccolato.png");

        inserisciSeAssente(stmt, "Snickers", "Cremoso alle arachidi con caramello salato, cioccolato al latte e arachidi tostate", "Latte, Arachidi, Frutta a guscio", 26.0, "Gelati", "Special", "snickers.png");
        inserisciSeAssente(stmt, "Bounty", "Gelato esotico al cocco fresco con scaglie di cioccolato e cocco rapè", "Latte", 25.0, "Gelati", "Special", "bounty.png");
        inserisciSeAssente(stmt, "Delizia al Limone", "Pan di Spagna bagnato al limoncello con crema vellutata ai limoni d'Amalfi", "Latte, Uova, Glutine", 26.0, "Gelati", "Special", "delizia_limone.png");
        inserisciSeAssente(stmt, "Nutella", "Cremoso artigianale con abbondante Nutella pura variegata", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "gelato_nutella.png");
        inserisciSeAssente(stmt, "Galak", "Gelato al cioccolato bianco Galak con croccanti sfoglie di wafer", "Latte, Glutine", 25.0, "Gelati", "Special", "galak.png");
        inserisciSeAssente(stmt, "Amarandola", "Mandorla di Sicilia variegato con amarene intere sciroppate", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "amarandola.png");
        inserisciSeAssente(stmt, "Croccante all'Amarena", "Fiordilatte con croccante di mandorle caramellate e variegatura all'amarena", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "croccante_amarena.png");
        inserisciSeAssente(stmt, "Pistacchio variegato Cioccolato e Wafer", "Pistacchio tostato variegato con crema al cioccolato e cialde di wafer", "Latte, Frutta a guscio, Glutine", 27.0, "Gelati", "Special", "pistacchio_cioccolato_wafer.png");
        inserisciSeAssente(stmt, "Cremino Nutella", "Strati vellutati di fiordilatte e nocciola alternati a colate di Nutella", "Latte, Frutta a guscio", 27.0, "Gelati", "Special", "cremino_nutella.png");
        inserisciSeAssente(stmt, "Mascarpone Amarena e Gentilini", "Crema al mascarpone fresco con amarene brusche e biscotti Osvego Gentilini", "Latte, Uova, Glutine", 27.0, "Gelati", "Special", "mascarpone_amarena_gentilini.png");
        inserisciSeAssente(stmt, "Caffè", "Gelato cremoso all'espresso 100% Arabica tostato a legna", "Latte", 23.0, "Gelati", "Special", "gelato_caffe.png");
        inserisciSeAssente(stmt, "Spagnola", "Crema antica e fiordilatte variegati con amarene Fabbri intere", "Latte, Uova", 25.0, "Gelati", "Special", "spagnola.png");
        inserisciSeAssente(stmt, "Zuppa Inglese", "Crema pasticcera e cacao con pan di Spagna all'Alchermes e savoiardi", "Latte, Uova, Glutine", 26.0, "Gelati", "Special", "zuppa_inglese.png");

        inserisciSeAssente(stmt, "Panna Fresca 100g", "Coppetta da 100g di panna fresca d'alpeggio montata al momento", "Latte", 2.5, "Panna", "Porzioni", "panna_100g.png");
        inserisciSeAssente(stmt, "Panna Fresca 200g", "Vaschetta da 200g di panna fresca d'alpeggio montata al momento", "Latte", 4.5, "Panna", "Porzioni", "panna_200g.png");
        inserisciSeAssente(stmt, "Panna Fresca 300g", "Vaschetta grande da 300g di panna fresca d'alpeggio montata", "Latte", 6.0, "Panna", "Porzioni", "panna_300g.png");

        stmt.executeUpdate("DELETE FROM menu_gusti WHERE categoria = 'Crepes' AND sottocategoria = 'Topping'");
        stmt.executeUpdate("DELETE FROM menu_gusti WHERE nome_gusto IN ('Crepe Marmellata di Visciole', 'Crepe Marmellata di Albicocca')");

        inserisciSeAssente(stmt, "Crepe alla Nutella", "Crepe calda artigianale farcita con abbondante Nutella cremosa", "Glutine, Latte, Frutta a guscio", 5.0, "Crepes", "Farciture", "crepe_nutella.png");
        inserisciSeAssente(stmt, "Crepe alle Visciole", "Crepe calda farcita con confettura artigianale di visciole brusche", "Glutine, Uova, Latte", 4.5, "Crepes", "Farciture", "crepe_visciole.png");
        inserisciSeAssente(stmt, "Crepe alla Marmellata", "Crepe calda farcita con confettura vellutata di albicocche", "Glutine, Uova, Latte", 4.5, "Crepes", "Farciture", "crepe_albicocca.png");
        inserisciSeAssente(stmt, "Crepe Crema Pan di Stelle", "Crepe calda con crema spalmabile Pan di Stelle, panna e stelline croccanti", "Glutine, Latte, Uova, Frutta a guscio", 5.5, "Crepes", "Farciture", "crepe_pandistelle.png");

        inserisciSeAssente(stmt, "Acqua Naturale 500ml", "Acqua minerale naturale fresca 500ml", "Nessuno", 1.5, "Bevande", "Acqua", "acqua_naturale.png");
        inserisciSeAssente(stmt, "Acqua Frizzante 500ml", "Acqua minerale frizzante fresca 500ml", "Nessuno", 1.5, "Bevande", "Acqua", "acqua_frizzante.png");
        inserisciSeAssente(stmt, "Coca Cola 33cl", "Lattina classica fresca 33cl", "Nessuno", 3.0, "Bevande", "Bibite", "coca_cola.png");
        inserisciSeAssente(stmt, "Fanta 33cl", "Lattina all'arancia fresca 33cl", "Nessuno", 3.0, "Bevande", "Bibite", "fanta.png");

        inserisciSeAssente(stmt, "Caffe Espresso", "Miscela 100% arabica tostata lentamente", "Nessuno", 1.2, "Caffetteria", "Caffe", "");
        inserisciSeAssente(stmt, "Cappuccino", "Espresso con latte fresco montato a vapore", "Latte", 1.8, "Caffetteria", "Caffe", "");
        inserisciSeAssente(stmt, "Affogato al Caffe", "Gelato fior di latte annegato nell'espresso caldo", "Latte", 4.0, "Caffetteria", "Specialita", "");
        inserisciSeAssente(stmt, "Cioccolata Calda Fondente", "Densa, con cacao 70% e un tocco di panna", "Latte", 3.5, "Caffetteria", "Specialita", "");
        inserisciSeAssente(stmt, "Cantucci alle Mandorle", "Biscotti toscani croccanti, ideali per il caffe", "Glutine, Frutta a guscio, Uova", 5.0, "Biscotti", "Secchi", "");
        inserisciSeAssente(stmt, "Baci di Dama", "Frollini alla nocciola farciti di cioccolato", "Glutine, Frutta a guscio, Latte", 6.0, "Biscotti", "Secchi", "");
        inserisciSeAssente(stmt, "Crostata alla Marmellata", "Frolla al burro con confettura di albicocche", "Glutine, Uova, Latte", 2.8, "Crostate", "Classiche", "");
        inserisciSeAssente(stmt, "Crostata ai Frutti di Bosco", "Crema pasticcera e frutti di bosco freschi", "Glutine, Uova, Latte", 3.2, "Crostate", "Alla frutta", "");
        inserisciSeAssente(stmt, "Tisana Zenzero e Limone", "Infuso caldo digestivo e rinfrescante", "Nessuno", 2.5, "Tisane", "Infusi", "");
        inserisciSeAssente(stmt, "Tisana ai Frutti Rossi", "Infuso fruttato senza caffeina", "Nessuno", 2.5, "Tisane", "Infusi", "");
    }

    private void aggiornaFotoSeVuota(Statement stmt, String nome, String imgUrl) throws SQLException {
        String n = nome.replace("'", "''");
        String img = imgUrl.replace("'", "''");
        stmt.executeUpdate("UPDATE menu_gusti SET immagine_url = '" + img + "' WHERE nome_gusto = '" + n + "' AND (immagine_url IS NULL OR immagine_url = '')");
    }

    private void inserisciSeAssente(Statement stmt, String nome, String desc, String allergeni,
                                    double prezzo, String cat, String sottocat, String imgUrl) throws SQLException {
        String n = nome.replace("'", "''");
        try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM menu_gusti WHERE nome_gusto = '" + n + "'")) {
            if (rs.next() && rs.getInt(1) > 0) {
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    aggiornaFotoSeVuota(stmt, nome, imgUrl);
                }
                return;
            }
        }
        String sql = String.format(java.util.Locale.US,
                "INSERT INTO menu_gusti (nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria, immagine_url) "
                + "VALUES ('%s', '%s', '%s', %.2f, 1, '%s', '%s', '%s')",
                n, desc.replace("'", "''"), allergeni.replace("'", "''"),
                prezzo, cat.replace("'", "''"), sottocat.replace("'", "''"),
                imgUrl == null ? "" : imgUrl.replace("'", "''"));
        stmt.executeUpdate(sql);
    }
}
