package control.grafico;

import bean.IngredienteBean;
import bean.RichiestaGustoBean;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import java.util.function.BiConsumer;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import view.StaffTurniView;

/**
 * Controller Grafico dedicato alla gestione del Magazzino Materie Prime
 * e delle Richieste di Gusto su Misura da parte dello Staff (Maestro e Dipendenti):
 * - Visualizzazione scorte e segnalazioni sotto soglia
 * - Registrazione rifornimenti manuali e rifornimenti urgenti (+5 Kg/L)
 * - Approvazione, approvazione con rifornimento urgente e rifiuto delle proposte "Gusto su Misura".
 */
public class MagazzinoGustiStaffControlGrafico {

    private ListView<IngredienteBean> listaMagazzino;
    private Label lblRiepilogoMagazzino;
    private ListView<String> listaUrgenzeMagazzino;
    private TextField campoNomeIngredienteRifornimento;
    private TextField campoKgRifornimento;
    private TextField campoIngredienteUrgente;
    private ListView<String> listaRichiesteGusto;
    private TextField campoCodRichiesta;
    private TextField campoRispostaGusto;
    private final BiConsumer<String, Boolean> notificaEsito;
    private final Runnable onAggiornaGlobale;

    public MagazzinoGustiStaffControlGrafico(BiConsumer<String, Boolean> notificaEsito,
                                             Runnable onAggiornaGlobale) {
        this.notificaEsito = notificaEsito;
        this.onAggiornaGlobale = onAggiornaGlobale;
    }

    public void legaCampiMagazzino(ListView<IngredienteBean> listaMagazzino,
                                   Label lblRiepilogoMagazzino,
                                   ListView<String> listaUrgenzeMagazzino,
                                   TextField campoNomeIngredienteRifornimento,
                                   TextField campoKgRifornimento,
                                   TextField campoIngredienteUrgente) {
        this.listaMagazzino = listaMagazzino;
        this.lblRiepilogoMagazzino = lblRiepilogoMagazzino;
        this.listaUrgenzeMagazzino = listaUrgenzeMagazzino;
        this.campoNomeIngredienteRifornimento = campoNomeIngredienteRifornimento;
        this.campoKgRifornimento = campoKgRifornimento;
        this.campoIngredienteUrgente = campoIngredienteUrgente;
    }

    public void legaCampiRichiesteGusto(ListView<String> listaRichiesteGusto,
                                        TextField campoCodRichiesta,
                                        TextField campoRispostaGusto) {
        this.listaRichiesteGusto = listaRichiesteGusto;
        this.campoCodRichiesta = campoCodRichiesta;
        this.campoRispostaGusto = campoRispostaGusto;
    }

