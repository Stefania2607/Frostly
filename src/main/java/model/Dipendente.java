package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity che rappresenta un Dipendente della gelateria Frostly.
 * Mantiene i riferimenti diretti agli oggetti Turno (no foreign key).
 */
public class Dipendente extends Utente {

    private final List<Turno> turni;

    public Dipendente(String nome, String cognome, String email, String password) {
        super(nome, cognome, email, password);
        this.turni = new ArrayList<>();
    }

    public void aggiungiTurno(Turno turno) {
        this.turni.add(turno);
    }

    public void rimuoviTurno(Turno turno) {
        this.turni.remove(turno);
    }

    /**
     * Operazione di business: verifica se il dipendente e' libero in quella data e fascia oraria.
     */
    public boolean isDisponibile(String data, String oraInizio, String oraFine) {
        for (Turno t : turni) {
            if (t.getData().equals(data) && t.isInConflitto(oraInizio, oraFine)) {
                return false;
            }
        }
        return true;
    }

    public List<Turno> getTurni() {
        return new ArrayList<>(turni);
    }

    @Override
    public String toString() {
        return getNome() + " " + getCognome();
    }
}
