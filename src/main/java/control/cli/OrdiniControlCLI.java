package control.cli;

import bean.GustoBean;
import bean.OrdineBean;
import bean.RichiestaGustoBean;
import control.applicativo.GestioneMenuControl;
import control.applicativo.OrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import frostly.exception.IngredienteEsauritoException;
import java.util.Scanner;

/**
 * Controller CLI dedicato ai casi d'uso di pre-ordine vaschette e richieste di gusti personalizzati.
 */
@SuppressWarnings("java:S106")
public class OrdiniControlCLI {

    private final Scanner scanner;
    private final GestioneMenuControl menuControl;
    private final OrdiniControl ordiniControl;
    private final RichiestaGustoControl richiestaGustoControl;

    public OrdiniControlCLI(Scanner scanner) {
        this.scanner = scanner;
        this.menuControl = new GestioneMenuControl();
        this.ordiniControl = new OrdiniControl();
        this.richiestaGustoControl = new RichiestaGustoControl();
    }

    public void effettuaPreOrdineVaschetta(String emailCliente) throws FrostlyException {
        for (GustoBean g : menuControl.visualizzaMenuGusti()) {
            if (!"Gelati".equalsIgnoreCase(g.getCategoria())) {
                continue;
            }
            System.out.println(" * " + g.getNome() + " (EUR " + g.getPrezzoAlKg() + "/kg)");
        }
        OrdineBean ob = new OrdineBean();
        ob.setEmailCliente(emailCliente);
        System.out.print("Gusto scelto: ");
        ob.setNomeGusto(scanner.nextLine().trim());
        System.out.print("Kg: ");
        ob.setPesoDaTesto(scanner.nextLine().trim());
        System.out.print("Giorno ritiro: ");
        ob.setDataRitiro(scanner.nextLine().trim());
        System.out.print("Aggiungere panna montata? (s/n): ");
        String respPanna = scanner.nextLine().trim();
        ob.setConPanna("s".equalsIgnoreCase(respPanna) || "si".equalsIgnoreCase(respPanna));
        System.out.print("Aggiungere granella di pistacchio di Bronte? (s/n): ");
        String respGranella = scanner.nextLine().trim();
        ob.setConGranellaBronte("s".equalsIgnoreCase(respGranella) || "si".equalsIgnoreCase(respGranella));

        OrdineBean res = ordiniControl.effettuaPreOrdine(ob);
        String stato = res.getStato() != null ? res.getStato() : "IN_ATTESA";
        String etichettaStato = "IN_ATTESA".equalsIgnoreCase(stato)
                ? "Inviato (in attesa di conferma dallo staff)"
                : stato;
        System.out.println("-> Ordine " + res.getCodiceOrdine() + " — " + etichettaStato + ": "
                + res.getDescrizioneCompleta() + " | EUR " + res.getPrezzoTotale());
    }

    public void inviaRichiestaGustoPersonalizzato(String emailCliente) throws FrostlyException {
        RichiestaGustoBean rb = new RichiestaGustoBean();
        rb.setEmailCliente(emailCliente);
        System.out.print("Nome gusto proposto: ");
        rb.setNomeGustoProposto(scanner.nextLine().trim());
        System.out.print("Ingrediente: ");
        rb.setNomeIngredientePrincipale(scanner.nextLine().trim());
        System.out.print("Kg: ");
        rb.setQuantitaDaTesto(scanner.nextLine().trim());
        System.out.print("Giorno ritiro: ");
        rb.setDataRitiroDesiderata(scanner.nextLine().trim());

        richiestaGustoControl.inviaRichiestaGusto(rb);
        System.out.println("-> Richiesta inviata!");
    }

    public void gestisciRichiesteGustoMaestro() throws FrostlyException {
        for (RichiestaGustoBean r : richiestaGustoControl.visualizzaTutteRichieste()) {
            System.out.println(r.getCodiceRichiesta() + " | '" + r.getNomeGustoProposto() + "' | Stato: " + r.getStato());
        }
        System.out.print("Codice richiesta da approvare (o INVIO): ");
        String cod = scanner.nextLine().trim();
        if (!cod.isEmpty()) {
            try {
                richiestaGustoControl.approvaRichiestaGusto(cod, "Confermato da CLI");
                System.out.println("-> Approvata!");
            } catch (IngredienteEsauritoException ex) {
                System.out.println(ex.getMessage());
                richiestaGustoControl.approvaConRifornimentoUrgente(cod, 5.0, "Pronto dopo rifornimento");
                System.out.println("-> Rifornito e approvato!");
            }
        }
    }
}
