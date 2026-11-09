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

    public VaschettaItemBean() {
        // Costruttore JavaBean standard
    }

    public VaschettaItemBean(double pesoKg, List<String> gusti, boolean conPanna, boolean conGranellaBronte,
                             String descrizione, double prezzo) {
        this.pesoKg = pesoKg;
        if (gusti != null) {
            this.gusti.addAll(gusti);
        }
        this.conPanna = conPanna;
        this.conGranellaBronte = conGranellaBronte;
        this.descrizione = descrizione;
        this.prezzo = prezzo;
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
