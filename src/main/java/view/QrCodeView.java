package view;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Desktop;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Componente View dedicato alla generazione grafica del QR Code (via ZXing)
 * e al riquadro elegante per la scansione dello scontrino digitale.
 */
public final class QrCodeView {

    private QrCodeView() {
    }

    public static Image generaImmagineQr(String contenuto, int dimensionePx) {
        if (contenuto == null || contenuto.isBlank()) {
            return null;
        }
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix matrix = new QRCodeWriter().encode(contenuto, BarcodeFormat.QR_CODE, dimensionePx, dimensionePx, hints);
            WritableImage image = new WritableImage(dimensionePx, dimensionePx);
            PixelWriter pw = image.getPixelWriter();
            Color scuro = Color.web("#141312");
            Color chiaro = Color.WHITE;

            for (int y = 0; y < dimensionePx; y++) {
                for (int x = 0; x < dimensionePx; x++) {
                    pw.setColor(x, y, matrix.get(x, y) ? scuro : chiaro);
                }
            }
            return image;
        } catch (WriterException e) {
            return null;
        }
    }

    public static VBox creaBoxQrScontrino(String urlScontrino) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(28, 25, 33, 0.94); -fx-border-color: rgba(186,164,135,0.40); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 14;");

        Image qrImg = generaImmagineQr(urlScontrino, 148);
        if (qrImg != null) {
            ImageView iv = new ImageView(qrImg);
            iv.setFitWidth(144);
            iv.setFitHeight(144);
            iv.setPreserveRatio(true);
            iv.setSmooth(false);

            StackPane cornice = new StackPane(iv);
            cornice.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-padding: 6; "
                    + "-fx-border-color: #baa487; -fx-border-width: 1.5; -fx-border-radius: 10;");
            box.getChildren().add(cornice);
        }

        Label lblScan = new Label("📱 Inquadra per lo Scontrino");
        lblScan.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #baa487;");

        Hyperlink linkBrowser = new Hyperlink("Apri scontrino sul Mac ↗");
        linkBrowser.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #ded7cd; -fx-border-color: transparent; -fx-padding: 0;");
        linkBrowser.setOnAction(e -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(urlScontrino));
                }
            } catch (Exception ignored) {
                // Ignora se il browser non può essere aperto
            }
        });

        box.getChildren().addAll(lblScan, linkBrowser);
        return box;
    }
}
