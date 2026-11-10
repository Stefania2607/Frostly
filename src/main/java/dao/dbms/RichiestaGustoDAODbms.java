package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.RichiestaGustoDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.RichiestaGustoPersonalizzato;

/**
 * Implementazione DBMS di RichiestaGustoDAO.
 */
public class RichiestaGustoDAODbms implements RichiestaGustoDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getInstance();

    @Override
    public List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto() {
        sincronizzaRichiesteGustoDaDatabase();
        return cache.getRichiesteGusto();
    }

    @Override
    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        cache.salvaRichiestaGusto(richiesta);
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
            // Mantiene in memoria
        }
    }

    private void sincronizzaRichiesteGustoDaDatabase() {
        String sql = "SELECT codice_richiesta, email_cliente, nome_gusto, ingrediente_principale, quantita_kg, data_ritiro, note_cliente, stato, risposta_maestro FROM richieste_gusto";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String codRich = rs.getString("codice_richiesta");
                    cache.sincronizzaRichiestaGustoDaDb(
                            codRich,
                            rs.getString("email_cliente"),
                            rs.getString("nome_gusto"),
                            rs.getString("ingrediente_principale"),
                            rs.getDouble("quantita_kg"),
                            rs.getString("data_ritiro"),
                            rs.getString("note_cliente"));
                    cache.aggiornaStatoRichiestaGustoDaDb(
                            codRich,
                            rs.getString("stato"),
                            rs.getString("risposta_maestro"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }
}
