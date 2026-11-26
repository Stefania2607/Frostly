package model;

import java.time.LocalDate;

/**
 * Richiesta di permesso / ferie / indisponibilita' inviata da un dipendente per una data specifica.
 * Entra nei vincoli di pianificazione solo dopo approvazione esplicita del Maestro Gelataio.
 */
public class RichiestaIndisponibilita {

    public enum StatoRichiesta {
        IN_ATTESA,
        APPROVATA,
        RIFIUTATA
    }

    private final String codiceRichiesta;
    private final String emailDipendente;
    private final String nomeDipendente;
    private final LocalDate dataRichiesta;
    private final String motivazione;
    private StatoRichiesta stato;
    private String notaMaestro;

    public RichiestaIndisponibilita(String codiceRichiesta,
                                    String emailDipendente,
                                    String nomeDipendente,
                                    LocalDate dataRichiesta,
                                    String motivazione) {
        this.codiceRichiesta = codiceRichiesta;
        this.emailDipendente = emailDipendente;
        this.nomeDipendente = nomeDipendente;
        this.dataRichiesta = dataRichiesta;
        this.motivazione = motivazione != null ? motivazione.trim() : "";
        this.stato = StatoRichiesta.IN_ATTESA;
        this.notaMaestro = "";
    }

    public void approva(String nota) {
        this.stato = StatoRichiesta.APPROVATA;
        this.notaMaestro = nota != null ? nota.trim() : "";
    }

    public void rifiuta(String nota) {
        this.stato = StatoRichiesta.RIFIUTATA;
        this.notaMaestro = nota != null ? nota.trim() : "";
    }

    public String getCodiceRichiesta() {
        return codiceRichiesta;
    }

    public String getEmailDipendente() {
        return emailDipendente;
    }

    public String getNomeDipendente() {
        return nomeDipendente;
    }

    public LocalDate getDataRichiesta() {
        return dataRichiesta;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        if (stato != null) {
            this.stato = stato;
        }
    }

    public String getNotaMaestro() {
        return notaMaestro;
    }

    public void setNotaMaestro(String notaMaestro) {
        this.notaMaestro = notaMaestro != null ? notaMaestro : "";
    }
}
