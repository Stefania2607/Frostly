package view;

import bean.EmergenzaBean;
import bean.IngredienteBean;
import bean.OrdineBean;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Componente View dedicato al rendering estetico delle schermate del Maestro
 * e dei Dipendenti (celle Turni, Magazzino, Ordini Staff, Popup Emergenza,
 * stato foto e badge urgenze).
 * Mantiene il Controller Grafico completamente privo di estetica (BCE).
 */
public final class StaffTurniView {

    private StaffTurniView() {
        // Classe di utilità View
    }

    public static Label creaBadgeStatoOrdine(String stato) {
        String badgeText;
        String badgeStyle;
        if ("IN_ATTESA".equalsIgnoreCase(stato)) {
            badgeText = "🟡 In attesa di conferma dalla gelateria";
            badgeStyle = "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-border-color: #f59e0b;";
        } else if ("APPROVATA".equalsIgnoreCase(stato)) {
            badgeText = "🟢 Confermato dal personale • In preparazione";
            badgeStyle = "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-border-color: #10b981;";
        } else if ("RIFIUTATA".equalsIgnoreCase(stato)) {
            badgeText = "🔴 Disdetto dalla gelateria";
            badgeStyle = "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-border-color: #ef4444;";
        } else {
            badgeText = "🔵 " + stato;
            badgeStyle = "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-border-color: #38bdf8;";
        }
        Label lbl = new Label(badgeText);
        lbl.setStyle(badgeStyle + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 6; -fx-border-radius: 6;");
        return lbl;
    }

    public static void aggiornaBottoneUrgenze(Button btnTendinaUrgenze, int totaleUrgenze) {
        if (btnTendinaUrgenze == null) return;
        if (totaleUrgenze > 0) {
            btnTendinaUrgenze.setText("🔔 Urgenze (" + totaleUrgenze + ")");
            btnTendinaUrgenze.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 7 15; -fx-cursor: hand;");
        } else {
            btnTendinaUrgenze.setText("🔔 Notizie (0)");
            btnTendinaUrgenze.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 7 15; -fx-cursor: hand;");
        }
    }

    public static void aggiornaStatoFotoLabel(Label lblStatoFotoMenu, Button btnRimuoviFoto, String nomeFile) {
        if (lblStatoFotoMenu == null) return;
        if (nomeFile == null || nomeFile.trim().isEmpty()) {
            lblStatoFotoMenu.setText("Nessuna foto selezionata");
            lblStatoFotoMenu.setStyle("-fx-font-size: 12px; -fx-text-fill: #9c6644; -fx-font-style: italic;");
            if (btnRimuoviFoto != null) {
                btnRimuoviFoto.setVisible(false);
                btnRimuoviFoto.setManaged(false);
            }
        } else {
            String pulito = nomeFile;
            int idx = Math.max(nomeFile.lastIndexOf('/'), nomeFile.lastIndexOf('\\'));
            if (idx >= 0) pulito = nomeFile.substring(idx + 1);
            if (pulito.contains("_") && pulito.substring(0, pulito.indexOf('_')).matches("^\\d{10,}$")) {
                pulito = pulito.substring(pulito.indexOf('_') + 1);
            }
            lblStatoFotoMenu.setText("🖼️ Foto: " + pulito);
            lblStatoFotoMenu.setStyle("-fx-font-size: 12px; -fx-text-fill: #2a9d8f; -fx-font-weight: bold;");
            if (btnRimuoviFoto != null) {
                btnRimuoviFoto.setVisible(true);
                btnRimuoviFoto.setManaged(true);
            }
        }
    }

    public static void configuraCelleMagazzino(ListView<IngredienteBean> listaMagazzino) {
        if (listaMagazzino == null) return;
        listaMagazzino.setCellFactory(lv -> new ListCell<IngredienteBean>() {
            @Override
            protected void updateItem(IngredienteBean ing, boolean empty) {
                super.updateItem(ing, empty);
                if (empty || ing == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                boolean basso = ing.isSottoSoglia();
                Label nome = new Label(ing.getNome());
                nome.setPrefWidth(230);
                nome.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #3a2618;");

                double riferimento = Math.max(ing.getSogliaMinima() * 2, 0.0001);
                ProgressBar barra = new ProgressBar(Math.min(1.0, ing.getQuantitaDisponibile() / riferimento));
                barra.setPrefWidth(220);
                barra.setStyle("-fx-accent: " + (basso ? "#d62828" : "#2a9d8f") + ";");

                Label qta = new Label(ing.getQuantitaDisponibile() + " " + ing.getUnitaMisura()
                        + "  (min. " + ing.getSogliaMinima() + ")");
                qta.setPrefWidth(190);
                qta.setStyle("-fx-text-fill: #5c3a21; -fx-font-size: 12.5px;");

                Label badge = new Label(basso ? "⚠ DA RIORDINARE" : "✔ OK");
                badge.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: white;"
                        + " -fx-padding: 3 10; -fx-background-radius: 12; -fx-background-color: "
                        + (basso ? "#d62828" : "#2a9d8f") + ";");

                HBox riga = new HBox(14, nome, barra, qta, badge);
                riga.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(riga);
                setStyle("-fx-background-color: " + (basso ? "rgba(255,225,225,0.95)" : "rgba(255,253,250,0.95)")
                        + "; -fx-padding: 8 12; -fx-border-color: transparent transparent #e6d5c3 transparent;");
            }
        });
    }

    public static void configuraCelleTurni(ListView<String> listView, boolean mostraDipendente) {
        if (listView == null) return;
        listView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                String[] parti = item.split("\\|");
                String cod = parti.length > 0 ? parti[0].trim() : "";
                String giorno = parti.length > 1 ? parti[1].trim() : "";
                String orario = parti.length > 2 ? parti[2].trim() : "";
                String staff = parti.length > 3 ? parti[3].trim() : "";
                String stato = parti.length > 4 ? parti[4].trim() : "";

                HBox riga = new HBox(10);
                riga.setAlignment(Pos.CENTER_LEFT);
                riga.setStyle("-fx-padding: 6 8; -fx-background-color: transparent;");

                Label lblCod = new Label(cod);
                lblCod.setStyle("-fx-background-color: #f5ebe0; -fx-text-fill: #7f5539; -fx-font-weight: bold; -fx-padding: 3 7; -fx-background-radius: 6; -fx-font-size: 11px;");

                Label lblGiorno = new Label("📅 " + giorno);
                lblGiorno.setStyle("-fx-background-color: #ede0d4; -fx-text-fill: #5c3a21; -fx-font-weight: bold; -fx-padding: 4 9; -fx-background-radius: 8; -fx-font-size: 12px;");

                Label lblOrario = new Label("⏰ " + orario);
                lblOrario.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d5bdaf; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: #3a2618; -fx-font-weight: bold; -fx-padding: 3 8; -fx-font-size: 12px;");

                Label lblStaff = new Label(mostraDipendente ? ("👤 " + staff) : "");
                HBox.setHgrow(lblStaff, Priority.ALWAYS);
                lblStaff.setStyle("-fx-font-weight: bold; -fx-text-fill: #3a2618; -fx-font-size: 12.5px;");

                Label lblStato = new Label();
                if (stato.toUpperCase().contains("EMERGENZA")) {
                    lblStato.setText("🚨 IN EMERGENZA");
                    lblStato.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 10; -fx-font-size: 11px;");
                } else if (stato.toUpperCase().contains("SOSTITUIT")) {
                    lblStato.setText("🔄 SOSTITUITO");
                    lblStato.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 10; -fx-font-size: 11px;");
                } else {
                    lblStato.setText("✔ ASSEGNATO");
                    lblStato.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 10; -fx-font-size: 11px;");
                }

                if (mostraDipendente) {
                    riga.getChildren().addAll(lblCod, lblGiorno, lblOrario, lblStaff, lblStato);
                } else {
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    riga.getChildren().addAll(lblCod, lblGiorno, lblOrario, spacer, lblStato);
                }
                setGraphic(riga);
                setText(null);
            }
        });
    }

    public static void configuraCelleOrdiniStaff(ListView<OrdineBean> listaOrdiniStaff,
                                                 Consumer<String> onConfermaOrdine,
                                                 Consumer<String> onDisdiciOrdine) {
        if (listaOrdiniStaff == null) return;
        listaOrdiniStaff.setCellFactory(lv -> new ListCell<OrdineBean>() {
            @Override
            protected void updateItem(OrdineBean item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setGraphic(buildCardOrdineStaff(item, onConfermaOrdine, onDisdiciOrdine));
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    public static VBox buildCardOrdineStaff(OrdineBean item,
                                            Consumer<String> onConfermaOrdine,
                                            Consumer<String> onDisdiciOrdine) {
        VBox card = new VBox(6);
        card.setStyle("-fx-padding: 10 14; -fx-background-color: #ffffff; -fx-border-color: #e6d5c3; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label lblId = new Label(item.getCodiceOrdine());
        lblId.setStyle("-fx-background-color: #5c3a21; -fx-text-fill: #f5ebe0; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 6;");

        Label lblCliente = new Label("👤 " + item.getEmailCliente());
        lblCliente.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #3a2618;");

        Label lblMod = new Label("• " + item.getDataRitiro());
        lblMod.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f5539;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblBadge = creaBadgeStatoOrdine(item.getStato());

        top.getChildren().addAll(lblId, lblCliente, lblMod, spacer, lblBadge);

        Label lblDesc = new Label(item.getDescrizioneCompleta());
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #5c3a21;");

        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        Label lblPrezzo = new Label(String.format(Locale.US, "EUR %.2f", item.getPrezzoTotale()));
        lblPrezzo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #2a9d8f;");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        bottom.getChildren().addAll(lblPrezzo, sp2);

        if ("IN_ATTESA".equalsIgnoreCase(item.getStato())) {
            Button btnConf = new Button("✔ Conferma");
            btnConf.setStyle("-fx-background-color: #2a9d8f; -fx-text-fill: white; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 3 9; -fx-cursor: hand;");
            btnConf.setOnAction(e -> onConfermaOrdine.accept(item.getCodiceOrdine()));

            Button btnDis = new Button("✕ Disdici");
            btnDis.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 3 9; -fx-cursor: hand;");
            btnDis.setOnAction(e -> onDisdiciOrdine.accept(item.getCodiceOrdine()));

            bottom.getChildren().addAll(btnConf, btnDis);
        }

        card.getChildren().addAll(top, lblDesc, bottom);
        return card;
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
        root.setStyle("-fx-padding: 24; -fx-background-color: #fffdfa;");
        root.setPrefWidth(540);

        Label lblTitolo = new Label("🚨 RICHIESTA DI SOSTITUZIONE URGENTE");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #bc4749;");
        Label lblSub = new Label("Codice: " + e.getCodiceEmergenza() + " • Turno: " + e.getCodiceTurno());
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #7f5539;");

        VBox cardInfo = new VBox(6);
        cardInfo.setStyle("-fx-padding: 12 16; -fx-background-color: #f7ebe1; -fx-background-radius: 10; -fx-border-color: #e3d5ca; -fx-border-radius: 10;");
        Label lblDip = new Label("👤 Dipendente: " + e.getNomeRichiedente() + " (" + e.getEmailRichiedente() + ")");
        lblDip.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #3a2618;");
        String turnoDettagli = e.getDataTurno() != null ? (e.getDataTurno() + " ore " + e.getOrarioTurno()) : "Turno di lavoro programmato";
        Label lblTurno = new Label("📅 Turno coinvolto: " + e.getCodiceTurno() + " • " + turnoDettagli);
        lblTurno.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #5c3a21;");
        cardInfo.getChildren().addAll(lblDip, lblTurno);

        VBox cardMotivo = new VBox(6);
        cardMotivo.setStyle("-fx-padding: 12 16; -fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #d5bdaf; -fx-border-radius: 10;");
        Label lblMotivoTitolo = new Label("💬 Motivo comunicato dal dipendente:");
        lblMotivoTitolo.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #bc4749;");
        Label lblMotivoTesto = new Label("\"" + e.getMotivazione() + "\"");
        lblMotivoTesto.setWrapText(true);
        lblMotivoTesto.setStyle("-fx-font-size: 13.5px; -fx-font-style: italic; -fx-text-fill: #3a2618; -fx-font-family: 'Georgia';");
        cardMotivo.getChildren().addAll(lblMotivoTitolo, lblMotivoTesto);

        VBox cardAzione = new VBox(8);
        Label lblSostituto = new Label("🔁 Scegli il dipendente per coprire e modificare il turno:");
        lblSostituto.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #3a2618;");

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
        lblNota.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #3a2618;");
        TextField txtNota = new TextField("Sostituzione approvata e turno riassegnato");

        cardAzione.getChildren().addAll(lblSostituto, comboSostitutoModal, lblNota, txtNota);

        HBox boxBottoni = new HBox(10);
        boxBottoni.setAlignment(Pos.CENTER_RIGHT);

        Button btnApprova = new Button("✔ Approva e Modifica Turno");
        btnApprova.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 8 16; -fx-cursor: hand;");
        btnApprova.setOnAction(evt -> {
            popup.close();
            onApprova.accept(comboSostitutoModal.getValue(), txtNota.getText());
        });

        Button btnRifiuta = new Button("✖ Rifiuta Richiesta");
        btnRifiuta.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 8 16; -fx-cursor: hand;");
        btnRifiuta.setOnAction(evt -> {
            popup.close();
            onRifiuta.accept(txtNota.getText());
        });

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: #f5ebe0; -fx-text-fill: #5c3a21; -fx-background-radius: 16; -fx-padding: 8 14; -fx-cursor: hand;");
        btnChiudi.setOnAction(evt -> popup.close());

        boxBottoni.getChildren().addAll(btnChiudi, btnRifiuta, btnApprova);

        root.getChildren().addAll(lblTitolo, lblSub, cardInfo, cardMotivo, cardAzione, boxBottoni);
        popup.setScene(new Scene(root));
        popup.showAndWait();
    }
}
