package dao;

import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Pattern GoF Factory Method:
 * - Se la modalita' e' DEMO_IN_MEMORY -  usa solo la RAM
 * - Se la modalita' e' FULL_DBMS - usa DbmsFrostlyDAO (SQLite JDBC)
 * - Se la modalita' e' FULL_FILESYSTEM -  usa FileSystemFrostlyDAO (File CSV)
 */
public final class DAOFactory {

    private DAOFactory() {
        // Costruttore privato
    }

    public static FrostlyDAO getFrostlyDAO() {
        ModalitaEsecuzione modalita = SessionManager.getInstance().getModalita();
        if (modalita == ModalitaEsecuzione.FULL_DBMS) {
            return DbmsFrostlyDAO.getInstance();
        } else if (modalita == ModalitaEsecuzione.FULL_FILESYSTEM) {
            return FileSystemFrostlyDAO.getInstance();
        }
        return InMemoryFrostlyDAO.getInstance();
    }
}