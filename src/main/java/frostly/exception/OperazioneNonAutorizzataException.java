package frostly.exception;

/**
 * Eccezione sollevata quando un utente tenta un'operazione riservata a un altro ruolo o richiede autenticazione.
 */
public class OperazioneNonAutorizzataException extends FrostlyException {

    public OperazioneNonAutorizzataException(String message) {
        super(message);
    }
}
