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
            if (t.getDipendenteAssegnato().getEmail().equalsIgnoreCase(emailDipendente)) {
                risultato.add(convertiTurnoInBean(t));
            }
        }
        return risultato;
    }

    public TurnoBean pubblicaNuovoTurno(TurnoBean beanInput) throws FrostlyException {
        List<Turno> turniEsistenti = turnoDao.caricaTuttiTurni();
        Dipendente dipendente = utenteDao.trovaDipendentePerEmail(beanInput.getEmailDipendente());
        if (dipendente == null) {
            throw new DatiNonValidiException("Dipendente non trovato con email: " + beanInput.getEmailDipendente());
        }

        if (!dipendente.isDisponibile(beanInput.getData(), beanInput.getOraInizio(), beanInput.getOraFine())) {
            throw new TurnoNonDisponibileException("Il dipendente " + dipendente
                    + " ha gia' un turno in conflitto per " + beanInput.getData()
                    + " nella fascia " + beanInput.getOraInizio() + "-" + beanInput.getOraFine());
        }

        String nuovoCodice = String.format("TRN-%02d", turniEsistenti.size() + 1);
        Turno nuovoTurno = new Turno(nuovoCodice, beanInput.getData(), beanInput.getOraInizio(),
                beanInput.getOraFine(), dipendente);
        nuovoTurno.pubblica();
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
            throw new DatiNonValidiException("Turno non trovato: " + beanInput.getCodiceTurno());
        }

        turnoCoinvolto.dichiaraEmergenza();
        String codiceEmergenza = String.format("EMG-%02d", emergenze.size() + 1);
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

    private Emergenza trovaEmergenzaEntity(String codiceEmergenza) throws DatiNonValidiException {
        for (Emergenza e : emergenzaDao.caricaTutteEmergenze()) {
            if (e.getCodiceEmergenza().equalsIgnoreCase(codiceEmergenza)) {
                return e;
            }
        }
        throw new DatiNonValidiException("Emergenza non trovata: " + codiceEmergenza);
    }

    private TurnoBean convertiTurnoInBean(Turno t) {
        TurnoBean b = new TurnoBean();
        try {
            b.setCodiceTurno(t.getCodiceTurno());
            b.setData(t.getData());
            b.setOraInizio(t.getOraInizio());
            b.setOraFine(t.getOraFine());
            b.setEmailDipendente(t.getDipendenteAssegnato().getEmail());
            b.setNomeDipendente(t.getDipendenteAssegnato().toString());
            b.setStato(t.getStato().name());
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
