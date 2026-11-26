package dao;

import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.Emergenza;
import model.Gusto;
import model.Ingrediente;
import model.MaestroGelataio;
import model.Notifica;
import model.OrdineCliente;
import model.Recensione;
import model.Ricetta;
import model.RichiestaGustoPersonalizzato;
import model.StatoRichiesta;
import model.Turno;

/**
 * Archivio dati in memoria per Frostly.
 * Mantiene due istanze distinte: una per lo storage DEMO (pre-popolata)
 * e una separata per la cache JDBC della modalita' FULL (sincronizzata dal database).
 */
public class ArchivioMemoria {

    private static ArchivioMemoria demoInstance;
    private static ArchivioMemoria jdbcCacheInstance;

    private MaestroGelataio maestro;
    private final List<Dipendente> dipendenti;
    private final List<Cliente> clienti;
    private final List<Turno> turni;
    private final List<Emergenza> emergenze;
    private final List<Ingrediente> ingredienti;
    private final List<Gusto> gusti;
    private final List<RichiestaGustoPersonalizzato> richiesteGusto;
    private final List<OrdineCliente> ordini;
    private final List<Notifica> notifiche;
    private final List<Recensione> recensioni;

    protected ArchivioMemoria(boolean popolaDemo) {
        this.maestro = new MaestroGelataio("Stefania", "Guga", "maestro@frostly.it", "admin123");
        this.dipendenti = new ArrayList<>();
        this.clienti = new ArrayList<>();
        this.turni = new ArrayList<>();
        this.emergenze = new ArrayList<>();
        this.ingredienti = new ArrayList<>();
        this.gusti = new ArrayList<>();
        this.richiesteGusto = new ArrayList<>();
        this.ordini = new ArrayList<>();
        this.notifiche = new ArrayList<>();
        this.recensioni = new ArrayList<>();
        if (popolaDemo) {
            popolaDatiIniziali();
        }
    }

    public static synchronized ArchivioMemoria getDemoInstance() {
        if (demoInstance == null) {
            demoInstance = new ArchivioMemoria(true);
        }
        return demoInstance;
    }

    public static synchronized ArchivioMemoria getJdbcCacheInstance() {
        if (jdbcCacheInstance == null) {
            jdbcCacheInstance = new ArchivioMemoria(false);
        }
        return jdbcCacheInstance;
    }

    public static synchronized ArchivioMemoria getInstance() {
        return getDemoInstance();
    }

