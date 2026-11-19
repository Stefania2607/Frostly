package model;

/**
 * Entità che rappresenta una transazione registrata di pagamento online.
 */
public class TransazionePagamento {

    private final String codiceTransazione;
    private final String emailCliente;
    private final double importo;
    private final String dataOra;
    private final String stato; // "AUTORIZZATO", "RIFIUTATO"
    private final String cartaMascherata;
    private final String modalitaConsegna;

    public TransazionePagamento(String codiceTransazione,
                                String emailCliente,
                                double importo,
                                String dataOra,
                                String stato,
                                String cartaMascherata,
                                String modalitaConsegna) {
        this.codiceTransazione = codiceTransazione;
        this.emailCliente = emailCliente;
        this.importo = importo;
        this.dataOra = dataOra;
        this.stato = stato;
        this.cartaMascherata = cartaMascherata;
        this.modalitaConsegna = modalitaConsegna;
    }

    public String getCodiceTransazione() {
        return codiceTransazione;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public double getImporto() {
        return importo;
    }

    public String getDataOra() {
        return dataOra;
    }

    public String getStato() {
        return stato;
    }

    public String getCartaMascherata() {
        return cartaMascherata;
    }

    public String getModalitaConsegna() {
        return modalitaConsegna;
    }
}
