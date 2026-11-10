package dao;

import java.util.List;
import model.Emergenza;

/**
 * Interfaccia DAO dedicata esclusivamente alla gestione delle Emergenze e sostituzioni di turno.
 */
public interface EmergenzaDAO {

    List<Emergenza> caricaTutteEmergenze();

    void salvaEmergenza(Emergenza emergenza);
}
