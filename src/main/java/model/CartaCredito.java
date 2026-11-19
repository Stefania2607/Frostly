package model;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Entità che rappresenta una Carta di Credito o Debito per il pagamento online.
 */
public class CartaCredito {

    private final String numeroCarta;
    private final String titolare;
    private final int meseScadenza;
    private final int annoScadenza;
    private final String cvv;
    private final String circuito;

    public CartaCredito(String numeroCarta, String titolare, int meseScadenza, int annoScadenza, String cvv) {
        this.numeroCarta = (numeroCarta != null) ? numeroCarta.replaceAll("\\s+", "") : "";
        this.titolare = (titolare != null) ? titolare.trim() : "";
        this.meseScadenza = meseScadenza;
        this.annoScadenza = annoScadenza;
        this.cvv = (cvv != null) ? cvv.trim() : "";
        this.circuito = rilevaCircuito(this.numeroCarta);
    }

    public static String rilevaCircuito(String numeroPulito) {
        if (numeroPulito == null || numeroPulito.isEmpty()) {
            return "CARTA";
        }
        if (numeroPulito.startsWith("4")) {
            return "VISA";
        }
        if (numeroPulito.startsWith("51") || numeroPulito.startsWith("52")
                || numeroPulito.startsWith("53") || numeroPulito.startsWith("54")
                || numeroPulito.startsWith("55") || numeroPulito.startsWith("2")) {
            return "MASTERCARD";
        }
        if (numeroPulito.startsWith("34") || numeroPulito.startsWith("37")) {
            return "AMERICAN EXPRESS";
        }
        return "CIRCUITO BANCARIO";
    }

    public String getNumeroMascherato() {
        if (numeroCarta.length() >= 4) {
            String ultimeQuattro = numeroCarta.substring(numeroCarta.length() - 4);
            return "•••• •••• •••• " + ultimeQuattro;
        }
        return "•••• •••• •••• ••••";
    }

    public boolean isScaduta(LocalDate oggi) {
        if (oggi == null) {
            oggi = LocalDate.now();
        }
        if (meseScadenza < 1 || meseScadenza > 12) {
            return true;
        }
        YearMonth scadenzaYm = YearMonth.of(annoScadenza, meseScadenza);
        YearMonth oggiYm = YearMonth.from(oggi);
        return scadenzaYm.isBefore(oggiYm);
    }

    public String getNumeroCarta() {
        return numeroCarta;
    }

    public String getTitolare() {
        return titolare;
    }

    public int getMeseScadenza() {
        return meseScadenza;
    }

    public int getAnnoScadenza() {
        return annoScadenza;
    }

    public String getCvv() {
        return cvv;
    }

    public String getCircuito() {
        return circuito;
    }
}
