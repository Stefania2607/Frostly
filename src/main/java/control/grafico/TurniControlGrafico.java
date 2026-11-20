package control.grafico;

import bean.IngredienteBean;
import bean.NotificaBean;
import bean.OrdineBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import java.util.ArrayList;
import java.util.List;
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
import view.AvvisiStaffView;
import view.VetrinaMenuView;

/**
 * Controller FXML Coordinator per le schermate Staff ('maestro.fxml', 'dipendente.fxml',
 * 'maestro_gui2.fxml', 'dipendente_gui2.fxml').
 * Rispetta rigorosamente il principio di singola responsabilità (SRP):
 * coordina il caricamento FXML e delega ciascun sotto-dominio a un controller dedicato.
 */
public class TurniControlGrafico {

    @FXML private TabPane tabPaneMaestro;
    @FXML private Button btnTendinaUrgenze;
    @FXML private VBox tendinaUrgenze;
    @FXML private ListView<String> listaNotificheUrgenti;
    @FXML private Label lblIntestazione;
    @FXML private Label lblEsito;
    @FXML private ListView<NotificaBean> listaNotifiche;

    // Turni ed Emergenze
    @FXML private VBox boxCalendarioTurni;
    @FXML private javafx.scene.control.DatePicker datePickerSettimana;
    @FXML private Label lblSettimanaAttiva;
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
    @FXML private ComboBox<String> comboCategoriaMagazzino;
    @FXML private TextField campoRicercaMagazzino;
    @FXML private ListView<String> listaUrgenzeMagazzino;
    @FXML private TextField campoNomeIngredienteRifornimento;
    @FXML private TextField campoKgRifornimento;
    @FXML private TextField campoIngredienteUrgente;
    @FXML private TextField campoNuovoNomeIngrediente;
    @FXML private TextField campoNuovaQtaIngrediente;
    @FXML private TextField campoNuovaSogliaIngrediente;
    @FXML private ComboBox<String> comboNuovaUnitaIngrediente;
    @FXML private ListView<String> listaRichiesteGusto;
    @FXML private TextField campoCodRichiesta;
    @FXML private TextField campoRispostaGusto;

    // Campi delegati a OrdiniStaffControlGrafico
    @FXML private ListView<OrdineBean> listaOrdiniStaff;
    @FXML private Label lblConteggioOrdiniStaff;
    @FXML private ListView<String> listaOrdiniUrgentiTendina;
    @FXML private VBox boxStatisticheMaestro;

    // Campi Sezione Recensioni Staff (Maestro)
    @FXML private VBox boxCardsRecensioniMaestro;
    @FXML private Label lblRiepilogoRecensioniMaestro;

    // Controller dedicati (SRP)
    private CatalogoMenuStaffControlGrafico catalogoMenuHelper;
    private MagazzinoGustiStaffControlGrafico magazzinoGustiHelper;
    private OrdiniStaffControlGrafico ordiniStaffHelper;
    private TurniStaffControlGrafico turniStaffHelper;
    private EmergenzeControlGrafico emergenzeHelper;
    private GestioneDipendentiControlGrafico dipendentiHelper;
    private RecensioniControlGrafico recensioniHelper;

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

        catalogoMenuHelper = new CatalogoMenuStaffControlGrafico(this::mostraEsito, this::aggiornaListe);
        catalogoMenuHelper.legaCampiForm(
                lblCategoriaFormMenu, campoNomeGustoMenu, campoDescGustoMenu,
                campoAllergeniGustoMenu, campoPrezzoGustoMenu);
        catalogoMenuHelper.legaCampiFoto(campoImmagineMenu, lblStatoFotoMenu, btnRimuoviFoto);
        catalogoMenuHelper.legaControlliVetrina(
                comboCategoriaMenu, comboSottocategoriaMenu, boxCategorieMenu,
                boxSottocategorieMenu, grigliaMenuGestione, listaMenuGestione);
        catalogoMenuHelper.inizializza();

