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
 */
public class VetrinaMenuView {

    private static final List<String> ORDINE_CATEGORIE =
            Arrays.asList("Gelati", "Crepes", "Panna", "Cioccolate calde", "Bevande", "Caffetteria", "Biscotti", "Crostate", "Tisane");
    private static final Map<String, Image> CACHE_IMMAGINI = new HashMap<>();
    private static Popup toastAttivo;

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
        boolean haTesto = msg != null && !msg.isBlank();
        if (lbl.getParent() != null && !lbl.getParent().isVisible() && haTesto) {
            lbl.getParent().setVisible(true);
            lbl.getParent().setManaged(true);
        }
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + (errore ? "#ef4444" : "#baa487") + ";");
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
        spunta.setStyle("-fx-background-color: #baa487; -fx-background-radius: 23; -fx-text-fill: #141312;"
                + " -fx-font-size: 24px; -fx-font-weight: bold;");

        Label lblTitolo = new Label(titolo);
        lblTitolo.setWrapText(true);
        lblTitolo.setMinHeight(Region.USE_PREF_SIZE);
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold;"
                + " -fx-text-fill: #f5ebe0;");
        Label lblMessaggio = new Label(messaggio);
        lblMessaggio.setWrapText(true);
        lblMessaggio.setMinHeight(Region.USE_PREF_SIZE);
        lblMessaggio.setStyle("-fx-font-size: 13px; -fx-text-fill: #ded7cd;");
        VBox testi = new VBox(4, lblTitolo, lblMessaggio);
        testi.setMinWidth(0);
        testi.setPrefWidth(larghezza - 46 - 16 - 48);
        testi.setMaxWidth(larghezza - 46 - 16 - 48);

        HBox scheda = new HBox(16, spunta, testi);
        scheda.setAlignment(Pos.CENTER_LEFT);
        scheda.setPrefWidth(larghezza);
        scheda.setMinWidth(larghezza);
        scheda.setMaxWidth(larghezza);
        scheda.setStyle("-fx-padding: 18 24; -fx-background-color: rgba(20,18,22,0.96); -fx-background-radius: 18;"
                + " -fx-border-color: rgba(186,164,135,0.45); -fx-border-radius: 18; -fx-border-width: 1.2;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.70), 22, 0.1, 0, 6); -fx-cursor: hand;");
        scheda.setOpacity(0);
        scheda.setTranslateY(-16);

        Popup popup = new Popup();
        TemaProvenzaHelper.applicaSeGui2(scheda);
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

    public static void mostraToast(Window finestra, String messaggio, boolean errore) {
        if (finestra == null || messaggio == null || messaggio.isBlank()) {
            return;
        }
        if (toastAttivo != null && toastAttivo.isShowing()) {
            toastAttivo.hide();
        }

        double larghezza = 520;
        String coloreAccento = errore ? "#ef4444" : "#baa487";
        String simbolo = errore ? "!" : "✓";

        Label icona = new Label(simbolo);
        icona.setAlignment(Pos.CENTER);
        icona.setMinSize(32, 32);
        icona.setMaxSize(32, 32);
        icona.setStyle("-fx-background-color: " + coloreAccento + "; -fx-background-radius: 16; -fx-text-fill: #141312;"
                + " -fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblMessaggio = new Label(messaggio);
        lblMessaggio.setWrapText(true);
        lblMessaggio.setMinHeight(Region.USE_PREF_SIZE);
        lblMessaggio.setMaxWidth(larghezza - 32 - 14 - 44);
        lblMessaggio.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        HBox scheda = new HBox(14, icona, lblMessaggio);
        scheda.setAlignment(Pos.CENTER_LEFT);
        scheda.setMaxWidth(larghezza);
        scheda.setStyle("-fx-padding: 12 20; -fx-background-color: rgba(20, 18, 22, 0.97); -fx-background-radius: 14;"
                + " -fx-border-color: " + coloreAccento + "; -fx-border-radius: 14; -fx-border-width: 1.4;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 20, 0.15, 0, 6); -fx-cursor: hand;");
        scheda.setOpacity(0);
        scheda.setTranslateY(12);

        Popup popup = new Popup();
        popup.setAutoFix(true);
        TemaProvenzaHelper.applicaSeGui2(scheda);
        popup.getContent().add(scheda);
        scheda.setOnMouseClicked(e -> popup.hide());
        toastAttivo = popup;

        popup.show(finestra);
        double wReale = scheda.prefWidth(-1);
        double hReale = scheda.prefHeight(wReale);
        double x = finestra.getX() + (finestra.getWidth() - Math.min(larghezza, wReale)) / 2.0;
        double y = finestra.getY() + finestra.getHeight() - hReale - 36;
        popup.setX(x);
        popup.setY(y);

        FadeTransition compari = new FadeTransition(Duration.millis(240), scheda);
        compari.setToValue(1);
        TranslateTransition sali = new TranslateTransition(Duration.millis(240), scheda);
        sali.setToY(0);
        PauseTransition attesa = new PauseTransition(Duration.seconds(errore ? 3.5 : 2.5));
        FadeTransition svanisci = new FadeTransition(Duration.millis(400), scheda);
        svanisci.setToValue(0);
        SequentialTransition sequenza = new SequentialTransition(
                new ParallelTransition(compari, sali), attesa, svanisci);
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
        boxCategorie.setSpacing(7);
        boolean provenza = util.SessionManager.getInstance().isGui2Attiva();
        for (String c : categorie) {
            Button b = new Button(emojiCategoria(c) + " " + c);
            b.setMinWidth(Region.USE_PREF_SIZE);
            b.setMinHeight(Region.USE_PREF_SIZE);
            boolean attiva = c.equals(categoriaSelezionata);
            if (provenza) {
                b.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 5 10; -fx-cursor: hand; "
                        + (attiva ? "-fx-background-color: #764c86; -fx-text-fill: #ffffff;"
                        : "-fx-background-color: #f3eae5; -fx-text-fill: #37253d; -fx-border-color: #e2d3df; -fx-border-radius: 16;"));
            } else {
                b.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 5 11;"
                        + " -fx-cursor: hand; "
                        + (attiva ? "-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-effect: dropshadow(gaussian, rgba(186,164,135,0.4), 6, 0, 0, 1);"
                        : "-fx-background-color: rgba(28,26,30,0.85); -fx-text-fill: #baa487; -fx-border-color: rgba(186,164,135,0.32); -fx-border-radius: 16;"));
            }
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
        boolean provenza = util.SessionManager.getInstance().isGui2Attiva();
        if (provenza) {
            chip.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 4 13; -fx-cursor: hand; "
                    + (attiva ? "-fx-background-color: #764c86; -fx-text-fill: #ffffff;"
                    : "-fx-background-color: #f3eae5; -fx-text-fill: #5e525e; -fx-border-color: #e2d3df; -fx-border-radius: 16;"));
        } else {
            chip.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 4 14;"
                    + " -fx-cursor: hand; "
                    + (attiva ? "-fx-background-color: #baa487; -fx-text-fill: #141312;"
                    : "-fx-background-color: rgba(28,26,30,0.85); -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.25);"
                    + " -fx-border-radius: 14;"));
        }
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
        boolean provenza = util.SessionManager.getInstance().isGui2Attiva();
        StackPane foto = creaBoxFotoProdotto(g.getImmagineUrl(), emojiCategoria(g.getCategoria()), larghezza, altezzaFoto, 22);
        foto.setStyle(provenza
                ? "-fx-background-color: #eee1d7; -fx-background-radius: 13 13 0 0; -fx-border-color: #eadde3; -fx-border-width: 0 0 1 0;"
                : "-fx-background-color: rgba(18,16,20,0.95); -fx-background-radius: 11 11 0 0; -fx-border-color: rgba(186,164,135,0.18); -fx-border-width: 0 0 1 0;");

        Label nome = new Label(g.getNome());
        nome.setWrapText(true);
        nome.setPrefWidth(larghezza - 18);
        nome.setMinHeight(Region.USE_PREF_SIZE);
        nome.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        Label descrizione = new Label(g.getDescrizione());
        descrizione.setWrapText(true);
        descrizione.setPrefWidth(larghezza - 18);
        descrizione.setMaxHeight(32);
        descrizione.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (provenza ? "#5e525e;" : "#ded7cd;"));

        String unita = "Gelati".equalsIgnoreCase(g.getCategoria()) ? "/kg" : "";
        Label prezzo = new Label(String.format("EUR %.2f%s", g.getPrezzoAlKg(), unita));
        prezzo.setMinWidth(Region.USE_PREF_SIZE);
        prezzo.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: " + (provenza ? "#5e316f;" : "#baa487;"));

        String testoBadge;
        String coloreBadge;
        if (!g.isDisponibileOggi()) {
            testoBadge = "● Esaurito";
            coloreBadge = provenza ? "#ad443f" : "#fca5a5";
        } else if (!"Gelati".equalsIgnoreCase(g.getCategoria()) && g.getVaschetteDisponibili() > 0) {
            int qtaDisp = g.getVaschetteDisponibili();
            if (qtaDisp <= 5) {
                testoBadge = "● Ultime " + qtaDisp + " disp.";
                coloreBadge = provenza ? "#b45309" : "#fcd34d";
            } else {
                testoBadge = "● " + qtaDisp + " disp.";
                coloreBadge = provenza ? "#2e7d32" : "#a7f3d0";
            }
        } else {
            testoBadge = "● Disponibile";
            coloreBadge = provenza ? "#2e7d32" : "#a7f3d0";
        }
        Label badge = new Label(testoBadge);
        badge.setMinWidth(Region.USE_PREF_SIZE);
        badge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + coloreBadge + ";");

        HBox riga = new HBox(4, prezzo, new Region(), badge);
        HBox.setHgrow(riga.getChildren().get(1), Priority.ALWAYS);
        riga.setAlignment(Pos.CENTER_LEFT);

        Label allergeni = new Label("Allergeni: " + g.getAllergeni());
        allergeni.setWrapText(true);
        allergeni.setPrefWidth(larghezza - 18);
        allergeni.setMaxHeight(26);
        allergeni.setStyle("-fx-font-size: 11.5px; -fx-text-fill: " + (provenza ? "#756978;" : "#a89f92;"));

        VBox testi = new VBox(3, nome, descrizione, riga, allergeni);
        if (nodoExtra != null) {
            testi.getChildren().add(nodoExtra);
        }
        testi.setStyle("-fx-padding: 7 9 9 9;");

        VBox scheda = new VBox(foto, testi);
        scheda.setPrefWidth(larghezza);
        scheda.setMinWidth(larghezza);
        scheda.setMaxWidth(larghezza);
        if (provenza) {
            scheda.setStyle("-fx-background-color: #fffcf9; -fx-background-radius: 14;"
                    + (selezionata
                    ? " -fx-border-color: #764c86; -fx-border-width: 2; -fx-border-radius: 14;"
                    : " -fx-border-color: #eadde3; -fx-border-width: 1; -fx-border-radius: 14;"));
        } else {
            scheda.setStyle("-fx-background-color: rgba(26,24,28,0.92); -fx-background-radius: 12;"
                    + (selezionata
                    ? " -fx-border-color: #baa487; -fx-border-width: 2.2; -fx-border-radius: 12;"
                    + " -fx-effect: dropshadow(gaussian, rgba(186,164,135,0.45), 14, 0, 0, 2);"
                    : " -fx-border-color: rgba(186,164,135,0.28); -fx-border-width: 1; -fx-border-radius: 12;"
                    + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0, 0, 3);"));
        }
        if (!g.isDisponibileOggi()) {
            scheda.setOpacity(0.55);
        }
        return scheda;
    }

    private VBox creaSchedaProdotto(GustoBean g) {
        boolean selezionata = g.getNome().equals(prodottoSelezionato);
        Button btnModifica = null;
        if (allaSelezione != null) {
            btnModifica = new Button("✏️ Modifica");
            btnModifica.setMaxWidth(Double.MAX_VALUE);
            boolean provenza = util.SessionManager.getInstance().isGui2Attiva();
            btnModifica.setStyle(provenza
                    ? "-fx-background-color: linear-gradient(to right, #9366a0, #6d427b); -fx-text-fill: #ffffff; -fx-font-weight: bold;"
                      + " -fx-font-size: 12.5px; -fx-background-radius: 14; -fx-padding: 5 10; -fx-cursor: hand;"
                    : "-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold;"
                      + " -fx-font-size: 13px; -fx-background-radius: 9; -fx-padding: 5 10; -fx-cursor: hand;");
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

    public static void aggiornaStatoFotoLabel(Label lblStatoFotoMenu, Button btnRimuoviFoto, String nomeFile) {
        if (lblStatoFotoMenu == null) return;
        if (nomeFile == null || nomeFile.trim().isEmpty()) {
            lblStatoFotoMenu.setText("Nessuna foto selezionata");
            lblStatoFotoMenu.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #9c6644; -fx-font-style: italic;");
            if (btnRimuoviFoto != null) {
                btnRimuoviFoto.setVisible(false);
                btnRimuoviFoto.setManaged(false);
            }
        } else {
            String pulito = nomeFile;
            int idx = Math.max(nomeFile.lastIndexOf('/'), nomeFile.lastIndexOf('\\'));
            if (idx >= 0) pulito = nomeFile.substring(idx + 1);
            if (pulito.contains("_") && pulito.substring(0, pulito.indexOf('_')).matches("^\\d{10,}$")) {
                pulito = pulito.substring(pulito.indexOf('_') + 1);
            }
            lblStatoFotoMenu.setText("🖼️ Foto: " + pulito);
            lblStatoFotoMenu.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #2a9d8f; -fx-font-weight: bold;");
            if (btnRimuoviFoto != null) {
                btnRimuoviFoto.setVisible(true);
                btnRimuoviFoto.setManaged(true);
            }
        }
    }
}
