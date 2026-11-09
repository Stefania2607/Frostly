package view;

import bean.GustoBean;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Componente View dedicato esclusivamente al configuratore visuale delle Crepes:
 * - Visualizzazione delle 4 Crepes artigianali (Nutella, Visciole, Marmellata, Crema Pan di Stelle)
 * - Bottone "Scegli il Topping & Granella (+0.50€)"
 * - Scelta Topping (+0.50€ o Nessuno) e Granella (+0.50€ o Nessuna)
 * - Calcolo prezzo e formattazione descrizione Crepe per il Carrello.
 */
public final class CrepeConfiguratoreView {

    public static final String NESSUN_TOPPING = "Nessuno";
    public static final String NESSUNA_GRANELLA = "Nessuna";
    public static final double PREZZO_EXTRA_CREPE = 0.50;

    public static final List<String> OPZIONI_TOPPING_CREPE = Arrays.asList(
            NESSUN_TOPPING,
            "Topping Cioccolato Fondente",
            "Topping Cioccolato Bianco",
            "Topping Caramello",
            "Topping Frutti di Bosco",
            "Topping Crema Pan di Stelle"
    );

    public static final List<String> OPZIONI_GRANELLA_CREPE = Arrays.asList(
            NESSUNA_GRANELLA,
            "Granella di Pistacchio",
            "Granella di Nocciola",
            "Cocco",
            "Granella Pan di Stelle"
    );

    private CrepeConfiguratoreView() {
        // Classe di utilità View
    }

    public static class AzioniCrepe {
        private final Consumer<GustoBean> onSelezionaCrepe;
        private final Runnable onClickApriScegliTopping;
        private final Consumer<String> onSelezionaTopping;
        private final Consumer<String> onSelezionaGranella;
        private final Runnable onConfermaCrepeNelCarrello;

        public AzioniCrepe(Consumer<GustoBean> onSelezionaCrepe,
                           Runnable onClickApriScegliTopping,
                           Consumer<String> onSelezionaTopping,
                           Consumer<String> onSelezionaGranella,
                           Runnable onConfermaCrepeNelCarrello) {
            this.onSelezionaCrepe = onSelezionaCrepe;
            this.onClickApriScegliTopping = onClickApriScegliTopping;
            this.onSelezionaTopping = onSelezionaTopping;
            this.onSelezionaGranella = onSelezionaGranella;
            this.onConfermaCrepeNelCarrello = onConfermaCrepeNelCarrello;
        }
    }

