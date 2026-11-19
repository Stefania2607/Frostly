package bean;

/**
 * Bean contenente l'esito della transazione di pagamento online.
 */
public class EsitoPagamentoBean {

    private final boolean successo;
    private final String codiceTransazione;
    private final String messaggio;
    private final double importo;
    private final String dataOra;
    private final String cartaMascherata;
    private final String circuito;

    public EsitoPagamentoBean(boolean successo,
                              String codiceTransazione,
                              String messaggio,
                              double importo,
                              String dataOra,
                              String cartaMascherata,
                              String circuito) {
        this.successo = successo;
        this.codiceTransazione = codiceTransazione;
        this.messaggio = messaggio;
        this.importo = importo;
        this.dataOra = dataOra;
        this.cartaMascherata = cartaMascherata;
        this.circuito = circuito;
    }

    public static EsitoPagamentoBean fallito(String messaggioErrore) {
        return new EsitoPagamentoBean(false, null, messaggioErrore, 0.0, null, null, null);
    }

    public boolean isSuccesso() {
        return successo;
    }

    public String getCodiceTransazione() {
        return codiceTransazione;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public double getImporto() {
        return importo;
    }

    public String getDataOra() {
        return dataOra;
    }

    public String getCartaMascherata() {
        return cartaMascherata;
    }

    public String getCircuito() {
        return circuito;
    }
}
