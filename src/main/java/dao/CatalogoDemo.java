package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singola sorgente di verita' (Single Source of Truth) per il catalogo prodotti demo di Frostly.
 * Utilizzato sia per il popolamento iniziale del database relazionale (MySQL)
 * sia per l'inizializzazione dell'archivio in-memory (DEMO), eliminando ogni duplicazione.
 */
public final class CatalogoDemo {

    public record ProdottoDemo(
            String nome,
            String descrizione,
            String allergeni,
            double prezzo,
            String categoria,
            String sottocategoria,
            String immagineUrl
    ) {}

    private static final List<ProdottoDemo> PRODOTTI = creaCatalogoDemo();

    private CatalogoDemo() {
    }

    public static List<ProdottoDemo> getProdotti() {
        return Collections.unmodifiableList(PRODOTTI);
    }

    private static List<ProdottoDemo> creaCatalogoDemo() {
        List<ProdottoDemo> lista = new ArrayList<>();

        // Gusti Gelati — Creme
        lista.add(new ProdottoDemo("Pistacchio di Bronte DOP", "Cremoso al pistacchio siciliano tostato", "Latte, Frutta a guscio", 26.0, "Gelati", "Creme", "1793962265284_nascita-gelato-pistacchio.jpg"));
        lista.add(new ProdottoDemo("Fondente Ecuador 75%", "Cioccolato intenso monorigine", "Latte", 24.0, "Gelati", "Cioccolato", "1793962340626_gelato-al-cioccolato-570x380.jpg"));
        lista.add(new ProdottoDemo("Nocciola Piemonte IGP", "Classico cremoso alle nocciole trilobate", "Latte, Frutta a guscio", 25.0, "Gelati", "Creme", "1793962399876_Screenshot_2026-11-06_alle_11.53.13.png"));
        lista.add(new ProdottoDemo("Crema Antica del Maestro", "Tuorli freschi, vaniglia Bourbon e scorza di limone", "Latte, Uova", 22.0, "Gelati", "Creme", "1793962432008_Screenshot_2026-11-06_alle_11.53.44.png"));
        lista.add(new ProdottoDemo("Stracciatella Artigianale", "Fiordilatte fresco con scaglie di fondente croccante", "Latte", 23.0, "Gelati", "Creme", "1793962480783_stracciatella-gelato-recipe-1-1200px.jpg"));

        // Gusti Gelati — Frutta
        lista.add(new ProdottoDemo("Sorbetto Mango Alphonso", "Sorbetto 100% frutta fresca senza latte", "Senza Lattosio", 24.0, "Gelati", "Frutta", "1793962514184_Screenshot_2026-11-06_alle_11.55.07.png"));
        lista.add(new ProdottoDemo("Fragola", "Sorbetto fresco alle fragole mature di bosco", "Senza Lattosio", 22.0, "Gelati", "Frutta", "fragola.png"));
        lista.add(new ProdottoDemo("Menta", "Gelato rinfrescante alla menta piperita del Piemonte", "Latte", 22.0, "Gelati", "Frutta", "menta.png"));
        lista.add(new ProdottoDemo("Limone", "Sorbetto artigianale ai limoni di Sorrento IGP", "Senza Lattosio", 22.0, "Gelati", "Frutta", "limone.png"));
        lista.add(new ProdottoDemo("Melone Bianco", "Sorbetto delicato e profumato al melone bianco invernale", "Senza Lattosio", 23.0, "Gelati", "Frutta", "melone_bianco.png"));
        lista.add(new ProdottoDemo("Lampone", "Sorbetto vellutato ai lamponi freschi di montagna", "Senza Lattosio", 24.0, "Gelati", "Frutta", "lampone.png"));
        lista.add(new ProdottoDemo("Anguria", "Sorbetto dissetante al cuore di anguria estiva", "Senza Lattosio", 22.0, "Gelati", "Frutta", "anguria.png"));
        lista.add(new ProdottoDemo("Liquirizia", "Gelato intenso alla liquirizia pura di Calabria DOP", "Latte", 24.0, "Gelati", "Frutta", "liquirizia.png"));
        lista.add(new ProdottoDemo("Pesca", "Sorbetto morbido alle pesche gialle di vigna", "Senza Lattosio", 22.0, "Gelati", "Frutta", "pesca.png"));
        lista.add(new ProdottoDemo("Albicocca", "Sorbetto solare alle albicocche del Vesuvio", "Senza Lattosio", 22.0, "Gelati", "Frutta", "albicocca.png"));
        lista.add(new ProdottoDemo("Banana", "Cremoso naturale alla banana matura", "Latte", 22.0, "Gelati", "Frutta", "banana.png"));
        lista.add(new ProdottoDemo("Banana al Cioccolato", "Gelato alla banana con variegatura e scaglie di cioccolato fondente", "Latte", 24.0, "Gelati", "Frutta", "banana_cioccolato.png"));

        // Gusti Gelati — Special
        lista.add(new ProdottoDemo("Snickers", "Cremoso alle arachidi con caramello salato, cioccolato al latte e arachidi tostate", "Latte, Arachidi, Frutta a guscio", 26.0, "Gelati", "Special", "snickers.png"));
        lista.add(new ProdottoDemo("Bounty", "Gelato esotico al cocco fresco con scaglie di cioccolato e cocco rapè", "Latte", 25.0, "Gelati", "Special", "bounty.png"));
        lista.add(new ProdottoDemo("Delizia al Limone", "Pan di Spagna bagnato al limoncello con crema vellutata ai limoni d'Amalfi", "Latte, Uova, Glutine", 26.0, "Gelati", "Special", "delizia_limone.png"));
        lista.add(new ProdottoDemo("Nutella", "Cremoso artigianale con abbondante Nutella pura variegata", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "gelato_nutella.png"));
        lista.add(new ProdottoDemo("Galak", "Gelato al cioccolato bianco Galak con croccanti sfoglie di wafer", "Latte, Glutine", 25.0, "Gelati", "Special", "galak.png"));
        lista.add(new ProdottoDemo("Amarandola", "Mandorla di Sicilia variegato con amarene intere sciroppate", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "amarandola.png"));
        lista.add(new ProdottoDemo("Croccante all'Amarena", "Fiordilatte con croccante di mandorle caramellate e variegatura all'amarena", "Latte, Frutta a guscio", 26.0, "Gelati", "Special", "croccante_amarena.png"));
        lista.add(new ProdottoDemo("Pistacchio variegato Cioccolato e Wafer", "Pistacchio tostato variegato con crema al cioccolato e cialde di wafer", "Latte, Frutta a guscio, Glutine", 27.0, "Gelati", "Special", "pistacchio_cioccolato_wafer.png"));
        lista.add(new ProdottoDemo("Cremino Nutella", "Strati vellutati di fiordilatte e nocciola alternati a colate di Nutella", "Latte, Frutta a guscio", 27.0, "Gelati", "Special", "cremino_nutella.png"));
        lista.add(new ProdottoDemo("Mascarpone Amarena e Gentilini", "Crema al mascarpone fresco con amarene brusche e biscotti Osvego Gentilini", "Latte, Uova, Glutine", 27.0, "Gelati", "Special", "mascarpone_amarena_gentilini.png"));
        lista.add(new ProdottoDemo("Caffè", "Gelato cremoso all'espresso 100% Arabica tostato a legna", "Latte", 23.0, "Gelati", "Special", "gelato_caffe.png"));
        lista.add(new ProdottoDemo("Spagnola", "Crema antica e fiordilatte variegati con amarene Fabbri intere", "Latte, Uova", 25.0, "Gelati", "Special", "spagnola.png"));
        lista.add(new ProdottoDemo("Zuppa Inglese", "Crema pasticcera e cacao con pan di Spagna all'Alchermes e savoiardi", "Latte, Uova, Glutine", 26.0, "Gelati", "Special", "zuppa_inglese.png"));

        // Panna
        lista.add(new ProdottoDemo("Panna Fresca 100g", "Coppetta da 100g di panna fresca d'alpeggio montata al momento", "Latte", 2.5, "Panna", "Porzioni", "panna_100g.png"));
        lista.add(new ProdottoDemo("Panna Fresca 200g", "Vaschetta da 200g di panna fresca d'alpeggio montata al momento", "Latte", 4.5, "Panna", "Porzioni", "panna_200g.png"));
        lista.add(new ProdottoDemo("Panna Fresca 300g", "Vaschetta grande da 300g di panna fresca d'alpeggio montata", "Latte", 6.0, "Panna", "Porzioni", "panna_300g.png"));

        // Crepes
        lista.add(new ProdottoDemo("Crepe alla Nutella", "Crepe calda artigianale farcita con abbondante Nutella cremosa", "Glutine, Latte, Frutta a guscio", 5.0, "Crepes", "Farciture", "crepe_nutella.png"));
        lista.add(new ProdottoDemo("Crepe alle Visciole", "Crepe calda farcita con confettura artigianale di visciole brusche", "Glutine, Uova, Latte", 4.5, "Crepes", "Farciture", "crepe_visciole.png"));
        lista.add(new ProdottoDemo("Crepe alla Marmellata", "Crepe calda farcita con confettura vellutata di albicocche", "Glutine, Uova, Latte", 4.5, "Crepes", "Farciture", "crepe_albicocca.png"));
        lista.add(new ProdottoDemo("Crepe Crema Pan di Stelle", "Crepe calda con crema spalmabile Pan di Stelle, panna e stelline croccanti", "Glutine, Latte, Uova, Frutta a guscio", 5.5, "Crepes", "Farciture", "crepe_pandistelle.png"));

        // Bevande — Acqua e Bibite
        lista.add(new ProdottoDemo("Acqua Naturale 500ml", "Acqua minerale naturale fresca 500ml", "Nessuno", 1.5, "Bevande", "Acqua", "acqua_naturale.png"));
        lista.add(new ProdottoDemo("Acqua Frizzante 500ml", "Acqua minerale frizzante fresca 500ml", "Nessuno", 1.5, "Bevande", "Acqua", "acqua_frizzante.png"));
        lista.add(new ProdottoDemo("Coca Cola 33cl", "Lattina classica fresca 33cl", "Nessuno", 3.0, "Bevande", "Bibite", "coca_cola.png"));
        lista.add(new ProdottoDemo("Fanta 33cl", "Lattina all'arancia fresca 33cl", "Nessuno", 3.0, "Bevande", "Bibite", "fanta.png"));

        // Bevande — Succhi Naturali
        lista.add(new ProdottoDemo("Spremuta d'Arancia", "Spremuta fresca di arance rosse di Sicilia spremute al momento", "Nessuno", 4.0, "Bevande", "Succhi Naturali", ""));
        lista.add(new ProdottoDemo("Succo di Mela Biologico", "Succo 100% mela Golden del Trentino, senza zuccheri aggiunti", "Nessuno", 3.5, "Bevande", "Succhi Naturali", ""));
        lista.add(new ProdottoDemo("Succo di Pesca", "Nettare denso di pesche gialle mature di Romagna", "Nessuno", 3.5, "Bevande", "Succhi Naturali", ""));
        lista.add(new ProdottoDemo("Succo all'Albicocca", "Nettare vellutato di albicocche del Vesuvio", "Nessuno", 3.5, "Bevande", "Succhi Naturali", ""));
        lista.add(new ProdottoDemo("Succo ai Frutti di Bosco", "Succo naturale di mirtilli, lamponi e more di montagna", "Nessuno", 3.5, "Bevande", "Succhi Naturali", ""));
        lista.add(new ProdottoDemo("Centrifugato Mela, Carota e Zenzero", "Centrifugato fresco preparato al momento con mela, carota e zenzero", "Nessuno", 4.5, "Bevande", "Succhi Naturali", ""));

        // Caffetteria
        lista.add(new ProdottoDemo("Caffe Espresso", "Miscela 100% arabica tostata lentamente", "Nessuno", 1.2, "Caffetteria", "Caffe", ""));
        lista.add(new ProdottoDemo("Cappuccino", "Espresso con latte fresco montato a vapore", "Latte", 1.8, "Caffetteria", "Caffe", ""));
        lista.add(new ProdottoDemo("Affogato al Caffe", "Gelato fior di latte annegato nell'espresso caldo", "Latte", 4.0, "Caffetteria", "Specialita", ""));
        lista.add(new ProdottoDemo("Caffe Macchiato", "Espresso cremoso con una morbida goccia di latte fresco caldo o freddo", "Latte", 1.3, "Caffetteria", "Caffe", ""));
        lista.add(new ProdottoDemo("Caffe Marocchino", "Espresso con morbida schiuma di latte e spolverata generosa di cacao amaro", "Latte", 1.8, "Caffetteria", "Caffe", ""));
        lista.add(new ProdottoDemo("Caffe Corretto", "Espresso 100% Arabica con goccia di grappa barricata o sambuca artigianale", "Nessuno", 2.0, "Caffetteria", "Caffe", ""));
        lista.add(new ProdottoDemo("Caffe al Ginseng", "Bevanda cremosa ed energizzante all'estratto puro di ginseng naturale", "Latte", 1.8, "Caffetteria", "Specialita", ""));
        lista.add(new ProdottoDemo("Caffe d'Orzo", "Caffe d'orzo tostato biologico in tazza grande, delicato e naturalmente privo di caffeina", "Glutine", 1.6, "Caffetteria", "Caffe", ""));
        lista.add(new ProdottoDemo("Caffe Shakerato", "Espresso doppio montato con ghiaccio e sciroppo di canna, servito in coppa fredda", "Nessuno", 3.5, "Caffetteria", "Specialita", ""));
        lista.add(new ProdottoDemo("Latte Macchiato", "Bicchiere alto di latte fresco intero montato con colata di espresso cremoso", "Latte", 2.2, "Caffetteria", "Specialita", ""));

        // Cioccolate Calde
        lista.add(new ProdottoDemo("Cioccolata Calda Fondente", "Cioccolata calda densa al cacao fondente Ecuador 75% servita con ciuffo di panna", "Latte", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda Bianca", "Vellutata cioccolata calda al cioccolato bianco belga e bacche di vaniglia Bourbon", "Latte", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Latte", "Cremosa cioccolata calda al puro cioccolato al latte d'alpeggio", "Latte", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Gianduia e Nocciole", "Cioccolata calda vellutata al gianduia torinese con granella di Nocciola Piemonte IGP", "Latte, Frutta a guscio", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Peperoncino", "Cioccolata calda fondente 70% con infusione piccante di peperoncino rosso di Calabria", "Latte", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda Arancia e Cannella", "Cioccolata calda profumata alle scorze d'arancia candita di Sicilia e cannella di Ceylon", "Latte", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Pistacchio di Bronte", "Densa cioccolata calda bianca variegata con crema pura di Pistacchio di Bronte DOP", "Latte, Frutta a guscio", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Caramello Salato", "Cioccolata calda al latte con colata di caramello al burro salato di Bretagna", "Latte", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));

        // Biscotti
        lista.add(new ProdottoDemo("Cantucci alle Mandorle", "Biscotti toscani croccanti, ideali per il caffe", "Glutine, Frutta a guscio, Uova", 5.0, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Baci di Dama", "Frollini alla nocciola farciti di cioccolato", "Glutine, Frutta a guscio, Latte", 6.0, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Canestrelli Liguri", "Frollini friabili al profumo di vaniglia e scorza di limone, spolverati di zucchero a velo", "Glutine, Uova, Latte", 5.5, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Occhi di Bue all'Albicocca", "Due friabili dischi di frolla al burro con cuore vellutato di confettura d'albicocche", "Glutine, Uova, Latte", 4.5, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Frollini con Gocce di Cioccolato", "Classici frollini da inzuppo arricchiti con gocce di cioccolato fondente 60%", "Glutine, Uova, Latte", 4.5, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Amaretti Morbidi di Sassello", "Pasticcini di pasta di mandorle dolci e armelline dall'intenso profumo aromatico", "Frutta a guscio, Uova", 6.0, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Zaletti Veneziani", "Biscotti tradizionali con farina di mais fioretto, uvetta sultanina e sentore di limone", "Glutine, Uova, Latte", 5.0, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Ricciarelli di Siena IGP", "Morbidi dolcetti di pasta reale alle mandorle macinate e zucchero a velo", "Frutta a guscio, Uova", 6.5, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Diamanti alla Vaniglia Bourbon", "Biscottini di sablé fine alla vaniglia Bourbon del Madagascar con bordo croccante di zucchero", "Glutine, Uova, Latte", 5.0, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Biscotti Cacao e Sale di Cervia", "Biscotti intensi al cacao monorigine con cristalli croccanti di sale dolce di Cervia", "Glutine, Uova, Latte", 5.5, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Krumiri Artigianali", "I tradizionali biscotti rigati piemontesi dalla tipica forma curva al profumo di vaniglia", "Glutine, Uova, Latte", 5.5, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Frollini alle Nocciole Piemonte", "Biscotti rustici ricchi di farina di Nocciola Piemonte IGP tostata", "Glutine, Frutta a guscio, Uova, Latte", 6.0, "Biscotti", "Secchi", ""));

        // Crostate — Classiche
        lista.add(new ProdottoDemo("Crostata alla Marmellata", "Frolla al burro con confettura di albicocche", "Glutine, Uova, Latte", 2.8, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata Visciole e Ricotta Romana", "Frolla dorata con ricotta fresca di pecora romana DOP e visciole intere brusche", "Glutine, Uova, Latte", 3.5, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata alla Nutella", "Morbida frolla artigianale al burro farcita con un ricco strato di Nutella Ferrero", "Glutine, Latte, Frutta a guscio, Uova", 3.2, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata Crema Pasticcera e Pinoli", "Frolla classica con crema pasticcera al limone e pinoli italiani tostati", "Glutine, Uova, Latte, Frutta a guscio", 3.5, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata Albicocche di Vignola", "Frolla profumata alla scorza d'arancia con confettura extra di albicocche di Vignola", "Glutine, Uova, Latte", 3.0, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata Fichi e Noci", "Frolla rustica ripiena di confettura di fichi bianchi del Cilento e gherigli di noce", "Glutine, Frutta a guscio, Uova, Latte", 3.5, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata Tradizionale al Limone", "Frolla friabile con crema vellutata cotta ai limoni di Sorrento IGP", "Glutine, Uova, Latte", 3.2, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata Pere e Cioccolato Fondente", "Abbinamento classico tra pere Williams spadellate e ganache al cioccolato fondente 70%", "Glutine, Uova, Latte", 3.5, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata Pesche e Amaretti", "Frolla farcita con confettura di pesche nettarine e granella croccante di amaretti", "Glutine, Uova, Latte, Frutta a guscio", 3.2, "Crostate", "Classiche", ""));

        // Crostate — Alla frutta
        lista.add(new ProdottoDemo("Crostata ai Frutti di Bosco", "Crema pasticcera e frutti di bosco freschi", "Glutine, Uova, Latte", 3.2, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Fragoline di Nemi", "Guscio di frolla con crema chantilly e cascata di profumate fragoline di bosco fresche", "Glutine, Uova, Latte", 4.0, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Esotica Mango e Frutto della Passione", "Frolla friabile con bavarese leggera al mango Alphonso e glassa al maracuja", "Glutine, Uova, Latte", 4.0, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Lamponi e Crema Diplomatica", "Crema diplomatica alla vaniglia Bourbon con abbondanti lamponi freschi dell'Alto Adige", "Glutine, Uova, Latte", 4.0, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Mirtilli Neri Selvatici", "Crema pasticcera profumata al limone ricoperta da mirtilli neri selvatici di montagna", "Glutine, Uova, Latte", 3.8, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Agrumi di Sicilia", "Frolla fine con crema curd all'arancia rossa di Sicilia IGP e mandarino tardivo", "Glutine, Uova, Latte", 3.5, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Morbida Mele e Cannella", "Frolla con fettine sottili di mela Golden caramellata, cannella di Ceylon e uvetta", "Glutine, Uova, Latte", 3.2, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata More di Rovo e Ribes", "Frolla con crema leggera e more selvatiche con ribes rosso fresco acidulo", "Glutine, Uova, Latte", 3.8, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Frutti Rossi e Crema Pistacchio", "Crema morbida al pistacchio di Bronte DOP con lamponi, mirtilli e more fresche", "Glutine, Uova, Latte, Frutta a guscio", 4.2, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Kiwi e Crema Pasticcera", "Frolla al burro con crema delicata e fettine fresche di kiwi italiano", "Glutine, Uova, Latte", 3.2, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Crostata Fichi Freschi e Miele", "Frolla arricchita da fichi neri freschi a fette, gocce di miele millefiori e timo limoncino", "Glutine, Uova, Latte", 3.8, "Crostate", "Alla frutta", ""));

        // Crostate — Special
        lista.add(new ProdottoDemo("Crostata Fondente Ecuador e Peperoncino", "Guscio sablé al cacao con ganache densa di cioccolato fondente 75% e infusione piccante", "Glutine, Uova, Latte", 4.2, "Crostate", "Special", ""));
        lista.add(new ProdottoDemo("Crostata Frangipane al Pistacchio di Bronte", "Frolla croccante ripiena di morbida crema frangipane pura al pistacchio di Bronte DOP", "Glutine, Frutta a guscio, Uova, Latte", 4.5, "Crostate", "Special", ""));
        lista.add(new ProdottoDemo("Crostata Cremino Caramello Salato e Noci Pecan", "Strati di ganache al cioccolato al latte, caramello al burro salato e noci pecan tostate", "Glutine, Frutta a guscio, Uova, Latte", 4.2, "Crostate", "Special", ""));
        lista.add(new ProdottoDemo("Crostata Zabaione e Cioccolato Ruby", "Frolla dorata con crema soffice allo zabaione al Marsala e scaglie di cioccolato rosa Ruby", "Glutine, Uova, Latte", 4.5, "Crostate", "Special", ""));
        lista.add(new ProdottoDemo("Crostata Delizia al Limone e Meringa", "Torta moderna con crema curd ai limoni d'Amalfi e soffice cupola di meringa all'italiana fiammeggiata", "Glutine, Uova, Latte", 4.2, "Crostate", "Special", ""));
        lista.add(new ProdottoDemo("Crostata Cheesecake ai Mirtilli e Rosmarino", "Frolla sablé con crema cotta al formaggio fresco, composta di mirtilli e infusione di rosmarino", "Glutine, Uova, Latte", 4.2, "Crostate", "Special", ""));

        // Tisane e Tè suddivisi per categorie: Tè Neri, Tè Verdi & Bianchi, Tisane Benessere, Infusi & Frutta
        // Tè Neri
        lista.add(new ProdottoDemo("Te Nero Earl Grey Bergamotto", "Miscele di tè neri d'alta quota aromatizzati con olio essenziale di bergamotto di Calabria", "Nessuno", 2.8, "Tisane", "Tè Neri", ""));
        lista.add(new ProdottoDemo("Te Nero English Breakfast", "Classico blend corposo e robusto di tè di Ceylon e Assam, perfetto per il mattino", "Nessuno", 2.8, "Tisane", "Tè Neri", ""));
        lista.add(new ProdottoDemo("Te Chai Speziato Indiano", "Tè nero infuso con cannella, cardamomo, chiodi di garofano, pepe nero e zenzero", "Nessuno", 3.0, "Tisane", "Tè Neri", ""));
        lista.add(new ProdottoDemo("Te Nero Darjeeling Grand Cru", "Lo champagne dei tè neri, raccolto ai piedi dell'Himalaya con note moscate", "Nessuno", 3.5, "Tisane", "Tè Neri", ""));

        // Tè Verdi & Bianchi
        lista.add(new ProdottoDemo("Te Verde Matcha Giapponese", "Pregiato tè verde Uji macinato a pietra, ricco di antiossidanti dal gusto erbaceo vellutato", "Nessuno", 3.5, "Tisane", "Tè Verdi & Bianchi", ""));
        lista.add(new ProdottoDemo("Te Bianco Silver Needle", "Gemme apicali primaverili raccolte a mano, delicatissimo con note floreali di gelsomino", "Nessuno", 3.8, "Tisane", "Tè Verdi & Bianchi", ""));
        lista.add(new ProdottoDemo("Te Verde Gunpowder alla Menta", "Tè verde arrotolato in palline infuso con foglie di menta Nanah marocchina", "Nessuno", 2.8, "Tisane", "Tè Verdi & Bianchi", ""));
        lista.add(new ProdottoDemo("Te Verde Jasmine al Gelsomino", "Foglie di tè verde profumate naturalmente durante la notte con boccioli di gelsomino fresco", "Nessuno", 3.0, "Tisane", "Tè Verdi & Bianchi", ""));
        lista.add(new ProdottoDemo("Te Bancha Fiorito", "Tè verde giapponese a basso contenuto di caffeina con fiori di fiordaliso e girasole", "Nessuno", 2.8, "Tisane", "Tè Verdi & Bianchi", ""));
        lista.add(new ProdottoDemo("Te Verde Sencha Makoto", "Tè verde tradizionale a foglia intera dal profilo fresco, vegetale e dissetante", "Nessuno", 3.0, "Tisane", "Tè Verdi & Bianchi", ""));
        lista.add(new ProdottoDemo("Te Oolong Imperiale", "Tè semi-ossidato con note tostate di noce e miele castagno, morbido e persistente", "Nessuno", 3.2, "Tisane", "Tè Verdi & Bianchi", ""));

        // Tisane Benessere
        lista.add(new ProdottoDemo("Tisana Relax Camomilla e Miele", "Fiori di camomilla selvatica setacciata con sentori naturali di miele di zagara e melissa", "Nessuno", 2.8, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Digestiva Finocchio e Anice", "Miscela benefica di semi di finocchio dolce, anice stellato e radice di liquirizia", "Nessuno", 2.8, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Depurativa Tarassaco e Betulla", "Infuso detossinante con foglie di betulla, tarassaco, cardo mariano ed equiseto", "Nessuno", 2.8, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Balsamica Timo ed Eucalipto", "Infusione aromatica e rinfrescante con timo serpillo, foglie di eucalipto e menta", "Nessuno", 2.8, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Vitalita Ginseng e Guarana", "Infuso energizzante e rinvigorente naturale con radice di ginseng coreano e guaranà", "Nessuno", 3.0, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Fiori d'Arancio e Melissa", "Infuso serale rilassante con fiori d'arancio amaro, melissa officinale e lavanda", "Nessuno", 2.8, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Curcuma Zenzero e Cannella", "Infuso dorato speziato ad alto potere antinfiammatorio e digestivo", "Nessuno", 3.0, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Menta Piperita e Liquirizia", "Equilibrio rinfrescante e dolce tra menta piperita alpina e pura radice di liquirizia DOP", "Nessuno", 2.8, "Tisane", "Tisane Benessere", ""));
        lista.add(new ProdottoDemo("Tisana Sogni d'Oro Passiflora e Tiglio", "Infuso calmante serale con fiori di tiglio, passiflora, verbena e melissa", "Nessuno", 2.8, "Tisane", "Tisane Benessere", ""));

        // Infusi & Frutta
        lista.add(new ProdottoDemo("Tisana Zenzero e Limone", "Infuso caldo digestivo e rinfrescante", "Nessuno", 2.5, "Tisane", "Infusi & Frutta", ""));
        lista.add(new ProdottoDemo("Tisana ai Frutti Rossi", "Infuso fruttato senza caffeina", "Nessuno", 2.5, "Tisane", "Infusi & Frutta", ""));
        lista.add(new ProdottoDemo("Tisana Rosa Canina e Ibisco", "Infuso vitaminico dal colore rubino con bacche di rosa canina e fiori di ibisco karkadè", "Nessuno", 2.8, "Tisane", "Infusi & Frutta", ""));
        lista.add(new ProdottoDemo("Tisana Mirtillo e Lavanda", "Infuso rilassante e antiossidante con mirtilli rossi, mirtilli neri e fiori di lavanda", "Nessuno", 3.0, "Tisane", "Infusi & Frutta", ""));
        lista.add(new ProdottoDemo("Te Rooibos Vaniglia e Mandorla", "Infuso rosso sudafricano privo di teina con bacche di vaniglia Bourbon e petali di fiori", "Frutta a guscio", 3.0, "Tisane", "Infusi & Frutta", ""));

        return lista;
    }

    /**
     * Inserisce o aggiorna i prodotti demo all'interno della tabella menu_gusti del DBMS.
     */
    public static void popolaTabellaMenuGusti(Connection conn, Statement stmt) throws SQLException {
        stmt.executeUpdate("DELETE FROM menu_gusti WHERE categoria = 'Crepes' AND sottocategoria = 'Topping'");
        stmt.executeUpdate("DELETE FROM menu_gusti WHERE nome_gusto IN ('Crepe Marmellata di Visciole', 'Crepe Marmellata di Albicocca')");

        String checkSql = "SELECT COUNT(*) FROM menu_gusti WHERE nome_gusto = ?";
        String insertSql = "INSERT INTO menu_gusti (nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria, immagine_url) "
                + "VALUES (?, ?, ?, ?, 1, ?, ?, ?)";
        String updateFotoSql = "UPDATE menu_gusti SET immagine_url = ? WHERE nome_gusto = ? AND (immagine_url IS NULL OR immagine_url = '')";
        String updateSottoSql = "UPDATE menu_gusti SET sottocategoria = ? WHERE nome_gusto = ?";

        try (PreparedStatement psCheck = conn.prepareStatement(checkSql);
             PreparedStatement psIns = conn.prepareStatement(insertSql);
             PreparedStatement psFoto = conn.prepareStatement(updateFotoSql);
             PreparedStatement psSotto = conn.prepareStatement(updateSottoSql)) {

            for (ProdottoDemo p : PRODOTTI) {
                psCheck.setString(1, p.nome());
                boolean esiste = false;
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        esiste = true;
                    }
                }
                if (esiste) {
                    psSotto.setString(1, p.sottocategoria());
                    psSotto.setString(2, p.nome());
                    psSotto.executeUpdate();

                    if (p.immagineUrl() != null && !p.immagineUrl().isEmpty()) {
                        psFoto.setString(1, p.immagineUrl());
                        psFoto.setString(2, p.nome());
                        psFoto.executeUpdate();
                    }
                } else {
                    psIns.setString(1, p.nome());
                    psIns.setString(2, p.descrizione());
                    psIns.setString(3, p.allergeni());
                    psIns.setDouble(4, p.prezzo());
                    psIns.setString(5, p.categoria());
                    psIns.setString(6, p.sottocategoria());
                    psIns.setString(7, p.immagineUrl() == null ? "" : p.immagineUrl());
                    psIns.executeUpdate();
                }
            }
        }
    }
}
