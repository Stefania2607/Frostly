package dao;

import frostly.exception.FrostlyException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.Emergenza;
import model.Gusto;
import model.Ingrediente;
import model.MaestroGelataio;
import model.Notifica;
import model.OrdineCliente;
import model.RichiestaGustoPersonalizzato;
import model.StatoTurno;
import model.Turno;

/**
 * Implementa direttamente l'interfaccia FrostlyDAO (nessuna doppia generalizzazione!).
 * Gestisce la persistenza dei Turni su File System CSV.
 */
public class FileSystemFrostlyDAO implements FrostlyDAO {

    private static final String TURNI_CSV_FILE = "turni_frostly.csv";
    private static final String SEP = ";";
    private static FileSystemFrostlyDAO instance;
    private final InMemoryFrostlyDAO cacheDatiBase = InMemoryFrostlyDAO.getInstance();

    private FileSystemFrostlyDAO() {
    }

    public static synchronized FileSystemFrostlyDAO getInstance() {
        if (instance == null) {
            instance = new FileSystemFrostlyDAO();
        }
        return instance;
    }

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        cacheDatiBase.salvaTurno(turno);
        List<Turno> tuttiTurni = cacheDatiBase.caricaTuttiTurni();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TURNI_CSV_FILE))) {
            for (Turno t : tuttiTurni) {
                String riga = String.join(SEP,
                        t.getCodiceTurno(),
                        t.getData(),
                        t.getOraInizio(),
                        t.getOraFine(),
                        t.getDipendenteAssegnato().getEmail(),
                        t.getStato().name());
                writer.write(riga);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new FrostlyException("Errore scrittura file CSV dei turni.", e);
        }
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        File file = new File(TURNI_CSV_FILE);
        if (!file.exists()) {
            return cacheDatiBase.caricaTuttiTurni();
        }
        List<Turno> daFile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] campi = line.split(SEP);
                if (campi.length >= 6) {
                    Dipendente dip = trovaDipendentePerEmail(campi[4]);
                    if (dip != null) {
                        Turno t = new Turno(campi[0], campi[1], campi[2], campi[3], dip);
                        t.setStato(StatoTurno.valueOf(campi[5]));
                        daFile.add(t);
                    }
                }
            }
        } catch (IOException e) {
            throw new FrostlyException("Errore lettura file CSV dei turni.", e);
        }
        return daFile.isEmpty() ? cacheDatiBase.caricaTuttiTurni() : daFile;
    }

    @Override
    public MaestroGelataio getMaestroGelataio() {
        return cacheDatiBase.getMaestroGelataio();
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        return cacheDatiBase.caricaTuttiDipendenti();
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        return cacheDatiBase.trovaDipendentePerEmail(email);
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        return cacheDatiBase.caricaTuttiClienti();
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        return cacheDatiBase.trovaClientePerEmail(email);
    }

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        return cacheDatiBase.caricaTutteEmergenze();
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
        cacheDatiBase.salvaEmergenza(emergenza);
    }

    @Override
    public List<Ingrediente> caricaTuttiIngredienti() {
        return cacheDatiBase.caricaTuttiIngredienti();
    }

    @Override
    public Ingrediente trovaIngredientePerNome(String nome) {
        return cacheDatiBase.trovaIngredientePerNome(nome);
    }

    @Override
    public void aggiornaIngrediente(Ingrediente ingrediente) {
        cacheDatiBase.aggiornaIngrediente(ingrediente);
    }

    @Override
    public List<Gusto> caricaTuttiGusti() {
        return cacheDatiBase.caricaTuttiGusti();
    }

    @Override
    public Gusto trovaGustoPerNome(String nome) {
        return cacheDatiBase.trovaGustoPerNome(nome);
    }

    @Override
    public List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto() {
        return cacheDatiBase.caricaTutteRichiesteGusto();
    }

    @Override
    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        cacheDatiBase.salvaRichiestaGusto(richiesta);
    }

    @Override
    public List<OrdineCliente> caricaTuttiOrdini() {
        return cacheDatiBase.caricaTuttiOrdini();
    }

    @Override
    public void salvaOrdine(OrdineCliente ordine) {
        cacheDatiBase.salvaOrdine(ordine);
    }

    @Override
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        return cacheDatiBase.caricaNotifichePerUtente(emailDestinatario);
    }

    @Override
    public void inviaNotifica(Notifica notifica) {
        cacheDatiBase.inviaNotifica(notifica);
    }
}