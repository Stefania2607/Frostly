package view;

import bean.RecensioneBean;
import control.applicativo.RecensioniControl;
import frostly.exception.FrostlyException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Vista grafica della schermata pubblica delle recensioni (consultabile senza accesso).
 * Adatta il layout moderno a card e statistiche interattive al tema scuro e oro champagne di Frostly.
 */
public class RecensioniPubblicheView {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String FILTER_ALL = "all";
    private static final String FILTER_MAESTRO = "maestro";

    private static final String SORT_RECENTI = "Più recenti";
    private static final String SORT_UTILI = "Più utili";
    private static final String SORT_ALTE = "Valutazione più alta";
    private static final String SORT_BASSE = "Valutazione più bassa";

    private static final String[][] AVATAR_PALETTE = {
        {"rgba(186, 164, 135, 0.20)", "#e3cfa8", "rgba(186, 164, 135, 0.45)"},
        {"rgba(46, 125, 50, 0.22)", "#81c784", "rgba(129, 199, 132, 0.40)"},
        {"rgba(171, 71, 188, 0.20)", "#ce93d8", "rgba(206, 147, 216, 0.40)"},
        {"rgba(2, 136, 209, 0.20)", "#81d4fa", "rgba(129, 212, 250, 0.40)"},
        {"rgba(216, 67, 21, 0.20)", "#ffab91", "rgba(255, 171, 145, 0.40)"}
    };

    private final VBox container;
    private final Runnable onRichiediLogin;
    private final RecensioniControl recensioniControl = new RecensioniControl();

    private List<RecensioneBean> tutteRecensioni = new ArrayList<>();
    private String filtroAttivo = FILTER_ALL;
    private String ordinamentoAttivo = SORT_RECENTI;

    private Label lblMedia;
    private Label lblStelleMedia;
    private Label lblTotaleRecensioni;
    private Label lblConsiglia;
    private VBox boxBarreDistribuzione;
    private HBox boxPilloleFiltro;
    private HBox bannerFiltroAttivo;
    private Label lblTestoFiltroAttivo;
    private GridPane grigliaRecensioni;
    private VBox boxStatoVuoto;

    public RecensioniPubblicheView(VBox container, Runnable onRichiediLogin) {
        this.container = container;
        this.onRichiediLogin = onRichiediLogin;
    }

    public void carica() {
        try {
            tutteRecensioni = recensioniControl.visualizzaTutteRecensioni();
        } catch (FrostlyException e) {
            tutteRecensioni = new ArrayList<>();
        }
        costruisciLayout();
        aggiornaRiepilogo();
        aggiornaPilloleFiltro();
        renderizzaRecensioni();
    }

