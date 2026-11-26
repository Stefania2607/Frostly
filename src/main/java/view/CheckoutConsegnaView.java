package view;

import bean.DatiConsegnaBean;
import bean.DatiConsegnaBean.TipoConsegna;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Finestra di checkout per scegliere tra consegna a domicilio e ritiro al banco
 * e inserire i relativi dati di recapito.
 */
public final class CheckoutConsegnaView {

    private CheckoutConsegnaView() {
    }

    public static void apriPopupSceltaConsegna(Window owner,
                                               double totaleOrdine,
                                               DatiConsegnaBean datiIniziali,
                                               Consumer<DatiConsegnaBean> onConsegnaConfermata) {
        apriPopupSceltaConsegna(owner, totaleOrdine, datiIniziali, onConsegnaConfermata, null);
    }

    public static void apriPopupSceltaConsegna(Window owner,
                                               double totaleOrdine,
                                               DatiConsegnaBean datiIniziali,
                                               Consumer<DatiConsegnaBean> onConsegnaConfermata,
                                               Runnable onTornaAlCarrello) {
        apriPopupSceltaConsegna(owner, totaleOrdine, datiIniziali, false, onConsegnaConfermata, onTornaAlCarrello);
    }

    public static void apriPopupSceltaConsegna(Window owner,
                                               double totaleOrdine,
                                               DatiConsegnaBean datiIniziali,
                                               boolean apriDirettamenteForm,
                                               Consumer<DatiConsegnaBean> onConsegnaConfermata,
                                               Runnable onTornaAlCarrello) {
        apriPopupSceltaConsegna(owner, totaleOrdine, datiIniziali, apriDirettamenteForm,
                null, onConsegnaConfermata, onTornaAlCarrello);
    }

    public static void apriPopupSceltaConsegna(Window owner,
                                               double totaleOrdine,
                                               DatiConsegnaBean datiIniziali,
                                               boolean apriDirettamenteForm,
                                               Consumer<DatiConsegnaBean> validatoreDati,
                                               Consumer<DatiConsegnaBean> onConsegnaConfermata,
                                               Runnable onTornaAlCarrello) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Modalità di Ricezione Ordine");
        if (owner != null) {
            popup.initOwner(owner);
        }

        final DatiConsegnaBean[] esito = new DatiConsegnaBean[1];
        final boolean[] tornaIndietro = {false};

        Consumer<DatiConsegnaBean> conferma = datiScelti -> {
            esito[0] = datiScelti;
            popup.close();
        };
        Runnable indietro = () -> {
            tornaIndietro[0] = true;
            popup.close();
        };

        if (apriDirettamenteForm && datiIniziali != null && datiIniziali.getTipoConsegna() != null) {
            mostraFormDati(
                    popup,
                    owner,
                    totaleOrdine,
                    datiIniziali.getTipoConsegna(),
                    datiIniziali,
                    validatoreDati,
                    conferma,
                    indietro
            );
        } else {
            mostraSchermataDueBottoni(
                    popup,
                    owner,
                    totaleOrdine,
                    datiIniziali,
                    validatoreDati,
                    conferma,
                    indietro
            );
        }

        popup.showAndWait();

