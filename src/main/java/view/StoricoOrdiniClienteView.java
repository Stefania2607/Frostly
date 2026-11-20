package view;

import bean.OrdineBean;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Componente View dedicato al rendering grafico della cronologia ordini del cliente
 * ("I Miei Ordini"), visualizzando badge di stato, dettagli articoli e totale spesa.
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

                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: rgba(28, 25, 33, 0.90); -fx-background-radius: 12; -fx-padding: 14 18; "
                        + "-fx-border-color: rgba(186, 164, 135, 0.30); -fx-border-radius: 12; -fx-border-width: 1; "
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 6, 0, 0, 2);");
                card.prefWidthProperty().bind(listaMieiOrdini.widthProperty().subtract(42));
                card.maxWidthProperty().bind(listaMieiOrdini.widthProperty().subtract(42));
                setPrefWidth(0);

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label lblId = new Label(item.getCodiceOrdine());
                lblId.setMinWidth(Region.USE_PREF_SIZE);
                lblId.setStyle("-fx-background-color: rgba(186, 164, 135, 0.22); -fx-text-fill: #baa487; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-size: 13px;");

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);

                Label lblStato = creaBadgeStatoCliente(item.getStato());
                lblStato.setMinWidth(Region.USE_PREF_SIZE);

                header.getChildren().addAll(lblId, r, lblStato);

                String rawConsegna = item.getDataRitiro() != null ? item.getDataRitiro() : "Ritiro al banco";
                String infoPrincipale = rawConsegna;
                String noteExtra = "";
                if (rawConsegna.contains("• Indirizzo/Note:")) {
                    String[] parti = rawConsegna.split("• Indirizzo/Note:", 2);
                    infoPrincipale = parti[0].trim();
                    noteExtra = parti.length > 1 ? parti[1].trim() : "";
                }

                VBox boxConsegna = new VBox(4);
                boxConsegna.setStyle("-fx-background-color: rgba(18, 16, 20, 0.78); -fx-border-color: rgba(186,164,135,0.22); "
                        + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");

                boolean haGiaIcona = infoPrincipale.startsWith("🛍") || infoPrincipale.startsWith("🛵");
                String icona = haGiaIcona ? "" : (infoPrincipale.toLowerCase().contains("ritiro") ? "🛍️  " : "🛵  ");
                Label lblRitiro = new Label(icona + infoPrincipale);
                lblRitiro.setWrapText(true);
                lblRitiro.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");
                boxConsegna.getChildren().add(lblRitiro);

                if (!noteExtra.isEmpty()) {
                    Label lblNote = new Label("📍 " + noteExtra);
                    lblNote.setWrapText(true);
                    lblNote.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #ded7cd;");
                    boxConsegna.getChildren().add(lblNote);
                }

                Label lblDettaglio = new Label(item.getDescrizioneCompleta());
                lblDettaglio.setWrapText(true);
                lblDettaglio.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #f5ebe0;");

                HBox footer = new HBox(10);
                footer.setAlignment(Pos.CENTER_RIGHT);

                Label lblTot = new Label("Totale Pagamento: " + String.format(Locale.US, "EUR %.2f", item.getPrezzoTotale()));
                lblTot.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #baa487;");

                footer.getChildren().add(lblTot);
                card.getChildren().addAll(header, boxConsegna, lblDettaglio, footer);

                setGraphic(card);
                setText(null);
            }
        });
    }

    private static Label creaBadgeStatoCliente(String stato) {
        if ("IN_ATTESA".equalsIgnoreCase(stato)) {
            Label lbl = new Label("⏳ In attesa di conferma");
            lbl.setStyle("-fx-background-color: rgba(65, 48, 20, 0.90); -fx-text-fill: #fcd34d; -fx-border-color: #baa487; "
                    + "-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8; -fx-border-radius: 8;");
            return lbl;
        }
        return OrdiniStaffView.creaBadgeStatoOrdine(stato);
    }
}
