package frostly.exception;

/**
 * Eccezione specifica per dati della carta di credito non validi o non conformi.
 * (Numero errato, formato scadenza invalido, carta scaduta, CVV non conforme).
 */
public class InvalidCardException extends PagamentoException {

    private static final long serialVersionUID = 1L;

    public InvalidCardException(String message) {
        super(message);
    }
}
