package frostly.exception;

/**
 * Eccezione sollevata quando un'entita' richiesta (utente, ordine, gusto, turno, ingrediente, ecc.) non esiste.
 */
public class RisorsaNonTrovataException extends FrostlyException {

    public RisorsaNonTrovataException(String message) {
        super(message);
    }
}
