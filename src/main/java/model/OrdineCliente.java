package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity che modella il Pre-ordine effettuato da un Cliente,
 * supportando una o molteplici vaschette artigianali con calcolo del totale.
 */
public class OrdineCliente {

    private String codiceOrdine;
    private Cliente cliente;
    private Gusto gusto; // Gusto principale per retrocompatibilita'
    private double pesoKg;
    private String dataRitiro;
    private StatoRichiesta stato;
    private double prezzoTotale;
    private final List<VaschettaOrdinata> vaschette = new ArrayList<>();
    private String descrizioneDettagliata;

    public OrdineCliente(String codiceOrdine, Cliente cliente, Gusto gusto, double pesoKg, String dataRitiro) {
        this.codiceOrdine = codiceOrdine;
        this.cliente = cliente;
        this.gusto = gusto;
        this.pesoKg = pesoKg;
        this.dataRitiro = dataRitiro;
        this.stato = StatoRichiesta.IN_ATTESA;
        this.prezzoTotale = calcolaPrezzo();
    }

    public OrdineCliente(String codiceOrdine, Cliente cliente, List<VaschettaOrdinata> listaVaschette, String dataRitiro) {
        this.codiceOrdine = codiceOrdine;
        this.cliente = cliente;
        this.dataRitiro = dataRitiro;
        this.stato = StatoRichiesta.IN_ATTESA;
        this.vaschette.addAll(listaVaschette);

        double sommaPrezzi = 0.0;
        double sommaPeso = 0.0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listaVaschette.size(); i++) {
            VaschettaOrdinata v = listaVaschette.get(i);
            sommaPrezzi += v.getPrezzo();
            sommaPeso += v.getPesoKg();
            if (i > 0) sb.append(" | ");
            sb.append("Vaschetta #").append(i + 1).append(": ").append(v.getDescrizione());
        }
        this.prezzoTotale = Math.round(sommaPrezzi * 100.0) / 100.0;
        this.pesoKg = sommaPeso;
        this.descrizioneDettagliata = sb.toString();
        if (!listaVaschette.isEmpty() && !listaVaschette.get(0).getGusti().isEmpty()) {
            this.gusto = listaVaschette.get(0).getGusti().get(0);
        }
    }

    public final double calcolaPrezzo() {
        if (this.gusto != null) {
            return Math.round(this.gusto.getPrezzoAlKg() * this.pesoKg * 100.0) / 100.0;
        }
        return prezzoTotale;
    }

    public void annullaPerEsaurimento() {
        this.stato = StatoRichiesta.RIFIUTATA;
    }

    public void completaRitiro() {
        this.stato = StatoRichiesta.COMPLETATA;
    }

    public String getCodiceOrdine() {
        return codiceOrdine;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Gusto getGusto() {
        return gusto;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public String getDataRitiro() {
        return dataRitiro;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public double getPrezzoTotale() {
        return prezzoTotale;
    }

    public void setPrezzoTotale(double prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }

    public List<VaschettaOrdinata> getVaschette() {
        return Collections.unmodifiableList(vaschette);
    }

    public String getDescrizioneDettagliata() {
        if (descrizioneDettagliata != null && !descrizioneDettagliata.isEmpty()) {
            return descrizioneDettagliata;
        }
        if (gusto != null) {
            return "Vaschetta " + gusto.getNome() + " (" + pesoKg + " kg)";
        }
        return "Ordine vaschette";
    }

    public void setDescrizioneDettagliata(String desc) {
        this.descrizioneDettagliata = desc;
    }
}
