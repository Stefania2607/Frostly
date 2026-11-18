package model;

import frostly.exception.IngredienteEsauritoException;
/**
 * Entity che rappresenta un Ingrediente nel magazzino di Frostly.
 */
public class Ingrediente {

    private String nome;
    private double quantitaDisponibile;
    private double sogliaMinima;
    private String unitaMisura;
    private String categoria;

    public Ingrediente(String nome, double quantitaDisponibile, double sogliaMinima, String unitaMisura) {
        this(nome, quantitaDisponibile, sogliaMinima, unitaMisura, inferisciCategoria(nome, unitaMisura));
    }

    public Ingrediente(String nome, double quantitaDisponibile, double sogliaMinima, String unitaMisura, String categoria) {
        this.nome = nome;
        this.quantitaDisponibile = quantitaDisponibile;
        this.sogliaMinima = sogliaMinima;
        this.unitaMisura = unitaMisura;
        this.categoria = (categoria != null && !categoria.trim().isEmpty())
                ? categoria.trim()
                : inferisciCategoria(nome, unitaMisura);
    }

    public static String inferisciCategoria(String nome, String unitaMisura) {
        if (nome == null) return "Laboratorio & Basi";
        String n = nome.trim().toLowerCase();

        if (n.startsWith("te ") || n.startsWith("tè ") || n.startsWith("tisana")
                || ("bustine".equalsIgnoreCase(unitaMisura) && !n.contains("cioccolata"))) {
            return "Tè e Tisane";
        }
        if (n.contains("cioccolata calda")) {
            return "Cioccolate calde";
        }
        if (n.contains("caffe in grani") || n.contains("caffè in grani") || n.contains("ginseng")
                || n.contains("orzo solubile") || n.contains("liquori per caffe") || n.contains("liquore per caffe")
                || n.startsWith("caffe") || n.startsWith("caffè")) {
            return "Caffetteria";
        }
        if (n.contains("biscott") || n.contains("cantucci") || n.contains("baci di dama") || n.contains("canestrelli")
                || n.contains("occhi di bue") || n.contains("frollini") || n.contains("amaretti") || n.contains("zaletti")
                || n.contains("ricciarelli") || n.contains("diamanti") || n.contains("krumiri")) {
            return "Biscotti";
        }
        if (n.contains("crostata")) {
            return "Crostate";
        }
        if (n.startsWith("acqua") || n.contains("coca cola") || n.contains("fanta") || n.startsWith("succo")
                || n.contains("spremuta") || n.contains("centrifugato") || "bottiglie".equalsIgnoreCase(unitaMisura)) {
            return "Bevande";
        }
        if (n.contains("crepe")) {
            return "Crepes";
        }
        if (n.startsWith("panna fresca 100") || n.startsWith("panna fresca 200") || n.startsWith("panna fresca 300")) {
            return "Panna";
        }
        if (n.contains("pistacchio") || n.contains("nocciola") || n.contains("cioccolato fondente")
                || n.contains("polpa di mango") || n.contains("fragole") || n.contains("limoni") || n.contains("vaschetta")) {
            return "Gelati";
        }
        return "Laboratorio & Basi";
    }

    public String getCategoria() {
        return (categoria != null && !categoria.isEmpty()) ? categoria : inferisciCategoria(nome, unitaMisura);
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

    public boolean haScortaSufficiente(double quantitaRichiesta) {
        return this.quantitaDisponibile >= quantitaRichiesta;
    }

    public boolean isSottoSoglia() {
        return this.quantitaDisponibile < this.sogliaMinima;
    }

    public void consuma(double quantitaDaScalare) throws IngredienteEsauritoException {
        if (!haScortaSufficiente(quantitaDaScalare)) {
            double mancante = quantitaDaScalare - this.quantitaDisponibile;
            throw new IngredienteEsauritoException(this.nome, mancante);
        }
        this.quantitaDisponibile -= quantitaDaScalare;
    }

    public void rifornisci(double quantitaAggiunta) {
        if (quantitaAggiunta > 0) {
            this.quantitaDisponibile += quantitaAggiunta;
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getQuantitaDisponibile() {
        return quantitaDisponibile;
    }

    public void setQuantitaDisponibile(double quantitaDisponibile) {
        this.quantitaDisponibile = quantitaDisponibile;
    }

    public double getSogliaMinima() {
        return sogliaMinima;
    }

    public void setSogliaMinima(double sogliaMinima) {
        this.sogliaMinima = sogliaMinima;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }
}