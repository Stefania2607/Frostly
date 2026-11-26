package view;

import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import bean.DipendenteBean;

/**
 * Vista dedicata alla schermata "Creazione e Gestione Profili Staff",
 * fedele al layout Frostly con card "Nuovo dipendente" (incluso limite massimo turni)
 * e tabella "Personale attualmente abilitato" a 7 colonne.
 */
public final class GestioneDipendentiView {

    /**
     * Riferimenti ai controlli interattivi della schermata costruiti dinamicamente.
     */
    public static final class ComponentiForm {
        public final TextField campoNome;
        public final TextField campoCognome;
        public final TextField campoEmail;
        public final TextField campoPassword;
        public final ComboBox<String> comboModalitaOre;
        public final Label lblCampoOre;
        public final TextField campoOreMin;
        public final TextField campoOreMax;
        public final ToggleSwitchPill toggleMaxTurni;
        public final Spinner<Integer> spinnerMaxTurni;
        public final Button btnConfiguraDispNuovo;
        public final VBox boxRigheTabella;
        private String emailSelezionata;

        public ComponentiForm(TextField campoNome, TextField campoCognome, TextField campoEmail,
                              TextField campoPassword, ComboBox<String> comboModalitaOre, Label lblCampoOre,
                              TextField campoOreMin, TextField campoOreMax, ToggleSwitchPill toggleMaxTurni,
                              Spinner<Integer> spinnerMaxTurni, Button btnConfiguraDispNuovo, VBox boxRigheTabella) {
            this.campoNome = campoNome;
            this.campoCognome = campoCognome;
            this.campoEmail = campoEmail;
            this.campoPassword = campoPassword;
            this.comboModalitaOre = comboModalitaOre;
            this.lblCampoOre = lblCampoOre;
            this.campoOreMin = campoOreMin;
            this.campoOreMax = campoOreMax;
            this.toggleMaxTurni = toggleMaxTurni;
            this.spinnerMaxTurni = spinnerMaxTurni;
            this.btnConfiguraDispNuovo = btnConfiguraDispNuovo;
            this.boxRigheTabella = boxRigheTabella;
        }

        public String getEmailSelezionata() {
            return emailSelezionata;
        }

        public void setEmailSelezionata(String email) {
            this.emailSelezionata = email;
        }
    }

    /**
     * Interruttore a pillola in stile moderno (off = grigio tenue, on = viola prugna).
     */
    public static final class ToggleSwitchPill extends StackPane {
        private boolean attivo;
        private final Region traccia;
        private final Circle pomello;
        private Consumer<Boolean> onCambio;

        public ToggleSwitchPill(boolean iniziale) {
            this.attivo = iniziale;
            setMinSize(44, 24);
            setPrefSize(44, 24);
            setMaxSize(44, 24);
            setStyle("-fx-cursor: hand;");

            traccia = new Region();
            traccia.setMinSize(44, 24);
            traccia.setPrefSize(44, 24);
            traccia.setMaxSize(44, 24);

            pomello = new Circle(9, Color.WHITE);
            pomello.setEffect(new DropShadow(3, 0, 1, Color.rgb(0, 0, 0, 0.22)));

            getChildren().addAll(traccia, pomello);
            aggiornaGrafica();

            setOnMouseClicked(e -> setAttivo(!attivo));
        }

        public boolean isAttivo() {
            return attivo;
        }

        public void setAttivo(boolean nuovoStato) {
            this.attivo = nuovoStato;
            aggiornaGrafica();
            if (onCambio != null) {
                onCambio.accept(attivo);
            }
        }

        public void setOnCambio(Consumer<Boolean> onCambio) {
            this.onCambio = onCambio;
        }

        private void aggiornaGrafica() {
            traccia.setStyle("-fx-background-color: " + (attivo ? "#7c4b8c" : "#cfc4d1")
                    + "; -fx-background-radius: 12;");
            StackPane.setAlignment(pomello, attivo ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            StackPane.setMargin(pomello, new Insets(0, 3, 0, 3));
        }
    }

    private GestioneDipendentiView() {
    }

