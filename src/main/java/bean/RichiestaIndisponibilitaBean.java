package bean;

import java.time.LocalDate;

/**
 * JavaBean puro per il trasporto delle richieste di permesso / indisponibilita'
 * tra Controller Applicativo e Boundary (GUI/CLI).
 */
public class RichiestaIndisponibilitaBean {

    private String codiceRichiesta;
    private String emailDipendente;
    private String nomeDipendente;
    private LocalDate dataRichiesta;
    private String motivazione;
    private String stato;
    private String notaMaestro;

    public RichiestaIndisponibilitaBean() {
    }

    public RichiestaIndisponibilitaBean(String codiceRichiesta,
                                        String emailDipendente,
                                        String nomeDipendente,
                                        LocalDate dataRichiesta,
                                        String motivazione,
                                        String stato,
                                        String notaMaestro) {
        this.codiceRichiesta = codiceRichiesta;
        this.emailDipendente = emailDipendente;
        this.nomeDipendente = nomeDipendente;
        this.dataRichiesta = dataRichiesta;
        this.motivazione = motivazione;
        this.stato = stato;
        this.notaMaestro = notaMaestro;
    }

    public String getCodiceRichiesta() {
        return codiceRichiesta;
    }

    public void setCodiceRichiesta(String codiceRichiesta) {
        this.codiceRichiesta = codiceRichiesta;
    }

    public String getEmailDipendente() {
        return emailDipendente;
    }

    public void setEmailDipendente(String emailDipendente) {
        this.emailDipendente = emailDipendente;
    }

    public String getNomeDipendente() {
        return nomeDipendente;
    }

    public void setNomeDipendente(String nomeDipendente) {
        this.nomeDipendente = nomeDipendente;
    }

    public LocalDate getDataRichiesta() {
        return dataRichiesta;
    }

    public void setDataRichiesta(LocalDate dataRichiesta) {
        this.dataRichiesta = dataRichiesta;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public void setMotivazione(String motivazione) {
        this.motivazione = motivazione;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getNotaMaestro() {
        return notaMaestro;
    }

    public void setNotaMaestro(String notaMaestro) {
        this.notaMaestro = notaMaestro;
    }
}
