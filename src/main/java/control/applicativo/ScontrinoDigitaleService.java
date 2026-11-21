package control.applicativo;

import bean.ScontrinoBean;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Servizio applicativo leggero (Singleton) che espone gli scontrini fiscali digitali su rete locale (HTTP).
 */
public final class ScontrinoDigitaleService {

    private static ScontrinoDigitaleService instance;

    private final Map<String, ScontrinoBean> archivioScontrini = new ConcurrentHashMap<>();
    private HttpServer server;
    private int portaAttiva = -1;
    private Function<ScontrinoBean, String> htmlRenderer;

    private ScontrinoDigitaleService() {
    }

    public static synchronized ScontrinoDigitaleService getInstance() {
        if (instance == null) {
            instance = new ScontrinoDigitaleService();
        }
        return instance;
    }

    /**
     * Registra uno scontrino e restituisce l'URL raggiungibile dallo smartphone sulla stessa rete Wi-Fi.
     */
    public synchronized String registraScontrino(ScontrinoBean scontrino, Function<ScontrinoBean, String> renderer) {
        if (scontrino == null) {
            return null;
        }
        if (renderer != null) {
            this.htmlRenderer = renderer;
        }
        avviaServerSeNecessario();
        String chiave = (scontrino.getCodiceTransazione() != null && !scontrino.getCodiceTransazione().isBlank())
                ? scontrino.getCodiceTransazione().trim()
                : ("DOC-" + System.currentTimeMillis());
        archivioScontrini.put(chiave, scontrino);

        String host = rilevaIpLocaleLan();
        String url = "http://" + host + ":" + portaAttiva + "/scontrino/" + chiave;
        scontrino.setUrlScontrino(url);
        return url;
    }

    private void avviaServerSeNecessario() {
        if (server != null) {
            return;
        }
        try {
            server = creaServerSuPortaDisponibile();
            server.createContext("/scontrino/", this::gestisciRichiestaScontrino);
            server.setExecutor(Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "frostly-scontrino-http");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            portaAttiva = server.getAddress().getPort();
        } catch (IOException e) {
            portaAttiva = -1;
        }
    }

    private HttpServer creaServerSuPortaDisponibile() throws IOException {
        try {
            return HttpServer.create(new InetSocketAddress("0.0.0.0", 8765), 0);
        } catch (IOException occupata) {
            return HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
        }
    }

    private void gestisciRichiestaScontrino(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String id = path.replaceFirst("^/scontrino/", "").trim();
        ScontrinoBean s = archivioScontrini.get(id);

        byte[] body;
        int status;
        if (s == null || htmlRenderer == null) {
            status = 404;
            body = "<html><body style='font-family:sans-serif;text-align:center;padding:40px;'><h2>Scontrino non trovato</h2></body></html>"
                    .getBytes(StandardCharsets.UTF_8);
        } else {
            status = 200;
            body = htmlRenderer.apply(s).getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private String rilevaIpLocaleLan() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // Fallback localhost
        }
        return "localhost";
    }
}
