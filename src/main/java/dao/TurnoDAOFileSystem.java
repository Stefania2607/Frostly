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
import model.Dipendente;
import model.StatoTurno;
import model.Turno;

/**
 * Seconda versione persistente di TurnoDAO: salva e recupera l'Entity Turno su File System (CSV).
 * Soddisfa il requisito: "At least one DAO shall be provided in two versions DBMS and file system".
 */
public class TurnoDAOFileSystem implements TurnoDAO {

    private static final String TURNI_CSV_FILE = "turni_frostly.csv";
    private static final String SEP = ";";

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        InMemoryFrostlyDAO.getInstance().salvaTurno(turno);
        List<Turno> tuttiTurni = InMemoryFrostlyDAO.getInstance().caricaTuttiTurni();
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
            return InMemoryFrostlyDAO.getInstance().caricaTuttiTurni();
        }
        List<Turno> daFile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] campi = line.split(SEP);
                if (campi.length >= 6) {
                    Dipendente dip = InMemoryFrostlyDAO.getInstance().trovaDipendentePerEmail(campi[4]);
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
        return daFile.isEmpty() ? InMemoryFrostlyDAO.getInstance().caricaTuttiTurni() : daFile;
    }
}