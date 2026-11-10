package control.grafico;

import bean.CarrelloBean;
import bean.GustoBean;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.RichiestaGustoBean;
import bean.VaschettaItemBean;
import control.applicativo.GestioneMenuControl;
import control.applicativo.LoginControl;
import control.applicativo.OrdiniControl;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import util.SessionManager;
import view.CarrelloView;
import view.ClienteMenuView;
import view.CrepeConfiguratoreView;
import view.VetrinaMenuView;

/**
 * Controller Grafico JavaFX principale per la schermata del Cliente ('cliente.fxml', 'cliente_gui2.fxml').
 * Coordina la navigazione del catalogo prodotti per macro-categorie, delegando:
 * - la composizione della vaschetta a {@link VaschettaControlGrafico}
 * - le proposte "Gusto su Misura" a {@link RichiestaGustoControlGrafico}.
 */
public class GelateriaControlGrafico {

    private static final List<String> ORDINE_CATEGORIE = Arrays.asList(
            "Gelati", "Crepes", "Panna", "Cioccolate calde", "Bevande", "Caffetteria", "Biscotti", "Crostate", "Tisane");

    @FXML private Label lblIntestazione;
    @FXML private Label lblEsito;
    @FXML private Button btnApriCarrello;

    // Navigazione Categorie e Griglia Prodotti
    @FXML private HBox boxCategorieCliente;
    @FXML private VBox boxGustiDisponibili;
    @FXML private VBox boxSezioneVaschetta;

    // Pannello Destro: Composizione Vaschetta
    @FXML private Button btnFormato350;
    @FXML private Button btnFormato500;
    @FXML private Button btnFormato750;
    @FXML private Button btnFormato1000;
    @FXML private Label lblCapienzaVaschetta;
    @FXML private HBox boxVaschettaGrafica;
    @FXML private CheckBox checkPanna;
    @FXML private CheckBox checkGranella;
    @FXML private Label lblPrezzoVaschettaCorrente;

    // Storico Ordini e Notifiche
    @FXML private ListView<OrdineBean> listaMieiOrdini;
    @FXML private ListView<String> listaNotificheCliente;

    // Sezione Recensioni Clienti
    @FXML private ComboBox<String> comboVotoRecensione;
    @FXML private TextField campoTitoloRecensione;
    @FXML private javafx.scene.control.TextArea campoCommentoRecensione;
    @FXML private VBox boxCardsRecensioniCliente;
    @FXML private Label lblMediaRecensioniCliente;

    // Campi Tab "Gusto su Misura" (compatibilita')
    @FXML private TextField campoNomeGustoCustom;
    @FXML private TextField campoDescrizioneCustom;
    @FXML private ComboBox<String> comboIngredienteCustom;
    @FXML private TextField campoIngredienteCustom;
    @FXML private TextField campoKgCustom;
    @FXML private TextField campoDataCustom;
    @FXML private Label lblRiepilogoNomeCustom;
    @FXML private Label lblRiepilogoIngredienteCustom;
    @FXML private Label lblRiepilogoDescrizioneCustom;
    @FXML private Label lblRiepilogoKgDataCustom;
    @FXML private ListView<String> listaMieRichieste;

    private final CarrelloBean carrello = new CarrelloBean();
    private final List<GustoBean> tuttiProdottiMenu = new ArrayList<>();
    private final List<GustoBean> catalogoGustiGelato = new ArrayList<>();

    private String categoriaCatalogoSelezionata = null;
    private String sottocategoriaCatalogoSelezionata = "Tutti";
    private GustoBean crepeSelezionata = null;
    private boolean mostraPannelloToppingCrepe = false;
    private String toppingCrepeScelto = CrepeConfiguratoreView.NESSUN_TOPPING;
    private String granellaCrepeScelta = CrepeConfiguratoreView.NESSUNA_GRANELLA;

    private RichiestaGustoControlGrafico gustoCustomHelper;
    private VaschettaControlGrafico vaschettaHelper;
    private RecensioniControlGrafico recensioniHelper;

