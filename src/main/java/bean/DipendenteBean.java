package bean;

import java.util.EnumSet;
import java.util.Set;
import model.ConfigurazioneOreDipendente;
import model.DisponibilitaDipendente;
import model.MansioneDipendente;

/**
 * JavaBean di trasporto per i profili dei dipendenti verso le Boundary (GUI/CLI).
 * Non espone credenziali o password dell'Entity Dipendente.
 */
public class DipendenteBean {

    private String nome;
    private String cognome;
    private String email;
    private boolean attivo = true;
    private Integer maxTurniSettimanali;
    private ConfigurazioneOreDipendente configurazioneOre = ConfigurazioneOreDipendente.nonConfigurato();
    private DisponibilitaDipendente disponibilita = new DisponibilitaDipendente();
    private Set<MansioneDipendente> mansioni = EnumSet.of(MansioneDipendente.BANCO);

    public DipendenteBean() {
    }

    public DipendenteBean(String nome, String cognome, String email, boolean attivo,
                          Integer maxTurniSettimanali,
                          ConfigurazioneOreDipendente configurazioneOre,
                          DisponibilitaDipendente disponibilita,
                          Set<MansioneDipendente> mansioni) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.attivo = attivo;
        this.maxTurniSettimanali = maxTurniSettimanali;
        this.configurazioneOre = configurazioneOre != null ? configurazioneOre : ConfigurazioneOreDipendente.nonConfigurato();
        this.disponibilita = disponibilita != null ? disponibilita : new DisponibilitaDipendente();
        if (mansioni != null && !mansioni.isEmpty()) {
            this.mansioni = EnumSet.copyOf(mansioni);
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getNomeCompleto() {
        String n = nome != null ? nome.trim() : "";
        String c = cognome != null ? cognome.trim() : "";
        return (n + " " + c).trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }

    public Integer getMaxTurniSettimanali() {
        return maxTurniSettimanali;
    }

    public void setMaxTurniSettimanali(Integer maxTurniSettimanali) {
        this.maxTurniSettimanali = (maxTurniSettimanali != null && maxTurniSettimanali > 0) ? maxTurniSettimanali : null;
    }

    public boolean hasLimiteMaxTurni() {
        return maxTurniSettimanali != null && maxTurniSettimanali > 0;
    }

    public Set<MansioneDipendente> getMansioni() {
        return EnumSet.copyOf(mansioni);
    }

    public void setMansioni(Set<MansioneDipendente> mansioni) {
        if (mansioni != null && !mansioni.isEmpty()) {
            this.mansioni = EnumSet.copyOf(mansioni);
        }
    }

    public boolean haMansione(MansioneDipendente mansione) {
        return mansione != null && mansioni.contains(mansione);
    }

    public ConfigurazioneOreDipendente getConfigurazioneOre() {
        return configurazioneOre;
    }

    public void setConfigurazioneOre(ConfigurazioneOreDipendente configurazioneOre) {
        this.configurazioneOre = configurazioneOre != null ? configurazioneOre : ConfigurazioneOreDipendente.nonConfigurato();
    }

    public DisponibilitaDipendente getDisponibilita() {
        return disponibilita;
    }

    public void setDisponibilita(DisponibilitaDipendente disponibilita) {
        this.disponibilita = disponibilita != null ? disponibilita : new DisponibilitaDipendente();
    }
}
