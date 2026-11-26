package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Rappresenta la disponibilità settimanale e le indisponibilità su date specifiche
 * di un dipendente (concetto distinto dal monte ore contrattuale).
 */
public class DisponibilitaDipendente {

    public static final String TUTTO_IL_GIORNO = "TUTTO_IL_GIORNO";
    public static final String MATTINA = "MATTINA";
    public static final String POMERIGGIO = "POMERIGGIO";
    public static final String SERA = "SERA";
    public static final String NON_DISPONIBILE = "NON_DISPONIBILE";

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter IT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Map<DayOfWeek, String> disponibilitaGiorni = new EnumMap<>(DayOfWeek.class);
    private final Set<LocalDate> dateNonDisponibili = new LinkedHashSet<>();
    private final Set<LocalDate> feriePermessi = new LinkedHashSet<>();
    private final Map<DayOfWeek, String> preferenzeGiorni = new EnumMap<>(DayOfWeek.class);
    private final Set<Integer> fasceAbilitate = new LinkedHashSet<>(java.util.List.of(0, 1, 2, 3, 4));
    private Integer maxTurniLunghiSettimana;

    public DisponibilitaDipendente() {
        for (DayOfWeek d : DayOfWeek.values()) {
            disponibilitaGiorni.put(d, TUTTO_IL_GIORNO);
        }
    }

    public static DisponibilitaDipendente tuttoDisponibile() {
        return new DisponibilitaDipendente();
    }

    public void impostaGiorno(DayOfWeek giorno, String fascia) {
        if (giorno == null) return;
        disponibilitaGiorni.put(giorno, normalizzaFascia(fascia));
    }

    public String getFasciaGiorno(DayOfWeek giorno) {
        return disponibilitaGiorni.getOrDefault(giorno, TUTTO_IL_GIORNO);
    }

    public Map<DayOfWeek, String> getMappaGiorni() {
        return Collections.unmodifiableMap(disponibilitaGiorni);
    }

    public void aggiungiDataIndisponibile(LocalDate data) {
        if (data != null) {
            dateNonDisponibili.add(data);
        }
    }

    public void rimuoviDataIndisponibile(LocalDate data) {
        dateNonDisponibili.remove(data);
    }

    public Set<LocalDate> getDateNonDisponibili() {
        return Collections.unmodifiableSet(dateNonDisponibili);
    }

    public void aggiungiFeriePermesso(LocalDate data) {
        if (data != null) {
            feriePermessi.add(data);
        }
    }

    public void rimuoviFeriePermesso(LocalDate data) {
        feriePermessi.remove(data);
    }

    public Set<LocalDate> getFeriePermessi() {
        return Collections.unmodifiableSet(feriePermessi);
    }

    public void impostaPreferenzaGiorno(DayOfWeek giorno, String fasciaPreferita) {
        if (giorno == null) {
            return;
        }
        if (fasciaPreferita == null || fasciaPreferita.isBlank() || TUTTO_IL_GIORNO.equalsIgnoreCase(fasciaPreferita)) {
            preferenzeGiorni.remove(giorno);
        } else {
            preferenzeGiorni.put(giorno, normalizzaFascia(fasciaPreferita));
        }
    }

    public String getPreferenzaGiorno(DayOfWeek giorno) {
        return preferenzeGiorni.get(giorno);
    }

    public Map<DayOfWeek, String> getPreferenzeGiorni() {
        return Collections.unmodifiableMap(preferenzeGiorni);
    }

    public Set<Integer> getFasceAbilitate() {
        return Collections.unmodifiableSet(fasceAbilitate);
    }

    public boolean isFasciaAbilitata(int indiceFascia) {
        return fasceAbilitate.contains(indiceFascia);
    }

    public void setFasceAbilitate(Set<Integer> nuoveFasce) {
        fasceAbilitate.clear();
        if (nuoveFasce != null) {
            for (Integer idx : nuoveFasce) {
                if (idx != null && idx >= 0 && idx <= 4) {
                    fasceAbilitate.add(idx);
                }
            }
        }
    }

    public Integer getMaxTurniLunghiSettimana() {
        return maxTurniLunghiSettimana;
    }

