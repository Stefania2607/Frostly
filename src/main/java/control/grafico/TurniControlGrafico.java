package control.grafico;

import bean.EmergenzaBean;
import bean.IngredienteBean;
import bean.LoginBean;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import frostly.exception.FrostlyException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import util.SessionManager;
import view.StaffTurniView;
import view.VetrinaMenuView;

/**
 * Controller Grafico JavaFX principale per lo Staff ('maestro.fxml', 'dipendente.fxml',
 * 'maestro_gui2.fxml', 'dipendente_gui2.fxml').
 * Gestisce direttamente Turni, Emergenze e Profili Dipendenti, delegando:
 * - Catalogo Menu Vetrina a {@link CatalogoMenuStaffControlGrafico}
 * - Magazzino e Richieste Gusto su Misura a {@link MagazzinoGustiStaffControlGrafico}
 * - Ordini Clienti Staff a {@link OrdiniStaffControlGrafico}.
 */
public class TurniControlGrafico {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @FXML private TabPane tabPaneMaestro;
    @FXML private Button btnTendinaUrgenze;
    @FXML private VBox tendinaUrgenze;
    @FXML private ListView<String> listaNotificheUrgenti;
    @FXML private Label lblIntestazione;
    @FXML private Label lblEsito;
    @FXML private ListView<String> listaNotifiche;

    // Turni ed Emergenze
    @FXML private Label lblConteggioTurni;
    @FXML private Label lblRiepilogoTurniDipendente;
    @FXML private Label lblTurnoSelezionatoEmergenza;
    @FXML private ListView<String> listaTurniDipendente;
    @FXML private ListView<String> listaTuttiTurni;
    @FXML private ListView<String> listaEmergenze;
    @FXML private ComboBox<String> comboMotivazioneRapida;
    @FXML private ComboBox<String> comboDipendenteTurno;
    @FXML private ComboBox<String> comboGiornoTurno;
    @FXML private ComboBox<String> comboSostituto;
    @FXML private TextField campoCodiceTurno;
    @FXML private TextField campoMotivazione;
    @FXML private TextField campoEmailDip;
    @FXML private TextField campoGiorno;
    @FXML private TextField campoOraInizio;
    @FXML private TextField campoOraFine;
    @FXML private TextField campoCodEmergenza;
    @FXML private TextField campoEmailSostituto;
    @FXML private TextField campoNotaMaestro;

    // Gestione Dipendenti Staff
    @FXML private ListView<String> listaDipendentiStaff;
    @FXML private TextField campoNomeNuovoDip;
    @FXML private TextField campoCognomeNuovoDip;
    @FXML private TextField campoEmailNuovoDip;
    @FXML private TextField campoPasswordNuovoDip;

    // Campi delegati a CatalogoMenuStaffControlGrafico
    @FXML private Label lblCategoriaFormMenu;
    @FXML private ComboBox<String> comboCategoriaMenu;
    @FXML private ComboBox<String> comboSottocategoriaMenu;
    @FXML private TextField campoNomeGustoMenu;
    @FXML private TextField campoDescGustoMenu;
    @FXML private TextField campoAllergeniGustoMenu;
    @FXML private TextField campoPrezzoGustoMenu;
    @FXML private TextField campoImmagineMenu;
    @FXML private Label lblStatoFotoMenu;
    @FXML private Button btnRimuoviFoto;
    @FXML private HBox boxCategorieMenu;
    @FXML private HBox boxSottocategorieMenu;
    @FXML private FlowPane grigliaMenuGestione;
    @FXML private ListView<String> listaMenuGestione;

    // Campi delegati a MagazzinoGustiStaffControlGrafico
    @FXML private ListView<IngredienteBean> listaMagazzino;
    @FXML private Label lblRiepilogoMagazzino;
    @FXML private ListView<String> listaUrgenzeMagazzino;
    @FXML private TextField campoNomeIngredienteRifornimento;
    @FXML private TextField campoKgRifornimento;
    @FXML private TextField campoIngredienteUrgente;
    @FXML private ListView<String> listaRichiesteGusto;
    @FXML private TextField campoCodRichiesta;
    @FXML private TextField campoRispostaGusto;

