package view;

import bean.RiepilogoOreDipendenteBean;
import bean.RiepilogoOreDipendenteBean.StatoMonteOre;
import frostly.exception.DatiNonValidiException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import bean.DipendenteBean;
import model.ConfigurazioneOreDipendente;
import model.DisponibilitaDipendente;
import model.ModalitaMonteOre;

/**
 * Componente View per il pannello laterale "Disponibilità Staff", la barra KPI settimanale
 * e il pop-up di configurazione monte ore e disponibilità dei dipendenti.
 */
public final class DisponibilitaStaffView {

    private static final DateTimeFormatter FMT_IT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DisponibilitaStaffView() {
    }

    public static void popolaBarraKpiSettimana(HBox contenitoreKpi,
                                               int turniSettimana,
                                               int dipendentiAttivi,
                                               double oreProgrammate,
                                               int alertAttivi) {
        if (contenitoreKpi == null) {
            return;
        }
        contenitoreKpi.getChildren().clear();
        boolean provenza = RecensioniView.isTemaSalottoChiaro(contenitoreKpi);

        VBox k1 = creaKpi("📅", "Turni pianificati", String.valueOf(turniSettimana),
                provenza ? "#5e316f" : "#baa487", provenza);
        VBox k2 = creaKpi("👥", "Dipendenti attivi", String.valueOf(dipendentiAttivi),
                provenza ? "#2e7d32" : "#a7f3d0", provenza);
        VBox k3 = creaKpi("⏱️", "Ore programmate", ConfigurazioneOreDipendente.formattaOre(oreProgrammate) + " h",
                provenza ? "#37253d" : "#f5ebe0", provenza);
        VBox k4 = creaKpi(alertAttivi > 0 ? "⚠️" : "✅", "Alert / Limiti", String.valueOf(alertAttivi),
                alertAttivi > 0 ? (provenza ? "#b91c1c" : "#fca5a5") : (provenza ? "#2e7d32" : "#a7f3d0"), provenza);

        HBox.setHgrow(k1, Priority.ALWAYS);
        HBox.setHgrow(k2, Priority.ALWAYS);
        HBox.setHgrow(k3, Priority.ALWAYS);
        HBox.setHgrow(k4, Priority.ALWAYS);
        contenitoreKpi.getChildren().addAll(k1, k2, k3, k4);
    }

