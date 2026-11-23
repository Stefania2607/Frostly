package control.grafico;

import bean.RecensioneBean;
import control.applicativo.RecensioniControl;
import frostly.exception.FrostlyException;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import view.RecensioniView;

/**
 * Controller Grafico dedicato alla gestione della sezione Recensioni per il Cliente
 * e per lo Staff (Maestro Gelataio).
 * Rispetta l'architettura BCE con zero chiamate a setStyle e delega completa del rendering alla View.
 */
public class RecensioniControlGrafico {

    private final RecensioniControl recensioniControl = new RecensioniControl();
    private final BiConsumer<String, Boolean> onEsito;
    private final Runnable onStatoAggiornato;

    private ComboBox<String> comboVotoRecensione;
    private TextField campoTitoloRecensione;
    private TextArea campoCommentoRecensione;
    private VBox boxCardsRecensioniCliente;
    private Label lblMediaRecensioniCliente;

    private VBox boxCardsRecensioniMaestro;
    private Label lblRiepilogoRecensioniMaestro;

    public RecensioniControlGrafico(BiConsumer<String, Boolean> onEsito, Runnable onStatoAggiornato) {
        this.onEsito = onEsito;
        this.onStatoAggiornato = onStatoAggiornato;
    }

    public void legaCampiCliente(ComboBox<String> comboVoto,
                                 TextField campoTitolo,
                                 TextArea campoCommento,
                                 VBox boxCards,
                                 Label lblMedia) {
        this.comboVotoRecensione = comboVoto;
        this.campoTitoloRecensione = campoTitolo;
        this.campoCommentoRecensione = campoCommento;
        this.boxCardsRecensioniCliente = boxCards;
        this.lblMediaRecensioniCliente = lblMedia;

        if (this.comboVotoRecensione != null) {
            this.comboVotoRecensione.setItems(FXCollections.observableArrayList(
                    "⭐⭐⭐⭐⭐ (5 Stelle - Eccellente)",
                    "⭐⭐⭐⭐ (4 Stelle - Molto Buono)",
                    "⭐⭐⭐ (3 Stelle - Buono)",
                    "⭐⭐ (2 Stelle - Sufficiente)",
                    "⭐ (1 Stella - Scarso)"));
            this.comboVotoRecensione.setValue("⭐⭐⭐⭐⭐ (5 Stelle - Eccellente)");
        }
    }

    public void legaCampiMaestro(VBox boxCards, Label lblRiepilogo) {
        this.boxCardsRecensioniMaestro = boxCards;
        this.lblRiepilogoRecensioniMaestro = lblRiepilogo;
    }

    public void inviaNuovaRecensione(String autoreEmail, String autoreNome) {
        try {
            int stelle = estraiVotoDaCombo();
            String tit = campoTitoloRecensione != null ? campoTitoloRecensione.getText() : "";
            String comm = campoCommentoRecensione != null ? campoCommentoRecensione.getText() : "";

            RecensioneBean bean = new RecensioneBean(autoreEmail, autoreNome, stelle, tit, comm);
            RecensioneBean salvata = recensioniControl.pubblicaRecensione(bean);

            if (campoTitoloRecensione != null) campoTitoloRecensione.clear();
            if (campoCommentoRecensione != null) campoCommentoRecensione.clear();
            if (comboVotoRecensione != null) comboVotoRecensione.setValue("⭐⭐⭐⭐⭐ (5 Stelle - Eccellente)");

            onEsito.accept("⭐ Grazie " + autoreNome + "! La tua recensione (" + salvata.getStelleIcone()
                    + ") è stata pubblicata. Il Maestro ti risponderà a breve!", false);

            aggiornaRecensioniCliente();
            if (onStatoAggiornato != null) {
                onStatoAggiornato.run();
            }
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void pubblicaRispostaMaestro(String codiceRecensione, String rispostaMaestro) {
        try {
            recensioniControl.rispondiARecensione(codiceRecensione, rispostaMaestro);
            onEsito.accept("💬 Risposta ufficiale del Maestro pubblicata e notificata al cliente!", false);
            aggiornaRecensioniMaestro();
            if (onStatoAggiornato != null) {
                onStatoAggiornato.run();
            }
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void mettiMiPiace(String cod) {
        try {
            recensioniControl.mettiMiPiace(cod);
            aggiornaRecensioniCliente();
            aggiornaRecensioniMaestro();
        } catch (FrostlyException ignored) {
            // Ignora se errore
        }
    }

    public void aggiornaRecensioniCliente() {
        if (boxCardsRecensioniCliente == null) return;
        boxCardsRecensioniCliente.getChildren().clear();
        try {
            List<RecensioneBean> lista = recensioniControl.visualizzaTutteRecensioni();
            double media = recensioniControl.calcolaMediaVoti(lista);
            if (lblMediaRecensioniCliente != null) {
                lblMediaRecensioniCliente.setText(String.format("⭐ %.1f / 5.0 (Basato su %d recensioni dei clienti)", media, lista.size()));
            }
            for (RecensioneBean r : lista) {
                // Cliente autenticato: può mettere mi piace
                boxCardsRecensioniCliente.getChildren().add(
                        RecensioniView.buildCardRecensione(r, true, false, b -> mettiMiPiace(b.getCodiceRecensione()), null, null));
            }
        } catch (FrostlyException ignored) {}
    }

    public void aggiornaRecensioniMaestro() {
        if (boxCardsRecensioniMaestro == null) return;
        boxCardsRecensioniMaestro.getChildren().clear();
        try {
            List<RecensioneBean> lista = recensioniControl.visualizzaTutteRecensioni();
            double media = recensioniControl.calcolaMediaVoti(lista);
            int inAttesaDiRisposta = 0;
            for (RecensioneBean r : lista) {
                if (!r.haRispostaMaestro()) inAttesaDiRisposta++;
            }
            if (lblRiepilogoRecensioniMaestro != null) {
                lblRiepilogoRecensioniMaestro.setText(String.format("Valutazione media: ★ %.1f / 5.0 (%d recensioni totali • %d in attesa di risposta)",
                        media, lista.size(), inAttesaDiRisposta));
            }
            for (RecensioneBean r : lista) {
                // Maestro autenticato: può mettere mi piace e aprire pop-up risposta
                boxCardsRecensioniMaestro.getChildren().add(
                        RecensioniView.buildCardRecensione(r, true, true, b -> mettiMiPiace(b.getCodiceRecensione()), this::apriPopupRisposta, null));
            }
        } catch (FrostlyException ignored) {}
    }

    private void apriPopupRisposta(RecensioneBean r) {
        Window owner = (boxCardsRecensioniMaestro != null && boxCardsRecensioniMaestro.getScene() != null)
                ? boxCardsRecensioniMaestro.getScene().getWindow() : null;
        RecensioniView.apriPopupRispostaMaestro(owner, r, testo -> {
            pubblicaRispostaMaestro(r.getCodiceRecensione(), testo);
        });
    }

    private int estraiVotoDaCombo() {
        if (comboVotoRecensione == null || comboVotoRecensione.getValue() == null) {
            return 5;
        }
        String val = comboVotoRecensione.getValue();
        if (val.contains("5")) return 5;
        if (val.contains("4")) return 4;
        if (val.contains("3")) return 3;
        if (val.contains("2")) return 2;
        if (val.contains("1")) return 1;
        return 5;
    }
}