    // Campi delegati a OrdiniStaffControlGrafico
    @FXML private ListView<OrdineBean> listaOrdiniStaff;
    @FXML private Label lblConteggioOrdiniStaff;
    @FXML private ListView<String> listaOrdiniUrgentiTendina;

    private String filtroGiornoCorrente = "TUTTI";
    private final Map<String, EmergenzaBean> mappaEmergenzeAttive = new LinkedHashMap<>();

    private CatalogoMenuStaffControlGrafico catalogoMenuHelper;
    private MagazzinoGustiStaffControlGrafico magazzinoGustiHelper;
    private OrdiniStaffControlGrafico ordiniStaffHelper;

    @FXML
    public void initialize() {
        if (lblIntestazione != null) {
            lblIntestazione.setText("Profilo Attivo: " + SessionManager.getInstance().getNomeUtenteLoggato()
                    + " (" + SessionManager.getInstance().getModalita() + ")");
        }
        if (tendinaUrgenze != null) {
            tendinaUrgenze.setVisible(false);
            tendinaUrgenze.setManaged(false);
        }

        catalogoMenuHelper = new CatalogoMenuStaffControlGrafico(
                lblCategoriaFormMenu, comboCategoriaMenu, comboSottocategoriaMenu,
                campoNomeGustoMenu, campoDescGustoMenu, campoAllergeniGustoMenu,
                campoPrezzoGustoMenu, campoImmagineMenu, lblStatoFotoMenu,
                btnRimuoviFoto, boxCategorieMenu, boxSottocategorieMenu,
                grigliaMenuGestione, listaMenuGestione,
                this::mostraEsito, this::aggiornaListe);
        catalogoMenuHelper.inizializza();

        magazzinoGustiHelper = new MagazzinoGustiStaffControlGrafico(
                listaMagazzino, lblRiepilogoMagazzino, listaUrgenzeMagazzino,
                campoNomeIngredienteRifornimento, campoKgRifornimento, campoIngredienteUrgente,
                listaRichiesteGusto, campoCodRichiesta, campoRispostaGusto,
                this::mostraEsito, this::aggiornaListe);
        magazzinoGustiHelper.inizializza(() -> selezionaTab(3));

        ordiniStaffHelper = new OrdiniStaffControlGrafico(
                listaOrdiniStaff, lblConteggioOrdiniStaff, listaOrdiniUrgentiTendina,
                this::mostraEsito, this::aggiornaListe);
        ordiniStaffHelper.inizializza();

        configuraTurniEdEmergenze();
        aggiornaListe();

        ordiniStaffHelper.avviaMonitoraggioRealTime(attuali -> {
            aggiornaListe();
            if (tendinaUrgenze != null) {
                tendinaUrgenze.setVisible(true);
                tendinaUrgenze.setManaged(true);
            }
            mostraSuccesso("🚨 NUOVO ORDINE DELIVEROO RICEVUTO! (" + attuali + " in attesa)");
        });
    }

    private void configuraTurniEdEmergenze() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();

        if (comboMotivazioneRapida != null) {
            comboMotivazioneRapida.setItems(FXCollections.observableArrayList(
                    "Visita medica improvvisa",
                    "Imprevisto familiare urgente",
                    "Guasto mezzo di trasporto",
                    "Febbre / Indisposizione"));
            comboMotivazioneRapida.setOnAction(e -> {
                if (comboMotivazioneRapida.getValue() != null && campoMotivazione != null) {
                    campoMotivazione.setText(comboMotivazioneRapida.getValue());
                }
            });
        }

        if (comboDipendenteTurno != null) {
            comboDipendenteTurno.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            comboDipendenteTurno.setOnAction(e -> {
                if (comboDipendenteTurno.getValue() != null && campoEmailDip != null) {
                    campoEmailDip.setText(comboDipendenteTurno.getValue());
                }
            });
        }

