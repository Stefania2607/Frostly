package dao;

import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Dipendente;
import model.StatoTurno;
import model.Turno;

/**
 * Versione DBMS (SQLite JDBC) del DAO per la modalita' FULL_DBMS.
 */
public class DbmsFrostlyDAO extends InMemoryFrostlyDAO {

    private static DbmsFrostlyDAO instance;

    private DbmsFrostlyDAO() {
        super();
    }

    public static synchronized DbmsFrostlyDAO getInstance() {
        if (instance == null) {
            instance = new DbmsFrostlyDAO();
        }
        return instance;
    }

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        super.salvaTurno(turno);
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
        return dalDb.isEmpty() ? super.caricaTuttiTurni() : dalDb;
    }
}