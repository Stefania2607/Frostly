package dao;

import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.Emergenza;
import model.Gusto;
import model.Ingrediente;
import model.MaestroGelataio;
import model.Notifica;
import model.OrdineCliente;
import model.Ricetta;
import model.RichiestaGustoPersonalizzato;
import model.Turno;

public class InMemoryFrostlyDAO implements FrostlyDAO {

    private static InMemoryFrostlyDAO instance;

    private final MaestroGelataio maestro;
    private final List<Dipendente> dipendenti;
    private final List<Cliente> clienti;
    private final List<Turno> turni;
    private final List<Emergenza> emergenze;
    private final List<Ingrediente> ingredienti;
    private final List<Gusto> gusti;
    private final List<RichiestaGustoPersonalizzato> richiesteGusto;
    private final List<OrdineCliente> ordini;
    private final List<Notifica> notifiche;

    protected InMemoryFrostlyDAO() {
        this.maestro = new MaestroGelataio("Guglielmo", "Bianchi", "maestro@frostly.it", "admin123");
        this.dipendenti = new ArrayList<>();
        this.clienti = new ArrayList<>();
        this.turni = new ArrayList<>();
        this.emergenze = new ArrayList<>();
        this.ingredienti = new ArrayList<>();
        this.gusti = new ArrayList<>();
        this.richiesteGusto = new ArrayList<>();
        this.ordini = new ArrayList<>();
        this.notifiche = new ArrayList<>();
        popolaDatiIniziali();
    }

