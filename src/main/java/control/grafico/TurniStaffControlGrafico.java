package control.grafico;

import bean.EmergenzaBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import frostly.exception.FrostlyException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import util.SessionManager;
import view.TurniStaffView;

/**
 * Controller Grafico dedicato alla pianificazione settimanale su calendario reale,
 * pubblicazione e consultazione tabellare dei Turni di lavoro dello Staff (Maestro e Dipendenti).
 */
public class TurniStaffControlGrafico {

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Label lblConteggioTurni;
    private final Label lblRiepilogoTurniDipendente;
    private final Label lblTurnoSelezionatoEmergenza;
    private final ListView<String> listaTurniDipendente;
    private final ListView<String> listaTuttiTurni;
    private final ComboBox<String> comboDipendenteTurno;
    private final ComboBox<String> comboGiornoTurno;
    private final TextField campoEmailDip;
    private final TextField campoGiorno;
    private final TextField campoOraInizio;
    private final TextField campoOraFine;
    private final TextField campoCodiceTurno;

    private VBox boxCalendarioTurni;
    private DatePicker datePickerSettimana;
    private Label lblSettimanaAttiva;

    private final BiConsumer<String, Boolean> onEsito;
    private final Runnable onRichiediAggiornamento;

    private LocalDate lunediSettimanaAttiva = LocalDate.now().with(DayOfWeek.MONDAY);
    private String filtroGiornoCorrente = "TUTTI";
    private boolean aggiornamentoPickerInCorso = false;

    public TurniStaffControlGrafico(Label lblConteggioTurni,
                                    Label lblRiepilogoTurniDipendente,
                                    Label lblTurnoSelezionatoEmergenza,
                                    ListView<String> listaTurniDipendente,
                                    ListView<String> listaTuttiTurni,
                                    ComboBox<String> comboDipendenteTurno,
                                    ComboBox<String> comboGiornoTurno,
                                    TextField campoEmailDip,
                                    TextField campoGiorno,
                                    TextField campoOraInizio,
                                    TextField campoOraFine,
                                    TextField campoCodiceTurno,
                                    BiConsumer<String, Boolean> onEsito,
                                    Runnable onRichiediAggiornamento) {
        this.lblConteggioTurni = lblConteggioTurni;
        this.lblRiepilogoTurniDipendente = lblRiepilogoTurniDipendente;
        this.lblTurnoSelezionatoEmergenza = lblTurnoSelezionatoEmergenza;
        this.listaTurniDipendente = listaTurniDipendente;
        this.listaTuttiTurni = listaTuttiTurni;
        this.comboDipendenteTurno = comboDipendenteTurno;
        this.comboGiornoTurno = comboGiornoTurno;
        this.campoEmailDip = campoEmailDip;
        this.campoGiorno = campoGiorno;
        this.campoOraInizio = campoOraInizio;
        this.campoOraFine = campoOraFine;
        this.campoCodiceTurno = campoCodiceTurno;
        this.onEsito = onEsito;
        this.onRichiediAggiornamento = onRichiediAggiornamento;
    }

    public void legaControlliCalendario(VBox boxCalendarioTurni,
                                        DatePicker datePickerSettimana,
                                        Label lblSettimanaAttiva) {
        this.boxCalendarioTurni = boxCalendarioTurni;
        this.datePickerSettimana = datePickerSettimana;
        this.lblSettimanaAttiva = lblSettimanaAttiva;

        if (this.datePickerSettimana != null) {
            this.aggiornamentoPickerInCorso = true;
            this.datePickerSettimana.setValue(lunediSettimanaAttiva);
            this.aggiornamentoPickerInCorso = false;
        }
    }

    public void inizializza() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();

        if (comboDipendenteTurno != null) {
            comboDipendenteTurno.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            comboDipendenteTurno.setOnAction(e -> {
                if (comboDipendenteTurno.getValue() != null && campoEmailDip != null) {
                    campoEmailDip.setText(comboDipendenteTurno.getValue());
                }
            });
        }

        if (comboGiornoTurno != null) {
            comboGiornoTurno.setItems(FXCollections.observableArrayList(
                    "Lunedi", "Martedi", "Mercoledi", "Giovedi", "Venerdi", "Sabato", "Domenica"));
            comboGiornoTurno.setOnAction(e -> {
                if (comboGiornoTurno.getValue() != null && campoGiorno != null) {
                    campoGiorno.setText(comboGiornoTurno.getValue());
                }
            });
        }

