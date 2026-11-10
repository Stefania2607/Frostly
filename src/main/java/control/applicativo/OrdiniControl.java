package control.applicativo;

import bean.OrdineBean;
import bean.VaschettaItemBean;
import dao.DAOFactory;
import dao.GustoDAO;
import dao.IngredienteDAO;
import dao.NotificaDAO;
import dao.OrdineDAO;
import dao.UtenteDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.ConGranellaBronteDecorator;
import model.ConPannaMontataDecorator;
import model.Dipendente;
import model.Gusto;
import model.Ingrediente;
import model.Notifica;
import model.OrdineCliente;
import model.StatoRichiesta;
import model.VaschettaBase;
import model.VaschettaComponent;
import model.VaschettaOrdinata;

/**
 * Controller applicativo dedicato alla gestione degli ordini dei clienti (pre-ordine vaschette,
 * calcolo decoratori GoF, conferma e disdetta ordini da parte dello staff).
 */
public class OrdiniControl {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final OrdineDAO ordineDAO;
    private final GustoDAO gustoDAO;
    private final UtenteDAO utenteDAO;
    private final NotificaDAO notificaDAO;
    private final IngredienteDAO ingredienteDAO;

    public OrdiniControl() {
        this(DAOFactory.getOrdineDAO(), DAOFactory.getGustoDAO(), DAOFactory.getUtenteDAO(),
                DAOFactory.getNotificaDAO(), DAOFactory.getIngredienteDAO());
    }

    public OrdiniControl(OrdineDAO ordineDAO, GustoDAO gustoDAO, UtenteDAO utenteDAO, NotificaDAO notificaDAO) {
        this(ordineDAO, gustoDAO, utenteDAO, notificaDAO, DAOFactory.getIngredienteDAO());
    }

    public OrdiniControl(OrdineDAO ordineDAO, GustoDAO gustoDAO, UtenteDAO utenteDAO,
                         NotificaDAO notificaDAO, IngredienteDAO ingredienteDAO) {
        this.ordineDAO = ordineDAO;
        this.gustoDAO = gustoDAO;
        this.utenteDAO = utenteDAO;
        this.notificaDAO = notificaDAO;
        this.ingredienteDAO = ingredienteDAO;
    }

