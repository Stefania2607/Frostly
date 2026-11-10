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

    private final ArchivioMemoria cache = ArchivioMemoria.getInstance();

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        sincronizzaEmergenzeDaDatabase();
        return cache.getEmergenze();
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
        cache.salvaEmergenza(emergenza);
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
            // Mantiene in cache
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
