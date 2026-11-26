package testing;

import static org.junit.jupiter.api.Assertions.*;

import bean.DettaglioAvvisoOrdineBean;
import bean.DettaglioAvvisoOrdineBean.RigaArticoloOrdine;
import bean.DettaglioAvvisoOrdineBean.TipoAvviso;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.VaschettaItemBean;
import java.util.List;
import org.junit.jupiter.api.Test;
import view.DettaglioAvvisoResolver;

class DettaglioAvvisoResolverTest {

    @Test
    void testOrdineSingoloProdottoRitiroAlBancoEPagamentoOnline() throws Exception {
        OrdineBean ordine = new OrdineBean();
        ordine.setCodiceOrdine("ORD-22");
        ordine.setNomeCliente("stefania guga");
        ordine.setEmailCliente("ste@frostly.it");
        ordine.setStato("IN_ATTESA");
        ordine.setPrezzoTotale(5.50);
        ordine.setDescrizioneCompleta("Crepe alla Nutella (Topping Cioccolato Bianco • Senza granella)");
        ordine.setDataRitiro("🛍️ Ritiro al Banco (Subito (~25-35 min)) • Indirizzo/Note: Ritiro in Gelateria (Banco) "
                + "• Orario: Subito (~25-35 min) • Ritiro: Stefania Guga • Tel: 3206860705 "
                + "[Transazione Online: TRX-20261123-133846-2535 • VISA •••• •••• •••• 5671]");

        NotificaBean notifica = new NotificaBean(
                "🛵 Nuovo Ordine ORD-22 da Confermare",
                "Testo grezzo notifica",
                "Oggi 13:38");

        DettaglioAvvisoOrdineBean det = DettaglioAvvisoResolver.risolvi(notifica, cod -> "ORD-22".equals(cod) ? ordine : null);

        assertTrue(det.isOrdineRealeTrovato());
        assertEquals(TipoAvviso.ORDINE, det.getTipoAvviso());
        assertEquals("ORD-22", det.getCodiceOrdine());
        assertEquals("Nuovo Ordine ORD-22", det.getTitoloPulito());
        assertEquals("Da confermare", det.getStatoEtichetta());
        assertEquals("Stefania Guga", det.getNomeCliente());
        assertEquals("3206860705", det.getTelefonoCliente());
        assertTrue(det.isRitiroAlBanco());
        assertEquals("Ritiro al banco", det.getModalitaLabel());
        assertEquals("Subito (~25-35 min)", det.getOrarioRichiesto());
        assertEquals("Pagamento online", det.getMetodoPagamento());
        assertEquals("Pagato", det.getStatoPagamento());
        assertEquals("VISA •••• 5671", det.getDettaglioCartaMascherata());
        assertEquals("TRX-20261123-133846-2535", det.getCodiceTransazione());
        assertEquals("€ 5,50", det.getTotaleFormattato());

        assertEquals(1, det.getArticoli().size());
        RigaArticoloOrdine riga = det.getArticoli().get(0);
        assertEquals(1, riga.getQuantita());
        assertEquals("Crepe alla Nutella", riga.getNomeProdotto());
        assertEquals(List.of("Cioccolato Bianco"), riga.getTopping());
        assertEquals(List.of("Senza granella"), riga.getEsclusioni());
        assertEquals(5.50, riga.getSubtotaleRiga());
    }

