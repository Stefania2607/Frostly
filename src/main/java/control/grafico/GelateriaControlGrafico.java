package control.grafico;

import bean.GustoBean;
import bean.IngredienteBean;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.RichiestaGustoBean;
import bean.VaschettaItemBean;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.collections.FXCollections;
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
import view.ClienteMenuView;
import view.VetrinaMenuView;

/**
 * Controller Grafico JavaFX collegato a 'cliente.fxml' e 'cliente_gui2.fxml'.
 * Gestisce la logica di interazione utente per il menu selezionabile (Gelati, Crepes con Topping & Granella,
 * Panna, Bevande, Caffetteria, Biscotti, Crostate, Tisane) e il carrello dedicato.
 * Decomposizione rigorosa BCE: nessuna estetica nel controller grafico; il rendering è delegato a ClienteMenuView.
 */
public class GelateriaControlGrafico {

    private static final List<String> ORDINE_CATEGORIE = Arrays.asList(
            "Gelati", "Crepes", "Panna", "Bevande", "Caffetteria", "Biscotti", "Crostate", "Tisane");

    @FXML private Label lblIntestazione;
    @FXML private Label lblEsito;
    @FXML private ListView<GustoBean> listaMenuGusti;
    @FXML private VBox boxGustiDisponibili;
    private final List<GustoBean> catalogoGustiGelato = new ArrayList<>();
    @FXML private ListView<String> listaMieRichieste;
    @FXML private ListView<String> listaNotificheCliente;

    // Tasti dedicati per aprire il Carrello & stato Carrello
    @FXML private Button btnApriCarrello;
    @FXML private Button btnApriCarrelloMenu;
    @FXML private Label lblPrezzoVaschettaCorrente;
    @FXML private Label lblConteggioVaschette;
    @FXML private VBox boxVaschetteOrdine;
    @FXML private ListView<OrdineBean> listaMieiOrdini;
    private final List<VaschettaItemBean> carrelloVaschette = new ArrayList<>();

    // Formato e capienza Vaschetta
    @FXML private Button btnFormato350;
    @FXML private Button btnFormato500;
    @FXML private Button btnFormato750;
    @FXML private Button btnFormato1000;
    @FXML private Label lblCapienzaVaschetta;
    @FXML private HBox boxVaschettaGrafica;
    @FXML private Label lblBadgePanna;
    @FXML private Label lblBadgeGranella;
    @FXML private Label lblRiepilogoPrezzoTotale;
    @FXML private Label lblRiepilogoGusti;

    private double pesoSelezionatoKg = 0.50;
    private int maxGustiSelezionabili = 4;
    private final List<GustoBean> gustiNellaVaschetta = new ArrayList<>();

    // Consegna / Ritiro
    @FXML private ComboBox<String> comboTipoConsegna;
    @FXML private ComboBox<String> comboOrarioConsegna;
    @FXML private TextField campoIndirizzoNote;

    // Campi compatibilità FXML
    @FXML private ComboBox<String> comboPesoRapido;
    @FXML private ComboBox<String> comboGiornoOrdine;
    @FXML private ComboBox<String> comboIngredienteCustom;
    @FXML private TextField campoGustoOrdine;
    @FXML private TextField campoPesoOrdine;
    @FXML private TextField campoDataOrdine;
    @FXML private CheckBox checkPanna;
    @FXML private CheckBox checkGranella;

    // Categorie Menu e Composizione Crepes (Topping +0.50€ e Granella +0.50€ o Nessuna)
    private final List<GustoBean> tuttiProdottiMenu = new ArrayList<>();
    private String categoriaCatalogoSelezionata = null;
    private String sottocategoriaCatalogoSelezionata = "Tutti";
    private GustoBean crepeSelezionata = null;
    private boolean mostraPannelloToppingCrepe = false;
    private String toppingCrepeScelto = ClienteMenuView.NESSUN_TOPPING;
    private String granellaCrepeScelta = ClienteMenuView.NESSUNA_GRANELLA;

    @FXML private HBox boxCategorieCliente;
    @FXML private Button btnCatGelati;
    @FXML private Button btnCatDolci;
    @FXML private Button btnCatBevande;
    @FXML private Button btnCatPanna;
    @FXML private Button btnCatAltro;
    @FXML private VBox boxSezioneVaschetta;
    @FXML private HBox boxPannaSempreVisibile;

    // Tab Gusto su misura
    @FXML private TextField campoNomeGustoCustom;
    @FXML private TextField campoDescrizioneCustom;
    @FXML private TextField campoIngredienteCustom;
    @FXML private TextField campoKgCustom;
    @FXML private TextField campoDataCustom;
    @FXML private Label lblRiepilogoNomeCustom;
    @FXML private Label lblRiepilogoIngredienteCustom;
    @FXML private Label lblRiepilogoDescrizioneCustom;
    @FXML private Label lblRiepilogoKgDataCustom;

