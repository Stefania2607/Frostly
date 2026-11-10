package dao.memoria;

import dao.ArchivioMemoria;
import dao.OrdineDAO;
import java.util.ArrayList;
import java.util.List;
import model.OrdineCliente;

/**
 * Implementazione in-memory di OrdineDAO.
 */
public class OrdineDAOMemory implements OrdineDAO {

    private final ArchivioMemoria archivio = ArchivioMemoria.getInstance();

    @Override
    public List<OrdineCliente> caricaTuttiOrdini() {
        return new ArrayList<>(archivio.getOrdini());
    }

    @Override
    public void salvaOrdine(OrdineCliente ordine) {
        archivio.salvaOrdine(ordine);
    }
}
