package dao.dbms;

import dao.ConnectionManager;
import dao.PagamentoDAO;
import frostly.exception.FrostlyException;
import model.TransazionePagamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione DBMS (MySQL) di PagamentoDAO con query parametrizzate.
 */
public class PagamentoDAODbms implements PagamentoDAO {

    @Override
    public void salvaTransazione(TransazionePagamento t) throws FrostlyException {
        if (t == null) {
            return;
        }
        String sql = "INSERT INTO pagamenti (codice_transazione, email_cliente, importo, data_ora, stato, carta_mascherata, modalita_consegna) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getCodiceTransazione());
            ps.setString(2, t.getEmailCliente());
            ps.setDouble(3, t.getImporto());
            ps.setString(4, t.getDataOra());
            ps.setString(5, t.getStato());
            ps.setString(6, t.getCartaMascherata());
            ps.setString(7, t.getModalitaConsegna());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel salvataggio della transazione di pagamento.", e);
        }
    }

    @Override
    public TransazionePagamento cercaPerCodice(String codiceTransazione) throws FrostlyException {
        if (codiceTransazione == null) {
            return null;
        }
        String sql = "SELECT codice_transazione, email_cliente, importo, data_ora, stato, carta_mascherata, modalita_consegna "
                + "FROM pagamenti WHERE codice_transazione = ?";
        Connection conn = ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceTransazione);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TransazionePagamento(
                            rs.getString("codice_transazione"),
                            rs.getString("email_cliente"),
                            rs.getDouble("importo"),
                            rs.getString("data_ora"),
                            rs.getString("stato"),
                            rs.getString("carta_mascherata"),
                            rs.getString("modalita_consegna")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel recupero della transazione di pagamento.", e);
        }
    }

    @Override
    public List<TransazionePagamento> trovaTuttePerCliente(String emailCliente) throws FrostlyException {
        List<TransazionePagamento> list = new ArrayList<>();
        if (emailCliente == null) {
            return list;
        }
        String sql = "SELECT codice_transazione, email_cliente, importo, data_ora, stato, carta_mascherata, modalita_consegna "
                + "FROM pagamenti WHERE email_cliente = ? ORDER BY data_ora DESC";
        Connection conn = ConnectionManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransazionePagamento(
                            rs.getString("codice_transazione"),
                            rs.getString("email_cliente"),
                            rs.getDouble("importo"),
                            rs.getString("data_ora"),
                            rs.getString("stato"),
                            rs.getString("carta_mascherata"),
                            rs.getString("modalita_consegna")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nel recupero delle transazioni del cliente.", e);
        }
    }
}