    private void popolaDatiIniziali() {
        Dipendente d1 = new Dipendente("Mario", "Rossi", "mario@frostly.it", "pass123");
        Dipendente d2 = new Dipendente("Giulia", "Verdi", "giulia@frostly.it", "pass123");
        Dipendente d3 = new Dipendente("Luca", "Neri", "luca@frostly.it", "pass123");
        dipendenti.add(d1);
        dipendenti.add(d2);
        dipendenti.add(d3);

        Cliente c1 = new Cliente("Elena", "Conti", "elena@cliente.it", "pass123");
        Cliente c2 = new Cliente("Marco", "Fabbri", "marco@cliente.it", "pass123");
        clienti.add(c1);
        clienti.add(c2);

        Turno t1 = new Turno("TRN-01", "Lunedi", "07:00", "14:00", d1);
        t1.pubblica();
        d1.aggiungiTurno(t1);
        turni.add(t1);

        Turno t2 = new Turno("TRN-02", "Lunedi", "14:00", "20:00", d2);
        t2.pubblica();
        d2.aggiungiTurno(t2);
        turni.add(t2);

        Turno t3 = new Turno("TRN-03", "Martedi", "07:00", "14:00", d1);
        t3.pubblica();
        d1.aggiungiTurno(t3);
        turni.add(t3);

        // Popola tutti gli ingredienti e prodotti magazzino dalla sorgente unica CatalogoMagazzino
        for (CatalogoMagazzino.ElementoMagazzino e : CatalogoMagazzino.getElementi()) {
            ingredienti.add(new Ingrediente(e.nome(), e.quantitaIniziale(), e.sogliaMinima(), e.unitaMisura(), e.categoria()));
        }

        // Popola tutti i prodotti demo dalla sorgente unica CatalogoDemo
        for (CatalogoDemo.ProdottoDemo p : CatalogoDemo.getProdotti()) {
            Gusto g = new Gusto(p.nome(), p.descrizione(), p.allergeni(), p.prezzo(),
                    new Ricetta("Preparazione " + p.nome(), "Preparazione del laboratorio"));
            g.setCategoria(p.categoria());
            g.setSottocategoria(p.sottocategoria());
            if (p.immagineUrl() != null && !p.immagineUrl().isEmpty()) {
                g.setImmagineUrl(p.immagineUrl());
            }
            gusti.add(g);
        }

        Emergenza emIniziale = new Emergenza("EMG-01", t3, d1, "Visita medica improvvisa martedi mattina");
        t3.dichiaraEmergenza();
        emergenze.add(emIniziale);

        Ingrediente pistacchio = trovaIngredientePerNome("Pasta Pistacchio di Bronte DOP");
        RichiestaGustoPersonalizzato reqIniziale = new RichiestaGustoPersonalizzato(
                "REQ-01", c1, "Pistacchio Salato Croccante", pistacchio, 1.5, "Venerdi", "Poco zucchero e sale di Cervia");
        richiesteGusto.add(reqIniziale);

        notifiche.add(new Notifica("maestro@frostly.it", "Nuova Emergenza Turno",
                "Mario Rossi ha segnalato un'emergenza per il turno TRN-03 (Martedi 08:00-14:00).", "Oggi 09:15"));
        notifiche.add(new Notifica("maestro@frostly.it", "Nuova Richiesta Gusto",
                "Elena Conti ha richiesto il gusto personalizzato 'Pistacchio Salato Croccante' (1.5 kg).", "Oggi 10:30"));

        recensioni.add(new Recensione("REC-01", "elena@cliente.it", "Elena Conti", 5,
                "Pistacchio di Bronte e panna insuperabili!",
                "La consistenza del pistacchio di Bronte DOP è puro velluto, e la panna fresca d'alpeggio è la perfezione assoluta. Il miglior gelato artigianale di Roma!",
                "08/11/2026 16:30",
                "Cara Elena, grazie di cuore per le splendide parole! La tostatura lenta del pistacchio è la nostra passione dal 1984. A presto! - Stefania",
                "08/11/2026 18:00", 14));

        recensioni.add(new Recensione("REC-02", "marco@cliente.it", "Marco Fabbri", 5,
                "Cioccolata calda fondente sublime",
                "Ho provato la nuova cioccolata calda fondente con panna: densa, profumata e avvolgente. Servizio impeccabile e atmosfera calda.",
                "09/11/2026 17:15",
                "Grazie Marco! Selezioniamo solo cacao monorigine 70% dell'Ecuador. Ti aspettiamo per le altre specialità invernali!",
                "09/11/2026 19:20", 9));

        recensioni.add(new Recensione("REC-03", "elena@cliente.it", "Elena Conti", 5,
                "Crepe alla Nutella e panna d'asporto top",
                "Le crepes calde preparate al momento sono fragranti e farcite generosamente. Ottima anche la consegna a domicilio super puntuale!",
                "10/11/2026 09:45", null, null, 6));
    }
    public MaestroGelataio getMaestroGelataio() {
        return maestro;
    }

    public void setMaestroGelataio(MaestroGelataio maestro) {
        if (maestro != null) {
            this.maestro = maestro;
        }
    }

    public List<Dipendente> getDipendenti() {
        return dipendenti;
    }

    public List<Cliente> getClienti() {
        return clienti;
    }

    public Dipendente trovaDipendentePerEmail(String email) {
        for (Dipendente d : dipendenti) {
            if (d.getEmail().equalsIgnoreCase(email)) {
                return d;
            }
        }
        return null;
    }