    @FXML
    public void initialize() {
        if (lblIntestazione != null) {
            lblIntestazione.setText("Cliente: " + SessionManager.getInstance().getNomeUtenteLoggato()
                    + " | Modalita': " + SessionManager.getInstance().getModalita());
        }
        configuraTendineEClickMenu();
        ClienteMenuView.configuraCelleMenuGusti(listaMenuGusti, gustiNellaVaschetta, this::onClickAggiungiGustoAllaVaschetta);
        ClienteMenuView.configuraCelleMieiOrdini(listaMieiOrdini);
        configuraAscoltoGustoCustom();
        selezionaFormato(0.50, 4);
        aggiornaVistaCliente();
        aggiornaCarrelloGrafico();
        aggiornaAnteprimaGustoCustom();
    }

    private void configuraTendineEClickMenu() {
        if (comboPesoRapido != null) {
            comboPesoRapido.setItems(FXCollections.observableArrayList("0.35", "0.5", "0.75", "1.0"));
            comboPesoRapido.setOnAction(e -> {
                if (comboPesoRapido.getValue() != null) {
                    try {
                        double p = Double.parseDouble(comboPesoRapido.getValue());
                        int m = p <= 0.35 ? 3 : (p <= 0.5 ? 4 : (p <= 0.75 ? 5 : 6));
                        selezionaFormato(p, m);
                    } catch (NumberFormatException ignored) {
                        // Formato non numerico
                    }
                }
            });
        }

        if (comboTipoConsegna != null) {
            comboTipoConsegna.setItems(FXCollections.observableArrayList(
                    "🛵 Consegna a Domicilio",
                    "🛍️ Ritiro al Banco"
            ));
            comboTipoConsegna.setValue("🛵 Consegna a Domicilio");
        }

        if (comboOrarioConsegna != null) {
            comboOrarioConsegna.setItems(FXCollections.observableArrayList(
                    "Subito (~25-35 min)",
                    "Oggi 16:30", "Oggi 17:00", "Oggi 17:30", "Oggi 18:00",
                    "Oggi 18:30", "Oggi 19:00", "Oggi 19:30", "Oggi 20:00",
                    "Oggi 20:30", "Oggi 21:00", "Oggi 21:30", "Oggi 22:00",
                    "Domani 13:00", "Domani 18:00", "Domani 20:00"
            ));
            comboOrarioConsegna.setValue("Subito (~25-35 min)");
        }

        if (comboGiornoOrdine != null) {
            comboGiornoOrdine.setItems(FXCollections.observableArrayList(
                    "Lunedi", "Martedi", "Mercoledi", "Giovedi", "Venerdi", "Sabato", "Domenica"));
            comboGiornoOrdine.setValue("Sabato");
            comboGiornoOrdine.setOnAction(e -> {
                if (comboGiornoOrdine.getValue() != null && campoDataOrdine != null) {
                    campoDataOrdine.setText(comboGiornoOrdine.getValue());
                }
            });
        }

        if (checkPanna != null) {
            checkPanna.setOnAction(e -> {
                aggiornaVaschettaGrafica();
                aggiornaRiepilogoPrezzo();
            });
        }
        if (checkGranella != null) {
            checkGranella.setOnAction(e -> {
                aggiornaVaschettaGrafica();
                aggiornaRiepilogoPrezzo();
            });
        }

        if (comboIngredienteCustom != null) {
            List<String> nomiIng = new ArrayList<>();
            for (IngredienteBean ib : new ProduzioneOrdiniControl().visualizzaMagazzino()) {
                nomiIng.add(ib.getNome());
            }
            comboIngredienteCustom.setItems(FXCollections.observableArrayList(nomiIng));
            if (!nomiIng.isEmpty()) {
                comboIngredienteCustom.setValue(nomiIng.get(0));
            }
            comboIngredienteCustom.setOnAction(e -> {
                if (comboIngredienteCustom.getValue() != null && campoIngredienteCustom != null) {
                    campoIngredienteCustom.setText(comboIngredienteCustom.getValue());
                }
                aggiornaAnteprimaGustoCustom();
            });
        }
    }

