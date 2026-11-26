package control.applicativo;

import bean.EmergenzaBean;
import bean.TurnoBean;
import dao.DAOFactory;
import dao.EmergenzaDAO;
import dao.NotificaDAO;
import dao.TurnoDAO;
import dao.UtenteDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import frostly.exception.RisorsaNonTrovataException;
import frostly.exception.TurnoNonDisponibileException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Dipendente;
import model.Emergenza;
import model.Notifica;
import model.Turno;

/**
 * Controller applicativo STATELESS per i Turni e le Emergenze (UC1 e UC2).
 * Utilizza esclusivamente i DAO dedicati (TurnoDAO, EmergenzaDAO, UtenteDAO, NotificaDAO).
 */
public class GestioneTurniEmergenzeControl {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final java.util.Map<java.time.LocalDate, List<TurnoBean>> SNAPSHOT_PRECEDENTI = new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile model.ConfigurazioneCoperturaFlessibile coperturaFlessibileAttiva = model.ConfigurazioneCoperturaFlessibile.predefinita();

    private final TurnoDAO turnoDao;
    private final EmergenzaDAO emergenzaDao;
    private final UtenteDAO utenteDao;
    private final NotificaDAO notificaDao;

    public GestioneTurniEmergenzeControl() {
        this(DAOFactory.getTurnoDAO(), DAOFactory.getEmergenzaDAO(), DAOFactory.getUtenteDAO(), DAOFactory.getNotificaDAO());
    }

    public GestioneTurniEmergenzeControl(TurnoDAO turnoDao, EmergenzaDAO emergenzaDao, UtenteDAO utenteDao, NotificaDAO notificaDao) {
        this.turnoDao = turnoDao;
        this.emergenzaDao = emergenzaDao;
        this.utenteDao = utenteDao;
        this.notificaDao = notificaDao;
    }

    public List<TurnoBean> visualizzaTuttiTurni() throws FrostlyException {
        List<Turno> turni = turnoDao.caricaTuttiTurni();
        List<TurnoBean> risultato = new ArrayList<>();
        for (Turno t : turni) {
            risultato.add(convertiTurnoInBean(t));
        }
        return risultato;
    }

    public List<TurnoBean> visualizzaTurniDipendente(String emailDipendente) throws FrostlyException {
        List<Turno> turni = turnoDao.caricaTuttiTurni();
        List<TurnoBean> risultato = new ArrayList<>();
        for (Turno t : turni) {
            if (t.getDipendenteAssegnato() != null
                    && emailDipendente != null
                    && emailDipendente.equalsIgnoreCase(t.getDipendenteAssegnato().getEmail())) {
                risultato.add(convertiTurnoInBean(t));
            }
        }
        return risultato;
    }

