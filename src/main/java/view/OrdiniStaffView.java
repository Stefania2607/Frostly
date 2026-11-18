package view;

import bean.OrdineBean;
import java.util.Locale;
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
 * Stile Quiet Luxury: grafite satinato, finiture oro champagne, avorio, zero scrollbar orizzontali.
 */
public final class OrdiniStaffView {

    private OrdiniStaffView() {
    }

    public static Label creaBadgeStatoOrdine(String stato) {
        String badgeText;
        String badgeStyle;
        if ("IN_ATTESA".equalsIgnoreCase(stato)) {
            badgeText = "🔔 NUOVO DA CONFERMARE";
            badgeStyle = "-fx-background-color: rgba(65, 48, 20, 0.90); -fx-text-fill: #fcd34d; -fx-border-color: #baa487;";
        } else if ("APPROVATA".equalsIgnoreCase(stato)) {
            badgeText = "🟢 Confermato • In Preparazione";
            badgeStyle = "-fx-background-color: rgba(22, 55, 38, 0.90); -fx-text-fill: #a7f3d0; -fx-border-color: #10b981;";
        } else if ("RIFIUTATA".equalsIgnoreCase(stato)) {
            badgeText = "🔴 Disdetto / Rifiutato";
            badgeStyle = "-fx-background-color: rgba(70, 20, 25, 0.90); -fx-text-fill: #fca5a5; -fx-border-color: #ef4444;";
        } else {
            badgeText = "🔵 " + stato;
            badgeStyle = "-fx-background-color: rgba(25, 45, 65, 0.90); -fx-text-fill: #93c5fd; -fx-border-color: #3b82f6;";
        }
        Label lbl = new Label(badgeText);
        lbl.setStyle(badgeStyle + " -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 9; -fx-background-radius: 8; -fx-border-radius: 8;");
        return lbl;
    }