        magazzinoGustiHelper = new MagazzinoGustiStaffControlGrafico(this::mostraEsito, this::aggiornaListe);
        magazzinoGustiHelper.legaCampiMagazzino(
                listaMagazzino, lblRiepilogoMagazzino, listaUrgenzeMagazzino,
                campoNomeIngredienteRifornimento, campoKgRifornimento, campoIngredienteUrgente);
        magazzinoGustiHelper.legaFiltriMagazzino(comboCategoriaMagazzino, campoRicercaMagazzino);
        magazzinoGustiHelper.legaCampiNuovoIngrediente(
                campoNuovoNomeIngrediente, campoNuovaQtaIngrediente, campoNuovaSogliaIngrediente,
                comboNuovaUnitaIngrediente);
        magazzinoGustiHelper.legaCampiRichiesteGusto(listaRichiesteGusto, campoCodRichiesta, campoRispostaGusto);
        magazzinoGustiHelper.inizializza(() -> selezionaTab(3));

        ordiniStaffHelper = new OrdiniStaffControlGrafico(
                listaOrdiniStaff, lblConteggioOrdiniStaff, listaOrdiniUrgentiTendina,
                this::mostraEsito, this::aggiornaListe);
        ordiniStaffHelper.inizializza();

        turniStaffHelper = new TurniStaffControlGrafico(
                lblConteggioTurni, lblRiepilogoTurniDipendente, lblTurnoSelezionatoEmergenza,
                listaTurniDipendente, listaTuttiTurni, comboDipendenteTurno, comboGiornoTurno,
                campoEmailDip, campoGiorno, campoOraInizio, campoOraFine, campoCodiceTurno,
                this::mostraEsito, this::aggiornaListe);
        turniStaffHelper.legaControlliCalendario(boxCalendarioTurni, datePickerSettimana, lblSettimanaAttiva);
        turniStaffHelper.inizializza();

        emergenzeHelper = new EmergenzeControlGrafico(
                listaEmergenze, comboMotivazioneRapida, comboSostituto,
                campoCodiceTurno, campoMotivazione, campoCodEmergenza, campoEmailSostituto,
                campoNotaMaestro, tendinaUrgenze, btnTendinaUrgenze,
                this::mostraEsito, this::aggiornaListe);
        emergenzeHelper.inizializza();

        dipendentiHelper = new GestioneDipendentiControlGrafico(
                listaDipendentiStaff, campoNomeNuovoDip, campoCognomeNuovoDip,
                campoEmailNuovoDip, campoPasswordNuovoDip, comboDipendenteTurno, comboSostituto,
                this::mostraEsito, this::aggiornaListe);
        dipendentiHelper.inizializza();

        recensioniHelper = new RecensioniControlGrafico(this::mostraEsito, this::aggiornaListe);
        recensioniHelper.legaCampiMaestro(boxCardsRecensioniMaestro, lblRiepilogoRecensioniMaestro);
        recensioniHelper.aggiornaRecensioniMaestro();

        if (listaNotifiche != null) {
            AvvisiStaffView.configuraListaAvvisi(listaNotifiche, notifica -> {
                Window owner = listaNotifiche.getScene() != null ? listaNotifiche.getScene().getWindow() : null;
                AvvisiStaffView.mostraPopupDettaglioAvviso(owner, notifica, this::selezionaTabPerNome, this::eliminaNotifica);
            }, this::eliminaNotifica);
        }

        aggiornaListe();

