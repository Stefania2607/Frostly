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
 * Utilizzato sia per il popolamento iniziale del database relazionale (DBMS/SQLite)
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
        // Utility class
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

        // Cioccolate Calde
        lista.add(new ProdottoDemo("Cioccolata Calda Fondente", "Cioccolata calda densa al cacao fondente Ecuador 75% servita con ciuffo di panna", "Latte", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda Bianca", "Vellutata cioccolata calda al cioccolato bianco belga e bacche di vaniglia Bourbon", "Latte", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Latte", "Cremosa cioccolata calda al puro cioccolato al latte d'alpeggio", "Latte", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Gianduia e Nocciole", "Cioccolata calda vellutata al gianduia torinese con granella di Nocciola Piemonte IGP", "Latte, Frutta a guscio", 6.5, "Cioccolate calde", "Classiche", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Peperoncino", "Cioccolata calda fondente 70% con infusione piccante di peperoncino rosso di Calabria", "Latte", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda Arancia e Cannella", "Cioccolata calda profumata alle scorze d'arancia candita di Sicilia e cannella di Ceylon", "Latte", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Pistacchio di Bronte", "Densa cioccolata calda bianca variegata con crema pura di Pistacchio di Bronte DOP", "Latte, Frutta a guscio", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));
        lista.add(new ProdottoDemo("Cioccolata Calda al Caramello Salato", "Cioccolata calda al latte con colata di caramello al burro salato di Bretagna", "Latte", 6.5, "Cioccolate calde", "Speziate & Gourmet", ""));

        // Pasticceria e Tisane
        lista.add(new ProdottoDemo("Cantucci alle Mandorle", "Biscotti toscani croccanti, ideali per il caffe", "Glutine, Frutta a guscio, Uova", 5.0, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Baci di Dama", "Frollini alla nocciola farciti di cioccolato", "Glutine, Frutta a guscio, Latte", 6.0, "Biscotti", "Secchi", ""));
        lista.add(new ProdottoDemo("Crostata alla Marmellata", "Frolla al burro con confettura di albicocche", "Glutine, Uova, Latte", 2.8, "Crostate", "Classiche", ""));
        lista.add(new ProdottoDemo("Crostata ai Frutti di Bosco", "Crema pasticcera e frutti di bosco freschi", "Glutine, Uova, Latte", 3.2, "Crostate", "Alla frutta", ""));
        lista.add(new ProdottoDemo("Tisana Zenzero e Limone", "Infuso caldo digestivo e rinfrescante", "Nessuno", 2.5, "Tisane", "Infusi", ""));
        lista.add(new ProdottoDemo("Tisana ai Frutti Rossi", "Infuso fruttato senza caffeina", "Nessuno", 2.5, "Tisane", "Infusi", ""));

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

        try (PreparedStatement psCheck = conn.prepareStatement(checkSql);
             PreparedStatement psIns = conn.prepareStatement(insertSql);
             PreparedStatement psFoto = conn.prepareStatement(updateFotoSql)) {

            for (ProdottoDemo p : PRODOTTI) {
                psCheck.setString(1, p.nome());
                boolean esiste = false;
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        esiste = true;
                    }
                }
                if (esiste) {
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
