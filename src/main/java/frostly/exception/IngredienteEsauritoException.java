package frostly.exception;

/**
 * Eccezione sollevata quando non ci sono ingredienti sufficienti in magazzino
 * per preparare un gusto o una richiesta personalizzata.
 */
public class IngredienteEsauritoException extends FrostlyException {

    private static final long serialVersionUID = 1L;
    private final String nomeIngrediente;
    private final double quantitaMancante;

    public IngredienteEsauritoException(String nomeIngrediente, double quantitaMancante) {
        super("Scorta insufficiente per l'ingrediente '" + nomeIngrediente
                + "'. Mancano: " + quantitaMancante);
        this.nomeIngrediente = nomeIngrediente;
        this.quantitaMancante = quantitaMancante;
    }

    public String getNomeIngrediente() {
        return nomeIngrediente;
    }

    public double getQuantitaMancante() {
        return quantitaMancante;
    }
}