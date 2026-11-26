package model;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Configurazione della copertura operativa della gelateria per intervalli orari
 * (personalizzabile per singolo giorno della settimana) e della copertura di chiusura.
 */
public class ConfigurazioneCoperturaFlessibile {

    /**
     * Singolo intervallo orario da coprire (es. 08:00-12:00, min 2 persone).
     */
    public record IntervalloCopertura(
            String oraInizio,
            String oraFine,
            int personaleMinimo,
            boolean isChiusura) {
    }

    private final Map<DayOfWeek, List<IntervalloCopertura>> fascePerGiorno = new EnumMap<>(DayOfWeek.class);
    private int personaleMinimoChiusura = 2;
    private String oraInizioChiusura = "22:00";
    private String oraFineChiusura = "00:00";
    private String oraFinePostChiusura = "00:00";

    public ConfigurazioneCoperturaFlessibile() {
        inizializzaPredefiniti();
    }

    public static ConfigurazioneCoperturaFlessibile predefinita() {
        return new ConfigurazioneCoperturaFlessibile();
    }

    private void inizializzaPredefiniti() {
        for (DayOfWeek g : DayOfWeek.values()) {
            boolean weekend = (g == DayOfWeek.SATURDAY || g == DayOfWeek.SUNDAY);
            List<IntervalloCopertura> lista = new ArrayList<>();
            lista.add(new IntervalloCopertura("08:00", "12:00", 1, false));
            lista.add(new IntervalloCopertura("12:00", "16:00", weekend ? 2 : 1, false));
            lista.add(new IntervalloCopertura("16:00", "20:00", weekend ? 2 : 2, false));
            lista.add(new IntervalloCopertura("20:00", "22:00", weekend ? 2 : 2, false));
            lista.add(new IntervalloCopertura(oraInizioChiusura, oraFineChiusura, personaleMinimoChiusura, true));
            fascePerGiorno.put(g, lista);
        }
    }

    public List<IntervalloCopertura> getIntervalliGiorno(DayOfWeek giorno) {
        List<IntervalloCopertura> base = fascePerGiorno.get(giorno);
        if (base == null) {
            return List.of();
        }
        return new ArrayList<>(base);
    }

    public void impostaIntervalliGiorno(DayOfWeek giorno, List<IntervalloCopertura> intervalli) {
        if (giorno != null && intervalli != null) {
            fascePerGiorno.put(giorno, new ArrayList<>(intervalli));
        }
    }

    public int getPersonaleMinimoChiusura() {
        return personaleMinimoChiusura;
    }

    public void setPersonaleMinimoChiusura(int personaleMinimoChiusura) {
        this.personaleMinimoChiusura = Math.max(0, personaleMinimoChiusura);
        sincronizzaChiusuraSuGiorni();
    }

    public String getOraInizioChiusura() {
        return oraInizioChiusura;
    }

    public void setOraInizioChiusura(String oraInizioChiusura) {
        if (oraInizioChiusura != null && oraInizioChiusura.matches("\\d{2}:\\d{2}")) {
            this.oraInizioChiusura = oraInizioChiusura;
            sincronizzaChiusuraSuGiorni();
        }
    }

    public String getOraFineChiusura() {
        return oraFineChiusura;
    }

    public void setOraFineChiusura(String oraFineChiusura) {
        if (oraFineChiusura != null && oraFineChiusura.matches("\\d{2}:\\d{2}")) {
            this.oraFineChiusura = oraFineChiusura;
            sincronizzaChiusuraSuGiorni();
        }
    }

    public String getOraFinePostChiusura() {
        return oraFinePostChiusura;
    }

    public void setOraFinePostChiusura(String oraFinePostChiusura) {
        if (oraFinePostChiusura != null && oraFinePostChiusura.matches("\\d{2}:\\d{2}")) {
            this.oraFinePostChiusura = oraFinePostChiusura;
        }
    }

    private void sincronizzaChiusuraSuGiorni() {
        for (DayOfWeek g : DayOfWeek.values()) {
            List<IntervalloCopertura> lista = fascePerGiorno.get(g);
            if (lista == null) continue;
            List<IntervalloCopertura> aggiornata = new ArrayList<>();
            boolean trovata = false;
            for (IntervalloCopertura ic : lista) {
                if (ic.isChiusura()) {
                    aggiornata.add(new IntervalloCopertura(oraInizioChiusura, oraFineChiusura, personaleMinimoChiusura, true));
                    trovata = true;
                } else {
                    aggiornata.add(ic);
                }
            }
            if (!trovata && personaleMinimoChiusura > 0) {
                aggiornata.add(new IntervalloCopertura(oraInizioChiusura, oraFineChiusura, personaleMinimoChiusura, true));
            }
            fascePerGiorno.put(g, aggiornata);
        }
    }

    public ConfigurazioneCoperturaFlessibile copia() {
        ConfigurazioneCoperturaFlessibile c = new ConfigurazioneCoperturaFlessibile();
        c.personaleMinimoChiusura = this.personaleMinimoChiusura;
        c.oraInizioChiusura = this.oraInizioChiusura;
        c.oraFineChiusura = this.oraFineChiusura;
        c.oraFinePostChiusura = this.oraFinePostChiusura;
        for (DayOfWeek g : DayOfWeek.values()) {
            c.impostaIntervalliGiorno(g, this.getIntervalliGiorno(g));
        }
        return c;
    }
}
