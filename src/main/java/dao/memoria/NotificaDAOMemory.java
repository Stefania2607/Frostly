package dao.memoria;

import dao.ArchivioMemoria;
import dao.NotificaDAO;
import java.util.List;
import model.Notifica;

/**
 * Implementazione in-memory di NotificaDAO.
 */
public class NotificaDAOMemory implements NotificaDAO {

    private final ArchivioMemoria archivio = ArchivioMemoria.getInstance();

    @Override
    public List<Notifica> caricaNotifichePerUtente(String emailDestinatario) {
        return archivio.caricaNotifichePerUtente(emailDestinatario);
    }

    @Override
    public void inviaNotifica(Notifica notifica) {
        archivio.inviaNotifica(notifica);
    }
}