        if (tornaIndietro[0] && onTornaAlCarrello != null) {
            javafx.application.Platform.runLater(onTornaAlCarrello);
        } else if (esito[0] != null && onConsegnaConfermata != null) {
            DatiConsegnaBean confermati = esito[0];
            javafx.application.Platform.runLater(() -> onConsegnaConfermata.accept(confermati));
        }
    }

    /**
     * Schermata 1: Due grandi tessere/bottoni affiancati per scegliere la modalità.
     */
    private static void mostraSchermataDueBottoni(Stage stage,
                                                  Window owner,
                                                  double totaleOrdine,
                                                  DatiConsegnaBean datiIniziali,
                                                  Consumer<DatiConsegnaBean> validatoreDati,
                                                  Consumer<DatiConsegnaBean> onConsegnaConfermata,
                                                  Runnable onTornaAlCarrello) {
        VBox root = new VBox(18);
        root.setStyle("-fx-padding: 26 30; -fx-background-color: rgba(18, 17, 20, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");
        root.setPrefWidth(680);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblBadge = new Label("✨");
        lblBadge.setStyle("-fx-font-size: 20px; -fx-background-color: rgba(186,164,135,0.15); "
                + "-fx-padding: 6 10; -fx-background-radius: 50; -fx-border-color: rgba(186,164,135,0.3);");

        VBox titleBox = new VBox(2);
        Label lblSub = new Label("STEP 1 DI 2 • MODALITÀ DI RICEZIONE");
        lblSub.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #baa487; -fx-letter-spacing: 0.8px;");
        Label lblTitolo = new Label("Come desideri ricevere il tuo ordine?");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #f5ebe0;");
        titleBox.getChildren().addAll(lblSub, lblTitolo);

        Region spH = new Region();
        HBox.setHgrow(spH, Priority.ALWAYS);

        Label lblTotale = new Label(String.format(Locale.US, "Totale: EUR %.2f", totaleOrdine));
        lblTotale.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #baa487;");

        header.getChildren().addAll(lblBadge, titleBox, spH, lblTotale);

        Label lblIstruzione = new Label("Clicca sulla modalità desiderata per proseguire:");
        lblIstruzione.setStyle("-fx-font-size: 14px; -fx-text-fill: #ded7cd;");

        HBox boxDueBottoni = new HBox(18);
        boxDueBottoni.setAlignment(Pos.CENTER);

        VBox cardDelivery = creaCardBottone(
                "🛵",
                "Consegna a Domicilio",
                "DELIVERY A CASA TUA",
                "Ricevi il gelato e i dolci caldi comodamente a casa con consegna in box termici brevettati.",
                () -> mostraFormDati(stage, owner, totaleOrdine, TipoConsegna.DOMICILIO, datiIniziali, validatoreDati, onConsegnaConfermata, onTornaAlCarrello)
        );
        HBox.setHgrow(cardDelivery, Priority.ALWAYS);

        VBox cardBanco = creaCardBottone(
                "🛍️",
                "Ritiro al Banco",
                "TAKE AWAY IN GELATERIA",
                "Salta la fila e ritira al banco del laboratorio di Via del Corso all'orario che preferisci.",
                () -> mostraFormDati(stage, owner, totaleOrdine, TipoConsegna.ASPORTO, datiIniziali, validatoreDati, onConsegnaConfermata, onTornaAlCarrello)
        );
        HBox.setHgrow(cardBanco, Priority.ALWAYS);

        boxDueBottoni.getChildren().addAll(cardDelivery, cardBanco);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Button btnIndietro = new Button("⬅ Torna al Carrello");
        btnIndietro.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; "
                + "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand;");
        btnIndietro.setOnAction(e -> {
            if (onTornaAlCarrello != null) {
                onTornaAlCarrello.run();
            } else {
                stage.close();
            }
        });

        footer.getChildren().add(btnIndietro);

        root.getChildren().addAll(header, lblIstruzione, boxDueBottoni, footer);
        applicaContenuto(stage, root, 700, 440);
    }

    private static void applicaContenuto(Stage stage, VBox root, double width, double height) {
        if (stage.getScene() == null) {
            javafx.scene.layout.StackPane contenitoreRadice = new javafx.scene.layout.StackPane(root);
            contenitoreRadice.setStyle("-fx-background-color: #121114;");
            Scene scene = new Scene(contenitoreRadice, width, height);
            scene.setFill(javafx.scene.paint.Color.web("#121114"));
            stage.setScene(scene);
            TemaProvenzaHelper.applicaSeGui2(stage.getScene());
        } else {
            javafx.scene.Parent currentRoot = stage.getScene().getRoot();
            if (currentRoot instanceof javafx.scene.layout.StackPane contenitoreRadice) {
                contenitoreRadice.getChildren().setAll(root);
            } else {
                stage.getScene().setRoot(root);
            }
            stage.setWidth(width);
            stage.setHeight(height + 28);
        }
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    private static VBox creaCardBottone(String emoji,
                                        String titolo,
                                        String badge,
                                        String descrizione,
                                        Runnable onClick) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: linear-gradient(to bottom, #26222b, #18151c); "
                + "-fx-border-color: rgba(186,164,135,0.30); -fx-border-radius: 14; -fx-background-radius: 14; "
                + "-fx-padding: 24 18; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0, 0, 2);");

        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 34px;");

        Label lblBadge = new Label(badge);
        lblBadge.setStyle("-fx-background-color: rgba(186,164,135,0.18); -fx-text-fill: #baa487; -fx-font-weight: bold; "
                + "-fx-font-size: 11.5px; -fx-padding: 3 8; -fx-background-radius: 6;");

        Label lblTit = new Label(titolo);
        lblTit.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #f5ebe0;");

        Label lblDesc = new Label(descrizione);
        lblDesc.setWrapText(true);
        lblDesc.setAlignment(Pos.CENTER);
        lblDesc.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #a89f92; -fx-line-spacing: 2px;");

        Button btnAzione = new Button("Scegli " + titolo + " ➔");
        btnAzione.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");

        card.getChildren().addAll(lblEmoji, lblBadge, lblTit, lblDesc, btnAzione);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: linear-gradient(to bottom, #2e2935, #1d1922); "
                + "-fx-border-color: #baa487; -fx-border-radius: 14; -fx-background-radius: 14; "
                + "-fx-padding: 24 18; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(186,164,135,0.3), 12, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: linear-gradient(to bottom, #26222b, #18151c); "
                + "-fx-border-color: rgba(186,164,135,0.30); -fx-border-radius: 14; -fx-background-radius: 14; "
                + "-fx-padding: 24 18; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0, 0, 2);"));

        card.setOnMouseClicked(e -> onClick.run());
        btnAzione.setOnAction(e -> onClick.run());

        return card;
    }

    /**
     * Schermata 2: Form per inserire i dati in base alla modalità scelta.
     */
    private static void mostraFormDati(Stage stage,
                                       Window owner,
                                       double totaleOrdine,
                                       TipoConsegna tipoScelto,
                                       DatiConsegnaBean datiIniziali,
                                       Consumer<DatiConsegnaBean> validatoreDati,
                                       Consumer<DatiConsegnaBean> onConsegnaConfermata,
                                       Runnable onTornaAlCarrello) {
        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 26 30; -fx-background-color: rgba(18, 17, 20, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");
        root.setPrefWidth(680);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblBadge = new Label(tipoScelto == TipoConsegna.DOMICILIO ? "🛵" : "🛍️");
        lblBadge.setStyle("-fx-font-size: 20px; -fx-background-color: rgba(186,164,135,0.15); "
                + "-fx-padding: 6 10; -fx-background-radius: 50; -fx-border-color: rgba(186,164,135,0.3);");

        VBox titleBox = new VBox(2);
        Label lblSub = new Label("STEP 2 DI 2 • COMPILAZIONE DATI");
        lblSub.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #baa487; -fx-letter-spacing: 0.8px;");

        String titStr = (tipoScelto == TipoConsegna.DOMICILIO)
                ? "Dati di Recapito per la Consegna a Domicilio"
                : "Dettagli per il Ritiro in Gelateria al Banco";
        Label lblTitolo = new Label(titStr);
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #f5ebe0;");
        titleBox.getChildren().addAll(lblSub, lblTitolo);

        Region spH = new Region();
        HBox.setHgrow(spH, Priority.ALWAYS);

        Label lblTotale = new Label(String.format(Locale.US, "Totale: EUR %.2f", totaleOrdine));
        lblTotale.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #baa487;");

        header.getChildren().addAll(lblBadge, titleBox, spH, lblTotale);

        VBox cardForm = new VBox(10);
        cardForm.setStyle("-fx-background-color: rgba(26,24,28,0.92); -fx-border-color: rgba(186,164,135,0.25); "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 16 18;");

        TextField txtIndirizzo = new TextField(datiIniziali != null ? datiIniziali.getIndirizzo() : "");
        TextField txtCittaCap = new TextField(datiIniziali != null && !datiIniziali.getCittaCap().isBlank() ? datiIniziali.getCittaCap() : "Roma (RM)");
        TextField txtTelefono = new TextField(datiIniziali != null ? datiIniziali.getTelefono() : "");
        TextField txtCitofono = new TextField(datiIniziali != null ? datiIniziali.getNoteCitofono() : "");
        ComboBox<String> comboOraDelivery = new ComboBox<>(FXCollections.observableArrayList(
                "Subito (~25-35 min)",
                "Oggi 16:30", "Oggi 17:00", "Oggi 17:30", "Oggi 18:00",
                "Oggi 18:30", "Oggi 19:00", "Oggi 19:30", "Oggi 20:00",
                "Oggi 20:30", "Oggi 21:00", "Oggi 21:30", "Oggi 22:00",
                "Domani 13:00", "Domani 18:00", "Domani 20:00"
        ));
        comboOraDelivery.setValue(datiIniziali != null ? datiIniziali.getOrarioRichiesto() : "Subito (~25-35 min)");

        TextField txtNomeRitiro = new TextField(datiIniziali != null ? datiIniziali.getNomeRitiro() : "");
        TextField txtCognomeRitiro = new TextField(datiIniziali != null ? datiIniziali.getCognomeRitiro() : "");
        TextField txtTelefonoBanco = new TextField(datiIniziali != null ? datiIniziali.getTelefono() : "");
        TextField txtNoteBanco = new TextField(datiIniziali != null ? datiIniziali.getNoteCitofono() : "");
        ComboBox<String> comboOraBanco = new ComboBox<>(FXCollections.observableArrayList(
                "Subito (~15-20 min)",
                "Oggi 16:30", "Oggi 17:00", "Oggi 17:30", "Oggi 18:00",
                "Oggi 18:30", "Oggi 19:00", "Oggi 19:30", "Oggi 20:00",
                "Oggi 20:30", "Oggi 21:00", "Oggi 21:30", "Oggi 22:00",
                "Domani 12:30", "Domani 17:30", "Domani 19:30"
        ));
        comboOraBanco.setValue(datiIniziali != null ? datiIniziali.getOrarioRichiesto() : "Subito (~15-20 min)");

        if (tipoScelto == TipoConsegna.DOMICILIO) {
            Label lblIndPrompt = new Label("📍 Indirizzo di consegna e numero civico *");
            lblIndPrompt.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");
            txtIndirizzo.setPromptText("Es. Via del Corso 124, interno 3");
            txtIndirizzo.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");

            HBox rigaCittaTel = new HBox(12);
            VBox bCitta = new VBox(4);
            Label lblCitta = new Label("Città / CAP");
            lblCitta.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd;");
            txtCittaCap.setPrefWidth(220);
            txtCittaCap.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
            bCitta.getChildren().addAll(lblCitta, txtCittaCap);

            VBox bTel = new VBox(4);
            Label lblTel = new Label("Recapito Telefonico per il Rider *");
            lblTel.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
            txtTelefono.setPromptText("Es. 333 1234567");
            HBox.setHgrow(bTel, Priority.ALWAYS);
            txtTelefono.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
            bTel.getChildren().addAll(lblTel, txtTelefono);

            rigaCittaTel.getChildren().addAll(bCitta, bTel);

            HBox rigaOraCit = new HBox(12);
            VBox bOra = new VBox(4);
            Label lblOra = new Label("Orario Consegna Desiderato");
            lblOra.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd;");
            comboOraDelivery.setPrefWidth(220);
            bOra.getChildren().addAll(lblOra, comboOraDelivery);

            VBox bCit = new VBox(4);
            Label lblCit = new Label("Istruzioni Consegna / Citofono (Facoltativo)");
            lblCit.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd;");
            txtCitofono.setPromptText("Citofono, scala, piano o note per il fattorino...");
            HBox.setHgrow(bCit, Priority.ALWAYS);
            txtCitofono.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
            bCit.getChildren().addAll(lblCit, txtCitofono);

            rigaOraCit.getChildren().addAll(bOra, bCit);
            cardForm.getChildren().addAll(lblIndPrompt, txtIndirizzo, rigaCittaTel, rigaOraCit);
        } else {
            String stileCampo = "-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;";

            Label lblOraTit = new Label("⏰ Seleziona l'Orario di Ritiro al Banco della Gelateria *");
            lblOraTit.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");
            comboOraBanco.setMaxWidth(Double.MAX_VALUE);

            HBox rigaNomeCognome = new HBox(12);
            VBox boxNome = new VBox(4);
            Label lblNomeTit = new Label("👤 Nome di chi ritira *");
            lblNomeTit.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
            txtNomeRitiro.setPromptText("Es. Mario");
            txtNomeRitiro.setStyle(stileCampo);
            HBox.setHgrow(boxNome, Priority.ALWAYS);
            boxNome.getChildren().addAll(lblNomeTit, txtNomeRitiro);

            VBox boxCognome = new VBox(4);
            Label lblCognomeTit = new Label("👤 Cognome di chi ritira *");
            lblCognomeTit.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
            txtCognomeRitiro.setPromptText("Es. Rossi");
            txtCognomeRitiro.setStyle(stileCampo);
            HBox.setHgrow(boxCognome, Priority.ALWAYS);
            boxCognome.getChildren().addAll(lblCognomeTit, txtCognomeRitiro);
            rigaNomeCognome.getChildren().addAll(boxNome, boxCognome);

            Label lblTelBanco = new Label("📞 Recapito Telefonico *");
            lblTelBanco.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
            txtTelefonoBanco.setPromptText("Es. 333 1234567");
            txtTelefonoBanco.setStyle(stileCampo);

            Label lblNoteTit = new Label("💬 Note per il Personale al Banco (Facoltativo)");
            lblNoteTit.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd;");
            txtNoteBanco.setPromptText("Es. Confezionare con cura, vaschette separate...");
            txtNoteBanco.setStyle(stileCampo);

            cardForm.getChildren().addAll(lblOraTit, comboOraBanco, rigaNomeCognome, lblTelBanco, txtTelefonoBanco, lblNoteTit, txtNoteBanco);
        }

        Label lblErrore = new Label();
        lblErrore.setWrapText(true);
        lblErrore.setVisible(false);
        lblErrore.setStyle("-fx-text-fill: #ff8a80; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 10; "
                + "-fx-background-color: rgba(188,71,73,0.22); -fx-border-color: rgba(255,138,128,0.4); -fx-border-radius: 6; -fx-background-radius: 6;");

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnCambiaModalita = new Button("⬅ Cambia Modalità");
        btnCambiaModalita.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; "
                + "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;");
        btnCambiaModalita.setOnAction(e -> mostraSchermataDueBottoni(stage, owner, totaleOrdine, datiIniziali, validatoreDati, onConsegnaConfermata, onTornaAlCarrello));

        Button btnProcediPagamento = new Button("💳 Procedi al Pagamento Online ➔");
        btnProcediPagamento.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 9 22; -fx-cursor: hand;");

        btnProcediPagamento.setOnAction(e -> {
            lblErrore.setVisible(false);
            DatiConsegnaBean bean = new DatiConsegnaBean();
            if (tipoScelto == TipoConsegna.DOMICILIO) {
                bean.setTipoConsegna(TipoConsegna.DOMICILIO);
                bean.setIndirizzo(txtIndirizzo.getText() != null ? txtIndirizzo.getText().trim() : "");
                bean.setCittaCap(txtCittaCap.getText() != null ? txtCittaCap.getText().trim() : "Roma (RM)");
                bean.setTelefono(txtTelefono.getText() != null ? txtTelefono.getText().trim() : "");
                bean.setOrarioRichiesto(comboOraDelivery.getValue());
                bean.setNoteCitofono(txtCitofono.getText() != null ? txtCitofono.getText().trim() : "");
            } else {
                bean.setTipoConsegna(TipoConsegna.ASPORTO);
                bean.setOrarioRichiesto(comboOraBanco.getValue());
                bean.setNomeRitiro(txtNomeRitiro.getText() != null ? txtNomeRitiro.getText().trim() : "");
                bean.setCognomeRitiro(txtCognomeRitiro.getText() != null ? txtCognomeRitiro.getText().trim() : "");
                bean.setTelefono(txtTelefonoBanco.getText() != null ? txtTelefonoBanco.getText().trim() : "");
                bean.setNoteCitofono(txtNoteBanco.getText() != null ? txtNoteBanco.getText().trim() : "");
            }

            if (validatoreDati != null) {
                try {
                    validatoreDati.accept(bean);
                } catch (RuntimeException ex) {
                    lblErrore.setText("⚠️ " + ex.getMessage());
                    lblErrore.setVisible(true);
                    return;
                }
            }

            if (onConsegnaConfermata != null) {
                onConsegnaConfermata.accept(bean);
            } else {
                stage.close();
            }
        });

        footer.getChildren().addAll(btnCambiaModalita, btnProcediPagamento);
        root.getChildren().addAll(header, cardForm, lblErrore, footer);
        applicaContenuto(stage, root, 700, 540);
    }
}