    private void costruisciLayout() {
        container.getChildren().clear();

        // 1. Intestazione sezione
        HBox header = new HBox(20);
        header.setAlignment(Pos.BOTTOM_LEFT);

        VBox headerTesti = new VBox(8);
        HBox.setHgrow(headerTesti, Priority.ALWAYS);

        Label badge = new Label("🛡  FEEDBACK AUTENTICI");
        badge.setStyle("-fx-background-color: rgba(186, 164, 135, 0.14); -fx-text-fill: #baa487; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12; "
                + "-fx-background-radius: 20; -fx-border-color: rgba(186, 164, 135, 0.35); -fx-border-radius: 20;");

        Label titolo = new Label("Cosa dicono i nostri clienti");
        titolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Label sottotitolo = new Label("Scopri le esperienze reali di chi ha già scelto i nostri gelati e dolci artigianali. Ogni recensione è verificata per garantirti la massima trasparenza.");
        sottotitolo.setWrapText(true);
        sottotitolo.setMaxWidth(640);
        sottotitolo.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #a89f92; -fx-line-spacing: 2;");

        headerTesti.getChildren().addAll(badge, titolo, sottotitolo);

        Button btnScrivi = new Button("✍️  Scrivi una recensione");
        btnScrivi.setMinWidth(Region.USE_PREF_SIZE);
        btnScrivi.setStyle("-fx-background-color: linear-gradient(to bottom, #c9b293, #967c5c); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 12; -fx-padding: 11 20; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(186,164,135,0.28), 12, 0, 0, 3);");
        btnScrivi.setOnAction(e -> mostraRichiestaLogin());

        header.getChildren().addAll(headerTesti, btnScrivi);

        // 2. Card Riepilogo Statistiche
        HBox cardStats = new HBox(28);
        cardStats.setAlignment(Pos.CENTER_LEFT);
        cardStats.setStyle("-fx-background-color: rgba(24, 21, 28, 0.92); -fx-background-radius: 18; "
                + "-fx-border-color: rgba(186, 164, 135, 0.28); -fx-border-radius: 18; -fx-border-width: 1; "
                + "-fx-padding: 22 26; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.40), 14, 0, 0, 4);");

        VBox colSinistra = new VBox(8);
        colSinistra.setAlignment(Pos.CENTER_LEFT);
        colSinistra.setMinWidth(260);
        colSinistra.setPrefWidth(275);
        colSinistra.setStyle("-fx-border-color: transparent rgba(186,164,135,0.18) transparent transparent; "
                + "-fx-border-width: 1; -fx-padding: 0 24 0 0;");

        HBox rigaPunteggio = new HBox(8);
        rigaPunteggio.setAlignment(Pos.BASELINE_LEFT);

        lblMedia = new Label("5.0");
        lblMedia.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 46px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Label lblSuCinque = new Label("/ 5");
        lblSuCinque.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8c837b;");
        rigaPunteggio.getChildren().addAll(lblMedia, lblSuCinque);

        lblStelleMedia = new Label("★★★★★");
        lblStelleMedia.setStyle("-fx-font-size: 20px; -fx-text-fill: #e2b659; -fx-letter-spacing: 2px;");

        lblTotaleRecensioni = new Label("Basato su 0 recensioni verificate");
        lblTotaleRecensioni.setStyle("-fx-font-size: 13px; -fx-text-fill: #ded7cd;");

        lblConsiglia = new Label("📈  Il 100% consiglia Frostly");
        lblConsiglia.setStyle("-fx-background-color: rgba(46, 125, 50, 0.20); -fx-text-fill: #81c784; "
                + "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 5 11; "
                + "-fx-background-radius: 10; -fx-border-color: rgba(129, 199, 132, 0.30); -fx-border-radius: 10;");
        VBox.setMargin(lblConsiglia, new Insets(4, 0, 0, 0));

        colSinistra.getChildren().addAll(rigaPunteggio, lblStelleMedia, lblTotaleRecensioni, lblConsiglia);

        boxBarreDistribuzione = new VBox(6);
        boxBarreDistribuzione.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(boxBarreDistribuzione, Priority.ALWAYS);

        cardStats.getChildren().addAll(colSinistra, boxBarreDistribuzione);

        // 3. Barra Filtri e Ordinamento
        HBox barraFiltri = new HBox(14);
        barraFiltri.setAlignment(Pos.CENTER_LEFT);

        boxPilloleFiltro = new HBox(8);
        boxPilloleFiltro.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(boxPilloleFiltro, Priority.ALWAYS);

        HBox boxOrdinamento = new HBox(8);
        boxOrdinamento.setAlignment(Pos.CENTER_RIGHT);

        Label lblOrdina = new Label("ORDINA PER:");
        lblOrdina.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #a89f92;");

        ComboBox<String> comboOrdina = new ComboBox<>(FXCollections.observableArrayList(
                SORT_RECENTI, SORT_UTILI, SORT_ALTE, SORT_BASSE));
        comboOrdina.setValue(ordinamentoAttivo);
        comboOrdina.getStyleClass().add("luxury-combo");
        comboOrdina.setPrefWidth(195);
        comboOrdina.setOnAction(e -> {
            if (comboOrdina.getValue() != null) {
                ordinamentoAttivo = comboOrdina.getValue();
                renderizzaRecensioni();
            }
        });

        boxOrdinamento.getChildren().addAll(lblOrdina, comboOrdina);
        barraFiltri.getChildren().addAll(boxPilloleFiltro, boxOrdinamento);

        // 4. Banner filtro attivo (nascosto di default)
        bannerFiltroAttivo = new HBox(12);
        bannerFiltroAttivo.setAlignment(Pos.CENTER_LEFT);
        bannerFiltroAttivo.setStyle("-fx-background-color: rgba(186, 164, 135, 0.12); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(186, 164, 135, 0.30); -fx-border-radius: 12; -fx-padding: 9 16;");
        bannerFiltroAttivo.setVisible(false);
        bannerFiltroAttivo.setManaged(false);

        lblTestoFiltroAttivo = new Label();
        lblTestoFiltroAttivo.setStyle("-fx-font-size: 13px; -fx-text-fill: #f5ebe0;");
        HBox.setHgrow(lblTestoFiltroAttivo, Priority.ALWAYS);

        Button btnResetBanner = new Button("Mostra tutte");
        btnResetBanner.setStyle("-fx-background-color: transparent; -fx-text-fill: #baa487; -fx-font-weight: bold; "
                + "-fx-font-size: 12.5px; -fx-underline: true; -fx-cursor: hand; -fx-padding: 2 6;");
        btnResetBanner.setOnAction(e -> resetFiltri());

        bannerFiltroAttivo.getChildren().addAll(lblTestoFiltroAttivo, btnResetBanner);

        // 5. Griglia 2 colonne delle recensioni
        grigliaRecensioni = new GridPane();
        grigliaRecensioni.setHgap(18);
        grigliaRecensioni.setVgap(18);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);
        grigliaRecensioni.getColumnConstraints().addAll(c1, c2);

