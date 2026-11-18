package view;

import bean.RecensioneBean;
import control.applicativo.RecensioniControl;
import frostly.exception.FrostlyException;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Componente View dedicato esclusivamente alla rappresentazione grafica ed estetica
 * delle Recensioni, nel pieno rispetto dell'architettura BCE:
 * - Card cliente con voto a stelle, commento e badge risposta Maestro
 * - Pop-up modale per consentire al Maestro di rispondere a una recensione
 * - Finestra pubblica di consultazione (senza login), con blocco interazioni per utenti non autenticati.
 */
public final class RecensioniView {

    private RecensioniView() {
        // Utility View
    }

    public static VBox buildCardRecensione(RecensioneBean r,
                                           boolean isAutenticato,
                                           boolean isMaestro,
                                           Consumer<RecensioneBean> onMiPiace,
                                           Consumer<RecensioneBean> onRispondi,
                                           Runnable onRichiediLogin) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: rgba(28, 25, 33, 0.88); -fx-background-radius: 12; -fx-padding: 14 18; "
                + "-fx-border-color: rgba(186, 164, 135, 0.28); -fx-border-radius: 12; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0, 0, 2);");

        // Intestazione: Nome Autore + Stelle + Data
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblAvatar = new Label("👤");
        lblAvatar.setStyle("-fx-font-size: 15px; -fx-text-fill: #baa487;");

        Label lblNome = new Label(r.getAutoreNome());
        lblNome.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #f5ebe0;");

        Label lblStelle = new Label(r.getStelleIcone());
        lblStelle.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #baa487; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblData = new Label(r.getDataOra() != null ? r.getDataOra() : "");
        lblData.setStyle("-fx-font-size: 11px; -fx-text-fill: #a89f92; -fx-font-style: italic;");

        header.getChildren().addAll(lblAvatar, lblNome, lblStelle, spacer, lblData);

        // Titolo
        Label lblTitolo = new Label(r.getTitolo());
        lblTitolo.setWrapText(true);
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #baa487;");

        // Commento
        Label lblCommento = new Label(r.getCommento());
        lblCommento.setWrapText(true);
        lblCommento.setStyle("-fx-font-size: 12px; -fx-text-fill: #ded7cd; -fx-line-spacing: 2.2;");

        // Barra Interazioni (Mi Piace / Rispondi)
        HBox rigaInterazioni = new HBox(10);
        rigaInterazioni.setAlignment(Pos.CENTER_LEFT);

        Button btnLike = new Button("👍 Utile (" + r.getMiPiace() + ")");
        if (isAutenticato) {
            btnLike.setStyle("-fx-background-color: rgba(186, 164, 135, 0.15); -fx-text-fill: #baa487; -fx-font-weight: bold; "
                    + "-fx-font-size: 11px; -fx-background-radius: 12; -fx-border-color: rgba(186, 164, 135, 0.35); "
                    + "-fx-border-radius: 12; -fx-padding: 4 11; -fx-cursor: hand;");
            if (onMiPiace != null) {
                btnLike.setOnAction(e -> onMiPiace.accept(r));
            }
        } else {
            // Utente non loggato: non può interagire
            btnLike.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: #a89f92; -fx-font-weight: bold; "
                    + "-fx-font-size: 11px; -fx-background-radius: 12; -fx-border-color: rgba(255, 255, 255, 0.12); "
                    + "-fx-border-radius: 12; -fx-padding: 4 11; -fx-cursor: hand;");
            btnLike.setOnAction(e -> {
                mostraAvvisoLoginNecessario(card.getScene() != null ? card.getScene().getWindow() : null, onRichiediLogin);
            });
        }
        rigaInterazioni.getChildren().add(btnLike);

        if (isMaestro && onRispondi != null) {
            Button btnRispondi = new Button(r.haRispostaMaestro() ? "✏️ Modifica Risposta Maestro" : "💬 Rispondi come Maestro");
            btnRispondi.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; "
                    + "-fx-font-size: 11px; -fx-background-radius: 12; -fx-padding: 4 12; -fx-cursor: hand;");
            btnRispondi.setOnAction(e -> onRispondi.accept(r));
            rigaInterazioni.getChildren().add(btnRispondi);
        }

        card.getChildren().addAll(header, lblTitolo, lblCommento, rigaInterazioni);

        // Eventuale Risposta Ufficiale del Maestro Gelataio
        if (r.haRispostaMaestro()) {
            VBox boxRisposta = new VBox(4);
            boxRisposta.setStyle("-fx-background-color: rgba(38, 33, 44, 0.85); -fx-border-color: #baa487; -fx-border-width: 1 1 1 3; "
                    + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10 12;");

            HBox rigaMaestro = new HBox(6);
            rigaMaestro.setAlignment(Pos.CENTER_LEFT);

            Label lblBadgeMaestro = new Label("👩‍🍳 Risposta della Maestra Gelataia (Stefania Guga)");
            lblBadgeMaestro.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #baa487;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label lblDataRisp = new Label(r.getDataRisposta() != null ? r.getDataRisposta() : "");
            lblDataRisp.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #a89f92; -fx-font-style: italic;");

            rigaMaestro.getChildren().addAll(lblBadgeMaestro, sp, lblDataRisp);

            Label lblTestoRisposta = new Label(r.getRispostaMaestro());
            lblTestoRisposta.setWrapText(true);
            lblTestoRisposta.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #f5ebe0;");

            boxRisposta.getChildren().addAll(rigaMaestro, lblTestoRisposta);
            card.getChildren().add(boxRisposta);
        }

        return card;
    }

    public static void apriPopupRispostaMaestro(Window owner,
                                                RecensioneBean recensione,
                                                Consumer<String> onInviaRisposta) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Risposta Ufficiale del Maestro Gelataio");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setStyle("-fx-padding: 24 28; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");
        root.setPrefWidth(580);

        Label lblTitolo = new Label("👨‍🍳 RISPOSTA DEL MAESTRO GELATAIO");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #f5ebe0;");

        // Anteprima della recensione del cliente
        VBox boxPreview = new VBox(6);
        boxPreview.setStyle("-fx-background-color: rgba(26,24,28,0.85); -fx-border-color: rgba(186,164,135,0.25); -fx-border-radius: 10; "
                + "-fx-background-radius: 10; -fx-padding: 12 16;");

        HBox rigaAutore = new HBox(8);
        rigaAutore.setAlignment(Pos.CENTER_LEFT);
        Label lblAutore = new Label(recensione.getAutoreNome());
        lblAutore.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f5ebe0;");
        Label lblStelle = new Label(recensione.getStelleIcone());
        lblStelle.setStyle("-fx-font-size: 13px; -fx-text-fill: #baa487; -fx-font-weight: bold;");
        rigaAutore.getChildren().addAll(lblAutore, lblStelle);

        Label lblTitRec = new Label("\"" + recensione.getTitolo() + "\"");
        lblTitRec.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #baa487;");

        Label lblCommRec = new Label(recensione.getCommento());
        lblCommRec.setWrapText(true);
        lblCommRec.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #ded7cd;");

        boxPreview.getChildren().addAll(rigaAutore, lblTitRec, lblCommRec);

        // Campo di testo per la risposta
        Label lblGuidaRisposta = new Label("Scrivi la risposta pubblica ufficiale del Maestro (visibile a tutti i clienti):");
        lblGuidaRisposta.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #baa487;");

        TextArea txtRisposta = new TextArea();
        txtRisposta.setPromptText("Gentile " + recensione.getAutoreNome() + ", ti ringraziamo di cuore per la tua visita e per le splendide parole...");
        txtRisposta.setWrapText(true);
        txtRisposta.setPrefRowCount(5);
        txtRisposta.setStyle("-fx-control-inner-background: #1c1921; -fx-text-fill: #f5ebe0; -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 8; -fx-background-radius: 8;");
        if (recensione.haRispostaMaestro()) {
            txtRisposta.setText(recensione.getRispostaMaestro());
        } else {
            txtRisposta.setText("Gentile " + recensione.getAutoreNome() + ", ti ringraziamo di cuore! ");
        }

        // Footer pulsanti
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnulla = new Button("Annulla");
        btnAnnulla.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; -fx-border-color: rgba(255,255,255,0.2); "
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 7 16; -fx-cursor: hand;");
        btnAnnulla.setOnAction(e -> popup.close());

        Button btnPubblica = new Button("💬 Pubblica Risposta Ufficiale");
        btnPubblica.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; "
                + "-fx-font-size: 12px; -fx-background-radius: 10; -fx-padding: 7 18; -fx-cursor: hand;");
        btnPubblica.setOnAction(e -> {
            String testo = txtRisposta.getText();
            popup.close();
            if (onInviaRisposta != null) {
                onInviaRisposta.accept(testo);
            }
        });

        footer.getChildren().addAll(btnAnnulla, btnPubblica);

        root.getChildren().addAll(lblTitolo, boxPreview, lblGuidaRisposta, txtRisposta, footer);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    public static void apriPopupRecensioniPubbliche(Window owner, Runnable onRichiediLogin) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Recensioni dei Clienti (Accesso Libero)");
        if (owner != null) {
            popup.initOwner(owner);
        }

        VBox root = new VBox(12);
        root.setStyle("-fx-padding: 24 28; -fx-background-color: #141216; -fx-border-color: rgba(186,164,135,0.4); -fx-border-width: 1;");
        root.setPrefWidth(680);
        root.setPrefHeight(620);

        Label lblTitolo = new Label("⭐ RECENSIONI DEI CLIENTI FROSTLY");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 19px; -fx-text-fill: #f5ebe0;");

        Label lblSotto = new Label("La parola ai veri amanti del gelato artigianale dal 1984");
        lblSotto.setStyle("-fx-font-size: 12px; -fx-text-fill: #baa487;");

        // Banner per non loggati
        HBox bannerCrea = new HBox(12);
        bannerCrea.setAlignment(Pos.CENTER_LEFT);
        bannerCrea.setStyle("-fx-background-color: rgba(28,25,33,0.85); -fx-border-color: rgba(186,164,135,0.3); -fx-border-radius: 10; "
                + "-fx-background-radius: 10; -fx-padding: 10 16;");

        VBox testiBanner = new VBox(2);
        Label lblBannerTit = new Label("✍️ Vuoi lasciare o votare una recensione?");
        lblBannerTit.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #f5ebe0;");
        Label lblBannerSub = new Label("Per interagire con le recensioni o pubblicarne una è necessario registrarsi o accedere.");
        lblBannerSub.setWrapText(true);
        lblBannerSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #ded7cd;");
        testiBanner.getChildren().addAll(lblBannerTit, lblBannerSub);
        HBox.setHgrow(testiBanner, Priority.ALWAYS);

        Button btnAccedi = new Button("Accedi o Registrati ➔");
        btnAccedi.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; "
                + "-fx-font-size: 11.5px; -fx-background-radius: 10; -fx-padding: 6 14; -fx-cursor: hand;");
        btnAccedi.setOnAction(e -> {
            popup.close();
            if (onRichiediLogin != null) {
                onRichiediLogin.run();
            }
        });
        bannerCrea.getChildren().addAll(testiBanner, btnAccedi);

        // Contenitore Recensioni
        VBox listaCards = new VBox(10);
        listaCards.setStyle("-fx-padding: 4 2;");

        RecensioniControl ctrl = new RecensioniControl();
        Runnable ricaricaCards = () -> {
            listaCards.getChildren().clear();
            try {
                List<RecensioneBean> lista = ctrl.visualizzaTutteRecensioni();
                double media = ctrl.calcolaMediaVoti(lista);
                lblSotto.setText(String.format("Valutazione Media: ★ %.1f / 5.0 (Basata su %d recensioni verificate)", media, lista.size()));
                for (RecensioneBean r : lista) {
                    // Non autenticato: isAutenticato = false
                    VBox card = buildCardRecensione(r, false, false, null, null, () -> {
                        mostraAvvisoLoginNecessario(popup, onRichiediLogin);
                    });
                    listaCards.getChildren().add(card);
                }
            } catch (FrostlyException ignored) {}
        };
        ricaricaCards.run();

        ScrollPane scroll = new ScrollPane(listaCards);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; "
                + "-fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-padding: 7 20; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());

        HBox footer = new HBox(btnChiudi);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitolo, lblSotto, bannerCrea, scroll, footer);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    private static void mostraAvvisoLoginNecessario(Window owner, Runnable onRichiediLogin) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Frostly — Accesso Richiesto");
        if (owner != null) {
            popup.initOwner(owner);
        }
        popup.setResizable(false);

        VBox root = new VBox(14);
        root.setStyle("-fx-padding: 24 26 20 26; -fx-background-color: #141216; -fx-border-color: rgba(186, 164, 135, 0.4); "
                + "-fx-border-width: 1.5; -fx-border-radius: 12; -fx-background-radius: 12;");
        root.setPrefWidth(480);

        // Header con badge icona ed eleganza tipografica
        HBox headerBox = new HBox(14);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconBadge = new Label("✨");
        iconBadge.setStyle("-fx-font-size: 22px; -fx-background-color: rgba(186,164,135,0.15); -fx-padding: 8 12; "
                + "-fx-background-radius: 50; -fx-border-color: rgba(186,164,135,0.35); -fx-border-radius: 50; -fx-border-width: 1;");

        VBox titleBox = new VBox(2);
        Label lblBrand = new Label("FROSTLY — HAUTE GELATERIA & SALON DE THÉ");
        lblBrand.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #baa487; -fx-letter-spacing: 1px;");

        Label lblTitolo = new Label("Autenticazione Necessaria");
        lblTitolo.setStyle("-fx-font-family: 'Cinzel', 'Playfair Display', 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #f5ebe0;");
        titleBox.getChildren().addAll(lblBrand, lblTitolo);

        headerBox.getChildren().addAll(iconBadge, titleBox);

        // Card centrale esplicativa
        VBox cardInfo = new VBox(8);
        cardInfo.setStyle("-fx-background-color: rgba(28,25,33,0.85); -fx-border-color: rgba(186,164,135,0.22); -fx-border-radius: 10; "
                + "-fx-background-radius: 10; -fx-padding: 14 16;");

        Label lblMessaggio = new Label("Per esprimere gradimento («Utile») o per scrivere una recensione alla Maestra Gelataia Stefania Guga è necessario accedere con il proprio account.");
        lblMessaggio.setWrapText(true);
        lblMessaggio.setStyle("-fx-font-size: 12px; -fx-text-fill: #ded7cd; -fx-line-spacing: 2.2px;");

        Label lblTip = new Label("💡 Se non hai ancora un profilo, puoi registrarti gratuitamente in pochi istanti.");
        lblTip.setWrapText(true);
        lblTip.setStyle("-fx-font-size: 11px; -fx-text-fill: #baa487; -fx-font-style: italic;");

        cardInfo.getChildren().addAll(lblMessaggio, lblTip);

        // Footer pulsanti azione
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-padding: 4 0 0 0;");

        Button btnChiudi = new Button("Continua come Ospite");
        btnChiudi.setStyle("-fx-background-color: transparent; -fx-text-fill: #ded7cd; -fx-font-weight: bold; "
                + "-fx-font-size: 11.5px; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10; -fx-padding: 8 18; -fx-cursor: hand;");
        btnChiudi.setOnAction(e -> popup.close());

        Button btnAccedi = new Button("Accedi o Registrati ➔");
        btnAccedi.setStyle("-fx-background-color: linear-gradient(to bottom, #baa487, #8c7355); -fx-text-fill: #141216; -fx-font-weight: bold; "
                + "-fx-font-size: 11.5px; -fx-background-radius: 10; -fx-padding: 8 20; -fx-cursor: hand;");
        btnAccedi.setOnAction(e -> {
            popup.close();
            if (owner instanceof Stage stage) {
                stage.close();
            }
            if (onRichiediLogin != null) {
                onRichiediLogin.run();
            }
        });

        footer.getChildren().addAll(btnChiudi, btnAccedi);
        root.getChildren().addAll(headerBox, cardInfo, footer);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }
}
