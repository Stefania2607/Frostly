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
        return util.CategoriaMagazzinoHelper.appartieneACategoria(this.nome, getCategoria(), this.unitaMisura, macroCategoria);
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
