package control.grafico;

import bean.CarrelloBean;
import bean.DatiConsegnaBean;
import bean.EsitoPagamentoBean;
import bean.OrdineBean;
import bean.ScontrinoBean;
import bean.VaschettaItemBean;
import control.applicativo.GestioneMenuControl;
import control.applicativo.OrdiniControl;
import control.applicativo.PagamentoControl;
import control.applicativo.ScontrinoDigitaleService;
import frostly.exception.DatiNonValidiException;
import frostly.exception.FrostlyException;
import frostly.exception.IngredienteEsauritoException;
import frostly.exception.PagamentoException;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.stage.Window;
import util.SessionManager;
import view.CarrelloView;
import view.CheckoutConsegnaView;
import view.PagamentoOnlineView;
import view.RicevutaPagamentoView;
import view.ScontrinoHtmlFormatter;

/**
 * Controller Grafico dedicato esclusivamente al caso d'uso di Checkout,
 * scelta consegna/ritiro, pagamento online simulato e generazione dello scontrino
 */
public class CheckoutControlGrafico {

    private final CarrelloBean carrello;
    private final BiConsumer<String, Boolean> mostraEsito;
    private final Runnable onAggiornaStatoCarrello;
    private final Runnable onOrdineCompletato;
    private final Runnable onAggiornaCatalogo;
    private final Runnable onAggiungiVaschettaSePronta;

    public CheckoutControlGrafico(CarrelloBean carrello,
                                  BiConsumer<String, Boolean> mostraEsito,
                                  Runnable onAggiornaStatoCarrello,
                                  Runnable onOrdineCompletato,
                                  Runnable onAggiornaCatalogo,
                                  Runnable onAggiungiVaschettaSePronta) {
        this.carrello = carrello;
        this.mostraEsito = mostraEsito;
        this.onAggiornaStatoCarrello = onAggiornaStatoCarrello;
        this.onOrdineCompletato = onOrdineCompletato;
        this.onAggiornaCatalogo = onAggiornaCatalogo;
        this.onAggiungiVaschettaSePronta = onAggiungiVaschettaSePronta;
    }

    public boolean verificaLimiteDisponibilitaPerProdotto(String nomeProdotto) {
        int maxDisp = new GestioneMenuControl().calcolaMaxOrdinabile(nomeProdotto);
        if (maxDisp < 0) {
            return true;
        }
        if (maxDisp == 0) {
            onAggiornaCatalogo.run();
            mostraEsito.accept("⚠️ '" + nomeProdotto + "' è momentaneamente esaurito.", true);
            return false;
        }
        int giaNelCarrello = contaQuantitaProdottoNelCarrello(nomeProdotto);
        if (giaNelCarrello >= maxDisp) {
            mostraEsito.accept("⚠️ Disponibilità massima raggiunta per '" + nomeProdotto + "' (max " + maxDisp + " ordinabili).", true);
            return false;
        }
        return true;
    }

    public void apriCarrello(Window owner) {
        CarrelloView.apriPopupCarrello(
                owner,
                carrello,
                idx -> {
                    List<VaschettaItemBean> lista = carrello.getArticoli();
                    if (idx >= 0 && idx < lista.size()) {
                        VaschettaItemBean item = lista.get(idx);
                        if (item.ottieniPesoKg() <= 0.0 && !item.ottieniGusti().isEmpty()
                                && !verificaLimiteDisponibilitaPerProdotto(item.ottieniGusti().get(0))) {
                            return;
                        }
                    }
                    GestoreCarrello.incrementaArticolo(carrello, idx);
                    onAggiornaStatoCarrello.run();
                },
                idx -> {
                    GestoreCarrello.decrementaArticolo(carrello, idx);
                    onAggiornaStatoCarrello.run();
                },
                idx -> {
                    GestoreCarrello.rimuoviArticolo(carrello, idx);
                    onAggiornaStatoCarrello.run();
                },
                () -> {
                    GestoreCarrello.svuota(carrello);
                    onAggiornaStatoCarrello.run();
                },
                (tipo, orario, note) -> avviaFlussoConsegnaEPagamento(tipo, owner));
    }

