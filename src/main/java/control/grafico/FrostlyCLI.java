package control.grafico;

import bean.LoginBean;
import control.applicativo.LoginControl;
import control.cli.LoginControlCLI;
import control.cli.OrdiniControlCLI;
import control.cli.TurniEmergenzeControlCLI;
import dao.DAOFactory;
import frostly.exception.FrostlyException;
import java.util.Scanner;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Avvio e smistamento dei menu della CLI di Frostly.
 * Delega i singoli casi d'uso ai controller CLI dedicati nel package {@code control.cli}.
 */
@SuppressWarnings("java:S106")
public class FrostlyCLI {

    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new FrostlyCLI().avviaCLI();
    }

    public void avviaCLI() {
        System.out.println("=== FROSTLY CLI (ISPW Tor Vergata) ===");
        selezionaModalita();

        LoginControlCLI loginCLI = new LoginControlCLI(scanner);
        boolean continua = true;
        while (continua) {
            System.out.println("\nAccount: maestro@frostly.it (admin123) | mario@frostly.it (pass123) | elena@cliente.it (pass123)");
            System.out.println("1) Login (Sign In)  2) Registrati (Sign Up)  3) Cambia modalita' (DEMO/FULL)  0) Esci");
            System.out.print("Scelta: ");
            String scelta = scanner.nextLine().trim();
            if ("1".equals(scelta)) {
                LoginBean autenticato = loginCLI.eseguiLogin();
                if (autenticato != null) {
                    avviaMenuRuolo(autenticato, loginCLI);
                }
            } else if ("2".equals(scelta)) {
                loginCLI.eseguiRegistrazione();
            } else if ("3".equals(scelta)) {
                selezionaModalita();
            } else if ("0".equals(scelta)) {
                continua = false;
            }
        }
    }

    private void selezionaModalita() {
        System.out.print("Modalita' [1 = DEMO In-Memory, 2 = FULL Persistente MySQL/CSV]: ");
        if ("2".equals(scanner.nextLine().trim())) {
            SessionManager.getInstance().setModalita(ModalitaEsecuzione.FULL);
            System.out.print("Persistenza TurnoDAO in FULL [1 = JDBC MySQL, 2 = File System CSV]: ");
            boolean usaCsv = "2".equals(scanner.nextLine().trim());
            DAOFactory.setUsaFileSystemPerTurni(usaCsv);
            System.out.println("-> Modalita' FULL attiva (TurnoDAO: " + (usaCsv ? "File System CSV" : "JDBC MySQL") + ")");
        } else {
            SessionManager.getInstance().setModalita(ModalitaEsecuzione.DEMO);
            DAOFactory.setUsaFileSystemPerTurni(false);
            System.out.println("-> Modalita' DEMO In-Memory attiva");
        }
    }

    private void avviaMenuRuolo(LoginBean autenticato, LoginControlCLI loginCLI) {
        String ruolo = autenticato.getRuoloAutenticato();
        String email = autenticato.getEmail();
        if (LoginControl.RUOLO_MAESTRO.equals(ruolo)) {
            menuMaestro(email, loginCLI);
        } else if (LoginControl.RUOLO_DIPENDENTE.equals(ruolo)) {
            menuDipendente(email);
        } else {
            menuCliente(email);
        }
    }

    private void menuMaestro(String email, LoginControlCLI loginCLI) {
        TurniEmergenzeControlCLI turniCLI = new TurniEmergenzeControlCLI(scanner);
        OrdiniControlCLI ordiniCLI = new OrdiniControlCLI(scanner);
        boolean attivo = true;
        while (attivo) {
            System.out.println("\n[MAESTRO] 1) Turni  2) Emergenze  3) Richieste Gusti  4) Magazzino  5) Notifiche  0) Logout");
            String s = scanner.nextLine().trim();
            try {
                if ("1".equals(s)) {
                    turniCLI.gestisciTurniMaestro();
                } else if ("2".equals(s)) {
                    turniCLI.gestisciEmergenzeMaestro();
                } else if ("3".equals(s)) {
                    ordiniCLI.gestisciRichiesteGustoMaestro();
                } else if ("4".equals(s)) {
                    turniCLI.mostraMagazzino();
                } else if ("5".equals(s)) {
                    loginCLI.mostraNotifiche(email);
                } else if ("0".equals(s)) {
                    SessionManager.getInstance().logout();
                    attivo = false;
                }
            } catch (FrostlyException e) {
                System.out.println("[ERRORE] " + e.getMessage());
            }
        }
    }

    private void menuDipendente(String email) {
        TurniEmergenzeControlCLI turniCLI = new TurniEmergenzeControlCLI(scanner);
        boolean attivo = true;
        while (attivo) {
            System.out.println("\n[DIPENDENTE] 1) I miei turni  2) Segnala emergenza  0) Logout");
            String s = scanner.nextLine().trim();
            try {
                if ("1".equals(s)) {
                    turniCLI.mostraTurniDipendente(email);
                } else if ("2".equals(s)) {
                    turniCLI.segnalaEmergenzaDipendente(email);
                } else if ("0".equals(s)) {
                    SessionManager.getInstance().logout();
                    attivo = false;
                }
            } catch (FrostlyException e) {
                System.out.println("[ERRORE] " + e.getMessage());
            }
        }
    }

    private void menuCliente(String email) {
        OrdiniControlCLI ordiniCLI = new OrdiniControlCLI(scanner);
        boolean attivo = true;
        while (attivo) {
            System.out.println("\n[CLIENTE] 1) Pre-ordina vaschetta  2) Richiedi gusto personalizzato  0) Logout");
            String s = scanner.nextLine().trim();
            try {
                if ("1".equals(s)) {
                    ordiniCLI.effettuaPreOrdineVaschetta(email);
                } else if ("2".equals(s)) {
                    ordiniCLI.inviaRichiestaGustoPersonalizzato(email);
                } else if ("0".equals(s)) {
                    SessionManager.getInstance().logout();
                    attivo = false;
                }
            } catch (FrostlyException e) {
                System.out.println("[ERRORE] " + e.getMessage());
            }
        }
    }
}
