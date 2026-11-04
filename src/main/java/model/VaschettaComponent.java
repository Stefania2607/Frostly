package model;

/**
 * Interfaccia Component del pattern GoF Decorator per la composizione di una vaschetta ordinata.
 */
public interface VaschettaComponent {

    String getDescrizioneCompleta();

    double calcolaPrezzoFinale();
}