    private void configuraAscoltoGustoCustom() {
        if (campoNomeGustoCustom != null) {
            campoNomeGustoCustom.textProperty().addListener((obs, oldV, newV) -> aggiornaAnteprimaGustoCustom());
        }
        if (campoDescrizioneCustom != null) {
            campoDescrizioneCustom.textProperty().addListener((obs, oldV, newV) -> aggiornaAnteprimaGustoCustom());
        }
        if (campoKgCustom != null) {
            campoKgCustom.textProperty().addListener((obs, oldV, newV) -> aggiornaAnteprimaGustoCustom());
        }
        if (campoDataCustom != null) {
            campoDataCustom.textProperty().addListener((obs, oldV, newV) -> aggiornaAnteprimaGustoCustom());
        }
    }

    private void aggiornaAnteprimaGustoCustom() {
        if (lblRiepilogoNomeCustom != null) {
            String nome = (campoNomeGustoCustom != null && !campoNomeGustoCustom.getText().trim().isEmpty())
                    ? campoNomeGustoCustom.getText().trim() : "Nome Creazione";
            lblRiepilogoNomeCustom.setText(nome);
        }
        if (lblRiepilogoIngredienteCustom != null) {
            String ing = (comboIngredienteCustom != null && comboIngredienteCustom.getValue() != null)
                    ? comboIngredienteCustom.getValue() : "Materia Prima";
            lblRiepilogoIngredienteCustom.setText(ing);
        }
        if (lblRiepilogoDescrizioneCustom != null) {
            String desc = (campoDescrizioneCustom != null && !campoDescrizioneCustom.getText().trim().isEmpty())
                    ? campoDescrizioneCustom.getText().trim() : "Nessuna nota specificata";
            lblRiepilogoDescrizioneCustom.setText(desc);
        }
        if (lblRiepilogoKgDataCustom != null) {
            String kg = (campoKgCustom != null && !campoKgCustom.getText().trim().isEmpty())
                    ? campoKgCustom.getText().trim() : "1.0";
            String data = (campoDataCustom != null && !campoDataCustom.getText().trim().isEmpty())
                    ? campoDataCustom.getText().trim() : "Domenica";
            lblRiepilogoKgDataCustom.setText("Quantità: " + kg + " Kg • Giorno: " + data);
        }
    }

    public void onClickAggiungiGustoAllaVaschetta(GustoBean g) {
        if (gustiNellaVaschetta.contains(g)) {
            gustiNellaVaschetta.remove(g);
            mostraEsito("ℹ️ Gusto '" + g.getNome() + "' rimosso dalla vaschetta.", false);
        } else {
            if (gustiNellaVaschetta.size() >= maxGustiSelezionabili) {
                mostraEsito("⚠️ Hai già raggiunto il limite di " + maxGustiSelezionabili
                        + " gusti per questo formato. Rimuovine uno per fare spazio!", true);
                return;
            }
            gustiNellaVaschetta.add(g);
            mostraEsito("🍦 '" + g.getNome() + "' aggiunto alla vaschetta! Quando hai finito clicca su '➕ Metti Vaschetta nel Carrello'.", false);
        }
        aggiornaVaschettaGrafica();
        aggiornaBoxGustiDisponibili();
        if (listaMenuGusti != null) listaMenuGusti.refresh();
    }

    @FXML public void onClickTornaAlleCategorie() { impostaCategoriaCatalogo(null); }
    @FXML public void onClickFiltroCategoriaGelati() { impostaCategoriaCatalogo("Gelati"); }
    @FXML public void onClickFiltroCategoriaDolci() { impostaCategoriaCatalogo("Crepes"); }
    @FXML public void onClickFiltroCategoriaBevande() { impostaCategoriaCatalogo("Bevande"); }
    @FXML public void onClickFiltroCategoriaPanna() { impostaCategoriaCatalogo("Panna"); }
    @FXML public void onClickFiltroCategoriaAltro() { impostaCategoriaCatalogo("Caffetteria"); }

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

    public void impostaCategoriaCatalogo(String cat) {
        this.categoriaCatalogoSelezionata = cat;
        this.sottocategoriaCatalogoSelezionata = "Tutti";
        if (!"Crepes".equalsIgnoreCase(cat) && !"Dolci".equalsIgnoreCase(cat)) {
            this.crepeSelezionata = null;
            this.mostraPannelloToppingCrepe = false;
        }
        ClienteMenuView.renderBarraCategorieDinamica(
                boxCategorieCliente, categorieMenuDisponibili(), cat, this::impostaCategoriaCatalogo);

        ClienteMenuView.evidenziaPulsanteCategoria(btnCatGelati, "Gelati".equalsIgnoreCase(cat));
        ClienteMenuView.evidenziaPulsanteCategoria(btnCatDolci, "Crepes".equalsIgnoreCase(cat) || "Dolci".equalsIgnoreCase(cat));
        ClienteMenuView.evidenziaPulsanteCategoria(btnCatPanna, "Panna".equalsIgnoreCase(cat));
        ClienteMenuView.evidenziaPulsanteCategoria(btnCatBevande, "Bevande".equalsIgnoreCase(cat));
        ClienteMenuView.evidenziaPulsanteCategoria(btnCatAltro, "Caffetteria".equalsIgnoreCase(cat) || "Altro".equalsIgnoreCase(cat));

        if (boxSezioneVaschetta != null) {
            boolean mostraVaschetta = "Gelati".equalsIgnoreCase(cat);
            boxSezioneVaschetta.setVisible(mostraVaschetta);
            boxSezioneVaschetta.setManaged(mostraVaschetta);
        }
        aggiornaBoxGustiDisponibili();
    }

