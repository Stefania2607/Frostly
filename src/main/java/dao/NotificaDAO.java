package dao;

import java.util.List;
import model.Notifica;

/**
 * Interfaccia DAO dedicata esclusivamente alle notifiche e comunicazioni di servizio.
 */
public interface NotificaDAO {

    List<Notifica> caricaNotifichePerUtente(String emailDestinatario);

    void inviaNotifica(Notifica notifica);
}
