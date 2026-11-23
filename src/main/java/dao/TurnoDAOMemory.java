package dao;

import frostly.exception.FrostlyException;
import java.util.List;
import model.Turno;

/**
 * Implementazione IN-MEMORY di TurnoDAO per la modalita' DEMO.
 * Nessun dato viene salvato su database o file system.
 */
public class TurnoDAOMemory implements TurnoDAO {

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        ArchivioMemoria.getDemoInstance().salvaTurno(turno);
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        return ArchivioMemoria.getDemoInstance().getTurni();
    }

    @Override
    public void eliminaTurno(String codiceTurno) throws FrostlyException {
        ArchivioMemoria.getDemoInstance().eliminaTurno(codiceTurno);
    }
}