    /**
     * Costruisce l'intera interfaccia "Creazione e Gestione Profili Staff" dentro contenitore.
     */
    public static ComponentiForm costruisciSchermata(VBox contenitore,
                                                     Runnable onGeneraPassword,
                                                     Runnable onConfiguraDispNuovo,
                                                     Runnable onSalvaNuovoDipendente,
                                                     Runnable onRimuoviSelezionato) {
        contenitore.getChildren().clear();
        boolean provenza = util.SessionManager.getInstance().isGui2Attiva();

        String colTitolo = provenza ? "#4b2654" : "#f5ebe0";
        String colSub = provenza ? "#6f5a75" : "#a89f92";
        String bgCard = provenza ? "#fffdfb" : "rgba(24, 22, 28, 0.94)";
        String borderCard = provenza ? "#e4d5df" : "rgba(186, 164, 135, 0.30)";
        String bgHeaderCard = provenza ? "#f4e5f0" : "rgba(42, 36, 48, 0.95)";
        String bgSubCard = provenza ? "#fbf8f9" : "rgba(30, 27, 36, 0.90)";
        String colLabel = provenza ? "#4b2654" : "#ded7cd";

        // A. INTESTAZIONE PAGINA
        HBox headerPagina = new HBox(12);
        headerPagina.setAlignment(Pos.CENTER_LEFT);
        headerPagina.setPadding(new Insets(2, 2, 4, 2));

        StackPane badgeIcona = new StackPane();
        badgeIcona.setMinSize(42, 42);
        badgeIcona.setPrefSize(42, 42);
        badgeIcona.setStyle("-fx-background-color: " + (provenza ? "#ecdceb" : "rgba(186,164,135,0.20)")
                + "; -fx-border-color: " + (provenza ? "#dcc3db" : "rgba(186,164,135,0.40)")
                + "; -fx-border-radius: 10; -fx-background-radius: 10;");
        Label icoStaff = new Label("👥");
        icoStaff.setStyle("-fx-font-size: 19px;");
        badgeIcona.getChildren().add(icoStaff);

        VBox testiHeader = new VBox(2);
        Label lblTitolo = new Label("Creazione e Gestione Profili Staff");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: " + colTitolo + ";");
        Label lblSub = new Label("Crea e gestisci gli account dei dipendenti (i clienti si registrano autonomamente) e definisci le loro abilitazioni, disponibilità e vincoli di lavoro.");
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: " + colSub + ";");
        testiHeader.getChildren().addAll(lblTitolo, lblSub);
        headerPagina.getChildren().addAll(badgeIcona, testiHeader);

        // B. CARD "NUOVO DIPENDENTE"
        VBox cardNuovo = new VBox(0);
        cardNuovo.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + borderCard
                + "; -fx-border-radius: 13; -fx-background-radius: 13;");
        if (provenza) {
            cardNuovo.setEffect(new DropShadow(10, 0, 2, Color.rgb(75, 38, 84, 0.07)));
        }

        HBox hdrNuovo = creaHeaderCard("+", "Nuovo dipendente", bgHeaderCard, borderCard, colTitolo, provenza);

        VBox bodyNuovo = new VBox(10);
        bodyNuovo.setPadding(new Insets(12, 16, 14, 16));

        // Riga 1: Nome * | Cognome * | Email aziendale *
        GridPane riga1 = new GridPane();
        riga1.setHgap(14);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setHgrow(Priority.ALWAYS);
            riga1.getColumnConstraints().add(cc);
        }

        TextField txtNome = creaInput("Inserisci il nome", provenza);
        TextField txtCognome = creaInput("Inserisci il cognome", provenza);
        TextField txtEmail = creaInput("nome.cognome@frostly.it", provenza);

        riga1.add(creaCampoConEtichetta("👤", "Nome", true, txtNome, colLabel), 0, 0);
        riga1.add(creaCampoConEtichetta("👤", "Cognome", true, txtCognome, colLabel), 1, 0);
        riga1.add(creaCampoConEtichetta("✉", "Email aziendale", true, txtEmail, colLabel), 2, 0);

