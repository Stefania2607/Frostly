package bean;

/**
 * Bean per il trasporto dei dati necessari alla generazione dello scontrino fiscale digitale
 * accessibile tramite QR Code al termine del pagamento online.
 */
public class ScontrinoBean {

    private String codiceOrdine;
    private String codiceTransazione;
    private String nomeCliente;
    private String dataOra;
    private String modalitaConsegna;
    private String dettaglioProdotti;
    private double importoTotale;
    private String circuito;
    private String cartaMascherata;
    private String urlScontrino;

    public ScontrinoBean() {
    }

    public String getCodiceOrdine() {
        return codiceOrdine;
    }

    public void setCodiceOrdine(String codiceOrdine) {
        this.codiceOrdine = codiceOrdine;
    }

    public String getCodiceTransazione() {
        return codiceTransazione;
    }

    public void setCodiceTransazione(String codiceTransazione) {
        this.codiceTransazione = codiceTransazione;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getDataOra() {
        return dataOra;
    }

    public void setDataOra(String dataOra) {
        this.dataOra = dataOra;
    }

    public String getModalitaConsegna() {
        return modalitaConsegna;
    }

    public void setModalitaConsegna(String modalitaConsegna) {
        this.modalitaConsegna = modalitaConsegna;
    }

    public String getDettaglioProdotti() {
        return dettaglioProdotti;
    }

    public void setDettaglioProdotti(String dettaglioProdotti) {
        this.dettaglioProdotti = dettaglioProdotti;
    }

    public double getImportoTotale() {
        return importoTotale;
    }

    public void setImportoTotale(double importoTotale) {
        this.importoTotale = importoTotale;
    }

    public String getCircuito() {
        return circuito;
    }

    public void setCircuito(String circuito) {
        this.circuito = circuito;
    }

    public String getCartaMascherata() {
        return cartaMascherata;
    }

    public void setCartaMascherata(String cartaMascherata) {
        this.cartaMascherata = cartaMascherata;
    }

    public String getUrlScontrino() {
        return urlScontrino;
    }

    public void setUrlScontrino(String urlScontrino) {
        this.urlScontrino = urlScontrino;
    }
}
