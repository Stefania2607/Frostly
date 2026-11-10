package control.applicativo;

import bean.GustoBean;
import dao.DAOFactory;
import dao.GustoDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import model.Gusto;
import model.Ricetta;

/**
 * Controller applicativo dedicato esclusivamente alla consultazione e gestione
 * del catalogo menu e dei prodotti in vetrina (Single Responsibility Principle).
 */
public class GestioneMenuControl {

    private final GustoDAO gustoDAO;

    public GestioneMenuControl() {
        this(DAOFactory.getGustoDAO());
    }

    public GestioneMenuControl(GustoDAO gustoDAO) {
        this.gustoDAO = gustoDAO;
    }

    public List<GustoBean> visualizzaMenuGusti() {
        List<GustoBean> risultato = new ArrayList<>();
        for (Gusto g : gustoDAO.caricaTuttiGusti()) {
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
    }

    public void cambiaDisponibilitaGustoMenu(String nomeGusto) throws FrostlyException {
        if (nomeGusto == null || nomeGusto.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona prima un gusto dal menu.");
        }
        Gusto g = gustoDAO.trovaGustoPerNome(nomeGusto.trim());
        if (g == null) {
            throw new DatiNonValidiException("Gusto non trovato nel menu: " + nomeGusto);
        }
        g.setDisponibileOggi(!g.isDisponibileOggi());
        gustoDAO.salvaGustoMenu(g);
    }

    public void eliminaGustoMenu(String nomeGusto) throws FrostlyException {
        if (nomeGusto == null || nomeGusto.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona prima il gusto da rimuovere.");
        }
        if (gustoDAO.trovaGustoPerNome(nomeGusto.trim()) == null) {
            throw new DatiNonValidiException("Gusto non presente nel menu: " + nomeGusto);
        }
        gustoDAO.rimuoviGustoMenu(nomeGusto.trim());
    }
}
