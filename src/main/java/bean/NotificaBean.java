package bean;

/**
 * JavaBean puro per il trasporto delle notifiche verso le Boundary.
 */
public class NotificaBean {

    private String titolo;
    private String messaggio;
    private String timestamp;

    public NotificaBean() {
        // Costruttore vuoto standard JavaBean
    }

    public NotificaBean(String titolo, String messaggio, String timestamp) {
        this.titolo = titolo;
        this.messaggio = messaggio;
        this.timestamp = timestamp;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}