    public void aggiornaBoxPannaSempreVisibile() {
        if (boxPannaSempreVisibile == null) return;
        String fotoPanna = "panna_fresca.png";
        double p100 = 2.50, p200 = 4.50, p300 = 6.00;
        for (GustoBean g : tuttiProdottiMenu) {
            if ("Panna".equalsIgnoreCase(g.getCategoria())) {
                if (g.getImmagineUrl() != null && !g.getImmagineUrl().isEmpty()) {
                    fotoPanna = g.getImmagineUrl();
                }
                if (g.getNome().contains("100")) p100 = g.getPrezzoAlKg();
                else if (g.getNome().contains("200")) p200 = g.getPrezzoAlKg();
                else if (g.getNome().contains("300")) p300 = g.getPrezzoAlKg();
            }
        }
        ClienteMenuView.renderPannaSempreVisibile(
                boxPannaSempreVisibile, fotoPanna, p100, p200, p300, this::aggiungiPannaAlCarrello);
    }

    @FXML public void onClickAggiungiPanna100() { aggiungiPannaAlCarrello("100g", 2.50); }
    @FXML public void onClickAggiungiPanna200() { aggiungiPannaAlCarrello("200g", 4.50); }
    @FXML public void onClickAggiungiPanna300() { aggiungiPannaAlCarrello("300g", 6.00); }

    public void aggiungiPannaAlCarrello(String formato, double prezzo) {
        double kg = formato.contains("100") ? 0.10 : (formato.contains("200") ? 0.20 : 0.30);
        carrelloVaschette.add(new VaschettaItemBean(kg, List.of(), true, false,
                "Panna Montata Fresca d'Alpeggio (" + formato + ")", prezzo));
        aggiornaCarrelloGrafico();
        mostraEsito("☁️ Panna Fresca " + formato + " aggiunta al carrello! Clicca '🛒 Vedi Carrello' per completare l'ordine.", false);
    }

    public void aggiungiProdottoMenuAlCarrello(GustoBean g) {
        if (!g.isDisponibileOggi()) {
            mostraEsito("⚠️ Il prodotto '" + g.getNome() + "' è momentaneamente esaurito.", true);
            return;
        }
        carrelloVaschette.add(new VaschettaItemBean(0.0, List.of(g.getNome()), false, false,
                g.getNome() + " (" + g.getDescrizione() + ")", g.getPrezzoAlKg()));
        aggiornaCarrelloGrafico();
        mostraEsito("🛒 '" + g.getNome() + "' aggiunto al carrello! Clicca sul tasto '🛒 Vedi Carrello' per vedere il riepilogo.", false);
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

        if ("Crepes".equalsIgnoreCase(categoriaCatalogoSelezionata) || "Dolci".equalsIgnoreCase(categoriaCatalogoSelezionata)) {
            costruisciSchermataCrepes();
            return;
        }

        List<GustoBean> daMostrare = new ArrayList<>();
        if ("Gelati".equalsIgnoreCase(categoriaCatalogoSelezionata)) {
            daMostrare.addAll(catalogoGustiGelato);
        } else if ("Altro".equalsIgnoreCase(categoriaCatalogoSelezionata)) {
            for (GustoBean g : tuttiProdottiMenu) {
                String c = g.getCategoria();
                if (!"Gelati".equalsIgnoreCase(c) && !"Crepes".equalsIgnoreCase(c)
                        && !"Panna".equalsIgnoreCase(c) && !"Bevande".equalsIgnoreCase(c)) {
                    daMostrare.add(g);
                }
            }
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
                gustiNellaVaschetta,
                this::onClickAggiungiGustoAllaVaschetta,
                this::aggiungiProdottoMenuAlCarrello);
    }