        ordiniStaffHelper.avviaMonitoraggioRealTime(attuali -> {
            aggiornaListe();
            if (tendinaUrgenze != null) {
                tendinaUrgenze.setVisible(true);
                tendinaUrgenze.setManaged(true);
            }
            mostraEsito("🚨 NUOVO ORDINE DELIVEROO RICEVUTO! (" + attuali + " in attesa)", false);
        });
    }
    @FXML public void onToggleTendinaUrgenze() { emergenzeHelper.toggleTendinaUrgenze(); }
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

    private void selezionaTabPerNome(String nomeTarget) {
        if (tabPaneMaestro == null || nomeTarget == null) return;
        String search = nomeTarget.toLowerCase();
        for (javafx.scene.control.Tab tab : tabPaneMaestro.getTabs()) {
            String titolo = tab.getText();
            if ((titolo == null || titolo.isBlank()) && tab.getGraphic() instanceof Label lbl) {
                titolo = lbl.getText();
            }
            if (titolo != null && titolo.toLowerCase().contains(search)) {
                tabPaneMaestro.getSelectionModel().select(tab);
                break;
            }
        }
    }
    @FXML public void onClickScegliFoto() { catalogoMenuHelper.scegliFoto(); }
    @FXML public void onClickRimuoviFoto() { catalogoMenuHelper.rimuoviFoto(); }
    @FXML public void onClickNuovoGustoMenu() { catalogoMenuHelper.preparaNuovoGusto(); }
    @FXML public void onClickSalvaGustoMenu() { catalogoMenuHelper.salvaOAggiornaGusto(); }
    @FXML public void onClickCambiaDisponibilitaGusto() { catalogoMenuHelper.cambiaDisponibilitaGusto(); }
    @FXML public void onClickRimuoviGustoMenu() { catalogoMenuHelper.eliminaGusto(); }
    @FXML public void onClickRegistraRifornimentoManuale() { magazzinoGustiHelper.registraRifornimentoManuale(); }
    @FXML public void onClickRegistraScaricoManuale() { magazzinoGustiHelper.registraScaricoManuale(); }
    @FXML public void onClickAggiungiNuovoIngrediente() { magazzinoGustiHelper.aggiungiNuovoIngrediente(); }
    @FXML public void onClickEliminaIngredienteMagazzino() { magazzinoGustiHelper.eliminaIngredienteSelezionato(); }
    @FXML public void onClickRifornisciUrgente() { magazzinoGustiHelper.rifornisciUrgente(); }
    @FXML public void onClickApprovaGusto() { magazzinoGustiHelper.approvaGusto(); }
    @FXML public void onClickRifornisciGusto() { magazzinoGustiHelper.rifornisciEAprovaGusto(); }
    @FXML public void onClickRifiutaGusto() { magazzinoGustiHelper.rifiutaGusto(); }
    @FXML public void onClickAggiornaRecensioniStaff() {
        if (recensioniHelper != null) {
            recensioniHelper.aggiornaRecensioniMaestro();
        }
        mostraEsito("Recensioni aggiornate.", false);
    }
    @FXML
    public void onClickAggiornaOrdiniStaff() {
        aggiornaListe();
        mostraEsito("Elenco ordini aggiornato.", false);
    }

    @FXML public void onClickConfermaOrdineStaff() { ordiniStaffHelper.confermaOrdineSelezionato(); }
    @FXML public void onClickDisdiciOrdineStaff() { ordiniStaffHelper.disdiciOrdineSelezionato(); }
    @FXML public void onClickGeneraPassword() { dipendentiHelper.generaPasswordSicura(); }
    @FXML public void onClickCreaDipendente() { dipendentiHelper.creaDipendente(); }
    @FXML public void onClickEliminaDipendente() { dipendentiHelper.eliminaDipendente(); }
    @FXML public void onClickSettimanaPrecedente() { turniStaffHelper.vaiASettimanaPrecedente(); }
    @FXML public void onClickSettimanaSuccessiva() { turniStaffHelper.vaiASettimanaSuccessiva(); }
    @FXML public void onClickSettimanaCorrente() { turniStaffHelper.vaiASettimanaCorrente(); }
    @FXML public void onCambioDataPicker() { turniStaffHelper.onCambioDataPicker(); }

    @FXML public void onClickPubblicaTurno() { turniStaffHelper.pubblicaNuovoTurno(); }
    @FXML public void onClickPresetMattina() { turniStaffHelper.presetMattina(); }
    @FXML public void onClickPresetPomeriggio() { turniStaffHelper.presetPomeriggio(); }
    @FXML public void onClickPresetChiusura() { turniStaffHelper.presetChiusura(); }

    @FXML public void onClickFiltroTuttiTurni() { turniStaffHelper.impostaFiltroGiorno("TUTTI"); }
    @FXML public void onClickFiltraLunedi() { turniStaffHelper.impostaFiltroGiorno("Lunedi"); }
    @FXML public void onClickFiltraMartedi() { turniStaffHelper.impostaFiltroGiorno("Martedi"); }
    @FXML public void onClickFiltraMercoledi() { turniStaffHelper.impostaFiltroGiorno("Mercoledi"); }
    @FXML public void onClickFiltraGiovedi() { turniStaffHelper.impostaFiltroGiorno("Giovedi"); }
    @FXML public void onClickFiltraVenerdi() { turniStaffHelper.impostaFiltroGiorno("Venerdi"); }
    @FXML public void onClickFiltraSabato() { turniStaffHelper.impostaFiltroGiorno("Sabato"); }
    @FXML public void onClickFiltraDomenica() { turniStaffHelper.impostaFiltroGiorno("Domenica"); }

    @FXML public void onClickSegnalaEmergenza() { emergenzeHelper.segnalaEmergenza(); }
    @FXML public void onClickApprovaEmergenza() { emergenzeHelper.approvaEmergenza(); }
    @FXML public void onClickRifiutaEmergenza() { emergenzeHelper.rifiutaEmergenza(); }

    @FXML
    public void onClickEliminaAvvisoSelezionato() {
        if (listaNotifiche != null) {
            NotificaBean sel = listaNotifiche.getSelectionModel().getSelectedItem();
            if (sel != null) {
                eliminaNotifica(sel);
            } else {
                mostraEsito("Seleziona prima un avviso dalla lista per poterlo eliminare.", true);
            }
        }
    }

    private void eliminaNotifica(NotificaBean notifica) {
        if (notifica == null) return;
        String emailUtente = SessionManager.getInstance().getEmailUtenteLoggato();
        new LoginControl().eliminaNotificaUtente(emailUtente, notifica);
        aggiornaListe();
        mostraEsito("✓ Avviso rimosso dal registro cronologico.", false);
    }

    @FXML
    public void onClickAggiornaStatistiche() {
        aggiornaStatistiche();
        mostraEsito("✓ Statistiche di vendita e magazzino aggiornate.", false);
    }

    private void aggiornaStatistiche() {
        if (boxStatisticheMaestro != null) {
            bean.StatisticheBean stats = new control.applicativo.StatisticheControl().calcolaStatistiche();
            view.StatisticheMaestroView.renderStatistiche(boxStatisticheMaestro, stats);
        }
    }

    @FXML
    public void onClickLogout() {
        SessionManager.getInstance().logout();
        LoginControlGrafico.cambiaSchermata("/login.fxml");
    }

    private void mostraEsito(String msg, boolean errore) {
        VetrinaMenuView.mostraMessaggioEsito(lblEsito, msg, errore);
    }

    private void aggiornaListe() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        String emailUtente = SessionManager.getInstance().getEmailUtenteLoggato();

        turniStaffHelper.aggiornaListeTurni(turniCtrl, emailUtente);
        int emergenzeAttive = emergenzeHelper.aggiornaListaEmergenze(turniCtrl);
        int scorteUrgenti = magazzinoGustiHelper != null ? magazzinoGustiHelper.aggiornaMagazzinoERichieste() : 0;
        if (catalogoMenuHelper != null) {
            catalogoMenuHelper.aggiornaCatalogo();
        }
        int ordiniInAttesa = ordiniStaffHelper != null ? ordiniStaffHelper.aggiornaOrdiniStaff() : 0;

        if (dipendentiHelper != null) {
            dipendentiHelper.aggiornaDipendenti();
        }
        if (recensioniHelper != null) {
            recensioniHelper.aggiornaRecensioniMaestro();
        }

        List<NotificaBean> listaN = new LoginControl().recuperaNotificheUtente(emailUtente);
        List<NotificaBean> listaNOrdinata = new ArrayList<>(listaN);
        listaNOrdinata.sort((a, b) -> {
            boolean aOggi = a.getTimestamp() != null && a.getTimestamp().toLowerCase().contains("oggi");
            boolean bOggi = b.getTimestamp() != null && b.getTimestamp().toLowerCase().contains("oggi");
            if (aOggi && !bOggi) return -1;
            if (!aOggi && bOggi) return 1;
            String tsA = a.getTimestamp() != null ? a.getTimestamp() : "";
            String tsB = b.getTimestamp() != null ? b.getTimestamp() : "";
            return tsB.compareToIgnoreCase(tsA);
        });

        if (listaNotifiche != null) {
            listaNotifiche.getItems().setAll(listaNOrdinata);
        }
        if (listaNotificheUrgenti != null) {
            listaNotificheUrgenti.getItems().clear();
            for (int i = 0; i < Math.min(listaNOrdinata.size(), 8); i++) {
                NotificaBean nb = listaNOrdinata.get(i);
                listaNotificheUrgenti.getItems().add("[" + nb.getTimestamp() + "] " + nb.getTitolo() + ": " + nb.getMessaggio());
            }
        }

        int totaleUrgenze = emergenzeAttive + scorteUrgenti + ordiniInAttesa;
        emergenzeHelper.aggiornaBadgeUrgenze(totaleUrgenze);
        aggiornaStatistiche();
    }
}
