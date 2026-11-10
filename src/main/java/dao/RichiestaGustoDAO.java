package dao;

import java.util.List;
import model.RichiestaGustoPersonalizzato;

/**
 * Interfaccia DAO dedicata esclusivamente alle richieste di gusti su misura avanzate dai clienti.
 */
public interface RichiestaGustoDAO {

    List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto();

    void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta);
}
