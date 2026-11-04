package frostly.exception;

/**
 * Eccezione sollevata quando i dati inseriti in un Bean non superano
 * la validazione sintattica.
 */
public class DatiNonValidiException extends FrostlyException {

    private static final long serialVersionUID = 1L;

    public DatiNonValidiException(String message) {
        super(message);
    }
}