    public TurnoBean pubblicaNuovoTurno(TurnoBean beanInput) throws FrostlyException {
        List<Turno> turniEsistenti = turnoDao.caricaTuttiTurni();
        Dipendente dipendente = utenteDao.trovaDipendentePerEmail(beanInput.getEmailDipendente());
        if (dipendente == null) {
            throw new RisorsaNonTrovataException("Dipendente non trovato con email: " + beanInput.getEmailDipendente());
        }

        if (!dipendente.isDisponibile(beanInput.getData(), beanInput.getOraInizio(), beanInput.getOraFine())
                || haConflittoInLista(turniEsistenti, dipendente.getEmail(), beanInput.getData(),
                beanInput.getOraInizio(), beanInput.getOraFine())) {
            throw new TurnoNonDisponibileException("Il dipendente " + dipendente
                    + " ha gia' un turno in conflitto per " + beanInput.getData()
                    + " nella fascia " + beanInput.getOraInizio() + "-" + beanInput.getOraFine());
        }

        // Verifica vincolo opzionale numero massimo di turni settimanali
        if (dipendente.hasLimiteMaxTurni()) {
            java.time.LocalDate rifLunedi = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate dataNuovo = CalcolatoreOreSettimanali.estraiDataTurno(beanInput.getData(), rifLunedi);
            java.time.LocalDate lunediTarget = dataNuovo != null
                    ? dataNuovo.with(java.time.DayOfWeek.MONDAY) : rifLunedi;
            List<TurnoBean> tuttiBean = new ArrayList<>();
            for (Turno t : turniEsistenti) {
                tuttiBean.add(convertiTurnoInBean(t));
            }
            int turniSettimana = CalcolatoreOreSettimanali.contaTurniDipendenteNellaSettimana(
                    dipendente.getEmail(), tuttiBean, lunediTarget);
            if (turniSettimana >= dipendente.getMaxTurniSettimanali()) {
                throw new TurnoNonDisponibileException("Assegnazione bloccata: " + dipendente
                        + " ha già raggiunto il limite massimo di " + dipendente.getMaxTurniSettimanali()
                        + " turni settimanali (" + turniSettimana + "/" + dipendente.getMaxTurniSettimanali() + ").");
            }
        }

        // Verifica limite opzionale di turni lunghi (>= 8 h) a settimana (es. max 1 turno da 8 h o solo turni corti)
        Integer maxLunghi = dipendente.getDisponibilita().getMaxTurniLunghiSettimana();
        double durataNuovo = CalcolatoreOreSettimanali.calcolaDurataOre(beanInput.getOraInizio(), beanInput.getOraFine());
        if (maxLunghi != null && durataNuovo >= 7.99) {
            java.time.LocalDate rifLunedi = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate dataNuovo = CalcolatoreOreSettimanali.estraiDataTurno(beanInput.getData(), rifLunedi);
            java.time.LocalDate lunediTarget = dataNuovo != null
                    ? dataNuovo.with(java.time.DayOfWeek.MONDAY) : rifLunedi;
            int lunghiSettimana = 0;
            for (Turno t : turniEsistenti) {
                if (t.getDipendenteAssegnato() != null
                        && dipendente.getEmail().equalsIgnoreCase(t.getDipendenteAssegnato().getEmail())) {
                    TurnoBean tb = convertiTurnoInBean(t);
                    if (CalcolatoreOreSettimanali.calcolaOreTurnoNellaSettimana(tb, lunediTarget) > 0
                            && CalcolatoreOreSettimanali.calcolaDurataOre(t.getOraInizio(), t.getOraFine()) >= 7.99) {
                        lunghiSettimana++;
                    }
                }
            }
            if (lunghiSettimana >= maxLunghi) {
                throw new TurnoNonDisponibileException("Assegnazione bloccata: " + dipendente
                        + (maxLunghi == 0
                        ? " è abilitato/a solo a turni corti (< 8 h)."
                        : " ha già raggiunto il limite di " + maxLunghi + " turno/i lungo/i da 8 h nella settimana."));
            }
        }

        String nuovoCodice = generaNuovoCodiceTurno(turniEsistenti);
        Turno nuovoTurno = new Turno(nuovoCodice, beanInput.getData(), beanInput.getOraInizio(),
                beanInput.getOraFine(), dipendente);
        nuovoTurno.pubblica();
        nuovoTurno.setBloccato(beanInput.isBloccato());
        nuovoTurno.setGeneratoAutomaticamente(beanInput.isGeneratoAutomaticamente());
        nuovoTurno.setPersonalizzato(beanInput.isPersonalizzato());
        nuovoTurno.setPausaMinuti(beanInput.getPausaMinuti());
        dipendente.aggiungiTurno(nuovoTurno);

        turnoDao.salvaTurno(nuovoTurno);
        notificaDao.inviaNotifica(new Notifica(
                dipendente.getEmail(),
                "Nuovo Turno Pubblicato",
                "Ti e' stato assegnato il turno " + nuovoCodice + " per " + nuovoTurno.getData()
                        + " (" + nuovoTurno.getOraInizio() + "-" + nuovoTurno.getOraFine() + ").",
                oraCorrente()));

        return convertiTurnoInBean(nuovoTurno);
    }

    public void eliminaTurno(String codiceTurno) throws FrostlyException {
        if (codiceTurno == null || codiceTurno.trim().isEmpty()) {
            throw new DatiNonValidiException("Codice turno non valido.");
        }
        turnoDao.eliminaTurno(codiceTurno.trim());
    }

    public TurnoBean modificaOrariTurno(String codiceTurno, String nuovaOraInizio, String nuovaOraFine) throws FrostlyException {
        if (codiceTurno == null || codiceTurno.trim().isEmpty()) {
            throw new DatiNonValidiException("Codice turno non valido.");
        }
        TurnoBean validatore = new TurnoBean();
        validatore.setOraInizio(nuovaOraInizio);
        validatore.setOraFine(nuovaOraFine);

        for (Turno t : turnoDao.caricaTuttiTurni()) {
            if (codiceTurno.trim().equalsIgnoreCase(t.getCodiceTurno())) {
                t.setOraInizio(validatore.getOraInizio());
                t.setOraFine(validatore.getOraFine());
                turnoDao.salvaTurno(t);
                return convertiTurnoInBean(t);
            }
        }
        throw new RisorsaNonTrovataException("Turno non trovato: " + codiceTurno);
    }

    public TurnoBean impostaBloccoTurno(String codiceTurno, boolean bloccato) throws FrostlyException {
        if (codiceTurno == null || codiceTurno.trim().isEmpty()) {
            throw new DatiNonValidiException("Codice turno non valido.");
        }
        for (Turno t : turnoDao.caricaTuttiTurni()) {
            if (codiceTurno.trim().equalsIgnoreCase(t.getCodiceTurno())) {
                t.setBloccato(bloccato);
                turnoDao.salvaTurno(t);
                return convertiTurnoInBean(t);
            }
        }
        throw new RisorsaNonTrovataException("Turno non trovato: " + codiceTurno);
    }

    public java.util.Map<String, model.FabbisognoTurno> caricaFabbisogniTurni() throws FrostlyException {
        return turnoDao.caricaFabbisogni();
    }

    public void salvaFabbisogniTurni(java.util.Map<String, model.FabbisognoTurno> fabbisogni) throws FrostlyException {
        turnoDao.salvaFabbisogni(fabbisogni);
    }

