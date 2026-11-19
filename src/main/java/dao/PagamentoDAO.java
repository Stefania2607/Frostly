package dao;

import frostly.exception.FrostlyException;
import model.TransazionePagamento;

import java.util.List;

/**
 * Interfaccia DAO per la persistenza delle transazioni di pagamento online.
 */
public interface PagamentoDAO {

    void salvaTransazione(TransazionePagamento transazione) throws FrostlyException;

    TransazionePagamento cercaPerCodice(String codiceTransazione) throws FrostlyException;

    List<TransazionePagamento> trovaTuttePerCliente(String emailCliente) throws FrostlyException;
}
