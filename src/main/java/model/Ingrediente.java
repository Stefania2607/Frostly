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
        return util.CategoriaMagazzinoHelper.appartieneACategoria(this.nome, getCategoria(), this.unitaMisura, macroCategoria);
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