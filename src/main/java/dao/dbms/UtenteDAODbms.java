package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.UtenteDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.MaestroGelataio;

/**
 * Implementazione DBMS di UtenteDAO.
 * Gestisce l'interazione con la tabella 'utenti' e sincronizza la cache in memoria.
 */
public class UtenteDAODbms implements UtenteDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getInstance();

    @Override
    public MaestroGelataio getMaestroGelataio() {
        sincronizzaUtentiDaDatabase();
        return cache.getMaestroGelataio();
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        sincronizzaUtentiDaDatabase();
        return cache.getDipendenti();
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        sincronizzaUtentiDaDatabase();
        return cache.trovaDipendentePerEmail(email);
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        sincronizzaUtentiDaDatabase();
        return cache.getClienti();
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        sincronizzaUtentiDaDatabase();
        return cache.trovaClientePerEmail(email);
    }

    @Override
    public void eliminaDipendente(String email) throws FrostlyException {
        cache.eliminaDipendente(email);
        Connection conn = ConnectionManager.getInstance().getConnection();
        try {
            try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM emergenze WHERE email_richiedente = ?")) {
                s1.setString(1, email);
                s1.executeUpdate();
            }
            try (PreparedStatement s2 = conn.prepareStatement("DELETE FROM turni WHERE email_dipendente = ?")) {
                s2.setString(1, email);
                s2.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM utenti WHERE email = ? AND ruolo = 'DIPENDENTE'")) {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nell'eliminazione del dipendente da database.", e);
        }
    }

    @Override
    public void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo)
            throws FrostlyException {
        sincronizzaUtentiDaDatabase();
        cache.registraNuovoUtente(nome, cognome, email, password, ruolo);

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

    private void sincronizzaUtentiDaDatabase() {
        String query = "SELECT email, nome, cognome, password, ruolo FROM utenti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                java.util.List<Dipendente> dipendentiDb = new java.util.ArrayList<>();
                java.util.List<Cliente> clientiDb = new java.util.ArrayList<>();
                while (rs.next()) {
                    String ruolo = rs.getString("ruolo");
                    String email = rs.getString("email");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String pass = rs.getString("password");
                    if ("DIPENDENTE".equalsIgnoreCase(ruolo)) {
                        dipendentiDb.add(new Dipendente(nome, cognome, email, pass));
                    } else if ("CLIENTE".equalsIgnoreCase(ruolo)) {
                        clientiDb.add(new Cliente(nome, cognome, email, pass));
                    } else if ("MAESTRO_GELATAIO".equalsIgnoreCase(ruolo)) {
                        cache.setMaestroGelataio(new MaestroGelataio(nome, cognome, email, pass));
                    }
                }
                cache.impostaUtentiDaDb(dipendentiDb, clientiDb);
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene i dati base della cache in caso di fallimento connessione
        }
    }
}
