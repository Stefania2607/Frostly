package dao;

import frostly.exception.FrostlyException;
import model.Turno;

import java.util.List;

/**
 * Implementazione IN-MEMORY di TurnoDAO per la modalita' DEMO.
 * Nessun dato viene salvato su database o file system.
 */
public class TurnoDAOMemory implements TurnoDAO {

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        InMemoryFrostlyDAO.getInstance().salvaTurno(turno);
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        return InMemoryFrostlyDAO.getInstance().caricaTuttiTurni();
    }
}