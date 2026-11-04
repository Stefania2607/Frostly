package model;

/**
 * Classe astratta Decorator (GoF) per arricchire una vaschetta con supplementi artigianali.
 */
public abstract class ExtraVaschettaDecorator implements VaschettaComponent {

    protected final VaschettaComponent componenteDecorato;

    protected ExtraVaschettaDecorator(VaschettaComponent componenteDecorato) {
        this.componenteDecorato = componenteDecorato;
    }

    @Override
    public String getDescrizioneCompleta() {
        return componenteDecorato.getDescrizioneCompleta();
    }

    @Override
    public double calcolaPrezzoFinale() {
        return componenteDecorato.calcolaPrezzoFinale();
    }
}