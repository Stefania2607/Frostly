package control.applicativo;

import bean.GustoBean;
import bean.IngredienteBean;
import bean.OrdineBean;
import bean.VaschettaItemBean;
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
import model.StatoRichiesta;
import model.VaschettaBase;
import model.VaschettaComponent;
import model.VaschettaOrdinata;

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

    private List<Gusto> validaERisolviGusti(FrostlyDAO dao, List<String> nomi) throws FrostlyException {
        List<Gusto> lista = new ArrayList<>();
        if (nomi == null || nomi.isEmpty()) {
            return lista;
        }
        for (String ng : nomi) {
            String nomePulito = ng.trim();
            if (nomePulito.isEmpty()) continue;
            Gusto g = dao.trovaGustoPerNome(nomePulito);
            if (g == null) {
                throw new DatiNonValidiException("Gusto '" + nomePulito + "' non trovato nel menu.");
            }
            if (!g.isDisponibileOggi()) {
                throw new DatiNonValidiException("Il gusto '" + g.getNome() + "' e' attualmente esaurito.");
            }
            lista.add(g);
        }
        return lista;
    }

    public OrdineBean effettuaPreOrdine(OrdineBean beanInput) throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();

        Cliente cliente = dao.trovaClientePerEmail(beanInput.getEmailCliente());
        if (cliente == null) {
            throw new DatiNonValidiException("Cliente selezionato non valido.");
        }

        int maxNum = 0;
        for (OrdineCliente oc : dao.caricaTuttiOrdini()) {
            String cod = oc.getCodiceOrdine();
            if (cod != null && cod.startsWith("ORD-")) {
                try {
                    int n = Integer.parseInt(cod.substring(4).trim());
                    if (n > maxNum) maxNum = n;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        String codiceOrdine = String.format("ORD-%02d", Math.max(maxNum + 1, dao.caricaTuttiOrdini().size() + 1));
        OrdineCliente nuovoOrdine;

        if (!beanInput.getVaschette().isEmpty()) {
            List<VaschettaOrdinata> listaVo = new ArrayList<>();
            for (VaschettaItemBean vib : beanInput.getVaschette()) {
                List<Gusto> gustiVaschetta = validaERisolviGusti(dao, vib.ottieniGusti());
                if (gustiVaschetta.isEmpty() && !vib.haPanna() && (vib.ottieniDescrizione() == null || vib.ottieniDescrizione().isEmpty())) {
                    throw new DatiNonValidiException("Ciascun articolo deve contenere almeno un gusto, la panna o un prodotto del menu.");
                }
                VaschettaOrdinata vo = new VaschettaOrdinata(gustiVaschetta, vib.ottieniPesoKg(),
                        vib.haPanna(), vib.haGranellaBronte(), vib.ottieniDescrizione(), vib.ottieniPrezzo());
                listaVo.add(vo);
            }
            nuovoOrdine = new OrdineCliente(codiceOrdine, cliente, listaVo, beanInput.getDataRitiro());
            beanInput.setDescrizioneCompleta(nuovoOrdine.getDescrizioneDettagliata());
            beanInput.setPrezzoTotale(nuovoOrdine.getPrezzoTotale());
        } else {
            String testoGusti = beanInput.getNomeGusto();
            List<Gusto> listaGusti = new ArrayList<>();
            if (testoGusti != null && !testoGusti.trim().isEmpty()) {
                List<String> nomiSingoli = List.of(testoGusti.split(","));
                listaGusti = validaERisolviGusti(dao, nomiSingoli);
            }
            if (listaGusti.isEmpty() && !beanInput.isConPanna()) {
                throw new DatiNonValidiException("Selezionare almeno un gusto o la panna montata fresca.");
            }

            VaschettaComponent decorata = new VaschettaBase(listaGusti, beanInput.getPesoKg());
            if (beanInput.isConPanna() && !listaGusti.isEmpty()) {
                decorata = new ConPannaMontataDecorator(decorata);
            }
            if (beanInput.isConGranellaBronte()) {
                decorata = new ConGranellaBronteDecorator(decorata);
            }

            Gusto principale = listaGusti.isEmpty() ? null : listaGusti.get(0);
            nuovoOrdine = new OrdineCliente(codiceOrdine, cliente, principale,
                    beanInput.getPesoKg(), beanInput.getDataRitiro());
            nuovoOrdine.setPrezzoTotale(decorata.calcolaPrezzoFinale());
            nuovoOrdine.setDescrizioneDettagliata(decorata.getDescrizioneCompleta());

            beanInput.setDescrizioneCompleta(decorata.getDescrizioneCompleta());
            beanInput.setPrezzoTotale(decorata.calcolaPrezzoFinale());
        }

        dao.salvaOrdine(nuovoOrdine);
        beanInput.setCodiceOrdine(codiceOrdine);
        beanInput.setStato(nuovoOrdine.getStato().name());

        String testoNotifica = cliente.getNome() + " " + cliente.getCognome() + " ha inviato un ordine: "
                + beanInput.getDescrizioneCompleta() + " [" + beanInput.getDataRitiro() + "] - Totale: EUR "
                + beanInput.getPrezzoTotale() + ". Accedi alla sezione Ordini per confermare o disdire.";

        dao.inviaNotifica(new Notifica(
                dao.getMaestroGelataio().getEmail(),
                "🛵 Nuovo Ordine " + codiceOrdine + " da Confermare",
                testoNotifica,
                oraCorrente()));

        for (model.Dipendente d : dao.caricaTuttiDipendenti()) {
            dao.inviaNotifica(new Notifica(
                    d.getEmail(),
                    "🛵 Nuovo Ordine " + codiceOrdine + " da Confermare",
                    testoNotifica,
                    oraCorrente()));
        }

        return beanInput;
    }

    public void confermaOrdine(String codiceOrdine, String operatore) throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        for (OrdineCliente oc : dao.caricaTuttiOrdini()) {
            if (oc.getCodiceOrdine().equalsIgnoreCase(codiceOrdine)) {
                oc.setStato(StatoRichiesta.APPROVATA);
                dao.salvaOrdine(oc);
                if (oc.getCliente() != null) {
                    dao.inviaNotifica(new Notifica(
                            oc.getCliente().getEmail(),
                            "🛵 Ordine " + codiceOrdine + " CONFERMATO!",
                            "La gelateria ha confermato il tuo ordine (operatore: " + operatore + "). Preparazione in corso per " + oc.getDataRitiro() + "!",
                            oraCorrente()));
                }
                return;
            }
        }
        throw new DatiNonValidiException("Ordine " + codiceOrdine + " non trovato.");
    }

    public void disdiciOrdine(String codiceOrdine, String operatore) throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        for (OrdineCliente oc : dao.caricaTuttiOrdini()) {
            if (oc.getCodiceOrdine().equalsIgnoreCase(codiceOrdine)) {
                oc.setStato(StatoRichiesta.RIFIUTATA);
                dao.salvaOrdine(oc);
                if (oc.getCliente() != null) {
                    dao.inviaNotifica(new Notifica(
                            oc.getCliente().getEmail(),
                            "❌ Ordine " + codiceOrdine + " DISDETTO",
                            "La gelateria non puo' evadere il tuo ordine per " + oc.getDataRitiro() + " (operatore: " + operatore + "). Ci scusiamo per il disagio.",
                            oraCorrente()));
                }
                return;
            }
        }
        throw new DatiNonValidiException("Ordine " + codiceOrdine + " non trovato.");
    }

    public List<OrdineBean> visualizzaOrdiniCliente(String emailCliente) {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<OrdineBean> lista = new ArrayList<>();
        for (OrdineCliente oc : dao.caricaTuttiOrdini()) {
            if (oc.getCliente() != null && oc.getCliente().getEmail().equalsIgnoreCase(emailCliente)) {
                lista.add(convertiOrdineInBean(oc));
            }
        }
        return lista;
    }

    public List<OrdineBean> visualizzaTuttiOrdini() {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<OrdineBean> lista = new ArrayList<>();
        for (OrdineCliente oc : dao.caricaTuttiOrdini()) {
            lista.add(convertiOrdineInBean(oc));
        }
        return lista;
    }

    private OrdineBean convertiOrdineInBean(OrdineCliente oc) {
        OrdineBean b = new OrdineBean();
        b.setCodiceOrdine(oc.getCodiceOrdine());
        b.setEmailCliente(oc.getCliente() != null ? oc.getCliente().getEmail() : "");
        b.setStato(oc.getStato() != null ? oc.getStato().name() : "APPROVATA");
        b.setPrezzoTotale(oc.getPrezzoTotale());
        b.setDescrizioneCompleta(oc.getDescrizioneDettagliata());
        try {
            b.setDataRitiro(oc.getDataRitiro());
            b.setPesoKg(oc.getPesoKg());
        } catch (DatiNonValidiException ignored) {
            // Dati gia' validati dall'entity
        }
        for (VaschettaOrdinata vo : oc.getVaschette()) {
            List<String> nomi = new ArrayList<>();
            for (Gusto g : vo.getGusti()) {
                nomi.add(g.getNome());
            }
            b.aggiungiVaschetta(new VaschettaItemBean(vo.getPesoKg(), nomi, vo.isConPanna(),
                    vo.isConGranellaBronte(), vo.getDescrizione(), vo.getPrezzo()));
        }
        return b;
    }

    private String oraCorrente() {
        return "Oggi " + LocalTime.now().format(TIME_FMT);
    }
}
