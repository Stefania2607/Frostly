package control.grafico;

import bean.LoginBean;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import frostly.exception.FrostlyException;
import java.io.IOException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.ModalitaEsecuzione;
import util.SessionManager;
import view.VetrinaMenuView;

/**
 * Controller Grafico JavaFX collegato a 'login.fxml', 'registrazione.fxml' e 'menu_pubblico.fxml'.
 * Gestisce Log In, Registrazione e consultazione libera del Menu Vetrina senza autenticazione.
 * Decomposizione rigorosa BCE: nessuna estetica nel controller grafico; il rendering è delegato a VetrinaMenuView.
 */
public class LoginControlGrafico extends Application {

    private static Stage finestraPrincipale;

    @FXML
    private ComboBox<ModalitaEsecuzione> comboModalita;
    @FXML
    private CheckBox checkSecondaGui;
    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoPassword;

    @FXML
    private TextField campoNomeReg;
    @FXML
    private TextField campoCognomeReg;
    @FXML
    private TextField campoEmailReg;
    @FXML
    private PasswordField campoPasswordReg;

    @FXML
    private HBox boxCategorie;
    @FXML
    private HBox boxSottocategorie;
    @FXML
    private FlowPane grigliaProdotti;

    private VetrinaMenuView vetrina;

    @FXML
    private Label etichettaMessaggio;

    @Override
    public void start(Stage stage) {
        finestraPrincipale = stage;
        finestraPrincipale.setTitle("Frostly — Maestri Gelatieri dal 1984");
        cambiaSchermata("/login.fxml");
        finestraPrincipale.show();
    }

    public static void cambiaSchermata(String fileFxml) {
        try {
            Parent root = FXMLLoader.load(LoginControlGrafico.class.getResource(fileFxml));
            finestraPrincipale.setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Errore caricamento FXML: " + fileFxml, e);
        }
    }

    @FXML
    public void initialize() {
        if (comboModalita != null) {
            comboModalita.setItems(FXCollections.observableArrayList(ModalitaEsecuzione.values()));
            comboModalita.setValue(SessionManager.getInstance().getModalita());
        }
        if (grigliaProdotti != null) {
            caricaMenuPubblico();
        }
    }

    private void caricaMenuPubblico() {
        vetrina = new VetrinaMenuView(boxCategorie, boxSottocategorie, grigliaProdotti, null);
        vetrina.carica(new ProduzioneOrdiniControl().visualizzaMenuGusti());
    }

    @FXML
    public void onClickLogin() {
        try {
            if (comboModalita != null && comboModalita.getValue() != null) {
                SessionManager.getInstance().setModalita(comboModalita.getValue());
            }

            LoginBean bean = new LoginBean(campoEmail.getText(), campoPassword.getText());
            LoginControl appControl = new LoginControl();
            LoginBean autenticato = appControl.autenticaUtente(bean);

            boolean usaGui2 = checkSecondaGui != null && checkSecondaGui.isSelected();
            String suffisso = usaGui2 ? "_gui2.fxml" : ".fxml";

            if (LoginControl.RUOLO_MAESTRO.equals(autenticato.getRuoloAutenticato())) {
                cambiaSchermata("/maestro" + suffisso);
            } else if (LoginControl.RUOLO_DIPENDENTE.equals(autenticato.getRuoloAutenticato())) {
                cambiaSchermata("/dipendente" + suffisso);
            } else {
                cambiaSchermata("/cliente" + suffisso);
            }
        } catch (FrostlyException e) {
            VetrinaMenuView.mostraMessaggioEsito(etichettaMessaggio, e.getMessage(), true);
        }
    }

    @FXML
    public void onClickApriMenuPubblico() {
        if (comboModalita != null && comboModalita.getValue() != null) {
            SessionManager.getInstance().setModalita(comboModalita.getValue());
        }
        cambiaSchermata("/menu_pubblico.fxml");
    }

    @FXML
    public void onClickVaiARegistrazione() {
        if (comboModalita != null && comboModalita.getValue() != null) {
            SessionManager.getInstance().setModalita(comboModalita.getValue());
        }
        cambiaSchermata("/registrazione.fxml");
    }

    @FXML
    public void onClickTornaALogin() {
        cambiaSchermata("/login.fxml");
    }

    @FXML
    public void onClickConfermaRegistrazione() {
        try {
            LoginBean regBean = new LoginBean();
            regBean.setNome(campoNomeReg.getText());
            regBean.setCognome(campoCognomeReg.getText());
            regBean.setEmail(campoEmailReg.getText());
            regBean.setPassword(campoPasswordReg.getText());
            regBean.setRuoloScelto(LoginControl.RUOLO_CLIENTE);

            LoginControl appControl = new LoginControl();
            LoginBean creato = appControl.registraUtente(regBean);
            appControl.autenticaUtente(new LoginBean(regBean.getEmail(), regBean.getPassword()));

            boolean usaGui2 = checkSecondaGui != null && checkSecondaGui.isSelected();
            cambiaSchermata("/cliente" + (usaGui2 ? "_gui2.fxml" : ".fxml"));

            if (finestraPrincipale != null) {
                VetrinaMenuView.mostraNotifica(finestraPrincipale,
                        "Benvenuto in Frostly, " + creato.getNome() + "!",
                        "Registrazione completata! Seleziona i prodotti dal menu per aggiungerli al tuo carrello.");
            }
        } catch (FrostlyException e) {
            VetrinaMenuView.mostraMessaggioEsito(etichettaMessaggio, e.getMessage(), true);
        }
    }
}
