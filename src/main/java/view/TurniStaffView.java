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
        griglia.setHgap(5);
        griglia.setVgap(5);
        VBox.setVgrow(griglia, Priority.ALWAYS);
        griglia.setStyle("-fx-padding: 6; -fx-background-color: rgba(20, 18, 22, 0.90); -fx-background-radius: 12; -fx-border-color: rgba(186, 164, 135, 0.28); -fx-border-radius: 12;");

        // Vincoli colonne: Colonna 0 per fasce orarie, Colonne 1..7 per Lunedì..Domenica
        ColumnConstraints colOrari = new ColumnConstraints();
        colOrari.setMinWidth(102);
        colOrari.setPrefWidth(106);
        colOrari.setMaxWidth(112);
        griglia.getColumnConstraints().add(colOrari);

        for (int g = 0; g < 7; g++) {
            ColumnConstraints colGiorno = new ColumnConstraints();
            colGiorno.setMinWidth(74);
            colGiorno.setPrefWidth(90);
            colGiorno.setHgrow(Priority.ALWAYS);
            griglia.getColumnConstraints().add(colGiorno);
        }

        // Vincoli righe: header fisso, 5 fasce orarie espanse equamente per evitare scrollbar
        javafx.scene.layout.RowConstraints rowHeader = new javafx.scene.layout.RowConstraints();
        rowHeader.setVgrow(Priority.NEVER);
        griglia.getRowConstraints().add(rowHeader);
        for (int f = 0; f < FASCE_ORARIE.length; f++) {
            javafx.scene.layout.RowConstraints rowFascia = new javafx.scene.layout.RowConstraints();
            rowFascia.setVgrow(Priority.ALWAYS);
            griglia.getRowConstraints().add(rowFascia);
        }

        boolean provenza = RecensioniView.isTemaSalottoChiaro(contenitore);
        if (provenza) {
            griglia.setStyle("-fx-background-color: #fbf6f1; -fx-background-radius: 14; -fx-padding: 8; -fx-border-color: #e9dbdf; -fx-border-radius: 14;");
        }

        VBox cellaAngolo = new VBox(2);
        cellaAngolo.setAlignment(Pos.CENTER);
        cellaAngolo.setStyle(provenza
                ? "-fx-background-color: #eee2ee; -fx-background-radius: 10; -fx-padding: 8 6; -fx-border-color: #ddcddc; -fx-border-radius: 10;"
                : "-fx-background-color: rgba(35, 32, 38, 0.95); -fx-background-radius: 8; -fx-padding: 8 6; -fx-border-color: rgba(186, 164, 135, 0.25); -fx-border-radius: 8;");
        Label lblAngolo1 = new Label("FASCE");
        lblAngolo1.setStyle("-fx-text-fill: " + (provenza ? "#37253d" : "#f5ebe0") + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label lblAngolo2 = new Label("ORARIE");
        lblAngolo2.setStyle("-fx-text-fill: " + (provenza ? "#764c86" : "#baa487") + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        cellaAngolo.getChildren().addAll(lblAngolo1, lblAngolo2);
        griglia.add(cellaAngolo, 0, 0);

        java.time.format.DateTimeFormatter fmtCorto = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        for (int g = 0; g < 7; g++) {
            LocalDate dataGiorno = lunediSettimana.plusDays(g);
            boolean isOggi = dataGiorno.equals(oggi);
            boolean isWeekend = (g >= 5);

            VBox headerGiorno = new VBox(1);
            headerGiorno.setAlignment(Pos.CENTER);
            String bgHeader;
            String borderHeader;
            if (provenza) {
                bgHeader = isOggi ? "#764c86" : (isWeekend ? "#f4eaf5" : "#f3eae5");
                borderHeader = isOggi ? "#5e316f" : "#e2d3df";
            } else {
                bgHeader = isOggi ? "#baa487" : (isWeekend ? "rgba(45, 40, 48, 0.95)" : "rgba(32, 30, 35, 0.90)");
                borderHeader = isOggi ? "#baa487" : "rgba(186, 164, 135, 0.25)";
            }
            headerGiorno.setStyle("-fx-background-color: " + bgHeader + "; -fx-background-radius: 10; -fx-padding: 6 3; -fx-border-color: " + borderHeader + "; -fx-border-radius: 10;");

            Label lblNomeGiorno = new Label(GIORNI_BREVI[g].toUpperCase() + (isOggi ? " •" : ""));
            lblNomeGiorno.setStyle("-fx-text-fill: " + (provenza ? (isOggi ? "#ffffff" : "#37253d") : (isOggi ? "#141312" : "#f5ebe0")) + "; -fx-font-weight: bold; -fx-font-size: 12px;");

            Label lblData = new Label(dataGiorno.format(fmtCorto));
            lblData.setStyle("-fx-text-fill: " + (provenza ? (isOggi ? "#f3eae5" : "#764c86") : (isOggi ? "#141312" : "#baa487")) + "; -fx-font-size: 11px; -fx-font-weight: bold;");

            headerGiorno.getChildren().addAll(lblNomeGiorno, lblData);
            griglia.add(headerGiorno, g + 1, 0);
        }

        for (int f = 0; f < FASCE_ORARIE.length; f++) {
            String oraInizio = FASCE_ORARIE[f][0];
            String oraFine = FASCE_ORARIE[f][1];
            String rangeLabel = FASCE_ORARIE[f][2];
            String descFascia = FASCE_ORARIE[f][3];
            boolean soloWeekend = "WEEKEND".equals(FASCE_ORARIE[f][4]);

            VBox boxFascia = new VBox(2);
            boxFascia.setAlignment(Pos.CENTER_LEFT);
            String bgFascia = provenza ? "#fffaf7" : "rgba(26, 24, 28, 0.90)";
            String borderFascia = provenza ? "#e9dbdf" : "rgba(186, 164, 135, 0.22)";
            boxFascia.setStyle("-fx-background-color: " + bgFascia + "; -fx-border-color: " + borderFascia
                    + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 6 7;");

            String shortRange = oraInizio.substring(0, 2) + ":00–" + oraFine.substring(0, 2) + ":00";
            Label lblRange = new Label(shortRange);
            lblRange.setMinWidth(Region.USE_PREF_SIZE);
            lblRange.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + (provenza ? "#37253d;" : "#f5ebe0;"));

            int idxPar = descFascia.indexOf('(');
            String baseDesc = idxPar >= 0 ? descFascia.substring(0, idxPar) : descFascia;
            String shortDesc = baseDesc.replaceFirst("^[^\\p{L}]+", "").trim();
            Label lblDesc = new Label(shortDesc);
            lblDesc.setWrapText(true);
            lblDesc.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: " + (provenza ? (soloWeekend ? "#ad443f;" : "#764c86;") : (soloWeekend ? "#fcd34d;" : "#baa487;")));

            boxFascia.getChildren().addAll(lblRange, lblDesc);
            griglia.add(boxFascia, 0, f + 1);

            for (int g = 0; g < 7; g++) {
                boolean isWeekend = (g >= 5);
                LocalDate dataGiorno = lunediSettimana.plusDays(g);
                String chiaveDataGiorno = GIORNI_BREVI[g] + " " + dataGiorno.format(FMT_COMPLETO);
                String titoloGiornoEsteso = GIORNI_ETICHETTE[g] + " " + dataGiorno.format(FMT_COMPLETO);

                if (soloWeekend && !isWeekend) {
                    VBox cellaDisabilitata = new VBox();
                    cellaDisabilitata.setAlignment(Pos.CENTER);
                    cellaDisabilitata.setMinHeight(64);
                    cellaDisabilitata.setStyle(provenza
                            ? "-fx-background-color: #f3eae5; -fx-border-color: #e9dbdf; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 4;"
                            : "-fx-background-color: rgba(24, 22, 26, 0.40); -fx-border-color: rgba(186, 164, 135, 0.12); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4;");
                    Label lblNo = new Label("Solo weekend");
                    lblNo.setWrapText(true);
                    lblNo.setStyle("-fx-text-fill: " + (provenza ? "#8d7c92" : "#7d756d") + "; -fx-font-size: 10.5px; -fx-font-style: italic;");
                    cellaDisabilitata.getChildren().add(lblNo);
                    griglia.add(cellaDisabilitata, g + 1, f + 1);
                    continue;
                }

                List<TurnoBean> turniCella = filtraTurniPerCella(
                        tuttiTurni, g, dataGiorno, isSettimanaCorrente, f, oraInizio, oraFine);

                VBox cella = costruisciCellaIntersezione(
                        g, f, chiaveDataGiorno, titoloGiornoEsteso,
                        turniCella, isMaestro, emailUtenteLoggato, provenza,
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
                                                    boolean provenza,
                                                    IncrocioClickHandler onClickIncrocioMaestro,
                                                    Consumer<TurnoBean> onClickTurnoDipendente) {
        VBox cella = new VBox(4);
        cella.setAlignment(turniCella.isEmpty() ? Pos.CENTER : Pos.TOP_LEFT);
        cella.setMinHeight(64);

        boolean haEmergenza = turniCella.stream().anyMatch(t -> "IN_EMERGENZA".equalsIgnoreCase(t.getStato()));
        boolean haMioTurno = !isMaestro && emailUtenteLoggato != null
                && turniCella.stream().anyMatch(t -> emailUtenteLoggato.equalsIgnoreCase(t.getEmailDipendente()));
        boolean scoperta = turniCella.isEmpty();

        String bgCella;
        String borderCella;
        if (provenza) {
            bgCella = haEmergenza ? "#fdf2f2" : (haMioTurno ? "#edf7f0" : (scoperta ? "#fdfaf7" : "#ffffff"));
            borderCella = haEmergenza ? "#ef4444" : (haMioTurno ? "#387548" : (scoperta ? "#decfe0" : "#eadde2"));
        } else {
            bgCella = haEmergenza ? "rgba(65, 20, 25, 0.85)" : (haMioTurno ? "rgba(22, 55, 38, 0.85)" : "rgba(24, 22, 26, 0.85)");
            borderCella = haEmergenza ? "#ef4444" : (haMioTurno ? "#10b981" : "rgba(186, 164, 135, 0.22)");
        }
        String borderWidth = (haEmergenza || haMioTurno) ? "1.6" : "1";
        String borderStyle = scoperta ? "-fx-border-style: dashed; " : "";
        String cursorStyle = (isMaestro && onClickIncrocioMaestro != null) ? "-fx-cursor: hand; " : "";

        String stileBase = "-fx-background-color: " + bgCella + "; -fx-border-color: " + borderCella
                + "; -fx-border-width: " + borderWidth + "; " + borderStyle + cursorStyle
                + "-fx-border-radius: 9; -fx-background-radius: 9; -fx-padding: 4;";
        String stileHover = "-fx-background-color: " + (provenza ? "#faf1fc" : "rgba(42, 36, 48, 0.94)")
                + "; -fx-border-color: " + (provenza ? "#8c5b9e" : "#baa487")
                + "; -fx-border-width: 1.5; " + cursorStyle
                + "-fx-border-radius: 9; -fx-background-radius: 9; -fx-padding: 4;";

        cella.setStyle(stileBase);

        if (scoperta) {
            Label lblVuoto = new Label(isMaestro ? "+ Assegna" : "—");
            lblVuoto.setStyle("-fx-text-fill: " + (provenza ? "#9d8ba4" : "#8b8175")
                    + "; -fx-font-size: 11px; -fx-font-weight: " + (isMaestro ? "bold;" : "normal;"));
            cella.getChildren().add(lblVuoto);
        } else {
            int maxVisibili = 2;
            int mostrati = Math.min(maxVisibili, turniCella.size());
            for (int i = 0; i < mostrati; i++) {
                TurnoBean t = turniCella.get(i);
                VBox badgeTurno = costruisciBadgeTurnoInCella(
                        t, isMaestro, emailUtenteLoggato, provenza,
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
            if (turniCella.size() > maxVisibili) {
                int extra = turniCella.size() - maxVisibili;
                Label lblExtra = new Label("+" + extra + (extra == 1 ? " altro" : " altri"));
                lblExtra.setStyle("-fx-background-color: " + (provenza ? "#eee2ee" : "rgba(186,164,135,0.20)")
                        + "; -fx-text-fill: " + (provenza ? "#5e316f" : "#baa487")
                        + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 8;");
                cella.getChildren().add(lblExtra);
            }
        }

        // Intera cella cliccabile per il Maestro (niente pulsanti "Gestisci" ripetuti)
        if (isMaestro && onClickIncrocioMaestro != null) {
            cella.setOnMouseEntered(e -> cella.setStyle(stileHover));
            cella.setOnMouseExited(e -> cella.setStyle(stileBase));
            cella.setOnMouseClicked(e -> onClickIncrocioMaestro.onIncrocioSelezionato(
                    indiceGiorno, indiceFascia, chiaveDataGiorno, titoloGiornoEsteso));

            StringBuilder tip = new StringBuilder(titoloGiornoEsteso)
                    .append(" • ").append(FASCE_ORARIE[indiceFascia][2]);
            if (scoperta) {
                tip.append("\nTurno scoperto — clicca per assegnare personale");
            } else {
                for (TurnoBean t : turniCella) {
                    tip.append("\n• ").append(t.getNomeDipendente())
                       .append(" (").append(t.getOraInizio()).append("–").append(t.getOraFine()).append(")");
                    if (t.getPausaMinuti() > 0) tip.append(" [pausa ").append(t.getPausaMinuti()).append("m]");
                    if (t.isBloccato()) tip.append(" 🔒");
                    if ("IN_EMERGENZA".equalsIgnoreCase(t.getStato())) tip.append(" [EMERGENZA]");
                }
                tip.append("\nClicca per gestire o modificare assegnazioni");
            }
            javafx.scene.control.Tooltip.install(cella, new javafx.scene.control.Tooltip(tip.toString()));
        }

        return cella;
    }

    private static VBox costruisciBadgeTurnoInCella(TurnoBean t,
                                                    boolean isMaestro,
                                                    String emailUtenteLoggato,
                                                    boolean provenza,
                                                    Runnable onClick) {
        boolean isMio = emailUtenteLoggato != null && emailUtenteLoggato.equalsIgnoreCase(t.getEmailDipendente());
        String stato = t.getStato() != null ? t.getStato().toUpperCase() : "PUBBLICATO";

        VBox badge = new VBox(1);
        badge.setMaxWidth(Double.MAX_VALUE);
        String bgBadge;
        String borderBadge;
        String textColor;
        String indicatore;

        if (stato.contains("EMERGENZA")) {
            bgBadge = provenza ? "#fde8e8" : "rgba(70, 20, 25, 0.90)";
            borderBadge = "#ef4444";
            textColor = provenza ? "#b91c1c" : "#fca5a5";
            indicatore = "● Urgenza";
        } else if (stato.contains("SOSTITUIT")) {
            bgBadge = provenza ? "#e0f2fe" : "rgba(25, 45, 65, 0.90)";
            borderBadge = "#3b82f6";
            textColor = provenza ? "#1d4ed8" : "#93c5fd";
            indicatore = "● Sostituito";
        } else if (isMio && !isMaestro) {
            bgBadge = provenza ? "#e6f4ea" : "rgba(22, 55, 38, 0.90)";
            borderBadge = provenza ? "#387548" : "#10b981";
            textColor = provenza ? "#1e6b3a" : "#a7f3d0";
            indicatore = "★ Tuo turno";
        } else {
            bgBadge = provenza ? "#f4eaf5" : "rgba(42, 38, 50, 0.95)";
            borderBadge = provenza ? "#dfd0e3" : "rgba(186, 164, 135, 0.32)";
            textColor = provenza ? "#3f224d" : "#f8f1e7";
            indicatore = null;
        }

        String borderBlocco = t.isBloccato()
                ? "-fx-border-color: " + (provenza ? "#764c86" : "#baa487") + "; -fx-border-width: 1 1 1 3; "
                : "-fx-border-color: " + borderBadge + "; -fx-border-width: 1; ";

        badge.setStyle("-fx-background-color: " + bgBadge + "; " + borderBlocco
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 3 5; -fx-cursor: hand;");

        String nomeCompatto = formattaNomeBreve(t.getNomeDipendente());
        Label lblNome = new Label((t.isBloccato() ? "▪ " : "") + nomeCompatto);
        lblNome.setStyle("-fx-font-family: -apple-system, 'Helvetica Neue', 'Arial', sans-serif; "
                + "-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + textColor + ";");
        badge.getChildren().add(lblNome);

        if (t.isPersonalizzato()) {
            Label lblCustom = new Label(t.getOraInizio() + "–" + t.getOraFine());
            lblCustom.setStyle("-fx-font-family: -apple-system, 'Helvetica Neue', 'Arial', sans-serif; "
                    + "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: "
                    + (provenza ? "#6d427b;" : "#f0cf95;"));
            badge.getChildren().add(lblCustom);
        }

        if (indicatore != null) {
            Label lblStato = new Label(indicatore);
            lblStato.setStyle("-fx-font-family: -apple-system, 'Helvetica Neue', 'Arial', sans-serif; "
                    + "-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
            badge.getChildren().add(lblStato);
        }
        badge.setOnMouseClicked(e -> {
            e.consume();
            if (onClick != null) {
                onClick.run();
            }
        });
        return badge;
    }

    /**
     * Formatta il nome dipendente in forma compatta e leggibile (es. "Martina S.")
     * evitando troncamenti con ellissi nelle celle del calendario.
     */
    private static String formattaNomeBreve(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "Staff";
        }
        String[] parti = nomeCompleto.trim().split("\\s+");
        if (parti.length == 1 || nomeCompleto.trim().length() <= 11) {
            return nomeCompleto.trim();
        }
        return parti[0] + " " + Character.toUpperCase(parti[1].charAt(0)) + ".";
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
        // Se il turno corrisponde esattamente a un'altra fascia del catalogo, non duplicarlo
        if (model.DisponibilitaDipendente.identificaIndiceFascia(in, fi) >= 0) {
            return false;
        }
        // Turni personalizzati/flessibili: mappatura univoca per ora di inizio (visibili sempre con badge orario)
        if (in.compareTo("12:00") < 0) {
            return indiceFascia == 0;
        }
        if (in.compareTo("16:00") < 0) {
            return indiceFascia == 1;
        }
        if (in.compareTo("19:00") < 0) {
            return indiceFascia == 2;
        }
        return indiceFascia == 3;
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
        apriPopupGestioneIncrocioMaestro(owner, titoloGiornoEsteso, rangeFascia, turniAttualiCella,
                dipendentiFormattati, cercaEmergenzaPerTurno, null,
                onAssegnaDipendenteEmail, onRimuoviTurnoCodice, onApprovaEmergenza, onRifiutaEmergenza);
    }

    public static void apriPopupGestioneIncrocioMaestro(Window owner,
                                                        String titoloGiornoEsteso,
                                                        String rangeFascia,
                                                        List<TurnoBean> turniAttualiCella,
                                                        List<String> dipendentiFormattati,
                                                        java.util.function.Function<String, EmergenzaBean> cercaEmergenzaPerTurno,
                                                        java.util.function.Function<String, control.applicativo.CalcolatoreOreSettimanali.AnteprimaAssegnazione> anteprimaProvider,
                                                        Consumer<String> onAssegnaDipendenteEmail,
                                                        Consumer<String> onRimuoviTurnoCodice,
                                                        GestioneEmergenzaCallback onApprovaEmergenza,
                                                        BiConsumer<String, String> onRifiutaEmergenza) {
        apriPopupGestioneIncrocioMaestro(owner, titoloGiornoEsteso, rangeFascia, turniAttualiCella,
                dipendentiFormattati, cercaEmergenzaPerTurno, anteprimaProvider,
                (email, bloccato) -> {
                    if (onAssegnaDipendenteEmail != null) {
                        onAssegnaDipendenteEmail.accept(email);
                    }
                },
                null, onRimuoviTurnoCodice, onApprovaEmergenza, onRifiutaEmergenza);
    }

    public static void apriPopupGestioneIncrocioMaestro(Window owner,
                                                        String titoloGiornoEsteso,
                                                        String rangeFascia,
                                                        List<TurnoBean> turniAttualiCella,
                                                        List<String> dipendentiFormattati,
                                                        java.util.function.Function<String, EmergenzaBean> cercaEmergenzaPerTurno,
                                                        java.util.function.Function<String, control.applicativo.CalcolatoreOreSettimanali.AnteprimaAssegnazione> anteprimaProvider,
                                                        BiConsumer<String, Boolean> onAssegnaDipendenteConBlocco,
                                                        BiConsumer<String, Boolean> onToggleBloccoTurno,
                                                        Consumer<String> onRimuoviTurnoCodice,
                                                        GestioneEmergenzaCallback onApprovaEmergenza,
                                                        BiConsumer<String, String> onRifiutaEmergenza) {
        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Gestione Incrocio Turno — " + titoloGiornoEsteso);

        VBox root = new VBox(12);
        root.setPrefWidth(680);
        root.setMinWidth(660);
        root.setPadding(new Insets(18, 22, 18, 22));
        root.setStyle("-fx-background-color: rgba(18,17,20,0.97); -fx-border-color: rgba(186,164,135,0.40); -fx-border-width: 1.2; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label lblTitolo = new Label("📅 Gestione Incrocio Calendario Turni");
        lblTitolo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f5ebe0;");

        Label lblSottotitolo = new Label("🗓️ " + titoloGiornoEsteso + "   •   ⏰ Fascia: " + rangeFascia);
        lblSottotitolo.setMaxWidth(Double.MAX_VALUE);
        lblSottotitolo.setStyle("-fx-background-color: rgba(35,32,38,0.92); -fx-text-fill: #baa487; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-padding: 7 12; -fx-background-radius: 10; -fx-border-color: rgba(186,164,135,0.28); -fx-border-radius: 10;");

        Label lblPresenti = new Label("👥 Personale assegnato in questo incrocio:");
        lblPresenti.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f5ebe0; "
                + "-fx-background-color: rgba(24,22,28,0.75); -fx-padding: 4 10; -fx-background-radius: 8;");

        boolean haEmergenzaInCella = false;
        VBox boxPresenti = new VBox(8);
        boxPresenti.setStyle("-fx-padding: 2;");
        if (turniAttualiCella == null || turniAttualiCella.isEmpty()) {
            Label lblNessuno = new Label("Nessun dipendente assegnato in questa fascia. Usa il modulo sotto per inserirne uno.");
            lblNessuno.setWrapText(true);
            lblNessuno.setStyle("-fx-text-fill: #ded7cd; -fx-font-style: italic; -fx-font-size: 13px;");
            boxPresenti.getChildren().add(lblNessuno);
        } else {
            for (TurnoBean t : turniAttualiCella) {
                VBox cardAssegnato = new VBox(8);
                boolean inEmergenza = "IN_EMERGENZA".equalsIgnoreCase(t.getStato());
                if (inEmergenza) {
                    haEmergenzaInCella = true;
                }
                cardAssegnato.setStyle("-fx-background-color: " + (inEmergenza ? "rgba(65,20,25,0.92)" : "rgba(26,24,28,0.95)")
                        + "; -fx-border-color: " + (inEmergenza ? "#ef4444" : "rgba(186,164,135,0.28)")
                        + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 12;");

                HBox rigaTop = new HBox(8);
                rigaTop.setAlignment(Pos.CENTER_LEFT);

                Label lblInfo = new Label((t.isBloccato() ? "🔒 " : "👤 ") + t.getNomeDipendente()
                        + " (" + t.getEmailDipendente() + ") — [" + t.getCodiceTurno() + "]");
                lblInfo.setWrapText(true);
                lblInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f5ebe0;");
                HBox.setHgrow(lblInfo, Priority.ALWAYS);

                Region spTop = new Region();
                HBox.setHgrow(spTop, Priority.SOMETIMES);

                if (onToggleBloccoTurno != null) {
                    Button btnBlocco = new Button(t.isBloccato() ? "🔓 Sblocca" : "🔒 Blocca");
                    btnBlocco.setMinWidth(Region.USE_PREF_SIZE);
                    btnBlocco.setStyle("-fx-background-color: " + (t.isBloccato() ? "#5e316f" : "#3a3542")
                            + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; "
                            + "-fx-padding: 5 11; -fx-background-radius: 8; -fx-cursor: hand;");
                    btnBlocco.setOnAction(e -> {
                        onToggleBloccoTurno.accept(t.getCodiceTurno(), !t.isBloccato());
                        dialog.close();
                    });
                    rigaTop.getChildren().addAll(lblInfo, spTop, btnBlocco);
                } else {
                    rigaTop.getChildren().addAll(lblInfo, spTop);
                }

                Button btnRimuovi = new Button("🗑️ Rimuovi");
                btnRimuovi.setMinWidth(Region.USE_PREF_SIZE);
                btnRimuovi.setStyle("-fx-background-color: #bc4749; -fx-text-fill: white; -fx-font-weight: bold; "
                        + "-fx-font-size: 12px; -fx-padding: 5 11; -fx-background-radius: 8; -fx-cursor: hand;");
                btnRimuovi.setOnAction(e -> {
                    if (onRimuoviTurnoCodice != null) {
                        onRimuoviTurnoCodice.accept(t.getCodiceTurno());
                    }
                    dialog.close();
                });

                rigaTop.getChildren().add(btnRimuovi);
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
        scrollPresenti.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        int baseHeight = haEmergenzaInCella ? 225 : Math.max(90, (turniAttualiCella != null ? turniAttualiCella.size() : 1) * 76);
        scrollPresenti.setPrefHeight(Math.min(280, baseHeight));
        scrollPresenti.setMinHeight(haEmergenzaInCella ? 205 : 70);
        VBox.setVgrow(scrollPresenti, Priority.ALWAYS);
        scrollPresenti.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

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

        Label lblAnteprima = new Label();
        lblAnteprima.setWrapText(true);
        lblAnteprima.setVisible(false);
        lblAnteprima.setManaged(false);
        lblAnteprima.setStyle("-fx-font-size: 12px; -fx-padding: 8 10; -fx-background-radius: 8; -fx-border-radius: 8;");

        Runnable aggiornaAnteprima = () -> {
            if (anteprimaProvider == null || comboNuovoDip.getValue() == null) {
                lblAnteprima.setVisible(false);
                lblAnteprima.setManaged(false);
                return;
            }
            String emailSel = estraiEmail(comboNuovoDip.getValue());
            control.applicativo.CalcolatoreOreSettimanali.AnteprimaAssegnazione ant = anteprimaProvider.apply(emailSel);
            if (ant == null) {
                lblAnteprima.setVisible(false);
                lblAnteprima.setManaged(false);
                return;
            }
            String att = model.ConfigurazioneOreDipendente.formattaOre(ant.oreAttuali());
            String dur = model.ConfigurazioneOreDipendente.formattaOre(ant.oreTurno());
            String ris = model.ConfigurazioneOreDipendente.formattaOre(ant.oreRisultanti());
            String target = ant.configurazioneOre().getEtichettaObiettivo();
            StringBuilder sb = new StringBuilder();
            sb.append("Ore attuali: ").append(att).append(" h  •  Turno: +").append(dur)
              .append(" h  →  Risultanti: ").append(ris).append(" h (Obiettivo: ").append(target).append(")");
            if (ant.maxTurniSettimanali() != null) {
                sb.append("  •  Turni: ").append(ant.turniAttuali()).append("/").append(ant.maxTurniSettimanali());
            }
            if (ant.superaMaxTurni()) {
                sb.append("\n⛔ Limite massimo turni raggiunto (").append(ant.turniAttuali())
                  .append("/").append(ant.maxTurniSettimanali())
                  .append("): l'assegnazione non è consentita.");
            }
            if (ant.superaMassimo()) {
                sb.append("\n⚠️ L'assegnazione comporterebbe il superamento di ")
                  .append(model.ConfigurazioneOreDipendente.formattaOre(ant.oreEccesso()))
                  .append(" h del limite settimanale.");
            }
            if (ant.avvisoDisponibilita() != null) {
                sb.append("\n🚫 Disponibilità: ").append(ant.avvisoDisponibilita());
            }
            lblAnteprima.setText(sb.toString());
            boolean alert = ant.superaMaxTurni() || ant.superaMassimo() || ant.avvisoDisponibilita() != null;
            lblAnteprima.setStyle(alert
                    ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #fca5a5; -fx-background-color: rgba(85,22,28,0.85); -fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;"
                    : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #a7f3d0; -fx-background-color: rgba(22,55,38,0.85); -fx-border-color: #10b981; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;");
            lblAnteprima.setVisible(true);
            lblAnteprima.setManaged(true);
            TemaProvenzaHelper.applicaSeGui2(lblAnteprima);
        };
        comboNuovoDip.setOnAction(ev -> aggiornaAnteprima.run());
        aggiornaAnteprima.run();

        javafx.scene.control.CheckBox chkBloccaNuovo = new javafx.scene.control.CheckBox("🔒 Blocca assegnazione (protetta dalla generazione automatica)");
        chkBloccaNuovo.setStyle("-fx-text-fill: #ded7cd; -fx-font-size: 12px;");

        Button btnInserisci = new Button("➕ Assegna al Turno");
        btnInserisci.setStyle("-fx-background-color: #baa487; -fx-text-fill: #141312; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-padding: 7 16; -fx-background-radius: 8; -fx-cursor: hand;");
        btnInserisci.setOnAction(e -> {
            String selezionato = comboNuovoDip.getValue();
            if (selezionato != null && onAssegnaDipendenteConBlocco != null) {
                String emailSel = estraiEmail(selezionato);
                if (anteprimaProvider != null) {
                    control.applicativo.CalcolatoreOreSettimanali.AnteprimaAssegnazione ant = anteprimaProvider.apply(emailSel);
                    if (ant != null && ant.superaMaxTurni()) {
                        javafx.scene.control.Alert blocco = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                        blocco.initOwner(dialog);
                        blocco.setTitle("Limite Massimo Turni Raggiunto");
                        blocco.setHeaderText("Impossibile assegnare un altro turno a " + ant.nomeCompleto());
                        blocco.setContentText(ant.nomeCompleto() + " ha già raggiunto il limite massimo di "
                                + ant.maxTurniSettimanali() + " turni settimanali (" + ant.turniAttuali()
                                + "/" + ant.maxTurniSettimanali() + ").\nModifica il limite nel profilo dipendente se necessario.");
                        blocco.showAndWait();
                        return;
                    }
                    if (ant != null && (ant.superaMassimo() || ant.avvisoDisponibilita() != null)) {
                        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                        confirm.initOwner(dialog);
                        confirm.setTitle("Conferma Assegnazione Turno");
                        confirm.setHeaderText("Attenzione: rilevato vincolo per " + ant.nomeCompleto());
                        StringBuilder msg = new StringBuilder();
                        if (ant.superaMassimo()) {
                            msg.append("L'assegnazione comporterebbe il superamento di ")
                               .append(model.ConfigurazioneOreDipendente.formattaOre(ant.oreEccesso()))
                               .append(" h del limite settimanale (")
                               .append(model.ConfigurazioneOreDipendente.formattaOre(ant.oreRisultanti()))
                               .append(" / ").append(ant.configurazioneOre().getEtichettaObiettivo()).append(").\n");
                        }
                        if (ant.avvisoDisponibilita() != null) {
                            msg.append("Disponibilità incompatibile: ").append(ant.avvisoDisponibilita()).append(".\n");
                        }
                        msg.append("\nVuoi confermare comunque l'assegnazione?");
                        confirm.setContentText(msg.toString());
                        javafx.scene.control.ButtonType btnSi = new javafx.scene.control.ButtonType("Conferma comunque");
                        javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("Annulla", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                        confirm.getButtonTypes().setAll(btnSi, btnNo);
                        if (confirm.showAndWait().orElse(btnNo) != btnSi) {
                            return;
                        }
                    }
                }
                onAssegnaDipendenteConBlocco.accept(emailSel, chkBloccaNuovo.isSelected());
                dialog.close();
            }
        });

        rigaAggiungi.getChildren().addAll(comboNuovoDip, btnInserisci);
        boxAggiungi.getChildren().addAll(lblAggiungi, rigaAggiungi, chkBloccaNuovo, lblAnteprima);

        HBox boxBottom = new HBox();
        boxBottom.setAlignment(Pos.CENTER_RIGHT);
        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: rgba(38,35,42,0.85); -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-padding: 6 18; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(186,164,135,0.25);");
        btnChiudi.setOnAction(e -> dialog.close());
        boxBottom.getChildren().add(btnChiudi);

        root.getChildren().addAll(lblTitolo, lblSottotitolo, lblPresenti, scrollPresenti, sep, boxAggiungi, boxBottom);

        Scene scene = new Scene(root, 700, haEmergenzaInCella ? 590 : 470);
        dialog.setScene(scene);
        TemaProvenzaHelper.applicaSeGui2(dialog.getScene());
        dialog.centerOnScreen();
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
        TemaProvenzaHelper.applicaSeGui2(dialog.getScene());
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
