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
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'maestro.fxml', 'dipendente.fxml',
 * 'maestro_gui2.fxml' e 'dipendente_gui2.fxml'.
 * Gestisce turni, emergenze, richieste gusto e l'aggiornamento in tempo reale del Menu Vetrina su DB.
 */
public class TurniControlGrafico {

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    @FXML
    private TabPane tabPaneMaestro;
    @FXML
    private Button btnTendinaUrgenze;
    @FXML
    private VBox tendinaUrgenze;
    @FXML
    private ListView<String> listaUrgenzeMagazzino;
    @FXML
    private ListView<String> listaNotificheUrgenti;
    @FXML
    private TextField campoIngredienteUrgente;
    @FXML
    private TextField campoNomeIngredienteRifornimento;
    @FXML
    private TextField campoKgRifornimento;
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
    private Label lblCategoriaFormMenu;
    private String categoriaCorrenteSelezionata = "Gelati";
    private String sottocategoriaCorrenteSelezionata = null;
    @FXML
    private ComboBox<String> comboCategoriaMenu;
    @FXML
    private ComboBox<String> comboSottocategoriaMenu;
    @FXML
    private TextField campoImmagineMenu;
    @FXML
    private Label lblStatoFotoMenu;
    @FXML
    private Button btnRimuoviFoto;
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
    private final java.util.Map<String, EmergenzaBean> mappaEmergenzeAttive = new java.util.LinkedHashMap<>();

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
        if (tendinaUrgenze != null) {
            tendinaUrgenze.setVisible(false);
            tendinaUrgenze.setManaged(false);
        }
        configuraTendineESelezioneClick();
        aggiornaBadgeReparto();
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
                if (newVal != null) {
                    EmergenzaBean e = mappaEmergenzeAttive.get(newVal);
                    if (e != null) {
                        javafx.application.Platform.runLater(() -> apriPopupDettaglioEmergenza(e));
                    }
                }
            });
        }

        if (listaUrgenzeMagazzino != null) {
            listaUrgenzeMagazzino.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|")) {
                    String nome = newVal.split("\\|")[0].replace("⚠️", "").trim();
                    selezionaTab(3); // Porta direttamente al Magazzino
                    if (campoNomeIngredienteRifornimento != null) {
                        campoNomeIngredienteRifornimento.setText(nome);
                    }
                    if (campoKgRifornimento != null) {
                        campoKgRifornimento.requestFocus();
                    }
                }
            });
        }

        if (listaMagazzino != null) {
            listaMagazzino.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && campoNomeIngredienteRifornimento != null) {
                    campoNomeIngredienteRifornimento.setText(newVal.getNome());
                    if (campoKgRifornimento != null) {
                        campoKgRifornimento.requestFocus();
                    }
                }
            });
        }

        if (listaDipendentiStaff != null) {
            listaDipendentiStaff.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoEmailNuovoDip != null) {
                    String[] parti = newVal.split("\\|");
                    if (parti.length >= 2) {
                        campoEmailNuovoDip.setText(parti[1].trim());
                        String[] nc = parti[0].trim().split(" ");
                        if (nc.length >= 1 && campoNomeNuovoDip != null) campoNomeNuovoDip.setText(nc[0]);
                        if (nc.length >= 2 && campoCognomeNuovoDip != null) campoCognomeNuovoDip.setText(nc[1]);
                    }
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
                    boxCategorieMenu, boxSottocategorieMenu, grigliaMenuGestione,
                    this::riempiFormGusto, this::alCambioFiltroVetrina);
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

    private void aggiornaStatoFoto(String nomeFile) {
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

    @FXML
    public void onClickRimuoviFoto() {
        if (campoImmagineMenu != null) {
            campoImmagineMenu.clear();
        }
        aggiornaStatoFoto(null);
        aggiornaStatoFoto(null);
    }

    private void alCambioFiltroVetrina(String cat, String sotto) {
        this.categoriaCorrenteSelezionata = cat;
        this.sottocategoriaCorrenteSelezionata = sotto;
        aggiornaBadgeReparto();
    }

    private void aggiornaBadgeReparto() {
        if (lblCategoriaFormMenu != null) {
            String testo = "📂 Reparto: " + LoginControlGrafico.emojiCategoria(categoriaCorrenteSelezionata) + " " + categoriaCorrenteSelezionata;
            if (sottocategoriaCorrenteSelezionata != null && !sottocategoriaCorrenteSelezionata.trim().isEmpty()) {
                testo += " › " + sottocategoriaCorrenteSelezionata;
            }
            lblCategoriaFormMenu.setText(testo);
        }
        if (campoPrezzoGustoMenu != null) {
            boolean isGelato = "Gelati".equalsIgnoreCase(categoriaCorrenteSelezionata);
            campoPrezzoGustoMenu.setPromptText(isGelato ? "Prezzo €/kg" : "Prezzo €");
        }
    }

    private String sottocategoriaPredefinita(String cat) {
        if ("Caffetteria".equalsIgnoreCase(cat)) return "Caffe";
        if ("Biscotti".equalsIgnoreCase(cat)) return "Secchi";
        if ("Crostate".equalsIgnoreCase(cat)) return "Classiche";
        if ("Tisane".equalsIgnoreCase(cat)) return "Infusi";
        return "Creme";
    }

    private void riempiFormGusto(GustoBean g) {
        campoNomeGustoMenu.setText(g.getNome());
        campoPrezzoGustoMenu.setText(String.valueOf(g.getPrezzoAlKg()));
        campoDescGustoMenu.setText(g.getDescrizione());
        campoAllergeniGustoMenu.setText(g.getAllergeni());
        this.categoriaCorrenteSelezionata = g.getCategoria();
        this.sottocategoriaCorrenteSelezionata = g.getSottocategoria();
        aggiornaBadgeReparto();
        if (campoImmagineMenu != null) {
            campoImmagineMenu.setText(g.getImmagineUrl());
        }
        aggiornaStatoFoto(g.getImmagineUrl());
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(alfabeto.charAt(SECURE_RANDOM.nextInt(alfabeto.length())));
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
    public void onClickEliminaDipendente() {
        try {
            String email = campoEmailNuovoDip != null ? campoEmailNuovoDip.getText() : null;
            if (email == null || email.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona prima un dipendente dalla lista sotto per eliminarlo.");
                return;
            }
            new LoginControl().eliminaProfiloDipendente(email.trim());
            mostraSuccesso("🗑️ Dipendente '" + email.trim() + "' eliminato con successo dallo staff.");
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

    private void apriPopupDettaglioEmergenza(EmergenzaBean e) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Gestione Emergenza " + e.getCodiceEmergenza());
        if (tendinaUrgenze != null && tendinaUrgenze.getScene() != null) {
            popup.initOwner(tendinaUrgenze.getScene().getWindow());
        }

        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 24; -fx-background-color: #fffdfa;");
        root.setPrefWidth(540);

        Label lblTitolo = new Label("🚨 RICHIESTA DI SOSTITUZIONE URGENTE");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #bc4749;");
        Label lblSub = new Label("Codice: " + e.getCodiceEmergenza() + " • Turno: " + e.getCodiceTurno());
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #7f5539;");

        // Scheda info dipendente e turno
        VBox cardInfo = new VBox(6);
        cardInfo.setStyle("-fx-padding: 12 16; -fx-background-color: #f7ebe1; -fx-background-radius: 10; -fx-border-color: #e3d5ca; -fx-border-radius: 10;");
        Label lblDip = new Label("👤 Dipendente: " + e.getNomeRichiedente() + " (" + e.getEmailRichiedente() + ")");
        lblDip.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #3a2618;");
        String turnoDettagli = e.getDataTurno() != null ? (e.getDataTurno() + " ore " + e.getOrarioTurno()) : "Turno di lavoro programmato";
        Label lblTurno = new Label("📅 Turno coinvolto: " + e.getCodiceTurno() + " • " + turnoDettagli);
        lblTurno.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #5c3a21;");
        cardInfo.getChildren().addAll(lblDip, lblTurno);

        // Sezione motivazione inserita a mano dal dipendente
        VBox cardMotivo = new VBox(6);
        cardMotivo.setStyle("-fx-padding: 12 16; -fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #d5bdaf; -fx-border-radius: 10;");
        Label lblMotivoTitolo = new Label("💬 Motivo comunicato dal dipendente:");
        lblMotivoTitolo.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #bc4749;");
        Label lblMotivoTesto = new Label("\"" + e.getMotivazione() + "\"");
        lblMotivoTesto.setWrapText(true);
        lblMotivoTesto.setStyle("-fx-font-size: 13.5px; -fx-font-style: italic; -fx-text-fill: #3a2618; -fx-font-family: 'Georgia';");
        cardMotivo.getChildren().addAll(lblMotivoTitolo, lblMotivoTesto);

        // Sezione modifica e riassegnazione turno
        VBox cardAzione = new VBox(8);
        Label lblSostituto = new Label("🔁 Scegli il dipendente per coprire e modificare il turno:");
        lblSostituto.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #3a2618;");

        ComboBox<String> comboSostitutoModal = new ComboBox<>();
        comboSostitutoModal.setMaxWidth(Double.MAX_VALUE);
        comboSostitutoModal.setPromptText("Seleziona sostituto dallo staff...");
        for (String dipEmail : new GestioneTurniEmergenzeControl().getEmailTuttiDipendenti()) {
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

        // Pulsanti di risposta rapida
        HBox boxBottoni = new HBox(10);
        boxBottoni.setAlignment(Pos.CENTER_RIGHT);

        Button btnApprova = new Button("✔ Approva e Modifica Turno");
        btnApprova.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 8 16; -fx-cursor: hand;");
        btnApprova.setOnAction(evt -> {
            try {
                String sost = comboSostitutoModal.getValue();
                if (sost == null || sost.trim().isEmpty()) {
                    mostraErrore("⚠️ Seleziona prima un sostituto per riassegnare il turno.");
                    return;
                }
                new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(
                        e.getCodiceEmergenza(), sost.trim(), txtNota.getText());
                popup.close();
                mostraSuccesso("✅ Emergenza approvata: Turno " + e.getCodiceTurno() + " riassegnato a " + sost);
                aggiornaListe();
            } catch (FrostlyException ex) {
                mostraErrore("⚠️ " + ex.getMessage());
            }
        });

        Button btnRifiuta = new Button("✖ Rifiuta Richiesta");
        btnRifiuta.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 8 16; -fx-cursor: hand;");
        btnRifiuta.setOnAction(evt -> {
            try {
                new GestioneTurniEmergenzeControl().rifiutaEmergenza(e.getCodiceEmergenza(), txtNota.getText());
                popup.close();
                mostraSuccesso("ℹ️ Emergenza " + e.getCodiceEmergenza() + " rifiutata.");
                aggiornaListe();
            } catch (FrostlyException ex) {
                mostraErrore("⚠️ " + ex.getMessage());
            }
        });

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: #f5ebe0; -fx-text-fill: #5c3a21; -fx-background-radius: 16; -fx-padding: 8 14; -fx-cursor: hand;");
        btnChiudi.setOnAction(evt -> popup.close());

        boxBottoni.getChildren().addAll(btnChiudi, btnRifiuta, btnApprova);

        root.getChildren().addAll(lblTitolo, lblSub, cardInfo, cardMotivo, cardAzione, boxBottoni);
        popup.setScene(new javafx.scene.Scene(root));
        popup.showAndWait();
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
            java.nio.file.Path cartella = java.nio.file.Paths.get("immagini_menu");
            java.nio.file.Files.createDirectories(cartella);
            java.nio.file.Path cartellaRes = java.nio.file.Paths.get("src/main/resources/immagini_menu");
            if (java.nio.file.Files.exists(java.nio.file.Paths.get("src/main/resources"))) {
                java.nio.file.Files.createDirectories(cartellaRes);
            }
            String pulito = file.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            String nomeUnivoco = System.currentTimeMillis() + "_" + pulito;
            java.nio.file.Path destinazione = cartella.resolve(nomeUnivoco);
            java.nio.file.Files.copy(file.toPath(), destinazione, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            if (java.nio.file.Files.exists(cartellaRes)) {
                java.nio.file.Files.copy(destinazione, cartellaRes.resolve(nomeUnivoco), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            // Salviamo il percorso relativo portatile: funziona su qualsiasi computer (Mac, Windows, Linux)
            campoImmagineMenu.setText("immagini_menu/" + nomeUnivoco);
            aggiornaStatoFoto(file.getName());
            mostraSuccesso("🖼 Foto caricata: " + file.getName() + ". Premi 'Salva / Aggiorna' per applicarla.");
        } catch (java.io.IOException e) {
            mostraErrore("⚠️ Impossibile salvare la foto: " + e.getMessage());
        }
    }

    @FXML
    public void onToggleTendinaUrgenze() {
        if (tendinaUrgenze != null) {
            boolean visibile = !tendinaUrgenze.isVisible();
            tendinaUrgenze.setVisible(visibile);
            tendinaUrgenze.setManaged(visibile);
        }
    }

    @FXML public void onClickApriSezioneMenu() { selezionaTab(1); }
    @FXML public void onClickApriSezioneTurni() { selezionaTab(2); }
    @FXML public void onClickApriSezioneMagazzino() { selezionaTab(3); }
    @FXML public void onClickApriSezioneGusti() { selezionaTab(4); }
    @FXML public void onClickApriSezioneStaff() { selezionaTab(5); }
    @FXML public void onClickApriSezioneAvvisi() { selezionaTab(6); }
    @FXML public void onClickTornaDashboard() { selezionaTab(0); }

    private void selezionaTab(int indice) {
        if (tabPaneMaestro != null && tabPaneMaestro.getTabs().size() > indice) {
            tabPaneMaestro.getSelectionModel().select(indice);
        }
    }

    @FXML
    public void onClickRegistraRifornimentoManuale() {
        try {
            String nome = campoNomeIngredienteRifornimento != null ? campoNomeIngredienteRifornimento.getText() : null;
            if (nome == null || nome.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona prima una materia prima dalla lista.");
                return;
            }
            String qtaTesto = campoKgRifornimento != null ? campoKgRifornimento.getText() : null;
            if (qtaTesto == null || qtaTesto.trim().isEmpty()) {
                mostraErrore("⚠️ Inserisci la quantita' arrivata (in Kg o Litri).");
                return;
            }
            double qta;
            try {
                qta = Double.parseDouble(qtaTesto.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                mostraErrore("⚠️ Inserisci un valore numerico valido (es. 10.0).");
                return;
            }
            if (qta <= 0) {
                mostraErrore("⚠️ La quantita' deve essere maggiore di zero.");
                return;
            }
            new ProduzioneOrdiniControl().rifornisciIngredienteMagazzino(nome.trim(), qta);
            mostraSuccesso("✅ Rifornimento registrato: aggiunti " + qta + " Kg/L di '" + nome.trim() + "' al magazzino!");
            if (campoKgRifornimento != null) {
                campoKgRifornimento.clear();
            }
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifornisciUrgente() {
        try {
            String nome = campoIngredienteUrgente != null ? campoIngredienteUrgente.getText() : null;
            if (nome == null || nome.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona prima una materia prima sotto soglia dalla lista.");
                return;
            }
            new ProduzioneOrdiniControl().rifornisciIngredienteMagazzino(nome.trim(), 5.0);
            mostraSuccesso("⚡ Scorte rifornite con successo (+5 Kg/L) per: " + nome.trim());
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
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
        aggiornaBadgeReparto();
        campoNomeGustoMenu.requestFocus();
    }

    @FXML
    public void onClickSalvaGustoMenu() {
        try {
            String catDaSalvare = (categoriaCorrenteSelezionata != null && !categoriaCorrenteSelezionata.trim().isEmpty())
                    ? categoriaCorrenteSelezionata : "Gelati";
            String sottoDaSalvare = (sottocategoriaCorrenteSelezionata != null && !sottocategoriaCorrenteSelezionata.trim().isEmpty())
                    ? sottocategoriaCorrenteSelezionata : sottocategoriaPredefinita(catDaSalvare);

            new ProduzioneOrdiniControl().salvaOAggiornaGustoMenu(
                    campoNomeGustoMenu.getText(),
                    campoDescGustoMenu.getText(),
                    campoAllergeniGustoMenu.getText(),
                    campoPrezzoGustoMenu.getText(),
                    true,
                    catDaSalvare,
                    sottoDaSalvare,
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
            String motScritto = campoMotivazione != null ? campoMotivazione.getText().trim() : "";
            String motRapido = comboMotivazioneRapida != null && comboMotivazioneRapida.getValue() != null ? comboMotivazioneRapida.getValue().trim() : "";
            String motFinale = !motScritto.isEmpty() ? motScritto : (!motRapido.isEmpty() ? motRapido : "Imprevisto personale urgente");
            bean.setMotivazione(motFinale);

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
            mappaEmergenzeAttive.clear();
            for (EmergenzaBean e : turniCtrl.visualizzaTutteEmergenze()) {
                if (!"IN_ATTESA".equals(e.getStato())) {
                    continue; // Le richieste approvate/rifiutate spariscono dalla lista
                }
                String orarioInfo = e.getDataTurno() != null ? (" (" + e.getDataTurno() + " " + e.getOrarioTurno() + ")") : "";
                String sintetica = "🚨 " + e.getNomeRichiedente() + " — Emergenza Turno " + e.getCodiceTurno() + orarioInfo;
                mappaEmergenzeAttive.put(sintetica, e);
                listaEmergenze.getItems().add(sintetica);
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

        // Calcolo urgenze per la campanellina laterale del Maestro
        int emergenzeAttive = 0;
        for (EmergenzaBean e : turniCtrl.visualizzaTutteEmergenze()) {
            if ("IN_ATTESA".equals(e.getStato())) {
                emergenzeAttive++;
            }
        }

        int scorteUrgenti = 0;
        if (listaUrgenzeMagazzino != null) {
            listaUrgenzeMagazzino.getItems().clear();
            for (IngredienteBean ing : prodCtrl.visualizzaMagazzino()) {
                if (ing.isSottoSoglia()) {
                    scorteUrgenti++;
                    listaUrgenzeMagazzino.getItems().add(ing.getNome() + " | Rimanenti: "
                            + ing.getQuantitaDisponibile() + " " + ing.getUnitaMisura()
                            + " (Min: " + ing.getSogliaMinima() + ")");
                }
            }
            if (scorteUrgenti == 0) {
                listaUrgenzeMagazzino.getItems().add("✅ Nessuna mancanza: materie prime ottimali");
            }
        }

        int totaleUrgenze = emergenzeAttive + scorteUrgenti;
        if (btnTendinaUrgenze != null) {
            if (totaleUrgenze > 0) {
                btnTendinaUrgenze.setText("🔔 Urgenze (" + totaleUrgenze + ")");
                btnTendinaUrgenze.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 7 15; -fx-cursor: hand;");
            } else {
                btnTendinaUrgenze.setText("🔔 Notizie (0)");
                btnTendinaUrgenze.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 7 15; -fx-cursor: hand;");
            }
        }

        if (listaNotificheUrgenti != null) {
            listaNotificheUrgenti.getItems().clear();
            List<NotificaBean> listaN = new LoginControl().recuperaNotificheUtente(emailUtente);
            for (int i = 0; i < Math.min(listaN.size(), 8); i++) {
                NotificaBean nb = listaN.get(i);
                listaNotificheUrgenti.getItems().add("[" + nb.getTimestamp() + "] " + nb.getTitolo() + ": " + nb.getMessaggio());
            }
        }
    }
}
