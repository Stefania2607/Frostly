package bean;

/**
 * JavaBean puro per visualizzare i dati di un Gusto nella Boundary.
 */
public class GustoBean {

    private String nome;
    private String descrizione;
    private String allergeni;
    private double prezzoAlKg;
    private boolean disponibileOggi;
    private int vaschetteDisponibili;

    public GustoBean() {
        // Costruttore vuoto standard JavaBean
    }

    public GustoBean(String nome, String descrizione, String allergeni, double prezzoAlKg,
                     boolean disponibileOggi, int vaschetteDisponibili) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.allergeni = allergeni;
        this.prezzoAlKg = prezzoAlKg;
        this.disponibileOggi = disponibileOggi;
        this.vaschetteDisponibili = vaschetteDisponibili;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getAllergeni() {
        return allergeni;
    }

    public void setAllergeni(String allergeni) {
        this.allergeni = allergeni;
    }

    public double getPrezzoAlKg() {
        return prezzoAlKg;
    }

    public void setPrezzoAlKg(double prezzoAlKg) {
        this.prezzoAlKg = prezzoAlKg;
    }

    public boolean isDisponibileOggi() {
        return disponibileOggi;
    }

    public void setDisponibileOggi(boolean disponibileOggi) {
        this.disponibileOggi = disponibileOggi;
    }

    public int getVaschetteDisponibili() {
        return vaschetteDisponibili;
    }

    public void setVaschetteDisponibili(int vaschetteDisponibili) {
        this.vaschetteDisponibili = vaschetteDisponibili;
    }
}