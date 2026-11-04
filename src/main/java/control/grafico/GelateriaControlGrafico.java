package control.grafico;

import bean.GustoBean;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.RichiestaGustoBean;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'cliente.fxml'.
 */
public class GelateriaControlGrafico {

    @FXML
    private Label lblIntestazione;
    @FXML
    private Label lblEsito;
    @FXML
    private ListView<String> listaMenuGusti;
    @FXML
    private ListView<String> listaMieRichieste;
    @FXML
    private ListView<String> listaNotificheCliente;

    @FXML
    private TextField campoGustoOrdine;
    @FXML
    private TextField campoPesoOrdine;
    @FXML
    private TextField campoDataOrdine;
    @FXML
    private CheckBox checkPanna;
    @FXML
    private CheckBox checkGranella;

    @FXML
    private TextField campoNomeGustoCustom;
    @FXML
    private TextField campoIngredienteCustom;
    @FXML
    private TextField campoKgCustom;
    @FXML
    private TextField campoDataCustom;

    @FXML
    public void initialize() {
        if (lblIntestazione != null) {
            lblIntestazione.setText("Cliente: " + SessionManager.getInstance().getNomeUtenteLoggato()
                    + " | Modalita': " + SessionManager.getInstance().getModalita());
        }
        aggiornaVistaCliente();
    }

    @FXML
    public void onClickPreOrdina() {
        try {
            OrdineBean bean = new OrdineBean();
            bean.setEmailCliente(SessionManager.getInstance().getEmailUtenteLoggato());
            bean.setNomeGusto(campoGustoOrdine.getText());
            bean.setPesoDaTesto(campoPesoOrdine.getText());
            bean.setDataRitiro(campoDataOrdine.getText());
            bean.setConPanna(checkPanna.isSelected());
            bean.setConGranellaBronte(checkGranella.isSelected());

            OrdineBean confermato = new ProduzioneOrdiniControl().effettuaPreOrdine(bean);
            lblEsito.setText("Ordine " + confermato.getCodiceOrdine() + " confermato! "
                    + confermato.getDescrizioneCompleta() + " -> EUR " + confermato.getPrezzoTotale());
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    public void onClickRichiediGusto() {
        try {
            RichiestaGustoBean bean = new RichiestaGustoBean();
            bean.setEmailCliente(SessionManager.getInstance().getEmailUtenteLoggato());
            bean.setNomeGustoProposto(campoNomeGustoCustom.getText());
            bean.setNomeIngredientePrincipale(campoIngredienteCustom.getText());
            bean.setQuantitaDaTesto(campoKgCustom.getText());
            bean.setDataRitiroDesiderata(campoDataCustom.getText());
            bean.setNoteCliente("Richiesta da app");

            RichiestaGustoBean creata = new RichiestaGustoControl().inviaRichiestaGusto(bean);
            lblEsito.setText("Richiesta inviata! Codice: " + creata.getCodiceRichiesta());
            aggiornaVistaCliente();
        } catch (FrostlyException e) {
            lblEsito.setText("Errore: " + e.getMessage());
        }
    }

    @FXML
    public void onClickLogout() {
        SessionManager.getInstance().logout();
        LoginControlGrafico.cambiaSchermata("/login.fxml");
    }

    private void aggiornaVistaCliente() {
        String email = SessionManager.getInstance().getEmailUtenteLoggato();
        if (listaMenuGusti != null) {
            listaMenuGusti.getItems().clear();
            for (GustoBean g : new ProduzioneOrdiniControl().visualizzaMenuGusti()) {
                listaMenuGusti.getItems().add(g.getNome() + " (EUR " + g.getPrezzoAlKg() + "/kg) - " + g.getDescrizione());
            }
        }
        if (listaMieRichieste != null) {
            listaMieRichieste.getItems().clear();
            for (RichiestaGustoBean r : new RichiestaGustoControl().visualizzaRichiesteCliente(email)) {
                listaMieRichieste.getItems().add(r.getCodiceRichiesta() + " | '" + r.getNomeGustoProposto()
                        + "' | Stato: " + r.getStato() + " | Risposta: " + r.getRispostaMaestro());
            }
        }
        if (listaNotificheCliente != null) {
            listaNotificheCliente.getItems().clear();
            for (NotificaBean n : new LoginControl().recuperaNotificheUtente(email)) {
                listaNotificheCliente.getItems().add("[" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
            }
        }
    }
}