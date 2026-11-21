package bean;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bean per il trasporto delle metriche di vendita e dello stato del magazzino
 * verso la dashboard statistiche del Maestro Gelataio.
 */
public class StatisticheBean {

    private double incassoTotale;
    private double scontrinoMedio;
    private int ordiniTotali;
    private int ordiniConfermati;
    private int ordiniInAttesa;
    private int ordiniDisdetti;
    private int ordiniDomicilio;
    private int ordiniRitiroBanco;
    private int ingredientiOk;
    private int ingredientiSottoSoglia;
    private int ingredientiEsauriti;
    private Map<String, Integer> topProdottiVenduti = new LinkedHashMap<>();

    public double getIncassoTotale() {
        return incassoTotale;
    }

    public void setIncassoTotale(double incassoTotale) {
        this.incassoTotale = incassoTotale;
    }

    public double getScontrinoMedio() {
        return scontrinoMedio;
    }

    public void setScontrinoMedio(double scontrinoMedio) {
        this.scontrinoMedio = scontrinoMedio;
    }

    public int getOrdiniTotali() {
        return ordiniTotali;
    }

    public void setOrdiniTotali(int ordiniTotali) {
        this.ordiniTotali = ordiniTotali;
    }

    public int getOrdiniConfermati() {
        return ordiniConfermati;
    }

    public void setOrdiniConfermati(int ordiniConfermati) {
        this.ordiniConfermati = ordiniConfermati;
    }

    public int getOrdiniInAttesa() {
        return ordiniInAttesa;
    }

    public void setOrdiniInAttesa(int ordiniInAttesa) {
        this.ordiniInAttesa = ordiniInAttesa;
    }

    public int getOrdiniDisdetti() {
        return ordiniDisdetti;
    }

    public void setOrdiniDisdetti(int ordiniDisdetti) {
        this.ordiniDisdetti = ordiniDisdetti;
    }

    public int getOrdiniDomicilio() {
        return ordiniDomicilio;
    }

    public void setOrdiniDomicilio(int ordiniDomicilio) {
        this.ordiniDomicilio = ordiniDomicilio;
    }

    public int getOrdiniRitiroBanco() {
        return ordiniRitiroBanco;
    }

    public void setOrdiniRitiroBanco(int ordiniRitiroBanco) {
        this.ordiniRitiroBanco = ordiniRitiroBanco;
    }

    public int getIngredientiOk() {
        return ingredientiOk;
    }

    public void setIngredientiOk(int ingredientiOk) {
        this.ingredientiOk = ingredientiOk;
    }

    public int getIngredientiSottoSoglia() {
        return ingredientiSottoSoglia;
    }

    public void setIngredientiSottoSoglia(int ingredientiSottoSoglia) {
        this.ingredientiSottoSoglia = ingredientiSottoSoglia;
    }

    public int getIngredientiEsauriti() {
        return ingredientiEsauriti;
    }

    public void setIngredientiEsauriti(int ingredientiEsauriti) {
        this.ingredientiEsauriti = ingredientiEsauriti;
    }

    public Map<String, Integer> getTopProdottiVenduti() {
        return topProdottiVenduti;
    }

    public void setTopProdottiVenduti(Map<String, Integer> topProdottiVenduti) {
        this.topProdottiVenduti = topProdottiVenduti != null ? topProdottiVenduti : new LinkedHashMap<>();
    }
}