    @FXML
    public void initialize() {
        if (lblIntestazione != null) {
            lblIntestazione.setText("Cliente: " + SessionManager.getInstance().getNomeUtenteLoggato()
                    + " | Modalita': " + SessionManager.getInstance().getModalita());
        }

        ClienteMenuView.configuraCelleMieiOrdini(listaMieiOrdini);

        recensioniHelper = new RecensioniControlGrafico(this::mostraEsito, null);
        recensioniHelper.legaCampiCliente(
                comboVotoRecensione, campoTitoloRecensione, campoCommentoRecensione,
                boxCardsRecensioniCliente, lblMediaRecensioniCliente);
        recensioniHelper.aggiornaRecensioniCliente();

        gustoCustomHelper = new RichiestaGustoControlGrafico();
        gustoCustomHelper.legaCampiInput(
                campoNomeGustoCustom, campoDescrizioneCustom, comboIngredienteCustom,
                campoIngredienteCustom, campoKgCustom, campoDataCustom);
        gustoCustomHelper.legaCampiRiepilogo(
                lblRiepilogoNomeCustom, lblRiepilogoIngredienteCustom,
                lblRiepilogoDescrizioneCustom, lblRiepilogoKgDataCustom, listaMieRichieste);
        gustoCustomHelper.inizializza();

        vaschettaHelper = new VaschettaControlGrafico(
                carrello, btnFormato350, btnFormato500, btnFormato750, btnFormato1000,
                lblCapienzaVaschetta, boxVaschettaGrafica, checkPanna, checkGranella,
                lblPrezzoVaschettaCorrente, this::mostraEsito, this::aggiornaBoxGustiDisponibili);
        vaschettaHelper.inizializza();

        aggiornaVistaCliente();
        aggiornaStatoCarrello();
    }

    public CarrelloBean getCarrello() {
        return carrello;
    }

    private List<String> categorieMenuDisponibili() {
        Set<String> presenti = new LinkedHashSet<>();
        for (GustoBean g : tuttiProdottiMenu) {
            presenti.add(g.getCategoria());
        }
        List<String> risultato = new ArrayList<>();
        for (String c : ORDINE_CATEGORIE) {
            if (presenti.remove(c)) {
                risultato.add(c);
            }
        }
        risultato.addAll(presenti);
        return risultato;
    }

    @FXML
    public void onClickTornaAlleCategorie() {
        impostaCategoriaCatalogo(null);
    }

    public void impostaCategoriaCatalogo(String cat) {
        this.categoriaCatalogoSelezionata = cat;
        this.sottocategoriaCatalogoSelezionata = "Tutti";
        if (!"Crepes".equalsIgnoreCase(cat)) {
            this.crepeSelezionata = null;
            this.mostraPannelloToppingCrepe = false;
        }
        ClienteMenuView.renderBarraCategorieDinamica(
                boxCategorieCliente, categorieMenuDisponibili(), cat, this::impostaCategoriaCatalogo);

        if (boxSezioneVaschetta != null) {
            boolean mostraVaschetta = "Gelati".equalsIgnoreCase(cat);
            boxSezioneVaschetta.setVisible(mostraVaschetta);
            boxSezioneVaschetta.setManaged(mostraVaschetta);
        }
        aggiornaBoxGustiDisponibili();
    }

    private void aggiornaBoxGustiDisponibili() {
        if (boxGustiDisponibili == null) return;

        if (categoriaCatalogoSelezionata == null || categoriaCatalogoSelezionata.isEmpty()) {
            ClienteMenuView.renderGrigliaMacroCategorie(
                    boxGustiDisponibili,
                    categorieMenuDisponibili(),
                    tuttiProdottiMenu,
                    this::impostaCategoriaCatalogo);
            return;
        }

        if ("Crepes".equalsIgnoreCase(categoriaCatalogoSelezionata)) {
            costruisciSchermataCrepes();
            return;
        }

        List<GustoBean> daMostrare = new ArrayList<>();
        if ("Gelati".equalsIgnoreCase(categoriaCatalogoSelezionata)) {
            daMostrare.addAll(catalogoGustiGelato);
        } else {
            for (GustoBean g : tuttiProdottiMenu) {
                if (categoriaCatalogoSelezionata.equalsIgnoreCase(g.getCategoria())) {
                    daMostrare.add(g);
                }
            }
        }

        ClienteMenuView.renderGrigliaProdottiSelezionabili(
                boxGustiDisponibili,
                daMostrare,
                sottocategoriaCatalogoSelezionata,
                sottocat -> {
                    sottocategoriaCatalogoSelezionata = sottocat;
                    aggiornaBoxGustiDisponibili();
                },
                carrello.getGustiNellaVaschetta(),
                this::onClickAggiungiGustoAllaVaschetta,
                this::aggiungiProdottoMenuAlCarrello);
    }

    // ================= GESTIONE VASCHETTA (DELEGA A VaschettaControlGrafico) =================

    public void onClickAggiungiGustoAllaVaschetta(GustoBean g) {
        vaschettaHelper.aggiungiORimuoviGusto(g);
    }

    @FXML public void onClickFormato350() { vaschettaHelper.selezionaFormato(0.35, 3); }
    @FXML public void onClickFormato500() { vaschettaHelper.selezionaFormato(0.50, 4); }
    @FXML public void onClickFormato750() { vaschettaHelper.selezionaFormato(0.75, 5); }
    @FXML public void onClickFormato1000() { vaschettaHelper.selezionaFormato(1.00, 6); }

