package control.grafico;

import bean.GustoBean;
import bean.LoginBean;
import control.applicativo.LoginControl;
import control.applicativo.ProduzioneOrdiniControl;
import frostly.exception.FrostlyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import util.ModalitaEsecuzione;
import util.SessionManager;

/**
 * Controller Grafico JavaFX collegato a 'login.fxml', 'registrazione.fxml' e 'menu_pubblico.fxml'.
 * Gestisce Log In, Registrazione e consultazione libera del Menu Vetrina senza autenticazione.
 */
public class LoginControlGrafico extends Application {

    private static Stage finestraPrincipale;

    @FXML
    private ComboBox<ModalitaEsecuzione> comboModalita;
    @FXML
    private CheckBox checkSecondaGui;
    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoPassword;

    @FXML
    private TextField campoNomeReg;
    @FXML
    private TextField campoCognomeReg;
    @FXML
    private TextField campoEmailReg;
    @FXML
    private PasswordField campoPasswordReg;

    @FXML
    private HBox boxCategorie;
    @FXML
    private HBox boxSottocategorie;
    @FXML
    private FlowPane grigliaProdotti;

    private VetrinaMenu vetrina;

    @FXML
    private Label etichettaMessaggio;

    @Override
    public void start(Stage stage) {
        finestraPrincipale = stage;
        finestraPrincipale.setTitle("Frostly — Maestri Gelatieri dal 1984");
        cambiaSchermata("/login.fxml");
        finestraPrincipale.show();
    }

    public static void cambiaSchermata(String fileFxml) {
        try {
            Parent root = FXMLLoader.load(LoginControlGrafico.class.getResource(fileFxml));
            finestraPrincipale.setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Errore caricamento FXML: " + fileFxml, e);
        }
    }

    @FXML
    public void initialize() {
        if (comboModalita != null) {
            comboModalita.setItems(FXCollections.observableArrayList(ModalitaEsecuzione.values()));
            comboModalita.setValue(SessionManager.getInstance().getModalita());
        }
        if (grigliaProdotti != null) {
            caricaMenuPubblico();
        }
    }

    static String emojiCategoria(String categoria) {
        if ("Gelati".equalsIgnoreCase(categoria)) {
            return "🍨";
        } else if ("Caffetteria".equalsIgnoreCase(categoria)) {
            return "☕";
        } else if ("Biscotti".equalsIgnoreCase(categoria)) {
            return "🍪";
        } else if ("Crostate".equalsIgnoreCase(categoria)) {
            return "🥧";
        } else if ("Tisane".equalsIgnoreCase(categoria)) {
            return "🍵";
        }
        return "🍽";
    }

    private void caricaMenuPubblico() {
        vetrina = new VetrinaMenu(boxCategorie, boxSottocategorie, grigliaProdotti, null);
        vetrina.carica(new ProduzioneOrdiniControl().visualizzaMenuGusti());
    }

    /**
     * Vista a categorie, sottocategorie e schede prodotto con foto.
     * Riutilizzata sia dal menu pubblico (sola lettura) sia dalla gestione del Maestro e dei dipendenti,
     * dove un click su una scheda la seleziona e notifica il chiamante.
     */
    public static class VetrinaMenu {

        private static final List<String> ORDINE_CATEGORIE =
                Arrays.asList("Gelati", "Caffetteria", "Biscotti", "Crostate", "Tisane");

        private final HBox boxCategorie;
        private final HBox boxSottocategorie;
        private final FlowPane griglia;
        private final java.util.function.Consumer<GustoBean> allaSelezione;

        private List<GustoBean> prodotti = new ArrayList<>();
        private String categoriaSelezionata;
        private String sottocategoriaSelezionata;
        private String prodottoSelezionato;

        public VetrinaMenu(HBox boxCategorie, HBox boxSottocategorie, FlowPane griglia,
                           java.util.function.Consumer<GustoBean> allaSelezione) {
            this.boxCategorie = boxCategorie;
            this.boxSottocategorie = boxSottocategorie;
            this.griglia = griglia;
            this.allaSelezione = allaSelezione;
        }

        public void carica(List<GustoBean> nuoviProdotti) {
            prodotti = nuoviProdotti;
            List<String> categorie = categorieDisponibili();
            if (categoriaSelezionata == null || !categorie.contains(categoriaSelezionata)) {
                categoriaSelezionata = categorie.isEmpty() ? null : categorie.get(0);
                sottocategoriaSelezionata = null;
            }
            disegnaTutto(categorie);
        }

