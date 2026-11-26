package view;

import bean.RichiestaIndisponibilitaBean;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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
 * Finestre dedicate alle richieste di permesso / indisponibilita' (lato Dipendente e lato Maestro).
 */
public final class RichiesteIndisponibilitaView {

    private static final DateTimeFormatter FMT_IT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FunctionalInterface
    public interface ValutazioneCallback {
        void valuta(String codiceRichiesta, boolean approva, String notaMaestro);
    }

    private RichiesteIndisponibilitaView() {
    }

    /**
     * Lato Dipendente: invio di una nuova richiesta di indisponibilita' per una data specifica
     * (in attesa di approvazione del Maestro) e consultazione dello storico personale.
     */
    public static void apriDialogDipendente(Window owner,
                                            List<RichiestaIndisponibilitaBean> richiestePersonali,
                                            BiConsumer<LocalDate, String> onInviaRichiesta) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Richiedi Permesso / Indisponibilità");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setPrefWidth(560);
        root.setStyle("-fx-padding: 18 22; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        Label lblTit = new Label("🗓️ Richiedi Permesso / Giorno di Indisponibilità");
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Label lblSub = new Label("Indica il giorno in cui non puoi lavorare. La richiesta verrà inviata al Maestro e bloccherà i turni solo dopo approvazione.");
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92;");

        VBox form = new VBox(8);
        form.setStyle("-fx-background-color: rgba(26,24,30,0.92); -fx-border-color: rgba(186,164,135,0.28); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 14;");

        HBox rigaData = new HBox(10);
        rigaData.setAlignment(Pos.CENTER_LEFT);
        Label lData = new Label("Data richiesta:");
        lData.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #f5ebe0;");
        DatePicker dpData = new DatePicker(LocalDate.now().plusDays(1));
        dpData.setPrefWidth(155);
        rigaData.getChildren().addAll(lData, dpData);

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Motivazione (es. visita medica, esame universitario, impegno familiare)...");

        Label lblErr = new Label();
        lblErr.setStyle("-fx-text-fill: #fca5a5; -fx-font-weight: bold; -fx-font-size: 11.5px;");

        Button btnInvia = new Button("📨 Invia Richiesta al Maestro");
        btnInvia.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                + "-fx-background-radius: 10; -fx-padding: 7 16; -fx-cursor: hand;");
        btnInvia.setOnAction(e -> {
            if (dpData.getValue() == null) {
                lblErr.setText("⚠️ Seleziona una data valida.");
                return;
            }
            if (txtMotivo.getText() == null || txtMotivo.getText().trim().isEmpty()) {
                lblErr.setText("⚠️ Inserisci una breve motivazione.");
                return;
            }
            LocalDate dataSel = dpData.getValue();
            String motivoSel = txtMotivo.getText().trim();
            popup.close();
            if (onInviaRichiesta != null) {
                onInviaRichiesta.accept(dataSel, motivoSel);
            }
        });

        form.getChildren().addAll(rigaData, txtMotivo, lblErr, btnInvia);

        Label lblStorico = new Label("📋 Le tue richieste inviate:");
        lblStorico.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");

        VBox boxLista = new VBox(7);
        if (richiestePersonali == null || richiestePersonali.isEmpty()) {
            Label vuoto = new Label("Nessuna richiesta di permesso inviata.");
            vuoto.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-text-fill: #a89f92;");
            boxLista.getChildren().add(vuoto);
        } else {
            for (RichiestaIndisponibilitaBean r : richiestePersonali) {
                boxLista.getChildren().add(creaCardStato(r));
            }
        }

