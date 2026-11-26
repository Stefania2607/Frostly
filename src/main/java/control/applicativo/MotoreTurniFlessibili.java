package control.applicativo;

import bean.PropostaTurniSettimanaBean;
import bean.RiepilogoOreDipendenteBean;
import bean.TurnoBean;
import control.applicativo.CalcolatoreOreSettimanali.RiepilogoCoperturaSettimana;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import model.ConfigurazioneCoperturaFlessibile;
import model.ConfigurazioneCoperturaFlessibile.IntervalloCopertura;
import model.ConfigurazioneGenerazioneTurni;
import model.ConfigurazioneOreDipendente;
import model.Dipendente;
import model.DisponibilitaDipendente;
import model.StatoTurno;

/**
 * Motore di generazione intelligente di turni flessibili (orari personalizzati anche fuori catalogo).
 * Opera esclusivamente su una bozza temporanea (PropostaTurniSettimanaBean) senza mai salvare
 * i turni ufficiali prima dell'approvazione esplicita del Maestro.
 */
public final class MotoreTurniFlessibili {

    private static final String[] GIORNI_BREVI = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
    private static final DateTimeFormatter FMT_COMPLETO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double EPS = 0.005;

    private MotoreTurniFlessibili() {
    }

    private record FinestraOraria(int inizioMin, int fineMin, String oraInizio, String oraFine, double durataOre) {
    }

