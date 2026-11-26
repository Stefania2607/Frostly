package view;

import bean.StatisticheBean;
import java.util.Locale;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View per la dashboard statistiche del Maestro: mostra i KPI principali,
 * il grafico a barre dei prodotti più venduti e il grafico a torta dello stato del magazzino.
 */
public final class StatisticheMaestroView {

    private StatisticheMaestroView() {
    }

    public static void renderStatistiche(VBox contenitore, StatisticheBean stats) {
        if (contenitore == null || stats == null) {
            return;
        }
        contenitore.getChildren().clear();

        boolean provenza = RecensioniView.isTemaSalottoChiaro(contenitore);
        HBox rigaKpi = new HBox(14);
        int totMag = stats.getIngredientiOk() + stats.getIngredientiSottoSoglia() + stats.getIngredientiEsauriti();
        int percOk = totMag > 0 ? (int) Math.round((stats.getIngredientiOk() * 100.0) / totMag) : 100;

        VBox kpiIncasso = creaKpiCard("💰 INCASSO ATTIVO",
                String.format(Locale.US, "EUR %.2f", stats.getIncassoTotale()),
                "Ordini confermati e in attesa", provenza ? "#5e316f" : "#baa487", provenza);
        VBox kpiMedio = creaKpiCard("🧾 SCONTRINO MEDIO",
                String.format(Locale.US, "EUR %.2f", stats.getScontrinoMedio()),
                "Valore medio per ordine", provenza ? "#37253d" : "#f5ebe0", provenza);
        VBox kpiOrdini = creaKpiCard("🛵 ORDINI RICEVUTI",
                String.valueOf(stats.getOrdiniConfermati() + stats.getOrdiniInAttesa()),
                stats.getOrdiniConfermati() + " confermati • " + stats.getOrdiniInAttesa() + " in attesa • " + stats.getOrdiniDisdetti() + " disdetti",
                provenza ? "#2e7d32" : "#a7f3d0", provenza);
        VBox kpiMag = creaKpiCard("📦 SALUTE MAGAZZINO",
                percOk + "% Ottimale",
                stats.getIngredientiEsauriti() + " esauriti • " + stats.getIngredientiSottoSoglia() + " sotto soglia",
                stats.getIngredientiEsauriti() > 0 ? (provenza ? "#b91c1c" : "#fca5a5") : (provenza ? "#2e7d32" : "#a7f3d0"), provenza);

        HBox.setHgrow(kpiIncasso, Priority.ALWAYS);
        HBox.setHgrow(kpiMedio, Priority.ALWAYS);
        HBox.setHgrow(kpiOrdini, Priority.ALWAYS);
        HBox.setHgrow(kpiMag, Priority.ALWAYS);
        rigaKpi.getChildren().addAll(kpiIncasso, kpiMedio, kpiOrdini, kpiMag);

        HBox rigaGrafici = new HBox(16);
        VBox.setVgrow(rigaGrafici, Priority.ALWAYS);

        VBox boxBar = creaCardGraficoTopProdotti(stats, provenza);
        VBox boxPie = creaCardGraficoMagazzino(stats, provenza);
        HBox.setHgrow(boxBar, Priority.ALWAYS);
        HBox.setHgrow(boxPie, Priority.ALWAYS);
        rigaGrafici.getChildren().addAll(boxBar, boxPie);

        HBox barraCanali = creaBarraCanaliConsegna(stats, provenza);

        contenitore.getChildren().addAll(rigaKpi, rigaGrafici, barraCanali);
    }

    private static VBox creaKpiCard(String titolo, String valore, String sottotitolo, String coloreValore, boolean provenza) {
        VBox card = new VBox(5);
        card.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e9dbdf; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 14 16;"
                : "-fx-background-color: rgba(26, 24, 30, 0.94); -fx-border-color: rgba(186,164,135,0.32); "
                  + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14 16;");

        Label lblTit = new Label(titolo);
        lblTit.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#764c86;" : "#baa487;"));

        Label lblVal = new Label(valore);
        lblVal.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + coloreValore + ";");

        Label lblSub = new Label(sottotitolo);
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (provenza ? "#756978;" : "#c7beb2;"));

        card.getChildren().addAll(lblTit, lblVal, lblSub);
        return card;
    }

    private static VBox creaCardGraficoTopProdotti(StatisticheBean stats, boolean provenza) {
        VBox box = new VBox(8);
        box.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e9dbdf; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 14 16;"
                : "-fx-background-color: rgba(22, 20, 25, 0.92); -fx-border-color: rgba(186,164,135,0.28); "
                  + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14 16;");

        Label tit = new Label("🏆 Prodotti più Ordinati");
        tit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Porzioni");
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(290);
        VBox.setVgrow(chart, Priority.ALWAYS);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        if (stats.getTopProdottiVenduti().isEmpty()) {
            serie.getData().add(new XYChart.Data<>("Nessun ordine", 0));
        } else {
            for (Map.Entry<String, Integer> entry : stats.getTopProdottiVenduti().entrySet()) {
                serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        chart.getData().add(serie);

        box.getChildren().addAll(tit, chart);
        return box;
    }

    private static VBox creaCardGraficoMagazzino(StatisticheBean stats, boolean provenza) {
        VBox box = new VBox(8);
        box.setPrefWidth(380);
        box.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e9dbdf; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 14 16;"
                : "-fx-background-color: rgba(22, 20, 25, 0.92); -fx-border-color: rgba(186,164,135,0.28); "
                  + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14 16;");

        Label tit = new Label("📊 Distribuzione Scorte Magazzino");
        tit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        PieChart pie = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Ottimali (" + stats.getIngredientiOk() + ")", stats.getIngredientiOk()),
                new PieChart.Data("Sotto Soglia (" + stats.getIngredientiSottoSoglia() + ")", stats.getIngredientiSottoSoglia()),
                new PieChart.Data("Esauriti (" + stats.getIngredientiEsauriti() + ")", stats.getIngredientiEsauriti())
        ));
        pie.setLabelsVisible(true);
        pie.setLegendVisible(true);
        pie.setAnimated(false);
        pie.setPrefHeight(290);
        VBox.setVgrow(pie, Priority.ALWAYS);

        box.getChildren().addAll(tit, pie);
        return box;
    }

    private static HBox creaBarraCanaliConsegna(StatisticheBean stats, boolean provenza) {
        HBox barra = new HBox(24);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setStyle(provenza
                ? "-fx-background-color: #fffaf7; -fx-border-color: #e9dbdf; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 10 18;"
                : "-fx-background-color: rgba(24, 22, 28, 0.92); -fx-border-color: rgba(186,164,135,0.25); "
                  + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 18;");

        Label lblTit = new Label("Canali di Ricezione Ordini Attivi:");
        lblTit.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + (provenza ? "#764c86;" : "#baa487;"));

        Label lblDom = new Label("🛵 Consegna a Domicilio: " + stats.getOrdiniDomicilio());
        lblDom.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        Label lblBan = new Label("🛍️ Ritiro al Banco: " + stats.getOrdiniRitiroBanco());
        lblBan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        barra.getChildren().addAll(lblTit, lblDom, lblBan);
        return barra;
    }
}
