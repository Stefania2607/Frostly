package view;

import bean.GustoBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Vista catalogo in stile Provenza (crema e lavanda) dedicata esclusivamente alla GUI 2.
 * Integra categorie reali, ricerca istantanea, filtri per sottocategoria e preferiti,
 * schede quadrate con popup di dettaglio e collegamento diretto a Vaschetta e Carrello.
 */
public class CatalogoProvenzaGui2View {

    private static final String FILTRO_TUTTI = "Tutti";
    private static final String FILTRO_PREFERITI = "♥ Preferiti";

    private final VBox container;
    private final Consumer<String> onCategoriaCambiata;
    private final Consumer<GustoBean> onAggiungiAllaVaschetta;
    private final Consumer<GustoBean> onAggiungiAlCarrello;

    private List<String> categorie = new ArrayList<>();
    private List<GustoBean> prodotti = new ArrayList<>();
    private List<String> gustiInVaschetta = new ArrayList<>();
    private final Set<String> preferiti = new HashSet<>();

    private String categoriaAttiva = "Gelati";
    private String filtroAttivo = FILTRO_TUTTI;
    private String queryRicerca = "";

    private FlowPane barraCategorie;
    private FlowPane barraFiltri;
    private TextField campoRicerca;
    private Label lblConteggio;
    private FlowPane grigliaProdotti;
    private VBox boxStatoVuoto;

    public CatalogoProvenzaGui2View(VBox container,
                                    Consumer<String> onCategoriaCambiata,
                                    Consumer<GustoBean> onAggiungiAllaVaschetta,
                                    Consumer<GustoBean> onAggiungiAlCarrello) {
        this.container = container;
        this.onCategoriaCambiata = onCategoriaCambiata;
        this.onAggiungiAllaVaschetta = onAggiungiAllaVaschetta;
        this.onAggiungiAlCarrello = onAggiungiAlCarrello;
    }

    public void aggiornaDati(List<String> categorieDisponibili,
                             List<GustoBean> tuttiProdotti,
                             List<GustoBean> gustiNellaVaschetta) {
        this.categorie = categorieDisponibili != null ? new ArrayList<>(categorieDisponibili) : new ArrayList<>();
        this.prodotti = tuttiProdotti != null ? new ArrayList<>(tuttiProdotti) : new ArrayList<>();
        this.gustiInVaschetta = estraiNomiGusti(gustiNellaVaschetta);

        if (!this.categorie.isEmpty() && !contieneCategoria(this.categoriaAttiva)) {
            this.categoriaAttiva = this.categorie.get(0);
        }

        if (container.getChildren().isEmpty()) {
            costruisciLayout();
        }
        renderCategorie();
        renderFiltri();
        renderGriglia();
        if (onCategoriaCambiata != null) {
            onCategoriaCambiata.accept(categoriaAttiva);
        }
    }

    public void aggiornaSelezioneVaschetta(List<GustoBean> gustiNellaVaschetta) {
        this.gustiInVaschetta = estraiNomiGusti(gustiNellaVaschetta);
        if (grigliaProdotti != null) {
            renderGriglia();
        }
    }

    private static List<String> estraiNomiGusti(List<GustoBean> lista) {
        List<String> nomi = new ArrayList<>();
        if (lista != null) {
            for (GustoBean g : lista) {
                if (g != null && g.getNome() != null) {
                    nomi.add(g.getNome());
                }
            }
        }
        return nomi;
    }

    public String getCategoriaAttiva() {
        return categoriaAttiva;
    }

