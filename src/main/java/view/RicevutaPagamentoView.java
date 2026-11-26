package view;

import bean.EsitoPagamentoBean;
import java.util.Locale;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Componente View dedicato esclusivamente alla schermata dinamica e animata
 * di conferma/rifiuto pagamento con QR Code dello scontrino fiscale digitale.
 */
public final class RicevutaPagamentoView {

    private RicevutaPagamentoView() {
    }

    public static void mostraRicevuta(Window owner,
                                      EsitoPagamentoBean esito,
                                      String codiceOrdine,
                                      String urlScontrino) {
        boolean haQr = esito != null && esito.isSuccesso() && urlScontrino != null && !urlScontrino.isBlank();
        double larghezza = haQr ? 680 : 540;
        double altezza = haQr ? 475 : 440;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Frostly — Conferma Pagamento");
        if (owner != null) {
            dialog.initOwner(owner);
        }

        StackPane contenitore = new StackPane();
        contenitore.setStyle("-fx-background-color: rgba(18, 17, 20, 0.99); "
                + "-fx-border-color: rgba(186,164,135,0.45); -fx-border-width: 1.5;");
        contenitore.setPrefSize(larghezza, altezza);

        VBox vistaElaborazione = creaVistaElaborazioneLive();
        VBox vistaEsito = creaVistaEsitoAnimata(dialog, esito, codiceOrdine, urlScontrino);
        vistaEsito.setOpacity(0);
        vistaEsito.setVisible(false);

        contenitore.getChildren().addAll(vistaEsito, vistaElaborazione);

        Scene scene = new Scene(contenitore, larghezza, altezza);
        scene.setFill(Color.web("#121114"));
        dialog.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(dialog.getScene());
        dialog.centerOnScreen();

        Label lblStatoLive = (Label) vistaElaborazione.getUserData();
        PauseTransition step1 = new PauseTransition(Duration.millis(550));
        step1.setOnFinished(ev -> {
            if (lblStatoLive != null) {
                lblStatoLive.setText(esito != null && esito.isSuccesso()
                        ? "Autorizzazione confermata • Invio automatico in gelateria…"
                        : "Verifica risposta circuito bancario…");
            }
        });

        PauseTransition step2 = new PauseTransition(Duration.millis(550));
        FadeTransition fadeOutLoad = new FadeTransition(Duration.millis(220), vistaElaborazione);
        fadeOutLoad.setFromValue(1.0);
        fadeOutLoad.setToValue(0.0);
        fadeOutLoad.setOnFinished(ev -> {
            vistaElaborazione.setVisible(false);
            vistaEsito.setVisible(true);
            avviaAnimazioneEsito(vistaEsito);
        });

        SequentialTransition seq = new SequentialTransition(step1, step2, fadeOutLoad);
        dialog.setOnShown(ev -> seq.play());
        dialog.showAndWait();
    }

    private static VBox creaVistaElaborazioneLive() {
        VBox box = new VBox(18);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 32;");

        StackPane spinnerPane = new StackPane();
        spinnerPane.setPrefSize(96, 96);

        Circle anelloBase = new Circle(40);
        anelloBase.setFill(Color.rgb(28, 25, 33, 0.9));
        anelloBase.setStroke(Color.rgb(186, 164, 135, 0.22));
        anelloBase.setStrokeWidth(4.0);

        Arc arcoRotante = new Arc(0, 0, 40, 40, 0, 110);
        arcoRotante.setType(ArcType.OPEN);
        arcoRotante.setFill(Color.TRANSPARENT);
        arcoRotante.setStroke(Color.web("#baa487"));
        arcoRotante.setStrokeWidth(4.5);
        arcoRotante.setStrokeLineCap(StrokeLineCap.ROUND);

        RotateTransition rot = new RotateTransition(Duration.millis(900), arcoRotante);
        rot.setByAngle(360);
        rot.setCycleCount(Animation.INDEFINITE);
        rot.setInterpolator(Interpolator.LINEAR);
        rot.play();

        Label lblCard = new Label("💳");
        lblCard.setStyle("-fx-font-size: 28px;");

        spinnerPane.getChildren().addAll(anelloBase, arcoRotante, lblCard);

        Label lblTit = new Label("Elaborazione Pagamento in corso");
        lblTit.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #f5ebe0;");

        Label lblStato = new Label("Connessione sicura al circuito di pagamento…");
        lblStato.setStyle("-fx-font-size: 13px; -fx-text-fill: #baa487; -fx-font-weight: bold;");

        box.setUserData(lblStato);
        box.getChildren().addAll(spinnerPane, lblTit, lblStato);
        return box;
    }

    private static VBox creaVistaEsitoAnimata(Stage dialog, EsitoPagamentoBean esito, String codiceOrdine, String urlScontrino) {
        boolean ok = esito != null && esito.isSuccesso();
        boolean haQr = ok && urlScontrino != null && !urlScontrino.isBlank();

        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 26 32;");

        StackPane badgePane = new StackPane();
        badgePane.setPrefSize(96, 96);

        Circle anelloPulse = new Circle(40);
        anelloPulse.setFill(Color.TRANSPARENT);
        anelloPulse.setStroke(ok ? Color.web("#10b981") : Color.web("#ef4444"));
        anelloPulse.setStrokeWidth(3.0);
        anelloPulse.setOpacity(0.0);

        Circle cerchio = new Circle(38);
        cerchio.setFill(ok ? Color.rgb(22, 65, 45, 0.95) : Color.rgb(75, 22, 26, 0.95));
        cerchio.setStroke(ok ? Color.web("#10b981") : Color.web("#ef4444"));
        cerchio.setStrokeWidth(3.0);

        Label lblSimbolo = new Label(ok ? "✓" : "✕");
        lblSimbolo.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: " + (ok ? "#a7f3d0;" : "#fca5a5;"));

        StackPane nucleo = new StackPane(cerchio, lblSimbolo);
        nucleo.setScaleX(0.2);
        nucleo.setScaleY(0.2);

        badgePane.getChildren().addAll(anelloPulse, nucleo);

        Label lblTitolo = new Label(ok ? "PAGAMENTO AVVENUTO CON SUCCESSO" : "PAGAMENTO RIFIUTATO");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 17.5px; "
                + "-fx-text-fill: " + (ok ? "#baa487;" : "#ff8a80;"));

