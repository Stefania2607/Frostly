package bean;

import frostly.exception.DatiNonValidiException;

/**
 * Bean per la Richiesta di Gusto Personalizzato con validazione sintattica nei setter.
 */
public class RichiestaGustoBean {

    private String codiceRichiesta;
    private String emailCliente;
    private String nomeCliente;
    private String nomeGustoProposto;
    private String nomeIngredientePrincipale;
    private double quantitaRichiestaKg;
    private String dataRitiroDesiderata;
    private String noteCliente;
    private String stato;
    private String rispostaMaestro;

    public RichiestaGustoBean() {
        // Costruttore vuoto JavaBean
    }

    public String getCodiceRichiesta() {
        return codiceRichiesta;
    }

    public void setCodiceRichiesta(String codiceRichiesta) {
        this.codiceRichiesta = codiceRichiesta;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getNomeGustoProposto() {
        return nomeGustoProposto;
    }

    public void setNomeGustoProposto(String nomeGustoProposto) throws DatiNonValidiException {
        if (nomeGustoProposto == null || nomeGustoProposto.trim().length() < 3) {
            throw new DatiNonValidiException("Il nome del gusto proposto deve avere almeno 3 caratteri.");
        }
        this.nomeGustoProposto = nomeGustoProposto.trim();
    }

    public String getNomeIngredientePrincipale() {
        return nomeIngredientePrincipale;
    }

    public void setNomeIngredientePrincipale(String nomeIngredientePrincipale) throws DatiNonValidiException {
        if (nomeIngredientePrincipale == null || nomeIngredientePrincipale.trim().isEmpty()) {
            throw new DatiNonValidiException("Selezionare un ingrediente principale valido.");
        }
        this.nomeIngredientePrincipale = nomeIngredientePrincipale.trim();
    }

    public double getQuantitaRichiestaKg() {
        return quantitaRichiestaKg;
    }

    public void setQuantitaRichiestaKg(double quantitaRichiestaKg) throws DatiNonValidiException {
        if (quantitaRichiestaKg <= 0 || quantitaRichiestaKg > 10.0) {
            throw new DatiNonValidiException("La quantita' deve essere compresa tra 0.5 kg e 10.0 kg.");
        }
        this.quantitaRichiestaKg = quantitaRichiestaKg;
    }

    /**
     * Setter di comodo per input testuali (es. da TextField o CLI): se l'utente scrive "mamma"
     * invece di un numero, il Bean lo blocca subito sollevando DatiNonValidiException!
     */
    public void setQuantitaDaTesto(String testoQuantita) throws DatiNonValidiException {
        try {
            double valore = Double.parseDouble(testoQuantita.trim());
            setQuantitaRichiestaKg(valore);
        } catch (NumberFormatException e) {
            throw new DatiNonValidiException("Quantita' non numerica ('" + testoQuantita + "'): inserire un numero valido.");
        }
    }

    public String getDataRitiroDesiderata() {
        return dataRitiroDesiderata;
    }

    public void setDataRitiroDesiderata(String dataRitiroDesiderata) throws DatiNonValidiException {
        if (dataRitiroDesiderata == null || dataRitiroDesiderata.trim().isEmpty()) {
            throw new DatiNonValidiException("Specificare la data di ritiro desiderata.");
        }
        this.dataRitiroDesiderata = dataRitiroDesiderata.trim();
    }

    public String getNoteCliente() {
        return noteCliente;
    }

    public void setNoteCliente(String noteCliente) {
        this.noteCliente = noteCliente;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getRispostaMaestro() {
        return rispostaMaestro;
    }

    public void setRispostaMaestro(String rispostaMaestro) {
        this.rispostaMaestro = rispostaMaestro;
    }
}