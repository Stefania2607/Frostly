package control.applicativo;

import bean.LoginBean;
import bean.NotificaBean;
import dao.DAOFactory;
import dao.FrostlyDAO;
import frostly.exception.DatiNonValidiException;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.MaestroGelataio;
import model.Notifica;
import util.SessionManager;

/**
 * Controller applicativo STATELESS per l'autenticazione degli attori
 * (MaestroGelataio, Dipendente, Cliente) e il recupero delle notifiche personali.
 */
public class LoginControl {

    public static final String RUOLO_MAESTRO = "MAESTRO_GELATAIO";
    public static final String RUOLO_DIPENDENTE = "DIPENDENTE";
    public static final String RUOLO_CLIENTE = "CLIENTE";

    public LoginBean autenticaUtente(LoginBean credenzialiBean) throws DatiNonValidiException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        String email = credenzialiBean.getEmail();
        String password = credenzialiBean.getPassword();

        MaestroGelataio maestro = dao.getMaestroGelataio();
        if (maestro.autenticati(email, password)) {
            credenzialiBean.setRuoloAutenticato(RUOLO_MAESTRO);
            credenzialiBean.setNomeCompleto(maestro.getNome() + " " + maestro.getCognome());
            SessionManager.getInstance().impostaSessione(email, RUOLO_MAESTRO, credenzialiBean.getNomeCompleto());
            return credenzialiBean;
        }

        for (Dipendente dip : dao.caricaTuttiDipendenti()) {
            if (dip.autenticati(email, password)) {
                credenzialiBean.setRuoloAutenticato(RUOLO_DIPENDENTE);
                credenzialiBean.setNomeCompleto(dip.getNome() + " " + dip.getCognome());
                SessionManager.getInstance().impostaSessione(email, RUOLO_DIPENDENTE, credenzialiBean.getNomeCompleto());
                return credenzialiBean;
            }
        }

        for (Cliente cli : dao.caricaTuttiClienti()) {
            if (cli.autenticati(email, password)) {
                credenzialiBean.setRuoloAutenticato(RUOLO_CLIENTE);
                credenzialiBean.setNomeCompleto(cli.getNome() + " " + cli.getCognome());
                SessionManager.getInstance().impostaSessione(email, RUOLO_CLIENTE, credenzialiBean.getNomeCompleto());
                return credenzialiBean;
            }
        }

        throw new DatiNonValidiException("Credenziali errate: nessun utente trovato con questa email/password.");
    }

    public List<NotificaBean> recuperaNotificheUtente(String emailUtente) {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<Notifica> notificheEntity = dao.caricaNotifichePerUtente(emailUtente);
        List<NotificaBean> risultato = new ArrayList<>();
        for (Notifica n : notificheEntity) {
            risultato.add(new NotificaBean(n.getTitolo(), n.getMessaggio(), n.getTimestamp()));
        }
        return risultato;
    }
}