    public static synchronized InMemoryFrostlyDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryFrostlyDAO();
        }
        return instance;
    }

    private void popolaDatiIniziali() {
        Dipendente d1 = new Dipendente("Mario", "Rossi", "mario@frostly.it", "pass123");
        Dipendente d2 = new Dipendente("Giulia", "Verdi", "giulia@frostly.it", "pass123");
        Dipendente d3 = new Dipendente("Luca", "Neri", "luca@frostly.it", "pass123");
        dipendenti.add(d1);
        dipendenti.add(d2);
        dipendenti.add(d3);

        Cliente c1 = new Cliente("Elena", "Conti", "elena@cliente.it", "pass123");
        Cliente c2 = new Cliente("Marco", "Fabbri", "marco@cliente.it", "pass123");
        clienti.add(c1);
        clienti.add(c2);

        Turno t1 = new Turno("TRN-01", "Lunedi", "08:00", "14:00", d1);
        t1.pubblica();
        d1.aggiungiTurno(t1);
        turni.add(t1);

        Turno t2 = new Turno("TRN-02", "Lunedi", "14:00", "20:00", d2);
        t2.pubblica();
        d2.aggiungiTurno(t2);
        turni.add(t2);

        Turno t3 = new Turno("TRN-03", "Martedi", "08:00", "14:00", d1);
        t3.pubblica();
        d1.aggiungiTurno(t3);
        turni.add(t3);

        Ingrediente latte = new Ingrediente("Latte Fresco Alta Qualita", 25.0, 10.0, "Litri");
        Ingrediente pistacchio = new Ingrediente("Pasta Pistacchio di Bronte DOP", 6.0, 2.5, "Kg");
        Ingrediente cioccolato = new Ingrediente("Cioccolato Fondente Ecuador 75%", 8.0, 3.0, "Kg");
        Ingrediente nocciola = new Ingrediente("Nocciola Piemonte IGP", 5.0, 2.0, "Kg");
        Ingrediente mango = new Ingrediente("Polpa di Mango Alphonso", 1.5, 2.0, "Kg");
        ingredienti.add(latte);
        ingredienti.add(pistacchio);
        ingredienti.add(cioccolato);
        ingredienti.add(nocciola);
        ingredienti.add(mango);

        Ricetta ricPistacchio = new Ricetta("Ricetta Pistacchio Reale", "Pastorizzazione a 85 gradi e mantecazione lenta");
        ricPistacchio.aggiungiIngrediente(latte, 2.5);
        ricPistacchio.aggiungiIngrediente(pistacchio, 0.8);

        Ricetta ricCioccolato = new Ricetta("Ricetta Fondente Nero", "Infusione cacao a caldo e mantecazione");
        ricCioccolato.aggiungiIngrediente(latte, 2.0);
        ricCioccolato.aggiungiIngrediente(cioccolato, 1.2);

        Ricetta ricNocciola = new Ricetta("Ricetta Crema di Nocciola", "Tostatura artigianale e mantecazione");
        ricNocciola.aggiungiIngrediente(latte, 2.5);
        ricNocciola.aggiungiIngrediente(nocciola, 0.9);

        gusti.add(new Gusto("Pistacchio di Bronte DOP", "Cremoso al pistacchio siciliano", "Latte, Frutta a guscio", 26.0, ricPistacchio));
        gusti.add(new Gusto("Fondente Ecuador 75%", "Cioccolato intenso monorigine", "Latte", 24.0, ricCioccolato));
        gusti.add(new Gusto("Nocciola Piemonte IGP", "Classico cremoso alle nocciole", "Latte, Frutta a guscio", 25.0, ricNocciola));

        Emergenza emIniziale = new Emergenza("EMG-01", t3, d1, "Visita medica improvvisa martedi mattina");
        t3.dichiaraEmergenza();
        emergenze.add(emIniziale);

        RichiestaGustoPersonalizzato reqIniziale = new RichiestaGustoPersonalizzato(
                "REQ-01", c1, "Pistacchio Salato Croccante", pistacchio, 1.5, "Venerdi", "Poco zucchero e sale di Cervia");
        richiesteGusto.add(reqIniziale);

        notifiche.add(new Notifica("maestro@frostly.it", "Nuova Emergenza Turno",
                "Mario Rossi ha segnalato un'emergenza per il turno TRN-03 (Martedi 08:00-14:00).", "Oggi 09:15"));
        notifiche.add(new Notifica("maestro@frostly.it", "Nuova Richiesta Gusto",
                "Elena Conti ha richiesto il gusto personalizzato 'Pistacchio Salato Croccante' (1.5 kg).", "Oggi 10:30"));
    }

    @Override
    public MaestroGelataio getMaestroGelataio() {
        return maestro;
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        return new ArrayList<>(dipendenti);
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        for (Dipendente d : dipendenti) {
            if (d.getEmail().equalsIgnoreCase(email)) {
                return d;
            }
        }
        return null;
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        return new ArrayList<>(clienti);
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        for (Cliente c : clienti) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        return new ArrayList<>(turni);
    }

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        for (int i = 0; i < turni.size(); i++) {
            if (turni.get(i).getCodiceTurno().equals(turno.getCodiceTurno())) {
                turni.set(i, turno);
                return;
            }
        }
        turni.add(turno);
    }

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        return new ArrayList<>(emergenze);
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
        for (int i = 0; i < emergenze.size(); i++) {
            if (emergenze.get(i).getCodiceEmergenza().equals(emergenza.getCodiceEmergenza())) {
                emergenze.set(i, emergenza);
                return;
            }
        }
        emergenze.add(emergenza);
    }

    @Override
    public List<Ingrediente> caricaTuttiIngredienti() {
        return new ArrayList<>(ingredienti);
    }

    @Override
    public Ingrediente trovaIngredientePerNome(String nome) {
        for (Ingrediente ing : ingredienti) {
            if (ing.getNome().equalsIgnoreCase(nome)) {
                return ing;
            }
        }
        return null;
    }

    @Override
    public void aggiornaIngrediente(Ingrediente ingrediente) {
        for (int i = 0; i < ingredienti.size(); i++) {
            if (ingredienti.get(i).getNome().equalsIgnoreCase(ingrediente.getNome())) {
                ingredienti.set(i, ingrediente);
                return;
            }
        }
        ingredienti.add(ingrediente);
    }

    @Override
    public List<Gusto> caricaTuttiGusti() {
        return new ArrayList<>(gusti);
    }

    @Override
    public Gusto trovaGustoPerNome(String nome) {
        for (Gusto g : gusti) {
            if (g.getNome().equalsIgnoreCase(nome)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto() {
        return new ArrayList<>(richiesteGusto);
    }

    @Override
    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        for (int i = 0; i < richiesteGusto.size(); i++) {
            if (richiesteGusto.get(i).getCodiceRichiesta().equals(richiesta.getCodiceRichiesta())) {
                richiesteGusto.set(i, richiesta);
                return;
            }
        }
        richiesteGusto.add(richiesta);
    }

    @Override
    public List<OrdineCliente> caricaTuttiOrdini() {
        return new ArrayList<>(ordini);
    }

    @Override
    public void salvaOrdine(OrdineCliente ordine) {
        for (int i = 0; i < ordini.size(); i++) {
            if (ordini.get(i).getCodiceOrdine().equals(ordine.getCodiceOrdine())) {
                ordini.set(i, ordine);
                return;
            }
        }
        ordini.add(ordine);
    }

    @Override
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        List<Notifica> risultato = new ArrayList<>();
        for (Notifica n : notifiche) {
            if (n.getEmailDestinatario().equalsIgnoreCase(emailDestinatario)) {
                risultato.add(n);
            }
        }
        return risultato;
    }

    @Override
    public void inviaNotifica(Notifica notifica) {
        this.notifiche.add(0, notifica);
    }
}