    public void avviaFlussoConsegnaEPagamento(String tipoSelezionato, Window owner) {
        if (GestoreCarrello.isVuoto(carrello)) {
            onAggiungiVaschettaSePronta.run();
            if (GestoreCarrello.isVuoto(carrello)) {
                mostraEsito.accept("⚠️ Aggiungi almeno un prodotto o una vaschetta al carrello prima del checkout.", true);
                return;
            }
        }

        DatiConsegnaBean datiIniziali = new DatiConsegnaBean();
        datiIniziali.setTipoConsegna(DatiConsegnaBean.TipoConsegna.daStringa(tipoSelezionato));
        apriSceltaConsegna(datiIniziali, false, owner);
    }

    private void apriSceltaConsegna(DatiConsegnaBean datiIniziali, boolean apriDirettamenteForm, Window owner) {
        CheckoutConsegnaView.apriPopupSceltaConsegna(
                owner,
                GestoreCarrello.calcolaTotale(carrello),
                datiIniziali,
                apriDirettamenteForm,
                bean -> {
                    try {
                        bean.validaSintassi();
                    } catch (DatiNonValidiException ex) {
                        throw new IllegalArgumentException(ex.getMessage(), ex);
                    }
                },
                datiConfermati -> avviaFlussoPagamento(datiConfermati, owner),
                () -> apriCarrello(owner)
        );
    }

    private void avviaFlussoPagamento(DatiConsegnaBean datiConfermati, Window owner) {
        PagamentoOnlineView.apriPopupPagamento(
                owner,
                GestoreCarrello.calcolaTotale(carrello),
                datiConfermati,
                cartaBean -> elaboraPagamentoEOrdine(cartaBean, datiConfermati, owner),
                () -> apriSceltaConsegna(datiConfermati, true, owner)
        );
    }

