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
import model.Ricetta;
import model.RichiestaGustoPersonalizzato;
import model.StatoRichiesta;
import model.Turno;

public class InMemoryFrostlyDAO implements FrostlyDAO {

    private static InMemoryFrostlyDAO instance;

    private final MaestroGelataio maestro;
    private final List<Dipendente> dipendenti;
    private final List<Cliente> clienti;
    private final List<Turno> turni;
    private final List<Emergenza> emergenze;
    private final List<Ingrediente> ingredienti;
    private final List<Gusto> gusti;
    private final List<RichiestaGustoPersonalizzato> richiesteGusto;
    private final List<OrdineCliente> ordini;
    private final List<Notifica> notifiche;

    protected InMemoryFrostlyDAO() {
        this.maestro = new MaestroGelataio("Guglielmo", "Bianchi", "maestro@frostly.it", "admin123");
        this.dipendenti = new ArrayList<>();
        this.clienti = new ArrayList<>();
        this.turni = new ArrayList<>();
        this.emergenze = new ArrayList<>();
        this.ingredienti = new ArrayList<>();
        this.gusti = new ArrayList<>();
        this.richiesteGusto = new ArrayList<>();
        this.ordini = new ArrayList<>();
        this.notifiche = new ArrayList<>();
        popolaDatiIniziali();
    }