        TurniStaffView.configuraCelleTurni(listaTuttiTurni, true);
        TurniStaffView.configuraCelleTurni(listaTurniDipendente, false);
    }

    public void vaiASettimanaPrecedente() {
        lunediSettimanaAttiva = lunediSettimanaAttiva.minusWeeks(1);
        sincronizzaDatePicker();
        onRichiediAggiornamento.run();
    }

    public void vaiASettimanaSuccessiva() {
        lunediSettimanaAttiva = lunediSettimanaAttiva.plusWeeks(1);
        sincronizzaDatePicker();
        onRichiediAggiornamento.run();
    }

    public void vaiASettimanaCorrente() {
        lunediSettimanaAttiva = LocalDate.now().with(DayOfWeek.MONDAY);
        sincronizzaDatePicker();
        onRichiediAggiornamento.run();
    }

    public void onCambioDataPicker() {
        if (aggiornamentoPickerInCorso || datePickerSettimana == null || datePickerSettimana.getValue() == null) {
            return;
        }
        lunediSettimanaAttiva = datePickerSettimana.getValue().with(DayOfWeek.MONDAY);
        onRichiediAggiornamento.run();
    }

    private void sincronizzaDatePicker() {
        if (datePickerSettimana != null) {
            aggiornamentoPickerInCorso = true;
            datePickerSettimana.setValue(lunediSettimanaAttiva);
            aggiornamentoPickerInCorso = false;
        }
    }

    public void pubblicaNuovoTurno() {
        try {
            TurnoBean bean = new TurnoBean();
            String email = (campoEmailDip != null && !campoEmailDip.getText().trim().isEmpty())
                    ? campoEmailDip.getText().trim()
                    : (comboDipendenteTurno != null ? comboDipendenteTurno.getValue() : null);
            String giorno = (campoGiorno != null && !campoGiorno.getText().trim().isEmpty())
                    ? campoGiorno.getText().trim()
                    : (comboGiornoTurno != null ? comboGiornoTurno.getValue() : null);

            if (email == null || email.trim().isEmpty()) {
                onEsito.accept("⚠️ Seleziona prima un dipendente.", true);
                return;
            }
            if (giorno == null || giorno.trim().isEmpty()) {
                onEsito.accept("⚠️ Seleziona il giorno della settimana per il turno.", true);
                return;
            }

            bean.setEmailDipendente(email);
            bean.setData(giorno);
            bean.setOraInizio(campoOraInizio != null ? campoOraInizio.getText() : "07:00");
            bean.setOraFine(campoOraFine != null ? campoOraFine.getText() : "14:00");

            TurnoBean creato = new GestioneTurniEmergenzeControl().pubblicaNuovoTurno(bean);
            onEsito.accept("✅ Turno pubblicato con successo: " + creato.getCodiceTurno()
                    + " (" + giorno + " " + bean.getOraInizio() + "-" + bean.getOraFine() + ")", false);
            onRichiediAggiornamento.run();
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void presetMattina() {
        if (campoOraInizio != null) campoOraInizio.setText("07:00");
        if (campoOraFine != null) campoOraFine.setText("14:00");
    }

    public void presetPomeriggio() {
        if (campoOraInizio != null) campoOraInizio.setText("14:00");
        if (campoOraFine != null) campoOraFine.setText("20:00");
    }

    public void presetChiusura() {
        if (campoOraInizio != null) campoOraInizio.setText("20:00");
        if (campoOraFine != null) campoOraFine.setText("00:00");
    }

    public void impostaFiltroGiorno(String giorno) {
        this.filtroGiornoCorrente = giorno;
        onRichiediAggiornamento.run();
    }

    public void aggiornaListeTurni(GestioneTurniEmergenzeControl turniCtrl, String emailUtente) {
        try {
            LocalDate domenicaSettimana = lunediSettimanaAttiva.plusDays(6);
            if (lblSettimanaAttiva != null) {
                lblSettimanaAttiva.setText("📅 Settimana: Lunedì " + lunediSettimanaAttiva.format(FMT_DATA)
                        + " — Domenica " + domenicaSettimana.format(FMT_DATA));
            }

            List<TurnoBean> tuttiTurni = turniCtrl.visualizzaTuttiTurni();
            boolean isMaestro = "MAESTRO_GELATAIO".equalsIgnoreCase(SessionManager.getInstance().getRuoloUtenteLoggato());

            if (boxCalendarioTurni != null) {
                TurniStaffView.popolaCalendarioSettimanale(
                        boxCalendarioTurni,
                        lunediSettimanaAttiva,
                        tuttiTurni,
                        isMaestro,
                        emailUtente,
                        (indGiorno, indFascia, chiaveDataGiorno, titoloGiornoEsteso) ->
                                apriDialogGestioneCellaMaestro(indGiorno, indFascia, chiaveDataGiorno, titoloGiornoEsteso, tuttiTurni),
                        turnoCliccato ->
                                gestisciClickTurnoDaDipendente(turnoCliccato, emailUtente)
                );
            }

            aggiornaTurniPersonali(turniCtrl, emailUtente);
            aggiornaTurniTotali(tuttiTurni);
        } catch (FrostlyException ignored) {
            // Ignorato in caricamento vista
        }
    }

    private void apriDialogGestioneCellaMaestro(int indiceGiorno,
                                                int indiceFascia,
                                                String chiaveDataGiorno,
                                                String titoloGiornoEsteso,
                                                List<TurnoBean> tuttiTurni) {
        String oraInizio = TurniStaffView.FASCE_ORARIE[indiceFascia][0];
        String oraFine = TurniStaffView.FASCE_ORARIE[indiceFascia][1];
        String rangeLabel = TurniStaffView.FASCE_ORARIE[indiceFascia][2];

        LocalDate dataGiorno = lunediSettimanaAttiva.plusDays(indiceGiorno);
        boolean isSettimanaCorrente = lunediSettimanaAttiva.equals(LocalDate.now().with(DayOfWeek.MONDAY));

        List<TurnoBean> turniCella = TurniStaffView.filtraTurniPerCella(
                tuttiTurni, indiceGiorno, dataGiorno, isSettimanaCorrente, indiceFascia, oraInizio, oraFine);

        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        List<String> dipendentiFormattati = turniCtrl.getDipendentiFormattati();

        Window owner = boxCalendarioTurni != null && boxCalendarioTurni.getScene() != null
                ? boxCalendarioTurni.getScene().getWindow() : null;

        TurniStaffView.apriPopupGestioneIncrocioMaestro(
                owner,
                titoloGiornoEsteso,
                rangeLabel,
                turniCella,
                dipendentiFormattati,
                turniCtrl::trovaEmergenzaAttivaPerTurno,
                emailDaAssegnare -> {
                    try {
                        TurnoBean nuovo = new TurnoBean();
                        nuovo.setEmailDipendente(emailDaAssegnare);
                        nuovo.setData(chiaveDataGiorno);
                        nuovo.setOraInizio(oraInizio);
                        nuovo.setOraFine(oraFine);
                        TurnoBean creato = turniCtrl.pubblicaNuovoTurno(nuovo);
                        onEsito.accept("✅ Turno " + creato.getCodiceTurno() + " assegnato a "
                                + creato.getNomeDipendente() + " per " + titoloGiornoEsteso + " (" + rangeLabel + ").", false);
                        onRichiediAggiornamento.run();
                    } catch (FrostlyException ex) {
                        onEsito.accept("⚠️ " + ex.getMessage(), true);
                    }
                },
                codiceTurnoDaRimuovere -> {
                    try {
                        turniCtrl.eliminaTurno(codiceTurnoDaRimuovere);
                        onEsito.accept("🗑️ Turno " + codiceTurnoDaRimuovere + " rimosso dal calendario.", false);
                        onRichiediAggiornamento.run();
                    } catch (FrostlyException ex) {
                        onEsito.accept("⚠️ " + ex.getMessage(), true);
                    }
                },
                (codEmergenza, emailSostituto, nota) -> {
                    try {
                        EmergenzaBean risolta = turniCtrl.approvaEmergenzaConSostituto(codEmergenza, emailSostituto, nota);
                        onEsito.accept("✔ Emergenza " + risolta.getCodiceEmergenza()
                                + " approvata! Turno riassegnato a " + risolta.getNomeSostituto() + ".", false);
                        onRichiediAggiornamento.run();
                    } catch (FrostlyException ex) {
                        onEsito.accept("⚠️ " + ex.getMessage(), true);
                    }
                },
                (codEmergenza, notaRifiuto) -> {
                    try {
                        turniCtrl.rifiutaEmergenza(codEmergenza, notaRifiuto);
                        onEsito.accept("✕ Emergenza " + codEmergenza + " rifiutata.", false);
                        onRichiediAggiornamento.run();
                    } catch (FrostlyException ex) {
                        onEsito.accept("⚠️ " + ex.getMessage(), true);
                    }
                }
        );
    }

    private void gestisciClickTurnoDaDipendente(TurnoBean turnoCliccato, String emailUtenteLoggato) {
        if (turnoCliccato == null) return;
        if (emailUtenteLoggato == null || !emailUtenteLoggato.equalsIgnoreCase(turnoCliccato.getEmailDipendente())) {
            onEsito.accept("ℹ️ Turno assegnato a " + turnoCliccato.getNomeDipendente()
                    + ". Seleziona un tuo turno (evidenziato in verde ⭐) per reclamare un problema o chiedere modifiche.", false);
            return;
        }

        Window owner = boxCalendarioTurni != null && boxCalendarioTurni.getScene() != null
                ? boxCalendarioTurni.getScene().getWindow() : null;

        TurniStaffView.apriPopupEmergenzaDipendente(owner, turnoCliccato, motivazione -> {
            try {
                EmergenzaBean emBean = new EmergenzaBean();
                emBean.setCodiceTurno(turnoCliccato.getCodiceTurno());
                emBean.setEmailRichiedente(emailUtenteLoggato);
                emBean.setMotivazione(motivazione);

                EmergenzaBean creata = new GestioneTurniEmergenzeControl().segnalaEmergenzaTurno(emBean);
                onEsito.accept("🚨 Emergenza " + creata.getCodiceEmergenza() + " inviata al Maestro per il turno "
                        + turnoCliccato.getCodiceTurno() + " (" + turnoCliccato.getData() + ").", false);
                onRichiediAggiornamento.run();
            } catch (FrostlyException ex) {
                onEsito.accept("⚠️ " + ex.getMessage(), true);
            }
        });
    }

    private void aggiornaTurniPersonali(GestioneTurniEmergenzeControl turniCtrl, String emailUtente) throws FrostlyException {
        List<TurnoBean> turniMiei = turniCtrl.visualizzaTurniDipendente(emailUtente);
        ordinaTurni(turniMiei);

        if (listaTurniDipendente != null) {
            listaTurniDipendente.getItems().clear();
        }
        int oreTotali = 0;
        for (TurnoBean t : turniMiei) {
            if (listaTurniDipendente != null) {
                listaTurniDipendente.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                        + " | " + t.getOraInizio() + " - " + t.getOraFine() + " | "
                        + t.getNomeDipendente() + " | " + t.getStato());
            }
            try {
                int hInizio = Integer.parseInt(t.getOraInizio().split(":")[0]);
                int hFine = Integer.parseInt(t.getOraFine().split(":")[0]);
                if (hFine == 0) hFine = 24;
                oreTotali += Math.max(0, hFine - hInizio);
            } catch (Exception ignored) {
                // Formato orario non standard
            }
        }
        if (lblRiepilogoTurniDipendente != null) {
            lblRiepilogoTurniDipendente.setText("📅 Tuoi turni assegnati: " + turniMiei.size()
                    + " (" + oreTotali + " ore) • Clicca su un tuo turno (⭐) nel calendario per segnalare un problema");
        }
    }

    private void aggiornaTurniTotali(List<TurnoBean> tuttiTurni) {
        ordinaTurni(tuttiTurni);
        if (listaTuttiTurni != null) {
            listaTuttiTurni.getItems().clear();
        }

        int conteggioMostrati = 0;
        int emergenze = 0;
        for (TurnoBean t : tuttiTurni) {
            if ("IN_EMERGENZA".equalsIgnoreCase(t.getStato())) {
                emergenze++;
            }
            if ("TUTTI".equalsIgnoreCase(filtroGiornoCorrente)
                    || normalizzaGiorno(t.getData()).contains(normalizzaGiorno(filtroGiornoCorrente))) {
                if (listaTuttiTurni != null) {
                    listaTuttiTurni.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                            + " | " + t.getOraInizio() + " - " + t.getOraFine() + " | "
                            + t.getNomeDipendente() + " | " + t.getStato());
                }
                conteggioMostrati++;
            }
        }
        if (lblConteggioTurni != null) {
            lblConteggioTurni.setText("Totale turni pianificati: " + conteggioMostrati
                    + (emergenze > 0 ? " • 🚨 " + emergenze + " emergenze attive" : " • ✅ Nessuna emergenza"));
        }
    }

    private static void ordinaTurni(List<TurnoBean> lista) {
        lista.sort((t1, t2) -> {
            int g1 = indiceGiorno(t1.getData());
            int g2 = indiceGiorno(t2.getData());
            if (g1 != g2) return Integer.compare(g1, g2);
            return t1.getOraInizio().compareTo(t2.getOraInizio());
        });
    }

    private static int indiceGiorno(String g) {
        if (g == null) return 7;
        String s = g.toLowerCase().replace("ì", "i");
        if (s.contains("lun")) return 0;
        if (s.contains("mar")) return 1;
        if (s.contains("mer")) return 2;
        if (s.contains("gio")) return 3;
        if (s.contains("ven")) return 4;
        if (s.contains("sab")) return 5;
        if (s.contains("dom")) return 6;
        return 7;
    }

    private static String normalizzaGiorno(String g) {
        if (g == null) return "";
        return g.toLowerCase().replace("ì", "i").trim();
    }
}
