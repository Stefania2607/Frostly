package control.applicativo;

import bean.PropostaTurniSettimanaBean;
import bean.RiepilogoOreDipendenteBean;
import bean.TurnoBean;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import model.ConfigurazioneGenerazioneTurni;
import model.ConfigurazioneGenerazioneTurni.PrioritaOttimizzazione;
import model.ConfigurazioneOreDipendente;
import model.Dipendente;
import model.FabbisognoTurno;
import model.MansioneDipendente;
import model.StatoTurno;

/**
 * Motore di pianificazione vincolata per la generazione automatica e opzionale dei turni settimanali.
 *
 * Non salva mai direttamente i dati ufficiali: produce una PropostaTurniSettimanaBean
 * temporanea che l'amministratore puo' ispezionare, modificare, rigenerare o confermare.
 */
public final class MotorePianificazioneTurni {

    public static final String[][] FASCE_CATALOGO = {
            {"07:00", "14:00", "Mattina"},
            {"14:00", "20:00", "Pomeriggio"},
            {"16:00", "00:00", "Pom./Sera"},
            {"20:00", "00:00", "Serale"},
            {"10:00", "18:00", "Extra Weekend"}
    };

    private static final String[] GIORNI_BREVI = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
    private static final DateTimeFormatter FMT_COMPLETO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double EPS = 0.005;

    private MotorePianificazioneTurni() {
    }

    private record SlotSettimana(
            int indiceGiorno,
            int indiceFascia,
            LocalDate data,
            String chiaveData,
            String oraInizio,
            String oraFine,
            double durataOre,
            boolean isSera,
            boolean isWeekend,
            FabbisognoTurno fabbisogno) {
    }

    private static final class AssegnazioneInterna {
        final String codiceTurno;
        final SlotSettimana slot;
        Dipendente dipendente;
        final boolean fisso;
        final boolean bloccato;
        final boolean generatoAuto;
        final String stato;

        AssegnazioneInterna(String codiceTurno, SlotSettimana slot, Dipendente dipendente,
                            boolean fisso, boolean bloccato, boolean generatoAuto, String stato) {
            this.codiceTurno = codiceTurno;
            this.slot = slot;
            this.dipendente = dipendente;
            this.fisso = fisso;
            this.bloccato = bloccato;
            this.generatoAuto = generatoAuto;
            this.stato = stato;
        }
    }