    private void elaboraPagamentoEOrdine(bean.CartaPagamentoBean cartaBean, DatiConsegnaBean datiConfermati, Window owner) {
        try {
            // Validazione sintattica sul livello di presentazione/Bean prima di invocare i controller applicativi
            datiConfermati.validaSintassi();
            cartaBean.validaSintassi();

            String emailCliente = SessionManager.getInstance().getEmailUtenteLoggato();
            OrdiniControl ordiniControl = new OrdiniControl();

            // Pre-verifica atomica di gusti e scorte PRIMA di autorizzare o salvare il pagamento
            carrello.setModalitaConsegna(datiConfermati.getTipoConsegna().getLabel());
            carrello.setFasciaOraria(datiConfermati.getOrarioRichiesto());
            carrello.setIndirizzoONote(datiConfermati.costruisciStringaRiepilogoConsegna());
            OrdineBean ordineVerifica = GestoreCarrello.costruisciOrdineBean(carrello, emailCliente);
            try {
                ordiniControl.verificaDisponibilitaOrdine(ordineVerifica);
            } catch (FrostlyException exDisponibilita) {
                onAggiornaCatalogo.run();
                String msgCliente = (exDisponibilita instanceof IngredienteEsauritoException)
                        ? "Ci dispiace, uno o più prodotti del tuo carrello non sono più disponibili nella quantità richiesta. "
                          + "Nessun addebito è stato effettuato: riduci la quantità nel carrello o scegli un'altra specialità."
                        : ("Ordine non completato (nessun addebito effettuato): " + exDisponibilita.getMessage());
                EsitoPagamentoBean esitoAnnullato = new EsitoPagamentoBean(
                        false, "", msgCliente, GestoreCarrello.calcolaTotale(carrello), "", "", "");
                Platform.runLater(() -> RicevutaPagamentoView.mostraRicevuta(owner, esitoAnnullato, null, null));
                return;
            }

            PagamentoControl payCtrl = new PagamentoControl();
            EsitoPagamentoBean esito = payCtrl.elaboraPagamento(
                    cartaBean,
                    GestoreCarrello.calcolaTotale(carrello),
                    datiConfermati,
                    emailCliente
            );

            if (esito.isSuccesso()) {
                try {
                    ScontrinoBean scontrino = finalizzaOrdinePagato(datiConfermati, esito);
                    Platform.runLater(() -> RicevutaPagamentoView.mostraRicevuta(
                            owner, esito, scontrino.getCodiceOrdine(), scontrino.getUrlScontrino()));
                } catch (FrostlyException exOrdine) {
                    onAggiornaCatalogo.run();
                    String msgCliente = (exOrdine instanceof IngredienteEsauritoException)
                            ? "Ci dispiace, uno o più prodotti del tuo carrello non sono più disponibili nella quantità richiesta. "
                              + "Nessun addebito è stato effettuato: riduci la quantità nel carrello o scegli un'altra specialità."
                            : ("Ordine non completato (nessun addebito effettuato): " + exOrdine.getMessage());
                    EsitoPagamentoBean esitoAnnullato = new EsitoPagamentoBean(
                            false, "", msgCliente, GestoreCarrello.calcolaTotale(carrello), "", "", "");
                    Platform.runLater(() -> RicevutaPagamentoView.mostraRicevuta(owner, esitoAnnullato, null, null));
                }
            } else {
                Platform.runLater(() -> RicevutaPagamentoView.mostraRicevuta(owner, esito, null, null));
            }
        } catch (PagamentoException e) {
            EsitoPagamentoBean esitoRifiutato = new EsitoPagamentoBean(
                    false, "", e.getMessage(), GestoreCarrello.calcolaTotale(carrello), "", "", "");
            Platform.runLater(() -> RicevutaPagamentoView.mostraRicevuta(owner, esitoRifiutato, null, null));
        } catch (FrostlyException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private ScontrinoBean finalizzaOrdinePagato(DatiConsegnaBean datiConfermati, EsitoPagamentoBean esito) throws FrostlyException {
        carrello.setModalitaConsegna(datiConfermati.getTipoConsegna().getLabel());
        carrello.setFasciaOraria(datiConfermati.getOrarioRichiesto());
        carrello.setIndirizzoONote(datiConfermati.costruisciStringaRiepilogoConsegna()
                + " [Transazione Online: " + esito.getCodiceTransazione() + " • " + esito.getCircuito() + " " + esito.getCartaMascherata() + "]");

        String email = SessionManager.getInstance().getEmailUtenteLoggato();
        String nomeCliente = SessionManager.getInstance().getNomeUtenteLoggato();
        OrdineBean bean = GestoreCarrello.costruisciOrdineBean(carrello, email);
        OrdineBean confermato = new OrdiniControl().effettuaPreOrdine(bean);

        ScontrinoBean scontrino = new ScontrinoBean();
        scontrino.setCodiceOrdine(confermato.getCodiceOrdine());
        scontrino.setCodiceTransazione(esito.getCodiceTransazione());
        scontrino.setNomeCliente(nomeCliente != null && !nomeCliente.isBlank() ? nomeCliente : email);
        scontrino.setDataOra(esito.getDataOra());
        scontrino.setModalitaConsegna(datiConfermati.costruisciStringaRiepilogoConsegna());
        scontrino.setDettaglioProdotti(confermato.getDescrizioneCompleta());
        scontrino.setImportoTotale(esito.getImporto());
        scontrino.setCircuito(esito.getCircuito());
        scontrino.setCartaMascherata(esito.getCartaMascherata());
        ScontrinoDigitaleService.getInstance().registraScontrino(scontrino, ScontrinoHtmlFormatter::costruisciHtmlScontrino);

        onOrdineCompletato.run();
        return scontrino;
    }

    private int contaQuantitaProdottoNelCarrello(String nomeProdotto) {
        if (nomeProdotto == null || nomeProdotto.isBlank()) return 0;
        int totale = 0;
        for (VaschettaItemBean item : carrello.getArticoli()) {
            if (item.ottieniPesoKg() <= 0.0 && !item.ottieniGusti().isEmpty()
                    && nomeProdotto.equalsIgnoreCase(item.ottieniGusti().get(0))) {
                totale += item.getQuantita();
            }
        }
        return totale;
    }
}
