package frostly.exception;

/**
 * Eccezione sollevata quando l'autenticazione fallisce per credenziali errate.
 */
public class CredenzialiErrateException extends FrostlyException {

    public CredenzialiErrateException(String message) {
        super(message);
    }
}
