package view;

import bean.EmergenzaBean;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Componente View dedicato al popup di gestione emergenze e al pulsante a tendina urgenze.
 */
public final class EmergenzeStaffView {

    private EmergenzeStaffView() {
    }

    public static void aggiornaBottoneUrgenze(Button btnTendinaUrgenze, int totaleUrgenze) {
        if (btnTendinaUrgenze == null) return;
        if (totaleUrgenze > 0) {
            btnTendinaUrgenze.setText("🔔 Urgenze (" + totaleUrgenze + ")");
            btnTendinaUrgenze.setStyle("-fx-background-color: rgba(188, 71, 73, 0.85); -fx-text-fill: #fffdfa; -fx-font-weight: bold; -fx-background-radius: 14; -fx-border-color: rgba(255, 138, 128, 0.5); -fx-border-radius: 14; -fx-padding: 7 15; -fx-cursor: hand;");
        } else {
            btnTendinaUrgenze.setText("🔔 Notizie (0)");
            btnTendinaUrgenze.setStyle("-fx-background-color: rgba(186, 164, 135, 0.18); -fx-text-fill: #baa487; -fx-font-weight: bold; -fx-background-radius: 14; -fx-border-color: rgba(186, 164, 135, 0.35); -fx-border-radius: 14; -fx-padding: 7 15; -fx-cursor: hand;");
        }
    }

    public static void apriPopupDettaglioEmergenza(Window owner,
                                                   EmergenzaBean e,
                                                   List<String> emailTuttiDipendenti,
                                                   BiConsumer<String, String> onApprova,
                                                   Consumer<String> onRifiuta) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Gestione Emergenza " + e.getCodiceEmergenza());
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 24 28; -fx-background-color: #141216; -fx-border-color: rgba(186, 164, 135, 0.4); -fx-border-width: 1;");
        root.setPrefWidth(540);

        Label lblTitolo = new Label("🚨 RICHIESTA DI SOSTITUZIONE URGENTE");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #ff8a80;");
        Label lblSub = new Label("Codice: " + e.getCodiceEmergenza() + " • Turno: " + e.getCodiceTurno());
        lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #baa487;");

        VBox cardInfo = new VBox(6);
        cardInfo.setStyle("-fx-padding: 12 16; -fx-background-color: rgba(28, 25, 33, 0.85); -fx-background-radius: 10; -fx-border-color: rgba(186, 164, 135, 0.25); -fx-border-radius: 10;");
        Label lblDip = new Label("👤 Dipendente: " + e.getNomeRichiedente() + " (" + e.getEmailRichiedente() + ")");
        lblDip.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f5ebe0;");
        String turnoDettagli = e.getDataTurno() != null ? (e.getDataTurno() + " ore " + e.getOrarioTurno()) : "Turno di lavoro programmato";
        Label lblTurno = new Label("📅 Turno coinvolto: " + e.getCodiceTurno() + " • " + turnoDettagli);
        lblTurno.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #ded7cd;");
        cardInfo.getChildren().addAll(lblDip, lblTurno);

        VBox cardMotivo = new VBox(6);
        cardMotivo.setStyle("-fx-padding: 12 16; -fx-background-color: rgba(24, 21, 28, 0.95); -fx-background-radius: 10; -fx-border-color: rgba(198, 40, 40, 0.35); -fx-border-radius: 10;");
        Label lblMotivoTitolo = new Label("💬 Motivo comunicato dal dipendente:");
        lblMotivoTitolo.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #ff8a80;");
        Label lblMotivoTesto = new Label("\"" + e.getMotivazione() + "\"");
        lblMotivoTesto.setWrapText(true);
        lblMotivoTesto.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-text-fill: #f5ebe0; -fx-font-family: 'Georgia';");
        cardMotivo.getChildren().addAll(lblMotivoTitolo, lblMotivoTesto);

        VBox cardAzione = new VBox(8);
        Label lblSostituto = new Label("🔁 Scegli il dipendente per coprire e modificare il turno:");
        lblSostituto.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #baa487;");

        ComboBox<String> comboSostitutoModal = new ComboBox<>();
        comboSostitutoModal.setMaxWidth(Double.MAX_VALUE);
        comboSostitutoModal.setPromptText("Seleziona sostituto dallo staff...");
        for (String dipEmail : emailTuttiDipendenti) {
            if (!dipEmail.equalsIgnoreCase(e.getEmailRichiedente())) {
                comboSostitutoModal.getItems().add(dipEmail);
            }
        }
        if (!comboSostitutoModal.getItems().isEmpty()) {
            comboSostitutoModal.setValue(comboSostitutoModal.getItems().get(0));
        }

        Label lblNota = new Label("✍️ Nota o risposta del Maestro in tempo reale:");
        lblNota.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #baa487;");
        TextField txtNota = new TextField("Sostituzione approvata e turno riassegnato");
        txtNota.setStyle("-fx-background-color: #1c1921; -fx-text-fill: #f5ebe0; -fx-border-color: rgba(186, 164, 135, 0.3); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;");

        cardAzione.getChildren().addAll(lblSostituto, comboSostitutoModal, lblNota, txtNota);

        HBox boxBottoni = new HBox(10);
        boxBottoni.setAlignment(Pos.CENTER_RIGHT);

        Button btnApprova = new Button("✔ Approva e Modifica Turno");
        btnApprova.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;");
        btnApprova.setOnAction(evt -> {
            popup.close();
            onApprova.accept(comboSostitutoModal.getValue(), txtNota.getText());
        });

        Button btnRifiuta = new Button("✖ Rifiuta Richiesta");
        btnRifiuta.setStyle("-fx-background-color: rgba(198, 40, 40, 0.25); -fx-border-color: rgba(198, 40, 40, 0.5); -fx-text-fill: #ff8a80; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;");
        btnRifiuta.setOnAction(evt -> {
            popup.close();
            onRifiuta.accept(txtNota.getText());
        });

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-border-color: rgba(255, 255, 255, 0.2); -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 14; -fx-cursor: hand;");
        btnChiudi.setOnAction(evt -> popup.close());

        boxBottoni.getChildren().addAll(btnChiudi, btnRifiuta, btnApprova);

        root.getChildren().addAll(lblTitolo, lblSub, cardInfo, cardMotivo, cardAzione, boxBottoni);
        popup.setScene(new Scene(root));
        TemaProvenzaHelper.applicaSeGui2(popup.getScene());
        popup.showAndWait();
    }
}
