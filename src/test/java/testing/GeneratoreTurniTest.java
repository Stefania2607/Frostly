package testing;

import bean.PropostaTurniSettimanaBean;
import bean.RiepilogoOreDipendenteBean;
import bean.RiepilogoOreDipendenteBean.StatoMonteOre;
import bean.TurnoBean;
import control.applicativo.CalcolatoreOreSettimanali;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.MotorePianificazioneTurni;
import dao.ArchivioMemoria;
import dao.TurnoDAOMemory;
import dao.memoria.EmergenzaDAOMemory;
import dao.memoria.NotificaDAOMemory;
import dao.memoria.UtenteDAOMemory;
import frostly.exception.FrostlyException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.ConfigurazioneGenerazioneTurni;
import model.ConfigurazioneGenerazioneTurni.ModalitaGenerazione;
import model.ConfigurazioneGenerazioneTurni.PrioritaOttimizzazione;
import model.ConfigurazioneOreDipendente;
import model.Dipendente;
import model.DisponibilitaDipendente;
import model.FabbisognoTurno;
import model.MansioneDipendente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite JUnit 5 dedicata al Generatore Intelligente di Turni Staff Frostly.
 * Verifica tutti gli scenari obbligatori: pianificazione manuale, automatica, mista,
 * turni bloccati, indisponibilita', tolleranze, mansioni, sovrapposizioni, mezzanotte,
 * settimane differenti, modifica/rigenerazione/annullamento proposta, persistenza e ripristino.
 */
class GeneratoreTurniTest {

    private static final LocalDate LUNEDI_W1 = LocalDate.of(2026, 11, 23);
    private static final LocalDate LUNEDI_W2 = LocalDate.of(2026, 11, 30);

    private GestioneTurniEmergenzeControl ctrl;
    private UtenteDAOMemory utenteDao;
    private TurnoDAOMemory turnoDao;

    @BeforeEach
    void setup() {
        ArchivioMemoria demo = ArchivioMemoria.getDemoInstance();
        for (model.Turno t : new ArrayList<>(demo.getTurni())) {
            demo.eliminaTurno(t.getCodiceTurno());
        }
        utenteDao = new UtenteDAOMemory();
        turnoDao = new TurnoDAOMemory();
        ctrl = new GestioneTurniEmergenzeControl(turnoDao, new EmergenzaDAOMemory(), utenteDao, new NotificaDAOMemory());
    }

    private Dipendente creaDipendente(String nome, String cognome, String email,
                                      ConfigurazioneOreDipendente cfg,
                                      DisponibilitaDipendente disp,
                                      EnumSet<MansioneDipendente> mansioni) {
        Dipendente d = new Dipendente(nome, cognome, email, "pass123");
        d.setConfigurazioneOre(cfg);
        d.setDisponibilita(disp != null ? disp : DisponibilitaDipendente.tuttoDisponibile());
        d.setMansioni(mansioni != null ? mansioni : EnumSet.of(MansioneDipendente.BANCO));
        return d;
    }

    private Map<String, FabbisognoTurno> fabbisogniSemplici(int perFasciaFeriale) {
        Map<String, FabbisognoTurno> map = new LinkedHashMap<>();
        for (DayOfWeek g : DayOfWeek.values()) {
            for (int f = 0; f < 5; f++) {
                int tot = (f < 2) ? perFasciaFeriale : 0;
                FabbisognoTurno fab = new FabbisognoTurno(g, f, tot);
                if (tot > 0) {
                    fab.setMinimoMansione(MansioneDipendente.BANCO, 1);
                }
                map.put(FabbisognoTurno.chiave(g, f), fab);
            }
        }
        return map;
    }

