package control.applicativo;

import bean.RichiestaGustoBean;
import dao.DAOFactory;
import dao.FrostlyDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Ingrediente;
import model.Notifica;
import model.RichiestaGustoPersonalizzato;

/**
 * Controller applicativo STATELESS per il Caso d'Uso UC3:
 * Richiesta Gusto Personalizzato da parte del Cliente e valutazione/produzione
 * da parte del Maestro Gelataio.
 */
public class RichiestaGustoControl {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public RichiestaGustoBean inviaRichiestaGusto(RichiestaGustoBean beanInput) throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();

        List<RichiestaGustoPersonalizzato> esistenti = dao.caricaTutteRichiesteGusto();
        Cliente cliente = dao.trovaClientePerEmail(beanInput.getEmailCliente());
        Ingrediente ingrediente = dao.trovaIngredientePerNome(beanInput.getNomeIngredientePrincipale());

        if (cliente == null) {
            throw new DatiNonValidiException("Cliente non trovato: " + beanInput.getEmailCliente());
        }
        if (ingrediente == null) {
            throw new DatiNonValidiException("Ingrediente non presente in catalogo: " + beanInput.getNomeIngredientePrincipale());
        }

        String nuovoCodice = String.format("REQ-%02d", esistenti.size() + 1);
        RichiestaGustoPersonalizzato nuovaRichiesta = new RichiestaGustoPersonalizzato(
                nuovoCodice,
                cliente,
                beanInput.getNomeGustoProposto(),
                ingrediente,
                beanInput.getQuantitaRichiestaKg(),
                beanInput.getDataRitiroDesiderata(),
                beanInput.getNoteCliente() != null ? beanInput.getNoteCliente() : "");

        dao.salvaRichiestaGusto(nuovaRichiesta);
        dao.inviaNotifica(new Notifica(
                dao.getMaestroGelataio().getEmail(),
                "Nuova Richiesta Gusto Personalizzato",
                cliente.getNome() + " " + cliente.getCognome() + " ha richiesto '"
                        + nuovaRichiesta.getNomeGustoProposto() + "' (" + nuovaRichiesta.getQuantitaRichiestaKg()
                        + " kg) con " + ingrediente.getNome() + " per " + nuovaRichiesta.getDataRitiroDesiderata() + ".",
                oraCorrente()));

