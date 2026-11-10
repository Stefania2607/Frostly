package model;

/**
 * Entity che rappresenta un Turno lavorativo in gelateria.
 * Contiene un riferimento diretto all'oggetto Dipendente assegnato (NO foreign key).
 */
public class Turno {

    private String codiceTurno;
    private String data;
    private String oraInizio;
    private String oraFine;
    private Dipendente dipendenteAssegnato;
    private StatoTurno stato;

    public Turno(String codiceTurno, String data, String oraInizio, String oraFine, Dipendente dipendenteAssegnato) {
        this.codiceTurno = codiceTurno;
        this.data = data;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.dipendenteAssegnato = dipendenteAssegnato;
        this.stato = StatoTurno.PIANIFICATO;
    }

    public void pubblica() {
        this.stato = StatoTurno.PUBBLICATO;
    }

    public void dichiaraEmergenza() {
        this.stato = StatoTurno.IN_EMERGENZA;
    }

    /**
     * Operazione di business: sostituisce il dipendente con il sostituto disponibile,
     * aggiornando i puntatori diretti in memoria.
     */
    public void riassegnaA(Dipendente nuovoDipendente) {
        if (nuovoDipendente == null) {
            return;
        }
        if (this.dipendenteAssegnato != null) {
            this.dipendenteAssegnato.rimuoviTurno(this);
        }
        this.dipendenteAssegnato = nuovoDipendente;
        nuovoDipendente.aggiungiTurno(this);
        this.stato = StatoTurno.SOSTITUITO;
    }

    public boolean isInConflitto(String inizioAltro, String fineAltro) {
        String fine1 = ("00:00".equals(this.oraFine) || "24:00".equals(this.oraFine)) ? "24:00" : this.oraFine;
        String fine2 = ("00:00".equals(fineAltro) || "24:00".equals(fineAltro)) ? "24:00" : fineAltro;
        return this.oraInizio.compareTo(fine2) < 0 && inizioAltro.compareTo(fine1) < 0;
    }

    public String getCodiceTurno() {
        return codiceTurno;
    }

    public void setCodiceTurno(String codiceTurno) {
        this.codiceTurno = codiceTurno;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(String oraInizio) {
        this.oraInizio = oraInizio;
    }

    public String getOraFine() {
        return oraFine;
    }

    public void setOraFine(String oraFine) {
        this.oraFine = oraFine;
    }

    public Dipendente getDipendenteAssegnato() {
        return dipendenteAssegnato;
    }

    public void setDipendenteAssegnato(Dipendente dipendenteAssegnato) {
        this.dipendenteAssegnato = dipendenteAssegnato;
    }

    public StatoTurno getStato() {
        return stato;
    }

    public void setStato(StatoTurno stato) {
        this.stato = stato;
    }
}