package frostly.exception;

public class FrostlyException extends Exception {

    private static final long serialVersionUID = 1L;

    public FrostlyException(String message) {
        super(message);
    }

    public FrostlyException(String message, Throwable cause) {
        super(message, cause);
    }
}