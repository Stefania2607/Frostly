package model;

/**
 * Entity che rappresenta una richiesta di Gusto Personalizzato inviata da un Cliente
 * e valutata dal Maestro Gelataio.
 * Mantiene il puntatore diretto al Cliente e all'Ingrediente principale richiesto.
 */
public class RichiestaGustoPersonalizzato {

    private String codiceRichiesta;
    private Cliente cliente;
    private String nomeGustoProposto;
    private Ingrediente ingredientePrincipale;
    private double quantitaRichiestaKg;
    private String dataRitiroDesiderata;
    private String noteCliente;
    private StatoRichiesta stato;
    private String rispostaMaestro;

    public RichiestaGustoPersonalizzato(String codiceRichiesta, Cliente cliente, String nomeGustoProposto,
                                        Ingrediente ingredientePrincipale, double quantitaRichiestaKg, String dataRitiroDesiderata,
                                        String noteCliente) {
        this.codiceRichiesta = codiceRichiesta;
        this.cliente = cliente;
        this.nomeGustoProposto = nomeGustoProposto;
        this.ingredientePrincipale = ingredientePrincipale;
        this.quantitaRichiestaKg = quantitaRichiestaKg;
        this.dataRitiroDesiderata = dataRitiroDesiderata;
        this.noteCliente = noteCliente;
        this.stato = StatoRichiesta.IN_ATTESA;
        this.rispostaMaestro = "";
    }

    public void approva(String messaggioMaestro) {
        this.stato = StatoRichiesta.APPROVATA;
        this.rispostaMaestro = messaggioMaestro;
    }

    public void rifiuta(String motivazioneRifiuto) {
        this.stato = StatoRichiesta.RIFIUTATA;
        this.rispostaMaestro = motivazioneRifiuto;
    }

    public void segnaComePronta(String dettaglioRitiro) {
        this.stato = StatoRichiesta.COMPLETATA;
        this.rispostaMaestro = dettaglioRitiro;
    }

    public String getCodiceRichiesta() {
        return codiceRichiesta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getNomeGustoProposto() {
        return nomeGustoProposto;
    }

    public Ingrediente getIngredientePrincipale() {
        return ingredientePrincipale;
    }

    public double getQuantitaRichiestaKg() {
        return quantitaRichiestaKg;
    }

    public String getDataRitiroDesiderata() {
        return dataRitiroDesiderata;
    }

    public String getNoteCliente() {
        return noteCliente;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public String getRispostaMaestro() {
        return rispostaMaestro;
    }

    public void setRispostaMaestro(String rispostaMaestro) {
        this.rispostaMaestro = rispostaMaestro;
    }
}