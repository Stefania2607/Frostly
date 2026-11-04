package model;

/**
 * ConcreteDecorator (GoF): aggiunge Granella di Pistacchio di Bronte DOP alla vaschetta.
 */
public class ConGranellaBronteDecorator extends ExtraVaschettaDecorator {

    private static final double SUPPLEMENTO_GRANELLA = 3.50;

    public ConGranellaBronteDecorator(VaschettaComponent componenteDecorato) {
        super(componenteDecorato);
    }

    @Override
    public String getDescrizioneCompleta() {
        return super.getDescrizioneCompleta() + " + Granella di Pistacchio DOP";
    }

    @Override
    public double calcolaPrezzoFinale() {
        return Math.round((super.calcolaPrezzoFinale() + SUPPLEMENTO_GRANELLA) * 100.0) / 100.0;
    }
}