    @Test
    void testOrdineCinqueProdottiStrutturatiConPrezziESubtotali() throws Exception {
        OrdineBean ordine = new OrdineBean();
        ordine.setCodiceOrdine("ORD-30");
        ordine.setNomeCliente("Marco Bianchi");
        ordine.setStato("IN_ATTESA");
        ordine.setDataRitiro("🛵 Consegna a Domicilio (19:30) • Indirizzo/Note: Consegna a Domicilio "
                + "• Indirizzo: Via del Corso 124, Roma (RM) • Tel: 3331234567 • Orario: 19:30 • Note: Citofono Bianchi scala B");

        ordine.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Crepe alla Nutella"), false, false,
                "Crepe alla Nutella (Topping Cioccolato Bianco • Senza granella)", 5.50, 2));
        ordine.aggiungiVaschetta(new VaschettaItemBean(0.5, List.of("Pistacchio", "Nocciola"), true, true,
                "Vaschetta 500g (Pistacchio, Nocciola)", 14.00, 1));
        ordine.aggiungiVaschetta(new VaschettaItemBean(0.0, List.of("Cappuccino"), false, false,
                "Cappuccino (Latte di avena • Nota: Molto caldo con schiuma)", 2.50, 2));
        ordine.setPrezzoTotale(30.00);

        NotificaBean notifica = new NotificaBean("🛵 Nuovo Ordine ORD-30 da Confermare", "", "Oggi 18:10");
        DettaglioAvvisoOrdineBean det = DettaglioAvvisoResolver.risolvi(notifica, cod -> ordine);

        assertFalse(det.isRitiroAlBanco());
        assertEquals("Consegna a domicilio", det.getModalitaLabel());
        assertEquals("Via del Corso 124, Roma (RM)", det.getLuogoOIndirizzo());
        assertEquals("Citofono Bianchi scala B", det.getNoteCliente());
        assertEquals(3, det.getArticoli().size());
        assertEquals(5, det.getTotalePezzi());
        assertEquals(11.00, det.getArticoli().get(0).getSubtotaleRiga());
        assertEquals(5.50, det.getArticoli().get(0).getPrezzoUnitario());
        assertTrue(det.getArticoli().get(1).getAggiunte().contains("Panna montata"));
        assertTrue(det.getArticoli().get(1).getAggiunte().contains("Granella di pistacchio di Bronte"));
        assertEquals("Molto caldo con schiuma", det.getArticoli().get(2).getNoteProdotto());
    }

    @Test
    void testOrdineMoltoLungoCon35ProdottiENomiPersonalizzazioniLunghe() throws Exception {
        OrdineBean ordine = new OrdineBean();
        ordine.setCodiceOrdine("ORD-99");
        ordine.setNomeCliente("Evento Aziendale Frostly");
        ordine.setStato("IN_ATTESA");
        ordine.setDataRitiro("🛵 Consegna a Domicilio (20:00) • Indirizzo/Note: Indirizzo: Viale Europa 55, Roma (RM) "
                + "• Note: Ingresso laterale portineria, chiamare cinque minuti prima dell'arrivo perché il cancello si apre solo da dentro");

        StringBuilder desc = new StringBuilder();
        for (int i = 1; i <= 35; i++) {
            if (i > 1) desc.append(" | ");
            desc.append("Prodotto Artigianale Speciale Edizione Limitata #").append(i)
                    .append(" (Topping Pistacchio di Bronte • + Panna montata • Senza lattosio • Nota: Preparare separatamente per allergia nocciole numero ")
                    .append(i).append(")");
        }
        ordine.setDescrizioneCompleta(desc.toString());
        ordine.setPrezzoTotale(245.00);

        NotificaBean notifica = new NotificaBean("Nuovo Ordine ORD-99 da Confermare", "", "Oggi 19:00");
        DettaglioAvvisoOrdineBean det = DettaglioAvvisoResolver.risolvi(notifica, cod -> ordine);

        assertEquals(35, det.getArticoli().size());
        assertEquals(35, det.getTotalePezzi());
        assertEquals("€ 245,00", det.getTotaleFormattato());
        assertTrue(det.getArticoli().get(34).getNoteProdotto().contains("numero 35"));
    }

    @Test
    void testStatiOrdineDatiMancantiESicurezzaCarta() throws Exception {
        OrdineBean annullato = new OrdineBean();
        annullato.setCodiceOrdine("ORD-12");
        annullato.setStato("RIFIUTATA");
        annullato.setPrezzoTotale(12.00);
        annullato.setDescrizioneCompleta("Vaschetta 500g (Cioccolato)");
        annullato.setDataRitiro("Ritiro al Banco • Pagamento in contanti");

        DettaglioAvvisoOrdineBean detAnn = DettaglioAvvisoResolver.risolvi(
                new NotificaBean("Ordine ORD-12", "", "Ieri 18:00"), cod -> annullato);

        assertEquals("Annullato", detAnn.getStatoEtichetta());
        assertNull(detAnn.getNomeCliente());
        assertNull(detAnn.getTelefonoCliente());
        assertEquals("Contanti", detAnn.getMetodoPagamento());

        // Verifica mascheratura PAN completo
        String mascherata = DettaglioAvvisoResolver.normalizzaCartaMascherata("VISA 4532 1111 2222 5671");
        assertFalse(mascherata.contains("4532"));
        assertTrue(mascherata.contains("5671"));
    }

    @Test
    void testNotificheNonOrdineEStoricheSenzaOrdine() {
        NotificaBean generica = new NotificaBean("📢 Comunicazione di servizio", "Pulizia mantecatori alle 22:00", "Oggi 10:00");
        DettaglioAvvisoOrdineBean detGen = DettaglioAvvisoResolver.risolvi(generica, cod -> null);
        assertEquals(TipoAvviso.GENERICO, detGen.getTipoAvviso());
        assertFalse(detGen.isOrdineRealeTrovato());
        assertEquals("Comunicazione di servizio", detGen.getTitoloPulito());

        NotificaBean chat = new NotificaBean("💬 Risposta Cliente • Ordine ORD-22", "Arrivo tra 5 minuti", "Oggi 14:05");
        DettaglioAvvisoOrdineBean detChat = DettaglioAvvisoResolver.risolvi(chat, cod -> null);
        assertEquals(TipoAvviso.CHAT_ORDINE, detChat.getTipoAvviso());
        assertFalse(detChat.isOrdineRealeTrovato());

        NotificaBean storico = new NotificaBean("🛵 Nuovo Ordine ORD-01 da Confermare", "Vecchio ordine archiviato", "01/01/2026");
        DettaglioAvvisoOrdineBean detStor = DettaglioAvvisoResolver.risolvi(storico, cod -> null);
        assertEquals(TipoAvviso.ORDINE, detStor.getTipoAvviso());
        assertFalse(detStor.isOrdineRealeTrovato());
        assertEquals("ORD-01", detStor.getCodiceOrdine());
    }
}
