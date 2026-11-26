package control.applicativo;

import bean.RecensioneBean;
import dao.DAOFactory;
import dao.RecensioneDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import frostly.exception.OperazioneNonAutorizzataException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Notifica;
import model.Recensione;

/**
 * Controller Applicativo dedicato alla gestione del ciclo di vita delle Recensioni:
 * pubblicazione da parte dei clienti registrati, risposte ufficiali del Maestro Gelataio,
 * consultazione pubblica e calcolo statistiche di gradimento.
 */
public class RecensioniControl {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final RecensioneDAO recensioneDAO;

    public RecensioniControl() {
        this.recensioneDAO = DAOFactory.getRecensioneDAO();
    }

    public List<RecensioneBean> visualizzaTutteRecensioni() throws FrostlyException {
        List<Recensione> entita = recensioneDAO.trovaTutteRecensioni();
        List<RecensioneBean> beans = new ArrayList<>();
        for (Recensione r : entita) {
            beans.add(mappaEntitaABean(r));
        }
        return beans;
    }

    public RecensioneBean pubblicaRecensione(RecensioneBean bean) throws FrostlyException {
        if (bean == null) {
            throw new DatiNonValidiException("Dati recensione non validi.");
        }
        if (bean.getAutoreEmail() == null || bean.getAutoreEmail().trim().isEmpty()) {
            throw new OperazioneNonAutorizzataException("Per pubblicare una recensione è necessario essere autenticati.");
        }
        if (bean.getVotoStelle() < 1 || bean.getVotoStelle() > 5) {
            throw new DatiNonValidiException("La valutazione deve essere compresa tra 1 e 5 stelle.");
        }
        if (bean.getTitolo() == null || bean.getTitolo().trim().isEmpty()) {
            throw new DatiNonValidiException("Inserisci un titolo per la tua recensione.");
        }
        if (bean.getCommento() == null || bean.getCommento().trim().isEmpty()) {
            throw new DatiNonValidiException("Inserisci il testo della tua recensione.");
        }

        String dataOra = LocalDateTime.now().format(TIME_FMT);
        Recensione r = new Recensione(
                null,
                bean.getAutoreEmail().trim(),
                bean.getAutoreNome() != null ? bean.getAutoreNome().trim() : "Cliente Frostly",
                bean.getVotoStelle(),
                bean.getTitolo().trim(),
                bean.getCommento().trim(),
                dataOra,
                null,
                null,
                0,
                bean.getFotoPath() != null && !bean.getFotoPath().trim().isEmpty() ? bean.getFotoPath().trim() : null);

        Recensione salvata = recensioneDAO.salvaNuovaRecensione(r);

        // Notifica il Maestro della nuova recensione
        DAOFactory.getNotificaDAO().inviaNotifica(new Notifica(
                "maestro@frostly.it",
                "⭐ Nuova Recensione (" + salvata.getVotoStelle() + "★)",
                salvata.getAutoreNome() + " ha recensito: \"" + salvata.getTitolo() + "\"",
                dataOra));

        return mappaEntitaABean(salvata);
    }

    public void rispondiARecensione(String codiceRecensione, String rispostaMaestro) throws FrostlyException {
        if (codiceRecensione == null || codiceRecensione.trim().isEmpty()) {
            throw new DatiNonValidiException("Seleziona una recensione a cui rispondere.");
        }
        if (rispostaMaestro == null || rispostaMaestro.trim().isEmpty()) {
            throw new DatiNonValidiException("Il testo della risposta del Maestro non può essere vuoto.");
        }

        String dataRisposta = LocalDateTime.now().format(TIME_FMT);
        recensioneDAO.aggiornaRispostaMaestro(codiceRecensione.trim(), rispostaMaestro.trim(), dataRisposta);

        // Cerca l'autore per inviargli notifica
        for (Recensione r : recensioneDAO.trovaTutteRecensioni()) {
            if (r.getCodiceRecensione().equalsIgnoreCase(codiceRecensione.trim())) {
                DAOFactory.getNotificaDAO().inviaNotifica(new Notifica(
                        r.getAutoreEmail(),
                        "👩‍🍳 Risposta dalla Maestra Gelataia",
                        "Stefania ha risposto alla tua recensione: \"" + rispostaMaestro.trim() + "\"",
                        dataRisposta));
                break;
            }
        }
    }

    public void mettiMiPiace(String codiceRecensione) throws FrostlyException {
        if (codiceRecensione == null || codiceRecensione.trim().isEmpty()) {
            throw new DatiNonValidiException("Codice recensione non valido.");
        }
        recensioneDAO.incrementaMiPiace(codiceRecensione.trim());
    }

    public double calcolaMediaVoti(List<RecensioneBean> lista) {
        if (lista == null || lista.isEmpty()) {
            return 5.0;
        }
        double somma = 0.0;
        for (RecensioneBean r : lista) {
            somma += r.getVotoStelle();
        }
        return Math.round((somma / lista.size()) * 10.0) / 10.0;
    }

    private RecensioneBean mappaEntitaABean(Recensione r) {
        RecensioneBean b = new RecensioneBean();
        b.setCodiceRecensione(r.getCodiceRecensione());
        b.setAutoreEmail(r.getAutoreEmail());
        b.setAutoreNome(r.getAutoreNome());
        try {
            b.setVotoStelle(r.getVotoStelle());
            b.setTitolo(r.getTitolo());
            b.setCommento(r.getCommento());
        } catch (DatiNonValidiException ignored) {
            // Dati dal DB già validati
        }
        b.setDataOra(r.getDataOra());
        b.setRispostaMaestro(r.getRispostaMaestro());
        b.setDataRisposta(r.getDataRisposta());
        b.setMiPiace(r.getMiPiace());
        b.setFotoPath(r.getFotoPath());
        return b;
    }
}
