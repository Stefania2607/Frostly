package control.cli;

import bean.LoginBean;
import bean.NotificaBean;
import control.applicativo.LoginControl;
import frostly.exception.FrostlyException;
import java.util.List;
import java.util.Scanner;

/**
 * Controller CLI dedicato ai casi d'uso di autenticazione, registrazione e consultazione notifiche.
 */
@SuppressWarnings("java:S106")
public class LoginControlCLI {

    private final Scanner scanner;
    private final LoginControl loginControl;

    public LoginControlCLI(Scanner scanner) {
        this.scanner = scanner;
        this.loginControl = new LoginControl();
    }

    public LoginBean eseguiLogin() {
        try {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            LoginBean autenticato = loginControl.autenticaUtente(new LoginBean(email, password));
            System.out.println("Benvenuto " + autenticato.getNomeCompleto() + " [" + autenticato.getRuoloAutenticato() + "]!");
            mostraNotifiche(email);
            return autenticato;
        } catch (FrostlyException e) {
            System.out.println("[ERRORE] " + e.getMessage());
            return null;
        }
    }

    public void eseguiRegistrazione() {
        try {
            LoginBean regBean = new LoginBean();
            System.out.print("Nome: ");
            regBean.setNome(scanner.nextLine().trim());
            System.out.print("Cognome: ");
            regBean.setCognome(scanner.nextLine().trim());
            System.out.print("Email: ");
            regBean.setEmail(scanner.nextLine().trim());
            System.out.print("Password (min 4 caratteri): ");
            regBean.setPassword(scanner.nextLine().trim());
            regBean.setRuoloScelto(LoginControl.RUOLO_CLIENTE);

            LoginBean creato = loginControl.registraUtente(regBean);
            System.out.println("-> Utente " + creato.getNomeCompleto() + " [" + creato.getRuoloAutenticato() + "] registrato con successo!");
        } catch (FrostlyException e) {
            System.out.println("[ERRORE REGISTRAZIONE] " + e.getMessage());
        }
    }

    public void mostraNotifiche(String email) {
        List<NotificaBean> notifiche = loginControl.recuperaNotificheUtente(email);
        for (NotificaBean n : notifiche) {
            System.out.println(" * [" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
        }
    }
}
