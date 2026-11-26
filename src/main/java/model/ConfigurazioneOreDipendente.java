package model;

import frostly.exception.DatiNonValidiException;
import java.util.Locale;

/**
 * Rappresenta il monte ore settimanale configurato per un dipendente
 * (modalità fissa oppure intervallo minimo–massimo, con supporto a ore frazionarie).
 */
public class ConfigurazioneOreDipendente {

    private final ModalitaMonteOre modalita;
    private final Double oreMinime;
    private final Double oreMassime;
    private final double tolleranzaSotto;
    private final double tolleranzaSopra;

    private ConfigurazioneOreDipendente(ModalitaMonteOre modalita,
                                        Double oreMinime,
                                        Double oreMassime,
                                        double tolleranzaSotto,
                                        double tolleranzaSopra) {
        this.modalita = modalita;
        this.oreMinime = oreMinime;
        this.oreMassime = oreMassime;
        this.tolleranzaSotto = Math.max(0.0, arrotonda(tolleranzaSotto));
        this.tolleranzaSopra = Math.max(0.0, arrotonda(tolleranzaSopra));
    }

    public static ConfigurazioneOreDipendente nonConfigurato() {
        return new ConfigurazioneOreDipendente(ModalitaMonteOre.NON_CONFIGURATO, null, null, 0.0, 0.0);
    }

    public static ConfigurazioneOreDipendente oreFisse(double ore) throws DatiNonValidiException {
        return oreFisse(ore, 0.0);
    }

    public static ConfigurazioneOreDipendente oreFisse(double ore, double tolleranza) throws DatiNonValidiException {
        validaValoreOre(ore, "Il monte ore settimanale");
        validaTolleranza(tolleranza, "La tolleranza oraria");
        double norm = arrotonda(ore);
        double toll = arrotonda(tolleranza);
        return new ConfigurazioneOreDipendente(ModalitaMonteOre.ORE_FISSE, norm, norm, toll, toll);
    }

    public static ConfigurazioneOreDipendente intervallo(double minimo, double massimo) throws DatiNonValidiException {
        return intervallo(minimo, massimo, 0.0, 0.0);
    }

    public static ConfigurazioneOreDipendente intervallo(double minimo,
                                                         double massimo,
                                                         double tolleranzaSotto,
                                                         double tolleranzaSopra) throws DatiNonValidiException {
        validaValoreOre(minimo, "Il minimo ore settimanali");
        validaValoreOre(massimo, "Il massimo ore settimanali");
        validaTolleranza(tolleranzaSotto, "La tolleranza sotto il minimo");
        validaTolleranza(tolleranzaSopra, "La tolleranza sopra il massimo");
        if (minimo > massimo) {
            throw new DatiNonValidiException("Il minimo ore settimanali non può superare il massimo.");
        }
        return new ConfigurazioneOreDipendente(
                ModalitaMonteOre.INTERVALLO, arrotonda(minimo), arrotonda(massimo), tolleranzaSotto, tolleranzaSopra);
    }

    public static ConfigurazioneOreDipendente daDatabase(String modalitaStr, Double min, Double max) {
        return daDatabase(modalitaStr, min, max, 0.0, 0.0);
    }

    public static ConfigurazioneOreDipendente daDatabase(String modalitaStr,
                                                         Double min,
                                                         Double max,
                                                         Double tollSotto,
                                                         Double tollSopra) {
        if (modalitaStr == null || modalitaStr.isBlank()
                || ModalitaMonteOre.NON_CONFIGURATO.name().equalsIgnoreCase(modalitaStr.trim())) {
            return nonConfigurato();
        }
        double ts = tollSotto != null && tollSotto >= 0 ? tollSotto : 0.0;
        double tp = tollSopra != null && tollSopra >= 0 ? tollSopra : 0.0;
        try {
            ModalitaMonteOre mod = ModalitaMonteOre.valueOf(modalitaStr.trim().toUpperCase(Locale.ROOT));
            if (mod == ModalitaMonteOre.ORE_FISSE && min != null && min > 0) {
                return oreFisse(min, Math.max(ts, tp));
            }
            if (mod == ModalitaMonteOre.INTERVALLO && min != null && max != null && min >= 0 && max > 0 && min <= max) {
                return intervallo(min, max, ts, tp);
            }
        } catch (Exception ignored) {
            // Valori legacy o incompleti: mantiene NON_CONFIGURATO
        }
        return nonConfigurato();
    }

    private static void validaTolleranza(double val, String etichetta) throws DatiNonValidiException {
        if (Double.isNaN(val) || Double.isInfinite(val) || val < 0) {
            throw new DatiNonValidiException(etichetta + " non può essere negativa.");
        }
        if (val > 40) {
            throw new DatiNonValidiException(etichetta + " non può superare 40 ore.");
        }
    }

    private static void validaValoreOre(double val, String etichetta) throws DatiNonValidiException {
        if (Double.isNaN(val) || Double.isInfinite(val) || val <= 0) {
            throw new DatiNonValidiException(etichetta + " deve essere un numero positivo maggiore di zero.");
        }
        if (val > 168) {
            throw new DatiNonValidiException(etichetta + " non può superare le 168 ore settimanali.");
        }
    }

    public static double arrotonda(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    public static String formattaOre(double ore) {
        double r = arrotonda(ore);
        if (Math.abs(r - Math.rint(r)) < 0.005) {
            return String.valueOf((long) Math.rint(r));
        }
        return String.format(Locale.ITALY, "%.1f", r);
    }

    public boolean isConfigurato() {
        return modalita != ModalitaMonteOre.NON_CONFIGURATO && oreMinime != null && oreMassime != null;
    }

    public ModalitaMonteOre getModalita() {
        return modalita;
    }

    public Double getOreMinime() {
        return oreMinime;
    }

    public Double getOreMassime() {
        return oreMassime;
    }

    public double getTolleranzaSotto() {
        return tolleranzaSotto;
    }

    public double getTolleranzaSopra() {
        return tolleranzaSopra;
    }

    public boolean haTolleranza() {
        return tolleranzaSotto > 0.005 || tolleranzaSopra > 0.005;
    }

    public double getMinimoConTolleranza() {
        if (!isConfigurato()) {
            return 0.0;
        }
        return Math.max(0.0, arrotonda(oreMinime - tolleranzaSotto));
    }

    public double getMassimoConTolleranza() {
        if (!isConfigurato()) {
            return 0.0;
        }
        return arrotonda(oreMassime + tolleranzaSopra);
    }

    public String getEtichettaObiettivo() {
        if (!isConfigurato()) {
            return "Configurazione richiesta";
        }
        if (modalita == ModalitaMonteOre.ORE_FISSE) {
            String base = formattaOre(oreMassime) + " h";
            if (haTolleranza()) {
                return base + " (±" + formattaOre(tolleranzaSopra) + " h)";
            }
            return base;
        }
        String base = formattaOre(oreMinime) + "–" + formattaOre(oreMassime) + " h";
        if (haTolleranza()) {
            return base + " (-" + formattaOre(tolleranzaSotto) + "/+" + formattaOre(tolleranzaSopra) + " h)";
        }
        return base;
    }
}
