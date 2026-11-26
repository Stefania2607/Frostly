# Frostly — Documentazione & Dump del Database (MySQL 8.0+)

Questa cartella contiene lo script DDL e DML completo per l'inizializzazione del database relazionale **MySQL** dell'applicazione **Frostly**, allineato a `SchemaDatabase.java` e ai requisiti del corso ISPW (persistenza **FULL su MySQL**, alternativa **DEMO in-memory** e **FileSystem CSV** per `TurnoDAO`).

---

## File Presente

- **`Frostly_MySQL.sql`**: script SQL ottimizzato per **MySQL Server 8.0+** / **MySQL Workbench**:
  - Creazione del database `frostly_db` con codifica `utf8mb4`.
  - Motore transazionale `ENGINE=InnoDB`.
  - Tutte le 12 tabelle relazionali (inclusi monte ore, limite turni settimanali, disponibilità, turni flessibili con pausa, fabbisogni, richieste indisponibilità e pagamenti).
  - Popolamento iniziale completo (utenti, turni, emergenze, materie prime di magazzino, prodotti della vetrina e recensioni).

---

## Struttura delle Tabelle (Schema Relazionale)

| Tabella | Descrizione | Chiave Primaria |
| :--- | :--- | :--- |
| **`utenti`** | Account (Maestro, Dipendenti, Clienti), monte ore, mansioni, stato attivo, limite max turni e disponibilità | `email` |
| **`turni`** | Pianificazione settimanale staff (turni standard, bloccati, automatici, flessibili personalizzati e pausa) | `codice_turno` |
| **`fabbisogni_turni`** | Fabbisogno minimo di personale e mansioni per giorno e fascia | `chiave` |
| **`richieste_indisponibilita`** | Richieste di ferie/permessi inviate dai dipendenti con approvazione del Maestro | `codice_richiesta` |
| **`emergenze`** | Segnalazioni di indisposizione su turno e richieste di sostituzione | `codice_emergenza` |
| **`ingredienti`** | Dispensa e magazzino con giacenze, soglie minime, unità di misura e categoria | `nome` |
| **`menu_gusti`** | Catalogo vetrina (Gelati, Crepes, Crostate, Cioccolate, Bevande, Caffetteria, Biscotti, Tè/Tisane) | `nome_gusto` |
| **`recensioni`** | Recensioni clienti con stelle, foto opzionale, gradimento e risposta del Maestro | `codice_recensione` |
| **`richieste_gusto`** | Proposte di nuovi gusti su misura inviate dai clienti al Maestro | `codice_richiesta` |
| **`ordini_clienti`** | Ordini ritiro al banco e consegna a domicilio con dettaglio articoli e stato | `codice_ordine` |
| **`notifiche_utenti`** | Registro cronologico avvisi e comunicazioni | `email_destinatario` + `timestamp_notifica` |
| **`pagamenti`** | Transazioni di pagamento online con carta mascherata (solo ultime 4 cifre) | `codice_transazione` |

---

## Istruzioni di Importazione (Modalità FULL — MySQL)

1. Apri **MySQL Workbench** (o DBeaver) e connettiti alla tua istanza MySQL (`localhost:3306`).
2. Esegui lo script `Database/Frostly_MySQL.sql` (in alternativa `SchemaDatabase.java` crea e migra automaticamente tutte le tabelle al primo avvio dell'applicazione).
3. Verifica la configurazione in `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/frostly_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=
   app.mode=FULL
   turni.persistence=DBMS
   ```
4. Inserisci la tua password locale in `db.local.properties` (file escluso da Git) oppure nella variabile d'ambiente `FROSTLY_DB_PASSWORD`.
