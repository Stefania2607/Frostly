package control.applicativo;

import bean.LoginBean;
import bean.NotificaBean;
import dao.DAOFactory;
import dao.NotificaDAO;
import dao.UtenteDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
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

    public LoginBean autenticaUtente(LoginBean credenzialiBean) throws DatiNonValidiException {
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

        throw new DatiNonValidiException("Credenziali errate: nessun utente trovato con questa email/password.");
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

    /** Creazione di un profilo dipendente: consentita solo al Maestro Gelataio autenticato. */
    public LoginBean creaProfiloDipendente(LoginBean dipendenteBean) throws FrostlyException {
        if (!RUOLO_MAESTRO.equals(SessionManager.getInstance().getRuoloUtenteLoggato())) {
            throw new DatiNonValidiException("Operazione non consentita: solo il Maestro puo' creare profili dipendente.");
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

    /** Eliminazione di un account dipendente: consentita esclusivamente al Maestro. */
    public void eliminaProfiloDipendente(String emailDipendente) throws FrostlyException {
        if (!RUOLO_MAESTRO.equals(SessionManager.getInstance().getRuoloUtenteLoggato())) {
            throw new DatiNonValidiException("Operazione non consentita: solo il Maestro puo' eliminare profili dipendente.");
        }
        if (emailDipendente == null || emailDipendente.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona il dipendente da eliminare.");
        }
        if (utenteDAO.trovaDipendentePerEmail(emailDipendente.trim()) == null) {
            throw new DatiNonValidiException("Nessun dipendente trovato con l'email: " + emailDipendente.trim());
        }
        utenteDAO.eliminaDipendente(emailDipendente.trim());
    }

    /** Elenco dei dipendenti registrati, in formato "Nome Cognome | email". */
    public List<String> elencoDipendenti() {
        List<String> risultato = new ArrayList<>();
        for (Dipendente d : utenteDAO.caricaTuttiDipendenti()) {
            risultato.add(d.getNome() + " " + d.getCognome() + " | " + d.getEmail());
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
}