        ScrollPane scroll = new ScrollPane(boxLista);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefHeight(180);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.4); "
                + "-fx-border-radius: 10; -fx-padding: 6 16; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());
        footer.getChildren().add(btnChiudi);

        root.getChildren().addAll(lblTit, lblSub, form, lblStorico, scroll, footer);
        Scene scene = new Scene(root, 580, 500);
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(scene);
        popup.centerOnScreen();
        popup.showAndWait();
    }

    /**
     * Lato Maestro: visualizza tutte le richieste di permesso/indisponibilita' inviate dallo staff
     * e permette di approvarle (bloccando automaticamente quel giorno nei turni) o rifiutarle.
     */
    public static void apriDialogMaestro(Window owner,
                                         List<RichiestaIndisponibilitaBean> tutteRichieste,
                                         ValutazioneCallback onValuta) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Richieste Permessi & Indisponibilità Staff");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setPrefWidth(680);
        root.setStyle("-fx-padding: 18 22; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        Label lblTit = new Label("📋 Richieste di Permesso & Indisponibilità Staff");
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Label lblSub = new Label("Solo le richieste approvate vengono registrate nelle ferie/permessi del dipendente ed escluse dalla generazione automatica.");
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92;");

        VBox boxLista = new VBox(9);
        if (tutteRichieste == null || tutteRichieste.isEmpty()) {
            Label vuoto = new Label("Nessuna richiesta di permesso o indisponibilità presente.");
            vuoto.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-text-fill: #a89f92; -fx-padding: 16 0;");
            boxLista.getChildren().add(vuoto);
        } else {
            for (RichiestaIndisponibilitaBean r : tutteRichieste) {
                VBox card = creaCardStato(r);
                if ("IN_ATTESA".equalsIgnoreCase(r.getStato())) {
                    TextField txtNota = new TextField();
                    txtNota.setPromptText("Nota opzionale per il dipendente...");
                    HBox.setHgrow(txtNota, Priority.ALWAYS);

                    Button btnRifiuta = new Button("✕ Rifiuta");
                    btnRifiuta.setMinWidth(Region.USE_PREF_SIZE);
                    btnRifiuta.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; "
                            + "-fx-font-size: 12px; -fx-padding: 5 12; -fx-background-radius: 8; -fx-cursor: hand;");
                    btnRifiuta.setOnAction(e -> {
                        popup.close();
                        if (onValuta != null) {
                            onValuta.valuta(r.getCodiceRichiesta(), false, txtNota.getText());
                        }
                    });

                    Button btnApprova = new Button("✔ Approva");
                    btnApprova.setMinWidth(Region.USE_PREF_SIZE);
                    btnApprova.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; "
                            + "-fx-font-size: 12px; -fx-padding: 5 12; -fx-background-radius: 8; -fx-cursor: hand;");
                    btnApprova.setOnAction(e -> {
                        popup.close();
                        if (onValuta != null) {
                            onValuta.valuta(r.getCodiceRichiesta(), true, txtNota.getText());
                        }
                    });

                    HBox rigaAzioni = new HBox(8, txtNota, btnRifiuta, btnApprova);
                    rigaAzioni.setAlignment(Pos.CENTER_LEFT);
                    card.getChildren().add(rigaAzioni);
                }
                boxLista.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(boxLista);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefHeight(360);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.4); "
                + "-fx-border-radius: 10; -fx-padding: 6 16; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());
        footer.getChildren().add(btnChiudi);

        root.getChildren().addAll(lblTit, lblSub, scroll, footer);
        Scene scene = new Scene(root, 700, 520);
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(scene);
        popup.centerOnScreen();
        popup.showAndWait();
    }

    private static VBox creaCardStato(RichiestaIndisponibilitaBean r) {
        VBox card = new VBox(5);
        String badgeText;
        String badgeBg;
        String badgeColor;
        if ("APPROVATA".equalsIgnoreCase(r.getStato())) {
            badgeText = "✔ Approvata";
            badgeBg = "#e6f4ea";
            badgeColor = "#1e6b3a";
        } else if ("RIFIUTATA".equalsIgnoreCase(r.getStato())) {
            badgeText = "✕ Rifiutata";
            badgeBg = "#fde8e8";
            badgeColor = "#b91c1c";
        } else {
            badgeText = "⏳ In attesa";
            badgeBg = "#fef3c7";
            badgeColor = "#9a5b13";
        }

        card.setStyle("-fx-background-color: rgba(26,24,30,0.92); -fx-border-color: rgba(186,164,135,0.28); "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 12;");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label lblInfo = new Label("👤 " + r.getNomeDipendente() + "  •  📅 " + r.getDataRichiesta().format(FMT_IT)
                + "  [" + r.getCodiceRichiesta() + "]");
        lblInfo.setWrapText(true);
        lblInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #f5ebe0;");
        HBox.setHgrow(lblInfo, Priority.ALWAYS);

        Label badge = new Label(badgeText);
        badge.setMinWidth(Region.USE_PREF_SIZE);
        badge.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeColor
                + "; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 9; -fx-background-radius: 10;");
        top.getChildren().addAll(lblInfo, badge);

        Label lblMotivo = new Label("Motivo: " + r.getMotivazione());
        lblMotivo.setWrapText(true);
        lblMotivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #ded7cd;");

        card.getChildren().addAll(top, lblMotivo);
        if (r.getNotaMaestro() != null && !r.getNotaMaestro().isBlank()) {
            Label lblNota = new Label("Risposta Maestro: " + r.getNotaMaestro());
            lblNota.setWrapText(true);
            lblNota.setStyle("-fx-font-size: 11.5px; -fx-font-style: italic; -fx-text-fill: #baa487;");
            card.getChildren().add(lblNota);
        }
        return card;
    }
}
