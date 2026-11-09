package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity che rappresenta una singola vaschetta configurata all'interno di un ordine cliente.
 */
public class VaschettaOrdinata {

    private final List<Gusto> gusti;
    private final double pesoKg;
    private final boolean conPanna;
    private final boolean conGranellaBronte;
    private final double prezzo;
    private final String descrizione;

    public VaschettaOrdinata(List<Gusto> gusti, double pesoKg, boolean conPanna, boolean conGranellaBronte) {
        this.gusti = new ArrayList<>(gusti);
        this.pesoKg = pesoKg;
        this.conPanna = conPanna;
        this.conGranellaBronte = conGranellaBronte;

        VaschettaComponent component = new VaschettaBase(gusti, pesoKg);
        if (conPanna) {
            component = new ConPannaMontataDecorator(component);
        }
        if (conGranellaBronte) {
            component = new ConGranellaBronteDecorator(component);
        }
        this.prezzo = component.calcolaPrezzoFinale();
        this.descrizione = component.getDescrizioneCompleta();
    }

    public VaschettaOrdinata(List<Gusto> gusti, double pesoKg, boolean conPanna, boolean conGranellaBronte,
                             String descrizione, double prezzo) {
        this.gusti = gusti != null ? new ArrayList<>(gusti) : new ArrayList<>();
        this.pesoKg = pesoKg;
        this.conPanna = conPanna;
        this.conGranellaBronte = conGranellaBronte;
        this.descrizione = descrizione != null ? descrizione : "";
        this.prezzo = prezzo;
    }

    public List<Gusto> getGusti() {
        return Collections.unmodifiableList(gusti);
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public boolean isConPanna() {
        return conPanna;
    }

    public boolean isConGranellaBronte() {
        return conGranellaBronte;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