    private void costruisciSchermataCrepes() {
        List<GustoBean> farciture = new ArrayList<>();
        for (GustoBean g : tuttiProdottiMenu) {
            if (("Crepes".equalsIgnoreCase(g.getCategoria()) || "Dolci".equalsIgnoreCase(g.getCategoria()))
                    && !"Topping".equalsIgnoreCase(g.getSottocategoria())) {
                farciture.add(g);
            }
        }
        ClienteMenuView.renderSchermataCrepes(
                boxGustiDisponibili,
                farciture,
                crepeSelezionata,
                toppingCrepeScelto,
                granellaCrepeScelta,
                mostraPannelloToppingCrepe,
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
                () -> confermaCrepeNelCarrello(true));
    }

    public void selezionaCrepePerTopping(GustoBean crepe) {
        this.crepeSelezionata = crepe;
        this.mostraPannelloToppingCrepe = false;
        this.toppingCrepeScelto = ClienteMenuView.NESSUN_TOPPING;
        this.granellaCrepeScelta = ClienteMenuView.NESSUNA_GRANELLA;
        aggiornaBoxGustiDisponibili();
        mostraEsito("🥞 Hai selezionato '" + crepe.getNome() + "'! Ora clicca sul bottone '✨ Scegli il Topping & Granella (+€0,50)'.", false);
    }

    public void impostaToppingEGranellaCrepe(String topping, String granella) {
        this.toppingCrepeScelto = (topping == null || topping.isEmpty()) ? ClienteMenuView.NESSUN_TOPPING : topping;
        this.granellaCrepeScelta = (granella == null || granella.isEmpty()) ? ClienteMenuView.NESSUNA_GRANELLA : granella;
        this.mostraPannelloToppingCrepe = true;
        aggiornaBoxGustiDisponibili();
    }

    public void confermaCrepeNelCarrello(boolean includiTopping) {
        if (crepeSelezionata == null) return;
        String top = includiTopping ? toppingCrepeScelto : ClienteMenuView.NESSUN_TOPPING;
        String gran = includiTopping ? granellaCrepeScelta : ClienteMenuView.NESSUNA_GRANELLA;

        double prezzo = ClienteMenuView.calcolaPrezzoCrepe(crepeSelezionata.getPrezzoAlKg(), top, gran);
        String desc = ClienteMenuView.formattaDescrizioneCrepe(crepeSelezionata.getNome(), top, gran);

        List<String> elementi = List.of(crepeSelezionata.getNome());
        carrelloVaschette.add(new VaschettaItemBean(0.0, elementi, false, false, desc, prezzo));

        crepeSelezionata = null;
        mostraPannelloToppingCrepe = false;
        toppingCrepeScelto = ClienteMenuView.NESSUN_TOPPING;
        granellaCrepeScelta = ClienteMenuView.NESSUNA_GRANELLA;
        aggiornaBoxGustiDisponibili();
        aggiornaCarrelloGrafico();
        mostraEsito("🥞 '" + desc + "' aggiunta al carrello! Clicca '🛒 Vedi Carrello' per completare l'ordine.", false);
    }

    @FXML
    public void onClickApriCarrello() {
        Window owner = (btnApriCarrello != null && btnApriCarrello.getScene() != null)
                ? btnApriCarrello.getScene().getWindow() : null;
        String modIniziale = (comboTipoConsegna != null && comboTipoConsegna.getValue() != null)
                ? comboTipoConsegna.getValue() : "🛵 Consegna a Domicilio";
        String oraIniziale = (comboOrarioConsegna != null && comboOrarioConsegna.getValue() != null)
                ? comboOrarioConsegna.getValue() : "Subito (~25-35 min)";
        String noteIniziali = campoIndirizzoNote != null ? campoIndirizzoNote.getText() : "";

        ClienteMenuView.apriPopupCarrelloDedicato(
                owner,
                carrelloVaschette,
                modIniziale,
                oraIniziale,
                noteIniziali,
                idx -> {
                    if (idx >= 0 && idx < carrelloVaschette.size()) {
                        carrelloVaschette.remove(idx);
                        aggiornaCarrelloGrafico();
                    }
                },
                () -> {
                    carrelloVaschette.clear();
                    aggiornaCarrelloGrafico();
                },
                (tipo, orario, note) -> {
                    if (comboTipoConsegna != null) comboTipoConsegna.setValue(tipo);
                    if (comboOrarioConsegna != null) comboOrarioConsegna.setValue(orario);
                    if (campoIndirizzoNote != null) campoIndirizzoNote.setText(note);
                    onClickPreOrdina();
                });
    }

    @FXML public void onClickFormato350() { selezionaFormato(0.35, 3); }
    @FXML public void onClickFormato500() { selezionaFormato(0.50, 4); }
    @FXML public void onClickFormato750() { selezionaFormato(0.75, 5); }
    @FXML public void onClickFormato1000() { selezionaFormato(1.00, 6); }

