package control.grafico;

import bean.LoginBean;
import bean.TurnoBean;
import control.applicativo.GestioneTurniEmergenzeControl;
import control.applicativo.LoginControl;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import bean.DipendenteBean;
import model.ConfigurazioneOreDipendente;
import model.DisponibilitaDipendente;
import view.DisponibilitaStaffView;
import view.GestioneDipendentiView;

/**
 * Controller Grafico dedicato all'amministrazione dell'organico aziendale:
 * anagrafica dipendenti, monte ore, limite opzionale max turni settimanali,
 * disponibilita', modifica profilo ed eliminazione/disattivazione in sicurezza.
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

    private ComboBox<String> comboModalitaOreDip;
    private TextField campoOreMinDip;
    private TextField campoOreMaxDip;

    private VBox contenitoreModerno;
    private GestioneDipendentiView.ComponentiForm formModerno;
    private DisponibilitaDipendente disponibilitaNuovoDipendente = DisponibilitaDipendente.tuttoDisponibile();

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

    public void legaControlliMonteOre(ComboBox<String> comboModalita,
                                      TextField campoOreMin,
                                      TextField campoOreMax) {
        this.comboModalitaOreDip = comboModalita;
        this.campoOreMinDip = campoOreMin;
        this.campoOreMaxDip = campoOreMax;

        if (comboModalitaOreDip != null) {
            comboModalitaOreDip.setItems(FXCollections.observableArrayList(
                    "Ore settimanali fisse", "Intervallo settimanale"));
            comboModalitaOreDip.setValue("Ore settimanali fisse");
            comboModalitaOreDip.setOnAction(e -> aggiornaVisibilitaCampiOre());
            aggiornaVisibilitaCampiOre();
        }
    }

    public void legaContenitoreModerno(VBox boxGestioneDipendenti) {
        this.contenitoreModerno = boxGestioneDipendenti;
    }

    public void inizializza() {
        if (contenitoreModerno != null) {
            formModerno = GestioneDipendentiView.costruisciSchermata(
                    contenitoreModerno,
                    this::generaPasswordSicura,
                    this::apriConfigurazioneDisponibilitaNuovo,
                    this::creaDipendente,
                    this::eliminaDipendente);
            formModerno.comboModalitaOre.setOnAction(e -> aggiornaVisibilitaCampiOre());
            aggiornaVisibilitaCampiOre();
            return;
        }
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
            listaDipendentiStaff.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    apriConfigurazioneDipendenteSelezionato();
                }
            });
        }
    }

    private void aggiornaVisibilitaCampiOre() {
        ComboBox<String> cb = formModerno != null ? formModerno.comboModalitaOre : comboModalitaOreDip;
        TextField tfMin = formModerno != null ? formModerno.campoOreMin : campoOreMinDip;
        TextField tfMax = formModerno != null ? formModerno.campoOreMax : campoOreMaxDip;
        if (cb == null) return;
        boolean intervallo = cb.getValue() != null && cb.getValue().toLowerCase().contains("intervallo");
        if (formModerno != null && formModerno.lblCampoOre != null) {
            formModerno.lblCampoOre.setText(intervallo ? "Min – Max (es. 20 – 30)" : "Ore fisse (es. 40)");
        }
        if (tfMin != null) {
            tfMin.setPromptText(intervallo ? "Min (es. 20)" : "Es. 40");
            tfMin.setPrefWidth(intervallo ? 85 : 115);
        }
        if (tfMax != null) {
            tfMax.setVisible(intervallo);
            tfMax.setManaged(intervallo);
        }
    }

    private Window getOwnerWindow() {
        if (contenitoreModerno != null && contenitoreModerno.getScene() != null) {
            return contenitoreModerno.getScene().getWindow();
        }
        if (listaDipendentiStaff != null && listaDipendentiStaff.getScene() != null) {
            return listaDipendentiStaff.getScene().getWindow();
        }
        return null;
    }

    private void apriConfigurazioneDisponibilitaNuovo() {
        DisponibilitaStaffView.apriDialogSoloDisponibilita(getOwnerWindow(), disponibilitaNuovoDipendente, nuova -> {
            this.disponibilitaNuovoDipendente = nuova;
            if (formModerno != null && formModerno.btnConfiguraDispNuovo != null) {
                formModerno.btnConfiguraDispNuovo.setText("📅 Disponibilità: " + nuova.getRiepilogoSintetico());
            }
        });
    }

    public void apriConfigurazioneDipendenteSelezionato() {
        String email = formModerno != null ? formModerno.getEmailSelezionata()
                : (campoEmailNuovoDip != null ? campoEmailNuovoDip.getText() : null);
        if ((email == null || email.trim().isEmpty()) && listaDipendentiStaff != null) {
            String sel = listaDipendentiStaff.getSelectionModel().getSelectedItem();
            if (sel != null && sel.contains("|")) {
                String[] parti = sel.split("\\|");
                if (parti.length >= 2) email = parti[1].trim();
            }
        }
        if (email == null || email.trim().isEmpty()) {
            onEsito.accept("⚠️ Seleziona prima un dipendente dalla tabella per modificarne il profilo.", true);
            return;
        }
        apriConfigurazioneDipendentePerEmail(email.trim());
    }

    public void apriConfigurazioneDipendentePerEmail(String email) {
        LoginControl loginCtrl = new LoginControl();
        DipendenteBean dip = loginCtrl.trovaDipendenteBeanPerEmail(email);
        if (dip == null) {
            onEsito.accept("⚠️ Dipendente non trovato: " + email, true);
            return;
        }
        DisponibilitaStaffView.apriDialogConfiguraDipendente(getOwnerWindow(), dip, (nuovaOre, nuovaDisp) -> {
            try {
                loginCtrl.aggiornaConfigurazioneDipendente(
                        email, nuovaOre, nuovaDisp, dip.getMansioni(), dip.isAttivo(), dip.getMaxTurniSettimanali());
                String infoMax = dip.hasLimiteMaxTurni() ? (" • Max turni: " + dip.getMaxTurniSettimanali()) : "";
                onEsito.accept("✅ Profilo aggiornato per " + dip.getNome() + " " + dip.getCognome()
                        + " (" + nuovaOre.getEtichettaObiettivo() + infoMax + ").", false);
                aggiornaDipendenti();
                onRichiediAggiornamento.run();
            } catch (FrostlyException ex) {
                onEsito.accept("⚠️ " + ex.getMessage(), true);
            }
        });
    }

    public void generaPasswordSicura() {
        TextField target = formModerno != null ? formModerno.campoPassword : campoPasswordNuovoDip;
        if (target == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(ALFABETO.charAt(SECURE_RANDOM.nextInt(ALFABETO.length())));
        }
        target.setText(sb.toString());
    }

    public void creaDipendente() {
        try {
            TextField tfNome = formModerno != null ? formModerno.campoNome : campoNomeNuovoDip;
            TextField tfCognome = formModerno != null ? formModerno.campoCognome : campoCognomeNuovoDip;
            TextField tfEmail = formModerno != null ? formModerno.campoEmail : campoEmailNuovoDip;
            TextField tfPass = formModerno != null ? formModerno.campoPassword : campoPasswordNuovoDip;
            ComboBox<String> cbMod = formModerno != null ? formModerno.comboModalitaOre : comboModalitaOreDip;
            TextField tfMin = formModerno != null ? formModerno.campoOreMin : campoOreMinDip;
            TextField tfMax = formModerno != null ? formModerno.campoOreMax : campoOreMaxDip;

            LoginBean nuovo = new LoginBean();
            nuovo.setNome(tfNome != null ? tfNome.getText() : "");
            nuovo.setCognome(tfCognome != null ? tfCognome.getText() : "");
            nuovo.setEmail(tfEmail != null ? tfEmail.getText() : "");
            nuovo.setPassword(tfPass != null ? tfPass.getText() : "");

            ConfigurazioneOreDipendente cfgOre;
            boolean intervallo = cbMod != null && cbMod.getValue() != null
                    && cbMod.getValue().toLowerCase().contains("intervallo");
            if (intervallo) {
                double min = DisponibilitaStaffView.parseOreDouble(
                        tfMin != null ? tfMin.getText() : "", "Minimo ore settimanali");
                double max = DisponibilitaStaffView.parseOreDouble(
                        tfMax != null ? tfMax.getText() : "", "Massimo ore settimanali");
                cfgOre = ConfigurazioneOreDipendente.intervallo(min, max);
            } else {
                double fisse = DisponibilitaStaffView.parseOreDouble(
                        tfMin != null ? tfMin.getText() : "", "Ore settimanali fisse");
                cfgOre = ConfigurazioneOreDipendente.oreFisse(fisse);
            }

            Integer maxTurni = null;
            if (formModerno != null && formModerno.toggleMaxTurni.isAttivo()) {
                String rawSpin = formModerno.spinnerMaxTurni.getEditor() != null
                        ? formModerno.spinnerMaxTurni.getEditor().getText() : null;
                try {
                    int val = (rawSpin != null && !rawSpin.trim().isEmpty())
                            ? Integer.parseInt(rawSpin.trim())
                            : formModerno.spinnerMaxTurni.getValue();
                    if (val <= 0) {
                        throw new DatiNonValidiException("Il limite massimo di turni settimanali deve essere un numero intero positivo.");
                    }
                    maxTurni = val;
                } catch (NumberFormatException ex) {
                    throw new DatiNonValidiException("Il limite massimo di turni settimanali deve essere un numero intero positivo.");
                }
            }

            LoginBean creato = new LoginControl().creaProfiloDipendente(
                    nuovo, cfgOre, disponibilitaNuovoDipendente, maxTurni);
            String infoMax = maxTurni != null ? (" • Max turni: " + maxTurni) : "";
            onEsito.accept("✅ Profilo dipendente creato per " + creato.getNomeCompleto()
                    + " (" + cfgOre.getEtichettaObiettivo() + infoMax + "). Credenziali: "
                    + creato.getEmail() + " / " + creato.getPassword(), false);
            resetCampiEAggiorna();
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void eliminaDipendente() {
        String email = formModerno != null ? formModerno.getEmailSelezionata()
                : (campoEmailNuovoDip != null ? campoEmailNuovoDip.getText() : null);
        if (email == null || email.trim().isEmpty()) {
            onEsito.accept("⚠️ Seleziona prima un dipendente dalla tabella sotto per eliminarlo o disattivarlo.", true);
            return;
        }
        DipendenteBean target = new LoginControl().trovaDipendenteBeanPerEmail(email.trim());
        if (target == null) {
            onEsito.accept("⚠️ Dipendente non trovato: " + email.trim(), true);
            return;
        }
        confermaEliminaODisattiva(target);
    }

    private void confermaEliminaODisattiva(DipendenteBean dip) {
        try {
            List<TurnoBean> turniStorici = new GestioneTurniEmergenzeControl().visualizzaTurniDipendente(dip.getEmail());
            int nTurni = turniStorici != null ? turniStorici.size() : 0;

            GestioneDipendentiView.SceltaRimozione scelta =
                    GestioneDipendentiView.mostraDialogRimozioneDipendente(getOwnerWindow(), dip, nTurni);

            if (scelta == GestioneDipendentiView.SceltaRimozione.DISATTIVA) {
                new LoginControl().impostaStatoAttivoDipendente(dip.getEmail(), false);
                onEsito.accept("⏸ Account di " + dip.getNome() + " " + dip.getCognome()
                        + " disattivato in sicurezza (storico turni preservato).", false);
                aggiornaDipendenti();
                onRichiediAggiornamento.run();
            } else if (scelta == GestioneDipendentiView.SceltaRimozione.ELIMINA) {
                new LoginControl().eliminaProfiloDipendente(dip.getEmail());
                if (formModerno != null) {
                    formModerno.setEmailSelezionata(null);
                }
                onEsito.accept("🗑️ Dipendente '" + dip.getEmail() + "' eliminato definitivamente dallo staff.", false);
                resetCampiEAggiorna();
            }
        } catch (FrostlyException e) {
            onEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void aggiornaDipendenti() {
        if (formModerno != null) {
            List<DipendenteBean> tutti = new LoginControl().caricaTuttiDipendentiBean();
            GestioneDipendentiView.popolaTabellaPersonale(
                    formModerno,
                    tutti,
                    dip -> apriConfigurazioneDipendentePerEmail(dip.getEmail()),
                    this::confermaEliminaODisattiva);
        }
        if (listaDipendentiStaff != null) {
            listaDipendentiStaff.getItems().setAll(new LoginControl().elencoDipendenti());
        }
    }

    private void resetCampiEAggiorna() {
        if (formModerno != null) {
            formModerno.campoNome.clear();
            formModerno.campoCognome.clear();
            formModerno.campoEmail.clear();
            formModerno.campoPassword.clear();
            formModerno.campoOreMin.clear();
            formModerno.campoOreMax.clear();
            formModerno.toggleMaxTurni.setAttivo(false);
            formModerno.btnConfiguraDispNuovo.setText("📅 Configura disponibilità");
        }
        this.disponibilitaNuovoDipendente = DisponibilitaDipendente.tuttoDisponibile();

        if (campoNomeNuovoDip != null) campoNomeNuovoDip.clear();
        if (campoCognomeNuovoDip != null) campoCognomeNuovoDip.clear();
        if (campoEmailNuovoDip != null) campoEmailNuovoDip.clear();
        if (campoPasswordNuovoDip != null) campoPasswordNuovoDip.clear();
        if (campoOreMinDip != null) campoOreMinDip.clear();
        if (campoOreMaxDip != null) campoOreMaxDip.clear();

        GestioneTurniEmergenzeControl turniCtrl = new GestioneTurniEmergenzeControl();
        if (comboDipendenteTurno != null) {
            comboDipendenteTurno.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
        }
        if (comboSostituto != null) {
            comboSostituto.setItems(FXCollections.observableArrayList(turniCtrl.getEmailTuttiDipendenti()));
        }
        aggiornaDipendenti();
        onRichiediAggiornamento.run();
    }
}
