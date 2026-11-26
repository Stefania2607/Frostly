package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.EmergenzaDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.Emergenza;

/**
 * Implementazione DBMS di EmergenzaDAO.
 */
public class EmergenzaDAODbms implements EmergenzaDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getJdbcCacheInstance();

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        sincronizzaEmergenzeDaDatabase();
        return cache.getEmergenze();
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
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
            cache.salvaEmergenza(emergenza);
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    @Override
    public List<model.RichiestaIndisponibilita> caricaRichiesteIndisponibilita() {
        String sql = "SELECT codice_richiesta, email_dipendente, nome_dipendente, data_richiesta, motivazione, stato, nota_maestro "
                + "FROM richieste_indisponibilita";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                java.util.List<model.RichiestaIndisponibilita> dalDb = new java.util.ArrayList<>();
                while (rs.next()) {
                    java.time.LocalDate data = java.time.LocalDate.parse(rs.getString("data_richiesta"));
                    model.RichiestaIndisponibilita r = new model.RichiestaIndisponibilita(
                            rs.getString("codice_richiesta"),
                            rs.getString("email_dipendente"),
                            rs.getString("nome_dipendente"),
                            data,
                            rs.getString("motivazione"));
                    r.setStato(model.RichiestaIndisponibilita.StatoRichiesta.valueOf(rs.getString("stato")));
                    r.setNotaMaestro(rs.getString("nota_maestro"));
                    dalDb.add(r);
                    cache.salvaRichiestaIndisponibilita(r);
                }
                return dalDb;
            }
        } catch (Exception ignored) {
            return cache.getRichiesteIndisponibilita();
        }
    }

    @Override
    public void salvaRichiestaIndisponibilita(model.RichiestaIndisponibilita r) {
        if (r == null) {
            return;
        }
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String del = "DELETE FROM richieste_indisponibilita WHERE codice_richiesta = ?";
            String ins = "INSERT INTO richieste_indisponibilita "
                    + "(codice_richiesta, email_dipendente, nome_dipendente, data_richiesta, motivazione, stato, nota_maestro) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement dStmt = conn.prepareStatement(del);
                 PreparedStatement iStmt = conn.prepareStatement(ins)) {
                dStmt.setString(1, r.getCodiceRichiesta());
                dStmt.executeUpdate();

                iStmt.setString(1, r.getCodiceRichiesta());
                iStmt.setString(2, r.getEmailDipendente());
                iStmt.setString(3, r.getNomeDipendente());
                iStmt.setString(4, r.getDataRichiesta().toString());
                iStmt.setString(5, r.getMotivazione());
                iStmt.setString(6, r.getStato().name());
                iStmt.setString(7, r.getNotaMaestro());
                iStmt.executeUpdate();
            }
            cache.salvaRichiestaIndisponibilita(r);
        } catch (FrostlyException | SQLException ignored) {
            cache.salvaRichiestaIndisponibilita(r);
        }
    }

    private void sincronizzaEmergenzeDaDatabase() {
        String sql = "SELECT codice_emergenza, codice_turno, email_richiedente, email_sostituto, motivazione, stato, nota_risposta FROM emergenze";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cache.sincronizzaEmergenzaDaDb(
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
}
