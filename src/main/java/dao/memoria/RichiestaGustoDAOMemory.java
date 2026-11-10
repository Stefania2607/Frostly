package dao.memoria;

import dao.ArchivioMemoria;
import dao.RichiestaGustoDAO;
import java.util.ArrayList;
import java.util.List;
import model.RichiestaGustoPersonalizzato;

/**
 * Implementazione in-memory di RichiestaGustoDAO.
 */
public class RichiestaGustoDAOMemory implements RichiestaGustoDAO {

    private final ArchivioMemoria archivio = ArchivioMemoria.getInstance();

    @Override
    public List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto() {
        return new ArrayList<>(archivio.getRichiesteGusto());
    }

    @Override
    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        archivio.salvaRichiestaGusto(richiesta);
    }
}
