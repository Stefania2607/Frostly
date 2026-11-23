package control.applicativo;

import bean.GustoBean;
import dao.DAOFactory;
import dao.GustoDAO;
import dao.IngredienteDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import frostly.exception.RisorsaNonTrovataException;
import java.util.ArrayList;
import java.util.List;
import model.Gusto;
import model.Ricetta;

/**
 * Controller applicativo dedicato esclusivamente alla consultazione e gestione
 * del catalogo menu e dei prodotti in vetrina.
 */
public class GestioneMenuControl {

    private final GustoDAO gustoDAO;
    private final IngredienteDAO ingredienteDAO;

    public GestioneMenuControl() {
        this(DAOFactory.getGustoDAO(), DAOFactory.getIngredienteDAO());
    }

    public GestioneMenuControl(GustoDAO gustoDAO) {
        this(gustoDAO, DAOFactory.getIngredienteDAO());
    }

    public GestioneMenuControl(GustoDAO gustoDAO, IngredienteDAO ingredienteDAO) {
        this.gustoDAO = gustoDAO;
        this.ingredienteDAO = ingredienteDAO;
    }

    public List<GustoBean> visualizzaMenuGusti() {
        List<GustoBean> risultato = new ArrayList<>();
        List<model.Ingrediente> magazzino = ingredienteDAO != null
                ? ingredienteDAO.caricaTuttiIngredienti()
                : java.util.Collections.emptyList();
        for (Gusto g : gustoDAO.caricaTuttiGusti()) {
            int porzioniDisp = g.isDisponibileOggi() ? calcolaPorzioniDisponibili(g, magazzino) : 0;
            boolean disponibileReale = g.isDisponibileOggi() && (porzioniDisp != 0);
            int vaschetteOpezzi = porzioniDisp >= 0 ? porzioniDisp : g.getVaschetteDisponibili();
            GustoBean bean = new GustoBean(
                    g.getNome(),
                    g.getDescrizione(),
                    g.getAllergeni(),
                    g.getPrezzoAlKg(),
                    disponibileReale,
                    vaschetteOpezzi);
            bean.setCategoria(g.getCategoria());
            bean.setSottocategoria(g.getSottocategoria());
            bean.setImmagineUrl(g.getImmagineUrl());
            risultato.add(bean);
        }
        return risultato;
    }

    /**
     * Calcola quante porzioni/pezzi di un prodotto sono ordinabili al momento in base al magazzino.
     * Restituisce -1 se il prodotto non ha un vincolo di conteggio diretto o non è mappato.
     */
    public int calcolaMaxOrdinabile(String nomeProdotto) {
        if (nomeProdotto == null || nomeProdotto.isBlank() || gustoDAO == null || ingredienteDAO == null) {
            return -1;
        }
        Gusto g = gustoDAO.trovaGustoPerNome(nomeProdotto.trim());
        if (g == null) {
            return -1;
        }
        if (!g.isDisponibileOggi()) {
            return 0;
        }
        return calcolaPorzioniDisponibili(g, ingredienteDAO.caricaTuttiIngredienti());
    }

    public int calcolaPorzioniDisponibili(Gusto g, List<model.Ingrediente> magazzino) {
        if (g == null || magazzino == null || magazzino.isEmpty()) {
            return -1;
        }
        String nome = g.getNome() != null ? g.getNome().trim() : "";
        if (nome.isEmpty()) {
            return -1;
        }
        String cat = g.getCategoria() != null ? g.getCategoria().trim() : "";

        if ("Gelati".equalsIgnoreCase(cat)) {
            return haScorteSufficientiPerProdotto(g, magazzino) ? g.getVaschetteDisponibili() : 0;
        }

        int max = Integer.MAX_VALUE;
        boolean vincolato = false;

        for (java.util.Map.Entry<String, Double> entry : CorrelazioneMagazzino.calcolaConsumoArticolo(nome, cat).entrySet()) {
            int p = porzioniDaIngrediente(magazzino, entry.getKey(), entry.getValue());
            if (p >= 0) {
                max = Math.min(max, p);
                vincolato = true;
            }
        }

        if (!CorrelazioneMagazzino.isProdottoEspresso(nome, cat)) {
            model.Ingrediente articolo = CorrelazioneMagazzino.trovaArticoloDiretto(magazzino, nome);
            if (articolo != null) {
                int pezzi = (int) Math.floor(Math.max(0.0, articolo.getQuantitaDisponibile()));
                max = Math.min(max, pezzi);
                vincolato = true;
            }
        }
        return vincolato ? max : -1;
    }

    private int porzioniDaIngrediente(List<model.Ingrediente> magazzino, String nomeIngrediente, double consumoPerPorzione) {
        if (consumoPerPorzione <= 0.0) return -1;
        for (model.Ingrediente i : magazzino) {
            if (i.getNome() != null && i.getNome().equalsIgnoreCase(nomeIngrediente)) {
                return (int) Math.floor(Math.max(0.0, i.getQuantitaDisponibile()) / consumoPerPorzione);
            }
        }
        return -1;
    }

