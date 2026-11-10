package dao.memoria;

import dao.ArchivioMemoria;
import dao.UtenteDAO;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.MaestroGelataio;

/**
 * Implementazione in-memory di UtenteDAO per la modalita' DEMO.
 */
public class UtenteDAOMemory implements UtenteDAO {

    private final ArchivioMemoria archivio = ArchivioMemoria.getInstance();

    @Override
    public MaestroGelataio getMaestroGelataio() {
        return archivio.getMaestroGelataio();
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        return new ArrayList<>(archivio.getDipendenti());
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        return archivio.trovaDipendentePerEmail(email);
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        return new ArrayList<>(archivio.getClienti());
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        return archivio.trovaClientePerEmail(email);
    }

    @Override
    public void eliminaDipendente(String email) throws FrostlyException {
        archivio.eliminaDipendente(email);
    }

    @Override
    public void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo)
            throws FrostlyException {
        archivio.registraNuovoUtente(nome, cognome, email, password, ruolo);
    }
}
