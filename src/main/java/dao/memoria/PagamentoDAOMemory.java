package dao.memoria;

import dao.PagamentoDAO;
import frostly.exception.FrostlyException;
import model.TransazionePagamento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementazione in memoria del PagamentoDAO per esecuzione DEMO e test automatici.
 */
public class PagamentoDAOMemory implements PagamentoDAO {

    private static final List<TransazionePagamento> TRANSAZIONI = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void salvaTransazione(TransazionePagamento transazione) throws FrostlyException {
        if (transazione != null) {
            TRANSAZIONI.add(transazione);
        }
    }

    @Override
    public TransazionePagamento cercaPerCodice(String codiceTransazione) throws FrostlyException {
        if (codiceTransazione == null) return null;
        synchronized (TRANSAZIONI) {
            for (TransazionePagamento t : TRANSAZIONI) {
                if (codiceTransazione.equalsIgnoreCase(t.getCodiceTransazione())) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public List<TransazionePagamento> trovaTuttePerCliente(String emailCliente) throws FrostlyException {
        List<TransazionePagamento> res = new ArrayList<>();
        if (emailCliente == null) return res;
        synchronized (TRANSAZIONI) {
            for (TransazionePagamento t : TRANSAZIONI) {
                if (emailCliente.equalsIgnoreCase(t.getEmailCliente())) {
                    res.add(t);
                }
            }
        }
        return res;
    }
}