    /**
     * Verifica automaticamente se in magazzino sono presenti scorte sufficienti
     * per tutte le materie prime o articoli correlati al prodotto del menu.
     */
    public boolean haScorteSufficientiPerProdotto(Gusto g, List<model.Ingrediente> magazzino) {
        if (g == null || magazzino == null || magazzino.isEmpty()) {
            return true;
        }
        String nome = g.getNome() != null ? g.getNome().trim() : "";
        if (nome.isEmpty()) {
            return true;
        }
        String cat = g.getCategoria() != null ? g.getCategoria().trim() : "";

        if ("Gelati".equalsIgnoreCase(cat)) {
            for (java.util.Map.Entry<String, Double> entry : CorrelazioneMagazzino.calcolaMinimoGustoGelato(nome).entrySet()) {
                if (!haScortaSePresente(magazzino, entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return calcolaPorzioniDisponibili(g, magazzino) != 0;
    }

    private boolean haScortaSePresente(List<model.Ingrediente> magazzino, String nomeIngrediente, double minimo) {
        for (model.Ingrediente i : magazzino) {
            if (i.getNome() != null && i.getNome().equalsIgnoreCase(nomeIngrediente)) {
                return i.getQuantitaDisponibile() >= minimo;
            }
        }
        return true;
    }

    public void salvaOAggiornaGustoMenu(String nome, String descrizione, String allergeni,
                                        double prezzo, boolean disponibile) throws FrostlyException {
        salvaOAggiornaGustoMenu(nome, descrizione, allergeni, prezzo, disponibile, "Gelati", "Creme", "");
    }

    public void salvaOAggiornaGustoMenu(String nome, String descrizione, String allergeni,
                                        double prezzo, boolean disponibile, String categoria,
                                        String sottocategoria, String immagineUrl) throws FrostlyException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DatiNonValidiException("Inserisci il nome del gusto da aggiungere o aggiornare.");
        }
        if (descrizione == null || descrizione.trim().isEmpty()) {
            throw new DatiNonValidiException("Inserisci una descrizione per il gusto.");
        }
        if (Double.isNaN(prezzo) || prezzo <= 0) {
            throw new DatiNonValidiException("Il prezzo deve essere maggiore di zero.");
        }

        String allergeniPuliti = (allergeni == null || allergeni.trim().isEmpty())
                ? "Latte" : allergeni.trim();

        String catPulita = (categoria == null || categoria.trim().isEmpty()) ? "Gelati" : categoria.trim();
        String sottoPulita = (sottocategoria == null || sottocategoria.trim().isEmpty())
                ? catPulita : sottocategoria.trim();
        String imgPulita = immagineUrl == null ? "" : immagineUrl.trim();

        Gusto esistente = gustoDAO.trovaGustoPerNome(nome.trim());
        if (esistente != null) {
            esistente.setDescrizione(descrizione.trim());
            esistente.setAllergeni(allergeniPuliti);
            esistente.setPrezzoAlKg(prezzo);
            esistente.setDisponibileOggi(disponibile);
            esistente.setCategoria(catPulita);
            esistente.setSottocategoria(sottoPulita);
            esistente.setImmagineUrl(imgPulita);
            gustoDAO.salvaGustoMenu(esistente);
        } else {
            Ricetta ric = new Ricetta("Ricetta " + nome.trim(), "Mantecazione artigianale di laboratorio");
            Gusto nuovo = new Gusto(nome.trim(), descrizione.trim(), allergeniPuliti, prezzo, ric);
            nuovo.setDisponibileOggi(disponibile);
            nuovo.setCategoria(catPulita);
            nuovo.setSottocategoria(sottoPulita);
            nuovo.setImmagineUrl(imgPulita);
            gustoDAO.salvaGustoMenu(nuovo);
        }

        sincronizzaMagazzinoPerProdottoMenu(nome.trim(), catPulita);
    }

    private void sincronizzaMagazzinoPerProdottoMenu(String nome, String categoria) {
        if (ingredienteDAO == null) {
            return;
        }
        // Gelati, Caffetteria e Crepes sono preparati espressi composti da materie prime già gestite a magazzino
        if ("Gelati".equalsIgnoreCase(categoria) || "Caffetteria".equalsIgnoreCase(categoria)
                || "Crepes".equalsIgnoreCase(categoria)) {
            return;
        }
        model.Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nome);
        if (ing == null) {
            String unita;
            double qtaIniziale;
            double sogliaMin;
            if ("Tisane".equalsIgnoreCase(categoria) || "Cioccolate calde".equalsIgnoreCase(categoria)) {
                unita = "Bustine";
                qtaIniziale = 35.0;
                sogliaMin = 10.0;
            } else if ("Bevande".equalsIgnoreCase(categoria)) {
                unita = "Bottiglie";
                qtaIniziale = 30.0;
                sogliaMin = 10.0;
            } else if ("Gelati".equalsIgnoreCase(categoria)) {
                unita = "Kg";
                qtaIniziale = 5.0;
                sogliaMin = 1.5;
            } else {
                unita = "Porzioni";
                qtaIniziale = 20.0;
                sogliaMin = 5.0;
            }
            model.Ingrediente nuovo = new model.Ingrediente(
                    nome, qtaIniziale, sogliaMin, unita, model.Ingrediente.inferisciCategoria(nome, unita));
            ingredienteDAO.salvaNuovoIngrediente(nuovo);
        }
    }

    public void cambiaDisponibilitaGustoMenu(String nomeGusto) throws FrostlyException {
        if (nomeGusto == null || nomeGusto.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona prima un gusto dal menu.");
        }
        Gusto g = gustoDAO.trovaGustoPerNome(nomeGusto.trim());
        if (g == null) {
            throw new RisorsaNonTrovataException("Gusto non trovato nel menu: " + nomeGusto);
        }
        g.setDisponibileOggi(!g.isDisponibileOggi());
        gustoDAO.salvaGustoMenu(g);
    }

    public void eliminaGustoMenu(String nomeGusto) throws FrostlyException {
        if (nomeGusto == null || nomeGusto.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona prima il gusto da rimuovere.");
        }
        if (gustoDAO.trovaGustoPerNome(nomeGusto.trim()) == null) {
            throw new RisorsaNonTrovataException("Gusto non presente nel menu: " + nomeGusto);
        }
        gustoDAO.rimuoviGustoMenu(nomeGusto.trim());
    }
}
