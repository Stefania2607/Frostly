package control.applicativo;

import bean.StatisticheBean;
import dao.DAOFactory;
import dao.IngredienteDAO;
import dao.OrdineDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Ingrediente;
import model.OrdineCliente;
import model.StatoRichiesta;

/**
 * Controller applicativo che elabora le statistiche di vendita e lo stato del magazzino
 * a partire dai dati salvati nei DAO.
 */
public class StatisticheControl {

    private static final Pattern PATTERN_QTA = Pattern.compile("^(\\d+)x\\s+(\\S.*)$");

    private final OrdineDAO ordineDAO;
    private final IngredienteDAO ingredienteDAO;

    public StatisticheControl() {
        this(DAOFactory.getOrdineDAO(), DAOFactory.getIngredienteDAO());
    }

    public StatisticheControl(OrdineDAO ordineDAO, IngredienteDAO ingredienteDAO) {
        this.ordineDAO = ordineDAO;
        this.ingredienteDAO = ingredienteDAO;
    }

    public StatisticheBean calcolaStatistiche() {
        StatisticheBean stats = new StatisticheBean();
        calcolaMetricheOrdini(stats);
        calcolaMetricheMagazzino(stats);
        return stats;
    }

    private void calcolaMetricheOrdini(StatisticheBean stats) {
        List<OrdineCliente> ordini = ordineDAO != null ? ordineDAO.caricaTuttiOrdini() : List.of();
        double incasso = 0.0;
        int confermati = 0;
        int inAttesa = 0;
        int disdetti = 0;
        int domicilio = 0;
        int banco = 0;
        Map<String, Integer> conteggioProdotti = new HashMap<>();

        for (OrdineCliente oc : ordini) {
            StatoRichiesta stato = oc.getStato();
            if (stato == StatoRichiesta.RIFIUTATA) {
                disdetti++;
                continue;
            }
            if (stato == StatoRichiesta.IN_ATTESA) {
                inAttesa++;
            } else {
                confermati++;
            }

            incasso += oc.getPrezzoTotale();

            String ritiro = oc.getDataRitiro() != null ? oc.getDataRitiro().toLowerCase() : "";
            if (ritiro.contains("banco") || ritiro.contains("ritiro")) {
                banco++;
            } else {
                domicilio++;
            }

            registraProdottiOrdine(oc.getDescrizioneDettagliata(), conteggioProdotti);
        }

        int ordiniValidi = confermati + inAttesa;
        stats.setOrdiniTotali(ordini.size());
        stats.setOrdiniConfermati(confermati);
        stats.setOrdiniInAttesa(inAttesa);
        stats.setOrdiniDisdetti(disdetti);
        stats.setOrdiniDomicilio(domicilio);
        stats.setOrdiniRitiroBanco(banco);
        stats.setIncassoTotale(Math.round(incasso * 100.0) / 100.0);
        stats.setScontrinoMedio(ordiniValidi > 0 ? Math.round((incasso / ordiniValidi) * 100.0) / 100.0 : 0.0);
        stats.setTopProdottiVenduti(estraiTopProdotti(conteggioProdotti, 6));
    }

    private void registraProdottiOrdine(String descrizione, Map<String, Integer> conteggio) {
        if (descrizione == null || descrizione.isBlank()) {
            return;
        }
        for (String seg : descrizione.split("\\|")) {
            String s = seg.trim();
            if (s.isEmpty()) continue;

            int qty = 1;
            Matcher m = PATTERN_QTA.matcher(s);
            if (m.matches()) {
                qty = Integer.parseInt(m.group(1));
                s = m.group(2).trim();
            }

            int idxPar = s.indexOf('(');
            String nomePulito = (idxPar > 0) ? s.substring(0, idxPar).trim() : s;
            if (nomePulito.length() > 26) {
                nomePulito = nomePulito.substring(0, 24) + "…";
            }
            if (!nomePulito.isEmpty()) {
                conteggio.merge(nomePulito, qty, Integer::sum);
            }
        }
    }

    private Map<String, Integer> estraiTopProdotti(Map<String, Integer> conteggio, int limite) {
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(conteggio.entrySet());
        lista.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        Map<String, Integer> top = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(limite, lista.size()); i++) {
            top.put(lista.get(i).getKey(), lista.get(i).getValue());
        }
        return top;
    }

    private void calcolaMetricheMagazzino(StatisticheBean stats) {
        List<Ingrediente> ingredienti = ingredienteDAO != null ? ingredienteDAO.caricaTuttiIngredienti() : List.of();
        int ok = 0;
        int sottoSoglia = 0;
        int esauriti = 0;

        for (Ingrediente ing : ingredienti) {
            if (ing.getQuantitaDisponibile() <= 0.0) {
                esauriti++;
            } else if (ing.isSottoSoglia()) {
                sottoSoglia++;
            } else {
                ok++;
            }
        }

        stats.setIngredientiOk(ok);
        stats.setIngredientiSottoSoglia(sottoSoglia);
        stats.setIngredientiEsauriti(esauriti);
    }
}
