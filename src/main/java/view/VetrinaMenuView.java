package view;

import bean.GustoBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Componente View dedicato alla presentazione estetica del catalogo vetrina,
 * al caricamento delle immagini prodotto e alle notifiche grafiche (toast).
 * Separa rigorosamente l'estetica dai Controller Grafici (pattern BCE).
 */
public class VetrinaMenuView {

    private static final List<String> ORDINE_CATEGORIE =
            Arrays.asList("Gelati", "Crepes", "Panna", "Cioccolate calde", "Bevande", "Caffetteria", "Biscotti", "Crostate", "Tisane");
    private static final Map<String, Image> CACHE_IMMAGINI = new HashMap<>();

    private final HBox boxCategorie;
    private final HBox boxSottocategorie;
    private final FlowPane griglia;
    private final Consumer<GustoBean> allaSelezione;
    private final BiConsumer<String, String> alCambioFiltro;

    private List<GustoBean> prodotti = new ArrayList<>();
    private String categoriaSelezionata;
    private String sottocategoriaSelezionata;
    private String prodottoSelezionato;

    public VetrinaMenuView(HBox boxCategorie, HBox boxSottocategorie, FlowPane griglia,
                           Consumer<GustoBean> allaSelezione) {
        this(boxCategorie, boxSottocategorie, griglia, allaSelezione, null);
    }

    public VetrinaMenuView(HBox boxCategorie, HBox boxSottocategorie, FlowPane griglia,
                           Consumer<GustoBean> allaSelezione,
                           BiConsumer<String, String> alCambioFiltro) {
        this.boxCategorie = boxCategorie;
        this.boxSottocategorie = boxSottocategorie;
        this.griglia = griglia;
        this.allaSelezione = allaSelezione;
        this.alCambioFiltro = alCambioFiltro;
    }

    public static String emojiCategoria(String categoria) {
        if ("Gelati".equalsIgnoreCase(categoria)) return "🍨";
        if ("Crepes".equalsIgnoreCase(categoria) || "Dolci".equalsIgnoreCase(categoria)) return "🥞";
        if ("Panna".equalsIgnoreCase(categoria)) return "☁️";
        if ("Cioccolate calde".equalsIgnoreCase(categoria)) return "🍫";
        if ("Bevande".equalsIgnoreCase(categoria)) return "🥤";
        if ("Caffetteria".equalsIgnoreCase(categoria)) return "☕";
        if ("Biscotti".equalsIgnoreCase(categoria)) return "🍪";
        if ("Crostate".equalsIgnoreCase(categoria)) return "🥧";
        if ("Tisane".equalsIgnoreCase(categoria)) return "🍵";
        return "🍽";
    }