    public model.ConfigurazioneCoperturaFlessibile caricaCoperturaFlessibile() {
        return coperturaFlessibileAttiva.copia();
    }

    public void salvaCoperturaFlessibile(model.ConfigurazioneCoperturaFlessibile nuova) {
        if (nuova != null) {
            coperturaFlessibileAttiva = nuova.copia();
        }
    }

    /**
     * Genera una proposta temporanea di turni settimanali senza modificare i dati ufficiali.
     * Supporta sia la modalita' Standard (catalogo) sia la modalita' Flessibile (orari personalizzati).
     */
    public bean.PropostaTurniSettimanaBean generaPropostaSettimana(
            java.time.LocalDate lunediSettimana,
            model.ConfigurazioneGenerazioneTurni config) throws FrostlyException {
        List<Dipendente> dipendenti = utenteDao.caricaTuttiDipendenti();
        List<TurnoBean> tuttiTurni = visualizzaTuttiTurni();
        if (config != null && config.isFlessibile()) {
            return MotoreTurniFlessibili.generaPropostaFlessibile(
                    lunediSettimana, dipendenti, tuttiTurni, coperturaFlessibileAttiva, config);
        }
        java.util.Map<String, model.FabbisognoTurno> fabbisogni = turnoDao.caricaFabbisogni();
        return MotorePianificazioneTurni.generaProposta(lunediSettimana, dipendenti, tuttiTurni, fabbisogni, config);
    }

    /**
     * Ricalcola in tempo reale ore, copertura e avvisi dopo una modifica manuale effettuata in anteprima.
     */
    public bean.PropostaTurniSettimanaBean rivalutaPropostaModificata(
            bean.PropostaTurniSettimanaBean propostaCorrente,
            List<TurnoBean> turniPropostiModificati) throws FrostlyException {
        java.time.LocalDate lunedi = propostaCorrente.getLunediSettimana();
        List<Dipendente> dipendenti = utenteDao.caricaTuttiDipendenti();
        List<TurnoBean> tuttiTurni = visualizzaTuttiTurni();
        List<TurnoBean> altreSettimane = new ArrayList<>();
        for (TurnoBean t : tuttiTurni) {
            if (!MotorePianificazioneTurni.appartieneASettimana(t, lunedi)) {
                altreSettimane.add(t);
            }
        }
        if (propostaCorrente.getConfigurazioneUsata() != null && propostaCorrente.getConfigurazioneUsata().isFlessibile()) {
            return MotoreTurniFlessibili.rivalutaPropostaFlessibile(
                    lunedi,
                    propostaCorrente.getConfigurazioneUsata(),
                    coperturaFlessibileAttiva,
                    dipendenti,
                    propostaCorrente.getTurniOriginaliSettimana(),
                    turniPropostiModificati,
                    altreSettimane,
                    propostaCorrente.getImprontaVersioneOriginale());
        }
        java.util.Map<String, model.FabbisognoTurno> fabbisogni = turnoDao.caricaFabbisogni();
        return MotorePianificazioneTurni.rivalutaProposta(
                lunedi,
                propostaCorrente.getConfigurazioneUsata(),
                dipendenti,
                propostaCorrente.getTurniOriginaliSettimana(),
                turniPropostiModificati,
                altreSettimane,
                fabbisogni,
                propostaCorrente.getImprontaVersioneOriginale());
    }

