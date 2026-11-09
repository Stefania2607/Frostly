package view;

import bean.CarrelloBean;
import bean.VaschettaItemBean;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Classe View dedicata esclusivamente alla rappresentazione grafica del Carrello
 * (pulsante "🛒 Vedi Carrello", lista degli articoli nel carrello e finestra modale di Checkout).
 */
public final class CarrelloView {

    private CarrelloView() {
        // Classe di utilità View
    }

    public static void aggiornaBottoneCarrello(Button btnCarrello, CarrelloBean carrello) {
        if (btnCarrello == null || carrello == null) return;
        int n = carrello.getNumeroArticoli();
        double tot = carrello.calcolaTotale();
        btnCarrello.setMinWidth(Region.USE_PREF_SIZE);
        String labelProdotti = n == 1 ? "1 prodotto" : (n + " prodotti");
        btnCarrello.setText(String.format(Locale.US, "🛒 Vedi Carrello (%s • EUR %.2f)", labelProdotti, tot));
        if (n > 0) {
            btnCarrello.setStyle("-fx-background-color: #2a9d8f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 18; -fx-padding: 7 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(42,157,143,0.45), 10, 0, 0, 2);");
        } else {
            btnCarrello.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-background-radius: 18; -fx-padding: 7 16; -fx-cursor: hand;");
        }
    }

