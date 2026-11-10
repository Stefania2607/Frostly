package control.applicativo;

import bean.GustoBean;
import bean.IngredienteBean;
import dao.DAOFactory;
import dao.GustoDAO;
import dao.IngredienteDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import model.Gusto;
import model.Ingrediente;

/**
 * Controller applicativo dedicato alla gestione delle giacenze di magazzino
 * e alla produzione di vaschette di gelato nel laboratorio.
 */
public class MagazzinoControl {

    private final IngredienteDAO ingredienteDAO;
    private final GustoDAO gustoDAO;

    public MagazzinoControl() {
        this(DAOFactory.getIngredienteDAO(), DAOFactory.getGustoDAO());
    }

    public MagazzinoControl(IngredienteDAO ingredienteDAO, GustoDAO gustoDAO) {
        this.ingredienteDAO = ingredienteDAO;
        this.gustoDAO = gustoDAO;
    }

    public List<IngredienteBean> visualizzaMagazzino() {
        List<IngredienteBean> risultato = new ArrayList<>();
        for (Ingrediente ing : ingredienteDAO.caricaTuttiIngredienti()) {
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
        Gusto gusto = gustoDAO.trovaGustoPerNome(nomeGusto);
        if (gusto == null) {
            throw new DatiNonValidiException("Gusto non trovato: " + nomeGusto);
        }

        gusto.getRicetta().consumaIngredientiPerProduzione(numeroVaschette);
        gusto.incrementaProduzione(numeroVaschette);

        for (Ingrediente ing : ingredienteDAO.caricaTuttiIngredienti()) {
            ingredienteDAO.aggiornaIngrediente(ing);
        }

        return new GustoBean(gusto.getNome(), gusto.getDescrizione(), gusto.getAllergeni(),
                gusto.getPrezzoAlKg(), gusto.isDisponibileOggi(), gusto.getVaschetteDisponibili());
    }

    public void rifornisciIngredienteMagazzino(String nomeIngrediente, double quantitaAggiunta)
            throws FrostlyException {
        if (quantitaAggiunta <= 0) {
            throw new DatiNonValidiException("La quantita' di rifornimento deve essere positiva.");
        }
        Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nomeIngrediente);
        if (ing == null) {
            throw new DatiNonValidiException("Ingrediente non trovato: " + nomeIngrediente);
        }
        ing.rifornisci(quantitaAggiunta);
        ingredienteDAO.aggiornaIngrediente(ing);
    }

    public void scaricaIngredienteMagazzino(String nomeIngrediente, double quantitaDaScaricare)
            throws FrostlyException {
        if (quantitaDaScaricare <= 0) {
            throw new DatiNonValidiException("La quantita' di scarico deve essere positiva.");
        }
        Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nomeIngrediente);
        if (ing == null) {
            throw new DatiNonValidiException("Ingrediente non trovato: " + nomeIngrediente);
        }
        ing.consuma(quantitaDaScaricare);
        ingredienteDAO.aggiornaIngrediente(ing);
    }

    public void aggiungiNuovoIngrediente(IngredienteBean bean) throws FrostlyException {
        if (bean == null) {
            throw new DatiNonValidiException("I dati del nuovo prodotto non possono essere nulli.");
        }
        String nome = bean.getNome() != null ? bean.getNome().trim() : "";
        if (nome.isEmpty()) {
            throw new DatiNonValidiException("Inserire il nome del prodotto o materia prima.");
        }
        if (bean.getQuantitaDisponibile() < 0) {
            throw new DatiNonValidiException("La quantita' iniziale non puo' essere negativa.");
        }
        if (bean.getSogliaMinima() < 0) {
            throw new DatiNonValidiException("La soglia minima non puo' essere negativa.");
        }
        String unita = bean.getUnitaMisura() != null ? bean.getUnitaMisura().trim() : "";
        if (unita.isEmpty()) {
            throw new DatiNonValidiException("Selezionare un'unita' di misura valida (es. Kg, Litri, Bustine, Bottiglie, Porzioni, Pezzi).");
        }
        if (ingredienteDAO.trovaIngredientePerNome(nome) != null) {
            throw new DatiNonValidiException("La voce '" + nome + "' e' gia' presente a magazzino.");
        }

        Ingrediente nuovo = new Ingrediente(nome, bean.getQuantitaDisponibile(), bean.getSogliaMinima(), unita);
        ingredienteDAO.salvaNuovoIngrediente(nuovo);
    }

    public void eliminaIngredienteMagazzino(String nomeIngrediente) throws FrostlyException {
        if (nomeIngrediente == null || nomeIngrediente.trim().isEmpty()) {
            throw new DatiNonValidiException("Selezionare la voce di magazzino da eliminare.");
        }
        String nome = nomeIngrediente.trim();
        Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nome);
        if (ing == null) {
            throw new DatiNonValidiException("Voce '" + nome + "' non trovata nel magazzino.");
        }
        ingredienteDAO.eliminaIngrediente(nome);
    }
}
