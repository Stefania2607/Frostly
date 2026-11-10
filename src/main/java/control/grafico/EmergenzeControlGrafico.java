package control.grafico;

import bean.EmergenzaBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import frostly.exception.FrostlyException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import util.SessionManager;
import view.EmergenzeStaffView;

/**
 * Controller Grafico dedicato alla gestione del ciclo di vita delle Emergenze del personale:
 * segnalazione da parte dei dipendenti, disamina del Maestro e riassegnazione con sostituto.
 */
public class EmergenzeControlGrafico {

    private final ListView<String> listaEmergenze;
    private final ComboBox<String> comboMotivazioneRapida;
    private final ComboBox<String> comboSostituto;
    private final TextField campoCodiceTurno;
    private final TextField campoMotivazione;
    private final TextField campoCodEmergenza;
    private final TextField campoEmailSostituto;
    private final TextField campoNotaMaestro;
    private final VBox tendinaUrgenze;
    private final Button btnTendinaUrgenze;

    private final BiConsumer<String, Boolean> onEsito;
    private final Runnable onRichiediAggiornamento;

    private final Map<String, EmergenzaBean> mappaEmergenzeAttive = new LinkedHashMap<>();

    public EmergenzeControlGrafico(ListView<String> listaEmergenze,
                                   ComboBox<String> comboMotivazioneRapida,
                                   ComboBox<String> comboSostituto,
                                   TextField campoCodiceTurno,
                                   TextField campoMotivazione,
                                   TextField campoCodEmergenza,
                                   TextField campoEmailSostituto,
                                   TextField campoNotaMaestro,
                                   VBox tendinaUrgenze,
                                   Button btnTendinaUrgenze,
                                   BiConsumer<String, Boolean> onEsito,
                                   Runnable onRichiediAggiornamento) {
        this.listaEmergenze = listaEmergenze;
        this.comboMotivazioneRapida = comboMotivazioneRapida;
        this.comboSostituto = comboSostituto;
        this.campoCodiceTurno = campoCodiceTurno;
        this.campoMotivazione = campoMotivazione;
        this.campoCodEmergenza = campoCodEmergenza;
        this.campoEmailSostituto = campoEmailSostituto;
        this.campoNotaMaestro = campoNotaMaestro;
        this.tendinaUrgenze = tendinaUrgenze;
        this.btnTendinaUrgenze = btnTendinaUrgenze;
        this.onEsito = onEsito;
        this.onRichiediAggiornamento = onRichiediAggiornamento;
    }

