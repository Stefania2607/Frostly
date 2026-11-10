package dao;

import frostly.exception.FrostlyException;
import java.util.List;
import model.Gusto;

/**
 * Interfaccia DAO dedicata esclusivamente al catalogo dei gusti e prodotti in vetrina.
 */
public interface GustoDAO {

    List<Gusto> caricaTuttiGusti();

    Gusto trovaGustoPerNome(String nome);

    void salvaGustoMenu(Gusto gusto) throws FrostlyException;

    void rimuoviGustoMenu(String nomeGusto) throws FrostlyException;
}
