package control.grafico;

import bean.GustoBean;
import control.applicativo.ProduzioneOrdiniControl;
import frostly.exception.FrostlyException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import view.StaffMenuEditorView;
import view.StaffTurniView;
import view.VetrinaMenuView;

/**
 * Controller Grafico dedicato esclusivamente alla gestione del Catalogo Menu Vetrina
 * da parte dello Staff (Maestro Gelataio e Dipendenti):
 * - Navigazione categorie e sottocategorie della vetrina di gestione
 * - Compilazione e salvataggio di nuovi gusti/prodotti o aggiornamento prezzi/allergeni
 * - Caricamento e rimozione della foto illustrativa del prodotto
 * - Attivazione/disattivazione della disponibilità giornaliera in vetrina.
 */
public class CatalogoMenuStaffControlGrafico {

    private Label lblCategoriaFormMenu;
    private ComboBox<String> comboCategoriaMenu;
    private ComboBox<String> comboSottocategoriaMenu;
    private TextField campoNomeGustoMenu;
    private TextField campoDescGustoMenu;
    private TextField campoAllergeniGustoMenu;
    private TextField campoPrezzoGustoMenu;
    private TextField campoImmagineMenu;
    private Label lblStatoFotoMenu;
    private Button btnRimuoviFoto;
    private HBox boxCategorieMenu;
    private HBox boxSottocategorieMenu;
    private FlowPane grigliaMenuGestione;
    private ListView<String> listaMenuGestione;
    private final BiConsumer<String, Boolean> notificaEsito;
    private final Runnable onAggiornaGlobale;

    private String categoriaCorrenteSelezionata = "Gelati";
    private String sottocategoriaCorrenteSelezionata = null;
    private VetrinaMenuView vetrinaGestione;
    private final Map<String, GustoBean> menuCorrente = new LinkedHashMap<>();

    public CatalogoMenuStaffControlGrafico(BiConsumer<String, Boolean> notificaEsito,
                                           Runnable onAggiornaGlobale) {
        this.notificaEsito = notificaEsito;
        this.onAggiornaGlobale = onAggiornaGlobale;
    }

    public void legaCampiForm(Label lblCategoriaFormMenu,
                              TextField campoNomeGustoMenu,
                              TextField campoDescGustoMenu,
                              TextField campoAllergeniGustoMenu,
                              TextField campoPrezzoGustoMenu) {
        this.lblCategoriaFormMenu = lblCategoriaFormMenu;
        this.campoNomeGustoMenu = campoNomeGustoMenu;
        this.campoDescGustoMenu = campoDescGustoMenu;
        this.campoAllergeniGustoMenu = campoAllergeniGustoMenu;
        this.campoPrezzoGustoMenu = campoPrezzoGustoMenu;
    }

    public void legaCampiFoto(TextField campoImmagineMenu,
                              Label lblStatoFotoMenu,
                              Button btnRimuoviFoto) {
        this.campoImmagineMenu = campoImmagineMenu;
        this.lblStatoFotoMenu = lblStatoFotoMenu;
        this.btnRimuoviFoto = btnRimuoviFoto;
    }

    public void legaControlliVetrina(ComboBox<String> comboCategoriaMenu,
                                     ComboBox<String> comboSottocategoriaMenu,
                                     HBox boxCategorieMenu,
                                     HBox boxSottocategorieMenu,
                                     FlowPane grigliaMenuGestione,
                                     ListView<String> listaMenuGestione) {
        this.comboCategoriaMenu = comboCategoriaMenu;
        this.comboSottocategoriaMenu = comboSottocategoriaMenu;
        this.boxCategorieMenu = boxCategorieMenu;
        this.boxSottocategorieMenu = boxSottocategorieMenu;
        this.grigliaMenuGestione = grigliaMenuGestione;
        this.listaMenuGestione = listaMenuGestione;
    }

