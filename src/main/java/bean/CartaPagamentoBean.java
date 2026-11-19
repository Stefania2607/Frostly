package bean;

/**
 * Bean per il trasporto dei dati di pagamento con carta di credito / debito.
 */
public class CartaPagamentoBean {

    private String titolare = "";
    private String numeroCarta = "";
    private String scadenza = ""; // Formato "MM/AA" o "MM/YYYY"
    private String cvv = "";

    public CartaPagamentoBean() {
    }

    public CartaPagamentoBean(String titolare, String numeroCarta, String scadenza, String cvv) {
        this.titolare = titolare;
        this.numeroCarta = numeroCarta;
        this.scadenza = scadenza;
        this.cvv = cvv;
    }

    public String getNumeroCartaPulito() {
        if (numeroCarta == null) return "";
        return numeroCarta.replaceAll("\\s+", "");
    }

    public String getTitolare() {
        return titolare;
    }

    public void setTitolare(String titolare) {
        this.titolare = titolare;
    }

    public String getNumeroCarta() {
        return numeroCarta;
    }

    public void setNumeroCarta(String numeroCarta) {
        this.numeroCarta = numeroCarta;
    }

    public String getScadenza() {
        return scadenza;
    }

    public void setScadenza(String scadenza) {
        this.scadenza = scadenza;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
