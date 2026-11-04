package model;

/**
 * Entity che rappresenta una Notifica destinata a un attore del sistema
 * (MaestroGelataio, Dipendente o Cliente).
 */
public class Notifica {

    private String emailDestinatario;
    private String titolo;
    private String messaggio;
    private String timestamp;
    private boolean letta;

    public Notifica(String emailDestinatario, String titolo, String messaggio, String timestamp) {
        this.emailDestinatario = emailDestinatario;
        this.titolo = titolo;
        this.messaggio = messaggio;
        this.timestamp = timestamp;
        this.letta = false;
    }

    public void segnaComeLetta() {
        this.letta = true;
    }

    public String getEmailDestinatario() {
        return emailDestinatario;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isLetta() {
        return letta;
    }
}