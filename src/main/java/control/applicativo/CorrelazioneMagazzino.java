package control.applicativo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Gusto;
import model.Ingrediente;

/**
 * Classe di supporto applicativo che centralizza la correlazione tra i prodotti del menu
 * e le relative materie prime o giacenze di magazzino.
 * Usata sia da GestioneMenuControl (calcolo disponibilità e porzioni ordinabili)
 * sia da OrdiniControl (scarico e ripristino automatico del magazzino).
 */
public final class CorrelazioneMagazzino {

    private CorrelazioneMagazzino() {
    }

    /**
     * Restituisce la mappa per un prodotto del menu. Per gli articoli venduti a pezzo diretto
     * (Crostate, Biscotti, Bevande, Tisane, Panna) usa {@link #trovaArticoloDiretto}.
     */
    public static Map<String, Double> calcolaConsumoArticolo(String nomeProdotto, String categoria) {
        Map<String, Double> consumi = new LinkedHashMap<>();
        if (nomeProdotto == null || nomeProdotto.isBlank()) {
            return consumi;
        }
        String lower = nomeProdotto.trim().toLowerCase();
        String cat = categoria != null ? categoria.trim() : "";

        if (isCaffetteria(lower, cat)) {
            aggiungiConsumiCaffetteria(lower, consumi);
            return consumi;
        }
        if (isCrepe(lower, cat)) {
            aggiungiConsumiCrepe(lower, consumi);
            return consumi;
        }
        if (lower.startsWith("cioccolata calda")) {
            consumi.put("Latte Fresco Alta Qualita", 0.15);
        }
        return consumi;
    }

    /**
     * Restituisce la mappa (nomeIngrediente -> consumo) per una vaschetta di gelato artigianale.
     */
    public static Map<String, Double> calcolaConsumoVaschettaGelato(List<Gusto> gusti, double pesoKg,
                                                                    boolean conPanna, boolean conGranella) {
        Map<String, Double> consumi = new LinkedHashMap<>();
        if (pesoKg <= 0.0) {
            return consumi;
        }
        somma(consumi, "Latte Fresco Alta Qualita", arrotonda(pesoKg * 0.40));
        somma(consumi, "Panna Fresca d'Alpeggio", arrotonda(pesoKg * 0.20));
        somma(consumi, "Zucchero Semolato Italiano", arrotonda(pesoKg * 0.15));

        if (gusti != null && !gusti.isEmpty()) {
            double quotaPerGusto = pesoKg / gusti.size();
            for (Gusto g : gusti) {
                aggiungiIngredienteCaratterizzanteGelato(g.getNome(), quotaPerGusto, consumi);
            }
        }
        if (conPanna) {
            somma(consumi, "Panna Fresca d'Alpeggio", 0.10);
        }
        if (conGranella) {
            somma(consumi, "Granella di Pistacchio di Bronte DOP", 0.05);
        }
        return consumi;
    }

    /**
     * Restituisce le materie prime minime richieste per preparare almeno una porzione di un gusto gelato.
     */
    public static Map<String, Double> calcolaMinimoGustoGelato(String nomeGusto) {
        Map<String, Double> minimi = new LinkedHashMap<>();
        minimi.put("Latte Fresco Alta Qualita", 0.14);
        minimi.put("Panna Fresca d'Alpeggio", 0.07);
        minimi.put("Zucchero Semolato Italiano", 0.05);
        if (nomeGusto != null) {
            aggiungiIngredienteCaratterizzanteGelato(nomeGusto, 0.20, minimi);
        }
        return minimi;
    }

    public static boolean isProdottoEspresso(String nomeProdotto, String categoria) {
        String lower = nomeProdotto != null ? nomeProdotto.trim().toLowerCase() : "";
        String cat = categoria != null ? categoria.trim() : "";
        return isCaffetteria(lower, cat) || isCrepe(lower, cat);
    }

    public static Ingrediente trovaArticoloDiretto(List<Ingrediente> magazzino, String nomeProdotto) {
        if (magazzino == null || nomeProdotto == null || nomeProdotto.isBlank()) {
            return null;
        }
        for (Ingrediente i : magazzino) {
            if (i.getNome() != null && i.getNome().equalsIgnoreCase(nomeProdotto)) {
                return i;
            }
        }
        String lowerProd = nomeProdotto.toLowerCase();
        for (Ingrediente i : magazzino) {
            if (i.getNome() != null) {
                String lowerIng = i.getNome().toLowerCase();
                if (lowerProd.startsWith(lowerIng) || lowerIng.startsWith(lowerProd)) {
                    return i;
                }
            }
        }
        return null;
    }

