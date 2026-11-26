package view;

import bean.NotificaBean;
import bean.OrdineBean;
import control.applicativo.OrdiniControl;
import frostly.exception.FrostlyException;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import util.SessionManager;

/**
 * Finestra di chat bidirezionale per un singolo ordine tra Cliente e Staff Frostly.
 */
public final class ChatOrdineView {

    private ChatOrdineView() {
    }

    public static void mostraChatOrdine(Window owner, OrdineBean item, boolean vistaStaff) {
        if (item == null) return;

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Chat Ordine " + item.getCodiceOrdine());
        if (owner != null) {
            popup.initOwner(owner);
        }

        String emailCliente = item.getEmailCliente() != null ? item.getEmailCliente().trim() : "";
        String nomeCliente = (item.getNomeCliente() != null && !item.getNomeCliente().isBlank())
                ? item.getNomeCliente() : emailCliente;

        Label lblTitolo = new Label("💬 Chat Ordine " + item.getCodiceOrdine());
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        Label lblSub = new Label(vistaStaff
                ? ("Conversazione diretta con " + nomeCliente + " (" + emailCliente + ")")
                : "Conversazione diretta con la Gelateria Frostly");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92;");
        VBox header = new VBox(2, lblTitolo, lblSub);

        VBox boxMessaggi = new VBox(8);
        boxMessaggi.setStyle("-fx-padding: 10 12;");

        ScrollPane scroll = new ScrollPane(boxMessaggi);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: rgba(18, 17, 22, 0.90); -fx-background: rgba(18, 17, 22, 0.90); "
                + "-fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 12; -fx-background-radius: 12;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Runnable ricaricaMessaggi = () -> {
            boxMessaggi.getChildren().clear();
            List<NotificaBean> lista = new OrdiniControl().caricaMessaggiChatOrdine(item.getCodiceOrdine(), emailCliente);
            if (lista.isEmpty()) {
                Label vuoto = new Label(vistaStaff
                        ? "Nessun messaggio ancora. Invia un aggiornamento al cliente qui sotto."
                        : "Nessun messaggio ancora. Scrivi alla gelateria se hai bisogno di assistenza sull'ordine.");
                vuoto.setWrapText(true);
                vuoto.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #8e8e93; -fx-padding: 20 8;");
                boxMessaggi.getChildren().add(vuoto);
            } else {
                for (NotificaBean m : lista) {
                    boxMessaggi.getChildren().add(creaBollaMessaggio(m, vistaStaff));
                }
            }
            TemaProvenzaHelper.applicaSeGui2(boxMessaggi);
            javafx.application.Platform.runLater(() -> scroll.setVvalue(1.0));
        };
        ricaricaMessaggi.run();

