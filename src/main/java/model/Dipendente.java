package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Entity che rappresenta un Dipendente della gelateria Frostly.
 * Mantiene i riferimenti diretti agli oggetti Turno (no foreign key).
 */
public class Dipendente extends Utente {

    private final List<Turno> turni;
    private ConfigurazioneOreDipendente configurazioneOre;
    private DisponibilitaDipendente disponibilita;
    private final EnumSet<MansioneDipendente> mansioni;
    private boolean attivo;
    private Integer maxTurniSettimanali;

    public Dipendente(String nome, String cognome, String email, String password) {
        super(nome, cognome, email, password);
        this.turni = new ArrayList<>();
        this.configurazioneOre = ConfigurazioneOreDipendente.nonConfigurato();
        this.disponibilita = DisponibilitaDipendente.tuttoDisponibile();
        this.mansioni = EnumSet.of(MansioneDipendente.BANCO);
        this.attivo = true;
        this.maxTurniSettimanali = null;
    }

    public Integer getMaxTurniSettimanali() {
        return maxTurniSettimanali;
    }

    public void setMaxTurniSettimanali(Integer maxTurniSettimanali) {
        if (maxTurniSettimanali != null && maxTurniSettimanali <= 0) {
            throw new IllegalArgumentException("Il limite massimo di turni settimanali deve essere un numero intero positivo.");
        }
        this.maxTurniSettimanali = maxTurniSettimanali;
    }

    public boolean hasLimiteMaxTurni() {
        return maxTurniSettimanali != null && maxTurniSettimanali > 0;
    }

    public Set<MansioneDipendente> getMansioni() {
        return Collections.unmodifiableSet(mansioni);
    }

    public void setMansioni(Set<MansioneDipendente> nuoveMansioni) {
        this.mansioni.clear();
        if (nuoveMansioni != null && !nuoveMansioni.isEmpty()) {
            this.mansioni.addAll(nuoveMansioni);
        } else {
            this.mansioni.add(MansioneDipendente.BANCO);
        }
    }

    public boolean haMansione(MansioneDipendente mansione) {
        return mansione != null && mansioni.contains(mansione);
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }

    public ConfigurazioneOreDipendente getConfigurazioneOre() {
        return configurazioneOre != null ? configurazioneOre : ConfigurazioneOreDipendente.nonConfigurato();
    }

    public void setConfigurazioneOre(ConfigurazioneOreDipendente configurazioneOre) {
        this.configurazioneOre = configurazioneOre != null ? configurazioneOre : ConfigurazioneOreDipendente.nonConfigurato();
    }

    public DisponibilitaDipendente getDisponibilita() {
        return disponibilita != null ? disponibilita : DisponibilitaDipendente.tuttoDisponibile();
    }

    public void setDisponibilita(DisponibilitaDipendente disponibilita) {
        this.disponibilita = disponibilita != null ? disponibilita : DisponibilitaDipendente.tuttoDisponibile();
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
