package control.grafico;

import bean.EmergenzaBean;
import bean.GustoBean;
import bean.IngredienteBean;
import bean.LoginBean;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.RichiestaGustoBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import dao.DAOFactory;
import frostly.exception.FrostlyException;
import frostly.exception.IngredienteEsauritoException;
import java.util.List;
import java.util.Scanner;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Interfaccia a Riga di Comando (CLI) di Frostly.
 * Supporta Log In (Sign In), Registrazione (Sign Up) e tutte le funzioni dei 3 attori.
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
        boolean continua = true;
        while (continua) {
            System.out.println("\nAccount: maestro@frostly.it (admin123) | mario@frostly.it (pass123) | elena@cliente.it (pass123)");
            System.out.println("1) Login (Sign In)  2) Registrati (Sign Up)  3) Cambia modalita' (DEMO/FULL)  0) Esci");
            System.out.print("Scelta: ");
            String scelta = scanner.nextLine().trim();
            if ("1".equals(scelta)) {
                gestisciLogin();
            } else if ("2".equals(scelta)) {
                gestisciRegistrazione();
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

    private void gestisciRegistrazione() {
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

            LoginBean creato = new LoginControl().registraUtente(regBean);
            System.out.println("-> Utente " + creato.getNomeCompleto() + " [" + creato.getRuoloAutenticato() + "] registrato con successo!");
        } catch (FrostlyException e) {
            System.out.println("[ERRORE REGISTRAZIONE] " + e.getMessage());
        }
    }

    private void gestisciLogin() {
        try {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            LoginBean autenticato = new LoginControl().autenticaUtente(new LoginBean(email, password));
            System.out.println("Benvenuto " + autenticato.getNomeCompleto() + " [" + autenticato.getRuoloAutenticato() + "]!");
            mostraNotifiche(email);

            if (LoginControl.RUOLO_MAESTRO.equals(autenticato.getRuoloAutenticato())) {
                menuMaestro(email);
            } else if (LoginControl.RUOLO_DIPENDENTE.equals(autenticato.getRuoloAutenticato())) {
                menuDipendente(email);
            } else {
                menuCliente(email);
            }
        } catch (FrostlyException e) {
            System.out.println("[ERRORE] " + e.getMessage());
        }
    }

    private void mostraNotifiche(String email) {
        List<NotificaBean> notifiche = new LoginControl().recuperaNotificheUtente(email);
        for (NotificaBean n : notifiche) {
            System.out.println(" * [" + n.getTimestamp() + "] " + n.getTitolo() + ": " + n.getMessaggio());
        }
    }

    private void menuMaestro(String email) {
        boolean attivo = true;
        while (attivo) {
            System.out.println("\n[MAESTRO] 1) Turni  2) Emergenze  3) Richieste Gusti  4) Magazzino  5) Notifiche  0) Logout");
            String s = scanner.nextLine().trim();
            try {
                if ("1".equals(s)) {
                    gestisciTurniCLI();
                } else if ("2".equals(s)) {
                    gestisciEmergenzeCLI();
                } else if ("3".equals(s)) {
                    gestisciRichiesteGustoCLI();
                } else if ("4".equals(s)) {
                    for (IngredienteBean i : new ProduzioneOrdiniControl().visualizzaMagazzino()) {
                        System.out.println(" - " + i.getNome() + ": " + i.getQuantitaDisponibile() + " " + i.getUnitaMisura());
                    }
                } else if ("5".equals(s)) {
                    mostraNotifiche(email);
                } else if ("0".equals(s)) {
                    SessionManager.getInstance().logout();
                    attivo = false;
                }
            } catch (FrostlyException e) {
                System.out.println("[ERRORE] " + e.getMessage());
            }
        }
    }

    private void gestisciTurniCLI() throws FrostlyException {
        GestioneTurniEmergenzeControl ctrl = new GestioneTurniEmergenzeControl();
        for (TurnoBean t : ctrl.visualizzaTuttiTurni()) {
            System.out.println(t.getCodiceTurno() + " | " + t.getData() + " " + t.getOraInizio() + "-" + t.getOraFine() + " | " + t.getNomeDipendente());
        }
        System.out.print("Pubblicare nuovo turno? (s/n): ");
        if ("s".equalsIgnoreCase(scanner.nextLine().trim())) {
            TurnoBean tb = new TurnoBean();
            System.out.print("Email dipendente: ");
            tb.setEmailDipendente(scanner.nextLine().trim());
            System.out.print("Giorno: ");
            tb.setData(scanner.nextLine().trim());
            System.out.print("Ora inizio (HH:mm): ");
            tb.setOraInizio(scanner.nextLine().trim());
            System.out.print("Ora fine (HH:mm): ");
            tb.setOraFine(scanner.nextLine().trim());
            ctrl.pubblicaNuovoTurno(tb);
            System.out.println("-> Turno pubblicato!");
        }
    }

    private void gestisciEmergenzeCLI() throws FrostlyException {
        GestioneTurniEmergenzeControl ctrl = new GestioneTurniEmergenzeControl();
        for (EmergenzaBean e : ctrl.visualizzaTutteEmergenze()) {
            System.out.println(e.getCodiceEmergenza() + " | Turno " + e.getCodiceTurno() + " | Stato: " + e.getStato());
        }
        System.out.print("Codice emergenza da approvare (o INVIO): ");
        String cod = scanner.nextLine().trim();
        if (!cod.isEmpty()) {
            System.out.print("Email sostituto: ");
            ctrl.approvaEmergenzaConSostituto(cod, scanner.nextLine().trim(), "OK da CLI");
            System.out.println("-> Emergenza approvata!");
        }
    }

    private void gestisciRichiesteGustoCLI() throws FrostlyException {
        RichiestaGustoControl ctrl = new RichiestaGustoControl();
        for (RichiestaGustoBean r : ctrl.visualizzaTutteRichieste()) {
            System.out.println(r.getCodiceRichiesta() + " | '" + r.getNomeGustoProposto() + "' | Stato: " + r.getStato());
        }
        System.out.print("Codice richiesta da approvare (o INVIO): ");
        String cod = scanner.nextLine().trim();
        if (!cod.isEmpty()) {
            try {
                ctrl.approvaRichiestaGusto(cod, "Confermato da CLI");
                System.out.println("-> Approvata!");
            } catch (IngredienteEsauritoException ex) {
                System.out.println(ex.getMessage());
                ctrl.approvaConRifornimentoUrgente(cod, 5.0, "Pronto dopo rifornimento");
                System.out.println("-> Rifornito e approvato!");
            }
        }
    }

    private void menuDipendente(String email) {
        boolean attivo = true;
        while (attivo) {
            System.out.println("\n[DIPENDENTE] 1) I miei turni  2) Segnala emergenza  0) Logout");
            String s = scanner.nextLine().trim();
            try {
                GestioneTurniEmergenzeControl ctrl = new GestioneTurniEmergenzeControl();
                if ("1".equals(s)) {
                    for (TurnoBean t : ctrl.visualizzaTurniDipendente(email)) {
                        System.out.println(t.getCodiceTurno() + " | " + t.getData() + " [" + t.getStato() + "]");
                    }
                } else if ("2".equals(s)) {
                    EmergenzaBean eb = new EmergenzaBean();
                    eb.setEmailRichiedente(email);
                    System.out.print("Codice turno: ");
                    eb.setCodiceTurno(scanner.nextLine().trim());
                    System.out.print("Motivazione: ");
                    eb.setMotivazione(scanner.nextLine().trim());
                    ctrl.segnalaEmergenzaTurno(eb);
                    System.out.println("-> Emergenza inviata!");
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
        boolean attivo = true;
        while (attivo) {
            System.out.println("\n[CLIENTE] 1) Pre-ordina vaschetta  2) Richiedi gusto personalizzato  0) Logout");
            String s = scanner.nextLine().trim();
            try {
                if ("1".equals(s)) {
                    ProduzioneOrdiniControl pCtrl = new ProduzioneOrdiniControl();
                    for (GustoBean g : pCtrl.visualizzaMenuGusti()) {
                        if (!"Gelati".equalsIgnoreCase(g.getCategoria())) {
                            continue;
                        }
                        System.out.println(" * " + g.getNome() + " (EUR " + g.getPrezzoAlKg() + "/kg)");
                    }
                    OrdineBean ob = new OrdineBean();
                    ob.setEmailCliente(email);
                    System.out.print("Gusto scelto: ");
                    ob.setNomeGusto(scanner.nextLine().trim());
                    System.out.print("Kg: ");
                    ob.setPesoDaTesto(scanner.nextLine().trim());
                    System.out.print("Giorno ritiro: ");
                    ob.setDataRitiro(scanner.nextLine().trim());
                    ob.setConPanna(true);
                    OrdineBean res = pCtrl.effettuaPreOrdine(ob);
                    System.out.println("-> Confermato: " + res.getDescrizioneCompleta() + " | EUR " + res.getPrezzoTotale());
                } else if ("2".equals(s)) {
                    RichiestaGustoBean rb = new RichiestaGustoBean();
                    rb.setEmailCliente(email);
                    System.out.print("Nome gusto proposto: ");
                    rb.setNomeGustoProposto(scanner.nextLine().trim());
                    System.out.print("Ingrediente: ");
                    rb.setNomeIngredientePrincipale(scanner.nextLine().trim());
                    System.out.print("Kg: ");
                    rb.setQuantitaDaTesto(scanner.nextLine().trim());
                    System.out.print("Giorno ritiro: ");
                    rb.setDataRitiroDesiderata(scanner.nextLine().trim());
                    new RichiestaGustoControl().inviaRichiestaGusto(rb);
                    System.out.println("-> Richiesta inviata!");
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
