package control.grafico;

import bean.OrdineBean;
import control.applicativo.OrdiniControl;
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
import view.OrdiniStaffView;

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
        OrdiniStaffView.configuraCelleOrdiniStaff(
                listaOrdiniStaff, this::eseguiConfermaOrdine, this::eseguiDisdettaOrdine);
    }

    public void avviaMonitoraggioRealTime(IntConsumer onNuovoOrdineRilevato) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    try {
                        int attuali = 0;
                        for (OrdineBean ob : new OrdiniControl().visualizzaTuttiOrdini()) {
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
        OrdiniControl ordiniCtrl = new OrdiniControl();
        List<OrdineBean> tuttiOrdini = ordiniCtrl.visualizzaTuttiOrdini();

        // Ordinamento ordini:
        // 1. Ordini nuovi in attesa ("IN_ATTESA") prioritari in cima alla lista
        // 2. Ordinamento temporale dal più recente al meno recente (es. ORD-05 prima di ORD-01)
        List<OrdineBean> listaOrdinata = new java.util.ArrayList<>(tuttiOrdini);
        listaOrdinata.sort((o1, o2) -> {
            boolean a1 = "IN_ATTESA".equalsIgnoreCase(o1.getStato());
            boolean a2 = "IN_ATTESA".equalsIgnoreCase(o2.getStato());
            if (a1 && !a2) return -1;
            if (!a1 && a2) return 1;
            String c1 = o1.getCodiceOrdine() != null ? o1.getCodiceOrdine() : "";
            String c2 = o2.getCodiceOrdine() != null ? o2.getCodiceOrdine() : "";
            return c2.compareToIgnoreCase(c1);
        });

        int ordiniInAttesa = 0;
        if (listaOrdiniStaff != null) {
            listaOrdiniStaff.getItems().clear();
            for (OrdineBean ob : listaOrdinata) {
                listaOrdiniStaff.getItems().add(ob);
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    ordiniInAttesa++;
                }
            }
            if (lblConteggioOrdiniStaff != null) {
                lblConteggioOrdiniStaff.setText(ordiniInAttesa > 0
                        ? "🚨 " + ordiniInAttesa + " nuovi ordini in attesa di conferma"
                        : "✅ Nessun ordine in attesa (" + listaOrdinata.size() + " totali)");
            }
        } else {
            for (OrdineBean ob : listaOrdinata) {
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    ordiniInAttesa++;
                }
            }
        }

        if (listaOrdiniUrgentiTendina != null) {
            listaOrdiniUrgentiTendina.getItems().clear();
            for (OrdineBean ob : listaOrdinata) {
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
            new OrdiniControl().confermaOrdine(codiceOrdine, operatore);
            notificaEsito.accept("✔ Ordine " + codiceOrdine + " CONFERMATO! Notifica inviata al cliente.", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void eseguiDisdettaOrdine(String codiceOrdine) {
        try {
            String operatore = SessionManager.getInstance().getNomeUtenteLoggato();
            new OrdiniControl().disdiciOrdine(codiceOrdine, operatore);
            notificaEsito.accept("✕ Ordine " + codiceOrdine + " DISDETTO. Notifica inviata al cliente.", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }
}
