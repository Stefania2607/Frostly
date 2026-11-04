package model;

/**
 * Classe d'associazione che collega una Ricetta a un Ingrediente con la dose per vaschetta.
 */
public class IngredienteRicetta {

    private Ingrediente ingrediente;
    private double dosePerVaschetta;

    public IngredienteRicetta(Ingrediente ingrediente, double dosePerVaschetta) {
        this.ingrediente = ingrediente;
        this.dosePerVaschetta = dosePerVaschetta;
    }

    public double calcolaFabbisogno(int numeroVaschette) {
        return this.dosePerVaschetta * numeroVaschette;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public void setIngrediente(Ingrediente ingrediente) {
        this.ingrediente = ingrediente;
    }

    public double getDosePerVaschetta() {
        return dosePerVaschetta;
    }

    public void setDosePerVaschetta(double dosePerVaschetta) {
        this.dosePerVaschetta = dosePerVaschetta;
    }
}