package control.applicativo;

import bean.RiepilogoOreDipendenteBean;
import bean.RiepilogoOreDipendenteBean.StatoMonteOre;
import bean.TurnoBean;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.ConfigurazioneOreDipendente;
import model.Dipendente;
import model.DisponibilitaDipendente;
import model.ModalitaMonteOre;

/**
 * Servizio di dominio STATELESS dedicato al calcolo deterministico delle ore settimanali
 * pianificate, alla gestione di turni notturni e a cavallo tra due settimane, e alla verifica
 * dei limiti contrattuali e delle disponibilità del personale.
 */
public final class CalcolatoreOreSettimanali {

    private static final Pattern DATA_PATTERN = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double EPSILON = 0.005;

    private CalcolatoreOreSettimanali() {
    }

    /**
     * Anteprima dell'impatto di una nuova assegnazione di turno su un dipendente.
     */
    public record AnteprimaAssegnazione(
            String email,
            String nomeCompleto,
            double oreAttuali,
            double oreTurno,
            double oreRisultanti,
            ConfigurazioneOreDipendente configurazioneOre,
            boolean superaMassimo,
            double oreEccesso,
            String avvisoDisponibilita,
            int turniAttuali,
            Integer maxTurniSettimanali,
            boolean superaMaxTurni) {
    }

    /**
     * Calcola la durata effettiva in ore di un turno, supportando ore frazionarie
     * e turni notturni che attraversano la mezzanotte (es. 22:00 -> 02:00 = 4.0 h).
     */
    public static double calcolaDurataOre(String oraInizio, String oraFine) {
        return calcolaDurataOre(oraInizio, oraFine, 0);
    }

    /**
     * Calcola la durata lavorativa netta in ore di un turno, sottraendo eventuali pause non retribuite.
     */
    public static double calcolaDurataOre(String oraInizio, String oraFine, int pausaMinuti) {
        if (oraInizio == null || oraFine == null) {
            return 0.0;
        }
        LocalDate base = LocalDate.of(2026, 1, 5);
        LocalDateTime start = parseInizio(base, oraInizio);
        LocalDateTime end = parseFine(base, start, oraFine);
        long minutiLordi = Math.max(0, java.time.temporal.ChronoUnit.MINUTES.between(start, end));
        long pausa = Math.max(0, Math.min(minutiLordi, pausaMinuti));
        return ConfigurazioneOreDipendente.arrotonda((minutiLordi - pausa) / 60.0);
    }

    /**
     * Calcola quante ore nette di un singolo turno ricadono effettivamente all'interno
     * della settimana [lunedi 00:00, lunedi+7 00:00). Gestisce pause non retribuite e
     * turni notturni che attraversano il confine tra Domenica e Lunedì della settimana successiva.
     */
    public static double calcolaOreTurnoNellaSettimana(TurnoBean turno, LocalDate lunediSettimana) {
        if (turno == null || lunediSettimana == null || isTurnoEsclusoDalConteggio(turno)) {
            return 0.0;
        }
        LocalDate dataTurno = estraiDataTurno(turno.getData(), lunediSettimana);
        if (dataTurno == null) {
            return 0.0;
        }
        LocalDateTime inizioTurno = parseInizio(dataTurno, turno.getOraInizio());
        LocalDateTime fineTurno = parseFine(dataTurno, inizioTurno, turno.getOraFine());
        long durataTotaleMin = Math.max(0, java.time.temporal.ChronoUnit.MINUTES.between(inizioTurno, fineTurno));
        if (durataTotaleMin <= 0) {
            return 0.0;
        }

        LocalDateTime inizioSettimana = lunediSettimana.atStartOfDay();
        LocalDateTime fineSettimana = lunediSettimana.plusDays(7).atStartOfDay();

        LocalDateTime overlapStart = inizioTurno.isAfter(inizioSettimana) ? inizioTurno : inizioSettimana;
        LocalDateTime overlapEnd = fineTurno.isBefore(fineSettimana) ? fineTurno : fineSettimana;
        if (!overlapEnd.isAfter(overlapStart)) {
            return 0.0;
        }
        long minutiInSettimana = java.time.temporal.ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
        int pausaTot = Math.max(0, Math.min((int) durataTotaleMin, turno.getPausaMinuti()));
        double pausaProporzionale = pausaTot * ((double) minutiInSettimana / (double) durataTotaleMin);
        return ConfigurazioneOreDipendente.arrotonda(Math.max(0.0, minutiInSettimana - pausaProporzionale) / 60.0);
    }

