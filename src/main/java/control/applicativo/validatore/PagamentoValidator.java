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
 * e delle opzioni di consegna (Single Responsibility Principle).
 * Ispirato alle best practice di validazione software (es. RouteX / MastercardValidator).
 */
public final class PagamentoValidator {

    private PagamentoValidator() {
        // Utility class
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

    private static void validaTitolare(String titolare) throws InvalidCardException {
        if (titolare == null || titolare.trim().length() < 2) {
            throw new InvalidCardException("Inserisci il nome e cognome dell'intestatario della carta.");
        }
        String t = titolare.trim();
        if (!t.matches("^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]{2,60}$")) {
            throw new InvalidCardException("Il nome dell'intestatario contiene caratteri non consentiti.");
        }
    }

    private static String validaNumeroCarta(String rawNumero) throws InvalidCardException {
        if (rawNumero == null || rawNumero.isBlank()) {
            throw new InvalidCardException("Il numero della carta è obbligatorio.");
        }
        String pulito = rawNumero.replaceAll("[\\s\\-]+", "");
        if (!pulito.matches("^\\d{15,16}$")) {
            throw new InvalidCardException("Il numero della carta deve essere composto da 16 cifre numeriche.");
        }
        if (!verificaAlgoritmoLuhn(pulito)) {
            throw new InvalidCardException("Numero di carta non valido (fallito algoritmo di checksum Luhn).");
        }
        return pulito;
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

        return new int[]{mese, anno};
    }

    private static void validaCvv(String cvvRaw, String numeroCartaPulito) throws InvalidCardException {
        if (cvvRaw == null || cvvRaw.isBlank()) {
            throw new InvalidCardException("Il codice di sicurezza CVV/CVC è obbligatorio.");
        }
        String cvv = cvvRaw.trim();
        boolean isAmex = (numeroCartaPulito != null && (numeroCartaPulito.startsWith("34") || numeroCartaPulito.startsWith("37")));
        int lungAttesa = isAmex ? 4 : 3;

        if (!cvv.matches("^\\d{" + lungAttesa + "}$") && !cvv.matches("^\\d{3}$")) {
            throw new InvalidCardException("Il codice CVV deve contenere esattamente " + lungAttesa + " cifre numeriche.");
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
     * Valida i dati di recapito o ritiro.
     */
    public static void validaDatiConsegna(DatiConsegnaBean dati) throws DatiNonValidiException {
        if (dati == null) {
            throw new DatiNonValidiException("Dati di consegna non specificati.");
        }
        if (dati.getTipoConsegna() == DatiConsegnaBean.TipoConsegna.DOMICILIO) {
            if (dati.getIndirizzo() == null || dati.getIndirizzo().trim().length() < 5) {
                throw new DatiNonValidiException("Inserisci un indirizzo di consegna valido (Via/Piazza e civico).");
            }
            if (dati.getTelefono() == null || dati.getTelefono().replaceAll("[^0-9]", "").length() < 6) {
                throw new DatiNonValidiException("Inserisci un numero di telefono valido per il corriere del delivery.");
            }
        } else {
            if (dati.getOrarioRichiesto() == null || dati.getOrarioRichiesto().isBlank()) {
                throw new DatiNonValidiException("Seleziona l'orario desiderato per il ritiro in gelateria.");
            }
        }
    }
}