    public void inizializza() {
        if (comboCategoriaMenu != null) {
            comboCategoriaMenu.setItems(FXCollections.observableArrayList(
                    "Gelati", "Crepes", "Panna", "Cioccolate calde", "Bevande", "Caffetteria", "Biscotti", "Crostate", "Tisane"));
            comboCategoriaMenu.setValue("Gelati");
            comboCategoriaMenu.valueProperty().addListener((obs, oldVal, newVal) -> aggiornaSottocategorieMenu(newVal));
            aggiornaSottocategorieMenu("Gelati");
        }

        if (grigliaMenuGestione != null) {
            vetrinaGestione = new VetrinaMenuView(
                    boxCategorieMenu, boxSottocategorieMenu, grigliaMenuGestione,
                    this::riempiFormGusto, this::alCambioFiltroVetrina);
        }

        if (listaMenuGestione != null) {
            listaMenuGestione.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || !newVal.contains("|")) {
                    return;
                }
                GustoBean g = menuCorrente.get(newVal.split("\\|")[0].trim());
                if (g != null) {
                    riempiFormGusto(g);
                }
            });
        }

        aggiornaBadgeReparto();
    }

    public void aggiornaCatalogo() {
        if (listaMenuGestione == null && vetrinaGestione == null) {
            return;
        }
        if (listaMenuGestione != null) {
            listaMenuGestione.getItems().clear();
        }
        menuCorrente.clear();
        List<GustoBean> menu = new ProduzioneOrdiniControl().visualizzaMenuGusti();
        for (GustoBean g : menu) {
            menuCorrente.put(g.getNome(), g);
            if (listaMenuGestione != null) {
                String disp = g.isDisponibileOggi() ? "🟢 IN VETRINA" : "🔴 ESAURITO";
                String unita = "Gelati".equalsIgnoreCase(g.getCategoria()) ? "/kg" : "";
                listaMenuGestione.getItems().add(g.getNome() + " | EUR " + g.getPrezzoAlKg() + unita + " | "
                        + g.getDescrizione() + " | Allergeni: " + g.getAllergeni() + " | " + disp
                        + " | 📂 " + g.getCategoria() + " › " + g.getSottocategoria()
                        + (g.getImmagineUrl().isEmpty() ? "" : " | 🖼"));
            }
        }
        if (vetrinaGestione != null) {
            vetrinaGestione.carica(menu);
        }
    }

    public void rimuoviFoto() {
        if (campoImmagineMenu != null) {
            campoImmagineMenu.clear();
        }
        aggiornaStatoFoto(null);
    }

    public void scegliFoto() {
        if (campoImmagineMenu == null || campoImmagineMenu.getScene() == null) {
            return;
        }
        String percorso = scegliFotoERitornaPercorso(campoImmagineMenu.getScene().getWindow());
        if (percorso != null) {
            campoImmagineMenu.setText(percorso);
            aggiornaStatoFoto(percorso);
        }
    }

    private String scegliFotoERitornaPercorso(Window finestra) {
        FileChooser scelta = new FileChooser();
        scelta.setTitle("Scegli la foto del prodotto");
        scelta.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Immagini (png, jpg, jpeg, gif)", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = scelta.showOpenDialog(finestra);
        if (file == null) {
            return null;
        }
        try {
            Path cartella = Paths.get("immagini_menu");
            Files.createDirectories(cartella);
            Path cartellaRes = Paths.get("src/main/resources/immagini_menu");
            if (Files.exists(Paths.get("src/main/resources"))) {
                Files.createDirectories(cartellaRes);
            }
            String pulito = file.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            String nomeUnivoco = System.currentTimeMillis() + "_" + pulito;
            Path destinazione = cartella.resolve(nomeUnivoco);
            Files.copy(file.toPath(), destinazione, StandardCopyOption.REPLACE_EXISTING);
            if (Files.exists(cartellaRes)) {
                Files.copy(destinazione, cartellaRes.resolve(nomeUnivoco), StandardCopyOption.REPLACE_EXISTING);
            }
            String percorsoRelativo = "immagini_menu/" + nomeUnivoco;
            notificaEsito.accept("🖼 Foto caricata: " + file.getName(), false);
            return percorsoRelativo;
        } catch (IOException e) {
            notificaEsito.accept("⚠️ Impossibile salvare la foto: " + e.getMessage(), true);
            return null;
        }
    }

    public void preparaNuovoGusto() {
        if (listaMenuGestione != null) {
            listaMenuGestione.getSelectionModel().clearSelection();
        }
        if (vetrinaGestione != null) {
            vetrinaGestione.deseleziona();
        }
        if (campoNomeGustoMenu != null) campoNomeGustoMenu.clear();
        if (campoPrezzoGustoMenu != null) campoPrezzoGustoMenu.clear();
        if (campoAllergeniGustoMenu != null) campoAllergeniGustoMenu.clear();
        if (campoDescGustoMenu != null) campoDescGustoMenu.clear();
        if (campoImmagineMenu != null) campoImmagineMenu.clear();
        aggiornaBadgeReparto();
        Window finestra = ricavaFinestraAttiva();
        if (finestra != null) {
            StaffMenuEditorView.apriPopupEditorProdotto(
                    finestra, null, categoriaCorrenteSelezionata, sottocategoriaCorrenteSelezionata, creaAzioniPopup());
        } else if (campoNomeGustoMenu != null) {
            campoNomeGustoMenu.requestFocus();
        }
    }

    public void salvaOAggiornaGusto() {
        try {
            String catDaSalvare = (categoriaCorrenteSelezionata != null && !categoriaCorrenteSelezionata.trim().isEmpty())
                    ? categoriaCorrenteSelezionata : "Gelati";
            String sottoDaSalvare = (sottocategoriaCorrenteSelezionata != null && !sottocategoriaCorrenteSelezionata.trim().isEmpty())
                    ? sottocategoriaCorrenteSelezionata : sottocategoriaPredefinita(catDaSalvare);

            new ProduzioneOrdiniControl().salvaOAggiornaGustoMenu(
                    campoNomeGustoMenu.getText(),
                    campoDescGustoMenu.getText(),
                    campoAllergeniGustoMenu.getText(),
                    campoPrezzoGustoMenu.getText(),
                    true,
                    catDaSalvare,
                    sottoDaSalvare,
                    campoImmagineMenu == null ? "" : campoImmagineMenu.getText());
            notificaEsito.accept("✅ Menu aggiornato con successo: '" + campoNomeGustoMenu.getText() + "' salvato!", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void cambiaDisponibilitaGusto() {
        try {
            new ProduzioneOrdiniControl().cambiaDisponibilitaGustoMenu(campoNomeGustoMenu.getText());
            notificaEsito.accept("✅ Disponibilita' in vetrina aggiornata per: " + campoNomeGustoMenu.getText(), false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    public void eliminaGusto() {
        try {
            String nome = campoNomeGustoMenu.getText();
            new ProduzioneOrdiniControl().eliminaGustoMenu(nome);
            notificaEsito.accept("🗑️ Gusto '" + nome + "' rimosso dal menu.", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    private void aggiornaStatoFoto(String nomeFile) {
        StaffTurniView.aggiornaStatoFotoLabel(lblStatoFotoMenu, btnRimuoviFoto, nomeFile);
    }

    private void alCambioFiltroVetrina(String cat, String sotto) {
        this.categoriaCorrenteSelezionata = cat;
        this.sottocategoriaCorrenteSelezionata = sotto;
        aggiornaBadgeReparto();
    }

    private void aggiornaBadgeReparto() {
        if (lblCategoriaFormMenu != null) {
            String testo = "📂 Reparto: " + VetrinaMenuView.emojiCategoria(categoriaCorrenteSelezionata) + " " + categoriaCorrenteSelezionata;
            if (sottocategoriaCorrenteSelezionata != null && !sottocategoriaCorrenteSelezionata.trim().isEmpty()) {
                testo += " › " + sottocategoriaCorrenteSelezionata;
            }
            lblCategoriaFormMenu.setText(testo);
        }
        if (campoPrezzoGustoMenu != null) {
            boolean isGelato = "Gelati".equalsIgnoreCase(categoriaCorrenteSelezionata);
            campoPrezzoGustoMenu.setPromptText(isGelato ? "Prezzo €/kg" : "Prezzo €");
        }
    }

    private String sottocategoriaPredefinita(String cat) {
        if ("Crepes".equalsIgnoreCase(cat)) return "Farciture";
        if ("Panna".equalsIgnoreCase(cat)) return "Porzioni";
        if ("Cioccolate calde".equalsIgnoreCase(cat)) return "Classiche";
        if ("Bevande".equalsIgnoreCase(cat)) return "Acqua";
        if ("Caffetteria".equalsIgnoreCase(cat)) return "Caffe";
        if ("Biscotti".equalsIgnoreCase(cat)) return "Secchi";
        if ("Crostate".equalsIgnoreCase(cat)) return "Classiche";
        if ("Tisane".equalsIgnoreCase(cat)) return "Infusi";
        return "Creme";
    }

    private void riempiFormGusto(GustoBean g) {
        if (campoNomeGustoMenu != null) campoNomeGustoMenu.setText(g.getNome());
        if (campoPrezzoGustoMenu != null) campoPrezzoGustoMenu.setText(String.valueOf(g.getPrezzoAlKg()));
        if (campoDescGustoMenu != null) campoDescGustoMenu.setText(g.getDescrizione());
        if (campoAllergeniGustoMenu != null) campoAllergeniGustoMenu.setText(g.getAllergeni());
        this.categoriaCorrenteSelezionata = g.getCategoria();
        this.sottocategoriaCorrenteSelezionata = g.getSottocategoria();
        aggiornaBadgeReparto();
        if (campoImmagineMenu != null) {
            campoImmagineMenu.setText(g.getImmagineUrl());
        }
        aggiornaStatoFoto(g.getImmagineUrl());
        Window finestra = ricavaFinestraAttiva();
        if (finestra != null) {
            StaffMenuEditorView.apriPopupEditorProdotto(
                    finestra, g, categoriaCorrenteSelezionata, sottocategoriaCorrenteSelezionata, creaAzioniPopup());
        }
    }

    private Window ricavaFinestraAttiva() {
        if (grigliaMenuGestione != null && grigliaMenuGestione.getScene() != null) {
            return grigliaMenuGestione.getScene().getWindow();
        }
        if (campoNomeGustoMenu != null && campoNomeGustoMenu.getScene() != null) {
            return campoNomeGustoMenu.getScene().getWindow();
        }
        return null;
    }

    private StaffMenuEditorView.AzioniEditorProdotto creaAzioniPopup() {
        return new StaffMenuEditorView.AzioniEditorProdotto(
                () -> scegliFotoERitornaPercorso(ricavaFinestraAttiva()),
                modificato -> {
                    this.categoriaCorrenteSelezionata = modificato.getCategoria();
                    this.sottocategoriaCorrenteSelezionata = modificato.getSottocategoria();
                    if (campoNomeGustoMenu != null) campoNomeGustoMenu.setText(modificato.getNome());
                    if (campoDescGustoMenu != null) campoDescGustoMenu.setText(modificato.getDescrizione());
                    if (campoAllergeniGustoMenu != null) campoAllergeniGustoMenu.setText(modificato.getAllergeni());
                    if (campoPrezzoGustoMenu != null) {
                        campoPrezzoGustoMenu.setText(modificato.getPrezzoAlKg() < 0 ? "" : String.valueOf(modificato.getPrezzoAlKg()));
                    }
                    if (campoImmagineMenu != null) campoImmagineMenu.setText(modificato.getImmagineUrl());
                    salvaOAggiornaGustoConDisponibilita(modificato.isDisponibileOggi());
                },
                nome -> {
                    if (campoNomeGustoMenu != null) campoNomeGustoMenu.setText(nome);
                    cambiaDisponibilitaGusto();
                },
                nome -> {
                    if (campoNomeGustoMenu != null) campoNomeGustoMenu.setText(nome);
                    eliminaGusto();
                }
        );
    }

    private void salvaOAggiornaGustoConDisponibilita(boolean disponibile) {
        try {
            String catDaSalvare = (categoriaCorrenteSelezionata != null && !categoriaCorrenteSelezionata.trim().isEmpty())
                    ? categoriaCorrenteSelezionata : "Gelati";
            String sottoDaSalvare = (sottocategoriaCorrenteSelezionata != null && !sottocategoriaCorrenteSelezionata.trim().isEmpty())
                    ? sottocategoriaCorrenteSelezionata : sottocategoriaPredefinita(catDaSalvare);

            new ProduzioneOrdiniControl().salvaOAggiornaGustoMenu(
                    campoNomeGustoMenu.getText(),
                    campoDescGustoMenu.getText(),
                    campoAllergeniGustoMenu.getText(),
                    campoPrezzoGustoMenu.getText(),
                    disponibile,
                    catDaSalvare,
                    sottoDaSalvare,
                    campoImmagineMenu == null ? "" : campoImmagineMenu.getText());
            notificaEsito.accept("✅ Menu aggiornato con successo: '" + campoNomeGustoMenu.getText() + "' salvato!", false);
            onAggiornaGlobale.run();
        } catch (FrostlyException e) {
            notificaEsito.accept("⚠️ " + e.getMessage(), true);
        }
    }

    private void aggiornaSottocategorieMenu(String categoria) {
        if (comboSottocategoriaMenu == null) {
            return;
        }
        List<String> opzioni = new ArrayList<>();
        if ("Gelati".equals(categoria)) {
            opzioni.addAll(Arrays.asList("Creme", "Frutta", "Cioccolato", "Special"));
        } else if ("Crepes".equals(categoria)) {
            opzioni.addAll(Arrays.asList("Farciture", "Topping"));
        } else if ("Panna".equals(categoria)) {
            opzioni.add("Porzioni");
        } else if ("Cioccolate calde".equals(categoria)) {
            opzioni.addAll(Arrays.asList("Classiche", "Speziate & Gourmet"));
        } else if ("Bevande".equals(categoria)) {
            opzioni.addAll(Arrays.asList("Acqua", "Bibite", "Succhi Naturali"));
        } else if ("Caffetteria".equals(categoria)) {
            opzioni.addAll(Arrays.asList("Caffe", "Specialita"));
        } else if ("Biscotti".equals(categoria)) {
            opzioni.add("Secchi");
        } else if ("Crostate".equals(categoria)) {
            opzioni.addAll(Arrays.asList("Classiche", "Alla frutta"));
        } else if ("Tisane".equals(categoria)) {
            opzioni.add("Infusi");
        }
        for (GustoBean g : menuCorrente.values()) {
            if (g.getCategoria().equals(categoria) && !opzioni.contains(g.getSottocategoria())) {
                opzioni.add(g.getSottocategoria());
            }
        }
        comboSottocategoriaMenu.setItems(FXCollections.observableArrayList(opzioni));
        if (!opzioni.isEmpty()) {
            comboSottocategoriaMenu.setValue(opzioni.get(0));
        }
    }
}