    public static void configuraCelleOrdiniStaff(ListView<OrdineBean> listaOrdiniStaff,
                                                 Consumer<String> onConfermaOrdine,
                                                 Consumer<String> onDisdiciOrdine) {
        if (listaOrdiniStaff == null) return;
        listaOrdiniStaff.setCellFactory(lv -> new ListCell<OrdineBean>() {
            @Override
            protected void updateItem(OrdineBean item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    VBox card = buildCardOrdineStaff(item, onConfermaOrdine, onDisdiciOrdine);
                    card.maxWidthProperty().bind(listaOrdiniStaff.widthProperty().subtract(28));
                    setGraphic(card);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
                } else {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
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

        // 1. RIGA TESTATA: ID Ordine, Cliente e Badge Stato
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label lblId = new Label(item.getCodiceOrdine());
        lblId.setMinWidth(Region.USE_PREF_SIZE);
        if (isNuovo) {
            lblId.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                    + "-fx-font-size: 13px; -fx-padding: 4 12; -fx-background-radius: 8;");
        } else if (isConfermato) {
            lblId.setStyle("-fx-background-color: rgba(30,55,42,0.9); -fx-text-fill: #a7f3d0; -fx-font-weight: bold; "
                    + "-fx-font-size: 12.5px; -fx-padding: 4 11; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-radius: 8;");
        } else {
            lblId.setStyle("-fx-background-color: rgba(38,36,40,0.85); -fx-text-fill: #baa487; -fx-font-weight: bold; "
                    + "-fx-font-size: 12.5px; -fx-padding: 4 11; -fx-background-radius: 8;");
        }

        Label lblCliente = new Label("👤  " + (item.getEmailCliente() != null ? item.getEmailCliente() : "Cliente"));
        lblCliente.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Region spTop = new Region();
        HBox.setHgrow(spTop, Priority.ALWAYS);

        Label lblBadge = creaBadgeStatoOrdine(item.getStato());

        top.getChildren().addAll(lblId, lblCliente, spTop, lblBadge);

        // 2. RIGA CONSEGNA / RITIRO & INDIRIZZO
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

        HBox boxConsegna = new HBox(12);
        boxConsegna.setAlignment(Pos.CENTER_LEFT);
        boxConsegna.setStyle("-fx-background-color: rgba(18, 16, 20, 0.75); -fx-border-color: rgba(186,164,135,0.20); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 12;");

        Label lblMod = new Label("🛵  " + infoConsegna);
        lblMod.setWrapText(true);
        lblMod.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
        HBox.setHgrow(lblMod, Priority.ALWAYS);
        boxConsegna.getChildren().add(lblMod);

        if (!noteIndirizzo.isEmpty()) {
            Label lblNote = new Label("📍 " + noteIndirizzo);
            lblNote.setWrapText(true);
            lblNote.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #d5bdaf; -fx-background-color: rgba(35,32,38,0.85); -fx-padding: 3 8; -fx-background-radius: 6;");
            boxConsegna.getChildren().add(lblNote);
        }

        // 3. SEZIONE ARTICOLI ORDINATI (ORGANIZZATI E CLASSIFICATI PER TIPOLOGIA)
        VBox boxArticoli = new VBox(6);
        boxArticoli.setStyle("-fx-background-color: rgba(15, 14, 18, 0.85); -fx-border-color: rgba(186,164,135,0.18); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 12;");

        String descCompleta = item.getDescrizioneCompleta();
        if (descCompleta != null && !descCompleta.trim().isEmpty()) {
            String[] splitArticoli = descCompleta.split(" \\| ");

            Label lblTitoloSezione = new Label("📋 Articoli Ordinati (" + splitArticoli.length + "):");
            lblTitoloSezione.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #baa487;");
            boxArticoli.getChildren().add(lblTitoloSezione);

            for (int i = 0; i < splitArticoli.length; i++) {
                String pezzo = splitArticoli[i].trim();
                if (pezzo.isEmpty()) continue;

                // Pulisce qualsiasi prefisso "Vaschetta #1:" erroneamente associato a crepes/caffetteria
                String cleaned = pezzo.replaceFirst("^[Vv]aschetta #\\d+:\\s*", "").trim();

                // Analizza nome prodotto e dettagli (toppings, gusti, opzioni)
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

                // Identifica categoria specifica, icona e badge cromatico
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
                } else if (lower.contains("biscott") || lower.contains("cantucc") || lower.contains("baci") || lower.contains("frollin")) {
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
                } else if (lower.contains("acqua") || lower.contains("coca") || lower.contains("fanta") || lower.contains("bibita") || lower.contains("succo")) {
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

                // Costruisce la riga ordinata del singolo prodotto
                HBox rowItem = new HBox(9);
                rowItem.setAlignment(Pos.CENTER_LEFT);
                rowItem.setStyle("-fx-padding: 8 10; -fx-background-color: rgba(26, 24, 28, 0.90); -fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 8; -fx-background-radius: 8;");

                Label lblIdx = new Label("#" + (i + 1));
                lblIdx.setStyle("-fx-background-color: rgba(40, 36, 42, 0.85); -fx-text-fill: #baa487; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 2 7; -fx-background-radius: 6;");

                Label lblCatBadge = new Label(icon + " " + cat);
                lblCatBadge.setStyle(styleBadge + " -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 6; -fx-border-radius: 6;");

                VBox vbInfo = new VBox(2);
                Label lblNome = new Label(nomeProdotto);
                lblNome.setWrapText(true);
                lblNome.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");
                vbInfo.getChildren().add(lblNome);

                if (!dettagli.isEmpty()) {
                    String prefissoDettaglio = cat.equals("GELATO") ? "↳ Gusti scelti: " : "↳ Dettagli: ";
                    Label lblDett = new Label(prefissoDettaglio + dettagli);
                    lblDett.setWrapText(true);
                    lblDett.setStyle("-fx-font-size: 11px; -fx-text-fill: #d5bdaf; -fx-font-style: italic;");
                    vbInfo.getChildren().add(lblDett);
                }
                HBox.setHgrow(vbInfo, Priority.ALWAYS);

                rowItem.getChildren().addAll(lblIdx, lblCatBadge, vbInfo);
                boxArticoli.getChildren().add(rowItem);
            }
        }

        // 4. RIGA INFERIORE: Totale in evidenza e Azioni Staff
        HBox bottom = new HBox(12);
        bottom.setAlignment(Pos.CENTER_LEFT);

        Label lblPrezzo = new Label("Totale Ordine: " + String.format(Locale.US, "EUR %.2f", item.getPrezzoTotale()));
        lblPrezzo.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #baa487;");

        Region spBottom = new Region();
        HBox.setHgrow(spBottom, Priority.ALWAYS);

        bottom.getChildren().addAll(lblPrezzo, spBottom);

        if (isNuovo) {
            Button btnDis = new Button("✕ Disdici");
            btnDis.setStyle("-fx-background-color: rgba(70, 20, 25, 0.85); -fx-text-fill: #fca5a5; -fx-font-size: 11.5px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand; -fx-border-color: #ef4444; -fx-border-radius: 8;");
            btnDis.setOnAction(e -> onDisdiciOrdine.accept(item.getCodiceOrdine()));

            Button btnConf = new Button("✔ Conferma Ordine");
            btnConf.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-size: 12px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 8; -fx-padding: 6 18; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(186,164,135,0.40), 8, 0, 0, 2);");
            btnConf.setOnAction(e -> onConfermaOrdine.accept(item.getCodiceOrdine()));

            bottom.getChildren().addAll(btnDis, btnConf);
        } else if (isConfermato) {
            Label lblOk = new Label("✔ Ordine approvato e notificato al cliente");
            lblOk.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #a7f3d0; -fx-font-weight: bold; -fx-background-color: rgba(22,55,38,0.85); -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #10b981; -fx-border-radius: 6;");
            bottom.getChildren().add(lblOk);
        } else {
            Label lblNo = new Label("✕ Ordine non evaso o disdetto");
            lblNo.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #fca5a5; -fx-font-weight: bold; -fx-background-color: rgba(65,20,25,0.85); -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #ef4444; -fx-border-radius: 6;");
            bottom.getChildren().add(lblNo);
        }

        card.getChildren().addAll(top, boxConsegna, boxArticoli, bottom);
        return card;
    }
}
