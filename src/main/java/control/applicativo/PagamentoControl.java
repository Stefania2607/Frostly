package control.applicativo;

import bean.CartaPagamentoBean;
import bean.DatiConsegnaBean;
import bean.EsitoPagamentoBean;
import dao.DAOFactory;
import dao.PagamentoDAO;
import frostly.exception.DatiNonValidiException;
import frostly.exception.InvalidCardException;
import frostly.exception.PagamentoException;
import model.CartaCredito;
import model.TransazionePagamento;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controller Applicativo dedicato alla gestione e simulazione delle transazioni di pagamento online.
 * Riceve dati gia' validati sintatticamente dal livello di presentazione/Bean e applica
 * le regole di business (limiti importo, validita' temporale carta, esito banca e persistenza).
 */
public class PagamentoControl {

    private final PagamentoDAO pagamentoDAO;
    private static final DateTimeFormatter FMT_TRX = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final SecureRandom RANDOM = new SecureRandom();

    public PagamentoControl() {
        this(DAOFactory.getPagamentoDAO());
    }

    public PagamentoControl(PagamentoDAO pagamentoDAO) {
        this.pagamentoDAO = pagamentoDAO;
    }

    public EsitoPagamentoBean elaboraPagamento(CartaPagamentoBean cartaBean,
                                               double importo,
                                               DatiConsegnaBean consegna,
                                               String emailUtente)
            throws InvalidCardException, DatiNonValidiException, PagamentoException {

        if (consegna == null) {
            throw new DatiNonValidiException("Dati di consegna non specificati.");
        }
        if (cartaBean == null) {
            throw new InvalidCardException("Dati della carta mancanti.");
        }

        // Regola di business: limiti di importo per singola transazione
        if (importo <= 0.0) {
            throw new PagamentoException("L'importo del pagamento deve essere superiore a zero.");
        }
        if (importo > 500.0) {
            throw new PagamentoException("Importo massimo per singola transazione online superato (limite: EUR 500.00).");
        }

        // Regola di business: validita' temporale della carta (gia' parsata in mese/anno dal Bean)
        int mese = cartaBean.getMeseScadenza();
        int anno = cartaBean.getAnnoScadenza();
        if (mese < 1 || mese > 12 || anno <= 0) {
            throw new InvalidCardException("Data di scadenza della carta non valida.");
        }

        CartaCredito carta = new CartaCredito(
                cartaBean.getNumeroCartaPulito(),
                cartaBean.getTitolare(),
                mese,
                anno,
                cartaBean.getCvv()
        );
        if (carta.isScaduta(LocalDate.now())) {
            throw new InvalidCardException(String.format("La carta è scaduta nel mese %02d/%d.", mese, anno));
        }
        if (YearMonth.of(anno, mese).isAfter(YearMonth.now().plusYears(15))) {
            throw new InvalidCardException("Data di scadenza non verosimile (oltre 15 anni nel futuro).");
        }

        // Simulazione autorizzazione bancaria (carte che terminano con 0000 rifiutate)
        if (carta.getNumeroCarta().endsWith("0000")) {
            throw new PagamentoException("Transazione rifiutata dall'istituto bancario emittente: Plafond esaurito o fondi insufficienti.");
        }

        LocalDateTime now = LocalDateTime.now();
        int randomSuffix = 1000 + RANDOM.nextInt(9000);
        String codiceTrx = "TRX-" + now.format(FMT_TRX) + "-" + Integer.toHexString(randomSuffix).toUpperCase(Locale.ROOT);
        String dataOraStr = now.format(FMT_DATA);

        String modalitaStr = consegna.costruisciStringaRiepilogoConsegna();
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
            java.util.logging.Logger.getLogger(PagamentoControl.class.getName())
                    .log(java.util.logging.Level.WARNING, "Avviso registrazione transazione: {0}", e.getMessage());
        }

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
