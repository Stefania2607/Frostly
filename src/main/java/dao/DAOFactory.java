package dao;

import dao.dbms.EmergenzaDAODbms;
import dao.dbms.GustoDAODbms;
import dao.dbms.IngredienteDAODbms;
import dao.dbms.NotificaDAODbms;
import dao.dbms.OrdineDAODbms;
import dao.dbms.RecensioneDAODbms;
import dao.dbms.RichiestaGustoDAODbms;
import dao.dbms.UtenteDAODbms;
import dao.memoria.EmergenzaDAOMemory;
import dao.memoria.GustoDAOMemory;
import dao.memoria.IngredienteDAOMemory;
import dao.memoria.NotificaDAOMemory;
import dao.memoria.OrdineDAOMemory;
import dao.memoria.RecensioneDAOMemory;
import dao.memoria.RichiestaGustoDAOMemory;
import dao.memoria.UtenteDAOMemory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Pattern GoF Factory Method per l'istanziazione dei DAO dedicati per entita'.
 * - In modalita' DEMO restituisce i DAO In-Memory (*DAOMemory).
 * - In modalita' FULL restituisce i DAO persistenti su DBMS (*DAODbms) o FileSystem (TurnoDAOFileSystem).
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

    private static boolean isModalitaDemo() {
        return SessionManager.getInstance().getModalita() == ModalitaEsecuzione.DEMO;
    }

    public static UtenteDAO getUtenteDAO() {
        return isModalitaDemo() ? new UtenteDAOMemory() : new UtenteDAODbms();
    }

    public static TurnoDAO getTurnoDAO() {
        if (isModalitaDemo()) {
            return new TurnoDAOMemory();
        }
        if (usaFileSystemPerTurni) {
            return new TurnoDAOFileSystem();
        }
        return new TurnoDAODbms();
    }

    public static EmergenzaDAO getEmergenzaDAO() {
        return isModalitaDemo() ? new EmergenzaDAOMemory() : new EmergenzaDAODbms();
    }

    public static IngredienteDAO getIngredienteDAO() {
        return isModalitaDemo() ? new IngredienteDAOMemory() : new IngredienteDAODbms();
    }

    public static GustoDAO getGustoDAO() {
        return isModalitaDemo() ? new GustoDAOMemory() : new GustoDAODbms();
    }

    public static RichiestaGustoDAO getRichiestaGustoDAO() {
        return isModalitaDemo() ? new RichiestaGustoDAOMemory() : new RichiestaGustoDAODbms();
    }

    public static OrdineDAO getOrdineDAO() {
        return isModalitaDemo() ? new OrdineDAOMemory() : new OrdineDAODbms();
    }

    public static NotificaDAO getNotificaDAO() {
        return isModalitaDemo() ? new NotificaDAOMemory() : new NotificaDAODbms();
    }

    public static RecensioneDAO getRecensioneDAO() {
        return isModalitaDemo() ? new RecensioneDAOMemory() : new RecensioneDAODbms();
    }
}
