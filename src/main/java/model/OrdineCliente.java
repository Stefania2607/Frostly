package model;

/**
 * Entity che rappresenta un Pre-ordine di vaschetta effettuato da un Cliente.
 */
public class OrdineCliente {

    private String codiceOrdine;
    private Cliente cliente;
    private Gusto gusto;
    private double pesoKg;
    private String dataRitiro;
    private StatoRichiesta stato;
    private double prezzoTotale;

    public OrdineCliente(String codiceOrdine, Cliente cliente, Gusto gusto, double pesoKg, String dataRitiro) {
        this.codiceOrdine = codiceOrdine;
        this.cliente = cliente;
        this.gusto = gusto;
        this.pesoKg = pesoKg;
        this.dataRitiro = dataRitiro;
        this.stato = StatoRichiesta.APPROVATA;
        this.prezzoTotale = calcolaPrezzo();
    }

    public final double calcolaPrezzo() {
        return Math.round(this.gusto.getPrezzoAlKg() * this.pesoKg * 100.0) / 100.0;
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
}