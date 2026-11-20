package view;

import bean.GustoBean;
import frostly.exception.FrostlyException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Componente View dedicato alla finestra modale di Modifica / Creazione di un singolo
 * prodotto del Menu Vetrina, riservata esclusivamente a Maestro Gelataio e Dipendenti.
 */
public final class StaffMenuEditorView {

    private static final String STILE_LABEL_CAMPO = "-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #f5ebe0;";
    private static final List<String> CATEGORIE = Arrays.asList(
            "Gelati", "Crepes", "Panna", "Cioccolate calde", "Bevande", "Caffetteria", "Biscotti", "Crostate", "Tisane");

    private StaffMenuEditorView() {
    }

    @FunctionalInterface
    public interface SalvaProdottoCallback {
        void salva(GustoBean bean) throws FrostlyException;
    }

    public static class AzioniEditorProdotto {
        private final Supplier<String> onScegliFotoDalPc;
        private final SalvaProdottoCallback onSalvaProdotto;
        private final Consumer<String> onCambiaDisponibilita;
        private final Consumer<String> onEliminaProdotto;

        public AzioniEditorProdotto(Supplier<String> onScegliFotoDalPc,
                                    SalvaProdottoCallback onSalvaProdotto,
                                    Consumer<String> onCambiaDisponibilita,
                                    Consumer<String> onEliminaProdotto) {
            this.onScegliFotoDalPc = onScegliFotoDalPc;
            this.onSalvaProdotto = onSalvaProdotto;
            this.onCambiaDisponibilita = onCambiaDisponibilita;
            this.onEliminaProdotto = onEliminaProdotto;
        }
    }