        if (comboSostituto != null) {
            comboSostituto.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            comboSostituto.setOnAction(e -> {
                if (comboSostituto.getValue() != null && campoEmailSostituto != null) {
                    campoEmailSostituto.setText(comboSostituto.getValue());
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

        StaffTurniView.configuraCelleTurni(listaTuttiTurni, true);
        StaffTurniView.configuraCelleTurni(listaTurniDipendente, false);

        if (listaTuttiTurni != null) {
            listaTuttiTurni.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|")) {
                    String[] parti = newVal.split("\\|");
                    if (parti.length >= 4) {
                        String giorno = parti[1].trim();
                        if (comboGiornoTurno != null) comboGiornoTurno.setValue(giorno);
                        if (campoGiorno != null) campoGiorno.setText(giorno);
                        String orari = parti[2].trim();
                        if (orari.contains("-")) {
                            String[] o = orari.split("-");
                            if (campoOraInizio != null) campoOraInizio.setText(o[0].trim());
                            if (campoOraFine != null) campoOraFine.setText(o[1].trim());
                        }
                    }
                }
            });
        }

        if (listaTurniDipendente != null) {
            listaTurniDipendente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoCodiceTurno != null) {
                    String[] parti = newVal.split("\\|");
                    String cod = parti[0].trim();
                    campoCodiceTurno.setText(cod);
                    if (lblTurnoSelezionatoEmergenza != null && parti.length >= 3) {
                        lblTurnoSelezionatoEmergenza.setText("Turno: " + cod + " • " + parti[1].trim() + " (" + parti[2].trim() + ")");
                    }
                }
            });
        }

        if (listaEmergenze != null) {
            listaEmergenze.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    EmergenzaBean e = mappaEmergenzeAttive.get(newVal);
                    if (e != null) {
                        Platform.runLater(() -> apriPopupDettaglioEmergenza(e));
                    }
                }
            });
        }

        if (listaDipendentiStaff != null) {
            listaDipendentiStaff.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoEmailNuovoDip != null) {
                    String[] parti = newVal.split("\\|");
                    if (parti.length >= 2) {
                        campoEmailNuovoDip.setText(parti[1].trim());
                        String[] nc = parti[0].trim().split(" ");
                        if (nc.length >= 1 && campoNomeNuovoDip != null) campoNomeNuovoDip.setText(nc[0]);
                        if (nc.length >= 2 && campoCognomeNuovoDip != null) campoCognomeNuovoDip.setText(nc[1]);
                    }
                }
            });
        }
    }

    // ================= NAVIGAZIONE DASHBOARD E TENDINA URGENZE =================

    @FXML
    public void onToggleTendinaUrgenze() {
        if (tendinaUrgenze != null) {
            boolean visibile = !tendinaUrgenze.isVisible();
            tendinaUrgenze.setVisible(visibile);
            tendinaUrgenze.setManaged(visibile);
        }
    }

    @FXML public void onClickApriSezioneMenu() { selezionaTab(1); }
    @FXML public void onClickApriSezioneTurni() { selezionaTab(2); }
    @FXML public void onClickApriSezioneMagazzino() { selezionaTab(3); }
    @FXML public void onClickApriSezioneGusti() { selezionaTab(4); }
    @FXML public void onClickApriSezioneStaff() { selezionaTab(5); }
    @FXML public void onClickApriSezioneAvvisi() { selezionaTab(6); }
    @FXML public void onClickApriSezioneOrdini() { selezionaTab(7); }
    @FXML public void onClickTornaDashboard() { selezionaTab(0); }

    private void selezionaTab(int indice) {
        if (tabPaneMaestro != null && tabPaneMaestro.getTabs().size() > indice) {
            tabPaneMaestro.getSelectionModel().select(indice);
        }
    }

    // ================= DELEGA A CATALOGO MENU STAFF =================

    @FXML public void onClickScegliFoto() { catalogoMenuHelper.scegliFoto(); }
    @FXML public void onClickRimuoviFoto() { catalogoMenuHelper.rimuoviFoto(); }
    @FXML public void onClickNuovoGustoMenu() { catalogoMenuHelper.preparaNuovoGusto(); }
    @FXML public void onClickSalvaGustoMenu() { catalogoMenuHelper.salvaOAggiornaGusto(); }
    @FXML public void onClickCambiaDisponibilitaGusto() { catalogoMenuHelper.cambiaDisponibilitaGusto(); }
    @FXML public void onClickRimuoviGustoMenu() { catalogoMenuHelper.eliminaGusto(); }

    // ================= DELEGA A MAGAZZINO & GUSTI SU MISURA =================

    @FXML public void onClickRegistraRifornimentoManuale() { magazzinoGustiHelper.registraRifornimentoManuale(); }
    @FXML public void onClickRifornisciUrgente() { magazzinoGustiHelper.rifornisciUrgente(); }
    @FXML public void onClickApprovaGusto() { magazzinoGustiHelper.approvaGusto(); }
    @FXML public void onClickRifornisciGusto() { magazzinoGustiHelper.rifornisciEAprovaGusto(); }
    @FXML public void onClickRifiutaGusto() { magazzinoGustiHelper.rifiutaGusto(); }

    // ================= DELEGA A ORDINI CLIENTI STAFF =================

    @FXML
    public void onClickAggiornaOrdiniStaff() {
        aggiornaListe();
        mostraSuccesso("Elenco ordini aggiornato.");
    }

    @FXML public void onClickConfermaOrdineStaff() { ordiniStaffHelper.confermaOrdineSelezionato(); }
    @FXML public void onClickDisdiciOrdineStaff() { ordiniStaffHelper.disdiciOrdineSelezionato(); }

    // ================= GESTIONE PROFILI DIPENDENTI STAFF =================

    @FXML
    public void onClickGeneraPassword() {
        String alfabeto = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(alfabeto.charAt(SECURE_RANDOM.nextInt(alfabeto.length())));
        }
        campoPasswordNuovoDip.setText(sb.toString());
    }

    @FXML
    public void onClickCreaDipendente() {
        try {
            LoginBean nuovo = new LoginBean();
            nuovo.setNome(campoNomeNuovoDip.getText());
            nuovo.setCognome(campoCognomeNuovoDip.getText());
            nuovo.setEmail(campoEmailNuovoDip.getText());
            nuovo.setPassword(campoPasswordNuovoDip.getText());
            LoginBean creato = new LoginControl().creaProfiloDipendente(nuovo);
            mostraSuccesso("✅ Profilo dipendente creato per " + creato.getNomeCompleto()
                    + ". Comunicagli email e password: " + creato.getEmail() + " / " + creato.getPassword());
            resetCampiDipendenteEAggiorna();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickEliminaDipendente() {
        try {
            String email = campoEmailNuovoDip != null ? campoEmailNuovoDip.getText() : null;
            if (email == null || email.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona prima un dipendente dalla lista sotto per eliminarlo.");
                return;
            }
            new LoginControl().eliminaProfiloDipendente(email.trim());
            mostraSuccesso("🗑️ Dipendente '" + email.trim() + "' eliminato con successo dallo staff.");
            resetCampiDipendenteEAggiorna();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    private void resetCampiDipendenteEAggiorna() {
        campoNomeNuovoDip.clear();
        campoCognomeNuovoDip.clear();
        campoEmailNuovoDip.clear();
        campoPasswordNuovoDip.clear();
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        if (comboDipendenteTurno != null) {
            comboDipendenteTurno.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
        }
        if (comboSostituto != null) {
            comboSostituto.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
        }
        aggiornaListe();
    }

    // ================= GESTIONE TURNI ED EMERGENZE =================

    @FXML
    public void onClickPubblicaTurno() {
        try {
            TurnoBean bean = new TurnoBean();
            String email = (campoEmailDip != null && !campoEmailDip.getText().trim().isEmpty())
                    ? campoEmailDip.getText().trim()
                    : (comboDipendenteTurno != null ? comboDipendenteTurno.getValue() : null);
            String giorno = (campoGiorno != null && !campoGiorno.getText().trim().isEmpty())
                    ? campoGiorno.getText().trim()
                    : (comboGiornoTurno != null ? comboGiornoTurno.getValue() : null);

            if (email == null || email.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona prima un dipendente dal menu a tendina.");
                return;
            }
            if (giorno == null || giorno.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona il giorno della settimana per il turno.");
                return;
            }

            bean.setEmailDipendente(email);
            bean.setData(giorno);
            bean.setOraInizio(campoOraInizio != null ? campoOraInizio.getText() : "08:00");
            bean.setOraFine(campoOraFine != null ? campoOraFine.getText() : "14:00");

            TurnoBean creato = new GestioneTurniEmergenzeControl().pubblicaNuovoTurno(bean);
            mostraSuccesso("✅ Turno pubblicato con successo: " + creato.getCodiceTurno() + " (" + giorno + " " + bean.getOraInizio() + "-" + bean.getOraFine() + ")");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML public void onClickPresetMattina() {
        if (campoOraInizio != null) campoOraInizio.setText("08:00");
        if (campoOraFine != null) campoOraFine.setText("14:00");
    }
    @FXML public void onClickPresetPomeriggio() {
        if (campoOraInizio != null) campoOraInizio.setText("14:00");
        if (campoOraFine != null) campoOraFine.setText("20:00");
    }
    @FXML public void onClickPresetChiusura() {
        if (campoOraInizio != null) campoOraInizio.setText("18:00");
        if (campoOraFine != null) campoOraFine.setText("00:00");
    }

    @FXML public void onClickFiltroTuttiTurni() { filtroGiornoCorrente = "TUTTI"; aggiornaListe(); }
    @FXML public void onClickFiltraLunedi() { filtroGiornoCorrente = "Lunedi"; aggiornaListe(); }
    @FXML public void onClickFiltraMartedi() { filtroGiornoCorrente = "Martedi"; aggiornaListe(); }
    @FXML public void onClickFiltraMercoledi() { filtroGiornoCorrente = "Mercoledi"; aggiornaListe(); }
    @FXML public void onClickFiltraGiovedi() { filtroGiornoCorrente = "Giovedi"; aggiornaListe(); }
    @FXML public void onClickFiltraVenerdi() { filtroGiornoCorrente = "Venerdi"; aggiornaListe(); }
    @FXML public void onClickFiltraSabato() { filtroGiornoCorrente = "Sabato"; aggiornaListe(); }
    @FXML public void onClickFiltraDomenica() { filtroGiornoCorrente = "Domenica"; aggiornaListe(); }

    @FXML
    public void onClickSegnalaEmergenza() {
        try {
            EmergenzaBean bean = new EmergenzaBean();
            bean.setEmailRichiedente(SessionManager.getInstance().getEmailUtenteLoggato());
            bean.setCodiceTurno(campoCodiceTurno.getText());
            String motScritto = campoMotivazione != null ? campoMotivazione.getText().trim() : "";
            String motRapido = comboMotivazioneRapida != null && comboMotivazioneRapida.getValue() != null ? comboMotivazioneRapida.getValue().trim() : "";
            String motFinale = !motScritto.isEmpty() ? motScritto : (!motRapido.isEmpty() ? motRapido : "Imprevisto personale urgente");
            bean.setMotivazione(motFinale);

            EmergenzaBean creata = new GestioneTurniEmergenzeControl().segnalaEmergenzaTurno(bean);
            mostraSuccesso("✅ Emergenza inviata al Maestro! Codice: " + creata.getCodiceEmergenza());
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickApprovaEmergenza() {
        try {
            new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(
                    campoCodEmergenza.getText(), campoEmailSostituto.getText(), campoNotaMaestro.getText());
            mostraSuccesso("✅ Emergenza approvata e turno riassegnato al sostituto!");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifiutaEmergenza() {
        try {
            new GestioneTurniEmergenzeControl().rifiutaEmergenza(
                    campoCodEmergenza.getText(), campoNotaMaestro.getText());
            mostraSuccesso("ℹ️ Emergenza rifiutata e dipendente notificato.");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    private void apriPopupDettaglioEmergenza(EmergenzaBean e) {
        Window owner = (tendinaUrgenze != null && tendinaUrgenze.getScene() != null)
                ? tendinaUrgenze.getScene().getWindow() : null;
        List<String> emailDipendenti = new GestioneTurniEmergenzeControl().getEmailTuttiDipendenti();

        StaffTurniView.apriPopupDettaglioEmergenza(
                owner,
                e,
                emailDipendenti,
                (sost, nota) -> {
                    try {
                        if (sost == null || sost.trim().isEmpty()) {
                            mostraErrore("⚠️ Seleziona prima un sostituto per riassegnare il turno.");
                            return;
                        }
                        new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(
                                e.getCodiceEmergenza(), sost.trim(), nota);
                        mostraSuccesso("✅ Emergenza approvata: Turno " + e.getCodiceTurno() + " riassegnato a " + sost);
                        aggiornaListe();
                    } catch (FrostlyException ex) {
                        mostraErrore("⚠️ " + ex.getMessage());
                    }
                },
                nota -> {
                    try {
                        new GestioneTurniEmergenzeControl().rifiutaEmergenza(e.getCodiceEmergenza(), nota);
                        mostraSuccesso("ℹ️ Emergenza " + e.getCodiceEmergenza() + " rifiutata.");
                        aggiornaListe();
                    } catch (FrostlyException ex) {
                        mostraErrore("⚠️ " + ex.getMessage());
                    }
                });
    }

    @FXML
    public void onClickLogout() {
        SessionManager.getInstance().logout();
        LoginControlGrafico.cambiaSchermata("/login.fxml");
    }

    private void mostraEsito(String msg, boolean errore) {
        VetrinaMenuView.mostraMessaggioEsito(lblEsito, msg, errore);
    }

    private void mostraSuccesso(String msg) {
        mostraEsito(msg, false);
    }

    private void mostraErrore(String msg) {
        mostraEsito(msg, true);
    }

    private void aggiornaListe() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        String emailUtente = SessionManager.getInstance().getEmailUtenteLoggato();

        aggiornaListeTurni(turniCtrl, emailUtente);

        int emergenzeAttive = aggiornaListaEmergenze(turniCtrl);
        int scorteUrgenti = magazzinoGustiHelper != null ? magazzinoGustiHelper.aggiornaMagazzinoERichieste() : 0;
        if (catalogoMenuHelper != null) {
            catalogoMenuHelper.aggiornaCatalogo();
        }
        int ordiniInAttesa = ordiniStaffHelper != null ? ordiniStaffHelper.aggiornaOrdiniStaff() : 0;

        if (listaDipendentiStaff != null) {
            listaDipendentiStaff.getItems().setAll(new LoginControl().elencoDipendenti());
        }

        List<NotificaBean> listaN = new LoginControl().recuperaNotificheUtente(emailUtente);
        if (listaNotifiche != null) {
            listaNotifiche.getItems().clear();
            for (NotificaBean n : listaN) {
                listaNotifiche.getItems().add("[" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
            }
        }
        if (listaNotificheUrgenti != null) {
            listaNotificheUrgenti.getItems().clear();
            for (int i = 0; i < Math.min(listaN.size(), 8); i++) {
                NotificaBean nb = listaN.get(i);
                listaNotificheUrgenti.getItems().add("[" + nb.getTimestamp() + "] " + nb.getTitolo() + ": " + nb.getMessaggio());
            }
        }

        int totaleUrgenze = emergenzeAttive + scorteUrgenti + ordiniInAttesa;
        StaffTurniView.aggiornaBottoneUrgenze(btnTendinaUrgenze, totaleUrgenze);
    }

    private void aggiornaListeTurni(GestioneTurniEmergenzeControl turniCtrl, String emailUtente) {
        try {
            if (listaTurniDipendente != null) {
                listaTurniDipendente.getItems().clear();
                List<TurnoBean> turniMiei = turniCtrl.visualizzaTurniDipendente(emailUtente);
                turniMiei.sort((t1, t2) -> {
                    int g1 = indiceGiorno(t1.getData());
                    int g2 = indiceGiorno(t2.getData());
                    if (g1 != g2) return Integer.compare(g1, g2);
                    return t1.getOraInizio().compareTo(t2.getOraInizio());
                });

                int oreTotali = 0;
                for (TurnoBean t : turniMiei) {
                    listaTurniDipendente.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                            + " | " + t.getOraInizio() + " - " + t.getOraFine() + " | "
                            + t.getNomeDipendente() + " | " + t.getStato());
                    try {
                        int hInizio = Integer.parseInt(t.getOraInizio().split(":")[0]);
                        int hFine = Integer.parseInt(t.getOraFine().split(":")[0]);
                        oreTotali += Math.max(0, hFine - hInizio);
                    } catch (Exception ignored) {
                        // Formato orario non standard
                    }
                }
                if (lblRiepilogoTurniDipendente != null) {
                    lblRiepilogoTurniDipendente.setText("📅 " + turniMiei.size() + " turni assegnati (" + oreTotali + " ore totali)");
                }
            }
            if (listaTuttiTurni != null) {
                listaTuttiTurni.getItems().clear();
                List<TurnoBean> tuttiTurni = turniCtrl.visualizzaTuttiTurni();
                tuttiTurni.sort((t1, t2) -> {
                    int g1 = indiceGiorno(t1.getData());
                    int g2 = indiceGiorno(t2.getData());
                    if (g1 != g2) return Integer.compare(g1, g2);
                    return t1.getOraInizio().compareTo(t2.getOraInizio());
                });

                int conteggioMostrati = 0;
                int emergenze = 0;
                for (TurnoBean t : tuttiTurni) {
                    if ("IN_EMERGENZA".equalsIgnoreCase(t.getStato())) {
                        emergenze++;
                    }
                    if ("TUTTI".equalsIgnoreCase(filtroGiornoCorrente)
                            || normalizzaGiorno(t.getData()).equalsIgnoreCase(normalizzaGiorno(filtroGiornoCorrente))) {
                        listaTuttiTurni.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                                + " | " + t.getOraInizio() + " - " + t.getOraFine() + " | "
                                + t.getNomeDipendente() + " | " + t.getStato());
                        conteggioMostrati++;
                    }
                }
                if (lblConteggioTurni != null) {
                    String infoFiltro = "TUTTI".equalsIgnoreCase(filtroGiornoCorrente) ? "Tutta la settimana" : filtroGiornoCorrente;
                    lblConteggioTurni.setText("Visualizzati: " + conteggioMostrati + " turni (" + infoFiltro + ")"
                            + (emergenze > 0 ? " • 🚨 " + emergenze + " emergenze attive" : " • ✅ Nessuna emergenza"));
                }
            }
        } catch (FrostlyException ignored) {
            // Ignorato in caricamento vista
        }
    }

    private int aggiornaListaEmergenze(GestioneTurniEmergenzeControl turniCtrl) {
        int emergenzeAttive = 0;
        if (listaEmergenze != null) {
            listaEmergenze.getItems().clear();
            mappaEmergenzeAttive.clear();
        }
        for (EmergenzaBean e : turniCtrl.visualizzaTutteEmergenze()) {
            if (!"IN_ATTESA".equals(e.getStato())) {
                continue;
            }
            emergenzeAttive++;
            if (listaEmergenze != null) {
                String orarioInfo = e.getDataTurno() != null ? (" (" + e.getDataTurno() + " " + e.getOrarioTurno() + ")") : "";
                String sintetica = "🚨 " + e.getNomeRichiedente() + " — Emergenza Turno " + e.getCodiceTurno() + orarioInfo;
                mappaEmergenzeAttive.put(sintetica, e);
                listaEmergenze.getItems().add(sintetica);
            }
        }
        return emergenzeAttive;
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