    @FXML
    public void onClickSvuotaVaschetta() {
        vaschettaHelper.svuotaVaschetta();
    }

    @FXML
    public void onClickAggiungiVaschettaAlCarrello() {
        vaschettaHelper.aggiungiVaschettaAlCarrello();
        aggiornaStatoCarrello();
    }

    public void aggiungiProdottoMenuAlCarrello(GustoBean g) {
        if (!g.isDisponibileOggi()) {
            mostraEsito("⚠️ Il prodotto '" + g.getNome() + "' è momentaneamente esaurito.", true);
            return;
        }
        carrello.aggiungiArticolo(new VaschettaItemBean(0.0, List.of(g.getNome()), false, false,
                g.getNome() + " (" + g.getDescrizione() + ")", g.getPrezzoAlKg()));
        aggiornaStatoCarrello();
        mostraEsito("🛒 '" + g.getNome() + "' aggiunto al carrello! Clicca sul tasto '🛒 Vedi Carrello' per il riepilogo.", false);
    }

    // ================= COMPOSIZIONE CREPES CON TOPPING & GRANELLA =================

    private void costruisciSchermataCrepes() {
        List<GustoBean> farciture = new ArrayList<>();
        for (GustoBean g : tuttiProdottiMenu) {
            if ("Crepes".equalsIgnoreCase(g.getCategoria()) && !"Topping".equalsIgnoreCase(g.getSottocategoria())) {
                farciture.add(g);
            }
        }
        CrepeConfiguratoreView.renderSchermataCrepes(
                boxGustiDisponibili,
                farciture,
                crepeSelezionata,
                toppingCrepeScelto,
                granellaCrepeScelta,
                mostraPannelloToppingCrepe,
                new CrepeConfiguratoreView.AzioniCrepe(
                        this::selezionaCrepePerTopping,
                        () -> {
                            mostraPannelloToppingCrepe = true;
                            aggiornaBoxGustiDisponibili();
                        },
                        top -> {
                            toppingCrepeScelto = top;
                            aggiornaBoxGustiDisponibili();
                        },
                        gran -> {
                            granellaCrepeScelta = gran;
                            aggiornaBoxGustiDisponibili();
                        },
                        () -> confermaCrepeNelCarrello(true)));
    }

    public void selezionaCrepePerTopping(GustoBean crepe) {
        this.crepeSelezionata = crepe;
        this.mostraPannelloToppingCrepe = false;
        this.toppingCrepeScelto = CrepeConfiguratoreView.NESSUN_TOPPING;
        this.granellaCrepeScelta = CrepeConfiguratoreView.NESSUNA_GRANELLA;
        aggiornaBoxGustiDisponibili();
        mostraEsito("🥞 Hai selezionato '" + crepe.getNome() + "'! Ora clicca sul bottone '✨ Scegli il Topping & Granella (+€0,50)'.", false);
    }

    public void confermaCrepeNelCarrello(boolean includiTopping) {
        if (crepeSelezionata == null) return;
        String top = includiTopping ? toppingCrepeScelto : CrepeConfiguratoreView.NESSUN_TOPPING;
        String gran = includiTopping ? granellaCrepeScelta : CrepeConfiguratoreView.NESSUNA_GRANELLA;

        double prezzo = CrepeConfiguratoreView.calcolaPrezzoCrepe(crepeSelezionata.getPrezzoAlKg(), top, gran);
        String desc = CrepeConfiguratoreView.formattaDescrizioneCrepe(crepeSelezionata.getNome(), top, gran);

        carrello.aggiungiArticolo(new VaschettaItemBean(0.0, List.of(crepeSelezionata.getNome()), false, false, desc, prezzo));

        crepeSelezionata = null;
        mostraPannelloToppingCrepe = false;
        toppingCrepeScelto = CrepeConfiguratoreView.NESSUN_TOPPING;
        granellaCrepeScelta = CrepeConfiguratoreView.NESSUNA_GRANELLA;
        aggiornaBoxGustiDisponibili();
        aggiornaStatoCarrello();
        mostraEsito("🥞 '" + desc + "' aggiunta al carrello! Clicca '🛒 Vedi Carrello' per completare l'ordine.", false);
    }

    // ================= CARRELLO E INVIO PRE-ORDINE =================

    private void aggiornaStatoCarrello() {
        CarrelloView.aggiornaBottoneCarrello(btnApriCarrello, carrello);
    }

