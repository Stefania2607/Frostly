package bean;

/**
 * Bean per il trasporto dei dati di consegna a domicilio o ritiro in gelateria.
 */
public class DatiConsegnaBean {

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
