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

    public TurnoBean() {
        // Costruttore vuoto JavaBean
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
        if (this.oraFine != null) {
            String fineNorm = ("00:00".equals(this.oraFine) || "24:00".equals(this.oraFine)) ? "24:00" : this.oraFine;
            String inizioNorm = ("00:00".equals(oraInizio) || "24:00".equals(oraInizio)) ? "24:00" : oraInizio;
            if (inizioNorm.compareTo(fineNorm) >= 0) {
                throw new DatiNonValidiException("L'ora di fine deve essere successiva all'ora di inizio.");
            }
        }
        this.oraInizio = oraInizio;
    }

    public void setOraFine(String oraFine) throws DatiNonValidiException {
        if (oraFine == null || !oraFine.matches("\\d{2}:\\d{2}")) {
            throw new DatiNonValidiException("Ora fine non valida: usare il formato HH:mm (es. 14:00).");
        }
        String fineNorm = ("00:00".equals(oraFine) || "24:00".equals(oraFine)) ? "24:00" : oraFine;
        String inizioNorm = (this.oraInizio != null && ("00:00".equals(this.oraInizio) || "24:00".equals(this.oraInizio)))
                ? "24:00" : this.oraInizio;
        if (this.oraInizio != null && inizioNorm.compareTo(fineNorm) >= 0) {
            throw new DatiNonValidiException("L'ora di fine deve essere successiva all'ora di inizio.");
        }
        this.oraFine = oraFine;
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
}
