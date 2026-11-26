package dao;

import frostly.exception.FrostlyException;
import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.FabbisognoTurno;
import model.Turno;

/**
 * Interfaccia DAO dedicata esclusivamente all'Entity Turno.
 * Implementata in 3 versioni:
 * - TurnoDAOMemory (per la modalita' DEMO In-Memory)
 * - TurnoDAODbms (per la modalita' FULL su Database MySQL JDBC)
 * - TurnoDAOFileSystem (per la modalita' FULL su File System CSV)
 */
public interface TurnoDAO {

    void salvaTurno(Turno turno) throws FrostlyException;

    List<Turno> caricaTuttiTurni() throws FrostlyException;

    default void eliminaTurno(String codiceTurno) throws FrostlyException {
        // Default vuoto per retrocompatibilita'
    }

    /**
     * Applica in modo atomico rimozione e inserimento di un insieme di turni della settimana.
     */
    default void sostituisciTurniAtomico(List<String> codiciDaRimuovere, List<Turno> turniDaSalvare)
            throws FrostlyException {
        if (codiciDaRimuovere != null) {
            for (String cod : codiciDaRimuovere) {
                eliminaTurno(cod);
            }
        }
        if (turniDaSalvare != null) {
            for (Turno t : turniDaSalvare) {
                salvaTurno(t);
            }
        }
    }

    default Map<String, FabbisognoTurno> caricaFabbisogni() throws FrostlyException {
        Map<String, FabbisognoTurno> map = new LinkedHashMap<>();
        for (DayOfWeek g : DayOfWeek.values()) {
            for (int f = 0; f < 5; f++) {
                map.put(FabbisognoTurno.chiave(g, f), FabbisognoTurno.predefinito(g, f));
            }
        }
        return map;
    }

    default void salvaFabbisogni(Map<String, FabbisognoTurno> fabbisogni) throws FrostlyException {
        // Default no-op per retrocompatibilita'
    }
}