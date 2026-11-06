package dao;

import frostly.exception.FrostlyException;
import java.util.List;
import model.Turno;

/**
 * Interfaccia DAO dedicata esclusivamente all'Entity Turno (Single Responsibility Principle).
 * Implementata in 3 versioni:
 * - TurnoDAOMemory (per la modalita' DEMO In-Memory)
 * - TurnoDAODbms (per la modalita' FULL su Database SQLite JDBC)
 * - TurnoDAOFileSystem (per la modalita' FULL su File System CSV)
 */
public interface TurnoDAO {

    void salvaTurno(Turno turno) throws FrostlyException;

    List<Turno> caricaTuttiTurni() throws FrostlyException;
}