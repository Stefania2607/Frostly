package model;

/**
 * ConcreteDecorator (GoF): aggiunge supplemento di Panna Montata Fresca alla vaschetta.
 */
public class ConPannaMontataDecorator extends ExtraVaschettaDecorator {

    private static final double SUPPLEMENTO_PANNA = 2.50;

    public ConPannaMontataDecorator(VaschettaComponent componenteDecorato) {
        super(componenteDecorato);
    }

    @Override
    public String getDescrizioneCompleta() {
        return super.getDescrizioneCompleta() + " + Panna Montata Fresca";
    }

    @Override
    public double calcolaPrezzoFinale() {
        return Math.round((super.calcolaPrezzoFinale() + SUPPLEMENTO_PANNA) * 100.0) / 100.0;
    }
}