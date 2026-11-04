package dao;

import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.Emergenza;
import model.Gusto;
import model.Ingrediente;
import model.MaestroGelataio;
import model.Notifica;
import model.OrdineCliente;
import model.RichiestaGustoPersonalizzato;
import model.StatoTurno;
import model.Turno;

/**
 * Implementa direttamente l'interfaccia FrostlyDAO (nessuna doppia generalizzazione!).
 * Gestisce la persistenza su Database SQLite via JDBC.
 */
public class DbmsFrostlyDAO implements FrostlyDAO {

    private static DbmsFrostlyDAO instance;
    private final InMemoryFrostlyDAO cacheDatiBase = InMemoryFrostlyDAO.getInstance();

    private DbmsFrostlyDAO() {
    }

    public static synchronized DbmsFrostlyDAO getInstance() {
        if (instance == null) {
            instance = new DbmsFrostlyDAO();
        }
        return instance;
    }

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        cacheDatiBase.salvaTurno(turno);
        Connection conn = ConnectionManager.getInstance().getConnection();
        String deleteSql = "DELETE FROM turni WHERE codice_turno = ?";
        String insertSql = "INSERT INTO turni (codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement delStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
            delStmt.setString(1, turno.getCodiceTurno());
            delStmt.executeUpdate();

            insStmt.setString(1, turno.getCodiceTurno());
            insStmt.setString(2, turno.getData());
            insStmt.setString(3, turno.getOraInizio());
            insStmt.setString(4, turno.getOraFine());
            insStmt.setString(5, turno.getDipendenteAssegnato().getEmail());
            insStmt.setString(6, turno.getStato().name());
            insStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel salvataggio del turno.", e);
        }
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        Connection conn = ConnectionManager.getInstance().getConnection();
        String query = "SELECT codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato FROM turni";
        List<Turno> dalDb = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Dipendente dip = trovaDipendentePerEmail(rs.getString("email_dipendente"));
                if (dip != null) {
                    Turno t = new Turno(
                            rs.getString("codice_turno"),
                            rs.getString("data_turno"),
                            rs.getString("ora_inizio"),
                            rs.getString("ora_fine"),
                            dip);
                    t.setStato(StatoTurno.valueOf(rs.getString("stato")));
                    dalDb.add(t);
                }
            }
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel caricamento dei turni.", e);
        }
        return dalDb.isEmpty() ? cacheDatiBase.caricaTuttiTurni() : dalDb;
    }

    @Override
    public MaestroGelataio getMaestroGelataio() {
        return cacheDatiBase.getMaestroGelataio();
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        return cacheDatiBase.caricaTuttiDipendenti();
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        return cacheDatiBase.trovaDipendentePerEmail(email);
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        return cacheDatiBase.caricaTuttiClienti();
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        return cacheDatiBase.trovaClientePerEmail(email);
    }

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        return cacheDatiBase.caricaTutteEmergenze();
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
        cacheDatiBase.salvaEmergenza(emergenza);
    }

    @Override
    public List<Ingrediente> caricaTuttiIngredienti() {
        return cacheDatiBase.caricaTuttiIngredienti();
    }

    @Override
    public Ingrediente trovaIngredientePerNome(String nome) {
        return cacheDatiBase.trovaIngredientePerNome(nome);
    }

    @Override
    public void aggiornaIngrediente(Ingrediente ingrediente) {
        cacheDatiBase.aggiornaIngrediente(ingrediente);
    }

    @Override
    public List<Gusto> caricaTuttiGusti() {
        return cacheDatiBase.caricaTuttiGusti();
    }

    @Override
    public Gusto trovaGustoPerNome(String nome) {
        return cacheDatiBase.trovaGustoPerNome(nome);
    }

    @Override
    public List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto() {
        return cacheDatiBase.caricaTutteRichiesteGusto();
    }

    @Override
    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        cacheDatiBase.salvaRichiestaGusto(richiesta);
    }

    @Override
    public List<OrdineCliente> caricaTuttiOrdini() {
        return cacheDatiBase.caricaTuttiOrdini();
    }

    @Override
    public void salvaOrdine(OrdineCliente ordine) {
        cacheDatiBase.salvaOrdine(ordine);
    }

    @Override
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        return cacheDatiBase.caricaNotifichePerUtente(emailDestinatario);
    }

    @Override
    public void inviaNotifica(Notifica notifica) {
        cacheDatiBase.inviaNotifica(notifica);
    }
}