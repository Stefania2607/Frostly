package model;

/**
 * Entity che rappresenta il Maestro Gelataio (Admin di Frostly).
 */
public class MaestroGelataio {

    private String nome;
    private String cognome;
    private String email;
    private String password;

    public MaestroGelataio(String nome, String cognome, String email, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
    }

    public boolean autenticati(String emailInput, String passwordInput) {
        return this.email.equals(emailInput) && this.password.equals(passwordInput);
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}