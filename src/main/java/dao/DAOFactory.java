package dao;

import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Pattern GoF Factory Method:
 * - In modalita' DEMO -> restituisce InMemoryFrostlyDAO
 * - In modalita' FULL -> restituisce DbmsFrostlyDAO
 */
public final class DAOFactory {

    private DAOFactory() {
    }

    public static FrostlyDAO getFrostlyDAO() {
        if (SessionManager.getInstance().getModalita() == ModalitaEsecuzione.FULL) {
            return DbmsFrostlyDAO.getInstance();
        }
        return InMemoryFrostlyDAO.getInstance();
    }
}