package view;

import bean.OrdineBean;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
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
 * Componente View dedicato alla visualizzazione e alle card grafiche degli ordini clienti per lo Staff.
 * Rispetta l'architettura BCE con zero chiamate a setStyle nei controller grafici.
 */
public final class OrdiniStaffView {

    public static final String HEADER_PREFIX = "__HEADER__";
    private static final DateTimeFormatter FMT_GIORNO_IT =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN);

    private OrdiniStaffView() {
    }

    public static boolean isHeaderGiorno(OrdineBean item) {
        return item != null && item.getCodiceOrdine() != null
                && item.getCodiceOrdine().startsWith(HEADER_PREFIX);
    }

    public static OrdineBean creaHeaderGiorno(String etichetta) {
        OrdineBean header = new OrdineBean();
        header.setCodiceOrdine(HEADER_PREFIX + etichetta);
        header.setDescrizioneCompleta(etichetta);
        return header;
    }

    public static Label creaBadgeStatoOrdine(OrdineBean item) {
        String fase = control.applicativo.OrdiniControl.calcolaFaseConsegna(item);
        return creaBadgeStatoOrdine(fase);
    }

    public static Label creaBadgeStatoOrdine(String stato) {
        String badgeText;
        String badgeStyle;
        if ("IN_ATTESA".equalsIgnoreCase(stato)) {
            badgeText = "🔔 NUOVO DA CONFERMARE";
            badgeStyle = "-fx-background-color: rgba(65, 48, 20, 0.90); -fx-text-fill: #fcd34d; -fx-border-color: #baa487;";
        } else if ("IN_PREPARAZIONE".equalsIgnoreCase(stato) || "APPROVATA".equalsIgnoreCase(stato)) {
            badgeText = "🟢 In Preparazione";
            badgeStyle = "-fx-background-color: rgba(22, 55, 38, 0.90); -fx-text-fill: #a7f3d0; -fx-border-color: #10b981;";
        } else if ("IN_CONSEGNA".equalsIgnoreCase(stato)) {
            badgeText = "🛵 In Consegna";
            badgeStyle = "-fx-background-color: rgba(25, 45, 72, 0.92); -fx-text-fill: #93c5fd; -fx-border-color: #3b82f6;";
        } else if ("PRONTO_AL_BANCO".equalsIgnoreCase(stato)) {
            badgeText = "🛍️ Pronto al Banco";
            badgeStyle = "-fx-background-color: rgba(58, 46, 20, 0.92); -fx-text-fill: #fde68a; -fx-border-color: #d4b483;";
        } else if ("CONSEGNATO".equalsIgnoreCase(stato)) {
            badgeText = "✅ Consegnato";
            badgeStyle = "-fx-background-color: rgba(16, 62, 52, 0.92); -fx-text-fill: #6ee7b7; -fx-border-color: #059669;";
        } else if ("RITIRATO".equalsIgnoreCase(stato)) {
            badgeText = "✅ Ritirato";
            badgeStyle = "-fx-background-color: rgba(16, 62, 52, 0.92); -fx-text-fill: #6ee7b7; -fx-border-color: #059669;";
        } else if ("RIFIUTATA".equalsIgnoreCase(stato)) {
            badgeText = "🔴 Disdetto / Rifiutato";
            badgeStyle = "-fx-background-color: rgba(70, 20, 25, 0.90); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444;";
        } else {
            badgeText = "🔵 " + stato;
            badgeStyle = "-fx-background-color: rgba(25, 45, 65, 0.90); -fx-text-fill: #93c5fd; -fx-border-color: #3b82f6;";
        }
        Label lbl = new Label(badgeText);
        lbl.setStyle(badgeStyle + " -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 3 9; -fx-background-radius: 8; -fx-border-radius: 8;");
        return lbl;
    }

    public static void configuraCelleOrdiniStaff(ListView<OrdineBean> listaOrdiniStaff,
                                                 Consumer<String> onConfermaOrdine,
                                                 Consumer<String> onDisdiciOrdine) {
        if (listaOrdiniStaff == null) return;
        listaOrdiniStaff.setCellFactory(lv -> new ListCell<OrdineBean>() {
            private HBox rigaCorrente;

            {
                selectedProperty().addListener((obs, oldSel, newSel) -> aggiornaStileSelezione());
            }

            private void aggiornaStileSelezione() {
                OrdineBean item = getItem();
                if (rigaCorrente == null || item == null || isEmpty() || isHeaderGiorno(item)) {
                    return;
                }
                applicaStileRigaOrdine(rigaCorrente, item, isSelected(), RecensioniView.isTemaSalottoChiaro(listaOrdiniStaff));
            }

            @Override
            protected void updateItem(OrdineBean item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    boolean provenza = RecensioniView.isTemaSalottoChiaro(listaOrdiniStaff);
                    if (isHeaderGiorno(item)) {
                        rigaCorrente = null;
                        setDisable(true);
                        Label lblTitolo = new Label(item.getDescrizioneCompleta());
                        lblTitolo.setStyle(provenza
                                ? "-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #5e316f; "
                                  + "-fx-background-color: #eee2ee; -fx-padding: 5 12; "
                                  + "-fx-background-radius: 10; -fx-border-color: #ddcddc; -fx-border-radius: 10;"
                                : "-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #d4b483; "
                                  + "-fx-background-color: rgba(212, 180, 131, 0.12); -fx-padding: 5 12; "
                                  + "-fx-background-radius: 8; -fx-border-color: rgba(212, 180, 131, 0.25); -fx-border-radius: 8;");
                        HBox boxHeader = new HBox(lblTitolo);
                        boxHeader.setAlignment(Pos.CENTER_LEFT);
                        boxHeader.setStyle("-fx-padding: 8 2 4 2;");
                        setGraphic(boxHeader);
                        setText(null);
                        setStyle("-fx-background-color: transparent; -fx-opacity: 1.0;");
                        return;
                    }

                    setDisable(false);
                    Runnable apriDettaglio = () -> {
                        javafx.stage.Window owner = listaOrdiniStaff.getScene() != null
                                ? listaOrdiniStaff.getScene().getWindow() : null;
                        mostraPopupDettaglioOrdine(owner, item, onConfermaOrdine, onDisdiciOrdine);
                    };
                    rigaCorrente = buildRigaCompattaOrdineStaff(item, isSelected(), provenza, apriDettaglio);
                    rigaCorrente.maxWidthProperty().bind(listaOrdiniStaff.widthProperty().subtract(28));
                    rigaCorrente.setOnMouseClicked(e -> {
                        listaOrdiniStaff.getSelectionModel().select(getIndex());
                        if (e.getClickCount() == 2) {
                            apriDettaglio.run();
                        }
                    });
                    setGraphic(rigaCorrente);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
                } else {
                    rigaCorrente = null;
                    setDisable(false);
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    private static void applicaStileRigaOrdine(HBox riga, OrdineBean item, boolean selezionato, boolean provenza) {
        boolean isNuovo = "IN_ATTESA".equalsIgnoreCase(item.getStato());
        boolean isConfermato = "APPROVATA".equalsIgnoreCase(item.getStato());

        if (provenza) {
            if (selezionato) {
                riga.setStyle("-fx-padding: 12 16; -fx-background-color: #eee2ee; -fx-border-color: #764c86; "
                        + "-fx-border-width: 1.8; -fx-border-radius: 14; -fx-background-radius: 14;");
            } else if (isNuovo) {
                riga.setStyle("-fx-padding: 12 16; -fx-background-color: #fffaf7; -fx-border-color: #9366a0; "
                        + "-fx-border-width: 1.5; -fx-border-radius: 14; -fx-background-radius: 14;");
            } else if (isConfermato) {
                riga.setStyle("-fx-padding: 11 16; -fx-background-color: #ffffff; -fx-border-color: #d7e6dc; "
                        + "-fx-border-width: 1.0; -fx-border-radius: 14; -fx-background-radius: 14;");
            } else {
                riga.setStyle("-fx-padding: 11 16; -fx-background-color: #fdf7f6; -fx-border-color: #ebd3d3; "
                        + "-fx-border-width: 1.0; -fx-border-radius: 14; -fx-background-radius: 14;");
            }
            return;
        }

        if (selezionato) {
            riga.setStyle("-fx-padding: 12 16; -fx-background-color: rgba(212, 180, 131, 0.22); -fx-border-color: #e5c99b; "
                    + "-fx-border-width: 2.0; -fx-border-radius: 12; -fx-background-radius: 12; "
                    + "-fx-effect: dropshadow(gaussian, rgba(212,180,131,0.35), 10, 0, 0, 2);");
        } else if (isNuovo) {
            riga.setStyle("-fx-padding: 12 16; -fx-background-color: rgba(30, 26, 33, 0.96); -fx-border-color: #baa487; "
                    + "-fx-border-width: 1.6; -fx-border-radius: 12; -fx-background-radius: 12; "
                    + "-fx-effect: dropshadow(gaussian, rgba(186,164,135,0.22), 8, 0, 0, 2);");
        } else if (isConfermato) {
            riga.setStyle("-fx-padding: 11 16; -fx-background-color: rgba(22, 20, 25, 0.92); -fx-border-color: rgba(16,185,129,0.35); "
                    + "-fx-border-width: 1.0; -fx-border-radius: 12; -fx-background-radius: 12;");
        } else {
            riga.setStyle("-fx-padding: 11 16; -fx-background-color: rgba(18, 17, 20, 0.86); -fx-border-color: rgba(239,68,68,0.28); "
                    + "-fx-border-width: 1.0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-opacity: 0.85;");
        }
    }

    private static HBox buildRigaCompattaOrdineStaff(OrdineBean item, boolean selezionato, Runnable onApriDettaglio) {
        return buildRigaCompattaOrdineStaff(item, selezionato, false, onApriDettaglio);
    }

    private static HBox buildRigaCompattaOrdineStaff(OrdineBean item, boolean selezionato, boolean provenza, Runnable onApriDettaglio) {
        boolean isNuovo = "IN_ATTESA".equalsIgnoreCase(item.getStato());

        HBox riga = new HBox(14);
        riga.setAlignment(Pos.CENTER_LEFT);
        riga.setCursor(javafx.scene.Cursor.HAND);
        applicaStileRigaOrdine(riga, item, selezionato, provenza);

        Label lblId = new Label(item.getCodiceOrdine());
        lblId.setMinWidth(Region.USE_PREF_SIZE);
        if (provenza) {
            lblId.setStyle(isNuovo
                    ? "-fx-background-color: #764c86; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 4 11; -fx-background-radius: 10;"
                    : "-fx-background-color: #f3eae5; -fx-text-fill: #5e316f; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 4 11; -fx-background-radius: 10;");
        } else {
            lblId.setStyle(isNuovo
                    ? "-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 4 11; -fx-background-radius: 8;"
                    : "-fx-background-color: rgba(186,164,135,0.18); -fx-text-fill: #baa487; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 4 11; -fx-background-radius: 8;");
        }

        String nomeMostrato = (item.getNomeCliente() != null && !item.getNomeCliente().isBlank())
                ? item.getNomeCliente()
                : (item.getEmailCliente() != null ? item.getEmailCliente() : "Cliente");
        VBox boxCliente = new VBox(2);
        Label lblNome = new Label("👤 " + nomeMostrato);
        lblNome.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));
        boxCliente.getChildren().add(lblNome);
        if (item.getEmailCliente() != null && !item.getEmailCliente().isBlank()
                && !item.getEmailCliente().equalsIgnoreCase(nomeMostrato)) {
            Label lblMail = new Label(item.getEmailCliente());
            lblMail.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (provenza ? "#756978;" : "#a89f92;"));
            boxCliente.getChildren().add(lblMail);
        }

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblTotale = new Label(String.format(Locale.US, "EUR %.2f", item.getPrezzoTotale()));
        lblTotale.setMinWidth(Region.USE_PREF_SIZE);
        lblTotale.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + (provenza ? "#5e316f;" : "#baa487;"));

        Label lblStato = creaBadgeStatoOrdine(item);
        lblStato.setMinWidth(Region.USE_PREF_SIZE);

        Button btnDet = new Button("Dettagli ➔");
        btnDet.setMinWidth(Region.USE_PREF_SIZE);
        btnDet.setStyle(provenza
                ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #5e316f; "
                  + "-fx-background-color: #f3eae5; -fx-border-color: #ddcddc; "
                  + "-fx-border-radius: 10; -fx-padding: 4 11; -fx-background-radius: 10; -fx-cursor: hand;"
                : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ded7cd; "
                  + "-fx-background-color: rgba(255,255,255,0.08); -fx-border-color: rgba(255,255,255,0.12); "
                  + "-fx-border-radius: 8; -fx-padding: 4 10; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDet.setOnAction(e -> {
            e.consume();
            if (onApriDettaglio != null) {
                onApriDettaglio.run();
            }
        });

        riga.getChildren().addAll(lblId, boxCliente, sp, lblTotale, lblStato, btnDet);
        return riga;
    }

    public static void mostraPopupStoricoOrdiniPerGiorno(javafx.stage.Window owner,
                                                         Map<LocalDate, List<OrdineBean>> ordiniPerGiorno,
                                                         Consumer<String> onConfermaOrdine,
                                                         Consumer<String> onDisdiciOrdine) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Storico Ordini per Giorno");
        if (owner != null) {
            popup.initOwner(owner);
        }

        Label lblTitolo = new Label("📚 Storico Ordini Archiviati (per Giorno)");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        Label lblSub = new Label("Ordini dei giorni precedenti (ieri, l'altro ieri e archivio), raggruppati per data.");
        lblSub.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #a89f92;");
        VBox header = new VBox(3, lblTitolo, lblSub);

        VBox contenuto = new VBox(14);
        contenuto.setStyle("-fx-padding: 4 6 8 2;");

        if (ordiniPerGiorno == null || ordiniPerGiorno.isEmpty()) {
            Label vuoto = new Label("Nessun ordine nei giorni precedenti.\nGli ordini di ieri e dei giorni passati appariranno qui raggruppati per data.");
            vuoto.setWrapText(true);
            vuoto.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #a89f92; -fx-padding: 30 16;");
            contenuto.getChildren().add(vuoto);
        } else {
            LocalDate oggi = LocalDate.now();
            for (Map.Entry<LocalDate, List<OrdineBean>> entry : ordiniPerGiorno.entrySet()) {
                LocalDate data = entry.getKey();
                List<OrdineBean> listaGiorno = entry.getValue();
                double incassoGiorno = 0.0;
                for (OrdineBean ob : listaGiorno) {
                    if (!"RIFIUTATA".equalsIgnoreCase(ob.getStato())) {
                        incassoGiorno += ob.getPrezzoTotale();
                    }
                }
                String dataFormattata = data.format(FMT_GIORNO_IT);
                dataFormattata = Character.toUpperCase(dataFormattata.charAt(0)) + dataFormattata.substring(1);
                if (data.equals(oggi.minusDays(1))) {
                    dataFormattata = "Ieri (" + dataFormattata + ")";
                } else if (data.equals(oggi.minusDays(2))) {
                    dataFormattata = "L'altro ieri (" + dataFormattata + ")";
                }

                Label lblData = new Label("📅 " + dataFormattata + "  •  " + listaGiorno.size()
                        + (listaGiorno.size() == 1 ? " ordine" : " ordini")
                        + String.format(Locale.US, "  •  EUR %.2f", incassoGiorno));
                lblData.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #d4b483; "
                        + "-fx-background-color: rgba(212, 180, 131, 0.12); -fx-padding: 6 12; -fx-background-radius: 8;");

                VBox boxGiorno = new VBox(7, lblData);
                for (OrdineBean ob : listaGiorno) {
                    HBox riga = buildRigaCompattaOrdineStaff(ob, false, () ->
                            mostraPopupDettaglioOrdine(popup, ob, onConfermaOrdine, onDisdiciOrdine));
                    boxGiorno.getChildren().add(riga);
                }
                contenuto.getChildren().add(boxGiorno);
            }
        }

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(contenuto);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-font-size: 13px; "
                + "-fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 7 18; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());

        HBox footer = new HBox(btnChiudi);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(14, header, scroll, footer);
        root.setStyle("-fx-padding: 20 24; -fx-background-color: rgba(16, 15, 18, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 780, 560);
        scene.setFill(javafx.scene.paint.Color.web("#121114"));
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(popup.getScene());
        popup.centerOnScreen();
        popup.showAndWait();
    }

    public static void mostraPopupDettaglioOrdine(javafx.stage.Window owner,
                                                  OrdineBean item,
                                                  Consumer<String> onConfermaOrdine,
                                                  Consumer<String> onDisdiciOrdine) {
        if (item == null) return;

        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Dettaglio Ordine " + item.getCodiceOrdine());
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox cardDettaglio = buildCardOrdineStaff(
                item,
                cod -> {
                    popup.close();
                    if (onConfermaOrdine != null) {
                        javafx.application.Platform.runLater(() -> onConfermaOrdine.accept(cod));
                    }
                },
                cod -> {
                    popup.close();
                    if (onDisdiciOrdine != null) {
                        javafx.application.Platform.runLater(() -> onDisdiciOrdine.accept(cod));
                    }
                });

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(cardDettaglio);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-font-size: 13px; "
                + "-fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 7 18; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());

        HBox footer = new HBox(btnChiudi);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(14, scroll, footer);
        root.setStyle("-fx-padding: 18 22; -fx-background-color: rgba(16, 15, 18, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");
        root.setPrefWidth(720);
        root.setPrefHeight(500);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 720, 500);
        scene.setFill(javafx.scene.paint.Color.web("#121114"));
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(popup.getScene());
        popup.centerOnScreen();
        popup.showAndWait();
    }

    public static VBox buildCardOrdineStaff(OrdineBean item,
                                            Consumer<String> onConfermaOrdine,
                                            Consumer<String> onDisdiciOrdine) {
        boolean isNuovo = "IN_ATTESA".equalsIgnoreCase(item.getStato());
        boolean isConfermato = "APPROVATA".equalsIgnoreCase(item.getStato());

        VBox card = new VBox(10);
        if (isNuovo) {
            card.setStyle("-fx-padding: 14 18; -fx-background-color: rgba(28, 25, 30, 0.95); -fx-border-color: #baa487; "
                    + "-fx-border-width: 2.0; -fx-border-radius: 12; -fx-background-radius: 12; "
                    + "-fx-effect: dropshadow(gaussian, rgba(186,164,135,0.30), 12, 0, 0, 3);");
        } else if (isConfermato) {
            card.setStyle("-fx-padding: 13 16; -fx-background-color: rgba(22, 20, 24, 0.90); -fx-border-color: rgba(186,164,135,0.28); "
                    + "-fx-border-width: 1.0; -fx-border-radius: 12; -fx-background-radius: 12; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.40), 8, 0, 0, 2);");
        } else {
            card.setStyle("-fx-padding: 13 16; -fx-background-color: rgba(18, 17, 20, 0.85); -fx-border-color: rgba(100,95,105,0.25); "
                    + "-fx-border-width: 1.0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-opacity: 0.85;");
        }

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label lblId = new Label(item.getCodiceOrdine());
        lblId.setMinWidth(Region.USE_PREF_SIZE);
        if (isNuovo) {
            lblId.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                    + "-fx-font-size: 13px; -fx-padding: 4 12; -fx-background-radius: 8;");
        } else if (isConfermato) {
            lblId.setStyle("-fx-background-color: rgba(30,55,42,0.9); -fx-text-fill: #a7f3d0; -fx-font-weight: bold; "
                    + "-fx-font-size: 14px; -fx-padding: 4 11; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-radius: 8;");
        } else {
            lblId.setStyle("-fx-background-color: rgba(38,36,40,0.85); -fx-text-fill: #baa487; -fx-font-weight: bold; "
                    + "-fx-font-size: 14px; -fx-padding: 4 11; -fx-background-radius: 8;");
        }

        String nomeCli = (item.getNomeCliente() != null && !item.getNomeCliente().isBlank())
                ? item.getNomeCliente()
                : (item.getEmailCliente() != null ? item.getEmailCliente() : "Cliente");
        Button btnCliente = new Button("👤  " + nomeCli + "  •  Contatta Cliente ›");
        btnCliente.setStyle("-fx-background-color: rgba(212, 180, 131, 0.14); -fx-text-fill: #f5ebe0; "
                + "-fx-border-color: rgba(212, 180, 131, 0.38); -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-cursor: hand;");
        btnCliente.setOnAction(e -> {
            javafx.stage.Window win = btnCliente.getScene() != null ? btnCliente.getScene().getWindow() : null;
            mostraPopupContattoCliente(win, item);
        });

        Region spTop = new Region();
        HBox.setHgrow(spTop, Priority.ALWAYS);

        Label lblBadge = creaBadgeStatoOrdine(item);

        top.getChildren().addAll(lblId, btnCliente, spTop, lblBadge);
        String rawDataRitiro = item.getDataRitiro() != null ? item.getDataRitiro() : "Ritiro al banco";
        String infoConsegna = rawDataRitiro;
        String noteIndirizzo = "";
        if (rawDataRitiro.contains("• Indirizzo/Note:")) {
            String[] splitNote = rawDataRitiro.split("• Indirizzo/Note:");
            infoConsegna = splitNote[0].trim();
            if (splitNote.length > 1) {
                noteIndirizzo = splitNote[1].trim();
            }
        }

        VBox boxConsegna = new VBox(6);
        boxConsegna.setStyle("-fx-background-color: rgba(18, 16, 20, 0.75); -fx-border-color: rgba(186,164,135,0.20); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");

        boolean isAsporto = infoConsegna.toLowerCase().contains("banco") || infoConsegna.toLowerCase().contains("ritiro");
        String prefissoCanale = (infoConsegna.startsWith("🛵") || infoConsegna.startsWith("🛍"))
                ? ""
                : (isAsporto ? "🛍️  " : "🛵  ");
        Label lblMod = new Label(prefissoCanale + infoConsegna);
        lblMod.setWrapText(true);
        lblMod.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
        boxConsegna.getChildren().add(lblMod);

        if (!noteIndirizzo.isEmpty()) {
            Label lblNote = new Label("📍 " + noteIndirizzo);
            lblNote.setWrapText(true);
            lblNote.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #d5bdaf; -fx-background-color: rgba(35,32,38,0.85); -fx-padding: 6 10; -fx-background-radius: 6;");
            boxConsegna.getChildren().add(lblNote);
        }
        VBox boxArticoli = new VBox(6);
        boxArticoli.setStyle("-fx-background-color: rgba(15, 14, 18, 0.85); -fx-border-color: rgba(186,164,135,0.18); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 12;");

        String descCompleta = item.getDescrizioneCompleta();
        if (descCompleta != null && !descCompleta.trim().isEmpty()) {
            Map<String, Integer> articoliRaggruppati = StoricoOrdiniClienteView.raggruppaArticoli(descCompleta);
            int totalePezzi = articoliRaggruppati.values().stream().mapToInt(Integer::intValue).sum();

            Label lblTitoloSezione = new Label("📋 Articoli Ordinati (" + totalePezzi + "):");
            lblTitoloSezione.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
            boxArticoli.getChildren().add(lblTitoloSezione);

            for (Map.Entry<String, Integer> entry : articoliRaggruppati.entrySet()) {
                String cleaned = entry.getKey();
                int qty = entry.getValue();

                String nomeProdotto = cleaned;
                String dettagli = "";

                int idxPar = cleaned.indexOf('(');
                int idxParEnd = cleaned.lastIndexOf(')');
                if (idxPar != -1 && idxParEnd > idxPar) {
                    nomeProdotto = cleaned.substring(0, idxPar).trim();
                    dettagli = cleaned.substring(idxPar + 1, idxParEnd).trim();
                    String extra = cleaned.substring(idxParEnd + 1).trim();
                    if (!extra.isEmpty()) {
                        if (!dettagli.isEmpty()) dettagli += " • ";
                        dettagli += extra;
                    }
                }

                String lower = cleaned.toLowerCase();
                String icon = "🍨";
                String cat = "GELATO";
                String styleBadge = "-fx-background-color: rgba(45, 38, 25, 0.90); -fx-text-fill: #fcd34d; -fx-border-color: rgba(186,164,135,0.35);";

                if (lower.contains("crepe") || lower.contains("pancake")) {
                    icon = "🥞";
                    cat = "CREPES";
                    styleBadge = "-fx-background-color: rgba(50, 36, 20, 0.90); -fx-text-fill: #fcd34d; -fx-border-color: rgba(186,164,135,0.35);";
                } else if (lower.contains("crostata")) {
                    icon = "🥧";
                    cat = "CROSTATE";
                    styleBadge = "-fx-background-color: rgba(55, 30, 20, 0.90); -fx-text-fill: #fdba74; -fx-border-color: rgba(253,186,116,0.35);";
                } else if (lower.contains("biscott") || lower.contains("cantucc") || lower.contains("baci")
                        || lower.contains("frollin") || lower.contains("amarett") || lower.contains("canestrell")
                        || lower.contains("occhi di bue") || lower.contains("zalett") || lower.contains("ricciarell")
                        || lower.contains("diamant") || lower.contains("krumir") || lower.contains("pasticcin")) {
                    icon = "🍪";
                    cat = "BISCOTTI";
                    styleBadge = "-fx-background-color: rgba(45, 35, 20, 0.90); -fx-text-fill: #fde68a; -fx-border-color: rgba(253,230,138,0.35);";
                } else if (lower.contains("caffe") || lower.contains("caffè") || lower.contains("cappuccino")
                        || lower.contains("marocchino") || lower.contains("shakerato") || lower.contains("ginseng")
                        || lower.contains("orzo") || lower.contains("espresso") || lower.contains("affogato")) {
                    icon = "☕";
                    cat = "CAFFETTERIA";
                    styleBadge = "-fx-background-color: rgba(40, 30, 25, 0.90); -fx-text-fill: #d5bdaf; -fx-border-color: rgba(213,189,175,0.35);";
                } else if (lower.contains("cioccolata")) {
                    icon = "☕";
                    cat = "CIOCCOLATA";
                    styleBadge = "-fx-background-color: rgba(42, 26, 20, 0.90); -fx-text-fill: #ddb892; -fx-border-color: rgba(221,184,146,0.35);";
                } else if (lower.contains("tisana") || lower.contains("tè ") || lower.contains("te ") || lower.contains("infuso") || lower.contains("infusi")) {
                    icon = "🍵";
                    cat = "TÈ & TISANE";
                    styleBadge = "-fx-background-color: rgba(20, 48, 36, 0.90); -fx-text-fill: #a7f3d0; -fx-border-color: rgba(167,243,208,0.35);";
                } else if (lower.contains("acqua") || lower.contains("coca") || lower.contains("fanta")
                        || lower.contains("bibita") || lower.contains("succo") || lower.contains("spremuta")
                        || lower.contains("centrifugato")) {
                    icon = "🥤";
                    cat = "BEVANDE";
                    styleBadge = "-fx-background-color: rgba(20, 45, 30, 0.90); -fx-text-fill: #bbf7d0; -fx-border-color: rgba(187,247,208,0.35);";
                } else if (lower.contains("panna") && !lower.contains("vaschetta")) {
                    icon = "☁️";
                    cat = "PANNA";
                    styleBadge = "-fx-background-color: rgba(35, 34, 40, 0.90); -fx-text-fill: #e2e8f0; -fx-border-color: rgba(203,213,225,0.35);";
                }

                // Per il gelato, assicura un'etichetta chiara (es. Vaschetta Gelato Artigianale 1000g)
                if (cat.equals("GELATO")) {
                    if (nomeProdotto.equalsIgnoreCase("vaschetta 1000g")) {
                        nomeProdotto = "Vaschetta Gelato Artigianale • 1000g (1 Kg)";
                    } else if (nomeProdotto.equalsIgnoreCase("vaschetta 500g")) {
                        nomeProdotto = "Vaschetta Gelato Artigianale • 500g (0.5 Kg)";
                    } else if (nomeProdotto.equalsIgnoreCase("vaschetta 750g")) {
                        nomeProdotto = "Vaschetta Gelato Artigianale • 750g (0.75 Kg)";
                    } else if (nomeProdotto.equalsIgnoreCase("vaschetta 1500g")) {
                        nomeProdotto = "Vaschetta Gelato Artigianale • 1500g (1.5 Kg)";
                    } else if (!nomeProdotto.toLowerCase().startsWith("vaschetta")) {
                        nomeProdotto = "Vaschetta Gelato: " + nomeProdotto;
                    }
                }

                HBox rowItem = new HBox(9);
                rowItem.setAlignment(Pos.CENTER_LEFT);
                rowItem.setStyle("-fx-padding: 8 10; -fx-background-color: rgba(26, 24, 28, 0.90); -fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 8; -fx-background-radius: 8;");

                Label lblIdx = new Label(qty + "×");
                lblIdx.setMinWidth(Region.USE_PREF_SIZE);
                lblIdx.setStyle("-fx-background-color: rgba(212, 180, 131, 0.20); -fx-text-fill: #e5c99b; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 2 8; -fx-background-radius: 6;");

                Label lblCatBadge = new Label(icon + " " + cat);
                lblCatBadge.setMinWidth(Region.USE_PREF_SIZE);
                lblCatBadge.setStyle(styleBadge + " -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 6; -fx-border-radius: 6;");

                VBox vbInfo = new VBox(2);
                Label lblNome = new Label(nomeProdotto);
                lblNome.setWrapText(true);
                lblNome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
                vbInfo.getChildren().add(lblNome);

                if (!dettagli.isEmpty()) {
                    String prefissoDettaglio = cat.equals("GELATO") ? "↳ Gusti scelti: " : "↳ Dettagli: ";
                    Label lblDett = new Label(prefissoDettaglio + dettagli);
                    lblDett.setWrapText(true);
                    lblDett.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #d5bdaf; -fx-font-style: italic;");
                    vbInfo.getChildren().add(lblDett);
                }
                HBox.setHgrow(vbInfo, Priority.ALWAYS);

                rowItem.getChildren().addAll(lblIdx, lblCatBadge, vbInfo);
                boxArticoli.getChildren().add(rowItem);
            }
        }

        HBox bottom = new HBox(12);
        bottom.setAlignment(Pos.CENTER_LEFT);

        Label lblPrezzo = new Label("Totale Ordine: " + String.format(Locale.US, "EUR %.2f", item.getPrezzoTotale()));
        lblPrezzo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #baa487;");

        Region spBottom = new Region();
        HBox.setHgrow(spBottom, Priority.ALWAYS);

        bottom.getChildren().addAll(lblPrezzo, spBottom);

        if (isNuovo) {
            Button btnDis = new Button("✕ Disdici");
            btnDis.setStyle("-fx-background-color: rgba(70, 20, 25, 0.85); -fx-text-fill: #fca5a5; -fx-font-size: 13px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand; -fx-border-color: #ef4444; -fx-border-radius: 8;");
            btnDis.setOnAction(e -> onDisdiciOrdine.accept(item.getCodiceOrdine()));

            Button btnConf = new Button("✔ Conferma Ordine");
            btnConf.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-size: 13.5px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 8; -fx-padding: 6 18; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(186,164,135,0.40), 8, 0, 0, 2);");
            btnConf.setOnAction(e -> onConfermaOrdine.accept(item.getCodiceOrdine()));

            bottom.getChildren().addAll(btnDis, btnConf);
        } else if (isConfermato) {
            Label lblOk = new Label("✔ Ordine approvato e notificato al cliente");
            lblOk.setStyle("-fx-font-size: 13px; -fx-text-fill: #a7f3d0; -fx-font-weight: bold; -fx-background-color: rgba(22,55,38,0.85); -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #10b981; -fx-border-radius: 6;");
            bottom.getChildren().add(lblOk);
        } else {
            Label lblNo = new Label("✕ Ordine non evaso o disdetto");
            lblNo.setStyle("-fx-font-size: 13px; -fx-text-fill: #fca5a5; -fx-font-weight: bold; -fx-background-color: rgba(65,20,25,0.85); -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #ef4444; -fx-border-radius: 6;");
            bottom.getChildren().add(lblNo);
        }

        card.getChildren().addAll(top, boxConsegna, boxArticoli, bottom);
        return card;
    }

    public static void mostraPopupContattoCliente(javafx.stage.Window owner, OrdineBean item) {
        if (item == null) return;

        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Scheda Cliente & Contatto Emergenza");
        if (owner != null) {
            popup.initOwner(owner);
        }

        String nome = (item.getNomeCliente() != null && !item.getNomeCliente().isBlank())
                ? item.getNomeCliente() : "Cliente";
        String email = item.getEmailCliente() != null ? item.getEmailCliente().trim() : "";
        String raw = item.getDataRitiro() != null ? item.getDataRitiro() : "";
        String telefono = estraiCampoDaConsegna(raw, "Tel:");
        String personaRitiro = estraiCampoDaConsegna(raw, "Ritiro:");
        String indirizzo = estraiCampoDaConsegna(raw, "Indirizzo:");

        Label lblTitolo = new Label("👤 Scheda Cliente — Ordine " + item.getCodiceOrdine());
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        VBox boxDati = new VBox(7);
        boxDati.setStyle("-fx-background-color: rgba(28, 26, 32, 0.94); -fx-border-color: rgba(186,164,135,0.28); "
                + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14 16;");

        boxDati.getChildren().add(creaRigaInfoCliente("Nome e Cognome:", nome));
        if (!personaRitiro.isEmpty() && !personaRitiro.equalsIgnoreCase(nome)) {
            boxDati.getChildren().add(creaRigaInfoCliente("Persona che ritira:", personaRitiro));
        }
        boxDati.getChildren().add(creaRigaInfoCliente("Email:", email.isEmpty() ? "Non disponibile" : email));
        boxDati.getChildren().add(creaRigaInfoCliente("Telefono:", telefono.isEmpty() ? "Non indicato" : telefono));
        if (!indirizzo.isEmpty()) {
            boxDati.getChildren().add(creaRigaInfoCliente("Indirizzo:", indirizzo));
        }

        Label lblEmergenza = new Label("⚠️ Contatta il Cliente per Emergenza sull'Ordine");
        lblEmergenza.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #d4b483;");

        javafx.scene.control.TextArea txtMessaggio = new javafx.scene.control.TextArea();
        txtMessaggio.setWrapText(true);
        txtMessaggio.setPrefRowCount(3);
        txtMessaggio.setPromptText("Scrivi un avviso urgente per il cliente (es. ritardo preparazione, gusto esaurito, problema consegna)...");
        txtMessaggio.setStyle("-fx-control-inner-background: #1a1820; -fx-text-fill: #f5ebe0; -fx-prompt-text-fill: #8c837b; "
                + "-fx-border-color: rgba(186,164,135,0.32); -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox boxPreset = new HBox(8);
        Button p1 = creaChipMessaggio("⏱️ Ritardo 10-15 min",
                "Gentile cliente, il tuo ordine " + item.getCodiceOrdine() + " subirà un lieve ritardo di circa 10-15 minuti. Ci scusiamo per l'attesa!", txtMessaggio);
        Button p2 = creaChipMessaggio("🍦 Gusto esaurito",
                "Gentile cliente, uno dei gusti richiesti nell'ordine " + item.getCodiceOrdine() + " è appena terminato. Ti preghiamo di contattarci per scegliere una sostituzione.", txtMessaggio);
        Button p3 = creaChipMessaggio("📍 Problema consegna",
                "Gentile cliente, il fattorino ha difficoltà a raggiungere l'indirizzo per l'ordine " + item.getCodiceOrdine() + ". Ti preghiamo di rispondere al telefono.", txtMessaggio);
        boxPreset.getChildren().addAll(p1, p2, p3);

        Label lblStatoInvio = new Label();
        lblStatoInvio.setWrapText(true);
        lblStatoInvio.setVisible(false);
        lblStatoInvio.setManaged(false);

        HBox boxAzioniRapide = new HBox(10);
        boxAzioniRapide.setAlignment(Pos.CENTER_LEFT);

        if (!telefono.isEmpty()) {
            Button btnCopiaTel = new Button("📋 Copia Telefono");
            btnCopiaTel.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #f5ebe0; -fx-font-weight: bold; "
                    + "-fx-font-size: 12.5px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
            btnCopiaTel.setOnAction(e -> {
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(telefono);
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
                mostraFeedbackContatto(lblStatoInvio, "📋 Numero " + telefono + " copiato negli appunti!", false);
            });
            boxAzioniRapide.getChildren().add(btnCopiaTel);
        }

        if (!email.isEmpty()) {
            Button btnMail = new Button("✉️ Apri Email");
            btnMail.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #f5ebe0; -fx-font-weight: bold; "
                    + "-fx-font-size: 12.5px; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
            btnMail.setOnAction(e -> {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        String subj = java.net.URLEncoder.encode("Frostly - Comunicazione Ordine " + item.getCodiceOrdine(), java.nio.charset.StandardCharsets.UTF_8);
                        java.awt.Desktop.getDesktop().mail(java.net.URI.create("mailto:" + email + "?subject=" + subj));
                    }
                } catch (Exception ex) {
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(email);
                    javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
                    mostraFeedbackContatto(lblStatoInvio, "✉️ Email " + email + " copiata negli appunti.", false);
                }
            });
            boxAzioniRapide.getChildren().add(btnMail);
        }

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        int numMsg = new control.applicativo.OrdiniControl()
                .caricaMessaggiChatOrdine(item.getCodiceOrdine(), email).size();
        Button btnApriChat = new Button(numMsg > 0 ? ("💬 Apri Chat (" + numMsg + ")") : "💬 Apri Chat");
        btnApriChat.setStyle("-fx-background-color: rgba(212, 180, 131, 0.16); -fx-text-fill: #e5c99b; -fx-border-color: rgba(212,180,131,0.40); "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 6 12; -fx-cursor: hand;");
        btnApriChat.setOnAction(e -> ChatOrdineView.mostraChatOrdine(popup, item, true));

        Button btnInvia = new Button("📩 Invia Avviso al Cliente");
        btnInvia.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");
        btnInvia.setOnAction(e -> {
            try {
                String operatore = util.SessionManager.getInstance().getNomeUtenteLoggato();
                new control.applicativo.OrdiniControl().contattaClientePerEmergenzaOrdine(
                        item.getCodiceOrdine(), email, txtMessaggio.getText(), operatore);
                txtMessaggio.clear();
                mostraFeedbackContatto(lblStatoInvio, "✅ Avviso urgente inviato nella chat dell'ordine!", false);
            } catch (frostly.exception.FrostlyException ex) {
                mostraFeedbackContatto(lblStatoInvio, "⚠️ " + ex.getMessage(), true);
            }
        });
        boxAzioniRapide.getChildren().addAll(btnApriChat, sp, btnInvia);

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-font-size: 13px; "
                + "-fx-border-color: rgba(255,255,255,0.22); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 6 16; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());
        HBox footer = new HBox(btnChiudi);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, lblTitolo, boxDati, lblEmergenza, boxPreset, txtMessaggio, lblStatoInvio, boxAzioniRapide, footer);
        root.setStyle("-fx-padding: 20 24; -fx-background-color: rgba(16, 15, 18, 0.98); "
                + "-fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2;");

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 620, 490);
        scene.setFill(javafx.scene.paint.Color.web("#121114"));
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(popup.getScene());
        popup.centerOnScreen();
        popup.showAndWait();
    }

    private static HBox creaRigaInfoCliente(String etichetta, String valore) {
        Label lblK = new Label(etichetta);
        lblK.setMinWidth(145);
        lblK.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #a89f92;");
        Label lblV = new Label(valore);
        lblV.setWrapText(true);
        lblV.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
        HBox.setHgrow(lblV, Priority.ALWAYS);
        HBox riga = new HBox(10, lblK, lblV);
        riga.setAlignment(Pos.CENTER_LEFT);
        return riga;
    }

    private static Button creaChipMessaggio(String testoChip, String testoMessaggio, javafx.scene.control.TextArea target) {
        Button b = new Button(testoChip);
        b.setStyle("-fx-background-color: rgba(38, 35, 42, 0.90); -fx-text-fill: #ded7cd; -fx-font-size: 11.5px; "
                + "-fx-border-color: rgba(186,164,135,0.28); -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 10; -fx-cursor: hand;");
        b.setOnAction(e -> target.setText(testoMessaggio));
        return b;
    }

    private static void mostraFeedbackContatto(Label lbl, String msg, boolean errore) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
        lbl.setStyle(errore
                ? "-fx-text-fill: #fca5a5; -fx-font-weight: bold; -fx-font-size: 12.5px;"
                : "-fx-text-fill: #a7f3d0; -fx-font-weight: bold; -fx-font-size: 12.5px;");
    }

    private static String estraiCampoDaConsegna(String raw, String prefisso) {
        if (raw == null || raw.isBlank()) return "";
        int idx = raw.indexOf(prefisso);
        if (idx < 0) return "";
        String resto = raw.substring(idx + prefisso.length()).trim();
        int fineBullet = resto.indexOf('•');
        int fineBracket = resto.indexOf('[');
        int fine = resto.length();
        if (fineBullet >= 0) fine = Math.min(fine, fineBullet);
        if (fineBracket >= 0) fine = Math.min(fine, fineBracket);
        return resto.substring(0, fine).trim();
    }
}