        TextField txtInput = new TextField();
        txtInput.setPromptText(vistaStaff ? "Scrivi un messaggio al cliente..." : "Scrivi una risposta alla gelateria...");
        txtInput.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; "
                + "-fx-border-color: rgba(186,164,135,0.32); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 12; -fx-font-size: 13px;");
        HBox.setHgrow(txtInput, Priority.ALWAYS);

        Label lblErrore = new Label();
        lblErrore.setVisible(false);
        lblErrore.setManaged(false);
        lblErrore.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 12px; -fx-font-weight: bold;");

        Runnable invia = () -> {
            lblErrore.setVisible(false);
            lblErrore.setManaged(false);
            try {
                String mittente = SessionManager.getInstance().getNomeUtenteLoggato();
                new OrdiniControl().inviaMessaggioChatOrdine(
                        item.getCodiceOrdine(), emailCliente, mittente, vistaStaff, txtInput.getText());
                txtInput.clear();
                ricaricaMessaggi.run();
            } catch (FrostlyException ex) {
                lblErrore.setText("⚠️ " + ex.getMessage());
                lblErrore.setVisible(true);
                lblErrore.setManaged(true);
            }
        };

        txtInput.setOnAction(e -> invia.run());

        Button btnInvia = new Button("Invia ➔");
        btnInvia.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 9 16; -fx-cursor: hand;");
        btnInvia.setOnAction(e -> invia.run());

        HBox barraInput = new HBox(8, txtInput, btnInvia);
        barraInput.setAlignment(Pos.CENTER_LEFT);

        Button btnAggiorna = new Button("🔄 Aggiorna");
        btnAggiorna.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-text-fill: #ded7cd; -fx-font-size: 12px; "
                + "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand;");
        btnAggiorna.setOnAction(e -> ricaricaMessaggi.run());

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-font-size: 12.5px; "
                + "-fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5 14; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox footer = new HBox(10, btnAggiorna, sp, btnChiudi);
        footer.setAlignment(Pos.CENTER_LEFT);

        boolean clienteSolaLettura = !vistaStaff && !OrdiniControl.isOrdineAttivoPerChatCliente(item);
        VBox root = new VBox(10, header, scroll);
        if (vistaStaff) {
            HBox presets = new HBox(8,
                    creaChip("⏱️ Ritardo 10-15 min",
                            "Gentile cliente, il tuo ordine " + item.getCodiceOrdine() + " subirà un lieve ritardo di 10-15 minuti. Ci scusiamo!", txtInput),
                    creaChip("🍦 Gusto esaurito",
                            "Gentile cliente, uno dei gusti dell'ordine " + item.getCodiceOrdine() + " è terminato: con quale preferisci sostituirlo?", txtInput),
                    creaChip("📍 Problema consegna",
                            "Gentile cliente, abbiamo difficoltà a contattarti per l'ordine " + item.getCodiceOrdine() + ". Puoi risponderci qui?", txtInput));
            root.getChildren().addAll(presets, lblErrore, barraInput, footer);
        } else if (clienteSolaLettura) {
            Label lblChiusa = new Label("🔒 Ordine concluso: la chat è in sola lettura. Puoi scrivere alla gelateria solo mentre un ordine è attivo.");
            lblChiusa.setWrapText(true);
            lblChiusa.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92; -fx-background-color: rgba(34,32,38,0.9); "
                    + "-fx-padding: 8 12; -fx-background-radius: 8; -fx-border-color: rgba(186,164,135,0.2); -fx-border-radius: 8;");
            root.getChildren().addAll(lblChiusa, footer);
        } else {
            root.getChildren().addAll(lblErrore, barraInput, footer);
        }
        root.setStyle("-fx-padding: 18 22; -fx-background-color: rgba(16, 15, 18, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");

        Scene scene = new Scene(root, 580, 500);
        scene.setFill(javafx.scene.paint.Color.web("#121114"));
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(popup.getScene());
        popup.centerOnScreen();
        popup.showAndWait();
    }

    public static void configuraListaChat(javafx.scene.control.ListView<OrdineBean> lista,
                                          boolean vistaStaff,
                                          Runnable onAggiornaDopoChat) {
        if (lista == null) return;
        lista.setCellFactory(lv -> new javafx.scene.control.ListCell<OrdineBean>() {
            @Override
            protected void updateItem(OrdineBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                OrdiniControl ctrl = new OrdiniControl();
                List<NotificaBean> msgs = ctrl.caricaMessaggiChatOrdine(item.getCodiceOrdine(), item.getEmailCliente());
                int nMsg = msgs.size();
                boolean attivo = OrdiniControl.isOrdineAttivoPerChatCliente(item);

                Label lblCod = new Label(item.getCodiceOrdine());
                lblCod.setMinWidth(Region.USE_PREF_SIZE);
                lblCod.setStyle("-fx-background-color: rgba(212,180,131,0.22); -fx-text-fill: #e5c99b; "
                        + "-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 4 10; -fx-background-radius: 7;");

                String titolo = vistaStaff
                        ? ("👤 " + (item.getNomeCliente() != null && !item.getNomeCliente().isBlank() ? item.getNomeCliente() : item.getEmailCliente()))
                        : "🍦 Gelateria Frostly";
                Label lblTitolo = new Label(titolo);
                lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

                String anteprima;
                if (nMsg > 0) {
                    NotificaBean ultimo = msgs.get(msgs.size() - 1);
                    String pulito = ultimo.getMessaggio() != null
                            ? ultimo.getMessaggio().replaceFirst("^\\[(STAFF|CLIENTE)\\]\\s*", "") : "";
                    anteprima = "💬 " + pulito + "  (" + ultimo.getTimestamp() + ")";
                } else {
                    anteprima = attivo
                            ? "Ordine attivo — clicca per avviare la chat"
                            : "Nessun messaggio su questo ordine";
                }
                boolean provenza = RecensioniView.isTemaSalottoChiaro(lista);
                Label lblPrev = new Label(anteprima);
                lblPrev.setWrapText(true);
                lblPrev.setStyle("-fx-font-size: 12.5px; -fx-text-fill: " + (provenza
                        ? (nMsg > 0 ? "#5e525e" : "#8d7c92")
                        : (nMsg > 0 ? "#ded7cd" : "#8e8e93")) + ";");

                if (provenza) {
                    lblCod.setStyle(nMsg > 0
                            ? "-fx-background-color: #764c86; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 4 10; -fx-background-radius: 10;"
                            : "-fx-background-color: #f3eae5; -fx-text-fill: #5e316f; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 4 10; -fx-background-radius: 10;");
                    lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #37253d;");
                }

                VBox centro = new VBox(3, lblTitolo, lblPrev);
                HBox.setHgrow(centro, Priority.ALWAYS);

                Label lblFase = OrdiniStaffView.creaBadgeStatoOrdine(item);
                lblFase.setMinWidth(Region.USE_PREF_SIZE);

                HBox card = new HBox(12, lblCod, centro, lblFase);
                card.setAlignment(Pos.CENTER_LEFT);

                if (nMsg > 0) {
                    Label badgeCount = new Label("🔔 " + nMsg);
                    badgeCount.setMinWidth(Region.USE_PREF_SIZE);
                    badgeCount.setStyle(provenza
                            ? "-fx-background-color: #764c86; -fx-text-fill: #ffffff; -fx-font-weight: bold; "
                              + "-fx-font-size: 12px; -fx-padding: 3 9; -fx-background-radius: 12;"
                            : "-fx-background-color: #d4b483; -fx-text-fill: #141216; -fx-font-weight: bold; "
                              + "-fx-font-size: 12px; -fx-padding: 3 9; -fx-background-radius: 12;");
                    card.getChildren().add(badgeCount);
                }

                Label lblApri = new Label("Apri ›");
                lblApri.setMinWidth(Region.USE_PREF_SIZE);
                lblApri.setStyle("-fx-text-fill: " + (provenza ? "#764c86" : "#d4b483") + "; -fx-font-weight: bold; -fx-font-size: 12.5px;");
                card.getChildren().add(lblApri);

                card.maxWidthProperty().bind(lista.widthProperty().subtract(28));
                card.setStyle(provenza
                        ? "-fx-padding: 12 16; -fx-background-color: " + (nMsg > 0 ? "#fffaf7" : "#ffffff")
                          + "; -fx-border-color: " + (nMsg > 0 ? "#764c86" : "#e9dbdf")
                          + "; -fx-border-width: " + (nMsg > 0 ? "1.5" : "1.0")
                          + "; -fx-border-radius: 14; -fx-background-radius: 14; -fx-cursor: hand;"
                        : "-fx-padding: 12 16; -fx-background-color: "
                          + (nMsg > 0 ? "rgba(36, 32, 38, 0.96)" : "rgba(24, 22, 27, 0.90)")
                          + "; -fx-border-color: " + (nMsg > 0 ? "#d4b483" : "rgba(186,164,135,0.22)")
                          + "; -fx-border-width: " + (nMsg > 0 ? "1.5" : "1.0")
                          + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;");
                card.setOnMouseClicked(e -> {
                    Window win = lista.getScene() != null ? lista.getScene().getWindow() : null;
                    mostraChatOrdine(win, item, vistaStaff);
                    if (onAggiornaDopoChat != null) {
                        onAggiornaDopoChat.run();
                    }
                });

                setGraphic(card);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
            }
        });
    }

    private static HBox creaBollaMessaggio(NotificaBean m, boolean vistaStaff) {
        String raw = m.getMessaggio() != null ? m.getMessaggio() : "";
        boolean daStaff = raw.startsWith("[STAFF]") || !raw.startsWith("[CLIENTE]");
        String testoPulito = raw.replaceFirst("^\\[(STAFF|CLIENTE)\\]\\s*", "");
        boolean mioMessaggio = (vistaStaff && daStaff) || (!vistaStaff && !daStaff);

        VBox bolla = new VBox(3);
        bolla.setMaxWidth(410);
        bolla.setStyle(mioMessaggio
                ? "-fx-background-color: rgba(212, 180, 131, 0.22); -fx-border-color: rgba(212, 180, 131, 0.45); "
                  + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8 12;"
                : "-fx-background-color: rgba(42, 40, 48, 0.95); -fx-border-color: rgba(255, 255, 255, 0.10); "
                  + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8 12;");

        Label lblAutore = new Label((daStaff ? "🍦 Gelateria Frostly" : "👤 Cliente") + "  •  " + m.getTimestamp());
        lblAutore.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (mioMessaggio ? "#e5c99b" : "#a89f92") + ";");

        Label lblTesto = new Label(testoPulito);
        lblTesto.setWrapText(true);
        lblTesto.setStyle("-fx-font-size: 13px; -fx-text-fill: #f5ebe0;");

        bolla.getChildren().addAll(lblAutore, lblTesto);

        HBox wrapper = new HBox(bolla);
        wrapper.setAlignment(mioMessaggio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return wrapper;
    }

    private static Button creaChip(String label, String testo, TextField target) {
        Button b = new Button(label);
        b.setStyle("-fx-background-color: rgba(38, 35, 42, 0.90); -fx-text-fill: #ded7cd; -fx-font-size: 11.5px; "
                + "-fx-border-color: rgba(186,164,135,0.28); -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 10; -fx-cursor: hand;");
        b.setOnAction(e -> target.setText(testo));
        return b;
    }
}
