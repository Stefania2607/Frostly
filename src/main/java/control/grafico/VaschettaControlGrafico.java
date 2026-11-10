package control.grafico;

import bean.CarrelloBean;
import bean.GustoBean;
import frostly.exception.FrostlyException;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import view.VaschettaView;

/**
 * Controller Grafico dedicato alla logica di composizione interattiva della Vaschetta di gelato:
 * selezione del formato/peso, aggiunta/rimozione gusti per scomparto, opzioni extra (panna/granella),
 * calcolo del prezzo dinamico e riversamento nel Carrello.
 */
public class VaschettaControlGrafico {

    private final CarrelloBean carrello;
    private final Button btnFormato350;
    private final Button btnFormato500;
    private final Button btnFormato750;
    private final Button btnFormato1000;
    private final Label lblCapienzaVaschetta;
    private final HBox boxVaschettaGrafica;
    private final CheckBox checkPanna;
    private final CheckBox checkGranella;
    private final Label lblPrezzoVaschettaCorrente;

    private final BiConsumer<String, Boolean> onEsito;
    private final Runnable onStatoCambiato;

    public VaschettaControlGrafico(CarrelloBean carrello,
                                   Button btnFormato350,
                                   Button btnFormato500,
                                   Button btnFormato750,
                                   Button btnFormato1000,
                                   Label lblCapienzaVaschetta,
                                   HBox boxVaschettaGrafica,
                                   CheckBox checkPanna,
                                   CheckBox checkGranella,
                                   Label lblPrezzoVaschettaCorrente,
                                   BiConsumer<String, Boolean> onEsito,
                                   Runnable onStatoCambiato) {
        this.carrello = carrello;
        this.btnFormato350 = btnFormato350;
        this.btnFormato500 = btnFormato500;
        this.btnFormato750 = btnFormato750;
        this.btnFormato1000 = btnFormato1000;
        this.lblCapienzaVaschetta = lblCapienzaVaschetta;
        this.boxVaschettaGrafica = boxVaschettaGrafica;
        this.checkPanna = checkPanna;
        this.checkGranella = checkGranella;
        this.lblPrezzoVaschettaCorrente = lblPrezzoVaschettaCorrente;
        this.onEsito = onEsito;
        this.onStatoCambiato = onStatoCambiato;
    }

    public void inizializza() {
        if (checkPanna != null) {
            checkPanna.setOnAction(e -> aggiornaVaschettaGrafica());
        }
        if (checkGranella != null) {
            checkGranella.setOnAction(e -> aggiornaVaschettaGrafica());
        }
        selezionaFormato(0.50, 4);
    }

    public void selezionaFormato(double pesoKg, int maxGusti) {
        carrello.impostaFormatoVaschetta(pesoKg, maxGusti);
        VaschettaView.evidenziaPulsanteFormato(btnFormato350, pesoKg == 0.35);
        VaschettaView.evidenziaPulsanteFormato(btnFormato500, pesoKg == 0.50);
        VaschettaView.evidenziaPulsanteFormato(btnFormato750, pesoKg == 0.75);
        VaschettaView.evidenziaPulsanteFormato(btnFormato1000, pesoKg == 1.00);
        aggiornaVaschettaGrafica();
        if (onStatoCambiato != null) {
            onStatoCambiato.run();
        }
    }

    public void aggiungiORimuoviGusto(GustoBean g) {
        List<GustoBean> scelti = carrello.getGustiNellaVaschetta();
        if (scelti.contains(g)) {
            scelti.remove(g);
            onEsito.accept("ℹ️ Gusto '" + g.getNome() + "' rimosso dalla vaschetta.", false);
        } else {
            if (scelti.size() >= carrello.getMaxGustiVaschetta()) {
                onEsito.accept("⚠️ Hai già raggiunto il limite di " + carrello.getMaxGustiVaschetta()
                        + " gusti per questo formato. Rimuovine uno per fare spazio!", true);
                return;
            }
            scelti.add(g);
            onEsito.accept("🍦 '" + g.getNome() + "' aggiunto alla vaschetta! Quando hai finito clicca su '➕ Metti Vaschetta nel Carrello'.", false);
        }
        aggiornaVaschettaGrafica();
        if (onStatoCambiato != null) {
            onStatoCambiato.run();
        }
    }

    public void svuotaVaschetta() {
        carrello.svuotaVaschettaInComposizione();
        aggiornaVaschettaGrafica();
        if (onStatoCambiato != null) {
            onStatoCambiato.run();
        }
        onEsito.accept("Vaschetta svuotata. Scegli i tuoi gusti preferiti dalle schede a sinistra.", false);
    }

    public void aggiungiVaschettaAlCarrello() {
        boolean conPanna = checkPanna != null && checkPanna.isSelected();
        boolean conGranella = checkGranella != null && checkGranella.isSelected();
        try {
            carrello.aggiungiVaschettaInComposizioneAlCarrello(conPanna, conGranella);
            resetSelettoriExtra();
            aggiornaVaschettaGrafica();
            if (onStatoCambiato != null) {
                onStatoCambiato.run();
            }
            onEsito.accept("🍦 Vaschetta aggiunta al carrello! Clicca '🛒 Vedi Carrello' in alto a destra per vedere l'ordine o comporne un'altra.", false);
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void resetSelettoriExtra() {
        if (checkPanna != null) checkPanna.setSelected(false);
        if (checkGranella != null) checkGranella.setSelected(false);
    }

    public void aggiornaVaschettaGrafica() {
        boolean conPanna = checkPanna != null && checkPanna.isSelected();
        boolean conGranella = checkGranella != null && checkGranella.isSelected();
        List<GustoBean> scelti = carrello.getGustiNellaVaschetta();
        int maxGusti = carrello.getMaxGustiVaschetta();
        double pesoKg = carrello.getPesoVaschettaKg();

        VaschettaView.renderVaschettaGrafica(
                boxVaschettaGrafica,
                scelti,
                maxGusti,
                pesoKg,
                conPanna,
                conGranella,
                g -> {
                    scelti.remove(g);
                    aggiornaVaschettaGrafica();
                    if (onStatoCambiato != null) {
                        onStatoCambiato.run();
                    }
                });

        if (lblCapienzaVaschetta != null) {
            int grammi = (int) (pesoKg * 1000);
            if (scelti.isEmpty() && conPanna) {
                lblCapienzaVaschetta.setText("Sola Panna (" + grammi + "g)");
            } else {
                lblCapienzaVaschetta.setText(scelti.size() + "/" + maxGusti + " gusti (" + grammi + "g)");
            }
        }

        if (lblPrezzoVaschettaCorrente != null) {
            double prezzo = carrello.calcolaPrezzoVaschettaInComposizione(conPanna, conGranella);
            lblPrezzoVaschettaCorrente.setText(String.format(Locale.US, "EUR %.2f", prezzo));
        }
    }
}