    private void costruisciLayout() {
        container.getChildren().clear();
        container.setSpacing(10);
        container.setStyle("-fx-background-color: linear-gradient(to bottom right, #fdf9f6, #fbf1f0, #f7f0ed); "
                + "-fx-background-radius: 20; -fx-border-color: #e9dbdf; -fx-border-radius: 20; -fx-padding: 12 16;");

        // 1. Barra Categorie Provenza fissa (senza scorrimento orizzontale)
        barraCategorie = new FlowPane(6, 6);
        barraCategorie.setAlignment(Pos.CENTER_LEFT);
        barraCategorie.setStyle("-fx-background-color: #fffcf9; -fx-background-radius: 20; "
                + "-fx-border-color: #e9dbdf; -fx-border-radius: 20; -fx-padding: 7 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(86,61,73,0.08), 10, 0, 0, 3);");

        // 2. Corpo principale: Colonna Lavanda full-height a sinistra + Catalogo a destra
        HBox corpo = new HBox(16);
        VBox.setVgrow(corpo, Priority.ALWAYS);

        StackPane pannelloLavanda = creaPannelloLavanda();

        VBox areaCatalogo = new VBox(10);
        HBox.setHgrow(areaCatalogo, Priority.ALWAYS);

        // Strumenti catalogo: ricerca + filtri + conteggio
        HBox tools = new HBox(10);
        tools.setAlignment(Pos.CENTER_LEFT);

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPrefWidth(240);
        searchBox.setMinWidth(200);
        searchBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2d3df; -fx-border-radius: 22; "
                + "-fx-background-radius: 22; -fx-padding: 4 14; "
                + "-fx-effect: dropshadow(gaussian, rgba(86,61,73,0.06), 6, 0, 0, 2);");

        Label icoSearch = new Label("🔍");
        icoSearch.setMinWidth(Region.USE_PREF_SIZE);
        icoSearch.setStyle("-fx-font-size: 13px; -fx-text-fill: #764c86;");