    public void inizializza() {
        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();

        if (comboMotivazioneRapida != null) {
            comboMotivazioneRapida.setItems(FXCollections.observableArrayList(
                    "Visita medica improvvisa",
                    "Imprevisto familiare urgente",
                    "Guasto mezzo di trasporto",
                    "Febbre / Indisposizione"));
            comboMotivazioneRapida.setOnAction(e -> {
                if (comboMotivazioneRapida.getValue() != null && campoMotivazione != null) {
                    campoMotivazione.setText(comboMotivazioneRapida.getValue());
                }
            });
        }

        if (comboSostituto != null) {
            comboSostituto.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
            comboSostituto.setOnAction(e -> {
                if (comboSostituto.getValue() != null && campoEmailSostituto != null) {
                    campoEmailSostituto.setText(comboSostituto.getValue());
                }
            });
        }

        if (listaEmergenze != null) {
            listaEmergenze.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    EmergenzaBean e = mappaEmergenzeAttive.get(newVal);
                    if (e != null) {
                        Platform.runLater(() -> apriPopupDettaglioEmergenza(e));
                    }
                }
            });
        }
    }

    public void toggleTendinaUrgenze() {
        if (tendinaUrgenze != null) {
            boolean visibile = !tendinaUrgenze.isVisible();
            tendinaUrgenze.setVisible(visibile);
            tendinaUrgenze.setManaged(visibile);
        }
    }

    public void segnalaEmergenza() {
        try {
            EmergenzaBean bean = new EmergenzaBean();
            bean.setEmailRichiedente(SessionManager.getInstance().getEmailUtenteLoggato());
            if (campoCodiceTurno != null) {
                bean.setCodiceTurno(campoCodiceTurno.getText());
            }
            String motScritto = campoMotivazione != null ? campoMotivazione.getText().trim() : "";
            String motRapido = comboMotivazioneRapida != null && comboMotivazioneRapida.getValue() != null
                    ? comboMotivazioneRapida.getValue().trim() : "";
            String motFinale = !motScritto.isEmpty() ? motScritto : (!motRapido.isEmpty() ? motRapido : "Imprevisto personale urgente");
            bean.setMotivazione(motFinale);

            EmergenzaBean creata = new GestioneTurniEmergenzeControl().segnalaEmergenzaTurno(bean);
            onEsito.accept("✅ Emergenza inviata al Maestro! Codice: " + creata.getCodiceEmergenza(), false);
            onRichiediAggiornamento.run();
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void approvaEmergenza() {
        try {
            String cod = campoCodEmergenza != null ? campoCodEmergenza.getText() : "";
            String sost = campoEmailSostituto != null ? campoEmailSostituto.getText() : "";
            String nota = campoNotaMaestro != null ? campoNotaMaestro.getText() : "";
            new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(cod, sost, nota);
            onEsito.accept("✅ Emergenza approvata e turno riassegnato al sostituto!", false);
            onRichiediAggiornamento.run();
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void rifiutaEmergenza() {
        try {
            String cod = campoCodEmergenza != null ? campoCodEmergenza.getText() : "";
            String nota = campoNotaMaestro != null ? campoNotaMaestro.getText() : "";
            new GestioneTurniEmergenzeControl().rifiutaEmergenza(cod, nota);
            onEsito.accept("ℹ️ Emergenza rifiutata e dipendente notificato.", false);
            onRichiediAggiornamento.run();
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public int aggiornaListaEmergenze(GestioneTurniEmergenzeControl turniCtrl) {
        int emergenzeAttive = 0;
        if (listaEmergenze != null) {
            listaEmergenze.getItems().clear();
            mappaEmergenzeAttive.clear();
        }
        for (EmergenzaBean e : turniCtrl.visualizzaTutteEmergenze()) {
            if (!"IN_ATTESA".equals(e.getStato())) {
                continue;
            }
            emergenzeAttive++;
            if (listaEmergenze != null) {
                String orarioInfo = e.getDataTurno() != null ? (" (" + e.getDataTurno() + " " + e.getOrarioTurno() + ")") : "";
                String sintetica = "🚨 " + e.getNomeRichiedente() + " — Emergenza Turno " + e.getCodiceTurno() + orarioInfo;
                mappaEmergenzeAttive.put(sintetica, e);
                listaEmergenze.getItems().add(sintetica);
            }
        }
        return emergenzeAttive;
    }

    public void aggiornaBadgeUrgenze(int totaleUrgenze) {
        EmergenzeStaffView.aggiornaBottoneUrgenze(btnTendinaUrgenze, totaleUrgenze);
    }

    private void apriPopupDettaglioEmergenza(EmergenzaBean e) {
        Window owner = (tendinaUrgenze != null && tendinaUrgenze.getScene() != null)
                ? tendinaUrgenze.getScene().getWindow() : null;
        List<String> emailDipendenti = new GestioneTurniEmergenzeControl().getEmailTuttiDipendenti();

        EmergenzeStaffView.apriPopupDettaglioEmergenza(
                owner,
                e,
                emailDipendenti,
                (sost, nota) -> {
                    try {
                        if (sost == null || sost.trim().isEmpty()) {
                            onEsito.accept("⚠️ Seleziona prima un sostituto per riassegnare il turno.", true);
                            return;
                        }
                        new GestioneTurniEmergenzeControl().approvaEmergenzaConSostituto(
                                e.getCodiceEmergenza(), sost.trim(), nota);
                        onEsito.accept("✅ Emergenza approvata: Turno " + e.getCodiceTurno() + " riassegnato a " + sost, false);
                        onRichiediAggiornamento.run();
                    } catch (FrostlyException ex) {
                        onEsito.accept("⚠️ " + ex.getMessage(), true);
                    }
                },
                nota -> {
                    try {
                        new GestioneTurniEmergenzeControl().rifiutaEmergenza(e.getCodiceEmergenza(), nota);
                        onEsito.accept("ℹ️ Emergenza " + e.getCodiceEmergenza() + " rifiutata.", false);
                        onRichiediAggiornamento.run();
                    } catch (FrostlyException ex) {
                        onEsito.accept("⚠️ " + ex.getMessage(), true);
                    }
                });
    }
}
