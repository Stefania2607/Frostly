package model;

/**
 * ConcreteComponent del pattern GoF Decorator: rappresenta la vaschetta base di un gusto.
 */
public class VaschettaBase implements VaschettaComponent {

    private final Gusto gusto;
    private final double pesoKg;

    public VaschettaBase(Gusto gusto, double pesoKg) {
        this.gusto = gusto;
        this.pesoKg = pesoKg;
    }

    @Override
    public String getDescrizioneCompleta() {
        return "Vaschetta " + gusto.getNome() + " (" + pesoKg + " kg)";
    }

    @Override
    public double calcolaPrezzoFinale() {
        return Math.round(gusto.getPrezzoAlKg() * pesoKg * 100.0) / 100.0;
    }
}