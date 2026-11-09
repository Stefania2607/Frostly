package bean;

import frostly.exception.DatiNonValidiException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean per il Pre-Ordine vaschetta con validazione sintattica nei setter,
 * che supporta ordini composti da una o molteplici vaschette.
 */
public class OrdineBean {

    private String codiceOrdine;
    private String emailCliente;
    private String nomeGusto;
    private double pesoKg;
    private String dataRitiro;
    private boolean conPanna;
    private boolean conGranellaBronte;
    private String descrizioneCompleta;
    private double prezzoTotale;
    private String stato;
    private final List<VaschettaItemBean> vaschette = new ArrayList<>();

    public OrdineBean() {
        // Costruttore vuoto JavaBean
    }

    public List<VaschettaItemBean> getVaschette() {
        return vaschette;
    }

    public void setVaschette(List<VaschettaItemBean> lista) {
        this.vaschette.clear();
        if (lista != null) {
            this.vaschette.addAll(lista);
        }
    }

    public void aggiungiVaschetta(VaschettaItemBean item) {
        if (item != null) {
            this.vaschette.add(item);
        }
    }

    public int getNumeroVaschette() {
        return vaschette.isEmpty() ? 1 : vaschette.size();
    }

    public String getCodiceOrdine() {
        return codiceOrdine;
    }

    public void setCodiceOrdine(String codiceOrdine) {
        this.codiceOrdine = codiceOrdine;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public String getNomeGusto() {
        return nomeGusto;
    }

    public void setNomeGusto(String nomeGusto) throws DatiNonValidiException {
        if (nomeGusto == null || nomeGusto.trim().isEmpty()) {
            throw new DatiNonValidiException("Selezionare un gusto dal menu.");
        }
        this.nomeGusto = nomeGusto.trim();
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) throws DatiNonValidiException {
        if (pesoKg < 0.3 || pesoKg > 5.0) {
            throw new DatiNonValidiException("Il peso della vaschetta deve essere tra 350g (0.35 kg) e 5.0 kg.");
        }
        this.pesoKg = pesoKg;
    }

    public void setPesoDaTesto(String testoPeso) throws DatiNonValidiException {
        try {
            double valore = Double.parseDouble(testoPeso.trim());
            setPesoKg(valore);
        } catch (NumberFormatException e) {
            throw new DatiNonValidiException("Peso non numerico ('" + testoPeso + "'): inserire un numero valido.");
        }
    }

    public String getDataRitiro() {
        return dataRitiro;
    }

    public void setDataRitiro(String dataRitiro) throws DatiNonValidiException {
        if (dataRitiro == null || dataRitiro.trim().isEmpty()) {
            throw new DatiNonValidiException("Inserire la data di ritiro.");
        }
        this.dataRitiro = dataRitiro.trim();
    }

    public boolean isConPanna() {
        return conPanna;
    }

    public void setConPanna(boolean conPanna) {
        this.conPanna = conPanna;
    }

    public boolean isConGranellaBronte() {
        return conGranellaBronte;
    }

    public void setConGranellaBronte(boolean conGranellaBronte) {
        this.conGranellaBronte = conGranellaBronte;
    }

    public String getDescrizioneCompleta() {
        return descrizioneCompleta;
    }

    public void setDescrizioneCompleta(String descrizioneCompleta) {
        this.descrizioneCompleta = descrizioneCompleta;
    }

    public double getPrezzoTotale() {
        return prezzoTotale;
    }

    public void setPrezzoTotale(double prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}