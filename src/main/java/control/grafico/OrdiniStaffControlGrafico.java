package control.grafico;

import bean.OrdineBean;
import control.applicativo.OrdiniControl;
import frostly.exception.FrostlyException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final Pattern PATTERN_TRX_DATA = Pattern.compile("TRX-(\\d{8})-");
    private static final Pattern PATTERN_DATA_IT = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
    private static final DateTimeFormatter FMT_BREVE = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN);

    private int conteggioOrdiniInAttesaPrecedente = 0;
    private String ultimaFirmaFasi = "";
    private final Map<LocalDate, List<OrdineBean>> storicoOrdiniPerGiorno = new TreeMap<>(Comparator.reverseOrder());

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
        view.MagazzinoStaffView.configuraCelleListaTendina(listaOrdiniUrgentiTendina, "rgba(186,164,135,0.55)");
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
                        if (listaOrdiniStaff != null && !listaOrdiniStaff.getItems().isEmpty()) {
                            StringBuilder firma = new StringBuilder();
                            for (OrdineBean item : listaOrdiniStaff.getItems()) {
                                if (!OrdiniStaffView.isHeaderGiorno(item)) {
                                    firma.append(item.getCodiceOrdine()).append(':')
                                            .append(OrdiniControl.calcolaFaseConsegna(item)).append(';');
                                }
                            }
                            String firmaAttuale = firma.toString();
                            if (!firmaAttuale.equals(ultimaFirmaFasi)) {
                                ultimaFirmaFasi = firmaAttuale;
                                listaOrdiniStaff.refresh();
                            }
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
        for (OrdineBean ob : listaOrdinata) {
            if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                ordiniInAttesa++;
            }
        }

        LocalDate oggi = LocalDate.now();
        List<OrdineBean> ordiniOggi = new ArrayList<>();
        storicoOrdiniPerGiorno.clear();

        for (OrdineBean ob : listaOrdinata) {
            LocalDate dataOrd = estraiDataOrdine(ob);
            if (dataOrd.equals(oggi) || dataOrd.isAfter(oggi)) {
                ordiniOggi.add(ob);
            } else {
                storicoOrdiniPerGiorno.computeIfAbsent(dataOrd, k -> new ArrayList<>()).add(ob);
            }
        }

        if (listaOrdiniStaff != null) {
            listaOrdiniStaff.getItems().clear();
            aggiungiGruppoGiorno(ordiniOggi, "📅 Oggi — " + oggi.format(FMT_BREVE));

            if (lblConteggioOrdiniStaff != null) {
                int archiviati = storicoOrdiniPerGiorno.values().stream().mapToInt(List::size).sum();
                String extraArch = archiviati > 0 ? (" • " + archiviati + " in storico") : "";
                lblConteggioOrdiniStaff.setText(ordiniInAttesa > 0
                        ? "🚨 " + ordiniInAttesa + " nuovi in attesa" + extraArch
                        : "✅ Oggi: " + ordiniOggi.size() + (ordiniOggi.size() == 1 ? " ordine" : " ordini") + extraArch);
            }
        }

        if (listaOrdiniUrgentiTendina != null) {
            listaOrdiniUrgentiTendina.getItems().clear();
            for (OrdineBean ob : listaOrdinata) {
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    listaOrdiniUrgentiTendina.getItems().add(formattaRigaOrdineTendina(ob));
                }
            }
            if (listaOrdiniUrgentiTendina.getItems().isEmpty()) {
                listaOrdiniUrgentiTendina.getItems().add("✅ Nessun ordine in attesa al momento");
            }
        }
        return ordiniInAttesa;
    }

    private String formattaRigaOrdineTendina(OrdineBean ob) {
        String codice = ob.getCodiceOrdine() != null ? ob.getCodiceOrdine() : "ORD";
        String cliente = (ob.getNomeCliente() != null && !ob.getNomeCliente().isBlank())
                ? ob.getNomeCliente()
                : (ob.getEmailCliente() != null ? ob.getEmailCliente() : "Cliente");
        String raw = ob.getDataRitiro() != null ? ob.getDataRitiro() : "";
        boolean isAsporto = raw.toLowerCase().contains("banco") || raw.toLowerCase().contains("ritiro in gelateria");
        String canale = isAsporto ? "🛍️ Ritiro al Banco" : "🛵 Consegna a Domicilio";
        String orario = estraiOrarioBreve(raw);
        String prezzo = String.format(java.util.Locale.US, "€ %.2f", ob.getPrezzoTotale());
        return codice + "|" + cliente + "|" + canale + "|" + orario + "|" + prezzo;
    }

    private String estraiOrarioBreve(String raw) {
        if (raw == null || raw.isBlank()) return "Subito";
        java.util.regex.Matcher mOrario = java.util.regex.Pattern.compile("Orario:\\s*([^•\\[]+)").matcher(raw);
        if (mOrario.find()) {
            return mOrario.group(1).trim();
        }
        int p1 = raw.indexOf('(');
        int p2 = raw.indexOf(')');
        if (p1 >= 0 && p2 > p1) {
            String dentro = raw.substring(p1 + 1, p2).trim();
            if (!dentro.equalsIgnoreCase("Banco")) {
                return dentro;
            }
        }
        return "Subito";
    }

    private void aggiungiGruppoGiorno(List<OrdineBean> ordini, String titoloGiorno) {
        if (ordini.isEmpty() || listaOrdiniStaff == null) return;
        String suffix = ordini.size() == 1 ? " (1 ordine)" : (" (" + ordini.size() + " ordini)");
        listaOrdiniStaff.getItems().add(OrdiniStaffView.creaHeaderGiorno(titoloGiorno + suffix));
        listaOrdiniStaff.getItems().addAll(ordini);
    }

    private LocalDate estraiDataOrdine(OrdineBean ob) {
        String raw = ob.getDataRitiro() != null ? ob.getDataRitiro() : "";
        Matcher mTrx = PATTERN_TRX_DATA.matcher(raw);
        if (mTrx.find()) {
            try {
                return LocalDate.parse(mTrx.group(1), DateTimeFormatter.BASIC_ISO_DATE);
            } catch (Exception ignored) {
                // Fallback ai formati successivi
            }
        }
        Matcher mIt = PATTERN_DATA_IT.matcher(raw);
        if (mIt.find()) {
            try {
                return LocalDate.parse(mIt.group(1), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ignored) {
                // Fallback a oggi
            }
        }
        if (raw.toLowerCase().contains("ieri")) {
            return LocalDate.now().minusDays(1);
        }
        return LocalDate.now();
    }

    public void apriStoricoOrdini() {
        javafx.stage.Window owner = (listaOrdiniStaff != null && listaOrdiniStaff.getScene() != null)
                ? listaOrdiniStaff.getScene().getWindow() : null;
        OrdiniStaffView.mostraPopupStoricoOrdiniPerGiorno(
                owner, storicoOrdiniPerGiorno, this::eseguiConfermaOrdine, this::eseguiDisdettaOrdine);
    }

    public boolean selezionaOrdinePerCodice(String codiceOrdine) {
        if (codiceOrdine == null || codiceOrdine.isBlank() || listaOrdiniStaff == null) {
            return false;
        }
        String target = codiceOrdine.trim();
        List<OrdineBean> items = listaOrdiniStaff.getItems();
        for (int i = 0; i < items.size(); i++) {
            OrdineBean ob = items.get(i);
            if (ob != null && !OrdiniStaffView.isHeaderGiorno(ob)
                    && ob.getCodiceOrdine() != null
                    && ob.getCodiceOrdine().equalsIgnoreCase(target)) {
                listaOrdiniStaff.getSelectionModel().select(i);
                listaOrdiniStaff.scrollTo(i);
                listaOrdiniStaff.requestFocus();
                return true;
            }
        }
        for (List<OrdineBean> listaGiorno : storicoOrdiniPerGiorno.values()) {
            for (OrdineBean ob : listaGiorno) {
                if (ob != null && ob.getCodiceOrdine() != null
                        && ob.getCodiceOrdine().equalsIgnoreCase(target)) {
                    notificaEsito.accept("Ordine " + target + " presente nello Storico Ordini.", false);
                    return false;
                }
            }
        }
        return false;
    }

    public void confermaOrdineSelezionato() {
        OrdineBean sel = listaOrdiniStaff != null ? listaOrdiniStaff.getSelectionModel().getSelectedItem() : null;
        if (sel == null || OrdiniStaffView.isHeaderGiorno(sel)) {
            notificaEsito.accept("⚠️ Seleziona prima un ordine dalla lista da confermare.", true);
            return;
        }
        eseguiConfermaOrdine(sel.getCodiceOrdine());
    }

    public void disdiciOrdineSelezionato() {
        OrdineBean sel = listaOrdiniStaff != null ? listaOrdiniStaff.getSelectionModel().getSelectedItem() : null;
        if (sel == null || OrdiniStaffView.isHeaderGiorno(sel)) {
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
