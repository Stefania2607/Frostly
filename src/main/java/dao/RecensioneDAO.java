package dao;

import frostly.exception.FrostlyException;
import java.util.List;
import model.Recensione;

/**
 * Interfaccia DAO dedicata all'accesso e alla persistenza delle Recensioni dei Clienti.
 */
public interface RecensioneDAO {

    List<Recensione> trovaTutteRecensioni() throws FrostlyException;

    Recensione salvaNuovaRecensione(Recensione r) throws FrostlyException;

    void aggiornaRispostaMaestro(String codiceRecensione, String rispostaMaestro, String dataRisposta) throws FrostlyException;

    void incrementaMiPiace(String codiceRecensione) throws FrostlyException;
}
