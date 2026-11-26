package model;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;

/**
 * Rappresenta il fabbisogno di personale per uno specifico incrocio (giorno x fascia oraria),
 * includendo il numero totale di dipendenti richiesti e i minimi per mansione.
 */
public class FabbisognoTurno {

    private final DayOfWeek giorno;
    private final int indiceFascia;
    private int totaleRichiesto;
    private final Map<MansioneDipendente, Integer> minimiPerMansione = new EnumMap<>(MansioneDipendente.class);

    public FabbisognoTurno(DayOfWeek giorno, int indiceFascia, int totaleRichiesto) {
        this.giorno = giorno;
        this.indiceFascia = indiceFascia;
        this.totaleRichiesto = Math.max(0, totaleRichiesto);
        for (MansioneDipendente m : MansioneDipendente.values()) {
            minimiPerMansione.put(m, 0);
        }
    }

    public FabbisognoTurno conMinimoMansione(MansioneDipendente mansione, int minimo) {
        if (mansione != null) {
            int val = Math.max(0, minimo);
            minimiPerMansione.put(mansione, val);
            if (val > this.totaleRichiesto) {
                this.totaleRichiesto = val;
            }
        }
        return this;
    }

    public DayOfWeek getGiorno() {
        return giorno;
    }

    public int getIndiceFascia() {
        return indiceFascia;
    }

    public int getTotaleRichiesto() {
        return totaleRichiesto;
    }

    public void setTotaleRichiesto(int totaleRichiesto) {
        this.totaleRichiesto = Math.max(0, totaleRichiesto);
    }

    public int getMinimoMansione(MansioneDipendente mansione) {
        return minimiPerMansione.getOrDefault(mansione, 0);
    }

    public void setMinimoMansione(MansioneDipendente mansione, int minimo) {
        if (mansione != null) {
            minimiPerMansione.put(mansione, Math.max(0, minimo));
        }
    }

    public Map<MansioneDipendente, Integer> getMinimiPerMansione() {
        return new EnumMap<>(minimiPerMansione);
    }

    public FabbisognoTurno copia() {
        FabbisognoTurno c = new FabbisognoTurno(giorno, indiceFascia, totaleRichiesto);
        for (Map.Entry<MansioneDipendente, Integer> e : minimiPerMansione.entrySet()) {
            c.setMinimoMansione(e.getKey(), e.getValue());
        }
        return c;
    }

    /**
     * Genera un profilo predefinito sensato per una gelateria artigianale:
     * 1 dipendente (almeno 1 banco) nelle fasce feriali standard e 1 per ciascuna delle 5 fasce nel weekend
     * (fascia Extra Weekend attiva solo Sabato e Domenica). Personalizzabile da Fabbisogni.
     */
    public static FabbisognoTurno predefinito(DayOfWeek giorno, int indiceFascia) {
        boolean weekend = (giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY);
        if (indiceFascia == 4 && !weekend) {
            return new FabbisognoTurno(giorno, indiceFascia, 0);
        }
        return new FabbisognoTurno(giorno, indiceFascia, 1)
                .conMinimoMansione(MansioneDipendente.BANCO, 1);
    }

    public static String chiave(DayOfWeek giorno, int indiceFascia) {
        return giorno.getValue() + "_" + indiceFascia;
    }
}
