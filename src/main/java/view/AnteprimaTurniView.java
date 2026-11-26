package view;

import bean.PropostaTurniSettimanaBean;
import bean.RiepilogoOreDipendenteBean;
import bean.TurnoBean;
import control.applicativo.CalcolatoreOreSettimanali;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import bean.DipendenteBean;
import model.ConfigurazioneOreDipendente;

/**
 * Finestra di anteprima obbligatoria della pianificazione generata automaticamente.
 * Consente all'amministratore di ispezionare il calendario proposto, ore e scostamenti live,
 * turni scoperti, modificare manualmente la proposta prima del salvataggio, rigenerare o annullare.
 */
public final class AnteprimaTurniView {

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private AnteprimaTurniView() {
    }

    public static void apriAnteprima(Window owner,
                                     PropostaTurniSettimanaBean propostaIniziale,
                                     List<DipendenteBean> dipendenti,
                                     Supplier<PropostaTurniSettimanaBean> onRigenera,
                                     Function<List<TurnoBean>, PropostaTurniSettimanaBean> onRivalutaModifica,
                                     Consumer<PropostaTurniSettimanaBean> onConfermaSalva) {
        if (propostaIniziale == null) {
            return;
        }
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Anteprima Pianificazione Settimanale");
        if (owner != null) {
            popup.initOwner(owner);
        }

        List<PropostaTurniSettimanaBean> storicoProposte = new ArrayList<>();
        storicoProposte.add(propostaIniziale);
        final int[] indiceCorrente = {0};

        VBox root = new VBox(10);
        root.setPrefWidth(1200);
        root.setPrefHeight(720);
        root.setStyle("-fx-padding: 16 20; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        boolean provenza = util.SessionManager.getInstance().isGui2Attiva();

        Label lblTitolo = new Label("✨ Anteprima Proposta Turni — "
                + propostaIniziale.getLunediSettimana().format(FMT_DATA) + " – "
                + propostaIniziale.getLunediSettimana().plusDays(6).format(FMT_DATA));
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: "
                + (provenza ? "#37253d;" : "#f5ebe0;"));

        Label lblSub = new Label("Nessun turno ufficiale è stato ancora modificato. Clicca su una cella per modificare manualmente la proposta prima di confermare.");
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-font-style: italic; -fx-text-fill: " + (provenza ? "#764c86;" : "#a89f92;"));

        HBox barraRiepilogo = new HBox(10);
        barraRiepilogo.setAlignment(Pos.CENTER_LEFT);

        VBox boxOreDipendenti = new VBox(6);
        ScrollPane scrollOre = new ScrollPane(boxOreDipendenti);
        scrollOre.setFitToWidth(true);
        scrollOre.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollOre.setPrefWidth(200);
        scrollOre.setMinWidth(190);
        scrollOre.setMaxWidth(210);
        scrollOre.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox boxCalendarioProposto = new VBox(4);
        ScrollPane scrollCal = new ScrollPane(boxCalendarioProposto);
        scrollCal.setFitToWidth(true);
        scrollCal.setFitToHeight(true);
        scrollCal.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCal.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        HBox.setHgrow(scrollCal, Priority.ALWAYS);

        HBox corpo = new HBox(10, scrollOre, scrollCal);
        VBox.setVgrow(corpo, Priority.ALWAYS);

        VBox boxAvvisi = new VBox(4);
        ScrollPane scrollAvvisi = new ScrollPane(boxAvvisi);
        scrollAvvisi.setFitToWidth(true);
        scrollAvvisi.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollAvvisi.setPrefHeight(95);
        scrollAvvisi.setMaxHeight(105);
        scrollAvvisi.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button btnPrecProposta = new Button("◀ Proposta prec.");
        btnPrecProposta.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (provenza ? "#5e316f" : "#ded7cd")
                + "; -fx-border-color: " + (provenza ? "#d8bfd8" : "rgba(186,164,135,0.4)")
                + "; -fx-font-weight: bold; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 7 12; -fx-cursor: hand;");

