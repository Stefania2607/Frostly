package view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

/**
 * Menu a tendina animato con contatti, indirizzo e orari di apertura della gelateria Frostly.
 */
public final class InfoBoutiquePopoverView {

    private static Popup popupAttivo;

    private InfoBoutiquePopoverView() {
    }

    public static void toggle(Node ancora) {
        if (ancora == null || ancora.getScene() == null || ancora.getScene().getWindow() == null) {
            return;
        }
        if (popupAttivo != null && popupAttivo.isShowing()) {
            chiudiConAnimazione(popupAttivo);
            return;
        }

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        VBox card = creaContenutoCard(popup);
        TemaProvenzaHelper.applicaSeGui2(card);
        card.setOpacity(0);
        card.setTranslateY(-14);
        card.setScaleX(0.97);
        card.setScaleY(0.97);

        popup.getContent().add(card);
        Bounds bounds = ancora.localToScreen(ancora.getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        popupAttivo = popup;
        popup.show(ancora.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY() + 8);

        FadeTransition fade = new FadeTransition(Duration.millis(230), card);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slide = new TranslateTransition(Duration.millis(250), card);
        slide.setFromY(-14);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scale = new ScaleTransition(Duration.millis(230), card);
        scale.setFromX(0.97);
        scale.setFromY(0.97);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide, scale).play();
    }

    private static void chiudiConAnimazione(Popup popup) {
        if (popup == null || !popup.isShowing() || popup.getContent().isEmpty()) {
            return;
        }
        Node card = popup.getContent().get(0);
        FadeTransition fade = new FadeTransition(Duration.millis(150), card);
        fade.setToValue(0.0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(150), card);
        slide.setToY(-10);
        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.setOnFinished(e -> {
            popup.hide();
            if (popupAttivo == popup) {
                popupAttivo = null;
            }
        });
        pt.play();
    }

    private static VBox creaContenutoCard(Popup popup) {
        VBox card = new VBox(12);
        card.setPrefWidth(365);
        card.setStyle("-fx-background-color: rgba(20, 19, 24, 0.97); "
                + "-fx-border-color: rgba(212, 180, 131, 0.42); -fx-border-width: 1.2; "
                + "-fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 18 20; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 24, 0, 0, 10);");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titoli = new VBox(2);
        Label lblBrand = new Label("FROSTLY — Boutique Roma");
        lblBrand.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        Label lblSub = new Label("Contatti & Orari di Apertura");
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #baa487;");
        titoli.getChildren().addAll(lblBrand, lblSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #ded7cd; "
                + "-fx-background-radius: 14; -fx-min-width: 26; -fx-min-height: 26; -fx-max-width: 26; -fx-max-height: 26; "
                + "-fx-padding: 0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnClose.setOnAction(e -> chiudiConAnimazione(popup));
        header.getChildren().addAll(titoli, sp, btnClose);

        VBox boxContatti = new VBox(8);
        boxContatti.setStyle("-fx-background-color: rgba(32, 30, 38, 0.85); -fx-border-color: rgba(255,255,255,0.07); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 11 13;");

        Label lblTel = new Label("📞  06 8122464");
        lblTel.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        Region spTel = new Region();
        HBox.setHgrow(spTel, Priority.ALWAYS);
        Button btnCopia = new Button("Copia");
        btnCopia.setStyle("-fx-background-color: rgba(212,180,131,0.18); -fx-text-fill: #e5c99b; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 9; -fx-background-radius: 8; -fx-cursor: hand;");
        btnCopia.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString("068122464");
            Clipboard.getSystemClipboard().setContent(cc);
            btnCopia.setText("✓ Copiato");
        });
        HBox rigaTel = new HBox(8, lblTel, spTel, btnCopia);
        rigaTel.setAlignment(Pos.CENTER_LEFT);

        Label lblInd = new Label("📍  P.zza Antonio Fradeletto, 8/9, 10\n      00139 Roma, Italia");
        lblInd.setWrapText(true);
        lblInd.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd; -fx-line-spacing: 2px;");

        boxContatti.getChildren().addAll(rigaTel, lblInd);

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.25;");

        Label lblOrariTit = new Label("🕒  Orari di Apertura");
        lblOrariTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #e5c99b;");

        DayOfWeek oggi = LocalDate.now().getDayOfWeek();
        VBox boxOrari = new VBox(5,
                creaRigaOrario("Lunedì", "06:30 – 22:00", oggi == DayOfWeek.MONDAY),
                creaRigaOrario("Martedì", "06:30 – 22:00", oggi == DayOfWeek.TUESDAY),
                creaRigaOrario("Mercoledì", "06:30 – 22:00", oggi == DayOfWeek.WEDNESDAY),
                creaRigaOrario("Giovedì", "06:30 – 22:00", oggi == DayOfWeek.THURSDAY),
                creaRigaOrario("Venerdì", "06:30 – 00:00", oggi == DayOfWeek.FRIDAY),
                creaRigaOrario("Sabato", "06:30 – 00:00", oggi == DayOfWeek.SATURDAY),
                creaRigaOrario("Domenica", "06:30 – 22:00", oggi == DayOfWeek.SUNDAY)
        );
        boxOrari.setStyle("-fx-background-color: rgba(28, 26, 33, 0.80); -fx-border-color: rgba(255,255,255,0.06); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 13;");

        card.getChildren().addAll(header, boxContatti, sep, lblOrariTit, boxOrari);
        return card;
    }

    private static HBox creaRigaOrario(String giorno, String orario, boolean isOggi) {
        Label lblGiorno = new Label(isOggi ? ("● " + giorno + " (Oggi)") : giorno);
        lblGiorno.setStyle("-fx-font-size: 12.5px; -fx-font-weight: " + (isOggi ? "bold" : "normal")
                + "; -fx-text-fill: " + (isOggi ? "#e5c99b" : "#ded7cd") + ";");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblOra = new Label(orario);
        lblOra.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: "
                + (isOggi ? "#f5ebe0" : "#baa487") + ";");

        HBox riga = new HBox(8, lblGiorno, sp, lblOra);
        riga.setAlignment(Pos.CENTER_LEFT);
        if (isOggi) {
            riga.setStyle("-fx-background-color: rgba(212, 180, 131, 0.14); -fx-background-radius: 7; -fx-padding: 4 8;");
        } else {
            riga.setStyle("-fx-padding: 2 8;");
        }
        return riga;
    }
}
