package bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean per lo stato del carrello e della vaschetta in composizione.
 */
public class CarrelloBean {

    private List<VaschettaItemBean> articoli = new ArrayList<>();
    private List<GustoBean> gustiNellaVaschetta = new ArrayList<>();
    private double pesoVaschettaKg = 0.50;
    private int maxGustiVaschetta = 4;

    private String modalitaConsegna = "🛵 Consegna a Domicilio";
    private String fasciaOraria = "Subito (~25-35 min)";
    private String indirizzoONote = "";

    public CarrelloBean() {
    }

    public List<VaschettaItemBean> getArticoli() {
        return articoli;
    }

    public void setArticoli(List<VaschettaItemBean> articoli) {
        this.articoli = (articoli != null) ? new ArrayList<>(articoli) : new ArrayList<>();
    }

    public List<GustoBean> getGustiNellaVaschetta() {
        return gustiNellaVaschetta;
    }

    public void setGustiNellaVaschetta(List<GustoBean> gustiNellaVaschetta) {
        this.gustiNellaVaschetta = (gustiNellaVaschetta != null) ? new ArrayList<>(gustiNellaVaschetta) : new ArrayList<>();
    }

    public double getPesoVaschettaKg() {
        return pesoVaschettaKg;
    }

    public void setPesoVaschettaKg(double pesoVaschettaKg) {
        this.pesoVaschettaKg = pesoVaschettaKg;
    }

    public int getMaxGustiVaschetta() {
        return maxGustiVaschetta;
    }

    public void setMaxGustiVaschetta(int maxGustiVaschetta) {
        this.maxGustiVaschetta = maxGustiVaschetta;
    }

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
}
