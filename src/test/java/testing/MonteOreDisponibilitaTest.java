package testing;

import static org.junit.jupiter.api.Assertions.*;

import bean.LoginBean;
import bean.RiepilogoOreDipendenteBean;
import bean.RiepilogoOreDipendenteBean.StatoMonteOre;
import bean.TurnoBean;
import control.applicativo.CalcolatoreOreSettimanali;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.ConfigurazioneOreDipendente;
import model.Dipendente;
import model.DisponibilitaDipendente;
import model.ModalitaMonteOre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Suite di test automatizzati per i 12 scenari obbligatori su Monte Ore,
 * Disponibilità Staff e Conteggio Automatico Settimanale.
 */
class MonteOreDisponibilitaTest {

    private static final LocalDate LUNEDI_SETT1 = LocalDate.of(2026, 11, 23);
    private static final LocalDate LUNEDI_SETT2 = LocalDate.of(2026, 11, 30);

    @BeforeEach
    void setupDemo() {
        SessionManager.getInstance().setModalita(ModalitaEsecuzione.DEMO);
        SessionManager.getInstance().impostaSessione("maestro@frostly.it", LoginControl.RUOLO_MAESTRO, "Stefania Guga");
    }

    private TurnoBean creaTurno(String cod, String email, String data, String inizio, String fine, String stato)
            throws DatiNonValidiException {
        TurnoBean t = new TurnoBean();
        t.setCodiceTurno(cod);
        t.setEmailDipendente(email);
        t.setData(data);
        t.setOraInizio(inizio);
        t.setOraFine(fine);
        t.setStato(stato);
        return t;
    }

    @Test
    void test01DipendenteObiettivoFisso40Ore() throws FrostlyException {
        Dipendente d = new Dipendente("Mario", "Rossi", "mario.t1@frostly.it", "pass1234");
        d.setConfigurazioneOre(ConfigurazioneOreDipendente.oreFisse(40));

        List<TurnoBean> turni = new ArrayList<>();
        turni.add(creaTurno("T-01", d.getEmail(), "Lun 23/11/2026", "08:00", "16:00", "PUBBLICATO"));
        turni.add(creaTurno("T-02", d.getEmail(), "Mar 24/11/2026", "08:00", "16:00", "PUBBLICATO"));
        turni.add(creaTurno("T-03", d.getEmail(), "Mer 25/11/2026", "08:00", "14:00", "PUBBLICATO"));

        RiepilogoOreDipendenteBean r1 = CalcolatoreOreSettimanali
                .calcolaRiepiloghiSettimana(List.of(d), turni, LUNEDI_SETT1).get(0);
        assertEquals(22.0, r1.getOreAssegnate(), 0.01);
        assertEquals(StatoMonteOre.SOTTO_MINIMO, r1.getStato());

        turni.add(creaTurno("T-04", d.getEmail(), "Gio 26/11/2026", "08:00", "18:00", "PUBBLICATO"));
        turni.add(creaTurno("T-05", d.getEmail(), "Ven 27/11/2026", "08:00", "16:00", "PUBBLICATO"));

        RiepilogoOreDipendenteBean r2 = CalcolatoreOreSettimanali
                .calcolaRiepiloghiSettimana(List.of(d), turni, LUNEDI_SETT1).get(0);
        assertEquals(40.0, r2.getOreAssegnate(), 0.01);
        assertEquals(StatoMonteOre.NEL_RANGE, r2.getStato());
    }

    @Test
    void test02DipendenteIntervallo20_30OreConFrazionarie() throws FrostlyException {
        Dipendente d = new Dipendente("Giulia", "Verdi", "giulia.t2@frostly.it", "pass1234");
        d.setConfigurazioneOre(ConfigurazioneOreDipendente.intervallo(20.0, 30.0));

        List<TurnoBean> turni = List.of(
                creaTurno("T-10", d.getEmail(), "Lun 23/11/2026", "08:00", "15:30", "PUBBLICATO"),
                creaTurno("T-11", d.getEmail(), "Mar 24/11/2026", "08:00", "15:30", "PUBBLICATO"),
                creaTurno("T-12", d.getEmail(), "Mer 25/11/2026", "08:00", "15:30", "PUBBLICATO")
        );

        RiepilogoOreDipendenteBean r = CalcolatoreOreSettimanali
                .calcolaRiepiloghiSettimana(List.of(d), turni, LUNEDI_SETT1).get(0);
        assertEquals(22.5, r.getOreAssegnate(), 0.01);
        assertEquals(StatoMonteOre.NEL_RANGE, r.getStato());
    }

