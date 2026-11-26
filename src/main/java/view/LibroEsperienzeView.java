package view;

import bean.RecensioneBean;
import control.applicativo.RecensioniControl;
import frostly.exception.FrostlyException;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
 * Vista editoriale completa de "Il Libro delle Esperienze" per la GUI 2 (Salotto Alabastro & Salvia).
 * Include hero asimmetrica, Sigillo d'Eccellenza, Barometro Sensoriale, filtri a pillole,
 * griglia a 2 colonne con supporto foto e lightbox, e modale per comporre una nuova cartolina con foto.
 */
public class LibroEsperienzeView {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String FILTER_ALL = "all";
    private static final String FILTER_PHOTO = "photo";
    private static final String FILTER_FIVE = "5";
    private static final String FILTER_MAESTRO = "maestro";

    private static final String SORT_RECENTI = "Più recenti";
    private static final String SORT_ISPIRANTI = "Più ispiranti (Brindisi)";
    private static final String SORT_ALTE = "Voto più alto";

    private static final String[] DESCRITTORI_VOTO = {
        "", "Da migliorare", "Discreto", "Piacevole", "Eccellente", "Sublime"
    };

    private final VBox container;
    private final String autoreEmail;
    private final String autoreNome;
    private final Consumer<RecensioneBean> onBrinda;
    private final BiConsumer<String, Boolean> onEsito;
    private final Runnable onAggiornaDopoPubblicazione;
    private final RecensioniControl recensioniControl = new RecensioniControl();

    private List<RecensioneBean> recensioni = new ArrayList<>();
    private String filtroAttivo = FILTER_ALL;
    private String ordinamentoAttivo = SORT_RECENTI;

    private Label lblMediaHero;
    private Label lblStelleHero;
    private Label lblPercConsiglia;
    private Label lblConteggioHero;
    private HBox boxPilloleFiltro;
    private GridPane grigliaCartoline;
    private VBox boxStatoVuoto;

    public LibroEsperienzeView(VBox container,
                               String autoreEmail,
                               String autoreNome,
                               Consumer<RecensioneBean> onBrinda,
                               BiConsumer<String, Boolean> onEsito,
                               Runnable onAggiornaDopoPubblicazione) {
        this.container = container;
        this.autoreEmail = autoreEmail;
        this.autoreNome = autoreNome;
        this.onBrinda = onBrinda;
        this.onEsito = onEsito;
        this.onAggiornaDopoPubblicazione = onAggiornaDopoPubblicazione;
    }

    public void aggiorna(List<RecensioneBean> lista) {
        this.recensioni = lista != null ? new ArrayList<>(lista) : new ArrayList<>();
        if (container.getChildren().isEmpty()) {
            costruisciStruttura();
        }
        aggiornaMetriche();
        aggiornaPillole();
        renderizzaCartoline();
    }

    private void costruisciStruttura() {
        container.getChildren().clear();
        container.setSpacing(22);

        // 1. Hero Section Asimmetrica
        HBox hero = new HBox(28);
        hero.setAlignment(Pos.CENTER_LEFT);

        VBox heroSinistra = new VBox(12);
        HBox.setHgrow(heroSinistra, Priority.ALWAYS);

        Label badgeEdizione = new Label("●  EDIZIONE AUTUNNO / INVERNO  •  CRONACHE DI SALA");
        badgeEdizione.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2DDD2; -fx-border-radius: 20; "
                + "-fx-background-radius: 20; -fx-padding: 5 14; -fx-font-size: 10.5px; -fx-font-weight: bold; "
                + "-fx-text-fill: #2D4A3E; -fx-letter-spacing: 0.8px;");

        Text t1 = new Text("Voci dal ");
        t1.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 40px; -fx-font-weight: bold; -fx-fill: #1B1E1C;");
        Text t2 = new Text("Salotto");
        t2.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 40px; -fx-font-style: italic; -fx-fill: #2D4A3E;");
        Text t3 = new Text("\ndel Freddo & del Tè.");
        t3.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 40px; -fx-font-weight: bold; -fx-fill: #1B1E1C;");
        TextFlow titoloFlow = new TextFlow(t1, t2, t3);
        titoloFlow.setLineSpacing(2);

        Label sottotitolo = new Label("Ogni cucchiaino di gelato mantecato al momento e ogni tazza d'infuso botanico lasciano un ricordo. Sfoglia le cartoline sensoriali scritte dai nostri ospiti.");
        sottotitolo.setWrapText(true);
        sottotitolo.setMaxWidth(580);
        sottotitolo.setStyle("-fx-font-size: 14px; -fx-text-fill: #686D69; -fx-line-spacing: 3;");

