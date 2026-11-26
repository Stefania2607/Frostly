package view;

import bean.DettaglioAvvisoOrdineBean;
import bean.DettaglioAvvisoOrdineBean.RigaArticoloOrdine;
import bean.DettaglioAvvisoOrdineBean.TipoAvviso;
import bean.NotificaBean;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import util.SessionManager;

/**
 * Finestra modale ridisegnata "Frostly — Dettaglio Avviso & Notifica".
 * Struttura in quattro aree (Intestazione fissa, Riepilogo compatto, Elenco prodotti scorrevole,
 * Barra inferiore fissa con totale e azioni) per gli avvisi legati a ordini reali,
 * e layout pulito coerente per notifiche generiche o relative ad altre entita'.
 */
public final class DettaglioAvvisoDialog {

    private static final String FONT_SANS = "-apple-system, 'Helvetica Neue', 'Arial', sans-serif";
    private static final String FONT_SERIF = "'Georgia', serif";

    private DettaglioAvvisoDialog() {
    }

    public static void mostra(Window owner,
                              NotificaBean notifica,
                              BiConsumer<String, String> onNavigaSezioneConCodice,
                              Consumer<NotificaBean> onEliminaNotifica) {
        if (notifica == null) {
            return;
        }
        DettaglioAvvisoOrdineBean dettaglio = DettaglioAvvisoResolver.risolvi(notifica);
        mostraDettaglio(owner, notifica, dettaglio, onNavigaSezioneConCodice, onEliminaNotifica);
    }

