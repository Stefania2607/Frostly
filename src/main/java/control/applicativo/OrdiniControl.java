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
                boolean eraDisdetto = oc.getStato() == StatoRichiesta.RIFIUTATA;
                if (eraDisdetto) {
                    // Se l'ordine era stato disdetto (e quindi ripristinato in magazzino), riscala le giacenze prima di riaccettarlo
                    scalaGiacenzeMagazzino(oc);
                }
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
                boolean giaDisdetto = oc.getStato() == StatoRichiesta.RIFIUTATA;
                oc.setStato(StatoRichiesta.RIFIUTATA);
                ordineDAO.salvaOrdine(oc);
                if (!giaDisdetto) {
                    ripristinaGiacenzeMagazzino(oc);
                }
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
        java.util.Collections.reverse(lista);
        return lista;
    }

    public List<OrdineBean> visualizzaTuttiOrdini() {
        List<OrdineBean> lista = new ArrayList<>();
        for (OrdineCliente oc : ordineDAO.caricaTuttiOrdini()) {
            lista.add(convertiOrdineInBean(oc));
        }
        java.util.Collections.reverse(lista);
        return lista;
    }

    private OrdineBean convertiOrdineInBean(OrdineCliente oc) {
        OrdineBean b = new OrdineBean();
        b.setCodiceOrdine(oc.getCodiceOrdine());
        String email = oc.getCliente() != null ? oc.getCliente().getEmail() : "";
        b.setEmailCliente(email);
        b.setNomeCliente(risolviNomeCliente(oc.getCliente(), email));
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

    private String risolviNomeCliente(Cliente cli, String email) {
        Cliente target = cli;
        if ((target == null || "Cliente".equalsIgnoreCase(target.getNome()) || target.getCognome() == null || target.getCognome().contains("@"))
                && email != null && !email.isBlank() && utenteDAO != null) {
            Cliente trovato = utenteDAO.trovaClientePerEmail(email);
            if (trovato != null) {
                target = trovato;
            }
        }
        if (target != null) {
            String n = target.getNome() != null ? target.getNome().trim() : "";
            String c = target.getCognome() != null ? target.getCognome().trim() : "";
            String completo = (n + " " + c).trim();
            if (!completo.isEmpty() && !completo.equalsIgnoreCase("Cliente") && !completo.contains("@")) {
                return completo;
            }
        }
        return (email != null && !email.isBlank()) ? email : "Cliente";
    }

    private String oraCorrente() {
        return "Oggi " + LocalTime.now().format(TIME_FMT);
    }

    private void scalaGiacenzeMagazzino(OrdineCliente ordine) throws FrostlyException {
        applicaMovimentoMagazzino(ordine, false);
    }

    private void ripristinaGiacenzeMagazzino(OrdineCliente ordine) throws FrostlyException {
        applicaMovimentoMagazzino(ordine, true);
    }

    private void applicaMovimentoMagazzino(OrdineCliente ordine, boolean ripristina) throws FrostlyException {
        if (ordine == null || ingredienteDAO == null) return;

        if (!ordine.getVaschette().isEmpty()) {
            for (VaschettaOrdinata vo : ordine.getVaschette()) {
                if (vo.getPesoKg() > 0.0) {
                    muoviGiacenzeGelato(vo.getGusti(), vo.getPesoKg(), vo.isConPanna(), vo.isConGranellaBronte(), ripristina);
                } else {
                    muoviGiacenzaArticoloMenu(vo.getGusti(), vo.getDescrizione(), 1, ripristina);
                }
            }
        } else if (ordine.getDescrizioneDettagliata() != null && !ordine.getDescrizioneDettagliata().isBlank()) {
            // Fallback per ordini ricaricati dal DB (senza lista vaschette in memoria): ricostruisce gli articoli dalla descrizione
            String[] segmenti = ordine.getDescrizioneDettagliata().split("\\|");
            for (String seg : segmenti) {
                String s = seg.trim();
                if (s.isEmpty()) continue;
                int qty = 1;
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)x\\s+(.+)$").matcher(s);
                if (m.matches()) {
                    qty = Integer.parseInt(m.group(1));
                    s = m.group(2).trim();
                }
                if (s.toLowerCase().startsWith("vaschetta")) {
                    double peso = estraiPesoDaDescrizioneVaschetta(s, ordine.getPesoKg());
                    List<Gusto> gusti = ordine.getGusto() != null ? List.of(ordine.getGusto()) : List.of();
                    boolean conPanna = s.toLowerCase().contains("panna");
                    boolean conGranella = s.toLowerCase().contains("granella");
                    for (int q = 0; q < qty; q++) {
                        muoviGiacenzeGelato(gusti, peso, conPanna, conGranella, ripristina);
                    }
                } else {
                    muoviGiacenzaArticoloMenu(null, s, qty, ripristina);
                }
            }
        } else if (ordine.getGusto() != null || ordine.getPesoKg() > 0.0) {
            List<Gusto> gusti = ordine.getGusto() != null ? List.of(ordine.getGusto()) : List.of();
            muoviGiacenzeGelato(gusti, ordine.getPesoKg(), false, false, ripristina);
        }
    }

    private double estraiPesoDaDescrizioneVaschetta(String desc, double pesoDefault) {
        String lower = desc.toLowerCase();
        if (lower.contains("1000g") || lower.contains("1 kg") || lower.contains("1kg")) return 1.0;
        if (lower.contains("750g")) return 0.75;
        if (lower.contains("500g")) return 0.50;
        if (lower.contains("350g")) return 0.35;
        return pesoDefault > 0.0 ? pesoDefault : 0.50;
    }

    private void muoviGiacenzaArticoloMenu(List<Gusto> gusti, String descrizione, int quantita, boolean ripristina) throws FrostlyException {
        String nomeProdotto = (gusti != null && !gusti.isEmpty()) ? gusti.get(0).getNome() : null;
        String categoria = (gusti != null && !gusti.isEmpty()) ? gusti.get(0).getCategoria() : "";
        if (nomeProdotto == null && descrizione != null) {
            int idx = descrizione.indexOf('(');
            nomeProdotto = (idx > 0) ? descrizione.substring(0, idx).trim() : descrizione.trim();
        }
        if (nomeProdotto == null || nomeProdotto.isEmpty() || quantita <= 0) return;

        for (java.util.Map.Entry<String, Double> entry : CorrelazioneMagazzino.calcolaConsumoArticolo(nomeProdotto, categoria).entrySet()) {
            muoviSePresente(entry.getKey(), entry.getValue() * quantita, ripristina);
        }

        if (!CorrelazioneMagazzino.isProdottoEspresso(nomeProdotto, categoria)) {
            Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nomeProdotto);
            if (ing == null) {
                ing = CorrelazioneMagazzino.trovaArticoloDiretto(ingredienteDAO.caricaTuttiIngredienti(), nomeProdotto);
            }
            if (ing != null) {
                if (ripristina) {
                    ing.rifornisci(1.0 * quantita);
                } else {
                    ing.consuma(1.0 * quantita);
                }
                ingredienteDAO.aggiornaIngrediente(ing);
            }
        }
    }

    private void muoviGiacenzeGelato(List<Gusto> gusti, double pesoKg, boolean conPanna, boolean conGranella, boolean ripristina)
            throws FrostlyException {
        for (java.util.Map.Entry<String, Double> entry : CorrelazioneMagazzino.calcolaConsumoVaschettaGelato(
                gusti, pesoKg, conPanna, conGranella).entrySet()) {
            muoviSePresente(entry.getKey(), entry.getValue(), ripristina);
        }
    }

    private void muoviSePresente(String nomeIngrediente, double quantita, boolean ripristina) throws FrostlyException {
        if (quantita <= 0.0 || ingredienteDAO == null) return;
        Ingrediente ing = ingredienteDAO.trovaIngredientePerNome(nomeIngrediente);
        if (ing != null) {
            if (ripristina) {
                ing.rifornisci(quantita);
            } else {
                ing.consuma(quantita);
            }
            ingredienteDAO.aggiornaIngrediente(ing);
        }
    }
}
