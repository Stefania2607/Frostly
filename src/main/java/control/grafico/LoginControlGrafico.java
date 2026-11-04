package control.grafico;

import bean.LoginBean;
import control.applicativo.LoginControl;
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
import javafx.stage.Stage;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'login.fxml' e 'registrazione.fxml'.
 * Gestisce il Log In (Sign In) e la schermata dedicata alla Registrazione (Sign Up).
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
    private ComboBox<String> comboRuoloReg;

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
        if (comboRuoloReg != null) {
            comboRuoloReg.setItems(FXCollections.observableArrayList(
                    LoginControl.RUOLO_CLIENTE, LoginControl.RUOLO_DIPENDENTE));
            comboRuoloReg.setValue(LoginControl.RUOLO_CLIENTE);
        }
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
            etichettaMessaggio.setStyle("-fx-text-fill: #b83b2b; -fx-font-weight: bold;");
            etichettaMessaggio.setText(e.getMessage());
        }
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
            regBean.setRuoloScelto(comboRuoloReg.getValue());

            LoginControl appControl = new LoginControl();
            LoginBean creato = appControl.registraUtente(regBean);

            etichettaMessaggio.setStyle("-fx-text-fill: #2e8b57; -fx-font-weight: bold;");
            etichettaMessaggio.setText("Registrazione completata per " + creato.getNomeCompleto()
                    + "! Clicca 'Torna all'Accesso' per entrare.");
        } catch (FrostlyException e) {
            etichettaMessaggio.setStyle("-fx-text-fill: #b83b2b; -fx-font-weight: bold;");
            etichettaMessaggio.setText(e.getMessage());
        }
    }
}