    public void setMaxTurniLunghiSettimana(Integer maxTurniLunghiSettimana) {
        if (maxTurniLunghiSettimana != null && maxTurniLunghiSettimana < 0) {
            throw new IllegalArgumentException("Il limite massimo di turni lunghi non può essere negativo.");
        }
        this.maxTurniLunghiSettimana = maxTurniLunghiSettimana;
    }

    public boolean hasLimiteTurniLunghi() {
        return maxTurniLunghiSettimana != null;
    }

    public static int identificaIndiceFascia(String oraInizio, String oraFine) {
        if ("07:00".equals(oraInizio) && "14:00".equals(oraFine)) return 0;
        if ("14:00".equals(oraInizio) && "20:00".equals(oraFine)) return 1;
        if ("16:00".equals(oraInizio) && ("00:00".equals(oraFine) || "24:00".equals(oraFine))) return 2;
        if ("20:00".equals(oraInizio) && ("00:00".equals(oraFine) || "24:00".equals(oraFine))) return 3;
        if ("10:00".equals(oraInizio) && "18:00".equals(oraFine)) return 4;
        return -1;
    }

    public static String nomeFasciaCatalogo(int indiceFascia) {
        return switch (indiceFascia) {
            case 0 -> "Mattina (07–14)";
            case 1 -> "Pomeriggio (14–20)";
            case 2 -> "Pom./Sera (16–00)";
            case 3 -> "Serale (20–00)";
            case 4 -> "Extra Weekend (10–18)";
            default -> "Fascia";
        };
    }

    /**
     * Valuta le preferenze non vincolanti (soft): true se il turno rispetta la preferenza
     * del giorno o se non e' espressa alcuna preferenza specifica.
     */
    public boolean rispettaPreferenza(LocalDate data, String oraInizio, String oraFine) {
        if (data == null) {
            return true;
        }
        String pref = preferenzeGiorni.get(data.getDayOfWeek());
        if (pref == null || TUTTO_IL_GIORNO.equals(pref)) {
            return true;
        }
        if (NON_DISPONIBILE.equals(pref)) {
            return false;
        }
        int startMin = parseMinuti(oraInizio);
        int endMin = parseMinuti(oraFine);
        if (endMin <= startMin) {
            endMin += 24 * 60;
        }
        if (MATTINA.equals(pref)) {
            return endMin <= 14 * 60;
        }
        if (POMERIGGIO.equals(pref)) {
            return startMin >= 14 * 60 && endMin <= 20 * 60;
        }
        if (SERA.equals(pref)) {
            return startMin >= 20 * 60;
        }
        return true;
    }

    /**
     * Verifica la compatibilità di un turno con la disponibilità del dipendente.
     * Restituisce null se compatibile, oppure un messaggio descrittivo se incompatibile.
     */
    public String verificaCompatibilita(LocalDate data, String oraInizio, String oraFine) {
        if (data == null) {
            return null;
        }
        if (feriePermessi.contains(data)) {
            return "In ferie / permesso nella data " + data.format(IT_DATE);
        }
        if (dateNonDisponibili.contains(data)) {
            return "Indisponibile nella data specifica " + data.format(IT_DATE);
        }

        // Verifica abilitazione sulla fascia del catalogo (es. nuovo assunto non abilitato al pomeriggio pieno)
        int idxFascia = identificaIndiceFascia(oraInizio, oraFine);
        if (idxFascia >= 0 && !fasceAbilitate.contains(idxFascia)) {
            return "Non abilitato alla fascia " + nomeFasciaCatalogo(idxFascia);
        }

        int startMin = parseMinuti(oraInizio);
        int endMin = parseMinuti(oraFine);
        if (endMin <= startMin) {
            endMin += 24 * 60;
        }
        if (maxTurniLunghiSettimana != null && maxTurniLunghiSettimana == 0 && (endMin - startMin) >= 8 * 60) {
            return "Abilitato solo a turni corti (< 8 h)";
        }

        String fascia = getFasciaGiorno(data.getDayOfWeek());
        if (NON_DISPONIBILE.equals(fascia)) {
            return "Non disponibile di " + nomeGiorno(data.getDayOfWeek());
        }
        if (TUTTO_IL_GIORNO.equals(fascia)) {
            return null;
        }

        if (MATTINA.equals(fascia) && endMin > 14 * 60) {
            return "Di " + nomeGiorno(data.getDayOfWeek()) + " è disponibile solo la mattina (fino alle 14:00)";
        }
        if (POMERIGGIO.equals(fascia) && (startMin < 14 * 60 || endMin > 20 * 60)) {
            return "Di " + nomeGiorno(data.getDayOfWeek()) + " è disponibile solo nel pomeriggio (14:00–20:00)";
        }
        if (SERA.equals(fascia) && startMin < 20 * 60) {
            return "Di " + nomeGiorno(data.getDayOfWeek()) + " è disponibile solo di sera (dalle 20:00)";
        }
        return null;
    }