    public Cliente trovaClientePerEmail(String email) {
        for (Cliente c : clienti) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                return c;
            }
        }
        return null;
    }

    public void eliminaDipendente(String email) {
        dipendenti.removeIf(d -> d.getEmail().equalsIgnoreCase(email));
        turni.removeIf(t -> t.getDipendenteAssegnato() != null && t.getDipendenteAssegnato().getEmail().equalsIgnoreCase(email));
        emergenze.removeIf(e -> e.getRichiedente() != null && e.getRichiedente().getEmail().equalsIgnoreCase(email));
    }

    public void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo) throws FrostlyException {
        if (maestro.getEmail().equalsIgnoreCase(email)
                || trovaDipendentePerEmail(email) != null
                || trovaClientePerEmail(email) != null) {
            throw new DatiNonValidiException("Esiste gia' un utente registrato con l'email: " + email);
        }
        if ("DIPENDENTE".equalsIgnoreCase(ruolo)) {
            dipendenti.add(new Dipendente(nome, cognome, email, password));
        } else {
            clienti.add(new Cliente(nome, cognome, email, password));
        }
    }

    public void sincronizzaUtenteDaDb(String nome, String cognome, String email, String password, String ruolo) {
        if ("DIPENDENTE".equalsIgnoreCase(ruolo) && trovaDipendentePerEmail(email) == null) {
            dipendenti.add(new Dipendente(nome, cognome, email, password));
        } else if ("CLIENTE".equalsIgnoreCase(ruolo) && trovaClientePerEmail(email) == null) {
            clienti.add(new Cliente(nome, cognome, email, password));
        }
    }

    public void impostaUtentiDaDb(List<Dipendente> daDbDipendenti, List<Cliente> daDbClienti) {
        if (daDbDipendenti != null) {
            dipendenti.clear();
            dipendenti.addAll(daDbDipendenti);
        }
        if (daDbClienti != null) {
            clienti.clear();
            clienti.addAll(daDbClienti);
        }
    }
    public List<Turno> getTurni() {
        return turni;
    }

    public void salvaTurno(Turno turno) {
        for (int i = 0; i < turni.size(); i++) {
            if (turni.get(i).getCodiceTurno().equals(turno.getCodiceTurno())) {
                turni.set(i, turno);
                return;
            }
        }
        turni.add(turno);
    }

    public void eliminaTurno(String codiceTurno) {
        if (codiceTurno == null) return;
        for (Turno t : new ArrayList<>(turni)) {
            if (codiceTurno.equalsIgnoreCase(t.getCodiceTurno())) {
                if (t.getDipendenteAssegnato() != null) {
                    t.getDipendenteAssegnato().rimuoviTurno(t);
                }
                turni.remove(t);
            }
        }
        emergenze.removeIf(e -> e.getTurno() != null && codiceTurno.equalsIgnoreCase(e.getTurno().getCodiceTurno()));
    }
    public List<Emergenza> getEmergenze() {
        return emergenze;
    }

    public void salvaEmergenza(Emergenza emergenza) {
        for (int i = 0; i < emergenze.size(); i++) {
            if (emergenze.get(i).getCodiceEmergenza().equals(emergenza.getCodiceEmergenza())) {
                emergenze.set(i, emergenza);
                return;
            }
        }
        emergenze.add(emergenza);
    }

    public void sincronizzaEmergenzaDaDb(String codEm, String codTurno, String emailRich, String emailSost, String motivo, String stato, String nota) {
        for (Emergenza e : emergenze) {
            if (e.getCodiceEmergenza().equalsIgnoreCase(codEm)) {
                if ("APPROVATA".equalsIgnoreCase(stato)) {
                    Dipendente sost = trovaDipendentePerEmail(emailSost);
                    if (sost != null) {
                        e.approvaConSostituto(sost, nota);
                    }
                } else if ("RIFIUTATA".equalsIgnoreCase(stato)) {
                    e.rifiuta(nota);
                }
                return;
            }
        }
        Turno t = null;
        for (Turno turn : turni) {
            if (turn.getCodiceTurno().equalsIgnoreCase(codTurno)) {
                t = turn;
                break;
            }
        }
        Dipendente rich = trovaDipendentePerEmail(emailRich);
        if (t != null && rich != null) {
            Emergenza em = new Emergenza(codEm, t, rich, motivo);
            if ("APPROVATA".equalsIgnoreCase(stato)) {
                Dipendente sost = trovaDipendentePerEmail(emailSost);
                if (sost != null) {
                    em.approvaConSostituto(sost, nota);
                }
            } else if ("RIFIUTATA".equalsIgnoreCase(stato)) {
                em.rifiuta(nota);
            }
            emergenze.add(em);
        }
    }
    public List<Ingrediente> getIngredienti() {
        return ingredienti;
    }

    public Ingrediente trovaIngredientePerNome(String nome) {
        for (Ingrediente ing : ingredienti) {
            if (ing.getNome().equalsIgnoreCase(nome)) {
                return ing;
            }
        }
        return null;
    }

    public void aggiornaIngrediente(Ingrediente ingrediente) {
        for (int i = 0; i < ingredienti.size(); i++) {
            if (ingredienti.get(i).getNome().equalsIgnoreCase(ingrediente.getNome())) {
                ingredienti.set(i, ingrediente);
                return;
            }
        }
        ingredienti.add(ingrediente);
    }

    public void eliminaIngrediente(String nome) {
        ingredienti.removeIf(ing -> ing.getNome().equalsIgnoreCase(nome));
    }

    public void sincronizzaIngredienteDaDb(String nome, double quantita, double sogliaMin, String unita) {
        sincronizzaIngredienteDaDb(nome, quantita, sogliaMin, unita, null);
    }

    public void sincronizzaIngredienteDaDb(String nome, double quantita, double sogliaMin, String unita, String categoria) {
        Ingrediente ing = trovaIngredientePerNome(nome);
        if (ing != null) {
            ing.setQuantitaDisponibile(quantita);
            if (categoria != null && !categoria.trim().isEmpty()) {
                ing.setCategoria(categoria.trim());
            }
        } else {
            ingredienti.add(new Ingrediente(nome, quantita, sogliaMin, unita, categoria));
        }
    }
    public List<Gusto> getGusti() {
        return gusti;
    }

    public Gusto trovaGustoPerNome(String nome) {
        for (Gusto g : gusti) {
            if (g.getNome().equalsIgnoreCase(nome)) {
                return g;
            }
        }
        return null;
    }

    public void salvaGustoMenu(Gusto gusto) {
        for (int i = 0; i < gusti.size(); i++) {
            if (gusti.get(i).getNome().equalsIgnoreCase(gusto.getNome())) {
                gusti.set(i, gusto);
                return;
            }
        }
        gusti.add(gusto);
    }

    public void rimuoviGustoMenu(String nomeGusto) {
        gusti.removeIf(g -> g.getNome().equalsIgnoreCase(nomeGusto));
    }

    public void sincronizzaGustoDaDb(String nome, String descrizione, String allergeni, double prezzoAlKg,
                                     boolean disponibile, String categoria, String sottocategoria, String immagineUrl) {
        Gusto esistente = trovaGustoPerNome(nome);
        if (esistente != null) {
            esistente.setDescrizione(descrizione);
            esistente.setAllergeni(allergeni);
            esistente.setPrezzoAlKg(prezzoAlKg);
            esistente.setDisponibileOggi(disponibile);
            esistente.setCategoria(categoria);
            esistente.setSottocategoria(sottocategoria);
            esistente.setImmagineUrl(immagineUrl);
        } else {
            Ricetta ricBase = new Ricetta("Ricetta Artigianale " + nome, "Mantecazione tradizionale di laboratorio");
            Gusto nuovo = new Gusto(nome, descrizione, allergeni, prezzoAlKg, ricBase);
            nuovo.setDisponibileOggi(disponibile);
            nuovo.setCategoria(categoria);
            nuovo.setSottocategoria(sottocategoria);
            nuovo.setImmagineUrl(immagineUrl);
            gusti.add(nuovo);
        }
    }
    public List<RichiestaGustoPersonalizzato> getRichiesteGusto() {
        return richiesteGusto;
    }

    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        for (int i = 0; i < richiesteGusto.size(); i++) {
            if (richiesteGusto.get(i).getCodiceRichiesta().equals(richiesta.getCodiceRichiesta())) {
                richiesteGusto.set(i, richiesta);
                return;
            }
        }
        richiesteGusto.add(richiesta);
    }

    public void sincronizzaRichiestaGustoDaDb(String codiceRich, String emailCliente, String nomeGusto,
                                              String nomeIngr, double quantitaKg, String dataRitiro,
                                              String noteCliente) {
        Cliente cli = trovaClientePerEmail(emailCliente);
        if (cli == null) {
            cli = new Cliente("Cliente", emailCliente, emailCliente, "");
        }
        Ingrediente ing = trovaIngredientePerNome(nomeIngr);
        if (ing == null) {
            ing = new Ingrediente(nomeIngr != null ? nomeIngr : "Ingrediente Speciale", 10.0, 2.0, "Kg");
        }
        for (RichiestaGustoPersonalizzato r : richiesteGusto) {
            if (r.getCodiceRichiesta().equalsIgnoreCase(codiceRich)) {
                return;
            }
        }
        RichiestaGustoPersonalizzato nuova = new RichiestaGustoPersonalizzato(
                codiceRich, cli, nomeGusto, ing, quantitaKg, dataRitiro, noteCliente != null ? noteCliente : "");
        richiesteGusto.add(nuova);
    }

    public void aggiornaStatoRichiestaGustoDaDb(String codiceRich, String stato, String rispostaMaestro) {
        StatoRichiesta st = parseStatoRichiesta(stato);
        for (RichiestaGustoPersonalizzato r : richiesteGusto) {
            if (r.getCodiceRichiesta().equalsIgnoreCase(codiceRich)) {
                r.setStato(st);
                r.setRispostaMaestro(rispostaMaestro != null ? rispostaMaestro : "");
                return;
            }
        }
    }

    private StatoRichiesta parseStatoRichiesta(String stato) {
        if (stato != null && !stato.isEmpty()) {
            try {
                return StatoRichiesta.valueOf(stato.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return StatoRichiesta.IN_ATTESA;
            }
        }
        return StatoRichiesta.IN_ATTESA;
    }
    public List<OrdineCliente> getOrdini() {
        return ordini;
    }

    public void salvaOrdine(OrdineCliente ordine) {
        for (int i = 0; i < ordini.size(); i++) {
            if (ordini.get(i).getCodiceOrdine().equals(ordine.getCodiceOrdine())) {
                ordini.set(i, ordine);
                return;
            }
        }
        ordini.add(ordine);
    }

    public void sincronizzaOrdineDaDb(String codiceOrdine, String emailCliente, String nomeGusto,
                                      double pesoKg, String dataRitiro,
                                      double prezzoTotale, String descrizioneDettagliata) {
        String descPulita = bean.OrdineBean.pulisciDescrizione(descrizioneDettagliata);
        Cliente cli = trovaClientePerEmail(emailCliente);
        if (cli == null) {
            cli = new Cliente("Cliente", emailCliente, emailCliente, "");
        }
        Gusto g = (nomeGusto != null && !nomeGusto.isEmpty()) ? trovaGustoPerNome(nomeGusto) : null;
        for (OrdineCliente esistente : ordini) {
            if (esistente.getCodiceOrdine().equalsIgnoreCase(codiceOrdine)) {
                esistente.setPrezzoTotale(prezzoTotale);
                esistente.setDescrizioneDettagliata(descPulita);
                return;
            }
        }
        OrdineCliente nuovo = new OrdineCliente(codiceOrdine, cli, g, pesoKg, dataRitiro);
        nuovo.setPrezzoTotale(prezzoTotale);
        nuovo.setDescrizioneDettagliata(descPulita);
        ordini.add(nuovo);
    }

    public void aggiornaStatoOrdineDaDb(String codiceOrdine, String stato) {
        StatoRichiesta st = parseStatoRichiesta(stato);
        for (OrdineCliente esistente : ordini) {
            if (esistente.getCodiceOrdine().equalsIgnoreCase(codiceOrdine)) {
                esistente.setStato(st);
                return;
            }
        }
    }
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        List<Notifica> risultato = new ArrayList<>();
        for (Notifica n : notifiche) {
            if (n.getEmailDestinatario().equalsIgnoreCase(emailDestinatario)) {
                risultato.add(n);
            }
        }
        return risultato;
    }

    public void inviaNotifica(Notifica notifica) {
        this.notifiche.add(0, notifica);
    }

    public void eliminaNotifica(String emailDestinatario, String titolo, String timestamp) {
        for (int i = 0; i < notifiche.size(); i++) {
            Notifica n = notifiche.get(i);
            if (n.getEmailDestinatario().equalsIgnoreCase(emailDestinatario)
                    && n.getTitolo().equals(titolo)
                    && (timestamp == null || timestamp.isEmpty() || n.getTimestamp().equals(timestamp))) {
                notifiche.remove(i);
                break;
            }
        }
    }

    public void sincronizzaNotificaDaDb(String emailDest, String titolo, String messaggio, String timestamp) {
        for (Notifica n : notifiche) {
            if (n.getEmailDestinatario().equalsIgnoreCase(emailDest)
                    && n.getTitolo().equals(titolo)
                    && n.getMessaggio().equals(messaggio)
                    && n.getTimestamp().equals(timestamp)) {
                return;
            }
        }
        notifiche.add(0, new Notifica(emailDest, titolo, messaggio, timestamp));
    }
    private final java.util.Map<String, model.FabbisognoTurno> fabbisogniTurni = new java.util.LinkedHashMap<>();
    private final java.util.List<model.RichiestaIndisponibilita> richiesteIndisponibilita = new java.util.ArrayList<>();

    public synchronized java.util.List<model.RichiestaIndisponibilita> getRichiesteIndisponibilita() {
        return new java.util.ArrayList<>(richiesteIndisponibilita);
    }

    public synchronized void salvaRichiestaIndisponibilita(model.RichiestaIndisponibilita r) {
        if (r == null) {
            return;
        }
        for (int i = 0; i < richiesteIndisponibilita.size(); i++) {
            if (richiesteIndisponibilita.get(i).getCodiceRichiesta().equalsIgnoreCase(r.getCodiceRichiesta())) {
                richiesteIndisponibilita.set(i, r);
                return;
            }
        }
        richiesteIndisponibilita.add(0, r);
    }

    public synchronized java.util.Map<String, model.FabbisognoTurno> getFabbisogniTurni() {
        if (fabbisogniTurni.isEmpty()) {
            for (java.time.DayOfWeek g : java.time.DayOfWeek.values()) {
                for (int f = 0; f < 5; f++) {
                    fabbisogniTurni.put(model.FabbisognoTurno.chiave(g, f), model.FabbisognoTurno.predefinito(g, f));
                }
            }
        }
        java.util.Map<String, model.FabbisognoTurno> copia = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, model.FabbisognoTurno> e : fabbisogniTurni.entrySet()) {
            copia.put(e.getKey(), e.getValue().copia());
        }
        return copia;
    }

    public synchronized void salvaFabbisogniTurni(java.util.Map<String, model.FabbisognoTurno> nuovi) {
        if (nuovi == null) {
            return;
        }
        fabbisogniTurni.clear();
        for (java.util.Map.Entry<String, model.FabbisognoTurno> e : nuovi.entrySet()) {
            fabbisogniTurni.put(e.getKey(), e.getValue().copia());
        }
    }

    public synchronized void aggiornaConfigurazioneDipendente(String email,
                                                             model.ConfigurazioneOreDipendente configurazioneOre,
                                                             model.DisponibilitaDipendente disponibilita) {
        aggiornaConfigurazioneDipendente(email, configurazioneOre, disponibilita, null, null);
    }

    public synchronized void aggiornaConfigurazioneDipendente(String email,
                                                             model.ConfigurazioneOreDipendente configurazioneOre,
                                                             model.DisponibilitaDipendente disponibilita,
                                                             java.util.Set<model.MansioneDipendente> mansioni,
                                                             Boolean attivo) {
        Dipendente dip = trovaDipendentePerEmail(email);
        aggiornaConfigurazioneDipendente(email, configurazioneOre, disponibilita, mansioni, attivo,
                dip != null ? dip.getMaxTurniSettimanali() : null);
    }

    public synchronized void aggiornaConfigurazioneDipendente(String email,
                                                             model.ConfigurazioneOreDipendente configurazioneOre,
                                                             model.DisponibilitaDipendente disponibilita,
                                                             java.util.Set<model.MansioneDipendente> mansioni,
                                                             Boolean attivo,
                                                             Integer maxTurniSettimanali) {
        Dipendente dip = trovaDipendentePerEmail(email);
        if (dip != null) {
            if (configurazioneOre != null) {
                dip.setConfigurazioneOre(configurazioneOre);
            }
            if (disponibilita != null) {
                dip.setDisponibilita(disponibilita);
            }
            if (mansioni != null && !mansioni.isEmpty()) {
                dip.setMansioni(mansioni);
            }
            if (attivo != null) {
                dip.setAttivo(attivo);
            }
            dip.setMaxTurniSettimanali(maxTurniSettimanali);
        }
    }

    public synchronized List<Recensione> getRecensioni() {
        return new ArrayList<>(recensioni);
    }

    public synchronized Recensione salvaRecensione(Recensione r) {
        if (r.getCodiceRecensione() == null || r.getCodiceRecensione().isEmpty()) {
            r.setCodiceRecensione("REC-" + String.format("%02d", recensioni.size() + 1));
        }
        for (int i = 0; i < recensioni.size(); i++) {
            if (recensioni.get(i).getCodiceRecensione().equalsIgnoreCase(r.getCodiceRecensione())) {
                recensioni.set(i, r);
                return r;
            }
        }
        recensioni.add(0, r);
        return r;
    }

    public synchronized void rispondiRecensione(String codiceRecensione, String rispostaMaestro, String dataRisposta) {
        for (Recensione r : recensioni) {
            if (r.getCodiceRecensione().equalsIgnoreCase(codiceRecensione)) {
                r.setRispostaMaestro(rispostaMaestro);
                r.setDataRisposta(dataRisposta);
                return;
            }
        }
    }

    public synchronized void incrementaMiPiaceRecensione(String codiceRecensione) {
        for (Recensione r : recensioni) {
            if (r.getCodiceRecensione().equalsIgnoreCase(codiceRecensione)) {
                r.incrementaMiPiace();
                return;
            }
        }
    }

    public synchronized void sincronizzaRecensioneDaDb(String cod, String email, String nome, int voto,
                                                       String tit, String comm, String data, String risp,
                                                       String dataRisp, int miPiace, String fotoPath) {
        for (Recensione r : recensioni) {
            if (r.getCodiceRecensione().equalsIgnoreCase(cod)) {
                r.setVotoStelle(voto);
                r.setTitolo(tit);
                r.setCommento(comm);
                r.setRispostaMaestro(risp);
                r.setDataRisposta(dataRisp);
                r.setMiPiace(miPiace);
                r.setFotoPath(fotoPath);
                return;
            }
        }
        recensioni.add(new Recensione(cod, email, nome, voto, tit, comm, data, risp, dataRisp, miPiace, fotoPath));
    }
}
