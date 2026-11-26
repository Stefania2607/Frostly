package model;

/**
 * Configurazione scelta dall'amministratore prima di avviare la generazione automatica dei turni.
 * Le modalita' di generazione e la gestione dei turni esistenti sono sincronizzate per
 * evitare combinazioni contraddittorie.
 */
public class ConfigurazioneGenerazioneTurni {

    public enum ModalitaGenerazione {
        INTERA_SETTIMANA("Genera l'intera settimana (mantiene solo i turni bloccati)"),
        SOLO_SCOPERTI("Completa soltanto i turni scoperti (mantiene tutti i turni esistenti)"),
        RIGENERA_NON_BLOCCATI("Rigenera i turni non bloccati (mantiene i turni bloccati)");

        private final String descrizione;

        ModalitaGenerazione(String descrizione) {
            this.descrizione = descrizione;
        }

        public String getDescrizione() {
            return descrizione;
        }
    }

    public enum PrioritaOttimizzazione {
        COPERTURA_PRIORITARIA("Copertura prioritaria (predefinita)"),
        PIANIFICAZIONE_EQUILIBRATA("Pianificazione equilibrata"),
        MONTE_ORE_PRIORITARIO("Monte ore prioritario");

        private final String descrizione;

        PrioritaOttimizzazione(String descrizione) {
            this.descrizione = descrizione;
        }

        public String getDescrizione() {
            return descrizione;
        }
    }

    public enum TipoGenerazione {
        STANDARD_CATALOGO("Standard — utilizza il catalogo turni"),
        FLESSIBILE("Flessibile — consente nuovi orari personalizzati");

        private final String descrizione;

        TipoGenerazione(String descrizione) {
            this.descrizione = descrizione;
        }

        public String getDescrizione() {
            return descrizione;
        }
    }

    private TipoGenerazione tipoGenerazione = TipoGenerazione.STANDARD_CATALOGO;
    private ModalitaGenerazione modalita = ModalitaGenerazione.SOLO_SCOPERTI;
    private PrioritaOttimizzazione priorita = PrioritaOttimizzazione.COPERTURA_PRIORITARIA;
    private int riposoMinimoOre = 11;
    private int durataMinimaOreFlessibile = 4;
    private int durataMassimaOreFlessibile = 8;
    private int incrementoMinutiFlessibile = 60;
    private int maxTurniGiorno = 1;
    private int intervalloPreferitoOre = 12;
    private long seedRigenerazione = 1L;

    public ConfigurazioneGenerazioneTurni() {
    }

    public ConfigurazioneGenerazioneTurni(ModalitaGenerazione modalita, PrioritaOttimizzazione priorita) {
        if (modalita != null) {
            this.modalita = modalita;
        }
        if (priorita != null) {
            this.priorita = priorita;
        }
    }

    public ModalitaGenerazione getModalita() {
        return modalita;
    }

    public void setModalita(ModalitaGenerazione modalita) {
        if (modalita != null) {
            this.modalita = modalita;
        }
    }

    public PrioritaOttimizzazione getPriorita() {
        return priorita;
    }

    public void setPriorita(PrioritaOttimizzazione priorita) {
        if (priorita != null) {
            this.priorita = priorita;
        }
    }

    public int getRiposoMinimoOre() {
        return riposoMinimoOre;
    }

    public void setRiposoMinimoOre(int riposoMinimoOre) {
        this.riposoMinimoOre = Math.max(0, Math.min(24, riposoMinimoOre));
    }

    public long getSeedRigenerazione() {
        return seedRigenerazione;
    }

    public void setSeedRigenerazione(long seedRigenerazione) {
        this.seedRigenerazione = seedRigenerazione;
    }

    public boolean mantieneTurniNonBloccatiEsistenti() {
        return modalita == ModalitaGenerazione.SOLO_SCOPERTI;
    }

    public TipoGenerazione getTipoGenerazione() {
        return tipoGenerazione;
    }

    public void setTipoGenerazione(TipoGenerazione tipoGenerazione) {
        if (tipoGenerazione != null) {
            this.tipoGenerazione = tipoGenerazione;
        }
    }

    public boolean isFlessibile() {
        return tipoGenerazione == TipoGenerazione.FLESSIBILE;
    }

    public int getDurataMinimaOreFlessibile() {
        return durataMinimaOreFlessibile;
    }

    public void setDurataMinimaOreFlessibile(int durataMinimaOreFlessibile) {
        this.durataMinimaOreFlessibile = Math.max(1, Math.min(12, durataMinimaOreFlessibile));
    }

    public int getDurataMassimaOreFlessibile() {
        return durataMassimaOreFlessibile;
    }

    public void setDurataMassimaOreFlessibile(int durataMassimaOreFlessibile) {
        this.durataMassimaOreFlessibile = Math.max(this.durataMinimaOreFlessibile, Math.min(12, durataMassimaOreFlessibile));
    }

    public int getIncrementoMinutiFlessibile() {
        return incrementoMinutiFlessibile;
    }

    public void setIncrementoMinutiFlessibile(int incrementoMinutiFlessibile) {
        if (incrementoMinutiFlessibile == 15 || incrementoMinutiFlessibile == 30 || incrementoMinutiFlessibile == 60) {
            this.incrementoMinutiFlessibile = incrementoMinutiFlessibile;
        }
    }

    public int getMaxTurniGiorno() {
        return maxTurniGiorno;
    }

    public void setMaxTurniGiorno(int maxTurniGiorno) {
        this.maxTurniGiorno = Math.max(1, Math.min(2, maxTurniGiorno));
    }

    public int getIntervalloPreferitoOre() {
        return Math.max(riposoMinimoOre, intervalloPreferitoOre);
    }

    public void setIntervalloPreferitoOre(int intervalloPreferitoOre) {
        this.intervalloPreferitoOre = Math.max(riposoMinimoOre, Math.min(24, intervalloPreferitoOre));
    }

    public ConfigurazioneGenerazioneTurni conNuovoSeed() {
        ConfigurazioneGenerazioneTurni copia = new ConfigurazioneGenerazioneTurni(modalita, priorita);
        copia.setTipoGenerazione(tipoGenerazione);
        copia.setRiposoMinimoOre(riposoMinimoOre);
        copia.setDurataMinimaOreFlessibile(durataMinimaOreFlessibile);
        copia.setDurataMassimaOreFlessibile(durataMassimaOreFlessibile);
        copia.setIncrementoMinutiFlessibile(incrementoMinutiFlessibile);
        copia.setMaxTurniGiorno(maxTurniGiorno);
        copia.setIntervalloPreferitoOre(intervalloPreferitoOre);
        copia.setSeedRigenerazione(seedRigenerazione + 1L);
        return copia;
    }
}
