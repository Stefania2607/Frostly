package frostly.exception;

/**
 * Eccezione di dominio sollevata durante l'elaborazione o la simulazione del pagamento online.
 */
public class PagamentoException extends FrostlyException {

    private static final long serialVersionUID = 1L;

    public PagamentoException(String message) {
        super(message);
    }

    public PagamentoException(String message, Throwable cause) {
        super(message, cause);
    }
}
