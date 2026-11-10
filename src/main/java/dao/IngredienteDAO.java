package dao;

import java.util.List;
import model.Ingrediente;

/**
 * Interfaccia DAO dedicata esclusivamente alle materie prime e giacenze di magazzino.
 */
public interface IngredienteDAO {

    List<Ingrediente> caricaTuttiIngredienti();

    Ingrediente trovaIngredientePerNome(String nome);

    void aggiornaIngrediente(Ingrediente ingrediente);

    void salvaNuovoIngrediente(Ingrediente ingrediente);

    void eliminaIngrediente(String nome);
}
