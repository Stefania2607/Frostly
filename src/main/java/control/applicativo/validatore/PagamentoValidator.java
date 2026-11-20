package control.applicativo.validatore;

import bean.CartaPagamentoBean;
import bean.DatiConsegnaBean;
import frostly.exception.DatiNonValidiException;
import frostly.exception.InvalidCardException;
import model.CartaCredito;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Validatore specializzato per la conformita' dei dati di pagamento con carta
 * e delle opzioni di consegna.
 */
public final class PagamentoValidator {

    private PagamentoValidator() {
    }

    /**
     * Valida integralmente i dati della carta di credito (Titolare, Numero, Scadenza, CVV).
     *
     * @param bean Dati della carta
     * @return Entità CartaCredito validata
     * @throws InvalidCardException se uno dei parametri non è conforme o la carta è scaduta
     */
    public static CartaCredito validaCarta(CartaPagamentoBean bean) throws InvalidCardException {
        if (bean == null) {
            throw new InvalidCardException("Dati della carta mancanti.");
        }

        validaTitolare(bean.getTitolare());
        String numeroPulito = validaNumeroCarta(bean.getNumeroCarta());
        int[] meseAnno = validaScadenza(bean.getScadenza());
        validaCvv(bean.getCvv(), numeroPulito);

        CartaCredito carta = new CartaCredito(numeroPulito, bean.getTitolare(), meseAnno[0], meseAnno[1], bean.getCvv());
        if (carta.isScaduta(LocalDate.now())) {
            throw new InvalidCardException("La carta di credito indicata risulta scaduta (" + bean.getScadenza() + ").");
        }

        return carta;
    }

    private static final String PATTERN_TOPONIMI_ITALIANI =
            "^(?i)(via|viale|piazza|piazzale|corso|largo|vicolo|lungotevere|lungomare|strada|salita|discesa|"
            + "circonvallazione|borgo|contrada|localit[aà]|monte|ponte|traversa|rampa|clivo|passeggiata|parco)\\.?\\s+.*";

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
        if (numero.startsWith("4") && numero.length() == 16) return true; // Visa
        if ((numero.startsWith("34") || numero.startsWith("37")) && numero.length() == 15) return true; // Amex
        if (numero.startsWith("6") && numero.length() == 16) return true; // Discover/Maestro
        if (numero.length() == 16) {
            int p2 = Integer.parseInt(numero.substring(0, 2));
            int p4 = Integer.parseInt(numero.substring(0, 4));
            if ((p2 >= 51 && p2 <= 55) || (p4 >= 2221 && p4 <= 2720)) return true; // Mastercard
        }
        return false;
    }

    private static int[] validaScadenza(String scadenzaRaw) throws InvalidCardException {
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

        // Se l'anno è espresso in formato a 2 cifre (es. 28 -> 2028)
        if (anno < 100) {
            anno += 2000;
        }

        YearMonth scadenzaYm = YearMonth.of(anno, mese);
        YearMonth oggiYm = YearMonth.now();
        if (scadenzaYm.isBefore(oggiYm)) {
            throw new InvalidCardException(String.format("La carta è scaduta nel mese %02d/%d.", mese, anno));
        }
        if (scadenzaYm.isAfter(oggiYm.plusYears(15))) {
            throw new InvalidCardException("Data di scadenza non verosimile (oltre 15 anni nel futuro).");
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

    /**
     * Algoritmo di Luhn (Modulo 10) per la verifica della verosimiglianza del numero carta.
     */
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

    /**
     * Valida i dati di recapito o ritiro (indirizzo italiano realistico, civico obbligatorio, telefono italiano valido).
     */
    public static void validaDatiConsegna(DatiConsegnaBean dati) throws DatiNonValidiException {
        if (dati == null) {
            throw new DatiNonValidiException("Dati di consegna non specificati.");
        }
        if (dati.getTipoConsegna() == DatiConsegnaBean.TipoConsegna.DOMICILIO) {
            validaIndirizzoItaliano(dati.getIndirizzo());
            validaTelefonoItaliano(dati.getTelefono());
        } else {
            if (dati.getOrarioRichiesto() == null || dati.getOrarioRichiesto().isBlank()) {
                throw new DatiNonValidiException("Seleziona l'orario desiderato per il ritiro in gelateria.");
            }
            String nom = dati.getNominativoRitiro() != null ? dati.getNominativoRitiro().trim() : "";
            if (!nom.isEmpty() && !nom.matches("^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]{2,60}$")) {
                throw new DatiNonValidiException("Il nominativo per il ritiro contiene caratteri non validi.");
            }
        }
    }

    public static void validaIndirizzoItaliano(String indirizzoRaw) throws DatiNonValidiException {
        if (indirizzoRaw == null || indirizzoRaw.trim().length() < 6) {
            throw new DatiNonValidiException("Inserisci un indirizzo completo (es. Via del Corso 124).");
        }
        String ind = indirizzoRaw.trim().replaceAll("\\s+", " ");
        if (!ind.matches(PATTERN_TOPONIMI_ITALIANI)) {
            throw new DatiNonValidiException("L'indirizzo deve iniziare con un toponimo valido (Via, Viale, Piazza, Corso, Largo, Vicolo, Monte, Strada, ecc.).");
        }
        // Rimuove il toponimo iniziale per verificare che ci sia un nome di via reale
        String resto = ind.replaceFirst("(?i)^[a-zà]+\\.?\\s+", "").trim();
        if (!resto.matches(".*[A-Za-zÀ-ÖØ-öø-ÿ]{2,}.*")) {
            throw new DatiNonValidiException("Inserisci il nome completo della via o piazza dopo il toponimo (es. Via Roma 12).");
        }
        // Numero civico obbligatorio (almeno un numero intero 1..99999)
        if (!resto.matches(".*\\b\\d{1,5}(?:/?[A-Za-z])?\\b.*")) {
            throw new DatiNonValidiException("Il numero civico è obbligatorio nell'indirizzo (es. Via del Corso 124).");
        }
    }

    public static void validaTelefonoItaliano(String telefonoRaw) throws DatiNonValidiException {
        if (telefonoRaw == null || telefonoRaw.isBlank()) {
            throw new DatiNonValidiException("Il recapito telefonico italiano è obbligatorio per la consegna.");
        }
        String pulito = telefonoRaw.trim().replaceAll("[\\s\\-\\.()]+", "");
        if (pulito.startsWith("+39")) {
            pulito = pulito.substring(3);
        } else if (pulito.startsWith("0039")) {
            pulito = pulito.substring(4);
        }
        if (!pulito.matches("^\\d+$")) {
            throw new DatiNonValidiException("Il numero di telefono deve contenere solo cifre (prefisso +39 facoltativo).");
        }
        boolean cellulareValido = pulito.matches("^3[1-9]\\d{7,8}$"); // es. 3331234567 (9-10 cifre)
        boolean fissoValido = pulito.matches("^0\\d{5,10}$");         // es. 0612345678 (6-11 cifre)
        if (!cellulareValido && !fissoValido) {
            throw new DatiNonValidiException("Inserisci un numero di telefono italiano valido (cellulare 3XX XXXXXXX oppure fisso 0X XXXXXXXX).");
        }
        if (pulito.matches("^(\\d)\\1+$")) {
            throw new DatiNonValidiException("Il numero di telefono inserito non è verosimile.");
        }
    }
}