    public static synchronized InMemoryFrostlyDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryFrostlyDAO();
        }
        return instance;
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

        Turno t1 = new Turno("TRN-01", "Lunedi", "08:00", "14:00", d1);
        t1.pubblica();
        d1.aggiungiTurno(t1);
        turni.add(t1);

        Turno t2 = new Turno("TRN-02", "Lunedi", "14:00", "20:00", d2);
        t2.pubblica();
        d2.aggiungiTurno(t2);
        turni.add(t2);

        Turno t3 = new Turno("TRN-03", "Martedi", "08:00", "14:00", d1);
        t3.pubblica();
        d1.aggiungiTurno(t3);
        turni.add(t3);

        Ingrediente latte = new Ingrediente("Latte Fresco Alta Qualita", 25.0, 10.0, "Litri");
        Ingrediente pistacchio = new Ingrediente("Pasta Pistacchio di Bronte DOP", 6.0, 2.5, "Kg");
        Ingrediente cioccolato = new Ingrediente("Cioccolato Fondente Ecuador 75%", 8.0, 3.0, "Kg");
        Ingrediente nocciola = new Ingrediente("Nocciola Piemonte IGP", 5.0, 2.0, "Kg");
        Ingrediente mango = new Ingrediente("Polpa di Mango Alphonso", 1.5, 2.0, "Kg");
        ingredienti.add(latte);
        ingredienti.add(pistacchio);
        ingredienti.add(cioccolato);
        ingredienti.add(nocciola);
        ingredienti.add(mango);

        Ricetta ricPistacchio = new Ricetta("Ricetta Pistacchio Reale", "Pastorizzazione a 85 gradi e mantecazione lenta");
        ricPistacchio.aggiungiIngrediente(latte, 2.5);
        ricPistacchio.aggiungiIngrediente(pistacchio, 0.8);

        Ricetta ricCioccolato = new Ricetta("Ricetta Fondente Nero", "Infusione cacao a caldo e mantecazione");
        ricCioccolato.aggiungiIngrediente(latte, 2.0);
        ricCioccolato.aggiungiIngrediente(cioccolato, 1.2);

        Ricetta ricNocciola = new Ricetta("Ricetta Crema di Nocciola", "Tostatura artigianale e mantecazione");
        ricNocciola.aggiungiIngrediente(latte, 2.5);
        ricNocciola.aggiungiIngrediente(nocciola, 0.9);

        gusti.add(new Gusto("Pistacchio di Bronte DOP", "Cremoso al pistacchio siciliano tostato", "Latte, Frutta a guscio", 26.0, ricPistacchio));
        gusti.add(new Gusto("Fondente Ecuador 75%", "Cioccolato intenso monorigine", "Latte", 24.0, ricCioccolato));
        gusti.add(new Gusto("Nocciola Piemonte IGP", "Classico cremoso alle nocciole trilobate", "Latte, Frutta a guscio", 25.0, ricNocciola));
        gusti.add(new Gusto("Crema Antica del Maestro", "Tuorli freschi, vaniglia Bourbon e scorza di limone", "Latte, Uova", 22.0, ricNocciola));
        gusti.add(new Gusto("Stracciatella Artigianale", "Fiordilatte fresco con scaglie di fondente croccante", "Latte", 23.0, ricCioccolato));
        gusti.add(new Gusto("Sorbetto Mango Alphonso", "Sorbetto 100% frutta fresca senza latte", "Senza Lattosio", 24.0, ricPistacchio));
        trovaGustoPerNome("Pistacchio di Bronte DOP").setImmagineUrl("1793962265284_nascita-gelato-pistacchio.jpg");
        trovaGustoPerNome("Fondente Ecuador 75%").setSottocategoria("Cioccolato");
        trovaGustoPerNome("Fondente Ecuador 75%").setImmagineUrl("1793962340626_gelato-al-cioccolato-570x380.jpg");
        trovaGustoPerNome("Nocciola Piemonte IGP").setImmagineUrl("1793962399876_Screenshot_2026-11-06_alle_11.53.13.png");
        trovaGustoPerNome("Crema Antica del Maestro").setImmagineUrl("1793962432008_Screenshot_2026-11-06_alle_11.53.44.png");
        trovaGustoPerNome("Stracciatella Artigianale").setImmagineUrl("1793962480783_stracciatella-gelato-recipe-1-1200px.jpg");
        trovaGustoPerNome("Sorbetto Mango Alphonso").setSottocategoria("Frutta");
        trovaGustoPerNome("Sorbetto Mango Alphonso").setImmagineUrl("1793962514184_Screenshot_2026-11-06_alle_11.55.07.png");

        // Gusti Gelati — Sottocategoria Frutta
        aggiungiProdottoEsempio("Fragola", "Sorbetto fresco alle fragole mature di bosco", "Senza Lattosio", 22.0, "Gelati", "Frutta", "fragola.png");
        aggiungiProdottoEsempio("Menta", "Gelato rinfrescante alla menta piperita del Piemonte", "Latte", 22.0, "Gelati", "Frutta", "menta.png");
        aggiungiProdottoEsempio("Limone", "Sorbetto artigianale ai limoni di Sorrento IGP", "Senza Lattosio", 22.0, "Gelati", "Frutta", "limone.png");
        aggiungiProdottoEsempio("Melone Bianco", "Sorbetto delicato e profumato al melone bianco invernale", "Senza Lattosio", 23.0, "Gelati", "Frutta", "melone_bianco.png");
        aggiungiProdottoEsempio("Lampone", "Sorbetto vellutato ai lamponi freschi di montagna", "Senza Lattosio", 24.0, "Gelati", "Frutta", "lampone.png");
        aggiungiProdottoEsempio("Anguria", "Sorbetto dissetante al cuore di anguria estiva", "Senza Lattosio", 22.0, "Gelati", "Frutta", "anguria.png");
        aggiungiProdottoEsempio("Liquirizia", "Gelato intenso alla liquirizia pura di Calabria DOP", "Latte", 24.0, "Gelati", "Frutta", "liquirizia.png");
        aggiungiProdottoEsempio("Pesca", "Sorbetto morbido alle pesche gialle di vigna", "Senza Lattosio", 22.0, "Gelati", "Frutta", "pesca.png");
        aggiungiProdottoEsempio("Albicocca", "Sorbetto solare alle albicocche del Vesuvio", "Senza Lattosio", 22.0, "Gelati", "Frutta", "albicocca.png");
        aggiungiProdottoEsempio("Banana", "Cremoso naturale alla banana matura", "Latte", 22.0, "Gelati", "Frutta", "banana.png");
        aggiungiProdottoEsempio("Banana al Cioccolato", "Gelato alla banana con variegatura e scaglie di cioccolato fondente", "Latte", 24.0, "Gelati", "Frutta", "banana_cioccolato.png");

        // Gusti Gelati — Sottocategoria Special
        aggiungiProdottoEsempio("Snickers", "Cremoso alle arachidi con caramello salato, cioccolato al latte e arachidi tostate", "Latte, Arachidi, Frutta a guscio", 26.0, "Gelati", "Special", "snickers.png");
        aggiungiProdottoEsempio("Bounty", "Gelato esotico al cocco fresco con scaglie di cioccolato e cocco rapè", "Latte", 25.0, "Gelati", "Special", "bounty.png");
        aggiungiProdottoEsempio("Delizia al Limone", "Pan di Spagna bagnato al limoncello con crema vellutata ai limoni d'Amalfi", "Latte, Uova, Glutine", 26.0, "Gelati", "Special", "delizia_limone.png");
        aggiungiProdottoEsempio("Nutella", "Cremoso artigianale con abbondante Nutella pura variegata", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "gelato_nutella.png");
        aggiungiProdottoEsempio("Galak", "Gelato al cioccolato bianco Galak con croccanti sfoglie di wafer", "Latte, Glutine", 25.0, "Gelati", "Special", "galak.png");
        aggiungiProdottoEsempio("Amarandola", "Mandorla di Sicilia variegato con amarene intere sciroppate", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "amarandola.png");
        aggiungiProdottoEsempio("Croccante all'Amarena", "Fiordilatte con croccante di mandorle caramellate e variegatura all'amarena", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "croccante_amarena.png");
        aggiungiProdottoEsempio("Pistacchio variegato Cioccolato e Wafer", "Pistacchio tostato variegato con crema al cioccolato e cialde di wafer", "Latte, Frutta a guscio, Glutine", 27.0, "Gelati", "Special", "pistacchio_cioccolato_wafer.png");
        aggiungiProdottoEsempio("Cremino Nutella", "Strati vellutati di fiordilatte e nocciola alternati a colate di Nutella", "Latte, Frutta a guscio", 27.0, "Gelati", "Special", "cremino_nutella.png");
        aggiungiProdottoEsempio("Mascarpone Amarena e Gentilini", "Crema al mascarpone fresco con amarene brusche e biscotti Osvego Gentilini", "Latte, Uova, Glutine", 27.0, "Gelati", "Special", "mascarpone_amarena_gentilini.png");
        aggiungiProdottoEsempio("Caffè", "Gelato cremoso all'espresso 100% Arabica tostato a legna", "Latte", 23.0, "Gelati", "Special", "gelato_caffe.png");
        aggiungiProdottoEsempio("Spagnola", "Crema antica e fiordilatte variegati con amarene Fabbri intere", "Latte, Uova", 25.0, "Gelati", "Special", "spagnola.png");
        aggiungiProdottoEsempio("Zuppa Inglese", "Crema pasticcera e cacao con pan di Spagna all'Alchermes e savoiardi", "Latte, Uova, Glutine", 26.0, "Gelati", "Special", "zuppa_inglese.png");

        // Panna Fresca d'Alpeggio (100g, 200g, 300g sempre selezionabile con foto)
        aggiungiProdottoEsempio("Panna Fresca 100g", "Coppetta da 100g di panna fresca d'alpeggio montata al momento", "Latte", 2.5, "Panna", "Porzioni", "panna_100g.png");
        aggiungiProdottoEsempio("Panna Fresca 200g", "Vaschetta da 200g di panna fresca d'alpeggio montata al momento", "Latte", 4.5, "Panna", "Porzioni", "panna_200g.png");
        aggiungiProdottoEsempio("Panna Fresca 300g", "Vaschetta grande da 300g di panna fresca d'alpeggio montata", "Latte", 6.0, "Panna", "Porzioni", "panna_300g.png");

        // Crepes Artigianali (le 4 varianti con foto: Nutella, Visciole, Marmellata, Crema Pan di Stelle)
        aggiungiProdottoEsempio("Crepe alla Nutella", "Crepe calda artigianale farcita con abbondante Nutella cremosa", "Glutine, Latte, Frutta a guscio", 5.0, "Crepes", "Farciture", "crepe_nutella.png");
        aggiungiProdottoEsempio("Crepe alle Visciole", "Crepe calda farcita con confettura artigianale di visciole brusche", "Glutine, Uova, Latte", 4.5, "Crepes", "Farciture", "crepe_visciole.png");
        aggiungiProdottoEsempio("Crepe alla Marmellata", "Crepe calda farcita con confettura vellutata di albicocche", "Glutine, Uova, Latte", 4.5, "Crepes", "Farciture", "crepe_albicocca.png");
        aggiungiProdottoEsempio("Crepe Crema Pan di Stelle", "Crepe calda con crema spalmabile Pan di Stelle, panna e stelline croccanti", "Glutine, Latte, Uova, Frutta a guscio", 5.5, "Crepes", "Farciture", "crepe_pandistelle.png");

        // Bevande
        aggiungiProdottoEsempio("Acqua Naturale 500ml", "Acqua minerale naturale fresca 500ml", "Nessuno", 1.5, "Bevande", "Acqua", "acqua_naturale.png");
        aggiungiProdottoEsempio("Acqua Frizzante 500ml", "Acqua minerale frizzante fresca 500ml", "Nessuno", 1.5, "Bevande", "Acqua", "acqua_frizzante.png");
        aggiungiProdottoEsempio("Coca Cola 33cl", "Lattina classica fresca 33cl", "Nessuno", 3.0, "Bevande", "Bibite", "coca_cola.png");
        aggiungiProdottoEsempio("Fanta 33cl", "Lattina all'arancia fresca 33cl", "Nessuno", 3.0, "Bevande", "Bibite", "fanta.png");

        // Caffetteria, Biscotti, Crostate, Tisane
        aggiungiProdottoEsempio("Caffe Espresso", "Miscela 100% arabica tostata lentamente", "Nessuno", 1.2, "Caffetteria", "Caffe", "");
        aggiungiProdottoEsempio("Cappuccino", "Espresso con latte fresco montato a vapore", "Latte", 1.8, "Caffetteria", "Caffe", "");
        aggiungiProdottoEsempio("Affogato al Caffe", "Gelato fior di latte annegato nell'espresso caldo", "Latte", 4.0, "Caffetteria", "Specialita", "");
        aggiungiProdottoEsempio("Cioccolata Calda Fondente", "Densa, con cacao 70% e un tocco di panna", "Latte", 3.5, "Caffetteria", "Specialita", "");
        aggiungiProdottoEsempio("Cantucci alle Mandorle", "Biscotti toscani croccanti, ideali per il caffe", "Glutine, Frutta a guscio, Uova", 5.0, "Biscotti", "Secchi", "");
        aggiungiProdottoEsempio("Baci di Dama", "Frollini alla nocciola farciti di cioccolato", "Glutine, Frutta a guscio, Latte", 6.0, "Biscotti", "Secchi", "");
        aggiungiProdottoEsempio("Crostata alla Marmellata", "Frolla al burro con confettura di albicocche", "Glutine, Uova, Latte", 2.8, "Crostate", "Classiche", "");
        aggiungiProdottoEsempio("Crostata ai Frutti di Bosco", "Crema pasticcera e frutti di bosco freschi", "Glutine, Uova, Latte", 3.2, "Crostate", "Alla frutta", "");
        aggiungiProdottoEsempio("Tisana Zenzero e Limone", "Infuso caldo digestivo e rinfrescante", "Nessuno", 2.5, "Tisane", "Infusi", "");
        aggiungiProdottoEsempio("Tisana ai Frutti Rossi", "Infuso fruttato senza caffeina", "Nessuno", 2.5, "Tisane", "Infusi", "");

        Emergenza emIniziale = new Emergenza("EMG-01", t3, d1, "Visita medica improvvisa martedi mattina");
        t3.dichiaraEmergenza();
        emergenze.add(emIniziale);

        RichiestaGustoPersonalizzato reqIniziale = new RichiestaGustoPersonalizzato(
                "REQ-01", c1, "Pistacchio Salato Croccante", pistacchio, 1.5, "Venerdi", "Poco zucchero e sale di Cervia");
        richiesteGusto.add(reqIniziale);

        notifiche.add(new Notifica("maestro@frostly.it", "Nuova Emergenza Turno",
                "Mario Rossi ha segnalato un'emergenza per il turno TRN-03 (Martedi 08:00-14:00).", "Oggi 09:15"));
        notifiche.add(new Notifica("maestro@frostly.it", "Nuova Richiesta Gusto",
                "Elena Conti ha richiesto il gusto personalizzato 'Pistacchio Salato Croccante' (1.5 kg).", "Oggi 10:30"));
    }

    private void aggiungiProdottoEsempio(String nome, String descrizione, String allergeni, double prezzo,
                                         String categoria, String sottocategoria, String imgUrl) {
        Gusto g = new Gusto(nome, descrizione, allergeni, prezzo,
                new Ricetta("Preparazione " + nome, "Preparazione del laboratorio"));
        g.setCategoria(categoria);
        g.setSottocategoria(sottocategoria);
        if (imgUrl != null && !imgUrl.isEmpty()) {
            g.setImmagineUrl(imgUrl);
        }
        gusti.add(g);
    }

    @Override
    public MaestroGelataio getMaestroGelataio() {
        return maestro;
    }

    @Override
    public void eliminaDipendente(String email) throws FrostlyException {
        dipendenti.removeIf(d -> d.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo)
            throws FrostlyException {
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

        public void sincronizzaIngredienteDaDb(String nome, double quantita, double sogliaMin, String unita) {
        Ingrediente ing = trovaIngredientePerNome(nome);
        if (ing != null) {
            ing.setQuantitaDisponibile(quantita);
        } else {
            ingredienti.add(new Ingrediente(nome, quantita, sogliaMin, unita));
        }
    }

    public void sincronizzaEmergenzaDaDb(String codEm, String codTurno, String emailRich, String emailSost, String motivo, String stato, String nota) {
        for (Emergenza e : emergenze) {
            if (e.getCodiceEmergenza().equalsIgnoreCase(codEm)) {
                if ("APPROVATA".equalsIgnoreCase(stato)) {
                    Dipendente sost = trovaDipendentePerEmail(emailSost);
                    e.approvaConSostituto(sost, nota);
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
                em.approvaConSostituto(trovaDipendentePerEmail(emailSost), nota);
            } else if ("RIFIUTATA".equalsIgnoreCase(stato)) {
                em.rifiuta(nota);
            }
            emergenze.add(em);
        }
    }

    public void sincronizzaGustoDaDb(String nome, String descrizione, String allergeni, double prezzoAlKg, boolean disponibile) {
        sincronizzaGustoDaDb(nome, descrizione, allergeni, prezzoAlKg, disponibile, "Gelati", "Creme", "");
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

    public void sincronizzaOrdineDaDb(String codiceOrdine, String emailCliente, String nomeGusto,
                                      double pesoKg, String dataRitiro, String stato,
                                      double prezzoTotale, String descrizioneDettagliata) {
        Cliente cli = trovaClientePerEmail(emailCliente);
        if (cli == null) {
            cli = new Cliente("Cliente", emailCliente, emailCliente, "");
        }
        Gusto g = (nomeGusto != null && !nomeGusto.isEmpty()) ? trovaGustoPerNome(nomeGusto) : null;
        StatoRichiesta st = StatoRichiesta.IN_ATTESA;
        if (stato != null && !stato.isEmpty()) {
            try {
                st = StatoRichiesta.valueOf(stato.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                st = StatoRichiesta.IN_ATTESA;
            }
        }
        for (OrdineCliente esistente : ordini) {
            if (esistente.getCodiceOrdine().equalsIgnoreCase(codiceOrdine)) {
                esistente.setStato(st);
                esistente.setPrezzoTotale(prezzoTotale);
                esistente.setDescrizioneDettagliata(descrizioneDettagliata);
                return;
            }
        }
        OrdineCliente nuovo = new OrdineCliente(codiceOrdine, cli, g, pesoKg, dataRitiro);
        nuovo.setStato(st);
        nuovo.setPrezzoTotale(prezzoTotale);
        nuovo.setDescrizioneDettagliata(descrizioneDettagliata);
        ordini.add(nuovo);
    }

    public void sincronizzaRichiestaGustoDaDb(String codiceRich, String emailCliente, String nomeGusto,
                                              String nomeIngr, double quantitaKg, String dataRitiro,
                                              String noteCliente, String stato, String rispostaMaestro) {
        Cliente cli = trovaClientePerEmail(emailCliente);
        if (cli == null) {
            cli = new Cliente("Cliente", emailCliente, emailCliente, "");
        }
        Ingrediente ing = trovaIngredientePerNome(nomeIngr);
        if (ing == null) {
            ing = new Ingrediente(nomeIngr != null ? nomeIngr : "Ingrediente Speciale", 10.0, 2.0, "Kg");
        }
        StatoRichiesta st = StatoRichiesta.IN_ATTESA;
        if (stato != null && !stato.isEmpty()) {
            try {
                st = StatoRichiesta.valueOf(stato.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                st = StatoRichiesta.IN_ATTESA;
            }
        }
        for (RichiestaGustoPersonalizzato r : richiesteGusto) {
            if (r.getCodiceRichiesta().equalsIgnoreCase(codiceRich)) {
                r.setStato(st);
                r.setRispostaMaestro(rispostaMaestro != null ? rispostaMaestro : "");
                return;
            }
        }
        RichiestaGustoPersonalizzato nuova = new RichiestaGustoPersonalizzato(
                codiceRich, cli, nomeGusto, ing, quantitaKg, dataRitiro, noteCliente != null ? noteCliente : "");
        nuova.setStato(st);
        nuova.setRispostaMaestro(rispostaMaestro != null ? rispostaMaestro : "");
        richiesteGusto.add(nuova);
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
        notifiche.add(new Notifica(emailDest, titolo, messaggio, timestamp));
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        return new ArrayList<>(dipendenti);
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        for (Dipendente d : dipendenti) {
            if (d.getEmail().equalsIgnoreCase(email)) {
                return d;
            }
        }
        return null;
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        return new ArrayList<>(clienti);
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        for (Cliente c : clienti) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public List<Turno> caricaTuttiTurni() throws FrostlyException {
        return new ArrayList<>(turni);
    }

    @Override
    public void salvaTurno(Turno turno) throws FrostlyException {
        for (int i = 0; i < turni.size(); i++) {
            if (turni.get(i).getCodiceTurno().equals(turno.getCodiceTurno())) {
                turni.set(i, turno);
                return;
            }
        }
        turni.add(turno);
    }

    @Override
    public List<Emergenza> caricaTutteEmergenze() {
        return new ArrayList<>(emergenze);
    }

    @Override
    public void salvaEmergenza(Emergenza emergenza) {
        for (int i = 0; i < emergenze.size(); i++) {
            if (emergenze.get(i).getCodiceEmergenza().equals(emergenza.getCodiceEmergenza())) {
                emergenze.set(i, emergenza);
                return;
            }
        }
        emergenze.add(emergenza);
    }

    @Override
    public List<Ingrediente> caricaTuttiIngredienti() {
        return new ArrayList<>(ingredienti);
    }

    @Override
    public Ingrediente trovaIngredientePerNome(String nome) {
        for (Ingrediente ing : ingredienti) {
            if (ing.getNome().equalsIgnoreCase(nome)) {
                return ing;
            }
        }
        return null;
    }

    @Override
    public void aggiornaIngrediente(Ingrediente ingrediente) {
        for (int i = 0; i < ingredienti.size(); i++) {
            if (ingredienti.get(i).getNome().equalsIgnoreCase(ingrediente.getNome())) {
                ingredienti.set(i, ingrediente);
                return;
            }
        }
        ingredienti.add(ingrediente);
    }

    @Override
    public List<Gusto> caricaTuttiGusti() {
        return new ArrayList<>(gusti);
    }

    @Override
    public Gusto trovaGustoPerNome(String nome) {
        for (Gusto g : gusti) {
            if (g.getNome().equalsIgnoreCase(nome)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public void salvaGustoMenu(Gusto gusto) throws FrostlyException {
        for (int i = 0; i < gusti.size(); i++) {
            if (gusti.get(i).getNome().equalsIgnoreCase(gusto.getNome())) {
                gusti.set(i, gusto);
                return;
            }
        }
        gusti.add(gusto);
    }

    @Override
    public void rimuoviGustoMenu(String nomeGusto) throws FrostlyException {
        gusti.removeIf(g -> g.getNome().equalsIgnoreCase(nomeGusto));
    }

    @Override
    public List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto() {
        return new ArrayList<>(richiesteGusto);
    }

    @Override
    public void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta) {
        for (int i = 0; i < richiesteGusto.size(); i++) {
            if (richiesteGusto.get(i).getCodiceRichiesta().equals(richiesta.getCodiceRichiesta())) {
                richiesteGusto.set(i, richiesta);
                return;
            }
        }
        richiesteGusto.add(richiesta);
    }

    @Override
    public List<OrdineCliente> caricaTuttiOrdini() {
        return new ArrayList<>(ordini);
    }

    @Override
    public void salvaOrdine(OrdineCliente ordine) {
        for (int i = 0; i < ordini.size(); i++) {
            if (ordini.get(i).getCodiceOrdine().equals(ordine.getCodiceOrdine())) {
                ordini.set(i, ordine);
                return;
            }
        }
        ordini.add(ordine);
    }

    @Override
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        List<Notifica> risultato = new ArrayList<>();
        for (Notifica n : notifiche) {
            if (n.getEmailDestinatario().equalsIgnoreCase(emailDestinatario)) {
                risultato.add(n);
            }
        }
        return risultato;
    }

    @Override
    public void inviaNotifica(Notifica notifica) {
        this.notifiche.add(0, notifica);
    }
}