        // Riga 2: Password * + Genera password
        GridPane riga2 = new GridPane();
        riga2.setHgap(14);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setHgrow(Priority.ALWAYS);
            riga2.getColumnConstraints().add(cc);
        }

        TextField txtPass = creaInput("Min. 4 caratteri", provenza);
        Button btnGenPass = new Button("↻ Genera password");
        btnGenPass.setMinWidth(Region.USE_PREF_SIZE);
        btnGenPass.setStyle(provenza
                ? "-fx-background-color: #f3e6f2; -fx-text-fill: #4b2654; -fx-border-color: #d7bdd6; "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7 14; -fx-background-radius: 9; -fx-border-radius: 9; -fx-cursor: hand;"
                : "-fx-background-color: #2f2a38; -fx-text-fill: #f5ebe0; -fx-border-color: rgba(186,164,135,0.45); "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7 14; -fx-background-radius: 9; -fx-border-radius: 9; -fx-cursor: hand;");
        btnGenPass.setOnAction(e -> {
            if (onGeneraPassword != null) onGeneraPassword.run();
        });

        VBox boxBtnGen = new VBox(btnGenPass);
        boxBtnGen.setAlignment(Pos.BOTTOM_LEFT);
        boxBtnGen.setPadding(new Insets(18, 0, 0, 0));

        riga2.add(creaCampoConEtichetta("🔒", "Password", true, txtPass, colLabel), 0, 0);
        riga2.add(boxBtnGen, 1, 0);

        // Riga 3: Riquadro sinistro (Monte ore) + Riquadro destro (Limite massimo turni)
        GridPane riga3 = new GridPane();
        riga3.setHgap(14);
        ColumnConstraints cLeft = new ColumnConstraints();
        cLeft.setPercentWidth(50);
        cLeft.setHgrow(Priority.ALWAYS);
        ColumnConstraints cRight = new ColumnConstraints();
        cRight.setPercentWidth(50);
        cRight.setHgrow(Priority.ALWAYS);
        riga3.getColumnConstraints().addAll(cLeft, cRight);

        // Riquadro sinistro — Monte ore
        HBox boxMonteOre = new HBox(12);
        boxMonteOre.setAlignment(Pos.CENTER_LEFT);
        boxMonteOre.setStyle("-fx-background-color: " + bgSubCard + "; -fx-border-color: " + borderCard
                + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 12;");

        ComboBox<String> comboModOre = new ComboBox<>(FXCollections.observableArrayList(
                "Ore settimanali fisse", "Intervallo settimanale"));
        comboModOre.setValue("Ore settimanali fisse");
        comboModOre.setMaxWidth(Double.MAX_VALUE);

        VBox colMod = creaCampoConEtichetta("🕒", "Monte ore", true, comboModOre, colLabel);
        HBox.setHgrow(colMod, Priority.ALWAYS);

        Label lblCampoOre = new Label("Ore fisse (es. 40)");
        lblCampoOre.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: " + colSub + ";");
        TextField txtOreMin = creaInput("Es. 40", provenza);
        txtOreMin.setPrefWidth(110);
        TextField txtOreMax = creaInput("Max (es. 30)", provenza);
        txtOreMax.setPrefWidth(100);
        txtOreMax.setVisible(false);
        txtOreMax.setManaged(false);

        HBox rigaCampiOre = new HBox(6, txtOreMin, txtOreMax);
        VBox colValOre = new VBox(4, lblCampoOre, rigaCampiOre);
        colValOre.setAlignment(Pos.CENTER_LEFT);
        boxMonteOre.getChildren().addAll(colMod, colValOre);

        // Riquadro destro — Limite massimo turni (opzionale)
        HBox boxMaxTurni = new HBox(12);
        boxMaxTurni.setAlignment(Pos.CENTER_LEFT);
        boxMaxTurni.setStyle("-fx-background-color: " + bgSubCard + "; -fx-border-color: " + borderCard
                + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 12;");

        VBox infoMax = new VBox(2);
        Label lblTitMax = new Label("📅 Limite massimo turni (opzionale)");
        lblTitMax.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + colLabel + ";");
        Label lblDescMax = new Label("Limita il numero di turni che il dipendente può svolgere in una settimana.");
        lblDescMax.setWrapText(true);
        lblDescMax.setStyle("-fx-font-size: 11px; -fx-text-fill: " + colSub + ";");
        infoMax.getChildren().addAll(lblTitMax, lblDescMax);
        HBox.setHgrow(infoMax, Priority.ALWAYS);

        ToggleSwitchPill toggleMax = new ToggleSwitchPill(false);
        Tooltip.install(toggleMax, new Tooltip("Attiva per impostare un numero massimo di turni a settimana"));

        VBox boxSpin = new VBox(3);
        Label lblSpin = new Label("Max turni / sett.");
        lblSpin.setMinWidth(Region.USE_PREF_SIZE);
        lblSpin.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + colSub + ";");
        Spinner<Integer> spinMaxTurni = new Spinner<>(1, 21, 5);
        spinMaxTurni.setEditable(true);
        spinMaxTurni.setPrefWidth(92);
        spinMaxTurni.setDisable(true);
        boxSpin.getChildren().addAll(lblSpin, spinMaxTurni);

        toggleMax.setOnCambio(attivo -> spinMaxTurni.setDisable(!attivo));

        boxMaxTurni.getChildren().addAll(infoMax, toggleMax, boxSpin);

        riga3.add(boxMonteOre, 0, 0);
        riga3.add(boxMaxTurni, 1, 0);

        // Riga 4: Azioni
        HBox rigaAzioni = new HBox(10);
        rigaAzioni.setAlignment(Pos.CENTER_LEFT);
        rigaAzioni.setPadding(new Insets(2, 0, 0, 0));

        Button btnDisp = new Button("📅 Configura disponibilità");
        btnDisp.setMinWidth(Region.USE_PREF_SIZE);
        btnDisp.setStyle(provenza
                ? "-fx-background-color: #f6f0ea; -fx-text-fill: #4b2654; -fx-border-color: #dcd0c8; "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;"
                : "-fx-background-color: #2b2733; -fx-text-fill: #f5ebe0; -fx-border-color: rgba(186,164,135,0.4); "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");
        btnDisp.setOnAction(e -> {
            if (onConfiguraDispNuovo != null) onConfiguraDispNuovo.run();
        });

        Region spAz = new Region();
        HBox.setHgrow(spAz, Priority.ALWAYS);

        Button btnSalva = new Button("+ Salva nuovo dipendente");
        btnSalva.setMinWidth(Region.USE_PREF_SIZE);
        btnSalva.setStyle(provenza
                ? "-fx-background-color: linear-gradient(to right, #8a5499, #653874); -fx-text-fill: #ffffff; "
                  + "-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 8 22; -fx-background-radius: 10; -fx-cursor: hand;"
                : "-fx-background-color: #baa487; -fx-text-fill: #141312; "
                  + "-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 8 22; -fx-background-radius: 10; -fx-cursor: hand;");
        btnSalva.setOnAction(e -> {
            if (onSalvaNuovoDipendente != null) onSalvaNuovoDipendente.run();
        });

        Button btnRimuovi = new Button("🗑 Rimuovi dipendente");
        btnRimuovi.setMinWidth(Region.USE_PREF_SIZE);
        btnRimuovi.setStyle(provenza
                ? "-fx-background-color: #fdf1f2; -fx-text-fill: #9e353f; -fx-border-color: #e5b8bd; "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;"
                : "-fx-background-color: rgba(127,29,29,0.55); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444; "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");
        btnRimuovi.setOnAction(e -> {
            if (onRimuoviSelezionato != null) onRimuoviSelezionato.run();
        });

        rigaAzioni.getChildren().addAll(btnDisp, spAz, btnSalva, btnRimuovi);
        bodyNuovo.getChildren().addAll(riga1, riga2, riga3, rigaAzioni);
        cardNuovo.getChildren().addAll(hdrNuovo, bodyNuovo);

        // C. CARD TABELLA "PERSONALE ATTUALMENTE ABILITATO"
        VBox cardTabella = new VBox(0);
        VBox.setVgrow(cardTabella, Priority.ALWAYS);
        cardTabella.setStyle("-fx-background-color: " + bgCard + "; -fx-border-color: " + borderCard
                + "; -fx-border-radius: 13; -fx-background-radius: 13;");
        if (provenza) {
            cardTabella.setEffect(new DropShadow(10, 0, 2, Color.rgb(75, 38, 84, 0.07)));
        }

        HBox hdrTab = creaHeaderCard("👥", "Personale attualmente abilitato", bgHeaderCard, borderCard, colTitolo, provenza);

        // Intestazione 7 colonne
        GridPane headerColonne = creaGrigliaRigaTabella();
        headerColonne.setStyle("-fx-background-color: " + (provenza ? "#f8eff5" : "rgba(35, 31, 42, 0.95)")
                + "; -fx-border-color: transparent transparent " + borderCard + " transparent; -fx-border-width: 1; -fx-padding: 8 14;");
        String[] titoliCol = {"👤 Nome", "✉ Email", "🕒 Monte ore", "📅 Disponibilità", "📅 Max turni", "ⓘ Stato / Note", "⚙ Azioni"};
        for (int i = 0; i < titoliCol.length; i++) {
            Label h = new Label(titoliCol[i]);
            h.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: " + colLabel + ";");
            headerColonne.add(h, i, 0);
        }

        VBox boxRigheTabella = new VBox(0);
        ScrollPane scrollTab = new ScrollPane(boxRigheTabella);
        scrollTab.setFitToWidth(true);
        scrollTab.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTab.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollTab, Priority.ALWAYS);

        cardTabella.getChildren().addAll(hdrTab, headerColonne, scrollTab);

        contenitore.setSpacing(12);
        contenitore.getChildren().addAll(headerPagina, cardNuovo, cardTabella);

        return new ComponentiForm(
                txtNome, txtCognome, txtEmail, txtPass,
                comboModOre, lblCampoOre, txtOreMin, txtOreMax,
                toggleMax, spinMaxTurni, btnDisp, boxRigheTabella);
    }

    /**
     * Popola la tabella "Personale attualmente abilitato" con i dati reali del database.
     */
    public static void popolaTabellaPersonale(ComponentiForm form,
                                              List<DipendenteBean> dipendenti,
                                              Consumer<DipendenteBean> onModifica,
                                              Consumer<DipendenteBean> onElimina) {
        if (form == null || form.boxRigheTabella == null) {
            return;
        }
        form.boxRigheTabella.getChildren().clear();
        boolean provenza = util.SessionManager.getInstance().isGui2Attiva();

        if (dipendenti == null || dipendenti.isEmpty()) {
            Label vuoto = new Label("Nessun dipendente registrato nell'organico.");
            vuoto.setStyle("-fx-padding: 18; -fx-font-style: italic; -fx-font-size: 12.5px; -fx-text-fill: "
                    + (provenza ? "#7a6880;" : "#a89f92;"));
            form.boxRigheTabella.getChildren().add(vuoto);
            return;
        }

        for (DipendenteBean d : dipendenti) {
            GridPane riga = creaGrigliaRigaTabella();
            boolean selezionato = form.getEmailSelezionata() != null
                    && form.getEmailSelezionata().equalsIgnoreCase(d.getEmail());

            String bgNorm = provenza
                    ? (selezionato ? "#f6eaf4" : "#fffdfb")
                    : (selezionato ? "rgba(65, 50, 75, 0.90)" : "rgba(24, 22, 28, 0.88)");
            String borderBottom = provenza ? "#efe5ec" : "rgba(186, 164, 135, 0.16)";
            String stileRiga = "-fx-background-color: " + bgNorm
                    + "; -fx-border-color: transparent transparent " + borderBottom + " transparent; "
                    + "-fx-border-width: 1; -fx-padding: 8 14; -fx-cursor: hand;";
            riga.setStyle(stileRiga);

            // Col 0: Nome
            Label lblNome = new Label("👤 " + d.getNome() + " " + d.getCognome());
            lblNome.setWrapText(true);
            lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

            // Col 1: Email
            Label lblEmail = new Label(d.getEmail());
            lblEmail.setWrapText(true);
            lblEmail.setStyle("-fx-font-size: 11.5px; -fx-text-fill: " + (provenza ? "#5e4b66;" : "#cfc6bc;"));

            // Col 2: Monte ore
            String oreTxt = d.getConfigurazioneOre().isConfigurato()
                    ? d.getConfigurazioneOre().getEtichettaObiettivo()
                    : "Non config.";
            Label lblOre = new Label(oreTxt);
            lblOre.setWrapText(true);
            lblOre.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: "
                    + (d.getConfigurazioneOre().isConfigurato()
                    ? (provenza ? "#37253d;" : "#f5ebe0;")
                    : "#b45309;"));

            // Col 3: Disponibilita'
            Label lblDisp = new Label(d.getDisponibilita().getRiepilogoSintetico());
            lblDisp.setWrapText(true);
            lblDisp.setStyle("-fx-font-size: 11.5px; -fx-text-fill: " + (provenza ? "#5e4b66;" : "#cfc6bc;"));

            // Col 4: Max turni (pillola lavanda o trattino)
            Node nodoMax;
            if (d.hasLimiteMaxTurni()) {
                Label pill = new Label(String.valueOf(d.getMaxTurniSettimanali()));
                pill.setStyle("-fx-background-color: " + (provenza ? "#eadcf0" : "rgba(186,164,135,0.22)")
                        + "; -fx-text-fill: " + (provenza ? "#4b2654" : "#f5ebe0")
                        + "; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-padding: 2 10; -fx-background-radius: 8;");
                nodoMax = pill;
            } else {
                Label dash = new Label("—");
                dash.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (provenza ? "#9a8aa0;" : "#8e857a;"));
                nodoMax = dash;
            }

            // Col 5: Stato / Note (stato reale dell'account)
            String statoTesto;
            String statoColore;
            if (!d.isAttivo()) {
                statoTesto = "● Disattivato";
                statoColore = "#b91c1c";
            } else if (!d.getConfigurazioneOre().isConfigurato()) {
                statoTesto = "● Config. richiesta";
                statoColore = "#b45309";
            } else {
                statoTesto = "● Attivo";
                statoColore = "#2e8540";
            }
            Label lblStato = new Label(statoTesto);
            lblStato.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: " + statoColore + ";");

            // Col 6: Azioni (Modifica + Elimina)
            Button btnEdit = new Button("✏");
            btnEdit.setTooltip(new Tooltip("Modifica profilo, monte ore, max turni e disponibilità"));
            btnEdit.setStyle(provenza
                    ? "-fx-background-color: #fbf7fa; -fx-text-fill: #5e316f; -fx-border-color: #e0cfe2; "
                      + "-fx-font-size: 11.5px; -fx-padding: 3 8; -fx-background-radius: 7; -fx-border-radius: 7; -fx-cursor: hand;"
                    : "-fx-background-color: #2f2a38; -fx-text-fill: #baa487; -fx-border-color: rgba(186,164,135,0.4); "
                      + "-fx-font-size: 11.5px; -fx-padding: 3 8; -fx-background-radius: 7; -fx-border-radius: 7; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> {
                e.consume();
                if (onModifica != null) onModifica.accept(d);
            });

            Button btnDel = new Button("🗑");
            btnDel.setTooltip(new Tooltip("Elimina o disattiva dipendente"));
            btnDel.setStyle(provenza
                    ? "-fx-background-color: #fdf1f2; -fx-text-fill: #b43c47; -fx-border-color: #ebc5c9; "
                      + "-fx-font-size: 11.5px; -fx-padding: 3 8; -fx-background-radius: 7; -fx-border-radius: 7; -fx-cursor: hand;"
                    : "-fx-background-color: rgba(127,29,29,0.55); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444; "
                      + "-fx-font-size: 11.5px; -fx-padding: 3 8; -fx-background-radius: 7; -fx-border-radius: 7; -fx-cursor: hand;");
            btnDel.setOnAction(e -> {
                e.consume();
                if (onElimina != null) onElimina.accept(d);
            });

            HBox azioni = new HBox(6, btnEdit, btnDel);
            azioni.setAlignment(Pos.CENTER_LEFT);

            riga.add(lblNome, 0, 0);
            riga.add(lblEmail, 1, 0);
            riga.add(lblOre, 2, 0);
            riga.add(lblDisp, 3, 0);
            riga.add(nodoMax, 4, 0);
            riga.add(lblStato, 5, 0);
            riga.add(azioni, 6, 0);

            riga.setOnMouseClicked(e -> {
                form.setEmailSelezionata(d.getEmail());
                popolaTabellaPersonale(form, dipendenti, onModifica, onElimina);
                if (e.getClickCount() == 2 && onModifica != null) {
                    onModifica.accept(d);
                }
            });

            form.boxRigheTabella.getChildren().add(riga);
        }
    }

    private static GridPane creaGrigliaRigaTabella() {
        GridPane gp = new GridPane();
        gp.setHgap(10);
        double[] perc = {18, 20, 12, 20, 10, 11, 9};
        for (double p : perc) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(p);
            cc.setHgrow(Priority.ALWAYS);
            gp.getColumnConstraints().add(cc);
        }
        return gp;
    }

    private static HBox creaHeaderCard(String icona, String titolo, String bg, String border, String colTitolo, boolean provenza) {
        HBox hdr = new HBox(8);
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 12 12 0 0; "
                + "-fx-border-color: transparent transparent " + border + " transparent; -fx-border-width: 1; -fx-padding: 8 15;");

        Label ico = new Label(icona);
        ico.setStyle(provenza
                ? "-fx-background-color: #7c4b8c; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 11px; "
                  + "-fx-padding: 1 6; -fx-background-radius: 5;"
                : "-fx-text-fill: #baa487; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label tit = new Label(titolo);
        tit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: " + colTitolo + ";");
        hdr.getChildren().addAll(ico, tit);
        return hdr;
    }

    private static VBox creaCampoConEtichetta(String icona, String testo, boolean obbligatorio, Node campo, String colLabel) {
        HBox lblRow = new HBox(4);
        lblRow.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(icona + " " + testo);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + colLabel + ";");
        lblRow.getChildren().add(l);
        if (obbligatorio) {
            Label star = new Label("*");
            star.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #c62828;");
            lblRow.getChildren().add(star);
        }
        return new VBox(4, lblRow, campo);
    }

    public enum SceltaRimozione {
        DISATTIVA,
        ELIMINA,
        ANNULLA
    }

    /**
     * Pop-up elegante coerente con l'identita' Frostly (Dark & Gold in GUI 1, Provenza in GUI 2)
     * per confermare la disattivazione sicura o l'eliminazione definitiva di un dipendente.
     */
    public static SceltaRimozione mostraDialogRimozioneDipendente(javafx.stage.Window owner,
                                                                  DipendenteBean dip,
                                                                  int turniStoriciCount) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Rimozione Dipendente");
        if (owner != null) {
            popup.initOwner(owner);
        }

        boolean provenza = util.SessionManager.getInstance().isGui2Attiva();
        final SceltaRimozione[] scelta = {SceltaRimozione.ANNULLA};

        VBox root = new VBox(14);
        root.setPrefWidth(520);
        root.setStyle(provenza
                ? "-fx-padding: 22 26; -fx-background-color: #fbf6f1; -fx-border-color: #e2d3df; -fx-border-width: 1.5;"
                : "-fx-padding: 22 26; -fx-background-color: #16141a; -fx-border-color: rgba(186,164,135,0.45); -fx-border-width: 1.2;");

        // Header con icona circolare + titolo
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane icoBox = new StackPane();
        icoBox.setMinSize(42, 42);
        icoBox.setPrefSize(42, 42);
        icoBox.setStyle(provenza
                ? "-fx-background-color: #fde8e8; -fx-border-color: #f3b8bc; -fx-border-radius: 21; -fx-background-radius: 21;"
                : "-fx-background-color: rgba(127, 29, 29, 0.45); -fx-border-color: #ef4444; -fx-border-radius: 21; -fx-background-radius: 21;");
        Label ico = new Label("🗑");
        ico.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (provenza ? "#b91c1c;" : "#fca5a5;"));
        icoBox.getChildren().add(ico);

        VBox titoli = new VBox(2);
        Label lblTit = new Label("Gestione rimozione profilo");
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: "
                + (provenza ? "#37253d;" : "#f5ebe0;"));
        Label lblNome = new Label(dip.getNome() + " " + dip.getCognome() + "  •  " + dip.getEmail());
        lblNome.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (provenza ? "#764c86;" : "#baa487;"));
        titoli.getChildren().addAll(lblTit, lblNome);
        header.getChildren().addAll(icoBox, titoli);

        // Card informativa
        VBox infoCard = new VBox(8);
        infoCard.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e9dbdf; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 14;"
                : "-fx-background-color: rgba(28, 25, 34, 0.92); -fx-border-color: rgba(186,164,135,0.25); -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 14;");

        if (turniStoriciCount > 0) {
            Label lWarn = new Label(dip.getNome() + " " + dip.getCognome() + " ha " + turniStoriciCount
                    + (turniStoriciCount == 1 ? " turno storico/assegnato" : " turni storici/assegnati")
                    + " nel sistema.");
            lWarn.setWrapText(true);
            lWarn.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: "
                    + (provenza ? "#9a5b13;" : "#fcd34d;"));

            Label lOpz1 = new Label("• Disattiva account (consigliato): conserva lo storico dei turni e lo esclude dalle future generazioni.");
            lOpz1.setWrapText(true);
            lOpz1.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (provenza ? "#4a3b52;" : "#ded7cd;"));

            Label lOpz2 = new Label("• Elimina definitivamente: rimuove account e tutti i turni collegati dal database.");
            lOpz2.setWrapText(true);
            lOpz2.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (provenza ? "#7a4449;" : "#fca5a5;"));

            infoCard.getChildren().addAll(lWarn, lOpz1, lOpz2);
        } else {
            Label lMsg = new Label("Vuoi eliminare definitivamente l'account di "
                    + dip.getNome() + " " + dip.getCognome() + "? L'operazione non può essere annullata.");
            lMsg.setWrapText(true);
            lMsg.setStyle("-fx-font-size: 12.5px; -fx-text-fill: " + (provenza ? "#37253d;" : "#ded7cd;"));
            infoCard.getChildren().add(lMsg);
        }

        // Pulsanti azione coerenti
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setMinWidth(Region.USE_PREF_SIZE);
        btnAnnulla.setStyle(provenza
                ? "-fx-background-color: transparent; -fx-text-fill: #5e316f; -fx-border-color: #d8bfd8; "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7 16; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.38); "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7 16; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> popup.close());

        Button btnElimina = new Button("Elimina definitivamente");
        btnElimina.setMinWidth(Region.USE_PREF_SIZE);
        btnElimina.setStyle(provenza
                ? "-fx-background-color: #fdf1f2; -fx-text-fill: #b43c47; -fx-border-color: #e5b8bd; "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7 15; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;"
                : "-fx-background-color: rgba(153, 27, 27, 0.78); -fx-text-fill: #fee2e2; -fx-border-color: #ef4444; "
                  + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7 15; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        btnElimina.setOnAction(e -> {
            scelta[0] = SceltaRimozione.ELIMINA;
            popup.close();
        });

        if (turniStoriciCount > 0) {
            Button btnDisattiva = new Button("⏸ Disattiva account");
            btnDisattiva.setMinWidth(Region.USE_PREF_SIZE);
            btnDisattiva.setStyle(provenza
                    ? "-fx-background-color: linear-gradient(to right, #8a5499, #653874); -fx-text-fill: #ffffff; "
                      + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 18; -fx-background-radius: 10; -fx-cursor: hand;"
                    : "-fx-background-color: #baa487; -fx-text-fill: #141312; "
                      + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 18; -fx-background-radius: 10; -fx-cursor: hand;");
            btnDisattiva.setOnAction(e -> {
                scelta[0] = SceltaRimozione.DISATTIVA;
                popup.close();
            });
            footer.getChildren().addAll(btnAnnulla, btnElimina, btnDisattiva);
        } else {
            footer.getChildren().addAll(btnAnnulla, btnElimina);
        }

        root.getChildren().addAll(header, infoCard, footer);
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(scene);
        popup.centerOnScreen();
        popup.showAndWait();
        return scelta[0];
    }

    private static TextField creaInput(String prompt, boolean provenza) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #ddd0da; -fx-border-radius: 8; -fx-background-radius: 8; "
                  + "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: #37253d;"
                : "-fx-background-color: #1f1c24; -fx-border-color: rgba(186,164,135,0.35); -fx-border-radius: 8; -fx-background-radius: 8; "
                  + "-fx-padding: 6 10; -fx-font-size: 12px; -fx-text-fill: #f5ebe0;");
        return tf;
    }
}
