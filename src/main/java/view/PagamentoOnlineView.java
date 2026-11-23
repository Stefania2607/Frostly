package view;

import bean.CartaPagamentoBean;
import bean.DatiConsegnaBean;
import bean.EsitoPagamentoBean;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * View dedicata alla simulazione del pagamento online con Carta di Credito / Debito.
 * controlli di validazione e schermata di esito (transazione autorizzata o rifiutata).
 */
public final class PagamentoOnlineView {

    private PagamentoOnlineView() {
    }

    public static void apriPopupPagamento(Window owner,
                                          double importo,
                                          DatiConsegnaBean datiConsegna,
                                          Consumer<CartaPagamentoBean> onRichiestaPagamento,
                                          Runnable onTornaIndietro) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Pagamento Sicuro Online");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 24 28; -fx-background-color: rgba(18, 17, 20, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");
        root.setPrefWidth(660);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblBadge = new Label("🔒");
        lblBadge.setStyle("-fx-font-size: 20px; -fx-background-color: rgba(186,164,135,0.15); "
                + "-fx-padding: 6 10; -fx-background-radius: 50; -fx-border-color: rgba(186,164,135,0.3);");

        VBox titleBox = new VBox(2);
        Label lblSub = new Label("FROSTLY • GATEWAY DI PAGAMENTO PROTETTO SSL 256-BIT");
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #baa487; -fx-letter-spacing: 0.8px;");
        Label lblTitolo = new Label("Simulazione Pagamento con Carta");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 17.5px; -fx-text-fill: #f5ebe0;");
        titleBox.getChildren().addAll(lblSub, lblTitolo);

        Region spH = new Region();
        HBox.setHgrow(spH, Priority.ALWAYS);

        VBox boxTotale = new VBox(2);
        boxTotale.setAlignment(Pos.CENTER_RIGHT);
        Label lblTotTit = new Label("IMPORTO DA SALDARE");
        lblTotTit.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #a89f92;");
        Label lblImporto = new Label(String.format(Locale.US, "EUR %.2f", importo));
        lblImporto.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #baa487;");
        boxTotale.getChildren().addAll(lblTotTit, lblImporto);

        header.getChildren().addAll(lblBadge, titleBox, spH, boxTotale);

        HBox boxRiepilogo = new HBox(10);
        boxRiepilogo.setAlignment(Pos.CENTER_LEFT);
        boxRiepilogo.setStyle("-fx-background-color: rgba(28,25,33,0.85); -fx-border-color: rgba(186,164,135,0.22); "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 14;");

        Label lblRiepIcon = new Label(datiConsegna.getTipoConsegna() == DatiConsegnaBean.TipoConsegna.DOMICILIO ? "🛵" : "🛍️");
        lblRiepIcon.setStyle("-fx-font-size: 14px;");
        Label lblRiepTesto = new Label(datiConsegna.costruisciStringaRiepilogoConsegna());
        lblRiepTesto.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd;");
        boxRiepilogo.getChildren().addAll(lblRiepIcon, lblRiepTesto);

        VBox cardGrafica = new VBox(10);
        cardGrafica.setStyle("-fx-background-color: linear-gradient(to bottom right, #242028, #16141a); "
                + "-fx-border-color: rgba(186,164,135,0.45); -fx-border-radius: 12; -fx-background-radius: 12; "
                + "-fx-padding: 16 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);");

        HBox rigaTopCard = new HBox(10);
        rigaTopCard.setAlignment(Pos.CENTER_LEFT);

        Label lblChip = new Label("💳 CHIP");
        lblChip.setStyle("-fx-background-color: rgba(186,164,135,0.3); -fx-text-fill: #baa487; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 6; -fx-background-radius: 4;");

        Region spCard = new Region();
        HBox.setHgrow(spCard, Priority.ALWAYS);

        Label lblCircuito = new Label("MASTERCARD / VISA");
        lblCircuito.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #baa487;");
        rigaTopCard.getChildren().addAll(lblChip, spCard, lblCircuito);

        Label lblPreviewNumero = new Label("••••  ••••  ••••  ••••");
        lblPreviewNumero.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0; -fx-padding: 6 0;");

        HBox rigaBottomCard = new HBox(20);
        rigaBottomCard.setAlignment(Pos.CENTER_LEFT);

        VBox bTitolare = new VBox(1);
        Label lblTitTit = new Label("TITOLARE CARTA");
        lblTitTit.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #a89f92;");
        Label lblPreviewTitolare = new Label("NOME COGNOME");
        lblPreviewTitolare.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ded7cd;");
        bTitolare.getChildren().addAll(lblTitTit, lblPreviewTitolare);

        VBox bScadenza = new VBox(1);
        Label lblScadTit = new Label("SCADENZA");
        lblScadTit.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #a89f92;");
        Label lblPreviewScadenza = new Label("MM/AA");
        lblPreviewScadenza.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ded7cd;");
        bScadenza.getChildren().addAll(lblScadTit, lblPreviewScadenza);

        Region spBC = new Region();
        HBox.setHgrow(spBC, Priority.ALWAYS);

        Label lblContactless = new Label("📶");
        lblContactless.setStyle("-fx-font-size: 14px; -fx-text-fill: #baa487;");

        rigaBottomCard.getChildren().addAll(bTitolare, bScadenza, spBC, lblContactless);
        cardGrafica.getChildren().addAll(rigaTopCard, lblPreviewNumero, rigaBottomCard);

        VBox formCampi = new VBox(10);
        formCampi.setStyle("-fx-background-color: rgba(24,22,28,0.90); -fx-border-color: rgba(186,164,135,0.25); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 14 16;");

        TextField txtTitolare = new TextField();
        txtTitolare.setPromptText("Nome e Cognome dell'Intestatario (es. Mario Rossi) *");
        txtTitolare.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
        txtTitolare.textProperty().addListener((obs, oldVal, newVal) -> {
            lblPreviewTitolare.setText((newVal == null || newVal.isBlank()) ? "NOME COGNOME" : newVal.toUpperCase());
        });

        TextField txtNumero = new TextField();
        txtNumero.setPromptText("Numero Carta (16 cifre) *");
        txtNumero.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8; -fx-font-family: 'Courier New', monospace;");
        txtNumero.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String clean = newVal.replaceAll("[^0-9]", "");
            if (clean.length() > 16) {
                clean = clean.substring(0, 16);
            }
            // Aggiorna anteprima carta con blocchi di 4
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < clean.length(); i++) {
                if (i > 0 && i % 4 == 0) formatted.append("  ");
                formatted.append(clean.charAt(i));
            }
            if (clean.length() < 16) {
                int rim = 16 - clean.length();
                for (int i = 0; i < rim; i++) {
                    int pos = clean.length() + i;
                    if (pos > 0 && pos % 4 == 0) formatted.append("  ");
                    formatted.append("•");
                }
            }
            lblPreviewNumero.setText(formatted.toString());
            lblCircuito.setText(rilevaEtichettaCircuito(clean));
        });

        HBox rigaScadCvv = new HBox(12);
        TextField txtScadenza = new TextField();
        txtScadenza.setPromptText("Scadenza MM/AA *");
        txtScadenza.setPrefWidth(160);
        txtScadenza.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
        txtScadenza.textProperty().addListener((obs, oldVal, newVal) -> {
            lblPreviewScadenza.setText((newVal == null || newVal.isBlank()) ? "MM/AA" : newVal.trim());
        });

        PasswordField txtCvv = new PasswordField();
        txtCvv.setPromptText("CVV (3 cifre) *");
        txtCvv.setPrefWidth(140);
        txtCvv.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");

        Region spFill = new Region();
        HBox.setHgrow(spFill, Priority.ALWAYS);

        Button btnDemo = new Button("✨ Compila con Carta di Test");
        btnDemo.setStyle("-fx-background-color: rgba(186,164,135,0.15); -fx-text-fill: #baa487; -fx-font-weight: bold; "
                + "-fx-font-size: 12.5px; -fx-border-color: rgba(186,164,135,0.35); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
        btnDemo.setOnAction(e -> {
            // Carta Visa di test standard valida con algoritmo di Luhn
            txtTitolare.setText("Mario Rossi");
            txtNumero.setText("4532 0150 1234 5671");
            txtScadenza.setText("12/28");
            txtCvv.setText("789");
        });

        rigaScadCvv.getChildren().addAll(txtScadenza, txtCvv, spFill, btnDemo);
        formCampi.getChildren().addAll(txtTitolare, txtNumero, rigaScadCvv);

        Label lblErrore = new Label();
        lblErrore.setWrapText(true);
        lblErrore.setVisible(false);
        lblErrore.setStyle("-fx-text-fill: #ff8a80; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 10; "
                + "-fx-background-color: rgba(188,71,73,0.22); -fx-border-color: rgba(255,138,128,0.4); -fx-border-radius: 6; -fx-background-radius: 6;");

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        final boolean[] tornaIndietro = {false};

        Button btnIndietro = new Button("⬅ Modifica Dati Consegna");
        btnIndietro.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; "
                + "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;");
        btnIndietro.setOnAction(e -> {
            tornaIndietro[0] = true;
            popup.close();
        });

        Button btnPaga = new Button(String.format(Locale.US, "🔒 Paga e Autorizza EUR %.2f", importo));
        btnPaga.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 9 22; -fx-cursor: hand;");

        btnPaga.setOnAction(e -> {
            lblErrore.setVisible(false);
            CartaPagamentoBean cartaBean = new CartaPagamentoBean(
                    txtTitolare.getText(),
                    txtNumero.getText(),
                    txtScadenza.getText(),
                    txtCvv.getText()
            );

            try {
                if (onRichiestaPagamento != null) {
                    onRichiestaPagamento.accept(cartaBean);
                }
                popup.close();
            } catch (Exception ex) {
                lblErrore.setText("❌ " + ex.getMessage());
                lblErrore.setVisible(true);
            }
        });

        footer.getChildren().addAll(btnIndietro, btnPaga);

        root.getChildren().addAll(header, boxRiepilogo, cardGrafica, formCampi, lblErrore, footer);

        Scene scene = new Scene(root, 680, 580);
        scene.setFill(javafx.scene.paint.Color.web("#121114"));
        popup.setScene(scene);
        popup.sizeToScene();
        popup.centerOnScreen();
        popup.showAndWait();

        if (tornaIndietro[0] && onTornaIndietro != null) {
            javafx.application.Platform.runLater(onTornaIndietro);
        }
    }

    public static void mostraRicevutaPagamento(Window owner,
                                               EsitoPagamentoBean esito,
                                               Runnable onConcludiOrdine) {
        if (esito != null && esito.isSuccesso() && onConcludiOrdine != null) {
            onConcludiOrdine.run();
        }
        RicevutaPagamentoView.mostraRicevuta(owner, esito, null, null);
    }

    public static void mostraRicevutaPagamento(Window owner,
                                               EsitoPagamentoBean esito,
                                               String codiceOrdine) {
        RicevutaPagamentoView.mostraRicevuta(owner, esito, codiceOrdine, null);
    }

    public static void mostraRicevutaPagamento(Window owner,
                                               EsitoPagamentoBean esito,
                                               String codiceOrdine,
                                               String urlScontrino) {
        RicevutaPagamentoView.mostraRicevuta(owner, esito, codiceOrdine, urlScontrino);
    }

    private static String rilevaEtichettaCircuito(String numero) {
        if (numero == null || numero.isEmpty()) return "CARTA DI CREDITO";
        if (numero.startsWith("4")) return "VISA";
        if (numero.startsWith("5") || numero.startsWith("2")) return "MASTERCARD";
        if (numero.startsWith("34") || numero.startsWith("37")) return "AMEX";
        if (numero.startsWith("6")) return "DISCOVER / MAESTRO";
        return "CARTA DI CREDITO";
    }
}
