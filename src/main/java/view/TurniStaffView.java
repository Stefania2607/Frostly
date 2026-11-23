package view;

import bean.EmergenzaBean;
import bean.TurnoBean;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Componente View dedicato esclusivamente alla renderizzazione grafica del Calendario Settimanale
 * Tabellare dei Turni (Lunedì - Domenica × Fasce Orarie) e dei relativi Pop-up modali:
 * - Pop-up Maestro per inserire/rimuovere dipendenti in ciascun incrocio e gestire emergenze;
 * - Pop-up Dipendente per reclamare un problema/emergenza con richiesta di modifica sul proprio turno.
 */
public final class TurniStaffView {

    public static final String[] GIORNI_SETTIMANA = {
        "Lunedi", "Martedi", "Mercoledi", "Giovedi", "Venerdi", "Sabato", "Domenica"
    };

    public static final String[] GIORNI_ETICHETTE = {
        "LUNEDÌ", "MARTEDÌ", "MERCOLEDÌ", "GIOVEDÌ", "VENERDÌ", "SABATO", "DOMENICA"
    };

    public static final String[] GIORNI_BREVI = {
        "Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"
    };

    public static final String[][] FASCE_ORARIE = {
        {"07:00", "14:00", "07:00 – 14:00", "🌅 Mattina (7-14)", "TUTTI"},
        {"14:00", "20:00", "14:00 – 20:00", "☀️ Pomeriggio (14-20)", "TUTTI"},
        {"16:00", "00:00", "16:00 – 00:00", "🌆 Pom./Sera (16-24)", "TUTTI"},
        {"20:00", "00:00", "20:00 – 00:00", "🌙 Serale (20-24)", "TUTTI"},
        {"10:00", "18:00", "10:00 – 18:00", "🎉 Extra Weekend (10-18)", "WEEKEND"}
    };

    private static final DateTimeFormatter FMT_BREVE = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_COMPLETO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FunctionalInterface
    public interface IncrocioClickHandler {
        void onIncrocioSelezionato(int indiceGiorno, int indiceFascia, String chiaveDataGiorno, String titoloGiornoEsteso);
    }

    @FunctionalInterface
    public interface GestioneEmergenzaCallback {
        void onApprova(String codiceEmergenza, String emailSostituto, String nota);
    }

    private TurniStaffView() {
    }

