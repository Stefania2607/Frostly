package control.applicativo;

import bean.GustoBean;
import bean.IngredienteBean;
import bean.OrdineBean;
import dao.DAOFactory;
import dao.FrostlyDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.ConGranellaBronteDecorator;
import model.ConPannaMontataDecorator;
import model.Gusto;
import model.Ingrediente;
import model.Notifica;
import model.OrdineCliente;
import model.VaschettaBase;
import model.VaschettaComponent;

/**
 * Controller applicativo STATELESS per:
 * - UC4: Pre-ordine vaschetta da parte del Cliente (con applicazione del pattern GoF Decorator)
 * - UC5: Gestione produzione giornaliera e magazzino ingredienti da parte del Maestro Gelataio.
 */
public class ProduzioneOrdiniControl {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public List<GustoBean> visualizzaMenuGusti() {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<GustoBean> risultato = new ArrayList<>();
        for (Gusto g : dao.caricaTuttiGusti()) {
            risultato.add(new GustoBean(
                    g.getNome(),
                    g.getDescrizione(),
                    g.getAllergeni(),
                    g.getPrezzoAlKg(),
                    g.isDisponibileOggi(),
                    g.getVaschetteDisponibili()));
        }
        return risultato;
    }

    public List<IngredienteBean> visualizzaMagazzino() {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<IngredienteBean> risultato = new ArrayList<>();
        for (Ingrediente ing : dao.caricaTuttiIngredienti()) {
            risultato.add(new IngredienteBean(
                    ing.getNome(),
                    Math.round(ing.getQuantitaDisponibile() * 100.0) / 100.0,
                    ing.getSogliaMinima(),
                    ing.getUnitaMisura(),
                    ing.isSottoSoglia()));
        }
        return risultato;
    }

    public GustoBean avviaProduzioneGusto(String nomeGusto, int numeroVaschette) throws FrostlyException {
        if (numeroVaschette <= 0) {
            throw new DatiNonValidiException("Il numero di vaschette da produrre deve essere maggiore di zero.");
        }
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        Gusto gusto = dao.trovaGustoPerNome(nomeGusto);
        if (gusto == null) {
            throw new DatiNonValidiException("Gusto non trovato: " + nomeGusto);
        }

        gusto.getRicetta().consumaIngredientiPerProduzione(numeroVaschette);
        gusto.incrementaProduzione(numeroVaschette);

        for (Ingrediente ing : dao.caricaTuttiIngredienti()) {
            dao.aggiornaIngrediente(ing);
        }

        return new GustoBean(gusto.getNome(), gusto.getDescrizione(), gusto.getAllergeni(),
                gusto.getPrezzoAlKg(), gusto.isDisponibileOggi(), gusto.getVaschetteDisponibili());
    }

    public void rifornisciIngredienteMagazzino(String nomeIngrediente, double quantitaAggiunta)
            throws FrostlyException {
        if (quantitaAggiunta <= 0) {
            throw new DatiNonValidiException("La quantita' di rifornimento deve essere positiva.");
        }
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        Ingrediente ing = dao.trovaIngredientePerNome(nomeIngrediente);
        if (ing == null) {
            throw new DatiNonValidiException("Ingrediente non trovato: " + nomeIngrediente);
        }
        ing.rifornisci(quantitaAggiunta);
        dao.aggiornaIngrediente(ing);
    }

    public OrdineBean effettuaPreOrdine(OrdineBean beanInput) throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();

        Cliente cliente = dao.trovaClientePerEmail(beanInput.getEmailCliente());
        Gusto gusto = dao.trovaGustoPerNome(beanInput.getNomeGusto());
        if (cliente == null || gusto == null) {
            throw new DatiNonValidiException("Cliente o Gusto selezionato non valido.");
        }
        if (!gusto.isDisponibileOggi()) {
            throw new DatiNonValidiException("Il gusto '" + gusto.getNome() + "' e' attualmente esaurito.");
        }

        // Costruzione dinamica della vaschetta tramite il pattern GoF Decorator
        VaschettaComponent vaschettaDecorata = new VaschettaBase(gusto, beanInput.getPesoKg());
        if (beanInput.isConPanna()) {
            vaschettaDecorata = new ConPannaMontataDecorator(vaschettaDecorata);
        }
        if (beanInput.isConGranellaBronte()) {
            vaschettaDecorata = new ConGranellaBronteDecorator(vaschettaDecorata);
        }

        String codiceOrdine = String.format("ORD-%02d", dao.caricaTuttiOrdini().size() + 1);
        OrdineCliente nuovoOrdine = new OrdineCliente(codiceOrdine, cliente, gusto,
                beanInput.getPesoKg(), beanInput.getDataRitiro());
        dao.salvaOrdine(nuovoOrdine);

        beanInput.setCodiceOrdine(codiceOrdine);
        beanInput.setDescrizioneCompleta(vaschettaDecorata.getDescrizioneCompleta());
        beanInput.setPrezzoTotale(vaschettaDecorata.calcolaPrezzoFinale());
        beanInput.setStato(nuovoOrdine.getStato().name());

        dao.inviaNotifica(new Notifica(
                dao.getMaestroGelataio().getEmail(),
                "Nuovo Pre-ordine Vaschetta " + codiceOrdine,
                cliente.getNome() + " " + cliente.getCognome() + " ha prenotato: "
                        + beanInput.getDescrizioneCompleta() + " per " + beanInput.getDataRitiro()
                        + " (Totale: EUR " + beanInput.getPrezzoTotale() + ").",
                oraCorrente()));

        return beanInput;
    }

    private String oraCorrente() {
        return "Oggi " + LocalTime.now().format(TIME_FMT);
    }
}