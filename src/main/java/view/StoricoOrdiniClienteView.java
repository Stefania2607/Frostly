package view;

import bean.OrdineBean;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Componente View dedicato al rendering grafico della cronologia ordini del cliente
 * ("I Miei Ordini"), con articoli raggruppati per quantita', badge di stato e totale spesa.
 */
public final class StoricoOrdiniClienteView {

    private StoricoOrdiniClienteView() {
    }

    public static void configuraCelleMieiOrdini(ListView<OrdineBean> listaMieiOrdini) {
        if (listaMieiOrdini == null) return;
        listaMieiOrdini.setCellFactory(lv -> new ListCell<OrdineBean>() {
            @Override
            protected void updateItem(OrdineBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                VBox card = new VBox(10);
                card.setStyle("-fx-background-color: rgba(24, 22, 28, 0.94); -fx-background-radius: 14; -fx-padding: 15 18; "
                        + "-fx-border-color: rgba(186, 164, 135, 0.28); -fx-border-radius: 14; -fx-border-width: 1; "
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 6, 0, 0, 2);");
                card.prefWidthProperty().bind(listaMieiOrdini.widthProperty().subtract(42));
                card.maxWidthProperty().bind(listaMieiOrdini.widthProperty().subtract(42));
                setPrefWidth(0);

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label lblId = new Label(item.getCodiceOrdine());
                lblId.setMinWidth(Region.USE_PREF_SIZE);
                lblId.setStyle("-fx-background-color: rgba(186, 164, 135, 0.22); -fx-text-fill: #e5c99b; -fx-font-weight: bold; -fx-padding: 4 11; -fx-background-radius: 7; -fx-font-size: 13px;");

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);

                Label lblStato = creaBadgeStatoCliente(item);
                lblStato.setMinWidth(Region.USE_PREF_SIZE);

                header.getChildren().addAll(lblId, r, lblStato);

                VBox boxConsegna = creaBoxConsegnaPulito(item.getDataRitiro());
                VBox boxProdotti = creaListaProdottiRaggruppati(item.getDescrizioneCompleta());

                HBox footer = new HBox(10);
                footer.setAlignment(Pos.CENTER_LEFT);
                int numMsg = new control.applicativo.OrdiniControl()
                        .caricaMessaggiChatOrdine(item.getCodiceOrdine(), item.getEmailCliente()).size();
                boolean ordineAttivo = control.applicativo.OrdiniControl.isOrdineAttivoPerChatCliente(item);
                if (ordineAttivo || numMsg > 0) {
                    Button btnChat = new Button(numMsg > 0
                            ? ("💬 Chat Gelateria (" + numMsg + ")")
                            : "💬 Chat con la Gelateria");
                    btnChat.setMinWidth(Region.USE_PREF_SIZE);
                    btnChat.setStyle(numMsg > 0
                            ? "-fx-background-color: rgba(212, 180, 131, 0.22); -fx-text-fill: #f5ebe0; -fx-border-color: #d4b483; "
                              + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand;"
                            : "-fx-background-color: rgba(255, 255, 255, 0.07); -fx-text-fill: #ded7cd; -fx-border-color: rgba(255,255,255,0.14); "
                              + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand;");
                    btnChat.setOnAction(e -> {
                        javafx.stage.Window win = btnChat.getScene() != null ? btnChat.getScene().getWindow() : null;
                        ChatOrdineView.mostraChatOrdine(win, item, false);
                        listaMieiOrdini.refresh();
                    });
                    footer.getChildren().add(btnChat);
                }

                String trx = estraiTransazione(item.getDataRitiro());
                if (!trx.isEmpty()) {
                    Label lblTrx = new Label("💳 " + trx);
                    lblTrx.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8e8e93;");
                    footer.getChildren().add(lblTrx);
                }
                Region spFooter = new Region();
                HBox.setHgrow(spFooter, Priority.ALWAYS);

                Label lblTot = new Label("Totale: " + String.format(Locale.US, "EUR %.2f", item.getPrezzoTotale()));
                lblTot.setMinWidth(Region.USE_PREF_SIZE);
                lblTot.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #e5c99b;");

                footer.getChildren().addAll(spFooter, lblTot);
                card.getChildren().addAll(header, boxConsegna, boxProdotti, footer);

                setGraphic(card);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
            }
        });
    }

