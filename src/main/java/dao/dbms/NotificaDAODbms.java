package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.NotificaDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.Notifica;

/**
 * Implementazione DBMS di NotificaDAO.
 */
public class NotificaDAODbms implements NotificaDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getInstance();

    @Override
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        sincronizzaNotificheDaDatabase();
        return cache.caricaNotifichePerUtente(emailDestinatario);
    }

    @Override
    public void inviaNotifica(Notifica notifica) {
        cache.inviaNotifica(notifica);
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            String ins = "INSERT INTO notifiche_utenti (email_destinatario, titolo, messaggio, timestamp_notifica) VALUES (?, ?, ?, ?)";
            try (PreparedStatement iStmt = conn.prepareStatement(ins)) {
                iStmt.setString(1, notifica.getEmailDestinatario());
                iStmt.setString(2, notifica.getTitolo());
                iStmt.setString(3, notifica.getMessaggio());
                iStmt.setString(4, notifica.getTimestamp());
                iStmt.executeUpdate();
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }

    private void sincronizzaNotificheDaDatabase() {
        String sql = "SELECT email_destinatario, titolo, messaggio, timestamp_notifica FROM notifiche_utenti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cache.sincronizzaNotificaDaDb(
                            rs.getString("email_destinatario"),
                            rs.getString("titolo"),
                            rs.getString("messaggio"),
                            rs.getString("timestamp_notifica"));
                }
            }
        } catch (FrostlyException | SQLException ignored) {
        }
    }
}
