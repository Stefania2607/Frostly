package dao;

import frostly.exception.FrostlyException;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.MaestroGelataio;

/**
 * Interfaccia DAO dedicata esclusivamente alla persistenza degli Utenti (Maestro, Dipendenti, Clienti).
 */
public interface UtenteDAO {

    MaestroGelataio getMaestroGelataio();

    List<Dipendente> caricaTuttiDipendenti();

    Dipendente trovaDipendentePerEmail(String email);

    List<Cliente> caricaTuttiClienti();

    Cliente trovaClientePerEmail(String email);

    void eliminaDipendente(String email) throws FrostlyException;

    void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo) throws FrostlyException;

    void aggiornaConfigurazioneDipendente(String email,
                                          model.ConfigurazioneOreDipendente configurazioneOre,
                                          model.DisponibilitaDipendente disponibilita) throws FrostlyException;

    default void aggiornaConfigurazioneDipendente(String email,
                                                  model.ConfigurazioneOreDipendente configurazioneOre,
                                                  model.DisponibilitaDipendente disponibilita,
                                                  java.util.Set<model.MansioneDipendente> mansioni,
                                                  Boolean attivo) throws FrostlyException {
        aggiornaConfigurazioneDipendente(email, configurazioneOre, disponibilita);
    }

    default void aggiornaConfigurazioneDipendente(String email,
                                                  model.ConfigurazioneOreDipendente configurazioneOre,
                                                  model.DisponibilitaDipendente disponibilita,
                                                  java.util.Set<model.MansioneDipendente> mansioni,
                                                  Boolean attivo,
                                                  Integer maxTurniSettimanali) throws FrostlyException {
        aggiornaConfigurazioneDipendente(email, configurazioneOre, disponibilita, mansioni, attivo);
    }
}