    /**
     * Genera una proposta settimanale flessibile sulla base dei vincoli rigidi, della copertura
     * per intervalli (con priorita' alla chiusura), del monte ore e dell'equita'.
     */
    public static PropostaTurniSettimanaBean generaPropostaFlessibile(
            LocalDate lunediSettimana,
            List<Dipendente> tuttiDipendenti,
            List<TurnoBean> tuttiTurniEsistenti,
            ConfigurazioneCoperturaFlessibile coperturaInput,
            ConfigurazioneGenerazioneTurni configInput) {
        LocalDate lunedi = lunediSettimana.with(DayOfWeek.MONDAY);
        ConfigurazioneGenerazioneTurni config = configInput != null
                ? configInput : new ConfigurazioneGenerazioneTurni();
        ConfigurazioneCoperturaFlessibile copertura = coperturaInput != null
                ? coperturaInput : ConfigurazioneCoperturaFlessibile.predefinita();

        List<Dipendente> dipendenti = tuttiDipendenti != null ? tuttiDipendenti : List.of();
        List<TurnoBean> esistenti = tuttiTurniEsistenti != null ? tuttiTurniEsistenti : List.of();

        Map<String, Dipendente> dipPerEmail = new HashMap<>();
        List<Dipendente> eleggibili = new ArrayList<>();
        List<String> esclusiNonConfig = new ArrayList<>();
        for (Dipendente d : dipendenti) {
            dipPerEmail.put(d.getEmail().toLowerCase(Locale.ROOT), d);
            if (!d.isAttivo()) {
                continue;
            }
            if (!d.getConfigurazioneOre().isConfigurato()) {
                esclusiNonConfig.add(d.getNome() + " " + d.getCognome() + " (" + d.getEmail() + ")");
                continue;
            }
            eleggibili.add(d);
        }

        // Separazione turni della settimana corrente vs altre settimane
        List<TurnoBean> originaliSettimana = new ArrayList<>();
        List<TurnoBean> altreSettimane = new ArrayList<>();
        for (TurnoBean t : esistenti) {
            if (MotorePianificazioneTurni.appartieneASettimana(t, lunedi)) {
                originaliSettimana.add(copiaBean(t));
            } else {
                altreSettimane.add(t);
            }
        }

        // Turni mantenuti (bloccati sempre; tutti gli esistenti se SOLO_SCOPERTI)
        List<TurnoBean> proposti = new ArrayList<>();
        List<String> avvisi = new ArrayList<>();
        for (TurnoBean t : originaliSettimana) {
            boolean mantieni = t.isBloccato() || config.mantieneTurniNonBloccatiEsistenti();
            if (!mantieni) {
                continue;
            }
            if (t.isBloccato()) {
                Dipendente dip = t.getEmailDipendente() != null
                        ? dipPerEmail.get(t.getEmailDipendente().toLowerCase(Locale.ROOT)) : null;
                LocalDate dt = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), lunedi);
                if (dip != null && dt != null) {
                    String err = dip.getDisponibilita().verificaCompatibilita(dt, t.getOraInizio(), t.getOraFine());
                    if (err != null) {
                        avvisi.add("Conflitto su turno bloccato " + t.getCodiceTurno() + " ("
                                + dip.getNome() + " " + dip.getCognome() + "): " + err + ".");
                    }
                }
            }
            proposti.add(copiaBean(t));
        }

        Random rng = new Random(config.getSeedRigenerazione() * 1_000_003L + lunedi.toEpochDay() * 37L);
        if (config.getSeedRigenerazione() > 1L) {
            Collections.shuffle(eleggibili, rng);
        }

        int stepMin = config.getIncrementoMinutiFlessibile();
        int minDurMin = config.getDurataMinimaOreFlessibile() * 60;
        int maxDurMin = config.getDurataMassimaOreFlessibile() * 60;
        int contatoreCod = calcolaProssimoProgressivo(esistenti);

        // Costruzione flessibile giorno per giorno, dando priorita' alla chiusura e agli intervalli piu' scoperti
        List<Integer> ordineGiorni = new ArrayList<>(List.of(5, 6, 4, 0, 1, 2, 3)); // Weekend prima
        if (config.getSeedRigenerazione() > 1L) {
            Collections.shuffle(ordineGiorni, rng);
        }

        for (int g : ordineGiorni) {
            LocalDate dataGiorno = lunedi.plusDays(g);
            DayOfWeek dow = dataGiorno.getDayOfWeek();
            List<IntervalloCopertura> intervalli = copertura.getIntervalliGiorno(dow);
            if (intervalli.isEmpty()) {
                continue;
            }

            // Ordina gli intervalli mettendo la CHIUSURA per prima
            List<IntervalloCopertura> ordinati = new ArrayList<>(intervalli);
            ordinati.sort((a, b) -> Boolean.compare(b.isChiusura(), a.isChiusura()));

            int sicurezzaCicli = 0;
            while (sicurezzaCicli++ < 40) {
                IntervalloCopertura target = trovaIntervalloScopertoPiuCritico(dataGiorno, lunedi, ordinati, proposti, stepMin);
                if (target == null) {
                    break;
                }

                List<FinestraOraria> finestre = generaFinestreCandidatePerIntervallo(
                        target, ordinati, minDurMin, maxDurMin, stepMin);

                Dipendente migliorDip = null;
                FinestraOraria migliorFin = null;
                double migliorScore = Double.POSITIVE_INFINITY;

                for (Dipendente cand : eleggibili) {
                    for (FinestraOraria fin : finestre) {
                        if (!isAssegnazioneAmmissibile(cand, dataGiorno, fin, proposti, altreSettimane, lunedi, config)) {
                            continue;
                        }
                        double score = valutaCoppiaCandidata(
                                cand, dataGiorno, fin, ordinati, proposti, altreSettimane, lunedi, config, rng);
                        if (score < migliorScore) {
                            migliorScore = score;
                            migliorDip = cand;
                            migliorFin = fin;
                        }
                    }
                }

                if (migliorDip == null || migliorFin == null) {
                    // Nessun dipendente puo' coprire questo intervallo: esce dal giorno se non ci sono altri copribili
                    ordinati.remove(target);
                    if (ordinati.isEmpty()) {
                        break;
                    }
                    continue;
                }

                TurnoBean nuovo = new TurnoBean();
                nuovo.setCodiceTurno("FLX-" + String.format("%03d", contatoreCod++));
                try {
                    nuovo.setData(GIORNI_BREVI[g] + " " + dataGiorno.format(FMT_COMPLETO));
                    nuovo.setOraInizio(migliorFin.oraInizio());
                    nuovo.setOraFine(migliorFin.oraFine());
                    nuovo.setEmailDipendente(migliorDip.getEmail());
                } catch (Exception ignored) {
                    // Orari gia' normalizzati
                }
                nuovo.setNomeDipendente(migliorDip.getNome() + " " + migliorDip.getCognome());
                nuovo.setStato(StatoTurno.PIANIFICATO.name());
                nuovo.setBloccato(false);
                nuovo.setGeneratoAutomaticamente(true);
                nuovo.setPersonalizzato(DisponibilitaDipendente.identificaIndiceFascia(
                        migliorFin.oraInizio(), migliorFin.oraFine()) < 0);
                proposti.add(nuovo);
            }
        }

        return costruisciPropostaValutata(
                lunedi, config, copertura, dipendenti, originaliSettimana, proposti, altreSettimane,
                esclusiNonConfig, avvisi, MotorePianificazioneTurni.calcolaImprontaSettimana(originaliSettimana, lunedi));
    }

    /**
     * Rivaluta una proposta flessibile dopo modifiche manuali in anteprima (orari, dipendente, pause, blocchi).
     */
    public static PropostaTurniSettimanaBean rivalutaPropostaFlessibile(
            LocalDate lunediSettimana,
            ConfigurazioneGenerazioneTurni config,
            ConfigurazioneCoperturaFlessibile copertura,
            List<Dipendente> tuttiDipendenti,
            List<TurnoBean> originaliSettimana,
            List<TurnoBean> propostiModificati,
            List<TurnoBean> altreSettimane,
            String improntaOriginale) {
        LocalDate lunedi = lunediSettimana.with(DayOfWeek.MONDAY);
        List<String> esclusi = new ArrayList<>();
        for (Dipendente d : tuttiDipendenti) {
            if (d.isAttivo() && !d.getConfigurazioneOre().isConfigurato()) {
                esclusi.add(d.getNome() + " " + d.getCognome() + " (" + d.getEmail() + ")");
            }
        }
        return costruisciPropostaValutata(
                lunedi, config, copertura, tuttiDipendenti, originaliSettimana,
                propostiModificati != null ? propostiModificati : List.of(),
                altreSettimane != null ? altreSettimane : List.of(),
                esclusi, new ArrayList<>(), improntaOriginale);
    }

    private static PropostaTurniSettimanaBean costruisciPropostaValutata(
            LocalDate lunedi,
            ConfigurazioneGenerazioneTurni config,
            ConfigurazioneCoperturaFlessibile copertura,
            List<Dipendente> tuttiDipendenti,
            List<TurnoBean> originaliSettimana,
            List<TurnoBean> proposti,
            List<TurnoBean> altreSettimane,
            List<String> esclusiNonConfig,
            List<String> avvisiIniziali,
            String improntaOriginale) {
        PropostaTurniSettimanaBean proposta = new PropostaTurniSettimanaBean();
        proposta.setLunediSettimana(lunedi);
        proposta.setConfigurazioneUsata(config);
        proposta.setTurniOriginaliSettimana(originaliSettimana);
        proposta.setTurniProposti(proposti);
        proposta.setDipendentiEsclusiNonConfigurati(esclusiNonConfig);
        proposta.setImprontaVersioneOriginale(improntaOriginale);

        List<TurnoBean> vistaCompleta = new ArrayList<>(altreSettimane);
        vistaCompleta.addAll(proposti);

        List<RiepilogoOreDipendenteBean> riepiloghi = CalcolatoreOreSettimanali.calcolaRiepiloghiSettimana(
                tuttiDipendenti, vistaCompleta, lunedi);
        proposta.setRiepiloghiDipendenti(riepiloghi);

        RiepilogoCoperturaSettimana cov = CalcolatoreOreSettimanali.valutaCoperturaIntervalli(
                lunedi, vistaCompleta, copertura, config != null ? config.getIncrementoMinutiFlessibile() : 30);

        proposta.setPostiRichiestiTotali(cov.postiRichiestiTotali());
        proposta.setPostiCopertiTotali(cov.postiCopertiTotali());
        proposta.setFasceCompletamenteCoperte(cov.fasceCoperte());
        proposta.setFasceParzialmenteScoperte(cov.fasceParziali());
        proposta.setFasceSenzaPersonale(cov.fasceVuote());
        proposta.setCoperturaChiusuraOk(cov.chiusuraOk());
        proposta.setRiepilogoChiusura(cov.riepilogoChiusura());
        proposta.setTurniScoperti(cov.dettagliScoperture());

        // Diff rispetto agli originali
        Set<String> firmeOrig = new HashSet<>();
        for (TurnoBean t : originaliSettimana) firmeOrig.add(firma(t));
        Set<String> firmeProp = new HashSet<>();
        int mant = 0;
        int agg = 0;
        for (TurnoBean t : proposti) {
            String f = firma(t);
            firmeProp.add(f);
            if (firmeOrig.contains(f)) mant++;
            else agg++;
        }
        int rim = 0;
        for (String f : firmeOrig) {
            if (!firmeProp.contains(f)) rim++;
        }
        proposta.setTurniMantenuti(mant);
        proposta.setTurniAggiunti(agg);
        proposta.setTurniRimossi(rim);

        // Avvisi & violazioni inderogabili
        List<String> avvisi = new ArrayList<>(avvisiIniziali);
        List<String> suggerimenti = new ArrayList<>();
        boolean haViolazioni = false;

        for (RiepilogoOreDipendenteBean r : riepiloghi) {
            if (r.isOltreMaxTurni()) {
                haViolazioni = true;
                avvisi.add("Violazione limite turni: " + r.getNomeCompleto() + " ("
                        + r.getNumeroTurniSettimana() + "/" + r.getMaxTurniSettimanali() + " turni).");
            }
            if (r.isFuoriTolleranza()) {
                avvisi.add(r.getNomeCompleto() + ": "
                        + ConfigurazioneOreDipendente.formattaOre(r.getOreAssegnate()) + " / "
                        + r.getConfigurazioneOre().getEtichettaObiettivo()
                        + " (" + r.getMessaggioStato() + ").");
            }
        }
        if (!cov.chiusuraOk()) {
            avvisi.add("Copertura chiusura incompleta: " + cov.riepilogoChiusura() + ".");
            suggerimenti.add("Verifica le disponibilità serali o modifica manualmente un turno in anteprima per coprire la chiusura.");
        }
        if (!cov.dettagliScoperture().isEmpty()) {
            suggerimenti.add("Puoi approvare consapevolmente con scoperture oppure modificare gli orari dei turni in anteprima.");
        }
        if (!esclusiNonConfig.isEmpty()) {
            avvisi.add(esclusiNonConfig.size() + " dipendente/i esclusi per monte ore non configurato.");
        }

        proposta.setHaViolazioniInderogabili(haViolazioni);
        proposta.setAvvisi(avvisi);
        proposta.setSuggerimenti(suggerimenti);
        return proposta;
    }

    private static IntervalloCopertura trovaIntervalloScopertoPiuCritico(
            LocalDate giorno,
            LocalDate lunedi,
            List<IntervalloCopertura> intervalli,
            List<TurnoBean> proposti,
            int stepMin) {
        IntervalloCopertura migliore = null;
        int maxDeficit = 0;
        boolean miglioreChiusura = false;

        for (IntervalloCopertura ic : intervalli) {
            if (ic.personaleMinimo() <= 0) continue;
            LocalDateTime start = parseDateTime(giorno, ic.oraInizio(), false);
            LocalDateTime end = parseDateTime(giorno, ic.oraFine(), true);
            int minPres = Integer.MAX_VALUE;
            for (LocalDateTime t = start; t.isBefore(end); t = t.plusMinutes(stepMin)) {
                LocalDateTime tNext = t.plusMinutes(stepMin);
                if (tNext.isAfter(end)) tNext = end;
                int p = CalcolatoreOreSettimanali.contaPresentiNellIntervallo(proposti, lunedi, t, tNext);
                if (p < minPres) minPres = p;
            }
            int deficit = ic.personaleMinimo() - (minPres == Integer.MAX_VALUE ? 0 : minPres);
            if (deficit > 0) {
                if (migliore == null
                        || (ic.isChiusura() && !miglioreChiusura)
                        || (ic.isChiusura() == miglioreChiusura && deficit > maxDeficit)) {
                    migliore = ic;
                    maxDeficit = deficit;
                    miglioreChiusura = ic.isChiusura();
                }
            }
        }
        return migliore;
    }

    private static List<FinestraOraria> generaFinestreCandidatePerIntervallo(
            IntervalloCopertura target,
            List<IntervalloCopertura> tuttiIntervalliGiorno,
            int minDurMin,
            int maxDurMin,
            int stepMin) {
        int minGiorno = Integer.MAX_VALUE;
        int maxGiorno = 0;
        for (IntervalloCopertura ic : tuttiIntervalliGiorno) {
            int s = toMinuti(ic.oraInizio(), false);
            int e = toMinuti(ic.oraFine(), true);
            if (s < minGiorno) minGiorno = s;
            if (e > maxGiorno) maxGiorno = e;
        }
        int tStart = toMinuti(target.oraInizio(), false);
        int tEnd = toMinuti(target.oraFine(), true);

        List<FinestraOraria> finestre = new ArrayList<>();
        Set<String> viste = new HashSet<>();
        for (int s = minGiorno; s <= tStart; s += stepMin) {
            for (int e = tEnd; e <= maxGiorno; e += stepMin) {
                int dur = e - s;
                if (dur < minDurMin || dur > maxDurMin) {
                    continue;
                }
                String iniStr = formattaMinuti(s);
                String finStr = formattaMinuti(e);
                if (viste.add(iniStr + "-" + finStr)) {
                    finestre.add(new FinestraOraria(s, e, iniStr, finStr, ConfigurazioneOreDipendente.arrotonda(dur / 60.0)));
                }
            }
        }
        // Fallback se target e' piu' corto della durata minima: estendi fino a minDurMin
        if (finestre.isEmpty()) {
            int s = Math.max(minGiorno, tEnd - minDurMin);
            int e = Math.min(24 * 60, s + minDurMin);
            finestre.add(new FinestraOraria(s, e, formattaMinuti(s), formattaMinuti(e),
                    ConfigurazioneOreDipendente.arrotonda((e - s) / 60.0)));
        }
        return finestre;
    }

    private static boolean isAssegnazioneAmmissibile(Dipendente cand,
                                                     LocalDate giorno,
                                                     FinestraOraria fin,
                                                     List<TurnoBean> proposti,
                                                     List<TurnoBean> altreSettimane,
                                                     LocalDate lunedi,
                                                     ConfigurazioneGenerazioneTurni config) {
        if (!cand.isAttivo() || !cand.getConfigurazioneOre().isConfigurato()) {
            return false;
        }
        if (cand.getDisponibilita().verificaCompatibilita(giorno, fin.oraInizio(), fin.oraFine()) != null) {
            return false;
        }

        int turniSettimana = 0;
        int turniGiorno = 0;
        int turniLunghi = 0;

        for (TurnoBean t : proposti) {
            if (t.getEmailDipendente() == null || !t.getEmailDipendente().equalsIgnoreCase(cand.getEmail())) {
                continue;
            }
            turniSettimana++;
            LocalDate dT = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), lunedi);
            if (giorno.equals(dT)) {
                turniGiorno++;
            }
            if (CalcolatoreOreSettimanali.calcolaDurataOre(t.getOraInizio(), t.getOraFine(), t.getPausaMinuti()) >= 8.0 - EPS) {
                turniLunghi++;
            }
            if (dT != null && CalcolatoreOreSettimanali.isConflittoORiposoInsufficiente(
                    dT, t.getOraInizio(), t.getOraFine(),
                    giorno, fin.oraInizio(), fin.oraFine(),
                    config.getRiposoMinimoOre())) {
                return false;
            }
        }
        if (turniGiorno >= config.getMaxTurniGiorno()) {
            return false;
        }
        if (cand.hasLimiteMaxTurni() && turniSettimana >= cand.getMaxTurniSettimanali()) {
            return false;
        }
        Integer max8h = cand.getDisponibilita().getMaxTurniLunghiSettimana();
        if (max8h != null && fin.durataOre() >= 8.0 - EPS && turniLunghi >= max8h) {
            return false;
        }
        for (TurnoBean t : altreSettimane) {
            if (t.getEmailDipendente() == null || !t.getEmailDipendente().equalsIgnoreCase(cand.getEmail())) {
                continue;
            }
            LocalDate dAltro = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), lunedi);
            if (dAltro != null && Math.abs(dAltro.toEpochDay() - giorno.toEpochDay()) <= 2
                    && CalcolatoreOreSettimanali.isConflittoORiposoInsufficiente(
                    dAltro, t.getOraInizio(), t.getOraFine(),
                    giorno, fin.oraInizio(), fin.oraFine(),
                    config.getRiposoMinimoOre())) {
                return false;
            }
        }
        return true;
    }

    private static double valutaCoppiaCandidata(Dipendente cand,
                                                LocalDate giorno,
                                                FinestraOraria fin,
                                                List<IntervalloCopertura> intervalliGiorno,
                                                List<TurnoBean> proposti,
                                                List<TurnoBean> altreSettimane,
                                                LocalDate lunedi,
                                                ConfigurazioneGenerazioneTurni config,
                                                Random rng) {
        // 1. Quanti intervalli scoperti del giorno vengono coperti da questa finestra (priorita' chiusura)
        int guadagnoCopertura = 0;
        int step = config.getIncrementoMinutiFlessibile();
        for (IntervalloCopertura ic : intervalliGiorno) {
            int sIc = toMinuti(ic.oraInizio(), false);
            int eIc = toMinuti(ic.oraFine(), true);
            if (fin.inizioMin() <= sIc && fin.fineMin() >= eIc) {
                LocalDateTime st = parseDateTime(giorno, ic.oraInizio(), false);
                LocalDateTime en = parseDateTime(giorno, ic.oraFine(), true);
                int minPres = Integer.MAX_VALUE;
                for (LocalDateTime t = st; t.isBefore(en); t = t.plusMinutes(step)) {
                    int p = CalcolatoreOreSettimanali.contaPresentiNellIntervallo(proposti, lunedi, t, t.plusMinutes(step));
                    if (p < minPres) minPres = p;
                }
                if ((minPres == Integer.MAX_VALUE ? 0 : minPres) < ic.personaleMinimo()) {
                    guadagnoCopertura += ic.isChiusura() ? 3 : 1;
                }
            }
        }

        // 2. Monte ore & tolleranze (quadratico)
        List<TurnoBean> vista = new ArrayList<>(altreSettimane);
        vista.addAll(proposti);
        double orePrima = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(cand.getEmail(), vista, lunedi);
        double oreDopo = orePrima + fin.durataOre();
        ConfigurazioneOreDipendente cfg = cand.getConfigurazioneOre();

        double eccTollPrima = Math.max(0.0, orePrima - cfg.getMassimoConTolleranza());
        double eccTollDopo = Math.max(0.0, oreDopo - cfg.getMassimoConTolleranza());
        double deltaEccToll = (eccTollDopo * eccTollDopo) - (eccTollPrima * eccTollPrima);

        double eccMaxPrima = Math.max(0.0, orePrima - cfg.getOreMassime());
        double eccMaxDopo = Math.max(0.0, oreDopo - cfg.getOreMassime());
        double deltaEccMax = (eccMaxDopo * eccMaxDopo) - (eccMaxPrima * eccMaxPrima);

        double defPrima = Math.max(0.0, cfg.getOreMinime() - orePrima);
        double defDopo = Math.max(0.0, cfg.getOreMinime() - oreDopo);
        double guadagnoDef = (defPrima * defPrima) - (defDopo * defDopo);

        double rapporto = cfg.getOreMassime() > 0 ? oreDopo / cfg.getOreMassime() : oreDopo;
        double penPref = cand.getDisponibilita().rispettaPreferenza(giorno, fin.oraInizio(), fin.oraFine()) ? 0.0 : 1.0;
        double jitter = rng != null ? rng.nextDouble() * (config.getSeedRigenerazione() > 1L ? 450.0 : 0.1) : 0.0;

        return -guadagnoCopertura * 30_000.0
                + deltaEccToll * 100_000.0
                + deltaEccMax * 2_500.0
                - guadagnoDef * 90.0
                + rapporto * 350.0
                + penPref * 15.0
                + jitter;
    }

    private static LocalDateTime parseDateTime(LocalDate giorno, String hhmm, boolean isFine) {
        if (isFine && ("00:00".equals(hhmm) || "24:00".equals(hhmm))) {
            return giorno.plusDays(1).atStartOfDay();
        }
        String[] p = hhmm.split(":");
        return giorno.atTime(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }

    private static int toMinuti(String hhmm, boolean isFine) {
        if (isFine && ("00:00".equals(hhmm) || "24:00".equals(hhmm))) {
            return 24 * 60;
        }
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private static String formattaMinuti(int minuti) {
        if (minuti >= 24 * 60) {
            return "00:00";
        }
        return String.format("%02d:%02d", minuti / 60, minuti % 60);
    }

    private static TurnoBean copiaBean(TurnoBean src) {
        TurnoBean b = new TurnoBean();
        b.setCodiceTurno(src.getCodiceTurno());
        try {
            if (src.getData() != null) b.setData(src.getData());
            if (src.getOraInizio() != null) b.setOraInizio(src.getOraInizio());
            if (src.getOraFine() != null) b.setOraFine(src.getOraFine());
            if (src.getEmailDipendente() != null) b.setEmailDipendente(src.getEmailDipendente());
        } catch (Exception ignored) {
            // Copia difensiva
        }
        b.setNomeDipendente(src.getNomeDipendente());
        b.setStato(src.getStato());
        b.setBloccato(src.isBloccato());
        b.setGeneratoAutomaticamente(src.isGeneratoAutomaticamente());
        b.setPersonalizzato(src.isPersonalizzato());
        b.setPausaMinuti(src.getPausaMinuti());
        return b;
    }

    private static String firma(TurnoBean t) {
        return Objects.toString(t.getData(), "") + "|"
                + Objects.toString(t.getOraInizio(), "") + "|"
                + Objects.toString(t.getOraFine(), "") + "|"
                + Objects.toString(t.getEmailDipendente(), "").toLowerCase(Locale.ROOT);
    }

    private static int calcolaProssimoProgressivo(List<TurnoBean> esistenti) {
        int max = 0;
        for (TurnoBean t : esistenti) {
            if (t.getCodiceTurno() != null) {
                String digits = t.getCodiceTurno().replaceAll("\\D", "");
                if (!digits.isEmpty()) {
                    try {
                        int n = Integer.parseInt(digits);
                        if (n > max) max = n;
                    } catch (NumberFormatException ignored) {
                        // Ignora formati non numerici
                    }
                }
            }
        }
        return max + 1;
    }
}
