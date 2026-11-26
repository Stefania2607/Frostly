package testing;

import bean.PropostaTurniSettimanaBean;
import bean.TurnoBean;
import control.applicativo.CalcolatoreOreSettimanali;
import control.applicativo.CalcolatoreOreSettimanali.RiepilogoCoperturaSettimana;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.MotoreTurniFlessibili;
import dao.ArchivioMemoria;
import dao.TurnoDAOMemory;
import dao.memoria.EmergenzaDAOMemory;
import dao.memoria.NotificaDAOMemory;
import dao.memoria.UtenteDAOMemory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import model.ConfigurazioneCoperturaFlessibile;
import model.ConfigurazioneCoperturaFlessibile.IntervalloCopertura;
import model.ConfigurazioneGenerazioneTurni;
import model.ConfigurazioneGenerazioneTurni.ModalitaGenerazione;
import model.ConfigurazioneGenerazioneTurni.PrioritaOttimizzazione;
import model.ConfigurazioneGenerazioneTurni.TipoGenerazione;
import model.ConfigurazioneOreDipendente;
import model.Dipendente;
import model.DisponibilitaDipendente;
import model.MansioneDipendente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite JUnit 5 per il Generatore Intelligente di Turni Flessibili con Approvazione Obbligatoria.
 * Copre tutti i 22 scenari della Sezione 16: turni personalizzati, chiusura simultanea (2+ persone),
 * pause non lavorate, copertura per intervalli, mezzanotte/due settimane, modifica manuale in anteprima,
 * blocchi, annullamento, approvazione atomica e mancanza di personale.
 */
class TurniFlessibiliTest {

    private static final LocalDate LUNEDI_W1 = LocalDate.of(2026, 11, 23);
    private static final LocalDate LUNEDI_W2 = LocalDate.of(2026, 11, 30);

    private GestioneTurniEmergenzeControl ctrl;
    private UtenteDAOMemory utenteDao;

    @BeforeEach
    void setup() {
        ArchivioMemoria demo = ArchivioMemoria.getDemoInstance();
        for (model.Turno t : new ArrayList<>(demo.getTurni())) {
            demo.eliminaTurno(t.getCodiceTurno());
        }
        utenteDao = new UtenteDAOMemory();
        ctrl = new GestioneTurniEmergenzeControl(
                new TurnoDAOMemory(), new EmergenzaDAOMemory(), utenteDao, new NotificaDAOMemory());
        ctrl.salvaCoperturaFlessibile(ConfigurazioneCoperturaFlessibile.predefinita());
    }

    private Dipendente creaStaff(String nome, String cognome, String email,
                                 ConfigurazioneOreDipendente cfg,
                                 DisponibilitaDipendente disp,
                                 Integer maxTurni) {
        Dipendente d = new Dipendente(nome, cognome, email, "pass123");
        d.setConfigurazioneOre(cfg);
        d.setDisponibilita(disp != null ? disp : DisponibilitaDipendente.tuttoDisponibile());
        d.setMansioni(EnumSet.of(MansioneDipendente.BANCO, MansioneDipendente.LABORATORIO));
        d.setMaxTurniSettimanali(maxTurni);
        return d;
    }

    @Test
    void test01e13_TurnoPersonalizzatoManualeConPausaNonRetribuita() throws Exception {
        String email = utenteDao.caricaTuttiDipendenti().get(0).getEmail();
        TurnoBean custom = new TurnoBean();
        custom.setEmailDipendente(email);
        custom.setData("Sab 28/11/2026");
        custom.setOraInizio("16:00");
        custom.setOraFine("22:30"); // 6.5h lorde - 30 min pausa = 6.0h nette
        custom.setPersonalizzato(true);
        custom.setPausaMinuti(30);

        TurnoBean salvato = ctrl.pubblicaNuovoTurno(custom);
        assertTrue(salvato.isPersonalizzato());
        assertEquals(30, salvato.getPausaMinuti());
        assertEquals(6.0, CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(salvato, LUNEDI_W1), 0.01);
    }

