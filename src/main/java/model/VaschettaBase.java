package model;

import java.util.ArrayList;
import java.util.List;

/**
 * ConcreteComponent del pattern GoF Decorator: rappresenta la vaschetta base con uno o piu' gusti artigianali.
 */
public class VaschettaBase implements VaschettaComponent {

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