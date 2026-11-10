package dao;

import java.util.List;
import model.OrdineCliente;

/**
 * Interfaccia DAO dedicata esclusivamente agli ordini e pre-ordini dei clienti (vaschette, torte, ecc.).
 */
public interface OrdineDAO {

    List<OrdineCliente> caricaTuttiOrdini();

    void salvaOrdine(OrdineCliente ordine);
}
