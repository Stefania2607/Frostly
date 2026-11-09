package control.grafico;

import bean.OrdineBean;
import control.applicativo.ProduzioneOrdiniControl;
import frostly.exception.FrostlyException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import util.SessionManager;
import view.StaffTurniView;

/**
 * Controller Grafico dedicato alla gestione degli Ordini dei Clienti (Deliveroo / Take Away)
 * da parte dello Staff (Maestro Gelataio e Dipendenti):
 * - Monitoraggio real-time degli ordini in entrata
 * - Visualizzazione ordini in attesa e storico ordini staff
 * - Conferma ordine e disdetta ordine con notifica al cliente.
 */
public class OrdiniStaffControlGrafico {

    private final ListView<OrdineBean> listaOrdiniStaff;
    private final Label lblConteggioOrdiniStaff;
    private final ListView<String> listaOrdiniUrgentiTendina;
    private final BiConsumer<String, Boolean> notificaEsito;
    private final Runnable onAggiornaGlobale;

    private int conteggioOrdiniInAttesaPrecedente = 0;

    public OrdiniStaffControlGrafico(ListView<OrdineBean> listaOrdiniStaff,
                                     Label lblConteggioOrdiniStaff,
                                     ListView<String> listaOrdiniUrgentiTendina,
                                     BiConsumer<String, Boolean> notificaEsito,
                                     Runnable onAggiornaGlobale) {
        this.listaOrdiniStaff = listaOrdiniStaff;
        this.lblConteggioOrdiniStaff = lblConteggioOrdiniStaff;
        this.listaOrdiniUrgentiTendina = listaOrdiniUrgentiTendina;
        this.notificaEsito = notificaEsito;
        this.onAggiornaGlobale = onAggiornaGlobale;
    }

    public void inizializza() {
        StaffTurniView.configuraCelleOrdiniStaff(
                listaOrdiniStaff, this::eseguiConfermaOrdine, this::eseguiDisdettaOrdine);
    }

    public void avviaMonitoraggioRealTime(IntConsumer onNuovoOrdineRilevato) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    try {
                        int attuali = 0;
                        for (OrdineBean ob : new ProduzioneOrdiniControl().visualizzaTuttiOrdini()) {
                            if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                                attuali++;
                            }
                        }
                        if (attuali > conteggioOrdiniInAttesaPrecedente) {
                            conteggioOrdiniInAttesaPrecedente = attuali;
                            onNuovoOrdineRilevato.accept(attuali);
                        } else {
                            conteggioOrdiniInAttesaPrecedente = attuali;
                        }
                    } catch (Exception ignored) {
                        // Monitoraggio silenzioso
                    }
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Aggiorna la lista ordini staff e la tendina urgenze e restituisce il conteggio ordini in attesa.
     */
    public int aggiornaOrdiniStaff() {
        ProduzioneOrdiniControl prodCtrl = new ProduzioneOrdiniControl();
        List<OrdineBean> tuttiOrdini = prodCtrl.visualizzaTuttiOrdini();

        int ordiniInAttesa = 0;
        if (listaOrdiniStaff != null) {
            listaOrdiniStaff.getItems().clear();
            for (OrdineBean ob : tuttiOrdini) {
                listaOrdiniStaff.getItems().add(ob);
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    ordiniInAttesa++;
                }
            }
            if (lblConteggioOrdiniStaff != null) {
                lblConteggioOrdiniStaff.setText(ordiniInAttesa > 0
                        ? "🚨 " + ordiniInAttesa + " ordini in attesa di conferma"
                        : "✅ Nessun ordine in attesa (" + tuttiOrdini.size() + " totali)");
            }
        } else {
            for (OrdineBean ob : tuttiOrdini) {
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    ordiniInAttesa++;
                }
            }
        }

        if (listaOrdiniUrgentiTendina != null) {
            listaOrdiniUrgentiTendina.getItems().clear();
            for (OrdineBean ob : tuttiOrdini) {
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    listaOrdiniUrgentiTendina.getItems().add("🛵 " + ob.getCodiceOrdine() + " • " + ob.getEmailCliente()
                            + " (" + ob.getDataRitiro() + ") - EUR " + ob.getPrezzoTotale());
                }
            }
            if (listaOrdiniUrgentiTendina.getItems().isEmpty()) {
                listaOrdiniUrgentiTendina.getItems().add("✅ Nessun ordine in attesa al momento");
            }
        }
        return ordiniInAttesa;
    }

    public void confermaOrdineSelezionato() {
        OrdineBean sel = listaOrdiniStaff != null ? listaOrdiniStaff.getSelectionModel().getSelectedItem() : null;
        if (sel == null) {
            notificaEsito.accept("⚠️ Seleziona prima un ordine dalla lista da confermare.", true);
            return;
        }
        eseguiConfermaOrdine(sel.getCodiceOrdine());
    }

    public void disdiciOrdineSelezionato() {
        OrdineBean sel = listaOrdiniStaff != null ? listaOrdiniStaff.getSelectionModel().getSelectedItem() : null;
        if (sel == null) {
            notificaEsito.accept("⚠️ Seleziona prima un ordine dalla lista da disdire.", true);
            return;
        }
        eseguiDisdettaOrdine(sel.getCodiceOrdine());
    }

    public void eseguiConfermaOrdine(String codiceOrdine) {
        try {
            String operatore = SessionManager.getInstance().getNomeUtenteLoggato();
            new ProduzioneOrdiniControl().confermaOrdine(codiceOrdine, operatore);
            notificaEsito.accept("✔ Ordine " + codiceOrdine + " CONFERMATO! Notifica inviata al cliente.", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void eseguiDisdettaOrdine(String codiceOrdine) {
        try {
            String operatore = SessionManager.getInstance().getNomeUtenteLoggato();
            new ProduzioneOrdiniControl().disdiciOrdine(codiceOrdine, operatore);
            notificaEsito.accept("✕ Ordine " + codiceOrdine + " DISDETTO. Notifica inviata al cliente.", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }
}