        return convertiInBean(nuovaRichiesta);
    }

    public RichiestaGustoBean approvaRichiestaGusto(String codiceRichiesta, String messaggioPerCliente)
            throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        RichiestaGustoPersonalizzato richiesta = trovaRichiestaEntity(dao, codiceRichiesta);

        Ingrediente ing = richiesta.getIngredientePrincipale();
        double fabbisogno = Math.round(richiesta.getQuantitaRichiestaKg() * 0.6 * 100.0) / 100.0;

        // Consuma la quantita' dall'Entity Ingrediente (puo' lanciare IngredienteEsauritoException!)
        ing.consuma(fabbisogno);
        richiesta.approva(messaggioPerCliente);

        dao.aggiornaIngrediente(ing);
        dao.salvaRichiestaGusto(richiesta);

        dao.inviaNotifica(new Notifica(
                richiesta.getCliente().getEmail(),
                "Gusto Personalizzato Approvato!",
                "Il Maestro Gelataio ha approvato il tuo gusto '" + richiesta.getNomeGustoProposto()
                        + "' per " + richiesta.getDataRitiroDesiderata() + ". Messaggio: " + messaggioPerCliente,
                oraCorrente()));

        if (ing.isSottoSoglia()) {
            dao.inviaNotifica(new Notifica(
                    dao.getMaestroGelataio().getEmail(),
                    "Allerta Magazzino Sotto Soglia",
                    "L'ingrediente '" + ing.getNome() + "' e' sceso a " + ing.getQuantitaDisponibile()
                            + " " + ing.getUnitaMisura() + " (soglia: " + ing.getSogliaMinima() + ").",
                    oraCorrente()));
        }

        return convertiInBean(richiesta);
    }

    public RichiestaGustoBean approvaConRifornimentoUrgente(String codiceRichiesta, double quantitaRifornimento,
                                                            String nuovaDataStimata) throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        RichiestaGustoPersonalizzato richiesta = trovaRichiestaEntity(dao, codiceRichiesta);

        Ingrediente ing = richiesta.getIngredientePrincipale();
        ing.rifornisci(quantitaRifornimento);
        double fabbisogno = Math.round(richiesta.getQuantitaRichiestaKg() * 0.6 * 100.0) / 100.0;
        ing.consuma(fabbisogno);

        String nota = "Approvato dopo rifornimento urgente ingrediente. Pronto per: " + nuovaDataStimata;
        richiesta.approva(nota);

        dao.aggiornaIngrediente(ing);
        dao.salvaRichiestaGusto(richiesta);

        dao.inviaNotifica(new Notifica(
                richiesta.getCliente().getEmail(),
                "Gusto Personalizzato Approvato (Con Rifornimento)",
                "Il tuo gusto '" + richiesta.getNomeGustoProposto()
                        + "' e' stato confermato! Data aggiornata: " + nuovaDataStimata,
                oraCorrente()));

        return convertiInBean(richiesta);
    }

    public RichiestaGustoBean rifiutaRichiestaGusto(String codiceRichiesta, String motivoRifiuto)
            throws FrostlyException {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        RichiestaGustoPersonalizzato richiesta = trovaRichiestaEntity(dao, codiceRichiesta);

        richiesta.rifiuta(motivoRifiuto);
        dao.salvaRichiestaGusto(richiesta);

        dao.inviaNotifica(new Notifica(
                richiesta.getCliente().getEmail(),
                "Richiesta Gusto Non Approvata",
                "La richiesta per il gusto '" + richiesta.getNomeGustoProposto()
                        + "' e' stata rifiutata. Motivo: " + motivoRifiuto,
                oraCorrente()));

        return convertiInBean(richiesta);
    }

    public List<RichiestaGustoBean> visualizzaTutteRichieste() {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<RichiestaGustoBean> risultato = new ArrayList<>();
        for (RichiestaGustoPersonalizzato r : dao.caricaTutteRichiesteGusto()) {
            risultato.add(convertiInBean(r));
        }
        return risultato;
    }

    public List<RichiestaGustoBean> visualizzaRichiesteCliente(String emailCliente) {
        FrostlyDAO dao = DAOFactory.getFrostlyDAO();
        List<RichiestaGustoBean> risultato = new ArrayList<>();
        for (RichiestaGustoPersonalizzato r : dao.caricaTutteRichiesteGusto()) {
            if (r.getCliente().getEmail().equalsIgnoreCase(emailCliente)) {
                risultato.add(convertiInBean(r));
            }
        }
        return risultato;
    }

    private RichiestaGustoPersonalizzato trovaRichiestaEntity(FrostlyDAO dao, String codiceRichiesta)
            throws DatiNonValidiException {
        for (RichiestaGustoPersonalizzato r : dao.caricaTutteRichiesteGusto()) {
            if (r.getCodiceRichiesta().equalsIgnoreCase(codiceRichiesta)) {
                return r;
            }
        }
        throw new DatiNonValidiException("Richiesta gusto non trovata: " + codiceRichiesta);
    }

    private RichiestaGustoBean convertiInBean(RichiestaGustoPersonalizzato r) {
        RichiestaGustoBean b = new RichiestaGustoBean();
        try {
            b.setCodiceRichiesta(r.getCodiceRichiesta());
            b.setEmailCliente(r.getCliente().getEmail());
            b.setNomeCliente(r.getCliente().getNome() + " " + r.getCliente().getCognome());
            b.setNomeGustoProposto(r.getNomeGustoProposto());
            b.setNomeIngredientePrincipale(r.getIngredientePrincipale().getNome());
            b.setQuantitaRichiestaKg(r.getQuantitaRichiestaKg());
            b.setDataRitiroDesiderata(r.getDataRitiroDesiderata());
            b.setNoteCliente(r.getNoteCliente());
            b.setStato(r.getStato().name());
            b.setRispostaMaestro(r.getRispostaMaestro());
        } catch (DatiNonValidiException ignored) {
            // Dati provenienti dal Model gia' validi
        }
        return b;
    }

    private String oraCorrente() {
        return "Oggi " + LocalTime.now().format(TIME_FMT);
    }
}