    /**
     * Conferma e salva in modo atomico una proposta validata, verificando che la settimana ufficiale
     * non sia stata modificata nel frattempo e salvando uno snapshot per eventuale ripristino.
     */
    public void confermaEApplicaProposta(bean.PropostaTurniSettimanaBean proposta) throws FrostlyException {
        if (proposta == null || proposta.getLunediSettimana() == null) {
            throw new DatiNonValidiException("Proposta di pianificazione non valida.");
        }
        if (proposta.isHaViolazioniInderogabili()) {
            throw new FrostlyException("Impossibile approvare la pianificazione: sono presenti violazioni di vincoli inderogabili.");
        }
        java.time.LocalDate lunedi = proposta.getLunediSettimana().with(java.time.DayOfWeek.MONDAY);
        List<TurnoBean> tuttiAttuali = visualizzaTuttiTurni();
        String improntaAttuale = MotorePianificazioneTurni.calcolaImprontaSettimana(tuttiAttuali, lunedi);
        if (proposta.getImprontaVersioneOriginale() != null
                && !proposta.getImprontaVersioneOriginale().equals(improntaAttuale)) {
            throw new FrostlyException("La pianificazione ufficiale della settimana e' stata modificata durante "
                    + "la generazione. Rigenera la proposta per evitare sovrascritture accidentali.");
        }

        // Salva snapshot della settimana precedente per consentire il ripristino
        List<TurnoBean> snapshot = new ArrayList<>();
        List<String> codiciDaRimuovere = new ArrayList<>();
        for (TurnoBean t : tuttiAttuali) {
            if (MotorePianificazioneTurni.appartieneASettimana(t, lunedi)) {
                snapshot.add(t);
                if (t.getCodiceTurno() != null) {
                    codiciDaRimuovere.add(t.getCodiceTurno());
                }
            }
        }

        // Converte i turni proposti in Entity garantendo codici univoci senza mai sovrascrivere turni
        List<Turno> nuoviTurniEntity = new ArrayList<>();
        java.util.Set<String> chiaviUnivoche = new java.util.HashSet<>();
        java.util.Set<String> codiciUsati = new java.util.HashSet<>();
        java.util.Set<String> setRimossi = new java.util.HashSet<>();
        for (String codRem : codiciDaRimuovere) {
            setRimossi.add(codRem.toUpperCase());
        }
        int progressivo = 1;
        for (TurnoBean t : tuttiAttuali) {
            if (t.getCodiceTurno() != null && !t.getCodiceTurno().isBlank()) {
                String upper = t.getCodiceTurno().trim().toUpperCase();
                if (!setRimossi.contains(upper)) {
                    codiciUsati.add(upper);
                }
                if (upper.startsWith("TUR-")) {
                    try {
                        int n = Integer.parseInt(upper.substring(4).replaceAll("\\D", ""));
                        if (n >= progressivo) {
                            progressivo = n + 1;
                        }
                    } catch (NumberFormatException ignored) {
                        // Ignora formati non numerici
                    }
                }
            }
        }

        for (TurnoBean tb : proposta.getTurniProposti()) {
            Dipendente dip = utenteDao.trovaDipendentePerEmail(tb.getEmailDipendente());
            if (dip == null) {
                throw new RisorsaNonTrovataException("Dipendente non trovato: " + tb.getEmailDipendente());
            }
            String chiave = (tb.getData() + "|" + tb.getOraInizio() + "|" + tb.getOraFine() + "|" + dip.getEmail()).toLowerCase();
            if (!chiaviUnivoche.add(chiave)) {
                continue;
            }
            String codice = null;
            if (tb.getCodiceTurno() != null && !tb.getCodiceTurno().isBlank()
                    && !tb.getCodiceTurno().toUpperCase().startsWith("TMP-")) {
                String cand = tb.getCodiceTurno().trim();
                if (codiciUsati.add(cand.toUpperCase())) {
                    codice = cand;
                }
            }
            if (codice == null) {
                do {
                    codice = String.format("TUR-%03d", progressivo++);
                } while (!codiciUsati.add(codice.toUpperCase()));
            }
            Turno entity = new Turno(codice, tb.getData(), tb.getOraInizio(), tb.getOraFine(), dip);
            entity.pubblica();
            entity.setBloccato(tb.isBloccato());
            entity.setGeneratoAutomaticamente(tb.isGeneratoAutomaticamente());
            entity.setPersonalizzato(tb.isPersonalizzato());
            entity.setPausaMinuti(tb.getPausaMinuti());
            nuoviTurniEntity.add(entity);
        }

        turnoDao.sostituisciTurniAtomico(codiciDaRimuovere, nuoviTurniEntity);
        SNAPSHOT_PRECEDENTI.put(lunedi, snapshot);
    }

    public boolean haSnapshotRipristinabile(java.time.LocalDate lunediSettimana) {
        if (lunediSettimana == null) {
            return false;
        }
        return SNAPSHOT_PRECEDENTI.containsKey(lunediSettimana.with(java.time.DayOfWeek.MONDAY));
    }

    public void ripristinaUltimaPianificazione(java.time.LocalDate lunediSettimana) throws FrostlyException {
        java.time.LocalDate lunedi = lunediSettimana.with(java.time.DayOfWeek.MONDAY);
        List<TurnoBean> snapshot = SNAPSHOT_PRECEDENTI.get(lunedi);
        if (snapshot == null) {
            throw new RisorsaNonTrovataException("Nessuna pianificazione precedente disponibile da ripristinare per questa settimana.");
        }
        List<TurnoBean> tuttiAttuali = visualizzaTuttiTurni();
        List<String> codiciDaRimuovere = new ArrayList<>();
        for (TurnoBean t : tuttiAttuali) {
            if (MotorePianificazioneTurni.appartieneASettimana(t, lunedi) && t.getCodiceTurno() != null) {
                codiciDaRimuovere.add(t.getCodiceTurno());
            }
        }
        List<Turno> daRipristinare = new ArrayList<>();
        for (TurnoBean tb : snapshot) {
            Dipendente dip = utenteDao.trovaDipendentePerEmail(tb.getEmailDipendente());
            if (dip != null) {
                Turno entity = new Turno(tb.getCodiceTurno(), tb.getData(), tb.getOraInizio(), tb.getOraFine(), dip);
                entity.pubblica();
                entity.setBloccato(tb.isBloccato());
                entity.setGeneratoAutomaticamente(tb.isGeneratoAutomaticamente());
                daRipristinare.add(entity);
            }
        }
        turnoDao.sostituisciTurniAtomico(codiciDaRimuovere, daRipristinare);
        SNAPSHOT_PRECEDENTI.remove(lunedi);
    }