        campoRicerca = new TextField();
        campoRicerca.setPromptText("Cerca un gusto...");
        campoRicerca.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; "
                + "-fx-text-fill: #37253d; -fx-prompt-text-fill: #8d7c92; -fx-font-size: 12.5px; -fx-padding: 4 0;");
        HBox.setHgrow(campoRicerca, Priority.ALWAYS);
        campoRicerca.textProperty().addListener((obs, oldV, newV) -> {
            queryRicerca = newV != null ? newV.trim() : "";
            renderGriglia();
        });
        searchBox.getChildren().addAll(icoSearch, campoRicerca);

        barraFiltri = new FlowPane(6, 6);
        barraFiltri.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(barraFiltri, Priority.ALWAYS);

        lblConteggio = new Label("0 gusti");
        lblConteggio.setMinWidth(Region.USE_PREF_SIZE);
        lblConteggio.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #5e316f;");

        tools.getChildren().addAll(searchBox, barraFiltri, lblConteggio);

        // Griglia prodotti scrollabile
        grigliaProdotti = new FlowPane(14, 14);
        grigliaProdotti.setAlignment(Pos.TOP_LEFT);
        grigliaProdotti.setStyle("-fx-padding: 4 4 14 4;");

        boxStatoVuoto = new VBox(8);
        boxStatoVuoto.setAlignment(Pos.CENTER);
        boxStatoVuoto.setStyle("-fx-background-color: #fffcf8; -fx-border-color: #c8b5c8; -fx-border-style: dashed; "
                + "-fx-border-radius: 18; -fx-background-radius: 18; -fx-padding: 44 24;");
        boxStatoVuoto.setVisible(false);
        boxStatoVuoto.setManaged(false);
        Label icoVuoto = new Label("✿");
        icoVuoto.setStyle("-fx-font-size: 36px; -fx-text-fill: #764c86;");
        Label titVuoto = new Label("Nessun prodotto trovato");
        titVuoto.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #37253d;");
        Label subVuoto = new Label("Prova un'altra ricerca oppure scegli una categoria diversa.");
        subVuoto.setStyle("-fx-font-size: 13px; -fx-text-fill: #756978;");
        Button btnReset = new Button("Mostra tutti i prodotti");
        btnReset.setStyle("-fx-background-color: linear-gradient(to right, #9366a0, #6d427b); -fx-text-fill: #ffffff; "
                + "-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-background-radius: 20; -fx-padding: 8 18; -fx-cursor: hand;");
        btnReset.setOnAction(e -> {
            campoRicerca.clear();
            filtroAttivo = FILTRO_TUTTI;
            renderFiltri();
            renderGriglia();
        });
        boxStatoVuoto.getChildren().addAll(icoVuoto, titVuoto, subVuoto, btnReset);

        VBox scrollContent = new VBox(10, grigliaProdotti, boxStatoVuoto);
        ScrollPane scrollGrid = new ScrollPane(scrollContent);
        scrollGrid.setFitToWidth(true);
        scrollGrid.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollGrid.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollGrid, Priority.ALWAYS);

        areaCatalogo.getChildren().addAll(barraCategorie, tools, scrollGrid);
        corpo.getChildren().addAll(pannelloLavanda, areaCatalogo);
        container.getChildren().add(corpo);
    }

    private StackPane creaPannelloLavanda() {
        StackPane panel = new StackPane();
        panel.setMinWidth(130);
        panel.setPrefWidth(136);
        panel.setMaxWidth(140);
        panel.setStyle("-fx-background-color: #faf5ef; -fx-background-radius: 18; -fx-border-color: #e8d9dd; "
                + "-fx-border-radius: 18; -fx-effect: dropshadow(gaussian, rgba(86,61,73,0.10), 12, 0, 0, 4);");

        Image imgLavanda = RecensioniView.caricaImmagineRecensione("/lavanda_provenza.png");
        if (imgLavanda != null) {
            ImageView iv = new ImageView(imgLavanda);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            iv.setFitWidth(134);
            iv.fitHeightProperty().bind(panel.heightProperty().subtract(2));
            Rectangle clip = new Rectangle();
            clip.setWidth(134);
            clip.heightProperty().bind(panel.heightProperty().subtract(2));
            clip.setArcWidth(34);
            clip.setArcHeight(34);
            iv.setClip(clip);
            panel.getChildren().add(iv);
        }

        return panel;
    }

    private void renderCategorie() {
        barraCategorie.getChildren().clear();
        for (String cat : categorie) {
            boolean attiva = cat.equalsIgnoreCase(categoriaAttiva);
            Button btn = new Button(iconaCategoria(cat) + "  " + cat);
            btn.setMinWidth(Region.USE_PREF_SIZE);
            if (attiva) {
                btn.setStyle("-fx-background-color: #764c86; -fx-text-fill: #ffffff; -fx-font-weight: bold; "
                        + "-fx-font-size: 11.5px; -fx-background-radius: 18; -fx-padding: 5 11; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: #f3eae5; -fx-text-fill: #42323f; -fx-font-weight: bold; "
                        + "-fx-font-size: 11.5px; -fx-background-radius: 18; -fx-padding: 5 11; -fx-cursor: hand;");
            }
            btn.setOnAction(e -> {
                categoriaAttiva = cat;
                filtroAttivo = FILTRO_TUTTI;
                renderCategorie();
                renderFiltri();
                renderGriglia();
                if (onCategoriaCambiata != null) {
                    onCategoriaCambiata.accept(cat);
                }
            });
            barraCategorie.getChildren().add(btn);
        }
    }

    private void renderFiltri() {
        barraFiltri.getChildren().clear();
        Set<String> filtri = new LinkedHashSet<>();
        filtri.add(FILTRO_TUTTI);
        for (GustoBean g : prodotti) {
            if (categoriaAttiva.equalsIgnoreCase(g.getCategoria())
                    && g.getSottocategoria() != null && !g.getSottocategoria().isBlank()) {
                filtri.add(g.getSottocategoria().trim());
            }
        }
        filtri.add(FILTRO_PREFERITI);

        for (String f : filtri) {
            boolean attivo = f.equalsIgnoreCase(filtroAttivo);
            Button pill = new Button(f);
            pill.setMinWidth(Region.USE_PREF_SIZE);
            if (attivo) {
                pill.setStyle("-fx-background-color: #764c86; -fx-text-fill: #ffffff; -fx-font-weight: bold; "
                        + "-fx-font-size: 12px; -fx-background-radius: 18; -fx-padding: 5 12; -fx-cursor: hand;");
            } else {
                pill.setStyle("-fx-background-color: #f3eae5; -fx-text-fill: #42323f; -fx-font-weight: bold; "
                        + "-fx-font-size: 12px; -fx-background-radius: 18; -fx-padding: 5 12; -fx-cursor: hand;");
            }
            pill.setOnAction(e -> {
                filtroAttivo = f;
                renderFiltri();
                renderGriglia();
            });
            barraFiltri.getChildren().add(pill);
        }
    }

    private void renderGriglia() {
        grigliaProdotti.getChildren().clear();

        List<GustoBean> visibili = new ArrayList<>();
        String q = queryRicerca.toLowerCase(Locale.ITALIAN);
        for (GustoBean g : prodotti) {
            if (!categoriaAttiva.equalsIgnoreCase(g.getCategoria())) {
                continue;
            }
            if (FILTRO_PREFERITI.equals(filtroAttivo) && !preferiti.contains(g.getNome())) {
                continue;
            }
            if (!FILTRO_TUTTI.equals(filtroAttivo) && !FILTRO_PREFERITI.equals(filtroAttivo)) {
                String sc = g.getSottocategoria() != null ? g.getSottocategoria().trim() : "";
                if (!filtroAttivo.equalsIgnoreCase(sc)) {
                    continue;
                }
            }
            if (!q.isEmpty()) {
                String testo = ((g.getNome() != null ? g.getNome() : "") + " "
                        + (g.getDescrizione() != null ? g.getDescrizione() : "") + " "
                        + (g.getAllergeni() != null ? g.getAllergeni() : "")).toLowerCase(Locale.ITALIAN);
                if (!testo.contains(q)) {
                    continue;
                }
            }
            visibili.add(g);
        }

        lblConteggio.setText(visibili.size() + ("Gelati".equalsIgnoreCase(categoriaAttiva) ? " gusti · " : " prodotti · ") + categoriaAttiva);

        if (visibili.isEmpty()) {
            grigliaProdotti.setVisible(false);
            grigliaProdotti.setManaged(false);
            boxStatoVuoto.setVisible(true);
            boxStatoVuoto.setManaged(true);
            return;
        }
        grigliaProdotti.setVisible(true);
        grigliaProdotti.setManaged(true);
        boxStatoVuoto.setVisible(false);
        boxStatoVuoto.setManaged(false);

        for (int i = 0; i < visibili.size(); i++) {
            grigliaProdotti.getChildren().add(creaCardProdotto(visibili.get(i), i));
        }
    }

    private VBox creaCardProdotto(GustoBean g, int index) {
        boolean isGelato = "Gelati".equalsIgnoreCase(g.getCategoria());
        boolean sceltoInVaschetta = isGelato && gustiInVaschetta.contains(g.getNome());
        boolean isPref = preferiti.contains(g.getNome());

        VBox card = new VBox(0);
        card.setPrefWidth(205);
        card.setMinWidth(205);
        card.setMaxWidth(205);
        String borderNormal = sceltoInVaschetta ? "#764c86" : "#eadde3";
        String baseStyle = "-fx-background-color: #fffcf9; -fx-background-radius: 16; -fx-border-color: " + borderNormal
                + "; -fx-border-radius: 16; -fx-border-width: " + (sceltoInVaschetta ? "2" : "1") + "; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-border-color: #9366a0; "
                + "-fx-border-radius: 16; -fx-border-width: 1.5; -fx-cursor: hand;";
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        // Immagine quadrata in alto con badge stato
        StackPane boxImg = new StackPane();
        boxImg.setPrefSize(203, 155);
        boxImg.setMaxSize(203, 155);
        boxImg.setStyle("-fx-background-color: #eee1d7; -fx-background-radius: 15 15 0 0;");

        Image img = RecensioniView.caricaImmagineRidimensionata(g.getImmagineUrl(), 900, 600);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(203);
            iv.setFitHeight(155);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            Rectangle clip = new Rectangle(203, 170);
            clip.setArcWidth(30);
            clip.setArcHeight(30);
            boxImg.setClip(clip);
            boxImg.getChildren().add(iv);
        } else {
            Label fallback = new Label(iconaCategoria(g.getCategoria()));
            fallback.setStyle("-fx-font-size: 44px;");
            boxImg.getChildren().add(fallback);
        }

        Label badgeStato = new Label(g.isDisponibileOggi() ? "✓ Disponibile" : "✕ Non disponibile");
        badgeStato.setStyle("-fx-background-color: rgba(255,253,249,0.95); -fx-background-radius: 16; "
                + "-fx-padding: 3 9; -fx-font-size: 10px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + (g.isDisponibileOggi() ? "#2e7d32" : "#ad443f") + ";");
        StackPane.setAlignment(badgeStato, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeStato, new Insets(8));
        boxImg.getChildren().add(badgeStato);

        // Contenuto card
        VBox info = new VBox(5);
        info.setStyle("-fx-padding: 11 12 12 12;");

        HBox topline = new HBox(4);
        topline.setAlignment(Pos.TOP_LEFT);
        Label lblNome = new Label(g.getNome());
        lblNome.setWrapText(true);
        lblNome.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #37253d;");
        HBox.setHgrow(lblNome, Priority.ALWAYS);

        Button btnCuore = new Button(isPref ? "♥" : "♡");
        btnCuore.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-padding: 0 2; -fx-cursor: hand; "
                + "-fx-text-fill: " + (isPref ? "#ba4b6d" : "#764c86") + ";");
        btnCuore.setOnAction(e -> {
            e.consume();
            if (preferiti.contains(g.getNome())) {
                preferiti.remove(g.getNome());
            } else {
                preferiti.add(g.getNome());
            }
            renderGriglia();
        });
        topline.getChildren().addAll(lblNome, btnCuore);

        String descText = g.getDescrizione() != null && !g.getDescrizione().isBlank() ? g.getDescrizione() : "Specialità artigianale Frostly.";
        Label lblDesc = new Label(descText);
        lblDesc.setWrapText(true);
        lblDesc.setMaxHeight(36);
        lblDesc.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #5e525e;");

        String allText = g.getAllergeni() != null && !g.getAllergeni().isBlank() ? g.getAllergeni() : "nessuno";
        HBox metaPrezzoAll = new HBox(7);
        metaPrezzoAll.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(metaPrezzoAll, new Insets(3, 0, 2, 0));

        String prezzoFmt = String.format(Locale.ITALY, "€ %.2f", g.getPrezzoAlKg()) + (isGelato ? "/kg" : "");
        Label lblPrezzo = new Label(prezzoFmt);
        lblPrezzo.setMinWidth(Region.USE_PREF_SIZE);
        lblPrezzo.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #5e316f;");

        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: #d9cad4; -fx-font-size: 11px;");

        Label lblAll = new Label("Allergeni: " + allText);
        lblAll.setStyle("-fx-font-size: 10px; -fx-text-fill: #625765;");
        HBox.setHgrow(lblAll, Priority.ALWAYS);
        metaPrezzoAll.getChildren().addAll(lblPrezzo, sep, lblAll);

        Button btnAzione = new Button(isGelato ? (sceltoInVaschetta ? "✓ Scelto in Vaschetta" : "+ Aggiungi alla Vaschetta") : "+ Aggiungi al Carrello");
        btnAzione.setMaxWidth(Double.MAX_VALUE);
        btnAzione.setDisable(!g.isDisponibileOggi());
        btnAzione.setStyle(sceltoInVaschetta
                ? "-fx-background-color: #387548; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 16; -fx-padding: 5 10; -fx-cursor: hand;"
                : "-fx-background-color: linear-gradient(to right, #9366a0, #6d427b); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 16; -fx-padding: 5 10; -fx-cursor: hand;");
        btnAzione.setOnAction(e -> {
            e.consume();
            if (isGelato) {
                if (onAggiungiAllaVaschetta != null) onAggiungiAllaVaschetta.accept(g);
            } else {
                if (onAggiungiAlCarrello != null) onAggiungiAlCarrello.accept(g);
            }
        });

        info.getChildren().addAll(topline, lblDesc, metaPrezzoAll, btnAzione);
        card.getChildren().addAll(boxImg, info);

        card.setOnMouseClicked(e -> apriPopupDettaglio(g));
        return card;
    }

    private void apriPopupDettaglio(GustoBean g) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — " + g.getNome());
        Window owner = container.getScene() != null ? container.getScene().getWindow() : null;
        if (owner != null) {
            popup.initOwner(owner);
        }

        boolean isGelato = "Gelati".equalsIgnoreCase(g.getCategoria());

        VBox root = new VBox(12);
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color: #fffaf7; -fx-padding: 22 24;");

        Image img = RecensioniView.caricaImmagineRidimensionata(g.getImmagineUrl(), 900, 600);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(412);
            iv.setFitHeight(200);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            Rectangle clip = new Rectangle(412, 200);
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            iv.setClip(clip);
            root.getChildren().add(iv);
        }

        Label lblCat = new Label(iconaCategoria(g.getCategoria()) + "  " + g.getCategoria().toUpperCase(Locale.ITALIAN)
                + (g.getSottocategoria() != null && !g.getSottocategoria().isBlank() ? "  •  " + g.getSottocategoria() : ""));
        lblCat.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #764c86;");

        Label lblNome = new Label(g.getNome());
        lblNome.setWrapText(true);
        lblNome.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #37253d;");

        Label lblDesc = new Label(g.getDescrizione() != null ? g.getDescrizione() : "");
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #5e525e; -fx-line-spacing: 2.5;");

        Label lblAll = new Label("Allergeni: " + (g.getAllergeni() != null && !g.getAllergeni().isBlank() ? g.getAllergeni() : "Nessuno dichiarato"));
        lblAll.setWrapText(true);
        lblAll.setStyle("-fx-font-size: 12px; -fx-text-fill: #756978; -fx-background-color: #f3eae5; "
                + "-fx-padding: 7 12; -fx-background-radius: 10;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label lblPrezzo = new Label(isGelato ? "Formato vaschetta" : String.format("€ %.2f", g.getPrezzoAlKg()));
        lblPrezzo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5e316f;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: #f3eae5; -fx-text-fill: #37253d; -fx-font-weight: bold; "
                + "-fx-background-radius: 18; -fx-padding: 8 16; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());

        Button btnAdd = new Button(isGelato ? "+ Aggiungi alla Vaschetta" : "+ Aggiungi al Carrello");
        btnAdd.setDisable(!g.isDisponibileOggi());
        btnAdd.setStyle("-fx-background-color: linear-gradient(to right, #9366a0, #6d427b); -fx-text-fill: #ffffff; "
                + "-fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 8 18; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            popup.close();
            if (isGelato) {
                if (onAggiungiAllaVaschetta != null) onAggiungiAllaVaschetta.accept(g);
            } else {
                if (onAggiungiAlCarrello != null) onAggiungiAlCarrello.accept(g);
            }
        });

        footer.getChildren().addAll(lblPrezzo, sp, btnChiudi, btnAdd);
        root.getChildren().addAll(lblCat, lblNome, lblDesc, lblAll, footer);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    private Image caricaImmagineProdotto(String url) {
        return RecensioniView.caricaImmagineRidimensionata(url, 520, 400);
    }

    private boolean contieneCategoria(String cat) {
        for (String c : categorie) {
            if (c.equalsIgnoreCase(cat)) {
                return true;
            }
        }
        return false;
    }

    private static String iconaCategoria(String cat) {
        if (cat == null) return "🍨";
        return switch (cat.toLowerCase(Locale.ITALIAN)) {
            case "gelati" -> "🍨";
            case "crepes" -> "🥞";
            case "panna" -> "☁️";
            case "cioccolate calde" -> "🍫";
            case "bevande" -> "🥤";
            case "caffetteria" -> "☕";
            case "biscotti" -> "🍪";
            case "crostate" -> "🥧";
            case "tisane" -> "🍵";
            default -> "✨";
        };
    }
}
