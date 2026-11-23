package bean;

import frostly.exception.InvalidCardException;

/**
 * Bean per il trasporto e la validazione sintattica dei dati di pagamento con carta.
 */
public class CartaPagamentoBean {

    private String titolare = "";
    private String numeroCarta = "";
    private String scadenza = ""; // Formato "MM/AA" o "MM/YYYY"
    private int meseScadenza;
    private int annoScadenza;
    private String cvv = "";

    public CartaPagamentoBean() {
    }

    public CartaPagamentoBean(String titolare, String numeroCarta, String scadenza, String cvv) {
        this.titolare = titolare;
        this.numeroCarta = numeroCarta;
        this.scadenza = scadenza;
        this.cvv = cvv;
        provaParsingScadenzaSilenzioso(scadenza);
    }

    /**
     * Esegue la validazione sintattica completa dei campi inseriti (intestatario, numero con Luhn,
     * formato scadenza MM/AA con parsing in mese/anno, formato CVV).
     */
    public void validaSintassi() throws InvalidCardException {
        validaTitolare(this.titolare);
        String numeroPulito = validaNumeroCarta(this.numeroCarta);
        int[] meseAnno = parseScadenza(this.scadenza);
        this.meseScadenza = meseAnno[0];
        this.annoScadenza = meseAnno[1];
        validaCvv(this.cvv, numeroPulito);
    }

    private void provaParsingScadenzaSilenzioso(String scadenzaRaw) {
        try {
            int[] ma = parseScadenza(scadenzaRaw);
            this.meseScadenza = ma[0];
            this.annoScadenza = ma[1];
        } catch (InvalidCardException ignored) {
            this.meseScadenza = 0;
            this.annoScadenza = 0;
        }
    }

    private static void validaTitolare(String titolare) throws InvalidCardException {
        if (titolare == null || titolare.trim().isEmpty()) {
            throw new InvalidCardException("Inserisci nome e cognome dell'intestatario della carta.");
        }
        String t = titolare.trim().replaceAll("\\s+", " ");
        if (!t.matches("^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]{5,60}$")) {
            throw new InvalidCardException("Nome e cognome intestatario non validi: usare solo lettere (es. Mario Rossi).");
        }
        String[] parole = t.split(" ");
        if (parole.length < 2) {
            throw new InvalidCardException("Inserisci sia il Nome che il Cognome dell'intestatario (es. Mario Rossi).");
        }
        for (String parola : parole) {
            String soloLettere = parola.replaceAll("['\\-]", "");
            if (soloLettere.length() < 2) {
                throw new InvalidCardException("Nome e cognome devono contenere parole valide di almeno 2 lettere (es. Mario Rossi).");
            }
            if (soloLettere.matches("^(?i)([a-zà-öø-ÿ])\\1{2,}$")) {
                throw new InvalidCardException("Il nominativo inserito non sembra verosimile.");
            }
        }
    }

    private static String validaNumeroCarta(String rawNumero) throws InvalidCardException {
        if (rawNumero == null || rawNumero.isBlank()) {
            throw new InvalidCardException("Il numero della carta è obbligatorio.");
        }
        String pulito = rawNumero.replaceAll("[\\s\\-]+", "");
        if (!pulito.matches("^\\d{15,16}$")) {
            throw new InvalidCardException("Il numero della carta deve essere composto da 16 cifre numeriche (15 per American Express).");
        }
        if (pulito.matches("^(\\d)\\1+$")) {
            throw new InvalidCardException("Numero di carta non verosimile (cifre tutte identiche).");
        }
        if (!haCircuitoRiconosciuto(pulito)) {
            throw new InvalidCardException("Circuito carta non riconosciuto (supportati: Visa, Mastercard, American Express, Maestro/Discover).");
        }
        if (!verificaAlgoritmoLuhn(pulito)) {
            throw new InvalidCardException("Numero di carta non valido (controllo di verosimiglianza Luhn fallito).");
        }
        return pulito;
    }