        /** Porta la vista sulla categoria del prodotto indicato e lo evidenzia. */
        public void mostraProdotto(GustoBean g) {
            categoriaSelezionata = g.getCategoria();
            sottocategoriaSelezionata = null;
            prodottoSelezionato = g.getNome();
            disegnaTutto(categorieDisponibili());
        }

        public void deseleziona() {
            prodottoSelezionato = null;
            disegnaProdotti();
        }

        private void disegnaTutto(List<String> categorie) {
            disegnaCategorie(categorie);
            disegnaSottocategorie();
            disegnaProdotti();
        }

        private List<String> categorieDisponibili() {
            Set<String> presenti = new LinkedHashSet<>();
            for (GustoBean g : prodotti) {
                presenti.add(g.getCategoria());
            }
            List<String> risultato = new ArrayList<>();
            for (String c : ORDINE_CATEGORIE) {
                if (presenti.remove(c)) {
                    risultato.add(c);
                }
            }
            risultato.addAll(presenti);
            return risultato;
        }

        private void disegnaCategorie(List<String> categorie) {
            boxCategorie.getChildren().clear();
            for (String c : categorie) {
                Button b = new Button(emojiCategoria(c) + "  " + c);
                boolean attiva = c.equals(categoriaSelezionata);
                b.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20;"
                        + " -fx-cursor: hand; "
                        + (attiva ? "-fx-background-color: #7f5539; -fx-text-fill: white;"
                        : "-fx-background-color: #ede0d4; -fx-text-fill: #5c3a21;"));
                b.setOnAction(e -> {
                    categoriaSelezionata = c;
                    sottocategoriaSelezionata = null;
                    disegnaTutto(categorie);
                });
                boxCategorie.getChildren().add(b);
            }
        }

        private void disegnaSottocategorie() {
            boxSottocategorie.getChildren().clear();
            Set<String> sotto = new LinkedHashSet<>();
            for (GustoBean g : prodotti) {
                if (g.getCategoria().equals(categoriaSelezionata)) {
                    sotto.add(g.getSottocategoria());
                }
            }
            if (sotto.size() < 2) {
                return;
            }
            boxSottocategorie.getChildren().add(chipSottocategoria("Tutti", null));
            for (String sc : sotto) {
                boxSottocategorie.getChildren().add(chipSottocategoria(sc, sc));
            }
        }

