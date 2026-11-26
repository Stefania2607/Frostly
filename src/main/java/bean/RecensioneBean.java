package bean;

import frostly.exception.DatiNonValidiException;

/**
 * Bean per il trasporto e la validazione dei dati delle Recensioni tra
 * il layer Grafico e il layer Applicativo.
 */
public class RecensioneBean {

    private String codiceRecensione;
    private String autoreEmail;
    private String autoreNome;
    private int votoStelle = 5;
    private String titolo;
    private String commento;
    private String dataOra;
    private String rispostaMaestro;
    private String dataRisposta;
    private int miPiace;
    private String fotoPath;

    public RecensioneBean() {
    }

    public RecensioneBean(String autoreEmail,
                          String autoreNome,
                          int votoStelle,
                          String titolo,
                          String commento) throws DatiNonValidiException {
        this.autoreEmail = autoreEmail;
        this.autoreNome = autoreNome;
        setVotoStelle(votoStelle);
        setTitolo(titolo);
        setCommento(commento);
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

    public final void setVotoStelle(int votoStelle) throws DatiNonValidiException {
        if (votoStelle < 1 || votoStelle > 5) {
            throw new DatiNonValidiException("La valutazione deve essere compresa tra 1 e 5 stelle.");
        }
        this.votoStelle = votoStelle;
    }

    public String getTitolo() {
        return titolo;
    }

    public final void setTitolo(String titolo) throws DatiNonValidiException {
        if (titolo == null || titolo.trim().isEmpty()) {
            throw new DatiNonValidiException("Il titolo della recensione non può essere vuoto.");
        }
        this.titolo = titolo.trim();
    }

    public String getCommento() {
        return commento;
    }

    public final void setCommento(String commento) throws DatiNonValidiException {
        if (commento == null || commento.trim().isEmpty()) {
            throw new DatiNonValidiException("Il testo della recensione non può essere vuoto.");
        }
        this.commento = commento.trim();
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

    public String getStelleIcone() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < votoStelle; i++) {
            sb.append("★");
        }
        for (int i = votoStelle; i < 5; i++) {
            sb.append("☆");
        }
        return sb.toString();
    }

    public boolean haRispostaMaestro() {
        return rispostaMaestro != null && !rispostaMaestro.trim().isEmpty();
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public boolean haFoto() {
        return fotoPath != null && !fotoPath.trim().isEmpty();
    }
}
