package control.applicativo;

import bean.LoginBean;
import bean.NotificaBean;
import dao.DAOFactory;
import dao.NotificaDAO;
import dao.UtenteDAO;
import frostly.exception.CredenzialiErrateException;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import frostly.exception.OperazioneNonAutorizzataException;
import frostly.exception.RisorsaNonTrovataException;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.MaestroGelataio;
import model.Notifica;
import util.SessionManager;

/**
 * Controller applicativo STATELESS per l'autenticazione (Sign In),
 * la registrazione di nuovi utenti (Sign Up) e il recupero delle notifiche personali.
 */
public class LoginControl {

    public static final String RUOLO_MAESTRO = "MAESTRO_GELATAIO";
    public static final String RUOLO_DIPENDENTE = "DIPENDENTE";
    public static final String RUOLO_CLIENTE = "CLIENTE";

    private final UtenteDAO utenteDAO;
    private final NotificaDAO notificaDAO;

    public LoginControl() {
        this(DAOFactory.getUtenteDAO(), DAOFactory.getNotificaDAO());
    }

    public LoginControl(UtenteDAO utenteDAO, NotificaDAO notificaDAO) {
        this.utenteDAO = utenteDAO;
        this.notificaDAO = notificaDAO;
    }

    public LoginBean autenticaUtente(LoginBean credenzialiBean) throws CredenzialiErrateException {
        String email = credenzialiBean.getEmail();
        String password = credenzialiBean.getPassword();

        MaestroGelataio maestro = utenteDAO.getMaestroGelataio();
        if (maestro.autenticati(email, password)) {
            credenzialiBean.setRuoloAutenticato(RUOLO_MAESTRO);
            credenzialiBean.setNomeCompleto(maestro.getNome() + " " + maestro.getCognome());
            SessionManager.getInstance().impostaSessione(email, RUOLO_MAESTRO, credenzialiBean.getNomeCompleto());
            return credenzialiBean;
        }

        for (Dipendente dip : utenteDAO.caricaTuttiDipendenti()) {
            if (dip.autenticati(email, password)) {
                credenzialiBean.setRuoloAutenticato(RUOLO_DIPENDENTE);
                credenzialiBean.setNomeCompleto(dip.getNome() + " " + dip.getCognome());
                SessionManager.getInstance().impostaSessione(email, RUOLO_DIPENDENTE, credenzialiBean.getNomeCompleto());
                return credenzialiBean;
            }
        }

        for (Cliente cli : utenteDAO.caricaTuttiClienti()) {
            if (cli.autenticati(email, password)) {
                credenzialiBean.setRuoloAutenticato(RUOLO_CLIENTE);
                credenzialiBean.setNomeCompleto(cli.getNome() + " " + cli.getCognome());
                SessionManager.getInstance().impostaSessione(email, RUOLO_CLIENTE, credenzialiBean.getNomeCompleto());
                return credenzialiBean;
            }
        }

        throw new CredenzialiErrateException("Credenziali errate: nessun utente trovato con questa email/password.");
    }

    /**
     * Registrazione pubblica (Sign Up): crea SEMPRE un profilo CLIENTE, qualunque ruolo sia indicato nel bean.
     * I profili dipendente possono essere creati solo dal Maestro con {@link #creaProfiloDipendente}.
     */
    public LoginBean registraUtente(LoginBean registrazioneBean) throws FrostlyException {
        utenteDAO.registraNuovoUtente(
                registrazioneBean.getNome(),
                registrazioneBean.getCognome(),
                registrazioneBean.getEmail(),
                registrazioneBean.getPassword(),
                RUOLO_CLIENTE);
        registrazioneBean.setNomeCompleto(registrazioneBean.getNome() + " " + registrazioneBean.getCognome());
        registrazioneBean.setRuoloAutenticato(RUOLO_CLIENTE);
        return registrazioneBean;
    }

    /** Creazione di un profilo dipendente con monte ore obbligatorio e disponibilità. */
    public LoginBean creaProfiloDipendente(LoginBean dipendenteBean,
                                           model.ConfigurazioneOreDipendente configurazioneOre,
                                           model.DisponibilitaDipendente disponibilita) throws FrostlyException {
        return creaProfiloDipendente(dipendenteBean, configurazioneOre, disponibilita, null);
    }