    private List<Gusto> validaERisolviGusti(List<String> nomi) throws FrostlyException {
        List<Gusto> lista = new ArrayList<>();
        if (nomi == null || nomi.isEmpty()) {
            return lista;
        }
        for (String ng : nomi) {
            String nomePulito = ng.trim();
            if (nomePulito.isEmpty()) continue;
            Gusto g = gustoDAO.trovaGustoPerNome(nomePulito);
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
        Cliente cliente = utenteDAO.trovaClientePerEmail(beanInput.getEmailCliente());
        if (cliente == null) {
            throw new DatiNonValidiException("Cliente selezionato non valido.");
        }

        int maxNum = 0;
        List<OrdineCliente> tuttiOrdini = ordineDAO.caricaTuttiOrdini();
        for (OrdineCliente oc : tuttiOrdini) {
            String cod = oc.getCodiceOrdine();
            if (cod != null && cod.startsWith("ORD-")) {
                try {
                    int n = Integer.parseInt(cod.substring(4).trim());
                    if (n > maxNum) maxNum = n;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        String codiceOrdine = String.format("ORD-%02d", Math.max(maxNum + 1, tuttiOrdini.size() + 1));
        OrdineCliente nuovoOrdine;

        if (!beanInput.getVaschette().isEmpty()) {
            List<VaschettaOrdinata> listaVo = new ArrayList<>();
            for (VaschettaItemBean vib : beanInput.getVaschette()) {
                List<Gusto> gustiVaschetta = validaERisolviGusti(vib.ottieniGusti());
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
                listaGusti = validaERisolviGusti(nomiSingoli);
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

        scalaGiacenzeMagazzino(nuovoOrdine);

        ordineDAO.salvaOrdine(nuovoOrdine);
        beanInput.setCodiceOrdine(codiceOrdine);
        beanInput.setStato(nuovoOrdine.getStato().name());

        String testoNotifica = cliente.getNome() + " " + cliente.getCognome() + " ha inviato un ordine: "
                + beanInput.getDescrizioneCompleta() + " [" + beanInput.getDataRitiro() + "] - Totale: EUR "
                + beanInput.getPrezzoTotale() + ". Accedi alla sezione Ordini per confermare o disdire.";

        notificaDAO.inviaNotifica(new Notifica(
                utenteDAO.getMaestroGelataio().getEmail(),
                "🛵 Nuovo Ordine " + codiceOrdine + " da Confermare",
                testoNotifica,
                oraCorrente()));

        for (Dipendente d : utenteDAO.caricaTuttiDipendenti()) {
            notificaDAO.inviaNotifica(new Notifica(
                    d.getEmail(),
                    "🛵 Nuovo Ordine " + codiceOrdine + " da Confermare",
                    testoNotifica,
                    oraCorrente()));
        }

        return beanInput;
    }

    public void confermaOrdine(String codiceOrdine, String operatore) throws FrostlyException {
        for (OrdineCliente oc : ordineDAO.caricaTuttiOrdini()) {
            if (oc.getCodiceOrdine().equalsIgnoreCase(codiceOrdine)) {
                oc.setStato(StatoRichiesta.APPROVATA);
                ordineDAO.salvaOrdine(oc);
                if (oc.getCliente() != null) {
                    notificaDAO.inviaNotifica(new Notifica(
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
        for (OrdineCliente oc : ordineDAO.caricaTuttiOrdini()) {
            if (oc.getCodiceOrdine().equalsIgnoreCase(codiceOrdine)) {
                oc.setStato(StatoRichiesta.RIFIUTATA);
                ordineDAO.salvaOrdine(oc);
                if (oc.getCliente() != null) {
                    notificaDAO.inviaNotifica(new Notifica(
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
        List<OrdineBean> lista = new ArrayList<>();
        for (OrdineCliente oc : ordineDAO.caricaTuttiOrdini()) {
            if (oc.getCliente() != null && oc.getCliente().getEmail().equalsIgnoreCase(emailCliente)) {
                lista.add(convertiOrdineInBean(oc));
            }
        }
        return lista;
    }

    public List<OrdineBean> visualizzaTuttiOrdini() {
        List<OrdineBean> lista = new ArrayList<>();
        for (OrdineCliente oc : ordineDAO.caricaTuttiOrdini()) {
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

    private void scalaGiacenzeMagazzino(OrdineCliente ordine) throws FrostlyException {
        if (ordine == null || ingredienteDAO == null) return;

        if (!ordine.getVaschette().isEmpty()) {
            for (VaschettaOrdinata vo : ordine.getVaschette()) {
                if (vo.getPesoKg() > 0.0) {
                    scalaGiacenzeGelato(vo.getGusti(), vo.getPesoKg(), vo.isConPanna(), vo.isConGranellaBronte());
                } else {
                    scalaGiacenzaArticoloMenu(vo.getGusti(), vo.getDescrizione());
                }
            }
        } else if (ordine.getGusto() != null || ordine.getPesoKg() > 0.0) {
            List<Gusto> gusti = ordine.getGusto() != null ? List.of(ordine.getGusto()) : List.of();
            scalaGiacenzeGelato(gusti, ordine.getPesoKg(), false, false);
        }
    }

    private void scalaGiacenzaArticoloMenu(List<Gusto> gusti, String descrizione) throws FrostlyException {
        String nomeProdotto = (gusti != null && !gusti.isEmpty()) ? gusti.get(0).getNome() : null;
        if (nomeProdotto == null && descrizione != null) {
            int idx = descrizione.indexOf('(');
            nomeProdotto = (idx > 0) ? descrizione.substring(0, idx).trim() : descrizione.trim();
        }
        if (nomeProdotto == null || nomeProdotto.isEmpty()) return;

        Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nomeProdotto);
        if (ing == null) {
            for (Ingrediente i : ingredienteDAO.caricaTuttiIngredienti()) {
                if (nomeProdotto.equalsIgnoreCase(i.getNome())
                        || nomeProdotto.toLowerCase().startsWith(i.getNome().toLowerCase())
                        || i.getNome().toLowerCase().startsWith(nomeProdotto.toLowerCase())) {
                    ing = i;
                    break;
                }
            }
        }
        if (ing != null) {
            ing.consuma(1.0);
            ingredienteDAO.aggiornaIngrediente(ing);
        }

        String lower = nomeProdotto.toLowerCase();
        if (lower.startsWith("crepe")) {
            consumaSePresente("Farina per Crepes e Dolci", 0.10);
            consumaSePresente("Latte Fresco Alta Qualita", 0.10);
            consumaSePresente("Uova Fresche da Allevamento", 1.0);
            if (lower.contains("nutella")) {
                consumaSePresente("Nutella Ferrero", 0.05);
            }
        } else if (lower.startsWith("cioccolata calda")) {
            consumaSePresente("Latte Fresco Alta Qualita", 0.15);
        }
    }

    private void scalaGiacenzeGelato(List<Gusto> gusti, double pesoKg, boolean conPanna, boolean conGranella)
            throws FrostlyException {
        if (pesoKg <= 0.0) return;

        consumaSePresente("Latte Fresco Alta Qualita", Math.round(pesoKg * 0.40 * 100.0) / 100.0);
        consumaSePresente("Panna Fresca d'Alpeggio", Math.round(pesoKg * 0.20 * 100.0) / 100.0);
        consumaSePresente("Zucchero Semolato Italiano", Math.round(pesoKg * 0.15 * 100.0) / 100.0);

        if (gusti != null && !gusti.isEmpty()) {
            double quotaPerGusto = pesoKg / gusti.size();
            for (Gusto g : gusti) {
                String nome = g.getNome().toLowerCase();
                if (nome.contains("pistacchio")) {
                    consumaSePresente("Pasta Pistacchio di Bronte DOP", Math.round(quotaPerGusto * 0.25 * 100.0) / 100.0);
                } else if (nome.contains("nocciola")) {
                    consumaSePresente("Nocciola Piemonte IGP", Math.round(quotaPerGusto * 0.25 * 100.0) / 100.0);
                } else if (nome.contains("fondente") || nome.contains("cioccolat")) {
                    consumaSePresente("Cioccolato Fondente Ecuador 75%", Math.round(quotaPerGusto * 0.25 * 100.0) / 100.0);
                } else if (nome.contains("mango")) {
                    consumaSePresente("Polpa di Mango Alphonso", Math.round(quotaPerGusto * 0.35 * 100.0) / 100.0);
                } else if (nome.contains("fragol")) {
                    consumaSePresente("Fragole Fresche di Bosco", Math.round(quotaPerGusto * 0.35 * 100.0) / 100.0);
                } else if (nome.contains("limon")) {
                    consumaSePresente("Limoni di Sorrento IGP", Math.round(quotaPerGusto * 0.30 * 100.0) / 100.0);
                } else if (nome.contains("caff")) {
                    consumaSePresente("Caffe in Grani 100% Arabica", Math.round(quotaPerGusto * 0.15 * 100.0) / 100.0);
                } else if (nome.contains("mandorl") || nome.contains("amarandola")) {
                    consumaSePresente("Mandorle di Avola", Math.round(quotaPerGusto * 0.20 * 100.0) / 100.0);
                } else if (nome.contains("nutella")) {
                    consumaSePresente("Nutella Ferrero", Math.round(quotaPerGusto * 0.20 * 100.0) / 100.0);
                } else if (nome.contains("cacao")) {
                    consumaSePresente("Cacao Amaro Monorigine", Math.round(quotaPerGusto * 0.20 * 100.0) / 100.0);
                }
            }
        }

        if (conPanna) {
            consumaSePresente("Panna Fresca d'Alpeggio", 0.10);
        }
        if (conGranella) {
            consumaSePresente("Granella di Pistacchio di Bronte DOP", 0.05);
        }
    }

    private void consumaSePresente(String nomeIngrediente, double quantita) throws FrostlyException {
        if (quantita <= 0.0 || ingredienteDAO == null) return;
        Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nomeIngrediente);
        if (ing != null) {
            ing.consuma(quantita);
            ingredienteDAO.aggiornaIngrediente(ing);
        }
    }
}
