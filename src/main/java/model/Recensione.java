package model;

/**
 * Entita' di Dominio che rappresenta una Recensione rilasciata da un Cliente
 * sull'esperienza in gelateria, con voto in stelle, commento, reazioni della community
 * e eventuale risposta ufficiale del Maestro Gelataio.
 */
public class Recensione {

    private String codiceRecensione;
    private String autoreEmail;
    private String autoreNome;
    private int votoStelle;
    private String titolo;
    private String commento;
    private String dataOra;
    private String rispostaMaestro;
    private String dataRisposta;
    private int miPiace;

    public Recensione(String codiceRecensione,
                      String autoreEmail,
                      String autoreNome,
                      int votoStelle,
                      String titolo,
                      String commento,
                      String dataOra,
                      String rispostaMaestro,
                      String dataRisposta,
                      int miPiace) {
        this.codiceRecensione = codiceRecensione;
        this.autoreEmail = autoreEmail;
        this.autoreNome = autoreNome;
        this.votoStelle = votoStelle;
        this.titolo = titolo;
        this.commento = commento;
        this.dataOra = dataOra;
        this.rispostaMaestro = rispostaMaestro;
        this.dataRisposta = dataRisposta;
        this.miPiace = miPiace;
    }

    public String getCodiceRecensione() {
        return codiceRecensione;
    }

    public void setCodiceRecensione(String codiceRecensione) {
        this.codiceRecensione = codiceRecensione;
    }

    public String getAutoreEmail() {
        return autoreEmail;
    }

    public void setAutoreEmail(String autoreEmail) {
        this.autoreEmail = autoreEmail;
    }

    public String getAutoreNome() {
        return autoreNome;
    }

    public void setAutoreNome(String autoreNome) {
        this.autoreNome = autoreNome;
    }

    public int getVotoStelle() {
        return votoStelle;
    }

    public void setVotoStelle(int votoStelle) {
        this.votoStelle = votoStelle;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public String getDataOra() {
        return dataOra;
    }

    public void setDataOra(String dataOra) {
        this.dataOra = dataOra;
    }

    public String getRispostaMaestro() {
        return rispostaMaestro;
    }

    public void setRispostaMaestro(String rispostaMaestro) {
        this.rispostaMaestro = rispostaMaestro;
    }

    public String getDataRisposta() {
        return dataRisposta;
    }

    public void setDataRisposta(String dataRisposta) {
        this.dataRisposta = dataRisposta;
    }

    public int getMiPiace() {
        return miPiace;
    }

    public void setMiPiace(int miPiace) {
        this.miPiace = miPiace;
    }

    public void incrementaMiPiace() {
        this.miPiace++;
    }
}
