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
    private final ArchivioMemoria cache = ArchivioMemoria.getJdbcCacheInstance();

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        cache.salvaTurno(turno);
        scriviCsvTurni(cache.getTurni());
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        File file = new File(TURNI_CSV_FILE);
        if (!file.exists()) {
            return cache.getTurni();
        }
        List<Turno> daFile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] campi = line.split(SEP);
                if (campi.length >= 6) {
                    String emailDip = campi[4];
                    Dipendente dip = cache.trovaDipendentePerEmail(emailDip);
                    if (dip == null && emailDip != null) {
                        dip = new Dipendente("Personale", "(Non in organico)", emailDip, "");
                    }
                    if (dip != null) {
                        Turno t = new Turno(campi[0], campi[1], campi[2], campi[3], dip);
                        t.setStato(StatoTurno.valueOf(campi[5]));
                        if (campi.length >= 7) {
                            t.setBloccato(Boolean.parseBoolean(campi[6]));
                        }
                        if (campi.length >= 8) {
                            t.setGeneratoAutomaticamente(Boolean.parseBoolean(campi[7]));
                        }
                        if (campi.length >= 9) {
                            t.setPersonalizzato(Boolean.parseBoolean(campi[8]));
                        }
                        if (campi.length >= 10) {
                            try {
                                t.setPausaMinuti(Integer.parseInt(campi[9]));
                            } catch (NumberFormatException ignored) {
                                // Ignora formato pausa non valido
                            }
                        }
                        daFile.add(t);
                    }
                }
            }
        } catch (IOException e) {
            throw new FrostlyException("Errore lettura file CSV dei turni.", e);
        }
        return daFile.isEmpty() ? cache.getTurni() : daFile;
    }

    @Override
    public void eliminaTurno(String codiceTurno) throws FrostlyException {
        cache.eliminaTurno(codiceTurno);
        scriviCsvTurni(cache.getTurni());
    }

    @Override
    public void sostituisciTurniAtomico(List<String> codiciDaEliminare, List<Turno> nuoviTurni) throws FrostlyException {
        if (codiciDaEliminare != null) {
            for (String cod : codiciDaEliminare) {
                cache.eliminaTurno(cod);
            }
        }
        if (nuoviTurni != null) {
            for (Turno t : nuoviTurni) {
                cache.salvaTurno(t);
            }
        }
        scriviCsvTurni(cache.getTurni());
    }

    private void scriviCsvTurni(List<Turno> tuttiTurni) throws FrostlyException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TURNI_CSV_FILE))) {
            for (Turno t : tuttiTurni) {
                if (t.getDipendenteAssegnato() == null) {
                    continue;
                }
                String riga = String.join(SEP,
                        t.getCodiceTurno(),
                        t.getData(),
                        t.getOraInizio(),
                        t.getOraFine(),
                        t.getDipendenteAssegnato().getEmail(),
                        t.getStato().name(),
                        String.valueOf(t.isBloccato()),
                        String.valueOf(t.isGeneratoAutomaticamente()),
                        String.valueOf(t.isPersonalizzato()),
                        String.valueOf(t.getPausaMinuti()));
                writer.write(riga);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new FrostlyException("Errore scrittura file CSV dei turni.", e);
        }
    }
}