    private void selezionaFormato(double pesoKg, int maxGusti) {
        this.pesoSelezionatoKg = pesoKg;
        this.maxGustiSelezionabili = maxGusti;
        if (campoPesoOrdine != null) {
            campoPesoOrdine.setText(String.valueOf(pesoKg));
        }

        while (gustiNellaVaschetta.size() > maxGustiSelezionabili) {
            gustiNellaVaschetta.remove(gustiNellaVaschetta.size() - 1);
        }

        ClienteMenuView.evidenziaPulsanteFormato(btnFormato350, pesoKg == 0.35);
        ClienteMenuView.evidenziaPulsanteFormato(btnFormato500, pesoKg == 0.50);
        ClienteMenuView.evidenziaPulsanteFormato(btnFormato750, pesoKg == 0.75);
        ClienteMenuView.evidenziaPulsanteFormato(btnFormato1000, pesoKg == 1.00);

        aggiornaVaschettaGrafica();
        aggiornaBoxGustiDisponibili();
        if (listaMenuGusti != null) listaMenuGusti.refresh();
    }

    private void aggiornaVaschettaGrafica() {
        boolean conPanna = checkPanna != null && checkPanna.isSelected();
        boolean conGranella = checkGranella != null && checkGranella.isSelected();

        ClienteMenuView.renderVaschettaGrafica(
                boxVaschettaGrafica,
                gustiNellaVaschetta,
                maxGustiSelezionabili,
                pesoSelezionatoKg,
                conPanna,
                conGranella,
                g -> {
                    gustiNellaVaschetta.remove(g);
                    aggiornaVaschettaGrafica();
                    aggiornaBoxGustiDisponibili();
                    if (listaMenuGusti != null) listaMenuGusti.refresh();
                });

        if (lblCapienzaVaschetta != null) {
            if (gustiNellaVaschetta.isEmpty() && conPanna) {
                lblCapienzaVaschetta.setText("Vaschetta di Sola Panna (" + ((int) (pesoSelezionatoKg * 1000)) + "g)");
            } else {
                lblCapienzaVaschetta.setText(gustiNellaVaschetta.size() + "/" + maxGustiSelezionabili + " gusti scelti (" + ((int) (pesoSelezionatoKg * 1000)) + "g)");
            }
        }

        if (lblBadgePanna != null && checkPanna != null) {
            boolean vis = conPanna && !gustiNellaVaschetta.isEmpty();
            lblBadgePanna.setVisible(vis);
            lblBadgePanna.setManaged(vis);
        }
        if (lblBadgeGranella != null && checkGranella != null) {
            boolean vis = conGranella && !gustiNellaVaschetta.isEmpty();
            lblBadgeGranella.setVisible(vis);
            lblBadgeGranella.setManaged(vis);
        }

        aggiornaRiepilogoPrezzo();
    }

    private void aggiornaRiepilogoPrezzo() {
        double prezzoQuestaVaschetta;
        if (gustiNellaVaschetta.isEmpty()) {
            if (checkPanna != null && checkPanna.isSelected()) {
                prezzoQuestaVaschetta = Math.round(12.0 * pesoSelezionatoKg * 100.0) / 100.0;
                if (checkGranella != null && checkGranella.isSelected()) {
                    prezzoQuestaVaschetta += 3.50;
                }
            } else {
                prezzoQuestaVaschetta = 0.0;
            }
        } else {
            double sommaPrezziAlKg = 0.0;
            for (GustoBean g : gustiNellaVaschetta) {
                sommaPrezziAlKg += g.getPrezzoAlKg();
            }
            double mediaKg = sommaPrezziAlKg / gustiNellaVaschetta.size();
            prezzoQuestaVaschetta = Math.round(mediaKg * pesoSelezionatoKg * 100.0) / 100.0;
            if (checkPanna != null && checkPanna.isSelected()) {
                prezzoQuestaVaschetta += 2.50;
            }
            if (checkGranella != null && checkGranella.isSelected()) {
                prezzoQuestaVaschetta += 3.50;
            }
        }

        if (lblPrezzoVaschettaCorrente != null) {
            lblPrezzoVaschettaCorrente.setText(String.format(Locale.US, "EUR %.2f", prezzoQuestaVaschetta));
        }

        aggiornaTotaleCarrello();
    }

