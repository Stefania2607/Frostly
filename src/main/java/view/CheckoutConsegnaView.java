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
 * View dedicata alla scelta della modalità di consegna e compilazione dei relativi dati:
 * - Step 1: Due grandi pulsanti/card affiancate ("🛵 Consegna a Domicilio" vs "🛍️ Ritiro al Banco").
 * - Step 2: Form specifico con i dati del delivery (indirizzo, telefono rider, note) oppure del ritiro (orario banco, nome).
 */
public final class CheckoutConsegnaView {

    private CheckoutConsegnaView() {
        // Utility View
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
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Modalità di Ricezione Ordine");
        if (owner != null) {
            popup.initOwner(owner);
        }

        final DatiConsegnaBean[] esito = new DatiConsegnaBean[1];
        final boolean[] tornaIndietro = {false};

        // Mostra prima la schermata dei due bottoni affiancati
        mostraSchermataDueBottoni(
                popup,
                owner,
                totaleOrdine,
                datiIniziali,
                datiScelti -> {
                    esito[0] = datiScelti;
                    popup.close();
                },
                () -> {
                    tornaIndietro[0] = true;
                    popup.close();
                }
        );

        popup.showAndWait();

        if (tornaIndietro[0] && onTornaAlCarrello != null) {
            onTornaAlCarrello.run();
        } else if (esito[0] != null && onConsegnaConfermata != null) {
            onConsegnaConfermata.accept(esito[0]);
        }
    }

    /**
     * Schermata 1: Due grandi tessere/bottoni affiancati per scegliere la modalità.
     */
    private static void mostraSchermataDueBottoni(Stage stage,
                                                  Window owner,
                                                  double totaleOrdine,
                                                  DatiConsegnaBean datiIniziali,
                                                  Consumer<DatiConsegnaBean> onConsegnaConfermata,
                                                  Runnable onTornaAlCarrello) {
        VBox root = new VBox(18);
        root.setStyle("-fx-padding: 26 30; -fx-background-color: rgba(18, 17, 20, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");
        root.setPrefWidth(680);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblBadge = new Label("✨");
        lblBadge.setStyle("-fx-font-size: 20px; -fx-background-color: rgba(186,164,135,0.15); "
                + "-fx-padding: 6 10; -fx-background-radius: 50; -fx-border-color: rgba(186,164,135,0.3);");

        VBox titleBox = new VBox(2);
        Label lblSub = new Label("STEP 1 DI 2 • MODALITÀ DI RICEZIONE");
        lblSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #baa487; -fx-letter-spacing: 0.8px;");
        Label lblTitolo = new Label("Come desideri ricevere il tuo ordine?");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #f5ebe0;");
        titleBox.getChildren().addAll(lblSub, lblTitolo);

        Region spH = new Region();
        HBox.setHgrow(spH, Priority.ALWAYS);

        Label lblTotale = new Label(String.format(Locale.US, "Totale: EUR %.2f", totaleOrdine));
        lblTotale.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #baa487;");

        header.getChildren().addAll(lblBadge, titleBox, spH, lblTotale);

        Label lblIstruzione = new Label("Clicca sulla modalità desiderata per proseguire:");
        lblIstruzione.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd;");

        // Due Bottoni Affiancati (HBox)
        HBox boxDueBottoni = new HBox(18);
        boxDueBottoni.setAlignment(Pos.CENTER);

        // Bottone Sinistro: Consegna a Domicilio
        VBox cardDelivery = creaCardBottone(
                "🛵",
                "Consegna a Domicilio",
                "DELIVERY A CASA TUA",
                "Ricevi il gelato e i dolci caldi comodamente a casa con consegna in box termici brevettati.",
                () -> mostraFormDati(stage, owner, totaleOrdine, TipoConsegna.DOMICILIO, datiIniziali, onConsegnaConfermata, onTornaAlCarrello)
        );
        HBox.setHgrow(cardDelivery, Priority.ALWAYS);

        // Bottone Destro: Ritiro al Banco
        VBox cardBanco = creaCardBottone(
                "🛍️",
                "Ritiro al Banco",
                "TAKE AWAY IN GELATERIA",
                "Salta la fila e ritira al banco del laboratorio di Via del Corso all'orario che preferisci.",
                () -> mostraFormDati(stage, owner, totaleOrdine, TipoConsegna.ASPORTO, datiIniziali, onConsegnaConfermata, onTornaAlCarrello)
        );
        HBox.setHgrow(cardBanco, Priority.ALWAYS);

        boxDueBottoni.getChildren().addAll(cardDelivery, cardBanco);

        // Footer con tasto Indietro al carrello
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Button btnIndietro = new Button("⬅ Torna al Carrello");
        btnIndietro.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; "
                + "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 18; -fx-cursor: hand;");
        btnIndietro.setOnAction(e -> {
            stage.close();
            if (onTornaAlCarrello != null) {
                onTornaAlCarrello.run();
            }
        });

