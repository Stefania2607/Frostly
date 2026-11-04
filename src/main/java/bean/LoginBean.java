package bean;

import frostly.exception.DatiNonValidiException;

/**
 * Bean per il Login (Sign In) e la Registrazione (Sign Up):
 * blocca input malformati direttamente nei setter prima di arrivare al Control e al Model.
 */
public class LoginBean {

    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String ruoloScelto;
    private String ruoloAutenticato;
    private String nomeCompleto;

    public LoginBean() {
        // Costruttore vuoto JavaBean
    }

    public LoginBean(String email, String password) throws DatiNonValidiException {
        setEmail(email);
        setPassword(password);
    }

    public String getNome() {
        return nome;
    }

    public final void setNome(String nome) throws DatiNonValidiException {
        if (nome == null || nome.trim().length() < 2) {
            throw new DatiNonValidiException("Nome non valido: inserire almeno 2 caratteri.");
        }
        this.nome = nome.trim();
    }

    public String getCognome() {
        return cognome;
    }

    public final void setCognome(String cognome) throws DatiNonValidiException {
        if (cognome == null || cognome.trim().length() < 2) {
            throw new DatiNonValidiException("Cognome non valido: inserire almeno 2 caratteri.");
        }
        this.cognome = cognome.trim();
    }

    public String getEmail() {
        return email;
    }

    public final void setEmail(String email) throws DatiNonValidiException {
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new DatiNonValidiException("Email non valida: deve contenere '@'.");
        }
        this.email = email.trim();
    }

    public String getPassword() {
        return password;
    }

    public final void setPassword(String password) throws DatiNonValidiException {
        if (password == null || password.trim().length() < 4) {
            throw new DatiNonValidiException("Password non valida: inserire almeno 4 caratteri.");
        }
        this.password = password;
    }

    public String getRuoloScelto() {
        return ruoloScelto;
    }

    public final void setRuoloScelto(String ruoloScelto) throws DatiNonValidiException {
        if (ruoloScelto == null || ruoloScelto.trim().isEmpty()) {
            throw new DatiNonValidiException("Selezionare un ruolo valido (CLIENTE o DIPENDENTE).");
        }
        this.ruoloScelto = ruoloScelto.trim().toUpperCase();
    }

    public String getRuoloAutenticato() {
        return ruoloAutenticato;
    }

    public void setRuoloAutenticato(String ruoloAutenticato) {
        this.ruoloAutenticato = ruoloAutenticato;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }
}