    /**
     * Costruisce e popola la tabella settimanale (Lunedì - Domenica × 5 Fasce Orarie)
     * connessa al calendario reale a partire dal lunedì della settimana selezionata.
     */
    public static void popolaCalendarioSettimanale(VBox contenitore,
                                                   LocalDate lunediSettimana,
                                                   List<TurnoBean> tuttiTurni,
                                                   boolean isMaestro,
                                                   String emailUtenteLoggato,
                                                   IncrocioClickHandler onClickIncrocioMaestro,
                                                   Consumer<TurnoBean> onClickTurnoDipendente) {
        if (contenitore == null) return;
        contenitore.getChildren().clear();

        LocalDate oggi = LocalDate.now();
        LocalDate lunediCorrente = oggi.with(DayOfWeek.MONDAY);
        boolean isSettimanaCorrente = lunediSettimana.equals(lunediCorrente);

        GridPane griglia = new GridPane();
        griglia.setHgap(6);
        griglia.setVgap(6);
        griglia.setStyle("-fx-padding: 6; -fx-background-color: rgba(20, 18, 22, 0.90); -fx-background-radius: 12; -fx-border-color: rgba(186, 164, 135, 0.28); -fx-border-radius: 12;");

        // Vincoli colonne: Colonna 0 per fasce orarie, Colonne 1..7 per Lunedì..Domenica
        ColumnConstraints colOrari = new ColumnConstraints();
        colOrari.setMinWidth(135);
        colOrari.setPrefWidth(142);
        colOrari.setMaxWidth(155);
        griglia.getColumnConstraints().add(colOrari);

        for (int g = 0; g < 7; g++) {
            ColumnConstraints colGiorno = new ColumnConstraints();
            colGiorno.setMinWidth(122);
            colGiorno.setPrefWidth(140);
            colGiorno.setHgrow(Priority.ALWAYS);
            griglia.getColumnConstraints().add(colGiorno);
        }

        VBox cellaAngolo = new VBox(2);
        cellaAngolo.setAlignment(Pos.CENTER);
        cellaAngolo.setStyle("-fx-background-color: rgba(35, 32, 38, 0.95); -fx-background-radius: 8; -fx-padding: 8 6; -fx-border-color: rgba(186, 164, 135, 0.25); -fx-border-radius: 8;");
        Label lblAngolo1 = new Label("⏰ FASCE ORARIE");
        lblAngolo1.setStyle("-fx-text-fill: #f5ebe0; -fx-font-weight: bold; -fx-font-size: 12.5px;");
        Label lblAngolo2 = new Label("🗓️ GIORNI SETTIMANA");
        lblAngolo2.setStyle("-fx-text-fill: #baa487; -fx-font-size: 11.5px; -fx-font-weight: bold;");
        cellaAngolo.getChildren().addAll(lblAngolo1, lblAngolo2);
        griglia.add(cellaAngolo, 0, 0);

        for (int g = 0; g < 7; g++) {
            LocalDate dataGiorno = lunediSettimana.plusDays(g);
            boolean isOggi = dataGiorno.equals(oggi);
            boolean isWeekend = (g >= 5);

            VBox headerGiorno = new VBox(2);
            headerGiorno.setAlignment(Pos.CENTER);
            String bgHeader = isOggi ? "#baa487" : (isWeekend ? "rgba(45, 40, 48, 0.95)" : "rgba(32, 30, 35, 0.90)");
            String borderHeader = isOggi ? "#baa487" : "rgba(186, 164, 135, 0.25)";
            headerGiorno.setStyle("-fx-background-color: " + bgHeader + "; -fx-background-radius: 8; -fx-padding: 7 4; -fx-border-color: " + borderHeader + "; -fx-border-radius: 8;");

            Label lblNomeGiorno = new Label(GIORNI_ETICHETTE[g] + (isOggi ? " (OGGI)" : ""));
            lblNomeGiorno.setStyle("-fx-text-fill: " + (isOggi ? "#141312" : "#f5ebe0") + "; -fx-font-weight: bold; -fx-font-size: 13px;");

            Label lblData = new Label(dataGiorno.format(FMT_COMPLETO));
            lblData.setStyle("-fx-text-fill: " + (isOggi ? "#141312" : "#baa487") + "; -fx-font-size: 12px; -fx-font-weight: bold;");

            headerGiorno.getChildren().addAll(lblNomeGiorno, lblData);
            griglia.add(headerGiorno, g + 1, 0);
        }

        for (int f = 0; f < FASCE_ORARIE.length; f++) {
            String oraInizio = FASCE_ORARIE[f][0];
            String oraFine = FASCE_ORARIE[f][1];
            String rangeLabel = FASCE_ORARIE[f][2];
            String descFascia = FASCE_ORARIE[f][3];
            boolean soloWeekend = "WEEKEND".equals(FASCE_ORARIE[f][4]);

            VBox boxFascia = new VBox(3);
            boxFascia.setAlignment(Pos.CENTER_LEFT);
            String bgFascia = "rgba(26, 24, 28, 0.90)";
            String borderFascia = "rgba(186, 164, 135, 0.22)";
            boxFascia.setStyle("-fx-background-color: " + bgFascia + "; -fx-border-color: " + borderFascia
                    + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;");

            Label lblRange = new Label("⏰ " + rangeLabel);
            lblRange.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5ebe0;");

            Label lblDesc = new Label(descFascia);
            lblDesc.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (soloWeekend ? "#fcd34d;" : "#baa487;"));

            boxFascia.getChildren().addAll(lblRange, lblDesc);
            griglia.add(boxFascia, 0, f + 1);

            for (int g = 0; g < 7; g++) {
                boolean isWeekend = (g >= 5);
                LocalDate dataGiorno = lunediSettimana.plusDays(g);
                String chiaveDataGiorno = GIORNI_BREVI[g] + " " + dataGiorno.format(FMT_COMPLETO);
                String titoloGiornoEsteso = GIORNI_ETICHETTE[g] + " " + dataGiorno.format(FMT_COMPLETO);

                if (soloWeekend && !isWeekend) {
                    // Cella non prevista da Lunedì a Venerdì per il turno 10:00 - 18:00
                    VBox cellaDisabilitata = new VBox();
                    cellaDisabilitata.setAlignment(Pos.CENTER);
                    cellaDisabilitata.setMinHeight(78);
                    cellaDisabilitata.setStyle("-fx-background-color: rgba(24, 22, 26, 0.40); -fx-border-color: rgba(186, 164, 135, 0.12); "
                            + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6;");
                    Label lblNo = new Label("— Solo Sab / Dom —");
                    lblNo.setStyle("-fx-text-fill: #7d756d; -fx-font-size: 12px; -fx-font-style: italic;");
                    cellaDisabilitata.getChildren().add(lblNo);
                    griglia.add(cellaDisabilitata, g + 1, f + 1);
                    continue;
                }

                // Filtra i turni appartenenti a questo incrocio (Giorno g, Fascia f)
                List<TurnoBean> turniCella = filtraTurniPerCella(
                        tuttiTurni, g, dataGiorno, isSettimanaCorrente, f, oraInizio, oraFine);

                VBox cella = costruisciCellaIntersezione(
                        g, f, chiaveDataGiorno, titoloGiornoEsteso,
                        turniCella, isMaestro, emailUtenteLoggato,
                        onClickIncrocioMaestro, onClickTurnoDipendente);

                griglia.add(cella, g + 1, f + 1);
            }
        }

        contenitore.getChildren().add(griglia);
    }

    private static VBox costruisciCellaIntersezione(int indiceGiorno,
                                                    int indiceFascia,
                                                    String chiaveDataGiorno,
                                                    String titoloGiornoEsteso,
                                                    List<TurnoBean> turniCella,
                                                    boolean isMaestro,
                                                    String emailUtenteLoggato,
                                                    IncrocioClickHandler onClickIncrocioMaestro,
                                                    Consumer<TurnoBean> onClickTurnoDipendente) {
        VBox cella = new VBox(5);
        cella.setAlignment(Pos.TOP_LEFT);
        cella.setMinHeight(82);

        boolean haEmergenza = turniCella.stream().anyMatch(t -> "IN_EMERGENZA".equalsIgnoreCase(t.getStato()));
        boolean haMioTurno = !isMaestro && emailUtenteLoggato != null
                && turniCella.stream().anyMatch(t -> emailUtenteLoggato.equalsIgnoreCase(t.getEmailDipendente()));

        String bgCella = haEmergenza ? "rgba(65, 20, 25, 0.85)" : (haMioTurno ? "rgba(22, 55, 38, 0.85)" : "rgba(24, 22, 26, 0.85)");
        String borderCella = haEmergenza ? "#ef4444" : (haMioTurno ? "#10b981" : "rgba(186, 164, 135, 0.20)");
        String borderWidth = (haEmergenza || haMioTurno) ? "1.8" : "1";

        cella.setStyle("-fx-background-color: " + bgCella + "; -fx-border-color: " + borderCella
                + "; -fx-border-width: " + borderWidth + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6;");

        if (turniCella.isEmpty()) {
            Label lblVuoto = new Label("Nessun assegnato");
            lblVuoto.setStyle("-fx-text-fill: #7d756d; -fx-font-size: 12px; -fx-font-style: italic;");
            cella.getChildren().add(lblVuoto);
        } else {
            for (TurnoBean t : turniCella) {
                VBox badgeTurno = costruisciBadgeTurnoInCella(
                        t, isMaestro, emailUtenteLoggato,
                        () -> {
                            if (isMaestro && onClickIncrocioMaestro != null) {
                                onClickIncrocioMaestro.onIncrocioSelezionato(
                                        indiceGiorno, indiceFascia, chiaveDataGiorno, titoloGiornoEsteso);
                            } else if (!isMaestro && onClickTurnoDipendente != null) {
                                onClickTurnoDipendente.accept(t);
                            }
                        });
                cella.getChildren().add(badgeTurno);
            }
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        cella.getChildren().add(spacer);

        if (isMaestro && onClickIncrocioMaestro != null) {
            Button btnGestisci = new Button(turniCella.isEmpty() ? "➕ Inserisci" : "✏️ Gestisci / +");
            btnGestisci.setMaxWidth(Double.MAX_VALUE);
            btnGestisci.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                    + "-fx-font-size: 12px; -fx-padding: 3 6; -fx-background-radius: 6; -fx-cursor: hand;");
            btnGestisci.setOnAction(e -> onClickIncrocioMaestro.onIncrocioSelezionato(
                    indiceGiorno, indiceFascia, chiaveDataGiorno, titoloGiornoEsteso));
            cella.getChildren().add(btnGestisci);
        }

        return cella;
    }

    private static VBox costruisciBadgeTurnoInCella(TurnoBean t,
                                                    boolean isMaestro,
                                                    String emailUtenteLoggato,
                                                    Runnable onClick) {
        boolean isMio = emailUtenteLoggato != null && emailUtenteLoggato.equalsIgnoreCase(t.getEmailDipendente());
        String stato = t.getStato() != null ? t.getStato().toUpperCase() : "PUBBLICATO";

        VBox badge = new VBox(2);
        String bgBadge;
        String borderBadge;
        String textColor;
        String statoLabel;

        if (stato.contains("EMERGENZA")) {
            bgBadge = "rgba(70, 20, 25, 0.90)";
            borderBadge = "#ef4444";
            textColor = "#fca5a5";
            statoLabel = "🚨 EMERGENZA";
        } else if (stato.contains("SOSTITUIT")) {
            bgBadge = "rgba(25, 45, 65, 0.90)";
            borderBadge = "#3b82f6";
            textColor = "#93c5fd";
            statoLabel = "🔄 Sostituito";
        } else if (isMio && !isMaestro) {
            bgBadge = "rgba(22, 55, 38, 0.90)";
            borderBadge = "#10b981";
            textColor = "#a7f3d0";
            statoLabel = "⭐ TUO TURNO (Clicca)";
        } else {
            bgBadge = "rgba(38, 35, 42, 0.90)";
            borderBadge = "rgba(186, 164, 135, 0.28)";
            textColor = "#f5ebe0";
            statoLabel = "✔ Assegnato";
        }

        badge.setStyle("-fx-background-color: " + bgBadge + "; -fx-border-color: " + borderBadge
                + "; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 6; -fx-cursor: hand;");

        Label lblNome = new Label("👤 " + t.getNomeDipendente());
        lblNome.setWrapText(true);
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: " + textColor + ";");

        Label lblStato = new Label(statoLabel);
        lblStato.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

        badge.getChildren().addAll(lblNome, lblStato);
        badge.setOnMouseClicked(e -> {
            e.consume();
            if (onClick != null) {
                onClick.run();
            }
        });
        return badge;
    }

    /**
     * Filtra i turni che appartengono al giorno (g, dataGiorno) e alla fascia oraria indicata.
     */
    public static List<TurnoBean> filtraTurniPerCella(List<TurnoBean> tuttiTurni,
                                                      int indiceGiorno,
                                                      LocalDate dataGiorno,
                                                      boolean isSettimanaCorrente,
                                                      int indiceFascia,
                                                      String oraInizioFascia,
                                                      String oraFineFascia) {
        List<TurnoBean> risultato = new ArrayList<>();
        if (tuttiTurni == null) return risultato;

        String dataStrCompleta = dataGiorno.format(FMT_COMPLETO);
        String dataStrBreve = dataGiorno.format(FMT_BREVE);

        for (TurnoBean t : tuttiTurni) {
            if (t.getData() == null) continue;
            String d = t.getData().trim();

            boolean giornoMatch = false;
            if (d.contains("/")) {
                // Turno associato a una data specifica del calendario
                giornoMatch = d.contains(dataStrCompleta) || d.endsWith(" " + dataStrBreve) || d.equals(dataStrBreve);
            } else if (isSettimanaCorrente) {
                // Turno legacy con solo nome del giorno (es. "Lunedi"): mostrato nella settimana corrente
                giornoMatch = corrispondeIndiceGiorno(d, indiceGiorno);
            }

            if (giornoMatch && corrispondeFasciaOraria(t.getOraInizio(), t.getOraFine(), indiceFascia, oraInizioFascia, oraFineFascia)) {
                risultato.add(t);
            }
        }
        return risultato;
    }

    private static boolean corrispondeIndiceGiorno(String testoGiorno, int indiceAtteso) {
        if (testoGiorno == null) return false;
        String s = testoGiorno.toLowerCase().replace("ì", "i");
        if (indiceAtteso == 0) return s.startsWith("lun");
        if (indiceAtteso == 1) return s.startsWith("mar");
        if (indiceAtteso == 2) return s.startsWith("mer");
        if (indiceAtteso == 3) return s.startsWith("gio");
        if (indiceAtteso == 4) return s.startsWith("ven");
        if (indiceAtteso == 5) return s.startsWith("sab");
        if (indiceAtteso == 6) return s.startsWith("dom");
        return false;
    }

    private static boolean corrispondeFasciaOraria(String inizioTurno,
                                                   String fineTurno,
                                                   int indiceFascia,
                                                   String inizioFascia,
                                                   String fineFascia) {
        if (inizioTurno == null || fineTurno == null) return false;
        String in = inizioTurno.trim();
        String fi = fineTurno.trim();
        if (in.equals(inizioFascia) && fi.equals(fineFascia)) {
            return true;
        }
        // Compatibilità con eventuali turni storici (08:00-14:00 -> fascia mattina 07:00-14:00; 18:00-00:00 -> fascia 20:00-00:00)
        if (indiceFascia == 0 && "08:00".equals(in) && "14:00".equals(fi)) {
            return true;
        }
        if (indiceFascia == 3 && "18:00".equals(in) && ("00:00".equals(fi) || "24:00".equals(fi))) {
            return true;
        }
        return false;
    }

    /**
     * Pop-up modale per il Maestro Gelataio: permette di inserire nuovi dipendenti nell'incrocio
     * (Giorno × Fascia Oraria), rimuovere assegnazioni o risolvere emergenze attive su quel turno.
     */
    public static void apriPopupGestioneIncrocioMaestro(Window owner,
                                                        String titoloGiornoEsteso,
                                                        String rangeFascia,
                                                        List<TurnoBean> turniAttualiCella,
                                                        List<String> dipendentiFormattati,
                                                        java.util.function.Function<String, EmergenzaBean> cercaEmergenzaPerTurno,
                                                        Consumer<String> onAssegnaDipendenteEmail,
                                                        Consumer<String> onRimuoviTurnoCodice,
                                                        GestioneEmergenzaCallback onApprovaEmergenza,
                                                        BiConsumer<String, String> onRifiutaEmergenza) {
        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Gestione Incrocio Turno — " + titoloGiornoEsteso);

        VBox root = new VBox(14);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setStyle("-fx-background-color: rgba(18,17,20,0.97); -fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label lblTitolo = new Label("📅 Gestione Incrocio Calendario Turni");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Label lblSottotitolo = new Label("🗓️ " + titoloGiornoEsteso + "   •   ⏰ Fascia: " + rangeFascia);
        lblSottotitolo.setStyle("-fx-background-color: rgba(35,32,38,0.90); -fx-text-fill: #baa487; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-padding: 6 12; -fx-background-radius: 8; -fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 8;");

        Label lblPresenti = new Label("👥 Personale assegnato in questo incrocio:");
        lblPresenti.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f5ebe0;");

        VBox boxPresenti = new VBox(8);
        if (turniAttualiCella == null || turniAttualiCella.isEmpty()) {
            Label lblNessuno = new Label("Nessun dipendente assegnato in questa fascia. Usa il modulo sotto per inserirne uno.");
            lblNessuno.setStyle("-fx-text-fill: #ded7cd; -fx-font-style: italic; -fx-font-size: 13.5px;");
            boxPresenti.getChildren().add(lblNessuno);
        } else {
            for (TurnoBean t : turniAttualiCella) {
                VBox cardAssegnato = new VBox(8);
                boolean inEmergenza = "IN_EMERGENZA".equalsIgnoreCase(t.getStato());
                cardAssegnato.setStyle("-fx-background-color: " + (inEmergenza ? "rgba(65,20,25,0.85)" : "rgba(26,24,28,0.92)")
                        + "; -fx-border-color: " + (inEmergenza ? "#ef4444" : "rgba(186,164,135,0.25)")
                        + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 12;");

                HBox rigaTop = new HBox(10);
                rigaTop.setAlignment(Pos.CENTER_LEFT);

                Label lblInfo = new Label("👤 " + t.getNomeDipendente() + " (" + t.getEmailDipendente() + ") — [" + t.getCodiceTurno() + "]");
                lblInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5ebe0;");
                lblInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3a2618;");
                HBox.setHgrow(lblInfo, Priority.ALWAYS);

                Button btnRimuovi = new Button("🗑️ Rimuovi dal Turno");
                btnRimuovi.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; "
                        + "-fx-font-size: 12.5px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand;");
                btnRimuovi.setOnAction(e -> {
                    if (onRimuoviTurnoCodice != null) {
                        onRimuoviTurnoCodice.accept(t.getCodiceTurno());
                    }
                    dialog.close();
                });

                rigaTop.getChildren().addAll(lblInfo, btnRimuovi);
                cardAssegnato.getChildren().add(rigaTop);

                // Se il turno è in emergenza, mostra i dettagli del reclamo e permette la sostituzione immediata
                if (inEmergenza && cercaEmergenzaPerTurno != null) {
                    EmergenzaBean em = cercaEmergenzaPerTurno.apply(t.getCodiceTurno());
                    if (em != null) {
                        VBox boxEm = new VBox(8);
                        boxEm.setStyle("-fx-background-color: #ffebee; -fx-border-color: #ef9a9a; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 10;");

                        Label lblMotivo = new Label("🚨 Emergenza segnalata (" + em.getCodiceEmergenza() + "): \"" + em.getMotivazione() + "\"");
                        lblMotivo.setWrapText(true);
                        lblMotivo.setStyle("-fx-font-weight: bold; -fx-text-fill: #b71c1c; -fx-font-size: 13.5px;");

                        HBox rigaSost = new HBox(8);
                        rigaSost.setAlignment(Pos.CENTER_LEFT);

                        ComboBox<String> comboSost = new ComboBox<>();
                        comboSost.setPromptText("Scegli Sostituto...");
                        comboSost.setPrefWidth(230);
                        if (dipendentiFormattati != null) {
                            for (String df : dipendentiFormattati) {
                                if (!df.toLowerCase().contains(t.getEmailDipendente().toLowerCase())) {
                                    comboSost.getItems().add(df);
                                }
                            }
                        }

                        TextField campoNota = new TextField("Sostituzione approvata dal calendario");
                        campoNota.setPromptText("Nota del Maestro...");
                        HBox.setHgrow(campoNota, Priority.ALWAYS);

                        rigaSost.getChildren().addAll(comboSost, campoNota);

                        HBox rigaAzioniEm = new HBox(8);
                        rigaAzioniEm.setAlignment(Pos.CENTER_RIGHT);

                        Button btnRifiuta = new Button("✕ Rifiuta Richiesta");
                        btnRifiuta.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 5 10; -fx-background-radius: 6; -fx-cursor: hand;");
                        btnRifiuta.setOnAction(ev -> {
                            if (onRifiutaEmergenza != null) {
                                onRifiutaEmergenza.accept(em.getCodiceEmergenza(), campoNota.getText());
                            }
                            dialog.close();
                        });

                        Button btnApprova = new Button("✔ Approva e Sostituisci");
                        btnApprova.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 5 12; -fx-background-radius: 6; -fx-cursor: hand;");
                        btnApprova.setOnAction(ev -> {
                            String sel = comboSost.getValue();
                            if (sel != null && onApprovaEmergenza != null) {
                                String emailSost = estraiEmail(sel);
                                onApprovaEmergenza.onApprova(em.getCodiceEmergenza(), emailSost, campoNota.getText());
                                dialog.close();
                            }
                        });

                        rigaAzioniEm.getChildren().addAll(btnRifiuta, btnApprova);
                        boxEm.getChildren().addAll(lblMotivo, rigaSost, rigaAzioniEm);
                        cardAssegnato.getChildren().add(boxEm);
                    }
                }

                boxPresenti.getChildren().add(cardAssegnato);
            }
        }

        ScrollPane scrollPresenti = new ScrollPane(boxPresenti);
        scrollPresenti.setFitToWidth(true);
        scrollPresenti.setPrefHeight(Math.min(220, Math.max(85, (turniAttualiCella != null ? turniAttualiCella.size() : 1) * 85)));
        scrollPresenti.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Separator sep = new Separator();

        VBox boxAggiungi = new VBox(8);
        boxAggiungi.setStyle("-fx-background-color: rgba(26,24,28,0.92); -fx-border-color: rgba(186,164,135,0.25); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");

        Label lblAggiungi = new Label("➕ Inserisci un Dipendente in questo Turno:");
        lblAggiungi.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #baa487;");

        HBox rigaAggiungi = new HBox(10);
        rigaAggiungi.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> comboNuovoDip = new ComboBox<>();
        if (dipendentiFormattati != null) {
            comboNuovoDip.setItems(FXCollections.observableArrayList(dipendentiFormattati));
            if (!dipendentiFormattati.isEmpty()) {
                comboNuovoDip.getSelectionModel().selectFirst();
            }
        }
        comboNuovoDip.setPromptText("Seleziona membro dello staff...");
        comboNuovoDip.setPrefWidth(300);

        Button btnInserisci = new Button("➕ Assegna al Turno");
        btnInserisci.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-padding: 7 16; -fx-background-radius: 8; -fx-cursor: hand;");
        btnInserisci.setOnAction(e -> {
            String selezionato = comboNuovoDip.getValue();
            if (selezionato != null && onAssegnaDipendenteEmail != null) {
                onAssegnaDipendenteEmail.accept(estraiEmail(selezionato));
                dialog.close();
            }
        });

        rigaAggiungi.getChildren().addAll(comboNuovoDip, btnInserisci);
        boxAggiungi.getChildren().addAll(lblAggiungi, rigaAggiungi);

        HBox boxBottom = new HBox();
        boxBottom.setAlignment(Pos.CENTER_RIGHT);
        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: rgba(38,35,42,0.85); -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-padding: 6 18; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(186,164,135,0.25);");
        btnChiudi.setOnAction(e -> dialog.close());
        boxBottom.getChildren().add(btnChiudi);

        root.getChildren().addAll(lblTitolo, lblSottotitolo, lblPresenti, scrollPresenti, sep, boxAggiungi, boxBottom);

        Scene scene = new Scene(root, 560, 470);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Pop-up modale per il Dipendente: si apre selezionando un proprio turno sul calendario settimanale
     * e consente di reclamare un problema / emergenza indicando la modifica o sostituzione richiesta.
     */
    public static void apriPopupEmergenzaDipendente(Window owner,
                                                    TurnoBean turnoSelezionato,
                                                    Consumer<String> onInviaMotivazioneEmergenza) {
        if (turnoSelezionato == null) return;

        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Segnalazione Emergenza Turno — " + turnoSelezionato.getCodiceTurno());

        VBox root = new VBox(14);
        root.setPadding(new Insets(22, 26, 22, 26));
        root.setStyle("-fx-background-color: rgba(18,17,20,0.97); -fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label lblTitolo = new Label("🚨 Segnala Problema o Richiedi Modifica Turno");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16.5px; -fx-font-weight: bold; -fx-text-fill: #fca5a5;");

        VBox boxRiepilogo = new VBox(4);
        boxRiepilogo.setStyle("-fx-background-color: rgba(70,20,25,0.85); -fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14;");

        Label lblTurnoInfo = new Label("📌 Turno Selezionato: " + turnoSelezionato.getCodiceTurno()
                + "   •   🗓️ " + turnoSelezionato.getData()
                + "   •   ⏰ " + turnoSelezionato.getOraInizio() + " – " + turnoSelezionato.getOraFine());
        lblTurnoInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f5ebe0;");

        Label lblDipInfo = new Label("👤 Dipendente: " + turnoSelezionato.getNomeDipendente()
                + "   •   Stato attuale: " + turnoSelezionato.getStato());
        lblDipInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #baa487; -fx-font-weight: bold;");
        boxRiepilogo.getChildren().addAll(lblTurnoInfo, lblDipInfo);

        Label lblTipo = new Label("1. Seleziona la tipologia di imprevisto o modifica richiesta:");
        lblTipo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5ebe0;");

        ComboBox<String> comboMotivoRapido = new ComboBox<>(FXCollections.observableArrayList(
                "🤒 Malattia / Febbre / Indisposizione medica",
                "🏠 Imprevisto familiare urgente",
                "🚗 Guasto mezzo di trasporto / Problemi viabilità",
                "🩺 Visita medica improrogabile",
                "🔄 Richiesta cambio fascia oraria o sostituzione con collega"
        ));
        comboMotivoRapido.setMaxWidth(Double.MAX_VALUE);
        comboMotivoRapido.getSelectionModel().selectFirst();

        Label lblDettagli = new Label("2. Descrivi la situazione dell'emergenza e la modifica richiesta al Maestro:");
        lblDettagli.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f5ebe0;");

        TextArea areaDettagli = new TextArea();
        areaDettagli.setPromptText("Es. Non potrò essere presente per visita medica urgente, chiedo sostituzione o spostamento al turno serale...");
        areaDettagli.setWrapText(true);
        areaDettagli.setPrefRowCount(4);

        Label lblErrore = new Label();
        lblErrore.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblErrore.setVisible(false);
        lblErrore.setManaged(false);

        HBox boxBottoni = new HBox(12);
        boxBottoni.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: #ede0d4; -fx-text-fill: #5c3a21; -fx-font-weight: bold; -fx-padding: 7 16; -fx-background-radius: 8; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> dialog.close());

        Button btnInvia = new Button("🚨 Invia Emergenza e Richiesta Modifica");
        btnInvia.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 18; -fx-background-radius: 8; -fx-cursor: hand;");
        btnInvia.setOnAction(e -> {
            String base = comboMotivoRapido.getValue() != null ? comboMotivoRapido.getValue() : "";
            String extra = areaDettagli.getText() != null ? areaDettagli.getText().trim() : "";
            String motivazioneCompleta = extra.isEmpty() ? base : (base + " — " + extra);

            if (motivazioneCompleta.trim().isEmpty()) {
                lblErrore.setText("⚠️ Specifica il motivo dell'emergenza o la modifica richiesta.");
                lblErrore.setVisible(true);
                lblErrore.setManaged(true);
                return;
            }
            if (onInviaMotivazioneEmergenza != null) {
                onInviaMotivazioneEmergenza.accept(motivazioneCompleta);
            }
            dialog.close();
        });

        boxBottoni.getChildren().addAll(btnAnnulla, btnInvia);
        root.getChildren().addAll(lblTitolo, boxRiepilogo, lblTipo, comboMotivoRapido, lblDettagli, areaDettagli, lblErrore, boxBottoni);

        Scene scene = new Scene(root, 540, 410);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static String estraiEmail(String formatoNomeEmail) {
        if (formatoNomeEmail == null) return "";
        if (formatoNomeEmail.contains("|")) {
            String[] p = formatoNomeEmail.split("\\|");
            return p[p.length - 1].trim();
        }
        return formatoNomeEmail.trim();
    }

    public static void configuraCelleTurni(ListView<String> listView, boolean mostraDipendente) {
        if (listView == null) return;
        listView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                String[] parti = item.split("\\|");
                String cod = parti.length > 0 ? parti[0].trim() : "";
                String giorno = parti.length > 1 ? parti[1].trim() : "";
                String orario = parti.length > 2 ? parti[2].trim() : "";
                String staff = parti.length > 3 ? parti[3].trim() : "";
                String stato = parti.length > 4 ? parti[4].trim() : "";

                HBox riga = new HBox(10);
                riga.setAlignment(Pos.CENTER_LEFT);
                riga.setStyle("-fx-padding: 8 12; -fx-background-color: rgba(30, 27, 34, 0.85); -fx-background-radius: 8; -fx-border-color: rgba(186, 164, 135, 0.22); -fx-border-radius: 8;");

                Label lblCod = new Label(cod);
                lblCod.setStyle("-fx-background-color: rgba(186, 164, 135, 0.18); -fx-text-fill: #baa487; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 12.5px;");

                Label lblGiorno = new Label("📅 " + giorno);
                lblGiorno.setStyle("-fx-background-color: rgba(255, 255, 255, 0.06); -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-padding: 4 9; -fx-background-radius: 6; -fx-font-size: 13px;");

                Label lblOrario = new Label("⏰ " + orario);
                lblOrario.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04); -fx-border-color: rgba(186, 164, 135, 0.25); -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #f5ebe0; -fx-font-weight: bold; -fx-padding: 3 8; -fx-font-size: 13px;");

                Label lblStaff = new Label(mostraDipendente ? ("👤 " + staff) : "");
                HBox.setHgrow(lblStaff, Priority.ALWAYS);
                lblStaff.setStyle("-fx-font-weight: bold; -fx-text-fill: #f5ebe0; -fx-font-size: 13.5px;");

                Label lblStato = new Label();
                if (stato.toUpperCase().contains("EMERGENZA")) {
                    lblStato.setText("🚨 IN EMERGENZA");
                    lblStato.setStyle("-fx-background-color: rgba(198, 40, 40, 0.22); -fx-text-fill: #ff8a80; -fx-border-color: rgba(198, 40, 40, 0.45); -fx-border-radius: 8; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 12px;");
                } else if (stato.toUpperCase().contains("SOSTITUIT")) {
                    lblStato.setText("🔄 SOSTITUITO");
                    lblStato.setStyle("-fx-background-color: rgba(21, 101, 192, 0.22); -fx-text-fill: #90caf9; -fx-border-color: rgba(21, 101, 192, 0.45); -fx-border-radius: 8; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 12px;");
                } else {
                    lblStato.setText("✔ ASSEGNATO");
                    lblStato.setStyle("-fx-background-color: rgba(46, 125, 50, 0.22); -fx-text-fill: #a5d6a7; -fx-border-color: rgba(46, 125, 50, 0.45); -fx-border-radius: 8; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 12px;");
                }

                if (mostraDipendente) {
                    riga.getChildren().addAll(lblCod, lblGiorno, lblOrario, lblStaff, lblStato);
                } else {
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    riga.getChildren().addAll(lblCod, lblGiorno, lblOrario, spacer, lblStato);
                }
                setGraphic(riga);
                setText(null);
            }
        });
    }
}