    @FXML
    public void onClickAggiungiVaschettaAlCarrello() {
        boolean conPanna = checkPanna != null && checkPanna.isSelected();
        boolean conGranella = checkGranella != null && checkGranella.isSelected();

        if (gustiNellaVaschetta.isEmpty() && !conPanna) {
            mostraEsito("⚠️ Seleziona almeno un gusto dalla carta qui sotto oppure la panna fresca d'alpeggio!", true);
            return;
        }

        List<String> nomi = new ArrayList<>();
        double prezzoVaschetta;
        String desc;

        if (gustiNellaVaschetta.isEmpty()) {
            prezzoVaschetta = Math.round(12.0 * pesoSelezionatoKg * 100.0) / 100.0;
            if (conGranella) prezzoVaschetta += 3.50;
            desc = "Vaschetta Panna Montata Fresca d'Alpeggio (" + ((int) (pesoSelezionatoKg * 1000)) + "g)"
                    + (conGranella ? " + Granella di Bronte DOP" : "");
        } else {
            double sommaPrezziAlKg = 0.0;
            StringBuilder sbGusti = new StringBuilder();
            for (int i = 0; i < gustiNellaVaschetta.size(); i++) {
                GustoBean g = gustiNellaVaschetta.get(i);
                nomi.add(g.getNome());
                sommaPrezziAlKg += g.getPrezzoAlKg();
                if (i > 0) sbGusti.append(", ");
                sbGusti.append(g.getNome());
            }

            double mediaKg = sommaPrezziAlKg / gustiNellaVaschetta.size();
            prezzoVaschetta = Math.round(mediaKg * pesoSelezionatoKg * 100.0) / 100.0;
            if (conPanna) prezzoVaschetta += 2.50;
            if (conGranella) prezzoVaschetta += 3.50;

            StringBuilder sb = new StringBuilder("Vaschetta " + ((int) (pesoSelezionatoKg * 1000)) + "g (" + sbGusti + ")");
            if (conPanna) sb.append(" + Panna");
            if (conGranella) sb.append(" + Granella");
            desc = sb.toString();
        }

        VaschettaItemBean item = new VaschettaItemBean(pesoSelezionatoKg, nomi, conPanna, conGranella,
                desc, prezzoVaschetta);
        carrelloVaschette.add(item);

        gustiNellaVaschetta.clear();
        if (checkPanna != null) checkPanna.setSelected(false);
        if (checkGranella != null) checkGranella.setSelected(false);

        aggiornaVaschettaGrafica();
        aggiornaBoxGustiDisponibili();
        aggiornaCarrelloGrafico();

        mostraEsito("🍦 Vaschetta aggiunta al carrello! Clicca '🛒 Vedi Carrello' in alto a destra per vedere l'ordine o comporne un'altra.", false);
    }

    private void aggiornaCarrelloGrafico() {
        ClienteMenuView.renderCarrelloGrafico(boxVaschetteOrdine, carrelloVaschette, idx -> {
            carrelloVaschette.remove(idx);
            aggiornaCarrelloGrafico();
        });
        aggiornaTotaleCarrello();
    }

    private void aggiornaTotaleCarrello() {
        double somma = 0.0;
        for (VaschettaItemBean item : carrelloVaschette) {
            somma += item.ottieniPrezzo();
        }

        if (lblConteggioVaschette != null) {
            int n = carrelloVaschette.size();
            lblConteggioVaschette.setText("(" + n + (n == 1 ? " vaschetta)" : " vaschette)"));
        }

        if (lblRiepilogoPrezzoTotale != null) {
            lblRiepilogoPrezzoTotale.setText(String.format(Locale.US, "EUR %.2f", somma));
        }

        ClienteMenuView.aggiornaBottoneCarrelloDedicato(btnApriCarrello, carrelloVaschette);
        ClienteMenuView.aggiornaBottoneCarrelloDedicato(btnApriCarrelloMenu, carrelloVaschette);
    }

    @FXML
    public void onClickSvuotaVaschetta() {
        gustiNellaVaschetta.clear();
        aggiornaVaschettaGrafica();
        aggiornaBoxGustiDisponibili();
        if (listaMenuGusti != null) listaMenuGusti.refresh();
        mostraEsito("Vaschetta svuotata. Scegli i tuoi gusti preferiti dalle schede qui sotto.", false);
    }

