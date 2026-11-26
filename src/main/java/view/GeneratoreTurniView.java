package view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import bean.DipendenteBean;
import model.ConfigurazioneGenerazioneTurni;
import model.ConfigurazioneGenerazioneTurni.ModalitaGenerazione;
import model.ConfigurazioneGenerazioneTurni.PrioritaOttimizzazione;
import model.FabbisognoTurno;
import model.MansioneDipendente;

/**
 * Finestre di configurazione per la generazione automatica dei turni e per i fabbisogni di copertura.
 */
public final class GeneratoreTurniView {

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] NOMI_GIORNI = {"Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica"};
    private static final String[] NOMI_FASCE = {"Mattina (07–14)", "Pomeriggio (14–20)", "Pom./Sera (16–00)", "Serale (20–00)", "Extra Weekend (10–18)"};

    private GeneratoreTurniView() {
    }

    /**
     * Apre la finestra di configurazione del generatore automatico di turni.
     */
    public static void apriDialogConfigurazione(Window owner,
                                                LocalDate lunediSettimana,
                                                List<DipendenteBean> dipendenti,
                                                Runnable onApriFabbisogni,
                                                Consumer<ConfigurazioneGenerazioneTurni> onAvviaGenerazione) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Genera turni automaticamente");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setPrefWidth(560);
        root.setStyle("-fx-padding: 20 24; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        LocalDate domenica = lunediSettimana.plusDays(6);
        Label lblTitolo = new Label("✨ Generazione Automatica Turni Settimanali");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Label lblSettimana = new Label("📅 Settimana interessata: Lunedì " + lunediSettimana.format(FMT_DATA)
                + " – Domenica " + domenica.format(FMT_DATA));
        lblSettimana.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #baa487; "
                + "-fx-background-color: rgba(35,32,40,0.85); -fx-padding: 7 12; -fx-background-radius: 10;");

        // Avviso dipendenti senza monte ore configurato
        List<String> nonConfigurati = dipendenti == null ? List.of()
                : dipendenti.stream()
                .filter(d -> d.isAttivo() && !d.getConfigurazioneOre().isConfigurato())
                .map(d -> d.getNome() + " " + d.getCognome())
                .toList();

        VBox boxAvvisoNonCfg = new VBox(3);
        if (!nonConfigurati.isEmpty()) {
            boxAvvisoNonCfg.setStyle("-fx-background-color: rgba(90, 48, 16, 0.30); -fx-border-color: #d97706; "
                    + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 10;");
            Label l1 = new Label("⚠️ Dipendenti esclusi finché non viene configurato il monte ore:");
            l1.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #fcd34d;");
            Label l2 = new Label(String.join(", ", nonConfigurati)
                    + ". Configura il loro profilo in Disponibilità Staff per includerli.");
            l2.setWrapText(true);
            l2.setStyle("-fx-font-size: 11px; -fx-text-fill: #f5ebe0;");
            boxAvvisoNonCfg.getChildren().addAll(l1, l2);
        } else {
            boxAvvisoNonCfg.setVisible(false);
            boxAvvisoNonCfg.setManaged(false);
        }

        // 0. Tipo di generazione (Standard Catalogo vs Flessibile Orari Personalizzati)
        VBox boxTipo = new VBox(7);
        boxTipo.setStyle("-fx-background-color: rgba(24,22,28,0.88); -fx-border-color: rgba(186,164,135,0.25); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 14;");
        Label lblTipo = new Label("0. Tipo di generazione");
        lblTipo.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");

        ToggleGroup tgTipo = new ToggleGroup();
        RadioButton rbStandard = new RadioButton("Standard — utilizza i turni del catalogo");
        rbStandard.setToggleGroup(tgTipo);
        rbStandard.setSelected(true);
        rbStandard.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12.5px; -fx-font-weight: bold;");

        RadioButton rbFlessibile = new RadioButton("Flessibile — crea orari personalizzati (es. 16:00–22:00, 18:00–00:00)");
        rbFlessibile.setToggleGroup(tgTipo);
        rbFlessibile.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12.5px; -fx-font-weight: bold;");

        // Parametri opzionali per la modalita' flessibile
        VBox boxParamFlessibili = new VBox(6);
        boxParamFlessibili.setStyle("-fx-background-color: rgba(35,30,44,0.65); -fx-border-color: rgba(186,164,135,0.22); "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;");
        Spinner<Integer> spDurMin = new Spinner<>(2, 8, 4);
        spDurMin.setPrefWidth(66);
        Spinner<Integer> spDurMax = new Spinner<>(4, 10, 8);
        spDurMax.setPrefWidth(66);
        ComboBox<Integer> cbStep = new ComboBox<>();
        cbStep.getItems().addAll(15, 30, 60);
        cbStep.setValue(60);
        cbStep.setPrefWidth(72);
        Spinner<Integer> spChiusura = new Spinner<>(1, 6,
                new control.applicativo.GestioneTurniEmergenzeControl().caricaCoperturaFlessibile().getPersonaleMinimoChiusura());
        spChiusura.setPrefWidth(66);

        HBox rowFlex1 = new HBox(8,
                new Label("Durata min (h):"), spDurMin,
                new Label("Max (h):"), spDurMax,
                new Label("Step (min):"), cbStep);
        rowFlex1.setAlignment(Pos.CENTER_LEFT);
        for (javafx.scene.Node n : rowFlex1.getChildren()) {
            if (n instanceof Label l) l.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #ded7cd;");
        }
        HBox rowFlex2 = new HBox(8,
                new Label("Personale minimo in chiusura (22:00–00:00):"), spChiusura);
        rowFlex2.setAlignment(Pos.CENTER_LEFT);
        ((Label) rowFlex2.getChildren().get(0)).setStyle("-fx-font-size: 11.5px; -fx-text-fill: #ded7cd; -fx-font-weight: bold;");

        boxParamFlessibili.getChildren().addAll(rowFlex1, rowFlex2);
        boxParamFlessibili.setVisible(false);
        boxParamFlessibili.setManaged(false);

        Runnable syncTipo = () -> {
            boolean flex = rbFlessibile.isSelected();
            boxParamFlessibili.setVisible(flex);
            boxParamFlessibili.setManaged(flex);
            popup.sizeToScene();
        };
        rbStandard.setOnAction(e -> syncTipo.run());
        rbFlessibile.setOnAction(e -> syncTipo.run());

        boxTipo.getChildren().addAll(lblTipo, rbStandard, rbFlessibile, boxParamFlessibili);

        // Modalita' di generazione (coerenti con la gestione dei turni esistenti)
        VBox boxModalita = new VBox(7);
        boxModalita.setStyle("-fx-background-color: rgba(24,22,28,0.88); -fx-border-color: rgba(186,164,135,0.25); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 14;");
        Label lblMod = new Label("1. Modalità di generazione e gestione turni esistenti");
        lblMod.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");

        ToggleGroup tgMod = new ToggleGroup();
        RadioButton rbNonBloccati = new RadioButton("Rigenera i turni non bloccati (consigliata)");
        rbNonBloccati.setToggleGroup(tgMod);
        rbNonBloccati.setSelected(true);
        rbNonBloccati.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        Label descNonBloccati = new Label("   Mantiene intatti tutti i turni bloccati con 🔒 e ricalcola/riequilibra in anteprima tutti gli altri.");
        descNonBloccati.setWrapText(true);
        descNonBloccati.setStyle("-fx-text-fill: #a89f92; -fx-font-size: 11px;");

        RadioButton rbSoloScoperti = new RadioButton("Completa soltanto i turni scoperti");
        rbSoloScoperti.setToggleGroup(tgMod);
        rbSoloScoperti.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        Label descSoloScoperti = new Label("   Mantiene congelati tutti i turni già salvati (anche non bloccati) e riempie solo le celle vuote.");
        descSoloScoperti.setWrapText(true);
        descSoloScoperti.setStyle("-fx-text-fill: #a89f92; -fx-font-size: 11px;");

        RadioButton rbIntera = new RadioButton("Genera una nuova proposta completa per l'intera settimana");
        rbIntera.setToggleGroup(tgMod);
        rbIntera.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        Label descIntera = new Label("   Crea una proposta completa rispettando comunque i turni bloccati 🔒; nulla viene salvato prima della conferma.");
        descIntera.setWrapText(true);
        descIntera.setStyle("-fx-text-fill: #a89f92; -fx-font-size: 11px;");

        boxModalita.getChildren().addAll(lblMod, rbNonBloccati, descNonBloccati, rbSoloScoperti, descSoloScoperti, rbIntera, descIntera);

        // Priorita' di ottimizzazione e riposo minimo
        VBox boxPriorita = new VBox(8);
        boxPriorita.setStyle("-fx-background-color: rgba(24,22,28,0.88); -fx-border-color: rgba(186,164,135,0.25); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 14;");
        Label lblPri = new Label("2. Priorità di ottimizzazione e vincoli di riposo");
        lblPri.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");

        ComboBox<String> comboPriorita = new ComboBox<>();
        comboPriorita.getItems().addAll(
                PrioritaOttimizzazione.COPERTURA_PRIORITARIA.getDescrizione(),
                PrioritaOttimizzazione.PIANIFICAZIONE_EQUILIBRATA.getDescrizione(),
                PrioritaOttimizzazione.MONTE_ORE_PRIORITARIO.getDescrizione());
        comboPriorita.setValue(PrioritaOttimizzazione.COPERTURA_PRIORITARIA.getDescrizione());
        comboPriorita.setMaxWidth(Double.MAX_VALUE);

        HBox rigaRiposo = new HBox(10);
        rigaRiposo.setAlignment(Pos.CENTER_LEFT);
        Label lblRip = new Label("Riposo minimo obbligatorio tra due turni (ore):");
        lblRip.setStyle("-fx-font-size: 12px; -fx-text-fill: #ded7cd;");
        Spinner<Integer> spinRiposo = new Spinner<>(0, 24, 11);
        spinRiposo.setPrefWidth(80);
        Region spRip = new Region();
        HBox.setHgrow(spRip, Priority.ALWAYS);
        Button btnFab = new Button("⚙ Fabbisogni copertura");
        btnFab.setStyle("-fx-background-color: #2b2730; -fx-text-fill: #f5ebe0; -fx-border-color: rgba(186,164,135,0.45); "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5 12; -fx-font-size: 11.5px; -fx-cursor: hand;");
        btnFab.setOnAction(e -> {
            if (onApriFabbisogni != null) {
                onApriFabbisogni.run();
            }
        });
        rigaRiposo.getChildren().addAll(lblRip, spinRiposo, spRip, btnFab);

        boxPriorita.getChildren().addAll(lblPri, comboPriorita, rigaRiposo);

        Label lblInfoAnteprima = new Label("ℹ️ Al clic su 'Genera anteprima' nessun dato ufficiale verrà modificato finché non confermerai.");
        lblInfoAnteprima.setWrapText(true);
        lblInfoAnteprima.setStyle("-fx-font-size: 11.5px; -fx-font-style: italic; -fx-text-fill: #a89f92;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.4); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 7 16; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> popup.close());

        final ConfigurazioneGenerazioneTurni[] risultatoScelto = {null};

        Button btnGenera = new Button("✨ Genera anteprima");
        btnGenera.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                + "-fx-background-radius: 12; -fx-padding: 8 20; -fx-cursor: hand;");
        btnGenera.setOnAction(e -> {
            ModalitaGenerazione mod = rbSoloScoperti.isSelected()
                    ? ModalitaGenerazione.SOLO_SCOPERTI
                    : (rbNonBloccati.isSelected() ? ModalitaGenerazione.RIGENERA_NON_BLOCCATI : ModalitaGenerazione.INTERA_SETTIMANA);

            String selPri = comboPriorita.getValue();
            PrioritaOttimizzazione pri = PrioritaOttimizzazione.COPERTURA_PRIORITARIA;
            if (PrioritaOttimizzazione.PIANIFICAZIONE_EQUILIBRATA.getDescrizione().equals(selPri)) {
                pri = PrioritaOttimizzazione.PIANIFICAZIONE_EQUILIBRATA;
            } else if (PrioritaOttimizzazione.MONTE_ORE_PRIORITARIO.getDescrizione().equals(selPri)) {
                pri = PrioritaOttimizzazione.MONTE_ORE_PRIORITARIO;
            }

            ConfigurazioneGenerazioneTurni cfg = new ConfigurazioneGenerazioneTurni(mod, pri);
            cfg.setRiposoMinimoOre(spinRiposo.getValue());
            if (rbFlessibile.isSelected()) {
                cfg.setTipoGenerazione(ConfigurazioneGenerazioneTurni.TipoGenerazione.FLESSIBILE);
                cfg.setDurataMinimaOreFlessibile(spDurMin.getValue());
                cfg.setDurataMassimaOreFlessibile(Math.max(spDurMin.getValue(), spDurMax.getValue()));
                cfg.setIncrementoMinutiFlessibile(cbStep.getValue());
                control.applicativo.GestioneTurniEmergenzeControl tc = new control.applicativo.GestioneTurniEmergenzeControl();
                model.ConfigurazioneCoperturaFlessibile cov = tc.caricaCoperturaFlessibile();
                cov.setPersonaleMinimoChiusura(spChiusura.getValue());
                tc.salvaCoperturaFlessibile(cov);
            } else {
                cfg.setTipoGenerazione(ConfigurazioneGenerazioneTurni.TipoGenerazione.STANDARD_CATALOGO);
            }
            risultatoScelto[0] = cfg;
            popup.close();
        });
        footer.getChildren().addAll(btnAnnulla, btnGenera);

        root.getChildren().addAll(lblTitolo, lblSettimana, boxAvvisoNonCfg, boxTipo, boxModalita, boxPriorita, lblInfoAnteprima, footer);

        Scene scene = new Scene(root);
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(scene);
        popup.showAndWait();

        // Avvia la generazione DOPO la chiusura completa della prima modale (evita blocchi Glass su macOS)
        if (risultatoScelto[0] != null && onAvviaGenerazione != null) {
            onAvviaGenerazione.accept(risultatoScelto[0]);
        }
    }

    /**
     * Dialog per configurare il fabbisogno di personale (totale e minimi per mansione)
     * per ciascun giorno e fascia oraria del catalogo.
     */
    public static void apriDialogConfiguraFabbisogni(Window owner,
                                                     Map<String, FabbisognoTurno> fabbisogniCorrenti,
                                                     Consumer<Map<String, FabbisognoTurno>> onSalva) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Fabbisogno di Copertura Turni");
        if (owner != null) {
            popup.initOwner(owner);
        }

        Map<String, FabbisognoTurno> working = new LinkedHashMap<>();
        for (DayOfWeek g : DayOfWeek.values()) {
            for (int f = 0; f < 5; f++) {
                String k = FabbisognoTurno.chiave(g, f);
                FabbisognoTurno src = fabbisogniCorrenti != null ? fabbisogniCorrenti.get(k) : null;
                working.put(k, src != null ? src.copia() : FabbisognoTurno.predefinito(g, f));
            }
        }

        VBox root = new VBox(10);
        root.setPrefWidth(760);
        root.setPrefHeight(560);
        root.setStyle("-fx-padding: 18 22; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        Label lblTit = new Label("⚙️ Fabbisogno Personale per Giorno e Fascia Oraria");
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        Label lblSub = new Label("Imposta il numero totale di dipendenti richiesti e il minimo per ciascuna mansione (Banco, Laboratorio, Responsabile).");
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #a89f92;");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.setStyle("-fx-padding: 6;");

        String[] headers = {"Giorno / Fascia", "Totale", "Min. Banco", "Min. Lab.", "Min. Resp."};
        for (int c = 0; c < headers.length; c++) {
            Label h = new Label(headers[c]);
            h.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #baa487;");
            grid.add(h, c, 0);
        }

        int row = 1;
        for (int gIdx = 0; gIdx < 7; gIdx++) {
            DayOfWeek dow = DayOfWeek.of(gIdx + 1);
            boolean weekend = (gIdx >= 5);
            for (int f = 0; f < 5; f++) {
                if (f == 4 && !weekend) {
                    continue;
                }
                String chiave = FabbisognoTurno.chiave(dow, f);
                FabbisognoTurno fab = working.get(chiave);

                Label lblSlot = new Label(NOMI_GIORNI[gIdx] + " • " + NOMI_FASCE[f]);
                lblSlot.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

                Spinner<Integer> spTot = new Spinner<>(0, 12, fab.getTotaleRichiesto());
                spTot.setPrefWidth(72);
                spTot.valueProperty().addListener((obs, o, n) -> fab.setTotaleRichiesto(n));

                Spinner<Integer> spBanco = new Spinner<>(0, 10, fab.getMinimoMansione(MansioneDipendente.BANCO));
                spBanco.setPrefWidth(72);
                spBanco.valueProperty().addListener((obs, o, n) -> fab.setMinimoMansione(MansioneDipendente.BANCO, n));

                Spinner<Integer> spLab = new Spinner<>(0, 10, fab.getMinimoMansione(MansioneDipendente.LABORATORIO));
                spLab.setPrefWidth(72);
                spLab.valueProperty().addListener((obs, o, n) -> fab.setMinimoMansione(MansioneDipendente.LABORATORIO, n));

                Spinner<Integer> spResp = new Spinner<>(0, 5, fab.getMinimoMansione(MansioneDipendente.RESPONSABILE));
                spResp.setPrefWidth(72);
                spResp.valueProperty().addListener((obs, o, n) -> fab.setMinimoMansione(MansioneDipendente.RESPONSABILE, n));

                grid.add(lblSlot, 0, row);
                grid.add(spTot, 1, row);
                grid.add(spBanco, 2, row);
                grid.add(spLab, 3, row);
                grid.add(spResp, 4, row);
                row++;
            }
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.4); "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 6 14; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> popup.close());

        Button btnSalva = new Button("💾 Salva fabbisogni");
        btnSalva.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                + "-fx-background-radius: 10; -fx-padding: 7 18; -fx-cursor: hand;");
        btnSalva.setOnAction(e -> {
            popup.close();
            if (onSalva != null) {
                onSalva.accept(working);
            }
        });
        footer.getChildren().addAll(btnAnnulla, btnSalva);

        root.getChildren().addAll(lblTit, lblSub, scroll, footer);
        Scene scene = new Scene(root);
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(scene);
        popup.showAndWait();
    }
}