    public static void apriPopupEditorProdotto(Window owner,
                                               GustoBean prodottoIniziale,
                                               String categoriaDefault,
                                               String sottocategoriaDefault,
                                               AzioniEditorProdotto azioni) {
        boolean isNuovo = (prodottoIniziale == null);
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(isNuovo
                ? "Frostly — Aggiungi Nuovo Prodotto al Menu"
                : "Frostly — Modifica Prodotto: " + prodottoIniziale.getNome());
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setStyle("-fx-padding: 22 26; -fx-background-color: rgba(18,17,20,0.97); -fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");
        root.setPrefWidth(620);

        Label lblTitolo = new Label(isNuovo
                ? "➕ NUOVO PRODOTTO DEL MENU"
                : "✏️ MODIFICA PRODOTTO: " + prodottoIniziale.getNome());
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 19px; -fx-text-fill: #f5ebe0;");

        String catIniziale = !isNuovo && prodottoIniziale.getCategoria() != null
                ? prodottoIniziale.getCategoria()
                : (categoriaDefault != null ? categoriaDefault : "Gelati");
        String sottoIniziale = !isNuovo && prodottoIniziale.getSottocategoria() != null
                ? prodottoIniziale.getSottocategoria()
                : (sottocategoriaDefault != null ? sottocategoriaDefault : sottocategoriePer(catIniziale).get(0));
        final String[] urlFotoCorrente = { !isNuovo && prodottoIniziale.getImmagineUrl() != null ? prodottoIniziale.getImmagineUrl() : "" };
        final boolean[] disponibileCorrente = { isNuovo || prodottoIniziale.isDisponibileOggi() };

        // Sezione superiore: Anteprima Foto a sinistra + Reparto e Stato a destra
        HBox boxSuperiore = new HBox(16);
        boxSuperiore.setAlignment(Pos.CENTER_LEFT);
        boxSuperiore.setStyle("-fx-background-color: rgba(26,24,28,0.92); -fx-border-color: rgba(186,164,135,0.25); -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12;");

        StackPane contenitoreAnteprima = new StackPane();
        Runnable aggiornaAnteprimaFoto = () -> {
            contenitoreAnteprima.getChildren().setAll(
                    VetrinaMenuView.creaBoxFotoProdotto(urlFotoCorrente[0], VetrinaMenuView.emojiCategoria(catIniziale), 175, 125, 18));
        };
        aggiornaAnteprimaFoto.run();

        VBox controlliDestraFoto = new VBox(8);
        HBox.setHgrow(controlliDestraFoto, Priority.ALWAYS);

        Label lblFotoInfo = new Label(urlFotoCorrente[0].isEmpty() ? "Nessuna foto caricata" : ("🖼 Foto: " + estraiNomeFile(urlFotoCorrente[0])));
        lblFotoInfo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5c3a21;");

        HBox bottoniFoto = new HBox(8);
        Button btnScegliFoto = new Button("📁 Scegli foto dal PC…");
        btnScegliFoto.setStyle("-fx-background-color: #d4a373; -fx-text-fill: #2b1810; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 6 12; -fx-cursor: hand;");
        Button btnRimuoviFoto = new Button("✕ Rimuovi foto");
        btnRimuoviFoto.setStyle("-fx-background-color: #f5ebe0; -fx-text-fill: #bc4749; -fx-font-weight: bold; -fx-border-color: #d5bdaf; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 6 10; -fx-cursor: hand;");
        btnRimuoviFoto.setVisible(!urlFotoCorrente[0].isEmpty());

        btnScegliFoto.setOnAction(e -> {
            String nuovoPercorso = azioni.onScegliFotoDalPc.get();
            if (nuovoPercorso != null && !nuovoPercorso.isEmpty()) {
                urlFotoCorrente[0] = nuovoPercorso;
                lblFotoInfo.setText("🖼 Foto: " + estraiNomeFile(nuovoPercorso));
                btnRimuoviFoto.setVisible(true);
                aggiornaAnteprimaFoto.run();
            }
        });
        btnRimuoviFoto.setOnAction(e -> {
            urlFotoCorrente[0] = "";
            lblFotoInfo.setText("Nessuna foto caricata");
            btnRimuoviFoto.setVisible(false);
            aggiornaAnteprimaFoto.run();
        });
        bottoniFoto.getChildren().addAll(btnScegliFoto, btnRimuoviFoto);

        HBox rigaReparto = new HBox(8);
        rigaReparto.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> comboCat = new ComboBox<>(FXCollections.observableArrayList(CATEGORIE));
        comboCat.setValue(catIniziale);
        comboCat.setPrefWidth(150);

        ComboBox<String> comboSotto = new ComboBox<>(FXCollections.observableArrayList(sottocategoriePer(catIniziale)));
        comboSotto.setValue(sottoIniziale);
        comboSotto.setPrefWidth(150);

        comboCat.setOnAction(e -> {
            List<String> nuoveSotto = sottocategoriePer(comboCat.getValue());
            comboSotto.setItems(FXCollections.observableArrayList(nuoveSotto));
            if (!nuoveSotto.isEmpty()) {
                comboSotto.setValue(nuoveSotto.get(0));
            }
        });
        rigaReparto.getChildren().addAll(new Label("Reparto:"), comboCat, comboSotto);

        Button btnToggleDisp = new Button();
        Runnable aggiornaTestoDisp = () -> {
            if (disponibileCorrente[0]) {
                btnToggleDisp.setText("🟢 Stato: Disponibile in Vetrina (clicca per segnare Esaurito)");
                btnToggleDisp.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2a9d8f; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;");
            } else {
                btnToggleDisp.setText("🔴 Stato: Esaurito Oggi (clicca per rimettere Disponibile)");
                btnToggleDisp.setStyle("-fx-background-color: #fceae8; -fx-text-fill: #bc4749; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 5 10; -fx-cursor: hand;");
            }
        };
        aggiornaTestoDisp.run();
        btnToggleDisp.setOnAction(e -> {
            disponibileCorrente[0] = !disponibileCorrente[0];
            aggiornaTestoDisp.run();
            if (!isNuovo) {
                azioni.onCambiaDisponibilita.accept(prodottoIniziale.getNome());
            }
        });

        controlliDestraFoto.getChildren().addAll(lblFotoInfo, bottoniFoto, rigaReparto, btnToggleDisp);
        boxSuperiore.getChildren().addAll(contenitoreAnteprima, controlliDestraFoto);

        // Campi del modulo
        TextField txtNome = new TextField(isNuovo ? "" : prodottoIniziale.getNome());
        txtNome.setPromptText("Es. Pistacchio di Bronte DOP, Crepe alla Nutella...");
        txtNome.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.trim().isEmpty()) txtNome.setStyle("");
        });

        HBox rigaPrezzoAllergeni = new HBox(12);
        VBox boxPrezzo = new VBox(4);
        Label lblPrezzo = new Label("Prezzo (€/kg per Gelati, € per gli altri):");
        lblPrezzo.setStyle(STILE_LABEL_CAMPO);
        TextField txtPrezzo = new TextField(isNuovo ? "" : String.valueOf(prodottoIniziale.getPrezzoAlKg()));
        txtPrezzo.setPromptText("Es. 26.0");
        txtPrezzo.setPrefWidth(180);
        txtPrezzo.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.trim().isEmpty()) txtPrezzo.setStyle("");
        });
        boxPrezzo.getChildren().addAll(lblPrezzo, txtPrezzo);

        VBox boxAllergeni = new VBox(4);
        HBox.setHgrow(boxAllergeni, Priority.ALWAYS);
        Label lblAllergeni = new Label("Allergeni:");
        lblAllergeni.setStyle(STILE_LABEL_CAMPO);
        TextField txtAllergeni = new TextField(isNuovo ? "" : prodottoIniziale.getAllergeni());
        txtAllergeni.setPromptText("Es. Latte, Uova, Glutine, Frutta a guscio o Senza Lattosio");
        boxAllergeni.getChildren().addAll(lblAllergeni, txtAllergeni);
        rigaPrezzoAllergeni.getChildren().addAll(boxPrezzo, boxAllergeni);

        Label lblNome = new Label("Nome Prodotto:");
        lblNome.setStyle(STILE_LABEL_CAMPO);

        Label lblDesc = new Label("Descrizione Artigianale:");
        lblDesc.setStyle(STILE_LABEL_CAMPO);
        TextField txtDesc = new TextField(isNuovo ? "" : prodottoIniziale.getDescrizione());
        txtDesc.setPromptText("Descrivi gli ingredienti e la lavorazione artigianale...");
        txtDesc.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.trim().isEmpty()) txtDesc.setStyle("");
        });

        // Barra pulsanti in basso
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        if (!isNuovo) {
            Button btnElimina = new Button("🗑️ Elimina dal Menu");
            btnElimina.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 8 14; -fx-cursor: hand;");
            btnElimina.setOnAction(e -> {
                popup.close();
                azioni.onEliminaProdotto.accept(prodottoIniziale.getNome());
            });
            footer.getChildren().add(btnElimina);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: rgba(38,35,40,0.85); -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: rgba(186,164,135,0.3);");
        btnAnnulla.setOnAction(e -> popup.close());

        Button btnSalva = new Button("💾 Salva Modifiche");
        btnSalva.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 14; -fx-padding: 8 20; -fx-cursor: hand;");
        btnSalva.setOnAction(e -> {
            String nomeVal = txtNome.getText() != null ? txtNome.getText().trim() : "";
            if (nomeVal.isEmpty()) {
                txtNome.setStyle("-fx-border-color: #e76f51; -fx-border-width: 1.5; -fx-border-radius: 6;");
                txtNome.requestFocus();
                mostraPopupAvviso(popup, "Nome Mancante", "Inserisci il nome del prodotto da aggiungere o aggiornare nel menu.");
                return;
            }

            String descVal = txtDesc.getText() != null ? txtDesc.getText().trim() : "";
            if (descVal.isEmpty()) {
                txtDesc.setStyle("-fx-border-color: #e76f51; -fx-border-width: 1.5; -fx-border-radius: 6;");
                txtDesc.requestFocus();
                mostraPopupAvviso(popup, "Descrizione Mancante", "Inserisci una descrizione artigianale per il prodotto. Descrivi gli ingredienti e la lavorazione per i tuoi clienti!");
                return;
            }

            double prezzoVal;
            try {
                prezzoVal = Double.parseDouble(txtPrezzo.getText().trim().replace(",", "."));
                if (prezzoVal <= 0) {
                    throw new NumberFormatException();
                }
            } catch (Exception ex) {
                txtPrezzo.setStyle("-fx-border-color: #e76f51; -fx-border-width: 1.5; -fx-border-radius: 6;");
                txtPrezzo.requestFocus();
                mostraPopupAvviso(popup, "Prezzo Non Valido", "Inserisci un prezzo numerico valido maggiore di zero (es. 2.50 o 24.0).");
                return;
            }

            GustoBean modificato = new GustoBean(
                    nomeVal,
                    descVal,
                    txtAllergeni.getText(),
                    prezzoVal,
                    disponibileCorrente[0],
                    0);
            modificato.setCategoria(comboCat.getValue());
            modificato.setSottocategoria(comboSotto.getValue());
            modificato.setImmagineUrl(urlFotoCorrente[0]);

            try {
                azioni.onSalvaProdotto.salva(modificato);
                popup.close();
            } catch (FrostlyException ex) {
                mostraPopupAvviso(popup, "Attenzione", ex.getMessage());
            }
        });

        footer.getChildren().addAll(spacer, btnAnnulla, btnSalva);

        root.getChildren().addAll(
                lblTitolo,
                boxSuperiore,
                lblNome, txtNome,
                rigaPrezzoAllergeni,
                lblDesc, txtDesc,
                new Separator(),
                footer);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    private static List<String> sottocategoriePer(String categoria) {
        if ("Gelati".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Creme", "Cioccolato", "Frutta", "Special");
        }
        if ("Crepes".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Farciture", "Topping");
        }
        if ("Panna".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Porzioni");
        }
        if ("Cioccolate calde".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Classiche", "Speziate & Gourmet");
        }
        if ("Bevande".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Acqua", "Bibite", "Succhi Naturali");
        }
        if ("Caffetteria".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Caffe", "Specialita");
        }
        if ("Biscotti".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Secchi");
        }
        if ("Crostate".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Classiche", "Alla frutta", "Special");
        }
        if ("Tisane".equalsIgnoreCase(categoria)) {
            return Arrays.asList("Tè Neri", "Tè Verdi & Bianchi", "Tisane Benessere", "Infusi & Frutta");
        }
        return Arrays.asList("Generale");
    }

    private static String estraiNomeFile(String path) {
        if (path == null || path.isEmpty()) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    public static void mostraPopupAvviso(Window owner, String titolo, String messaggio) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle("Frostly — " + (titolo != null ? titolo : "Attenzione"));
        dialog.setResizable(false);

        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 24 26 20 26; -fx-background-color: #fffdfa; -fx-border-color: #ddb892; "
                + "-fx-border-width: 1.5; -fx-border-radius: 14; -fx-background-radius: 14;");
        root.setPrefWidth(460);

        // Header con icona avviso circolare ed eleganza tipografica
        HBox headerBox = new HBox(14);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconBadge = new Label("⚠️");
        iconBadge.setStyle("-fx-font-size: 24px; -fx-background-color: #fdf0ed; -fx-padding: 8 12; "
                + "-fx-background-radius: 50; -fx-border-color: #e76f51; -fx-border-radius: 50; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        VBox titleBox = new VBox(2);
        Label lblBrand = new Label("FROSTLY — AVVISO COMPILAZIONE");
        lblBrand.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #bc4749; -fx-letter-spacing: 0.8px;");

        Label lblTit = new Label(titolo != null ? titolo : "Attenzione");
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 17.5px; -fx-text-fill: #3a2618;");
        titleBox.getChildren().addAll(lblBrand, lblTit);

        headerBox.getChildren().addAll(iconBadge, titleBox);

        // Card centrale esplicativa
        VBox cardInfo = new VBox(8);
        cardInfo.setStyle("-fx-background-color: #faf4ed; -fx-border-color: #e8ddd0; -fx-border-radius: 12; "
                + "-fx-background-radius: 12; -fx-padding: 14 16;");

        Label lblMsg = new Label(messaggio != null ? messaggio : "");
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a2c1b; -fx-line-spacing: 2.5px; -fx-font-weight: 500;");

        cardInfo.getChildren().add(lblMsg);

        // Footer pulsante Ho Capito
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-padding: 4 0 0 0;");

        Button btnOk = new Button("Ho Capito ➔");
        btnOk.setStyle("-fx-background-color: #3a2618; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13.5px; -fx-background-radius: 14; -fx-padding: 8 22; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(58,38,24,0.3), 6, 0, 0, 2);");
        btnOk.setOnMouseEntered(e -> btnOk.setStyle("-fx-background-color: #5c3a21; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13.5px; -fx-background-radius: 14; -fx-padding: 8 22; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(58,38,24,0.45), 8, 0, 0, 3);"));
        btnOk.setOnMouseExited(e -> btnOk.setStyle("-fx-background-color: #3a2618; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13.5px; -fx-background-radius: 14; -fx-padding: 8 22; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(58,38,24,0.3), 6, 0, 0, 2);"));
        btnOk.setOnAction(e -> dialog.close());

        footer.getChildren().add(btnOk);
        root.getChildren().addAll(headerBox, cardInfo, footer);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}
