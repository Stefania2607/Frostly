package dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Pattern GoF Factory Method per l'istanziazione dei DAO.
 * - In modalita' DEMO restituisce i DAO In-Memory (TurnoDAOMemory / InMemoryFrostlyDAO).
 * - In modalita' FULL restituisce i DAO persistenti (TurnoDAODbms o TurnoDAOFileSystem, e DbmsFrostlyDAO).
 */
public final class DAOFactory {

    private static boolean usaFileSystemPerTurni = caricaPreferenzaFileSystem();

    private DAOFactory() {
    }

    private static boolean caricaPreferenzaFileSystem() {
        try (InputStream input = DAOFactory.class.getResourceAsStream("/db.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String tipo = prop.getProperty("turni.persistence", "DBMS").trim();
                return "CSV".equalsIgnoreCase(tipo) || "FILESYSTEM".equalsIgnoreCase(tipo);
            }
        } catch (IOException ignored) {
            // Default DBMS
        }
        return false;
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
        if (SessionManager.getInstance().getModalita() == ModalitaEsecuzione.DEMO) {
            return InMemoryFrostlyDAO.getInstance();
        }
        return DbmsFrostlyDAO.getInstance();
    }
}
