package dao;

import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.*;

/**
 * Implementazione FULL di FrostlyDAO collegata al Database MySQL.
 * Gestisce utenti, menu gusti ('menu_gusti'), turni, emergenze e richieste gusto su DB.
 */
public class DbmsFrostlyDAO implements FrostlyDAO {

    private static DbmsFrostlyDAO instance;
    private final InMemoryFrostlyDAO cacheDatiBase = InMemoryFrostlyDAO.getInstance();

    private DbmsFrostlyDAO() {
        sincronizzaUtentiDaDatabase();
        sincronizzaMenuDaDatabase();
        sincronizzaIngredientiDaDatabase();
        sincronizzaEmergenzeDaDatabase();
        sincronizzaRichiesteGustoDaDatabase();
        sincronizzaOrdiniDaDatabase();
        sincronizzaNotificheDaDatabase();
    }

    public static synchronized DbmsFrostlyDAO getInstance() {
        if (instance == null) {
            instance = new DbmsFrostlyDAO();
        }
        return instance;
    }

    private void sincronizzaUtentiDaDatabase() {
        String query = "SELECT email, nome, cognome, password, ruolo FROM utenti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cacheDatiBase.sincronizzaUtenteDaDb(
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("ruolo"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
            // Se la tabella non risponde mantiene i dati base
        }
    }

