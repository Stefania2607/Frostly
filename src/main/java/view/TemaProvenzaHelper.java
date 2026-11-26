package view;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import util.SessionManager;

/**
 * Helper che converte automaticamente gli stili scuri dei pop-up e delle finestre modali
 * nel tema chiaro Provenza (crema e lavanda) quando la GUI 2 è attiva.
 * In GUI 1 non applica alcuna modifica.
 */
public final class TemaProvenzaHelper {

    private static final Pattern DARK_RGBA = Pattern.compile(
            "rgba\\(\\s*([0-4]?\\d)\\s*,\\s*([0-4]?\\d)\\s*,\\s*([0-5]?\\d)\\s*,\\s*[0-9.]+\\s*\\)");
    private static final Pattern WHITE_LOW_RGBA = Pattern.compile(
            "rgba\\(\\s*255\\s*,\\s*255\\s*,\\s*255\\s*,\\s*0?\\.[0-2]\\d*\\s*\\)");
    private static final Pattern GOLD_RGBA = Pattern.compile(
            "rgba\\(\\s*(186|212|214)\\s*,\\s*(164|180|175)\\s*,\\s*(135|131|138|55)\\s*,\\s*[0-9.]+\\s*\\)");

    private TemaProvenzaHelper() {
    }

    public static void applicaSeGui2(Scene scene) {
        if (!SessionManager.getInstance().isGui2Attiva() || scene == null) {
            return;
        }
        scene.setFill(Color.web("#fbf6f1"));
        URL css = TemaProvenzaHelper.class.getResource("/style_gui2.css");
        if (css != null) {
            String ext = css.toExternalForm();
            if (!scene.getStylesheets().contains(ext)) {
                scene.getStylesheets().add(ext);
            }
        }
        if (scene.getRoot() != null) {
            applicaRicorsivo(scene.getRoot(), true);
        }
    }

    public static void applicaSeGui2(Node root) {
        if (!SessionManager.getInstance().isGui2Attiva() || root == null) {
            return;
        }
        applicaRicorsivo(root, true);
    }

    private static void applicaRicorsivo(Node node, boolean isRoot) {
        if (node == null) {
            return;
        }
        String style = node.getStyle();
        if (style != null && !style.isBlank()) {
            String converted = convertiStile(style, isRoot, node instanceof ButtonBase);
            if (!converted.equals(style)) {
                node.setStyle(converted);
            }
        }
        if (node instanceof ScrollPane sp && sp.getContent() != null) {
            applicaRicorsivo(sp.getContent(), false);
        }
        if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                applicaRicorsivo(child, false);
            }
        }
    }

    private static String convertiStile(String raw, boolean isRoot, boolean isButton) {
        String s = raw;

        // 1. Bottoni primari oro -> viola lavanda Provenza con testo bianco
        if (isButton && (s.contains("linear-gradient(to bottom, #baa487")
                || s.contains("linear-gradient(to bottom, #d6b48a")
                || s.contains("-fx-background-color: #baa487")
                || s.contains("-fx-background-color: #d4b483"))) {
            s = s.replace("linear-gradient(to bottom, #baa487, #8c7355)", "linear-gradient(to right, #9366a0, #6d427b)")
                 .replace("linear-gradient(to bottom, #d6b48a, #9e764d)", "linear-gradient(to right, #9366a0, #6d427b)")
                 .replace("-fx-background-color: #baa487", "-fx-background-color: #764c86")
                 .replace("-fx-background-color: #d4b483", "-fx-background-color: #764c86")
                 .replace("-fx-text-fill: #141216", "-fx-text-fill: #ffffff")
                 .replace("-fx-text-fill: #141312", "-fx-text-fill: #ffffff")
                 .replace("-fx-text-fill: #16141a", "-fx-text-fill: #ffffff");
            return s;
        }

        // 2. Sfondi scuri -> crema/porcellana Provenza
        String bgTarget = isRoot ? "#fbf6f1" : "#fffcf9";
        s = s.replace("#141216", bgTarget)
             .replace("#141312", bgTarget)
             .replace("#121114", bgTarget)
             .replace("#16141a", bgTarget)
             .replace("#18161a", "#fffaf7")
             .replace("#1a1820", "#ffffff")
             .replace("#1c1921", "#ffffff")
             .replace("#1e1b24", "#fffcf9")
             .replace("#221f28", "#f3eae5");

        // Dark rgba(...) backgrounds -> crema / bianco
        Matcher mDark = DARK_RGBA.matcher(s);
        if (mDark.find()) {
            s = mDark.replaceAll(isRoot ? "#fbf6f1" : "#fffcf9");
        }

        // Subtle white-transparent fills/borders -> crema/lavanda tenue
        Matcher mWhite = WHITE_LOW_RGBA.matcher(s);
        if (mWhite.find()) {
            s = mWhite.replaceAll("#e9dbdf");
        }

        // Gold-transparent borders/fills -> lavanda tenue
        Matcher mGold = GOLD_RGBA.matcher(s);
        if (mGold.find()) {
            s = mGold.replaceAll("#e2d3df");
        }

        // 3. Testi chiari su scuro -> prugna scuro / viola lavanda leggibile
        s = s.replace("#f5ebe0", "#37253d")
             .replace("#faf4e6", "#37253d")
             .replace("#ded7cd", "#5e525e")
             .replace("#d5bdaf", "#5e525e")
             .replace("#c7beb2", "#6e6172")
             .replace("#a89f92", "#756978")
             .replace("#8e8e93", "#756978")
             .replace("#8c837b", "#756978")
             .replace("#e5e5ea", "#37253d")
             .replace("#baa487", "#764c86")
             .replace("#d4b483", "#5e316f")
             .replace("#d6b48a", "#764c86");

        // Badge/testi verdi e rossi scuri -> pastello leggibile su chiaro
        s = s.replaceAll("rgba\\(\\s*2[0-9]\\s*,\\s*[56][0-9]\\s*,\\s*[34][0-9]\\s*,\\s*[0-9.]+\\s*\\)", "#e6f4ea")
             .replaceAll("rgba\\(\\s*[6-9][0-9]\\s*,\\s*2[0-9]\\s*,\\s*2[0-9]\\s*,\\s*[0-9.]+\\s*\\)", "#fde8e8")
             .replace("#a7f3d0", "#1e6b3a")
             .replace("#fca5a5", "#b91c1c")
             .replace("#fcd34d", "#9a5b13");

        return s;
    }
}
