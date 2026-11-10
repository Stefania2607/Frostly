package view;

import bean.GustoBean;
import bean.OrdineBean;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Componente View dedicato al rendering estetico del Menu per Categorie del Cliente:
 * - Griglia iniziale delle Macro-Categorie a icone (Gelati, Crepes, Panna, Bevande, Caffetteria, Biscotti, Crostate, Tisane)
 * - Griglia dei prodotti selezionabili per categoria/sottocategoria.
 * Per la composizione della Vaschetta vedere {@link VaschettaView};
 * per la cronologia ordini vedere {@link StoricoOrdiniClienteView};
 * per il configuratore Crepes vedere {@link CrepeConfiguratoreView};
 * per il Carrello vedere {@link CarrelloView}.
 */
public final class ClienteMenuView {

    private ClienteMenuView() {
        // Classe di utilità View
    }

    public static void evidenziaPulsanteCategoria(Button b, boolean attivo) {
        if (b == null) return;
        b.setMinWidth(Region.USE_PREF_SIZE);
        if (attivo) {
            b.setStyle("-fx-background-color: #7f5539; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 6 12; -fx-font-size: 12px;");
        } else {
            b.setStyle("-fx-background-color: #ede0d4; -fx-text-fill: #5c3a21; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
        }
    }

    public static void evidenziaPulsanteFormato(Button b, boolean attivo) {
        VaschettaView.evidenziaPulsanteFormato(b, attivo);
    }

    public static void renderBarraCategorieDinamica(HBox boxCategorie,
                                                    List<String> categorie,
                                                    String categoriaAttiva,
                                                    Consumer<String> onSelezionaCategoria) {
        if (boxCategorie == null) return;
        boxCategorie.getChildren().clear();
        if (categoriaAttiva == null || categoriaAttiva.isEmpty()) {
            boxCategorie.setVisible(false);
            boxCategorie.setManaged(false);
            return;
        }
        boxCategorie.setVisible(true);
        boxCategorie.setManaged(true);
        boxCategorie.setSpacing(6);

        Button btnHomeCategorie = new Button("⬅ Tutte le Categorie");
        btnHomeCategorie.setMinWidth(Region.USE_PREF_SIZE);
        btnHomeCategorie.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 6 14; -fx-font-size: 12px; -fx-cursor: hand;");
        btnHomeCategorie.setOnAction(e -> onSelezionaCategoria.accept(null));
        boxCategorie.getChildren().add(btnHomeCategorie);

        for (String c : categorie) {
            Button b = new Button(VetrinaMenuView.emojiCategoria(c) + " " + c);
            evidenziaPulsanteCategoria(b, c.equalsIgnoreCase(categoriaAttiva));
            b.setOnAction(e -> onSelezionaCategoria.accept(c));
            boxCategorie.getChildren().add(b);
        }
    }

    public static void renderGrigliaMacroCategorie(VBox contenitore,
                                                   List<String> categorie,
                                                   List<GustoBean> tuttiProdotti,
                                                   Consumer<String> onSelezionaCategoria) {
        if (contenitore == null) return;
        contenitore.getChildren().clear();

        HBox banner = new HBox(10);
        banner.setAlignment(Pos.CENTER);
        banner.setStyle("-fx-background-color: #faf0ca55; -fx-border-color: #ddb892; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8 16;");
        Label lblGuida = new Label("👇 Scegli una Macrocategoria qui sotto per visualizzare i prodotti e comporre il tuo ordine:");
        lblGuida.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #3a2618;");
        banner.getChildren().add(lblGuida);

        FlowPane griglia = new FlowPane(18, 18);
        griglia.setAlignment(Pos.CENTER);
        griglia.setStyle("-fx-padding: 8 4;");

        for (String cat : categorie) {
            int conteggio = 0;
            for (GustoBean g : tuttiProdotti) {
                if (cat.equalsIgnoreCase(g.getCategoria())) {
                    conteggio++;
                }
            }
            VBox card = creaSchedaMacroCategoria(cat, conteggio, () -> onSelezionaCategoria.accept(cat));
            griglia.getChildren().add(card);
        }

        contenitore.getChildren().addAll(banner, griglia);
    }

    private static VBox creaSchedaMacroCategoria(String categoria, int numeroProdotti, Runnable onClick) {
        double larghezza = 215;

        StackPane topIconBox = new StackPane();
        topIconBox.setPrefSize(larghezza, 82);
        topIconBox.setMinSize(larghezza, 82);
        topIconBox.setMaxSize(larghezza, 82);
        topIconBox.setStyle("-fx-background-color: " + coloreSfondoMacroCategoria(categoria)
                + "; -fx-background-radius: 14 14 0 0;");

        Label lblIconaGrande = new Label(VetrinaMenuView.emojiCategoria(categoria));
        lblIconaGrande.setStyle("-fx-font-size: 40px;");

        String unita = "Gelati".equalsIgnoreCase(categoria) ? (numeroProdotti + " gusti") : (numeroProdotti + " prodotti");
        Label badgeCount = new Label(unita);
        badgeCount.setStyle("-fx-background-color: rgba(58,38,24,0.82); -fx-text-fill: #fffdfa; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10;");
        StackPane.setAlignment(badgeCount, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeCount, new javafx.geometry.Insets(8, 8, 0, 0));

        topIconBox.getChildren().addAll(lblIconaGrande, badgeCount);

        Label lblTitolo = new Label(categoria);
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3a2618;");

        Label lblDesc = new Label(descrizioneMacroCategoria(categoria));
        lblDesc.setWrapText(true);
        lblDesc.setPrefWidth(larghezza - 24);
        lblDesc.setMinHeight(46);
        lblDesc.setAlignment(Pos.TOP_LEFT);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b4f3a;");

        Button btnApri = new Button("Apri " + categoria + " ➔");
        btnApri.setMaxWidth(Double.MAX_VALUE);
        btnApri.setMinHeight(Region.USE_PREF_SIZE);
        btnApri.setStyle("-fx-background-color: #7f5539; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 10; -fx-padding: 6 12; -fx-cursor: hand;");
        btnApri.setOnAction(e -> {
            onClick.run();
            e.consume();
        });

        VBox corpo = new VBox(6, lblTitolo, lblDesc, btnApri);
        corpo.setStyle("-fx-padding: 10 12 12 12;");

        VBox card = new VBox(topIconBox, corpo);
        card.setPrefWidth(larghezza);
        card.setMinWidth(larghezza);
        card.setMaxWidth(larghezza);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 14; -fx-border-color: #e6d5c3; -fx-border-radius: 14; -fx-border-width: 1.5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(60,40,20,0.22), 10, 0, 0, 3);");
        card.setOnMouseClicked(e -> onClick.run());
        return card;
    }

    private static String descrizioneMacroCategoria(String categoria) {
        if ("Gelati".equalsIgnoreCase(categoria)) {
            return "Componi le tue vaschette artigianali: Creme, Cioccolato, Frutta e Special.";
        }
        if ("Crepes".equalsIgnoreCase(categoria)) {
            return "Crepes calde farcite (Nutella, Visciole, Marmellata, Pan di Stelle) + Topping.";
        }
        if ("Panna".equalsIgnoreCase(categoria)) {
            return "Panna montata fresca d'alpeggio in porzioni da 100g, 200g e 300g.";
        }
        if ("Cioccolate calde".equalsIgnoreCase(categoria)) {
            return "Cioccolate calde dense artigianali: Fondente, Bianca, al Latte, Arancia & Cannella, Peperoncino e Gourmet.";
        }
        if ("Bevande".equalsIgnoreCase(categoria)) {
            return "Acqua naturale e frizzante, Coca Cola, Fanta e Succhi Naturali freschi.";
        }
        if ("Caffetteria".equalsIgnoreCase(categoria)) {
            return "Caffè espresso 100% Arabica, Cappuccino e Affogato al Caffè.";
        }
        if ("Biscotti".equalsIgnoreCase(categoria)) {
            return "Cantucci alle Mandorle e Baci di Dama di pasticceria artigianale.";
        }
        if ("Crostate".equalsIgnoreCase(categoria)) {
            return "Crostate di frolla al burro alla marmellata o ai frutti di bosco.";
        }
        if ("Tisane".equalsIgnoreCase(categoria)) {
            return "Infusi caldi rilassanti Zenzero & Limone e Frutti Rossi.";
        }
        return "Scopri e seleziona i prodotti artigianali di questa categoria.";
    }

    private static String coloreSfondoMacroCategoria(String categoria) {
        if ("Gelati".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #f5ebe0, #e3d5ca)";
        if ("Crepes".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #faedcd, #ddb892)";
        if ("Panna".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #fffdfa, #ede0d4)";
        if ("Cioccolate calde".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #e6ccb2, #b08968)";
        if ("Bevande".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #e0fbfc, #bde0fe)";
        if ("Caffetteria".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #ede0d4, #b08968)";
        if ("Biscotti".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #fefae0, #e9edc9)";
        if ("Crostate".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #ffedd8, #f3d5b5)";
        if ("Tisane".equalsIgnoreCase(categoria)) return "linear-gradient(to bottom right, #d8f3dc, #b7e4c7)";
        return "#f1e3d3";
    }

    public static void renderGrigliaProdottiSelezionabili(VBox contenitore,
                                                          List<GustoBean> prodottiCategoria,
                                                          String sottocategoriaAttiva,
                                                          Consumer<String> onSelezionaSottocategoria,
                                                          List<GustoBean> gustiNellaVaschetta,
                                                          Consumer<GustoBean> onClickGelato,
                                                          Consumer<GustoBean> onClickProdotto) {
        if (contenitore == null) return;
        contenitore.getChildren().clear();

        Set<String> sottoSet = new LinkedHashSet<>();
        for (GustoBean g : prodottiCategoria) {
            if (g.getSottocategoria() != null && !g.getSottocategoria().isEmpty()) {
                sottoSet.add(g.getSottocategoria());
            }
        }
        if (sottoSet.size() > 1 && onSelezionaSottocategoria != null) {
            HBox barraSotto = new HBox(8);
            barraSotto.setAlignment(Pos.CENTER);
            barraSotto.setStyle("-fx-padding: 2 0 6 0;");
            String attiva = sottocategoriaAttiva == null ? "Tutti" : sottocategoriaAttiva;
            barraSotto.getChildren().add(CrepeConfiguratoreView.creaChipOpzione(
                    "Tutti", "Tutti".equalsIgnoreCase(attiva), () -> onSelezionaSottocategoria.accept("Tutti")));
            for (String sc : sottoSet) {
                barraSotto.getChildren().add(CrepeConfiguratoreView.creaChipOpzione(
                    sc, sc.equalsIgnoreCase(attiva), () -> onSelezionaSottocategoria.accept(sc)));
            }
            contenitore.getChildren().add(barraSotto);
        }

        FlowPane griglia = new FlowPane(14, 14);
        griglia.setAlignment(Pos.TOP_CENTER);
        griglia.setStyle("-fx-padding: 4;");

        for (GustoBean g : prodottiCategoria) {
            if (sottocategoriaAttiva != null
                    && !"Tutti".equalsIgnoreCase(sottocategoriaAttiva)
                    && !sottocategoriaAttiva.equalsIgnoreCase(g.getSottocategoria())) {
                continue;
            }
            boolean isGelato = "Gelati".equalsIgnoreCase(g.getCategoria());
            boolean selezionato = isGelato && gustiNellaVaschetta.contains(g);
            String testoBottone;
            if (!g.isDisponibileOggi()) {
                testoBottone = "Esaurito";
            } else if (isGelato) {
                testoBottone = selezionato ? "✔ Nella Vaschetta" : "➕ Metti in Vaschetta";
            } else {
                testoBottone = "🛒 Aggiungi al Carrello";
            }
            VBox scheda = creaSchedaVisualeMenu(
                    g,
                    selezionato,
                    testoBottone,
                    () -> {
                        if (isGelato) {
                            onClickGelato.accept(g);
                        } else {
                            onClickProdotto.accept(g);
                        }
                    });
            griglia.getChildren().add(scheda);
        }
        contenitore.getChildren().add(griglia);
    }

    public static VBox creaSchedaVisualeMenu(GustoBean g,
                                             boolean selezionata,
                                             String testoAzione,
                                             Runnable onClickAzione) {
        Button btnAzione = new Button(testoAzione);
        btnAzione.setMaxWidth(Double.MAX_VALUE);
        btnAzione.setMinHeight(Region.USE_PREF_SIZE);
        btnAzione.setDisable(!g.isDisponibileOggi());
        btnAzione.setStyle(selezionata
                ? "-fx-background-color: #2a9d8f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-background-radius: 10; -fx-padding: 6 10; -fx-cursor: hand;"
                : "-fx-background-color: #7f5539; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-background-radius: 10; -fx-padding: 6 10; -fx-cursor: hand;");
        btnAzione.setOnAction(ev -> {
            onClickAzione.run();
            ev.consume();
        });

        VBox scheda = VetrinaMenuView.creaCorpoSchedaProdotto(g, 214, 154, btnAzione, selezionata);
        if (g.isDisponibileOggi()) {
            scheda.setStyle(scheda.getStyle() + " -fx-cursor: hand;");
            scheda.setOnMouseClicked(e -> onClickAzione.run());
        }
        return scheda;
    }

    public static void renderVaschettaGrafica(HBox boxVaschettaGrafica,
                                              List<GustoBean> gustiNellaVaschetta,
                                              int maxGustiSelezionabili,
                                              double pesoSelezionatoKg,
                                              boolean conPanna,
                                              boolean conGranella,
                                              Consumer<GustoBean> onRimuoviGusto) {
        VaschettaView.renderVaschettaGrafica(
                boxVaschettaGrafica, gustiNellaVaschetta, maxGustiSelezionabili,
                pesoSelezionatoKg, conPanna, conGranella, onRimuoviGusto);
    }

    public static void configuraCelleMieiOrdini(ListView<OrdineBean> listaMieiOrdini) {
        StoricoOrdiniClienteView.configuraCelleMieiOrdini(listaMieiOrdini);
    }

    public static String determinaEmojiGusto(String nome) {
        return VaschettaView.determinaEmojiGusto(nome);
    }

    public static String determinaColoreGusto(String nome) {
        return VaschettaView.determinaColoreGusto(nome);
    }
}