        private Button chipSottocategoria(String testo, String valore) {
            Button chip = new Button(testo);
            boolean attiva = Objects.equals(valore, sottocategoriaSelezionata);
            chip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 4 14;"
                    + " -fx-cursor: hand; "
                    + (attiva ? "-fx-background-color: #588157; -fx-text-fill: white;"
                    : "-fx-background-color: white; -fx-text-fill: #588157; -fx-border-color: #588157;"
                    + " -fx-border-radius: 14;"));
            chip.setOnAction(e -> {
                sottocategoriaSelezionata = valore;
                disegnaSottocategorie();
                disegnaProdotti();
            });
            return chip;
        }

        private void disegnaProdotti() {
            griglia.getChildren().clear();
            for (GustoBean g : prodotti) {
                if (!g.getCategoria().equals(categoriaSelezionata)) {
                    continue;
                }
                if (sottocategoriaSelezionata != null && !sottocategoriaSelezionata.equals(g.getSottocategoria())) {
                    continue;
                }
                griglia.getChildren().add(creaSchedaProdotto(g));
            }
        }

        private VBox creaSchedaProdotto(GustoBean g) {
            double larghezza = 190;
            double altezzaFoto = 125;

            // Segnaposto con emoji, sempre sotto la foto: resta visibile se il link manca o non si carica
            Label segnaposto = new Label(emojiCategoria(g.getCategoria()));
            segnaposto.setStyle("-fx-font-size: 48px;");
            StackPane foto = new StackPane(segnaposto);
            foto.setPrefSize(larghezza, altezzaFoto);
            foto.setMinSize(larghezza, altezzaFoto);
            foto.setMaxSize(larghezza, altezzaFoto);
            foto.setStyle("-fx-background-color: #f1e3d3; -fx-background-radius: 12 12 0 0;");

            String url = g.getImmagineUrl();
            if (url != null && !url.trim().isEmpty()) {
                try {
                    Image img = new Image(url.trim(), larghezza, altezzaFoto, false, true, true);
                    ImageView vista = new ImageView(img);
                    vista.setFitWidth(larghezza);
                    vista.setFitHeight(altezzaFoto);
                    vista.setPreserveRatio(false);
                    vista.setVisible(!img.isError());
                    img.errorProperty().addListener((o, vecchio, errore) -> vista.setVisible(!errore));
                    foto.getChildren().add(vista);
                } catch (IllegalArgumentException ignored) {
                    // Link non valido: resta il segnaposto
                }
            }
            Rectangle ritaglio = new Rectangle(larghezza, altezzaFoto);
            ritaglio.setArcWidth(24);
            ritaglio.setArcHeight(24);
            foto.setClip(ritaglio);

            Label nome = new Label(g.getNome());
            nome.setWrapText(true);
            nome.setMinHeight(38);
            nome.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3a2618;");

            Label descrizione = new Label(g.getDescrizione());
            descrizione.setWrapText(true);
            descrizione.setMinHeight(48);
            descrizione.setMaxHeight(48);
            descrizione.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b4f3a;");

            String unita = "Gelati".equalsIgnoreCase(g.getCategoria()) ? "/kg" : "";
            Label prezzo = new Label(String.format("EUR %.2f%s", g.getPrezzoAlKg(), unita));
            prezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #7f5539;");

            Label badge = new Label(g.isDisponibileOggi() ? "● Disponibile" : "● Esaurito");
            badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                    + (g.isDisponibileOggi() ? "#2a9d8f" : "#d62828") + ";");

            HBox riga = new HBox(prezzo, new Region(), badge);
            HBox.setHgrow(riga.getChildren().get(1), Priority.ALWAYS);
            riga.setAlignment(Pos.CENTER_LEFT);

            Label allergeni = new Label("Allergeni: " + g.getAllergeni());
            allergeni.setWrapText(true);
            allergeni.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #9c6644;");

            VBox testi = new VBox(4, nome, descrizione, riga, allergeni);
            testi.setStyle("-fx-padding: 10 12 12 12;");

            VBox scheda = new VBox(foto, testi);
            scheda.setPrefWidth(larghezza);
            scheda.setMinWidth(larghezza);
            scheda.setMaxWidth(larghezza);
            boolean selezionata = g.getNome().equals(prodottoSelezionato);
            scheda.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                    + (selezionata
                    ? " -fx-border-color: #588157; -fx-border-width: 3; -fx-border-radius: 12;"
                    + " -fx-effect: dropshadow(gaussian, rgba(88,129,87,0.6), 12, 0, 0, 0);"
                    : " -fx-effect: dropshadow(gaussian, rgba(60,40,20,0.25), 8, 0, 0, 2);"));
            if (!g.isDisponibileOggi()) {
                scheda.setOpacity(0.6);
            }
            if (allaSelezione != null) {
                scheda.setStyle(scheda.getStyle() + " -fx-cursor: hand;");
                scheda.setOnMouseClicked(e -> {
                    prodottoSelezionato = g.getNome();
                    disegnaProdotti();
                    allaSelezione.accept(g);
                });
            }
            return scheda;
        }
    }

    @FXML
    public void onClickLogin() {
        try {
            if (comboModalita != null && comboModalita.getValue() != null) {
                SessionManager.getInstance().setModalita(comboModalita.getValue());
            }

            LoginBean bean = new LoginBean(campoEmail.getText(), campoPassword.getText());
            LoginControl appControl = new LoginControl();
            LoginBean autenticato = appControl.autenticaUtente(bean);

            boolean usaGui2 = checkSecondaGui != null && checkSecondaGui.isSelected();
            String suffisso = usaGui2 ? "_gui2.fxml" : ".fxml";

            if (LoginControl.RUOLO_MAESTRO.equals(autenticato.getRuoloAutenticato())) {
                cambiaSchermata("/maestro" + suffisso);
            } else if (LoginControl.RUOLO_DIPENDENTE.equals(autenticato.getRuoloAutenticato())) {
                cambiaSchermata("/dipendente" + suffisso);
            } else {
                cambiaSchermata("/cliente" + suffisso);
            }
        } catch (FrostlyException e) {
            etichettaMessaggio.setStyle("-fx-text-fill: #b83b2b; -fx-font-weight: bold;");
            etichettaMessaggio.setText(e.getMessage());
        }
    }

    @FXML
    public void onClickApriMenuPubblico() {
        if (comboModalita != null && comboModalita.getValue() != null) {
            SessionManager.getInstance().setModalita(comboModalita.getValue());
        }
        cambiaSchermata("/menu_pubblico.fxml");
    }

    @FXML
    public void onClickVaiARegistrazione() {
        if (comboModalita != null && comboModalita.getValue() != null) {
            SessionManager.getInstance().setModalita(comboModalita.getValue());
        }
        cambiaSchermata("/registrazione.fxml");
    }

    @FXML
    public void onClickTornaALogin() {
        cambiaSchermata("/login.fxml");
    }

    /**
     * Notifica elegante a comparsa (stile "toast"): scheda bianca con segno di spunta verde che appare dall'alto,
     * resta visibile qualche secondo e svanisce da sola. Un click la chiude subito.
     */
    public static void mostraNotifica(Window finestra, String titolo, String messaggio) {
        if (finestra == null) {
            return;
        }
        double larghezza = 540;

        Label spunta = new Label("✓");
        spunta.setAlignment(Pos.CENTER);
        spunta.setMinSize(46, 46);
        spunta.setMaxSize(46, 46);
        spunta.setStyle("-fx-background-color: #588157; -fx-background-radius: 23; -fx-text-fill: white;"
                + " -fx-font-size: 24px; -fx-font-weight: bold;");

        Label lblTitolo = new Label(titolo);
        lblTitolo.setWrapText(true);
        lblTitolo.setMinHeight(Region.USE_PREF_SIZE);
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold;"
                + " -fx-text-fill: #3a2618;");
        Label lblMessaggio = new Label(messaggio);
        lblMessaggio.setWrapText(true);
        lblMessaggio.setMinHeight(Region.USE_PREF_SIZE);
        lblMessaggio.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f5539;");
        VBox testi = new VBox(4, lblTitolo, lblMessaggio);
        testi.setMinWidth(0);
        testi.setPrefWidth(larghezza - 46 - 16 - 48);
        testi.setMaxWidth(larghezza - 46 - 16 - 48);

        HBox scheda = new HBox(16, spunta, testi);
        scheda.setAlignment(Pos.CENTER_LEFT);
        scheda.setPrefWidth(larghezza);
        scheda.setMinWidth(larghezza);
        scheda.setMaxWidth(larghezza);
        scheda.setStyle("-fx-padding: 18 24; -fx-background-color: #fffdfa; -fx-background-radius: 18;"
                + " -fx-border-color: #d5bdaf; -fx-border-radius: 18; -fx-border-width: 1;"
                + " -fx-effect: dropshadow(gaussian, rgba(60,40,20,0.35), 22, 0.1, 0, 6); -fx-cursor: hand;");
        scheda.setOpacity(0);
        scheda.setTranslateY(-16);

        Popup popup = new Popup();
        popup.getContent().add(scheda);
        scheda.setOnMouseClicked(e -> popup.hide());

        double x = finestra.getX() + (finestra.getWidth() - larghezza) / 2;
        double y = finestra.getY() + 60;
        popup.show(finestra, x, y);

        FadeTransition compari = new FadeTransition(Duration.millis(350), scheda);
        compari.setToValue(1);
        TranslateTransition scendi = new TranslateTransition(Duration.millis(350), scheda);
        scendi.setToY(0);
        PauseTransition attesa = new PauseTransition(Duration.seconds(3));
        FadeTransition svanisci = new FadeTransition(Duration.millis(500), scheda);
        svanisci.setToValue(0);
        SequentialTransition sequenza = new SequentialTransition(
                new ParallelTransition(compari, scendi), attesa, svanisci);
        sequenza.setOnFinished(e -> popup.hide());
        sequenza.play();
    }

    @FXML
    public void onClickConfermaRegistrazione() {
        try {
            LoginBean regBean = new LoginBean();
            regBean.setNome(campoNomeReg.getText());
            regBean.setCognome(campoCognomeReg.getText());
            regBean.setEmail(campoEmailReg.getText());
            regBean.setPassword(campoPasswordReg.getText());
            regBean.setRuoloScelto(LoginControl.RUOLO_CLIENTE);

            LoginControl appControl = new LoginControl();
            LoginBean creato = appControl.registraUtente(regBean);

            etichettaMessaggio.setStyle("-fx-text-fill: #2e8b57; -fx-font-weight: bold;");
            etichettaMessaggio.setText("Registrazione completata per " + creato.getNomeCompleto()
                    + "! Clicca 'Torna all'Accesso' per entrare.");
            mostraNotifica(campoNomeReg.getScene().getWindow(), "Registrazione avvenuta con successo",
                    "Benvenuto in Frostly, " + creato.getNome() + ". Ora puoi accedere con le tue credenziali.");
            campoNomeReg.clear();
            campoCognomeReg.clear();
            campoEmailReg.clear();
            campoPasswordReg.clear();
        } catch (FrostlyException e) {
            etichettaMessaggio.setStyle("-fx-text-fill: #b83b2b; -fx-font-weight: bold;");
            etichettaMessaggio.setText(e.getMessage());
        }
    }
}
