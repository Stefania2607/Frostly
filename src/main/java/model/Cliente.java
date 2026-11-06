package model;

/**
 * Entity che rappresenta un Cliente registrato di Frostly.
 */
public class Cliente extends Utente {

    public Cliente(String nome, String cognome, String email, String password) {
        super(nome, cognome, email, password);
    }
}
