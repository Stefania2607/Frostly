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
 * Stile Quiet Luxury: grafite satinato, avorio, accenti oro champagne, zero scrollbar orizzontali.
 */
public final class MagazzinoStaffView {

    private MagazzinoStaffView() {
    }

    public static Button creaChipCategoria(String testo, boolean attiva, Runnable onAzione) {
        Button chip = new Button(testo);
        chip.setCursor(javafx.scene.Cursor.HAND);
        chip.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 12; "
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
                Label nome = new Label(ing.getNome());
                nome.setPrefWidth(210);
                nome.setMinWidth(150);
                HBox.setHgrow(nome, Priority.ALWAYS);
                nome.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f5ebe0;");

                String cat = ing.getCategoria() != null && !ing.getCategoria().isEmpty()
                        ? ing.getCategoria() : "Laboratorio & Basi";
                Label badgeCat = new Label(iconaCategoria(cat) + " " + cat);
                badgeCat.setPrefWidth(130);
                badgeCat.setMinWidth(110);
                badgeCat.setAlignment(Pos.CENTER);
                badgeCat.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #baa487; "
                        + "-fx-background-color: rgba(38,35,40,0.85); -fx-padding: 3 8; -fx-background-radius: 8; "
                        + "-fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 8;");

                double riferimento = Math.max(ing.getSogliaMinima() * 2, 0.0001);
                ProgressBar barra = new ProgressBar(Math.min(1.0, ing.getQuantitaDisponibile() / riferimento));
                barra.setPrefWidth(140);
                barra.setMinWidth(100);
                barra.setStyle("-fx-accent: " + (basso ? "#e63946" : "#baa487") + ";");

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
                qta.setStyle("-fx-text-fill: #d5bdaf; -fx-font-size: 11.5px;");

                Label badge = new Label(basso ? "⚠ DA RIORDINARE" : "✔ OK");
                badge.setPrefWidth(115);
                badge.setAlignment(Pos.CENTER);
                badge.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 10; "
                        + (basso
                        ? "-fx-background-color: rgba(90,20,25,0.85); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444; -fx-border-radius: 10;"
                        : "-fx-background-color: rgba(25,60,42,0.85); -fx-text-fill: #a7f3d0; -fx-border-color: #10b981; -fx-border-radius: 10;"));

                HBox riga = new HBox(10, nome, badgeCat, barra, qta, badge);
                riga.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(riga);
                setStyle("-fx-background-color: " + (basso ? "rgba(45,20,24,0.75)" : "rgba(24,22,26,0.75)")
                        + "; -fx-padding: 7 12; -fx-border-color: transparent transparent rgba(186,164,135,0.15) transparent;");
            }
        });
    }
}
