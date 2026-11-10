package dao;

import frostly.exception.FrostlyException;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.MaestroGelataio;

/**
 * Interfaccia DAO dedicata esclusivamente alla persistenza degli Utenti (Maestro, Dipendenti, Clienti).
 * Rispetta il Single Responsibility Principle e l'Interface Segregation Principle.
 */
public interface UtenteDAO {

    MaestroGelataio getMaestroGelataio();

    List<Dipendente> caricaTuttiDipendenti();

    Dipendente trovaDipendentePerEmail(String email);

    List<Cliente> caricaTuttiClienti();

    Cliente trovaClientePerEmail(String email);

    void eliminaDipendente(String email) throws FrostlyException;

    void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo) throws FrostlyException;
}
