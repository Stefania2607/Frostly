package control.grafico;

import bean.LoginBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import frostly.exception.FrostlyException;
import java.security.SecureRandom;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller Grafico dedicato all'amministrazione dell'organico aziendale:
 * anagrafica dipendenti, generazione di credenziali sicure e revoca accessi.
 */
public class GestioneDipendentiControlGrafico {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ALFABETO = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";

    private final ListView<String> listaDipendentiStaff;
    private final TextField campoNomeNuovoDip;
    private final TextField campoCognomeNuovoDip;
    private final TextField campoEmailNuovoDip;
    private final TextField campoPasswordNuovoDip;
    private final ComboBox<String> comboDipendenteTurno;
    private final ComboBox<String> comboSostituto;

    private final BiConsumer<String, Boolean> onEsito;
    private final Runnable onRichiediAggiornamento;

    public GestioneDipendentiControlGrafico(ListView<String> listaDipendentiStaff,
                                            TextField campoNomeNuovoDip,
                                            TextField campoCognomeNuovoDip,
                                            TextField campoEmailNuovoDip,
                                            TextField campoPasswordNuovoDip,
                                            ComboBox<String> comboDipendenteTurno,
                                            ComboBox<String> comboSostituto,
                                            BiConsumer<String, Boolean> onEsito,
                                            Runnable onRichiediAggiornamento) {
        this.listaDipendentiStaff = listaDipendentiStaff;
        this.campoNomeNuovoDip = campoNomeNuovoDip;
        this.campoCognomeNuovoDip = campoCognomeNuovoDip;
        this.campoEmailNuovoDip = campoEmailNuovoDip;
        this.campoPasswordNuovoDip = campoPasswordNuovoDip;
        this.comboDipendenteTurno = comboDipendenteTurno;
        this.comboSostituto = comboSostituto;
        this.onEsito = onEsito;
        this.onRichiediAggiornamento = onRichiediAggiornamento;
    }

    public void inizializza() {
        if (listaDipendentiStaff != null) {
            listaDipendentiStaff.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("|") && campoEmailNuovoDip != null) {
                    String[] parti = newVal.split("\\|");
                    if (parti.length >= 2) {
                        campoEmailNuovoDip.setText(parti[1].trim());
                        String[] nc = parti[0].trim().split(" ");
                        if (nc.length >= 1 && campoNomeNuovoDip != null) campoNomeNuovoDip.setText(nc[0]);
                        if (nc.length >= 2 && campoCognomeNuovoDip != null) campoCognomeNuovoDip.setText(nc[1]);
                    }
                }
            });
        }
    }

    public void generaPasswordSicura() {
        if (campoPasswordNuovoDip == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(ALFABETO.charAt(SECURE_RANDOM.nextInt(ALFABETO.length())));
        }
        campoPasswordNuovoDip.setText(sb.toString());
    }

    public void creaDipendente() {
        try {
            LoginBean nuovo = new LoginBean();
            nuovo.setNome(campoNomeNuovoDip != null ? campoNomeNuovoDip.getText() : "");
            nuovo.setCognome(campoCognomeNuovoDip != null ? campoCognomeNuovoDip.getText() : "");
            nuovo.setEmail(campoEmailNuovoDip != null ? campoEmailNuovoDip.getText() : "");
            nuovo.setPassword(campoPasswordNuovoDip != null ? campoPasswordNuovoDip.getText() : "");
            LoginBean creato = new LoginControl().creaProfiloDipendente(nuovo);
            onEsito.accept("✅ Profilo dipendente creato per " + creato.getNomeCompleto()
                    + ". Comunicagli email e password: " + creato.getEmail() + " / " + creato.getPassword(), false);
            resetCampiEAggiorna();
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void eliminaDipendente() {
        try {
            String email = campoEmailNuovoDip != null ? campoEmailNuovoDip.getText() : null;
            if (email == null || email.trim().isEmpty()) {
                onEsito.accept("⚠️ Seleziona prima un dipendente dalla lista sotto per eliminarlo.", true);
                return;
            }
            new LoginControl().eliminaProfiloDipendente(email.trim());
            onEsito.accept("🗑️ Dipendente '" + email.trim() + "' eliminato con successo dallo staff.", false);
            resetCampiEAggiorna();
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void aggiornaDipendenti() {
        if (listaDipendentiStaff != null) {
            listaDipendentiStaff.getItems().setAll(new LoginControl().elencoDipendenti());
        }
    }

    private void resetCampiEAggiorna() {
        if (campoNomeNuovoDip != null) campoNomeNuovoDip.clear();
        if (campoCognomeNuovoDip != null) campoCognomeNuovoDip.clear();
        if (campoEmailNuovoDip != null) campoEmailNuovoDip.clear();
        if (campoPasswordNuovoDip != null) campoPasswordNuovoDip.clear();

        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        if (comboDipendenteTurno != null) {
            comboDipendenteTurno.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
        }
        if (comboSostituto != null) {
            comboSostituto.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
        }
        onRichiediAggiornamento.run();
    }
}
