package model;

import java.util.ArrayList;
import java.util.List;

/**
 * ConcreteComponent del pattern GoF Decorator: rappresenta la vaschetta base con uno o piu' gusti artigianali.
 */
public class VaschettaBase implements VaschettaComponent {

    public static final double PREZZO_SOLA_PANNA_AL_KG = 12.0;
    public static final double PESO_350G = 0.35;
    public static final double PESO_500G = 0.50;
    public static final double PESO_750G = 0.75;
    public static final double PESO_1000G = 1.00;
    public static final int MAX_GUSTI_350G = 3;
    public static final int MAX_GUSTI_500G = 4;
    public static final int MAX_GUSTI_750G = 5;
    public static final int MAX_GUSTI_1000G = 6;

    private final List<Gusto> gusti;
    private final double pesoKg;

    public VaschettaBase(Gusto gusto, double pesoKg) {
        this.gusti = new ArrayList<>();
        if (gusto != null) {
            this.gusti.add(gusto);
        }
        this.pesoKg = pesoKg;
    }

    public VaschettaBase(List<Gusto> gusti, double pesoKg) {
        this.gusti = new ArrayList<>(gusti);
        this.pesoKg = pesoKg;
    }

    @Override
    public String getDescrizioneCompleta() {
        if (gusti.isEmpty()) {
            return "Vaschetta Panna Montata Fresca (" + pesoKg + " kg)";
        }
        if (gusti.size() == 1) {
            return "Vaschetta " + gusti.get(0).getNome() + " (" + pesoKg + " kg)";
        }
        StringBuilder sb = new StringBuilder("Vaschetta ");
        for (int i = 0; i < gusti.size(); i++) {
            if (i > 0) sb.append(" + ");
            sb.append(gusti.get(i).getNome());
        }
        sb.append(" (").append(pesoKg).append(" kg)");
        return sb.toString();
    }

    @Override
    public double calcolaPrezzoFinale() {
        if (gusti.isEmpty()) {
            return Math.round(14.0 * pesoKg * 100.0) / 100.0;
        }
        double sommaPrezziAlKg = 0.0;
        for (Gusto g : gusti) {
            sommaPrezziAlKg += g.getPrezzoAlKg();
        }
        double prezzoMedioAlKg = sommaPrezziAlKg / gusti.size();
        return Math.round(prezzoMedioAlKg * pesoKg * 100.0) / 100.0;
    }
}