    /**
     * Genera una proposta ottimizzata di turni per la settimana che inizia a lunediSettimana.
     */
    public static PropostaTurniSettimanaBean generaProposta(LocalDate lunediSettimana,
                                                            List<Dipendente> tuttiDipendenti,
                                                            List<TurnoBean> tuttiTurniEsistenti,
                                                            Map<String, FabbisognoTurno> fabbisogni,
                                                            ConfigurazioneGenerazioneTurni configInput) {
        LocalDate lunedi = lunediSettimana.with(DayOfWeek.MONDAY);
        ConfigurazioneGenerazioneTurni config = configInput != null
                ? configInput : new ConfigurazioneGenerazioneTurni();
        List<Dipendente> dipendenti = tuttiDipendenti != null ? tuttiDipendenti : List.of();
        List<TurnoBean> esistenti = tuttiTurniEsistenti != null ? tuttiTurniEsistenti : List.of();
        Map<String, FabbisognoTurno> mappaFab = normalizzaFabbisogni(fabbisogni);

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

        // 1. Costruzione dei 35 slot settimanali
        List<SlotSettimana> slots = costruisciSlotsSettimana(lunedi, mappaFab);

        // 2. Separazione turni della settimana corrente vs altre settimane
        List<TurnoBean> turniOriginaliSettimana = new ArrayList<>();
        List<TurnoBean> turniAltreSettimane = new ArrayList<>();
        for (TurnoBean t : esistenti) {
            if (appartieneASettimana(t, lunedi)) {
                turniOriginaliSettimana.add(copiaBean(t));
            } else {
                turniAltreSettimane.add(t);
            }
        }

        // 3. Inizializzazione assegnazioni fisse (bloccati + esistenti se SOLO_SCOPERTI)
        List<AssegnazioneInterna> assegnazioni = new ArrayList<>();
        List<String> avvisi = new ArrayList<>();
        int contatoreCodice = calcolaProssimoProgressivoCodice(esistenti);

        for (TurnoBean t : turniOriginaliSettimana) {
            boolean mantieni = t.isBloccato() || config.mantieneTurniNonBloccatiEsistenti();
            if (!mantieni) {
                continue;
            }
            Dipendente dip = t.getEmailDipendente() != null
                    ? dipPerEmail.get(t.getEmailDipendente().toLowerCase(Locale.ROOT)) : null;
            SlotSettimana slotMatch = trovaSlotPerTurno(slots, t, lunedi);
            if (dip != null && slotMatch != null) {
                if (t.isBloccato()) {
                    String errDisp = dip.getDisponibilita().verificaCompatibilita(
                            slotMatch.data(), slotMatch.oraInizio(), slotMatch.oraFine());
                    if (errDisp != null) {
                        avvisi.add("Conflitto su turno bloccato " + t.getCodiceTurno() + " ("
                                + dip.getNome() + " " + dip.getCognome() + ", " + slotMatch.chiaveData()
                                + "): " + errDisp + ". Il turno bloccato e' stato mantenuto senza modifiche.");
                    }
                    if (!dip.isAttivo()) {
                        avvisi.add("Conflitto su turno bloccato " + t.getCodiceTurno() + ": il dipendente "
                                + dip.getNome() + " " + dip.getCognome() + " risulta inattivo.");
                    }
                }
                assegnazioni.add(new AssegnazioneInterna(
                        t.getCodiceTurno(), slotMatch, dip, true, t.isBloccato(),
                        t.isGeneratoAutomaticamente(), t.getStato() != null ? t.getStato() : StatoTurno.PUBBLICATO.name()));
            }
        }

        // 4. Costruzione guidata dai vincoli con euristica MRV dinamica (Minimum Remaining Values):
        // ad ogni passo assegna lo slot scoperto con il minor numero di candidati ancora ammissibili.
        Random rng = new Random(config.getSeedRigenerazione() * 1_000_003L + lunedi.toEpochDay() * 97L);
        if (config.getSeedRigenerazione() > 1L) {
            java.util.Collections.shuffle(eleggibili, rng);
        }
        List<SlotSettimana> slotsAttivi = new ArrayList<>();
        for (SlotSettimana s : slots) {
            if (s.fabbisogno().getTotaleRichiesto() > 0) {
                slotsAttivi.add(s);
            }
        }
        if (config.getSeedRigenerazione() > 1L) {
            java.util.Collections.shuffle(slotsAttivi, rng);
        }

        MansioneDipendente[] ordineMansioni = {
                MansioneDipendente.RESPONSABILE,
                MansioneDipendente.LABORATORIO,
                MansioneDipendente.BANCO
        };

        while (true) {
            SlotSettimana slotScelto = null;
            MansioneDipendente mansioneScelta = null;
            int minCandidati = Integer.MAX_VALUE;

            for (SlotSettimana slot : slotsAttivi) {
                FabbisognoTurno fab = slot.fabbisogno();
                if (contaAssegnatiInSlot(assegnazioni, slot) >= fab.getTotaleRichiesto()) {
                    continue;
                }
                MansioneDipendente mansioneMancante = null;
                for (MansioneDipendente m : ordineMansioni) {
                    if (contaMansioneInSlot(assegnazioni, slot, m) < fab.getMinimoMansione(m)) {
                        mansioneMancante = m;
                        break;
                    }
                }
                int numCand = contaCandidatiAmmissibili(
                        slot, mansioneMancante, eleggibili, assegnazioni, turniAltreSettimane, lunedi, config);
                if (numCand == 0) {
                    continue;
                }
                // Preferisce prima slot con mansioni obbligatorie mancanti, poi minor numero di candidati (MRV)
                int prioritaSlot = numCand + (mansioneMancante == null ? 100 : 0);
                if (prioritaSlot < minCandidati) {
                    minCandidati = prioritaSlot;
                    slotScelto = slot;
                    mansioneScelta = mansioneMancante;
                }
            }

            if (slotScelto == null) {
                break;
            }
            Dipendente scelto = scegliMigliorCandidato(
                    slotScelto, mansioneScelta, eleggibili, assegnazioni, turniAltreSettimane, lunedi, config, rng);
            if (scelto == null) {
                break;
            }
            String cod = "TUR-" + String.format("%03d", contatoreCodice++);
            assegnazioni.add(new AssegnazioneInterna(
                    cod, slotScelto, scelto, false, false, true, StatoTurno.PIANIFICATO.name()));
        }

        // 5. Miglioramento locale (riassegnazioni sicure sui soli turni generati)
        eseguiMiglioramentoLocale(assegnazioni, eleggibili, turniAltreSettimane, lunedi, config, rng);

        // 6. Conversione in TurnoBean e valutazione centralizzata tramite CalcolatoreOreSettimanali
        List<TurnoBean> turniProposti = new ArrayList<>();
        for (AssegnazioneInterna a : assegnazioni) {
            turniProposti.add(convertiInBean(a));
        }

        List<TurnoBean> vistaCompletaSimulata = new ArrayList<>(turniAltreSettimane);
        vistaCompletaSimulata.addAll(turniProposti);

        return costruisciRisultatoProposta(
                lunedi, config, dipendenti, turniOriginaliSettimana, turniProposti,
                vistaCompletaSimulata, slots, esclusiNonConfig, avvisi);
    }

    /**
     * Ricalcola tutti gli indicatori di una proposta dopo una modifica manuale in anteprima,
     * riutilizzando sempre CalcolatoreOreSettimanali come unica fonte di verita'.
     */
    public static PropostaTurniSettimanaBean rivalutaProposta(LocalDate lunediSettimana,
                                                             ConfigurazioneGenerazioneTurni config,
                                                             List<Dipendente> tuttiDipendenti,
                                                             List<TurnoBean> turniOriginaliSettimana,
                                                             List<TurnoBean> turniPropostiModificati,
                                                             List<TurnoBean> tuttiTurniAltreSettimane,
                                                             Map<String, FabbisognoTurno> fabbisogni,
                                                             String improntaOriginale) {
        LocalDate lunedi = lunediSettimana.with(DayOfWeek.MONDAY);
        List<SlotSettimana> slots = costruisciSlotsSettimana(lunedi, normalizzaFabbisogni(fabbisogni));
        List<String> esclusi = new ArrayList<>();
        for (Dipendente d : tuttiDipendenti) {
            if (d.isAttivo() && !d.getConfigurazioneOre().isConfigurato()) {
                esclusi.add(d.getNome() + " " + d.getCognome() + " (" + d.getEmail() + ")");
            }
        }
        List<TurnoBean> vistaCompleta = new ArrayList<>();
        if (tuttiTurniAltreSettimane != null) {
            vistaCompleta.addAll(tuttiTurniAltreSettimane);
        }
        if (turniPropostiModificati != null) {
            vistaCompleta.addAll(turniPropostiModificati);
        }
        PropostaTurniSettimanaBean res = costruisciRisultatoProposta(
                lunedi, config, tuttiDipendenti, turniOriginaliSettimana, turniPropostiModificati,
                vistaCompleta, slots, esclusi, new ArrayList<>());
        res.setImprontaVersioneOriginale(improntaOriginale);
        return res;
    }

