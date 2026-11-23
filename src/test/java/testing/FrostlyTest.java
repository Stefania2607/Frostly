package testing;

import bean.LoginBean;
import bean.OrdineBean;
import bean.RecensioneBean;
import bean.RichiestaGustoBean;
import bean.TurnoBean;
import bean.VaschettaItemBean;
import control.applicativo.GestioneMenuControl;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import control.applicativo.MagazzinoControl;
import control.applicativo.OrdiniControl;
import control.applicativo.RecensioniControl;
import control.applicativo.RichiestaGustoControl;
import control.grafico.GestoreCarrello;
import dao.ArchivioMemoria;
import dao.DAOFactory;
import dao.PagamentoDAO;
import dao.TurnoDAO;
import dao.TurnoDAODbms;
import dao.TurnoDAOFileSystem;
import dao.TurnoDAOMemory;
import dao.dbms.PagamentoDAODbms;
import dao.memoria.PagamentoDAOMemory;
import frostly.exception.CredenzialiErrateException;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import frostly.exception.IngredienteEsauritoException;
import frostly.exception.OperazioneNonAutorizzataException;
import frostly.exception.RisorsaNonTrovataException;
import frostly.exception.TurnoNonDisponibileException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.ModalitaEsecuzione;
import util.SessionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suite di test JUnit 5 per il progetto Frostly.
 */
