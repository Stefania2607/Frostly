package bean;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Bean strutturato per la presentazione del dettaglio di un avviso/notifica,
 * in particolare per gli avvisi legati a un ordine cliente.
 * Separato dal testo grezzo della notifica per supportare layout scalabili
 * con 1 fino a decine di articoli e personalizzazioni.
 */
public class DettaglioAvvisoOrdineBean {

    public enum TipoAvviso {
        ORDINE,
        CHAT_ORDINE,
        EMERGENZA,
        GUSTO,
        RECENSIONE,
        GENERICO
    }

    public static final class RigaArticoloOrdine {
        private final int quantita;
        private final String nomeProdotto;
        private final List<String> varianti;
        private final List<String> topping;
        private final List<String> aggiunte;
        private final List<String> esclusioni;
        private final List<String> altrePersonalizzazioni;
        private final String noteProdotto;
        private final Double prezzoUnitario;
        private final Double subtotaleRiga;

        public RigaArticoloOrdine(int quantita,
                                  String nomeProdotto,
                                  List<String> varianti,
                                  List<String> topping,
                                  List<String> aggiunte,
                                  List<String> esclusioni,
                                  List<String> altrePersonalizzazioni,
                                  String noteProdotto,
                                  Double prezzoUnitario,
                                  Double subtotaleRiga) {
            this.quantita = Math.max(1, quantita);
            this.nomeProdotto = nomeProdotto != null ? nomeProdotto.trim() : "";
            this.varianti = varianti != null ? Collections.unmodifiableList(new ArrayList<>(varianti)) : List.of();
            this.topping = topping != null ? Collections.unmodifiableList(new ArrayList<>(topping)) : List.of();
            this.aggiunte = aggiunte != null ? Collections.unmodifiableList(new ArrayList<>(aggiunte)) : List.of();
            this.esclusioni = esclusioni != null ? Collections.unmodifiableList(new ArrayList<>(esclusioni)) : List.of();
            this.altrePersonalizzazioni = altrePersonalizzazioni != null
                    ? Collections.unmodifiableList(new ArrayList<>(altrePersonalizzazioni)) : List.of();
            this.noteProdotto = noteProdotto != null ? noteProdotto.trim() : "";
            this.prezzoUnitario = prezzoUnitario;
            this.subtotaleRiga = subtotaleRiga;
        }

        public int getQuantita() {
            return quantita;
        }

        public String getNomeProdotto() {
            return nomeProdotto;
        }

        public List<String> getVarianti() {
            return varianti;
        }

        public List<String> getTopping() {
            return topping;
        }

        public List<String> getAggiunte() {
            return aggiunte;
        }

        public List<String> getEsclusioni() {
            return esclusioni;
        }

        public List<String> getAltrePersonalizzazioni() {
            return altrePersonalizzazioni;
        }

        public String getNoteProdotto() {
            return noteProdotto;
        }

        public Double getPrezzoUnitario() {
            return prezzoUnitario;
        }

        public Double getSubtotaleRiga() {
            return subtotaleRiga;
        }

        public boolean haDettagli() {
            return !varianti.isEmpty() || !topping.isEmpty() || !aggiunte.isEmpty()
                    || !esclusioni.isEmpty() || !altrePersonalizzazioni.isEmpty() || !noteProdotto.isEmpty();
        }
    }

    private TipoAvviso tipoAvviso = TipoAvviso.GENERICO;
    private boolean ordineRealeTrovato;
    private String codiceOrdine;
    private String titoloPulito;
    private String timestamp;
    private String messaggioOriginale;
    private String statoCodice;
    private String statoEtichetta;

    private String nomeCliente;
    private String emailCliente;
    private String telefonoCliente;

    private boolean ritiroAlBanco;
    private String modalitaLabel;
    private String luogoOIndirizzo;
    private String orarioRichiesto;
    private String nominativoRitiro;
    private String noteCliente;

    private String metodoPagamento;
    private String statoPagamento;
    private String dettaglioCartaMascherata;
    private String codiceTransazione;

    private Double prezzoTotale;
    private final List<RigaArticoloOrdine> articoli = new ArrayList<>();
    private String notaInformativaFooter;

