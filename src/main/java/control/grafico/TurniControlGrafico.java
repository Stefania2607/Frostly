package control.grafico;

import bean.EmergenzaBean;
import bean.GustoBean;
import bean.IngredienteBean;
import bean.NotificaBean;
import bean.RichiestaGustoBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'maestro.fxml', 'dipendente.fxml',
 * 'maestro_gui2.fxml' e 'dipendente_gui2.fxml'.
 * Gestisce turni, emergenze, richieste gusto e l'aggiornamento in tempo reale del Menu Vetrina su DB.
 */
public class TurniControlGrafico {

    @FXML
    private Label lblIntestazione;
    @FXML
    private Label lblEsito;
    @FXML
    private ListView<String> listaTurniDipendente;
    @FXML
    private ListView<String> listaTuttiTurni;
    @FXML
    private ListView<String> listaEmergenze;
    @FXML
    private ListView<String> listaRichiesteGusto;
    @FXML
    private ComboBox<String> comboCategoriaMenu;
    @FXML
    private ComboBox<String> comboSottocategoriaMenu;
    @FXML
    private TextField campoImmagineMenu;
    @FXML
    private ListView<String> listaDipendentiStaff;
    @FXML
    private TextField campoNomeNuovoDip;
    @FXML
    private TextField campoCognomeNuovoDip;
    @FXML
    private TextField campoEmailNuovoDip;
    @FXML
    private TextField campoPasswordNuovoDip;
    @FXML
    private HBox boxCategorieMenu;
    @FXML
    private HBox boxSottocategorieMenu;
    @FXML
    private javafx.scene.layout.FlowPane grigliaMenuGestione;
    private LoginControlGrafico.VetrinaMenu vetrinaGestione;
    private final java.util.Map<String, GustoBean> menuCorrente = new java.util.LinkedHashMap<>();

    @FXML
    private ListView<IngredienteBean> listaMagazzino;
    @FXML
    private Label lblRiepilogoMagazzino;
    @FXML
    private ListView<String> listaMenuGestione;
    @FXML
    private ListView<String> listaNotifiche;

    @FXML
    private ComboBox<String> comboMotivazioneRapida;
    @FXML
    private ComboBox<String> comboDipendenteTurno;
    @FXML
    private ComboBox<String> comboGiornoTurno;
    @FXML
    private ComboBox<String> comboSostituto;

    @FXML
    private TextField campoCodiceTurno;
    @FXML
    private TextField campoMotivazione;
    @FXML
    private TextField campoEmailDip;
    @FXML
    private TextField campoGiorno;
    @FXML
    private TextField campoOraInizio;
    @FXML
    private TextField campoOraFine;

    @FXML
    private TextField campoCodEmergenza;
    @FXML
    private TextField campoEmailSostituto;
    @FXML
    private TextField campoNotaMaestro;
    @FXML
    private TextField campoCodRichiesta;
    @FXML
    private TextField campoRispostaGusto;

    @FXML
    private TextField campoNomeGustoMenu;
    @FXML
    private TextField campoDescGustoMenu;
    @FXML
    private TextField campoAllergeniGustoMenu;
    @FXML
    private TextField campoPrezzoGustoMenu;

    @FXML
    public void initialize() {
        if (lblIntestazione != null) {
            lblIntestazione.setText("Profilo Attivo: " + SessionManager.getInstance().getNomeUtenteLoggato()
                    + " (" + SessionManager.getInstance().getModalita() + ")");
        }
        configuraTendineESelezioneClick();
        aggiornaListe();
    }

    private void configuraTendineESelezioneClick() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();

        if (comboMotivazioneRapida != null) {
            comboMotivazioneRapida.setItems(FXCollections.observableArrayList(
                    "Visita medica improvvisa",
                    "Imprevisto familiare urgente",
                    "Guasto mezzo di trasporto",
                    "Febbre / Indisposizione"));
            comboMotivazioneRapida.setOnAction(e -> {
                if (comboMotivazioneRapida.getValue() != null && campoMotivazione != null) {
                    campoMotivazione.setText(comboMotivazioneRapida.getValue());
                }
            });
        }

