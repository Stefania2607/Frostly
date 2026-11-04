package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity che rappresenta un Dipendente della gelateria Frostly.
 * Mantiene i riferimenti diretti agli oggetti Turno (no foreign key).
 */
public class Dipendente {

    private String nome;
    private String cognome;
    private String email;
    private String password;
    private List<Turno> turni;

    public Dipendente(String nome, String cognome, String email, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.turni = new ArrayList<>();
    }

    public void aggiungiTurno(Turno turno) {
        this.turni.add(turno);
    }

    public void rimuoviTurno(Turno turno) {
        this.turni.remove(turno);
    }

    /**
     * Operazione di business: verifica se il dipendente e' libero in quella data e fascia oraria.
     */
    public boolean isDisponibile(String data, String oraInizio, String oraFine) {
        for (Turno t : turni) {
            if (t.getData().equals(data) && t.isInConflitto(oraInizio, oraFine)) {
                return false;
            }
        }
        return true;
    }

    public boolean autenticati(String emailInput, String passwordInput) {
        return this.email.equals(emailInput) && this.password.equals(passwordInput);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Turno> getTurni() {
        return new ArrayList<>(turni);
    }

    @Override
    public String toString() {
        return nome + " " + cognome;
    }
}