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
import model.Ricetta;
import model.VaschettaBase;
import model.VaschettaComponent;

/**
 * Controller applicativo STATELESS per:
 * - Consultazione e gestione del Menu Vetrina Gusti (aperto a tutti in lettura, aggiornabile da Admin e Dipendenti)
 * - Pre-ordine vaschetta da parte del Cliente (con applicazione del pattern GoF Decorator)
 * - Gestione produzione giornaliera e magazzino ingredienti.
 */
public class ProduzioneOrdiniControl {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public List<GustoBean> visualizzaMenuGusti() {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<GustoBean> risultato = new ArrayList<>();
        for (Gusto g : dao.caricaTuttiGusti()) {
            GustoBean bean = new GustoBean(
                    g.getNome(),
                    g.getDescrizione(),
                    g.getAllergeni(),
                    g.getPrezzoAlKg(),
                    g.isDisponibileOggi(),
                    g.getVaschetteDisponibili());
            bean.setCategoria(g.getCategoria());
            bean.setSottocategoria(g.getSottocategoria());
            bean.setImmagineUrl(g.getImmagineUrl());
            risultato.add(bean);
        }
        return risultato;
    }

    public void salvaOAggiornaGustoMenu(String nome, String descrizione, String allergeni,
                                        String prezzoTesto, boolean disponibile) throws FrostlyException {
        salvaOAggiornaGustoMenu(nome, descrizione, allergeni, prezzoTesto, disponibile, "Gelati", "Creme", "");
    }

    public void salvaOAggiornaGustoMenu(String nome, String descrizione, String allergeni,
                                        String prezzoTesto, boolean disponibile, String categoria,
                                        String sottocategoria, String immagineUrl) throws FrostlyException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DatiNonValidiException("Inserisci il nome del gusto da aggiungere o aggiornare.");
        }
        if (descrizione == null || descrizione.trim().isEmpty()) {
            throw new DatiNonValidiException("Inserisci una descrizione per il gusto.");
        }
        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoTesto.trim().replace(",", "."));
        } catch (NumberFormatException | NullPointerException e) {
            throw new DatiNonValidiException("Inserisci un prezzo valido (es. 2.50 o 24.0).");
        }
        if (prezzo <= 0) {
            throw new DatiNonValidiException("Il prezzo deve essere maggiore di zero.");
        }

        String allergeniPuliti = (allergeni == null || allergeni.trim().isEmpty())
                ? "Latte" : allergeni.trim();

        String catPulita = (categoria == null || categoria.trim().isEmpty()) ? "Gelati" : categoria.trim();
        String sottoPulita = (sottocategoria == null || sottocategoria.trim().isEmpty())
                ? catPulita : sottocategoria.trim();
        String imgPulita = immagineUrl == null ? "" : immagineUrl.trim();
        if (!imgPulita.isEmpty()
                && !imgPulita.matches("(?i)^(https?|file)://.+")
                && !imgPulita.startsWith("immagini_menu/")
                && !imgPulita.matches("^[A-Za-z0-9._-]+$")) {
            throw new DatiNonValidiException("La foto deve essere un file locale (immagini_menu/...) o un link http:// o https://");
        }

        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        Gusto esistente = dao.trovaGustoPerNome(nome.trim());
        if (esistente != null) {
            esistente.setDescrizione(descrizione.trim());
            esistente.setAllergeni(allergeniPuliti);
            esistente.setPrezzoAlKg(prezzo);
            esistente.setCategoria(catPulita);
            esistente.setSottocategoria(sottoPulita);
            esistente.setImmagineUrl(imgPulita);
            dao.salvaGustoMenu(esistente);
        } else {
            Ricetta ric = new Ricetta("Ricetta " + nome.trim(), "Mantecazione artigianale di laboratorio");
            Gusto nuovo = new Gusto(nome.trim(), descrizione.trim(), allergeniPuliti, prezzo, ric);
            nuovo.setDisponibileOggi(disponibile);
            nuovo.setCategoria(catPulita);
            nuovo.setSottocategoria(sottoPulita);
            nuovo.setImmagineUrl(imgPulita);
            dao.salvaGustoMenu(nuovo);
        }
    }

    public void cambiaDisponibilitaGustoMenu(String nomeGusto) throws FrostlyException {
        if (nomeGusto == null || nomeGusto.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona prima un gusto dal menu.");
        }
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        Gusto g = dao.trovaGustoPerNome(nomeGusto.trim());
        if (g == null) {
            throw new DatiNonValidiException("Gusto non trovato nel menu: " + nomeGusto);
        }
        g.setDisponibileOggi(!g.isDisponibileOggi());
        dao.salvaGustoMenu(g);
    }

    public void eliminaGustoMenu(String nomeGusto) throws FrostlyException {
        if (nomeGusto == null || nomeGusto.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona prima il gusto da rimuovere.");
        }
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        if (dao.trovaGustoPerNome(nomeGusto.trim()) == null) {
            throw new DatiNonValidiException("Gusto non presente nel menu: " + nomeGusto);
        }
        dao.rimuoviGustoMenu(nomeGusto.trim());
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
