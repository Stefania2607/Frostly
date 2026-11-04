package control.grafico;

import bean.EmergenzaBean;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'maestro.fxml', 'dipendente.fxml',
 * 'maestro_gui2.fxml' e 'dipendente_gui2.fxml'.
 * Supporta la selezione rapida con un click dalle liste e dai menu a tendina.
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
    private ListView<String> listaMagazzino;
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
    public void initialize() {
        if (lblIntestazione != null) {
            lblIntestazione.setText("Utente: " + SessionManager.getInstance().getNomeUtenteLoggato()
                    + " | Modalita': " + SessionManager.getInstance().getModalita());
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
            for (IngredienteBean ing : new ProduzioneOrdiniControl().visualizzaMagazzino()) {
                String alert = ing.isSottoSoglia() ? " ⚠️ [SOTTO SOGLIA]" : " ✅";
                listaMagazzino.getItems().add(ing.getNome() + " -> Disponibili: "
                        + ing.getQuantitaDisponibile() + " " + ing.getUnitaMisura() + alert);
            }
        }

        if (listaNotifiche != null) {
            listaNotifiche.getItems().clear();
            for (NotificaBean n : new LoginControl().recuperaNotificheUtente(emailUtente)) {
                listaNotifiche.getItems().add("[" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
            }
        }
    }
}