    /**
     * Calcola il totale ore settimanali pianificate per un dipendente evitando doppi conteggi
     * sullo stesso codice turno e ignorando turni annullati/rifiutati.
     */
    public static double calcolaOreDipendenteNellaSettimana(String emailDipendente,
                                                            List<TurnoBean> tuttiTurni,
                                                            LocalDate lunediSettimana) {
        if (emailDipendente == null || tuttiTurni == null || lunediSettimana == null) {
            return 0.0;
        }
        double totale = 0.0;
        Set<String> codiciContati = new HashSet<>();
        for (TurnoBean t : tuttiTurni) {
            if (t == null || t.getEmailDipendente() == null
                    || !emailDipendente.equalsIgnoreCase(t.getEmailDipendente().trim())) {
                continue;
            }
            if (t.getCodiceTurno() != null && !t.getCodiceTurno().isBlank()
                    && !codiciContati.add(t.getCodiceTurno().trim().toUpperCase(Locale.ROOT))) {
                continue;
            }
            totale += calcolaOreTurnoNellaSettimana(t, lunediSettimana);
        }
        return ConfigurazioneOreDipendente.arrotonda(totale);
    }

    public static int contaTurniDipendenteNellaSettimana(String emailDipendente,
                                                         List<TurnoBean> tuttiTurni,
                                                         LocalDate lunediSettimana) {
        if (emailDipendente == null || tuttiTurni == null || lunediSettimana == null) {
            return 0;
        }
        int count = 0;
        Set<String> codiciContati = new HashSet<>();
        for (TurnoBean t : tuttiTurni) {
            if (t == null || t.getEmailDipendente() == null
                    || !emailDipendente.equalsIgnoreCase(t.getEmailDipendente().trim())) {
                continue;
            }
            if (t.getCodiceTurno() != null && !t.getCodiceTurno().isBlank()
                    && !codiciContati.add(t.getCodiceTurno().trim().toUpperCase(Locale.ROOT))) {
                continue;
            }
            if (calcolaOreTurnoNellaSettimana(t, lunediSettimana) > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Costruisce il riepilogo settimanale completo per ogni dipendente, con stato cromatico,
     * testo esplicativo e percentuale di avanzamento.
     */
    public static List<RiepilogoOreDipendenteBean> calcolaRiepiloghiSettimana(List<Dipendente> dipendenti,
                                                                             List<TurnoBean> tuttiTurni,
                                                                             LocalDate lunediSettimana) {
        List<RiepilogoOreDipendenteBean> risultati = new ArrayList<>();
        if (dipendenti == null) {
            return risultati;
        }
        for (Dipendente d : dipendenti) {
            double ore = calcolaOreDipendenteNellaSettimana(d.getEmail(), tuttiTurni, lunediSettimana);
            int nTurni = contaTurniDipendenteNellaSettimana(d.getEmail(), tuttiTurni, lunediSettimana);
            ConfigurazioneOreDipendente cfg = d.getConfigurazioneOre() != null
                    ? d.getConfigurazioneOre() : ConfigurazioneOreDipendente.nonConfigurato();
            DisponibilitaDipendente disp = d.getDisponibilita() != null
                    ? d.getDisponibilita() : DisponibilitaDipendente.tuttoDisponibile();

            StatoMonteOre stato;
            String messaggio;
            double progresso;

            if (!cfg.isConfigurato()) {
                stato = StatoMonteOre.CONFIGURAZIONE_RICHIESTA;
                messaggio = "Configurazione richiesta";
                progresso = 0.0;
            } else {
                double min = cfg.getOreMinime();
                double max = cfg.getOreMassime();
                double minToll = cfg.getMinimoConTolleranza();
                double maxToll = cfg.getMassimoConTolleranza();
                progresso = max > 0 ? Math.min(1.0, ore / max) : 0.0;

                if (ore > maxToll + EPSILON) {
                    stato = StatoMonteOre.OLTRE_MASSIMO;
                    String ecc = ConfigurazioneOreDipendente.formattaOre(ore - max);
                    messaggio = cfg.haTolleranza()
                            ? "Fuori tolleranza (+" + ecc + " h)"
                            : "Superamento di " + ecc + " h";
                } else if (ore > max + EPSILON) {
                    stato = StatoMonteOre.ENTRO_TOLLERANZA;
                    messaggio = "+" + ConfigurazioneOreDipendente.formattaOre(ore - max) + " h (entro tolleranza)";
                } else if (ore + EPSILON < minToll) {
                    stato = StatoMonteOre.SOTTO_MINIMO;
                    String mancanti = ConfigurazioneOreDipendente.formattaOre(min - ore);
                    messaggio = ore == 0
                            ? "Pianificazione in corso (mancano " + mancanti + " h)"
                            : "Mancano " + mancanti + " h";
                } else if (ore + EPSILON < min) {
                    stato = StatoMonteOre.ENTRO_TOLLERANZA;
                    messaggio = "-" + ConfigurazioneOreDipendente.formattaOre(min - ore) + " h (entro tolleranza)";
                } else {
                    stato = StatoMonteOre.NEL_RANGE;
                    messaggio = cfg.getModalita() == ModalitaMonteOre.ORE_FISSE
                            ? "Obiettivo raggiunto"
                            : "Nel range previsto";
                }
            }

            Integer maxTurni = d.getMaxTurniSettimanali();
            if (maxTurni != null && maxTurni > 0 && nTurni > maxTurni) {
                stato = StatoMonteOre.OLTRE_MASSIMO;
                messaggio = "Oltre max turni (" + nTurni + "/" + maxTurni + ")";
            }

            risultati.add(new RiepilogoOreDipendenteBean(
                    d.getEmail(),
                    d.getNome() + " " + d.getCognome(),
                    ore,
                    cfg,
                    disp,
                    stato,
                    messaggio,
                    progresso,
                    nTurni,
                    maxTurni));
        }
        return risultati;
    }

    /**
     * Calcola l'anteprima dell'assegnazione di un turno (ore risultanti, eventuale superamento
     * e verifica disponibilità) prima della conferma da parte del Maestro.
     */
    public static AnteprimaAssegnazione calcolaAnteprimaAssegnazione(Dipendente dipendente,
                                                                     List<TurnoBean> tuttiTurni,
                                                                     LocalDate lunediSettimana,
                                                                     LocalDate dataTurno,
                                                                     String oraInizio,
                                                                     String oraFine) {
        double attuali = calcolaOreDipendenteNellaSettimana(dipendente.getEmail(), tuttiTurni, lunediSettimana);
        TurnoBean simulato = new TurnoBean();
        try {
            simulato.setData(dataTurno.format(FMT_DATA));
            simulato.setOraInizio(oraInizio);
            simulato.setOraFine(oraFine);
        } catch (Exception ignored) {
            // Fallback su durata intera
        }
        double oreTurno = calcolaOreTurnoNellaSettimana(simulato, lunediSettimana);
        if (oreTurno <= 0) {
            oreTurno = calcolaDurataOre(oraInizio, oraFine);
        }
        double risultanti = ConfigurazioneOreDipendente.arrotonda(attuali + oreTurno);

        ConfigurazioneOreDipendente cfg = dipendente.getConfigurazioneOre() != null
                ? dipendente.getConfigurazioneOre() : ConfigurazioneOreDipendente.nonConfigurato();
        boolean supera = cfg.isConfigurato() && risultanti > cfg.getOreMassime() + EPSILON;
        double eccesso = supera ? ConfigurazioneOreDipendente.arrotonda(risultanti - cfg.getOreMassime()) : 0.0;

        DisponibilitaDipendente disp = dipendente.getDisponibilita() != null
                ? dipendente.getDisponibilita() : DisponibilitaDipendente.tuttoDisponibile();
        String avvisoDisp = disp.verificaCompatibilita(dataTurno, oraInizio, oraFine);

        int turniAttuali = contaTurniDipendenteNellaSettimana(dipendente.getEmail(), tuttiTurni, lunediSettimana);
        Integer maxTurni = dipendente.getMaxTurniSettimanali();
        boolean superaMaxTurni = maxTurni != null && maxTurni > 0 && (turniAttuali + 1) > maxTurni;

        return new AnteprimaAssegnazione(
                dipendente.getEmail(),
                dipendente.getNome() + " " + dipendente.getCognome(),
                attuali,
                oreTurno,
                risultanti,
                cfg,
                supera,
                eccesso,
                avvisoDisp,
                turniAttuali,
                maxTurni,
                superaMaxTurni);
    }

    /**
     * Verifica se due turni si sovrappongono oppure non rispettano il riposo minimo obbligatorio
     * (in ore) tra la fine di un turno e l'inizio dell'altro, gestendo anche turni oltre la mezzanotte.
     */
    /**
     * Risultato documentabile della verifica di copertura oraria effettiva su tutta la settimana.
     */
    public record RiepilogoCoperturaSettimana(
            int fasceCoperte,
            int fasceParziali,
            int fasceVuote,
            int postiRichiestiTotali,
            int postiCopertiTotali,
            boolean chiusuraOk,
            String riepilogoChiusura,
            List<String> dettagliScoperture) {

        public double getPercentualeCopertura() {
            if (postiRichiestiTotali <= 0) {
                return 100.0;
            }
            return Math.min(100.0, Math.round((postiCopertiTotali * 1000.0) / postiRichiestiTotali) / 10.0);
        }
    }

    /**
     * Verifica la presenza simultanea effettiva del personale sull'intera durata di ogni fascia
     * configurata (non solo all'inizio del turno), controllando in particolare la copertura di chiusura.
     */
    public static RiepilogoCoperturaSettimana valutaCoperturaIntervalli(
            LocalDate lunediSettimana,
            List<TurnoBean> turni,
            model.ConfigurazioneCoperturaFlessibile copertura,
            int stepMinuti) {
        LocalDate lunedi = lunediSettimana.with(DayOfWeek.MONDAY);
        model.ConfigurazioneCoperturaFlessibile cfg = copertura != null
                ? copertura : model.ConfigurazioneCoperturaFlessibile.predefinita();
        int step = (stepMinuti == 15 || stepMinuti == 30 || stepMinuti == 60) ? stepMinuti : 30;

        int coperte = 0;
        int parziali = 0;
        int vuote = 0;
        int richiestiTot = 0;
        int copertiTot = 0;
        int chiusureOkCount = 0;
        int chiusureTotCount = 0;
        List<String> scoperture = new ArrayList<>();

        for (int g = 0; g < 7; g++) {
            LocalDate giorno = lunedi.plusDays(g);
            DayOfWeek dow = giorno.getDayOfWeek();
            for (model.ConfigurazioneCoperturaFlessibile.IntervalloCopertura ic : cfg.getIntervalliGiorno(dow)) {
                if (ic.personaleMinimo() <= 0) {
                    continue;
                }
                LocalDateTime startIc = parseInizio(giorno, ic.oraInizio());
                LocalDateTime endIc = parseFine(giorno, startIc, ic.oraFine());

                int minPresenti = Integer.MAX_VALUE;
                for (LocalDateTime t = startIc; t.isBefore(endIc); t = t.plusMinutes(step)) {
                    LocalDateTime tNext = t.plusMinutes(step);
                    if (tNext.isAfter(endIc)) {
                        tNext = endIc;
                    }
                    int presenti = contaPresentiNellIntervallo(turni, lunedi, t, tNext);
                    if (presenti < minPresenti) {
                        minPresenti = presenti;
                    }
                }
                if (minPresenti == Integer.MAX_VALUE) {
                    minPresenti = 0;
                }

                richiestiTot += ic.personaleMinimo();
                copertiTot += Math.min(ic.personaleMinimo(), minPresenti);

                if (ic.isChiusura()) {
                    chiusureTotCount++;
                    if (minPresenti >= ic.personaleMinimo()) {
                        chiusureOkCount++;
                    }
                }

                if (minPresenti >= ic.personaleMinimo()) {
                    coperte++;
                } else if (minPresenti == 0) {
                    vuote++;
                    scoperture.add(DisponibilitaDipendente.nomeGiorno(dow) + " " + giorno.format(FMT_DATA)
                            + " " + ic.oraInizio() + "–" + ic.oraFine()
                            + (ic.isChiusura() ? " [CHIUSURA]" : "")
                            + " — Richiesti: " + ic.personaleMinimo()
                            + ", Presenti: 0 (fascia scoperta)");
                } else {
                    parziali++;
                    int mancanti = ic.personaleMinimo() - minPresenti;
                    scoperture.add(DisponibilitaDipendente.nomeGiorno(dow) + " " + giorno.format(FMT_DATA)
                            + " " + ic.oraInizio() + "–" + ic.oraFine()
                            + (ic.isChiusura() ? " [CHIUSURA]" : "")
                            + " — Richiesti: " + ic.personaleMinimo()
                            + ", Presenti: " + minPresenti
                            + " (manca " + mancanti + (mancanti == 1 ? " dipendente)" : " dipendenti)"));
                }
            }
        }

        boolean chiusuraOk = (chiusureOkCount == chiusureTotCount);
        String riepilogoChiusura = "Chiusura: " + chiusureOkCount + "/" + chiusureTotCount + " giorni coperti";
        return new RiepilogoCoperturaSettimana(
                coperte, parziali, vuote, richiestiTot, copertiTot, chiusuraOk, riepilogoChiusura, scoperture);
    }

    public static int contaPresentiNellIntervallo(List<TurnoBean> turni,
                                                  LocalDate lunediRif,
                                                  LocalDateTime startSlot,
                                                  LocalDateTime endSlot) {
        if (turni == null || turni.isEmpty()) {
            return 0;
        }
        Set<String> presentiUnici = new HashSet<>();
        for (TurnoBean t : turni) {
            if (t == null || t.getEmailDipendente() == null || isTurnoEsclusoDalConteggio(t)) {
                continue;
            }
            LocalDate dataT = estraiDataTurno(t.getData(), lunediRif);
            if (dataT == null) {
                continue;
            }
            LocalDateTime iniT = parseInizio(dataT, t.getOraInizio());
            LocalDateTime finT = parseFine(dataT, iniT, t.getOraFine());
            // Il dipendente copre lo slot se il suo turno copre interamente [startSlot, endSlot]
            if (!iniT.isAfter(startSlot) && !finT.isBefore(endSlot)) {
                presentiUnici.add(t.getEmailDipendente().trim().toLowerCase(Locale.ROOT));
            }
        }
        return presentiUnici.size();
    }

    public static boolean isConflittoORiposoInsufficiente(LocalDate dataA, String inizioA, String fineA,
                                                          LocalDate dataB, String inizioB, String fineB,
                                                          int riposoMinimoOre) {
        if (dataA == null || dataB == null) {
            return false;
        }
        LocalDateTime startA = parseInizio(dataA, inizioA);
        LocalDateTime endA = parseFine(dataA, startA, fineA);
        LocalDateTime startB = parseInizio(dataB, inizioB);
        LocalDateTime endB = parseFine(dataB, startB, fineB);

        // Sovrapposizione diretta
        if (startA.isBefore(endB) && startB.isBefore(endA)) {
            return true;
        }
        if (riposoMinimoOre <= 0) {
            return false;
        }
        long gapMinuti;
        if (!endA.isAfter(startB)) {
            gapMinuti = java.time.temporal.ChronoUnit.MINUTES.between(endA, startB);
        } else {
            gapMinuti = java.time.temporal.ChronoUnit.MINUTES.between(endB, startA);
        }
        return gapMinuti < (long) riposoMinimoOre * 60L;
    }

    private static boolean isTurnoEsclusoDalConteggio(TurnoBean t) {
        if (t.getStato() == null) {
            return false;
        }
        String st = t.getStato().trim().toUpperCase(Locale.ROOT);
        return st.contains("ANNULLAT") || st.contains("ELIMINAT") || st.contains("RIFIUTAT");
    }

    public static LocalDate estraiDataTurno(String campoData, LocalDate lunediRiferimento) {
        if (campoData == null || campoData.isBlank()) {
            return null;
        }
        Matcher m = DATA_PATTERN.matcher(campoData);
        if (m.find()) {
            try {
                return LocalDate.parse(m.group(1), FMT_DATA);
            } catch (Exception ignored) {
                return null;
            }
        }
        // Dati legacy senza data esplicita (es. "Lunedi"): associati solo alla settimana corrente
        LocalDate lunediCorrente = LocalDate.now().with(DayOfWeek.MONDAY);
        if (lunediRiferimento != null && !lunediRiferimento.equals(lunediCorrente)) {
            return null;
        }
        String norm = campoData.toLowerCase(Locale.ITALIAN).replace("ì", "i");
        if (norm.contains("lun")) return lunediCorrente;
        if (norm.contains("mar")) return lunediCorrente.plusDays(1);
        if (norm.contains("mer")) return lunediCorrente.plusDays(2);
        if (norm.contains("gio")) return lunediCorrente.plusDays(3);
        if (norm.contains("ven")) return lunediCorrente.plusDays(4);
        if (norm.contains("sab")) return lunediCorrente.plusDays(5);
        if (norm.contains("dom")) return lunediCorrente.plusDays(6);
        return null;
    }

    private static LocalDateTime parseInizio(LocalDate data, String oraInizio) {
        String s = oraInizio != null ? oraInizio.trim() : "00:00";
        if ("24:00".equals(s)) {
            return data.plusDays(1).atStartOfDay();
        }
        return LocalDateTime.of(data, LocalTime.parse(s));
    }

    private static LocalDateTime parseFine(LocalDate data, LocalDateTime inizio, String oraFine) {
        String s = oraFine != null ? oraFine.trim() : "00:00";
        if ("24:00".equals(s) || "00:00".equals(s)) {
            return data.plusDays(1).atStartOfDay();
        }
        LocalDateTime fine = LocalDateTime.of(data, LocalTime.parse(s));
        if (!fine.isAfter(inizio)) {
            fine = fine.plusDays(1);
        }
        return fine;
    }
}
