package model;

/**
 * Entity che rappresenta un Gusto di gelato in vetrina, collegato direttamente alla sua Ricetta.
 */
public class Gusto {

    private String nome;
    private String descrizione;
    private String allergeni;
    private double prezzoAlKg;
    private boolean disponibileOggi;
    private int vaschetteDisponibili;
    private Ricetta ricetta;

    public Gusto(String nome, String descrizione, String allergeni, double prezzoAlKg, Ricetta ricetta) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.allergeni = allergeni;
        this.prezzoAlKg = prezzoAlKg;
        this.ricetta = ricetta;
        this.disponibileOggi = true;
        this.vaschetteDisponibili = 2;
    }

    public void incrementaProduzione(int nuoveVaschette) {
        this.vaschetteDisponibili += nuoveVaschette;
        if (this.vaschetteDisponibili > 0) {
            this.disponibileOggi = true;
        }
    }

    public void segnaEsaurito() {
        this.vaschetteDisponibili = 0;
        this.disponibileOggi = false;
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

    public Ricetta getRicetta() {
        return ricetta;
    }

    public void setRicetta(Ricetta ricetta) {
        this.ricetta = ricetta;
    }
}