    private static VBox creaKpi(String icona, String etichetta, String valore, String coloreVal, boolean provenza) {
        HBox riga = new HBox(10);
        riga.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icona);
        ico.setStyle("-fx-font-size: 18px;");
        VBox testi = new VBox(1);
        Label lblTit = new Label(etichetta);
        lblTit.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#756978;" : "#a89f92;"));
        Label lblVal = new Label(valore);
        lblVal.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + coloreVal + ";");
        testi.getChildren().addAll(lblTit, lblVal);
        riga.getChildren().addAll(ico, testi);

        VBox card = new VBox(riga);
        card.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e9dbdf; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 8 14;"
                : "-fx-background-color: rgba(26, 24, 30, 0.90); -fx-border-color: rgba(186,164,135,0.28); -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8 14;");
        return card;
    }

    public static void popolaPannelloDisponibilita(VBox contenitore,
                                                   List<RiepilogoOreDipendenteBean> riepiloghi,
                                                   Consumer<String> onConfiguraDipendente) {
        if (contenitore == null) {
            return;
        }
        contenitore.getChildren().clear();
        boolean provenza = RecensioniView.isTemaSalottoChiaro(contenitore);

        if (riepiloghi == null || riepiloghi.isEmpty()) {
            Label vuoto = new Label("Nessun dipendente registrato.");
            vuoto.setStyle("-fx-font-size: 12.5px; -fx-text-fill: " + (provenza ? "#756978;" : "#a89f92;"));
            contenitore.getChildren().add(vuoto);
            return;
        }

        for (RiepilogoOreDipendenteBean r : riepiloghi) {
            contenitore.getChildren().add(creaCardDipendente(r, provenza, onConfiguraDipendente));
        }
    }

    private static VBox creaCardDipendente(RiepilogoOreDipendenteBean r,
                                           boolean provenza,
                                           Consumer<String> onConfiguraDipendente) {
        String coloreStato;
        String bgBadgeStato;
        String testoStatoColor;
        switch (r.getStato()) {
            case OLTRE_MASSIMO -> {
                coloreStato = "#dc2626";
                bgBadgeStato = provenza ? "#fde8e8" : "rgba(90, 20, 25, 0.85)";
                testoStatoColor = provenza ? "#b91c1c" : "#fca5a5";
            }
            case SOTTO_MINIMO -> {
                coloreStato = "#d97706";
                bgBadgeStato = provenza ? "#fef3c7" : "rgba(75, 48, 16, 0.85)";
                testoStatoColor = provenza ? "#9a5b13" : "#fcd34d";
            }
            case ENTRO_TOLLERANZA -> {
                coloreStato = "#2563eb";
                bgBadgeStato = provenza ? "#e0f2fe" : "rgba(25, 45, 70, 0.85)";
                testoStatoColor = provenza ? "#1d4ed8" : "#93c5fd";
            }
            case NEL_RANGE -> {
                coloreStato = "#16a34a";
                bgBadgeStato = provenza ? "#e6f4ea" : "rgba(22, 60, 40, 0.85)";
                testoStatoColor = provenza ? "#1e6b3a" : "#a7f3d0";
            }
            default -> {
                coloreStato = "#764c86";
                bgBadgeStato = provenza ? "#eee2ee" : "rgba(60, 45, 70, 0.85)";
                testoStatoColor = provenza ? "#5e316f" : "#d8b4fe";
            }
        }

        VBox card = new VBox(4);
        String stileCardBase = provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: " + (r.getStato() == StatoMonteOre.OLTRE_MASSIMO ? "#fca5a5" : "#eadde2")
                  + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 7 8;"
                  + (onConfiguraDipendente != null ? " -fx-cursor: hand;" : "")
                : "-fx-background-color: rgba(24, 22, 28, 0.92); -fx-border-color: " + (r.getStato() == StatoMonteOre.OLTRE_MASSIMO ? "#ef4444" : "rgba(186,164,135,0.24)")
                  + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 7 8;"
                  + (onConfiguraDipendente != null ? " -fx-cursor: hand;" : "");
        card.setStyle(stileCardBase);

        // Riga 1: Avatar iniziali + Pallino stato + Nome + tasto Configura
        HBox top = new HBox(5);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setMinSize(22, 22);
        avatar.setPrefSize(22, 22);
        avatar.setStyle("-fx-background-color: " + (provenza ? "#f4eaf5" : "rgba(186,164,135,0.20)")
                + "; -fx-background-radius: 11; -fx-border-color: " + (provenza ? "#dfd0e3" : "rgba(186,164,135,0.40)")
                + "; -fx-border-radius: 11;");
        Label lblInit = new Label(r.getIniziali());
        lblInit.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#5e316f;" : "#baa487;"));
        avatar.getChildren().add(lblInit);

        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 8.5px; -fx-text-fill: " + coloreStato + ";");

        Label lblNome = new Label(r.getNomeCompleto());
        lblNome.setWrapText(true);
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));
        HBox.setHgrow(lblNome, Priority.ALWAYS);

        Button btnCfg = new Button("⚙");
        btnCfg.setMinWidth(Region.USE_PREF_SIZE);
        btnCfg.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (provenza ? "#8c5b9e" : "#baa487")
                + "; -fx-font-size: 11.5px; -fx-padding: 0 3; -fx-cursor: hand;");
        btnCfg.setOnAction(e -> {
            e.consume();
            if (onConfiguraDipendente != null) {
                onConfiguraDipendente.accept(r.getEmail());
            }
        });
        top.getChildren().addAll(avatar, dot, lblNome, btnCfg);

        // Riga 2: Ore assegnate / Obiettivo compatto
        HBox rigaOre = new HBox(4);
        rigaOre.setAlignment(Pos.CENTER_LEFT);
        String oreAss = ConfigurazioneOreDipendente.formattaOre(r.getOreAssegnate()) + " h";
        Label lblOre = new Label(oreAss);
        lblOre.setMinWidth(Region.USE_PREF_SIZE);
        lblOre.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#37253d;" : "#ded7cd;"));
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label lblTarget = new Label(r.getConfigurazioneOre().isConfigurato()
                ? ("/ " + r.getConfigurazioneOre().getEtichettaObiettivo())
                : "Da config.");
        lblTarget.setMinWidth(Region.USE_PREF_SIZE);
        lblTarget.setStyle("-fx-font-size: 10.5px; -fx-text-fill: " + (provenza ? "#756978;" : "#a89f92;"));
        rigaOre.getChildren().addAll(lblOre, sp, lblTarget);

        // Barra avanzamento sottile
        ProgressBar bar = new ProgressBar(r.getPercentualeProgresso());
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(5);
        bar.setMinHeight(5);
        bar.setStyle("-fx-accent: " + coloreStato + ";");

        // Badge stato testuale esplicito e compatto
        Label lblStato = new Label(r.getMessaggioStato());
        lblStato.setWrapText(true);
        lblStato.setStyle("-fx-background-color: " + bgBadgeStato + "; -fx-text-fill: " + testoStatoColor
                + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 6;");

        card.getChildren().addAll(top, rigaOre, bar, lblStato);

        // Dettagli secondari nel Tooltip + clic sull'intera card per aprire la configurazione
        String tipText = r.getNomeCompleto()
                + "\nOre settimana: " + oreAss + " (" + r.getNumeroTurniSettimana() + " turni)"
                + "\nObiettivo: " + r.getConfigurazioneOre().getEtichettaObiettivo()
                + "\nDisponibilità: " + r.getDisponibilita().getRiepilogoSintetico()
                + (onConfiguraDipendente != null ? "\nClicca per modificare monte ore, tolleranza, mansioni e disponibilità" : "");
        javafx.scene.control.Tooltip.install(card, new javafx.scene.control.Tooltip(tipText));
        if (onConfiguraDipendente != null) {
            card.setOnMouseClicked(e -> onConfiguraDipendente.accept(r.getEmail()));
        }
        return card;
    }

    /**
     * Pop-up modale per configurare Monte Ore (Ore fisse o Intervallo) e Disponibilità settimanale/date
     * di un dipendente esistente.
     */
    public static void apriDialogConfiguraDipendente(Window owner,
                                                     DipendenteBean dipendente,
                                                     BiConsumer<ConfigurazioneOreDipendente, DisponibilitaDipendente> onSalva) {
        if (dipendente == null) {
            return;
        }
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Monte Ore e Disponibilità: " + dipendente.getNome() + " " + dipendente.getCognome());
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setPrefWidth(560);
        root.setStyle("-fx-padding: 20 24; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        Label lblTit = new Label("⚙️ " + dipendente.getNome() + " " + dipendente.getCognome());
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        Label lblMail = new Label(dipendente.getEmail());
        lblMail.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92;");

        // Sezione 1: Monte Ore Settimanale
        VBox boxOre = new VBox(8);
        boxOre.setStyle("-fx-background-color: rgba(28,25,33,0.90); -fx-border-color: rgba(186,164,135,0.28); -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 14;");

        Label lblSezOre = new Label("1. MONTE ORE SETTIMANALE (OBBLIGATORIO)");
        lblSezOre.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #baa487;");

        ToggleGroup grpMod = new ToggleGroup();
        RadioButton rbFisse = new RadioButton("Ore settimanali fisse (es. 40 h)");
        rbFisse.setToggleGroup(grpMod);
        rbFisse.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12.5px;");
        RadioButton rbIntervallo = new RadioButton("Intervallo settimanale (min – max, es. 20–30 h)");
        rbIntervallo.setToggleGroup(grpMod);
        rbIntervallo.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12.5px;");

        TextField txtFisse = new TextField();
        txtFisse.setPromptText("Es. 40 o 22,5");
        txtFisse.setPrefWidth(100);

        TextField txtTollFisse = new TextField();
        txtTollFisse.setPromptText("± h (es. 3)");
        txtTollFisse.setPrefWidth(80);

        TextField txtMin = new TextField();
        txtMin.setPromptText("Min (es. 20)");
        txtMin.setPrefWidth(85);

        TextField txtMax = new TextField();
        txtMax.setPromptText("Max (es. 30)");
        txtMax.setPrefWidth(85);

        TextField txtTollSotto = new TextField();
        txtTollSotto.setPromptText("-h (es. 2)");
        txtTollSotto.setPrefWidth(72);

        TextField txtTollSopra = new TextField();
        txtTollSopra.setPromptText("+h (es. 3)");
        txtTollSopra.setPrefWidth(72);

        HBox rigaFisse = new HBox(8, new Label("Ore:"), txtFisse, new Label("Tolleranza ±:"), txtTollFisse);
        rigaFisse.setAlignment(Pos.CENTER_LEFT);
        ((Label) rigaFisse.getChildren().get(0)).setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 12px;");
        ((Label) rigaFisse.getChildren().get(2)).setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 12px;");

        HBox rigaInt = new HBox(8, new Label("Min:"), txtMin, new Label("Max:"), txtMax,
                new Label("Toll. -:"), txtTollSotto, new Label("+:"), txtTollSopra);
        rigaInt.setAlignment(Pos.CENTER_LEFT);
        ((Label) rigaInt.getChildren().get(0)).setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 12px;");
        ((Label) rigaInt.getChildren().get(2)).setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 12px;");
        ((Label) rigaInt.getChildren().get(4)).setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 12px;");
        ((Label) rigaInt.getChildren().get(6)).setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 12px;");

        ConfigurazioneOreDipendente cfgAtt = dipendente.getConfigurazioneOre();
        if (cfgAtt.getModalita() == ModalitaMonteOre.INTERVALLO) {
            rbIntervallo.setSelected(true);
            txtMin.setText(ConfigurazioneOreDipendente.formattaOre(cfgAtt.getOreMinime()));
            txtMax.setText(ConfigurazioneOreDipendente.formattaOre(cfgAtt.getOreMassime()));
            txtTollSotto.setText(ConfigurazioneOreDipendente.formattaOre(cfgAtt.getTolleranzaSotto()));
            txtTollSopra.setText(ConfigurazioneOreDipendente.formattaOre(cfgAtt.getTolleranzaSopra()));
        } else {
            rbFisse.setSelected(true);
            if (cfgAtt.isConfigurato()) {
                txtFisse.setText(ConfigurazioneOreDipendente.formattaOre(cfgAtt.getOreMassime()));
                txtTollFisse.setText(ConfigurazioneOreDipendente.formattaOre(cfgAtt.getTolleranzaSopra()));
            }
        }

        // Mansioni del dipendente
        javafx.scene.control.CheckBox chkBanco = new javafx.scene.control.CheckBox("Addetto al Banco");
        chkBanco.setSelected(dipendente.haMansione(model.MansioneDipendente.BANCO));
        chkBanco.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12px;");
        javafx.scene.control.CheckBox chkLab = new javafx.scene.control.CheckBox("Laboratorio");
        chkLab.setSelected(dipendente.haMansione(model.MansioneDipendente.LABORATORIO));
        chkLab.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12px;");
        javafx.scene.control.CheckBox chkResp = new javafx.scene.control.CheckBox("Responsabile");
        chkResp.setSelected(dipendente.haMansione(model.MansioneDipendente.RESPONSABILE));
        chkResp.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12px;");
        HBox rigaMansioni = new HBox(12, new Label("Mansioni:"), chkBanco, chkLab, chkResp);
        rigaMansioni.setAlignment(Pos.CENTER_LEFT);
        ((Label) rigaMansioni.getChildren().get(0)).setStyle("-fx-text-fill: #baa487; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Limite massimo turni settimanali (opzionale) + stato account attivo
        GestioneDipendentiView.ToggleSwitchPill toggleMaxTurni = new GestioneDipendentiView.ToggleSwitchPill(dipendente.hasLimiteMaxTurni());
        javafx.scene.control.Spinner<Integer> spinMaxTurni = new javafx.scene.control.Spinner<>(
                1, 21, dipendente.hasLimiteMaxTurni() ? dipendente.getMaxTurniSettimanali() : 5);
        spinMaxTurni.setEditable(true);
        spinMaxTurni.setPrefWidth(82);
        spinMaxTurni.setDisable(!dipendente.hasLimiteMaxTurni());
        toggleMaxTurni.setOnCambio(on -> spinMaxTurni.setDisable(!on));

        Label lblMaxT = new Label("Limite max turni/sett. (opz.):");
        lblMaxT.setStyle("-fx-text-fill: #baa487; -fx-font-weight: bold; -fx-font-size: 12px;");

        javafx.scene.control.CheckBox chkAttivo = new javafx.scene.control.CheckBox("Account attivo");
        chkAttivo.setSelected(dipendente.isAttivo());
        chkAttivo.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox rigaMaxEStato = new HBox(10, lblMaxT, toggleMaxTurni, spinMaxTurni, new Region(), chkAttivo);
        rigaMaxEStato.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(rigaMaxEStato.getChildren().get(3), Priority.ALWAYS);

        Runnable syncCampiOre = () -> {
            boolean fisse = rbFisse.isSelected();
            rigaFisse.setVisible(fisse);
            rigaFisse.setManaged(fisse);
            rigaInt.setVisible(!fisse);
            rigaInt.setManaged(!fisse);
        };
        rbFisse.setOnAction(e -> syncCampiOre.run());
        rbIntervallo.setOnAction(e -> syncCampiOre.run());
        syncCampiOre.run();

        boxOre.getChildren().addAll(lblSezOre, rbFisse, rbIntervallo, rigaFisse, rigaInt, rigaMansioni, rigaMaxEStato);

        // Sezione 2: Disponibilità Settimanale e Date Specifiche
        VBox boxDisp = new VBox(8);
        boxDisp.setStyle("-fx-background-color: rgba(28,25,33,0.90); -fx-border-color: rgba(186,164,135,0.28); -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 14;");

        Label lblSezDisp = new Label("2. DISPONIBILITÀ SETTIMANALE E DATE NON DISPONIBILI");
        lblSezDisp.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #baa487;");

        List<String> opzioniFascia = List.of(
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.TUTTO_IL_GIORNO),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.MATTINA),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.POMERIGGIO),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.SERA),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.NON_DISPONIBILE)
        );

        DisponibilitaDipendente dispAtt = dipendente.getDisponibilita();
        @SuppressWarnings("unchecked")
        ComboBox<String>[] combosGiorni = new ComboBox[7];
        VBox righeGiorni = new VBox(5);
        for (int i = 0; i < 7; i++) {
            DayOfWeek dow = DayOfWeek.of(i + 1);
            Label lblG = new Label(DisponibilitaDipendente.nomeGiorno(dow));
            lblG.setMinWidth(95);
            lblG.setStyle("-fx-text-fill: #f5ebe0; -fx-font-weight: bold; -fx-font-size: 12px;");
            ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(opzioniFascia));
            cb.setValue(DisponibilitaDipendente.etichettaFascia(dispAtt.getFasciaGiorno(dow)));
            cb.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cb, Priority.ALWAYS);
            combosGiorni[i] = cb;
            HBox row = new HBox(10, lblG, cb);
            row.setAlignment(Pos.CENTER_LEFT);
            righeGiorni.getChildren().add(row);
        }

        ListView<LocalDate> listaDateBloccate = new ListView<>();
        listaDateBloccate.setPrefHeight(78);
        listaDateBloccate.getItems().setAll(dispAtt.getDateNonDisponibili());
        listaDateBloccate.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "🚫 " + item.format(FMT_IT));
            }
        });

        DatePicker dpBloccata = new DatePicker();
        dpBloccata.setPromptText("Data indisponibile");
        Button btnAddData = new Button("+ Blocca data");
        btnAddData.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141216; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-background-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;");
        btnAddData.setOnAction(e -> {
            LocalDate d = dpBloccata.getValue();
            if (d != null && !listaDateBloccate.getItems().contains(d)) {
                listaDateBloccate.getItems().add(d);
            }
        });
        Button btnRemData = new Button("Rimuovi");
        btnRemData.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 10; -fx-font-size: 11.5px; -fx-padding: 5 10; -fx-cursor: hand;");
        btnRemData.setOnAction(e -> {
            LocalDate sel = listaDateBloccate.getSelectionModel().getSelectedItem();
            if (sel != null) {
                listaDateBloccate.getItems().remove(sel);
            }
        });

        HBox rigaDate = new HBox(8, dpBloccata, btnAddData, btnRemData);
        rigaDate.setAlignment(Pos.CENTER_LEFT);

        // Abilitazione fasce del catalogo (es. nuovo assunto solo chiusure 16-00 / 20-00, no pomeriggio pieno)
        Label lblAbilFasce = new Label("3. FASCE ORARIE ABILITATE & TURNI LUNGHI (≥ 8 H)");
        lblAbilFasce.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
        javafx.scene.control.CheckBox[] chkFasce = new javafx.scene.control.CheckBox[5];
        javafx.scene.layout.FlowPane flowFasce = new javafx.scene.layout.FlowPane(10, 6);
        for (int f = 0; f < 5; f++) {
            javafx.scene.control.CheckBox cbF = new javafx.scene.control.CheckBox(DisponibilitaDipendente.nomeFasciaCatalogo(f));
            cbF.setSelected(dispAtt.isFasciaAbilitata(f));
            cbF.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 11.5px;");
            chkFasce[f] = cbF;
            flowFasce.getChildren().add(cbF);
        }

        GestioneDipendentiView.ToggleSwitchPill toggleMax8h = new GestioneDipendentiView.ToggleSwitchPill(dispAtt.hasLimiteTurniLunghi());
        javafx.scene.control.Spinner<Integer> spinMax8h = new javafx.scene.control.Spinner<>(
                0, 7, dispAtt.hasLimiteTurniLunghi() ? dispAtt.getMaxTurniLunghiSettimana() : 1);
        spinMax8h.setEditable(true);
        spinMax8h.setPrefWidth(72);
        spinMax8h.setDisable(!dispAtt.hasLimiteTurniLunghi());
        toggleMax8h.setOnCambio(on -> spinMax8h.setDisable(!on));

        Label lblMax8h = new Label("Max turni da 8 h/sett. (0 = solo turni corti):");
        lblMax8h.setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 11.5px;");
        HBox rigaMax8h = new HBox(8, lblMax8h, toggleMax8h, spinMax8h);
        rigaMax8h.setAlignment(Pos.CENTER_LEFT);

        boxDisp.getChildren().addAll(
                lblSezDisp, righeGiorni,
                new Separator(), lblAbilFasce, flowFasce, rigaMax8h,
                new Separator(), rigaDate, listaDateBloccate);

        Label lblErr = new Label();
        lblErr.setWrapText(true);
        lblErr.setStyle("-fx-text-fill: #fca5a5; -fx-font-weight: bold; -fx-font-size: 12px;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 10; -fx-padding: 7 16; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> popup.close());

        Button btnSalva = new Button("💾 Salva Configurazione");
        btnSalva.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 7 18; -fx-cursor: hand;");
        btnSalva.setOnAction(e -> {
            try {
                ConfigurazioneOreDipendente nuovaCfg;
                if (rbFisse.isSelected()) {
                    double ore = parseOreDouble(txtFisse.getText(), "Ore settimanali");
                    double toll = parseTolleranzaOpzionale(txtTollFisse.getText());
                    nuovaCfg = ConfigurazioneOreDipendente.oreFisse(ore, toll);
                } else {
                    double min = parseOreDouble(txtMin.getText(), "Minimo ore");
                    double max = parseOreDouble(txtMax.getText(), "Massimo ore");
                    double tollSotto = parseTolleranzaOpzionale(txtTollSotto.getText());
                    double tollSopra = parseTolleranzaOpzionale(txtTollSopra.getText());
                    nuovaCfg = ConfigurazioneOreDipendente.intervallo(min, max, tollSotto, tollSopra);
                }
                java.util.EnumSet<model.MansioneDipendente> mansioniSel = java.util.EnumSet.noneOf(model.MansioneDipendente.class);
                if (chkBanco.isSelected()) mansioniSel.add(model.MansioneDipendente.BANCO);
                if (chkLab.isSelected()) mansioniSel.add(model.MansioneDipendente.LABORATORIO);
                if (chkResp.isSelected()) mansioniSel.add(model.MansioneDipendente.RESPONSABILE);
                if (mansioniSel.isEmpty()) {
                    mansioniSel.add(model.MansioneDipendente.BANCO);
                }
                dipendente.setMansioni(mansioniSel);
                dipendente.setAttivo(chkAttivo.isSelected());

                if (toggleMaxTurni.isAttivo()) {
                    String rawSpin = spinMaxTurni.getEditor() != null ? spinMaxTurni.getEditor().getText() : null;
                    int valMax;
                    try {
                        valMax = (rawSpin != null && !rawSpin.trim().isEmpty())
                                ? Integer.parseInt(rawSpin.trim())
                                : spinMaxTurni.getValue();
                    } catch (NumberFormatException ex) {
                        throw new DatiNonValidiException("Il limite massimo di turni deve essere un numero intero positivo.");
                    }
                    if (valMax <= 0) {
                        throw new DatiNonValidiException("Il limite massimo di turni deve essere un numero intero positivo.");
                    }
                    dipendente.setMaxTurniSettimanali(valMax);
                } else {
                    dipendente.setMaxTurniSettimanali(null);
                }

                DisponibilitaDipendente nuovaDisp = new DisponibilitaDipendente();
                for (int i = 0; i < 7; i++) {
                    nuovaDisp.impostaGiorno(DayOfWeek.of(i + 1),
                            DisponibilitaDipendente.codiceDaEtichetta(combosGiorni[i].getValue()));
                }
                for (LocalDate dt : listaDateBloccate.getItems()) {
                    nuovaDisp.aggiungiDataIndisponibile(dt);
                }
                for (LocalDate dt : dispAtt.getFeriePermessi()) {
                    nuovaDisp.aggiungiFeriePermesso(dt);
                }
                for (java.util.Map.Entry<DayOfWeek, String> ePref : dispAtt.getPreferenzeGiorni().entrySet()) {
                    nuovaDisp.impostaPreferenzaGiorno(ePref.getKey(), ePref.getValue());
                }
                java.util.Set<Integer> fasceSel = new java.util.LinkedHashSet<>();
                for (int f = 0; f < 5; f++) {
                    if (chkFasce[f].isSelected()) {
                        fasceSel.add(f);
                    }
                }
                if (fasceSel.isEmpty()) {
                    throw new DatiNonValidiException("Seleziona almeno una fascia oraria abilitata per il dipendente.");
                }
                nuovaDisp.setFasceAbilitate(fasceSel);
                if (toggleMax8h.isAttivo()) {
                    String raw8 = spinMax8h.getEditor() != null ? spinMax8h.getEditor().getText() : null;
                    int val8;
                    try {
                        val8 = (raw8 != null && !raw8.trim().isEmpty())
                                ? Integer.parseInt(raw8.trim())
                                : spinMax8h.getValue();
                    } catch (NumberFormatException ex) {
                        throw new DatiNonValidiException("Il limite di turni da 8 h deve essere un numero intero (0 o positivo).");
                    }
                    if (val8 < 0) {
                        throw new DatiNonValidiException("Il limite di turni da 8 h non può essere negativo.");
                    }
                    nuovaDisp.setMaxTurniLunghiSettimana(val8);
                } else {
                    nuovaDisp.setMaxTurniLunghiSettimana(null);
                }
                popup.close();
                if (onSalva != null) {
                    onSalva.accept(nuovaCfg, nuovaDisp);
                }
            } catch (DatiNonValidiException ex) {
                lblErr.setText("⚠️ " + ex.getMessage());
            }
        });
        footer.getChildren().addAll(btnAnnulla, btnSalva);

        ScrollPane scroll = new ScrollPane(new VBox(10, boxOre, boxDisp));
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(450);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.getChildren().addAll(lblTit, lblMail, scroll, lblErr, footer);
        Scene scene = new Scene(root);
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(popup.getScene());
        popup.showAndWait();
    }

    /**
     * Pop-up snello per pre-configurare la disponibilità di un nuovo dipendente prima del salvataggio.
     */
    public static void apriDialogSoloDisponibilita(Window owner,
                                                   DisponibilitaDipendente corrente,
                                                   Consumer<DisponibilitaDipendente> onConferma) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Configura Disponibilità Nuovo Dipendente");
        if (owner != null) {
            popup.initOwner(owner);
        }

        DisponibilitaDipendente dispAtt = corrente != null ? corrente : DisponibilitaDipendente.tuttoDisponibile();
        VBox root = new VBox(10);
        root.setPrefWidth(520);
        root.setStyle("-fx-padding: 18 22; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        Label lblTit = new Label("📅 Disponibilità settimanale e date non disponibili");
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        List<String> opzioniFascia = List.of(
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.TUTTO_IL_GIORNO),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.MATTINA),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.POMERIGGIO),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.SERA),
                DisponibilitaDipendente.etichettaFascia(DisponibilitaDipendente.NON_DISPONIBILE)
        );

        @SuppressWarnings("unchecked")
        ComboBox<String>[] combos = new ComboBox[7];
        VBox righe = new VBox(6);
        for (int i = 0; i < 7; i++) {
            DayOfWeek dow = DayOfWeek.of(i + 1);
            Label l = new Label(DisponibilitaDipendente.nomeGiorno(dow));
            l.setMinWidth(95);
            l.setStyle("-fx-text-fill: #f5ebe0; -fx-font-weight: bold; -fx-font-size: 12px;");
            ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(opzioniFascia));
            cb.setValue(DisponibilitaDipendente.etichettaFascia(dispAtt.getFasciaGiorno(dow)));
            cb.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cb, Priority.ALWAYS);
            combos[i] = cb;
            HBox r = new HBox(10, l, cb);
            r.setAlignment(Pos.CENTER_LEFT);
            righe.getChildren().add(r);
        }

        ListView<LocalDate> listaDate = new ListView<>();
        listaDate.setPrefHeight(74);
        listaDate.getItems().setAll(dispAtt.getDateNonDisponibili());
        listaDate.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "🚫 " + item.format(FMT_IT));
            }
        });
        DatePicker dp = new DatePicker();
        dp.setPromptText("Data indisponibile");
        Button btnAdd = new Button("+ Blocca data");
        btnAdd.setOnAction(e -> {
            if (dp.getValue() != null && !listaDate.getItems().contains(dp.getValue())) {
                listaDate.getItems().add(dp.getValue());
            }
        });
        Button btnRem = new Button("Rimuovi");
        btnRem.setOnAction(e -> {
            if (listaDate.getSelectionModel().getSelectedItem() != null) {
                listaDate.getItems().remove(listaDate.getSelectionModel().getSelectedItem());
            }
        });
        HBox rowDate = new HBox(8, dp, btnAdd, btnRem);
        rowDate.setAlignment(Pos.CENTER_LEFT);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setOnAction(e -> popup.close());
        Button btnOk = new Button("✔ Applica disponibilità");
        btnOk.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 10; -fx-cursor: hand;");
        btnOk.setOnAction(e -> {
            DisponibilitaDipendente nuova = new DisponibilitaDipendente();
            for (int i = 0; i < 7; i++) {
                nuova.impostaGiorno(DayOfWeek.of(i + 1), DisponibilitaDipendente.codiceDaEtichetta(combos[i].getValue()));
            }
            for (LocalDate dt : listaDate.getItems()) {
                nuova.aggiungiDataIndisponibile(dt);
            }
            popup.close();
            if (onConferma != null) {
                onConferma.accept(nuova);
            }
        });
        footer.getChildren().addAll(btnAnnulla, btnOk);

        root.getChildren().addAll(lblTit, righe, new Separator(), rowDate, listaDate, footer);
        Scene sc = new Scene(root);
        popup.setScene(sc);
        TemaProvenzaHelper.applicaSeGui2(sc);
        popup.centerOnScreen();
        popup.showAndWait();
    }

    public static double parseOreDouble(String raw, String campo) throws DatiNonValidiException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new DatiNonValidiException(campo + " è obbligatorio.");
        }
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DatiNonValidiException(campo + " deve essere un numero valido (es. 40 oppure 22,5).");
        }
    }

    private static double parseTolleranzaOpzionale(String raw) throws DatiNonValidiException {
        if (raw == null || raw.trim().isEmpty()) {
            return 0.0;
        }
        try {
            double v = Double.parseDouble(raw.trim().replace(',', '.'));
            if (v < 0) {
                throw new DatiNonValidiException("La tolleranza oraria non può essere negativa.");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new DatiNonValidiException("La tolleranza oraria deve essere un numero valido (es. 2 oppure 2,5).");
        }
    }
}