        footer.getChildren().add(btnIndietro);

        root.getChildren().addAll(header, lblIstruzione, boxDueBottoni, footer);
        Scene scene = new Scene(root, 700, 440);
        scene.setFill(javafx.scene.paint.Color.web("#121114"));
        stage.setScene(scene);
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
                + "-fx-font-size: 9.5px; -fx-padding: 3 8; -fx-background-radius: 6;");

        Label lblTit = new Label(titolo);
        lblTit.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #f5ebe0;");

        Label lblDesc = new Label(descrizione);
        lblDesc.setWrapText(true);
        lblDesc.setAlignment(Pos.CENTER);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #a89f92; -fx-line-spacing: 2px;");

        Button btnAzione = new Button("Scegli " + titolo + " ➔");
        btnAzione.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");

        card.getChildren().addAll(lblEmoji, lblBadge, lblTit, lblDesc, btnAzione);

        // Effetti Hover
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
                                       Consumer<DatiConsegnaBean> onConsegnaConfermata,
                                       Runnable onTornaAlCarrello) {
        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 26 30; -fx-background-color: rgba(18, 17, 20, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");
        root.setPrefWidth(680);

        // Header Form
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblBadge = new Label(tipoScelto == TipoConsegna.DOMICILIO ? "🛵" : "🛍️");
        lblBadge.setStyle("-fx-font-size: 20px; -fx-background-color: rgba(186,164,135,0.15); "
                + "-fx-padding: 6 10; -fx-background-radius: 50; -fx-border-color: rgba(186,164,135,0.3);");

        VBox titleBox = new VBox(2);
        Label lblSub = new Label("STEP 2 DI 2 • COMPILAZIONE DATI");
        lblSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #baa487; -fx-letter-spacing: 0.8px;");

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

        // Area Form specifica
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

        TextField txtNomeRitiro = new TextField(datiIniziali != null ? datiIniziali.getNominativoRitiro() : "");
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
            lblIndPrompt.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #baa487;");
            txtIndirizzo.setPromptText("Es. Via del Corso 124, interno 3");
            txtIndirizzo.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");

            HBox rigaCittaTel = new HBox(12);
            VBox bCitta = new VBox(4);
            Label lblCitta = new Label("Città / CAP");
            lblCitta.setStyle("-fx-font-size: 11px; -fx-text-fill: #ded7cd;");
            txtCittaCap.setPrefWidth(220);
            txtCittaCap.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
            bCitta.getChildren().addAll(lblCitta, txtCittaCap);

            VBox bTel = new VBox(4);
            Label lblTel = new Label("Recapito Telefonico per il Rider *");
            lblTel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
            txtTelefono.setPromptText("Es. 333 1234567");
            HBox.setHgrow(bTel, Priority.ALWAYS);
            txtTelefono.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
            bTel.getChildren().addAll(lblTel, txtTelefono);

            rigaCittaTel.getChildren().addAll(bCitta, bTel);

            HBox rigaOraCit = new HBox(12);
            VBox bOra = new VBox(4);
            Label lblOra = new Label("Orario Consegna Desiderato");
            lblOra.setStyle("-fx-font-size: 11px; -fx-text-fill: #ded7cd;");
            comboOraDelivery.setPrefWidth(220);
            bOra.getChildren().addAll(lblOra, comboOraDelivery);

            VBox bCit = new VBox(4);
            Label lblCit = new Label("Istruzioni Consegna / Citofono (Facoltativo)");
            lblCit.setStyle("-fx-font-size: 11px; -fx-text-fill: #ded7cd;");
            txtCitofono.setPromptText("Citofono, scala, piano o note per il fattorino...");
            HBox.setHgrow(bCit, Priority.ALWAYS);
            txtCitofono.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");
            bCit.getChildren().addAll(lblCit, txtCitofono);

            rigaOraCit.getChildren().addAll(bOra, bCit);
            cardForm.getChildren().addAll(lblIndPrompt, txtIndirizzo, rigaCittaTel, rigaOraCit);
        } else {
            Label lblOraTit = new Label("⏰ Seleziona l'Orario di Ritiro al Banco della Gelateria *");
            lblOraTit.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #baa487;");
            comboOraBanco.setMaxWidth(Double.MAX_VALUE);

            Label lblNomeTit = new Label("👤 Nominativo per il Ritiro");
            lblNomeTit.setStyle("-fx-font-size: 11px; -fx-text-fill: #ded7cd;");
            txtNomeRitiro.setPromptText("Nome e cognome per il ritiro dell'ordine al banco (es. Mario Rossi)");
            txtNomeRitiro.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");

            Label lblNoteTit = new Label("💬 Note per il Personale al Banco (Facoltativo)");
            lblNoteTit.setStyle("-fx-font-size: 11px; -fx-text-fill: #ded7cd;");
            txtNoteBanco.setPromptText("Es. Confezionare con cura, vaschette separate...");
            txtNoteBanco.setStyle("-fx-background-color: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");

            cardForm.getChildren().addAll(lblOraTit, comboOraBanco, lblNomeTit, txtNomeRitiro, lblNoteTit, txtNoteBanco);
        }

        // Messaggio di Errore
        Label lblErrore = new Label();
        lblErrore.setWrapText(true);
        lblErrore.setVisible(false);
        lblErrore.setStyle("-fx-text-fill: #ff8a80; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-padding: 6 10; "
                + "-fx-background-color: rgba(188,71,73,0.22); -fx-border-color: rgba(255,138,128,0.4); -fx-border-radius: 6; -fx-background-radius: 6;");

        // Footer Pulsanti
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnCambiaModalita = new Button("⬅ Cambia Modalità");
        btnCambiaModalita.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; "
                + "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;");
        btnCambiaModalita.setOnAction(e -> mostraSchermataDueBottoni(stage, owner, totaleOrdine, datiIniziali, onConsegnaConfermata, onTornaAlCarrello));

        Button btnProcediPagamento = new Button("💳 Procedi al Pagamento Online ➔");
        btnProcediPagamento.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-background-radius: 10; -fx-padding: 9 22; -fx-cursor: hand;");

        btnProcediPagamento.setOnAction(e -> {
            lblErrore.setVisible(false);
            DatiConsegnaBean bean = new DatiConsegnaBean();
            if (tipoScelto == TipoConsegna.DOMICILIO) {
                bean.setTipoConsegna(TipoConsegna.DOMICILIO);
                String ind = txtIndirizzo.getText() != null ? txtIndirizzo.getText().trim() : "";
                String tel = txtTelefono.getText() != null ? txtTelefono.getText().trim() : "";
                if (ind.length() < 5) {
                    lblErrore.setText("⚠️ Inserisci un indirizzo valido completo di via e numero civico.");
                    lblErrore.setVisible(true);
                    return;
                }
                if (tel.replaceAll("[^0-9]", "").length() < 6) {
                    lblErrore.setText("⚠️ Inserisci un recapito telefonico valido per il rider.");
                    lblErrore.setVisible(true);
                    return;
                }
                bean.setIndirizzo(ind);
                bean.setCittaCap(txtCittaCap.getText() != null ? txtCittaCap.getText().trim() : "Roma (RM)");
                bean.setTelefono(tel);
                bean.setOrarioRichiesto(comboOraDelivery.getValue());
                bean.setNoteCitofono(txtCitofono.getText() != null ? txtCitofono.getText().trim() : "");
            } else {
                bean.setTipoConsegna(TipoConsegna.ASPORTO);
                bean.setOrarioRichiesto(comboOraBanco.getValue());
                bean.setNominativoRitiro(txtNomeRitiro.getText() != null ? txtNomeRitiro.getText().trim() : "");
                bean.setNoteCitofono(txtNoteBanco.getText() != null ? txtNoteBanco.getText().trim() : "");
            }

            stage.close();
            if (onConsegnaConfermata != null) {
                onConsegnaConfermata.accept(bean);
            }
        });

        footer.getChildren().addAll(btnCambiaModalita, btnProcediPagamento);
        root.getChildren().addAll(header, cardForm, lblErrore, footer);
        Scene scene = new Scene(root, 700, 540);
        scene.setFill(javafx.scene.paint.Color.web("#121114"));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
    }
}
