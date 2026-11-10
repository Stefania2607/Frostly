package dao.memoria;

import dao.ArchivioMemoria;
import dao.EmergenzaDAO;
import java.util.ArrayList;
import java.util.List;
import model.Emergenza;

/**
 * Implementazione in-memory di EmergenzaDAO.
 */
public class EmergenzaDAOMemory implements EmergenzaDAO {

    private final ArchivioMemoria archivio = ArchivioMemoria.getInstance();

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        return new ArrayList<>(archivio.getEmergenze());
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
        archivio.salvaEmergenza(emergenza);
    }
}
