package control.applicativo;

import bean.CartaPagamentoBean;
import bean.DatiConsegnaBean;
import bean.EsitoPagamentoBean;
import control.applicativo.validatore.PagamentoValidator;
import dao.PagamentoDAO;
import dao.dbms.PagamentoDAODbms;
import frostly.exception.DatiNonValidiException;
import frostly.exception.InvalidCardException;
import frostly.exception.PagamentoException;
import model.CartaCredito;
import model.TransazionePagamento;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controller Applicativo dedicato alla gestione e simulazione delle transazioni di pagamento online.
 */
public class PagamentoControl {

    private final PagamentoDAO pagamentoDAO;
    private static final DateTimeFormatter FMT_TRX = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final SecureRandom RANDOM = new SecureRandom();

    public PagamentoControl() {
        this(new PagamentoDAODbms());
    }

    public PagamentoControl(PagamentoDAO pagamentoDAO) {
        this.pagamentoDAO = pagamentoDAO;
    }

    /**
     * Elabora e simula la transazione di pagamento online tramite carta di credito/debito.
     *
     * @param cartaBean   Dati della carta inseriti dall'utente
     * @param importo     Importo totale dell'ordine in EUR
     * @param consegna    Dati di consegna (domicilio o asporto)
     * @param emailUtente Email del cliente pagante
     * @return EsitoPagamentoBean con codice autorizzazione e ricevuta
     * @throws InvalidCardException se i dati della carta sono invalidi o scaduti
     * @throws DatiNonValidiException se i dati di consegna sono incompleti
     * @throws PagamentoException se la transazione viene rifiutata dalla banca
     */
    public EsitoPagamentoBean elaboraPagamento(CartaPagamentoBean cartaBean,
                                              double importo,
                                              DatiConsegnaBean consegna,
                                              String emailUtente)
            throws InvalidCardException, DatiNonValidiException, PagamentoException {

        // 1. Validazione dati di consegna / orario
        PagamentoValidator.validaDatiConsegna(consegna);

        // 2. Controllo importo ordine
        if (importo <= 0.0) {
            throw new PagamentoException("L'importo del pagamento deve essere superiore a zero.");
        }
        if (importo > 500.0) {
            throw new PagamentoException("Importo massimo per singola transazione online superato (limite: EUR 500.00).");
        }

        // 3. Validazione strutturale e temporale della carta (Algoritmo Luhn, data scadenza, CVV)
        CartaCredito carta = PagamentoValidator.validaCarta(cartaBean);

        // 4. Simulazione autorizzazione gateway bancario
        // Carte che terminano con 0000 simulano fondi insufficienti
        if (carta.getNumeroCarta().endsWith("0000")) {
            throw new PagamentoException("Transazione rifiutata dall'istituto bancario emittente: Plafond esaurito o fondi insufficienti.");
        }

        // 5. Generazione codice autorizzazione univoco (es. TRX-20261118-123456-A8F2)
        LocalDateTime now = LocalDateTime.now();
        int randomSuffix = 1000 + RANDOM.nextInt(9000);
        String codiceTrx = "TRX-" + now.format(FMT_TRX) + "-" + Integer.toHexString(randomSuffix).toUpperCase(Locale.ROOT);
        String dataOraStr = now.format(FMT_DATA);

        // 6. Registrazione persistente della transazione
        String modalitaStr = (consegna != null) ? consegna.costruisciStringaRiepilogoConsegna() : "Non specificata";
        TransazionePagamento transazione = new TransazionePagamento(
                codiceTrx,
                (emailUtente != null) ? emailUtente : "cliente@frostly.it",
                Math.round(importo * 100.0) / 100.0,
                dataOraStr,
                "AUTORIZZATO",
                carta.getNumeroMascherato(),
                modalitaStr
        );

        try {
            pagamentoDAO.salvaTransazione(transazione);
        } catch (Exception e) {
            // Non blocca il checkout se il DB ha un ritardo momentaneo
            java.util.logging.Logger.getLogger(PagamentoControl.class.getName())
                    .log(java.util.logging.Level.WARNING, "Avviso registrazione transazione: {0}", e.getMessage());
        }

        // 7. Restituzione esito positivo per il controller grafico e la view
        String messaggioSuccesso = String.format(Locale.US,
                "Autorizzazione bancaria concessa. Addebito di EUR %.2f eseguito con successo su %s (%s).",
                importo, carta.getCircuito(), carta.getNumeroMascherato());

        return new EsitoPagamentoBean(
                true,
                codiceTrx,
                messaggioSuccesso,
                importo,
                dataOraStr,
                carta.getNumeroMascherato(),
                carta.getCircuito()
        );
    }
}
