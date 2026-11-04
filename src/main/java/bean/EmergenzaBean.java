package bean;

import frostly.exception.DatiNonValidiException;

/**
 * Bean per le Emergenze: valida sintatticamente codice turno e motivazione nei setter.
 */
public class EmergenzaBean {

    private String codiceEmergenza;
    private String codiceTurno;
    private String dataTurno;
    private String orarioTurno;
    private String emailRichiedente;
    private String nomeRichiedente;
    private String emailSostituto;
    private String nomeSostituto;
    private String motivazione;
    private String stato;
    private String notaRisposta;

    public EmergenzaBean() {
        // Costruttore vuoto JavaBean
    }

    public String getCodiceEmergenza() {
        return codiceEmergenza;
    }

    public void setCodiceEmergenza(String codiceEmergenza) {
        this.codiceEmergenza = codiceEmergenza;
    }

    public String getCodiceTurno() {
        return codiceTurno;
    }

    public void setCodiceTurno(String codiceTurno) throws DatiNonValidiException {
        if (codiceTurno == null || codiceTurno.trim().isEmpty()) {
            throw new DatiNonValidiException("Il codice del turno e' obbligatorio.");
        }
        this.codiceTurno = codiceTurno.trim();
    }

    public String getDataTurno() {
        return dataTurno;
    }

    public void setDataTurno(String dataTurno) {
        this.dataTurno = dataTurno;
    }

    public String getOrarioTurno() {
        return orarioTurno;
    }

    public void setOrarioTurno(String orarioTurno) {
        this.orarioTurno = orarioTurno;
    }

    public String getEmailRichiedente() {
        return emailRichiedente;
    }

    public void setEmailRichiedente(String emailRichiedente) {
        this.emailRichiedente = emailRichiedente;
    }

    public String getNomeRichiedente() {
        return nomeRichiedente;
    }

    public void setNomeRichiedente(String nomeRichiedente) {
        this.nomeRichiedente = nomeRichiedente;
    }

    public String getEmailSostituto() {
        return emailSostituto;
    }

    public void setEmailSostituto(String emailSostituto) {
        this.emailSostituto = emailSostituto;
    }

    public String getNomeSostituto() {
        return nomeSostituto;
    }

    public void setNomeSostituto(String nomeSostituto) {
        this.nomeSostituto = nomeSostituto;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public void setMotivazione(String motivazione) throws DatiNonValidiException {
        if (motivazione == null || motivazione.trim().length() < 5) {
            throw new DatiNonValidiException("Inserire una motivazione valida (almeno 5 caratteri).");
        }
        this.motivazione = motivazione.trim();
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getNotaRisposta() {
        return notaRisposta;
    }

    public void setNotaRisposta(String notaRisposta) {
        this.notaRisposta = notaRisposta;
    }
}