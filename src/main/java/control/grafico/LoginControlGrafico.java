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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'login.fxml'.
 */
public class LoginControlGrafico extends Application {

    private static Stage finestraPrincipale;

    @FXML
    private ComboBox<ModalitaEsecuzione> comboModalita;
    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoPassword;
    @FXML
    private Label etichettaMessaggio;

    @Override
    public void start(Stage stage) {
        finestraPrincipale = stage;
        finestraPrincipale.setTitle("Frostly - Gelateria Artigianale (ISPW)");
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
    }

    @FXML
    public void onClickLogin() {
        try {
            SessionManager.getInstance().setModalita(comboModalita.getValue());

            LoginBean bean = new LoginBean(campoEmail.getText(), campoPassword.getText());
            LoginControl appControl = new LoginControl();
            LoginBean autenticato = appControl.autenticaUtente(bean);

            if (LoginControl.RUOLO_MAESTRO.equals(autenticato.getRuoloAutenticato())) {
                cambiaSchermata("/maestro.fxml");
            } else if (LoginControl.RUOLO_DIPENDENTE.equals(autenticato.getRuoloAutenticato())) {
                cambiaSchermata("/dipendente.fxml");
            } else {
                cambiaSchermata("/cliente.fxml");
            }
        } catch (FrostlyException e) {
            etichettaMessaggio.setText(e.getMessage());
        }
    }

    @FXML
    public void impostaMaestro() {
        campoEmail.setText("maestro@frostly.it");
        campoPassword.setText("admin123");
    }

    @FXML
    public void impostaMario() {
        campoEmail.setText("mario@frostly.it");
        campoPassword.setText("pass123");
    }

    @FXML
    public void impostaElena() {
        campoEmail.setText("elena@cliente.it");
        campoPassword.setText("pass123");
    }
}