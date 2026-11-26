-- ==========================================================
-- FROSTLY - FULL DATABASE DUMP (MySQL 8.0+)
-- Progetto di Ingegneria del Software - Architettura BCE / RouteX
-- ==========================================================

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

-- DROP + CREATE DATABASE
DROP DATABASE IF EXISTS frostly_db;
CREATE DATABASE frostly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE frostly_db;

-- ----------------------------------------------------------
-- 1. TABELLA: utenti
-- ----------------------------------------------------------
DROP TABLE IF EXISTS utenti;
CREATE TABLE utenti (
    email VARCHAR(64) PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    password VARCHAR(64) NOT NULL,
    ruolo VARCHAR(30) NOT NULL,
    modalita_ore VARCHAR(24),
    ore_minime DOUBLE,
    ore_massime DOUBLE,
    tolleranza_sotto DOUBLE DEFAULT 0,
    tolleranza_sopra DOUBLE DEFAULT 0,
    mansioni VARCHAR(120) DEFAULT 'BANCO',
    attivo BOOLEAN DEFAULT TRUE,
    max_turni_settimanali INT,
    disponibilita_config VARCHAR(1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO utenti (email, nome, cognome, password, ruolo) VALUES
('maestro@frostly.it', 'Stefania', 'Guga', 'admin123', 'MAESTRO_GELATAIO'),
('mario@frostly.it', 'Mario', 'Rossi', 'pass123', 'DIPENDENTE'),
('giulia@frostly.it', 'Giulia', 'Verdi', 'pass123', 'DIPENDENTE'),
('luca@frostly.it', 'Luca', 'Neri', 'pass123', 'DIPENDENTE'),
('elena@cliente.it', 'Elena', 'Conti', 'pass123', 'CLIENTE'),
('marco@cliente.it', 'Marco', 'Fabbri', 'pass123', 'CLIENTE');

-- ----------------------------------------------------------
-- 2. TABELLA: turni
-- ----------------------------------------------------------
DROP TABLE IF EXISTS turni;
CREATE TABLE turni (
    codice_turno VARCHAR(32) PRIMARY KEY,
    data_turno VARCHAR(20) NOT NULL,
    ora_inizio VARCHAR(10) NOT NULL,
    ora_fine VARCHAR(10) NOT NULL,
    email_dipendente VARCHAR(64) NOT NULL,
    stato VARCHAR(20) NOT NULL,
    bloccato BOOLEAN NOT NULL DEFAULT FALSE,
    generato_auto BOOLEAN NOT NULL DEFAULT FALSE,
    personalizzato BOOLEAN NOT NULL DEFAULT FALSE,
    pausa_minuti INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO turni (codice_turno, data_turno, ora_inizio, ora_fine, email_dipendente, stato) VALUES
('TRN-01', 'Lunedi', '08:00', '14:00', 'mario@frostly.it', 'PUBBLICATO'),
('TRN-02', 'Lunedi', '14:00', '20:00', 'giulia@frostly.it', 'PUBBLICATO'),
('TRN-03', 'Martedi', '08:00', '14:00', 'mario@frostly.it', 'IN_EMERGENZA');

-- ----------------------------------------------------------
-- 2B. TABELLA: fabbisogni_turni
-- ----------------------------------------------------------
DROP TABLE IF EXISTS fabbisogni_turni;
CREATE TABLE fabbisogni_turni (
    chiave VARCHAR(16) PRIMARY KEY,
    giorno INT NOT NULL,
    indice_fascia INT NOT NULL,
    totale_richiesto INT NOT NULL,
    min_banco INT NOT NULL DEFAULT 0,
    min_laboratorio INT NOT NULL DEFAULT 0,
    min_responsabile INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 2C. TABELLA: richieste_indisponibilita
-- ----------------------------------------------------------
DROP TABLE IF EXISTS richieste_indisponibilita;
CREATE TABLE richieste_indisponibilita (
    codice_richiesta VARCHAR(32) PRIMARY KEY,
    email_dipendente VARCHAR(64) NOT NULL,
    nome_dipendente VARCHAR(100) NOT NULL,
    data_richiesta VARCHAR(20) NOT NULL,
    motivazione VARCHAR(255) NOT NULL,
    stato VARCHAR(20) NOT NULL,
    nota_maestro VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 3. TABELLA: emergenze
-- ----------------------------------------------------------
DROP TABLE IF EXISTS emergenze;
CREATE TABLE emergenze (
    codice_emergenza VARCHAR(32) PRIMARY KEY,
    codice_turno VARCHAR(32) NOT NULL,
    email_richiedente VARCHAR(64) NOT NULL,
    email_sostituto VARCHAR(64),
    motivazione VARCHAR(255) NOT NULL,
    stato VARCHAR(20) NOT NULL,
    nota_risposta VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO emergenze (codice_emergenza, codice_turno, email_richiedente, email_sostituto, motivazione, stato, nota_risposta) VALUES
('EMG-01', 'TRN-03', 'mario@frostly.it', NULL, 'Visita medica improvvisa martedi mattina', 'IN_ATTESA', NULL);

-- ----------------------------------------------------------
-- 4. TABELLA: ingredienti (Dispensa & Magazzino)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS ingredienti;
CREATE TABLE ingredienti (
    nome VARCHAR(64) PRIMARY KEY,
    quantita_disponibile DOUBLE NOT NULL,
    soglia_minima DOUBLE NOT NULL,
    unita_misura VARCHAR(20) NOT NULL,
    categoria VARCHAR(50) NOT NULL DEFAULT 'Laboratorio & Basi'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO ingredienti (nome, quantita_disponibile, soglia_minima, unita_misura, categoria) VALUES
('Latte Fresco Alta Qualita', 35.00, 10.00, 'Litri', 'Laboratorio & Basi'),
('Panna Fresca d''Alpeggio', 25.00, 8.00, 'Litri', 'Laboratorio & Basi'),
('Zucchero Semolato Italiano', 40.00, 15.00, 'Kg', 'Laboratorio & Basi'),
('Uova Fresche da Allevamento', 60.00, 20.00, 'Pezzi', 'Laboratorio & Basi'),
('Pasta Pistacchio di Bronte DOP', 8.00, 2.50, 'Kg', 'Gelati'),
('Nocciola Piemonte IGP', 7.00, 2.00, 'Kg', 'Gelati'),
('Cioccolato Fondente Ecuador 75%', 10.00, 3.00, 'Kg', 'Gelati'),
('Cacao Amaro Monorigine', 8.00, 2.50, 'Kg', 'Laboratorio & Basi'),
('Mandorle di Avola', 6.00, 2.00, 'Kg', 'Laboratorio & Basi'),
('Polpa di Mango Alphonso', 1.50, 2.00, 'Kg', 'Gelati'),
('Fragole Fresche di Bosco', 8.00, 3.00, 'Kg', 'Gelati'),
('Limoni di Sorrento IGP', 8.00, 2.50, 'Kg', 'Gelati'),
('Farina per Crepes e Dolci', 15.00, 5.00, 'Kg', 'Crepes'),
('Burro Fresco di Latteria', 15.00, 4.00, 'Kg', 'Laboratorio & Basi'),
('Nutella Ferrero', 10.00, 3.00, 'Kg', 'Laboratorio & Basi'),
('Granella di Pistacchio di Bronte DOP', 5.00, 1.50, 'Kg', 'Gelati'),
('Confettura Extra di Albicocche', 10.00, 3.00, 'Kg', 'Laboratorio & Basi'),
('Confettura di Frutti di Bosco', 10.00, 3.00, 'Kg', 'Laboratorio & Basi'),
('Tisana Zenzero e Limone', 40.00, 15.00, 'Bustine', 'Tè e Tisane'),
('Tisana ai Frutti Rossi', 35.00, 15.00, 'Bustine', 'Tè e Tisane'),
('Te Verde Matcha Giapponese', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Nero Earl Grey Bergamotto', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Oolong Imperiale', 25.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Bianco Silver Needle', 20.00, 8.00, 'Bustine', 'Tè e Tisane'),
('Te Verde Gunpowder alla Menta', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Nero English Breakfast', 35.00, 12.00, 'Bustine', 'Tè e Tisane'),
('Te Chai Speziato Indiano', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Rooibos Vaniglia e Mandorla', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Verde Jasmine al Gelsomino', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Nero Darjeeling Grand Cru', 25.00, 8.00, 'Bustine', 'Tè e Tisane'),
('Te Bancha Fiorito', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Te Verde Sencha Makoto', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Tisana Relax Camomilla e Miele', 40.00, 15.00, 'Bustine', 'Tè e Tisane'),
('Tisana Digestiva Finocchio e Anice', 35.00, 12.00, 'Bustine', 'Tè e Tisane'),
('Tisana Depurativa Tarassaco e Betulla', 35.00, 12.00, 'Bustine', 'Tè e Tisane'),
('Tisana Balsamica Timo ed Eucalipto', 35.00, 12.00, 'Bustine', 'Tè e Tisane'),
('Tisana Vitalita Ginseng e Guarana', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Tisana Fiori d''Arancio e Melissa', 35.00, 12.00, 'Bustine', 'Tè e Tisane'),
('Tisana Rosa Canina e Ibisco', 35.00, 12.00, 'Bustine', 'Tè e Tisane'),
('Tisana Curcuma Zenzero e Cannella', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Tisana Menta Piperita e Liquirizia', 35.00, 12.00, 'Bustine', 'Tè e Tisane'),
('Tisana Mirtillo e Lavanda', 30.00, 10.00, 'Bustine', 'Tè e Tisane'),
('Tisana Sogni d''Oro Passiflora e Tiglio', 40.00, 15.00, 'Bustine', 'Tè e Tisane'),
('Cioccolata Calda Fondente', 45.00, 15.00, 'Bustine', 'Cioccolate calde'),
('Cioccolata Calda Bianca', 40.00, 15.00, 'Bustine', 'Cioccolate calde'),
('Cioccolata Calda al Latte', 45.00, 15.00, 'Bustine', 'Cioccolate calde'),
('Cioccolata Calda al Gianduia e Nocciole', 35.00, 12.00, 'Bustine', 'Cioccolate calde'),
('Cioccolata Calda al Peperoncino', 25.00, 10.00, 'Bustine', 'Cioccolate calde'),
('Cioccolata Calda Arancia e Cannella', 30.00, 10.00, 'Bustine', 'Cioccolate calde'),
('Cioccolata Calda al Pistacchio di Bronte', 35.00, 12.00, 'Bustine', 'Cioccolate calde'),
('Cioccolata Calda al Caramello Salato', 30.00, 10.00, 'Bustine', 'Cioccolate calde'),
('Caffe in Grani 100% Arabica', 15.00, 4.00, 'Kg', 'Caffetteria'),
('Caffe al Ginseng Solubile', 5.00, 1.50, 'Kg', 'Caffetteria'),
('Orzo Solubile Tostato', 5.00, 1.50, 'Kg', 'Caffetteria'),
('Liquori per Caffe Corretto (Grappa e Sambuca)', 6.00, 2.00, 'Bottiglie', 'Caffetteria'),
('Acqua Naturale 500ml', 120.00, 30.00, 'Bottiglie', 'Bevande'),
('Acqua Frizzante 500ml', 96.00, 30.00, 'Bottiglie', 'Bevande'),
('Coca Cola 33cl', 60.00, 20.00, 'Bottiglie', 'Bevande'),
('Fanta 33cl', 48.00, 15.00, 'Bottiglie', 'Bevande'),
('Succo di Mela Biologico', 36.00, 12.00, 'Bottiglie', 'Bevande'),
('Succo di Pesca', 36.00, 12.00, 'Bottiglie', 'Bevande'),
('Succo all''Albicocca', 36.00, 12.00, 'Bottiglie', 'Bevande'),
('Succo ai Frutti di Bosco', 36.00, 12.00, 'Bottiglie', 'Bevande'),
('Spremuta d''Arancia', 30.00, 10.00, 'Bottiglie', 'Bevande'),
('Centrifugato Mela, Carota e Zenzero', 25.00, 10.00, 'Bottiglie', 'Bevande'),
('Cantucci alle Mandorle', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Baci di Dama', 30.00, 10.00, 'Porzioni', 'Biscotti'),
('Canestrelli Liguri', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Occhi di Bue all''Albicocca', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Frollini con Gocce di Cioccolato', 30.00, 10.00, 'Porzioni', 'Biscotti'),
('Amaretti Morbidi di Sassello', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Zaletti Veneziani', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Ricciarelli di Siena IGP', 20.00, 6.00, 'Porzioni', 'Biscotti'),
('Diamanti alla Vaniglia Bourbon', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Biscotti Cacao e Sale di Cervia', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Krumiri Artigianali', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Frollini alle Nocciole Piemonte', 25.00, 8.00, 'Porzioni', 'Biscotti'),
('Crostata alla Marmellata', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Visciole e Ricotta Romana', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata alla Nutella', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Crema Pasticcera e Pinoli', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Albicocche di Vignola', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Fichi e Noci', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Tradizionale al Limone', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Pere e Cioccolato Fondente', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Pesche e Amaretti', 15.00, 5.00, 'Porzioni', 'Biscotti'),
('Crostata ai Frutti di Bosco', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Fragoline di Nemi', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Esotica Mango e Frutto della Passione', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Lamponi e Crema Diplomatica', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Mirtilli Neri Selvatici', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Agrumi di Sicilia', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Morbida Mele e Cannella', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata More di Rovo e Ribes', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Frutti Rossi e Crema Pistacchio', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Kiwi e Crema Pasticcera', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Fichi Freschi e Miele', 15.00, 5.00, 'Porzioni', 'Crostate'),
('Crostata Fondente Ecuador e Peperoncino', 12.00, 4.00, 'Porzioni', 'Crostate'),
('Crostata Frangipane al Pistacchio di Bronte', 12.00, 4.00, 'Porzioni', 'Crostate'),
('Crostata Cremino Caramello Salato e Noci Pecan', 12.00, 4.00, 'Porzioni', 'Crostate'),
('Crostata Zabaione e Cioccolato Ruby', 12.00, 4.00, 'Porzioni', 'Crostate'),
('Crostata Delizia al Limone e Meringa', 12.00, 4.00, 'Porzioni', 'Crostate'),
('Crostata Cheesecake ai Mirtilli e Rosmarino', 12.00, 4.00, 'Porzioni', 'Crostate'),
('Panna Fresca 100g', 30.00, 10.00, 'Porzioni', 'Panna'),
('Panna Fresca 200g', 25.00, 8.00, 'Porzioni', 'Panna'),
('Panna Fresca 300g', 20.00, 5.00, 'Porzioni', 'Panna');

-- ----------------------------------------------------------
-- 5. TABELLA: menu_gusti (Catalogo Prodotti & Vetrina)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS menu_gusti;
CREATE TABLE menu_gusti (
    nome_gusto VARCHAR(64) PRIMARY KEY,
    descrizione VARCHAR(255) NOT NULL,
    allergeni VARCHAR(128) NOT NULL,
    prezzo_al_kg DOUBLE NOT NULL,
    disponibile BOOLEAN NOT NULL DEFAULT 1,
    categoria VARCHAR(40) NOT NULL DEFAULT 'Gelati',
    sottocategoria VARCHAR(40) NOT NULL DEFAULT 'Creme',
    immagine_url VARCHAR(500) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO menu_gusti (nome_gusto, descrizione, allergeni, prezzo_al_kg, disponibile, categoria, sottocategoria, immagine_url) VALUES
('Pistacchio di Bronte DOP', 'Cremoso al pistacchio siciliano tostato', 'Latte, Frutta a guscio', 26.00, 1, 'Gelati', 'Creme', '1793962265284_nascita-gelato-pistacchio.jpg'),
('Fondente Ecuador 75%', 'Cioccolato intenso monorigine', 'Latte', 24.00, 1, 'Gelati', 'Cioccolato', '1793962340626_gelato-al-cioccolato-570x380.jpg'),
('Nocciola Piemonte IGP', 'Classico cremoso alle nocciole trilobate', 'Latte, Frutta a guscio', 25.00, 1, 'Gelati', 'Creme', '1793962399876_Screenshot_2026-11-06_alle_11.53.13.png'),
('Crema Antica del Maestro', 'Tuorli freschi, vaniglia Bourbon e scorza di limone', 'Latte, Uova', 22.00, 1, 'Gelati', 'Creme', '1793962432008_Screenshot_2026-11-06_alle_11.53.44.png'),
('Stracciatella Artigianale', 'Fiordilatte fresco con scaglie di fondente croccante', 'Latte', 23.00, 1, 'Gelati', 'Creme', '1793962480783_stracciatella-gelato-recipe-1-1200px.jpg'),
('Sorbetto Mango Alphonso', 'Sorbetto 100% frutta fresca senza latte', 'Senza Lattosio', 24.00, 1, 'Gelati', 'Frutta', '1793962514184_Screenshot_2026-11-06_alle_11.55.07.png'),
('Fragola', 'Sorbetto fresco alle fragole mature di bosco', 'Senza Lattosio', 22.00, 1, 'Gelati', 'Frutta', 'fragola.png'),
('Menta', 'Gelato rinfrescante alla menta piperita del Piemonte', 'Latte', 22.00, 1, 'Gelati', 'Frutta', 'menta.png'),
('Limone', 'Sorbetto artigianale ai limoni di Sorrento IGP', 'Senza Lattosio', 22.00, 1, 'Gelati', 'Frutta', 'limone.png'),
('Melone Bianco', 'Sorbetto delicato e profumato al melone bianco invernale', 'Senza Lattosio', 23.00, 1, 'Gelati', 'Frutta', 'melone_bianco.png'),
('Lampone', 'Sorbetto vellutato ai lamponi freschi di montagna', 'Senza Lattosio', 24.00, 1, 'Gelati', 'Frutta', 'lampone.png'),
('Anguria', 'Sorbetto dissetante al cuore di anguria estiva', 'Senza Lattosio', 22.00, 1, 'Gelati', 'Frutta', 'anguria.png'),
('Liquirizia', 'Gelato intenso alla liquirizia pura di Calabria DOP', 'Latte', 24.00, 1, 'Gelati', 'Frutta', 'liquirizia.png'),
('Pesca', 'Sorbetto morbido alle pesche gialle di vigna', 'Senza Lattosio', 22.00, 1, 'Gelati', 'Frutta', 'pesca.png'),
('Albicocca', 'Sorbetto solare alle albicocche del Vesuvio', 'Senza Lattosio', 22.00, 1, 'Gelati', 'Frutta', 'albicocca.png'),
('Banana', 'Cremoso naturale alla banana matura', 'Latte', 22.00, 1, 'Gelati', 'Frutta', 'banana.png'),
('Banana al Cioccolato', 'Gelato alla banana con variegatura e scaglie di cioccolato fondente', 'Latte', 24.00, 1, 'Gelati', 'Frutta', 'banana_cioccolato.png'),
('Snickers', 'Cremoso alle arachidi con caramello salato, cioccolato al latte e arachidi tostate', 'Latte, Arachidi, Frutta a guscio', 26.00, 1, 'Gelati', 'Special', 'snickers.png'),
('Bounty', 'Gelato esotico al cocco fresco con scaglie di cioccolato e cocco rapè', 'Latte', 25.00, 1, 'Gelati', 'Special', 'bounty.png'),
('Delizia al Limone', 'Pan di Spagna bagnato al limoncello con crema vellutata ai limoni d''Amalfi', 'Latte, Uova, Glutine', 26.00, 1, 'Gelati', 'Special', 'delizia_limone.png'),
('Nutella', 'Cremoso artigianale con abbondante Nutella pura variegata', 'Latte, Frutta a guscio', 26.00, 1, 'Gelati', 'Special', 'gelato_nutella.png'),
('Galak', 'Gelato al cioccolato bianco Galak con croccanti sfoglie di wafer', 'Latte, Glutine', 25.00, 1, 'Gelati', 'Special', 'galak.png'),
('Amarandola', 'Mandorla di Sicilia variegato con amarene intere sciroppate', 'Latte, Frutta a guscio', 26.00, 1, 'Gelati', 'Special', 'amarandola.png'),
('Croccante all''Amarena', 'Fiordilatte con croccante di mandorle caramellate e variegatura all''amarena', 'Latte, Frutta a guscio', 26.00, 1, 'Gelati', 'Special', 'croccante_amarena.png'),
('Pistacchio variegato Cioccolato e Wafer', 'Pistacchio tostato variegato con crema al cioccolato e cialde di wafer', 'Latte, Frutta a guscio, Glutine', 27.00, 1, 'Gelati', 'Special', 'pistacchio_cioccolato_wafer.png'),
('Cremino Nutella', 'Strati vellutati di fiordilatte e nocciola alternati a colate di Nutella', 'Latte, Frutta a guscio', 27.00, 1, 'Gelati', 'Special', 'cremino_nutella.png'),
('Mascarpone Amarena e Gentilini', 'Crema al mascarpone fresco con amarene brusche e biscotti Osvego Gentilini', 'Latte, Uova, Glutine', 27.00, 1, 'Gelati', 'Special', 'mascarpone_amarena_gentilini.png'),
('Caffè', 'Gelato cremoso all''espresso 100% Arabica tostato a legna', 'Latte', 23.00, 1, 'Gelati', 'Special', 'gelato_caffe.png'),
('Spagnola', 'Crema antica e fiordilatte variegati con amarene Fabbri intere', 'Latte, Uova', 25.00, 1, 'Gelati', 'Special', 'spagnola.png'),
('Zuppa Inglese', 'Crema pasticcera e cacao con pan di Spagna all''Alchermes e savoiardi', 'Latte, Uova, Glutine', 26.00, 1, 'Gelati', 'Special', 'zuppa_inglese.png'),
('Panna Fresca 100g', 'Coppetta da 100g di panna fresca d''alpeggio montata al momento', 'Latte', 2.50, 1, 'Panna', 'Porzioni', 'panna_100g.png'),
('Panna Fresca 200g', 'Vaschetta da 200g di panna fresca d''alpeggio montata al momento', 'Latte', 4.50, 1, 'Panna', 'Porzioni', 'panna_200g.png'),
('Panna Fresca 300g', 'Vaschetta grande da 300g di panna fresca d''alpeggio montata', 'Latte', 6.00, 1, 'Panna', 'Porzioni', 'panna_300g.png'),
('Crepe alla Nutella', 'Crepe calda artigianale farcita con abbondante Nutella cremosa', 'Glutine, Latte, Frutta a guscio', 5.00, 1, 'Crepes', 'Farciture', 'crepe_nutella.png'),
('Crepe alle Visciole', 'Crepe calda farcita con confettura artigianale di visciole brusche', 'Glutine, Uova, Latte', 4.50, 1, 'Crepes', 'Farciture', 'crepe_visciole.png'),
('Crepe alla Marmellata', 'Crepe calda farcita con confettura vellutata di albicocche', 'Glutine, Uova, Latte', 4.50, 1, 'Crepes', 'Farciture', 'crepe_albicocca.png'),
('Crepe Crema Pan di Stelle', 'Crepe calda con crema spalmabile Pan di Stelle, panna e stelline croccanti', 'Glutine, Latte, Uova, Frutta a guscio', 5.50, 1, 'Crepes', 'Farciture', 'crepe_pandistelle.png'),
('Acqua Naturale 500ml', 'Acqua minerale naturale fresca 500ml', 'Nessuno', 1.50, 1, 'Bevande', 'Acqua', 'acqua_naturale.png'),
('Acqua Frizzante 500ml', 'Acqua minerale frizzante fresca 500ml', 'Nessuno', 1.50, 1, 'Bevande', 'Acqua', 'acqua_frizzante.png'),
('Coca Cola 33cl', 'Lattina classica fresca 33cl', 'Nessuno', 3.00, 1, 'Bevande', 'Bibite', 'coca_cola.png'),
('Fanta 33cl', 'Lattina all''arancia fresca 33cl', 'Nessuno', 3.00, 1, 'Bevande', 'Bibite', 'fanta.png'),
('Spremuta d''Arancia', 'Spremuta fresca di arance rosse di Sicilia spremute al momento', 'Nessuno', 4.00, 1, 'Bevande', 'Succhi Naturali', ''),
('Succo di Mela Biologico', 'Succo 100% mela Golden del Trentino, senza zuccheri aggiunti', 'Nessuno', 3.50, 1, 'Bevande', 'Succhi Naturali', ''),
('Succo di Pesca', 'Nettare denso di pesche gialle mature di Romagna', 'Nessuno', 3.50, 1, 'Bevande', 'Succhi Naturali', ''),
('Succo all''Albicocca', 'Nettare vellutato di albicocche del Vesuvio', 'Nessuno', 3.50, 1, 'Bevande', 'Succhi Naturali', ''),
('Succo ai Frutti di Bosco', 'Succo naturale di mirtilli, lamponi e more di montagna', 'Nessuno', 3.50, 1, 'Bevande', 'Succhi Naturali', ''),
('Centrifugato Mela, Carota e Zenzero', 'Centrifugato fresco preparato al momento con mela, carota e zenzero', 'Nessuno', 4.50, 1, 'Bevande', 'Succhi Naturali', ''),
('Caffe Espresso', 'Miscela 100% arabica tostata lentamente', 'Nessuno', 1.20, 1, 'Caffetteria', 'Caffe', ''),
('Cappuccino', 'Espresso con latte fresco montato a vapore', 'Latte', 1.80, 1, 'Caffetteria', 'Caffe', ''),
('Affogato al Caffe', 'Gelato fior di latte annegato nell''espresso caldo', 'Latte', 4.00, 1, 'Caffetteria', 'Specialita', ''),
('Caffe Macchiato', 'Espresso cremoso con una morbida goccia di latte fresco caldo o freddo', 'Latte', 1.30, 1, 'Caffetteria', 'Caffe', ''),
('Caffe Marocchino', 'Espresso con morbida schiuma di latte e spolverata generosa di cacao amaro', 'Latte', 1.80, 1, 'Caffetteria', 'Caffe', ''),
('Caffe Corretto', 'Espresso 100% Arabica con goccia di grappa barricata o sambuca artigianale', 'Nessuno', 2.00, 1, 'Caffetteria', 'Caffe', ''),
('Caffe al Ginseng', 'Bevanda cremosa ed energizzante all''estratto puro di ginseng naturale', 'Latte', 1.80, 1, 'Caffetteria', 'Specialita', ''),
('Caffe d''Orzo', 'Caffe d''orzo tostato biologico in tazza grande, delicato e naturalmente privo di caffeina', 'Glutine', 1.60, 1, 'Caffetteria', 'Caffe', ''),
('Caffe Shakerato', 'Espresso doppio montato con ghiaccio e sciroppo di canna, servito in coppa fredda', 'Nessuno', 3.50, 1, 'Caffetteria', 'Specialita', ''),
('Latte Macchiato', 'Bicchiere alto di latte fresco intero montato con colata di espresso cremoso', 'Latte', 2.20, 1, 'Caffetteria', 'Specialita', ''),
('Cioccolata Calda Fondente', 'Cioccolata calda densa al cacao fondente Ecuador 75% servita con ciuffo di panna', 'Latte', 6.50, 1, 'Cioccolate calde', 'Classiche', ''),
('Cioccolata Calda Bianca', 'Vellutata cioccolata calda al cioccolato bianco belga e bacche di vaniglia Bourbon', 'Latte', 6.50, 1, 'Cioccolate calde', 'Classiche', ''),
('Cioccolata Calda al Latte', 'Cremosa cioccolata calda al puro cioccolato al latte d''alpeggio', 'Latte', 6.50, 1, 'Cioccolate calde', 'Classiche', ''),
('Cioccolata Calda al Gianduia e Nocciole', 'Cioccolata calda vellutata al gianduia torinese con granella di Nocciola Piemonte IGP', 'Latte, Frutta a guscio', 6.50, 1, 'Cioccolate calde', 'Classiche', ''),
('Cioccolata Calda al Peperoncino', 'Cioccolata calda fondente 70% con infusione piccante di peperoncino rosso di Calabria', 'Latte', 6.50, 1, 'Cioccolate calde', 'Speziate & Gourmet', ''),
('Cioccolata Calda Arancia e Cannella', 'Cioccolata calda profumata alle scorze d''arancia candita di Sicilia e cannella di Ceylon', 'Latte', 6.50, 1, 'Cioccolate calde', 'Speziate & Gourmet', ''),
('Cioccolata Calda al Pistacchio di Bronte', 'Densa cioccolata calda bianca variegata con crema pura di Pistacchio di Bronte DOP', 'Latte, Frutta a guscio', 6.50, 1, 'Cioccolate calde', 'Speziate & Gourmet', ''),
('Cioccolata Calda al Caramello Salato', 'Cioccolata calda al latte con colata di caramello al burro salato di Bretagna', 'Latte', 6.50, 1, 'Cioccolate calde', 'Speziate & Gourmet', ''),
('Cantucci alle Mandorle', 'Biscotti toscani croccanti, ideali per il caffe', 'Glutine, Frutta a guscio, Uova', 5.00, 1, 'Biscotti', 'Secchi', ''),
('Baci di Dama', 'Frollini alla nocciola farciti di cioccolato', 'Glutine, Frutta a guscio, Latte', 6.00, 1, 'Biscotti', 'Secchi', ''),
('Canestrelli Liguri', 'Frollini friabili al profumo di vaniglia e scorza di limone, spolverati di zucchero a velo', 'Glutine, Uova, Latte', 5.50, 1, 'Biscotti', 'Secchi', ''),
('Occhi di Bue all''Albicocca', 'Due friabili dischi di frolla al burro con cuore vellutato di confettura d''albicocche', 'Glutine, Uova, Latte', 4.50, 1, 'Biscotti', 'Secchi', ''),
('Frollini con Gocce di Cioccolato', 'Classici frollini da inzuppo arricchiti con gocce di cioccolato fondente 60%', 'Glutine, Uova, Latte', 4.50, 1, 'Biscotti', 'Secchi', ''),
('Amaretti Morbidi di Sassello', 'Pasticcini di pasta di mandorle dolci e armelline dall''intenso profumo aromatico', 'Frutta a guscio, Uova', 6.00, 1, 'Biscotti', 'Secchi', ''),
('Zaletti Veneziani', 'Biscotti tradizionali con farina di mais fioretto, uvetta sultanina e sentore di limone', 'Glutine, Uova, Latte', 5.00, 1, 'Biscotti', 'Secchi', ''),
('Ricciarelli di Siena IGP', 'Morbidi dolcetti di pasta reale alle mandorle macinate e zucchero a velo', 'Frutta a guscio, Uova', 6.50, 1, 'Biscotti', 'Secchi', ''),
('Diamanti alla Vaniglia Bourbon', 'Biscottini di sablé fine alla vaniglia Bourbon del Madagascar con bordo croccante di zucchero', 'Glutine, Uova, Latte', 5.00, 1, 'Biscotti', 'Secchi', ''),
('Biscotti Cacao e Sale di Cervia', 'Biscotti intensi al cacao monorigine con cristalli croccanti di sale dolce di Cervia', 'Glutine, Uova, Latte', 5.50, 1, 'Biscotti', 'Secchi', ''),
('Krumiri Artigianali', 'I tradizionali biscotti rigati piemontesi dalla tipica forma curva al profumo di vaniglia', 'Glutine, Uova, Latte', 5.50, 1, 'Biscotti', 'Secchi', ''),
('Frollini alle Nocciole Piemonte', 'Biscotti rustici ricchi di farina di Nocciola Piemonte IGP tostata', 'Glutine, Frutta a guscio, Uova, Latte', 6.00, 1, 'Biscotti', 'Secchi', ''),
('Crostata alla Marmellata', 'Frolla al burro con confettura di albicocche', 'Glutine, Uova, Latte', 2.80, 1, 'Crostate', 'Classiche', ''),
('Crostata Visciole e Ricotta Romana', 'Frolla dorata con ricotta fresca di pecora romana DOP e visciole intere brusche', 'Glutine, Uova, Latte', 3.50, 1, 'Crostate', 'Classiche', ''),
('Crostata alla Nutella', 'Morbida frolla artigianale al burro farcita con un ricco strato di Nutella Ferrero', 'Glutine, Latte, Frutta a guscio, Uova', 3.20, 1, 'Crostate', 'Classiche', ''),
('Crostata Crema Pasticcera e Pinoli', 'Frolla classica con crema pasticcera al limone e pinoli italiani tostati', 'Glutine, Uova, Latte, Frutta a guscio', 3.50, 1, 'Crostate', 'Classiche', ''),
('Crostata Albicocche di Vignola', 'Frolla profumata alla scorza d''arancia con confettura extra di albicocche di Vignola', 'Glutine, Uova, Latte', 3.00, 1, 'Crostate', 'Classiche', ''),
('Crostata Fichi e Noci', 'Frolla rustica ripiena di confettura di fichi bianchi del Cilento e gherigli di noce', 'Glutine, Frutta a guscio, Uova, Latte', 3.50, 1, 'Crostate', 'Classiche', ''),
('Crostata Tradizionale al Limone', 'Frolla friabile con crema vellutata cotta ai limoni di Sorrento IGP', 'Glutine, Uova, Latte', 3.20, 1, 'Crostate', 'Classiche', ''),
('Crostata Pere e Cioccolato Fondente', 'Abbinamento classico tra pere Williams spadellate e ganache al cioccolato fondente 70%', 'Glutine, Uova, Latte', 3.50, 1, 'Crostate', 'Classiche', ''),
('Crostata Pesche e Amaretti', 'Frolla farcita con confettura di pesche nettarine e granella croccante di amaretti', 'Glutine, Uova, Latte, Frutta a guscio', 3.20, 1, 'Crostate', 'Classiche', ''),
('Crostata ai Frutti di Bosco', 'Crema pasticcera e frutti di bosco freschi', 'Glutine, Uova, Latte', 3.20, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Fragoline di Nemi', 'Guscio di frolla con crema chantilly e cascata di profumate fragoline di bosco fresche', 'Glutine, Uova, Latte', 4.00, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Esotica Mango e Frutto della Passione', 'Frolla friabile con bavarese leggera al mango Alphonso e glassa al maracuja', 'Glutine, Uova, Latte', 4.00, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Lamponi e Crema Diplomatica', 'Crema diplomatica alla vaniglia Bourbon con abbondanti lamponi freschi dell''Alto Adige', 'Glutine, Uova, Latte', 4.00, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Mirtilli Neri Selvatici', 'Crema pasticcera profumata al limone ricoperta da mirtilli neri selvatici di montagna', 'Glutine, Uova, Latte', 3.80, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Agrumi di Sicilia', 'Frolla fine con crema curd all''arancia rossa di Sicilia IGP e mandarino tardivo', 'Glutine, Uova, Latte', 3.50, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Morbida Mele e Cannella', 'Frolla con fettine sottili di mela Golden caramellata, cannella di Ceylon e uvetta', 'Glutine, Uova, Latte', 3.20, 1, 'Crostate', 'Alla frutta', ''),
('Crostata More di Rovo e Ribes', 'Frolla con crema leggera e more selvatiche con ribes rosso fresco acidulo', 'Glutine, Uova, Latte', 3.80, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Frutti Rossi e Crema Pistacchio', 'Crema morbida al pistacchio di Bronte DOP con lamponi, mirtilli e more fresche', 'Glutine, Uova, Latte, Frutta a guscio', 4.20, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Kiwi e Crema Pasticcera', 'Frolla al burro con crema delicata e fettine fresche di kiwi italiano', 'Glutine, Uova, Latte', 3.20, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Fichi Freschi e Miele', 'Frolla arricchita da fichi neri freschi a fette, gocce di miele millefiori e timo limoncino', 'Glutine, Uova, Latte', 3.80, 1, 'Crostate', 'Alla frutta', ''),
('Crostata Fondente Ecuador e Peperoncino', 'Guscio sablé al cacao con ganache densa di cioccolato fondente 75% e infusione piccante', 'Glutine, Uova, Latte', 4.20, 1, 'Crostate', 'Special', ''),
('Crostata Frangipane al Pistacchio di Bronte', 'Frolla croccante ripiena di morbida crema frangipane pura al pistacchio di Bronte DOP', 'Glutine, Frutta a guscio, Uova, Latte', 4.50, 1, 'Crostate', 'Special', ''),
('Crostata Cremino Caramello Salato e Noci Pecan', 'Strati di ganache al cioccolato al latte, caramello al burro salato e noci pecan tostate', 'Glutine, Frutta a guscio, Uova, Latte', 4.20, 1, 'Crostate', 'Special', ''),
('Crostata Zabaione e Cioccolato Ruby', 'Frolla dorata con crema soffice allo zabaione al Marsala e scaglie di cioccolato rosa Ruby', 'Glutine, Uova, Latte', 4.50, 1, 'Crostate', 'Special', ''),
('Crostata Delizia al Limone e Meringa', 'Torta moderna con crema curd ai limoni d''Amalfi e soffice cupola di meringa all''italiana fiammeggiata', 'Glutine, Uova, Latte', 4.20, 1, 'Crostate', 'Special', ''),
('Crostata Cheesecake ai Mirtilli e Rosmarino', 'Frolla sablé con crema cotta al formaggio fresco, composta di mirtilli e infusione di rosmarino', 'Glutine, Uova, Latte', 4.20, 1, 'Crostate', 'Special', ''),
('Te Nero Earl Grey Bergamotto', 'Miscele di tè neri d''alta quota aromatizzati con olio essenziale di bergamotto di Calabria', 'Nessuno', 2.80, 1, 'Tisane', 'Tè Neri', ''),
('Te Nero English Breakfast', 'Classico blend corposo e robusto di tè di Ceylon e Assam, perfetto per il mattino', 'Nessuno', 2.80, 1, 'Tisane', 'Tè Neri', ''),
('Te Chai Speziato Indiano', 'Tè nero infuso con cannella, cardamomo, chiodi di garofano, pepe nero e zenzero', 'Nessuno', 3.00, 1, 'Tisane', 'Tè Neri', ''),
('Te Nero Darjeeling Grand Cru', 'Lo champagne dei tè neri, raccolto ai piedi dell''Himalaya con note moscate', 'Nessuno', 3.50, 1, 'Tisane', 'Tè Neri', ''),
('Te Verde Matcha Giapponese', 'Pregiato tè verde Uji macinato a pietra, ricco di antiossidanti dal gusto erbaceo vellutato', 'Nessuno', 3.50, 1, 'Tisane', 'Tè Verdi & Bianchi', ''),
('Te Bianco Silver Needle', 'Gemme apicali primaverili raccolte a mano, delicatissimo con note floreali di gelsomino', 'Nessuno', 3.80, 1, 'Tisane', 'Tè Verdi & Bianchi', ''),
('Te Verde Gunpowder alla Menta', 'Tè verde arrotolato in palline infuso con foglie di menta Nanah marocchina', 'Nessuno', 2.80, 1, 'Tisane', 'Tè Verdi & Bianchi', ''),
('Te Verde Jasmine al Gelsomino', 'Foglie di tè verde profumate naturalmente durante la notte con boccioli di gelsomino fresco', 'Nessuno', 3.00, 1, 'Tisane', 'Tè Verdi & Bianchi', ''),
('Te Bancha Fiorito', 'Tè verde giapponese a basso contenuto di caffeina con fiori di fiordaliso e girasole', 'Nessuno', 2.80, 1, 'Tisane', 'Tè Verdi & Bianchi', ''),
('Te Verde Sencha Makoto', 'Tè verde tradizionale a foglia intera dal profilo fresco, vegetale e dissetante', 'Nessuno', 3.00, 1, 'Tisane', 'Tè Verdi & Bianchi', ''),
('Te Oolong Imperiale', 'Tè semi-ossidato con note tostate di noce e miele castagno, morbido e persistente', 'Nessuno', 3.20, 1, 'Tisane', 'Tè Verdi & Bianchi', ''),
('Tisana Relax Camomilla e Miele', 'Fiori di camomilla selvatica setacciata con sentori naturali di miele di zagara e melissa', 'Nessuno', 2.80, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Digestiva Finocchio e Anice', 'Miscela benefica di semi di finocchio dolce, anice stellato e radice di liquirizia', 'Nessuno', 2.80, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Depurativa Tarassaco e Betulla', 'Infuso detossinante con foglie di betulla, tarassaco, cardo mariano ed equiseto', 'Nessuno', 2.80, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Balsamica Timo ed Eucalipto', 'Infusione aromatica e rinfrescante con timo serpillo, foglie di eucalipto e menta', 'Nessuno', 2.80, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Vitalita Ginseng e Guarana', 'Infuso energizzante e rinvigorente naturale con radice di ginseng coreano e guaranà', 'Nessuno', 3.00, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Fiori d''Arancio e Melissa', 'Infuso serale rilassante con fiori d''arancio amaro, melissa officinale e lavanda', 'Nessuno', 2.80, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Curcuma Zenzero e Cannella', 'Infuso dorato speziato ad alto potere antinfiammatorio e digestivo', 'Nessuno', 3.00, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Menta Piperita e Liquirizia', 'Equilibrio rinfrescante e dolce tra menta piperita alpina e pura radice di liquirizia DOP', 'Nessuno', 2.80, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Sogni d''Oro Passiflora e Tiglio', 'Infuso calmante serale con fiori di tiglio, passiflora, verbena e melissa', 'Nessuno', 2.80, 1, 'Tisane', 'Tisane Benessere', ''),
('Tisana Zenzero e Limone', 'Infuso caldo digestivo e rinfrescante', 'Nessuno', 2.50, 1, 'Tisane', 'Infusi & Frutta', ''),
('Tisana ai Frutti Rossi', 'Infuso fruttato senza caffeina', 'Nessuno', 2.50, 1, 'Tisane', 'Infusi & Frutta', ''),
('Tisana Rosa Canina e Ibisco', 'Infuso vitaminico dal colore rubino con bacche di rosa canina e fiori di ibisco karkadè', 'Nessuno', 2.80, 1, 'Tisane', 'Infusi & Frutta', ''),
('Tisana Mirtillo e Lavanda', 'Infuso rilassante e antiossidante con mirtilli rossi, mirtilli neri e fiori di lavanda', 'Nessuno', 3.00, 1, 'Tisane', 'Infusi & Frutta', ''),
('Te Rooibos Vaniglia e Mandorla', 'Infuso rosso sudafricano privo di teina con bacche di vaniglia Bourbon e petali di fiori', 'Frutta a guscio', 3.00, 1, 'Tisane', 'Infusi & Frutta', '');

-- ----------------------------------------------------------
-- 6. TABELLA: recensioni
-- ----------------------------------------------------------
DROP TABLE IF EXISTS recensioni;
CREATE TABLE recensioni (
    codice_recensione VARCHAR(32) PRIMARY KEY,
    autore_email VARCHAR(64) NOT NULL,
    autore_nome VARCHAR(64) NOT NULL,
    voto_stelle INT NOT NULL,
    titolo VARCHAR(128) NOT NULL,
    commento VARCHAR(1000) NOT NULL,
    data_ora VARCHAR(32) NOT NULL,
    risposta_maestro VARCHAR(1000),
    data_risposta VARCHAR(32),
    mi_piace INT NOT NULL DEFAULT 0,
    foto_path VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO recensioni (codice_recensione, autore_email, autore_nome, voto_stelle, titolo, commento, data_ora, risposta_maestro, data_risposta, mi_piace) VALUES
('REC-01', 'elena@cliente.it', 'Elena Conti', 5, 'Pistacchio di Bronte e panna insuperabili!', 'La consistenza del pistacchio di Bronte DOP è puro velluto, e la panna fresca d''alpeggio è la perfezione assoluta. Il miglior gelato di Roma!', '08/11/2026 16:30', 'Cara Elena, grazie di cuore per le splendide parole! La tostatura lenta del pistacchio è la nostra passione dal 1984. A presto! - Stefania', '08/11/2026 18:00', 14),
('REC-02', 'marco@cliente.it', 'Marco Fabbri', 5, 'Cioccolata calda fondente sublime', 'Ho provato la nuova cioccolata calda fondente con panna: densa, profumata e avvolgente. Servizio impeccabile e atmosfera calda.', '09/11/2026 17:15', 'Grazie Marco! Selezioniamo solo cacao monorigine 70% dell''Ecuador. Ti aspettiamo per le altre specialità invernali!', '09/11/2026 19:20', 9),
('REC-03', 'elena@cliente.it', 'Elena Conti', 5, 'Crepe alla Nutella e panna d''asporto top', 'Le crepes calde preparate al momento sono fragranti e farcite generosamente. Ottima anche la consegna a domicilio super puntuale!', '10/11/2026 09:45', NULL, NULL, 6);

-- ----------------------------------------------------------
-- 7. TABELLA: richieste_gusto
-- ----------------------------------------------------------
DROP TABLE IF EXISTS richieste_gusto;
CREATE TABLE richieste_gusto (
    codice_richiesta VARCHAR(32) PRIMARY KEY,
    email_cliente VARCHAR(64) NOT NULL,
    nome_gusto VARCHAR(64) NOT NULL,
    ingrediente_principale VARCHAR(64) NOT NULL,
    quantita_kg DOUBLE NOT NULL,
    data_ritiro VARCHAR(20) NOT NULL,
    note_cliente VARCHAR(255),
    stato VARCHAR(20) NOT NULL,
    risposta_maestro VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 8. TABELLA: ordini_clienti
-- ----------------------------------------------------------
DROP TABLE IF EXISTS ordini_clienti;
CREATE TABLE ordini_clienti (
    codice_ordine VARCHAR(32) PRIMARY KEY,
    email_cliente VARCHAR(64) NOT NULL,
    gusto_principale VARCHAR(128),
    peso_kg DOUBLE NOT NULL,
    data_ritiro VARCHAR(512) NOT NULL,
    stato VARCHAR(20) NOT NULL,
    prezzo_totale DOUBLE NOT NULL,
    descrizione_dettagliata VARCHAR(2000) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 9. TABELLA: notifiche_utenti
-- ----------------------------------------------------------
DROP TABLE IF EXISTS notifiche_utenti;
CREATE TABLE notifiche_utenti (
    email_destinatario VARCHAR(64) NOT NULL,
    titolo VARCHAR(128) NOT NULL,
    messaggio VARCHAR(1000) NOT NULL,
    timestamp_notifica VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 10. TABELLA: pagamenti
-- ----------------------------------------------------------
DROP TABLE IF EXISTS pagamenti;
CREATE TABLE pagamenti (
    codice_transazione VARCHAR(64) PRIMARY KEY,
    email_cliente VARCHAR(64) NOT NULL,
    importo DOUBLE NOT NULL,
    data_ora VARCHAR(32) NOT NULL,
    stato VARCHAR(20) NOT NULL,
    carta_mascherata VARCHAR(30) NOT NULL,
    modalita_consegna VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
