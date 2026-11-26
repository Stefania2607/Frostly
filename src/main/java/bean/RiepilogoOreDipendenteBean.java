package bean;

import model.ConfigurazioneOreDipendente;
import model.DisponibilitaDipendente;

/**
 * Bean di riepilogo delle ore settimanali e dello stato di conformità di un dipendente.
 */
public class RiepilogoOreDipendenteBean {

    public enum StatoMonteOre {
        CONFIGURAZIONE_RICHIESTA,
        SOTTO_MINIMO,
        ENTRO_TOLLERANZA,
        NEL_RANGE,
        OLTRE_MASSIMO
    }

    private final String email;
    private final String nomeCompleto;
    private final double oreAssegnate;
    private final ConfigurazioneOreDipendente configurazioneOre;
    private final DisponibilitaDipendente disponibilita;
    private final StatoMonteOre stato;
    private final String messaggioStato;
    private final double percentualeProgresso;
    private final int numeroTurniSettimana;
    private final Integer maxTurniSettimanali;

    public RiepilogoOreDipendenteBean(String email,
                                     String nomeCompleto,
                                     double oreAssegnate,
                                     ConfigurazioneOreDipendente configurazioneOre,
                                     DisponibilitaDipendente disponibilita,
                                     StatoMonteOre stato,
                                     String messaggioStato,
                                     double percentualeProgresso,
                                     int numeroTurniSettimana) {
        this(email, nomeCompleto, oreAssegnate, configurazioneOre, disponibilita,
                stato, messaggioStato, percentualeProgresso, numeroTurniSettimana, null);
    }

    public RiepilogoOreDipendenteBean(String email,
                                     String nomeCompleto,
                                     double oreAssegnate,
                                     ConfigurazioneOreDipendente configurazioneOre,
                                     DisponibilitaDipendente disponibilita,
                                     StatoMonteOre stato,
                                     String messaggioStato,
                                     double percentualeProgresso,
                                     int numeroTurniSettimana,
                                     Integer maxTurniSettimanali) {
        this.email = email;
        this.nomeCompleto = nomeCompleto;
        this.oreAssegnate = oreAssegnate;
        this.configurazioneOre = configurazioneOre;
        this.disponibilita = disponibilita;
        this.stato = stato;
        this.messaggioStato = messaggioStato;
        this.percentualeProgresso = percentualeProgresso;
        this.numeroTurniSettimana = numeroTurniSettimana;
        this.maxTurniSettimanali = maxTurniSettimanali;
    }

    public String getEmail() {
        return email;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public double getOreAssegnate() {
        return oreAssegnate;
    }

    public ConfigurazioneOreDipendente getConfigurazioneOre() {
        return configurazioneOre;
    }

    public DisponibilitaDipendente getDisponibilita() {
        return disponibilita;
    }

    public StatoMonteOre getStato() {
        return stato;
    }

    public String getMessaggioStato() {
        return messaggioStato;
    }

    public double getPercentualeProgresso() {
        return percentualeProgresso;
    }

    public int getNumeroTurniSettimana() {
        return numeroTurniSettimana;
    }

    public Integer getMaxTurniSettimanali() {
        return maxTurniSettimanali;
    }

    public boolean isOltreMaxTurni() {
        return maxTurniSettimanali != null && maxTurniSettimanali > 0 && numeroTurniSettimana > maxTurniSettimanali;
    }

    /**
     * Scostamento rispetto all'obiettivo o all'intervallo originale:
     * positivo se sopra il massimo, negativo se sotto il minimo, 0 nel range.
     */
    public double getScostamentoOre() {
        if (configurazioneOre == null || !configurazioneOre.isConfigurato()) {
            return 0.0;
        }
        if (oreAssegnate > configurazioneOre.getOreMassime() + 0.005) {
            return ConfigurazioneOreDipendente.arrotonda(oreAssegnate - configurazioneOre.getOreMassime());
        }
        if (oreAssegnate + 0.005 < configurazioneOre.getOreMinime()) {
            return ConfigurazioneOreDipendente.arrotonda(oreAssegnate - configurazioneOre.getOreMinime());
        }
        return 0.0;
    }

    public boolean isFuoriTolleranza() {
        if (configurazioneOre == null || !configurazioneOre.isConfigurato()) {
            return false;
        }
        return oreAssegnate > configurazioneOre.getMassimoConTolleranza() + 0.005
                || oreAssegnate + 0.005 < configurazioneOre.getMinimoConTolleranza();
    }

    public String getIniziali() {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "ST";
        }
        String[] parti = nomeCompleto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parti.length); i++) {
            if (!parti[i].isEmpty()) {
                sb.append(Character.toUpperCase(parti[i].charAt(0)));
            }
        }
        return sb.length() > 0 ? sb.toString() : "ST";
    }
}
