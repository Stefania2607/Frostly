package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.OrdineDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.OrdineCliente;

/**
 * Implementazione DBMS di OrdineDAO.
 */
public class OrdineDAODbms implements OrdineDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getInstance();

    @Override
    public List<OrdineCliente> caricaTuttiOrdini() {
        sincronizzaOrdiniDaDatabase();
        return cache.getOrdini();
    }

    @Override
    public void salvaOrdine(OrdineCliente ordine) {
        cache.salvaOrdine(ordine);
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
        }
    }

    private void sincronizzaOrdiniDaDatabase() {
        String sql = "SELECT codice_ordine, email_cliente, gusto_principale, peso_kg, data_ritiro, stato, prezzo_totale, descrizione_dettagliata FROM ordini_clienti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String codOrd = rs.getString("codice_ordine");
                    cache.sincronizzaOrdineDaDb(
                            codOrd,
                            rs.getString("email_cliente"),
                            rs.getString("gusto_principale"),
                            rs.getDouble("peso_kg"),
                            rs.getString("data_ritiro"),
                            rs.getDouble("prezzo_totale"),
                            rs.getString("descrizione_dettagliata"));
                    cache.aggiornaStatoOrdineDaDb(codOrd, rs.getString("stato"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }
}