    /** Creazione di un profilo dipendente con monte ore obbligatorio, disponibilità e limite opzionale max turni. */
    public LoginBean creaProfiloDipendente(LoginBean dipendenteBean,
                                           model.ConfigurazioneOreDipendente configurazioneOre,
                                           model.DisponibilitaDipendente disponibilita,
                                           Integer maxTurniSettimanali) throws FrostlyException {
        if (!RUOLO_MAESTRO.equals(SessionManager.getInstance().getRuoloUtenteLoggato())) {
            throw new OperazioneNonAutorizzataException("Operazione non consentita: solo il Maestro puo' creare profili dipendente.");
        }
        if (configurazioneOre == null || !configurazioneOre.isConfigurato()) {
            throw new DatiNonValidiException("Il monte ore settimanale è obbligatorio: imposta ore fisse o un intervallo valido.");
        }
        if (maxTurniSettimanali != null && maxTurniSettimanali <= 0) {
            throw new DatiNonValidiException("Il limite massimo di turni settimanali deve essere un numero intero positivo.");
        }
        utenteDAO.registraNuovoUtente(
                dipendenteBean.getNome(),
                dipendenteBean.getCognome(),
                dipendenteBean.getEmail(),
                dipendenteBean.getPassword(),
                RUOLO_DIPENDENTE);
        utenteDAO.aggiornaConfigurazioneDipendente(
                dipendenteBean.getEmail(),
                configurazioneOre,
                disponibilita != null ? disponibilita : model.DisponibilitaDipendente.tuttoDisponibile(),
                null,
                Boolean.TRUE,
                maxTurniSettimanali);
        dipendenteBean.setNomeCompleto(dipendenteBean.getNome() + " " + dipendenteBean.getCognome());
        dipendenteBean.setRuoloAutenticato(RUOLO_DIPENDENTE);
        return dipendenteBean;
    }

    /** Creazione di un profilo dipendente: consentita solo al Maestro Gelataio autenticato. */
    public LoginBean creaProfiloDipendente(LoginBean dipendenteBean) throws FrostlyException {
        if (!RUOLO_MAESTRO.equals(SessionManager.getInstance().getRuoloUtenteLoggato())) {
            throw new OperazioneNonAutorizzataException("Operazione non consentita: solo il Maestro puo' creare profili dipendente.");
        }
        utenteDAO.registraNuovoUtente(
                dipendenteBean.getNome(),
                dipendenteBean.getCognome(),
                dipendenteBean.getEmail(),
                dipendenteBean.getPassword(),
                RUOLO_DIPENDENTE);
        dipendenteBean.setNomeCompleto(dipendenteBean.getNome() + " " + dipendenteBean.getCognome());
        dipendenteBean.setRuoloAutenticato(RUOLO_DIPENDENTE);
        return dipendenteBean;
    }

    public void aggiornaConfigurazioneDipendente(String emailDipendente,
                                                 model.ConfigurazioneOreDipendente configurazioneOre,
                                                 model.DisponibilitaDipendente disponibilita) throws FrostlyException {
        aggiornaConfigurazioneDipendente(emailDipendente, configurazioneOre, disponibilita, null, null);
    }

    public void aggiornaConfigurazioneDipendente(String emailDipendente,
                                                 model.ConfigurazioneOreDipendente configurazioneOre,
                                                 model.DisponibilitaDipendente disponibilita,
                                                 java.util.Set<model.MansioneDipendente> mansioni,
                                                 Boolean attivo) throws FrostlyException {
        Dipendente esistente = emailDipendente != null ? utenteDAO.trovaDipendentePerEmail(emailDipendente.trim()) : null;
        aggiornaConfigurazioneDipendente(emailDipendente, configurazioneOre, disponibilita, mansioni, attivo,
                esistente != null ? esistente.getMaxTurniSettimanali() : null);
    }

    public void aggiornaConfigurazioneDipendente(String emailDipendente,
                                                 model.ConfigurazioneOreDipendente configurazioneOre,
                                                 model.DisponibilitaDipendente disponibilita,
                                                 java.util.Set<model.MansioneDipendente> mansioni,
                                                 Boolean attivo,
                                                 Integer maxTurniSettimanali) throws FrostlyException {
        if (!RUOLO_MAESTRO.equals(SessionManager.getInstance().getRuoloUtenteLoggato())) {
            throw new OperazioneNonAutorizzataException("Operazione non consentita: solo il Maestro può modificare i profili dipendente.");
        }
        if (emailDipendente == null || emailDipendente.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona un dipendente valido.");
        }
        if (configurazioneOre == null || !configurazioneOre.isConfigurato()) {
            throw new DatiNonValidiException("Il monte ore settimanale deve essere configurato con valori validi.");
        }
        if (maxTurniSettimanali != null && maxTurniSettimanali <= 0) {
            throw new DatiNonValidiException("Il limite massimo di turni settimanali deve essere un numero intero positivo.");
        }
        Dipendente esistente = utenteDAO.trovaDipendentePerEmail(emailDipendente.trim());
        if (esistente == null) {
            throw new RisorsaNonTrovataException("Nessun dipendente trovato con l'email: " + emailDipendente.trim());
        }
        utenteDAO.aggiornaConfigurazioneDipendente(
                emailDipendente.trim(),
                configurazioneOre,
                disponibilita != null ? disponibilita : model.DisponibilitaDipendente.tuttoDisponibile(),
                mansioni != null && !mansioni.isEmpty() ? mansioni : esistente.getMansioni(),
                attivo != null ? attivo : esistente.isAttivo(),
                maxTurniSettimanali);
    }