    /**
     * Calcola una firma deterministica dei turni ufficiali di una settimana per rilevare
     * modifiche concorrenti prima di applicare una proposta generata.
     */
    public static String calcolaImprontaSettimana(List<TurnoBean> tuttiTurni, LocalDate lunediSettimana) {
        LocalDate lunedi = lunediSettimana.with(DayOfWeek.MONDAY);
        List<String> righe = new ArrayList<>();
        if (tuttiTurni != null) {
            for (TurnoBean t : tuttiTurni) {
                if (appartieneASettimana(t, lunedi)) {
                    righe.add(Objects.toString(t.getCodiceTurno(), "") + "|"
                            + Objects.toString(t.getData(), "") + "|"
                            + Objects.toString(t.getOraInizio(), "") + "|"
                            + Objects.toString(t.getOraFine(), "") + "|"
                            + Objects.toString(t.getEmailDipendente(), "") + "|"
                            + t.isBloccato());
                }
            }
        }
        righe.sort(String::compareTo);
        return Integer.toHexString(String.join("\n", righe).hashCode());
    }

    public static boolean appartieneASettimana(TurnoBean t, LocalDate lunediSettimana) {
        if (t == null || lunediSettimana == null) {
            return false;
        }
        LocalDate dataTurno = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), lunediSettimana);
        if (dataTurno == null) {
            return false;
        }
        LocalDate domenica = lunediSettimana.plusDays(6);
        return !dataTurno.isBefore(lunediSettimana) && !dataTurno.isAfter(domenica);
    }

    private static int contaCandidatiAmmissibili(SlotSettimana slot,
                                                 MansioneDipendente mansioneRichiesta,
                                                 List<Dipendente> eleggibili,
                                                 List<AssegnazioneInterna> assegnazioni,
                                                 List<TurnoBean> turniAltreSettimane,
                                                 LocalDate lunedi,
                                                 ConfigurazioneGenerazioneTurni config) {
        int c = 0;
        for (Dipendente cand : eleggibili) {
            if (isCandidatoAmmissibile(cand, slot, mansioneRichiesta, assegnazioni, turniAltreSettimane, lunedi, config)) {
                c++;
            }
        }
        return c;
    }

    private static Dipendente scegliMigliorCandidato(SlotSettimana slot,
                                                     MansioneDipendente mansioneRichiesta,
                                                     List<Dipendente> eleggibili,
                                                     List<AssegnazioneInterna> assegnazioni,
                                                     List<TurnoBean> turniAltreSettimane,
                                                     LocalDate lunedi,
                                                     ConfigurazioneGenerazioneTurni config,
                                                     Random rng) {
        Dipendente migliore = null;
        double migliorScore = Double.POSITIVE_INFINITY;

        for (Dipendente cand : eleggibili) {
            if (!isCandidatoAmmissibile(cand, slot, mansioneRichiesta, assegnazioni, turniAltreSettimane, lunedi, config)) {
                continue;
            }
            double score = calcolaCostoCandidato(cand, slot, eleggibili, assegnazioni, turniAltreSettimane, lunedi, config, rng);
            if (score < migliorScore) {
                migliorScore = score;
                migliore = cand;
            }
        }
        return migliore;
    }

    private static boolean isCandidatoAmmissibile(Dipendente cand,
                                                  SlotSettimana slot,
                                                  MansioneDipendente mansioneRichiesta,
                                                  List<AssegnazioneInterna> assegnazioni,
                                                  List<TurnoBean> turniAltreSettimane,
                                                  LocalDate lunedi,
                                                  ConfigurazioneGenerazioneTurni config) {
        if (!cand.isAttivo() || !cand.getConfigurazioneOre().isConfigurato()) {
            return false;
        }
        if (mansioneRichiesta != null && !cand.haMansione(mansioneRichiesta)) {
            return false;
        }
        // Vincolo rigido 1: disponibilita' ordinaria, date bloccate, ferie/permessi
        if (cand.getDisponibilita().verificaCompatibilita(slot.data(), slot.oraInizio(), slot.oraFine()) != null) {
            return false;
        }
        // Vincolo rigido 1b: limite massimo opzionale di turni settimanali
        if (cand.hasLimiteMaxTurni()) {
            int turniGiaAssegnati = 0;
            for (AssegnazioneInterna a : assegnazioni) {
                if (a.dipendente.getEmail().equalsIgnoreCase(cand.getEmail())) {
                    turniGiaAssegnati++;
                }
            }
            if (turniGiaAssegnati >= cand.getMaxTurniSettimanali()) {
                return false;
            }
        }
        // Vincolo rigido 1c: limite massimo opzionale di turni lunghi (>= 8 h) a settimana
        Integer maxLunghi = cand.getDisponibilita().getMaxTurniLunghiSettimana();
        if (maxLunghi != null && slot.durataOre() >= 8.0 - EPS) {
            int lunghiGiaAssegnati = 0;
            for (AssegnazioneInterna a : assegnazioni) {
                if (a.dipendente.getEmail().equalsIgnoreCase(cand.getEmail())
                        && a.slot.durataOre() >= 8.0 - EPS) {
                    lunghiGiaAssegnati++;
                }
            }
            if (lunghiGiaAssegnati >= maxLunghi) {
                return false;
            }
        }
        // Vincolo rigido 2: nessun duplicato nello stesso slot, nessuna sovrapposizione, riposo minimo
        for (AssegnazioneInterna a : assegnazioni) {
            if (!a.dipendente.getEmail().equalsIgnoreCase(cand.getEmail())) {
                continue;
            }
            if (a.slot.indiceGiorno() == slot.indiceGiorno() && a.slot.indiceFascia() == slot.indiceFascia()) {
                return false;
            }
            if (CalcolatoreOreSettimanali.isConflittoORiposoInsufficiente(
                    a.slot.data(), a.slot.oraInizio(), a.slot.oraFine(),
                    slot.data(), slot.oraInizio(), slot.oraFine(),
                    config.getRiposoMinimoOre())) {
                return false;
            }
        }
        // Controllo sovrapposizione/riposo anche con turni di settimane adiacenti
        for (TurnoBean t : turniAltreSettimane) {
            if (t.getEmailDipendente() == null || !t.getEmailDipendente().equalsIgnoreCase(cand.getEmail())) {
                continue;
            }
            LocalDate dataAltro = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), lunedi);
            if (dataAltro != null && Math.abs(dataAltro.toEpochDay() - slot.data().toEpochDay()) <= 2) {
                if (CalcolatoreOreSettimanali.isConflittoORiposoInsufficiente(
                        dataAltro, t.getOraInizio(), t.getOraFine(),
                        slot.data(), slot.oraInizio(), slot.oraFine(),
                        config.getRiposoMinimoOre())) {
                    return false;
                }
            }
        }
        // In modalita' MONTE_ORE_PRIORITARIO evitiamo di sforare oltre la tolleranza superiore
        if (config.getPriorita() == PrioritaOttimizzazione.MONTE_ORE_PRIORITARIO) {
            double oreAttuali = calcolaOreAssegnate(cand.getEmail(), assegnazioni, turniAltreSettimane, lunedi);
            double oreDopo = oreAttuali + slot.durataOre();
            if (oreDopo > cand.getConfigurazioneOre().getMassimoConTolleranza() + EPS) {
                return false;
            }
        }
        return true;
    }

    /**
     * Funzione di costo gerarchica (lessicografica tramite pesi di ordine di grandezza separati):
     * - Livello 1 (1e6): superamento oltre la tolleranza massima
     * - Livello 2 (1e4): scostamento rispetto al range contrattuale [min, max]
     * - Livello 3 (1e2): equilibrio relativo tra dipendenti (% di riempimento monte ore)
     * - Livello 4 (1e1): distribuzione equa di turni serali e weekend
     * - Livello 5 (1e0): preferenze non vincolanti
     */
    private static double calcolaCostoCandidato(Dipendente cand,
                                                SlotSettimana slot,
                                                List<Dipendente> eleggibili,
                                                List<AssegnazioneInterna> assegnazioni,
                                                List<TurnoBean> turniAltreSettimane,
                                                LocalDate lunedi,
                                                ConfigurazioneGenerazioneTurni config,
                                                Random rng) {
        ConfigurazioneOreDipendente cfg = cand.getConfigurazioneOre();
        double orePrima = calcolaOreAssegnate(cand.getEmail(), assegnazioni, turniAltreSettimane, lunedi);
        double oreDopo = orePrima + slot.durataOre();

        // Penalita' QUADRATICA sugli eccessi: distribuisce equamente gli sforamenti (+2,+2 invece di +4,0)
        double eccTollPrima = Math.max(0.0, orePrima - cfg.getMassimoConTolleranza());
        double eccTollDopo = Math.max(0.0, oreDopo - cfg.getMassimoConTolleranza());
        double deltaEccTollQuad = (eccTollDopo * eccTollDopo) - (eccTollPrima * eccTollPrima);

        double eccMaxPrima = Math.max(0.0, orePrima - cfg.getOreMassime());
        double eccMaxDopo = Math.max(0.0, oreDopo - cfg.getOreMassime());
        double deltaEccMaxQuad = (eccMaxDopo * eccMaxDopo) - (eccMaxPrima * eccMaxPrima);

        // Premio QUADRATICO nel colmare chi e' piu' indietro rispetto al proprio minimo
        double defPrima = Math.max(0.0, cfg.getOreMinime() - orePrima);
        double defDopo = Math.max(0.0, cfg.getOreMinime() - oreDopo);
        double guadagnoDefQuad = (defPrima * defPrima) - (defDopo * defDopo);

        double rapportoCarico = cfg.getOreMassime() > 0 ? (oreDopo / cfg.getOreMassime()) : oreDopo;
        int sereAttuali = contaSereDipendente(cand.getEmail(), assegnazioni);
        int weekendAttuali = contaWeekendDipendente(cand.getEmail(), assegnazioni);
        double penalitaSeraWeekend = (slot.isSera() ? sereAttuali : 0) + (slot.isWeekend() ? weekendAttuali : 0);
        double penalitaPreferenza = cand.getDisponibilita().rispettaPreferenza(
                slot.data(), slot.oraInizio(), slot.oraFine()) ? 0.0 : 1.0;

        // Su Rigenera (seed > 1) ruota in modo evidente i turni tra candidati ugualmente equilibrati
        // senza mai compromettere copertura, tolleranze (1e5 * quad) o sforamenti massimi (2e3 * quad)
        double ampiezzaJitter = config.getSeedRigenerazione() > 1L ? 1200.0 : 0.05;
        double jitter = rng != null ? rng.nextDouble() * ampiezzaJitter : 0.0;

        if (config.getPriorita() == PrioritaOttimizzazione.PIANIFICAZIONE_EQUILIBRATA) {
            return deltaEccTollQuad * 100_000.0
                    + deltaEccMaxQuad * 4_000.0
                    + rapportoCarico * 3_500.0
                    - guadagnoDefQuad * 60.0
                    + penalitaSeraWeekend * 250.0
                    + penalitaPreferenza * 20.0
                    + jitter;
        }
        // Default COPERTURA_PRIORITARIA e MONTE_ORE_PRIORITARIO
        return deltaEccTollQuad * 100_000.0
                + deltaEccMaxQuad * 2_000.0
                - guadagnoDefQuad * 80.0
                + rapportoCarico * 400.0
                + penalitaSeraWeekend * 25.0
                + penalitaPreferenza * 5.0
                + jitter;
    }

    /**
     * Miglioramento locale: prova a riassegnare ciascun turno generato (mai quelli fissi/bloccati)
     * ad un altro dipendente ammissibile se riduce strettamente il costo globale della settimana.
     */
    private static void eseguiMiglioramentoLocale(List<AssegnazioneInterna> assegnazioni,
                                                  List<Dipendente> eleggibili,
                                                  List<TurnoBean> turniAltreSettimane,
                                                  LocalDate lunedi,
                                                  ConfigurazioneGenerazioneTurni config,
                                                  Random rng) {
        if (eleggibili.size() < 2) {
            return;
        }
        List<Integer> indici = new ArrayList<>();
        for (int i = 0; i < assegnazioni.size(); i++) {
            indici.add(i);
        }
        for (int iter = 0; iter < 4; iter++) {
            boolean migliorato = false;
            if (config.getSeedRigenerazione() > 1L && rng != null) {
                java.util.Collections.shuffle(indici, rng);
            }
            for (int i : indici) {
                AssegnazioneInterna corrente = assegnazioni.get(i);
                if (corrente.fisso || corrente.bloccato) {
                    continue;
                }
                Dipendente vecchio = corrente.dipendente;
                double costoAttuale = calcolaCostoGlobale(assegnazioni, eleggibili, turniAltreSettimane, lunedi, config);

                // Rimuoviamo temporaneamente corrente per valutare sostituti ammissibili
                List<AssegnazioneInterna> senzaCorrente = new ArrayList<>(assegnazioni);
                senzaCorrente.remove(i);

                Dipendente migliorSostituto = vecchio;
                double migliorCosto = costoAttuale;

                for (Dipendente cand : eleggibili) {
                    if (cand.getEmail().equalsIgnoreCase(vecchio.getEmail())) {
                        continue;
                    }
                    if (!isCandidatoAmmissibile(cand, corrente.slot, null, senzaCorrente, turniAltreSettimane, lunedi, config)) {
                        continue;
                    }
                    // Verifica che la sostituzione non faccia perdere una mansione obbligatoria nello slot
                    corrente.dipendente = cand;
                    if (!slotRispettaMansioniComePrima(assegnazioni, corrente.slot, vecchio, cand)) {
                        corrente.dipendente = vecchio;
                        continue;
                    }
                    double nuovoCosto = calcolaCostoGlobale(assegnazioni, eleggibili, turniAltreSettimane, lunedi, config);
                    if (nuovoCosto + 0.1 < migliorCosto) {
                        migliorCosto = nuovoCosto;
                        migliorSostituto = cand;
                    }
                    corrente.dipendente = vecchio;
                }

                if (!migliorSostituto.getEmail().equalsIgnoreCase(vecchio.getEmail())) {
                    corrente.dipendente = migliorSostituto;
                    migliorato = true;
                }
            }
            // Scambi a coppie (2-swap) tra due turni generati di durata diversa per riequilibrare le ore
            for (int a = 0; a < assegnazioni.size(); a++) {
                AssegnazioneInterna tA = assegnazioni.get(a);
                if (tA.fisso || tA.bloccato) continue;
                for (int b = a + 1; b < assegnazioni.size(); b++) {
                    AssegnazioneInterna tB = assegnazioni.get(b);
                    if (tB.fisso || tB.bloccato) continue;
                    if (tA.dipendente.getEmail().equalsIgnoreCase(tB.dipendente.getEmail())) continue;
                    if (Math.abs(tA.slot.durataOre() - tB.slot.durataOre()) < 0.25) continue;

                    double costoPrima = calcolaCostoGlobale(assegnazioni, eleggibili, turniAltreSettimane, lunedi, config);
                    Dipendente dipA = tA.dipendente;
                    Dipendente dipB = tB.dipendente;

                    List<AssegnazioneInterna> senzaEntrambi = new ArrayList<>(assegnazioni);
                    senzaEntrambi.remove(tA);
                    senzaEntrambi.remove(tB);

                    if (!isCandidatoAmmissibile(dipB, tA.slot, null, senzaEntrambi, turniAltreSettimane, lunedi, config)
                            || !isCandidatoAmmissibile(dipA, tB.slot, null, senzaEntrambi, turniAltreSettimane, lunedi, config)) {
                        continue;
                    }
                    tA.dipendente = dipB;
                    tB.dipendente = dipA;
                    boolean okMansioni = slotRispettaMansioniComePrima(assegnazioni, tA.slot, dipA, dipB)
                            && slotRispettaMansioniComePrima(assegnazioni, tB.slot, dipB, dipA);
                    double costoDopo = okMansioni
                            ? calcolaCostoGlobale(assegnazioni, eleggibili, turniAltreSettimane, lunedi, config)
                            : Double.POSITIVE_INFINITY;
                    if (costoDopo + 0.5 < costoPrima) {
                        migliorato = true;
                    } else {
                        tA.dipendente = dipA;
                        tB.dipendente = dipB;
                    }
                }
            }
            if (!migliorato) {
                break;
            }
        }
    }

    private static boolean slotRispettaMansioniComePrima(List<AssegnazioneInterna> assegnazioni,
                                                         SlotSettimana slot,
                                                         Dipendente vecchio,
                                                         Dipendente nuovo) {
        FabbisognoTurno fab = slot.fabbisogno();
        for (MansioneDipendente m : MansioneDipendente.values()) {
            int min = fab.getMinimoMansione(m);
            if (min > 0 && vecchio.haMansione(m) && !nuovo.haMansione(m)) {
                if (contaMansioneInSlot(assegnazioni, slot, m) < min) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double calcolaCostoGlobale(List<AssegnazioneInterna> assegnazioni,
                                              List<Dipendente> eleggibili,
                                              List<TurnoBean> turniAltreSettimane,
                                              LocalDate lunedi,
                                              ConfigurazioneGenerazioneTurni config) {
        double fuoriTolleranza = 0.0;
        double scostamentoRange = 0.0;
        double sommaRapporti = 0.0;
        List<Double> rapporti = new ArrayList<>();
        int penalitaPref = 0;

        for (Dipendente d : eleggibili) {
            double ore = calcolaOreAssegnate(d.getEmail(), assegnazioni, turniAltreSettimane, lunedi);
            ConfigurazioneOreDipendente cfg = d.getConfigurazioneOre();
            if (ore > cfg.getMassimoConTolleranza() + EPS) {
                double e = ore - cfg.getMassimoConTolleranza();
                fuoriTolleranza += e * e;
            } else if (ore + EPS < cfg.getMinimoConTolleranza()) {
                double def = cfg.getMinimoConTolleranza() - ore;
                fuoriTolleranza += def * def * 0.5;
            }
            if (ore > cfg.getOreMassime() + EPS) {
                double e = ore - cfg.getOreMassime();
                scostamentoRange += e * e;
            } else if (ore + EPS < cfg.getOreMinime()) {
                double def = cfg.getOreMinime() - ore;
                scostamentoRange += def * def;
            }
            double rap = cfg.getOreMassime() > 0 ? ore / cfg.getOreMassime() : 0.0;
            rapporti.add(rap);
            sommaRapporti += rap;
        }

        double media = rapporti.isEmpty() ? 0.0 : sommaRapporti / rapporti.size();
        double varianza = 0.0;
        for (double r : rapporti) {
            varianza += (r - media) * (r - media);
        }

        for (AssegnazioneInterna a : assegnazioni) {
            if (!a.dipendente.getDisponibilita().rispettaPreferenza(
                    a.slot.data(), a.slot.oraInizio(), a.slot.oraFine())) {
                penalitaPref++;
            }
        }

        double pesoEquilibrio = config.getPriorita() == PrioritaOttimizzazione.PIANIFICAZIONE_EQUILIBRATA ? 5000.0 : 500.0;
        return fuoriTolleranza * 1_000_000.0
                + scostamentoRange * 10_000.0
                + varianza * pesoEquilibrio
                + penalitaPref * 10.0;
    }

    private static PropostaTurniSettimanaBean costruisciRisultatoProposta(LocalDate lunedi,
                                                                         ConfigurazioneGenerazioneTurni config,
                                                                         List<Dipendente> tuttiDipendenti,
                                                                         List<TurnoBean> turniOriginaliSettimana,
                                                                         List<TurnoBean> turniProposti,
                                                                         List<TurnoBean> vistaCompletaSimulata,
                                                                         List<SlotSettimana> slots,
                                                                         List<String> esclusiNonConfig,
                                                                         List<String> avvisiIniziali) {
        PropostaTurniSettimanaBean proposta = new PropostaTurniSettimanaBean();
        proposta.setLunediSettimana(lunedi);
        proposta.setConfigurazioneUsata(config);
        proposta.setTurniOriginaliSettimana(turniOriginaliSettimana);
        proposta.setTurniProposti(turniProposti);
        proposta.setDipendentiEsclusiNonConfigurati(esclusiNonConfig);
        proposta.setImprontaVersioneOriginale(calcolaImprontaSettimana(turniOriginaliSettimana, lunedi));

        List<RiepilogoOreDipendenteBean> riepiloghi = CalcolatoreOreSettimanali.calcolaRiepiloghiSettimana(
                tuttiDipendenti, vistaCompletaSimulata, lunedi);
        proposta.setRiepiloghiDipendenti(riepiloghi);

        Map<String, Dipendente> dipPerEmail = new HashMap<>();
        for (Dipendente d : tuttiDipendenti) {
            dipPerEmail.put(d.getEmail().toLowerCase(Locale.ROOT), d);
        }

        int richiestiTot = 0;
        int copertiTot = 0;
        List<String> turniScoperti = new ArrayList<>();
        List<String> avvisi = new ArrayList<>(avvisiIniziali);
        List<String> suggerimenti = new ArrayList<>();

        for (SlotSettimana slot : slots) {
            FabbisognoTurno fab = slot.fabbisogno();
            if (fab.getTotaleRichiesto() <= 0) {
                continue;
            }
            richiestiTot += fab.getTotaleRichiesto();
            List<TurnoBean> nelloSlot = new ArrayList<>();
            for (TurnoBean t : turniProposti) {
                if (corrispondeASlot(t, slot, lunedi)) {
                    nelloSlot.add(t);
                }
            }
            int coperti = Math.min(fab.getTotaleRichiesto(), nelloSlot.size());
            copertiTot += coperti;
            if (coperti < fab.getTotaleRichiesto()) {
                int mancanti = fab.getTotaleRichiesto() - coperti;
                turniScoperti.add(slot.chiaveData() + " " + slot.oraInizio() + "–" + slot.oraFine()
                        + ": coperti " + coperti + "/" + fab.getTotaleRichiesto()
                        + " (mancano " + mancanti + ")");
            }
            // Controllo mansioni richieste
            for (MansioneDipendente m : MansioneDipendente.values()) {
                int minM = fab.getMinimoMansione(m);
                if (minM > 0) {
                    int presenti = 0;
                    for (TurnoBean t : nelloSlot) {
                        Dipendente d = t.getEmailDipendente() != null
                                ? dipPerEmail.get(t.getEmailDipendente().toLowerCase(Locale.ROOT)) : null;
                        if (d != null && d.haMansione(m)) {
                            presenti++;
                        }
                    }
                    if (presenti < minM) {
                        avvisi.add("Mansione scoperta in " + slot.chiaveData() + " " + slot.oraInizio()
                                + "–" + slot.oraFine() + ": " + m.getEtichetta()
                                + " (" + presenti + "/" + minM + ").");
                    }
                }
            }
        }

        proposta.setPostiRichiestiTotali(richiestiTot);
        proposta.setPostiCopertiTotali(copertiTot);
        proposta.setTurniScoperti(turniScoperti);

        // Diff rispetto ai turni originali della settimana
        Set<String> firmeOriginali = new HashSet<>();
        for (TurnoBean t : turniOriginaliSettimana) {
            firmeOriginali.add(firmaTurno(t));
        }
        Set<String> firmeProposte = new HashSet<>();
        int mantenuti = 0;
        int aggiunti = 0;
        for (TurnoBean t : turniProposti) {
            String f = firmaTurno(t);
            firmeProposte.add(f);
            if (firmeOriginali.contains(f)) {
                mantenuti++;
            } else {
                aggiunti++;
            }
        }
        int rimossi = 0;
        for (String fo : firmeOriginali) {
            if (!firmeProposte.contains(fo)) {
                rimossi++;
            }
        }
        proposta.setTurniMantenuti(mantenuti);
        proposta.setTurniAggiunti(aggiunti);
        proposta.setTurniRimossi(rimossi);

        // Avvisi sugli scostamenti orari, conflitti max turni e suggerimenti operativi
        for (RiepilogoOreDipendenteBean r : riepiloghi) {
            if (r.isOltreMaxTurni()) {
                avvisi.add("Conflitto limite turni per " + r.getNomeCompleto() + ": "
                        + r.getNumeroTurniSettimana() + "/" + r.getMaxTurniSettimanali()
                        + " turni nella settimana (da assegnazioni preesistenti/bloccate).");
            }
            if (r.getStato() == RiepilogoOreDipendenteBean.StatoMonteOre.OLTRE_MASSIMO) {
                avvisi.add(r.getNomeCompleto() + ": "
                        + ConfigurazioneOreDipendente.formattaOre(r.getOreAssegnate()) + " / "
                        + r.getConfigurazioneOre().getEtichettaObiettivo()
                        + " (" + r.getMessaggioStato() + ").");
            } else if (r.getStato() == RiepilogoOreDipendenteBean.StatoMonteOre.SOTTO_MINIMO
                    && r.getOreAssegnate() > 0) {
                avvisi.add(r.getNomeCompleto() + ": "
                        + ConfigurazioneOreDipendente.formattaOre(r.getOreAssegnate()) + " / "
                        + r.getConfigurazioneOre().getEtichettaObiettivo()
                        + " (" + r.getMessaggioStato() + ").");
            }
        }

        if (!esclusiNonConfig.isEmpty()) {
            avvisi.add(esclusiNonConfig.size() + " dipendente/i esclusi per monte ore non configurato.");
            suggerimenti.add("Configura il monte ore per: " + String.join(", ", esclusiNonConfig) + ".");
        }
        if (!turniScoperti.isEmpty()) {
            suggerimenti.add("Aggiungi personale disponibile nelle fasce scoperte oppure verifica i fabbisogni di copertura.");
            suggerimenti.add("Valuta con lo staff eventuali disponibilita' aggiuntive (senza violare i riposi obbligatori).");
        }
        boolean haEccessi = riepiloghi.stream().anyMatch(RiepilogoOreDipendenteBean::isFuoriTolleranza);
        if (haEccessi) {
            suggerimenti.add("Redistribuisci alcuni turni dai dipendenti fuori tolleranza a colleghi sotto il minimo settimanale.");
        }

        proposta.setAvvisi(avvisi);
        proposta.setSuggerimenti(suggerimenti);
        return proposta;
    }

    private static List<SlotSettimana> costruisciSlotsSettimana(LocalDate lunedi, Map<String, FabbisognoTurno> fabbisogni) {
        List<SlotSettimana> slots = new ArrayList<>();
        for (int g = 0; g < 7; g++) {
            LocalDate data = lunedi.plusDays(g);
            DayOfWeek dow = data.getDayOfWeek();
            boolean weekend = (g >= 5);
            String chiaveData = GIORNI_BREVI[g] + " " + data.format(FMT_COMPLETO);
            for (int f = 0; f < FASCE_CATALOGO.length; f++) {
                String ini = FASCE_CATALOGO[f][0];
                String fin = FASCE_CATALOGO[f][1];
                double durata = CalcolatoreOreSettimanali.calcolaDurataOre(ini, fin);
                boolean sera = (f == 2 || f == 3);
                FabbisognoTurno fab = fabbisogni.getOrDefault(
                        FabbisognoTurno.chiave(dow, f), FabbisognoTurno.predefinito(dow, f));
                slots.add(new SlotSettimana(g, f, data, chiaveData, ini, fin, durata, sera, weekend, fab));
            }
        }
        return slots;
    }

    private static Map<String, FabbisognoTurno> normalizzaFabbisogni(Map<String, FabbisognoTurno> input) {
        Map<String, FabbisognoTurno> out = new LinkedHashMap<>();
        for (DayOfWeek g : DayOfWeek.values()) {
            for (int f = 0; f < FASCE_CATALOGO.length; f++) {
                String k = FabbisognoTurno.chiave(g, f);
                if (input != null && input.containsKey(k) && input.get(k) != null) {
                    out.put(k, input.get(k).copia());
                } else {
                    out.put(k, FabbisognoTurno.predefinito(g, f));
                }
            }
        }
        return out;
    }

    private static int contaCandidatiBase(SlotSettimana slot, List<Dipendente> eleggibili) {
        int c = 0;
        for (Dipendente d : eleggibili) {
            if (d.getDisponibilita().verificaCompatibilita(slot.data(), slot.oraInizio(), slot.oraFine()) == null) {
                c++;
            }
        }
        return c;
    }

    private static int sommaMinimiMansioni(FabbisognoTurno fab) {
        int s = 0;
        for (MansioneDipendente m : MansioneDipendente.values()) {
            s += fab.getMinimoMansione(m);
        }
        return s;
    }

    private static int contaAssegnatiInSlot(List<AssegnazioneInterna> assegnazioni, SlotSettimana slot) {
        int c = 0;
        for (AssegnazioneInterna a : assegnazioni) {
            if (a.slot.indiceGiorno() == slot.indiceGiorno() && a.slot.indiceFascia() == slot.indiceFascia()) {
                c++;
            }
        }
        return c;
    }

    private static int contaMansioneInSlot(List<AssegnazioneInterna> assegnazioni,
                                           SlotSettimana slot,
                                           MansioneDipendente mansione) {
        int c = 0;
        for (AssegnazioneInterna a : assegnazioni) {
            if (a.slot.indiceGiorno() == slot.indiceGiorno()
                    && a.slot.indiceFascia() == slot.indiceFascia()
                    && a.dipendente.haMansione(mansione)) {
                c++;
            }
        }
        return c;
    }

    private static double calcolaOreAssegnate(String email,
                                              List<AssegnazioneInterna> assegnazioni,
                                              List<TurnoBean> turniAltreSettimane,
                                              LocalDate lunedi) {
        List<TurnoBean> temp = new ArrayList<>();
        for (TurnoBean t : turniAltreSettimane) {
            if (t.getEmailDipendente() != null && t.getEmailDipendente().equalsIgnoreCase(email)) {
                temp.add(t);
            }
        }
        for (AssegnazioneInterna a : assegnazioni) {
            if (a.dipendente.getEmail().equalsIgnoreCase(email)) {
                temp.add(convertiInBean(a));
            }
        }
        return CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(email, temp, lunedi);
    }

    private static int contaSereDipendente(String email, List<AssegnazioneInterna> assegnazioni) {
        int c = 0;
        for (AssegnazioneInterna a : assegnazioni) {
            if (a.dipendente.getEmail().equalsIgnoreCase(email) && a.slot.isSera()) {
                c++;
            }
        }
        return c;
    }

    private static int contaWeekendDipendente(String email, List<AssegnazioneInterna> assegnazioni) {
        int c = 0;
        for (AssegnazioneInterna a : assegnazioni) {
            if (a.dipendente.getEmail().equalsIgnoreCase(email) && a.slot.isWeekend()) {
                c++;
            }
        }
        return c;
    }

    private static SlotSettimana trovaSlotPerTurno(List<SlotSettimana> slots, TurnoBean t, LocalDate lunedi) {
        LocalDate data = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), lunedi);
        if (data == null) {
            return null;
        }
        for (SlotSettimana s : slots) {
            if (s.data().equals(data)
                    && Objects.equals(s.oraInizio(), t.getOraInizio())
                    && Objects.equals(s.oraFine(), t.getOraFine())) {
                return s;
            }
        }
        // Fallback: se il turno ha orari personalizzati, creiamo uno slot dedicato per non perderlo
        int g = (int) (data.toEpochDay() - lunedi.toEpochDay());
        if (g >= 0 && g < 7) {
            double dur = CalcolatoreOreSettimanali.calcolaDurataOre(t.getOraInizio(), t.getOraFine());
            return new SlotSettimana(g, -1, data, t.getData(), t.getOraInizio(), t.getOraFine(),
                    dur, t.getOraInizio() != null && t.getOraInizio().compareTo("16:00") >= 0, g >= 5,
                    new FabbisognoTurno(data.getDayOfWeek(), -1, 0));
        }
        return null;
    }

    private static boolean corrispondeASlot(TurnoBean t, SlotSettimana slot, LocalDate lunedi) {
        LocalDate data = CalcolatoreOreSettimanali.estraiDataTurno(t.getData(), lunedi);
        return data != null && data.equals(slot.data())
                && Objects.equals(t.getOraInizio(), slot.oraInizio())
                && Objects.equals(t.getOraFine(), slot.oraFine());
    }

    private static TurnoBean convertiInBean(AssegnazioneInterna a) {
        TurnoBean b = new TurnoBean();
        b.setCodiceTurno(a.codiceTurno);
        try {
            b.setData(a.slot.chiaveData());
            b.setOraInizio(a.slot.oraInizio());
            b.setOraFine(a.slot.oraFine());
            b.setEmailDipendente(a.dipendente.getEmail());
        } catch (Exception ignored) {
            // Valori gia' validati dal catalogo
        }
        b.setNomeDipendente(a.dipendente.getNome() + " " + a.dipendente.getCognome());
        b.setStato(a.stato);
        b.setBloccato(a.bloccato);
        b.setGeneratoAutomaticamente(a.generatoAuto);
        return b;
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
        return b;
    }

    private static String firmaTurno(TurnoBean t) {
        return Objects.toString(t.getData(), "") + "|"
                + Objects.toString(t.getOraInizio(), "") + "|"
                + Objects.toString(t.getOraFine(), "") + "|"
                + Objects.toString(t.getEmailDipendente(), "").toLowerCase(Locale.ROOT);
    }

    private static int calcolaProssimoProgressivoCodice(List<TurnoBean> esistenti) {
        int max = 0;
        for (TurnoBean t : esistenti) {
            if (t.getCodiceTurno() != null && t.getCodiceTurno().toUpperCase(Locale.ROOT).startsWith("TUR-")) {
                try {
                    int n = Integer.parseInt(t.getCodiceTurno().substring(4).replaceAll("\\D", ""));
                    if (n > max) {
                        max = n;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignora codici non numerici
                }
            }
        }
        return max + 1;
    }
}