        if (comboDipendenteTurno != null) {
            comboDipendenteTurno.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            comboDipendenteTurno.setOnAction(e -> {
                if (comboDipendenteTurno.getValue() != null && campoEmailDip != null) {
                    campoEmailDip.setText(comboDipendenteTurno.getValue());
                }
            });
        }

        if (comboSostituto != null) {
            comboSostituto.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            comboSostituto.setOnAction(e -> {
                if (comboSostituto.getValue() != null && campoEmailSostituto != null) {
                    campoEmailSostituto.setText(comboSostituto.getValue());
                }
            });
        }

        if (comboGiornoTurno != null) {
            comboGiornoTurno.setItems(FXCollections.observableArrayList(
                    "Lunedi", "Martedi", "Mercoledi", "Giovedi", "Venerdi", "Sabato", "Domenica"));
            comboGiornoTurno.setOnAction(e -> {
                if (comboGiornoTurno.getValue() != null && campoGiorno != null) {
                    campoGiorno.setText(comboGiornoTurno.getValue());
                }
            });
        }

        if (listaTurniDipendente != null) {
            listaTurniDipendente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoCodiceTurno != null) {
                    campoCodiceTurno.setText(newVal.split("\\|")[0].trim());
                }
            });
        }

        if (listaEmergenze != null) {
            listaEmergenze.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoCodEmergenza != null) {
                    campoCodEmergenza.setText(newVal.split("\\|")[0].trim());
                }
            });
        }

        if (listaRichiesteGusto != null) {
            listaRichiesteGusto.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoCodRichiesta != null) {
                    campoCodRichiesta.setText(newVal.split("\\|")[0].trim());
                }
            });
        }

        if (listaMagazzino != null) {
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

        if (comboCategoriaMenu != null) {
            comboCategoriaMenu.setItems(FXCollections.observableArrayList(
                    "Gelati", "Caffetteria", "Biscotti", "Crostate", "Tisane"));
            comboCategoriaMenu.setValue("Gelati");
            comboCategoriaMenu.valueProperty().addListener((obs, oldVal, newVal) -> aggiornaSottocategorieMenu(newVal));
            aggiornaSottocategorieMenu("Gelati");
        }

        if (grigliaMenuGestione != null) {
            vetrinaGestione = new LoginControlGrafico.VetrinaMenu(
                    boxCategorieMenu, boxSottocategorieMenu, grigliaMenuGestione, this::riempiFormGusto);
        }

        if (listaMenuGestione != null) {
            listaMenuGestione.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || !newVal.contains("|")) {
                    return;
                }
                GustoBean g = menuCorrente.get(newVal.split("\\|")[0].trim());
                if (g != null) {
                    riempiFormGusto(g);
                }
            });
        }
    }

    private void riempiFormGusto(GustoBean g) {
        campoNomeGustoMenu.setText(g.getNome());
        campoPrezzoGustoMenu.setText(String.valueOf(g.getPrezzoAlKg()));
        campoDescGustoMenu.setText(g.getDescrizione());
        campoAllergeniGustoMenu.setText(g.getAllergeni());
        if (comboCategoriaMenu != null) {
            comboCategoriaMenu.setValue(g.getCategoria());
            aggiornaSottocategorieMenu(g.getCategoria());
            comboSottocategoriaMenu.getEditor().setText(g.getSottocategoria());
            comboSottocategoriaMenu.setValue(g.getSottocategoria());
        }
        if (campoImmagineMenu != null) {
            campoImmagineMenu.setText(g.getImmagineUrl());
        }
    }

    private void aggiornaSottocategorieMenu(String categoria) {
        if (comboSottocategoriaMenu == null) {
            return;
        }
        java.util.List<String> opzioni = new java.util.ArrayList<>();
        if ("Gelati".equals(categoria)) {
            opzioni.addAll(java.util.Arrays.asList("Creme", "Frutta", "Cioccolato"));
        } else if ("Caffetteria".equals(categoria)) {
            opzioni.addAll(java.util.Arrays.asList("Caffe", "Specialita"));
        } else if ("Biscotti".equals(categoria)) {
            opzioni.add("Secchi");
        } else if ("Crostate".equals(categoria)) {
            opzioni.addAll(java.util.Arrays.asList("Classiche", "Alla frutta"));
        } else if ("Tisane".equals(categoria)) {
            opzioni.add("Infusi");
        }
        for (GustoBean g : menuCorrente.values()) {
            if (g.getCategoria().equals(categoria) && !opzioni.contains(g.getSottocategoria())) {
                opzioni.add(g.getSottocategoria());
            }
        }
        comboSottocategoriaMenu.setItems(FXCollections.observableArrayList(opzioni));
        if (!opzioni.isEmpty()) {
            comboSottocategoriaMenu.setValue(opzioni.get(0));
        }
    }

    private static String testoCombo(ComboBox<String> combo, String predefinito) {
        if (combo == null) {
            return predefinito;
        }
        String t = combo.isEditable() && combo.getEditor() != null ? combo.getEditor().getText() : combo.getValue();
        return (t == null || t.trim().isEmpty()) ? predefinito : t.trim();
    }

    @FXML
    public void onClickGeneraPassword() {
        String alfabeto = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(alfabeto.charAt(random.nextInt(alfabeto.length())));
        }
        campoPasswordNuovoDip.setText(sb.toString());
    }

    @FXML
    public void onClickCreaDipendente() {
        try {
            bean.LoginBean nuovo = new bean.LoginBean();
            nuovo.setNome(campoNomeNuovoDip.getText());
            nuovo.setCognome(campoCognomeNuovoDip.getText());
            nuovo.setEmail(campoEmailNuovoDip.getText());
            nuovo.setPassword(campoPasswordNuovoDip.getText());
            bean.LoginBean creato = new LoginControl().creaProfiloDipendente(nuovo);
            mostraSuccesso("✅ Profilo dipendente creato per " + creato.getNomeCompleto()
                    + ". Comunicagli email e password: " + creato.getEmail() + " / " + creato.getPassword());
            campoNomeNuovoDip.clear();
            campoCognomeNuovoDip.clear();
            campoEmailNuovoDip.clear();
            campoPasswordNuovoDip.clear();
            GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
            if (comboDipendenteTurno != null) {
                comboDipendenteTurno.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            }
            if (comboSostituto != null) {
                comboSostituto.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            }
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickScegliFoto() {
        javafx.stage.FileChooser scelta = new javafx.stage.FileChooser();
        scelta.setTitle("Scegli la foto del prodotto");
        scelta.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter(
                "Immagini (png, jpg, jpeg, gif)", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        java.io.File file = scelta.showOpenDialog(campoImmagineMenu.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            // La foto viene copiata nella cartella 'immagini_menu' dell'app: resta valida anche se l'originale viene spostato
            java.nio.file.Path cartella = java.nio.file.Paths.get("immagini_menu");
            java.nio.file.Files.createDirectories(cartella);
            String pulito = file.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            java.nio.file.Path destinazione = cartella.resolve(System.currentTimeMillis() + "_" + pulito);
            java.nio.file.Files.copy(file.toPath(), destinazione);
            campoImmagineMenu.setText(destinazione.toAbsolutePath().toUri().toString());
            mostraSuccesso("🖼 Foto caricata: " + file.getName() + ". Premi 'Salva / Aggiorna' per applicarla.");
        } catch (java.io.IOException e) {
            mostraErrore("⚠️ Impossibile copiare la foto: " + e.getMessage());
        }
    }

    @FXML
    public void onClickNuovoGustoMenu() {
        if (listaMenuGestione != null) {
            listaMenuGestione.getSelectionModel().clearSelection();
        }
        if (vetrinaGestione != null) {
            vetrinaGestione.deseleziona();
        }
        campoNomeGustoMenu.clear();
        campoPrezzoGustoMenu.clear();
        campoAllergeniGustoMenu.clear();
        campoDescGustoMenu.clear();
        if (campoImmagineMenu != null) {
            campoImmagineMenu.clear();
        }
        campoNomeGustoMenu.requestFocus();
    }

    @FXML
    public void onClickSalvaGustoMenu() {
        try {
            new ProduzioneOrdiniControl().salvaOAggiornaGustoMenu(
                    campoNomeGustoMenu.getText(),
                    campoDescGustoMenu.getText(),
                    campoAllergeniGustoMenu.getText(),
                    campoPrezzoGustoMenu.getText(),
                    true,
                    testoCombo(comboCategoriaMenu, "Gelati"),
                    testoCombo(comboSottocategoriaMenu, "Creme"),
                    campoImmagineMenu == null ? "" : campoImmagineMenu.getText());
            mostraSuccesso("✅ Menu aggiornato con successo: '" + campoNomeGustoMenu.getText() + "' salvato!");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickCambiaDisponibilitaGusto() {
        try {
            new ProduzioneOrdiniControl().cambiaDisponibilitaGustoMenu(campoNomeGustoMenu.getText());
            mostraSuccesso("✅ Disponibilita' in vetrina aggiornata per: " + campoNomeGustoMenu.getText());
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickRimuoviGustoMenu() {
        try {
            String nome = campoNomeGustoMenu.getText();
            new ProduzioneOrdiniControl().eliminaGustoMenu(nome);
            mostraSuccesso("🗑️ Gusto '" + nome + "' rimosso dal menu.");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickPubblicaTurno() {
        try {
            TurnoBean bean = new TurnoBean();
            bean.setEmailDipendente(campoEmailDip.getText());
            bean.setData(campoGiorno.getText());
            bean.setOraInizio(campoOraInizio.getText());
            bean.setOraFine(campoOraFine.getText());

            TurnoBean creato = new GestioneTurniEmergenzeControl().pubblicaNuovoTurno(bean);
            mostraSuccesso("✅ Turno pubblicato con successo: " + creato.getCodiceTurno());
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickSegnalaEmergenza() {
        try {
            EmergenzaBean bean = new EmergenzaBean();
            bean.setEmailRichiedente(SessionManager.getInstance().getEmailUtenteLoggato());
            bean.setCodiceTurno(campoCodiceTurno.getText());
            bean.setMotivazione(campoMotivazione.getText());

            EmergenzaBean creata = new GestioneTurniEmergenzeControl().segnalaEmergenzaTurno(bean);
            mostraSuccesso("✅ Emergenza inviata al Maestro! Codice: " + creata.getCodiceEmergenza());
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickApprovaEmergenza() {
        try {
            new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(
                    campoCodEmergenza.getText(), campoEmailSostituto.getText(), campoNotaMaestro.getText());
            mostraSuccesso("✅ Emergenza approvata e turno riassegnato al sostituto!");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifiutaEmergenza() {
        try {
            new GestioneTurniEmergenzeControl().rifiutaEmergenza(
                    campoCodEmergenza.getText(), campoNotaMaestro.getText());
            mostraSuccesso("ℹ️ Emergenza rifiutata e dipendente notificato.");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickApprovaGusto() {
        try {
            new RichiestaGustoControl().approvaRichiestaGusto(
                    campoCodRichiesta.getText(), campoRispostaGusto.getText());
            mostraSuccesso("✅ Richiesta gusto approvata e ingrediente scalato dal magazzino!");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifornisciGusto() {
        try {
            new RichiestaGustoControl().approvaConRifornimentoUrgente(
                    campoCodRichiesta.getText(), 5.0, campoRispostaGusto.getText());
            mostraSuccesso("✅ Rifornimento urgente (+5 Kg) effettuato e gusto approvato!");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifiutaGusto() {
        try {
            new RichiestaGustoControl().rifiutaRichiestaGusto(
                    campoCodRichiesta.getText(), campoRispostaGusto.getText());
            mostraSuccesso("ℹ️ Richiesta gusto rifiutata.");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickLogout() {
        SessionManager.getInstance().logout();
        LoginControlGrafico.cambiaSchermata("/login.fxml");
    }

    private void mostraSuccesso(String msg) {
        if (lblEsito != null) {
            lblEsito.setStyle("-fx-text-fill: #2a9d8f; -fx-font-weight: bold; -fx-font-size: 13px;");
            lblEsito.setText(msg);
        }
    }

    private void mostraErrore(String msg) {
        if (lblEsito != null) {
            lblEsito.setStyle("-fx-text-fill: #d62828; -fx-font-weight: bold; -fx-font-size: 13px;");
            lblEsito.setText(msg);
        }
    }

    private void aggiornaListe() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        ProduzioneOrdiniControl prodCtrl = new ProduzioneOrdiniControl();
        String emailUtente = SessionManager.getInstance().getEmailUtenteLoggato();

        try {
            if (listaTurniDipendente != null) {
                listaTurniDipendente.getItems().clear();
                for (TurnoBean t : turniCtrl.visualizzaTurniDipendente(emailUtente)) {
                    listaTurniDipendente.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                            + " (" + t.getOraInizio() + "-" + t.getOraFine() + ") | Stato: " + t.getStato());
                }
            }
            if (listaTuttiTurni != null) {
                listaTuttiTurni.getItems().clear();
                for (TurnoBean t : turniCtrl.visualizzaTuttiTurni()) {
                    listaTuttiTurni.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                            + " (" + t.getOraInizio() + "-" + t.getOraFine() + ") | "
                            + t.getNomeDipendente() + " [" + t.getStato() + "]");
                }
            }
        } catch (FrostlyException ignored) {
            // Ignorato in caricamento vista
        }

        if (listaEmergenze != null) {
            listaEmergenze.getItems().clear();
            for (EmergenzaBean e : turniCtrl.visualizzaTutteEmergenze()) {
                if (!"IN_ATTESA".equals(e.getStato())) {
                    continue; // Le richieste approvate/rifiutate spariscono dalla lista
                }
                listaEmergenze.getItems().add(e.getCodiceEmergenza() + " | Turno " + e.getCodiceTurno()
                        + " | Da: " + e.getNomeRichiedente() + " | Motivo: " + e.getMotivazione()
                        + " | Stato: " + e.getStato() + " | Sostituto: " + e.getNomeSostituto());
            }
        }

        if (listaRichiesteGusto != null) {
            listaRichiesteGusto.getItems().clear();
            for (RichiestaGustoBean r : new RichiestaGustoControl().visualizzaTutteRichieste()) {
                listaRichiesteGusto.getItems().add(r.getCodiceRichiesta() + " | Cliente: " + r.getNomeCliente()
                        + " | Gusto: '" + r.getNomeGustoProposto() + "' (" + r.getQuantitaRichiestaKg() + " kg)"
                        + " | Ing: " + r.getNomeIngredientePrincipale() + " | Stato: " + r.getStato());
            }
        }

        if (listaMagazzino != null) {
            listaMagazzino.getItems().clear();
            int sottoSoglia = 0;
            for (IngredienteBean ing : prodCtrl.visualizzaMagazzino()) {
                listaMagazzino.getItems().add(ing);
                if (ing.isSottoSoglia()) {
                    sottoSoglia++;
                }
            }
            if (lblRiepilogoMagazzino != null) {
                lblRiepilogoMagazzino.setText(sottoSoglia == 0
                        ? "✅ Tutte le scorte sono sufficienti"
                        : "⚠️ " + sottoSoglia + " ingredienti sotto soglia: da riordinare");
            }
        }

        if (listaMenuGestione != null || vetrinaGestione != null) {
            if (listaMenuGestione != null) {
                listaMenuGestione.getItems().clear();
            }
            menuCorrente.clear();
            java.util.List<GustoBean> menu = prodCtrl.visualizzaMenuGusti();
            for (GustoBean g : menu) {
                menuCorrente.put(g.getNome(), g);
                if (listaMenuGestione != null) {
                    String disp = g.isDisponibileOggi() ? "🟢 IN VETRINA" : "🔴 ESAURITO";
                    String unita = "Gelati".equalsIgnoreCase(g.getCategoria()) ? "/kg" : "";
                    listaMenuGestione.getItems().add(g.getNome() + " | EUR " + g.getPrezzoAlKg() + unita + " | "
                            + g.getDescrizione() + " | Allergeni: " + g.getAllergeni() + " | " + disp
                            + " | 📂 " + g.getCategoria() + " › " + g.getSottocategoria()
                            + (g.getImmagineUrl().isEmpty() ? "" : " | 🖼"));
                }
            }
            if (vetrinaGestione != null) {
                vetrinaGestione.carica(menu);
            }
        }

        if (listaDipendentiStaff != null) {
            listaDipendentiStaff.getItems().setAll(new LoginControl().elencoDipendenti());
        }

        if (listaNotifiche != null) {
            listaNotifiche.getItems().clear();
            for (NotificaBean n : new LoginControl().recuperaNotificheUtente(emailUtente)) {
                listaNotifiche.getItems().add("[" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
            }
        }
    }
}