        HBox rigaCta = new HBox(16);
        rigaCta.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(rigaCta, new Insets(4, 0, 0, 0));

        Button btnLasciaTraccia = new Button("🪶  + Lascia la tua traccia");
        btnLasciaTraccia.setStyle("-fx-background-color: #C86D44; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; "
                + "-fx-font-size: 13.5px; -fx-background-radius: 24; -fx-padding: 11 22; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(200,109,68,0.38), 14, 0, 0, 5);");
        btnLasciaTraccia.setOnAction(e -> apriModaleNuovaCartolina());

        lblConteggioHero = new Label("🛡  0 degustazioni certificate");
        lblConteggioHero.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #686D69;");

        rigaCta.getChildren().addAll(btnLasciaTraccia, lblConteggioHero);
        heroSinistra.getChildren().addAll(badgeEdizione, titoloFlow, sottotitolo, rigaCta);

        // Colonna Destra: Sigillo d'Eccellenza
        HBox cardSigillo = new HBox(18);
        cardSigillo.setAlignment(Pos.CENTER);
        cardSigillo.setMinWidth(350);
        cardSigillo.setMaxWidth(370);
        cardSigillo.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 24; -fx-border-color: #E2DDD2; "
                + "-fx-border-radius: 24; -fx-padding: 22 24; "
                + "-fx-effect: dropshadow(gaussian, rgba(27,30,28,0.08), 22, 0, 0, 8);");

        VBox sigilloSinistra = new VBox(5);
        HBox.setHgrow(sigilloSinistra, Priority.ALWAYS);

        Label lblSigTit = new Label("SIGILLO D'ECCELLENZA");
        lblSigTit.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #C86D44; -fx-letter-spacing: 1px;");

        HBox rigaVoto = new HBox(4);
        rigaVoto.setAlignment(Pos.BASELINE_LEFT);
        lblMediaHero = new Label("5.0");
        lblMediaHero.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 46px; -fx-font-weight: bold; -fx-text-fill: #1B1E1C;");
        Label lblSu5 = new Label("/ 5");
        lblSu5.setStyle("-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-size: 18px; -fx-text-fill: #686D69;");
        rigaVoto.getChildren().addAll(lblMediaHero, lblSu5);

        lblStelleHero = new Label("★★★★★");
        lblStelleHero.setStyle("-fx-font-size: 17px; -fx-text-fill: #C86D44; -fx-letter-spacing: 1.5px;");

        lblPercConsiglia = new Label("Raccomandato dal 100% degli ospiti del Tea Salon.");
        lblPercConsiglia.setWrapText(true);
        lblPercConsiglia.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #686D69;");

        sigilloSinistra.getChildren().addAll(lblSigTit, rigaVoto, lblStelleHero, lblPercConsiglia);

        StackPane medaglione = new StackPane();
        medaglione.setMinSize(96, 96);
        medaglione.setPrefSize(96, 96);
        medaglione.setMaxSize(96, 96);
        medaglione.setStyle("-fx-background-color: #F7F4EE; -fx-background-radius: 48; "
                + "-fx-border-color: #2D4A3E; -fx-border-radius: 48; -fx-border-width: 1.4; -fx-border-style: dashed;");

        VBox centroMed = new VBox(1);
        centroMed.setAlignment(Pos.CENTER);
        centroMed.setMaxSize(64, 64);
        centroMed.setStyle("-fx-background-color: #EFECE4; -fx-background-radius: 32; -fx-border-color: #E2DDD2; -fx-border-radius: 32;");
        Label lblN1 = new Label("N°1");
        lblN1.setStyle("-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #C86D44;");
        Label lblArt = new Label("ARTIGIANI");
        lblArt.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: #2D4A3E; -fx-letter-spacing: 0.6px;");
        centroMed.getChildren().addAll(lblN1, lblArt);
        medaglione.getChildren().add(centroMed);

        cardSigillo.getChildren().addAll(sigilloSinistra, medaglione);
        hero.getChildren().addAll(heroSinistra, cardSigillo);

        // 2. Barometro Sensoriale della Maison (4 Pilastri)
        VBox sezioneBarometro = new VBox(10);
        Label lblBarTit = new Label("BAROMETRO SENSORIALE DELLA MAISON  —  L'ECCELLENZA DEL SALOTTO");
        lblBarTit.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #686D69; -fx-letter-spacing: 1.2px;");

        GridPane grigliaPilastri = new GridPane();
        grigliaPilastri.setHgap(14);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            grigliaPilastri.getColumnConstraints().add(col);
        }
        grigliaPilastri.add(creaPilastro("🍨", "99%", "Velluto & Mantecatura", "Gelati Artigianali & Sorbetti", "#2D4A3E", 0.99), 0, 0);
        grigliaPilastri.add(creaPilastro("🍵", "98%", "Infusione & Calore", "Tea Salon & Caffetteria", "#C86D44", 0.98), 1, 0);
        grigliaPilastri.add(creaPilastro("🥞", "97%", "Croccantezza & Panna", "Crepes Calde & Cioccolate", "#2D4A3E", 0.97), 2, 0);
        grigliaPilastri.add(creaPilastro("✨", "99%", "Atmosfera & Rituale", "Ospitalità & Servizio al Tavolo", "#C86D44", 0.99), 3, 0);
        sezioneBarometro.getChildren().addAll(lblBarTit, grigliaPilastri);

        // 3. Barra Filtri e Ordinamento
        HBox barraFiltri = new HBox(14);
        barraFiltri.setAlignment(Pos.CENTER_LEFT);
        barraFiltri.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 24; -fx-border-color: #E2DDD2; "
                + "-fx-border-radius: 24; -fx-padding: 10 16;");

        boxPilloleFiltro = new HBox(8);
        boxPilloleFiltro.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(boxPilloleFiltro, Priority.ALWAYS);

        Label lblOrdina = new Label("ORDINA PER:");
        lblOrdina.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #686D69;");

        ComboBox<String> comboOrdina = new ComboBox<>(FXCollections.observableArrayList(
                SORT_RECENTI, SORT_ISPIRANTI, SORT_ALTE));
        comboOrdina.setValue(ordinamentoAttivo);
        comboOrdina.getStyleClass().add("luxury-combo");
        comboOrdina.setPrefWidth(185);
        comboOrdina.setOnAction(e -> {
            if (comboOrdina.getValue() != null) {
                ordinamentoAttivo = comboOrdina.getValue();
                renderizzaCartoline();
            }
        });

        barraFiltri.getChildren().addAll(boxPilloleFiltro, lblOrdina, comboOrdina);

        // 4. Griglia 2 Colonne Cartoline d'Esperienza
        grigliaCartoline = new GridPane();
        grigliaCartoline.setHgap(20);
        grigliaCartoline.setVgap(20);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);
        grigliaCartoline.getColumnConstraints().addAll(c1, c2);

        // 5. Stato Vuoto
        boxStatoVuoto = new VBox(10);
        boxStatoVuoto.setAlignment(Pos.CENTER);
        boxStatoVuoto.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 22; -fx-border-color: #E2DDD2; "
                + "-fx-border-radius: 22; -fx-padding: 40 24;");
        boxStatoVuoto.setVisible(false);
        boxStatoVuoto.setManaged(false);
        Label lblVuotoTit = new Label("Nessuna cartolina trovata");
        lblVuotoTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1B1E1C;");
        Label lblVuotoSub = new Label("Non ci sono ancora cartoline con questo filtro. Prova a mostrare tutte le esperienze.");
        lblVuotoSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #686D69;");
        Button btnReset = new Button("Mostra tutte le cartoline");
        btnReset.getStyleClass().add("btn-primary");
        btnReset.setOnAction(e -> {
            filtroAttivo = FILTER_ALL;
            aggiornaPillole();
            renderizzaCartoline();
        });
        boxStatoVuoto.getChildren().addAll(lblVuotoTit, lblVuotoSub, btnReset);

        container.getChildren().addAll(hero, sezioneBarometro, barraFiltri, grigliaCartoline, boxStatoVuoto);
    }

    private VBox creaPilastro(String icona, String perc, String titolo, String sotto, String coloreBarra, double ratio) {
        VBox card = new VBox(6);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 18; -fx-border-color: #E2DDD2; "
                + "-fx-border-radius: 18; -fx-padding: 16 18; "
                + "-fx-effect: dropshadow(gaussian, rgba(27,30,28,0.05), 12, 0, 0, 3);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBadge = new StackPane(new Label(icona));
        iconBadge.setMinSize(34, 34);
        iconBadge.setPrefSize(34, 34);
        iconBadge.setStyle("-fx-background-color: #EFECE4; -fx-background-radius: 10; -fx-font-size: 15px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label lblPerc = new Label(perc);
        lblPerc.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1B1E1C;");
        top.getChildren().addAll(iconBadge, sp, lblPerc);

        Label lblTit = new Label(titolo);
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1B1E1C;");
        Label lblSub = new Label(sotto);
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #686D69;");

        StackPane track = new StackPane();
        track.setAlignment(Pos.CENTER_LEFT);
        track.setPrefHeight(5);
        track.setMaxHeight(5);
        track.setStyle("-fx-background-color: #EFECE4; -fx-background-radius: 4;");
        Region fill = new Region();
        fill.setPrefHeight(5);
        fill.setMaxHeight(5);
        fill.setStyle("-fx-background-color: " + coloreBarra + "; -fx-background-radius: 4;");
        fill.maxWidthProperty().bind(track.widthProperty().multiply(ratio));
        fill.prefWidthProperty().bind(track.widthProperty().multiply(ratio));
        track.getChildren().add(fill);
        VBox.setMargin(track, new Insets(6, 0, 0, 0));

        card.getChildren().addAll(top, lblTit, lblSub, track);
        return card;
    }

    private void aggiornaMetriche() {
        int tot = recensioni.size();
        double media = recensioniControl.calcolaMediaVoti(recensioni);
        lblMediaHero.setText(String.format("%.1f", tot > 0 ? media : 0.0));
        int rounded = (int) Math.round(tot > 0 ? media : 0.0);
        lblStelleHero.setText("★".repeat(Math.max(0, Math.min(5, rounded))) + "☆".repeat(Math.max(0, 5 - rounded)));
        lblConteggioHero.setText("🛡  " + tot + (tot == 1 ? " degustazione certificata" : " degustazioni certificate"));

        int pos = 0;
        for (RecensioneBean r : recensioni) {
            if (r.getVotoStelle() >= 4) {
                pos++;
            }
        }
        int perc = tot > 0 ? (int) Math.round((pos * 100.0) / tot) : 100;
        lblPercConsiglia.setText("Raccomandato dal " + perc + "% degli ospiti del Tea Salon.");
    }

    private void aggiornaPillole() {
        boxPilloleFiltro.getChildren().clear();
        int conFoto = 0;
        int conMaestro = 0;
        for (RecensioneBean r : recensioni) {
            if (r.haFoto()) conFoto++;
            if (r.haRispostaMaestro()) conMaestro++;
        }
        boxPilloleFiltro.getChildren().addAll(
                creaPillola("Tutte le Esperienze (" + recensioni.size() + ")", FILTER_ALL),
                creaPillola("📷 Con Scatti (" + conFoto + ")", FILTER_PHOTO),
                creaPillola("5 Stelle ★", FILTER_FIVE),
                creaPillola("🪶 Risposta Maestro (" + conMaestro + ")", FILTER_MAESTRO)
        );
    }

    private Button creaPillola(String testo, String filtro) {
        boolean attiva = filtro.equals(filtroAttivo);
        Button btn = new Button(testo);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        if (attiva) {
            btn.setStyle("-fx-background-color: #2D4A3E; -fx-text-fill: #F7F4EE; -fx-font-weight: bold; "
                    + "-fx-font-size: 12px; -fx-background-radius: 20; -fx-padding: 7 15; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #EFECE4; -fx-text-fill: #1B1E1C; -fx-font-weight: bold; "
                    + "-fx-font-size: 12px; -fx-background-radius: 20; -fx-border-color: #E2DDD2; "
                    + "-fx-border-radius: 20; -fx-padding: 7 15; -fx-cursor: hand;");
        }
        btn.setOnAction(e -> {
            filtroAttivo = filtro.equals(filtroAttivo) && !FILTER_ALL.equals(filtro) ? FILTER_ALL : filtro;
            aggiornaPillole();
            renderizzaCartoline();
        });
        return btn;
    }

    private void renderizzaCartoline() {
        List<RecensioneBean> filtrate = new ArrayList<>();
        for (RecensioneBean r : recensioni) {
            if (FILTER_ALL.equals(filtroAttivo)
                    || (FILTER_PHOTO.equals(filtroAttivo) && r.haFoto())
                    || (FILTER_FIVE.equals(filtroAttivo) && r.getVotoStelle() == 5)
                    || (FILTER_MAESTRO.equals(filtroAttivo) && r.haRispostaMaestro())) {
                filtrate.add(r);
            }
        }

        Comparator<RecensioneBean> cmp = switch (ordinamentoAttivo) {
            case SORT_ISPIRANTI -> Comparator.comparingInt(RecensioneBean::getMiPiace).reversed();
            case SORT_ALTE -> Comparator.comparingInt(RecensioneBean::getVotoStelle).reversed()
                    .thenComparing(this::parseData, Comparator.reverseOrder());
            default -> Comparator.comparing(this::parseData, Comparator.reverseOrder());
        };
        filtrate.sort(cmp);

        grigliaCartoline.getChildren().clear();
        if (filtrate.isEmpty()) {
            grigliaCartoline.setVisible(false);
            grigliaCartoline.setManaged(false);
            boxStatoVuoto.setVisible(true);
            boxStatoVuoto.setManaged(true);
            return;
        }
        grigliaCartoline.setVisible(true);
        grigliaCartoline.setManaged(true);
        boxStatoVuoto.setVisible(false);
        boxStatoVuoto.setManaged(false);

        for (int i = 0; i < filtrate.size(); i++) {
            VBox card = creaCartolina(filtrate.get(i), i);
            grigliaCartoline.add(card, i % 2, i / 2);
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setVgrow(card, Priority.ALWAYS);
        }
    }

    private VBox creaCartolina(RecensioneBean r, int idx) {
        VBox card = new VBox(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        String baseStyle = "-fx-background-color: #FFFFFF; -fx-background-radius: 22; -fx-border-color: #E2DDD2; "
                + "-fx-border-radius: 22; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(27,30,28,0.07), 18, 0, 0, 6);";
        String hoverStyle = "-fx-background-color: #FFFFFF; -fx-background-radius: 22; -fx-border-color: #2D4A3E; "
                + "-fx-border-radius: 22; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(45,74,62,0.14), 24, 0, 0, 8);";
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        // Foto di copertina (se presente)
        Image foto = r.haFoto() ? RecensioniView.caricaImmagineRecensione(r.getFotoPath()) : null;
        if (foto != null) {
            StackPane boxFoto = new StackPane();
            boxFoto.setPrefHeight(210);
            boxFoto.setMaxHeight(210);
            boxFoto.setStyle("-fx-background-color: #EFECE4; -fx-background-radius: 21 21 0 0; -fx-cursor: hand;");

            ImageView iv = new ImageView(foto);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            iv.setFitHeight(210);
            iv.fitWidthProperty().bind(boxFoto.widthProperty());

            Rectangle clip = new Rectangle();
            clip.setArcWidth(42);
            clip.setArcHeight(42);
            clip.widthProperty().bind(boxFoto.widthProperty());
            clip.heightProperty().set(228);
            boxFoto.setClip(clip);

            Label badgeFoto = new Label("SALOTTO FROSTLY");
            badgeFoto.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-text-fill: #1B1E1C; "
                    + "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 11; "
                    + "-fx-background-radius: 16; -fx-border-color: #E2DDD2; -fx-border-radius: 16;");
            StackPane.setAlignment(badgeFoto, Pos.TOP_LEFT);
            StackPane.setMargin(badgeFoto, new Insets(12));

            Label badgeZoom = new Label("🔍");
            badgeZoom.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 16; "
                    + "-fx-padding: 5 8; -fx-font-size: 12px;");
            StackPane.setAlignment(badgeZoom, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(badgeZoom, new Insets(12));

            boxFoto.getChildren().addAll(iv, badgeFoto, badgeZoom);
            boxFoto.setOnMouseClicked(e -> {
                Window w = container.getScene() != null ? container.getScene().getWindow() : null;
                RecensioniView.apriLightboxFoto(w, r.getFotoPath(), r.getTitolo());
            });
            card.getChildren().add(boxFoto);
        }

        // Corpo della cartolina
        VBox corpo = new VBox(10);
        corpo.setStyle("-fx-padding: 20 22 18 22;");
        VBox.setVgrow(corpo, Priority.ALWAYS);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        if (foto == null) {
            Label badgeCat = new Label("SALOTTO FROSTLY");
            badgeCat.setStyle("-fx-background-color: #EFECE4; -fx-text-fill: #2D4A3E; -fx-font-size: 10px; "
                    + "-fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 14; "
                    + "-fx-border-color: #E2DDD2; -fx-border-radius: 14;");
            meta.getChildren().add(badgeCat);
        }
        Label lblData = new Label(r.getDataOra() != null ? r.getDataOra() : "");
        lblData.setStyle("-fx-font-size: 11px; -fx-text-fill: #686D69;");
        Region spMeta = new Region();
        HBox.setHgrow(spMeta, Priority.ALWAYS);
        Label lblStelle = new Label(r.getStelleIcone());
        lblStelle.setStyle("-fx-font-size: 15px; -fx-text-fill: #C86D44; -fx-letter-spacing: 1.5px;");
        meta.getChildren().addAll(lblData, spMeta, lblStelle);

        Label lblTitolo = new Label("“" + (r.getTitolo() != null ? r.getTitolo() : "") + "”");
        lblTitolo.setWrapText(true);
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-weight: bold; "
                + "-fx-font-size: 20px; -fx-text-fill: #1B1E1C;");

        Label lblCommento = new Label(r.getCommento() != null ? r.getCommento() : "");
        lblCommento.setWrapText(true);
        lblCommento.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #686D69; -fx-line-spacing: 2.6;");

        VBox contenuto = new VBox(8, meta, lblTitolo, lblCommento);
        VBox.setVgrow(contenuto, Priority.ALWAYS);

        if (r.haRispostaMaestro()) {
            VBox boxRisp = new VBox(4);
            boxRisp.setStyle("-fx-background-color: #F2F6F4; -fx-border-color: #E1EBE6; -fx-border-radius: 14; "
                    + "-fx-background-radius: 14; -fx-padding: 12 14;");
            Label lblRuolo = new Label("🪶  MAESTRA GELATAIA • LABORATORIO 1984");
            lblRuolo.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2D4A3E; -fx-letter-spacing: 0.8px;");
            Label lblTestoRisp = new Label("“" + r.getRispostaMaestro() + "”");
            lblTestoRisp.setWrapText(true);
            lblTestoRisp.setStyle("-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-size: 13.5px; -fx-text-fill: #1B1E1C;");
            boxRisp.getChildren().addAll(lblRuolo, lblTestoRisp);
            contenuto.getChildren().add(boxRisp);
        }

        // Footer autore + Brinda
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-border-color: #EFECE4 transparent transparent transparent; -fx-border-width: 1; -fx-padding: 12 0 0 0;");

        String nome = r.getAutoreNome() != null && !r.getAutoreNome().isBlank() ? r.getAutoreNome().trim() : "Ospite Frostly";
        StackPane avatar = new StackPane();
        avatar.setMinSize(36, 36);
        avatar.setPrefSize(36, 36);
        avatar.setStyle("-fx-background-color: #2D4A3E; -fx-background-radius: 18;");
        Label lblInit = new Label(calcolaIniziali(nome));
        lblInit.setStyle("-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #F7F4EE;");
        avatar.getChildren().add(lblInit);

        VBox autoreBox = new VBox(1);
        Label lblAutore = new Label(nome);
        lblAutore.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #1B1E1C;");
        Label lblVer = new Label("✓ Degustazione Verificata");
        lblVer.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #3D6353;");
        autoreBox.getChildren().addAll(lblAutore, lblVer);

        Region spFoot = new Region();
        HBox.setHgrow(spFoot, Priority.ALWAYS);

        Button btnBrinda = new Button("🥂  Brinda (" + r.getMiPiace() + ")");
        btnBrinda.setStyle("-fx-background-color: #EFECE4; -fx-text-fill: #1B1E1C; -fx-font-weight: bold; "
                + "-fx-font-size: 12px; -fx-background-radius: 18; -fx-border-color: #E2DDD2; "
                + "-fx-border-radius: 18; -fx-padding: 6 14; -fx-cursor: hand;");
        btnBrinda.setOnAction(e -> {
            if (onBrinda != null) {
                onBrinda.accept(r);
            }
        });

        footer.getChildren().addAll(avatar, autoreBox, spFoot, btnBrinda);
        corpo.getChildren().addAll(contenuto, footer);
        card.getChildren().add(corpo);

        animaCard(card, Math.min(idx, 6));
        return card;
    }

    private void apriModaleNuovaCartolina() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Componi la tua Cartolina");
        Window owner = container.getScene() != null ? container.getScene().getWindow() : null;
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(14);
        root.setPrefWidth(540);
        root.setStyle("-fx-background-color: #F7F4EE; -fx-padding: 24 28;");

        Label lblKicker = new Label("ARCHIVIO OSPITI FROSTLY");
        lblKicker.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #C86D44; -fx-letter-spacing: 1.2px;");
        Label lblTit = new Label("Componi la tua Cartolina");
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1B1E1C;");

        // Selettore Stelle Interattivo
        VBox boxStelle = new VBox(6);
        boxStelle.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2DDD2; -fx-border-radius: 14; "
                + "-fx-background-radius: 14; -fx-padding: 12 16;");
        HBox topStelle = new HBox(8);
        topStelle.setAlignment(Pos.CENTER_LEFT);
        Label lblGiudizio = new Label("IL TUO GIUDIZIO *");
        lblGiudizio.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #686D69;");
        Region spS = new Region();
        HBox.setHgrow(spS, Priority.ALWAYS);
        Label lblParolaVoto = new Label("Sublime");
        lblParolaVoto.setStyle("-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #C86D44;");
        topStelle.getChildren().addAll(lblGiudizio, spS, lblParolaVoto);

        final int[] votoScelto = {5};
        HBox rigaStelle = new HBox(6);
        Button[] starBtns = new Button[5];
        Runnable aggiornaStelle = () -> {
            for (int i = 0; i < 5; i++) {
                boolean piena = (i + 1) <= votoScelto[0];
                starBtns[i].setText(piena ? "★" : "☆");
                starBtns[i].setStyle("-fx-background-color: transparent; -fx-font-size: 26px; -fx-padding: 0 4; -fx-cursor: hand; "
                        + "-fx-text-fill: " + (piena ? "#C86D44" : "#D5CFC2") + ";");
            }
            lblParolaVoto.setText(DESCRITTORI_VOTO[votoScelto[0]]);
        };
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            Button sb = new Button("★");
            sb.setOnAction(e -> {
                votoScelto[0] = val;
                aggiornaStelle.run();
            });
            starBtns[i] = sb;
            rigaStelle.getChildren().add(sb);
        }
        aggiornaStelle.run();
        boxStelle.getChildren().addAll(topStelle, rigaStelle);

        Label lblTitField = new Label("Titolo della Cartolina *");
        lblTitField.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #1B1E1C;");
        TextField txtTitolo = new TextField();
        txtTitolo.setPromptText("Es. Un rituale pomeridiano fuori dal tempo");
        txtTitolo.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2DDD2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 9 12; -fx-text-fill: #1B1E1C;");

        Label lblCommField = new Label("Il tuo racconto sensoriale *");
        lblCommField.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #1B1E1C;");
        TextArea txtCommento = new TextArea();
        txtCommento.setPromptText("Racconta i profumi, la consistenza o l'accoglienza ricevuta nel salotto...");
        txtCommento.setWrapText(true);
        txtCommento.setPrefRowCount(4);
        txtCommento.setStyle("-fx-control-inner-background: #FFFFFF; -fx-text-fill: #1B1E1C; -fx-border-color: #E2DDD2; -fx-border-radius: 12; -fx-background-radius: 12;");

        // Sezione Upload Foto con Anteprima
        final String[] fotoSelezionata = {null};
        VBox boxFotoUpload = new VBox(8);
        boxFotoUpload.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2DDD2; -fx-border-radius: 14; "
                + "-fx-background-radius: 14; -fx-padding: 12 14;");

        HBox rigaUpload = new HBox(10);
        rigaUpload.setAlignment(Pos.CENTER_LEFT);
        Button btnScegliFoto = new Button("📷  Allega una foto dal PC");
        btnScegliFoto.setStyle("-fx-background-color: #EFECE4; -fx-text-fill: #2D4A3E; -fx-font-weight: bold; "
                + "-fx-font-size: 12.5px; -fx-background-radius: 18; -fx-border-color: #E2DDD2; -fx-border-radius: 18; -fx-padding: 6 14; -fx-cursor: hand;");
        Label lblNomeFoto = new Label("Nessuna foto allegata (opzionale)");
        lblNomeFoto.setStyle("-fx-font-size: 12px; -fx-text-fill: #686D69; -fx-font-style: italic;");
        HBox.setHgrow(lblNomeFoto, Priority.ALWAYS);
        Button btnRimuoviFoto = new Button("✕");
        btnRimuoviFoto.setVisible(false);
        btnRimuoviFoto.setManaged(false);
        btnRimuoviFoto.setStyle("-fx-background-color: #FCF5F2; -fx-text-fill: #A85632; -fx-font-weight: bold; "
                + "-fx-background-radius: 14; -fx-padding: 3 8; -fx-cursor: hand;");
        rigaUpload.getChildren().addAll(btnScegliFoto, lblNomeFoto, btnRimuoviFoto);

        ImageView previewFoto = new ImageView();
        previewFoto.setFitHeight(110);
        previewFoto.setPreserveRatio(true);
        previewFoto.setVisible(false);
        previewFoto.setManaged(false);

        btnScegliFoto.setOnAction(e -> {
            String path = RecensioniView.scegliESalvaFotoRecensione(popup);
            if (path != null) {
                fotoSelezionata[0] = path;
                lblNomeFoto.setText("✓ " + new File(path).getName());
                lblNomeFoto.setStyle("-fx-font-size: 12px; -fx-text-fill: #2D4A3E; -fx-font-weight: bold;");
                btnRimuoviFoto.setVisible(true);
                btnRimuoviFoto.setManaged(true);
                Image img = RecensioniView.caricaImmagineRecensione(path);
                if (img != null) {
                    previewFoto.setImage(img);
                    previewFoto.setVisible(true);
                    previewFoto.setManaged(true);
                }
            }
        });
        btnRimuoviFoto.setOnAction(e -> {
            fotoSelezionata[0] = null;
            lblNomeFoto.setText("Nessuna foto allegata (opzionale)");
            lblNomeFoto.setStyle("-fx-font-size: 12px; -fx-text-fill: #686D69; -fx-font-style: italic;");
            btnRimuoviFoto.setVisible(false);
            btnRimuoviFoto.setManaged(false);
            previewFoto.setImage(null);
            previewFoto.setVisible(false);
            previewFoto.setManaged(false);
        });
        boxFotoUpload.getChildren().addAll(rigaUpload, previewFoto);

        Label lblErrore = new Label();
        lblErrore.setWrapText(true);
        lblErrore.setStyle("-fx-text-fill: #B91C1C; -fx-font-weight: bold; -fx-font-size: 12px;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: transparent; -fx-text-fill: #686D69; -fx-font-weight: bold; "
                + "-fx-border-color: #E2DDD2; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 18; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> popup.close());

        Button btnPubblica = new Button("✦  Affiggi nel Libro delle Esperienze");
        btnPubblica.setStyle("-fx-background-color: #2D4A3E; -fx-text-fill: #F7F4EE; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 9 20; -fx-cursor: hand;");
        btnPubblica.setOnAction(e -> {
            try {
                RecensioneBean bean = new RecensioneBean(
                        autoreEmail, autoreNome, votoScelto[0], txtTitolo.getText(), txtCommento.getText());
                bean.setFotoPath(fotoSelezionata[0]);
                RecensioneBean salvata = recensioniControl.pubblicaRecensione(bean);
                popup.close();
                if (onEsito != null) {
                    onEsito.accept("✦ Cartolina affissa nel Libro delle Esperienze (" + salvata.getStelleIcone() + ")!", false);
                }
                if (onAggiornaDopoPubblicazione != null) {
                    onAggiornaDopoPubblicazione.run();
                }
            } catch (FrostlyException ex) {
                lblErrore.setText("⚠️ " + ex.getMessage());
            }
        });
        footer.getChildren().addAll(btnAnnulla, btnPubblica);

        root.getChildren().addAll(lblKicker, lblTit, boxStelle, lblTitField, txtTitolo,
                lblCommField, txtCommento, boxFotoUpload, lblErrore, footer);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    private void animaCard(VBox card, int slot) {
        card.setOpacity(0);
        card.setTranslateY(12);
        FadeTransition fade = new FadeTransition(Duration.millis(280), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition slide = new TranslateTransition(Duration.millis(280), card);
        slide.setFromY(12);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition anim = new ParallelTransition(fade, slide);
        anim.setDelay(Duration.millis(slot * 40L));
        anim.play();
    }

    private LocalDateTime parseData(RecensioneBean r) {
        if (r == null || r.getDataOra() == null || r.getDataOra().isBlank()) {
            return LocalDateTime.MIN;
        }
        try {
            return LocalDateTime.parse(r.getDataOra().trim(), DATA_FMT);
        } catch (Exception e) {
            return LocalDateTime.MIN;
        }
    }

    private static String calcolaIniziali(String nome) {
        String[] parti = nome.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parti.length); i++) {
            if (!parti[i].isEmpty()) {
                sb.append(Character.toUpperCase(parti[i].charAt(0)));
            }
        }
        return sb.length() > 0 ? sb.toString() : "OF";
    }
}
