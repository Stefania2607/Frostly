package view;

import bean.NotificaBean;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Componente View dedicato alla visualizzazione del Registro Cronologico Avvisi & Notifiche
 * per lo Staff (Maestro e Dipendenti).
 * Gestisce il rendering delle card, l'evidenziazione degli avvisi nuovi,
 * l'eliminazione dei singoli avvisi, l'eliminazione delle scrollbar e il popup modale per la lettura completa.
 */
public final class AvvisiStaffView {

    private AvvisiStaffView() {
    }

    public static void configuraListaAvvisi(ListView<NotificaBean> listaNotifiche,
                                            Consumer<NotificaBean> onVisualizzaDettaglio) {
        configuraListaAvvisi(listaNotifiche, onVisualizzaDettaglio, null);
    }

    public static void configuraListaAvvisi(ListView<NotificaBean> listaNotifiche,
                                            Consumer<NotificaBean> onVisualizzaDettaglio,
                                            Consumer<NotificaBean> onEliminaNotifica) {
        if (listaNotifiche == null) return;

        listaNotifiche.setCellFactory(lv -> new ListCell<NotificaBean>() {
            @Override
            protected void updateItem(NotificaBean item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    VBox card = buildCardAvviso(item, onVisualizzaDettaglio, onEliminaNotifica);
                    card.maxWidthProperty().bind(listaNotifiche.widthProperty().subtract(28));
                    setGraphic(card);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
                } else {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        listaNotifiche.setOnMouseClicked(event -> {
            NotificaBean sel = listaNotifiche.getSelectionModel().getSelectedItem();
            if (sel != null && onVisualizzaDettaglio != null) {
                onVisualizzaDettaglio.accept(sel);
            }
        });
    }

    public static VBox buildCardAvviso(NotificaBean n, Consumer<NotificaBean> onVisualizzaDettaglio) {
        return buildCardAvviso(n, onVisualizzaDettaglio, null);
    }

    public static VBox buildCardAvviso(NotificaBean n,
                                       Consumer<NotificaBean> onVisualizzaDettaglio,
                                       Consumer<NotificaBean> onEliminaNotifica) {
        String ts = n.getTimestamp() != null ? n.getTimestamp() : "";
        boolean isNuovo = ts.toLowerCase().contains("oggi");

        VBox card = new VBox(7);
        card.setCursor(javafx.scene.Cursor.HAND);

        if (isNuovo) {
            card.setStyle("-fx-padding: 12 16; -fx-background-color: rgba(28, 25, 30, 0.95); -fx-border-color: #baa487; "
                    + "-fx-border-width: 1.8; -fx-border-radius: 12; -fx-background-radius: 12; "
                    + "-fx-effect: dropshadow(gaussian, rgba(186,164,135,0.30), 8, 0, 0, 2);");
        } else {
            card.setStyle("-fx-padding: 11 15; -fx-background-color: rgba(22, 20, 24, 0.90); -fx-border-color: rgba(186,164,135,0.28); "
                    + "-fx-border-width: 1.0; -fx-border-radius: 12; -fx-background-radius: 12;");
        }

        String tit = n.getTitolo() != null ? n.getTitolo() : "Avviso";
        String titLower = tit.toLowerCase();
        String msgLower = n.getMessaggio() != null ? n.getMessaggio().toLowerCase() : "";

        String icona;
        String categoria;
        String badgeStyle;

        if (titLower.contains("ordine") || msgLower.contains("ordine")) {
            icona = "🛵";
            categoria = "ORDINE RICEVUTO";
            badgeStyle = "-fx-background-color: rgba(22, 55, 38, 0.90); -fx-text-fill: #a7f3d0; -fx-border-color: #10b981;";
        } else if (titLower.contains("emergenza") || msgLower.contains("emergenza")) {
            icona = "🚨";
            categoria = "EMERGENZA TURNO";
            badgeStyle = "-fx-background-color: rgba(70, 20, 25, 0.90); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444;";
        } else if (titLower.contains("gusto") || msgLower.contains("gusto")) {
            icona = "🎨";
            categoria = "RICHIESTA GUSTO";
            badgeStyle = "-fx-background-color: rgba(65, 48, 20, 0.90); -fx-text-fill: #fcd34d; -fx-border-color: #baa487;";
        } else if (titLower.contains("recension") || msgLower.contains("recension")) {
            icona = "⭐";
            categoria = "NUOVA RECENSIONE";
            badgeStyle = "-fx-background-color: rgba(55, 30, 20, 0.90); -fx-text-fill: #fdba74; -fx-border-color: #f97316;";
        } else {
            icona = "📢";
            categoria = "COMUNICAZIONE";
            badgeStyle = "-fx-background-color: rgba(25, 45, 65, 0.90); -fx-text-fill: #93c5fd; -fx-border-color: #3b82f6;";
        }

        HBox rigaTop = new HBox(8);
        rigaTop.setAlignment(Pos.CENTER_LEFT);

        Label lblCat = new Label(icona + "  " + categoria);
        lblCat.setStyle(badgeStyle + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 6; -fx-border-radius: 6;");

        Label lblTimestamp = new Label("🕒 " + ts);
        lblTimestamp.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #baa487; -fx-font-weight: bold;");

        Region spTop = new Region();
        HBox.setHgrow(spTop, Priority.ALWAYS);

        rigaTop.getChildren().addAll(lblCat, lblTimestamp, spTop);

        if (isNuovo) {
            Label lblBadgeNuovo = new Label("🔔 NUOVO");
            lblBadgeNuovo.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 2 7; -fx-background-radius: 6;");
            rigaTop.getChildren().add(lblBadgeNuovo);
        }
        Label lblTitolo = new Label(tit);
        lblTitolo.setWrapText(true);
        lblTitolo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        String msg = n.getMessaggio() != null ? n.getMessaggio().trim() : "";
        Label lblMsg = new Label(msg);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #ded7cd;");
        HBox rigaBottom = new HBox(10);
        rigaBottom.setAlignment(Pos.CENTER_LEFT);

        Label lblCallout = new Label("👁️ Clicca per visualizzare l'avviso completo ➔");
        lblCallout.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #baa487; -fx-cursor: hand;");

        Region spBottom = new Region();
        HBox.setHgrow(spBottom, Priority.ALWAYS);

        rigaBottom.getChildren().addAll(lblCallout, spBottom);

        if (onEliminaNotifica != null) {
            Button btnElimina = new Button("🗑️ Elimina");
            btnElimina.setStyle("-fx-background-color: rgba(70, 20, 25, 0.85); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444; "
                    + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12.5px; -fx-font-weight: bold; "
                    + "-fx-padding: 3 10; -fx-cursor: hand;");
            btnElimina.setOnAction(e -> {
                e.consume();
                onEliminaNotifica.accept(n);
            });
            rigaBottom.getChildren().add(btnElimina);
        }

        card.getChildren().addAll(rigaTop, lblTitolo, lblMsg, rigaBottom);

        if (onVisualizzaDettaglio != null) {
            card.setOnMouseClicked(e -> onVisualizzaDettaglio.accept(n));
        }

        return card;
    }

    public static void mostraPopupDettaglioAvviso(Window owner, NotificaBean n, Consumer<String> onNavigaSezione) {
        mostraPopupDettaglioAvviso(owner, n, onNavigaSezione, null);
    }

    public static void mostraPopupDettaglioAvviso(Window owner, NotificaBean n,
                                                   Consumer<String> onNavigaSezione,
                                                   Consumer<NotificaBean> onEliminaNotifica) {
        if (n == null) return;

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Dettaglio Avviso & Notifica");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 22 26; -fx-background-color: rgba(18, 17, 19, 0.97); -fx-border-color: rgba(186,164,135,0.35); -fx-border-width: 1.2;");
        root.setPrefWidth(600);
        root.setPrefHeight(440);

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        String tit = n.getTitolo() != null ? n.getTitolo() : "Avviso";
        String titLower = tit.toLowerCase();
        String msgLower = n.getMessaggio() != null ? n.getMessaggio().toLowerCase() : "";

        String icona = "📋";
        if (titLower.contains("ordine") || msgLower.contains("ordine")) icona = "🛵";
        else if (titLower.contains("emergenza") || msgLower.contains("emergenza")) icona = "🚨";
        else if (titLower.contains("gusto") || msgLower.contains("gusto")) icona = "🎨";
        else if (titLower.contains("recension") || msgLower.contains("recension")) icona = "⭐";

        Label lblIcon = new Label(icona);
        lblIcon.setStyle("-fx-font-size: 26px;");

        VBox vTop = new VBox(2);
        Label lblTit = new Label(tit);
        lblTit.setWrapText(true);
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #f5ebe0;");

        Label lblTs = new Label("Registrato: " + n.getTimestamp());
        lblTs.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #baa487;");
        vTop.getChildren().addAll(lblTit, lblTs);
        HBox.setHgrow(vTop, Priority.ALWAYS);

        top.getChildren().addAll(lblIcon, vTop);

        VBox boxCorpo = new VBox(8);
        boxCorpo.setStyle("-fx-background-color: rgba(26, 24, 28, 0.90); -fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 14 16;");
        VBox.setVgrow(boxCorpo, Priority.ALWAYS);

        Label lblMsgCompleto = new Label(n.getMessaggio());
        lblMsgCompleto.setWrapText(true);
        lblMsgCompleto.setStyle("-fx-font-size: 13px; -fx-text-fill: #f5ebe0; -fx-line-spacing: 3px;");

        ScrollPane scroll = new ScrollPane(lblMsgCompleto);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        boxCorpo.getChildren().add(scroll);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        if (onEliminaNotifica != null) {
            Button btnEliminaModal = new Button("🗑️ Elimina Avviso");
            btnEliminaModal.setStyle("-fx-background-color: rgba(70, 20, 25, 0.85); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444; "
                    + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13.5px; -fx-font-weight: bold; -fx-padding: 7 14; -fx-cursor: hand;");
            btnEliminaModal.setOnAction(e -> {
                popup.close();
                onEliminaNotifica.accept(n);
            });
            footer.getChildren().add(btnEliminaModal);
        }

        Region spFooter = new Region();
        HBox.setHgrow(spFooter, Priority.ALWAYS);
        footer.getChildren().add(spFooter);

        if (onNavigaSezione != null) {
            if (titLower.contains("ordine") || msgLower.contains("ordine")) {
                Button btnVai = new Button("🛵 Vai a Gestione Ordini ➔");
                btnVai.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;");
                btnVai.setOnAction(e -> {
                    popup.close();
                    onNavigaSezione.accept("Ordini");
                });
                footer.getChildren().add(btnVai);
            } else if (titLower.contains("emergenza") || msgLower.contains("emergenza")) {
                Button btnVai = new Button("🚨 Vai a Turni & Emergenze ➔");
                btnVai.setStyle("-fx-background-color: rgba(70,20,25,0.85); -fx-text-fill: #fca5a5; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand; -fx-border-color: #ef4444;");
                btnVai.setOnAction(e -> {
                    popup.close();
                    onNavigaSezione.accept("Turni");
                });
                footer.getChildren().add(btnVai);
            } else if (titLower.contains("gusto") || msgLower.contains("gusto")) {
                Button btnVai = new Button("🎨 Vai a Gusti su Misura ➔");
                btnVai.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;");
                btnVai.setOnAction(e -> {
                    popup.close();
                    onNavigaSezione.accept("Magazzino");
                });
                footer.getChildren().add(btnVai);
            } else if (titLower.contains("recension") || msgLower.contains("recension")) {
                Button btnVai = new Button("⭐ Vai a Recensioni ➔");
                btnVai.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;");
                btnVai.setOnAction(e -> {
                    popup.close();
                    onNavigaSezione.accept("Recensioni");
                });
                footer.getChildren().add(btnVai);
            }
        }

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: rgba(38, 35, 40, 0.85); -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand; -fx-border-color: rgba(186,164,135,0.3);");
        btnChiudi.setOnAction(e -> popup.close());

        footer.getChildren().add(btnChiudi);

        root.getChildren().addAll(top, boxCorpo, footer);
        popup.setScene(new Scene(root));
        popup.showAndWait();
    }
}