class FrostlyTest {

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().setModalita(ModalitaEsecuzione.DEMO);
        DAOFactory.setUsaFileSystemPerTurni(false);
    }

    /**
     * Test Case 1 - Responsabile: Stefania Guga
     * Verifica l'autenticazione valida (Maestro Gelataio) e il blocco con eccezione
     * DatiNonValidiException su credenziali errate o email sintatticamente non valida.
     */
    @Test
    void testAutenticazioneEValidazioneBean() throws FrostlyException {
        LoginControl loginControl = new LoginControl();

        LoginBean beanValido = new LoginBean("maestro@frostly.it", "admin123");
        LoginBean autenticato = loginControl.autenticaUtente(beanValido);
        assertNotNull(autenticato);
        assertEquals(LoginControl.RUOLO_MAESTRO, autenticato.getRuoloAutenticato());

        LoginBean beanErrato = new LoginBean("maestro@frostly.it", "passwordSbagliata");
        assertThrows(CredenzialiErrateException.class, () -> loginControl.autenticaUtente(beanErrato));

        assertThrows(DatiNonValidiException.class, () -> new LoginBean("emailSenzaChiocciola", "admin123"));
    }

    /**
     * Test Case 2 - Responsabile: Stefania Guga
     * Verifica la Registrazione (Sign Up) di un nuovo utente e il successivo Login (Sign In).
     */
    @Test
    void testRegistrazioneNuovoUtenteELogin() throws FrostlyException {
        LoginControl loginControl = new LoginControl();

        LoginBean regBean = new LoginBean();
        regBean.setNome("Stefania");
        regBean.setCognome("Guga");
        regBean.setEmail("stefania.test@frostly.it");
        regBean.setPassword("stef123");
        regBean.setRuoloScelto(LoginControl.RUOLO_CLIENTE);

        LoginBean registrato = loginControl.registraUtente(regBean);
        assertEquals("Stefania Guga", registrato.getNomeCompleto());

        LoginBean loginDopoReg = loginControl.autenticaUtente(new LoginBean("stefania.test@frostly.it", "stef123"));
        assertEquals(LoginControl.RUOLO_CLIENTE, loginDopoReg.getRuoloAutenticato());
    }

    /**
     * Test Case 3 - Responsabile: Stefania Guga
     * Verifica che DAOFactory restituisca correttamente:
     * - TurnoDAOMemory in modalita' DEMO
     * - TurnoDAODbms in modalita' FULL (default JDBC MySQL)
     * - TurnoDAOFileSystem in modalita' FULL quando selezionato File System CSV.
     */
    @Test
    void testSelezionePersistenzaTurnoDAO() {
        SessionManager.getInstance().setModalita(ModalitaEsecuzione.DEMO);
        TurnoDAO daoDemo = DAOFactory.getTurnoDAO();
        assertTrue(daoDemo instanceof TurnoDAOMemory);

        SessionManager.getInstance().setModalita(ModalitaEsecuzione.FULL);
        DAOFactory.setUsaFileSystemPerTurni(false);
        TurnoDAO daoDbms = DAOFactory.getTurnoDAO();
        assertTrue(daoDbms instanceof TurnoDAODbms);

        DAOFactory.setUsaFileSystemPerTurni(true);
        TurnoDAO daoFile = DAOFactory.getTurnoDAO();
        assertTrue(daoFile instanceof TurnoDAOFileSystem);
    }

    /**
     * Test Case 4 - Responsabile: Stefania Guga
     * Verifica che l'assegnazione di un turno sovrapposto allo stesso dipendente
     * sollevi l'eccezione checked TurnoNonDisponibileException.
     */
    @Test
    void testConflittoTurnoSollevaTurnoNonDisponibileException() {
        GestioneTurniEmergenzeControl control = new GestioneTurniEmergenzeControl();

        assertThrows(TurnoNonDisponibileException.class, () -> {
            TurnoBean turnoInConflitto = new TurnoBean();
            turnoInConflitto.setEmailDipendente("mario@frostly.it");
            turnoInConflitto.setData("Lunedi");
            turnoInConflitto.setOraInizio("10:00");
            turnoInConflitto.setOraFine("16:00");
            control.pubblicaNuovoTurno(turnoInConflitto);
        });
    }

    /**
     * Test Case 5 - Responsabile: Stefania Guga
     * Verifica l'eccezione IngredienteEsauritoException quando un cliente richiede
     * un gusto personalizzato con quantita' superiore alla scorta disponibile,
     * e il successivo recupero tramite rifornimento urgente (+5 Kg).
     */
    @Test
    void testRichiestaGustoIngredienteEsauritoERifornimento() throws FrostlyException {
        RichiestaGustoControl control = new RichiestaGustoControl();

        RichiestaGustoBean beanRichiesta = new RichiestaGustoBean();
        beanRichiesta.setEmailCliente("elena@cliente.it");
        beanRichiesta.setNomeGustoProposto("Sorbetto Mango Imperiale");
        beanRichiesta.setNomeIngredientePrincipale("Polpa di Mango Alphonso");
        beanRichiesta.setQuantitaDaTesto("3.0");
        beanRichiesta.setDataRitiroDesiderata("Domenica");
        beanRichiesta.setNoteCliente("Test JUnit scorta insufficiente");

        RichiestaGustoBean creata = control.inviaRichiestaGusto(beanRichiesta);
        String codice = creata.getCodiceRichiesta();

        assertThrows(IngredienteEsauritoException.class, () ->
                control.approvaRichiestaGusto(codice, "Tentativo senza scorta"));

        RichiestaGustoBean approvata = control.approvaConRifornimentoUrgente(
                codice, 5.0, "Rifornimento effettuato e gusto in produzione");
        assertEquals("APPROVATA", approvata.getStato());
    }

    /**
     * Test Case 6 - Responsabile: Stefania Guga
     * Verifica il calcolo dinamico del prezzo del pre-ordine vaschetta tramite
     * il pattern GoF Decorator (VaschettaBase + Panna Montata + Granella di Pistacchio DOP).
     */
    @Test
    void testPreOrdineVaschettaConPatternDecorator() throws FrostlyException {
        OrdiniControl control = new OrdiniControl();

        OrdineBean ordineInput = new OrdineBean();
        ordineInput.setEmailCliente("elena@cliente.it");
        ordineInput.setNomeGusto("Pistacchio di Bronte DOP");
        ordineInput.setPesoDaTesto("1.5");
        ordineInput.setDataRitiro("Sabato");
        ordineInput.setConPanna(true);
        ordineInput.setConGranellaBronte(true);

        OrdineBean risultato = control.effettuaPreOrdine(ordineInput);

        assertEquals(45.0, risultato.getPrezzoTotale(), 0.001);
        assertTrue(risultato.getDescrizioneCompleta().contains("Panna Montata Fresca"));
        assertTrue(risultato.getDescrizioneCompleta().contains("Granella di Pistacchio DOP"));
    }

    /**
     * Test Case 7 - Responsabile: Stefania Guga
     * Verifica la composizione di un pre-ordine con molteplici vaschette (multi-vaschetta)
     * e la consultazione dello storico ordini per il cliente.
     */
    @Test
    void testPreOrdineMultiVaschettaEStorico() throws FrostlyException {
        OrdiniControl control = new OrdiniControl();

        OrdineBean multiOrdine = new OrdineBean();
        multiOrdine.setEmailCliente("elena@cliente.it");
        multiOrdine.setDataRitiro("Domenica");

        // Vaschetta 1: 500g Pistacchio con panna (prezzo = 26.0 * 0.5 + 2.50 = 15.50)
        VaschettaItemBean v1 = new VaschettaItemBean(0.5, List.of("Pistacchio di Bronte DOP"),
                true, false, "Vaschetta 500g Pistacchio + Panna", 15.5);
        // Vaschetta 2: 350g Fondente Ecuador (prezzo = 24.0 * 0.35 = 8.40)
        VaschettaItemBean v2 = new VaschettaItemBean(0.35, List.of("Fondente Ecuador 75%"),
                false, false, "Vaschetta 350g Fondente Ecuador", 8.4);

        multiOrdine.aggiungiVaschetta(v1);
        multiOrdine.aggiungiVaschetta(v2);

        OrdineBean confermato = control.effettuaPreOrdine(multiOrdine);
        assertNotNull(confermato.getCodiceOrdine());
        assertEquals(23.90, confermato.getPrezzoTotale(), 0.01);

        List<OrdineBean> storico = control.visualizzaOrdiniCliente("elena@cliente.it");
        assertTrue(storico.size() >= 1);
    }

    /**
     * Test Case 8 - Responsabile: Stefania Guga
     * Verifica il flusso Deliveroo / Take Away: l'ordine viene creato in stato IN_ATTESA
     * e successivamente confermato dal personale (stato APPROVATA con notifica al cliente).
     */
    @Test
    void testConfermaOrdineStaffDeliveroo() throws FrostlyException {
        OrdiniControl control = new OrdiniControl();

        OrdineBean ordine = new OrdineBean();
        ordine.setEmailCliente("elena@cliente.it");
        ordine.setDataRitiro("🛵 Consegna a Domicilio (Subito ~25-35 min)");
        ordine.aggiungiVaschetta(new VaschettaItemBean(0.5, List.of("Pistacchio di Bronte DOP"),
                false, false, "Vaschetta 500g Pistacchio", 13.0));

        OrdineBean creato = control.effettuaPreOrdine(ordine);
        assertEquals("IN_ATTESA", creato.getStato());

        control.confermaOrdine(creato.getCodiceOrdine(), "Maestro Gelataio");
        List<OrdineBean> lista = control.visualizzaOrdiniCliente("elena@cliente.it");
        boolean trovato = lista.stream().anyMatch(o -> o.getCodiceOrdine().equals(creato.getCodiceOrdine()) && "APPROVATA".equals(o.getStato()));
        assertTrue(trovato);
    }

    /**
     * Test Case 9 - Responsabile: Stefania Guga
     * Verifica la presenza nel catalogo delle 4 Crepes (Nutella, Visciole, Marmellata, Crema Pan di Stelle),
     * il calcolo del Topping (+0.50€) e della Granella (+0.50€ o Nessuna), delle porzioni di Panna (100g, 200g, 300g)
     * e delle Bevande con foto, nonche' l'invio di un ordine misto nel carrello.
     */
    @Test
    void testOrdineCrepeConToppingPannaEBevande() throws FrostlyException {
        GestioneMenuControl control = new GestioneMenuControl();
        var menu = control.visualizzaMenuGusti();

        assertTrue(menu.stream().anyMatch(g -> "Crepe alla Nutella".equals(g.getNome()) && "Crepes".equals(g.getCategoria())));
        assertTrue(menu.stream().anyMatch(g -> "Crepe alle Visciole".equals(g.getNome()) && "Farciture".equals(g.getSottocategoria())));
        assertTrue(menu.stream().anyMatch(g -> "Crepe alla Marmellata".equals(g.getNome()) && "Farciture".equals(g.getSottocategoria())));
        assertTrue(menu.stream().anyMatch(g -> "Crepe Crema Pan di Stelle".equals(g.getNome()) && "Farciture".equals(g.getSottocategoria())));
        assertTrue(menu.stream().anyMatch(g -> "Panna Fresca 200g".equals(g.getNome()) && "Panna".equals(g.getCategoria())));

        String[] gustiFrutta = {
            "Fragola", "Menta", "Limone", "Melone Bianco", "Lampone",
            "Anguria", "Liquirizia", "Pesca", "Albicocca", "Banana", "Banana al Cioccolato"
        };
        for (String gf : gustiFrutta) {
            assertTrue(menu.stream().anyMatch(g -> gf.equals(g.getNome())
                    && "Gelati".equals(g.getCategoria())
                    && "Frutta".equals(g.getSottocategoria())));
        }

        String[] gustiSpecial = {
            "Snickers", "Bounty", "Delizia al Limone", "Nutella", "Galak",
            "Amarandola", "Croccante all'Amarena", "Pistacchio variegato Cioccolato e Wafer",
            "Cremino Nutella", "Mascarpone Amarena e Gentilini", "Caffè", "Spagnola", "Zuppa Inglese"
        };
        for (String gs : gustiSpecial) {
            assertTrue(menu.stream().anyMatch(g -> gs.equals(g.getNome())
                    && "Gelati".equals(g.getCategoria())
                    && "Special".equals(g.getSottocategoria())));
        }

        double prezzoCrepe = view.CrepeConfiguratoreView.calcolaPrezzoCrepe(5.50, "Topping Cioccolato Fondente", "Granella di Pistacchio");
        assertEquals(6.50, prezzoCrepe, 0.01);

        double prezzoSenzaGranella = view.CrepeConfiguratoreView.calcolaPrezzoCrepe(4.50, "Topping Caramello", view.CrepeConfiguratoreView.NESSUNA_GRANELLA);
        assertEquals(5.00, prezzoSenzaGranella, 0.01);

        bean.CarrelloBean carrello = new bean.CarrelloBean();
        carrello.setModalitaConsegna("🛵 Consegna a Domicilio");
        carrello.setFasciaOraria("Oggi 18:00");

        String descCrepe = view.CrepeConfiguratoreView.formattaDescrizioneCrepe(
                "Crepe Crema Pan di Stelle", "Topping Cioccolato Fondente", "Granella di Pistacchio");
        GestoreCarrello.aggiungiArticolo(carrello, new VaschettaItemBean(0.0,
                List.of("Crepe Crema Pan di Stelle"),
                false, false, descCrepe, prezzoCrepe));
        // Panna 200g = 4.50
        GestoreCarrello.aggiungiArticolo(carrello, new VaschettaItemBean(0.20, List.of(),
                true, false, "Panna Montata Fresca d'Alpeggio (200g)", 4.50));
        // Fanta 33cl = 3.00
        GestoreCarrello.aggiungiArticolo(carrello, new VaschettaItemBean(0.0, List.of("Fanta 33cl"),
                false, false, "Fanta 33cl", 3.00));

        assertEquals(3, GestoreCarrello.getNumeroArticoli(carrello));
        assertEquals(14.00, GestoreCarrello.calcolaTotale(carrello), 0.01);

        OrdineBean ordineMisto = GestoreCarrello.costruisciOrdineBean(carrello, "elena@cliente.it");
        OrdineBean confermato = new OrdiniControl().effettuaPreOrdine(ordineMisto);
        assertEquals(14.00, confermato.getPrezzoTotale(), 0.01);
        assertTrue(confermato.getDescrizioneCompleta().contains("Crepe Crema Pan di Stelle"));
        assertTrue(confermato.getDescrizioneCompleta().contains("Granella di Pistacchio"));
        assertFalse(confermato.getDescrizioneCompleta().contains("Vaschetta #"));
    }

    /**
     * Test Case 8 - Responsabile: Stefania Guga
     * Verifica il ciclo di vita delle Recensioni: consultazione pubblica, calcolo media voti,
     * pubblicazione da parte del cliente, risposta ufficiale del Maestro Gelataio e interazioni (mi piace).
     */
    @Test
    void testCicloVitaRecensioniERispostaMaestro() throws FrostlyException {
        RecensioniControl control = new RecensioniControl();

        List<RecensioneBean> iniziali = control.visualizzaTutteRecensioni();
        assertTrue(iniziali.size() >= 3);
        double mediaIniziale = control.calcolaMediaVoti(iniziali);
        assertTrue(mediaIniziale >= 4.5);

        // Pubblicazione nuova recensione
        RecensioneBean nuova = new RecensioneBean(
                "elena@cliente.it", "Elena Conti", 5,
                "Gelato al mascarpone e visciole spettacolare",
                "Uno dei migliori gelati mai assaggiati. Cremoso, non troppo dolce e con ingredienti freschissimi!");
        RecensioneBean pubblicata = control.pubblicaRecensione(nuova);

        assertNotNull(pubblicata.getCodiceRecensione());
        assertEquals(5, pubblicata.getVotoStelle());
        assertEquals("★★★★★", pubblicata.getStelleIcone());
        assertEquals(0, pubblicata.getMiPiace());

        // Interazione Utile / Mi Piace
        control.mettiMiPiace(pubblicata.getCodiceRecensione());
        List<RecensioneBean> dopoLike = control.visualizzaTutteRecensioni();
        RecensioneBean trovata = dopoLike.stream()
                .filter(r -> r.getCodiceRecensione().equals(pubblicata.getCodiceRecensione()))
                .findFirst()
                .orElse(null);
        assertNotNull(trovata);
        assertEquals(1, trovata.getMiPiace());

        // Risposta ufficiale del Maestro / Maestra
        control.rispondiARecensione(pubblicata.getCodiceRecensione(), "Grazie infinite Elena per il prezioso riscontro! - Stefania");
        List<RecensioneBean> dopoRisposta = control.visualizzaTutteRecensioni();
        RecensioneBean conRisposta = dopoRisposta.stream()
                .filter(r -> r.getCodiceRecensione().equals(pubblicata.getCodiceRecensione()))
                .findFirst()
                .orElse(null);
        assertNotNull(conRisposta);
        assertTrue(conRisposta.haRispostaMaestro());
        assertTrue(conRisposta.getRispostaMaestro().contains("Stefania"));

        // Validazione: voto stelle errato o campi vuoti
        assertThrows(DatiNonValidiException.class, () -> new RecensioneBean("test@test.it", "Test", 0, "Titolo", "Testo"));
        assertThrows(DatiNonValidiException.class, () -> new RecensioneBean("test@test.it", "Test", 6, "Titolo", "Testo"));
        assertThrows(DatiNonValidiException.class, () -> new RecensioneBean("test@test.it", "Test", 5, "", "Testo"));
        assertThrows(DatiNonValidiException.class, () -> new RecensioneBean("test@test.it", "Test", 5, "Titolo", ""));
    }

    /**
     * TEST 11: Robustezza Turni & Dipendenti
     * Verifica che il caricamento e la conversione dei turni non sollevi NullPointerException
     * anche nel caso in cui un dipendente sia stato rimosso o un turno non abbia dipendente assegnato.
     */
    @Test
    void testRobustezzaTurniSenzaDipendente() throws FrostlyException {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        List<TurnoBean> turni = turniCtrl.visualizzaTuttiTurni();
        assertNotNull(turni);

        // Simulazione turno orfano
        model.Turno turnoOrfano = new model.Turno("TRN-ORFANO", "Venerdi", "08:00", "14:00", null);
        dao.ArchivioMemoria.getInstance().salvaTurno(turnoOrfano);

        List<TurnoBean> turniDopo = turniCtrl.visualizzaTuttiTurni();
        TurnoBean beanOrfano = turniDopo.stream()
                .filter(t -> "TRN-ORFANO".equals(t.getCodiceTurno()))
                .findFirst()
                .orElse(null);

        assertNotNull(beanOrfano);
        assertNotNull(beanOrfano.getEmailDipendente());
        assertNotNull(beanOrfano.getNomeDipendente());
    }

    /**
     * TEST 12: Validazione Turni con Chiusura a Mezzanotte (00:00)
     * Verifica che i turni serali (es. 16:00 - 00:00 e 20:00 - 00:00)
     * siano considerati validi dal TurnoBean riconoscendo 00:00 come mezzanotte (fine giornata).
     */
    @Test
    void testValidazioneTurnoMezzanotte() throws DatiNonValidiException {
        TurnoBean turno16 = new TurnoBean();
        turno16.setOraInizio("16:00");
        turno16.setOraFine("00:00");
        assertEquals("16:00", turno16.getOraInizio());
        assertEquals("00:00", turno16.getOraFine());

        TurnoBean turno20 = new TurnoBean();
        turno20.setOraInizio("20:00");
        turno20.setOraFine("00:00");
        assertEquals("20:00", turno20.getOraInizio());
        assertEquals("00:00", turno20.getOraFine());

        // Verifica che un orario veramente precedente (es. 14:00 - 10:00) continui a sollevare eccezione
        TurnoBean turnoInvalido = new TurnoBean();
        turnoInvalido.setOraInizio("14:00");
        assertThrows(DatiNonValidiException.class, () -> turnoInvalido.setOraFine("10:00"));
    }

    /**
     * Test Case 13 - Responsabile: Stefania Guga
     * Verifica:
     * 1. Presenza di tutti i prodotti del menu a magazzino (Tisane in Bustine, Cioccolate in Bustine, Bevande in Bottiglie, Materie Prime in Kg/Litri).
     * 2. Decremento automatico delle giacenze all'invio di un pre-ordine contenente prodotti menu e vaschette gelato.
     * 3. Registrazione di rifornimento scorte (carico) e scarico manuale con MagazzinoControl.
     */
    @Test
    void testMagazzinoDecrementoOrdiniECaricoScarico() throws FrostlyException {
        MagazzinoControl magControl = new MagazzinoControl();
        OrdiniControl ordiniControl = new OrdiniControl();

        // 1. Scorte iniziali
        var listaIniziale = magControl.visualizzaMagazzino();
        var tisanaPrima = listaIniziale.stream()
                .filter(i -> "Tisana Zenzero e Limone".equals(i.getNome()))
                .findFirst().orElseThrow();
        assertEquals("Bustine", tisanaPrima.getUnitaMisura());
        double qtaTisanaPrima = tisanaPrima.getQuantitaDisponibile();

        var acquaPrima = listaIniziale.stream()
                .filter(i -> "Acqua Naturale 500ml".equals(i.getNome()))
                .findFirst().orElseThrow();
        assertEquals("Bottiglie", acquaPrima.getUnitaMisura());
        double qtaAcquaPrima = acquaPrima.getQuantitaDisponibile();

        // 2. Invio pre-ordine con 1 Tisana, 1 Bottiglia d'Acqua e 1 Vaschetta gelato 500g Pistacchio
        OrdineBean ordine = new OrdineBean();
        ordine.setEmailCliente("elena@cliente.it");
        ordine.setDataRitiro("Sabato");
        ordine.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Tisana Zenzero e Limone"), false, false,
                "Tisana Zenzero e Limone (Infuso caldo)", 2.5));
        ordine.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Acqua Naturale 500ml"), false, false,
                "Acqua Naturale 500ml (Acqua fresca)", 1.5));
        ordine.aggiungiVaschetta(new VaschettaItemBean(0.5, List.of("Pistacchio di Bronte DOP"), false, false,
                "Vaschetta 500g Pistacchio", 13.0));

        ordiniControl.effettuaPreOrdine(ordine);

        // 3. Verifica decremento automatico a magazzino
        var listaDopoOrdine = magControl.visualizzaMagazzino();
        var tisanaDopo = listaDopoOrdine.stream()
                .filter(i -> "Tisana Zenzero e Limone".equals(i.getNome()))
                .findFirst().orElseThrow();
        assertEquals(qtaTisanaPrima - 1.0, tisanaDopo.getQuantitaDisponibile(), 0.001);

        var acquaDopo = listaDopoOrdine.stream()
                .filter(i -> "Acqua Naturale 500ml".equals(i.getNome()))
                .findFirst().orElseThrow();
        assertEquals(qtaAcquaPrima - 1.0, acquaDopo.getQuantitaDisponibile(), 0.001);

        // 4. Test Carico (Rifornimento merci) e Scarico manuale
        magControl.rifornisciIngredienteMagazzino("Tisana Zenzero e Limone", 10.0);
        var tisanaDopoCarico = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Tisana Zenzero e Limone".equals(i.getNome()))
                .findFirst().orElseThrow();
        assertEquals(tisanaDopo.getQuantitaDisponibile() + 10.0, tisanaDopoCarico.getQuantitaDisponibile(), 0.001);

        magControl.scaricaIngredienteMagazzino("Tisana Zenzero e Limone", 5.0);
        var tisanaDopoScarico = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Tisana Zenzero e Limone".equals(i.getNome()))
                .findFirst().orElseThrow();
        assertEquals(tisanaDopoCarico.getQuantitaDisponibile() - 5.0, tisanaDopoScarico.getQuantitaDisponibile(), 0.001);

        // 5. Test Aggiunta nuova voce a magazzino
        bean.IngredienteBean nuovaVoce = new bean.IngredienteBean();
        nuovaVoce.setNome("Miele di Zagara di Sicilia");
        nuovaVoce.setQuantitaDisponibile(20.0);
        nuovaVoce.setSogliaMinima(5.0);
        nuovaVoce.setUnitaMisura("Kg");
        magControl.aggiungiNuovoIngrediente(nuovaVoce);

        var mieleAggiunto = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Miele di Zagara di Sicilia".equals(i.getNome()))
                .findFirst().orElse(null);
        assertNotNull(mieleAggiunto);
        assertEquals(20.0, mieleAggiunto.getQuantitaDisponibile());
        assertEquals("Kg", mieleAggiunto.getUnitaMisura());

        // Verifica blocco duplicato
        assertThrows(DatiNonValidiException.class, () -> magControl.aggiungiNuovoIngrediente(nuovaVoce));

        // 6. Test Eliminazione voce dal magazzino
        magControl.eliminaIngredienteMagazzino("Miele di Zagara di Sicilia");
        var mieleDopoEliminazione = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Miele di Zagara di Sicilia".equals(i.getNome()))
                .findFirst().orElse(null);
        org.junit.jupiter.api.Assertions.assertNull(mieleDopoEliminazione);
    }

    /**
     * Test Case 14 - Responsabile: Stefania Guga
     * Verifica la gestione unificata delle materie prime coincidenti (Latte, Burro, Farina, Uova, Zucchero, Nutella)
     * e il decremento congiunto dallo stesso stock unico di magazzino per ordini di Crepes, Cioccolate e Caffetteria.
     */
    @Test
    void testMateriePrimeCoincidentiECrepes() throws FrostlyException {
        MagazzinoControl magControl = new MagazzinoControl();
        OrdiniControl ordiniControl = new OrdiniControl();

        var magazzinoIniziale = magControl.visualizzaMagazzino();

        // 1. Verifica che tutte le materie prime per le Crepes appartengano al filtro "Crepes"
        var latte = magazzinoIniziale.stream().filter(i -> "Latte Fresco Alta Qualita".equals(i.getNome())).findFirst().orElseThrow();
        var burro = magazzinoIniziale.stream().filter(i -> "Burro Fresco di Latteria".equals(i.getNome())).findFirst().orElseThrow();
        var farina = magazzinoIniziale.stream().filter(i -> "Farina per Crepes e Dolci".equals(i.getNome())).findFirst().orElseThrow();
        var uova = magazzinoIniziale.stream().filter(i -> "Uova Fresche da Allevamento".equals(i.getNome())).findFirst().orElseThrow();
        var zucchero = magazzinoIniziale.stream().filter(i -> "Zucchero Semolato Italiano".equals(i.getNome())).findFirst().orElseThrow();
        var nutella = magazzinoIniziale.stream().filter(i -> "Nutella Ferrero".equals(i.getNome())).findFirst().orElseThrow();

        assertTrue(latte.appartieneACategoria("Crepes"));
        assertTrue(burro.appartieneACategoria("Crepes"));
        assertTrue(farina.appartieneACategoria("Crepes"));
        assertTrue(uova.appartieneACategoria("Crepes"));
        assertTrue(zucchero.appartieneACategoria("Crepes"));
        assertTrue(nutella.appartieneACategoria("Crepes"));

        // Verifica che prodotti finiti di altre categorie (crostate a porzioni, cioccolate in bustina) NON compaiano sotto Crepes
        var crostataNutella = magazzinoIniziale.stream().filter(i -> "Crostata alla Nutella".equals(i.getNome())).findFirst().orElseThrow();
        var cioccolataPistacchio = magazzinoIniziale.stream().filter(i -> "Cioccolata Calda al Pistacchio di Bronte".equals(i.getNome())).findFirst().orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(crostataNutella.appartieneACategoria("Crepes"));
        org.junit.jupiter.api.Assertions.assertFalse(cioccolataPistacchio.appartieneACategoria("Crepes"));

        // E che Latte appartenga anche a Caffetteria, Gelati e Cioccolate calde
        assertTrue(latte.appartieneACategoria("Caffetteria"));
        assertTrue(latte.appartieneACategoria("Cioccolate calde"));
        assertTrue(latte.appartieneACategoria("Gelati"));

        double latteIniziale = latte.getQuantitaDisponibile();
        double farinaIniziale = farina.getQuantitaDisponibile();
        double burroIniziale = burro.getQuantitaDisponibile();
        double nutellaIniziale = nutella.getQuantitaDisponibile();

        // 2. Ordine di una Cioccolata Calda: consuma 0.15L di latte dallo stock comune
        OrdineBean ordineCioccolata = new OrdineBean();
        ordineCioccolata.setEmailCliente("elena@cliente.it");
        ordineCioccolata.setDataRitiro("Oggi 17:00");
        ordineCioccolata.setPrezzoTotale(4.0);
        ordineCioccolata.setDescrizioneCompleta("Cioccolata Calda Fondente");
        ordineCioccolata.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Cioccolata Calda Fondente"), false, false,
                "Cioccolata Calda Fondente", 4.0));
        ordiniControl.effettuaPreOrdine(ordineCioccolata);

        double latteDopoCioccolata = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Latte Fresco Alta Qualita".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();
        assertEquals(latteIniziale - 0.15, latteDopoCioccolata, 0.001);

        // 3. Ordine di una Crepe alla Nutella: consuma 0.10L di latte dallo STESSO stock, oltre a farina, burro, nutella
        OrdineBean ordineCrepe = new OrdineBean();
        ordineCrepe.setEmailCliente("elena@cliente.it");
        ordineCrepe.setDataRitiro("Oggi 17:30");
        ordineCrepe.setPrezzoTotale(5.0);
        ordineCrepe.setDescrizioneCompleta("Crepe alla Nutella");
        ordineCrepe.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Crepe alla Nutella"), false, false,
                "Crepe alla Nutella", 5.0));
        ordiniControl.effettuaPreOrdine(ordineCrepe);

        var magazzinoFinale = magControl.visualizzaMagazzino();
        double latteFinale = magazzinoFinale.stream()
                .filter(i -> "Latte Fresco Alta Qualita".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();
        double farinaFinale = magazzinoFinale.stream()
                .filter(i -> "Farina per Crepes e Dolci".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();
        double burroFinale = magazzinoFinale.stream()
                .filter(i -> "Burro Fresco di Latteria".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();
        double nutellaFinale = magazzinoFinale.stream()
                .filter(i -> "Nutella Ferrero".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();

        // Verifica che il latte sia diminuito sia per la cioccolata che per la crepe (scalate insieme dallo stesso inventario)
        assertEquals(latteIniziale - 0.15 - 0.10, latteFinale, 0.001);
        assertEquals(farinaIniziale - 0.08, farinaFinale, 0.001);
        assertEquals(burroIniziale - 0.02, burroFinale, 0.001);
        assertEquals(nutellaIniziale - 0.05, nutellaFinale, 0.001);
    }

    /**
     * Test Case 15 - Responsabile: Stefania Guga
     * Verifica la modifica interattiva delle quantita' nel riepilogo dell'ordine (+ / -),
     * il ricalcolo istantaneo dei subtotali, il totale complessivo e la scalatura corretta
     * delle scorte a magazzino moltiplicata per le quantita' selezionate.
     */
    @Test
    void testIncrementoDecrementoQuantitaCarrelloERiepilogo() throws FrostlyException {
        bean.CarrelloBean carrello = new bean.CarrelloBean();
        OrdiniControl ordiniControl = new OrdiniControl();
        MagazzinoControl magControl = new MagazzinoControl();

        // 1. Aggiunta iniziale di 1 bottiglia d'acqua e 1 bustina di tisana
        VaschettaItemBean acqua = new VaschettaItemBean(0.0, List.of("Acqua Naturale 500ml"), false, false,
                "Acqua Naturale 500ml", 1.50);
        VaschettaItemBean tisana = new VaschettaItemBean(0.0, List.of("Tisana Zenzero e Limone"), false, false,
                "Tisana Zenzero e Limone", 2.50);

        GestoreCarrello.aggiungiArticolo(carrello, acqua);
        GestoreCarrello.aggiungiArticolo(carrello, tisana);

        assertEquals(2, GestoreCarrello.getNumeroArticoli(carrello));
        assertEquals(4.00, GestoreCarrello.calcolaTotale(carrello), 0.001);

        // 2. Incremento quantità per l'acqua (da 1 a 3) tramite tasto +
        GestoreCarrello.incrementaArticolo(carrello, 0);
        assertEquals(2, carrello.getArticoli().get(0).getQuantita());
        assertEquals(3.00, carrello.getArticoli().get(0).ottieniPrezzo(), 0.001);
        assertEquals(5.50, GestoreCarrello.calcolaTotale(carrello), 0.001);

        GestoreCarrello.incrementaArticolo(carrello, 0);
        assertEquals(3, carrello.getArticoli().get(0).getQuantita());
        assertEquals(4.50, carrello.getArticoli().get(0).ottieniPrezzo(), 0.001);
        assertEquals(7.00, GestoreCarrello.calcolaTotale(carrello), 0.001);
        assertEquals(4, GestoreCarrello.getNumeroArticoli(carrello)); // 3 acque + 1 tisana

        // 3. Decremento quantità per l'acqua (da 3 a 2) tramite tasto -
        GestoreCarrello.decrementaArticolo(carrello, 0);
        assertEquals(2, carrello.getArticoli().get(0).getQuantita());
        assertEquals(3.00, carrello.getArticoli().get(0).ottieniPrezzo(), 0.001);
        assertEquals(5.50, GestoreCarrello.calcolaTotale(carrello), 0.001);

        // 4. Invio dell'ordine con 2 acque e 1 tisana
        double qtaAcquaMagPrima = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Acqua Naturale 500ml".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();

        OrdineBean ordineBean = GestoreCarrello.costruisciOrdineBean(carrello, "elena@cliente.it");
        assertEquals(3, ordineBean.getVaschette().size()); // 2 acque + 1 tisana
        ordiniControl.effettuaPreOrdine(ordineBean);

        double qtaAcquaMagDopo = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Acqua Naturale 500ml".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();

        // Verifica che il magazzino sia scalato esattamente di 2 bottiglie
        assertEquals(qtaAcquaMagPrima - 2.0, qtaAcquaMagDopo, 0.001);

        // 5. Decremento fino a rimozione
        GestoreCarrello.decrementaArticolo(carrello, 1); // Tisana da 1 a 0 -> rimossa
        assertEquals(1, carrello.getArticoli().size()); // Solo acqua rimasta
        assertEquals(2, GestoreCarrello.getNumeroArticoli(carrello)); // 2 acque

        GestoreCarrello.decrementaArticolo(carrello, 0); // Acqua da 2 a 1
        assertEquals(1, carrello.getArticoli().get(0).getQuantita());
        GestoreCarrello.decrementaArticolo(carrello, 0); // Acqua da 1 a 0 -> rimossa
        assertTrue(GestoreCarrello.isVuoto(carrello));
        assertEquals(0, GestoreCarrello.getNumeroArticoli(carrello));
    }

    /**
     * Test Case 16 - Eliminazione Avvisi e Notifiche dal Registro Cronologico Staff.
     * Verifica che lo Staff possa eliminare notifiche specifiche dal registro cronologico.
     */
    @Test
    void testEliminazioneNotificaStaff() {
        LoginControl loginControl = new LoginControl();
        String emailMaestro = "stefania.guga@yahoo.com";

        // Invio notifica di prova
        DAOFactory.getNotificaDAO().inviaNotifica(new model.Notifica(
                emailMaestro,
                "Avviso Test Eliminazione",
                "Messaggio di test per la cancellazione",
                "Oggi 17:40"));

        List<bean.NotificaBean> notifichePrima = loginControl.recuperaNotificheUtente(emailMaestro);
        boolean trovata = notifichePrima.stream()
                .anyMatch(n -> "Avviso Test Eliminazione".equals(n.getTitolo()));
        assertTrue(trovata);

        // Eliminazione della notifica
        bean.NotificaBean daEliminare = notifichePrima.stream()
                .filter(n -> "Avviso Test Eliminazione".equals(n.getTitolo()))
                .findFirst().orElseThrow();
        loginControl.eliminaNotificaUtente(emailMaestro, daEliminare);

        List<bean.NotificaBean> notificheDopo = loginControl.recuperaNotificheUtente(emailMaestro);
        boolean ancoraPresente = notificheDopo.stream()
                .anyMatch(n -> "Avviso Test Eliminazione".equals(n.getTitolo()));
        assertFalse(ancoraPresente);
    }

    /**
     * Test Case 17 - Servizio Pagamento Online: Pagamento Autorizzato con Carta Valida.
     * Verifica la corretta elaborazione dell'addebito simulato, algoritmo di Luhn e codice transazione.
     */
    @Test
    void testPagamentoOnlineConCartaValida() throws FrostlyException {
        // Verifica che DAOFactory selezioni PagamentoDAOMemory in DEMO e PagamentoDAODbms in FULL
        SessionManager.getInstance().setModalita(ModalitaEsecuzione.DEMO);
        PagamentoDAO daoDemo = DAOFactory.getPagamentoDAO();
        assertTrue(daoDemo instanceof PagamentoDAOMemory);

        SessionManager.getInstance().setModalita(ModalitaEsecuzione.FULL);
        PagamentoDAO daoFull = DAOFactory.getPagamentoDAO();
        assertTrue(daoFull instanceof PagamentoDAODbms);

        SessionManager.getInstance().setModalita(ModalitaEsecuzione.DEMO);
        control.applicativo.PagamentoControl payControl = new control.applicativo.PagamentoControl();

        bean.CartaPagamentoBean carta = new bean.CartaPagamentoBean(
                "Mario Rossi",
                "4532 0150 1234 5671",
                "12/28",
                "789"
        );
        carta.validaSintassi();

        bean.DatiConsegnaBean consegna = new bean.DatiConsegnaBean(
                bean.DatiConsegnaBean.TipoConsegna.DOMICILIO,
                "Via del Corso 124",
                "3331234567",
                "Subito (~25-35 min)",
                "Citofono 4B"
        );
        consegna.validaSintassi();

        bean.EsitoPagamentoBean esito = payControl.elaboraPagamento(carta, 28.50, consegna, "mario@cliente.it");
        assertTrue(esito.isSuccesso());
        assertNotNull(esito.getCodiceTransazione());
        assertTrue(esito.getCodiceTransazione().startsWith("TRX-"));
        assertEquals(28.50, esito.getImporto());
        assertEquals("VISA", esito.getCircuito());
    }

    /**
     * Test Case 18 - Servizio Pagamento Online: Rifiuto Carta Scaduta.
     * Verifica che una data di scadenza nel passato sollevi l'eccezione InvalidCardException.
     */
    @Test
    void testPagamentoOnlineRifiutoCartaScaduta() {
        control.applicativo.PagamentoControl payControl = new control.applicativo.PagamentoControl(new dao.memoria.PagamentoDAOMemory());

        bean.CartaPagamentoBean cartaScaduta = new bean.CartaPagamentoBean(
                "Mario Rossi",
                "4532 0150 1234 5678",
                "01/20",
                "789"
        );

        bean.DatiConsegnaBean consegna = new bean.DatiConsegnaBean(
                bean.DatiConsegnaBean.TipoConsegna.ASPORTO,
                "",
                "",
                "Oggi 18:00",
                "Ritiro al banco"
        );

        assertThrows(frostly.exception.InvalidCardException.class, () ->
                payControl.elaboraPagamento(cartaScaduta, 15.00, consegna, "mario@cliente.it"));
    }

    /**
     * Test Case 19 - Validazione sintattica CartaPagamentoBean: CVV non valido o Numero non conforme a Luhn.
     */
    @Test
    void testPagamentoOnlineRifiutoCvvENumeroInvalido() {
        bean.CartaPagamentoBean cartaCvvErrato = new bean.CartaPagamentoBean(
                "Mario Rossi", "4532 0150 1234 5671", "12/28", "12"
        );
        assertThrows(frostly.exception.InvalidCardException.class, cartaCvvErrato::validaSintassi);

        bean.CartaPagamentoBean cartaLuhnErrato = new bean.CartaPagamentoBean(
                "Mario Rossi", "4111 1111 1111 1112", "12/28", "123"
        );
        assertThrows(frostly.exception.InvalidCardException.class, cartaLuhnErrato::validaSintassi);
    }

    /**
     * Test Case 20 - Validazione sintattica DatiConsegnaBean e CartaPagamentoBean.
     */
    @Test
    void testValidazioneDatiConsegnaDomicilio() {
        bean.DatiConsegnaBean consegnaSenzaTel = new bean.DatiConsegnaBean(
                bean.DatiConsegnaBean.TipoConsegna.DOMICILIO, "Via Roma 10", "", "Subito", ""
        );
        assertThrows(DatiNonValidiException.class, consegnaSenzaTel::validaSintassi);

        bean.DatiConsegnaBean consegnaSenzaIndirizzo = new bean.DatiConsegnaBean(
                bean.DatiConsegnaBean.TipoConsegna.DOMICILIO, "Via", "3331234567", "Subito", ""
        );
        assertThrows(DatiNonValidiException.class, consegnaSenzaIndirizzo::validaSintassi);

        bean.DatiConsegnaBean consegnaSenzaToponimo = new bean.DatiConsegnaBean(
                bean.DatiConsegnaBean.TipoConsegna.DOMICILIO, "Casa Mia 12", "3331234567", "Subito", ""
        );
        assertThrows(DatiNonValidiException.class, consegnaSenzaToponimo::validaSintassi);

        bean.DatiConsegnaBean consegnaSenzaCivico = new bean.DatiConsegnaBean(
                bean.DatiConsegnaBean.TipoConsegna.DOMICILIO, "Piazza Navona", "3331234567", "Subito", ""
        );
        assertThrows(DatiNonValidiException.class, consegnaSenzaCivico::validaSintassi);

        bean.DatiConsegnaBean consegnaTelNonItaliano = new bean.DatiConsegnaBean(
                bean.DatiConsegnaBean.TipoConsegna.DOMICILIO, "Monte Mario 45", "1234567890", "Subito", ""
        );
        assertThrows(DatiNonValidiException.class, consegnaTelNonItaliano::validaSintassi);

        bean.CartaPagamentoBean cartaSoloNome = new bean.CartaPagamentoBean(
                "Mario", "4532 0150 1234 5671", "12/28", "789"
        );
        assertThrows(frostly.exception.InvalidCardException.class, cartaSoloNome::validaSintassi);

        bean.CartaPagamentoBean cartaCvvZero = new bean.CartaPagamentoBean(
                "Mario Rossi", "4532 0150 1234 5671", "12/28", "000"
        );
        assertThrows(frostly.exception.InvalidCardException.class, cartaCvvZero::validaSintassi);
    }

    /**
     * Test Case 21 - Checkout atomico, separazione storage DEMO vs cache JDBC ed eccezioni specifiche.
     */
    @Test
    void testCheckoutAtomicoArchiviSeparatiEdEccezioniSpecifiche() throws FrostlyException {
        // 1. Storage DEMO e cache JDBC devono essere due istanze completamente separate
        org.junit.jupiter.api.Assertions.assertNotSame(
                ArchivioMemoria.getDemoInstance(), ArchivioMemoria.getJdbcCacheInstance());

        // 2. Checkout atomico: se l'articolo 2 non ha scorte, l'articolo 1 NON deve essere scalato
        OrdiniControl ordiniControl = new OrdiniControl();
        MagazzinoControl magControl = new MagazzinoControl();

        double acquaPrima = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Acqua Naturale 500ml".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();

        OrdineBean ordineParziale = new OrdineBean();
        ordineParziale.setEmailCliente("elena@cliente.it");
        ordineParziale.setDataRitiro("Oggi 19:00");
        ordineParziale.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Acqua Naturale 500ml"),
                false, false, "Acqua Naturale 500ml", 1.50));
        // Aggiunge 500 bottiglie di Fanta per forzare IngredienteEsauritoException sul secondo articolo
        for (int i = 0; i < 500; i++) {
            ordineParziale.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Fanta 33cl"),
                    false, false, "Fanta 33cl", 3.00));
        }

        assertThrows(IngredienteEsauritoException.class, () -> ordiniControl.verificaDisponibilitaOrdine(ordineParziale));
        assertThrows(IngredienteEsauritoException.class, () -> ordiniControl.effettuaPreOrdine(ordineParziale));

        double acquaDopo = magControl.visualizzaMagazzino().stream()
                .filter(i -> "Acqua Naturale 500ml".equals(i.getNome()))
                .findFirst().orElseThrow().getQuantitaDisponibile();
        assertEquals(acquaPrima, acquaDopo, 0.001);

        // 3. Eccezioni specifiche: RisorsaNonTrovataException e OperazioneNonAutorizzataException
        assertThrows(RisorsaNonTrovataException.class, () -> ordiniControl.confermaOrdine("ORD-INESISTENTE", "Maestro"));
        SessionManager.getInstance().logout();
        assertThrows(OperazioneNonAutorizzataException.class, () -> new LoginControl().eliminaProfiloDipendente("mario@frostly.it"));
    }
}