        // 6. Stato vuoto
        boxStatoVuoto = new VBox(10);
        boxStatoVuoto.setAlignment(Pos.CENTER);
        boxStatoVuoto.setStyle("-fx-background-color: rgba(24, 21, 28, 0.85); -fx-background-radius: 18; "
                + "-fx-border-color: rgba(186, 164, 135, 0.22); -fx-border-radius: 18; -fx-padding: 40 24;");
        boxStatoVuoto.setVisible(false);
        boxStatoVuoto.setManaged(false);

        Label lblIconaVuoto = new Label("🔍");
        lblIconaVuoto.setStyle("-fx-font-size: 28px; -fx-background-color: rgba(186,164,135,0.12); "
                + "-fx-padding: 10 14; -fx-background-radius: 16;");
        Label lblTitVuoto = new Label("Nessuna recensione trovata");
        lblTitVuoto.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        Label lblSubVuoto = new Label("Non ci sono ancora recensioni che corrispondono al filtro selezionato.");
        lblSubVuoto.setStyle("-fx-font-size: 13px; -fx-text-fill: #a89f92;");
        Button btnReimposta = new Button("Reimposta filtri");
        btnReimposta.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;");
        btnReimposta.setOnAction(e -> resetFiltri());
        VBox.setMargin(btnReimposta, new Insets(6, 0, 0, 0));

        boxStatoVuoto.getChildren().addAll(lblIconaVuoto, lblTitVuoto, lblSubVuoto, btnReimposta);