    private static boolean haCircuitoRiconosciuto(String numero) {
        if (numero.startsWith("4") && numero.length() == 16) return true;
        if ((numero.startsWith("34") || numero.startsWith("37")) && numero.length() == 15) return true;
        if (numero.startsWith("6") && numero.length() == 16) return true;
        if (numero.length() == 16) {
            int p2 = Integer.parseInt(numero.substring(0, 2));
            int p4 = Integer.parseInt(numero.substring(0, 4));
            if ((p2 >= 51 && p2 <= 55) || (p4 >= 2221 && p4 <= 2720)) return true;
        }
        return false;
    }

    private static int[] parseScadenza(String scadenzaRaw) throws InvalidCardException {
        if (scadenzaRaw == null || scadenzaRaw.isBlank()) {
            throw new InvalidCardException("La data di scadenza della carta è obbligatoria.");
        }
        String s = scadenzaRaw.trim().replace("-", "/");
        String[] parti = s.split("/");
        if (parti.length != 2) {
            throw new InvalidCardException("Formato scadenza non valido. Utilizzare MM/AA (es. 12/28).");
        }
        int mese;
        int anno;
        try {
            mese = Integer.parseInt(parti[0].trim());
            anno = Integer.parseInt(parti[1].trim());
        } catch (NumberFormatException e) {
            throw new InvalidCardException("Mese o anno di scadenza non numerici.");
        }
        if (mese < 1 || mese > 12) {
            throw new InvalidCardException("Mese di scadenza non valido (deve essere compreso tra 01 e 12).");
        }
        if (anno < 100) {
            anno += 2000;
        }
        return new int[]{mese, anno};
    }

    private static void validaCvv(String cvvRaw, String numeroCartaPulito) throws InvalidCardException {
        if (cvvRaw == null || cvvRaw.isBlank()) {
            throw new InvalidCardException("Il codice di sicurezza CVV/CVC è obbligatorio.");
        }
        String cvv = cvvRaw.trim();
        boolean isAmex = (numeroCartaPulito != null && (numeroCartaPulito.startsWith("34") || numeroCartaPulito.startsWith("37")));
        int lungAttesa = isAmex ? 4 : 3;
        if (!cvv.matches("^\\d{" + lungAttesa + "}$")) {
            throw new InvalidCardException("Il codice CVV deve contenere esattamente " + lungAttesa + " cifre numeriche.");
        }
        if (cvv.matches("^0+$")) {
            throw new InvalidCardException("Il codice di sicurezza CVV inserito non è valido.");
        }
    }

    public static boolean verificaAlgoritmoLuhn(String numeroCarta) {
        if (numeroCarta == null || numeroCarta.length() < 13) {
            return false;
        }
        int somma = 0;
        boolean raddoppia = false;
        for (int i = numeroCarta.length() - 1; i >= 0; i--) {
            int cifra = Character.getNumericValue(numeroCarta.charAt(i));
            if (cifra < 0 || cifra > 9) {
                return false;
            }
            if (raddoppia) {
                cifra *= 2;
                if (cifra > 9) {
                    cifra -= 9;
                }
            }
            somma += cifra;
            raddoppia = !raddoppia;
        }
        return (somma % 10 == 0);
    }

    public String getNumeroCartaPulito() {
        if (numeroCarta == null) return "";
        return numeroCarta.replaceAll("[\\s\\-]+", "");
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
        provaParsingScadenzaSilenzioso(scadenza);
    }

    public int getMeseScadenza() {
        return meseScadenza;
    }

    public void setMeseScadenza(int meseScadenza) {
        this.meseScadenza = meseScadenza;
    }

    public int getAnnoScadenza() {
        return annoScadenza;
    }

    public void setAnnoScadenza(int annoScadenza) {
        this.annoScadenza = annoScadenza;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
