package control.cli;

import bean.EmergenzaBean;
import bean.IngredienteBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.MagazzinoControl;
import frostly.exception.FrostlyException;
import java.util.Scanner;

/**
 * Controller CLI dedicato alla gestione di turni, emergenze e magazzino per Maestro e Dipendenti.
 */
@SuppressWarnings("java:S106")
public class TurniEmergenzeControlCLI {

    private final Scanner scanner;
    private final GestioneTurniEmergenzeControl turniControl;
    private final MagazzinoControl magazzinoControl;

    public TurniEmergenzeControlCLI(Scanner scanner) {
        this.scanner = scanner;
        this.turniControl = new GestioneTurniEmergenzeControl();
        this.magazzinoControl = new MagazzinoControl();
    }

    public void gestisciTurniMaestro() throws FrostlyException {
        for (TurnoBean t : turniControl.visualizzaTuttiTurni()) {
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
            turniControl.pubblicaNuovoTurno(tb);
            System.out.println("-> Turno pubblicato!");
        }
    }

    public void gestisciEmergenzeMaestro() throws FrostlyException {
        for (EmergenzaBean e : turniControl.visualizzaTutteEmergenze()) {
            System.out.println(e.getCodiceEmergenza() + " | Turno " + e.getCodiceTurno() + " | Stato: " + e.getStato());
        }
        System.out.print("Codice emergenza da approvare (o INVIO): ");
        String cod = scanner.nextLine().trim();
        if (!cod.isEmpty()) {
            System.out.print("Email sostituto: ");
            turniControl.approvaEmergenzaConSostituto(cod, scanner.nextLine().trim(), "OK da CLI");
            System.out.println("-> Emergenza approvata!");
        }
    }

    public void mostraMagazzino() {
        for (IngredienteBean i : magazzinoControl.visualizzaMagazzino()) {
            System.out.println(" - " + i.getNome() + ": " + i.getQuantitaDisponibile() + " " + i.getUnitaMisura());
        }
    }

    public void mostraTurniDipendente(String emailDipendente) throws FrostlyException {
        for (TurnoBean t : turniControl.visualizzaTurniDipendente(emailDipendente)) {
            System.out.println(t.getCodiceTurno() + " | " + t.getData() + " [" + t.getStato() + "]");
        }
    }

    public void segnalaEmergenzaDipendente(String emailDipendente) throws FrostlyException {
        EmergenzaBean eb = new EmergenzaBean();
        eb.setEmailRichiedente(emailDipendente);
        System.out.print("Codice turno: ");
        eb.setCodiceTurno(scanner.nextLine().trim());
        System.out.print("Motivazione: ");
        eb.setMotivazione(scanner.nextLine().trim());
        turniControl.segnalaEmergenzaTurno(eb);
        System.out.println("-> Emergenza inviata!");
    }
}
