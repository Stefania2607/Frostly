package control.grafico;

import bean.IngredienteBean;
import bean.RichiestaGustoBean;
import control.applicativo.ProduzioneOrdiniControl;
import control.applicativo.RichiestaGustoControl;
import frostly.exception.FrostlyException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller Grafico dedicato al Caso d'Uso "Richiesta Gusto Personalizzato" (Gusto su Misura).
 * Separa la gestione del modulo di proposta creativa del cliente dal controller del Menu e Carrello,
 * mantenendo zero istruzioni estetiche (BCE).
 */
public class RichiestaGustoControlGrafico {

    private TextField campoNome;
    private TextField campoDescrizione;
    private ComboBox<String> comboIngrediente;
    private TextField campoIngredienteFallback;
    private TextField campoKg;
    private TextField campoData;
    private Label lblRiepilogoNome;
    private Label lblRiepilogoIngrediente;
    private Label lblRiepilogoDescrizione;
    private Label lblRiepilogoKgData;
    private ListView<String> listaMieRichieste;

    public void legaCampiInput(TextField campoNome,
                               TextField campoDescrizione,
                               ComboBox<String> comboIngrediente,
                               TextField campoIngredienteFallback,
                               TextField campoKg,
                               TextField campoData) {
        this.campoNome = campoNome;
        this.campoDescrizione = campoDescrizione;
        this.comboIngrediente = comboIngrediente;
        this.campoIngredienteFallback = campoIngredienteFallback;
        this.campoKg = campoKg;
        this.campoData = campoData;
    }

    public void legaCampiRiepilogo(Label lblRiepilogoNome,
                                   Label lblRiepilogoIngrediente,
                                   Label lblRiepilogoDescrizione,
                                   Label lblRiepilogoKgData,
                                   ListView<String> listaMieRichieste) {
        this.lblRiepilogoNome = lblRiepilogoNome;
        this.lblRiepilogoIngrediente = lblRiepilogoIngrediente;
        this.lblRiepilogoDescrizione = lblRiepilogoDescrizione;
        this.lblRiepilogoKgData = lblRiepilogoKgData;
        this.listaMieRichieste = listaMieRichieste;
    }

    public void inizializza() {
        if (comboIngrediente != null) {
            List<String> nomiIng = new ArrayList<>();
            for (IngredienteBean ib : new ProduzioneOrdiniControl().visualizzaMagazzino()) {
                nomiIng.add(ib.getNome());
            }
            comboIngrediente.setItems(FXCollections.observableArrayList(nomiIng));
            if (!nomiIng.isEmpty()) {
                comboIngrediente.setValue(nomiIng.get(0));
            }
            comboIngrediente.setOnAction(e -> {
                if (comboIngrediente.getValue() != null && campoIngredienteFallback != null) {
                    campoIngredienteFallback.setText(comboIngrediente.getValue());
                }
                aggiornaAnteprima();
            });
        }
        if (campoNome != null) {
            campoNome.textProperty().addListener((obs, o, n) -> aggiornaAnteprima());
        }
        if (campoDescrizione != null) {
            campoDescrizione.textProperty().addListener((obs, o, n) -> aggiornaAnteprima());
        }
        if (campoKg != null) {
            campoKg.textProperty().addListener((obs, o, n) -> aggiornaAnteprima());
        }
        if (campoData != null) {
            campoData.textProperty().addListener((obs, o, n) -> aggiornaAnteprima());
        }
        aggiornaAnteprima();
    }

    public void aggiornaAnteprima() {
        if (lblRiepilogoNome != null) {
            String nome = (campoNome != null && !campoNome.getText().trim().isEmpty())
                    ? campoNome.getText().trim() : "Nome Creazione";
            lblRiepilogoNome.setText(nome);
        }
        if (lblRiepilogoIngrediente != null) {
            String ing = (comboIngrediente != null && comboIngrediente.getValue() != null)
                    ? comboIngrediente.getValue() : "Materia Prima";
            lblRiepilogoIngrediente.setText(ing);
        }
        if (lblRiepilogoDescrizione != null) {
            String desc = (campoDescrizione != null && !campoDescrizione.getText().trim().isEmpty())
                    ? campoDescrizione.getText().trim() : "Nessuna nota specificata";
            lblRiepilogoDescrizione.setText(desc);
        }
        if (lblRiepilogoKgData != null) {
            String kg = (campoKg != null && !campoKg.getText().trim().isEmpty())
                    ? campoKg.getText().trim() : "1.0";
            String data = (campoData != null && !campoData.getText().trim().isEmpty())
                    ? campoData.getText().trim() : "Domenica";
            lblRiepilogoKgData.setText("Quantità: " + kg + " Kg • Giorno: " + data);
        }
    }

    public RichiestaGustoBean inviaProposta(String emailCliente) throws FrostlyException {
        RichiestaGustoBean bean = new RichiestaGustoBean();
        bean.setEmailCliente(emailCliente);
        bean.setNomeGustoProposto(campoNome != null ? campoNome.getText() : "");

        String ingrediente = (comboIngrediente != null && comboIngrediente.getValue() != null)
                ? comboIngrediente.getValue()
                : (campoIngredienteFallback != null ? campoIngredienteFallback.getText() : "Pistacchio di Bronte DOP");
        bean.setNomeIngredientePrincipale(ingrediente);
        bean.setQuantitaDaTesto(campoKg != null ? campoKg.getText() : "");
        bean.setDataRitiroDesiderata(campoData != null ? campoData.getText() : "");

        String desc = (campoDescrizione != null && !campoDescrizione.getText().trim().isEmpty())
                ? campoDescrizione.getText().trim()
                : "Richiesta personalizzata da app";
        bean.setNoteCliente(desc);

        RichiestaGustoBean creata = new RichiestaGustoControl().inviaRichiestaGusto(bean);
        aggiornaListaRichieste(emailCliente);
        aggiornaAnteprima();
        return creata;
    }

    public void aggiornaListaRichieste(String emailCliente) {
        if (listaMieRichieste == null) return;
        listaMieRichieste.getItems().clear();
        for (RichiestaGustoBean r : new RichiestaGustoControl().visualizzaRichiesteCliente(emailCliente)) {
            listaMieRichieste.getItems().add(r.getCodiceRichiesta() + " | '" + r.getNomeGustoProposto()
                    + "' | Stato: " + r.getStato() + " | Risposta: " + r.getRispostaMaestro());
        }
    }
}
