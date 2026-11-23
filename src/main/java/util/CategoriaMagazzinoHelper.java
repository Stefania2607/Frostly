package util;

/**
 * Classifica l'appartenenza di una voce di magazzino alle macro-categorie del laboratorio.
 */
public final class CategoriaMagazzinoHelper {

    private CategoriaMagazzinoHelper() {
    }

    public static boolean appartieneACategoria(String nome, String categoria, String unitaMisura, String macroCategoria) {
        if (macroCategoria == null || macroCategoria.trim().isEmpty()
                || "Tutti".equalsIgnoreCase(macroCategoria)
                || macroCategoria.toLowerCase().contains("tutte")) {
            return true;
        }
        String target = macroCategoria.trim();
        if (categoria != null && categoria.equalsIgnoreCase(target)) {
            return true;
        }
        if (isProdottoFinitoAltraCategoria(categoria)) {
            return false;
        }

        String n = (nome != null) ? nome.toLowerCase() : "";
        return matchMateriaPrimaCondivisa(target, n, categoria, unitaMisura);
    }

    private static boolean isProdottoFinitoAltraCategoria(String cat) {
        if (cat == null) return false;
        return cat.equalsIgnoreCase("Crostate")
                || cat.equalsIgnoreCase("Cioccolate calde")
                || cat.equalsIgnoreCase("Biscotti")
                || cat.equalsIgnoreCase("Bevande")
                || cat.equalsIgnoreCase("Tè e Tisane");
    }

    private static boolean matchMateriaPrimaCondivisa(String target, String n, String cat, String unitaMisura) {
        if (target.equalsIgnoreCase("Crepes")) {
            return contieneUno(n, "farina", "latte fresco", "uova", "burro", "zucchero",
                    "nutella", "pasta pistacchio", "granella di pistacchio", "confettura", "fragole");
        }
        if (target.equalsIgnoreCase("Cioccolate calde")) {
            return contieneUno(n, "latte fresco", "panna fresca d'alpeggio",
                    "cacao amaro", "cioccolato fondente", "pasta pistacchio");
        }
        if (target.equalsIgnoreCase("Caffetteria")) {
            return contieneUno(n, "caffe", "caffè", "latte fresco", "cacao amaro", "orzo", "ginseng", "liquori");
        }
        if (target.equalsIgnoreCase("Gelati")) {
            return contieneUno(n, "latte fresco", "panna fresca d'alpeggio", "zucchero", "uova",
                    "pasta pistacchio", "nocciola", "cioccolato fondente", "cacao amaro", "mandorle",
                    "mango", "fragole", "limoni", "caffe in grani", "nutella", "granella di pistacchio");
        }
        if (target.equalsIgnoreCase("Crostate")) {
            return contieneUno(n, "farina", "burro", "uova", "zucchero",
                    "latte fresco", "nutella", "confettura", "pasta pistacchio");
        }
        if (target.equalsIgnoreCase("Biscotti")) {
            return contieneUno(n, "farina", "burro", "uova", "zucchero", "mandorle", "nocciola", "cacao amaro");
        }
        if (target.equalsIgnoreCase("Panna")) {
            return n.contains("panna fresca");
        }
        if (target.equalsIgnoreCase("Laboratorio & Basi")) {
            return "Kg".equalsIgnoreCase(unitaMisura) || "Litri".equalsIgnoreCase(unitaMisura)
                    || "Pezzi".equalsIgnoreCase(unitaMisura)
                    || (cat != null && cat.equalsIgnoreCase("Laboratorio & Basi"));
        }
        return false;
    }

    private static boolean contieneUno(String testo, String... chiavi) {
        for (String k : chiavi) {
            if (testo.contains(k)) {
                return true;
            }
        }
        return false;
    }
}
