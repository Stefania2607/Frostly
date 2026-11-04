package model;

import frostly.exception.IngredienteEsauritoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity che rappresenta la Ricetta artigianale di un gusto di gelato.
 * Contiene puntatori diretti agli oggetti IngredienteRicetta.
 */
public class Ricetta {

    private String nomeRicetta;
    private String procedimento;
    private List<IngredienteRicetta> composizione;

    public Ricetta(String nomeRicetta, String procedimento) {
        this.nomeRicetta = nomeRicetta;
        this.procedimento = procedimento;
        this.composizione = new ArrayList<>();
    }

    public void aggiungiIngrediente(Ingrediente ingrediente, double dosePerVaschetta) {
        this.composizione.add(new IngredienteRicetta(ingrediente, dosePerVaschetta));
    }

    public boolean verificaDisponibilitaIngredienti(int numeroVaschette) {
        for (IngredienteRicetta voce : composizione) {
            double fabbisogno = voce.calcolaFabbisogno(numeroVaschette);
            if (!voce.getIngrediente().haScortaSufficiente(fabbisogno)) {
                return false;
            }
        }
        return true;
    }

    public void consumaIngredientiPerProduzione(int numeroVaschette) throws IngredienteEsauritoException {
        for (IngredienteRicetta voce : composizione) {
            double fabbisogno = voce.calcolaFabbisogno(numeroVaschette);
            if (!voce.getIngrediente().haScortaSufficiente(fabbisogno)) {
                double mancante = fabbisogno - voce.getIngrediente().getQuantitaDisponibile();
                throw new IngredienteEsauritoException(voce.getIngrediente().getNome(), mancante);
            }
        }
        for (IngredienteRicetta voce : composizione) {
            voce.getIngrediente().consuma(voce.calcolaFabbisogno(numeroVaschette));
        }
    }

    public String getNomeRicetta() {
        return nomeRicetta;
    }

    public String getProcedimento() {
        return procedimento;
    }

    public List<IngredienteRicetta> getComposizione() {
        return new ArrayList<>(composizione);
    }
}