    public static void renderListaArticoliCarrello(VBox boxVaschetteOrdine,
                                                   List<VaschettaItemBean> articoli,
                                                   IntConsumer onRimuoviIndice) {
        if (boxVaschetteOrdine == null) return;
        boxVaschetteOrdine.getChildren().clear();

        if (articoli == null || articoli.isEmpty()) {
            VBox emptyBox = new VBox(3);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 14; -fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 8; -fx-border-color: #ede0d4; -fx-border-radius: 8;");
            Label vuoto = new Label("Il tuo carrello è vuoto.");
            vuoto.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #9c6644;");
            Label hint = new Label("Seleziona i prodotti dal menu per aggiungerli al carrello.");
            hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #b08968;");
            emptyBox.getChildren().addAll(vuoto, hint);
            boxVaschetteOrdine.getChildren().add(emptyBox);
        } else {
            for (int i = 0; i < articoli.size(); i++) {
                final int idx = i;
                VaschettaItemBean vib = articoli.get(i);

                VBox card = new VBox(4);
                card.getStyleClass().add("carrello-item");
                card.setStyle("-fx-padding: 8 12; -fx-background-color: #ffffff; -fx-border-color: #e6d5c3; -fx-border-radius: 8; -fx-background-radius: 8;");

                HBox header = new HBox(8);
                header.setAlignment(Pos.CENTER_LEFT);

                String dLower = vib.ottieniDescrizione().toLowerCase();
                String icon = "🍨";
                if (dLower.contains("panna") && vib.ottieniGusti().isEmpty()) icon = "☁️";
                else if (dLower.contains("crepe") || dLower.contains("pancake")) icon = "🥞";
                else if (dLower.contains("coca") || dLower.contains("fanta") || dLower.contains("bibita")) icon = "🥤";
                else if (dLower.contains("acqua")) icon = "💧";
                else if (dLower.contains("crostata")) icon = "🥧";
                else if (dLower.contains("cantucci") || dLower.contains("baci")) icon = "🍪";
                else if (dLower.contains("caffe") || dLower.contains("caffè") || dLower.contains("cappuccino") || dLower.contains("cioccolata")) icon = "☕";
                else if (dLower.contains("tisana")) icon = "🍵";

                Label lblIcon = new Label(icon);
                lblIcon.setStyle("-fx-font-size: 16px;");
                lblIcon.setMinWidth(Region.USE_PREF_SIZE);

                String titolo;
                if (vib.ottieniPesoKg() > 0 && !vib.ottieniGusti().isEmpty()) {
                    titolo = "Vaschetta #" + (idx + 1) + " • " + ((int) (vib.ottieniPesoKg() * 1000)) + "g";
                } else if (dLower.contains("panna") && vib.ottieniPesoKg() > 0) {
                    titolo = "Panna d'Asporto • " + ((int) (vib.ottieniPesoKg() * 1000)) + "g";
                } else {
                    titolo = vib.ottieniDescrizione();
                }
                Label lblTitolo = new Label(titolo);
                lblTitolo.setWrapText(true);
                lblTitolo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #3a2618;");
                HBox.setHgrow(lblTitolo, Priority.ALWAYS);

                Label lblP = new Label(String.format(Locale.US, "EUR %.2f", vib.ottieniPrezzo()));
                lblP.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2a9d8f;");
                lblP.setMinWidth(Region.USE_PREF_SIZE);

                Button btnCanc = new Button("✕ Rimuovi");
                btnCanc.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #fceae8; -fx-text-fill: #bc4749; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                btnCanc.setMinWidth(Region.USE_PREF_SIZE);
                btnCanc.setOnAction(ev -> onRimuoviIndice.accept(idx));

                header.getChildren().addAll(lblIcon, lblTitolo, lblP, btnCanc);

                Label lblDet = new Label(vib.ottieniDescrizione());
                lblDet.setWrapText(true);
                lblDet.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f5539;");

                card.getChildren().addAll(header, lblDet);
                boxVaschetteOrdine.getChildren().add(card);
            }
        }
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    public static void apriPopupCarrello(Window owner,
                                         CarrelloBean carrello,
                                         IntConsumer onRimuoviArticolo,
                                         Runnable onSvuotaCarrello,
                                         TriConsumer<String, String, String> onConfermaOrdine) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Il Tuo Carrello & Checkout");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setStyle("-fx-padding: 22 26; -fx-background-color: #fffdfa;");
        root.setPrefWidth(620);
        root.setPrefHeight(540);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label lblTitolo = new Label("🛒 IL TUO CARRELLO FROSTLY");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #3a2618;");
        Region spTop = new Region();
        HBox.setHgrow(spTop, Priority.ALWAYS);
        Button btnSvuota = new Button("🗑️ Svuota Carrello");
        btnSvuota.setStyle("-fx-background-color: #fceae8; -fx-text-fill: #bc4749; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 10; -fx-padding: 5 12; -fx-cursor: hand;");
        top.getChildren().addAll(lblTitolo, spTop, btnSvuota);

        VBox boxLista = new VBox(8);
        ScrollPane scroll = new ScrollPane(boxLista);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: #e6d5c3; -fx-border-radius: 10;");

        Label lblTotale = new Label();
        lblTotale.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2a9d8f;");

        Runnable refreshInterno = new Runnable() {
            @Override
            public void run() {
                renderListaArticoliCarrello(boxLista, carrello.getArticoli(), idx -> {
                    onRimuoviArticolo.accept(idx);
                    this.run();
                });
                lblTotale.setText(String.format(Locale.US, "Totale Ordine: EUR %.2f", carrello.calcolaTotale()));
            }
        };
        refreshInterno.run();

        btnSvuota.setOnAction(e -> {
            onSvuotaCarrello.run();
            refreshInterno.run();
        });

        VBox boxCheckout = new VBox(8);
        boxCheckout.setStyle("-fx-background-color: #f7ede2; -fx-border-color: #d5bdaf; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 14;");

        Label lblRitiroTit = new Label("🛵 Modalità di Consegna / Ritiro e Orario desiderato:");
        lblRitiroTit.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #3a2618;");

        HBox rigaCombo = new HBox(10);
        rigaCombo.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> comboTipo = new ComboBox<>(FXCollections.observableArrayList(
                "🛵 Consegna a Domicilio", "🛍️ Ritiro al Banco"));
        comboTipo.setValue(carrello.getModalitaConsegna() != null ? carrello.getModalitaConsegna() : "🛵 Consegna a Domicilio");
        comboTipo.setPrefWidth(220);

        ComboBox<String> comboOra = new ComboBox<>(FXCollections.observableArrayList(
                "Subito (~25-35 min)",
                "Oggi 16:30", "Oggi 17:00", "Oggi 17:30", "Oggi 18:00",
                "Oggi 18:30", "Oggi 19:00", "Oggi 19:30", "Oggi 20:00",
                "Oggi 20:30", "Oggi 21:00", "Oggi 21:30", "Oggi 22:00",
                "Domani 13:00", "Domani 18:00", "Domani 20:00"));
        comboOra.setValue(carrello.getFasciaOraria() != null ? carrello.getFasciaOraria() : "Subito (~25-35 min)");
        comboOra.setPrefWidth(190);
        rigaCombo.getChildren().addAll(comboTipo, comboOra);

        TextField txtNote = new TextField(carrello.getIndirizzoONote() != null ? carrello.getIndirizzoONote() : "");
        txtNote.setPromptText("Indirizzo di consegna (es. Via Roma 10, int. 4) o note per la gelateria...");

        boxCheckout.getChildren().addAll(lblRitiroTit, rigaCombo, txtNote);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Button btnContinua = new Button("⬅ Continua a Scegliere dal Menu");
        btnContinua.setStyle("-fx-background-color: #ede0d4; -fx-text-fill: #5c3a21; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 14; -fx-padding: 9 16; -fx-cursor: hand;");
        btnContinua.setOnAction(e -> popup.close());

        Region spF = new Region();
        HBox.setHgrow(spF, Priority.ALWAYS);

        Button btnInvia = new Button("🛵 Conferma e Invia Ordine");
        btnInvia.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 14; -fx-padding: 9 20; -fx-cursor: hand;");
        btnInvia.setOnAction(e -> {
            popup.close();
            onConfermaOrdine.accept(comboTipo.getValue(), comboOra.getValue(), txtNote.getText());
        });

        footer.getChildren().addAll(btnContinua, spF, lblTotale, btnInvia);

        root.getChildren().addAll(top, scroll, boxCheckout, footer);
        popup.setScene(new Scene(root));
        popup.showAndWait();
    }
}