        // 7. Footer discreto
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-border-color: rgba(186,164,135,0.16) transparent transparent transparent; "
                + "-fx-border-width: 1; -fx-padding: 14 4 0 4;");
        Label lblFootLeft = new Label("Sistema di Recensioni Certificate Frostly • Trasparenza e Qualità dal 1984");
        lblFootLeft.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8c837b;");
        Region spFoot = new Region();
        HBox.setHgrow(spFoot, Priority.ALWAYS);
        Label lblFootRight = new Label("Accedi o registrati per lasciare il tuo feedback o votare le recensioni utili.");
        lblFootRight.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8c837b; -fx-font-style: italic;");
        footer.getChildren().addAll(lblFootLeft, spFoot, lblFootRight);

        container.getChildren().addAll(header, cardStats, barraFiltri, bannerFiltroAttivo, grigliaRecensioni, boxStatoVuoto, footer);
    }

    private void aggiornaRiepilogo() {
        int totale = tutteRecensioni.size();
        double media = recensioniControl.calcolaMediaVoti(tutteRecensioni);
        lblMedia.setText(String.format("%.1f", totale > 0 ? media : 0.0));
        lblStelleMedia.setText(generaStelle((int) Math.round(totale > 0 ? media : 0.0)));
        lblTotaleRecensioni.setText("Basato su " + totale + (totale == 1 ? " recensione verificata" : " recensioni verificate"));

        int positive = 0;
        int[] conteggi = new int[6];
        for (RecensioneBean r : tutteRecensioni) {
            int voto = Math.max(1, Math.min(5, r.getVotoStelle()));
            conteggi[voto]++;
            if (voto >= 4) {
                positive++;
            }
        }
        int percConsiglia = totale > 0 ? (int) Math.round((positive * 100.0) / totale) : 100;
        lblConsiglia.setText("📈  Il " + percConsiglia + "% consiglia Frostly");

        boxBarreDistribuzione.getChildren().clear();
        for (int stella = 5; stella >= 1; stella--) {
            final String filtroStella = String.valueOf(stella);
            int count = conteggi[stella];
            int perc = totale > 0 ? (int) Math.round((count * 100.0) / totale) : 0;
            boolean attiva = filtroStella.equals(filtroAttivo);

            HBox riga = new HBox(12);
            riga.setAlignment(Pos.CENTER_LEFT);
            riga.setStyle(attiva
                    ? "-fx-background-color: rgba(186, 164, 135, 0.16); -fx-background-radius: 10; "
                      + "-fx-border-color: rgba(186, 164, 135, 0.45); -fx-border-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;"
                    : "-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;");
            riga.setOnMouseEntered(e -> {
                if (!filtroStella.equals(filtroAttivo)) {
                    riga.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04); -fx-background-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;");
                }
            });
            riga.setOnMouseExited(e -> {
                if (!filtroStella.equals(filtroAttivo)) {
                    riga.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;");
                }
            });
            riga.setOnMouseClicked(e -> toggleFiltro(filtroStella));

            Label lblStella = new Label(stella + " ★");
            lblStella.setMinWidth(38);
            lblStella.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e2b659;");

            StackPane track = new StackPane();
            track.setAlignment(Pos.CENTER_LEFT);
            track.setPrefHeight(10);
            track.setMaxHeight(10);
            track.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 6;");
            HBox.setHgrow(track, Priority.ALWAYS);

            Region fill = new Region();
            fill.setPrefHeight(10);
            fill.setMaxHeight(10);
            fill.setStyle("-fx-background-color: linear-gradient(to right, #baa487, #e2b659); -fx-background-radius: 6;");
            fill.maxWidthProperty().bind(track.widthProperty().multiply(perc / 100.0));
            fill.prefWidthProperty().bind(track.widthProperty().multiply(perc / 100.0));

            track.getChildren().add(fill);

            Label lblPerc = new Label(perc + "%");
            lblPerc.setMinWidth(40);
            lblPerc.setAlignment(Pos.CENTER_RIGHT);
            lblPerc.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #a89f92;");

            riga.getChildren().addAll(lblStella, track, lblPerc);
            boxBarreDistribuzione.getChildren().add(riga);
        }
    }

    private void aggiornaPilloleFiltro() {
        boxPilloleFiltro.getChildren().clear();

        int conRisposta = 0;
        for (RecensioneBean r : tutteRecensioni) {
            if (r.haRispostaMaestro()) {
                conRisposta++;
            }
        }

        boxPilloleFiltro.getChildren().addAll(
                creaPillola("Tutte (" + tutteRecensioni.size() + ")", FILTER_ALL),
                creaPillola("5 ★", "5"),
                creaPillola("4 ★", "4"),
                creaPillola("3 ★", "3"),
                creaPillola("2 ★", "2"),
                creaPillola("1 ★", "1"),
                creaPillola("👩‍🍳 Risposta Maestro (" + conRisposta + ")", FILTER_MAESTRO)
        );
    }

    private Button creaPillola(String testo, String valoreFiltro) {
        boolean attiva = valoreFiltro.equals(filtroAttivo);
        Button btn = new Button(testo);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        if (attiva) {
            btn.setStyle("-fx-background-color: linear-gradient(to bottom, #c9b293, #967c5c); -fx-text-fill: #141216; "
                    + "-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-background-radius: 20; -fx-padding: 6 14; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: rgba(28, 25, 33, 0.90); -fx-text-fill: #ded7cd; "
                    + "-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-background-radius: 20; "
                    + "-fx-border-color: rgba(186, 164, 135, 0.28); -fx-border-radius: 20; -fx-padding: 6 14; -fx-cursor: hand;");
        }
        btn.setOnAction(e -> toggleFiltro(valoreFiltro));
        return btn;
    }

    private void toggleFiltro(String valore) {
        if (valore.equals(filtroAttivo) && !FILTER_ALL.equals(valore)) {
            filtroAttivo = FILTER_ALL;
        } else {
            filtroAttivo = valore;
        }
        aggiornaRiepilogo();
        aggiornaPilloleFiltro();
        renderizzaRecensioni();
    }

    private void resetFiltri() {
        filtroAttivo = FILTER_ALL;
        aggiornaRiepilogo();
        aggiornaPilloleFiltro();
        renderizzaRecensioni();
    }

    private void renderizzaRecensioni() {
        List<RecensioneBean> filtrate = new ArrayList<>();
        for (RecensioneBean r : tutteRecensioni) {
            if (FILTER_ALL.equals(filtroAttivo)) {
                filtrate.add(r);
            } else if (FILTER_MAESTRO.equals(filtroAttivo)) {
                if (r.haRispostaMaestro()) {
                    filtrate.add(r);
                }
            } else {
                try {
                    int target = Integer.parseInt(filtroAttivo);
                    if (r.getVotoStelle() == target) {
                        filtrate.add(r);
                    }
                } catch (NumberFormatException ignored) {
                    filtrate.add(r);
                }
            }
        }

        Comparator<RecensioneBean> cmp = switch (ordinamentoAttivo) {
            case SORT_UTILI -> Comparator.comparingInt(RecensioneBean::getMiPiace).reversed();
            case SORT_ALTE -> Comparator.comparingInt(RecensioneBean::getVotoStelle).reversed()
                    .thenComparing(this::estraiDataOra, Comparator.reverseOrder());
            case SORT_BASSE -> Comparator.comparingInt(RecensioneBean::getVotoStelle)
                    .thenComparing(this::estraiDataOra, Comparator.reverseOrder());
            default -> Comparator.comparing(this::estraiDataOra, Comparator.reverseOrder());
        };
        filtrate.sort(cmp);

        if (!FILTER_ALL.equals(filtroAttivo)) {
            String desc = FILTER_MAESTRO.equals(filtroAttivo)
                    ? "con risposta della Maestra Gelataia"
                    : "con " + filtroAttivo + ("1".equals(filtroAttivo) ? " stella" : " stelle");
            lblTestoFiltroAttivo.setText("Stai visualizzando " + filtrate.size()
                    + (filtrate.size() == 1 ? " recensione " : " recensioni ") + desc + ".");
            bannerFiltroAttivo.setVisible(true);
            bannerFiltroAttivo.setManaged(true);
        } else {
            bannerFiltroAttivo.setVisible(false);
            bannerFiltroAttivo.setManaged(false);
        }

        grigliaRecensioni.getChildren().clear();

        if (filtrate.isEmpty()) {
            grigliaRecensioni.setVisible(false);
            grigliaRecensioni.setManaged(false);
            boxStatoVuoto.setVisible(true);
            boxStatoVuoto.setManaged(true);
            return;
        }

        grigliaRecensioni.setVisible(true);
        grigliaRecensioni.setManaged(true);
        boxStatoVuoto.setVisible(false);
        boxStatoVuoto.setManaged(false);

        for (int i = 0; i < filtrate.size(); i++) {
            VBox card = creaCardRecensione(filtrate.get(i), i);
            int col = i % 2;
            int row = i / 2;
            grigliaRecensioni.add(card, col, row);
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setVgrow(card, Priority.ALWAYS);
        }
    }

    private VBox creaCardRecensione(RecensioneBean r, int index) {
        VBox card = new VBox(12);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        String baseStyle = "-fx-background-color: rgba(26, 23, 30, 0.92); -fx-background-radius: 18; "
                + "-fx-border-color: rgba(186, 164, 135, 0.26); -fx-border-radius: 18; -fx-border-width: 1; "
                + "-fx-padding: 18 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);";
        String hoverStyle = "-fx-background-color: rgba(32, 28, 37, 0.96); -fx-background-radius: 18; "
                + "-fx-border-color: rgba(186, 164, 135, 0.52); -fx-border-radius: 18; -fx-border-width: 1; "
                + "-fx-padding: 18 20; -fx-effect: dropshadow(gaussian, rgba(186,164,135,0.18), 14, 0, 0, 4);";
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        // Intestazione autore con avatar a iniziali
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        String nome = r.getAutoreNome() != null && !r.getAutoreNome().isBlank() ? r.getAutoreNome().trim() : "Cliente Frostly";
        String[] palette = AVATAR_PALETTE[Math.abs(nome.hashCode()) % AVATAR_PALETTE.length];

        StackPane avatar = new StackPane();
        avatar.setMinSize(40, 40);
        avatar.setPrefSize(40, 40);
        avatar.setMaxSize(40, 40);
        avatar.setStyle("-fx-background-color: " + palette[0] + "; -fx-background-radius: 20; "
                + "-fx-border-color: " + palette[2] + "; -fx-border-radius: 20; -fx-border-width: 1;");
        Label lblInit = new Label(calcolaIniziali(nome));
        lblInit.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: " + palette[1] + ";");
        avatar.getChildren().add(lblInit);

        VBox infoAutore = new VBox(3);
        HBox.setHgrow(infoAutore, Priority.ALWAYS);

        HBox rigaNome = new HBox(8);
        rigaNome.setAlignment(Pos.CENTER_LEFT);

        Label lblNome = new Label(nome);
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5ebe0;");

        Label badgeVerificato = new Label("✓ Cliente verificato");
        badgeVerificato.setStyle("-fx-background-color: rgba(46, 125, 50, 0.20); -fx-text-fill: #81c784; "
                + "-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;");

        rigaNome.getChildren().addAll(lblNome, badgeVerificato);

        Label lblData = new Label(r.getDataOra() != null ? r.getDataOra() : "");
        lblData.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8c837b;");

        infoAutore.getChildren().addAll(rigaNome, lblData);
        header.getChildren().addAll(avatar, infoAutore);

        // Stelle e badge codice
        HBox rigaStelle = new HBox(8);
        rigaStelle.setAlignment(Pos.CENTER_LEFT);

        Label lblStelle = new Label(generaStelle(r.getVotoStelle()));
        lblStelle.setStyle("-fx-font-size: 15px; -fx-text-fill: #e2b659; -fx-letter-spacing: 1.5px;");

        Region spStelle = new Region();
        HBox.setHgrow(spStelle, Priority.ALWAYS);

        rigaStelle.getChildren().addAll(lblStelle, spStelle);
        if (r.getCodiceRecensione() != null && !r.getCodiceRecensione().isBlank()) {
            Label lblCod = new Label(r.getCodiceRecensione());
            lblCod.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #a89f92; -fx-background-color: rgba(255,255,255,0.04); "
                    + "-fx-padding: 2 8; -fx-background-radius: 8; -fx-border-color: rgba(186,164,135,0.16); -fx-border-radius: 8;");
            rigaStelle.getChildren().add(lblCod);
        }

        // Titolo e commento
        Label lblTitolo = new Label(r.getTitolo() != null ? r.getTitolo() : "");
        lblTitolo.setWrapText(true);
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #f5ebe0;");

        Label lblCommento = new Label(r.getCommento() != null ? r.getCommento() : "");
        lblCommento.setWrapText(true);
        lblCommento.setStyle("-fx-font-size: 13px; -fx-text-fill: #ded7cd; -fx-line-spacing: 2.5;");

        VBox corpo = new VBox(8, header, rigaStelle, lblTitolo, lblCommento);
        VBox.setVgrow(corpo, Priority.ALWAYS);

        if (r.haFoto()) {
            javafx.scene.image.Image img = RecensioniView.caricaImmagineRecensione(r.getFotoPath());
            if (img != null) {
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitHeight(130);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                StackPane thumb = new StackPane(iv);
                thumb.setMaxWidth(Region.USE_PREF_SIZE);
                thumb.setStyle("-fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.32); "
                        + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 3; -fx-cursor: hand;");
                thumb.setOnMouseClicked(e -> RecensioniView.apriLightboxFoto(
                        container.getScene() != null ? container.getScene().getWindow() : null,
                        r.getFotoPath(), r.getTitolo()));
                corpo.getChildren().add(thumb);
            }
        }

        if (r.haRispostaMaestro()) {
            VBox boxRisposta = new VBox(4);
            boxRisposta.setStyle("-fx-background-color: rgba(40, 34, 46, 0.85); -fx-border-color: #baa487; "
                    + "-fx-border-width: 1 1 1 3; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 12;");

            HBox rigaMaestro = new HBox(6);
            rigaMaestro.setAlignment(Pos.CENTER_LEFT);
            Label lblBadgeMaestro = new Label("👩‍🍳 Risposta Maestra Gelataia");
            lblBadgeMaestro.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #baa487;");
            Region spM = new Region();
            HBox.setHgrow(spM, Priority.ALWAYS);
            Label lblDataRisp = new Label(r.getDataRisposta() != null ? r.getDataRisposta() : "");
            lblDataRisp.setStyle("-fx-font-size: 11px; -fx-text-fill: #8c837b; -fx-font-style: italic;");
            rigaMaestro.getChildren().addAll(lblBadgeMaestro, spM, lblDataRisp);

            Label lblTestoRisp = new Label(r.getRispostaMaestro());
            lblTestoRisp.setWrapText(true);
            lblTestoRisp.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #f5ebe0;");

            boxRisposta.getChildren().addAll(rigaMaestro, lblTestoRisp);
            corpo.getChildren().add(boxRisposta);
        }

        // Footer card: pulsante Utile (richiede login fuori accesso)
        HBox footerCard = new HBox(10);
        footerCard.setAlignment(Pos.CENTER_LEFT);
        footerCard.setStyle("-fx-border-color: rgba(186,164,135,0.14) transparent transparent transparent; "
                + "-fx-border-width: 1; -fx-padding: 10 0 0 0;");

        Label lblDomanda = new Label("Questa recensione ti è stata utile?");
        lblDomanda.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8c837b;");
        Region spFoot = new Region();
        HBox.setHgrow(spFoot, Priority.ALWAYS);

        Button btnUtile = new Button("👍  Utile (" + r.getMiPiace() + ")");
        btnUtile.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: #ded7cd; "
                + "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; "
                + "-fx-border-color: rgba(186, 164, 135, 0.25); -fx-border-radius: 10; -fx-padding: 5 12; -fx-cursor: hand;");
        btnUtile.setOnAction(e -> mostraRichiestaLogin());

        footerCard.getChildren().addAll(lblDomanda, spFoot, btnUtile);
        card.getChildren().addAll(corpo, footerCard);

        animaIngressoCard(card, Math.min(index, 8));
        return card;
    }

    private void animaIngressoCard(VBox card, int delaySlot) {
        card.setOpacity(0);
        card.setTranslateY(10);

        FadeTransition fade = new FadeTransition(Duration.millis(260), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slide = new TranslateTransition(Duration.millis(260), card);
        slide.setFromY(10);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition anim = new ParallelTransition(fade, slide);
        anim.setDelay(Duration.millis(delaySlot * 35L));
        anim.play();
    }

    private void mostraRichiestaLogin() {
        Window owner = container.getScene() != null ? container.getScene().getWindow() : null;
        RecensioniView.mostraAvvisoLoginNecessario(owner, onRichiediLogin);
    }

    private LocalDateTime estraiDataOra(RecensioneBean r) {
        if (r == null || r.getDataOra() == null || r.getDataOra().isBlank()) {
            return LocalDateTime.MIN;
        }
        try {
            return LocalDateTime.parse(r.getDataOra().trim(), DATA_FMT);
        } catch (Exception e) {
            return LocalDateTime.MIN;
        }
    }

    private static String generaStelle(int voto) {
        int clamped = Math.max(0, Math.min(5, voto));
        return "★".repeat(clamped) + "☆".repeat(5 - clamped);
    }

    private static String calcolaIniziali(String nome) {
        String[] parti = nome.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parti.length); i++) {
            if (!parti[i].isEmpty()) {
                sb.append(Character.toUpperCase(parti[i].charAt(0)));
            }
        }
        return sb.length() > 0 ? sb.toString() : "CF";
    }
}