    public void inizializza(Runnable onSelezionaTabMagazzino) {
        StaffTurniView.configuraCelleMagazzino(listaMagazzino);

        if (listaUrgenzeMagazzino != null) {
            listaUrgenzeMagazzino.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|")) {
                    String nome = newVal.split("\\|")[0].replace("⚠️", "").trim();
                    onSelezionaTabMagazzino.run();
                    if (campoNomeIngredienteRifornimento != null) {
                        campoNomeIngredienteRifornimento.setText(nome);
                    }
                    if (campoKgRifornimento != null) {
                        campoKgRifornimento.requestFocus();
                    }
                }
            });
        }

        if (listaMagazzino != null) {
            listaMagazzino.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && campoNomeIngredienteRifornimento != null) {
                    campoNomeIngredienteRifornimento.setText(newVal.getNome());
                    if (campoKgRifornimento != null) {
                        campoKgRifornimento.requestFocus();
                    }
                }
            });
        }

        if (listaRichiesteGusto != null) {
            listaRichiesteGusto.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoCodRichiesta != null) {
                    campoCodRichiesta.setText(newVal.split("\\|")[0].trim());
                }
            });
        }
    }

    /**
     * Aggiorna le liste di magazzino e richieste gusto e restituisce il numero di materie prime sotto soglia.
     */
    public int aggiornaMagazzinoERichieste() {
        ProduzioneOrdiniControl prodCtrl = new ProduzioneOrdiniControl();

        if (listaRichiesteGusto != null) {
            listaRichiesteGusto.getItems().clear();
            for (RichiestaGustoBean r : new RichiestaGustoControl().visualizzaTutteRichieste()) {
                listaRichiesteGusto.getItems().add(r.getCodiceRichiesta() + " | Cliente: " + r.getNomeCliente()
                        + " | Gusto: '" + r.getNomeGustoProposto() + "' (" + r.getQuantitaRichiestaKg() + " kg)"
                        + " | Ing: " + r.getNomeIngredientePrincipale() + " | Stato: " + r.getStato());
            }
        }

        if (listaMagazzino != null) {
            listaMagazzino.getItems().clear();
            int sottoSoglia = 0;
            for (IngredienteBean ing : prodCtrl.visualizzaMagazzino()) {
                listaMagazzino.getItems().add(ing);
                if (ing.isSottoSoglia()) {
                    sottoSoglia++;
                }
            }
            if (lblRiepilogoMagazzino != null) {
                lblRiepilogoMagazzino.setText(sottoSoglia == 0
                        ? "✅ Tutte le scorte sono sufficienti"
                        : "⚠️ " + sottoSoglia + " ingredienti sotto soglia: da riordinare");
            }
        }

        int scorteUrgenti = 0;
        if (listaUrgenzeMagazzino != null) {
            listaUrgenzeMagazzino.getItems().clear();
            for (IngredienteBean ing : prodCtrl.visualizzaMagazzino()) {
                if (ing.isSottoSoglia()) {
                    scorteUrgenti++;
                    listaUrgenzeMagazzino.getItems().add(ing.getNome() + " | Rimanenti: "
                            + ing.getQuantitaDisponibile() + " " + ing.getUnitaMisura()
                            + " (Min: " + ing.getSogliaMinima() + ")");
                }
            }
            if (scorteUrgenti == 0) {
                listaUrgenzeMagazzino.getItems().add("✅ Nessuna mancanza: materie prime ottimali");
            }
        } else {
            for (IngredienteBean ing : prodCtrl.visualizzaMagazzino()) {
                if (ing.isSottoSoglia()) {
                    scorteUrgenti++;
                }
            }
        }
        return scorteUrgenti;
    }

    public void registraRifornimentoManuale() {
        try {
            String nome = campoNomeIngredienteRifornimento != null ? campoNomeIngredienteRifornimento.getText() : null;
            if (nome == null || nome.trim().isEmpty()) {
                notificaEsito.accept("⚠️ Seleziona prima una materia prima dalla lista.", true);
                return;
            }
            String qtaTesto = campoKgRifornimento != null ? campoKgRifornimento.getText() : null;
            if (qtaTesto == null || qtaTesto.trim().isEmpty()) {
                notificaEsito.accept("⚠️ Inserisci la quantita' arrivata (in Kg o Litri).", true);
                return;
            }
            double qta;
            try {
                qta = Double.parseDouble(qtaTesto.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                notificaEsito.accept("⚠️ Inserisci un valore numerico valido (es. 10.0).", true);
                return;
            }
            if (qta <= 0) {
                notificaEsito.accept("⚠️ La quantita' deve essere maggiore di zero.", true);
                return;
            }
            new ProduzioneOrdiniControl().rifornisciIngredienteMagazzino(nome.trim(), qta);
            notificaEsito.accept("✅ Rifornimento registrato: aggiunti " + qta + " Kg/L di '" + nome.trim() + "' al magazzino!", false);
            if (campoKgRifornimento != null) {
                campoKgRifornimento.clear();
            }
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void rifornisciUrgente() {
        try {
            String nome = campoIngredienteUrgente != null ? campoIngredienteUrgente.getText() : null;
            if (nome == null || nome.trim().isEmpty()) {
                notificaEsito.accept("⚠️ Seleziona prima una materia prima sotto soglia dalla lista.", true);
                return;
            }
            new ProduzioneOrdiniControl().rifornisciIngredienteMagazzino(nome.trim(), 5.0);
            notificaEsito.accept("⚡ Scorte rifornite con successo (+5 Kg/L) per: " + nome.trim(), false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void approvaGusto() {
        try {
            new RichiestaGustoControl().approvaRichiestaGusto(
                    campoCodRichiesta.getText(), campoRispostaGusto.getText());
            notificaEsito.accept("✅ Richiesta gusto approvata e ingrediente scalato dal magazzino!", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void rifornisciEAprovaGusto() {
        try {
            new RichiestaGustoControl().approvaConRifornimentoUrgente(
                    campoCodRichiesta.getText(), 5.0, campoRispostaGusto.getText());
            notificaEsito.accept("✅ Rifornimento urgente (+5 Kg) effettuato e gusto approvato!", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void rifiutaGusto() {
        try {
            new RichiestaGustoControl().rifiutaRichiestaGusto(
                    campoCodRichiesta.getText(), campoRispostaGusto.getText());
            notificaEsito.accept("ℹ️ Richiesta gusto rifiutata.", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }
}