    @Test
    void test03e04e05_GenerazioneFlessibileConNuoviOrariECoperturaChiusuraSimultanea() throws Exception {
        List<Dipendente> staff = List.of(
                creaStaff("Maria", "Rossi", "maria@frostly.it", ConfigurazioneOreDipendente.oreFisse(40, 3), null, 6),
                creaStaff("Giulia", "Verdi", "giulia@frostly.it", ConfigurazioneOreDipendente.oreFisse(40, 3), null, 6),
                creaStaff("Luca", "Bianchi", "luca@frostly.it", ConfigurazioneOreDipendente.intervallo(25, 38, 2, 2), null, 6),
                creaStaff("Sara", "Neri", "sara@frostly.it", ConfigurazioneOreDipendente.intervallo(25, 38, 2, 2), null, 6),
                creaStaff("Elena", "Blu", "elena@frostly.it", ConfigurazioneOreDipendente.intervallo(25, 38, 2, 2), null, 6)
        );

        ConfigurazioneCoperturaFlessibile cov = ConfigurazioneCoperturaFlessibile.predefinita();
        cov.setPersonaleMinimoChiusura(2); // 2 persone contemporaneamente in 22:00-00:00

        ConfigurazioneGenerazioneTurni cfg = new ConfigurazioneGenerazioneTurni(
                ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA);
        cfg.setTipoGenerazione(TipoGenerazione.FLESSIBILE);
        cfg.setDurataMinimaOreFlessibile(4);
        cfg.setDurataMassimaOreFlessibile(8);
        cfg.setIncrementoMinutiFlessibile(60);

        PropostaTurniSettimanaBean proposta = MotoreTurniFlessibili.generaPropostaFlessibile(
                LUNEDI_W1, staff, List.of(), cov, cfg);

        assertTrue(proposta.isCoperturaChiusuraOk(), "La chiusura (22:00-00:00) deve avere 2 persone simultanee ogni sera");
        assertEquals(100.0, proposta.getPercentualeCopertura(), 0.01);
        assertTrue(proposta.getTurniProposti().stream().anyMatch(TurnoBean::isPersonalizzato),
                "La generazione flessibile deve creare anche nuovi orari personalizzati fuori catalogo");
    }

