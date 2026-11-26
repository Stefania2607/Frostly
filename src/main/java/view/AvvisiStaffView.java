package view;

import bean.NotificaBean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
                    boolean provenza = RecensioniView.isTemaSalottoChiaro(listaNotifiche);
                    VBox card = buildCardAvviso(item, onVisualizzaDettaglio, onEliminaNotifica, provenza);
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
        return buildCardAvviso(n, onVisualizzaDettaglio, onEliminaNotifica, false);
    }

    public static VBox buildCardAvviso(NotificaBean n,
                                       Consumer<NotificaBean> onVisualizzaDettaglio,
                                       Consumer<NotificaBean> onEliminaNotifica,
                                       boolean provenza) {
        String ts = n.getTimestamp() != null ? n.getTimestamp() : "";
        boolean isNuovo = ts.toLowerCase().contains("oggi");

        VBox card = new VBox(7);
        card.setCursor(javafx.scene.Cursor.HAND);

        if (provenza) {
            if (isNuovo) {
                card.setStyle("-fx-padding: 12 16; -fx-background-color: #fffaf7; -fx-border-color: #9366a0; "
                        + "-fx-border-width: 1.5; -fx-border-radius: 14; -fx-background-radius: 14;");
            } else {
                card.setStyle("-fx-padding: 11 15; -fx-background-color: #ffffff; -fx-border-color: #e9dbdf; "
                        + "-fx-border-width: 1.0; -fx-border-radius: 14; -fx-background-radius: 14;");
            }
        } else if (isNuovo) {
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
        lblTimestamp.setStyle("-fx-font-size: 12.5px; -fx-text-fill: " + (provenza ? "#764c86" : "#baa487") + "; -fx-font-weight: bold;");

        Region spTop = new Region();
        HBox.setHgrow(spTop, Priority.ALWAYS);

        rigaTop.getChildren().addAll(lblCat, lblTimestamp, spTop);

        if (isNuovo) {
            Label lblBadgeNuovo = new Label("🔔 NUOVO");
            lblBadgeNuovo.setStyle(provenza
                    ? "-fx-background-color: #764c86; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-padding: 2 8; -fx-background-radius: 10;"
                    : "-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 2 7; -fx-background-radius: 6;");
            rigaTop.getChildren().add(lblBadgeNuovo);
        }
        Label lblTitolo = new Label(tit);
        lblTitolo.setWrapText(true);
        lblTitolo.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));
        String msg = n.getMessaggio() != null ? n.getMessaggio().trim() : "";
        Label lblMsg = new Label(msg);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (provenza ? "#5e525e;" : "#ded7cd;"));
        HBox rigaBottom = new HBox(10);
        rigaBottom.setAlignment(Pos.CENTER_LEFT);

        Label lblCallout = new Label("👁️ Clicca per visualizzare l'avviso completo ➔");
        lblCallout.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#764c86" : "#baa487") + "; -fx-cursor: hand;");

        Region spBottom = new Region();
        HBox.setHgrow(spBottom, Priority.ALWAYS);

        rigaBottom.getChildren().addAll(lblCallout, spBottom);

        if (onEliminaNotifica != null) {
            Button btnElimina = new Button("🗑️ Elimina");
            btnElimina.setStyle(provenza
                    ? "-fx-background-color: #fdf2f2; -fx-text-fill: #b91c1c; -fx-border-color: #fca5a5; "
                      + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 12px; -fx-font-weight: bold; "
                      + "-fx-padding: 3 10; -fx-cursor: hand;"
                    : "-fx-background-color: rgba(70, 20, 25, 0.85); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444; "
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
            card.setOnMouseClicked(e -> {
                e.consume();
                onVisualizzaDettaglio.accept(n);
            });
        }

        return card;
    }

    public static void mostraPopupDettaglioAvviso(Window owner, NotificaBean n, Consumer<String> onNavigaSezione) {
        mostraPopupDettaglioAvviso(owner, n, onNavigaSezione, null);
    }

    public static void mostraPopupDettaglioAvviso(Window owner, NotificaBean n,
                                                   Consumer<String> onNavigaSezione,
                                                   Consumer<NotificaBean> onEliminaNotifica) {
        BiConsumer<String, String> nav = onNavigaSezione != null
                ? (sezione, codiceOrdine) -> onNavigaSezione.accept(sezione)
                : null;
        mostraPopupDettaglioAvviso(owner, n, nav, onEliminaNotifica);
    }

    public static void mostraPopupDettaglioAvviso(Window owner, NotificaBean n,
                                                   BiConsumer<String, String> onNavigaSezioneConCodice,
                                                   Consumer<NotificaBean> onEliminaNotifica) {
        DettaglioAvvisoDialog.mostra(owner, n, onNavigaSezioneConCodice, onEliminaNotifica);
    }
}
