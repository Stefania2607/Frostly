package model;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Mansioni operative riconosciute in gelateria per la copertura dei turni.
 */
public enum MansioneDipendente {
    BANCO("Addetto al Banco"),
    LABORATORIO("Addetto al Laboratorio"),
    RESPONSABILE("Responsabile di Turno");

    private final String etichetta;

    MansioneDipendente(String etichetta) {
        this.etichetta = etichetta;
    }

    public String getEtichetta() {
        return etichetta;
    }

    public static String serializza(Set<MansioneDipendente> mansioni) {
        if (mansioni == null || mansioni.isEmpty()) {
            return BANCO.name();
        }
        StringBuilder sb = new StringBuilder();
        for (MansioneDipendente m : mansioni) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(m.name());
        }
        return sb.toString();
    }

    public static EnumSet<MansioneDipendente> deserializza(String raw) {
        EnumSet<MansioneDipendente> set = EnumSet.noneOf(MansioneDipendente.class);
        if (raw == null || raw.isBlank()) {
            set.add(BANCO);
            return set;
        }
        for (String token : raw.split(",")) {
            if (!token.isBlank()) {
                try {
                    set.add(MansioneDipendente.valueOf(token.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                    // Ignora valori sconosciuti per retrocompatibilita'
                }
            }
        }
        if (set.isEmpty()) {
            set.add(BANCO);
        }
        return set;
    }
}
