package dao;

import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Pattern GoF Factory Method per l'istanziazione dei DAO.
 */
public final class DAOFactory {

    private static boolean usaFileSystemPerTurni = false;

    private DAOFactory() {
    }

    public static void setUsaFileSystemPerTurni(boolean usaFile) {
        usaFileSystemPerTurni = usaFile;
    }

    public static TurnoDAO getTurnoDAO() {
        if (SessionManager.getInstance().getModalita() == ModalitaEsecuzione.DEMO) {
            return new TurnoDAOMemory();
        }
        if (usaFileSystemPerTurni) {
            return new TurnoDAOFileSystem();
        }
        return new TurnoDAODbms();
    }

    public static FrostlyDAO getFrostlyDAO() {
        return InMemoryFrostlyDAO.getInstance();
    }
}