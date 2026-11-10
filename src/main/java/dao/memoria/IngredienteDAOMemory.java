package dao.memoria;

import dao.ArchivioMemoria;
import dao.IngredienteDAO;
import java.util.ArrayList;
import java.util.List;
import model.Ingrediente;

/**
 * Implementazione in-memory di IngredienteDAO.
 */
public class IngredienteDAOMemory implements IngredienteDAO {

    private final ArchivioMemoria archivio = ArchivioMemoria.getInstance();

    @Override
    public List<Ingrediente> caricaTuttiIngredienti() {
        return new ArrayList<>(archivio.getIngredienti());
    }

    @Override
    public Ingrediente trovaIngredientePerNome(String nome) {
        return archivio.trovaIngredientePerNome(nome);
    }

    @Override
    public void aggiornaIngrediente(Ingrediente ingrediente) {
        archivio.aggiornaIngrediente(ingrediente);
    }

    @Override
    public void salvaNuovoIngrediente(Ingrediente ingrediente) {
        archivio.aggiornaIngrediente(ingrediente);
    }

    @Override
    public void eliminaIngrediente(String nome) {
        archivio.eliminaIngrediente(nome);
    }
}
