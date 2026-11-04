package frostly.exception;

/**
 * Eccezione sollevata quando un turno non puo' essere assegnato o sostituito
 * per conflitto d'orario o assenza di dipendenti disponibili.
 */
public class TurnoNonDisponibileException extends FrostlyException {

    private static final long serialVersionUID = 1L;

    public TurnoNonDisponibileException(String message) {
        super(message);
    }
}