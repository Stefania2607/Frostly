package bean;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaBean per rappresentare una singola vaschetta nel pre-ordine o nel carrello.
 */
public class VaschettaItemBean {

    private double pesoKg;
    private final List<String> gusti = new ArrayList<>();
    private boolean conPanna;
    private boolean conGranellaBronte;
    private String descrizione;
    private double prezzo;
    private double prezzoUnitario;
    private int quantita = 1;

    public VaschettaItemBean() {
        // Costruttore JavaBean standard
    }

    public VaschettaItemBean(double pesoKg, List<String> gusti, boolean conPanna, boolean conGranellaBronte,
                             String descrizione, double prezzo) {
        this(pesoKg, gusti, conPanna, conGranellaBronte, descrizione, prezzo, 1);
    }

    public VaschettaItemBean(double pesoKg, List<String> gusti, boolean conPanna, boolean conGranellaBronte,
                             String descrizione, double prezzoUnitario, int quantita) {
        this.pesoKg = pesoKg;
        if (gusti != null) {
            this.gusti.addAll(gusti);
        }
        this.conPanna = conPanna;
        this.conGranellaBronte = conGranellaBronte;
        this.descrizione = descrizione;
        this.prezzoUnitario = prezzoUnitario;
        this.quantita = Math.max(1, quantita);
        this.prezzo = Math.round(prezzoUnitario * this.quantita * 100.0) / 100.0;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = Math.max(0, quantita);
        this.prezzo = Math.round(getPrezzoUnitario() * this.quantita * 100.0) / 100.0;
    }

    public double getPrezzoUnitario() {
        if (prezzoUnitario > 0.0) return prezzoUnitario;
        return quantita > 0 ? (prezzo / quantita) : prezzo;
    }

    public void setPrezzoUnitario(double prezzoUnitario) {
        this.prezzoUnitario = prezzoUnitario;
        this.prezzo = Math.round(this.prezzoUnitario * this.quantita * 100.0) / 100.0;
    }

    public void incrementaQuantita() {
        this.quantita++;
        this.prezzo = Math.round(getPrezzoUnitario() * this.quantita * 100.0) / 100.0;
    }

    public void decrementaQuantita() {
        if (this.quantita > 0) {
            this.quantita--;
            this.prezzo = Math.round(getPrezzoUnitario() * this.quantita * 100.0) / 100.0;
        }
    }

    public VaschettaItemBean creaCopiaSingola() {
        return new VaschettaItemBean(pesoKg, gusti, conPanna, conGranellaBronte, descrizione, getPrezzoUnitario(), 1);
    }

    public double ottieniPesoKg() {
        return pesoKg;
    }

    public void impostaPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public List<String> ottieniGusti() {
        return gusti;
    }

    public void impostaGusti(List<String> lista) {
        this.gusti.clear();
        if (lista != null) {
            this.gusti.addAll(lista);
        }
    }

    public boolean haPanna() {
        return conPanna;
    }

    public void impostaPanna(boolean conPanna) {
        this.conPanna = conPanna;
    }

    public boolean haGranellaBronte() {
        return conGranellaBronte;
    }

    public void impostaGranellaBronte(boolean conGranellaBronte) {
        this.conGranellaBronte = conGranellaBronte;
    }

    public String ottieniDescrizione() {
        return descrizione;
    }

    public void impostaDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public double ottieniPrezzo() {
        return prezzo;
    }

    public void impostaPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }
}