    @Test
    void test03InserimentoPiuTurniEDeduplicazione() throws FrostlyException {
        Dipendente d = new Dipendente("Luca", "Neri", "luca.t3@frostly.it", "pass1234");
        d.setConfigurazioneOre(ConfigurazioneOreDipendente.oreFisse(30));

        TurnoBean t1 = creaTurno("T-DUP", d.getEmail(), "Lun 23/11/2026", "08:00", "14:00", "PUBBLICATO");
        TurnoBean t1Copia = creaTurno("T-DUP", d.getEmail(), "Lun 23/11/2026", "08:00", "14:00", "PUBBLICATO");
        TurnoBean t2 = creaTurno("T-OK2", d.getEmail(), "Mar 24/11/2026", "14:00", "20:00", "PUBBLICATO");
        TurnoBean tAnnullato = creaTurno("T-ANN", d.getEmail(), "Mer 25/11/2026", "08:00", "16:00", "ANNULLATO");

        double ore = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                d.getEmail(), List.of(t1, t1Copia, t2, tAnnullato), LUNEDI_SETT1);
        assertEquals(12.0, ore, 0.01);
    }

    @Test
    void test04EliminazioneTurnoAggiornaConteggio() throws FrostlyException {
        GestioneTurniEmergenzeControl ctrl = new GestioneTurniEmergenzeControl();
        List<String> emails = ctrl.getEmailTuttiDipendenti();
        assertFalse(emails.isEmpty());
        String email = emails.get(0);

        double prima = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                email, ctrl.visualizzaTuttiTurni(), LUNEDI_SETT2);

        TurnoBean nuovo = new TurnoBean();
        nuovo.setEmailDipendente(email);
        nuovo.setData("Lun 30/11/2026");
        nuovo.setOraInizio("07:00");
        nuovo.setOraFine("14:00");
        TurnoBean creato = ctrl.pubblicaNuovoTurno(nuovo);

        double dopoIns = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                email, ctrl.visualizzaTuttiTurni(), LUNEDI_SETT2);
        assertEquals(prima + 7.0, dopoIns, 0.01);

        ctrl.eliminaTurno(creato.getCodiceTurno());
        double dopoElim = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                email, ctrl.visualizzaTuttiTurni(), LUNEDI_SETT2);
        assertEquals(prima, dopoElim, 0.01);
    }

    @Test
    void test05ModificaOrariTurnoRicalcolaOre() throws FrostlyException {
        GestioneTurniEmergenzeControl ctrl = new GestioneTurniEmergenzeControl();
        String email = ctrl.getEmailTuttiDipendenti().get(0);

        TurnoBean nuovo = new TurnoBean();
        nuovo.setEmailDipendente(email);
        nuovo.setData("Mar 01/12/2026");
        nuovo.setOraInizio("08:00");
        nuovo.setOraFine("12:00");
        TurnoBean creato = ctrl.pubblicaNuovoTurno(nuovo);

        double primaMod = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                email, ctrl.visualizzaTuttiTurni(), LUNEDI_SETT2);
        ctrl.modificaOrariTurno(creato.getCodiceTurno(), "08:00", "15:00");

        double dopoMod = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                email, ctrl.visualizzaTuttiTurni(), LUNEDI_SETT2);
        assertEquals(primaMod + 3.0, dopoMod, 0.01);

        ctrl.eliminaTurno(creato.getCodiceTurno());
    }

    @Test
    void test06TurnoAttraversaMezzanotte() throws FrostlyException {
        assertEquals(4.0, CalcolatoreOreSettimanali.calcolaDurataOre("22:00", "02:00"), 0.01);
        TurnoBean notturno = creaTurno("T-NOTTE", "dip@frostly.it", "Ven 27/11/2026", "22:00", "02:00", "PUBBLICATO");
        assertEquals(4.0, CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(notturno, LUNEDI_SETT1), 0.01);
    }

    @Test
    void test07TurnoAttraversaConfineDueSettimane() throws FrostlyException {
        // Domenica 29/11/2026 22:00 -> Lunedì 30/11/2026 02:00: 2h in settimana 1 e 2h in settimana 2
        TurnoBean cavallo = creaTurno("T-CAV", "dip@frostly.it", "Dom 29/11/2026", "22:00", "02:00", "PUBBLICATO");
        assertEquals(2.0, CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(cavallo, LUNEDI_SETT1), 0.01);
        assertEquals(2.0, CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(cavallo, LUNEDI_SETT2), 0.01);
    }

    @Test
    void test08SuperamentoLimiteMassimoAnteprima() throws FrostlyException {
        Dipendente d = new Dipendente("Mario", "Rossi", "mario.t8@frostly.it", "pass1234");
        d.setConfigurazioneOre(ConfigurazioneOreDipendente.oreFisse(40));

        List<TurnoBean> turni = List.of(
                creaTurno("T-8A", d.getEmail(), "Lun 23/11/2026", "08:00", "20:00", "PUBBLICATO"),
                creaTurno("T-8B", d.getEmail(), "Mar 24/11/2026", "08:00", "20:00", "PUBBLICATO"),
                creaTurno("T-8C", d.getEmail(), "Mer 25/11/2026", "08:00", "20:00", "PUBBLICATO")
        );

        CalcolatoreOreSettimanali.AnteprimaAssegnazione ant = CalcolatoreOreSettimanali.calcolaAnteprimaAssegnazione(
                d, turni, LUNEDI_SETT1, LocalDate.of(2026, 11, 26), "08:00", "16:00");
        assertEquals(36.0, ant.oreAttuali(), 0.01);
        assertEquals(8.0, ant.oreTurno(), 0.01);
        assertEquals(44.0, ant.oreRisultanti(), 0.01);
        assertTrue(ant.superaMassimo());
        assertEquals(4.0, ant.oreEccesso(), 0.01);
    }

    @Test
    void test09DipendenteNonDisponibileSegnalato() {
        DisponibilitaDipendente disp = new DisponibilitaDipendente();
        disp.impostaGiorno(DayOfWeek.SUNDAY, DisponibilitaDipendente.NON_DISPONIBILE);
        disp.impostaGiorno(DayOfWeek.SATURDAY, DisponibilitaDipendente.MATTINA);
        disp.aggiungiDataIndisponibile(LocalDate.of(2026, 11, 25));

        assertNotNull(disp.verificaCompatibilita(LocalDate.of(2026, 11, 29), "08:00", "14:00"));
        assertNotNull(disp.verificaCompatibilita(LocalDate.of(2026, 11, 28), "14:00", "20:00"));
        assertNull(disp.verificaCompatibilita(LocalDate.of(2026, 11, 28), "07:00", "14:00"));
        assertNotNull(disp.verificaCompatibilita(LocalDate.of(2026, 11, 25), "08:00", "12:00"));
    }

    @Test
    void test10CambioSettimanaIsolaConteggi() throws FrostlyException {
        TurnoBean tSett1 = creaTurno("T-S1", "dip@frostly.it", "Mer 25/11/2026", "08:00", "16:00", "PUBBLICATO");
        assertEquals(8.0, CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana("dip@frostly.it", List.of(tSett1), LUNEDI_SETT1), 0.01);
        assertEquals(0.0, CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana("dip@frostly.it", List.of(tSett1), LUNEDI_SETT2), 0.01);
    }

    @Test
    void test11PersistenzaConfigurazioneOreEDisponibilita() throws FrostlyException {
        LoginControl loginCtrl = new LoginControl();
        String emailTest = "persist." + System.currentTimeMillis() + "@frostly.it";
        LoginBean b = new LoginBean();
        b.setNome("Anna");
        b.setCognome("Bianchi");
        b.setEmail(emailTest);
        b.setPassword("pass1234");

        ConfigurazioneOreDipendente ore = ConfigurazioneOreDipendente.intervallo(18.5, 28.5);
        DisponibilitaDipendente disp = new DisponibilitaDipendente();
        disp.impostaGiorno(DayOfWeek.SUNDAY, DisponibilitaDipendente.NON_DISPONIBILE);
        disp.aggiungiDataIndisponibile(LocalDate.of(2026, 12, 25));

        loginCtrl.creaProfiloDipendente(b, ore, disp);

        Dipendente ricaricato = loginCtrl.trovaDipendentePerEmail(emailTest);
        assertNotNull(ricaricato);
        assertEquals(ModalitaMonteOre.INTERVALLO, ricaricato.getConfigurazioneOre().getModalita());
        assertEquals(18.5, ricaricato.getConfigurazioneOre().getOreMinime(), 0.01);
        assertEquals(28.5, ricaricato.getConfigurazioneOre().getOreMassime(), 0.01);

        // Verifica anche round-trip di serializzazione DB
        DisponibilitaDipendente deser = DisponibilitaDipendente.deserializza(ricaricato.getDisponibilita().serializza());
        assertEquals(DisponibilitaDipendente.NON_DISPONIBILE, deser.getFasciaGiorno(DayOfWeek.SUNDAY));
        assertTrue(deser.getDateNonDisponibili().contains(LocalDate.of(2026, 12, 25)));

        loginCtrl.eliminaProfiloDipendente(emailTest);
    }

    @Test
    void test12DipendentePreesistenteSenzaConfigurazioneEValidazioni() {
        Dipendente legacy = new Dipendente("Vecchio", "Staff", "legacy@frostly.it", "pass1234");
        assertFalse(legacy.getConfigurazioneOre().isConfigurato());

        RiepilogoOreDipendenteBean r = CalcolatoreOreSettimanali
                .calcolaRiepiloghiSettimana(List.of(legacy), List.of(), LUNEDI_SETT1).get(0);
        assertEquals(StatoMonteOre.CONFIGURAZIONE_RICHIESTA, r.getStato());
        assertEquals("Configurazione richiesta", r.getMessaggioStato());

        assertThrows(DatiNonValidiException.class, () -> ConfigurazioneOreDipendente.oreFisse(-5));
        assertThrows(DatiNonValidiException.class, () -> ConfigurazioneOreDipendente.intervallo(35, 20));
    }
}