    private static boolean isCaffetteria(String lower, String cat) {
        return "Caffetteria".equalsIgnoreCase(cat)
                || lower.startsWith("caffe") || lower.startsWith("caffè")
                || lower.contains("cappuccino") || lower.contains("latte macchiato")
                || lower.contains("affogato");
    }

    private static boolean isCrepe(String lower, String cat) {
        return "Crepes".equalsIgnoreCase(cat) || lower.startsWith("crepe");
    }

    private static void aggiungiConsumiCaffetteria(String lower, Map<String, Double> consumi) {
        if (lower.contains("ginseng")) {
            somma(consumi, "Caffe al Ginseng Solubile", 0.015);
        } else if (lower.contains("orzo")) {
            somma(consumi, "Orzo Solubile Tostato", 0.015);
        } else {
            somma(consumi, "Caffe in Grani 100% Arabica", 0.008);
        }

        if (lower.contains("cappuccino")) {
            somma(consumi, "Latte Fresco Alta Qualita", 0.12);
        } else if (lower.contains("latte macchiato")) {
            somma(consumi, "Latte Fresco Alta Qualita", 0.18);
        } else if (lower.contains("macchiato")) {
            somma(consumi, "Latte Fresco Alta Qualita", 0.03);
        } else if (lower.contains("affogato")) {
            somma(consumi, "Latte Fresco Alta Qualita", 0.05);
        }

        if (lower.contains("marocchino")) {
            somma(consumi, "Cacao Amaro Monorigine", 0.005);
        } else if (lower.contains("corretto")) {
            somma(consumi, "Liquori per Caffe Corretto (Grappa e Sambuca)", 0.02);
        }
    }

    private static void aggiungiConsumiCrepe(String lower, Map<String, Double> consumi) {
        somma(consumi, "Farina per Crepes e Dolci", 0.08);
        somma(consumi, "Latte Fresco Alta Qualita", 0.10);
        somma(consumi, "Uova Fresche da Allevamento", 1.0);
        somma(consumi, "Burro Fresco di Latteria", 0.02);
        somma(consumi, "Zucchero Semolato Italiano", 0.01);

        if (lower.contains("nutella") || lower.contains("stelle")) {
            somma(consumi, "Nutella Ferrero", 0.05);
        } else if (lower.contains("pistacchio")) {
            somma(consumi, "Pasta Pistacchio di Bronte DOP", 0.04);
            somma(consumi, "Granella di Pistacchio di Bronte DOP", 0.01);
        } else if (lower.contains("albicocc") || lower.contains("marmellata")) {
            somma(consumi, "Confettura Extra di Albicocche", 0.04);
        } else if (lower.contains("visciol") || lower.contains("frutti di bosco") || lower.contains("fragol")) {
            somma(consumi, "Confettura di Frutti di Bosco", 0.04);
        }
    }

    private static void aggiungiIngredienteCaratterizzanteGelato(String nomeGusto, double quotaKg, Map<String, Double> consumi) {
        if (nomeGusto == null) return;
        String nome = nomeGusto.toLowerCase();
        if (nome.contains("pistacchio")) {
            somma(consumi, "Pasta Pistacchio di Bronte DOP", arrotonda(quotaKg * 0.25));
        } else if (nome.contains("nocciola")) {
            somma(consumi, "Nocciola Piemonte IGP", arrotonda(quotaKg * 0.25));
        } else if (nome.contains("fondente") || nome.contains("cioccolat")) {
            somma(consumi, "Cioccolato Fondente Ecuador 75%", arrotonda(quotaKg * 0.25));
        } else if (nome.contains("mango")) {
            somma(consumi, "Polpa di Mango Alphonso", arrotonda(quotaKg * 0.35));
        } else if (nome.contains("fragol")) {
            somma(consumi, "Fragole Fresche di Bosco", arrotonda(quotaKg * 0.35));
        } else if (nome.contains("limon")) {
            somma(consumi, "Limoni di Sorrento IGP", arrotonda(quotaKg * 0.30));
        } else if (nome.contains("caff")) {
            somma(consumi, "Caffe in Grani 100% Arabica", arrotonda(quotaKg * 0.15));
        } else if (nome.contains("mandorl") || nome.contains("amarandola")) {
            somma(consumi, "Mandorle di Avola", arrotonda(quotaKg * 0.20));
        } else if (nome.contains("nutella")) {
            somma(consumi, "Nutella Ferrero", arrotonda(quotaKg * 0.20));
        } else if (nome.contains("cacao")) {
            somma(consumi, "Cacao Amaro Monorigine", arrotonda(quotaKg * 0.20));
        }
    }

    private static void somma(Map<String, Double> mappa, String chiave, double valore) {
        if (valore > 0.0) {
            mappa.merge(chiave, valore, Double::sum);
        }
    }

    private static double arrotonda(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