    /**
     * Disattiva in sicurezza l'account di un dipendente preservando i turni storici collegati.
     */
    public void impostaStatoAttivoDipendente(String emailDipendente, boolean attivo) throws FrostlyException {
        if (!RUOLO_MAESTRO.equals(SessionManager.getInstance().getRuoloUtenteLoggato())) {
            throw new OperazioneNonAutorizzataException("Operazione non consentita: solo il Maestro può modificare lo stato di un dipendente.");
        }
        if (emailDipendente == null || emailDipendente.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona un dipendente valido.");
        }
        Dipendente esistente = utenteDAO.trovaDipendentePerEmail(emailDipendente.trim());
        if (esistente == null) {
            throw new RisorsaNonTrovataException("Nessun dipendente trovato con l'email: " + emailDipendente.trim());
        }
        utenteDAO.aggiornaConfigurazioneDipendente(
                esistente.getEmail(),
                esistente.getConfigurazioneOre(),
                esistente.getDisponibilita(),
                esistente.getMansioni(),
                attivo,
                esistente.getMaxTurniSettimanali());
    }

    public Dipendente trovaDipendentePerEmail(String email) {
        return utenteDAO.trovaDipendentePerEmail(email);
    }

    public List<Dipendente> caricaTuttiDipendenti() {
        return utenteDAO.caricaTuttiDipendenti();
    }

    public bean.DipendenteBean trovaDipendenteBeanPerEmail(String email) {
        Dipendente d = utenteDAO.trovaDipendentePerEmail(email);
        return convertiInDipendenteBean(d);
    }

    public List<bean.DipendenteBean> caricaTuttiDipendentiBean() {
        List<bean.DipendenteBean> out = new ArrayList<>();
        for (Dipendente d : utenteDAO.caricaTuttiDipendenti()) {
            bean.DipendenteBean b = convertiInDipendenteBean(d);
            if (b != null) {
                out.add(b);
            }
        }
        return out;
    }

    private static bean.DipendenteBean convertiInDipendenteBean(Dipendente d) {
        if (d == null) {
            return null;
        }
        return new bean.DipendenteBean(
                d.getNome(),
                d.getCognome(),
                d.getEmail(),
                d.isAttivo(),
                d.getMaxTurniSettimanali(),
                d.getConfigurazioneOre(),
                d.getDisponibilita(),
                d.getMansioni());
    }

    /** Eliminazione di un account dipendente: consentita esclusivamente al Maestro. */
    public void eliminaProfiloDipendente(String emailDipendente) throws FrostlyException {
        if (!RUOLO_MAESTRO.equals(SessionManager.getInstance().getRuoloUtenteLoggato())) {
            throw new OperazioneNonAutorizzataException("Operazione non consentita: solo il Maestro puo' eliminare profili dipendente.");
        }
        if (emailDipendente == null || emailDipendente.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona il dipendente da eliminare.");
        }
        if (utenteDAO.trovaDipendentePerEmail(emailDipendente.trim()) == null) {
            throw new RisorsaNonTrovataException("Nessun dipendente trovato con l'email: " + emailDipendente.trim());
        }
        utenteDAO.eliminaDipendente(emailDipendente.trim());
    }

    /** Elenco dei dipendenti registrati con riepilogo monte ore e disponibilità. */
    public List<String> elencoDipendenti() {
        List<String> risultato = new ArrayList<>();
        for (Dipendente d : utenteDAO.caricaTuttiDipendenti()) {
            String ore = d.getConfigurazioneOre().isConfigurato()
                    ? ("Monte ore: " + d.getConfigurazioneOre().getEtichettaObiettivo())
                    : "⚠ Configurazione richiesta";
            String disp = d.getDisponibilita().getRiepilogoSintetico();
            risultato.add(d.getNome() + " " + d.getCognome() + " | " + d.getEmail() + " | " + ore + " | " + disp);
        }
        return risultato;
    }

    public List<NotificaBean> recuperaNotificheUtente(String emailUtente) {
        List<Notifica> notificheEntity = notificaDAO.caricaNotifichePerUtente(emailUtente);
        List<NotificaBean> risultato = new ArrayList<>();
        for (Notifica n : notificheEntity) {
            risultato.add(new NotificaBean(n.getTitolo(), n.getMessaggio(), n.getTimestamp()));
        }
        return risultato;
    }

    public void eliminaNotificaUtente(String emailUtente, NotificaBean notifica) {
        if (emailUtente == null || notifica == null) return;
        notificaDAO.eliminaNotifica(emailUtente, notifica.getTitolo(), notifica.getTimestamp());
    }
}
