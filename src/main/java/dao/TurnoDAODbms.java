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
 * Implementazione DBMS di TurnoDAO su database MySQL tramite ConnectionManager.
 */
public class TurnoDAODbms implements TurnoDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getJdbcCacheInstance();

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        Connection conn = ConnectionManager.getInstance().getConnection();
        String deleteSql = "DELETE FROM turni WHERE codice_turno = ?";
        String insertSql = "INSERT INTO turni (codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato, bloccato, generato_auto, personalizzato, pausa_minuti) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            insStmt.setBoolean(7, turno.isBloccato());
            insStmt.setBoolean(8, turno.isGeneratoAutomaticamente());
            insStmt.setBoolean(9, turno.isPersonalizzato());
            insStmt.setInt(10, turno.getPausaMinuti());
            insStmt.executeUpdate();
            cache.salvaTurno(turno);
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel salvataggio del turno.", e);
        }
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        new dao.dbms.UtenteDAODbms().caricaTuttiDipendenti();
        Connection conn = ConnectionManager.getInstance().getConnection();
        String query = "SELECT codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato, bloccato, generato_auto, personalizzato, pausa_minuti FROM turni";
        List<Turno> dalDb = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String emailDip = rs.getString("email_dipendente");
                Dipendente dip = cache.trovaDipendentePerEmail(emailDip);
                if (dip != null) {
                    Turno t = new Turno(
                            rs.getString("codice_turno"),
                            rs.getString("data_turno"),
                            rs.getString("ora_inizio"),
                            rs.getString("ora_fine"),
                            dip);
                    t.setStato(StatoTurno.valueOf(rs.getString("stato")));
                    t.setBloccato(rs.getBoolean("bloccato"));
                    t.setGeneratoAutomaticamente(rs.getBoolean("generato_auto"));
                    t.setPersonalizzato(rs.getBoolean("personalizzato"));
                    t.setPausaMinuti(rs.getInt("pausa_minuti"));
                    dalDb.add(t);
                    cache.salvaTurno(t);
                }
            }
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel caricamento dei turni.", e);
        }
        return dalDb;
    }

    @Override
    public void sostituisciTurniAtomico(List<String> codiciDaRimuovere, List<Turno> turniDaSalvare)
            throws FrostlyException {
        Connection conn = ConnectionManager.getInstance().getConnection();
        boolean prevAutoCommit = true;
        try {
            prevAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (codiciDaRimuovere != null) {
                try (PreparedStatement delEm = conn.prepareStatement("DELETE FROM emergenze WHERE codice_turno = ?");
                     PreparedStatement delT = conn.prepareStatement("DELETE FROM turni WHERE codice_turno = ?")) {
                    for (String cod : codiciDaRimuovere) {
                        delEm.setString(1, cod);
                        delEm.executeUpdate();
                        delT.setString(1, cod);
                        delT.executeUpdate();
                    }
                }
            }

            if (turniDaSalvare != null) {
                String delSql = "DELETE FROM turni WHERE codice_turno = ?";
                String insSql = "INSERT INTO turni (codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato, bloccato, generato_auto, personalizzato, pausa_minuti) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement delStmt = conn.prepareStatement(delSql);
                     PreparedStatement insStmt = conn.prepareStatement(insSql)) {
                    for (Turno turno : turniDaSalvare) {
                        delStmt.setString(1, turno.getCodiceTurno());
                        delStmt.executeUpdate();

                        insStmt.setString(1, turno.getCodiceTurno());
                        insStmt.setString(2, turno.getData());
                        insStmt.setString(3, turno.getOraInizio());
                        insStmt.setString(4, turno.getOraFine());
                        insStmt.setString(5, turno.getDipendenteAssegnato().getEmail());
                        insStmt.setString(6, turno.getStato().name());
                        insStmt.setBoolean(7, turno.isBloccato());
                        insStmt.setBoolean(8, turno.isGeneratoAutomaticamente());
                        insStmt.setBoolean(9, turno.isPersonalizzato());
                        insStmt.setInt(10, turno.getPausaMinuti());
                        insStmt.executeUpdate();
                    }
                }
            }

            conn.commit();

            if (codiciDaRimuovere != null) {
                for (String cod : codiciDaRimuovere) {
                    cache.eliminaTurno(cod);
                }
            }
            if (turniDaSalvare != null) {
                for (Turno turno : turniDaSalvare) {
                    cache.salvaTurno(turno);
                }
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // Rollback best effort
            }
            throw new FrostlyException("Errore JDBC durante il salvataggio atomico della settimana.", e);
        } finally {
            try {
                conn.setAutoCommit(prevAutoCommit);
            } catch (SQLException ignored) {
                // Ripristino autoCommit
            }
        }
    }

    @Override
    public java.util.Map<String, model.FabbisognoTurno> caricaFabbisogni() throws FrostlyException {
        java.util.Map<String, model.FabbisognoTurno> result = TurnoDAO.super.caricaFabbisogni();
        Connection conn = ConnectionManager.getInstance().getConnection();
        String query = "SELECT chiave, giorno, indice_fascia, totale_richiesto, min_banco, min_laboratorio, min_responsabile FROM fabbisogni_turni";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int gVal = rs.getInt("giorno");
                int fascia = rs.getInt("indice_fascia");
                if (gVal >= 1 && gVal <= 7 && fascia >= 0 && fascia < 5) {
                    java.time.DayOfWeek g = java.time.DayOfWeek.of(gVal);
                    model.FabbisognoTurno fab = new model.FabbisognoTurno(g, fascia, rs.getInt("totale_richiesto"));
                    fab.setMinimoMansione(model.MansioneDipendente.BANCO, rs.getInt("min_banco"));
                    fab.setMinimoMansione(model.MansioneDipendente.LABORATORIO, rs.getInt("min_laboratorio"));
                    fab.setMinimoMansione(model.MansioneDipendente.RESPONSABILE, rs.getInt("min_responsabile"));
                    result.put(model.FabbisognoTurno.chiave(g, fascia), fab);
                }
            }
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel caricamento dei fabbisogni di copertura.", e);
        }
        return result;
    }

    @Override
    public void salvaFabbisogni(java.util.Map<String, model.FabbisognoTurno> fabbisogni) throws FrostlyException {
        if (fabbisogni == null) {
            return;
        }
        Connection conn = ConnectionManager.getInstance().getConnection();
        String delSql = "DELETE FROM fabbisogni_turni WHERE chiave = ?";
        String insSql = "INSERT INTO fabbisogni_turni (chiave, giorno, indice_fascia, totale_richiesto, min_banco, min_laboratorio, min_responsabile) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement delStmt = conn.prepareStatement(delSql);
             PreparedStatement insStmt = conn.prepareStatement(insSql)) {
            for (model.FabbisognoTurno fab : fabbisogni.values()) {
                String chiave = model.FabbisognoTurno.chiave(fab.getGiorno(), fab.getIndiceFascia());
                delStmt.setString(1, chiave);
                delStmt.executeUpdate();

                insStmt.setString(1, chiave);
                insStmt.setInt(2, fab.getGiorno().getValue());
                insStmt.setInt(3, fab.getIndiceFascia());
                insStmt.setInt(4, fab.getTotaleRichiesto());
                insStmt.setInt(5, fab.getMinimoMansione(model.MansioneDipendente.BANCO));
                insStmt.setInt(6, fab.getMinimoMansione(model.MansioneDipendente.LABORATORIO));
                insStmt.setInt(7, fab.getMinimoMansione(model.MansioneDipendente.RESPONSABILE));
                insStmt.executeUpdate();
            }
            cache.salvaFabbisogniTurni(fabbisogni);
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel salvataggio dei fabbisogni di copertura.", e);
        }
    }

    @Override
    public void eliminaTurno(String codiceTurno) throws FrostlyException {
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
            cache.eliminaTurno(codiceTurno);
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nell'eliminazione del turno.", e);
        }
    }
}
