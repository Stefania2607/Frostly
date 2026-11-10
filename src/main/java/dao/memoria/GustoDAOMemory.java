package dao.memoria;

import dao.ArchivioMemoria;
import dao.GustoDAO;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import model.Gusto;

/**
 * Implementazione in-memory di GustoDAO.
 */
public class GustoDAOMemory implements GustoDAO {

    private final ArchivioMemoria archivio = ArchivioMemoria.getInstance();

    @Override
    public List<Gusto> caricaTuttiGusti() {
        return new ArrayList<>(archivio.getGusti());
    }

    @Override
    public Gusto trovaGustoPerNome(String nome) {
        return archivio.trovaGustoPerNome(nome);
    }

    @Override
    public void salvaGustoMenu(Gusto gusto) throws FrostlyException {
        archivio.salvaGustoMenu(gusto);
    }

    @Override
    public void rimuoviGustoMenu(String nomeGusto) throws FrostlyException {
        archivio.rimuoviGustoMenu(nomeGusto);
    }
}
