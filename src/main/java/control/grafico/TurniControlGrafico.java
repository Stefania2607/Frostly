package control.grafico;

import bean.EmergenzaBean;
import bean.NotificaBean;
import bean.RichiestaGustoBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'maestro.fxml' e 'dipendente.fxml'.
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
    private ListView<String> listaNotifiche;

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
        aggiornaListe();
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
            lblEsito.setText("Turno pubblicato: " + creato.getCodiceTurno());
            aggiornaListe();
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
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
            lblEsito.setText("Emergenza inviata! Codice: " + creata.getCodiceEmergenza());
            aggiornaListe();
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    public void onClickApprovaEmergenza() {
        try {
            new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(
                    campoCodEmergenza.getText(), campoEmailSostituto.getText(), campoNotaMaestro.getText());
            lblEsito.setText("Emergenza approvata e sostituto notificato!");
            aggiornaListe();
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifiutaEmergenza() {
        try {
            new GestioneTurniEmergenzeControl().rifiutaEmergenza(
                    campoCodEmergenza.getText(), campoNotaMaestro.getText());
            lblEsito.setText("Emergenza rifiutata.");
            aggiornaListe();
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    public void onClickApprovaGusto() {
        try {
            new RichiestaGustoControl().approvaRichiestaGusto(
                    campoCodRichiesta.getText(), campoRispostaGusto.getText());
            lblEsito.setText("Richiesta gusto approvata e ingrediente scalato!");
            aggiornaListe();
        } catch (FrostlyException e) {
            lblEsito.setText("Attenzione: " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifornisciGusto() {
        try {
            new RichiestaGustoControl().approvaConRifornimentoUrgente(
                    campoCodRichiesta.getText(), 5.0, campoRispostaGusto.getText());
            lblEsito.setText("Rifornimento (+5Kg) effettuato e gusto approvato!");
            aggiornaListe();
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    public void onClickRifiutaGusto() {
        try {
            new RichiestaGustoControl().rifiutaRichiestaGusto(
                    campoCodRichiesta.getText(), campoRispostaGusto.getText());
            lblEsito.setText("Richiesta gusto rifiutata.");
            aggiornaListe();
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    public void onClickLogout() {
        SessionManager.getInstance().logout();
        LoginControlGrafico.cambiaSchermata("/login.fxml");
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

        if (listaNotifiche != null) {
            listaNotifiche.getItems().clear();
            for (NotificaBean n : new LoginControl().recuperaNotificheUtente(emailUtente)) {
                listaNotifiche.getItems().add("[" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
            }
        }
    }
}