        Label lblBadgeInvio = new Label(ok
                ? ("✓ Ordine " + (codiceOrdine != null && !codiceOrdine.isBlank() ? codiceOrdine + " " : "")
                   + "inviato automaticamente alla gelateria")
                : (esito != null ? esito.getMessaggio() : "Transazione non autorizzata"));
        lblBadgeInvio.setWrapText(true);
        lblBadgeInvio.setAlignment(Pos.CENTER);
        lblBadgeInvio.setStyle(ok
                ? "-fx-background-color: rgba(22, 55, 38, 0.90); -fx-text-fill: #a7f3d0; -fx-border-color: #10b981; "
                  + "-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 20; -fx-border-radius: 20;"
                : "-fx-font-size: 13px; -fx-text-fill: #ded7cd;");

        VBox cardRicevuta = new VBox(7);
        cardRicevuta.setStyle("-fx-background-color: rgba(28,25,33,0.92); -fx-border-color: rgba(186,164,135,0.28); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14 18;");
        HBox.setHgrow(cardRicevuta, Priority.ALWAYS);

        if (ok) {
            if (codiceOrdine != null && !codiceOrdine.isBlank()) {
                cardRicevuta.getChildren().add(creaRigaDettaglio("Codice Ordine:", codiceOrdine));
            }
            cardRicevuta.getChildren().addAll(
                    creaRigaDettaglio("Codice Transazione:", esito.getCodiceTransazione()),
                    creaRigaDettaglio("Importo Pagato:", String.format(Locale.US, "EUR %.2f", esito.getImporto())),
                    creaRigaDettaglio("Data e Ora:", esito.getDataOra()),
                    creaRigaDettaglio("Metodo:", esito.getCircuito() + " (" + esito.getCartaMascherata() + ")")
            );
        }

        Node areaContenuto = cardRicevuta;
        if (haQr) {
            VBox boxQr = QrCodeView.creaBoxQrScontrino(urlScontrino);
            HBox rigaRicevutaQr = new HBox(16, cardRicevuta, boxQr);
            rigaRicevutaQr.setAlignment(Pos.CENTER);
            areaContenuto = rigaRicevutaQr;
        }
        areaContenuto.setTranslateY(14);
        areaContenuto.setOpacity(0);

        Button btnChiudi = new Button(ok ? "✓ Perfetto, Torna al Menu" : "Chiudi e Riprova");
        btnChiudi.setStyle(ok
                ? "-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 10; -fx-padding: 10 26; -fx-cursor: hand;"
                : "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #f5ebe0; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 10; -fx-padding: 9 20; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> dialog.close());

        root.getChildren().addAll(badgePane, lblTitolo, lblBadgeInvio);
        if (ok) {
            root.getChildren().add(areaContenuto);
        }
        root.getChildren().add(btnChiudi);

        root.setUserData(new Object[]{nucleo, anelloPulse, areaContenuto});
        return root;
    }

    private static void avviaAnimazioneEsito(VBox vistaEsito) {
        FadeTransition fadeRoot = new FadeTransition(Duration.millis(250), vistaEsito);
        fadeRoot.setFromValue(0);
        fadeRoot.setToValue(1);
        fadeRoot.play();

        Object[] nodi = (Object[]) vistaEsito.getUserData();
        if (nodi == null) return;

        Node nucleo = (Node) nodi[0];
        Node anelloPulse = (Node) nodi[1];
        Node areaContenuto = (Node) nodi[2];

        ScaleTransition popUp = new ScaleTransition(Duration.millis(320), nucleo);
        popUp.setFromX(0.2);
        popUp.setFromY(0.2);
        popUp.setToX(1.12);
        popUp.setToY(1.12);
        popUp.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition settle = new ScaleTransition(Duration.millis(160), nucleo);
        settle.setToX(1.0);
        settle.setToY(1.0);
        settle.setInterpolator(Interpolator.EASE_BOTH);
        new SequentialTransition(popUp, settle).play();

        ScaleTransition pulseScale = new ScaleTransition(Duration.millis(750), anelloPulse);
        pulseScale.setFromX(0.95);
        pulseScale.setFromY(0.95);
        pulseScale.setToX(1.48);
        pulseScale.setToY(1.48);

        FadeTransition pulseFade = new FadeTransition(Duration.millis(750), anelloPulse);
        pulseFade.setFromValue(0.85);
        pulseFade.setToValue(0.0);
        new ParallelTransition(pulseScale, pulseFade).play();

        FadeTransition cardFade = new FadeTransition(Duration.millis(380), areaContenuto);
        cardFade.setDelay(Duration.millis(180));
        cardFade.setToValue(1.0);

        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(380), areaContenuto);
        cardSlide.setDelay(Duration.millis(180));
        cardSlide.setToY(0);
        cardSlide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(cardFade, cardSlide).play();
    }

    private static HBox creaRigaDettaglio(String label, String valore) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #baa487; -fx-font-weight: bold;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label v = new Label(valore != null ? valore : "");
        v.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #f5ebe0;");
        r.getChildren().addAll(l, sp, v);
        return r;
    }
}
