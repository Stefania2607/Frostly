package model;

/**
 * Entity che rappresenta una segnalazione di emergenza per un turno.
 * Mantiene puntatori diretti a Turno, Dipendente richiedente e Dipendente sostituto.
 */
public class Emergenza {

    private String codiceEmergenza;
    private Turno turno;
    private Dipendente richiedente;
    private Dipendente sostituto;
    private String motivazione;
    private StatoRichiesta stato;
    private String notaRisposta;

    public Emergenza(String codiceEmergenza, Turno turno, Dipendente richiedente, String motivazione) {
        this.codiceEmergenza = codiceEmergenza;
        this.turno = turno;
        this.richiedente = richiedente;
        this.motivazione = motivazione;
        this.stato = StatoRichiesta.IN_ATTESA;
        this.notaRisposta = "";
    }

    /**
     * Approva l'emergenza assegnando il turno al dipendente sostituto.
     */
    public void approvaConSostituto(Dipendente nuovoDipendente, String nota) {
        this.sostituto = nuovoDipendente;
        this.turno.riassegnaA(nuovoDipendente);
        this.stato = StatoRichiesta.APPROVATA;
        this.notaRisposta = nota;
    }

    /**
     * Rifiuta l'emergenza mantenendo il turno originale pubblicato.
     */
    public void rifiuta(String motivoRifiuto) {
        this.turno.pubblica();
        this.stato = StatoRichiesta.RIFIUTATA;
        this.notaRisposta = motivoRifiuto;
    }

    public String getCodiceEmergenza() {
        return codiceEmergenza;
    }

    public Turno getTurno() {
        return turno;
    }

    public Dipendente getRichiedente() {
        return richiedente;
    }

    public Dipendente getSostituto() {
        return sostituto;
    }

    public void setSostituto(Dipendente sostituto) {
        this.sostituto = sostituto;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public String getNotaRisposta() {
        return notaRisposta;
    }

    public void setNotaRisposta(String notaRisposta) {
        this.notaRisposta = notaRisposta;
    }
}