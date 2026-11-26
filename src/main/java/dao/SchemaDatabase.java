package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestisce l'inizializzazione dello schema del database MySQL e il popolamento dei dati base.
 */
public final class SchemaDatabase {

    private SchemaDatabase() {
    }

    public static void inizializzaSchemaEDati(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            creaTabelle(stmt);
            migraUtentiSeNecessario(stmt);
            migraTurniSeNecessario(stmt);
            migraMenuGustiSeNecessario(stmt);
            migraIngredientiSeNecessario(stmt);
            migraOrdiniClientiSeNecessario(stmt);
            migraRecensioniSeNecessario(stmt);
            popolaDatiIniziali(conn, stmt);
        }
    }

    private static void creaTabelle(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS utenti ("
                + "email VARCHAR(64) PRIMARY KEY, "
                + "nome VARCHAR(50) NOT NULL, "
                + "cognome VARCHAR(50) NOT NULL, "
                + "password VARCHAR(64) NOT NULL, "
                + "ruolo VARCHAR(30) NOT NULL, "
                + "modalita_ore VARCHAR(24), "
                + "ore_minime DOUBLE, "
                + "ore_massime DOUBLE, "
                + "tolleranza_sotto DOUBLE DEFAULT 0, "
                + "tolleranza_sopra DOUBLE DEFAULT 0, "
                + "mansioni VARCHAR(120) DEFAULT 'BANCO', "
                + "attivo BOOLEAN DEFAULT TRUE, "
                + "max_turni_settimanali INT, "
                + "disponibilita_config VARCHAR(1000))");

        stmt.execute("CREATE TABLE IF NOT EXISTS turni ("
                + "codice_turno VARCHAR(32) PRIMARY KEY, "
                + "data_turno VARCHAR(20) NOT NULL, "
                + "ora_inizio VARCHAR(10) NOT NULL, "
                + "ora_fine VARCHAR(10) NOT NULL, "
                + "email_dipendente VARCHAR(64) NOT NULL, "
                + "stato VARCHAR(20) NOT NULL, "
                + "bloccato BOOLEAN NOT NULL DEFAULT FALSE, "
                + "generato_auto BOOLEAN NOT NULL DEFAULT FALSE, "
                + "personalizzato BOOLEAN NOT NULL DEFAULT FALSE, "
                + "pausa_minuti INT NOT NULL DEFAULT 0)");

        stmt.execute("CREATE TABLE IF NOT EXISTS fabbisogni_turni ("
                + "chiave VARCHAR(16) PRIMARY KEY, "
                + "giorno INT NOT NULL, "
                + "indice_fascia INT NOT NULL, "
                + "totale_richiesto INT NOT NULL, "
                + "min_banco INT NOT NULL DEFAULT 0, "
                + "min_laboratorio INT NOT NULL DEFAULT 0, "
                + "min_responsabile INT NOT NULL DEFAULT 0)");

        stmt.execute("CREATE TABLE IF NOT EXISTS richieste_indisponibilita ("
                + "codice_richiesta VARCHAR(32) PRIMARY KEY, "
                + "email_dipendente VARCHAR(64) NOT NULL, "
                + "nome_dipendente VARCHAR(100) NOT NULL, "
                + "data_richiesta VARCHAR(20) NOT NULL, "
                + "motivazione VARCHAR(255) NOT NULL, "
                + "stato VARCHAR(20) NOT NULL, "
                + "nota_maestro VARCHAR(255))");

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
                + "data_ritiro VARCHAR(512) NOT NULL, "
                + "stato VARCHAR(20) NOT NULL, "
                + "prezzo_totale DOUBLE NOT NULL, "
                + "descrizione_dettagliata VARCHAR(2000) NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS notifiche_utenti ("
                + "email_destinatario VARCHAR(64) NOT NULL, "
                + "titolo VARCHAR(128) NOT NULL, "
                + "messaggio VARCHAR(1000) NOT NULL, "
                + "timestamp_notifica VARCHAR(64) NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS ingredienti ("
                + "nome VARCHAR(64) PRIMARY KEY, "
                + "quantita_disponibile DOUBLE NOT NULL, "
                + "soglia_minima DOUBLE NOT NULL, "
                + "unita_misura VARCHAR(20) NOT NULL, "
                + "categoria VARCHAR(50) NOT NULL DEFAULT 'Laboratorio & Basi')");

        stmt.execute("CREATE TABLE IF NOT EXISTS menu_gusti ("
                + "nome_gusto VARCHAR(64) PRIMARY KEY, "
                + "descrizione VARCHAR(255) NOT NULL, "
                + "allergeni VARCHAR(128) NOT NULL, "
                + "prezzo_al_kg DOUBLE NOT NULL, "
                + "disponibile BOOLEAN NOT NULL, "
                + "categoria VARCHAR(40) NOT NULL DEFAULT 'Gelati', "
                + "sottocategoria VARCHAR(40) NOT NULL DEFAULT 'Creme', "
                + "immagine_url VARCHAR(500) NOT NULL DEFAULT '')");

        stmt.execute("CREATE TABLE IF NOT EXISTS recensioni ("
                + "codice_recensione VARCHAR(32) PRIMARY KEY, "
                + "autore_email VARCHAR(64) NOT NULL, "
                + "autore_nome VARCHAR(64) NOT NULL, "
                + "voto_stelle INT NOT NULL, "
                + "titolo VARCHAR(128) NOT NULL, "
                + "commento VARCHAR(1000) NOT NULL, "
                + "data_ora VARCHAR(32) NOT NULL, "
                + "risposta_maestro VARCHAR(1000), "
                + "data_risposta VARCHAR(32), "
                + "mi_piace INT NOT NULL DEFAULT 0, "
                + "foto_path VARCHAR(500))");

        stmt.execute("CREATE TABLE IF NOT EXISTS pagamenti ("
                + "codice_transazione VARCHAR(64) PRIMARY KEY, "
                + "email_cliente VARCHAR(64) NOT NULL, "
                + "importo DOUBLE NOT NULL, "
                + "data_ora VARCHAR(32) NOT NULL, "
                + "stato VARCHAR(20) NOT NULL, "
                + "carta_mascherata VARCHAR(30) NOT NULL, "
                + "modalita_consegna VARCHAR(255) NOT NULL)");
    }

    private static void migraUtentiSeNecessario(Statement stmt) throws SQLException {
        boolean haModalita = false;
        boolean haMin = false;
        boolean haMax = false;
        boolean haDisp = false;
        boolean haTollSotto = false;
        boolean haTollSopra = false;
        boolean haMansioni = false;
        boolean haAttivo = false;
        boolean haMaxTurni = false;
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM utenti LIMIT 1")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String col = meta.getColumnName(i);
                if ("modalita_ore".equalsIgnoreCase(col)) haModalita = true;
                else if ("ore_minime".equalsIgnoreCase(col)) haMin = true;
                else if ("ore_massime".equalsIgnoreCase(col)) haMax = true;
                else if ("disponibilita_config".equalsIgnoreCase(col)) haDisp = true;
                else if ("tolleranza_sotto".equalsIgnoreCase(col)) haTollSotto = true;
                else if ("tolleranza_sopra".equalsIgnoreCase(col)) haTollSopra = true;
                else if ("mansioni".equalsIgnoreCase(col)) haMansioni = true;
                else if ("attivo".equalsIgnoreCase(col)) haAttivo = true;
                else if ("max_turni_settimanali".equalsIgnoreCase(col)) haMaxTurni = true;
            }
        }
        if (!haModalita) stmt.execute("ALTER TABLE utenti ADD COLUMN modalita_ore VARCHAR(24)");
        if (!haMin) stmt.execute("ALTER TABLE utenti ADD COLUMN ore_minime DOUBLE");
        if (!haMax) stmt.execute("ALTER TABLE utenti ADD COLUMN ore_massime DOUBLE");
        if (!haDisp) stmt.execute("ALTER TABLE utenti ADD COLUMN disponibilita_config VARCHAR(1000)");
        if (!haTollSotto) stmt.execute("ALTER TABLE utenti ADD COLUMN tolleranza_sotto DOUBLE DEFAULT 0");
        if (!haTollSopra) stmt.execute("ALTER TABLE utenti ADD COLUMN tolleranza_sopra DOUBLE DEFAULT 0");
        if (!haMansioni) stmt.execute("ALTER TABLE utenti ADD COLUMN mansioni VARCHAR(120) DEFAULT 'BANCO'");
        if (!haAttivo) stmt.execute("ALTER TABLE utenti ADD COLUMN attivo BOOLEAN DEFAULT TRUE");
        if (!haMaxTurni) stmt.execute("ALTER TABLE utenti ADD COLUMN max_turni_settimanali INT");
    }

    private static void migraTurniSeNecessario(Statement stmt) throws SQLException {
        boolean haBloccato = false;
        boolean haGeneratoAuto = false;
        boolean haPersonalizzato = false;
        boolean haPausaMinuti = false;
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM turni LIMIT 1")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String col = meta.getColumnName(i);
                if ("bloccato".equalsIgnoreCase(col)) haBloccato = true;
                else if ("generato_auto".equalsIgnoreCase(col)) haGeneratoAuto = true;
                else if ("personalizzato".equalsIgnoreCase(col)) haPersonalizzato = true;
                else if ("pausa_minuti".equalsIgnoreCase(col)) haPausaMinuti = true;
            }
        }
        if (!haBloccato) stmt.execute("ALTER TABLE turni ADD COLUMN bloccato BOOLEAN NOT NULL DEFAULT FALSE");
        if (!haGeneratoAuto) stmt.execute("ALTER TABLE turni ADD COLUMN generato_auto BOOLEAN NOT NULL DEFAULT FALSE");
        if (!haPersonalizzato) stmt.execute("ALTER TABLE turni ADD COLUMN personalizzato BOOLEAN NOT NULL DEFAULT FALSE");
        if (!haPausaMinuti) stmt.execute("ALTER TABLE turni ADD COLUMN pausa_minuti INT NOT NULL DEFAULT 0");
    }

    private static void migraRecensioniSeNecessario(Statement stmt) throws SQLException {
        boolean haFotoPath = false;
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM recensioni LIMIT 1")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if ("foto_path".equalsIgnoreCase(meta.getColumnName(i))) {
                    haFotoPath = true;
                    break;
                }
            }
        }
        if (!haFotoPath) {
            stmt.execute("ALTER TABLE recensioni ADD COLUMN foto_path VARCHAR(500)");
        }
    }

    private static void migraMenuGustiSeNecessario(Statement stmt) throws SQLException {
        boolean haCategoria = false;
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM menu_gusti LIMIT 1")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                if ("categoria".equalsIgnoreCase(md.getColumnName(i))) {
                    haCategoria = true;
                    break;
                }
            }
        }
        if (!haCategoria) {
            stmt.execute("ALTER TABLE menu_gusti ADD COLUMN categoria VARCHAR(40) NOT NULL DEFAULT 'Gelati'");
            stmt.execute("ALTER TABLE menu_gusti ADD COLUMN sottocategoria VARCHAR(40) NOT NULL DEFAULT 'Creme'");
            stmt.execute("ALTER TABLE menu_gusti ADD COLUMN immagine_url VARCHAR(500) NOT NULL DEFAULT ''");
            stmt.execute("UPDATE menu_gusti SET sottocategoria = 'Cioccolato' WHERE nome_gusto LIKE 'Fondente%'");
            stmt.execute("UPDATE menu_gusti SET sottocategoria = 'Frutta' "
                    + "WHERE nome_gusto LIKE 'Sorbetto%' OR nome_gusto LIKE 'Mango%'");
        }
    }

    private static void migraIngredientiSeNecessario(Statement stmt) throws SQLException {
        boolean haCategoria = false;
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM ingredienti LIMIT 1")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                if ("categoria".equalsIgnoreCase(md.getColumnName(i))) {
                    haCategoria = true;
                    break;
                }
            }
        }
        if (!haCategoria) {
            stmt.execute("ALTER TABLE ingredienti ADD COLUMN categoria VARCHAR(50) NOT NULL DEFAULT 'Laboratorio & Basi'");
        }
    }

    private static void migraOrdiniClientiSeNecessario(Statement stmt) {
        try {
            stmt.execute("ALTER TABLE ordini_clienti MODIFY COLUMN data_ritiro VARCHAR(512) NOT NULL");
            stmt.execute("ALTER TABLE ordini_clienti MODIFY COLUMN descrizione_dettagliata VARCHAR(2000) NOT NULL");
        } catch (SQLException ignored) {
            // Se gia' compatibile o non applicabile, prosegue senza bloccare l'avvio
        }
    }

    private static void popolaDatiIniziali(Connection conn, Statement stmt) throws SQLException {
        CatalogoMagazzino.popolaTabellaIngredienti(conn, stmt);

        boolean emergenzeVuote = true;
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM emergenze")) {
            if (rs.next()) {
                emergenzeVuote = rs.getInt(1) == 0;
            }
        }
        if (emergenzeVuote) {
            stmt.execute("INSERT INTO emergenze (codice_emergenza, codice_turno, email_richiedente, email_sostituto, motivazione, stato, nota_risposta) VALUES "
                    + "('EMG-01', 'TRN-03', 'mario@frostly.it', NULL, 'Visita medica improvvisa martedi mattina', 'IN_ATTESA', NULL)");
        }

        boolean recensioniVuote = true;
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM recensioni")) {
            if (rs.next()) {
                recensioniVuote = rs.getInt(1) == 0;
            }
        }
        if (recensioniVuote) {
            String insertRecSql = "INSERT INTO recensioni (codice_recensione, autore_email, autore_nome, voto_stelle, titolo, commento, data_ora, risposta_maestro, data_risposta, mi_piace) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertRecSql)) {
                ps.setString(1, "REC-01");
                ps.setString(2, "elena@cliente.it");
                ps.setString(3, "Elena Conti");
                ps.setInt(4, 5);
                ps.setString(5, "Pistacchio di Bronte e panna insuperabili!");
                ps.setString(6, "La consistenza del pistacchio di Bronte DOP è puro velluto, e la panna fresca d'alpeggio è la perfezione assoluta. Il miglior gelato di Roma!");
                ps.setString(7, "08/11/2026 16:30");
                ps.setString(8, "Cara Elena, grazie di cuore per le splendide parole! La tostatura lenta del pistacchio è la nostra passione dal 1984. A presto! - Stefania");
                ps.setString(9, "08/11/2026 18:00");
                ps.setInt(10, 14);
                ps.executeUpdate();

                ps.setString(1, "REC-02");
                ps.setString(2, "marco@cliente.it");
                ps.setString(3, "Marco Fabbri");
                ps.setInt(4, 5);
                ps.setString(5, "Cioccolata calda fondente sublime");
                ps.setString(6, "Ho provato la nuova cioccolata calda fondente con panna: densa, profumata e avvolgente. Servizio impeccabile e atmosfera calda.");
                ps.setString(7, "09/11/2026 17:15");
                ps.setString(8, "Grazie Marco! Selezioniamo solo cacao monorigine 70% dell'Ecuador. Ti aspettiamo per le altre specialità invernali!");
                ps.setString(9, "09/11/2026 19:20");
                ps.setInt(10, 9);
                ps.executeUpdate();

                ps.setString(1, "REC-03");
                ps.setString(2, "elena@cliente.it");
                ps.setString(3, "Elena Conti");
                ps.setInt(4, 5);
                ps.setString(5, "Crepe alla Nutella e panna d'asporto top");
                ps.setString(6, "Le crepes calde preparate al momento sono fragranti e farcite generosamente. Ottima anche la consegna a domicilio super puntuale!");
                ps.setString(7, "10/11/2026 09:45");
                ps.setString(8, null);
                ps.setString(9, null);
                ps.setInt(10, 6);
                ps.executeUpdate();
            }
        }

        boolean utentiVuoti = true;
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utenti")) {
            if (rs.next()) {
                utentiVuoti = rs.getInt(1) == 0;
            }
        }
        if (utentiVuoti) {
            String insertUsersSql = "INSERT IGNORE INTO "
                    + "utenti (email, nome, cognome, password, ruolo) VALUES "
                    + "('maestro@frostly.it', 'Stefania', 'Guga', 'admin123', 'MAESTRO_GELATAIO'),"
                    + "('mario@frostly.it', 'Mario', 'Rossi', 'pass123', 'DIPENDENTE'),"
                    + "('giulia@frostly.it', 'Giulia', 'Verdi', 'pass123', 'DIPENDENTE'),"
                    + "('luca@frostly.it', 'Luca', 'Neri', 'pass123', 'DIPENDENTE'),"
                    + "('elena@cliente.it', 'Elena', 'Conti', 'pass123', 'CLIENTE'),"
                    + "('marco@cliente.it', 'Marco', 'Fabbri', 'pass123', 'CLIENTE')";
            stmt.execute(insertUsersSql);
        } else {
            String ensureMaestroSql = "INSERT IGNORE INTO "
                    + "utenti (email, nome, cognome, password, ruolo) VALUES "
                    + "('maestro@frostly.it', 'Stefania', 'Guga', 'admin123', 'MAESTRO_GELATAIO')";
            stmt.execute(ensureMaestroSql);
        }

        boolean turniVuoti = true;
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM turni")) {
            if (rs.next()) {
                turniVuoti = rs.getInt(1) == 0;
            }
        }
        if (turniVuoti) {
            String insertTurniSql = "INSERT IGNORE INTO "
                    + "turni (codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato) VALUES "
                    + "('TRN-01', 'Lunedi', '08:00', '14:00', 'mario@frostly.it', 'PUBBLICATO'),"
                    + "('TRN-02', 'Lunedi', '14:00', '20:00', 'giulia@frostly.it', 'PUBBLICATO'),"
                    + "('TRN-03', 'Martedi', '08:00', '14:00', 'mario@frostly.it', 'IN_EMERGENZA')";
            stmt.execute(insertTurniSql);
        }

        CatalogoDemo.popolaTabellaMenuGusti(conn, stmt);
    }
}
