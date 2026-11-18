package bean;

/**
 * JavaBean puro per il trasporto dei dati di un Ingrediente tra Boundary e Control.
 */
public class IngredienteBean {

    private String nome;
    private double quantitaDisponibile;
    private double sogliaMinima;
    private String unitaMisura;
    private boolean sottoSoglia;
    private String categoria;

    public IngredienteBean() {
        // Costruttore vuoto standard JavaBean
    }

    public IngredienteBean(String nome, double quantitaDisponibile, double sogliaMinima,
                           String unitaMisura, boolean sottoSoglia) {
        this(nome, quantitaDisponibile, sogliaMinima, unitaMisura, sottoSoglia, "");
    }

    public IngredienteBean(String nome, double quantitaDisponibile, double sogliaMinima,
                           String unitaMisura, boolean sottoSoglia, String categoria) {
        this.nome = nome;
        this.quantitaDisponibile = quantitaDisponibile;
        this.sogliaMinima = sogliaMinima;
        this.unitaMisura = unitaMisura;
        this.sottoSoglia = sottoSoglia;
        this.categoria = categoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean appartieneACategoria(String macroCategoria) {
        if (macroCategoria == null || macroCategoria.trim().isEmpty()
                || "Tutti".equalsIgnoreCase(macroCategoria)
                || macroCategoria.toLowerCase().contains("tutte")) {
            return true;
        }
        String target = macroCategoria.trim();
        String cat = getCategoria();
        if (cat != null && cat.equalsIgnoreCase(target)) {
            return true;
        }

        // I prodotti finiti di altre categorie (crostate a porzioni, bustine di cioccolata, biscotti, bevande, tè)
        // NON sono materie prime e non devono MAI comparire nel magazzino di un'altra categoria!
        if (cat != null && (cat.equalsIgnoreCase("Crostate")
                || cat.equalsIgnoreCase("Cioccolate calde")
                || cat.equalsIgnoreCase("Biscotti")
                || cat.equalsIgnoreCase("Bevande")
                || cat.equalsIgnoreCase("Tè e Tisane"))) {
            return false;
        }

        String n = (this.nome != null) ? this.nome.toLowerCase() : "";

        // Condivisione delle sole materie prime coincidenti del laboratorio per macro-categoria:
        if (target.equalsIgnoreCase("Crepes")) {
            return n.contains("farina") || n.contains("latte fresco") || n.contains("uova")
                    || n.contains("burro") || n.contains("zucchero") || n.contains("nutella")
                    || n.contains("pasta pistacchio") || n.contains("granella di pistacchio")
                    || n.contains("confettura") || n.contains("fragole");
        }
        if (target.equalsIgnoreCase("Cioccolate calde")) {
            return n.contains("latte fresco") || n.contains("panna fresca d'alpeggio")
                    || n.contains("cacao amaro") || n.contains("cioccolato fondente")
                    || n.contains("pasta pistacchio");
        }
        if (target.equalsIgnoreCase("Caffetteria")) {
            return n.contains("caffe") || n.contains("caffè") || n.contains("latte fresco")
                    || n.contains("cacao amaro") || n.contains("orzo") || n.contains("ginseng")
                    || n.contains("liquori");
        }
        if (target.equalsIgnoreCase("Gelati")) {
            return n.contains("latte fresco") || n.contains("panna fresca d'alpeggio")
                    || n.contains("zucchero") || n.contains("uova") || n.contains("pasta pistacchio")
                    || n.contains("nocciola") || n.contains("cioccolato fondente")
                    || n.contains("cacao amaro") || n.contains("mandorle") || n.contains("mango")
                    || n.contains("fragole") || n.contains("limoni") || n.contains("caffe in grani")
                    || n.contains("nutella") || n.contains("granella di pistacchio");
        }
        if (target.equalsIgnoreCase("Crostate")) {
            return n.contains("farina") || n.contains("burro") || n.contains("uova")
                    || n.contains("zucchero") || n.contains("latte fresco") || n.contains("nutella")
                    || n.contains("confettura") || n.contains("pasta pistacchio");
        }
        if (target.equalsIgnoreCase("Biscotti")) {
            return n.contains("farina") || n.contains("burro") || n.contains("uova")
                    || n.contains("zucchero") || n.contains("mandorle") || n.contains("nocciola")
                    || n.contains("cacao amaro");
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

    public String getNome() {
        return nome;
    }

    public double getQuantitaDisponibile() {
        return quantitaDisponibile;
    }

    public double getSogliaMinima() {
        return sogliaMinima;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public boolean isSottoSoglia() {
        return sottoSoglia;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setQuantitaDisponibile(double quantitaDisponibile) {
        this.quantitaDisponibile = quantitaDisponibile;
    }

    public void setSogliaMinima(double sogliaMinima) {
        this.sogliaMinima = sogliaMinima;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    public void setSottoSoglia(boolean sottoSoglia) {
        this.sottoSoglia = sottoSoglia;
    }
}
