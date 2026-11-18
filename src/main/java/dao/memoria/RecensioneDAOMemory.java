package dao.memoria;

import dao.ArchivioMemoria;
import dao.RecensioneDAO;
import frostly.exception.FrostlyException;
import java.util.List;
import model.Recensione;

/**
 * Implementazione In-Memory del DAO Recensioni.
 */
public class RecensioneDAOMemory implements RecensioneDAO {

    private final ArchivioMemoria archivio;

    public RecensioneDAOMemory() {
        this.archivio = ArchivioMemoria.getInstance();
    }

    @Override
    public List<Recensione> trovaTutteRecensioni() throws FrostlyException {
        return archivio.getRecensioni();
    }

    @Override
    public Recensione salvaNuovaRecensione(Recensione r) throws FrostlyException {
        return archivio.salvaRecensione(r);
    }

    @Override
    public void aggiornaRispostaMaestro(String codiceRecensione, String rispostaMaestro, String dataRisposta) throws FrostlyException {
        archivio.rispondiRecensione(codiceRecensione, rispostaMaestro, dataRisposta);
    }

    @Override
    public void incrementaMiPiace(String codiceRecensione) throws FrostlyException {
        archivio.incrementaMiPiaceRecensione(codiceRecensione);
    }
}
