package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.GustoDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.Gusto;

/**
 * Implementazione DBMS di GustoDAO.
 */
public class GustoDAODbms implements GustoDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getInstance();

    @Override
    public List<Gusto> caricaTuttiGusti() {
        sincronizzaMenuDaDatabase();
        return cache.getGusti();
    }

    @Override
    public Gusto trovaGustoPerNome(String nome) {
        sincronizzaMenuDaDatabase();
        return cache.trovaGustoPerNome(nome);
    }

    @Override
    public void salvaGustoMenu(Gusto gusto) throws FrostlyException {
        cache.salvaGustoMenu(gusto);
        Connection conn = ConnectionManager.getInstance().getConnection();
        String del = "DELETE FROM menu_gusti WHERE nome_gusto = ?";
        String ins = "INSERT INTO menu_gusti (nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria, immagine_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dStmt = conn.prepareStatement(del);
             PreparedStatement iStmt = conn.prepareStatement(ins)) {
            dStmt.setString(1, gusto.getNome());
            dStmt.executeUpdate();

            iStmt.setString(1, gusto.getNome());
            iStmt.setString(2, gusto.getDescrizione());
            iStmt.setString(3, gusto.getAllergeni());
            iStmt.setDouble(4, gusto.getPrezzoAlKg());
            iStmt.setBoolean(5, gusto.isDisponibileOggi());
            iStmt.setString(6, gusto.getCategoria());
            iStmt.setString(7, gusto.getSottocategoria());
            iStmt.setString(8, gusto.getImmagineUrl() == null ? "" : gusto.getImmagineUrl());
            iStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel salvataggio del gusto nel menu su database.", e);
        }
    }

    @Override
    public void rimuoviGustoMenu(String nomeGusto) throws FrostlyException {
        cache.rimuoviGustoMenu(nomeGusto);
        Connection conn = ConnectionManager.getInstance().getConnection();
        String del = "DELETE FROM menu_gusti WHERE nome_gusto = ?";
        try (PreparedStatement dStmt = conn.prepareStatement(del)) {
            dStmt.setString(1, nomeGusto);
            dStmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nella rimozione del gusto dal menu su database.", e);
        }
    }

    private void sincronizzaMenuDaDatabase() {
        String query = "SELECT nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria, immagine_url FROM menu_gusti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cache.sincronizzaGustoDaDb(
                            rs.getString("nome_gusto"),
                            rs.getString("descrizione"),
                            rs.getString("allergeni"),
                            rs.getDouble("prezzo_al_kg"),
                            rs.getBoolean("disponibile"),
                            rs.getString("categoria"),
                            rs.getString("sottocategoria"),
                            rs.getString("immagine_url"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene cache locale in caso di assenza connessione
        }
    }
}
