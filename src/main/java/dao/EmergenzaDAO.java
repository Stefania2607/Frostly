package dao;

import java.util.ArrayList;
import java.util.List;
import model.Emergenza;
import model.RichiestaIndisponibilita;

/**
 * Interfaccia DAO dedicata esclusivamente alla gestione delle Emergenze, sostituzioni di turno
 * e richieste di indisponibilita'/permessi dello staff.
 */
public interface EmergenzaDAO {

    List<Emergenza> caricaTutteEmergenze();

    void salvaEmergenza(Emergenza emergenza);

    default List<RichiestaIndisponibilita> caricaRichiesteIndisponibilita() {
        return new ArrayList<>();
    }

    default void salvaRichiestaIndisponibilita(RichiestaIndisponibilita richiesta) {
        // Default no-op per retrocompatibilita'
    }
}