    public static void renderSchermataCrepes(VBox contenitore,
                                             List<GustoBean> farciture,
                                             GustoBean crepeSelezionata,
                                             String toppingScelto,
                                             String granellaScelta,
                                             boolean mostraPannelloTopping,
                                             AzioniCrepe azioni) {
        if (contenitore == null) return;
        contenitore.getChildren().clear();

        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle("-fx-background-color: #faf0ca66; -fx-border-color: #ddb892; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8 14;");
        Label lblTitolo = new Label("🥞 Le Nostre Crepes Artigianali — 1. Clicca sul tipo di Crepe che preferisci:");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #3a2618;");
        Region spB = new Region();
        HBox.setHgrow(spB, Priority.ALWAYS);
        Label lblInfoExtra = new Label("Topping a scelta (+€0,50) • Granella a scelta (+€0,50 o Nessuna)");
        lblInfoExtra.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f5539;");
        banner.getChildren().addAll(lblTitolo, spB, lblInfoExtra);
        contenitore.getChildren().add(banner);

        FlowPane grigliaCrepes = new FlowPane(14, 14);
        grigliaCrepes.setAlignment(Pos.TOP_CENTER);
        grigliaCrepes.setStyle("-fx-padding: 4;");

        for (GustoBean crepe : farciture) {
            boolean selezionata = crepeSelezionata != null && crepeSelezionata.getNome().equalsIgnoreCase(crepe.getNome());
            VBox scheda = ClienteMenuView.creaSchedaVisualeMenu(
                    crepe,
                    selezionata,
                    selezionata ? "✔ Crepe Selezionata" : "🥞 Seleziona questa Crepe",
                    () -> azioni.onSelezionaCrepe.accept(crepe));
            grigliaCrepes.getChildren().add(scheda);
        }
        contenitore.getChildren().add(grigliaCrepes);

        if (crepeSelezionata != null) {
            VBox boxConfig = new VBox(10);
            boxConfig.setStyle("-fx-background-color: #fff8ee; -fx-border-color: #588157; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 16;");

            HBox barraScelta = new HBox(12);
            barraScelta.setAlignment(Pos.CENTER_LEFT);

            StackPane miniatura = VetrinaMenuView.creaBoxFotoProdotto(crepeSelezionata.getImmagineUrl(), "🥞", 72, 48, 12);

            VBox infoScelta = new VBox(2);
            HBox.setHgrow(infoScelta, Priority.ALWAYS);
            Label lblScelta = new Label("🥞 Hai selezionato: " + crepeSelezionata.getNome()
                    + String.format(Locale.US, " (Base EUR %.2f)", crepeSelezionata.getPrezzoAlKg()));
            lblScelta.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #3a2618;");
            Label lblSub = new Label("Clicca su 'Scegli il Topping' per personalizzare topping (+€0,50) e granella (+€0,50 o Nessuna):");
            lblSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #7f5539;");
            infoScelta.getChildren().addAll(lblScelta, lblSub);

            Button btnScegliTopping = new Button("✨ Scegli il Topping & Granella (+€0,50)");
            btnScegliTopping.setMinWidth(Region.USE_PREF_SIZE);
            btnScegliTopping.setStyle("-fx-background-color: #7f5539; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-background-radius: 14; -fx-padding: 8 16; -fx-cursor: hand;");
            btnScegliTopping.setOnAction(e -> azioni.onClickApriScegliTopping.run());

            barraScelta.getChildren().addAll(miniatura, infoScelta, btnScegliTopping);
            boxConfig.getChildren().add(barraScelta);

            if (mostraPannelloTopping) {
                VBox pannelloOpzioni = new VBox(8);
                pannelloOpzioni.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e6d5c3; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 14;");

                Label lblTopTitle = new Label("1. Scegli il Topping (+ EUR 0.50, oppure Nessuno):");
                lblTopTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #3a2618;");
                FlowPane chipToppings = new FlowPane(8, 6);
                for (String opzTop : OPZIONI_TOPPING_CREPE) {
                    boolean attivo = opzTop.equalsIgnoreCase(toppingScelto);
                    String etichetta = NESSUN_TOPPING.equalsIgnoreCase(opzTop)
                            ? "🚫 Nessun Topping (+€0.00)"
                            : ("🍫 " + opzTop + " (+€0.50)");
                    Button chip = creaChipOpzione(etichetta, attivo, () -> azioni.onSelezionaTopping.accept(opzTop));
                    chipToppings.getChildren().add(chip);
                }

                Label lblGranTitle = new Label("2. Scegli la Granella (+ EUR 0.50, oppure Nessuna):");
                lblGranTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #3a2618;");
                FlowPane chipGranelle = new FlowPane(8, 6);
                for (String opzGran : OPZIONI_GRANELLA_CREPE) {
                    boolean attivo = opzGran.equalsIgnoreCase(granellaScelta);
                    String etichetta = NESSUNA_GRANELLA.equalsIgnoreCase(opzGran)
                            ? "🚫 Nessuna Granella (+€0.00)"
                            : ("✨ " + opzGran + " (+€0.50)");
                    Button chip = creaChipOpzione(etichetta, attivo, () -> azioni.onSelezionaGranella.accept(opzGran));
                    chipGranelle.getChildren().add(chip);
                }

                double totCrepe = calcolaPrezzoCrepe(crepeSelezionata.getPrezzoAlKg(), toppingScelto, granellaScelta);
                String descCompleta = formattaDescrizioneCrepe(crepeSelezionata.getNome(), toppingScelto, granellaScelta);

                HBox footerConferma = new HBox(12);
                footerConferma.setAlignment(Pos.CENTER_LEFT);
                footerConferma.setStyle("-fx-padding: 6 0 0 0;");

                Label lblRiep = new Label("🧾 " + descCompleta);
                lblRiep.setWrapText(true);
                lblRiep.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #5c3a21;");
                HBox.setHgrow(lblRiep, Priority.ALWAYS);

                Label lblPrezzoTot = new Label(String.format(Locale.US, "Totale Crepe: EUR %.2f", totCrepe));
                lblPrezzoTot.setMinWidth(Region.USE_PREF_SIZE);
                lblPrezzoTot.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #2a9d8f;");

                Button btnMettiCarrello = new Button("➕ Aggiungi Crepe al Carrello");
                btnMettiCarrello.setMinWidth(Region.USE_PREF_SIZE);
                btnMettiCarrello.setStyle("-fx-background-color: #588157; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-background-radius: 14; -fx-padding: 8 18; -fx-cursor: hand;");
                btnMettiCarrello.setOnAction(e -> azioni.onConfermaCrepeNelCarrello.run());

                footerConferma.getChildren().addAll(lblRiep, lblPrezzoTot, btnMettiCarrello);
                pannelloOpzioni.getChildren().addAll(lblTopTitle, chipToppings, lblGranTitle, chipGranelle, footerConferma);
                boxConfig.getChildren().add(pannelloOpzioni);
            }

            contenitore.getChildren().add(boxConfig);
        }
    }

    public static double calcolaPrezzoCrepe(double base, String topping, String granella) {
        double p = base;
        if (topping != null && !topping.isEmpty() && !NESSUN_TOPPING.equalsIgnoreCase(topping)) {
            p += PREZZO_EXTRA_CREPE;
        }
        if (granella != null && !granella.isEmpty() && !NESSUNA_GRANELLA.equalsIgnoreCase(granella)) {
            p += PREZZO_EXTRA_CREPE;
        }
        return Math.round(p * 100.0) / 100.0;
    }

    public static String formattaDescrizioneCrepe(String nomeCrepe, String topping, String granella) {
        StringBuilder sb = new StringBuilder(nomeCrepe);
        boolean hasTop = topping != null && !topping.isEmpty() && !NESSUN_TOPPING.equalsIgnoreCase(topping);
        boolean hasGran = granella != null && !granella.isEmpty() && !NESSUNA_GRANELLA.equalsIgnoreCase(granella);
        if (hasTop || hasGran) {
            sb.append(" (");
            if (hasTop) {
                sb.append(topping);
            }
            if (hasGran) {
                if (hasTop) sb.append(" + ");
                sb.append(granella);
            } else {
                sb.append(" • Senza granella");
            }
            sb.append(")");
        } else {
            sb.append(" (Semplice: nessun topping, nessuna granella)");
        }
        return sb.toString();
    }

    public static Button creaChipOpzione(String testo, boolean attivo, Runnable onClick) {
        Button b = new Button(testo);
        b.setMinWidth(Region.USE_PREF_SIZE);
        b.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 5 12; -fx-cursor: hand; "
                + (attivo
                ? "-fx-background-color: #2a9d8f; -fx-text-fill: white; -fx-border-color: #1f7a6f; -fx-border-radius: 14;"
                : "-fx-background-color: #f8f4ef; -fx-text-fill: #5c3a21; -fx-border-color: #d5bdaf; -fx-border-radius: 14;"));
        b.setOnAction(e -> onClick.run());
        return b;
    }
}
