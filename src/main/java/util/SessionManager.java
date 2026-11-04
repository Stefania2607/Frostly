package util;

/**
 * Configurazione globale dell'applicazione (Singleton) per gestire la modalita'
 * Demo vs Full (DBMS o File System) e l'utente attualmente loggato.
 */
public class SessionManager {

    private static SessionManager instance;
    private ModalitaEsecuzione modalita = ModalitaEsecuzione.DEMO_IN_MEMORY;
    private String emailUtenteLoggato;
    private String ruoloUtenteLoggato;
    private String nomeUtenteLoggato;

    private SessionManager() {
        // Costruttore privato per Singleton
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public ModalitaEsecuzione getModalita() {
        return modalita;
    }

    public void setModalita(ModalitaEsecuzione modalita) {
        this.modalita = modalita;
    }

    public void impostaSessione(String email, String ruolo, String nome) {
        this.emailUtenteLoggato = email;
        this.ruoloUtenteLoggato = ruolo;
        this.nomeUtenteLoggato = nome;
    }

    public void logout() {
        this.emailUtenteLoggato = null;
        this.ruoloUtenteLoggato = null;
        this.nomeUtenteLoggato = null;
    }

    public String getEmailUtenteLoggato() {
        return emailUtenteLoggato;
    }

    public String getRuoloUtenteLoggato() {
        return ruoloUtenteLoggato;
    }

    public String getNomeUtenteLoggato() {
        return nomeUtenteLoggato;
    }
}