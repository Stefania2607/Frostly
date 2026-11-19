# 🗄️ Frostly — Documentazione & Dump del Database

Questa cartella contiene gli script DDL e DML completi per il popolamento e l'inizializzazione del database relazionale dell'applicazione **Frostly**, strutturati secondo i requisiti del progetto di Ingegneria del Software (conformità RouteX).

---

## 📂 File Presenti

1. **`Frostly_MySQL.sql`**  
   Script SQL ottimizzato per **MySQL Server 8.0+** / **MySQL Workbench**:
   * Creazione automatica del database `frostly_db` con codifica `utf8mb4`.
   * Motore transazionale `ENGINE=InnoDB`.
   * Reset (`DROP TABLE IF EXISTS`), definizione vincoli di chiave primaria e indici.
   * `INSERT INTO` complete di tutti gli utenti, turni, emergenze, 106 materie prime di magazzino, 128 prodotti del menu/vetrina e recensioni verificate.

2. **`Frostly_SQLite.sql`**  
   Script SQL compatibile al 100% con **SQLite 3** per esecuzione immediata locale:
   ```bash
   sqlite3 frostly_db.sqlite < Frostly_SQLite.sql
   ```

---

## 📑 Struttura delle Tabelle (Schema Relazionale)

| Tabella | Descrizione | Chiave Primaria |
|---|---|---|
| **`utenti`** | Account di sistema (Maestro Gelataio, Dipendenti, Clienti) | `email` |
| **`turni`** | Pianificazione oraria settimanale dello staff | `codice_turno` |
| **`emergenze`** | Segnalazioni di malattia/indisposizione e richieste di sostituzione turno | `codice_emergenza` |
| **`ingredienti`** | Dispensa e magazzino con giacenze, soglie minime, unità di misura e macro-categorie | `nome` |
| **`menu_gusti`** | Prodotti della vetrina (Gelati, Crepes, Crostate, Cioccolate, Bevande, Caffetteria, Biscotti, Tè) con prezzi al Kg/porzione | `nome_gusto` |
| **`recensioni`** | Recensioni clienti con punteggio in stelle, gradimento ("Utile") e risposte ufficiali del Maestro | `codice_recensione` |
| **`richieste_gusto`** | Modulo cliente per proporre nuovi gusti personalizzati al Maestro | `codice_richiesta` |
| **`ordini_clienti`** | Storico ordini asporto e delivery con dettaglio articoli composti e stato evasione | `codice_ordine` |
| **`notifiche_utenti`** | Registro avvisi e comunicazioni push dello staff e della clientela | `timestamp_notifica` + `email_destinatario` |

---

## 🚀 Istruzioni di Importazione

### Opzione A: MySQL Workbench (Modalità RouteX FULL)
1. Apri **MySQL Workbench** e connettiti alla tua istanza locale (`localhost:3306`).
2. Vai su **File** ➔ **Open SQL Script...** e seleziona `Database/Frostly_MySQL.sql`.
3. Clicca sull'icona del **Fulmine ⚡** (*Execute*) per creare il database e importare tutte le tabelle e i dati.
4. Nel file `db.properties` del progetto, assicurati che la connessione punti a:
   ```properties
   CONNECTION_URL=jdbc:mysql://localhost:3306/frostly_db
   LOGIN_USER=root
   LOGIN_PASS=tua_password
   ```

### Opzione B: SQLite (Auto-alimentato o tramite file)
Frostly include già il supporto automatico:
* Se avvii l'app normalmente, `SchemaDatabase.java` rileva e auto-inizializza `frostly_db.sqlite`.
* Se preferisci ricrearlo da zero tramite lo script:
  ```bash
  sqlite3 frostly_db.sqlite < Database/Frostly_SQLite.sql
  ```