        Button btnSuccProposta = new Button("Proposta succ. ▶");
        btnSuccProposta.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (provenza ? "#5e316f" : "#ded7cd")
                + "; -fx-border-color: " + (provenza ? "#d8bfd8" : "rgba(186,164,135,0.4)")
                + "; -fx-font-weight: bold; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 7 12; -fx-cursor: hand;");

        final Runnable[] aggiornaVista = new Runnable[1];
        aggiornaVista[0] = () -> {
            PropostaTurniSettimanaBean p = storicoProposte.get(indiceCorrente[0]);
            btnPrecProposta.setDisable(indiceCorrente[0] <= 0);
            btnSuccProposta.setDisable(indiceCorrente[0] >= storicoProposte.size() - 1);

            // Pillole KPI riepilogo proposta + diff + chiusura
            barraRiepilogo.getChildren().clear();
            String coloreCop = p.getPercentualeCopertura() >= 100.0 ? "#16a34a" : (p.getPercentualeCopertura() >= 80.0 ? "#d97706" : "#dc2626");
            barraRiepilogo.getChildren().addAll(
                    creaPillola("Proposta #" + (indiceCorrente[0] + 1) + " / " + storicoProposte.size(), "#5e316f", provenza),
                    creaPillola("Copertura: " + p.getPercentualeCopertura() + "% (" + p.getPostiCopertiTotali() + "/" + p.getPostiRichiestiTotali() + ")", coloreCop, provenza),
                    creaPillola("Aggiunti: +" + p.getTurniAggiunti(), "#764c86", provenza),
                    creaPillola("Mantenuti: " + p.getTurniMantenuti(), "#387548", provenza),
                    creaPillola("Rimossi: -" + p.getTurniRimossi(), "#ad443f", provenza),
                    creaPillola("Scoperti: " + p.getTurniScoperti().size(), p.getTurniScoperti().isEmpty() ? "#16a34a" : "#dc2626", provenza));
            if (p.getRiepilogoChiusura() != null && !p.getRiepilogoChiusura().isBlank()) {
                barraRiepilogo.getChildren().add(
                        creaPillola(p.getRiepilogoChiusura(), p.isCoperturaChiusuraOk() ? "#16a34a" : "#dc2626", provenza));
            }

            // Ore live per dipendente
            DisponibilitaStaffView.popolaPannelloDisponibilita(boxOreDipendenti, p.getRiepiloghiDipendenti(), null);

            // Calendario proposto con modifica manuale diretta in anteprima
            TurniStaffView.popolaCalendarioSettimanale(
                    boxCalendarioProposto,
                    p.getLunediSettimana(),
                    p.getTurniProposti(),
                    true,
                    null,
                    (indiceGiorno, indiceFascia, chiaveData, titoloEsteso) -> apriModificaCellaAnteprima(
                            popup, p, indiceGiorno, indiceFascia, chiaveData, titoloEsteso,
                            dipendenti, onRivalutaModifica, nuovaProposta -> {
                                storicoProposte.set(indiceCorrente[0], nuovaProposta);
                                aggiornaVista[0].run();
                            }),
                    null);

            // Avvisi, turni scoperti e suggerimenti
            boxAvvisi.getChildren().clear();
            if (p.getTurniScoperti().isEmpty() && p.getAvvisi().isEmpty()) {
                Label ok = new Label("✅ Copertura completa e tutti i vincoli rispettati. Puoi confermare e salvare la pianificazione.");
                ok.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#1e6b3a;" : "#a7f3d0;"));
                boxAvvisi.getChildren().add(ok);
            } else {
                for (String sc : p.getTurniScoperti()) {
                    Label l = new Label("⚠️ Scoperto: " + sc);
                    l.setWrapText(true);
                    l.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? "#b91c1c;" : "#fca5a5;"));
                    boxAvvisi.getChildren().add(l);
                }
                for (String av : p.getAvvisi()) {
                    Label l = new Label("• " + av);
                    l.setWrapText(true);
                    l.setStyle("-fx-font-size: 11.5px; -fx-text-fill: " + (provenza ? "#9a5b13;" : "#fcd34d;"));
                    boxAvvisi.getChildren().add(l);
                }
                for (String sug : p.getSuggerimenti()) {
                    Label l = new Label("💡 Suggerimento: " + sug);
                    l.setWrapText(true);
                    l.setStyle("-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: " + (provenza ? "#764c86;" : "#d8b4fe;"));
                    boxAvvisi.getChildren().add(l);
                }
            }
        };

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (provenza ? "#5e316f" : "#ded7cd")
                + "; -fx-border-color: " + (provenza ? "#d8bfd8" : "rgba(186,164,135,0.4)")
                + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 7 16; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> popup.close());

        Button btnRigenera = new Button("🔄 Rigenera");
        btnRigenera.setStyle("-fx-background-color: " + (provenza ? "#f3eae5" : "#2b2730")
                + "; -fx-text-fill: " + (provenza ? "#37253d" : "#f5ebe0")
                + "; -fx-border-color: " + (provenza ? "#d8bfd8" : "rgba(186,164,135,0.4)")
                + "; -fx-font-weight: bold; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 7 16; -fx-cursor: hand;");
        btnPrecProposta.setOnAction(e -> {
            if (indiceCorrente[0] > 0) {
                indiceCorrente[0]--;
                aggiornaVista[0].run();
            }
        });
        btnSuccProposta.setOnAction(e -> {
            if (indiceCorrente[0] < storicoProposte.size() - 1) {
                indiceCorrente[0]++;
                aggiornaVista[0].run();
            }
        });

        btnRigenera.setOnAction(e -> {
            if (onRigenera != null) {
                PropostaTurniSettimanaBean nuova = onRigenera.get();
                if (nuova != null) {
                    storicoProposte.add(nuova);
                    indiceCorrente[0] = storicoProposte.size() - 1;
                    aggiornaVista[0].run();
                }
            }
        });

        Button btnConferma = new Button("✔ Approva e salva pianificazione");
        btnConferma.setStyle("-fx-background-color: " + (provenza ? "linear-gradient(to right, #9366a0, #6d427b)" : "#baa487")
                + "; -fx-text-fill: " + (provenza ? "#ffffff" : "#141312")
                + "; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 8 22; -fx-cursor: hand;");
        btnConferma.setOnAction(e -> {
            PropostaTurniSettimanaBean scelta = storicoProposte.get(indiceCorrente[0]);
            if (scelta.isHaViolazioniInderogabili()) {
                javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                err.initOwner(popup);
                err.setTitle("Violazioni Inderogabili");
                err.setHeaderText("Impossibile approvare questa proposta");
                err.setContentText("La proposta contiene violazioni di vincoli inderogabili. Correggile manualmente o rigenera.");
                err.showAndWait();
                return;
            }
            if (!scelta.getTurniScoperti().isEmpty()) {
                javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                warn.initOwner(popup);
                warn.setTitle("Conferma con Scoperture");
                warn.setHeaderText("Attenzione: restano " + scelta.getTurniScoperti().size() + " fasce con scoperture");
                warn.setContentText("Vuoi approvare e salvare comunque questa pianificazione settimanale?");
                javafx.scene.control.ButtonType si = new javafx.scene.control.ButtonType("Approva comunque");
                javafx.scene.control.ButtonType no = new javafx.scene.control.ButtonType("Annulla", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                warn.getButtonTypes().setAll(si, no);
                if (warn.showAndWait().orElse(no) != si) {
                    return;
                }
            }
            if (onConfermaSalva != null) {
                onConfermaSalva.accept(scelta);
            }
            popup.close();
        });

        Region spFooter = new Region();
        HBox.setHgrow(spFooter, Priority.ALWAYS);
        footer.getChildren().addAll(btnPrecProposta, btnSuccProposta, spFooter, btnAnnulla, btnRigenera, btnConferma);
        root.getChildren().addAll(lblTitolo, lblSub, barraRiepilogo, corpo, scrollAvvisi, footer);

        Scene scene = new Scene(root, 1200, 720);
        popup.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(scene);
        aggiornaVista[0].run();
        popup.centerOnScreen();
        popup.showAndWait();
    }

    /**
     * Permette la modifica manuale di una cella direttamente dentro la proposta temporanea
     * (senza toccare il database fino alla conferma finale).
     */
    private static void apriModificaCellaAnteprima(Window owner,
                                                   PropostaTurniSettimanaBean proposta,
                                                   int indiceGiorno,
                                                   int indiceFascia,
                                                   String chiaveData,
                                                   String titoloEsteso,
                                                   List<DipendenteBean> dipendenti,
                                                   Function<List<TurnoBean>, PropostaTurniSettimanaBean> onRivaluta,
                                                   Consumer<PropostaTurniSettimanaBean> onAggiorna) {
        String oraInizio = TurniStaffView.FASCE_ORARIE[indiceFascia][0];
        String oraFine = TurniStaffView.FASCE_ORARIE[indiceFascia][1];
        LocalDate dataGiorno = proposta.getLunediSettimana().plusDays(indiceGiorno);

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(owner);
        dlg.setTitle("Modifica Proposta — " + titoloEsteso + " (" + oraInizio + "–" + oraFine + ")");

        VBox box = new VBox(10);
        box.setPrefWidth(460);
        box.setStyle("-fx-padding: 18 22; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");

        Label lblTit = new Label("✏ Modifica Proposta: " + titoloEsteso + " (" + oraInizio + "–" + oraFine + ")");
        lblTit.setWrapText(true);
        lblTit.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        VBox listaAttuali = new VBox(6);
        List<TurnoBean> listaProposti = new ArrayList<>(proposta.getTurniProposti());
        boolean isSettimanaCorrente = proposta.getLunediSettimana() != null
                && proposta.getLunediSettimana().equals(LocalDate.now().with(java.time.DayOfWeek.MONDAY));
        List<TurnoBean> nellaCella = TurniStaffView.filtraTurniPerCella(
                listaProposti, indiceGiorno, dataGiorno, isSettimanaCorrente, indiceFascia, oraInizio, oraFine);

        final TurnoBean[] turnoInModifica = {null};

        ComboBox<String> comboDip = new ComboBox<>();
        if (dipendenti != null) {
            for (DipendenteBean d : dipendenti) {
                if (d.isAttivo()) {
                    double ore = CalcolatoreOreSettimanali.calcolaOreDipendenteNellaSettimana(
                            d.getEmail(), listaProposti, proposta.getLunediSettimana());
                    String info = d.getEmail() + " — " + d.getNome() + " " + d.getCognome()
                            + " (" + ConfigurazioneOreDipendente.formattaOre(ore) + " / "
                            + d.getConfigurazioneOre().getEtichettaObiettivo() + ")";
                    comboDip.getItems().add(info);
                }
            }
        }
        comboDip.setPromptText("Aggiungi un dipendente alla proposta...");
        comboDip.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.TextField txtInizioCustom = new javafx.scene.control.TextField(oraInizio);
        txtInizioCustom.setPrefWidth(80);
        javafx.scene.control.TextField txtFineCustom = new javafx.scene.control.TextField(oraFine);
        txtFineCustom.setPrefWidth(80);
        javafx.scene.control.TextField txtPausaMin = new javafx.scene.control.TextField("0");
        txtPausaMin.setPrefWidth(65);
        HBox rigaOrariCustom = new HBox(8,
                new Label("Inizio:"), txtInizioCustom,
                new Label("Fine:"), txtFineCustom,
                new Label("Pausa (min):"), txtPausaMin);
        rigaOrariCustom.setAlignment(Pos.CENTER_LEFT);
        for (javafx.scene.Node n : rigaOrariCustom.getChildren()) {
            if (n instanceof Label l) l.setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 11.5px;");
        }

        CheckBox chkBlocca = new CheckBox("🔒 Blocca questa assegnazione");
        chkBlocca.setStyle("-fx-text-fill: #f5ebe0; -fx-font-size: 12px;");

        Label lblModoForm = new Label();
        lblModoForm.setStyle("-fx-text-fill: #e5c99b; -fx-font-weight: bold; -fx-font-size: 11.5px;");
        lblModoForm.setVisible(false);
        lblModoForm.setManaged(false);

        Label lblErrore = new Label();
        lblErrore.setStyle("-fx-text-fill: #fca5a5; -fx-font-weight: bold; -fx-font-size: 11.5px;");
        lblErrore.setVisible(false);
        lblErrore.setManaged(false);

        Button btnAnnullaMod = new Button("Annulla modifica");
        btnAnnullaMod.setVisible(false);
        btnAnnullaMod.setManaged(false);

        Button btnAgg = new Button("+ Aggiungi alla proposta");
        btnAgg.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");

        btnAnnullaMod.setOnAction(e -> {
            turnoInModifica[0] = null;
            comboDip.getSelectionModel().clearSelection();
            txtInizioCustom.setText(oraInizio);
            txtFineCustom.setText(oraFine);
            txtPausaMin.setText("0");
            chkBlocca.setSelected(false);
            btnAgg.setText("+ Aggiungi alla proposta");
            lblModoForm.setVisible(false);
            lblModoForm.setManaged(false);
            btnAnnullaMod.setVisible(false);
            btnAnnullaMod.setManaged(false);
            lblErrore.setVisible(false);
            lblErrore.setManaged(false);
        });

        if (nellaCella.isEmpty()) {
            Label vuoto = new Label("Nessun dipendente proposto in questa fascia.");
            vuoto.setStyle("-fx-font-size: 12px; -fx-text-fill: #a89f92; -fx-font-style: italic;");
            listaAttuali.getChildren().add(vuoto);
        } else {
            for (TurnoBean t : nellaCella) {
                HBox riga = new HBox(8);
                riga.setAlignment(Pos.CENTER_LEFT);
                String orarioTurno = (t.getOraInizio() != null && t.getOraFine() != null)
                        ? ("  (" + t.getOraInizio() + "–" + t.getOraFine()
                           + (t.getPausaMinuti() > 0 ? ", pausa " + t.getPausaMinuti() + "m" : "") + ")")
                        : "";
                Label l = new Label((t.isBloccato() ? "🔒 " : "👤 ") + t.getNomeDipendente() + orarioTurno);
                l.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #f5ebe0;");
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Button btnMod = new Button("Modifica");
                btnMod.setStyle("-fx-background-color: #764c86; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 9; -fx-cursor: hand;");
                btnMod.setOnAction(e -> {
                    turnoInModifica[0] = t;
                    for (String item : comboDip.getItems()) {
                        if (t.getEmailDipendente() != null
                                && item.toLowerCase().startsWith(t.getEmailDipendente().toLowerCase() + " — ")) {
                            comboDip.setValue(item);
                            break;
                        }
                    }
                    txtInizioCustom.setText(t.getOraInizio() != null ? t.getOraInizio() : oraInizio);
                    txtFineCustom.setText(t.getOraFine() != null ? t.getOraFine() : oraFine);
                    txtPausaMin.setText(String.valueOf(Math.max(0, t.getPausaMinuti())));
                    chkBlocca.setSelected(t.isBloccato());
                    lblModoForm.setText("✏ Modifica in corso: " + t.getNomeDipendente() + orarioTurno);
                    lblModoForm.setVisible(true);
                    lblModoForm.setManaged(true);
                    btnAnnullaMod.setVisible(true);
                    btnAnnullaMod.setManaged(true);
                    btnAgg.setText("✔ Salva modifica");
                    lblErrore.setVisible(false);
                    lblErrore.setManaged(false);
                });

                Button btnLock = new Button(t.isBloccato() ? "Sblocca" : "🔒 Blocca");
                btnLock.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-cursor: hand;");
                btnLock.setOnAction(e -> {
                    t.setBloccato(!t.isBloccato());
                    PropostaTurniSettimanaBean agg = onRivaluta.apply(listaProposti);
                    onAggiorna.accept(agg);
                    dlg.close();
                });

                Button btnRem = new Button("Rimuovi");
                btnRem.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-padding: 3 8; -fx-cursor: hand;");
                btnRem.setOnAction(e -> {
                    listaProposti.remove(t);
                    PropostaTurniSettimanaBean agg = onRivaluta.apply(listaProposti);
                    onAggiorna.accept(agg);
                    dlg.close();
                });
                riga.getChildren().addAll(l, sp, btnMod, btnLock, btnRem);
                listaAttuali.getChildren().add(riga);
            }
        }

        HBox az = new HBox(8);
        az.setAlignment(Pos.CENTER_RIGHT);
        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setOnAction(e -> dlg.close());

        btnAgg.setOnAction(e -> {
            String sel = comboDip.getValue();
            if (sel == null || sel.isBlank()) {
                lblErrore.setText("Seleziona un dipendente.");
                lblErrore.setVisible(true);
                lblErrore.setManaged(true);
                return;
            }
            String email = sel.split(" — ")[0].trim();
            DipendenteBean dip = dipendenti.stream().filter(d -> d.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
            if (dip == null) {
                return;
            }
            String ini = txtInizioCustom.getText() != null ? txtInizioCustom.getText().trim() : oraInizio;
            String fin = txtFineCustom.getText() != null ? txtFineCustom.getText().trim() : oraFine;
            int pausa = 0;
            try {
                pausa = Math.max(0, Integer.parseInt(txtPausaMin.getText().trim()));
            } catch (Exception ignored) {
                pausa = 0;
            }

            TurnoBean target = turnoInModifica[0] != null ? turnoInModifica[0] : new TurnoBean();
            if (turnoInModifica[0] == null) {
                target.setCodiceTurno("TMP-" + System.nanoTime());
            }
            try {
                target.setData(chiaveData);
                target.setOraInizio(ini);
                target.setOraFine(fin);
                target.setEmailDipendente(dip.getEmail());
            } catch (Exception ex) {
                lblErrore.setText("Orario non valido: usa il formato HH:mm (es. 09:00, 17:30).");
                lblErrore.setVisible(true);
                lblErrore.setManaged(true);
                return;
            }
            target.setNomeDipendente(dip.getNome() + " " + dip.getCognome());
            target.setStato("PIANIFICATO");
            target.setBloccato(chkBlocca.isSelected());
            target.setGeneratoAutomaticamente(false);
            target.setPersonalizzato(model.DisponibilitaDipendente.identificaIndiceFascia(ini, fin) < 0);
            target.setPausaMinuti(pausa);
            if (turnoInModifica[0] == null) {
                listaProposti.add(target);
            }

            PropostaTurniSettimanaBean agg = onRivaluta.apply(listaProposti);
            onAggiorna.accept(agg);
            dlg.close();
        });
        az.getChildren().addAll(btnAnnullaMod, btnChiudi, btnAgg);

        box.getChildren().addAll(lblTit, listaAttuali, lblModoForm, comboDip, rigaOrariCustom, chkBlocca, lblErrore, az);
        Scene dlgScene = new Scene(box);
        dlg.setScene(dlgScene);
        TemaProvenzaHelper.applicaSeGui2(dlgScene);
        dlg.showAndWait();
    }

    private static Label creaPillola(String testo, String colore, boolean provenza) {
        Label l = new Label(testo);
        l.setStyle("-fx-background-color: " + (provenza ? "#ffffff" : "rgba(28,25,34,0.92)")
                + "; -fx-border-color: " + colore
                + "; -fx-text-fill: " + (provenza ? colore : "#f5ebe0")
                + "; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-padding: 4 10; -fx-background-radius: 12; -fx-border-radius: 12;");
        return l;
    }
}