    @FXML
    public void onClickPreOrdina() {
        try {
            if (carrelloVaschette.isEmpty()) {
                boolean conPanna = checkPanna != null && checkPanna.isSelected();
                if (!gustiNellaVaschetta.isEmpty() || conPanna) {
                    onClickAggiungiVaschettaAlCarrello();
                } else {
                    mostraEsito("⚠️ Aggiungi almeno un prodotto o una vaschetta al carrello prima di confermare l'ordine.", true);
                    return;
                }
            }

            OrdineBean bean = new OrdineBean();
            bean.setEmailCliente(SessionManager.getInstance().getEmailUtenteLoggato());
            bean.setVaschette(carrelloVaschette);

            String tipo = (comboTipoConsegna != null && comboTipoConsegna.getValue() != null)
                    ? comboTipoConsegna.getValue()
                    : "🛵 Consegna a Domicilio";
            String orario = (comboOrarioConsegna != null && comboOrarioConsegna.getValue() != null)
                    ? comboOrarioConsegna.getValue()
                    : ((comboGiornoOrdine != null && comboGiornoOrdine.getValue() != null) ? comboGiornoOrdine.getValue() : "Subito (~25-35 min)");
            String note = (campoIndirizzoNote != null && !campoIndirizzoNote.getText().trim().isEmpty())
                    ? (" • Indirizzo/Note: " + campoIndirizzoNote.getText().trim())
                    : "";
            String modalitaRitiro = tipo + " (" + orario + ")" + note;
            bean.setDataRitiro(modalitaRitiro);

            OrdineBean confermato = new ProduzioneOrdiniControl().effettuaPreOrdine(bean);
            mostraEsito("🛵 Ordine " + confermato.getCodiceOrdine() + " inviato alla Gelateria! Riceverai la notifica di conferma da Maestro o Dipendenti (" + orario + ").", false);

            carrelloVaschette.clear();
            gustiNellaVaschetta.clear();
            if (checkPanna != null) checkPanna.setSelected(false);
            if (checkGranella != null) checkGranella.setSelected(false);

            aggiornaVaschettaGrafica();
            aggiornaBoxGustiDisponibili();
            aggiornaCarrelloGrafico();
            aggiornaMieiOrdini();
        } catch (FrostlyException e) {
            mostraEsito("⚠️ " + e.getMessage(), true);
        }
    }

    private void aggiornaMieiOrdini() {
        if (listaMieiOrdini == null) return;
        String email = SessionManager.getInstance().getEmailUtenteLoggato();
        listaMieiOrdini.getItems().clear();
        listaMieiOrdini.getItems().addAll(new ProduzioneOrdiniControl().visualizzaOrdiniCliente(email));
    }

    @FXML
    public void onClickAggiornaOrdini() {
        aggiornaMieiOrdini();
        mostraEsito("Elenco ordini aggiornato.", false);
    }

    @FXML
    public void onClickRichiediGusto() {
        try {
            RichiestaGustoBean bean = new RichiestaGustoBean();
            bean.setEmailCliente(SessionManager.getInstance().getEmailUtenteLoggato());
            bean.setNomeGustoProposto(campoNomeGustoCustom.getText());

            String ingrediente = (comboIngredienteCustom != null && comboIngredienteCustom.getValue() != null)
                    ? comboIngredienteCustom.getValue()
                    : (campoIngredienteCustom != null ? campoIngredienteCustom.getText() : "Pistacchio di Bronte DOP");
            bean.setNomeIngredientePrincipale(ingrediente);

            bean.setQuantitaDaTesto(campoKgCustom.getText());
            bean.setDataRitiroDesiderata(campoDataCustom.getText());

            String desc = (campoDescrizioneCustom != null && !campoDescrizioneCustom.getText().trim().isEmpty())
                    ? campoDescrizioneCustom.getText().trim()
                    : "Richiesta personalizzata da app";
            bean.setNoteCliente(desc);

            RichiestaGustoBean creata = new RichiestaGustoControl().inviaRichiestaGusto(bean);
            mostraEsito("✅ Proposta per '" + creata.getNomeGustoProposto() + "' inviata al Maestro! Codice: " + creata.getCodiceRichiesta(), false);
            aggiornaVistaCliente();
            aggiornaAnteprimaGustoCustom();
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
        for (GustoBean g : new ProduzioneOrdiniControl().visualizzaMenuGusti()) {
            tuttiProdottiMenu.add(g);
            if ("Gelati".equalsIgnoreCase(g.getCategoria())) {
                catalogoGustiGelato.add(g);
            }
        }
        if (listaMenuGusti != null) {
            listaMenuGusti.getItems().clear();
            listaMenuGusti.getItems().addAll(catalogoGustiGelato);
        }
        aggiornaBoxPannaSempreVisibile();
        impostaCategoriaCatalogo(categoriaCatalogoSelezionata);
        if (listaMieRichieste != null) {
            listaMieRichieste.getItems().clear();
            for (RichiestaGustoBean r : new RichiestaGustoControl().visualizzaRichiesteCliente(email)) {
                listaMieRichieste.getItems().add(r.getCodiceRichiesta() + " | '" + r.getNomeGustoProposto()
                        + "' | Stato: " + r.getStato() + " | Risposta: " + r.getRispostaMaestro());
            }
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