    public static void mostraDettaglio(Window owner,
                                       NotificaBean notificaOriginale,
                                       DettaglioAvvisoOrdineBean dettaglio,
                                       BiConsumer<String, String> onNavigaSezioneConCodice,
                                       Consumer<NotificaBean> onEliminaNotifica) {
        if (dettaglio == null) {
            return;
        }

        boolean provenza = SessionManager.getInstance().isGui2Attiva();
        boolean isVistaOrdine = dettaglio.getTipoAvviso() == TipoAvviso.ORDINE && dettaglio.isOrdineRealeTrovato();

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Dettaglio Avviso & Notifica");
        if (owner != null) {
            popup.initOwner(owner);
        }

        final boolean[] richiestaEliminazione = {false};
        final String[] navigazioneRichiesta = {null, null};

        BorderPane root = new BorderPane();
        root.setStyle(provenza
                ? "-fx-background-color: #faf4f5; -fx-background-image: none;"
                : "-fx-background-color: #161419; -fx-background-image: none;");

        // 1. Intestazione fissa
        HBox header = creaIntestazioneFissa(dettaglio, isVistaOrdine, provenza);
        root.setTop(header);

        // 2. Contenuto centrale scorrevole (senza scrollbar annidate)
        VBox contenutoCentrale = isVistaOrdine
                ? creaCorpoOrdine(dettaglio, provenza)
                : creaCorpoGenerico(dettaglio, provenza);

        ScrollPane scrollCentrale = new ScrollPane(contenutoCentrale);
        scrollCentrale.setFitToWidth(true);
        scrollCentrale.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCentrale.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollCentrale.setPannable(false);
        scrollCentrale.setStyle("-fx-background-color: transparent; -fx-background: transparent; "
                + "-fx-border-color: transparent; -fx-padding: 0;");
        root.setCenter(scrollCentrale);

        // 3. Barra inferiore fissa
        VBox footer = creaBarraInferioreFissa(
                dettaglio,
                isVistaOrdine,
                provenza,
                onEliminaNotifica != null,
                onNavigaSezioneConCodice != null,
                () -> {
                    richiestaEliminazione[0] = true;
                    popup.close();
                },
                (sezione, codice) -> {
                    navigazioneRichiesta[0] = sezione;
                    navigazioneRichiesta[1] = codice;
                    popup.close();
                },
                popup::close
        );
        root.setBottom(footer);

        // Dimensionamento intelligente in base allo schermo e alla lunghezza dell'ordine
        Rectangle2D bounds = trovaAreaSchermo(owner);
        double maxH = Math.max(480, Math.round(bounds.getHeight() * 0.84));
        double maxW = Math.max(580, Math.round(bounds.getWidth() * 0.86));
        double prefW = isVistaOrdine
                ? Math.min(maxW, Math.max(660, Math.min(760, Math.round(bounds.getWidth() * 0.56))))
                : Math.min(maxW, 600);
        double prefH = calcolaAltezzaIdeale(dettaglio, isVistaOrdine, maxH);

        Scene scene = new Scene(root, prefW, prefH);
        scene.setFill(Color.web(provenza ? "#faf4f5" : "#161419"));
        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) {
                popup.close();
            }
        });

        popup.setScene(scene);
        popup.setMinWidth(isVistaOrdine ? 560 : 480);
        popup.setMinHeight(420);
        popup.setMaxHeight(maxH + 20);
        popup.setResizable(true);
        popup.showAndWait();

        // Esecuzione post-chiusura per evitare finestre bianche su macOS con modali concatenate
        if (richiestaEliminazione[0] && onEliminaNotifica != null && notificaOriginale != null) {
            if (confermaEliminazioneAvviso(owner, dettaglio, provenza)) {
                onEliminaNotifica.accept(notificaOriginale);
            }
        } else if (navigazioneRichiesta[0] != null && onNavigaSezioneConCodice != null) {
            onNavigaSezioneConCodice.accept(navigazioneRichiesta[0], navigazioneRichiesta[1]);
        }
    }

    private static HBox creaIntestazioneFissa(DettaglioAvvisoOrdineBean d, boolean isVistaOrdine, boolean provenza) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 22, 14, 22));
        header.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: transparent transparent #e8d9e2 transparent; -fx-border-width: 0 0 1 0;"
                : "-fx-background-color: #1d1a22; -fx-border-color: transparent transparent rgba(186,164,135,0.25) transparent; -fx-border-width: 0 0 1 0;");

        String icona = risolviIconaTestata(d, isVistaOrdine);
        Label lblIcona = new Label(icona);
        lblIcona.setAlignment(Pos.CENTER);
        lblIcona.setMinSize(42, 42);
        lblIcona.setPrefSize(42, 42);
        lblIcona.setStyle(provenza
                ? "-fx-font-size: 20px; -fx-background-color: #f6ecf2; -fx-border-color: #e4d2de; "
                  + "-fx-border-radius: 12; -fx-background-radius: 12;"
                : "-fx-font-size: 20px; -fx-background-color: rgba(186,164,135,0.14); -fx-border-color: rgba(186,164,135,0.34); "
                  + "-fx-border-radius: 12; -fx-background-radius: 12;");

        VBox boxTitolo = new VBox(3);
        Label lblTitolo = new Label(d.getTitoloPulito() != null ? d.getTitoloPulito() : "Avviso");
        lblTitolo.setWrapText(true);
        lblTitolo.setStyle("-fx-font-family: " + FONT_SERIF + "; -fx-font-size: 17px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        String ts = (d.getTimestamp() != null && !d.getTimestamp().isBlank())
                ? "Registrato: " + d.getTimestamp()
                : "Notifica di sistema";
        Label lblTs = new Label(ts);
        lblTs.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-text-fill: "
                + (provenza ? "#7d6884;" : "#baa487;"));

        boxTitolo.getChildren().addAll(lblTitolo, lblTs);
        HBox.setHgrow(boxTitolo, Priority.ALWAYS);

        header.getChildren().addAll(lblIcona, boxTitolo);

        if (isVistaOrdine) {
            Label badgeStato = creaBadgeStatoOrdine(d.getStatoCodice(), d.getStatoEtichetta(), provenza);
            header.getChildren().add(badgeStato);
        } else {
            Label badgeCat = creaBadgeCategoriaGenerica(d.getTipoAvviso(), provenza);
            header.getChildren().add(badgeCat);
        }

        return header;
    }

    private static VBox creaCorpoOrdine(DettaglioAvvisoOrdineBean d, boolean provenza) {
        VBox corpo = new VBox(14);
        corpo.setPadding(new Insets(16, 22, 16, 22));
        corpo.setStyle("-fx-background-color: transparent;");

        // Sezione B: Informazioni generali (3 card compatte)
        GridPane grigliaInfo = creaGrigliaInfoGenerali(d, provenza);
        corpo.getChildren().add(grigliaInfo);

        // Sezione C: Elenco prodotti strutturato
        VBox cardProdotti = creaSezioneProdotti(d, provenza);
        corpo.getChildren().add(cardProdotti);

        return corpo;
    }

    private static GridPane creaGrigliaInfoGenerali(DettaglioAvvisoOrdineBean d, boolean provenza) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 3.0);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        VBox cardCliente = creaCardCliente(d, provenza);
        VBox cardModalita = creaCardModalita(d, provenza);
        VBox cardPagamento = creaCardPagamento(d, provenza);

        GridPane.setHgrow(cardCliente, Priority.ALWAYS);
        GridPane.setHgrow(cardModalita, Priority.ALWAYS);
        GridPane.setHgrow(cardPagamento, Priority.ALWAYS);

        grid.add(cardCliente, 0, 0);
        grid.add(cardModalita, 1, 0);
        grid.add(cardPagamento, 2, 0);

        return grid;
    }

    private static VBox creaCardCliente(DettaglioAvvisoOrdineBean d, boolean provenza) {
        VBox card = creaContenitoreCardInfo(provenza);

        card.getChildren().add(creaIntestazioneCardInfo("👤", "Cliente", provenza));

        String nome = (d.getNomeCliente() != null && !d.getNomeCliente().isBlank())
                ? d.getNomeCliente()
                : "Cliente non indicato";
        card.getChildren().add(creaValorePrincipaleCard(nome, provenza));

        if (d.getTelefonoCliente() != null && !d.getTelefonoCliente().isBlank()) {
            card.getChildren().add(creaRigaSecondariaCard("📞 " + d.getTelefonoCliente(), provenza));
        } else if (d.getEmailCliente() != null && !d.getEmailCliente().isBlank()
                && !d.getEmailCliente().equalsIgnoreCase(nome)) {
            card.getChildren().add(creaRigaSecondariaCard(d.getEmailCliente(), provenza));
        } else {
            card.getChildren().add(creaRigaSecondariaCard("Recapito non indicato", provenza));
        }

        if (d.getNominativoRitiro() != null && !d.getNominativoRitiro().isBlank()
                && !d.getNominativoRitiro().equalsIgnoreCase(nome)) {
            card.getChildren().add(creaRigaSecondariaCard("Ritiro a nome di: " + d.getNominativoRitiro(), provenza));
        }

        return card;
    }

    private static VBox creaCardModalita(DettaglioAvvisoOrdineBean d, boolean provenza) {
        VBox card = creaContenitoreCardInfo(provenza);

        String icona = d.isRitiroAlBanco() ? "🏪" : "🛵";
        card.getChildren().add(creaIntestazioneCardInfo(icona, "Modalità ordine", provenza));

        String modalita = (d.getModalitaLabel() != null && !d.getModalitaLabel().isBlank())
                ? d.getModalitaLabel()
                : "Ritiro al banco";
        card.getChildren().add(creaValorePrincipaleCard(modalita, provenza));

        if (d.getOrarioRichiesto() != null && !d.getOrarioRichiesto().isBlank()) {
            card.getChildren().add(creaRigaSecondariaCard("🕒 " + d.getOrarioRichiesto(), provenza));
        }
        if (d.getLuogoOIndirizzo() != null && !d.getLuogoOIndirizzo().isBlank()) {
            card.getChildren().add(creaRigaSecondariaCard("📍 " + d.getLuogoOIndirizzo(), provenza));
        }
        if (d.getNoteCliente() != null && !d.getNoteCliente().isBlank()) {
            Label lblNote = new Label("Note: " + d.getNoteCliente());
            lblNote.setWrapText(true);
            lblNote.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 11.5px; -fx-font-style: italic; "
                    + "-fx-text-fill: " + (provenza ? "#5f4967;" : "#d5bdaf;"));
            card.getChildren().add(lblNote);
        }

        return card;
    }

    private static VBox creaCardPagamento(DettaglioAvvisoOrdineBean d, boolean provenza) {
        VBox card = creaContenitoreCardInfo(provenza);

        card.getChildren().add(creaIntestazioneCardInfo("💳", "Pagamento", provenza));

        String metodo = (d.getMetodoPagamento() != null && !d.getMetodoPagamento().isBlank())
                ? d.getMetodoPagamento()
                : "Non specificato";
        card.getChildren().add(creaValorePrincipaleCard(metodo, provenza));

        if (d.getStatoPagamento() != null && !d.getStatoPagamento().isBlank()) {
            boolean pagato = "Pagato".equalsIgnoreCase(d.getStatoPagamento());
            Label lblStatoPag = new Label(pagato ? "● Pagato" : d.getStatoPagamento());
            lblStatoPag.setWrapText(true);
            lblStatoPag.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 11.5px; -fx-font-weight: bold; "
                    + "-fx-text-fill: " + (pagato
                    ? (provenza ? "#1d6f42;" : "#86efac;")
                    : (provenza ? "#7d6884;" : "#c7beb2;")));
            card.getChildren().add(lblStatoPag);
        }

        if (d.getDettaglioCartaMascherata() != null && !d.getDettaglioCartaMascherata().isBlank()) {
            card.getChildren().add(creaRigaSecondariaCard(d.getDettaglioCartaMascherata(), provenza));
        }
        if (d.getCodiceTransazione() != null && !d.getCodiceTransazione().isBlank()) {
            Label lblTrx = new Label("Rif: " + d.getCodiceTransazione());
            lblTrx.setWrapText(true);
            lblTrx.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 11px; "
                    + "-fx-text-fill: " + (provenza ? "#86738d;" : "#a89f92;"));
            card.getChildren().add(lblTrx);
        }

        return card;
    }

    private static VBox creaContenitoreCardInfo(boolean provenza) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(11, 13, 11, 13));
        card.setMaxHeight(Double.MAX_VALUE);
        card.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e6d7df; -fx-border-width: 1; "
                  + "-fx-border-radius: 12; -fx-background-radius: 12; "
                  + "-fx-effect: dropshadow(gaussian, rgba(55,37,61,0.05), 6, 0, 0, 1);"
                : "-fx-background-color: #1f1c24; -fx-border-color: rgba(186,164,135,0.24); -fx-border-width: 1; "
                  + "-fx-border-radius: 12; -fx-background-radius: 12;");
        return card;
    }

    private static HBox creaIntestazioneCardInfo(String icona, String titolo, boolean provenza) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lblIcon = new Label(icona);
        lblIcon.setStyle("-fx-font-size: 13px;");
        Label lblTit = new Label(titolo);
        lblTit.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 11.5px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + (provenza ? "#7a5e85;" : "#baa487;"));
        row.getChildren().addAll(lblIcon, lblTit);
        return row;
    }

    private static Label creaValorePrincipaleCard(String testo, boolean provenza) {
        Label lbl = new Label(testo);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 13.5px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));
        return lbl;
    }

    private static Label creaRigaSecondariaCard(String testo, boolean provenza) {
        Label lbl = new Label(testo);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; "
                + "-fx-text-fill: " + (provenza ? "#5d4f63;" : "#cfc6bc;"));
        return lbl;
    }

    private static VBox creaSezioneProdotti(DettaglioAvvisoOrdineBean d, boolean provenza) {
        VBox box = new VBox(0);
        box.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e6d7df; -fx-border-width: 1; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14; "
                  + "-fx-effect: dropshadow(gaussian, rgba(55,37,61,0.05), 6, 0, 0, 1);"
                : "-fx-background-color: #1f1c24; -fx-border-color: rgba(186,164,135,0.24); -fx-border-width: 1; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14;");

        int numPezzi = d.getTotalePezzi();
        int numRighe = d.getArticoli().size();

        HBox headerProdotti = new HBox(8);
        headerProdotti.setAlignment(Pos.CENTER_LEFT);
        headerProdotti.setPadding(new Insets(10, 14, 10, 14));
        headerProdotti.setStyle(provenza
                ? "-fx-background-color: #f7eef3; -fx-background-radius: 13 13 0 0; "
                  + "-fx-border-color: transparent transparent #eadce5 transparent; -fx-border-width: 0 0 1 0;"
                : "-fx-background-color: rgba(186,164,135,0.10); -fx-background-radius: 13 13 0 0; "
                  + "-fx-border-color: transparent transparent rgba(186,164,135,0.20) transparent; -fx-border-width: 0 0 1 0;");

        Label lblTitSezione = new Label("Elenco prodotti");
        lblTitSezione.setStyle("-fx-font-family: " + FONT_SERIF + "; -fx-font-size: 13.5px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String labelConteggio = numPezzi == 1
                ? "1 articolo"
                : (numPezzi + " articoli" + (numRighe != numPezzi ? " (" + numRighe + " righe)" : ""));
        Label badgeCount = new Label(labelConteggio);
        badgeCount.setStyle(provenza
                ? "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 11.5px; -fx-font-weight: bold; "
                  + "-fx-background-color: #eadced; -fx-text-fill: #5e316f; -fx-padding: 2 9; -fx-background-radius: 10;"
                : "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 11.5px; -fx-font-weight: bold; "
                  + "-fx-background-color: rgba(186,164,135,0.20); -fx-text-fill: #e5c99b; -fx-padding: 2 9; -fx-background-radius: 10;");

        headerProdotti.getChildren().addAll(lblTitSezione, spacer, badgeCount);
        box.getChildren().add(headerProdotti);

        List<RigaArticoloOrdine> articoli = d.getArticoli();
        if (articoli.isEmpty()) {
            Label lblVuoto = new Label("Nessun dettaglio articolo registrato per questo ordine.");
            lblVuoto.setPadding(new Insets(14, 16, 14, 16));
            lblVuoto.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12.5px; -fx-font-style: italic; "
                    + "-fx-text-fill: " + (provenza ? "#7d6884;" : "#a89f92;"));
            box.getChildren().add(lblVuoto);
            return box;
        }

        for (int i = 0; i < articoli.size(); i++) {
            RigaArticoloOrdine riga = articoli.get(i);
            boolean ultimo = (i == articoli.size() - 1);
            box.getChildren().add(creaRigaProdotto(riga, ultimo, provenza));
        }

        return box;
    }

    private static HBox creaRigaProdotto(RigaArticoloOrdine riga, boolean ultimo, boolean provenza) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(11, 14, 11, 14));
        if (!ultimo) {
            row.setStyle(provenza
                    ? "-fx-border-color: transparent transparent #f0e5ec transparent; -fx-border-width: 0 0 1 0;"
                    : "-fx-border-color: transparent transparent rgba(186,164,135,0.14) transparent; -fx-border-width: 0 0 1 0;");
        }

        // Badge quantita'
        Label lblQty = new Label(riga.getQuantita() + "×");
        lblQty.setMinWidth(34);
        lblQty.setAlignment(Pos.CENTER);
        lblQty.setStyle(provenza
                ? "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-weight: bold; "
                  + "-fx-background-color: #f0e3f3; -fx-text-fill: #5e316f; -fx-padding: 3 8; -fx-background-radius: 8;"
                : "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-weight: bold; "
                  + "-fx-background-color: rgba(186,164,135,0.20); -fx-text-fill: #e5c99b; -fx-padding: 3 8; -fx-background-radius: 8;");

        // Dettagli gerarchici del prodotto
        VBox colTesto = new VBox(3);
        HBox.setHgrow(colTesto, Priority.ALWAYS);

        Label lblNome = new Label(riga.getNomeProdotto());
        lblNome.setWrapText(true);
        lblNome.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 13.5px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));
        colTesto.getChildren().add(lblNome);

        if (!riga.getVarianti().isEmpty()) {
            colTesto.getChildren().add(creaDettaglioProdotto(
                    "Varianti: " + String.join(", ", riga.getVarianti()),
                    provenza ? "#5d4f63" : "#cfc6bc", false));
        }
        if (!riga.getTopping().isEmpty()) {
            colTesto.getChildren().add(creaDettaglioProdotto(
                    "Topping: " + String.join(", ", riga.getTopping()),
                    provenza ? "#6f497c" : "#d4b483", false));
        }
        if (!riga.getAggiunte().isEmpty()) {
            colTesto.getChildren().add(creaDettaglioProdotto(
                    "Extra: + " + String.join(", + ", riga.getAggiunte()),
                    provenza ? "#1d6f42" : "#86efac", false));
        }
        if (!riga.getEsclusioni().isEmpty()) {
            colTesto.getChildren().add(creaDettaglioProdotto(
                    String.join(" • ", riga.getEsclusioni()),
                    provenza ? "#b45309" : "#fcd34d", false));
        }
        if (!riga.getAltrePersonalizzazioni().isEmpty()) {
            colTesto.getChildren().add(creaDettaglioProdotto(
                    String.join(" • ", riga.getAltrePersonalizzazioni()),
                    provenza ? "#5d4f63" : "#cfc6bc", false));
        }
        if (!riga.getNoteProdotto().isEmpty()) {
            colTesto.getChildren().add(creaDettaglioProdotto(
                    "Nota: " + riga.getNoteProdotto(),
                    provenza ? "#7d6884" : "#a89f92", true));
        }

        row.getChildren().addAll(lblQty, colTesto);

        // Prezzo riga (solo se disponibile nei dati reali)
        if (riga.getSubtotaleRiga() != null) {
            VBox colPrezzo = new VBox(2);
            colPrezzo.setAlignment(Pos.TOP_RIGHT);
            colPrezzo.setMinWidth(Region.USE_PREF_SIZE);

            Label lblSub = new Label(DettaglioAvvisoOrdineBean.formattaEuro(riga.getSubtotaleRiga()));
            lblSub.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 13px; -fx-font-weight: bold; "
                    + "-fx-text-fill: " + (provenza ? "#37253d;" : "#e5c99b;"));
            colPrezzo.getChildren().add(lblSub);

            if (riga.getQuantita() > 1 && riga.getPrezzoUnitario() != null) {
                Label lblUnit = new Label(DettaglioAvvisoOrdineBean.formattaEuro(riga.getPrezzoUnitario()) + " cad.");
                lblUnit.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 11px; "
                        + "-fx-text-fill: " + (provenza ? "#86738d;" : "#a89f92;"));
                colPrezzo.getChildren().add(lblUnit);
            }
            row.getChildren().add(colPrezzo);
        }

        return row;
    }

    private static Label creaDettaglioProdotto(String testo, String colore, boolean corsivo) {
        Label lbl = new Label(testo);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-text-fill: " + colore + ";"
                + (corsivo ? " -fx-font-style: italic;" : ""));
        return lbl;
    }

    private static VBox creaCorpoGenerico(DettaglioAvvisoOrdineBean d, boolean provenza) {
        VBox corpo = new VBox(12);
        corpo.setPadding(new Insets(18, 22, 18, 22));

        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e6d7df; -fx-border-width: 1; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14; "
                  + "-fx-effect: dropshadow(gaussian, rgba(55,37,61,0.05), 6, 0, 0, 1);"
                : "-fx-background-color: #1f1c24; -fx-border-color: rgba(186,164,135,0.24); -fx-border-width: 1; "
                  + "-fx-border-radius: 14; -fx-background-radius: 14;");

        if (d.getCodiceOrdine() != null && !d.getCodiceOrdine().isBlank() && d.getTipoAvviso() == TipoAvviso.ORDINE) {
            Label lblNoteStorico = new Label("Riferimento ordine: " + d.getCodiceOrdine()
                    + " (dettaglio strutturato non più presente nell'archivio attivo)");
            lblNoteStorico.setWrapText(true);
            lblNoteStorico.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-weight: bold; "
                    + "-fx-text-fill: " + (provenza ? "#7a5e85;" : "#baa487;"));
            card.getChildren().add(lblNoteStorico);
        }

        String msg = (d.getMessaggioOriginale() != null && !d.getMessaggioOriginale().isBlank())
                ? d.getMessaggioOriginale()
                : "Nessun contenuto aggiuntivo disponibile.";
        Label lblMsg = new Label(msg);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 13.5px; -fx-line-spacing: 4px; "
                + "-fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));
        card.getChildren().add(lblMsg);

        corpo.getChildren().add(card);
        return corpo;
    }

    private static VBox creaBarraInferioreFissa(DettaglioAvvisoOrdineBean d,
                                                boolean isVistaOrdine,
                                                boolean provenza,
                                                boolean mostraElimina,
                                                boolean mostraNavigazione,
                                                Runnable onElimina,
                                                BiConsumer<String, String> onNaviga,
                                                Runnable onChiudi) {
        VBox footer = new VBox(10);
        footer.setPadding(new Insets(12, 22, 16, 22));
        footer.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-border-color: #e8d9e2 transparent transparent transparent; -fx-border-width: 1 0 0 0;"
                : "-fx-background-color: #1d1a22; -fx-border-color: rgba(186,164,135,0.25) transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        if (isVistaOrdine) {
            // Barra Totale ordine (stile lilla-cipria Frostly come da mockup)
            HBox barTotale = new HBox(12);
            barTotale.setAlignment(Pos.CENTER_LEFT);
            barTotale.setPadding(new Insets(10, 16, 10, 16));
            barTotale.setStyle(provenza
                    ? "-fx-background-color: #ede1f0; -fx-border-color: #d8c4de; -fx-border-width: 1; "
                      + "-fx-background-radius: 12; -fx-border-radius: 12;"
                    : "-fx-background-color: rgba(186,164,135,0.14); -fx-border-color: rgba(186,164,135,0.36); -fx-border-width: 1; "
                      + "-fx-background-radius: 12; -fx-border-radius: 12;");

            Label lblTitTot = new Label("Totale ordine");
            lblTitTot.setStyle("-fx-font-family: " + FONT_SERIF + "; -fx-font-size: 14px; -fx-font-weight: bold; "
                    + "-fx-text-fill: " + (provenza ? "#553363;" : "#d4b483;"));

            Region divTot = new Region();
            divTot.setMinWidth(1);
            divTot.setPrefHeight(18);
            divTot.setStyle("-fx-background-color: " + (provenza ? "#c7afd0;" : "rgba(186,164,135,0.35);"));

            String totaleStr = d.getPrezzoTotale() != null
                    ? d.getTotaleFormattato()
                    : "Non disponibile";
            Label lblImporto = new Label(totaleStr);
            lblImporto.setStyle("-fx-font-family: " + FONT_SERIF + "; -fx-font-size: 18px; -fx-font-weight: bold; "
                    + "-fx-text-fill: " + (provenza ? "#3f224b;" : "#f5ebe0;"));

            Region spTot = new Region();
            HBox.setHgrow(spTot, Priority.ALWAYS);

            int pezzi = d.getTotalePezzi();
            Label lblPezzi = new Label(pezzi > 0 ? (pezzi == 1 ? "1 articolo" : pezzi + " articoli") : "");
            lblPezzi.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-weight: bold; "
                    + "-fx-text-fill: " + (provenza ? "#6f497c;" : "#baa487;"));

            barTotale.getChildren().addAll(lblTitTot, divTot, lblImporto, spTot, lblPezzi);
            footer.getChildren().add(barTotale);

            if (d.getNotaInformativaFooter() != null && !d.getNotaInformativaFooter().isBlank()) {
                HBox infoStrip = new HBox(7);
                infoStrip.setAlignment(Pos.CENTER_LEFT);
                infoStrip.setPadding(new Insets(6, 12, 6, 12));
                infoStrip.setStyle(provenza
                        ? "-fx-background-color: #f8f2f6; -fx-border-color: #ebe0e7; -fx-border-radius: 9; -fx-background-radius: 9;"
                        : "-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(186,164,135,0.16); -fx-border-radius: 9; -fx-background-radius: 9;");
                Label lblInfo = new Label("ⓘ  " + d.getNotaInformativaFooter());
                lblInfo.setWrapText(true);
                lblInfo.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-style: italic; "
                        + "-fx-text-fill: " + (provenza ? "#6c5a73;" : "#bfb6ab;"));
                infoStrip.getChildren().add(lblInfo);
                footer.getChildren().add(infoStrip);
            }
        }

        // Riga pulsanti azione
        HBox azioni = new HBox(10);
        azioni.setAlignment(Pos.CENTER_LEFT);

        if (mostraElimina) {
            Button btnElimina = new Button("Elimina Avviso");
            btnElimina.setStyle(provenza
                    ? "-fx-background-color: #fff6f6; -fx-text-fill: #b91c1c; -fx-border-color: #e59a9e; -fx-border-width: 1.2; "
                      + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-family: " + FONT_SANS + "; "
                      + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;"
                    : "-fx-background-color: rgba(127, 29, 29, 0.28); -fx-text-fill: #fca5a5; -fx-border-color: rgba(239, 68, 68, 0.55); -fx-border-width: 1.2; "
                      + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-family: " + FONT_SANS + "; "
                      + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
            btnElimina.setOnAction(e -> onElimina.run());
            azioni.getChildren().add(btnElimina);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        azioni.getChildren().add(spacer);

        if (mostraNavigazione) {
            Node btnNav = creaPulsanteNavigazione(d, onNaviga);
            if (btnNav != null) {
                azioni.getChildren().add(btnNav);
            }
        }

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-text-fill: #37253d; -fx-border-color: #d6c5d1; -fx-border-width: 1.2; "
                  + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-family: " + FONT_SANS + "; "
                  + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 18; -fx-cursor: hand;"
                : "-fx-background-color: rgba(255,255,255,0.07); -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.32); -fx-border-width: 1.2; "
                  + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-family: " + FONT_SANS + "; "
                  + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 18; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> onChiudi.run());
        azioni.getChildren().add(btnChiudi);

        footer.getChildren().add(azioni);
        return footer;
    }

    private static Button creaPulsanteNavigazione(DettaglioAvvisoOrdineBean d, BiConsumer<String, String> onNaviga) {
        String testo;
        String tabTarget;
        switch (d.getTipoAvviso()) {
            case ORDINE -> {
                testo = "Vai a Gestione Ordini →";
                tabTarget = "Ordini";
            }
            case CHAT_ORDINE -> {
                testo = "Vai a Chat Ordini →";
                tabTarget = "Chat";
            }
            case EMERGENZA -> {
                testo = "Vai a Turni & Emergenze →";
                tabTarget = "Turni";
            }
            case GUSTO -> {
                testo = "Vai a Gusti su Misura →";
                tabTarget = "Magazzino";
            }
            case RECENSIONE -> {
                testo = "Vai a Recensioni →";
                tabTarget = "Recensioni";
            }
            default -> {
                return null;
            }
        }

        Button btn = new Button(testo);
        btn.setStyle("-fx-background-color: linear-gradient(to right, #845596, #693f7a); -fx-text-fill: #ffffff; "
                + "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 13px; -fx-font-weight: bold; "
                + "-fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(105,63,122,0.28), 6, 0, 0, 2);");
        btn.setOnAction(e -> onNaviga.accept(tabTarget, d.getCodiceOrdine()));
        return btn;
    }

    private static Label creaBadgeStatoOrdine(String statoCodice, String etichetta, boolean provenza) {
        String code = statoCodice != null ? statoCodice.toUpperCase(Locale.ROOT) : "IN_ATTESA";
        String text = (etichetta != null && !etichetta.isBlank()) ? etichetta : "Da confermare";

        String bg;
        String border;
        String fg;
        String prefix;

        switch (code) {
            case "IN_ATTESA" -> {
                prefix = "⏱ ";
                bg = provenza ? "#fdf0c8" : "rgba(180, 120, 20, 0.25)";
                border = provenza ? "#e0b864" : "#eab308";
                fg = provenza ? "#7a4c08" : "#fde047";
            }
            case "RIFIUTATA" -> {
                prefix = "✕ ";
                bg = provenza ? "#fdeaea" : "rgba(153, 27, 27, 0.30)";
                border = provenza ? "#e5989b" : "#ef4444";
                fg = provenza ? "#9e1c24" : "#fca5a5";
            }
            case "IN_PREPARAZIONE", "IN_CONSEGNA" -> {
                prefix = "● ";
                bg = provenza ? "#eee4f7" : "rgba(118, 76, 134, 0.32)";
                border = provenza ? "#bfa0d6" : "#a855f7";
                fg = provenza ? "#552f70" : "#e9d5ff";
            }
            default -> {
                prefix = "✓ ";
                bg = provenza ? "#e3f5ea" : "rgba(22, 101, 52, 0.30)";
                border = provenza ? "#86c9a0" : "#22c55e";
                fg = provenza ? "#195c34" : "#86efac";
            }
        }

        Label badge = new Label(prefix + text);
        badge.setMinWidth(Region.USE_PREF_SIZE);
        badge.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-weight: bold; "
                + "-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-text-fill: " + fg + "; "
                + "-fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 5 12;");
        return badge;
    }

    private static Label creaBadgeCategoriaGenerica(TipoAvviso tipo, boolean provenza) {
        String testo = switch (tipo) {
            case ORDINE -> "Ordine";
            case CHAT_ORDINE -> "Chat Ordine";
            case EMERGENZA -> "Emergenza";
            case GUSTO -> "Richiesta Gusto";
            case RECENSIONE -> "Recensione";
            default -> "Comunicazione";
        };
        Label badge = new Label(testo);
        badge.setMinWidth(Region.USE_PREF_SIZE);
        badge.setStyle(provenza
                ? "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-weight: bold; "
                  + "-fx-background-color: #f0e4f3; -fx-border-color: #d6bedd; -fx-text-fill: #5e316f; "
                  + "-fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 4 12;"
                : "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12px; -fx-font-weight: bold; "
                  + "-fx-background-color: rgba(186,164,135,0.18); -fx-border-color: rgba(186,164,135,0.40); -fx-text-fill: #e5c99b; "
                  + "-fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 4 12;");
        return badge;
    }

    private static boolean confermaEliminazioneAvviso(Window owner, DettaglioAvvisoOrdineBean d, boolean provenza) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Frostly — Conferma eliminazione avviso");
        if (owner != null) {
            dialog.initOwner(owner);
        }

        final boolean[] confermato = {false};

        VBox root = new VBox(14);
        root.setPadding(new Insets(20, 22, 18, 22));
        root.setStyle(provenza
                ? "-fx-background-color: #faf4f5; -fx-background-image: none;"
                : "-fx-background-color: #161419; -fx-background-image: none;");

        Label lblTit = new Label("Eliminare questo avviso dal registro?");
        lblTit.setWrapText(true);
        lblTit.setStyle("-fx-font-family: " + FONT_SERIF + "; -fx-font-size: 15.5px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

        String corpoMsg = (d.getTipoAvviso() == TipoAvviso.ORDINE && d.getCodiceOrdine() != null)
                ? "Verrà rimossa soltanto questa notifica. L'ordine " + d.getCodiceOrdine()
                  + " resterà invariato nella sezione Gestione Ordini."
                : "Questa notifica verrà rimossa definitivamente dal registro avvisi.";
        Label lblDesc = new Label(corpoMsg);
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-family: " + FONT_SANS + "; -fx-font-size: 13px; -fx-line-spacing: 3px; "
                + "-fx-text-fill: " + (provenza ? "#5d4f63;" : "#cfc6bc;"));

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle(provenza
                ? "-fx-background-color: #ffffff; -fx-text-fill: #37253d; -fx-border-color: #d6c5d1; -fx-border-radius: 9; "
                  + "-fx-background-radius: 9; -fx-font-family: " + FONT_SANS + "; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;"
                : "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #ded7cd; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 9; "
                  + "-fx-background-radius: 9; -fx-font-family: " + FONT_SANS + "; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> dialog.close());

        Button btnConferma = new Button("Elimina solo l'avviso");
        btnConferma.setStyle("-fx-background-color: #b91c1c; -fx-text-fill: #ffffff; -fx-background-radius: 9; "
                + "-fx-font-family: " + FONT_SANS + "; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 7 16; -fx-cursor: hand;");
        btnConferma.setOnAction(e -> {
            confermato[0] = true;
            dialog.close();
        });

        buttons.getChildren().addAll(btnAnnulla, btnConferma);
        root.getChildren().addAll(lblTit, lblDesc, buttons);

        Scene scene = new Scene(root, 430, 175);
        scene.setFill(Color.web(provenza ? "#faf4f5" : "#161419"));
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
        return confermato[0];
    }

    private static String risolviIconaTestata(DettaglioAvvisoOrdineBean d, boolean isVistaOrdine) {
        if (isVistaOrdine) {
            return d.isRitiroAlBanco() ? "🛍️" : "🛵";
        }
        return switch (d.getTipoAvviso()) {
            case ORDINE -> "🛵";
            case CHAT_ORDINE -> "💬";
            case EMERGENZA -> "🚨";
            case GUSTO -> "🎨";
            case RECENSIONE -> "⭐";
            default -> "📋";
        };
    }

    private static Rectangle2D trovaAreaSchermo(Window owner) {
        try {
            if (owner != null) {
                List<Screen> screens = Screen.getScreensForRectangle(
                        owner.getX(), owner.getY(),
                        Math.max(1, owner.getWidth()), Math.max(1, owner.getHeight()));
                if (!screens.isEmpty()) {
                    return screens.get(0).getVisualBounds();
                }
            }
            return Screen.getPrimary().getVisualBounds();
        } catch (Exception ignored) {
            return new Rectangle2D(0, 0, 1280, 800);
        }
    }

    private static double calcolaAltezzaIdeale(DettaglioAvvisoOrdineBean d, boolean isVistaOrdine, double maxH) {
        if (!isVistaOrdine) {
            int len = d.getMessaggioOriginale() != null ? d.getMessaggioOriginale().length() : 0;
            double stimata = 270 + Math.min(280, (len / 70.0) * 22.0);
            return Math.min(maxH, Math.max(360, stimata));
        }

        int numRighe = d.getArticoli().size();
        int extraDettagli = 0;
        for (RigaArticoloOrdine r : d.getArticoli()) {
            if (r.haDettagli()) {
                extraDettagli++;
            }
        }
        // Base (header + 3 info card + intestazione lista + barra totale + bottoni) ~ 435px
        double stimata = 445 + (numRighe * 48.0) + (extraDettagli * 20.0);
        return Math.min(maxH, Math.max(500, stimata));
    }
}
