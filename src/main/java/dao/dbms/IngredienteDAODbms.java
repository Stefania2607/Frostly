package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.IngredienteDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.Ingrediente;

/**
 * Implementazione DBMS di IngredienteDAO.
 */
public class IngredienteDAODbms implements IngredienteDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getInstance();

    @Override
    public List<Ingrediente> caricaTuttiIngredienti() {
        sincronizzaIngredientiDaDatabase();
        return cache.getIngredienti();
    }

    @Override
    public Ingrediente trovaIngredientePerNome(String nome) {
        sincronizzaIngredientiDaDatabase();
        return cache.trovaIngredientePerNome(nome);
    }

    @Override
    public void aggiornaIngrediente(Ingrediente ingrediente) {
        cache.aggiornaIngrediente(ingrediente);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String sql = "UPDATE ingredienti SET quantita_disponibile = ? WHERE nome = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, ingrediente.getQuantitaDisponibile());
                stmt.setString(2, ingrediente.getNome());
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    String ins = "INSERT INTO ingredienti (nome, quantita_disponibile, soglia_minima, unita_misura) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement iStmt = conn.prepareStatement(ins)) {
                        iStmt.setString(1, ingrediente.getNome());
                        iStmt.setDouble(2, ingrediente.getQuantitaDisponibile());
                        iStmt.setDouble(3, ingrediente.getSogliaMinima());
                        iStmt.setString(4, ingrediente.getUnitaMisura());
                        iStmt.executeUpdate();
                    }
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    @Override
    public void salvaNuovoIngrediente(Ingrediente ingrediente) {
        aggiornaIngrediente(ingrediente);
    }

    @Override
    public void eliminaIngrediente(String nome) {
        cache.eliminaIngrediente(nome);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String sql = "DELETE FROM ingredienti WHERE nome = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.executeUpdate();
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    private void sincronizzaIngredientiDaDatabase() {
        String sql = "SELECT nome, quantita_disponibile, soglia_minima, unita_misura FROM ingredienti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cache.sincronizzaIngredienteDaDb(
                            rs.getString("nome"),
                            rs.getDouble("quantita_disponibile"),
                            rs.getDouble("soglia_minima"),
                            rs.getString("unita_misura"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }
}
