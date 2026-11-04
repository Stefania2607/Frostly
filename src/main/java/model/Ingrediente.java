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

    public Ingrediente(String nome, double quantitaDisponibile, double sogliaMinima, String unitaMisura) {
        this.nome = nome;
        this.quantitaDisponibile = quantitaDisponibile;
        this.sogliaMinima = sogliaMinima;
        this.unitaMisura = unitaMisura;
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