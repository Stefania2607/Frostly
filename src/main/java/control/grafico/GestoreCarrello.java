package control.grafico;

import bean.CarrelloBean;
import bean.GustoBean;
import bean.OrdineBean;
import bean.RegoleVaschettaBean;
import bean.VaschettaItemBean;
import control.applicativo.OrdiniControl;
import frostly.exception.DatiNonValidiException;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente di presentazione che gestisce le operazioni sul carrello
 * (aggiunta/modifica quantita', composizione vaschetta, calcolo totale e costruzione OrdineBean).
 */
public final class GestoreCarrello {

    private GestoreCarrello() {
    }

    public static void aggiungiArticolo(CarrelloBean carrello, VaschettaItemBean articolo) {
        if (carrello == null || articolo == null) return;
        List<VaschettaItemBean> articoli = carrello.getArticoli();
        if (articolo.ottieniPesoKg() <= 0.0) {
            for (VaschettaItemBean esistente : articoli) {
                if (esistente.ottieniPesoKg() <= 0.0
                        && esistente.ottieniDescrizione().equalsIgnoreCase(articolo.ottieniDescrizione())
                        && Math.abs(esistente.getPrezzoUnitario() - articolo.getPrezzoUnitario()) < 0.001) {
                    esistente.incrementaQuantita();
                    return;
                }
            }
        }
        articoli.add(articolo);
    }

    public static void incrementaArticolo(CarrelloBean carrello, int indice) {
        if (carrello == null) return;
        List<VaschettaItemBean> articoli = carrello.getArticoli();
        if (indice >= 0 && indice < articoli.size()) {
            articoli.get(indice).incrementaQuantita();
        }
    }

    public static void decrementaArticolo(CarrelloBean carrello, int indice) {
        if (carrello == null) return;
        List<VaschettaItemBean> articoli = carrello.getArticoli();
        if (indice >= 0 && indice < articoli.size()) {
            VaschettaItemBean item = articoli.get(indice);
            item.decrementaQuantita();
            if (item.getQuantita() <= 0) {
                articoli.remove(indice);
            }
        }
    }

    public static void rimuoviArticolo(CarrelloBean carrello, int indice) {
        if (carrello == null) return;
        List<VaschettaItemBean> articoli = carrello.getArticoli();
        if (indice >= 0 && indice < articoli.size()) {
            articoli.remove(indice);
        }
    }

    public static void svuota(CarrelloBean carrello) {
        if (carrello != null) {
            carrello.getArticoli().clear();
        }
    }

    public static boolean isVuoto(CarrelloBean carrello) {
        return carrello == null || carrello.getArticoli().isEmpty();
    }

    public static int getNumeroArticoli(CarrelloBean carrello) {
        if (carrello == null) return 0;
        int n = 0;
        for (VaschettaItemBean item : carrello.getArticoli()) {
            n += item.getQuantita();
        }
        return n;
    }

    public static double calcolaTotale(CarrelloBean carrello) {
        if (carrello == null) return 0.0;
        double somma = 0.0;
        for (VaschettaItemBean item : carrello.getArticoli()) {
            somma += item.ottieniPrezzo();
        }
        return Math.round(somma * 100.0) / 100.0;
    }

    public static void impostaFormatoVaschetta(CarrelloBean carrello, double pesoKg, int maxGusti) {
        if (carrello == null) return;
        carrello.setPesoVaschettaKg(pesoKg);
        carrello.setMaxGustiVaschetta(maxGusti);
        List<GustoBean> gusti = carrello.getGustiNellaVaschetta();
        while (gusti.size() > maxGusti) {
            gusti.remove(gusti.size() - 1);
        }
    }

    public static void svuotaVaschettaInComposizione(CarrelloBean carrello) {
        if (carrello != null) {
            carrello.getGustiNellaVaschetta().clear();
        }
    }

    public static double calcolaPrezzoVaschettaInComposizione(CarrelloBean carrello, boolean conPanna, boolean conGranella) {
        if (carrello == null) return 0.0;
        List<GustoBean> gusti = carrello.getGustiNellaVaschetta();
        double pesoKg = carrello.getPesoVaschettaKg();
        RegoleVaschettaBean regole = OrdiniControl.ottieniRegoleVaschetta();
        if (gusti.isEmpty()) {
            if (!conPanna) {
                return 0.0;
            }
            double p = Math.round(regole.getPrezzoSolaPannaAlKg() * pesoKg * 100.0) / 100.0;
            return conGranella ? p + regole.getSupplementoGranella() : p;
        }
        double sommaPrezziAlKg = 0.0;
        for (GustoBean g : gusti) {
            sommaPrezziAlKg += g.getPrezzoAlKg();
        }
        double mediaKg = sommaPrezziAlKg / gusti.size();
        double prezzo = Math.round(mediaKg * pesoKg * 100.0) / 100.0;
        if (conPanna) prezzo += regole.getSupplementoPanna();
        if (conGranella) prezzo += regole.getSupplementoGranella();
        return prezzo;
    }

    public static VaschettaItemBean aggiungiVaschettaInComposizioneAlCarrello(
            CarrelloBean carrello, boolean conPanna, boolean conGranella) throws DatiNonValidiException {
        List<GustoBean> gusti = carrello.getGustiNellaVaschetta();
        if (gusti.isEmpty() && !conPanna) {
            throw new DatiNonValidiException("Seleziona almeno un gusto dalla carta oppure la panna fresca d'alpeggio!");
        }
        List<String> nomi = new ArrayList<>();
        double prezzo = calcolaPrezzoVaschettaInComposizione(carrello, conPanna, conGranella);
        double pesoKg = carrello.getPesoVaschettaKg();
        int grammi = (int) (pesoKg * 1000);
        String desc;

        if (gusti.isEmpty()) {
            desc = "Vaschetta Panna Montata Fresca d'Alpeggio (" + grammi + "g)"
                    + (conGranella ? " + Granella di Bronte DOP" : "");
        } else {
            StringBuilder sbGusti = new StringBuilder();
            for (int i = 0; i < gusti.size(); i++) {
                GustoBean g = gusti.get(i);
                nomi.add(g.getNome());
                if (i > 0) sbGusti.append(", ");
                sbGusti.append(g.getNome());
            }
            StringBuilder sb = new StringBuilder("Vaschetta " + grammi + "g (" + sbGusti + ")");
            if (conPanna) sb.append(" + Panna");
            if (conGranella) sb.append(" + Granella");
            desc = sb.toString();
        }

        VaschettaItemBean item = new VaschettaItemBean(pesoKg, nomi, conPanna, conGranella, desc, prezzo);
        carrello.getArticoli().add(item);
        gusti.clear();
        return item;
    }

    public static OrdineBean costruisciOrdineBean(CarrelloBean carrello, String emailCliente) throws DatiNonValidiException {
        if (isVuoto(carrello)) {
            throw new DatiNonValidiException("Il carrello è vuoto! Aggiungi almeno un prodotto dal menu.");
        }
        String modalita = carrello.getModalitaConsegna();
        String fascia = carrello.getFasciaOraria();
        String noteRaw = carrello.getIndirizzoONote();

        String tipo = (modalita != null && !modalita.isEmpty()) ? modalita : "🛵 Consegna a Domicilio";
        String orario = (fascia != null && !fascia.isEmpty()) ? fascia : "Subito (~25-35 min)";
        String note = (noteRaw != null) ? noteRaw.trim() : "";

        String riepilogoConsegna = tipo + " (" + orario + ")"
                + (note.isEmpty() ? "" : " • Indirizzo/Note: " + note);

        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setEmailCliente(emailCliente);
        ordineBean.setDataRitiro(riepilogoConsegna);
        for (VaschettaItemBean item : carrello.getArticoli()) {
            for (int q = 0; q < item.getQuantita(); q++) {
                ordineBean.aggiungiVaschetta(item.creaCopiaSingola());
            }
        }
        return ordineBean;
    }
}
