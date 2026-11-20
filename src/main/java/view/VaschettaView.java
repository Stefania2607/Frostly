package view;

import bean.GustoBean;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Componente View dedicato alla rappresentazione visuale della Vaschetta di gelato:
 * - Formato vaschetta (500g, 750g, 1000g, 1500g)
 * - Rendering grafico degli scomparti (gusti inseriti e slot liberi)
 * - Mappatura colori ed emoji per ciascun gusto.
 */
public final class VaschettaView {

    private VaschettaView() {
    }

    public static void evidenziaPulsanteFormato(Button b, boolean attivo) {
        if (b == null) return;
        b.setMinWidth(Region.USE_PREF_SIZE);
        if (attivo) {
            b.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; -fx-border-color: #baa487; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 12;");
        } else {
            b.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-border-color: rgba(186, 164, 135, 0.25); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
        }
    }

    public static void renderVaschettaGrafica(HBox boxVaschettaGrafica,
                                              List<GustoBean> gustiNellaVaschetta,
                                              int maxGustiSelezionabili,
                                              double pesoSelezionatoKg,
                                              boolean conPanna,
                                              boolean conGranella,
                                              Consumer<GustoBean> onRimuoviGusto) {
        if (boxVaschettaGrafica == null) return;
        boxVaschettaGrafica.getChildren().clear();

        if (gustiNellaVaschetta.isEmpty() && conPanna) {
            VBox pannaCard = new VBox(4);
            pannaCard.setAlignment(Pos.CENTER);
            HBox.setHgrow(pannaCard, Priority.ALWAYS);
            pannaCard.setStyle("-fx-background-color: rgba(28, 25, 33, 0.85); -fx-background-radius: 10; -fx-padding: 12 8; -fx-border-color: rgba(186, 164, 135, 0.3); -fx-border-radius: 10;");

            Label lblIco = new Label("☁️");
            lblIco.setStyle("-fx-font-size: 22px;");

            Label lblTit = new Label("Vaschetta di Sola Panna Montata Fresca (" + ((int) (pesoSelezionatoKg * 1000)) + "g)");
            lblTit.setWrapText(true);
            lblTit.setAlignment(Pos.CENTER);
            lblTit.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #baa487;");

            pannaCard.getChildren().addAll(lblIco, lblTit);
            boxVaschettaGrafica.getChildren().add(pannaCard);
        } else {
            VBox grigliaScomparti = new VBox(6);
            grigliaScomparti.setAlignment(Pos.CENTER);
            HBox.setHgrow(grigliaScomparti, Priority.ALWAYS);

            HBox rigaCorrente = null;
            for (int i = 0; i < maxGustiSelezionabili; i++) {
                if (i % 2 == 0) {
                    rigaCorrente = new HBox(6);
                    rigaCorrente.setAlignment(Pos.CENTER);
                    grigliaScomparti.getChildren().add(rigaCorrente);
                }
                if (i < gustiNellaVaschetta.size()) {
                    GustoBean g = gustiNellaVaschetta.get(i);
                    HBox scomparto = new HBox(6);
                    scomparto.setAlignment(Pos.CENTER_LEFT);
                    scomparto.setPrefWidth(0);
                    scomparto.setMinHeight(46);
                    HBox.setHgrow(scomparto, Priority.ALWAYS);

                    String coloreGusto = determinaColoreGusto(g.getNome());
                    boolean testoChiaro = coloreGusto.equals("#4a2c1b") || coloreGusto.equals("#264653");
                    String coloreTesto = testoChiaro ? "#fffdfa" : "#1c1921";

                    scomparto.setStyle("-fx-background-color: " + coloreGusto + "; -fx-background-radius: 10; -fx-padding: 6 8; -fx-border-color: rgba(0,0,0,0.18); -fx-border-radius: 10;");

                    Label lblEmoji = new Label(determinaEmojiGusto(g.getNome()));
                    lblEmoji.setMinWidth(Region.USE_PREF_SIZE);
                    lblEmoji.setStyle("-fx-font-size: 15px;");

                    Label lblNome = new Label(g.getNome());
                    lblNome.setWrapText(true);
                    HBox.setHgrow(lblNome, Priority.ALWAYS);
                    lblNome.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + coloreTesto + ";");

                    Button btnRimuovi = new Button("✕");
                    btnRimuovi.setMinWidth(Region.USE_PREF_SIZE);
                    btnRimuovi.setStyle("-fx-font-size: 11.5px; -fx-padding: 2 6; -fx-background-color: rgba(0,0,0,0.22); -fx-text-fill: " + coloreTesto + "; -fx-background-radius: 6; -fx-cursor: hand;");
                    btnRimuovi.setOnAction(ev -> onRimuoviGusto.accept(g));

                    scomparto.getChildren().addAll(lblEmoji, lblNome, btnRimuovi);
                    rigaCorrente.getChildren().add(scomparto);
                } else {
                    HBox slotVuoto = new HBox(6);
                    slotVuoto.setAlignment(Pos.CENTER);
                    slotVuoto.setPrefWidth(0);
                    slotVuoto.setMinHeight(46);
                    HBox.setHgrow(slotVuoto, Priority.ALWAYS);
                    slotVuoto.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(186,164,135,0.3); -fx-border-style: dashed; -fx-border-width: 1.2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 6 8;");

                    Label lblPiu = new Label("＋");
                    lblPiu.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #baa487; -fx-font-weight: bold;");

                    Label lblSlot = new Label("Gusto " + (i + 1));
                    lblSlot.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92; -fx-font-style: italic;");

                    slotVuoto.getChildren().addAll(lblPiu, lblSlot);
                    rigaCorrente.getChildren().add(slotVuoto);
                }
            }
            boxVaschettaGrafica.getChildren().add(grigliaScomparti);
        }
    }

    public static String determinaEmojiGusto(String nome) {
        if (nome == null) return "🍨";
        String s = nome.toLowerCase();
        if (s.contains("crepe") || s.contains("pancake")) return "🥞";
        if (s.contains("coca") || s.contains("fanta") || s.contains("bibita")) return "🥤";
        if (s.contains("acqua")) return "💧";
        if (s.contains("snickers")) return "🥜";
        if (s.contains("bounty") || s.contains("cocco")) return "🥥";
        if (s.contains("amarandola") || s.contains("amarena") || s.contains("spagnola")) return "🍒";
        if (s.contains("zuppa inglese") || s.contains("delizia")) return "🍰";
        if (s.contains("galak")) return "🤍";
        if (s.contains("nutella") || s.contains("cremino")) return "🍫";
        if (s.contains("banana")) return "🍌";
        if (s.contains("fragola")) return "🍓";
        if (s.contains("lampone")) return "🫐";
        if (s.contains("anguria")) return "🍉";
        if (s.contains("melone")) return "🍈";
        if (s.contains("limone")) return "🍋";
        if (s.contains("menta")) return "🌿";
        if (s.contains("pesca") || s.contains("albicocca")) return "🍑";
        if (s.contains("liquirizia")) return "🖤";
        if (s.contains("pistacchio")) return "💚";
        if (s.contains("cioccolato") || s.contains("fondente") || s.contains("cacao")) return "🍫";
        if (s.contains("nocciola") || s.contains("bacio")) return "🌰";
        if (s.contains("mango") || s.contains("frutta")) return "🥭";
        if (s.contains("crema") || s.contains("vaniglia") || s.contains("fiordilatte") || s.contains("mascarpone")) return "🍦";
        if (s.contains("caffe") || s.contains("caffè") || s.contains("cappuccino")) return "☕";
        if (s.contains("crostata")) return "🥧";
        return "🍨";
    }

    public static String determinaColoreGusto(String nome) {
        if (nome == null) return "#ede0d4";
        String s = nome.toLowerCase();
        if (s.contains("liquirizia")) return "#264653";
        if (s.contains("nutella") || s.contains("cremino") || s.contains("caffe") || s.contains("caffè")) return "#4a2c1b";
        if (s.contains("snickers")) return "#d4a373";
        if (s.contains("bounty") || s.contains("galak")) return "#fffdfa";
        if (s.contains("amarandola") || s.contains("amarena") || s.contains("spagnola") || s.contains("zuppa inglese")) return "#f4acb7";
        if (s.contains("banana") && s.contains("cioccolato")) return "#e9c46a";
        if (s.contains("banana") || s.contains("delizia")) return "#fae588";
        if (s.contains("pistacchio")) return "#c7d9b7";
        if (s.contains("cioccolato") || s.contains("fondente")) return "#4a2c1b";
        if (s.contains("menta")) return "#a8e6cf";
        if (s.contains("melone")) return "#e2f0cb";
        if (s.contains("nocciola") || s.contains("bacio")) return "#ddb892";
        if (s.contains("crema") || s.contains("vaniglia") || s.contains("mascarpone")) return "#faedcd";
        if (s.contains("pesca") || s.contains("albicocca")) return "#ffcb9a";
        if (s.contains("mango") || s.contains("limone")) return "#ffd166";
        if (s.contains("fragola") || s.contains("lampone") || s.contains("anguria") || s.contains("frutti")) return "#f28482";
        if (s.contains("stracciatella")) return "#ffffff";
        return "#e8d5b5";
    }
}
