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
        // Classe di utilità View
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

                VBox card = new VBox(6);
                card.setStyle("-fx-background-color: rgba(28, 25, 33, 0.88); -fx-background-radius: 12; -fx-padding: 12 16; "
                        + "-fx-border-color: rgba(186, 164, 135, 0.28); -fx-border-radius: 12; -fx-border-width: 1; "
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 6, 0, 0, 2);");
                card.maxWidthProperty().bind(listaMieiOrdini.widthProperty().subtract(34));

                HBox header = new HBox(8);
                header.setAlignment(Pos.CENTER_LEFT);

                Label lblId = new Label(item.getCodiceOrdine());
                lblId.setStyle("-fx-background-color: rgba(186, 164, 135, 0.20); -fx-text-fill: #baa487; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 11px;");

                Label lblRitiro = new Label(item.getDataRitiro());
                lblRitiro.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #ded7cd;");

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);

                Label lblStato = OrdiniStaffView.creaBadgeStatoOrdine(item.getStato());

                header.getChildren().addAll(lblId, lblRitiro, r, lblStato);

                Label lblDettaglio = new Label(item.getDescrizioneCompleta());
                lblDettaglio.setWrapText(true);
                lblDettaglio.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #f5ebe0;");

                HBox footer = new HBox(10);
                footer.setAlignment(Pos.CENTER_RIGHT);

                Label lblTot = new Label("Totale Pagamento: " + String.format(Locale.US, "EUR %.2f", item.getPrezzoTotale()));
                lblTot.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #baa487;");

                footer.getChildren().add(lblTot);
                card.getChildren().addAll(header, lblDettaglio, footer);

                setGraphic(card);
                setText(null);
            }
        });
    }
}