        private void sincronizzaIngredientiDaDatabase() {
        String sql = "SELECT nome, quantita_disponibile, soglia_minima, unita_misura FROM ingredienti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cacheDatiBase.sincronizzaIngredienteDaDb(
                            rs.getString("nome"),
                            rs.getDouble("quantita_disponibile"),
                            rs.getDouble("soglia_minima"),
                            rs.getString("unita_misura"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    private void sincronizzaEmergenzeDaDatabase() {
        String sql = "SELECT codice_emergenza, codice_turno, email_richiedente, email_sostituto, motivazione, stato, nota_risposta FROM emergenze";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cacheDatiBase.sincronizzaEmergenzaDaDb(
                            rs.getString("codice_emergenza"),
                            rs.getString("codice_turno"),
                            rs.getString("email_richiedente"),
                            rs.getString("email_sostituto"),
                            rs.getString("motivazione"),
                            rs.getString("stato"),
                            rs.getString("nota_risposta"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    private void sincronizzaMenuDaDatabase() {
        String query = "SELECT nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria, immagine_url FROM menu_gusti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cacheDatiBase.sincronizzaGustoDaDb(
                            rs.getString("nome_gusto"),
                            rs.getString("descrizione"),
                            rs.getString("allergeni"),
                            rs.getDouble("prezzo_al_kg"),
                            rs.getBoolean("disponibile"),
                            rs.getString("categoria"),
                            rs.getString("sottocategoria"),
                            rs.getString("immagine_url"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene i gusti in memoria in caso di assenza connessione
        }
    }

        @Override
    public void eliminaDipendente(String email) throws FrostlyException {
        cacheDatiBase.eliminaDipendente(email);
        Connection conn = ConnectionManager.getInstance().getConnection();
        String sql = "DELETE FROM utenti WHERE email = ? AND ruolo = 'DIPENDENTE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nell'eliminazione del dipendente da database.", e);
        }
    }

    @Override
    public void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo)
            throws FrostlyException {
        sincronizzaUtentiDaDatabase();
        cacheDatiBase.registraNuovoUtente(nome, cognome, email, password, ruolo);

        Connection conn = ConnectionManager.getInstance().getConnection();
        String insertSql = "INSERT INTO utenti (email, nome, cognome, password, ruolo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, email);
            stmt.setString(2, nome);
            stmt.setString(3, cognome);
            stmt.setString(4, password);
            stmt.setString(5, ruolo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nella registrazione dell'utente su database.", e);
        }
    }

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        new TurnoDAODbms().salvaTurno(turno);
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        return new TurnoDAODbms().caricaTuttiTurni();
    }

    @Override
    public MaestroGelataio getMaestroGelataio() {
        return cacheDatiBase.getMaestroGelataio();
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        sincronizzaUtentiDaDatabase();
        return cacheDatiBase.caricaTuttiDipendenti();
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        sincronizzaUtentiDaDatabase();
        return cacheDatiBase.trovaDipendentePerEmail(email);
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        sincronizzaUtentiDaDatabase();
        return cacheDatiBase.caricaTuttiClienti();
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        sincronizzaUtentiDaDatabase();
        return cacheDatiBase.trovaClientePerEmail(email);
    }

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        sincronizzaEmergenzeDaDatabase();
        return cacheDatiBase.caricaTutteEmergenze();
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
        cacheDatiBase.salvaEmergenza(emergenza);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String del = "DELETE FROM emergenze WHERE codice_emergenza = ?";
            String ins = "INSERT INTO emergenze (codice_emergenza, codice_turno, email_richiedente, email_sostituto, motivazione, stato, nota_risposta) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement dStmt = conn.prepareStatement(del);
                 PreparedStatement iStmt = conn.prepareStatement(ins)) {
                dStmt.setString(1, emergenza.getCodiceEmergenza());
                dStmt.executeUpdate();

                iStmt.setString(1, emergenza.getCodiceEmergenza());
                iStmt.setString(2, emergenza.getTurno().getCodiceTurno());
                iStmt.setString(3, emergenza.getRichiedente().getEmail());
                iStmt.setString(4, emergenza.getSostituto() != null ? emergenza.getSostituto().getEmail() : null);
                iStmt.setString(5, emergenza.getMotivazione());
                iStmt.setString(6, emergenza.getStato().name());
                iStmt.setString(7, emergenza.getNotaRisposta());
                iStmt.executeUpdate();
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene comunque il dato in memoria
        }
    }

    @Override
    public List<Ingrediente> caricaTuttiIngredienti() {
        sincronizzaIngredientiDaDatabase();
        return cacheDatiBase.caricaTuttiIngredienti();
    }

    @Override
    public Ingrediente trovaIngredientePerNome(String nome) {
        sincronizzaIngredientiDaDatabase();
        return cacheDatiBase.trovaIngredientePerNome(nome);
    }

    @Override
    public void aggiornaIngrediente(Ingrediente ingrediente) {
        cacheDatiBase.aggiornaIngrediente(ingrediente);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String sql = "UPDATE ingredienti SET quantita_disponibile = ? WHERE nome = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, ingrediente.getQuantitaDisponibile());
                stmt.setString(2, ingrediente.getNome());
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    String ins = "INSERT INTO ingredienti (nome, quantita_disponibile, soglia_minima, unita_misura) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement iStmt = conn.prepareStatement(ins)) {
                        iStmt.setString(1, ingrediente.getNome());
                        iStmt.setDouble(2, ingrediente.getQuantitaDisponibile());
                        iStmt.setDouble(3, ingrediente.getSogliaMinima());
                        iStmt.setString(4, ingrediente.getUnitaMisura());
                        iStmt.executeUpdate();
                    }
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    @Override
    public List<Gusto> caricaTuttiGusti() {
        sincronizzaMenuDaDatabase();
        return cacheDatiBase.caricaTuttiGusti();
    }

    @Override
    public Gusto trovaGustoPerNome(String nome) {
        sincronizzaMenuDaDatabase();
        return cacheDatiBase.trovaGustoPerNome(nome);
    }

    @Override
    public void salvaGustoMenu(Gusto gusto) throws FrostlyException {
        cacheDatiBase.salvaGustoMenu(gusto);
        Connection conn = ConnectionManager.getInstance().getConnection();
        String del = "DELETE FROM menu_gusti WHERE nome_gusto = ?";
        String ins = "INSERT INTO menu_gusti (nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria, immagine_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dStmt = conn.prepareStatement(del);
             PreparedStatement iStmt = conn.prepareStatement(ins)) {
            dStmt.setString(1, gusto.getNome());
            dStmt.executeUpdate();

            iStmt.setString(1, gusto.getNome());
            iStmt.setString(2, gusto.getDescrizione());
            iStmt.setString(3, gusto.getAllergeni());
            iStmt.setDouble(4, gusto.getPrezzoAlKg());
            iStmt.setBoolean(5, gusto.isDisponibileOggi());
            iStmt.setString(6, gusto.getCategoria());
            iStmt.setString(7, gusto.getSottocategoria());
            iStmt.setString(8, gusto.getImmagineUrl() == null ? "" : gusto.getImmagineUrl());
            iStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel salvataggio del gusto nel menu su database.", e);
        }
    }

    @Override
    public void rimuoviGustoMenu(String nomeGusto) throws FrostlyException {
        cacheDatiBase.rimuoviGustoMenu(nomeGusto);
        Connection conn = ConnectionManager.getInstance().getConnection();
        String del = "DELETE FROM menu_gusti WHERE nome_gusto = ?";
        try (PreparedStatement dStmt = conn.prepareStatement(del)) {
            dStmt.setString(1, nomeGusto);
            dStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nella rimozione del gusto dal menu su database.", e);
        }
    }

    @Override
    public List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto() {
        sincronizzaRichiesteGustoDaDatabase();
        return cacheDatiBase.caricaTutteRichiesteGusto();
    }

    @Override
    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        cacheDatiBase.salvaRichiestaGusto(richiesta);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String del = "DELETE FROM richieste_gusto WHERE codice_richiesta = ?";
            String ins = "INSERT INTO richieste_gusto (codice_richiesta, email_cliente, nome_gusto, ingrediente_principale, quantita_kg, data_ritiro, note_cliente, stato, risposta_maestro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement dStmt = conn.prepareStatement(del);
                 PreparedStatement iStmt = conn.prepareStatement(ins)) {
                dStmt.setString(1, richiesta.getCodiceRichiesta());
                dStmt.executeUpdate();

                iStmt.setString(1, richiesta.getCodiceRichiesta());
                iStmt.setString(2, richiesta.getCliente() != null ? richiesta.getCliente().getEmail() : "");
                iStmt.setString(3, richiesta.getNomeGustoProposto());
                iStmt.setString(4, richiesta.getIngredientePrincipale() != null ? richiesta.getIngredientePrincipale().getNome() : "");
                iStmt.setDouble(5, richiesta.getQuantitaRichiestaKg());
                iStmt.setString(6, richiesta.getDataRitiroDesiderata());
                iStmt.setString(7, richiesta.getNoteCliente() != null ? richiesta.getNoteCliente() : "");
                iStmt.setString(8, richiesta.getStato() != null ? richiesta.getStato().name() : "IN_ATTESA");
                iStmt.setString(9, richiesta.getRispostaMaestro() != null ? richiesta.getRispostaMaestro() : "");
                iStmt.executeUpdate();
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene in memoria se DB non raggiungibile
        }
    }

    @Override
    public List<OrdineCliente> caricaTuttiOrdini() {
        sincronizzaOrdiniDaDatabase();
        return cacheDatiBase.caricaTuttiOrdini();
    }

    @Override
    public void salvaOrdine(OrdineCliente ordine) {
        cacheDatiBase.salvaOrdine(ordine);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String del = "DELETE FROM ordini_clienti WHERE codice_ordine = ?";
            String ins = "INSERT INTO ordini_clienti (codice_ordine, email_cliente, gusto_principale, peso_kg, data_ritiro, stato, prezzo_totale, descrizione_dettagliata) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement dStmt = conn.prepareStatement(del);
                 PreparedStatement iStmt = conn.prepareStatement(ins)) {
                dStmt.setString(1, ordine.getCodiceOrdine());
                dStmt.executeUpdate();

                iStmt.setString(1, ordine.getCodiceOrdine());
                iStmt.setString(2, ordine.getCliente() != null ? ordine.getCliente().getEmail() : "");
                iStmt.setString(3, ordine.getGusto() != null ? ordine.getGusto().getNome() : "");
                iStmt.setDouble(4, ordine.getPesoKg());
                iStmt.setString(5, ordine.getDataRitiro() != null ? ordine.getDataRitiro() : "");
                iStmt.setString(6, ordine.getStato() != null ? ordine.getStato().name() : "IN_ATTESA");
                iStmt.setDouble(7, ordine.getPrezzoTotale());
                iStmt.setString(8, ordine.getDescrizioneDettagliata() != null ? ordine.getDescrizioneDettagliata() : "");
                iStmt.executeUpdate();
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene in memoria se DB non raggiungibile
        }
    }

    @Override
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        sincronizzaNotificheDaDatabase();
        return cacheDatiBase.caricaNotifichePerUtente(emailDestinatario);
    }

    @Override
    public void inviaNotifica(Notifica notifica) {
        cacheDatiBase.inviaNotifica(notifica);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String ins = "INSERT INTO notifiche_utenti (email_destinatario, titolo, messaggio, timestamp_notifica) VALUES (?, ?, ?, ?)";
            try (PreparedStatement iStmt = conn.prepareStatement(ins)) {
                iStmt.setString(1, notifica.getEmailDestinatario());
                iStmt.setString(2, notifica.getTitolo());
                iStmt.setString(3, notifica.getMessaggio());
                iStmt.setString(4, notifica.getTimestamp());
                iStmt.executeUpdate();
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene in memoria se DB non raggiungibile
        }
    }

    private void sincronizzaRichiesteGustoDaDatabase() {
        String sql = "SELECT codice_richiesta, email_cliente, nome_gusto, ingrediente_principale, quantita_kg, data_ritiro, note_cliente, stato, risposta_maestro FROM richieste_gusto";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cacheDatiBase.sincronizzaRichiestaGustoDaDb(
                            rs.getString("codice_richiesta"),
                            rs.getString("email_cliente"),
                            rs.getString("nome_gusto"),
                            rs.getString("ingrediente_principale"),
                            rs.getDouble("quantita_kg"),
                            rs.getString("data_ritiro"),
                            rs.getString("note_cliente"),
                            rs.getString("stato"),
                            rs.getString("risposta_maestro"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    private void sincronizzaOrdiniDaDatabase() {
        String sql = "SELECT codice_ordine, email_cliente, gusto_principale, peso_kg, data_ritiro, stato, prezzo_totale, descrizione_dettagliata FROM ordini_clienti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cacheDatiBase.sincronizzaOrdineDaDb(
                            rs.getString("codice_ordine"),
                            rs.getString("email_cliente"),
                            rs.getString("gusto_principale"),
                            rs.getDouble("peso_kg"),
                            rs.getString("data_ritiro"),
                            rs.getString("stato"),
                            rs.getDouble("prezzo_totale"),
                            rs.getString("descrizione_dettagliata"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    private void sincronizzaNotificheDaDatabase() {
        String sql = "SELECT email_destinatario, titolo, messaggio, timestamp_notifica FROM notifiche_utenti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cacheDatiBase.sincronizzaNotificaDaDb(
                            rs.getString("email_destinatario"),
                            rs.getString("titolo"),
                            rs.getString("messaggio"),
                            rs.getString("timestamp_notifica"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }
}
