package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.RecensioneDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Recensione;

/**
 * Implementazione JDBC / DBMS del DAO Recensioni.
 * Utilizza query SQL parametrizzate tramite PreparedStatement.
 */
public class RecensioneDAODbms implements RecensioneDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getJdbcCacheInstance();

    public RecensioneDAODbms() {
    }

    @Override
    public List<Recensione> trovaTutteRecensioni() throws FrostlyException {
        List<Recensione> lista = new ArrayList<>();
        String sql = "SELECT codice_recensione, autore_email, autore_nome, voto_stelle, "
                + "titolo, commento, data_ora, risposta_maestro, data_risposta, mi_piace, foto_path "
                + "FROM recensioni ORDER BY codice_recensione DESC";
        Connection conn = ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Recensione r = new Recensione(
                        rs.getString("codice_recensione"),
                        rs.getString("autore_email"),
                        rs.getString("autore_nome"),
                        rs.getInt("voto_stelle"),
                        rs.getString("titolo"),
                        rs.getString("commento"),
                        rs.getString("data_ora"),
                        rs.getString("risposta_maestro"),
                        rs.getString("data_risposta"),
                        rs.getInt("mi_piace"),
                        rs.getString("foto_path"));
                lista.add(r);
                cache.sincronizzaRecensioneDaDb(
                        r.getCodiceRecensione(), r.getAutoreEmail(), r.getAutoreNome(),
                        r.getVotoStelle(), r.getTitolo(), r.getCommento(), r.getDataOra(),
                        r.getRispostaMaestro(), r.getDataRisposta(), r.getMiPiace(), r.getFotoPath());
            }
            return lista;
        } catch (SQLException e) {
            java.util.logging.Logger.getLogger(RecensioneDAODbms.class.getName())
                    .log(java.util.logging.Level.WARNING, "Errore durante il recupero delle recensioni da DB: {0}", e.getMessage());
            return cache.getRecensioni();
        }
    }

    @Override
    public Recensione salvaNuovaRecensione(Recensione r) throws FrostlyException {
        if (r.getCodiceRecensione() == null || r.getCodiceRecensione().isEmpty()) {
            r.setCodiceRecensione("REC-" + System.currentTimeMillis());
        }

        String sql = "INSERT INTO recensioni (codice_recensione, autore_email, autore_nome, voto_stelle, "
                + "titolo, commento, data_ora, risposta_maestro, data_risposta, mi_piace, foto_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCodiceRecensione());
            ps.setString(2, r.getAutoreEmail());
            ps.setString(3, r.getAutoreNome());
            ps.setInt(4, r.getVotoStelle());
            ps.setString(5, r.getTitolo());
            ps.setString(6, r.getCommento());
            ps.setString(7, r.getDataOra());
            ps.setString(8, r.getRispostaMaestro());
            ps.setString(9, r.getDataRisposta());
            ps.setInt(10, r.getMiPiace());
            ps.setString(11, r.getFotoPath());
            ps.executeUpdate();
            cache.salvaRecensione(r);
            return r;
        } catch (SQLException e) {
            throw new FrostlyException("Impossibile salvare la recensione nel database.", e);
        }
    }

    @Override
    public void aggiornaRispostaMaestro(String codiceRecensione, String rispostaMaestro, String dataRisposta) throws FrostlyException {
        String sql = "UPDATE recensioni SET risposta_maestro = ?, data_risposta = ? WHERE codice_recensione = ?";
        Connection conn = ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rispostaMaestro);
            ps.setString(2, dataRisposta);
            ps.setString(3, codiceRecensione);
            ps.executeUpdate();
            cache.rispondiRecensione(codiceRecensione, rispostaMaestro, dataRisposta);
        } catch (SQLException e) {
            throw new FrostlyException("Impossibile salvare la risposta del Maestro nel database.", e);
        }
    }

    @Override
    public void incrementaMiPiace(String codiceRecensione) throws FrostlyException {
        String sql = "UPDATE recensioni SET mi_piace = mi_piace + 1 WHERE codice_recensione = ?";
        Connection conn = ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceRecensione);
            ps.executeUpdate();
            cache.incrementaMiPiaceRecensione(codiceRecensione);
        } catch (SQLException e) {
            throw new FrostlyException("Impossibile aggiornare i mi piace nel database.", e);
        }
    }
}
