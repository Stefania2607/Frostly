package control.grafico;

import bean.EmergenzaBean;
import bean.GustoBean;
import bean.IngredienteBean;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.RichiestaGustoBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import util.SessionManager;
import view.StaffTurniView;
import view.VetrinaMenuView;

/**
 * Controller Grafico JavaFX collegato a 'maestro.fxml', 'dipendente.fxml',
 * 'maestro_gui2.fxml' e 'dipendente_gui2.fxml'.
 * Gestisce turni, emergenze, richieste gusto e l'aggiornamento in tempo reale del Menu Vetrina su DB.
 * Decomposizione rigorosa BCE: nessuna estetica nel controller grafico; il rendering è delegato a StaffTurniView e VetrinaMenuView.
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
    private Label lblConteggioTurni;
    @FXML
    private Label lblRiepilogoTurniDipendente;
    @FXML
    private Label lblTurnoSelezionatoEmergenza;
    private String filtroGiornoCorrente = "TUTTI";
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
    private FlowPane grigliaMenuGestione;
    private VetrinaMenuView vetrinaGestione;
    private final java.util.Map<String, GustoBean> menuCorrente = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, EmergenzaBean> mappaEmergenzeAttive = new java.util.LinkedHashMap<>();

    @FXML
    private ListView<OrdineBean> listaOrdiniStaff;
    @FXML
    private Label lblConteggioOrdiniStaff;
    @FXML
    private ListView<String> listaOrdiniUrgentiTendina;

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

    private int conteggioOrdiniInAttesaPrecedente = 0;

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
        avviaMonitoraggioOrdiniRealTime();
    }

    private void avviaMonitoraggioOrdiniRealTime() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                    try {
                        int attuali = 0;
                        for (OrdineBean ob : new ProduzioneOrdiniControl().visualizzaTuttiOrdini()) {
                            if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                                attuali++;
                            }
                        }
                        if (attuali > conteggioOrdiniInAttesaPrecedente) {
                            conteggioOrdiniInAttesaPrecedente = attuali;
                            aggiornaListe();
                            if (tendinaUrgenze != null) {
                                tendinaUrgenze.setVisible(true);
                                tendinaUrgenze.setManaged(true);
                            }
                            mostraSuccesso("🚨 NUOVO ORDINE DELIVEROO RICEVUTO! (" + attuali + " in attesa)");
                        } else {
                            conteggioOrdiniInAttesaPrecedente = attuali;
                        }
                    } catch (Exception ignored) {
                        // Monitoraggio silenzioso
                    }
                })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
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

        StaffTurniView.configuraCelleTurni(listaTuttiTurni, true);
        StaffTurniView.configuraCelleTurni(listaTurniDipendente, false);
        StaffTurniView.configuraCelleOrdiniStaff(listaOrdiniStaff, this::eseguiConfermaOrdine, this::eseguiDisdettaOrdine);
        StaffTurniView.configuraCelleMagazzino(listaMagazzino);

        if (listaTuttiTurni != null) {
            listaTuttiTurni.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|")) {
                    String[] parti = newVal.split("\\|");
                    if (parti.length >= 4) {
                        String giorno = parti[1].trim();
                        if (comboGiornoTurno != null) comboGiornoTurno.setValue(giorno);
                        if (campoGiorno != null) campoGiorno.setText(giorno);
                        String orari = parti[2].trim();
                        if (orari.contains("-")) {
                            String[] o = orari.split("-");
                            if (campoOraInizio != null) campoOraInizio.setText(o[0].trim());
                            if (campoOraFine != null) campoOraFine.setText(o[1].trim());
                        }
                    }
                }
            });
        }

        if (listaTurniDipendente != null) {
            listaTurniDipendente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoCodiceTurno != null) {
                    String[] parti = newVal.split("\\|");
                    String cod = parti[0].trim();
                    campoCodiceTurno.setText(cod);
                    if (lblTurnoSelezionatoEmergenza != null && parti.length >= 3) {
                        lblTurnoSelezionatoEmergenza.setText("Turno: " + cod + " • " + parti[1].trim() + " (" + parti[2].trim() + ")");
                    }
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
                    selezionaTab(3);
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

        if (comboCategoriaMenu != null) {
            comboCategoriaMenu.setItems(FXCollections.observableArrayList(
                    "Gelati", "Crepes", "Panna", "Bevande", "Caffetteria", "Biscotti", "Crostate", "Tisane"));
            comboCategoriaMenu.setValue("Gelati");
            comboCategoriaMenu.valueProperty().addListener((obs, oldVal, newVal) -> aggiornaSottocategorieMenu(newVal));
            aggiornaSottocategorieMenu("Gelati");
        }

        if (grigliaMenuGestione != null) {
            vetrinaGestione = new VetrinaMenuView(
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
        StaffTurniView.aggiornaStatoFotoLabel(lblStatoFotoMenu, btnRimuoviFoto, nomeFile);
    }

    @FXML
    public void onClickRimuoviFoto() {
        if (campoImmagineMenu != null) {
            campoImmagineMenu.clear();
        }
        aggiornaStatoFoto(null);
    }

    private void alCambioFiltroVetrina(String cat, String sotto) {
        this.categoriaCorrenteSelezionata = cat;
        this.sottocategoriaCorrenteSelezionata = sotto;
        aggiornaBadgeReparto();
    }

    private void aggiornaBadgeReparto() {
        if (lblCategoriaFormMenu != null) {
            String testo = "📂 Reparto: " + VetrinaMenuView.emojiCategoria(categoriaCorrenteSelezionata) + " " + categoriaCorrenteSelezionata;
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
        if ("Crepes".equalsIgnoreCase(cat)) return "Farciture";
        if ("Panna".equalsIgnoreCase(cat)) return "Porzioni";
        if ("Bevande".equalsIgnoreCase(cat)) return "Acqua";
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
            opzioni.addAll(java.util.Arrays.asList("Creme", "Frutta", "Cioccolato", "Special"));
        } else if ("Crepes".equals(categoria)) {
            opzioni.addAll(java.util.Arrays.asList("Farciture", "Topping"));
        } else if ("Panna".equals(categoria)) {
            opzioni.add("Porzioni");
        } else if ("Bevande".equals(categoria)) {
            opzioni.addAll(java.util.Arrays.asList("Acqua", "Bibite"));
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
            resetCampiDipendenteEAggiorna();
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
            resetCampiDipendenteEAggiorna();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    private void resetCampiDipendenteEAggiorna() {
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
    }

    private void apriPopupDettaglioEmergenza(EmergenzaBean e) {
        Window owner = (tendinaUrgenze != null && tendinaUrgenze.getScene() != null)
                ? tendinaUrgenze.getScene().getWindow() : null;
        List<String> emailDipendenti = new GestioneTurniEmergenzeControl().getEmailTuttiDipendenti();

        StaffTurniView.apriPopupDettaglioEmergenza(
                owner,
                e,
                emailDipendenti,
                (sost, nota) -> {
                    try {
                        if (sost == null || sost.trim().isEmpty()) {
                            mostraErrore("⚠️ Seleziona prima un sostituto per riassegnare il turno.");
                            return;
                        }
                        new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(
                                e.getCodiceEmergenza(), sost.trim(), nota);
                        mostraSuccesso("✅ Emergenza approvata: Turno " + e.getCodiceTurno() + " riassegnato a " + sost);
                        aggiornaListe();
                    } catch (FrostlyException ex) {
                        mostraErrore("⚠️ " + ex.getMessage());
                    }
                },
                nota -> {
                    try {
                        new GestioneTurniEmergenzeControl().rifiutaEmergenza(e.getCodiceEmergenza(), nota);
                        mostraSuccesso("ℹ️ Emergenza " + e.getCodiceEmergenza() + " rifiutata.");
                        aggiornaListe();
                    } catch (FrostlyException ex) {
                        mostraErrore("⚠️ " + ex.getMessage());
                    }
                });
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
    @FXML public void onClickApriSezioneOrdini() { selezionaTab(7); }
    @FXML public void onClickTornaDashboard() { selezionaTab(0); }

    @FXML
    public void onClickAggiornaOrdiniStaff() {
        aggiornaListe();
        mostraSuccesso("Elenco ordini aggiornato.");
    }

    @FXML
    public void onClickConfermaOrdineStaff() {
        OrdineBean sel = listaOrdiniStaff != null ? listaOrdiniStaff.getSelectionModel().getSelectedItem() : null;
        if (sel == null) {
            mostraErrore("⚠️ Seleziona prima un ordine dalla lista da confermare.");
            return;
        }
        eseguiConfermaOrdine(sel.getCodiceOrdine());
    }

    @FXML
    public void onClickDisdiciOrdineStaff() {
        OrdineBean sel = listaOrdiniStaff != null ? listaOrdiniStaff.getSelectionModel().getSelectedItem() : null;
        if (sel == null) {
            mostraErrore("⚠️ Seleziona prima un ordine dalla lista da disdire.");
            return;
        }
        eseguiDisdettaOrdine(sel.getCodiceOrdine());
    }

    private void eseguiConfermaOrdine(String codiceOrdine) {
        try {
            String operatore = SessionManager.getInstance().getNomeUtenteLoggato();
            new ProduzioneOrdiniControl().confermaOrdine(codiceOrdine, operatore);
            mostraSuccesso("✔ Ordine " + codiceOrdine + " CONFERMATO! Notifica inviata al cliente.");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    private void eseguiDisdettaOrdine(String codiceOrdine) {
        try {
            String operatore = SessionManager.getInstance().getNomeUtenteLoggato();
            new ProduzioneOrdiniControl().disdiciOrdine(codiceOrdine, operatore);
            mostraSuccesso("✕ Ordine " + codiceOrdine + " DISDETTO. Notifica inviata al cliente.");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

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
            String email = (campoEmailDip != null && !campoEmailDip.getText().trim().isEmpty())
                    ? campoEmailDip.getText().trim()
                    : (comboDipendenteTurno != null ? comboDipendenteTurno.getValue() : null);
            String giorno = (campoGiorno != null && !campoGiorno.getText().trim().isEmpty())
                    ? campoGiorno.getText().trim()
                    : (comboGiornoTurno != null ? comboGiornoTurno.getValue() : null);

            if (email == null || email.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona prima un dipendente dal menu a tendina.");
                return;
            }
            if (giorno == null || giorno.trim().isEmpty()) {
                mostraErrore("⚠️ Seleziona il giorno della settimana per il turno.");
                return;
            }

            bean.setEmailDipendente(email);
            bean.setData(giorno);
            bean.setOraInizio(campoOraInizio != null ? campoOraInizio.getText() : "08:00");
            bean.setOraFine(campoOraFine != null ? campoOraFine.getText() : "14:00");

            TurnoBean creato = new GestioneTurniEmergenzeControl().pubblicaNuovoTurno(bean);
            mostraSuccesso("✅ Turno pubblicato con successo: " + creato.getCodiceTurno() + " (" + giorno + " " + bean.getOraInizio() + "-" + bean.getOraFine() + ")");
            aggiornaListe();
        } catch (FrostlyException e) {
            mostraErrore("⚠️ " + e.getMessage());
        }
    }

    @FXML public void onClickPresetMattina() {
        if (campoOraInizio != null) campoOraInizio.setText("08:00");
        if (campoOraFine != null) campoOraFine.setText("14:00");
    }
    @FXML public void onClickPresetPomeriggio() {
        if (campoOraInizio != null) campoOraInizio.setText("14:00");
        if (campoOraFine != null) campoOraFine.setText("20:00");
    }
    @FXML public void onClickPresetChiusura() {
        if (campoOraInizio != null) campoOraInizio.setText("18:00");
        if (campoOraFine != null) campoOraFine.setText("00:00");
    }

    @FXML public void onClickFiltroTuttiTurni() { filtroGiornoCorrente = "TUTTI"; aggiornaListe(); }
    @FXML public void onClickFiltraLunedi() { filtroGiornoCorrente = "Lunedi"; aggiornaListe(); }
    @FXML public void onClickFiltraMartedi() { filtroGiornoCorrente = "Martedi"; aggiornaListe(); }
    @FXML public void onClickFiltraMercoledi() { filtroGiornoCorrente = "Mercoledi"; aggiornaListe(); }
    @FXML public void onClickFiltraGiovedi() { filtroGiornoCorrente = "Giovedi"; aggiornaListe(); }
    @FXML public void onClickFiltraVenerdi() { filtroGiornoCorrente = "Venerdi"; aggiornaListe(); }
    @FXML public void onClickFiltraSabato() { filtroGiornoCorrente = "Sabato"; aggiornaListe(); }
    @FXML public void onClickFiltraDomenica() { filtroGiornoCorrente = "Domenica"; aggiornaListe(); }

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
        VetrinaMenuView.mostraMessaggioEsito(lblEsito, msg, false);
    }

    private void mostraErrore(String msg) {
        VetrinaMenuView.mostraMessaggioEsito(lblEsito, msg, true);
    }

    private void aggiornaListe() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        ProduzioneOrdiniControl prodCtrl = new ProduzioneOrdiniControl();
        String emailUtente = SessionManager.getInstance().getEmailUtenteLoggato();

        try {
            if (listaTurniDipendente != null) {
                listaTurniDipendente.getItems().clear();
                List<TurnoBean> turniMiei = turniCtrl.visualizzaTurniDipendente(emailUtente);
                turniMiei.sort((t1, t2) -> {
                    int g1 = indiceGiorno(t1.getData());
                    int g2 = indiceGiorno(t2.getData());
                    if (g1 != g2) return Integer.compare(g1, g2);
                    return t1.getOraInizio().compareTo(t2.getOraInizio());
                });

                int oreTotali = 0;
                for (TurnoBean t : turniMiei) {
                    listaTurniDipendente.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                            + " | " + t.getOraInizio() + " - " + t.getOraFine() + " | "
                            + t.getNomeDipendente() + " | " + t.getStato());
                    try {
                        int hInizio = Integer.parseInt(t.getOraInizio().split(":")[0]);
                        int hFine = Integer.parseInt(t.getOraFine().split(":")[0]);
                        oreTotali += Math.max(0, hFine - hInizio);
                    } catch (Exception ignored) {
                        // Formato orario non standard
                    }
                }
                if (lblRiepilogoTurniDipendente != null) {
                    lblRiepilogoTurniDipendente.setText("📅 " + turniMiei.size() + " turni assegnati (" + oreTotali + " ore totali)");
                }
            }
            if (listaTuttiTurni != null) {
                listaTuttiTurni.getItems().clear();
                List<TurnoBean> tuttiTurni = turniCtrl.visualizzaTuttiTurni();
                tuttiTurni.sort((t1, t2) -> {
                    int g1 = indiceGiorno(t1.getData());
                    int g2 = indiceGiorno(t2.getData());
                    if (g1 != g2) return Integer.compare(g1, g2);
                    return t1.getOraInizio().compareTo(t2.getOraInizio());
                });

                int conteggioMostrati = 0;
                int emergenze = 0;
                for (TurnoBean t : tuttiTurni) {
                    if ("IN_EMERGENZA".equalsIgnoreCase(t.getStato())) {
                        emergenze++;
                    }
                    if ("TUTTI".equalsIgnoreCase(filtroGiornoCorrente)
                            || normalizzaGiorno(t.getData()).equalsIgnoreCase(normalizzaGiorno(filtroGiornoCorrente))) {
                        listaTuttiTurni.getItems().add(t.getCodiceTurno() + " | " + t.getData()
                                + " | " + t.getOraInizio() + " - " + t.getOraFine() + " | "
                                + t.getNomeDipendente() + " | " + t.getStato());
                        conteggioMostrati++;
                    }
                }
                if (lblConteggioTurni != null) {
                    String infoFiltro = "TUTTI".equalsIgnoreCase(filtroGiornoCorrente) ? "Tutta la settimana" : filtroGiornoCorrente;
                    lblConteggioTurni.setText("Visualizzati: " + conteggioMostrati + " turni (" + infoFiltro + ")"
                            + (emergenze > 0 ? " • 🚨 " + emergenze + " emergenze attive" : " • ✅ Nessuna emergenza"));
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
                    continue;
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

        int ordiniInAttesa = 0;
        if (listaOrdiniStaff != null) {
            listaOrdiniStaff.getItems().clear();
            List<OrdineBean> tuttiOrdini = prodCtrl.visualizzaTuttiOrdini();
            for (OrdineBean ob : tuttiOrdini) {
                listaOrdiniStaff.getItems().add(ob);
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    ordiniInAttesa++;
                }
            }
            if (lblConteggioOrdiniStaff != null) {
                lblConteggioOrdiniStaff.setText(ordiniInAttesa > 0
                        ? "🚨 " + ordiniInAttesa + " ordini in attesa di conferma"
                        : "✅ Nessun ordine in attesa (" + tuttiOrdini.size() + " totali)");
            }
        } else {
            for (OrdineBean ob : prodCtrl.visualizzaTuttiOrdini()) {
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    ordiniInAttesa++;
                }
            }
        }

        if (listaOrdiniUrgentiTendina != null) {
            listaOrdiniUrgentiTendina.getItems().clear();
            for (OrdineBean ob : prodCtrl.visualizzaTuttiOrdini()) {
                if ("IN_ATTESA".equalsIgnoreCase(ob.getStato())) {
                    listaOrdiniUrgentiTendina.getItems().add("🛵 " + ob.getCodiceOrdine() + " • " + ob.getEmailCliente()
                            + " (" + ob.getDataRitiro() + ") - EUR " + ob.getPrezzoTotale());
                }
            }
            if (listaOrdiniUrgentiTendina.getItems().isEmpty()) {
                listaOrdiniUrgentiTendina.getItems().add("✅ Nessun ordine in attesa al momento");
            }
        }

        int totaleUrgenze = emergenzeAttive + scorteUrgenti + ordiniInAttesa;
        StaffTurniView.aggiornaBottoneUrgenze(btnTendinaUrgenze, totaleUrgenze);

        if (listaNotificheUrgenti != null) {
            listaNotificheUrgenti.getItems().clear();
            List<NotificaBean> listaN = new LoginControl().recuperaNotificheUtente(emailUtente);
            for (int i = 0; i < Math.min(listaN.size(), 8); i++) {
                NotificaBean nb = listaN.get(i);
                listaNotificheUrgenti.getItems().add("[" + nb.getTimestamp() + "] " + nb.getTitolo() + ": " + nb.getMessaggio());
            }
        }
    }

    private static int indiceGiorno(String g) {
        if (g == null) return 7;
        String s = g.toLowerCase().replace("ì", "i");
        if (s.contains("lun")) return 0;
        if (s.contains("mar")) return 1;
        if (s.contains("mer")) return 2;
        if (s.contains("gio")) return 3;
        if (s.contains("ven")) return 4;
        if (s.contains("sab")) return 5;
        if (s.contains("dom")) return 6;
        return 7;
    }

    private static String normalizzaGiorno(String g) {
        if (g == null) return "";
        return g.toLowerCase().replace("ì", "i").trim();
    }
}