    @Test
    void test01e20_PianificazioneCompletamenteManualeContinuaAFunzionare() throws Exception {
        List<Dipendente> dips = utenteDao.caricaTuttiDipendenti();
        assertFalse(dips.isEmpty());
        String email = dips.get(0).getEmail();

        TurnoBean manuale = new TurnoBean();
        manuale.setEmailDipendente(email);
        manuale.setData("Lun 23/11/2026");
        manuale.setOraInizio("07:00");
        manuale.setOraFine("14:00");
        TurnoBean salvato = ctrl.pubblicaNuovoTurno(manuale);

        assertNotNull(salvato.getCodiceTurno());
        assertFalse(salvato.isGeneratoAutomaticamente());
        assertEquals(7.0, CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                email, ctrl.visualizzaTuttiTurni(), LUNEDI_W1), 0.01);
    }

    @Test
    void test02_GenerazioneAutomaticaSettimanaVuota() throws Exception {
        List<Dipendente> staff = List.of(
                creaDipendente("Maria", "Rossi", "maria@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO)),
                creaDipendente("Luca", "Bianchi", "luca@frostly.it", ConfigurazioneOreDipendente.intervallo(20, 35), null, EnumSet.of(MansioneDipendente.BANCO)),
                creaDipendente("Giulia", "Verdi", "giulia@frostly.it", ConfigurazioneOreDipendente.intervallo(20, 35), null, EnumSet.of(MansioneDipendente.BANCO))
        );

        PropostaTurniSettimanaBean proposta = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, staff, List.of(), fabbisogniSemplici(1),
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        assertEquals(100.0, proposta.getPercentualeCopertura(), 0.01);
        assertEquals(14, proposta.getTurniProposti().size());
        assertTrue(proposta.getTurniScoperti().isEmpty());
    }

    @Test
    void test03_CompletamentoSettimanaParzialmenteCompilata() throws Exception {
        List<Dipendente> staff = List.of(
                creaDipendente("Maria", "Rossi", "maria@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), null, null),
                creaDipendente("Luca", "Bianchi", "luca@frostly.it", ConfigurazioneOreDipendente.oreFisse(35), null, null),
                creaDipendente("Sara", "Neri", "sara@frostly.it", ConfigurazioneOreDipendente.intervallo(20, 35), null, null)
        );
        TurnoBean esistente = new TurnoBean();
        esistente.setCodiceTurno("TUR-001");
        esistente.setEmailDipendente("maria@frostly.it");
        esistente.setNomeDipendente("Maria Rossi");
        esistente.setData("Lun 23/11/2026");
        esistente.setOraInizio("07:00");
        esistente.setOraFine("14:00");

        PropostaTurniSettimanaBean proposta = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, staff, List.of(esistente), fabbisogniSemplici(1),
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.SOLO_SCOPERTI, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        assertEquals(100.0, proposta.getPercentualeCopertura(), 0.01);
        assertEquals(1, proposta.getTurniMantenuti());
        assertEquals(13, proposta.getTurniAggiunti());
        assertTrue(proposta.getTurniProposti().stream()
                .anyMatch(t -> "maria@frostly.it".equals(t.getEmailDipendente())
                        && "Lun 23/11/2026".equals(t.getData()) && "07:00".equals(t.getOraInizio())));
    }

    @Test
    void test04_RispettoTurniBloccatiEConflittoSegnalato() throws Exception {
        DisponibilitaDipendente dispMaria = new DisponibilitaDipendente();
        dispMaria.impostaGiorno(DayOfWeek.SATURDAY, DisponibilitaDipendente.NON_DISPONIBILE);

        List<Dipendente> staff = List.of(
                creaDipendente("Maria", "Rossi", "maria@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), dispMaria, null),
                creaDipendente("Luca", "Bianchi", "luca@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), null, null)
        );

        TurnoBean bloccato = new TurnoBean();
        bloccato.setCodiceTurno("TUR-LOCK");
        bloccato.setEmailDipendente("maria@frostly.it");
        bloccato.setNomeDipendente("Maria Rossi");
        bloccato.setData("Sab 28/11/2026");
        bloccato.setOraInizio("07:00");
        bloccato.setOraFine("14:00");
        bloccato.setBloccato(true);

        PropostaTurniSettimanaBean proposta = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, staff, List.of(bloccato), fabbisogniSemplici(1),
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.RIGENERA_NON_BLOCCATI, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        // Il turno bloccato NON viene mai spostato o eliminato, ma il conflitto viene segnalato
        assertTrue(proposta.getTurniProposti().stream()
                .anyMatch(t -> "TUR-LOCK".equals(t.getCodiceTurno()) && t.isBloccato()));
        assertTrue(proposta.getAvvisi().stream().anyMatch(a -> a.contains("Conflitto su turno bloccato")));
    }

    @Test
    void test05_RispettoIndisponibilitaEFeriePermessi() throws Exception {
        DisponibilitaDipendente disp = new DisponibilitaDipendente();
        disp.impostaGiorno(DayOfWeek.WEDNESDAY, DisponibilitaDipendente.NON_DISPONIBILE);
        disp.impostaGiorno(DayOfWeek.THURSDAY, DisponibilitaDipendente.POMERIGGIO);
        disp.aggiungiFeriePermesso(LocalDate.of(2026, 11, 24)); // Martedi' in ferie

        List<Dipendente> staff = List.of(
                creaDipendente("Maria", "Rossi", "maria@frostly.it", ConfigurazioneOreDipendente.oreFisse(35), disp, null),
                creaDipendente("Luca", "Bianchi", "luca@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), null, null)
        );

        PropostaTurniSettimanaBean proposta = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, staff, List.of(), fabbisogniSemplici(1),
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        for (TurnoBean t : proposta.getTurniProposti()) {
            if ("maria@frostly.it".equals(t.getEmailDipendente())) {
                LocalDate dt = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), LUNEDI_W1);
                assertNotEquals(LocalDate.of(2026, 11, 24), dt, "Non deve lavorare in ferie");
                assertNotEquals(DayOfWeek.WEDNESDAY, dt.getDayOfWeek(), "Non deve lavorare di mercoledi'");
                if (DayOfWeek.THURSDAY.equals(dt.getDayOfWeek())) {
                    assertTrue(t.getOraInizio().compareTo("14:00") >= 0, "Giovedi' solo dopo le 14:00");
                }
            }
        }
    }

    @Test
    void test06_07_08_MonteOreFissoIntervalloETolleranza() throws Exception {
        Dipendente maria = creaDipendente("Maria", "Rossi", "maria@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40, 3), null, null); // 40 +-3 -> 37..43
        Dipendente luca = creaDipendente("Luca", "Bianchi", "luca@frostly.it",
                ConfigurazioneOreDipendente.intervallo(20, 30, 2, 3), null, null); // 18..33

        List<TurnoBean> turni = new ArrayList<>();
        // Maria: 42 h (+2 rispetto a 40 -> ENTRO_TOLLERANZA)
        for (int i = 0; i < 6; i++) {
            TurnoBean t = new TurnoBean();
            t.setCodiceTurno("M-" + i);
            t.setEmailDipendente("maria@frostly.it");
            t.setData("Lun " + String.format("%02d/11/2026", 23 + i));
            t.setOraInizio("07:00");
            t.setOraFine("14:00"); // 6 * 7 = 42h
            turni.add(t);
        }
        // Luca: 24 h (dentro 20-30 -> NEL_RANGE)
        for (int i = 0; i < 4; i++) {
            TurnoBean t = new TurnoBean();
            t.setCodiceTurno("L-" + i);
            t.setEmailDipendente("luca@frostly.it");
            t.setData("Lun " + String.format("%02d/11/2026", 23 + i));
            t.setOraInizio("14:00");
            t.setOraFine("20:00"); // 4 * 6 = 24h
            turni.add(t);
        }

        List<RiepilogoOreDipendenteBean> riep = CalcolatoreOreSettimanali.calcolaRiepiloghiSettimana(
                List.of(maria, luca), turni, LUNEDI_W1);

        assertEquals(StatoMonteOre.ENTRO_TOLLERANZA, riep.get(0).getStato());
        assertFalse(riep.get(0).isFuoriTolleranza());
        assertEquals(2.0, riep.get(0).getScostamentoOre(), 0.01);

        assertEquals(StatoMonteOre.NEL_RANGE, riep.get(1).getStato());

        // Aggiungiamo altre 6h a Maria -> 48h (fuori tolleranza 43h!)
        TurnoBean extra = new TurnoBean();
        extra.setCodiceTurno("M-EX");
        extra.setEmailDipendente("maria@frostly.it");
        extra.setData("Dom 29/11/2026");
        extra.setOraInizio("14:00");
        extra.setOraFine("20:00");
        turni.add(extra);

        List<RiepilogoOreDipendenteBean> riep2 = CalcolatoreOreSettimanali.calcolaRiepiloghiSettimana(
                List.of(maria), turni, LUNEDI_W1);
        assertEquals(StatoMonteOre.OLTRE_MASSIMO, riep2.get(0).getStato());
        assertTrue(riep2.get(0).isFuoriTolleranza());
    }

    @Test
    void test09_MancanzaPersonaleSufficienteGestitaConCoperturaParzialeESuggerimenti() throws Exception {
        List<Dipendente> staff = List.of(
                creaDipendente("Solo", "Uno", "solo@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), null, null)
        );
        // Richiediamo 3 persone per ogni turno -> impossibile con 1 solo dipendente
        PropostaTurniSettimanaBean proposta = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, staff, List.of(), fabbisogniSemplici(3),
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        assertTrue(proposta.getPercentualeCopertura() < 100.0);
        assertFalse(proposta.getTurniScoperti().isEmpty());
        assertFalse(proposta.getSuggerimenti().isEmpty());
    }

    @Test
    void test10_CoperturaConRequisitiMansione() throws Exception {
        Dipendente banco = creaDipendente("Anna", "Banco", "anna@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO));
        Dipendente lab = creaDipendente("Marco", "Lab", "marco@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.LABORATORIO));

        Map<String, FabbisognoTurno> fab = new LinkedHashMap<>();
        for (DayOfWeek g : DayOfWeek.values()) {
            for (int f = 0; f < 5; f++) {
                fab.put(FabbisognoTurno.chiave(g, f), new FabbisognoTurno(g, f, 0));
            }
        }
        // Sabato pomeriggio: 2 dipendenti, almeno 1 Banco e almeno 1 Laboratorio
        FabbisognoTurno sabPom = new FabbisognoTurno(DayOfWeek.SATURDAY, 1, 2)
                .conMinimoMansione(MansioneDipendente.BANCO, 1)
                .conMinimoMansione(MansioneDipendente.LABORATORIO, 1);
        fab.put(FabbisognoTurno.chiave(DayOfWeek.SATURDAY, 1), sabPom);

        PropostaTurniSettimanaBean proposta = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, List.of(banco, lab), List.of(), fab,
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        assertEquals(100.0, proposta.getPercentualeCopertura(), 0.01);
        assertEquals(2, proposta.getTurniProposti().size());
        assertTrue(proposta.getTurniProposti().stream().anyMatch(t -> "anna@frostly.it".equals(t.getEmailDipendente())));
        assertTrue(proposta.getTurniProposti().stream().anyMatch(t -> "marco@frostly.it".equals(t.getEmailDipendente())));
    }

    @Test
    void test11_12_13_SovrapposizioniMezzanotteESettimaneDifferenti() throws Exception {
        // Turno notturno 20:00 -> 02:00 di domenica 29/11 attraversa la mezzanotte verso lunedi' 30/11
        TurnoBean notturno = new TurnoBean();
        notturno.setCodiceTurno("TUR-NIGHT");
        notturno.setEmailDipendente("maria@frostly.it");
        notturno.setData("Dom 29/11/2026");
        notturno.setOraInizio("20:00");
        notturno.setOraFine("02:00");

        assertEquals(4.0, CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(notturno, LUNEDI_W1), 0.01);
        assertEquals(2.0, CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(notturno, LUNEDI_W2), 0.01);

        // Conflitto / riposo insufficiente: finisce alle 02:00 di lunedi', mattina inizia alle 07:00 (solo 5h di riposo < 11h)
        assertTrue(CalcolatoreOreSettimanali.isConflittoORiposoInsufficiente(
                LocalDate.of(2026, 11, 29), "20:00", "02:00",
                LocalDate.of(2026, 11, 30), "07:00", "14:00", 11));
        // Invece pomeriggio inizia alle 14:00 (12h di riposo >= 11h) -> ammesso!
        assertFalse(CalcolatoreOreSettimanali.isConflittoORiposoInsufficiente(
                LocalDate.of(2026, 11, 29), "20:00", "02:00",
                LocalDate.of(2026, 11, 30), "14:00", "20:00", 11));
    }

    @Test
    void test14_15_16_17_18_19_FlussoAnteprimaModificaRigeneraAnnullaConfermaRipristino() throws Exception {
        // Configura monte ore sui dipendenti demo in ArchivioMemoria
        List<Dipendente> dips = utenteDao.caricaTuttiDipendenti();
        for (Dipendente d : dips) {
            utenteDao.aggiornaConfigurazioneDipendente(d.getEmail(),
                    ConfigurazioneOreDipendente.intervallo(20, 40, 2, 2),
                    DisponibilitaDipendente.tuttoDisponibile(),
                    EnumSet.of(MansioneDipendente.BANCO, MansioneDipendente.LABORATORIO),
                    true);
        }
        // Inserisci un turno in W2 per verificare che le altre settimane siano preservate
        TurnoBean turnoW2 = new TurnoBean();
        turnoW2.setEmailDipendente(dips.get(0).getEmail());
        turnoW2.setData("Lun 30/11/2026");
        turnoW2.setOraInizio("07:00");
        turnoW2.setOraFine("14:00");
        ctrl.pubblicaNuovoTurno(turnoW2);
        int countPrima = ctrl.visualizzaTuttiTurni().size();

        // 16. Genera proposta (anteprima): NON salva nulla finche' non confermi (equivalente ad Annulla)
        ConfigurazioneGenerazioneTurni cfg = new ConfigurazioneGenerazioneTurni(
                ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA);
        PropostaTurniSettimanaBean proposta1 = ctrl.generaPropostaSettimana(LUNEDI_W1, cfg);
        assertEquals(countPrima, ctrl.visualizzaTuttiTurni().size(), "La generazione non deve salvare prima della conferma");

        // 14. Modifica manuale della proposta + rivalutazione live
        List<TurnoBean> modificati = new ArrayList<>(proposta1.getTurniProposti());
        assertFalse(modificati.isEmpty());
        modificati.remove(0);
        PropostaTurniSettimanaBean rivalutata = ctrl.rivalutaPropostaModificata(proposta1, modificati);
        assertEquals(proposta1.getTurniProposti().size() - 1, rivalutata.getTurniProposti().size());

        // 15. Rigenerazione con nuovo seed
        PropostaTurniSettimanaBean proposta2 = ctrl.generaPropostaSettimana(LUNEDI_W1, cfg.conNuovoSeed());
        assertFalse(proposta2.getTurniProposti().isEmpty());

        // 17. Conferma e persistenza atomica
        ctrl.confermaEApplicaProposta(proposta2);
        List<TurnoBean> dopoConferma = ctrl.visualizzaTuttiTurni();
        assertTrue(dopoConferma.size() > countPrima);

        // 19. Verifica conservazione pianificazione della settimana W2
        assertTrue(dopoConferma.stream().anyMatch(t -> "Lun 30/11/2026".equals(t.getData())));

        // 18. Verifica ripristino pianificazione precedente
        assertTrue(ctrl.haSnapshotRipristinabile(LUNEDI_W1));
        ctrl.ripristinaUltimaPianificazione(LUNEDI_W1);
        assertEquals(countPrima, ctrl.visualizzaTuttiTurni().size());
    }

    @Test
    void testControlloVersioneControModificheConcorrenti() throws Exception {
        List<Dipendente> dips = utenteDao.caricaTuttiDipendenti();
        for (Dipendente d : dips) {
            utenteDao.aggiornaConfigurazioneDipendente(d.getEmail(),
                    ConfigurazioneOreDipendente.oreFisse(40),
                    DisponibilitaDipendente.tuttoDisponibile(),
                    EnumSet.of(MansioneDipendente.BANCO),
                    true);
        }
        PropostaTurniSettimanaBean proposta = ctrl.generaPropostaSettimana(LUNEDI_W1, new ConfigurazioneGenerazioneTurni());

        // Nel frattempo un altro operatore aggiunge un turno manuale in W1
        TurnoBean concorrente = new TurnoBean();
        concorrente.setEmailDipendente(dips.get(0).getEmail());
        concorrente.setData("Lun 23/11/2026");
        concorrente.setOraInizio("07:00");
        concorrente.setOraFine("14:00");
        ctrl.pubblicaNuovoTurno(concorrente);

        // La conferma della vecchia proposta deve essere bloccata per evitare sovrascritture accidentali
        assertThrows(FrostlyException.class, () -> ctrl.confermaEApplicaProposta(proposta));
    }

    @Test
    void testSalvataggioCorrispondeEsattamenteAllAnteprimaSenzaCollisioniCodici() throws Exception {
        List<Dipendente> dips = utenteDao.caricaTuttiDipendenti();
        for (Dipendente d : dips) {
            utenteDao.aggiornaConfigurazioneDipendente(d.getEmail(),
                    ConfigurazioneOreDipendente.intervallo(20, 42, 2, 2),
                    DisponibilitaDipendente.tuttoDisponibile(),
                    EnumSet.of(MansioneDipendente.BANCO, MansioneDipendente.LABORATORIO),
                    true);
        }
        // Inserisci 2 turni esistenti in W1 (di cui 1 bloccato) prima della generazione SOLO_SCOPERTI
        TurnoBean t1 = new TurnoBean();
        t1.setEmailDipendente(dips.get(0).getEmail());
        t1.setData("Lun 23/11/2026");
        t1.setOraInizio("07:00");
        t1.setOraFine("14:00");
        t1.setBloccato(true);
        ctrl.pubblicaNuovoTurno(t1);

        TurnoBean t2 = new TurnoBean();
        t2.setEmailDipendente(dips.get(1 % dips.size()).getEmail());
        t2.setData("Mar 24/11/2026");
        t2.setOraInizio("14:00");
        t2.setOraFine("20:00");
        ctrl.pubblicaNuovoTurno(t2);

        PropostaTurniSettimanaBean proposta = ctrl.generaPropostaSettimana(
                LUNEDI_W1,
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.SOLO_SCOPERTI, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        java.util.Set<String> firmeAnteprima = new java.util.HashSet<>();
        for (TurnoBean tb : proposta.getTurniProposti()) {
            firmeAnteprima.add((tb.getData() + "|" + tb.getOraInizio() + "|" + tb.getOraFine() + "|" + tb.getEmailDipendente()).toLowerCase());
        }

        ctrl.confermaEApplicaProposta(proposta);

        List<TurnoBean> salvati = ctrl.visualizzaTuttiTurni();
        java.util.Set<String> firmeSalvate = new java.util.HashSet<>();
        java.util.Set<String> codiciSalvati = new java.util.HashSet<>();
        for (TurnoBean tb : salvati) {
            firmeSalvate.add((tb.getData() + "|" + tb.getOraInizio() + "|" + tb.getOraFine() + "|" + tb.getEmailDipendente()).toLowerCase());
            assertTrue(codiciSalvati.add(tb.getCodiceTurno()), "Codice turno duplicato: " + tb.getCodiceTurno());
        }

        assertEquals(firmeAnteprima.size(), firmeSalvate.size(), "Numero di turni salvati diverso dall'anteprima");
        assertEquals(firmeAnteprima, firmeSalvate, "I turni salvati devono coincidere esattamente con quelli mostrati in anteprima");
    }

    @Test
    void testDistribuzioneEquaSu6DipendentiConMonteOreDiversi() throws Exception {
        List<Dipendente> staff = List.of(
                creaDipendente("Carmen", "CC", "carmen@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO)),
                creaDipendente("Jessica", "Iannone", "jessica@frostly.it", ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO)),
                creaDipendente("Leila", "Bho", "leila@frostly.it", ConfigurazioneOreDipendente.oreFisse(35), null, EnumSet.of(MansioneDipendente.BANCO)),
                creaDipendente("Martina", "Sorrentino", "martina@frostly.it", ConfigurazioneOreDipendente.intervallo(30, 40), null, EnumSet.of(MansioneDipendente.BANCO)),
                creaDipendente("Stefania", "Guga", "stefania@frostly.it", ConfigurazioneOreDipendente.oreFisse(25), null, EnumSet.of(MansioneDipendente.BANCO)),
                creaDipendente("Virginia", "Rossi", "virginia@frostly.it", ConfigurazioneOreDipendente.intervallo(20, 30), null, EnumSet.of(MansioneDipendente.BANCO))
        );

        PropostaTurniSettimanaBean p = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, staff, List.of(), null,
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.RIGENERA_NON_BLOCCATI, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        assertEquals(100.0, p.getPercentualeCopertura(), 0.01);
        for (RiepilogoOreDipendenteBean r : p.getRiepiloghiDipendenti()) {
            assertTrue(r.getOreAssegnate() >= 20.0,
                    "Nessun dipendente deve restare con poche ore: " + r.getNomeCompleto() + " ha " + r.getOreAssegnate() + "h");
            assertTrue(r.getOreAssegnate() <= r.getConfigurazioneOre().getOreMassime() + 4.0,
                    "Nessun dipendente deve avere sforamenti eccessivi: " + r.getNomeCompleto() + " ha " + r.getOreAssegnate() + "h");
        }
    }

    @Test
    void testRichiestaIndisponibilitaDipendenteSoloDopoApprovazioneMaestro() throws Exception {
        List<Dipendente> dips = utenteDao.caricaTuttiDipendenti();
        for (Dipendente d : dips) {
            utenteDao.aggiornaConfigurazioneDipendente(d.getEmail(),
                    ConfigurazioneOreDipendente.intervallo(20, 40, 2, 2),
                    DisponibilitaDipendente.tuttoDisponibile(),
                    EnumSet.of(MansioneDipendente.BANCO),
                    true);
        }
        String emailDip = dips.get(0).getEmail();
        LocalDate mercoledi = LocalDate.of(2026, 11, 25);
        LocalDate giovedi = LocalDate.of(2026, 11, 26);

        // 1. Richiesta in attesa: NON blocca ancora il dipendente
        model.RichiestaIndisponibilita r1 = ctrl.inviaRichiestaIndisponibilita(emailDip, mercoledi, "Visita medica");
        assertEquals(model.RichiestaIndisponibilita.StatoRichiesta.IN_ATTESA, r1.getStato());
        assertFalse(utenteDao.trovaDipendentePerEmail(emailDip).getDisponibilita().getFeriePermessi().contains(mercoledi));

        // 2. Rifiuto Maestro: NON blocca il dipendente
        ctrl.valutaRichiestaIndisponibilita(r1.getCodiceRichiesta(), false, "Servizio scoperto");
        assertFalse(utenteDao.trovaDipendentePerEmail(emailDip).getDisponibilita().getFeriePermessi().contains(mercoledi));

        // 3. Nuova richiesta approvata: blocca automaticamente la data e il generatore la rispetta
        model.RichiestaIndisponibilita r2 = ctrl.inviaRichiestaIndisponibilita(emailDip, giovedi, "Esame universitario");
        ctrl.valutaRichiestaIndisponibilita(r2.getCodiceRichiesta(), true, "In bocca al lupo");
        assertTrue(utenteDao.trovaDipendentePerEmail(emailDip).getDisponibilita().getFeriePermessi().contains(giovedi));

        PropostaTurniSettimanaBean proposta = ctrl.generaPropostaSettimana(
                LUNEDI_W1,
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        for (TurnoBean t : proposta.getTurniProposti()) {
            if (emailDip.equalsIgnoreCase(t.getEmailDipendente())) {
                LocalDate dt = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), LUNEDI_W1);
                assertNotEquals(giovedi, dt, "Il dipendente con permesso approvato non deve essere messo in turno il " + giovedi);
            }
        }
    }

    @Test
    void testMaxTurniSettimanaliManualeAutomaticoConflittoStoricoEDisattivazione() throws Exception {
        // 1. Configura un dipendente con monte ore 40h e max 2 turni settimanali
        List<Dipendente> dips = utenteDao.caricaTuttiDipendenti();
        String email = dips.get(0).getEmail();
        utenteDao.aggiornaConfigurazioneDipendente(
                email,
                ConfigurazioneOreDipendente.oreFisse(40),
                DisponibilitaDipendente.tuttoDisponibile(),
                EnumSet.of(MansioneDipendente.BANCO),
                true,
                2);
        assertEquals(2, utenteDao.trovaDipendentePerEmail(email).getMaxTurniSettimanali());

        // 2. Inserimento manuale: i primi 2 turni vanno a buon fine
        TurnoBean t1 = new TurnoBean();
        t1.setEmailDipendente(email);
        t1.setData("Lun 23/11/2026");
        t1.setOraInizio("07:00");
        t1.setOraFine("14:00");
        ctrl.pubblicaNuovoTurno(t1);

        TurnoBean t2 = new TurnoBean();
        t2.setEmailDipendente(email);
        t2.setData("Mar 24/11/2026");
        t2.setOraInizio("07:00");
        t2.setOraFine("14:00");
        ctrl.pubblicaNuovoTurno(t2);

        // 3. Il 3° turno manuale nella stessa settimana DEVE essere bloccato senza cancellare i 2 esistenti
        TurnoBean t3 = new TurnoBean();
        t3.setEmailDipendente(email);
        t3.setData("Mer 25/11/2026");
        t3.setOraInizio("07:00");
        t3.setOraFine("14:00");
        assertThrows(frostly.exception.TurnoNonDisponibileException.class, () -> ctrl.pubblicaNuovoTurno(t3));
        assertEquals(2, ctrl.visualizzaTurniDipendente(email).size());

        // 4. Conflitto storico: abbassiamo il limite a 1 -> riepilogo segnala subito isOltreMaxTurni senza cancellare i 2 turni
        utenteDao.aggiornaConfigurazioneDipendente(
                email,
                ConfigurazioneOreDipendente.oreFisse(40),
                DisponibilitaDipendente.tuttoDisponibile(),
                EnumSet.of(MansioneDipendente.BANCO),
                true,
                1);
        List<RiepilogoOreDipendenteBean> riep = ctrl.calcolaRiepiloghiOreSettimana(LUNEDI_W1);
        RiepilogoOreDipendenteBean rDip = riep.stream()
                .filter(r -> email.equalsIgnoreCase(r.getEmail())).findFirst().orElseThrow();
        assertTrue(rDip.isOltreMaxTurni());
        assertEquals(2, ctrl.visualizzaTurniDipendente(email).size());

        // 5. Vincolo rigido nel generatore automatico: con 1 solo dipendente con maxTurni = 3, assegna al massimo 3 turni e segnala scoperture
        Dipendente solo = creaDipendente("Jessica", "Iannone", "jessica@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO));
        solo.setMaxTurniSettimanali(3);
        PropostaTurniSettimanaBean p = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, List.of(solo), List.of(), fabbisogniSemplici(1),
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));
        assertEquals(3, p.getTurniProposti().size(), "Il generatore non deve mai superare maxTurniSettimanali");
        assertFalse(p.getTurniScoperti().isEmpty());

        // 6. Disattivazione del limite (null) e disattivazione sicura account preservando turni storici
        utenteDao.aggiornaConfigurazioneDipendente(
                email,
                ConfigurazioneOreDipendente.intervallo(20, 30),
                DisponibilitaDipendente.tuttoDisponibile(),
                EnumSet.of(MansioneDipendente.BANCO),
                false,
                null);
        Dipendente aggiornato = utenteDao.trovaDipendentePerEmail(email);
        assertFalse(aggiornato.hasLimiteMaxTurni());
        assertFalse(aggiornato.isAttivo());
        assertEquals(2, ctrl.visualizzaTurniDipendente(email).size(), "Disattivare l'account preserva i turni storici");
    }

    @Test
    void testNuovoAssuntoSoloChiusureEMaxUnTurnoDa8Ore() throws Exception {
        // Nuovo assunto: niente mattina, niente pomeriggio pieno (14-20), abilitato solo a Pom./Sera (16-00, 8h)
        // e Serale (20-00, 4h), con massimo 1 turno lungo da 8h a settimana
        DisponibilitaDipendente dispJunior = new DisponibilitaDipendente();
        dispJunior.setFasceAbilitate(java.util.Set.of(2, 3)); // solo 16-00 e 20-00
        dispJunior.setMaxTurniLunghiSettimana(1);

        // Verifica serializzazione / deserializzazione persistente
        DisponibilitaDipendente ricaricata = DisponibilitaDipendente.deserializza(dispJunior.serializza());
        assertEquals(java.util.Set.of(2, 3), ricaricata.getFasceAbilitate());
        assertEquals(1, ricaricata.getMaxTurniLunghiSettimana());

        Dipendente junior = creaDipendente("Nuovo", "Assunto", "junior@frostly.it",
                ConfigurazioneOreDipendente.intervallo(12, 24), ricaricata, EnumSet.of(MansioneDipendente.BANCO));
        Dipendente senior1 = creaDipendente("Senior", "Uno", "s1@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO));
        Dipendente senior2 = creaDipendente("Senior", "Due", "s2@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO));
        Dipendente senior3 = creaDipendente("Senior", "Tre", "s3@frostly.it",
                ConfigurazioneOreDipendente.oreFisse(40), null, EnumSet.of(MansioneDipendente.BANCO));

        PropostaTurniSettimanaBean p = MotorePianificazioneTurni.generaProposta(
                LUNEDI_W1, List.of(junior, senior1, senior2, senior3), List.of(), null,
                new ConfigurazioneGenerazioneTurni(ModalitaGenerazione.INTERA_SETTIMANA, PrioritaOttimizzazione.COPERTURA_PRIORITARIA));

        int turniDa8Junior = 0;
        for (TurnoBean t : p.getTurniProposti()) {
            if ("junior@frostly.it".equalsIgnoreCase(t.getEmailDipendente())) {
                int idx = DisponibilitaDipendente.identificaIndiceFascia(t.getOraInizio(), t.getOraFine());
                assertTrue(idx == 2 || idx == 3, "Il nuovo assunto non deve mai fare mattina o pomeriggio pieno (fascia=" + idx + ")");
                if (CalcolatoreOreSettimanali.calcolaDurataOre(t.getOraInizio(), t.getOraFine()) >= 7.99) {
                    turniDa8Junior++;
                }
            }
        }
        assertTrue(turniDa8Junior <= 1, "Il nuovo assunto deve fare al massimo 1 turno da 8 h, trovati: " + turniDa8Junior);
    }
}