    public static String formattaEuro(double importo) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.ITALY);
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        return "€ " + fmt.format(importo);
    }

    public TipoAvviso getTipoAvviso() {
        return tipoAvviso;
    }

    public void setTipoAvviso(TipoAvviso tipoAvviso) {
        this.tipoAvviso = tipoAvviso != null ? tipoAvviso : TipoAvviso.GENERICO;
    }

    public boolean isOrdineRealeTrovato() {
        return ordineRealeTrovato;
    }

    public void setOrdineRealeTrovato(boolean ordineRealeTrovato) {
        this.ordineRealeTrovato = ordineRealeTrovato;
    }

    public String getCodiceOrdine() {
        return codiceOrdine;
    }

    public void setCodiceOrdine(String codiceOrdine) {
        this.codiceOrdine = codiceOrdine;
    }

    public String getTitoloPulito() {
        return titoloPulito;
    }

    public void setTitoloPulito(String titoloPulito) {
        this.titoloPulito = titoloPulito;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessaggioOriginale() {
        return messaggioOriginale;
    }

    public void setMessaggioOriginale(String messaggioOriginale) {
        this.messaggioOriginale = messaggioOriginale;
    }

    public String getStatoCodice() {
        return statoCodice;
    }

    public void setStatoCodice(String statoCodice) {
        this.statoCodice = statoCodice;
    }

    public String getStatoEtichetta() {
        return statoEtichetta;
    }

    public void setStatoEtichetta(String statoEtichetta) {
        this.statoEtichetta = statoEtichetta;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public boolean isRitiroAlBanco() {
        return ritiroAlBanco;
    }

    public void setRitiroAlBanco(boolean ritiroAlBanco) {
        this.ritiroAlBanco = ritiroAlBanco;
    }

    public String getModalitaLabel() {
        return modalitaLabel;
    }

    public void setModalitaLabel(String modalitaLabel) {
        this.modalitaLabel = modalitaLabel;
    }

    public String getLuogoOIndirizzo() {
        return luogoOIndirizzo;
    }

    public void setLuogoOIndirizzo(String luogoOIndirizzo) {
        this.luogoOIndirizzo = luogoOIndirizzo;
    }

    public String getOrarioRichiesto() {
        return orarioRichiesto;
    }

    public void setOrarioRichiesto(String orarioRichiesto) {
        this.orarioRichiesto = orarioRichiesto;
    }

    public String getNominativoRitiro() {
        return nominativoRitiro;
    }

    public void setNominativoRitiro(String nominativoRitiro) {
        this.nominativoRitiro = nominativoRitiro;
    }

    public String getNoteCliente() {
        return noteCliente;
    }

    public void setNoteCliente(String noteCliente) {
        this.noteCliente = noteCliente;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public String getStatoPagamento() {
        return statoPagamento;
    }

    public void setStatoPagamento(String statoPagamento) {
        this.statoPagamento = statoPagamento;
    }

    public String getDettaglioCartaMascherata() {
        return dettaglioCartaMascherata;
    }

    public void setDettaglioCartaMascherata(String dettaglioCartaMascherata) {
        this.dettaglioCartaMascherata = dettaglioCartaMascherata;
    }

    public String getCodiceTransazione() {
        return codiceTransazione;
    }

    public void setCodiceTransazione(String codiceTransazione) {
        this.codiceTransazione = codiceTransazione;
    }

    public Double getPrezzoTotale() {
        return prezzoTotale;
    }

    public void setPrezzoTotale(Double prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }

    public String getTotaleFormattato() {
        return prezzoTotale != null ? formattaEuro(prezzoTotale) : "";
    }

    public List<RigaArticoloOrdine> getArticoli() {
        return Collections.unmodifiableList(articoli);
    }

    public void aggiungiArticolo(RigaArticoloOrdine articolo) {
        if (articolo != null) {
            this.articoli.add(articolo);
        }
    }

    public int getTotalePezzi() {
        int somma = 0;
        for (RigaArticoloOrdine r : articoli) {
            somma += r.getQuantita();
        }
        return somma;
    }

    public String getNotaInformativaFooter() {
        return notaInformativaFooter;
    }

    public void setNotaInformativaFooter(String notaInformativaFooter) {
        this.notaInformativaFooter = notaInformativaFooter;
    }
}
