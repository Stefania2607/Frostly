package bean;

import frostly.exception.DatiNonValidiException;

/**
 * Bean per i Turni: i setter effettuano la validazione sintattica dei campi
 * bloccando formati errati prima di invocare il Control.
 */
public class TurnoBean {

    private String codiceTurno;
    private String data;
    private String oraInizio;
    private String oraFine;
    private String emailDipendente;
    private String nomeDipendente;
    private String stato;
    private boolean bloccato;
    private boolean generatoAutomaticamente;
    private boolean personalizzato;
    private int pausaMinuti;

    public TurnoBean() {
    }

    public String getCodiceTurno() {
        return codiceTurno;
    }

    public String getData() {
        return data;
    }

    public String getOraInizio() {
        return oraInizio;
    }

    public String getOraFine() {
        return oraFine;
    }

    public String getEmailDipendente() {
        return emailDipendente;
    }

    public String getNomeDipendente() {
        return nomeDipendente;
    }

    public String getStato() {
        return stato;
    }

    public void setCodiceTurno(String codiceTurno) {
        this.codiceTurno = codiceTurno;
    }

    public void setData(String data) throws DatiNonValidiException {
        if (data == null || data.trim().isEmpty()) {
            throw new DatiNonValidiException("Il giorno del turno non puo' essere vuoto.");
        }
        this.data = data.trim();
    }

    public void setOraInizio(String oraInizio) throws DatiNonValidiException {
        if (oraInizio == null || !oraInizio.matches("\\d{2}:\\d{2}")) {
            throw new DatiNonValidiException("Ora inizio non valida: usare il formato HH:mm (es. 08:00).");
        }
        if (this.oraFine != null && !isIntervalloOrarioValido(oraInizio, this.oraFine)) {
            throw new DatiNonValidiException("L'ora di fine deve essere successiva all'ora di inizio (o turno notturno oltre la mezzanotte).");
        }
        this.oraInizio = oraInizio;
    }

    public void setOraFine(String oraFine) throws DatiNonValidiException {
        if (oraFine == null || !oraFine.matches("\\d{2}:\\d{2}")) {
            throw new DatiNonValidiException("Ora fine non valida: usare il formato HH:mm (es. 14:00).");
        }
        if (this.oraInizio != null && !isIntervalloOrarioValido(this.oraInizio, oraFine)) {
            throw new DatiNonValidiException("L'ora di fine deve essere successiva all'ora di inizio (o turno notturno oltre la mezzanotte).");
        }
        this.oraFine = oraFine;
    }

    private static boolean isIntervalloOrarioValido(String inizio, String fine) {
        if (inizio.equals(fine)) {
            return false;
        }
        String fineNorm = ("00:00".equals(fine) || "24:00".equals(fine)) ? "24:00" : fine;
        String inizioNorm = ("00:00".equals(inizio) || "24:00".equals(inizio)) ? "24:00" : inizio;
        if (inizioNorm.compareTo(fineNorm) < 0) {
            return true;
        }
        // Turno notturno che attraversa la mezzanotte (es. 20:00 -> 02:00)
        return inizio.compareTo("18:00") >= 0 && fine.compareTo("06:00") <= 0;
    }

    public void setEmailDipendente(String emailDipendente) throws DatiNonValidiException {
        if (emailDipendente == null || !emailDipendente.contains("@")) {
            throw new DatiNonValidiException("Email dipendente non valida.");
        }
        this.emailDipendente = emailDipendente.trim();
    }

    public void setNomeDipendente(String nomeDipendente) {
        this.nomeDipendente = nomeDipendente;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public boolean isBloccato() {
        return bloccato;
    }

    public void setBloccato(boolean bloccato) {
        this.bloccato = bloccato;
    }

    public boolean isGeneratoAutomaticamente() {
        return generatoAutomaticamente;
    }

    public void setGeneratoAutomaticamente(boolean generatoAutomaticamente) {
        this.generatoAutomaticamente = generatoAutomaticamente;
    }

    public boolean isPersonalizzato() {
        return personalizzato;
    }

    public void setPersonalizzato(boolean personalizzato) {
        this.personalizzato = personalizzato;
    }

    public int getPausaMinuti() {
        return pausaMinuti;
    }

    public void setPausaMinuti(int pausaMinuti) {
        this.pausaMinuti = Math.max(0, pausaMinuti);
    }
}