    /**
     * Invia una richiesta di permesso/indisponibilita' da parte di un dipendente per una data specifica.
     * La richiesta rimane in stato IN_ATTESA e NON modifica i vincoli di pianificazione finche' il Maestro non approva.
     */
    public model.RichiestaIndisponibilita inviaRichiestaIndisponibilita(String emailDipendente,
                                                                        java.time.LocalDate data,
                                                                        String motivazione) throws FrostlyException {
        if (emailDipendente == null || emailDipendente.isBlank()) {
            throw new DatiNonValidiException("Dipendente non valido.");
        }
        if (data == null) {
            throw new DatiNonValidiException("Seleziona la data per cui richiedere l'indisponibilità.");
        }
        if (motivazione == null || motivazione.trim().isEmpty()) {
            throw new DatiNonValidiException("Inserisci una breve motivazione per la richiesta.");
        }
        Dipendente dip = utenteDao.trovaDipendentePerEmail(emailDipendente.trim());
        if (dip == null) {
            throw new RisorsaNonTrovataException("Dipendente non trovato: " + emailDipendente);
        }

        List<model.RichiestaIndisponibilita> esistenti = emergenzaDao.caricaRichiesteIndisponibilita();
        for (model.RichiestaIndisponibilita r : esistenti) {
            if (r.getEmailDipendente().equalsIgnoreCase(dip.getEmail())
                    && data.equals(r.getDataRichiesta())
                    && r.getStato() == model.RichiestaIndisponibilita.StatoRichiesta.IN_ATTESA) {
                throw new DatiNonValidiException("Esiste già una richiesta in attesa per questa data.");
            }
        }

        int max = 0;
        for (model.RichiestaIndisponibilita r : esistenti) {
            if (r.getCodiceRichiesta() != null && r.getCodiceRichiesta().startsWith("IND-")) {
                try {
                    int n = Integer.parseInt(r.getCodiceRichiesta().substring(4).trim());
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {
                    // Ignora formati non numerici
                }
            }
        }
        String codice = String.format("IND-%03d", max + 1);
        model.RichiestaIndisponibilita nuova = new model.RichiestaIndisponibilita(
                codice, dip.getEmail(), dip.getNome() + " " + dip.getCognome(), data, motivazione.trim());
        emergenzaDao.salvaRichiestaIndisponibilita(nuova);

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        notificaDao.inviaNotifica(new Notifica(
                utenteDao.getMaestroGelataio().getEmail(),
                "Richiesta Permesso / Indisponibilità da " + dip,
                dip + " ha richiesto di non essere messo in turno il " + data.format(fmt)
                        + " (" + codice + "): " + motivazione.trim()
                        + ". Accedi a Turni Staff → Permessi per approvare o rifiutare.",
                oraCorrente()));
        return nuova;
    }

    public List<model.RichiestaIndisponibilita> caricaRichiesteIndisponibilita(String emailFiltroOpzionale) {
        List<model.RichiestaIndisponibilita> tutte = emergenzaDao.caricaRichiesteIndisponibilita();
        if (emailFiltroOpzionale == null || emailFiltroOpzionale.isBlank()) {
            return tutte;
        }
        List<model.RichiestaIndisponibilita> filtrate = new ArrayList<>();
        for (model.RichiestaIndisponibilita r : tutte) {
            if (emailFiltroOpzionale.equalsIgnoreCase(r.getEmailDipendente())) {
                filtrate.add(r);
            }
        }
        return filtrate;
    }

    public bean.RichiestaIndisponibilitaBean inviaRichiestaIndisponibilitaBean(String emailDipendente,
                                                                               java.time.LocalDate dataRichiesta,
                                                                               String motivazione) throws FrostlyException {
        model.RichiestaIndisponibilita r = inviaRichiestaIndisponibilita(emailDipendente, dataRichiesta, motivazione);
        return new bean.RichiestaIndisponibilitaBean(
                r.getCodiceRichiesta(),
                r.getEmailDipendente(),
                r.getNomeDipendente(),
                r.getDataRichiesta(),
                r.getMotivazione(),
                r.getStato() != null ? r.getStato().name() : "IN_ATTESA",
                r.getNotaMaestro());
    }

    public List<bean.RichiestaIndisponibilitaBean> caricaRichiesteIndisponibilitaBean(String emailFiltroOpzionale) {
        List<bean.RichiestaIndisponibilitaBean> out = new ArrayList<>();
        for (model.RichiestaIndisponibilita r : caricaRichiesteIndisponibilita(emailFiltroOpzionale)) {
            out.add(new bean.RichiestaIndisponibilitaBean(
                    r.getCodiceRichiesta(),
                    r.getEmailDipendente(),
                    r.getNomeDipendente(),
                    r.getDataRichiesta(),
                    r.getMotivazione(),
                    r.getStato() != null ? r.getStato().name() : "IN_ATTESA",
                    r.getNotaMaestro()));
        }
        return out;
    }

    public int contaRichiesteIndisponibilitaInAttesa() {
        int c = 0;
        for (model.RichiestaIndisponibilita r : emergenzaDao.caricaRichiesteIndisponibilita()) {
            if (r.getStato() == model.RichiestaIndisponibilita.StatoRichiesta.IN_ATTESA) {
                c++;
            }
        }
        return c;
    }

    /**
     * Valuta (approva o rifiuta) una richiesta di indisponibilita'.
     * Solo in caso di approvazione la data viene registrata nelle ferie/permessi del dipendente
     * bloccando l'assegnazione nei turni futuri e nel generatore automatico.
     */
    public String valutaRichiestaIndisponibilita(String codiceRichiesta,
                                                 boolean approva,
                                                 String notaMaestro) throws FrostlyException {
        if (codiceRichiesta == null || codiceRichiesta.isBlank()) {
            throw new DatiNonValidiException("Codice richiesta non valido.");
        }
        model.RichiestaIndisponibilita trovata = null;
        for (model.RichiestaIndisponibilita r : emergenzaDao.caricaRichiesteIndisponibilita()) {
            if (codiceRichiesta.trim().equalsIgnoreCase(r.getCodiceRichiesta())) {
                trovata = r;
                break;
            }
        }
        if (trovata == null) {
            throw new RisorsaNonTrovataException("Richiesta non trovata: " + codiceRichiesta);
        }

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataFmt = trovata.getDataRichiesta().format(fmt);
        String nota = notaMaestro != null ? notaMaestro.trim() : "";

        if (approva) {
            trovata.approva(nota);
            emergenzaDao.salvaRichiestaIndisponibilita(trovata);

            Dipendente dip = utenteDao.trovaDipendentePerEmail(trovata.getEmailDipendente());
            boolean turnoGiaPresente = false;
            if (dip != null) {
                model.DisponibilitaDipendente disp = dip.getDisponibilita();
                disp.aggiungiFeriePermesso(trovata.getDataRichiesta());
                utenteDao.aggiornaConfigurazioneDipendente(
                        dip.getEmail(), dip.getConfigurazioneOre(), disp, dip.getMansioni(), dip.isAttivo());

                for (TurnoBean tb : visualizzaTurniDipendente(dip.getEmail())) {
                    java.time.LocalDate dTurno = CalcolatoreOreSettimanali.estraiDataTurno(
                            tb.getData(), trovata.getDataRichiesta().with(java.time.DayOfWeek.MONDAY));
                    if (trovata.getDataRichiesta().equals(dTurno)) {
                        turnoGiaPresente = true;
                        break;
                    }
                }
            }

            notificaDao.inviaNotifica(new Notifica(
                    trovata.getEmailDipendente(),
                    "Permesso Approvato (" + dataFmt + ")",
                    "La tua richiesta di indisponibilità per il " + dataFmt + " è stata approvata"
                            + (nota.isEmpty() ? "." : " — Nota: " + nota),
                    oraCorrente()));

            return "✅ Richiesta " + trovata.getCodiceRichiesta() + " approvata (" + trovata.getNomeDipendente()
                    + " non verrà messo in turno il " + dataFmt + ")."
                    + (turnoGiaPresente ? " ⚠️ Attenzione: ha già un turno salvato in quella data, rimuovilo o rigenera la settimana." : "");
        } else {
            trovata.rifiuta(nota);
            emergenzaDao.salvaRichiestaIndisponibilita(trovata);

            notificaDao.inviaNotifica(new Notifica(
                    trovata.getEmailDipendente(),
                    "Richiesta Permesso Non Approvata (" + dataFmt + ")",
                    "La tua richiesta di indisponibilità per il " + dataFmt + " non è stata approvata"
                            + (nota.isEmpty() ? "." : " — Nota: " + nota),
                    oraCorrente()));

            return "✕ Richiesta " + trovata.getCodiceRichiesta() + " rifiutata: la disponibilità del dipendente resta invariata.";
        }
    }

    public List<bean.RiepilogoOreDipendenteBean> calcolaRiepiloghiOreSettimana(java.time.LocalDate lunediSettimana)
            throws FrostlyException {
        List<Dipendente> dipendenti = utenteDao.caricaTuttiDipendenti();
        List<TurnoBean> tuttiTurni = visualizzaTuttiTurni();
        return CalcolatoreOreSettimanali.calcolaRiepiloghiSettimana(dipendenti, tuttiTurni, lunediSettimana);
    }

    public CalcolatoreOreSettimanali.AnteprimaAssegnazione calcolaAnteprimaAssegnazione(
            String emailDipendente,
            java.time.LocalDate lunediSettimana,
            java.time.LocalDate dataTurno,
            String oraInizio,
            String oraFine) throws FrostlyException {
        Dipendente dip = utenteDao.trovaDipendentePerEmail(emailDipendente);
        if (dip == null) {
            throw new RisorsaNonTrovataException("Dipendente non trovato: " + emailDipendente);
        }
        List<TurnoBean> tuttiTurni = visualizzaTuttiTurni();
        return CalcolatoreOreSettimanali.calcolaAnteprimaAssegnazione(
                dip, tuttiTurni, lunediSettimana, dataTurno, oraInizio, oraFine);
    }

    public EmergenzaBean segnalaEmergenzaTurno(EmergenzaBean beanInput) throws FrostlyException {
        List<Turno> turni = turnoDao.caricaTuttiTurni();
        List<Emergenza> emergenze = emergenzaDao.caricaTutteEmergenze();
        Dipendente richiedente = utenteDao.trovaDipendentePerEmail(beanInput.getEmailRichiedente());

        Turno turnoCoinvolto = null;
        for (Turno t : turni) {
            if (t.getCodiceTurno().equalsIgnoreCase(beanInput.getCodiceTurno())) {
                turnoCoinvolto = t;
                break;
            }
        }
        if (turnoCoinvolto == null) {
            throw new RisorsaNonTrovataException("Turno non trovato: " + beanInput.getCodiceTurno());
        }

        turnoCoinvolto.dichiaraEmergenza();
        String codiceEmergenza = generaNuovoCodiceEmergenza(emergenze);
        Emergenza nuovaEmergenza = new Emergenza(codiceEmergenza, turnoCoinvolto, richiedente, beanInput.getMotivazione());

        turnoDao.salvaTurno(turnoCoinvolto);
        emergenzaDao.salvaEmergenza(nuovaEmergenza);

        notificaDao.inviaNotifica(new Notifica(
                utenteDao.getMaestroGelataio().getEmail(),
                "Emergenza Turno da " + richiedente,
                "Segnalata emergenza " + codiceEmergenza + " sul turno " + turnoCoinvolto.getCodiceTurno()
                        + " (" + turnoCoinvolto.getData() + " " + turnoCoinvolto.getOraInizio() + "-"
                        + turnoCoinvolto.getOraFine() + "): " + beanInput.getMotivazione(),
                oraCorrente()));

        return convertiEmergenzaInBean(nuovaEmergenza);
    }

    public EmergenzaBean approvaEmergenzaConSostituto(String codiceEmergenza, String emailSostituto, String notaMaestro)
            throws FrostlyException {
        Emergenza emergenza = trovaEmergenzaEntity(codiceEmergenza);
        Dipendente sostituto = utenteDao.trovaDipendentePerEmail(emailSostituto);
        if (sostituto == null) {
            throw new TurnoNonDisponibileException("Selezionare un dipendente sostituto valido.");
        }

        Turno turno = emergenza.getTurno();
        if (!sostituto.isDisponibile(turno.getData(), turno.getOraInizio(), turno.getOraFine())) {
            throw new TurnoNonDisponibileException("Il dipendente " + sostituto
                    + " non e' disponibile in quella fascia oraria.");
        }

        Dipendente richiedenteOriginale = emergenza.getRichiedente();
        emergenza.approvaConSostituto(sostituto, notaMaestro);

        turnoDao.salvaTurno(turno);
        emergenzaDao.salvaEmergenza(emergenza);

        notificaDao.inviaNotifica(new Notifica(
                richiedenteOriginale.getEmail(),
                "Emergenza Approvata - Turno Coperto",
                "La tua emergenza " + codiceEmergenza + " per il turno " + turno.getCodiceTurno()
                        + " e' stata approvata. Sostituto: " + sostituto + ". Nota: " + notaMaestro,
                oraCorrente()));

        notificaDao.inviaNotifica(new Notifica(
                sostituto.getEmail(),
                "Nuova Assegnazione Sostituzione Turno",
                "Ti e' stato assegnato in sostituzione il turno " + turno.getCodiceTurno()
                        + " (" + turno.getData() + " " + turno.getOraInizio() + "-" + turno.getOraFine() + ").",
                oraCorrente()));

        return convertiEmergenzaInBean(emergenza);
    }

    public EmergenzaBean rifiutaEmergenza(String codiceEmergenza, String motivazioneRifiuto)
            throws FrostlyException {
        Emergenza emergenza = trovaEmergenzaEntity(codiceEmergenza);

        emergenza.rifiuta(motivazioneRifiuto);
        turnoDao.salvaTurno(emergenza.getTurno());
        emergenzaDao.salvaEmergenza(emergenza);

        notificaDao.inviaNotifica(new Notifica(
                emergenza.getRichiedente().getEmail(),
                "Emergenza Rifiutata",
                "La tua richiesta di emergenza " + codiceEmergenza + " per il turno "
                        + emergenza.getTurno().getCodiceTurno() + " non e' stata approvata. Motivo: " + motivazioneRifiuto,
                oraCorrente()));

        return convertiEmergenzaInBean(emergenza);
    }

    public List<EmergenzaBean> visualizzaTutteEmergenze() {
        List<Emergenza> lista = emergenzaDao.caricaTutteEmergenze();
        List<EmergenzaBean> risultato = new ArrayList<>();
        for (Emergenza e : lista) {
            risultato.add(convertiEmergenzaInBean(e));
        }
        return risultato;
    }

    public List<String> getEmailTuttiDipendenti() {
        List<String> emails = new ArrayList<>();
        for (Dipendente d : utenteDao.caricaTuttiDipendenti()) {
            emails.add(d.getEmail());
        }
        return emails;
    }

    public List<String> getDipendentiFormattati() {
        List<String> risultato = new ArrayList<>();
        for (Dipendente d : utenteDao.caricaTuttiDipendenti()) {
            risultato.add(d.getNome() + " " + d.getCognome() + " | " + d.getEmail());
        }
        return risultato;
    }

    public EmergenzaBean trovaEmergenzaAttivaPerTurno(String codiceTurno) {
        if (codiceTurno == null) return null;
        for (Emergenza e : emergenzaDao.caricaTutteEmergenze()) {
            if (e.getTurno() != null
                    && codiceTurno.equalsIgnoreCase(e.getTurno().getCodiceTurno())
                    && "IN_ATTESA".equalsIgnoreCase(e.getStato().name())) {
                return convertiEmergenzaInBean(e);
            }
        }
        return null;
    }

    private boolean haConflittoInLista(List<Turno> lista, String emailDip, String data, String inizio, String fine) {
        for (Turno t : lista) {
            if (t.getDipendenteAssegnato() != null
                    && t.getDipendenteAssegnato().getEmail().equalsIgnoreCase(emailDip)
                    && t.getData() != null
                    && t.getData().equalsIgnoreCase(data)
                    && t.isInConflitto(inizio, fine)) {
                return true;
            }
        }
        return false;
    }

    private String generaNuovoCodiceTurno(List<Turno> turniEsistenti) {
        int max = 0;
        for (Turno t : turniEsistenti) {
            if (t.getCodiceTurno() != null && t.getCodiceTurno().startsWith("TRN-")) {
                try {
                    int num = Integer.parseInt(t.getCodiceTurno().substring(4).trim());
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignora formati non numerici
                }
            }
        }
        return String.format("TRN-%02d", max + 1);
    }

    private String generaNuovoCodiceEmergenza(List<Emergenza> emergenzeEsistenti) {
        int max = 0;
        for (Emergenza e : emergenzeEsistenti) {
            if (e.getCodiceEmergenza() != null && e.getCodiceEmergenza().startsWith("EMG-")) {
                try {
                    int num = Integer.parseInt(e.getCodiceEmergenza().substring(4).trim());
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignora formati non numerici
                }
            }
        }
        return String.format("EMG-%02d", max + 1);
    }

    private Emergenza trovaEmergenzaEntity(String codiceEmergenza) throws RisorsaNonTrovataException {
        for (Emergenza e : emergenzaDao.caricaTutteEmergenze()) {
            if (e.getCodiceEmergenza().equalsIgnoreCase(codiceEmergenza)) {
                return e;
            }
        }
        throw new RisorsaNonTrovataException("Emergenza non trovata: " + codiceEmergenza);
    }

    private TurnoBean convertiTurnoInBean(Turno t) {
        TurnoBean b = new TurnoBean();
        try {
            b.setCodiceTurno(t.getCodiceTurno());
            b.setData(t.getData());
            b.setOraInizio(t.getOraInizio());
            b.setOraFine(t.getOraFine());
            if (t.getDipendenteAssegnato() != null) {
                b.setEmailDipendente(t.getDipendenteAssegnato().getEmail());
                b.setNomeDipendente(t.getDipendenteAssegnato().toString());
            } else {
                b.setEmailDipendente("non.assegnato@frostly.it");
                b.setNomeDipendente("Personale (Non assegnato)");
            }
            b.setStato(t.getStato() != null ? t.getStato().name() : "PIANIFICATO");
            b.setBloccato(t.isBloccato());
            b.setGeneratoAutomaticamente(t.isGeneratoAutomaticamente());
            b.setPersonalizzato(t.isPersonalizzato());
            b.setPausaMinuti(t.getPausaMinuti());
        } catch (DatiNonValidiException ignored) {
        }
        return b;
    }

    private EmergenzaBean convertiEmergenzaInBean(Emergenza e) {
        EmergenzaBean b = new EmergenzaBean();
        try {
            b.setCodiceEmergenza(e.getCodiceEmergenza());
            b.setCodiceTurno(e.getTurno().getCodiceTurno());
            b.setDataTurno(e.getTurno().getData());
            b.setOrarioTurno(e.getTurno().getOraInizio() + "-" + e.getTurno().getOraFine());
            b.setEmailRichiedente(e.getRichiedente().getEmail());
            b.setNomeRichiedente(e.getRichiedente().toString());
            if (e.getSostituto() != null) {
                b.setEmailSostituto(e.getSostituto().getEmail());
                b.setNomeSostituto(e.getSostituto().toString());
            } else {
                b.setEmailSostituto("-");
                b.setNomeSostituto("Nessuno");
            }
            b.setMotivazione(e.getMotivazione());
            b.setStato(e.getStato().name());
            b.setNotaRisposta(e.getNotaRisposta());
        } catch (DatiNonValidiException ignored) {
        }
        return b;
    }

    private String oraCorrente() {
        return "Oggi " + LocalTime.now().format(TIME_FMT);
    }
}
