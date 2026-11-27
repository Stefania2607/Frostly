package dao;

import frostly.exception.FrostlyException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Dipendente;
import model.StatoTurno;
import model.Turno;

public class TurnoDAOFileSystem implements TurnoDAO {

    private static final String DEFAULT_CSV_FILE = "turni_frostly.csv";
    private static final String SEP = ";";

    private final Path csvPath;
    private final ArchivioMemoria archivioUtenti;

    public TurnoDAOFileSystem() {
        this(Paths.get(DEFAULT_CSV_FILE), ArchivioMemoria.getJdbcCacheInstance());
    }

    public TurnoDAOFileSystem(Path csvPath) {
        this(csvPath, ArchivioMemoria.getJdbcCacheInstance());
    }

    TurnoDAOFileSystem(Path csvPath, ArchivioMemoria archivioUtenti) {
        this.csvPath = csvPath != null ? csvPath : Paths.get(DEFAULT_CSV_FILE);
        this.archivioUtenti = archivioUtenti;
    }

    @Override
    public synchronized void salvaTurno(Turno turno) throws FrostlyException {
        if (turno == null || turno.getCodiceTurno() == null || turno.getDipendenteAssegnato() == null) {
            return;
        }
        List<Turno> esistenti = leggiTurniDaFile();
        boolean aggiornato = false;
        for (int i = 0; i < esistenti.size(); i++) {
            if (turno.getCodiceTurno().equalsIgnoreCase(esistenti.get(i).getCodiceTurno())) {
                esistenti.set(i, turno);
                aggiornato = true;
                break;
            }
        }
        if (!aggiornato) {
            esistenti.add(turno);
        }
        scriviCsvAtomico(esistenti);
    }

    @Override
    public synchronized List<Turno> caricaTuttiTurni() throws FrostlyException {
        return leggiTurniDaFile();
    }

    @Override
    public synchronized void eliminaTurno(String codiceTurno) throws FrostlyException {
        if (codiceTurno == null || codiceTurno.isBlank()) {
            return;
        }
        List<Turno> esistenti = leggiTurniDaFile();
        esistenti.removeIf(t -> t.getCodiceTurno() != null && t.getCodiceTurno().equalsIgnoreCase(codiceTurno));
        scriviCsvAtomico(esistenti);
    }

    @Override
    public synchronized void sostituisciTurniAtomico(List<String> codiciDaEliminare, List<Turno> nuoviTurni) throws FrostlyException {
        List<Turno> esistenti = leggiTurniDaFile();

        Set<String> daRimuovere = new HashSet<>();
        if (codiciDaEliminare != null) {
            for (String cod : codiciDaEliminare) {
                if (cod != null && !cod.isBlank()) {
                    daRimuovere.add(cod.trim().toUpperCase(java.util.Locale.ROOT));
                }
            }
        }
        if (!daRimuovere.isEmpty()) {
            esistenti.removeIf(t -> t.getCodiceTurno() != null
                    && daRimuovere.contains(t.getCodiceTurno().trim().toUpperCase(java.util.Locale.ROOT)));
        }

        if (nuoviTurni != null) {
            for (Turno nuovo : nuoviTurni) {
                if (nuovo == null || nuovo.getCodiceTurno() == null || nuovo.getDipendenteAssegnato() == null) {
                    continue;
                }
                boolean trovato = false;
                for (int i = 0; i < esistenti.size(); i++) {
                    if (nuovo.getCodiceTurno().equalsIgnoreCase(esistenti.get(i).getCodiceTurno())) {
                        esistenti.set(i, nuovo);
                        trovato = true;
                        break;
                    }
                }
                if (!trovato) {
                    esistenti.add(nuovo);
                }
            }
        }

        scriviCsvAtomico(esistenti);
    }

    private List<Turno> leggiTurniDaFile() throws FrostlyException {
        List<Turno> daFile = new ArrayList<>();
        if (!Files.exists(csvPath)) {
            return daFile;
        }
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Turno t = parseRigaCsv(line);
                if (t != null) {
                    daFile.add(t);
                }
            }
        } catch (IOException e) {
            throw new FrostlyException("Errore lettura file CSV dei turni.", e);
        }
        return daFile;
    }

    private Turno parseRigaCsv(String line) {
        String[] campi = line.split(SEP, -1);
        if (campi.length < 6) {
            return null;
        }
        String emailDip = campi[4].trim();
        if (emailDip.isEmpty()) {
            return null;
        }
        Dipendente dip = risolviDipendente(emailDip);
        Turno t = new Turno(campi[0].trim(), campi[1].trim(), campi[2].trim(), campi[3].trim(), dip);
        try {
            t.setStato(StatoTurno.valueOf(campi[5].trim()));
        } catch (IllegalArgumentException ignored) {
            t.setStato(StatoTurno.PIANIFICATO);
        }
        if (campi.length >= 7) {
            t.setBloccato(Boolean.parseBoolean(campi[6].trim()));
        }
        if (campi.length >= 8) {
            t.setGeneratoAutomaticamente(Boolean.parseBoolean(campi[7].trim()));
        }
        if (campi.length >= 9) {
            t.setPersonalizzato(Boolean.parseBoolean(campi[8].trim()));
        }
        if (campi.length >= 10) {
            try {
                t.setPausaMinuti(Integer.parseInt(campi[9].trim()));
            } catch (NumberFormatException ignored) {
                // Mantiene 0 minuti di pausa in caso di valore non numerico
            }
        }
        return t;
    }

    private Dipendente risolviDipendente(String emailDip) {
        Dipendente dip = archivioUtenti != null ? archivioUtenti.trovaDipendentePerEmail(emailDip) : null;
        if (dip == null) {
            dip = new Dipendente("Personale", "(Non in organico)", emailDip, "");
        }
        return dip;
    }

    private void scriviCsvAtomico(List<Turno> tuttiTurni) throws FrostlyException {
        Path parent = csvPath.toAbsolutePath().getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tempFile = Files.createTempFile(parent, "turni_frostly_", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                for (Turno t : tuttiTurni) {
                    if (t.getDipendenteAssegnato() == null) {
                        continue;
                    }
                    String riga = String.join(SEP,
                            sanitizzaCampo(t.getCodiceTurno()),
                            sanitizzaCampo(t.getData()),
                            sanitizzaCampo(t.getOraInizio()),
                            sanitizzaCampo(t.getOraFine()),
                            sanitizzaCampo(t.getDipendenteAssegnato().getEmail()),
                            t.getStato() != null ? t.getStato().name() : StatoTurno.PIANIFICATO.name(),
                            String.valueOf(t.isBloccato()),
                            String.valueOf(t.isGeneratoAutomaticamente()),
                            String.valueOf(t.isPersonalizzato()),
                            String.valueOf(t.getPausaMinuti()));
                    writer.write(riga);
                    writer.newLine();
                }
            }
            Files.move(tempFile, csvPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FrostlyException("Errore scrittura file CSV dei turni.", e);
        }
    }

    private static String sanitizzaCampo(String val) {
        if (val == null) {
            return "";
        }
        return val.replace(SEP, ",").replace("\r", " ").replace("\n", " ").trim();
    }
}