    public static String risolviPercorsoImmagine(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String s = raw.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return s;
        }
        String nomeFile = s;
        int idxSlash = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (idxSlash >= 0) {
            nomeFile = s.substring(idxSlash + 1);
        }
        java.io.File fileLocale = new java.io.File("immagini_menu", nomeFile);
        if (fileLocale.exists()) {
            return fileLocale.toURI().toString();
        }
        java.net.URL res = VetrinaMenuView.class.getResource("/immagini_menu/" + nomeFile);
        if (res != null) {
            return res.toExternalForm();
        }
        java.io.File fileDiretto = new java.io.File(s);
        if (fileDiretto.exists()) {
            return fileDiretto.toURI().toString();
        }
        if (s.startsWith("file:")) {
            return s;
        }
        return null;
    }

    public static StackPane creaBoxFotoProdotto(String url, String fallbackEmoji,
                                                double larghezza, double altezzaFoto, double arc) {
        Label segnaposto = new Label(fallbackEmoji);
        segnaposto.setStyle("-fx-font-size: " + Math.max(18, (int) (altezzaFoto * 0.38)) + "px;");
        StackPane foto = new StackPane(segnaposto);
        foto.setPrefSize(larghezza, altezzaFoto);
        foto.setMinSize(larghezza, altezzaFoto);
        foto.setMaxSize(larghezza, altezzaFoto);
        foto.setStyle("-fx-background-color: #f1e3d3; -fx-background-radius: " + (int) (arc / 2) + ";");

        String uriCaricabile = risolviPercorsoImmagine(url);
        if (uriCaricabile != null) {
            try {
                double scalaRetina = 3.0;
                String chiaveCache = uriCaricabile + "#" + (int) (larghezza * scalaRetina);
                Image img = CACHE_IMMAGINI.computeIfAbsent(
                        chiaveCache,
                        k -> new Image(uriCaricabile, larghezza * scalaRetina, 0, true, true, false));
                if (!img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
                    ImageView vista = new ImageView(img);
                    applicaRitaglioCentrato(vista, img, larghezza, altezzaFoto);
                    vista.setFitWidth(larghezza);
                    vista.setFitHeight(altezzaFoto);
                    vista.setPreserveRatio(false);
                    vista.setSmooth(true);
                    foto.getChildren().add(vista);
                }
            } catch (IllegalArgumentException ignored) {
                // Resta il segnaposto
            }
        }
        Rectangle ritaglio = new Rectangle(larghezza, altezzaFoto);
        ritaglio.setArcWidth(arc);
        ritaglio.setArcHeight(arc);
        foto.setClip(ritaglio);
        return foto;
    }

    private static void applicaRitaglioCentrato(ImageView vista, Image img, double larghezzaBox, double altezzaBox) {
        double imgW = img.getWidth();
        double imgH = img.getHeight();
        if (imgW <= 0 || imgH <= 0 || larghezzaBox <= 0 || altezzaBox <= 0) {
            return;
        }
        double rapportoBox = larghezzaBox / altezzaBox;
        double rapportoImg = imgW / imgH;
        double cropW = imgW;
        double cropH = imgH;
        if (rapportoImg > rapportoBox) {
            cropW = imgH * rapportoBox;
        } else {
            cropH = imgW / rapportoBox;
        }
        double x = (imgW - cropW) / 2.0;
        double y = (imgH - cropH) / 2.0;
        vista.setViewport(new Rectangle2D(x, y, cropW, cropH));
    }

    public static void mostraMessaggioEsito(Label lbl, String msg, boolean errore) {
        if (lbl == null) return;
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + (errore ? "#d62828" : "#2a9d8f") + ";");
        lbl.setText(msg);
    }

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

    public String getCategoriaSelezionata() {
        return categoriaSelezionata;
    }

    public void carica(List<GustoBean> nuoviProdotti) {
        prodotti = nuoviProdotti;
        List<String> categorie = categorieDisponibili();
        if (categoriaSelezionata == null || !categorie.contains(categoriaSelezionata)) {
            categoriaSelezionata = categorie.isEmpty() ? null : categorie.get(0);
            sottocategoriaSelezionata = null;
        }
        if (alCambioFiltro != null && categoriaSelezionata != null) {
            alCambioFiltro.accept(categoriaSelezionata, sottocategoriaSelezionata);
        }
        disegnaTutto(categorie);
    }

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
        boxCategorie.setSpacing(6);
        for (String c : categorie) {
            Button b = new Button(emojiCategoria(c) + " " + c);
            b.setMinWidth(Region.USE_PREF_SIZE);
            boolean attiva = c.equals(categoriaSelezionata);
            b.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 6 12;"
                    + " -fx-cursor: hand; "
                    + (attiva ? "-fx-background-color: #7f5539; -fx-text-fill: white;"
                    : "-fx-background-color: #ede0d4; -fx-text-fill: #5c3a21;"));
            b.setOnAction(e -> {
                categoriaSelezionata = c;
                sottocategoriaSelezionata = null;
                if (alCambioFiltro != null) {
                    alCambioFiltro.accept(c, null);
                }
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
        chip.setMinWidth(Region.USE_PREF_SIZE);
        boolean attiva = Objects.equals(valore, sottocategoriaSelezionata);
        chip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 4 14;"
                + " -fx-cursor: hand; "
                + (attiva ? "-fx-background-color: #588157; -fx-text-fill: white;"
                : "-fx-background-color: white; -fx-text-fill: #588157; -fx-border-color: #588157;"
                + " -fx-border-radius: 14;"));
        chip.setOnAction(e -> {
            sottocategoriaSelezionata = valore;
            if (alCambioFiltro != null) {
                alCambioFiltro.accept(categoriaSelezionata, valore);
            }
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

    public static VBox creaCorpoSchedaProdotto(GustoBean g,
                                               double larghezza,
                                               double altezzaFoto,
                                               javafx.scene.Node nodoExtra,
                                               boolean selezionata) {
        StackPane foto = creaBoxFotoProdotto(g.getImmagineUrl(), emojiCategoria(g.getCategoria()), larghezza, altezzaFoto, 22);
        foto.setStyle("-fx-background-color: #f1e3d3; -fx-background-radius: 11 11 0 0;");

        Label nome = new Label(g.getNome());
        nome.setWrapText(true);
        nome.setPrefWidth(larghezza - 18);
        nome.setMinHeight(Region.USE_PREF_SIZE);
        nome.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #3a2618;");

        Label descrizione = new Label(g.getDescrizione());
        descrizione.setWrapText(true);
        descrizione.setPrefWidth(larghezza - 18);
        descrizione.setMaxHeight(32);
        descrizione.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #6b4f3a;");

        String unita = "Gelati".equalsIgnoreCase(g.getCategoria()) ? "/kg" : "";
        Label prezzo = new Label(String.format("EUR %.2f%s", g.getPrezzoAlKg(), unita));
        prezzo.setMinWidth(Region.USE_PREF_SIZE);
        prezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #7f5539;");

        Label badge = new Label(g.isDisponibileOggi() ? "● Disponibile" : "● Esaurito");
        badge.setMinWidth(Region.USE_PREF_SIZE);
        badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: "
                + (g.isDisponibileOggi() ? "#2a9d8f" : "#d62828") + ";");

        HBox riga = new HBox(4, prezzo, new Region(), badge);
        HBox.setHgrow(riga.getChildren().get(1), Priority.ALWAYS);
        riga.setAlignment(Pos.CENTER_LEFT);

        Label allergeni = new Label("Allergeni: " + g.getAllergeni());
        allergeni.setWrapText(true);
        allergeni.setPrefWidth(larghezza - 18);
        allergeni.setMaxHeight(26);
        allergeni.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #9c6644;");

        VBox testi = new VBox(3, nome, descrizione, riga, allergeni);
        if (nodoExtra != null) {
            testi.getChildren().add(nodoExtra);
        }
        testi.setStyle("-fx-padding: 7 9 9 9;");

        VBox scheda = new VBox(foto, testi);
        scheda.setPrefWidth(larghezza);
        scheda.setMinWidth(larghezza);
        scheda.setMaxWidth(larghezza);
        scheda.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                + (selezionata
                ? " -fx-border-color: #588157; -fx-border-width: 3; -fx-border-radius: 12;"
                + " -fx-effect: dropshadow(gaussian, rgba(88,129,87,0.6), 12, 0, 0, 0);"
                : " -fx-effect: dropshadow(gaussian, rgba(60,40,20,0.22), 7, 0, 0, 2);"));
        if (!g.isDisponibileOggi()) {
            scheda.setOpacity(0.6);
        }
        return scheda;
    }

    private VBox creaSchedaProdotto(GustoBean g) {
        boolean selezionata = g.getNome().equals(prodottoSelezionato);
        Button btnModifica = null;
        if (allaSelezione != null) {
            btnModifica = new Button("✏️ Modifica");
            btnModifica.setMaxWidth(Double.MAX_VALUE);
            btnModifica.setStyle("-fx-background-color: #7f5539; -fx-text-fill: white; -fx-font-weight: bold;"
                    + " -fx-font-size: 11.5px; -fx-background-radius: 9; -fx-padding: 5 10; -fx-cursor: hand;");
            btnModifica.setOnAction(e -> {
                prodottoSelezionato = g.getNome();
                disegnaProdotti();
                allaSelezione.accept(g);
            });
        }
        VBox scheda = creaCorpoSchedaProdotto(g, 214, 154, btnModifica, selezionata);
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
