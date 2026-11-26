package view;

import bean.IngredienteBean;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Componente View dedicato esclusivamente alla renderizzazione delle celle del magazzino materie prime e prodotti.
 */
public final class MagazzinoStaffView {

    private MagazzinoStaffView() {
    }

    public static Button creaChipCategoria(String testo, boolean attiva, Runnable onAzione) {
        Button chip = new Button(testo);
        chip.setCursor(javafx.scene.Cursor.HAND);
        chip.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-background-radius: 12; "
                + "-fx-padding: 5 12; -fx-cursor: hand; "
                + (attiva ? "-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-effect: dropshadow(gaussian, rgba(186,164,135,0.4), 6, 0, 0, 1);"
                : "-fx-background-color: rgba(26,24,28,0.85); -fx-text-fill: #baa487; -fx-border-color: rgba(186,164,135,0.32); -fx-border-radius: 12;"));
        chip.setOnAction(e -> {
            if (onAzione != null) onAzione.run();
        });
        return chip;
    }

    public static String iconaCategoria(String cat) {
        if (cat == null) return "📦";
        String c = cat.toLowerCase();
        if (c.contains("tè") || c.contains("tisane")) return "🍵";
        if (c.contains("caffett")) return "☕";
        if (c.contains("gelat")) return "🍨";
        if (c.contains("biscott")) return "🍪";
        if (c.contains("crostat")) return "🥧";
        if (c.contains("cioccolat")) return "🍫";
        if (c.contains("bevand")) return "🥤";
        if (c.contains("crepe")) return "🥞";
        if (c.contains("panna")) return "🥛";
        return "📦";
    }

    public static void configuraCelleMagazzino(ListView<IngredienteBean> listaMagazzino) {
        if (listaMagazzino == null) return;
        listaMagazzino.setCellFactory(lv -> new ListCell<IngredienteBean>() {
            {
                selectedProperty().addListener((obs, oldSel, newSel) -> applicaStileMagazzino());
            }

            private void applicaStileMagazzino() {
                IngredienteBean ing = getItem();
                if (isEmpty() || ing == null) {
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                boolean basso = ing.isSottoSoglia();
                boolean provenza = RecensioniView.isTemaSalottoChiaro(listaMagazzino);
                if (provenza) {
                    if (isSelected()) {
                        setStyle("-fx-background-color: #eee2ee; -fx-padding: 7 12; "
                                + "-fx-border-color: #764c86; -fx-border-width: 1.3; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;");
                    } else {
                        setStyle("-fx-background-color: " + (basso ? "#fdf2f2" : "#ffffff")
                                + "; -fx-padding: 7 12; -fx-border-color: transparent transparent #e9dbdf transparent; -fx-cursor: hand;");
                    }
                    return;
                }
                if (isSelected()) {
                    setStyle("-fx-background-color: rgba(186, 164, 135, 0.24); -fx-padding: 7 12; "
                            + "-fx-border-color: #d4b483; -fx-border-width: 1.2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
                } else {
                    setStyle("-fx-background-color: " + (basso ? "rgba(45,20,24,0.75)" : "rgba(24,22,26,0.75)")
                            + "; -fx-padding: 7 12; -fx-border-color: transparent transparent rgba(186,164,135,0.15) transparent; -fx-cursor: hand;");
                }
            }

            @Override
            protected void updateItem(IngredienteBean ing, boolean empty) {
                super.updateItem(ing, empty);
                if (empty || ing == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                boolean basso = ing.isSottoSoglia();
                boolean provenza = RecensioniView.isTemaSalottoChiaro(listaMagazzino);
                Label nome = new Label(ing.getNome());
                nome.setPrefWidth(210);
                nome.setMinWidth(150);
                HBox.setHgrow(nome, Priority.ALWAYS);
                nome.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

                String cat = ing.getCategoria() != null && !ing.getCategoria().isEmpty()
                        ? ing.getCategoria() : "Laboratorio & Basi";
                Label badgeCat = new Label(iconaCategoria(cat) + " " + cat);
                badgeCat.setPrefWidth(130);
                badgeCat.setMinWidth(110);
                badgeCat.setAlignment(Pos.CENTER);
                badgeCat.setStyle(provenza
                        ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #5e316f; "
                          + "-fx-background-color: #f3eae5; -fx-padding: 3 8; -fx-background-radius: 10; "
                          + "-fx-border-color: #ddcddc; -fx-border-radius: 10;"
                        : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #baa487; "
                          + "-fx-background-color: rgba(38,35,40,0.85); -fx-padding: 3 8; -fx-background-radius: 8; "
                          + "-fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 8;");

                double riferimento = Math.max(ing.getSogliaMinima() * 2, 0.0001);
                ProgressBar barra = new ProgressBar(Math.min(1.0, ing.getQuantitaDisponibile() / riferimento));
                barra.setPrefWidth(140);
                barra.setMinWidth(100);
                barra.setMouseTransparent(true);
                barra.setFocusTraversable(false);
                barra.setStyle("-fx-accent: " + (basso ? "#e63946" : (provenza ? "#764c86" : "#baa487")) + ";");

                String qtaStr = (ing.getQuantitaDisponibile() % 1 == 0)
                        ? String.valueOf((long) ing.getQuantitaDisponibile())
                        : String.format(Locale.US, "%.1f", ing.getQuantitaDisponibile());
                String sogliaStr = (ing.getSogliaMinima() % 1 == 0)
                        ? String.valueOf((long) ing.getSogliaMinima())
                        : String.format(Locale.US, "%.1f", ing.getSogliaMinima());

                Label qta = new Label(qtaStr + " " + ing.getUnitaMisura()
                        + " (min. " + sogliaStr + ")");
                qta.setPrefWidth(140);
                qta.setMinWidth(120);
                qta.setStyle("-fx-text-fill: " + (provenza ? "#5e525e" : "#d5bdaf") + "; -fx-font-size: 13px;");

                Label badge = new Label(basso ? "⚠ DA RIORDINARE" : "✔ OK");
                badge.setPrefWidth(115);
                badge.setAlignment(Pos.CENTER);
                badge.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 10; "
                        + (provenza
                        ? (basso
                           ? "-fx-background-color: #fde8e8; -fx-text-fill: #b91c1c; -fx-border-color: #fca5a5; -fx-border-radius: 10;"
                           : "-fx-background-color: #e6f4ea; -fx-text-fill: #1e6b3a; -fx-border-color: #a7d7b8; -fx-border-radius: 10;")
                        : (basso
                           ? "-fx-background-color: rgba(90,20,25,0.85); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444; -fx-border-radius: 10;"
                           : "-fx-background-color: rgba(25,60,42,0.85); -fx-text-fill: #a7f3d0; -fx-border-color: #10b981; -fx-border-radius: 10;")));

                HBox riga = new HBox(10, nome, badgeCat, barra, qta, badge);
                riga.setAlignment(Pos.CENTER_LEFT);
                riga.setMouseTransparent(true);
                setText(null);
                setGraphic(riga);
                applicaStileMagazzino();
            }
        });
    }

    public static void configuraCelleUrgenzeMagazzino(ListView<String> listaUrgenze) {
        if (listaUrgenze == null) return;
        collegaAltezzaDinamica(listaUrgenze, 230);
        listaUrgenze.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                if (item.startsWith("✅")) {
                    setGraphic(creaRigaStatoOk(item, listaUrgenze));
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 2 0;");
                    return;
                }

                String[] parti = item.split("\\|", 2);
                String nome = parti[0].replace("⚠️", "").trim();
                String dettaglio = parti.length > 1 ? parti[1].trim() : "";

                javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(2);
                card.setMouseTransparent(true);
                card.maxWidthProperty().bind(listaUrgenze.widthProperty().subtract(10));
                card.setStyle("-fx-background-color: rgba(255, 69, 58, 0.12); -fx-border-color: rgba(255, 69, 58, 0.35); "
                        + "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 11; -fx-cursor: hand;");

                Label lblNome = new Label(nome);
                lblNome.setWrapText(true);
                lblNome.setStyle("-fx-text-fill: #f5f5f7; -fx-font-size: 12.5px; -fx-font-weight: bold;");

                Label lblDet = new Label(dettaglio);
                lblDet.setWrapText(true);
                lblDet.setStyle("-fx-text-fill: #ff8a80; -fx-font-size: 11.5px;");

                card.getChildren().addAll(lblNome, lblDet);
                setGraphic(card);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
            }
        });
    }

    public static void configuraCelleListaTendina(ListView<String> lista, String coloreBordo) {
        if (lista == null) return;
        collegaAltezzaDinamica(lista, 180);
        lista.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                if (item.startsWith("✅")) {
                    setGraphic(creaRigaStatoOk(item, lista));
                } else if (item.split("\\|").length == 5) {
                    String[] p = item.split("\\|", 5);
                    Label lblCod = new Label(p[0].trim());
                    lblCod.setStyle("-fx-background-color: rgba(212, 180, 131, 0.18); -fx-text-fill: #e5c99b; "
                            + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 6;");
                    lblCod.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

                    Label lblCli = new Label(p[1].trim());
                    lblCli.setStyle("-fx-text-fill: #f5f5f7; -fx-font-size: 12.5px; -fx-font-weight: bold;");
                    HBox.setHgrow(lblCli, Priority.ALWAYS);

                    Label lblPrezzo = new Label(p[4].trim());
                    lblPrezzo.setStyle("-fx-text-fill: #e5c99b; -fx-font-size: 12.5px; -fx-font-weight: bold;");
                    lblPrezzo.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

                    HBox riga1 = new HBox(8, lblCod, lblCli, lblPrezzo);
                    riga1.setAlignment(Pos.CENTER_LEFT);

                    Label lblSub = new Label(p[2].trim() + "  •  " + p[3].trim());
                    lblSub.setWrapText(true);
                    lblSub.setStyle("-fx-text-fill: #aeaeb2; -fx-font-size: 11.5px;");

                    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(5, riga1, lblSub);
                    card.setMouseTransparent(true);
                    card.maxWidthProperty().bind(lista.widthProperty().subtract(10));
                    card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-color: " + coloreBordo + "; "
                            + "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 9 11; -fx-cursor: hand;");
                    setGraphic(card);
                } else {
                    Label lbl = new Label(item);
                    lbl.setWrapText(true);
                    lbl.maxWidthProperty().bind(lista.widthProperty().subtract(28));
                    lbl.setStyle("-fx-text-fill: #f5f5f7; -fx-font-size: 12.5px;");

                    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(lbl);
                    card.maxWidthProperty().bind(lista.widthProperty().subtract(10));
                    card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-color: " + coloreBordo + "; "
                            + "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 11; -fx-cursor: hand;");
                    setGraphic(card);
                }
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
            }
        });
    }

    private static HBox creaRigaStatoOk(String item, ListView<String> lista) {
        String testoPulito = item.replace("✅", "").trim();
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: #30d158; -fx-font-size: 9px;");
        dot.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);

        Label lbl = new Label(testoPulito);
        lbl.setWrapText(true);
        lbl.maxWidthProperty().bind(lista.widthProperty().subtract(34));
        lbl.setStyle("-fx-text-fill: #98d8b6; -fx-font-size: 12.5px;");

        HBox riga = new HBox(8, dot, lbl);
        riga.setAlignment(Pos.CENTER_LEFT);
        riga.setStyle("-fx-background-color: rgba(48, 209, 88, 0.08); -fx-background-radius: 10; -fx-padding: 8 10;");
        return riga;
    }

    private static void collegaAltezzaDinamica(ListView<String> lista, int maxAltezza) {
        Runnable aggiorna = () -> {
            int n = lista.getItems() == null ? 0 : lista.getItems().size();
            if (n <= 1 && (n == 0 || lista.getItems().get(0).startsWith("✅"))) {
                lista.setPrefHeight(46);
                lista.setMinHeight(46);
                lista.setMaxHeight(54);
            } else {
                double h = Math.min(maxAltezza, n * 70.0 + 12.0);
                lista.setPrefHeight(h);
                lista.setMinHeight(74);
                lista.setMaxHeight(maxAltezza);
            }
        };
        lista.getItems().addListener((javafx.collections.ListChangeListener<String>) c -> aggiorna.run());
        aggiorna.run();
    }
}
