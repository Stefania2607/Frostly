package util;

/**
 * Le 2 modalita' ufficiali richieste
 * - DEMO: tutti i dati sono salvati solo IN-MEMORY (nessuna persistenza).
 * - FULL: i dati sono salvati sul layer di persistenza (Database + File System per il DAO in doppia versione).
 */
public enum ModalitaEsecuzione {
    DEMO,
    FULL
}