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
 * Prima versione persistente di TurnoDAO: salva e recupera l'Entity Turno su DBMS (SQLite JDBC)
 * usando l'unica connessione aperta da ConnectionManager.
 */
public class TurnoDAODbms implements TurnoDAO {

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        ArchivioMemoria.getInstance().salvaTurno(turno);
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
        new dao.dbms.UtenteDAODbms().caricaTuttiDipendenti();
        Connection conn = ConnectionManager.getInstance().getConnection();
        String query = "SELECT codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato FROM turni";
        List<Turno> dalDb = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String emailDip = rs.getString("email_dipendente");
                Dipendente dip = ArchivioMemoria.getInstance().trovaDipendentePerEmail(emailDip);
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
        return dalDb;
    }

    @Override
    public void eliminaTurno(String codiceTurno) throws FrostlyException {
        ArchivioMemoria.getInstance().eliminaTurno(codiceTurno);
        Connection conn = ConnectionManager.getInstance().getConnection();
        try {
            try (PreparedStatement delEm = conn.prepareStatement("DELETE FROM emergenze WHERE codice_turno = ?")) {
                delEm.setString(1, codiceTurno);
                delEm.executeUpdate();
            }
            try (PreparedStatement delT = conn.prepareStatement("DELETE FROM turni WHERE codice_turno = ?")) {
                delT.setString(1, codiceTurno);
                delT.executeUpdate();
            }
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nell'eliminazione del turno.", e);
        }
    }
}