    private static VBox creaBoxConsegnaPulito(String rawDataRitiro) {
        String raw = rawDataRitiro != null ? rawDataRitiro : "Ritiro al banco";
        String senzaTrx = raw.replaceAll("\\s*\\[Transazione Online:[^\\]]*\\]", "").trim();
        String rigaPrincipale = senzaTrx;
        String dettagli = "";
        if (senzaTrx.contains("• Indirizzo/Note:")) {
            String[] parti = senzaTrx.split("• Indirizzo/Note:", 2);
            rigaPrincipale = parti[0].trim();
            dettagli = parti[1].trim();
        }

        VBox box = new VBox(3);
        box.setStyle("-fx-background-color: rgba(16, 15, 20, 0.78); -fx-border-color: rgba(186,164,135,0.18); "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");

        boolean haGiaIcona = rigaPrincipale.startsWith("🛍") || rigaPrincipale.startsWith("🛵");
        String icona = haGiaIcona ? "" : (rigaPrincipale.toLowerCase().contains("ritiro") || rigaPrincipale.toLowerCase().contains("banco") ? "🛍️  " : "🛵  ");
        Label lblRitiro = new Label(icona + rigaPrincipale);
        lblRitiro.setWrapText(true);
        lblRitiro.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #d4b483;");
        box.getChildren().add(lblRitiro);

        if (!dettagli.isEmpty()) {
            Label lblDet = new Label("📍 " + dettagli);
            lblDet.setWrapText(true);
            lblDet.setStyle("-fx-font-size: 12px; -fx-text-fill: #c7bfb5;");
            box.getChildren().add(lblDet);
        }
        return box;
    }

    private static String estraiTransazione(String rawDataRitiro) {
        if (rawDataRitiro == null) return "";
        int idx = rawDataRitiro.indexOf("[Transazione Online:");
        if (idx >= 0) {
            int fine = rawDataRitiro.indexOf(']', idx);
            if (fine > idx) {
                return rawDataRitiro.substring(idx + 1, fine).trim();
            }
        }
        return "";
    }

    /**
     * Raggruppa automaticamente gli articoli identici (es. 6x Amaretti Morbidi di Sassello)
     * e li mostra in un elenco puntato ordinato e leggibile.
     */
    private static VBox creaListaProdottiRaggruppati(String descrizioneCompleta) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: rgba(18, 17, 22, 0.72); -fx-border-color: rgba(255,255,255,0.06); "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 14;");

        if (descrizioneCompleta == null || descrizioneCompleta.isBlank()) {
            return box;
        }

        Map<String, Integer> conteggi = raggruppaArticoli(descrizioneCompleta);
        for (Map.Entry<String, Integer> entry : conteggi.entrySet()) {
            String testo = entry.getKey();
            int qty = entry.getValue();

            String nome = testo;
            String sottotitolo = "";
            int p1 = testo.indexOf('(');
            int p2 = testo.lastIndexOf(')');
            if (p1 > 0 && p2 > p1) {
                nome = testo.substring(0, p1).trim();
                sottotitolo = testo.substring(p1 + 1, p2).trim();
                String extra = testo.substring(p2 + 1).trim();
                if (!extra.isEmpty()) {
                    sottotitolo = sottotitolo.isEmpty() ? extra : (sottotitolo + " • " + extra);
                }
            }

            Label lblQty = new Label(qty + "×");
            lblQty.setMinWidth(Region.USE_PREF_SIZE);
            lblQty.setStyle("-fx-background-color: rgba(212, 180, 131, 0.18); -fx-text-fill: #e5c99b; "
                    + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 2 8; -fx-background-radius: 6;");

            VBox testoBox = new VBox(1);
            Label lblNome = new Label(nome);
            lblNome.setWrapText(true);
            lblNome.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
            testoBox.getChildren().add(lblNome);

            if (!sottotitolo.isEmpty()) {
                Label lblSub = new Label(sottotitolo);
                lblSub.setWrapText(true);
                lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92; -fx-font-style: italic;");
                testoBox.getChildren().add(lblSub);
            }
            HBox.setHgrow(testoBox, Priority.ALWAYS);

            HBox riga = new HBox(10, lblQty, testoBox);
            riga.setAlignment(Pos.TOP_LEFT);
            box.getChildren().add(riga);
        }
        return box;
    }

    public static Map<String, Integer> raggruppaArticoli(String descrizioneCompleta) {
        Map<String, Integer> conteggi = new LinkedHashMap<>();
        if (descrizioneCompleta == null || descrizioneCompleta.isBlank()) {
            return conteggi;
        }
        String[] pezzi = descrizioneCompleta.split(" \\| ");
        for (String p : pezzi) {
            String cleaned = p.trim().replaceFirst("^[Vv]aschetta #\\d+:\\s*", "").trim();
            if (cleaned.isEmpty()) continue;
            int qty = 1;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+)x\\s+(\\S.*)$").matcher(cleaned);
            if (m.matches()) {
                qty = Integer.parseInt(m.group(1));
                cleaned = m.group(2).trim();
            }
            conteggi.merge(cleaned, qty, Integer::sum);
        }
        return conteggi;
    }

    private static Label creaBadgeStatoCliente(OrdineBean item) {
        if (item != null && "IN_ATTESA".equalsIgnoreCase(item.getStato())) {
            Label lbl = new Label("⏳ In attesa di conferma");
            lbl.setStyle("-fx-background-color: rgba(65, 48, 20, 0.90); -fx-text-fill: #fcd34d; -fx-border-color: #baa487; "
                    + "-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8; -fx-border-radius: 8;");
            return lbl;
        }
        return OrdiniStaffView.creaBadgeStatoOrdine(item);
    }
}
