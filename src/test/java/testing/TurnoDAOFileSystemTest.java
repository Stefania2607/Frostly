package testing;

import static org.junit.jupiter.api.Assertions.*;

import dao.TurnoDAOFileSystem;
import java.nio.file.Path;
import java.util.List;
import model.Dipendente;
import model.StatoTurno;
import model.Turno;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TurnoDAOFileSystemTest {

    @TempDir
    Path tempDir;

    @Test
    void testSalvaTurnoDopoRiavvioNonPerdeTurniEsistenti() throws Exception {
        Path csv = tempDir.resolve("turni_test.csv");
        Dipendente mario = new Dipendente("Mario", "Rossi", "mario@frostly.it", "pass123");
        Dipendente giulia = new Dipendente("Giulia", "Verdi", "giulia@frostly.it", "pass123");

        // Prima sessione: salva 2 turni su disco
        TurnoDAOFileSystem daoSessione1 = new TurnoDAOFileSystem(csv);
        Turno t1 = new Turno("TRN-01", "Lun 23/11/2026", "07:00", "14:00", mario);
        t1.setBloccato(true);
        Turno t2 = new Turno("TRN-02", "Lun 23/11/2026", "16:00", "00:00", giulia);
        daoSessione1.salvaTurno(t1);
        daoSessione1.salvaTurno(t2);

        // Seconda sessione (nuova istanza DAO, RAM vuota): salva 1 nuovo turno senza aver chiamato prima caricaTuttiTurni()
        TurnoDAOFileSystem daoSessione2 = new TurnoDAOFileSystem(csv);
        Turno t3 = new Turno("FLX-03", "Mar 24/11/2026", "12:00", "18:00", giulia);
        t3.setGeneratoAutomaticamente(true);
        t3.setPersonalizzato(true);
        t3.setPausaMinuti(30);
        daoSessione2.salvaTurno(t3);

        // Terza sessione: verifica che tutti e 3 i turni e tutti i loro campi siano intatti
        TurnoDAOFileSystem daoSessione3 = new TurnoDAOFileSystem(csv);
        List<Turno> caricati = daoSessione3.caricaTuttiTurni();
        assertEquals(3, caricati.size(), "Nessun turno gia' salvato su file deve andare perso");

        Turno caricatoT1 = caricati.stream().filter(t -> "TRN-01".equals(t.getCodiceTurno())).findFirst().orElseThrow();
        assertTrue(caricatoT1.isBloccato());

        Turno caricatoT3 = caricati.stream().filter(t -> "FLX-03".equals(t.getCodiceTurno())).findFirst().orElseThrow();
        assertTrue(caricatoT3.isGeneratoAutomaticamente());
        assertTrue(caricatoT3.isPersonalizzato());
        assertEquals(30, caricatoT3.getPausaMinuti());
    }

    @Test
    void testEliminaTurnoESostituzioneAtomicaSuFile() throws Exception {
        Path csv = tempDir.resolve("turni_atomico.csv");
        Dipendente mario = new Dipendente("Mario", "Rossi", "mario@frostly.it", "pass123");

        TurnoDAOFileSystem dao = new TurnoDAOFileSystem(csv);
        Turno t1 = new Turno("TRN-01", "Lun 23/11/2026", "07:00", "14:00", mario);
        Turno t2 = new Turno("TRN-02", "Mar 24/11/2026", "14:00", "20:00", mario);
        dao.salvaTurno(t1);
        dao.salvaTurno(t2);

        // Sostituzione atomica: rimuove TRN-02, mantiene TRN-01, aggiunge TRN-03
        Turno t3 = new Turno("TRN-03", "Mer 25/11/2026", "16:00", "00:00", mario);
        t3.setStato(StatoTurno.PIANIFICATO);
        new TurnoDAOFileSystem(csv).sostituisciTurniAtomico(List.of("TRN-02"), List.of(t3));

        List<Turno> dopoAtomico = new TurnoDAOFileSystem(csv).caricaTuttiTurni();
        assertEquals(2, dopoAtomico.size());
        assertTrue(dopoAtomico.stream().anyMatch(t -> "TRN-01".equals(t.getCodiceTurno())));
        assertTrue(dopoAtomico.stream().anyMatch(t -> "TRN-03".equals(t.getCodiceTurno())));
        assertFalse(dopoAtomico.stream().anyMatch(t -> "TRN-02".equals(t.getCodiceTurno())));

        // Eliminando tutti i turni il file deve restituire lista vuota (nessun dato fantasma)
        new TurnoDAOFileSystem(csv).eliminaTurno("TRN-01");
        new TurnoDAOFileSystem(csv).eliminaTurno("TRN-03");
        assertTrue(new TurnoDAOFileSystem(csv).caricaTuttiTurni().isEmpty());
    }
}
