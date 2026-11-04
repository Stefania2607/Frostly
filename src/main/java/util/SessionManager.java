package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SessionManager {

    private static SessionManager instance;
    private ModalitaEsecuzione modalita;
    private String emailUtenteLoggato;
    private String ruoloUtenteLoggato;
    private String nomeUtenteLoggato;

    private SessionManager() {
        this.modalita = caricaModalitaDaConfigurazione();
    }

    private ModalitaEsecuzione caricaModalitaDaConfigurazione() {
        try (InputStream input = SessionManager.class.getResourceAsStream("/db.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String mode = prop.getProperty("app.mode", "FULL").trim();
                if ("DEMO".equalsIgnoreCase(mode)) {
                    return ModalitaEsecuzione.DEMO;
                }
            }
        } catch (IOException ignored) {
            // Default FULL in caso di assenza proprietà
        }
        return ModalitaEsecuzione.FULL;
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