    @FXML
    public void onClickApriCarrello() {
        Window owner = (btnApriCarrello != null && btnApriCarrello.getScene() != null)
                ? btnApriCarrello.getScene().getWindow() : null;

        CarrelloView.apriPopupCarrello(
                owner,
                carrello,
                idx -> {
                    carrello.rimuoviArticolo(idx);
                    aggiornaStatoCarrello();
                },
                () -> {
                    carrello.svuota();
                    aggiornaStatoCarrello();
                },
                (tipo, orario, note) -> {
                    carrello.setModalitaConsegna(tipo);
                    carrello.setFasciaOraria(orario);
                    carrello.setIndirizzoONote(note);
                    onClickPreOrdina();
                });
    }

    @FXML
    public void onClickPreOrdina() {
        try {
            if (carrello.isVuoto()) {
                boolean conPanna = checkPanna != null && checkPanna.isSelected();
                if (!carrello.getGustiNellaVaschetta().isEmpty() || conPanna) {
                    onClickAggiungiVaschettaAlCarrello();
                } else {
                    mostraEsito("⚠️ Aggiungi almeno un prodotto o una vaschetta al carrello prima di confermare l'ordine.", true);
                    return;
                }
            }

            OrdineBean bean = carrello.costruisciOrdineBean(SessionManager.getInstance().getEmailUtenteLoggato());
            OrdineBean confermato = new OrdiniControl().effettuaPreOrdine(bean);
            mostraEsito("🛵 Ordine " + confermato.getCodiceOrdine() + " inviato alla Gelateria! Riceverai la notifica di conferma da Maestro o Dipendenti (" + carrello.getFasciaOraria() + ").", false);

            carrello.svuota();
            carrello.svuotaVaschettaInComposizione();
            vaschettaHelper.resetSelettoriExtra();
            vaschettaHelper.aggiornaVaschettaGrafica();

            aggiornaBoxGustiDisponibili();
            aggiornaStatoCarrello();
            aggiornaMieiOrdini();
        } catch (FrostlyException e) {
            mostraEsito("⚠️ " + e.getMessage(), true);
        }
    }

    // ================= STORICO ORDINI, GUSTO SU MISURA E LOGOUT =================

    private void aggiornaMieiOrdini() {
        if (listaMieiOrdini == null) return;
        String email = SessionManager.getInstance().getEmailUtenteLoggato();
        listaMieiOrdini.getItems().clear();
        listaMieiOrdini.getItems().addAll(new OrdiniControl().visualizzaOrdiniCliente(email));
    }

    @FXML
    public void onClickAggiornaOrdini() {
        aggiornaMieiOrdini();
        mostraEsito("Elenco ordini aggiornato.", false);
    }

    @FXML
    public void onClickPubblicaRecensione() {
        String email = SessionManager.getInstance().getEmailUtenteLoggato();
        String nome = SessionManager.getInstance().getNomeUtenteLoggato();
        recensioniHelper.inviaNuovaRecensione(email, nome);
    }

    @FXML
    public void onClickAggiornaRecensioni() {
        recensioniHelper.aggiornaRecensioniCliente();
        mostraEsito("Recensioni aggiornate.", false);
    }

    @FXML
    public void onClickRichiediGusto() {
        try {
            String email = SessionManager.getInstance().getEmailUtenteLoggato();
            RichiestaGustoBean creata = gustoCustomHelper.inviaProposta(email);
            mostraEsito("✅ Proposta per '" + creata.getNomeGustoProposto() + "' inviata al Maestro! Codice: " + creata.getCodiceRichiesta(), false);
        } catch (FrostlyException e) {
            mostraEsito("⚠️ " + e.getMessage(), true);
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

    private void aggiornaVistaCliente() {
        String email = SessionManager.getInstance().getEmailUtenteLoggato();
        catalogoGustiGelato.clear();
        tuttiProdottiMenu.clear();
        for (GustoBean g : new GestioneMenuControl().visualizzaMenuGusti()) {
            tuttiProdottiMenu.add(g);
            if ("Gelati".equalsIgnoreCase(g.getCategoria())) {
                catalogoGustiGelato.add(g);
            }
        }
        impostaCategoriaCatalogo(categoriaCatalogoSelezionata);
        if (gustoCustomHelper != null) {
            gustoCustomHelper.aggiornaListaRichieste(email);
        }
        if (recensioniHelper != null) {
            recensioniHelper.aggiornaRecensioniCliente();
        }
        if (listaNotificheCliente != null) {
            listaNotificheCliente.getItems().clear();
            for (NotificaBean n : new LoginControl().recuperaNotificheUtente(email)) {
                listaNotificheCliente.getItems().add("[" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
            }
        }
        aggiornaMieiOrdini();
    }
}
