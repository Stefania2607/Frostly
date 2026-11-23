package bean;

import frostly.exception.DatiNonValidiException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean per il trasporto e la validazione sintattica dei dati di consegna o ritiro.
 */
public class DatiConsegnaBean {

    private static final Pattern PATTERN_TOPONIMI_ITALIANI = Pattern.compile(
            "^(?iu)(via|viale|piazza|piazzale|corso|largo|vicolo|lungotevere|lungomare|strada|salita|discesa|"
            + "circonvallazione|borgo|contrada|localit[aà]|monte|ponte|traversa|rampa|clivo|passeggiata|parco)\\.?\\s+");
    private static final Pattern PATTERN_NOME_VIA = Pattern.compile("[A-Za-zÀ-ÖØ-öø-ÿ]{2,}");
    private static final Pattern PATTERN_CIVICO = Pattern.compile("\\b\\d{1,5}(?:/?[A-Za-z])?\\b");

    public enum TipoConsegna {
        DOMICILIO("🛵 Consegna a Domicilio"),
        ASPORTO("🛍️ Ritiro al Banco");

        private final String label;

        TipoConsegna(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static TipoConsegna daStringa(String val) {
            if (val != null && val.toLowerCase().contains("banco")) {
                return ASPORTO;
            }
            return DOMICILIO;
        }
    }

    private TipoConsegna tipoConsegna = TipoConsegna.DOMICILIO;
    private String indirizzo = "";
    private String cittaCap = "Roma (RM)";
    private String telefono = "";
    private String noteCitofono = "";
    private String orarioRichiesto = "Subito (~25-35 min)";
    private String nominativoRitiro = "";

    public DatiConsegnaBean() {
    }

    public DatiConsegnaBean(TipoConsegna tipo, String indirizzo, String telefono, String orario, String note) {
        this.tipoConsegna = tipo;
        this.indirizzo = indirizzo;
        this.telefono = telefono;
        this.orarioRichiesto = orario;
        this.noteCitofono = note;
    }

    public void validaSintassi() throws DatiNonValidiException {
        if (this.tipoConsegna == TipoConsegna.DOMICILIO) {
            validaIndirizzoItaliano(this.indirizzo);
            validaTelefonoItaliano(this.telefono);
        } else {
            if (this.orarioRichiesto == null || this.orarioRichiesto.isBlank()) {
                throw new DatiNonValidiException("Seleziona l'orario desiderato per il ritiro in gelateria.");
            }
            String nom = this.nominativoRitiro != null ? this.nominativoRitiro.trim() : "";
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
        Matcher mToponimo = PATTERN_TOPONIMI_ITALIANI.matcher(ind);
        if (!mToponimo.find()) {
            throw new DatiNonValidiException("L'indirizzo deve iniziare con un toponimo valido (Via, Viale, Piazza, Corso, Largo, Vicolo, Monte, Strada, ecc.).");
        }
        String resto = ind.substring(mToponimo.end()).trim();
        if (!PATTERN_NOME_VIA.matcher(resto).find()) {
            throw new DatiNonValidiException("Inserisci il nome completo della via o piazza dopo il toponimo (es. Via Roma 12).");
        }
        if (!PATTERN_CIVICO.matcher(resto).find()) {
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
        boolean cellulareValido = pulito.matches("^3[1-9]\\d{7,8}$");
        boolean fissoValido = pulito.matches("^0\\d{5,10}$");
        if (!cellulareValido && !fissoValido) {
            throw new DatiNonValidiException("Inserisci un numero di telefono italiano valido (cellulare 3XX XXXXXXX oppure fisso 0X XXXXXXXX).");
        }
        if (pulito.matches("^(\\d)\\1+$")) {
            throw new DatiNonValidiException("Il numero di telefono inserito non è verosimile.");
        }
    }

    public String costruisciStringaRiepilogoConsegna() {
        if (tipoConsegna == TipoConsegna.ASPORTO) {
            StringBuilder sb = new StringBuilder("Ritiro in Gelateria (Banco)");
            if (orarioRichiesto != null && !orarioRichiesto.isBlank()) {
                sb.append(" • Orario: ").append(orarioRichiesto);
            }
            if (nominativoRitiro != null && !nominativoRitiro.isBlank()) {
                sb.append(" • Nome: ").append(nominativoRitiro.trim());
            }
            if (noteCitofono != null && !noteCitofono.isBlank()) {
                sb.append(" • Note: ").append(noteCitofono.trim());
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder("Consegna a Domicilio");
            if (indirizzo != null && !indirizzo.isBlank()) {
                sb.append(" • Indirizzo: ").append(indirizzo.trim());
            }
            if (cittaCap != null && !cittaCap.isBlank()) {
                sb.append(", ").append(cittaCap.trim());
            }
            if (telefono != null && !telefono.isBlank()) {
                sb.append(" • Tel: ").append(telefono.trim());
            }
            if (orarioRichiesto != null && !orarioRichiesto.isBlank()) {
                sb.append(" • Orario: ").append(orarioRichiesto);
            }
            if (noteCitofono != null && !noteCitofono.isBlank()) {
                sb.append(" • Note: ").append(noteCitofono.trim());
            }
            return sb.toString();
        }
    }

    public TipoConsegna getTipoConsegna() {
        return tipoConsegna;
    }

    public void setTipoConsegna(TipoConsegna tipoConsegna) {
        this.tipoConsegna = tipoConsegna;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCittaCap() {
        return cittaCap;
    }

    public void setCittaCap(String cittaCap) {
        this.cittaCap = cittaCap;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNoteCitofono() {
        return noteCitofono;
    }

    public void setNoteCitofono(String noteCitofono) {
        this.noteCitofono = noteCitofono;
    }

    public String getOrarioRichiesto() {
        return orarioRichiesto;
    }

    public void setOrarioRichiesto(String orarioRichiesto) {
        this.orarioRichiesto = orarioRichiesto;
    }

    public String getNominativoRitiro() {
        return nominativoRitiro;
    }

    public void setNominativoRitiro(String nominativoRitiro) {
        this.nominativoRitiro = nominativoRitiro;
    }
}