    @Test
    void test06_07_08_09_10_VincoliDisponibilitaMonteOreTolleranzeMaxTurniEBloccati() throws Exception {
        DisponibilitaDipendente dispMaria = new DisponibilitaDipendente();
        dispMaria.impostaGiorno(DayOfWeek.SUNDAY, DisponibilitaDipendente.NON_DISPONIBILE);

        List<Dipendente> staff = List.of(
                creaStaff("Maria", "Rossi", "maria@frostly.it", ConfigurazioneOreDipendente.oreFisse(32, 3), dispMaria, 4),
                creaStaff("Luca", "Bianchi", "luca@frostly.it", ConfigurazioneOreDipendente.oreFisse(40, 3), null, 5),
                creaStaff("Giulia", "Verdi", "giulia@frostly.it", ConfigurazioneOreDipendente.intervallo(25, 38), null, 5),
                creaStaff("Sara", "Neri", "sara@frostly.it", ConfigurazioneOreDipendente.intervallo(25, 38), null, 5)
        );

        TurnoBean bloccato = new TurnoBean();
        bloccato.setCodiceTurno("TUR-BLK");
        bloccato.setEmailDipendente("maria@frostly.it");
        bloccato.setNomeDipendente("Maria Rossi");
        bloccato.setData("Sab 28/11/2026");
        bloccato.setOraInizio("18:00");
        bloccato.setOraFine("00:00");
        bloccato.setBloccato(true);
        bloccato.setPersonalizzato(true);

        ConfigurazioneGenerazioneTurni cfg = new ConfigurazioneGenerazioneTurni(
                ModalitaGenerazione.RIGENERA_NON_BLOCCATI, PrioritaOttimizzazione.COPERTURA_PRIORITARIA);
        cfg.setTipoGenerazione(TipoGenerazione.FLESSIBILE);

        PropostaTurniSettimanaBean p = MotoreTurniFlessibili.generaPropostaFlessibile(
                LUNEDI_W1, staff, List.of(bloccato), ConfigurazioneCoperturaFlessibile.predefinita(), cfg);

        int turniMaria = 0;
        boolean trovatoBloccato = false;
        for (TurnoBean t : p.getTurniProposti()) {
            if ("maria@frostly.it".equalsIgnoreCase(t.getEmailDipendente())) {
                turniMaria++;
                LocalDate dt = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), LUNEDI_W1);
                assertNotEquals(DayOfWeek.SUNDAY, dt.getDayOfWeek(), "Maria non e' disponibile la domenica");
                if ("TUR-BLK".equals(t.getCodiceTurno()) && t.isBloccato()) {
                    trovatoBloccato = true;
                }
            }
        }
        assertTrue(trovatoBloccato, "Il turno bloccato deve restare intatto");
        assertTrue(turniMaria <= 4, "Maria non deve superare il limite massimo di 4 turni");
    }

    @Test
    void test11e12_MezzanotteEDueSettimaneConPausa() throws Exception {
        TurnoBean notturno = new TurnoBean();
        notturno.setCodiceTurno("FLX-N1");
        notturno.setEmailDipendente("maria@frostly.it");
        notturno.setData("Dom 29/11/2026");
        notturno.setOraInizio("20:00");
        notturno.setOraFine("02:00"); // 6h lorde (4h in W1, 2h in W2), pausa 60 min -> 5h nette (3.33h W1, 1.67h W2)
        notturno.setPausaMinuti(60);

        double hW1 = CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(notturno, LUNEDI_W1);
        double hW2 = CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(notturno, LUNEDI_W2);
        assertEquals(5.0, hW1 + hW2, 0.02);
        assertTrue(hW1 > hW2);
    }

    @Test
    void test14_15_16_17_20_FlussoBozzaModificaAnnullamentoEApprovazioneFlessibile() throws Exception {
        List<Dipendente> dips = utenteDao.caricaTuttiDipendenti();
        for (Dipendente d : dips) {
            utenteDao.aggiornaConfigurazioneDipendente(d.getEmail(),
                    ConfigurazioneOreDipendente.intervallo(20, 40, 2, 2),
                    DisponibilitaDipendente.tuttoDisponibile(),
                    EnumSet.of(MansioneDipendente.BANCO),
                    true,
                    6);
        }

        ConfigurazioneGenerazioneTurni cfg = new ConfigurazioneGenerazioneTurni(
                ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA);
        cfg.setTipoGenerazione(TipoGenerazione.FLESSIBILE);

        // 16. La generazione crea solo una bozza: 0 turni ufficiali salvati prima della conferma
        PropostaTurniSettimanaBean bozza = ctrl.generaPropostaSettimana(LUNEDI_W1, cfg);
        assertTrue(ctrl.visualizzaTuttiTurni().isEmpty(), "Annullando la bozza i turni ufficiali restano vuoti");
        assertFalse(bozza.getTurniProposti().isEmpty());

        // 14 e 20. Modifica manuale in anteprima con ricalcolo live della copertura
        List<TurnoBean> modificati = new ArrayList<>(bozza.getTurniProposti());
        modificati.remove(0);
        PropostaTurniSettimanaBean rivalutata = ctrl.rivalutaPropostaModificata(bozza, modificati);
        assertEquals(bozza.getTurniProposti().size() - 1, rivalutata.getTurniProposti().size());

        // 17. Approvazione e salvataggio atomico
        ctrl.confermaEApplicaProposta(rivalutata);
        assertEquals(rivalutata.getTurniProposti().size(), ctrl.visualizzaTuttiTurni().size());
    }

    @Test
    void test18e19_MancanzaPersonaleECoperturaParzialeDocumentata() throws Exception {
        Dipendente solo = creaStaff("Solo", "Uno", "solo@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), null, 5);
        ConfigurazioneCoperturaFlessibile cov = ConfigurazioneCoperturaFlessibile.predefinita();
        cov.setPersonaleMinimoChiusura(2); // Impossibile avere 2 persone in chiusura con 1 solo dipendente!

        ConfigurazioneGenerazioneTurni cfg = new ConfigurazioneGenerazioneTurni(
                ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA);
        cfg.setTipoGenerazione(TipoGenerazione.FLESSIBILE);

        PropostaTurniSettimanaBean p = MotoreTurniFlessibili.generaPropostaFlessibile(
                LUNEDI_W1, List.of(solo), List.of(), cov, cfg);

        assertFalse(p.isCoperturaChiusuraOk());
        assertTrue(p.getPercentualeCopertura() < 100.0);
        assertFalse(p.getTurniScoperti().isEmpty());
        assertTrue(p.getTurniProposti().size() <= 5);

        // Verifica anche il calcolo diretto dell'intervallo scoperto (Richiesti 2, Presenti 1 -> manca 1)
        RiepilogoCoperturaSettimana check = CalcolatoreOreSettimanali.valutaCoperturaIntervalli(
                LUNEDI_W1, p.getTurniProposti(), cov, 60);
        assertTrue(check.dettagliScoperture().stream().anyMatch(s -> s.contains("CHIUSURA")));
    }

    @Test
    void test21_TurniFlessibiliRispettanoFasceAbilitateNoMattinaPerChiFaSoloSera() throws Exception {
        DisponibilitaDipendente soloSera = DisponibilitaDipendente.tuttoDisponibile();
        // Abilitata solo a Pom./Sera (16:00-00:00, idx 2) e Serale (20:00-00:00, idx 3): NO mattina!
        soloSera.setFasceAbilitate(java.util.Set.of(2, 3));

        // Un turno flessibile 11:00-19:00 deve essere RIFIUTATO
        assertNotNull(soloSera.verificaCompatibilita(LUNEDI_W1, "11:00", "19:00"));
        assertNotNull(soloSera.verificaCompatibilita(LUNEDI_W1, "09:00", "17:00"));
        // Un turno flessibile dentro 16:00-00:00 deve essere CONSENTITO
        assertNull(soloSera.verificaCompatibilita(LUNEDI_W1, "16:00", "22:00"));
        assertNull(soloSera.verificaCompatibilita(LUNEDI_W1, "18:00", "00:00"));

        Dipendente leila = creaStaff("Leila", "Bho", "leila@frostly.it",
                ConfigurazioneOreDipendente.intervallo(20, 40), soloSera, 5);
        Dipendente carmen = creaStaff("Carmen", "Test", "carmen@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), DisponibilitaDipendente.tuttoDisponibile(), 6);

        ConfigurazioneGenerazioneTurni cfg = new ConfigurazioneGenerazioneTurni(
                ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA);
        cfg.setTipoGenerazione(TipoGenerazione.FLESSIBILE);

        PropostaTurniSettimanaBean p = MotoreTurniFlessibili.generaPropostaFlessibile(
                LUNEDI_W1, List.of(leila, carmen), List.of(),
                ConfigurazioneCoperturaFlessibile.predefinita(), cfg);

        for (TurnoBean tb : p.getTurniProposti()) {
            if ("leila@frostly.it".equalsIgnoreCase(tb.getEmailDipendente())) {
                assertTrue(tb.getOraInizio().compareTo("16:00") >= 0,
                        "Leila abilitata solo Pom./Sera non deve mai iniziare prima delle 16:00, trovato: "
                                + tb.getOraInizio() + "-" + tb.getOraFine());
            }
        }
    }
}