    public String serializza() {
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek d : DayOfWeek.values()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(d.getValue()).append('=').append(getFasciaGiorno(d));
        }
        if (!dateNonDisponibili.isEmpty()) {
            sb.append("|DATES=");
            boolean first = true;
            for (LocalDate dt : dateNonDisponibili) {
                if (!first) sb.append(',');
                sb.append(dt.format(ISO_DATE));
                first = false;
            }
        }
        if (!feriePermessi.isEmpty()) {
            sb.append("|FERIE=");
            boolean first = true;
            for (LocalDate dt : feriePermessi) {
                if (!first) sb.append(',');
                sb.append(dt.format(ISO_DATE));
                first = false;
            }
        }
        if (!preferenzeGiorni.isEmpty()) {
            sb.append("|PREF=");
            boolean first = true;
            for (Map.Entry<DayOfWeek, String> e : preferenzeGiorni.entrySet()) {
                if (!first) sb.append(';');
                sb.append(e.getKey().getValue()).append('=').append(e.getValue());
                first = false;
            }
        }
        if (fasceAbilitate.size() < 5) {
            sb.append("|FASCE=");
            boolean first = true;
            for (Integer idx : fasceAbilitate) {
                if (!first) sb.append(',');
                sb.append(idx);
                first = false;
            }
        }
        if (maxTurniLunghiSettimana != null) {
            sb.append("|MAX8H=").append(maxTurniLunghiSettimana);
        }
        return sb.toString();
    }

    public static DisponibilitaDipendente deserializza(String raw) {
        DisponibilitaDipendente d = new DisponibilitaDipendente();
        if (raw == null || raw.isBlank()) {
            return d;
        }
        String[] sezioni = raw.split("\\|");
        for (String pair : sezioni[0].split(";")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                try {
                    int dow = Integer.parseInt(kv[0].trim());
                    if (dow >= 1 && dow <= 7) {
                        d.impostaGiorno(DayOfWeek.of(dow), kv[1].trim());
                    }
                } catch (Exception ignored) {
                    // Ignora coppia non valida
                }
            }
        }
        for (int i = 1; i < sezioni.length; i++) {
            String sez = sezioni[i];
            if (sez.startsWith("DATES=")) {
                parseDateList(sez.substring("DATES=".length()), d::aggiungiDataIndisponibile);
            } else if (sez.startsWith("FERIE=")) {
                parseDateList(sez.substring("FERIE=".length()), d::aggiungiFeriePermesso);
            } else if (sez.startsWith("PREF=")) {
                for (String pair : sez.substring("PREF=".length()).split(";")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        try {
                            int dow = Integer.parseInt(kv[0].trim());
                            if (dow >= 1 && dow <= 7) {
                                d.impostaPreferenzaGiorno(DayOfWeek.of(dow), kv[1].trim());
                            }
                        } catch (Exception ignored) {
                            // Ignora preferenza non valida
                        }
                    }
                }
            } else if (sez.startsWith("FASCE=")) {
                Set<Integer> parsed = new LinkedHashSet<>();
                String rawF = sez.substring("FASCE=".length());
                for (String tok : rawF.split(",")) {
                    if (!tok.isBlank()) {
                        try {
                            int idx = Integer.parseInt(tok.trim());
                            if (idx >= 0 && idx <= 4) {
                                parsed.add(idx);
                            }
                        } catch (NumberFormatException ignored) {
                            // Ignora indice non valido
                        }
                    }
                }
                d.setFasceAbilitate(parsed);
            } else if (sez.startsWith("MAX8H=")) {
                try {
                    int m = Integer.parseInt(sez.substring("MAX8H=".length()).trim());
                    if (m >= 0) {
                        d.setMaxTurniLunghiSettimana(m);
                    }
                } catch (NumberFormatException ignored) {
                    // Ignora valore non numerico
                }
            }
        }
        return d;
    }

    private static void parseDateList(String rawDates, java.util.function.Consumer<LocalDate> consumer) {
        for (String token : rawDates.split(",")) {
            if (!token.isBlank()) {
                try {
                    consumer.accept(LocalDate.parse(token.trim(), ISO_DATE));
                } catch (Exception ignored) {
                    // Ignora data malformata
                }
            }
        }
    }

    public String getRiepilogoSintetico() {
        int nonDisp = 0;
        int parziali = 0;
        for (DayOfWeek d : DayOfWeek.values()) {
            String f = getFasciaGiorno(d);
            if (NON_DISPONIBILE.equals(f)) nonDisp++;
            else if (!TUTTO_IL_GIORNO.equals(f)) parziali++;
        }
        if (nonDisp == 0 && parziali == 0 && dateNonDisponibili.isEmpty()
                && fasceAbilitate.size() == 5 && maxTurniLunghiSettimana == null) {
            return "Sempre disponibile";
        }
        StringBuilder sb = new StringBuilder();
        if (nonDisp > 0) sb.append(nonDisp).append(nonDisp == 1 ? " giorno off" : " giorni off");
        if (parziali > 0) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(parziali).append(parziali == 1 ? " fascia limitata" : " fasce limitate");
        }
        if (fasceAbilitate.size() < 5) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(fasceAbilitate.size()).append("/5 fasce abilitate");
        }
        if (maxTurniLunghiSettimana != null) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(maxTurniLunghiSettimana == 0
                    ? "Solo turni corti"
                    : "Max " + maxTurniLunghiSettimana + " turno/i da 8 h");
        }
        if (!dateNonDisponibili.isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(dateNonDisponibili.size()).append(dateNonDisponibili.size() == 1 ? " data bloccata" : " date bloccate");
        }
        return sb.toString();
    }

    public static String etichettaFascia(String fascia) {
        return switch (normalizzaFascia(fascia)) {
            case MATTINA -> "Solo mattina (fino alle 14)";
            case POMERIGGIO -> "Solo pomeriggio (14–20)";
            case SERA -> "Solo sera (dalle 20)";
            case NON_DISPONIBILE -> "Non disponibile";
            default -> "Disponibile tutto il giorno";
        };
    }

    public static String codiceDaEtichetta(String etichetta) {
        if (etichetta == null) return TUTTO_IL_GIORNO;
        String s = etichetta.toLowerCase();
        if (s.contains("non disponibile")) return NON_DISPONIBILE;
        if (s.contains("mattina")) return MATTINA;
        if (s.contains("pomeriggio")) return POMERIGGIO;
        if (s.contains("sera")) return SERA;
        return TUTTO_IL_GIORNO;
    }

    private static String normalizzaFascia(String f) {
        if (f == null) return TUTTO_IL_GIORNO;
        return switch (f.trim().toUpperCase()) {
            case MATTINA -> MATTINA;
            case POMERIGGIO -> POMERIGGIO;
            case SERA -> SERA;
            case NON_DISPONIBILE -> NON_DISPONIBILE;
            default -> TUTTO_IL_GIORNO;
        };
    }

    private static int parseMinuti(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) return 0;
        String[] p = hhmm.trim().split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        return h * 60 + m;
    }

    public static String nomeGiorno(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "Lunedì";
            case TUESDAY -> "Martedì";
            case WEDNESDAY -> "Mercoledì";
            case THURSDAY -> "Giovedì";
            case FRIDAY -> "Venerdì";
            case SATURDAY -> "Sabato";
            case SUNDAY -> "Domenica";
        };
    }
}
