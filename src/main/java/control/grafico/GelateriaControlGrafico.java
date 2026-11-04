package control.grafico;

import bean.GustoBean;
import bean.IngredienteBean;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.RichiestaGustoBean;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'cliente.fxml' e 'cliente_gui2.fxml'.
 * Supporta la selezione rapida con un click dal menu gusti e dai menu a tendina.
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
    private ComboBox<String> comboPesoRapido;
    @FXML
    private ComboBox<String> comboGiornoOrdine;
    @FXML
    private ComboBox<String> comboIngredienteCustom;

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
        configuraTendineEClickMenu();
        aggiornaVistaCliente();
    }

    private void configuraTendineEClickMenu() {
        if (listaMenuGusti != null) {
            listaMenuGusti.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains(" (EUR") && campoGustoOrdine != null) {
                    campoGustoOrdine.setText(newVal.substring(0, newVal.indexOf(" (EUR")).trim());
                }
            });
        }

        if (comboPesoRapido != null) {
            comboPesoRapido.setItems(FXCollections.observableArrayList("0.5", "1.0", "1.5", "2.0"));
            comboPesoRapido.setOnAction(e -> {
                if (comboPesoRapido.getValue() != null && campoPesoOrdine != null) {
                    campoPesoOrdine.setText(comboPesoRapido.getValue());
                }
            });
        }

        if (comboGiornoOrdine != null) {
            comboGiornoOrdine.setItems(FXCollections.observableArrayList(
                    "Lunedi", "Martedi", "Mercoledi", "Giovedi", "Venerdi", "Sabato", "Domenica"));
            comboGiornoOrdine.setOnAction(e -> {
                if (comboGiornoOrdine.getValue() != null && campoDataOrdine != null) {
                    campoDataOrdine.setText(comboGiornoOrdine.getValue());
                }
            });
        }

        if (comboIngredienteCustom != null) {
            List<String> nomiIng = new ArrayList<>();
            for (IngredienteBean ib : new ProduzioneOrdiniControl().visualizzaMagazzino()) {
                nomiIng.add(ib.getNome());
            }
            comboIngredienteCustom.setItems(FXCollections.observableArrayList(nomiIng));
            comboIngredienteCustom.setOnAction(e -> {
                if (comboIngredienteCustom.getValue() != null && campoIngredienteCustom != null) {
                    campoIngredienteCustom.setText(comboIngredienteCustom.getValue());
                }
            });
        }
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
            lblEsito.setStyle("-fx-text-fill: #2a9d8f; -fx-font-weight: bold;");
            lblEsito.setText("✅ Ordine " + confermato.getCodiceOrdine() + " confermato! "
                    + confermato.getDescrizioneCompleta() + " -> Totale: EUR " + confermato.getPrezzoTotale());
        } catch (FrostlyException e) {
            lblEsito.setStyle("-fx-text-fill: #d62828; -fx-font-weight: bold;");
            lblEsito.setText("⚠️ " + e.getMessage());
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
            bean.setNoteCliente("Richiesta personalizzata da app");

            RichiestaGustoBean creata = new RichiestaGustoControl().inviaRichiestaGusto(bean);
            lblEsito.setStyle("-fx-text-fill: #2a9d8f; -fx-font-weight: bold;");
            lblEsito.setText("✅ Richiesta inviata al Maestro! Codice: " + creata.getCodiceRichiesta());
            aggiornaVistaCliente();
        } catch (FrostlyException e) {
            lblEsito.setStyle("-fx-text-fill: #d62828; -fx-font-weight: bold;");
            lblEsito.setText("⚠️ " + e.getMessage());
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
