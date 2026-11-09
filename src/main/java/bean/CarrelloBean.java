package bean;

import frostly.exception.DatiNonValidiException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean che modella il Carrello della spesa del Cliente e lo stato della Vaschetta
 * attualmente in fase di composizione durante la sessione d'ordine.
 * Incapsula la lista degli articoli selezionati dal menu (Vaschette, Crepes con Topping/Granella,
 * Panna d'asporto, Bevande, Caffetteria, Dolci), il calcolo dinamico dei prezzi e la
 * costruzione dell'OrdineBean al momento del checkout.
 */
public class CarrelloBean {

    private final List<VaschettaItemBean> articoli = new ArrayList<>();
    private final List<GustoBean> gustiNellaVaschetta = new ArrayList<>();
    private double pesoVaschettaKg = 0.50;
    private int maxGustiVaschetta = 4;

    private String modalitaConsegna = "🛵 Consegna a Domicilio";
    private String fasciaOraria = "Subito (~25-35 min)";
    private String indirizzoONote = "";

    public void aggiungiArticolo(VaschettaItemBean articolo) {
        if (articolo != null) {
            articoli.add(articolo);
        }
    }

    public void rimuoviArticolo(int indice) {
        if (indice >= 0 && indice < articoli.size()) {
            articoli.remove(indice);
        }
    }

    public void svuota() {
        articoli.clear();
    }

    public boolean isVuoto() {
        return articoli.isEmpty();
    }

    public int getNumeroArticoli() {
        return articoli.size();
    }

    public List<VaschettaItemBean> getArticoli() {
        return Collections.unmodifiableList(articoli);
    }

    public double calcolaTotale() {
        double somma = 0.0;
        for (VaschettaItemBean item : articoli) {
            somma += item.ottieniPrezzo();
        }
        return Math.round(somma * 100.0) / 100.0;
    }

    // ================= COMPOSIZIONE VASCHETTA CORRENTE =================

    public List<GustoBean> getGustiNellaVaschetta() {
        return gustiNellaVaschetta;
    }

    public double getPesoVaschettaKg() {
        return pesoVaschettaKg;
    }

    public int getMaxGustiVaschetta() {
        return maxGustiVaschetta;
    }

    public void impostaFormatoVaschetta(double pesoKg, int maxGusti) {
        this.pesoVaschettaKg = pesoKg;
        this.maxGustiVaschetta = maxGusti;
        while (gustiNellaVaschetta.size() > maxGustiVaschetta) {
            gustiNellaVaschetta.remove(gustiNellaVaschetta.size() - 1);
        }
    }

    public void svuotaVaschettaInComposizione() {
        gustiNellaVaschetta.clear();
    }

    public double calcolaPrezzoVaschettaInComposizione(boolean conPanna, boolean conGranella) {
        if (gustiNellaVaschetta.isEmpty()) {
            if (!conPanna) {
                return 0.0;
            }
            double p = Math.round(12.0 * pesoVaschettaKg * 100.0) / 100.0;
            return conGranella ? p + 3.50 : p;
        }
        double sommaPrezziAlKg = 0.0;
        for (GustoBean g : gustiNellaVaschetta) {
            sommaPrezziAlKg += g.getPrezzoAlKg();
        }
        double mediaKg = sommaPrezziAlKg / gustiNellaVaschetta.size();
        double prezzo = Math.round(mediaKg * pesoVaschettaKg * 100.0) / 100.0;
        if (conPanna) prezzo += 2.50;
        if (conGranella) prezzo += 3.50;
        return prezzo;
    }

    public VaschettaItemBean aggiungiVaschettaInComposizioneAlCarrello(boolean conPanna, boolean conGranella)
            throws DatiNonValidiException {
        if (gustiNellaVaschetta.isEmpty() && !conPanna) {
            throw new DatiNonValidiException("Seleziona almeno un gusto dalla carta oppure la panna fresca d'alpeggio!");
        }
        List<String> nomi = new ArrayList<>();
        double prezzo = calcolaPrezzoVaschettaInComposizione(conPanna, conGranella);
        int grammi = (int) (pesoVaschettaKg * 1000);
        String desc;

        if (gustiNellaVaschetta.isEmpty()) {
            desc = "Vaschetta Panna Montata Fresca d'Alpeggio (" + grammi + "g)"
                    + (conGranella ? " + Granella di Bronte DOP" : "");
        } else {
            StringBuilder sbGusti = new StringBuilder();
            for (int i = 0; i < gustiNellaVaschetta.size(); i++) {
                GustoBean g = gustiNellaVaschetta.get(i);
                nomi.add(g.getNome());
                if (i > 0) sbGusti.append(", ");
                sbGusti.append(g.getNome());
            }
            StringBuilder sb = new StringBuilder("Vaschetta " + grammi + "g (" + sbGusti + ")");
            if (conPanna) sb.append(" + Panna");
            if (conGranella) sb.append(" + Granella");
            desc = sb.toString();
        }

        VaschettaItemBean item = new VaschettaItemBean(pesoVaschettaKg, nomi, conPanna, conGranella, desc, prezzo);
        articoli.add(item);
        gustiNellaVaschetta.clear();
        return item;
    }

    // ================= DATI CONSEGNA E CHECKOUT =================

    public String getModalitaConsegna() {
        return modalitaConsegna;
    }

    public void setModalitaConsegna(String modalitaConsegna) {
        this.modalitaConsegna = modalitaConsegna;
    }

    public String getFasciaOraria() {
        return fasciaOraria;
    }

    public void setFasciaOraria(String fasciaOraria) {
        this.fasciaOraria = fasciaOraria;
    }

    public String getIndirizzoONote() {
        return indirizzoONote;
    }

    public void setIndirizzoONote(String indirizzoONote) {
        this.indirizzoONote = indirizzoONote;
    }

    /**
     * Costruisce l'OrdineBean completo a partire dal contenuto del carrello,
     * pronto per essere inviato al Controller Applicativo (ProduzioneOrdiniControl).
     */
    public OrdineBean costruisciOrdineBean(String emailCliente) throws DatiNonValidiException {
        if (articoli.isEmpty()) {
            throw new DatiNonValidiException("Il carrello è vuoto! Aggiungi almeno un prodotto dal menu.");
        }
        String tipo = (modalitaConsegna != null && !modalitaConsegna.isEmpty())
                ? modalitaConsegna : "🛵 Consegna a Domicilio";
        String orario = (fasciaOraria != null && !fasciaOraria.isEmpty())
                ? fasciaOraria : "Subito (~25-35 min)";
        String note = (indirizzoONote != null) ? indirizzoONote.trim() : "";

        String riepilogoConsegna = tipo + " (" + orario + ")"
                + (note.isEmpty() ? "" : " • Indirizzo/Note: " + note);

        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setEmailCliente(emailCliente);
        ordineBean.setDataRitiro(riepilogoConsegna);
        for (VaschettaItemBean item : articoli) {
            ordineBean.aggiungiVaschetta(item);
        }
        return ordineBean;
    }
}
