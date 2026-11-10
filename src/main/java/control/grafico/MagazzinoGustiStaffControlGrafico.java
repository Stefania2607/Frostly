package control.grafico;

import bean.IngredienteBean;
import bean.RichiestaGustoBean;
import control.applicativo.MagazzinoControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import java.util.function.BiConsumer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import view.MagazzinoStaffView;

/**
 * Controller Grafico dedicato alla gestione del Magazzino Materie Prime
 * e delle Richieste di Gusto su Misura da parte dello Staff (Maestro e Dipendenti):
 * - Visualizzazione scorte e segnalazioni sotto soglia
 * - Registrazione carichi manuali, scarichi manuali e rifornimenti urgenti (+5 Kg/L)
 * - Aggiunta ed eliminazione di voci a magazzino
 * - Approvazione, approvazione con rifornimento urgente e rifiuto delle proposte "Gusto su Misura".
 */
public class MagazzinoGustiStaffControlGrafico {

    private ListView<IngredienteBean> listaMagazzino;
    private Label lblRiepilogoMagazzino;
    private ListView<String> listaUrgenzeMagazzino;
    private TextField campoNomeIngredienteRifornimento;
    private TextField campoKgRifornimento;
    private TextField campoIngredienteUrgente;
    private TextField campoNuovoNomeIngrediente;
    private TextField campoNuovaQtaIngrediente;
    private TextField campoNuovaSogliaIngrediente;
    private ComboBox<String> comboNuovaUnitaIngrediente;
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

    public void legaCampiNuovoIngrediente(TextField campoNuovoNomeIngrediente,
                                          TextField campoNuovaQtaIngrediente,
                                          TextField campoNuovaSogliaIngrediente,
                                          ComboBox<String> comboNuovaUnitaIngrediente) {
        this.campoNuovoNomeIngrediente = campoNuovoNomeIngrediente;
        this.campoNuovaQtaIngrediente = campoNuovaQtaIngrediente;
        this.campoNuovaSogliaIngrediente = campoNuovaSogliaIngrediente;
        this.comboNuovaUnitaIngrediente = comboNuovaUnitaIngrediente;
        if (this.comboNuovaUnitaIngrediente != null) {
            this.comboNuovaUnitaIngrediente.getItems().setAll(
                    "Kg", "Litri", "Bustine", "Bottiglie", "Porzioni", "Pezzi"
            );
            this.comboNuovaUnitaIngrediente.setValue("Kg");
        }
    }

    public void legaCampiRichiesteGusto(ListView<String> listaRichiesteGusto,
                                        TextField campoCodRichiesta,
                                        TextField campoRispostaGusto) {
        this.listaRichiesteGusto = listaRichiesteGusto;
        this.campoCodRichiesta = campoCodRichiesta;
        this.campoRispostaGusto = campoRispostaGusto;
    }

    public void inizializza(Runnable onSelezionaTabMagazzino) {
        MagazzinoStaffView.configuraCelleMagazzino(listaMagazzino);

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
        MagazzinoControl magCtrl = new MagazzinoControl();

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
            for (IngredienteBean ing : magCtrl.visualizzaMagazzino()) {
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
            for (IngredienteBean ing : magCtrl.visualizzaMagazzino()) {
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
            for (IngredienteBean ing : magCtrl.visualizzaMagazzino()) {
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
            new MagazzinoControl().rifornisciIngredienteMagazzino(nome.trim(), qta);
            notificaEsito.accept("✅ Carico registrato: aggiunti " + qta + " a '" + nome.trim() + "' in magazzino!", false);
            if (campoKgRifornimento != null) {
                campoKgRifornimento.clear();
            }
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void registraScaricoManuale() {
        try {
            String nome = campoNomeIngredienteRifornimento != null ? campoNomeIngredienteRifornimento.getText() : null;
            if (nome == null || nome.trim().isEmpty()) {
                notificaEsito.accept("⚠️ Seleziona prima una voce dalla lista del magazzino.", true);
                return;
            }
            String qtaTesto = campoKgRifornimento != null ? campoKgRifornimento.getText() : null;
            if (qtaTesto == null || qtaTesto.trim().isEmpty()) {
                notificaEsito.accept("⚠️ Inserisci la quantita' da scaricare.", true);
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
            new MagazzinoControl().scaricaIngredienteMagazzino(nome.trim(), qta);
            notificaEsito.accept("📤 Scarico registrato: scaricati " + qta + " di '" + nome.trim() + "' dal magazzino!", false);
            if (campoKgRifornimento != null) {
                campoKgRifornimento.clear();
            }
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void aggiungiNuovoIngrediente() {
        try {
            String nome = campoNuovoNomeIngrediente != null ? campoNuovoNomeIngrediente.getText() : null;
            if (nome == null || nome.trim().isEmpty()) {
                notificaEsito.accept("⚠️ Inserisci il nome della nuova voce di magazzino.", true);
                return;
            }
            String qtaStr = campoNuovaQtaIngrediente != null ? campoNuovaQtaIngrediente.getText() : null;
            String sogliaStr = campoNuovaSogliaIngrediente != null ? campoNuovaSogliaIngrediente.getText() : null;
            String unita = comboNuovaUnitaIngrediente != null ? comboNuovaUnitaIngrediente.getValue() : null;

            double qta = 0.0;
            if (qtaStr != null && !qtaStr.trim().isEmpty()) {
                try {
                    qta = Double.parseDouble(qtaStr.trim().replace(",", "."));
                } catch (NumberFormatException e) {
                    notificaEsito.accept("⚠️ Inserisci una quantita' iniziale valida.", true);
                    return;
                }
            }
            double soglia = 0.0;
            if (sogliaStr != null && !sogliaStr.trim().isEmpty()) {
                try {
                    soglia = Double.parseDouble(sogliaStr.trim().replace(",", "."));
                } catch (NumberFormatException e) {
                    notificaEsito.accept("⚠️ Inserisci una soglia minima valida.", true);
                    return;
                }
            }

            IngredienteBean bean = new IngredienteBean();
            bean.setNome(nome.trim());
            bean.setQuantitaDisponibile(qta);
            bean.setSogliaMinima(soglia);
            bean.setUnitaMisura(unita);

            new MagazzinoControl().aggiungiNuovoIngrediente(bean);

            notificaEsito.accept("✅ Nuova voce '" + nome.trim() + "' aggiunta con successo al magazzino!", false);
            if (campoNuovoNomeIngrediente != null) campoNuovoNomeIngrediente.clear();
            if (campoNuovaQtaIngrediente != null) campoNuovaQtaIngrediente.clear();
            if (campoNuovaSogliaIngrediente != null) campoNuovaSogliaIngrediente.clear();

            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void eliminaIngredienteSelezionato() {
        try {
            String nome = campoNomeIngredienteRifornimento != null ? campoNomeIngredienteRifornimento.getText() : null;
            if (nome == null || nome.trim().isEmpty()) {
                notificaEsito.accept("⚠️ Seleziona prima dalla lista la voce da eliminare dal magazzino.", true);
                return;
            }
            new MagazzinoControl().eliminaIngredienteMagazzino(nome.trim());
            notificaEsito.accept("🗑 Voce '" + nome.trim() + "' eliminata definitivamente dal magazzino!", false);
            if (campoNomeIngredienteRifornimento != null) campoNomeIngredienteRifornimento.clear();
            if (campoKgRifornimento != null) campoKgRifornimento.clear();

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
            new MagazzinoControl().rifornisciIngredienteMagazzino(nome.trim(), 5.0);
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
