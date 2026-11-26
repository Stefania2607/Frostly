package view;

import bean.DettaglioAvvisoOrdineBean;
import bean.DettaglioAvvisoOrdineBean.RigaArticoloOrdine;
import bean.DettaglioAvvisoOrdineBean.TipoAvviso;
import bean.NotificaBean;
import bean.OrdineBean;
import bean.VaschettaItemBean;
import control.applicativo.OrdiniControl;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Risolve una NotificaBean in un DettaglioAvvisoOrdineBean strutturato.
 * Quando la notifica fa riferimento a un ordine (es. ORD-22), recupera i dati reali
 * dal sistema ordini tramite OrdiniControl evitando parsing fragili del testo notifica.
 */
public final class DettaglioAvvisoResolver {

    private static final Pattern PATTERN_CODICE_ORDINE = Pattern.compile("\\b(ORD-\\d+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_QUANTITA = Pattern.compile("^(\\d+)\\s*[xX×]\\s+(\\S.*)$");
    private static final Pattern PATTERN_PAN_GREZZO = Pattern.compile("(?<!\\d)(?:\\d[\\s-]?){13,19}(?!\\d)");
    private static final Pattern PATTERN_TRX_BOX = Pattern.compile("\\[Transazione Online:([^\\]]+)\\]");

    private DettaglioAvvisoResolver() {
    }

    public static DettaglioAvvisoOrdineBean risolvi(NotificaBean notifica) {
        OrdiniControl ctrl = new OrdiniControl();
        return risolvi(notifica, ctrl::trovaOrdinePerCodice);
    }

    public static DettaglioAvvisoOrdineBean risolvi(NotificaBean notifica,
                                                    Function<String, OrdineBean> lookupOrdine) {
        DettaglioAvvisoOrdineBean out = new DettaglioAvvisoOrdineBean();
        if (notifica == null) {
            out.setTitoloPulito("Avviso");
            return out;
        }

        String titoloRaw = notifica.getTitolo() != null ? notifica.getTitolo().trim() : "Avviso";
        String msgRaw = notifica.getMessaggio() != null ? notifica.getMessaggio().trim() : "";
        String ts = notifica.getTimestamp() != null ? notifica.getTimestamp().trim() : "";

        out.setTitoloPulito(pulisciEmojiIniziali(titoloRaw));
        out.setTimestamp(ts);
        out.setMessaggioOriginale(mascheraEventualiNumeriCarta(msgRaw));

        TipoAvviso tipo = classificaNotifica(titoloRaw, msgRaw);
        out.setTipoAvviso(tipo);

        String codiceOrdine = estraiCodiceOrdine(titoloRaw);
        if (codiceOrdine == null) {
            codiceOrdine = estraiCodiceOrdine(msgRaw);
        }
        out.setCodiceOrdine(codiceOrdine);

        if (tipo == TipoAvviso.ORDINE && codiceOrdine != null && lookupOrdine != null) {
            OrdineBean ordine = lookupOrdine.apply(codiceOrdine);
            if (ordine != null) {
                popolaDaOrdineReale(out, ordine);
            }
        }

        return out;
    }

    public static TipoAvviso classificaNotifica(String titolo, String messaggio) {
        String t = titolo != null ? titolo.toLowerCase(Locale.ITALIAN) : "";
        String m = messaggio != null ? messaggio.toLowerCase(Locale.ITALIAN) : "";

        if (t.contains("chat") || t.contains("risposta cliente") || m.contains("ha scritto nella chat")) {
            return TipoAvviso.CHAT_ORDINE;
        }
        if (t.contains("ordine") || PATTERN_CODICE_ORDINE.matcher(t).find()) {
            return TipoAvviso.ORDINE;
        }
        if (t.contains("emergenza") || m.contains("emergenza")) {
            return TipoAvviso.EMERGENZA;
        }
        if (t.contains("gusto") || m.contains("gusto")) {
            return TipoAvviso.GUSTO;
        }
        if (t.contains("recension") || m.contains("recension")) {
            return TipoAvviso.RECENSIONE;
        }
        if (m.contains("ordine") && PATTERN_CODICE_ORDINE.matcher(m).find()) {
            return TipoAvviso.ORDINE;
        }
        return TipoAvviso.GENERICO;
    }

    public static String estraiCodiceOrdine(String testo) {
        if (testo == null || testo.isBlank()) {
            return null;
        }
        Matcher m = PATTERN_CODICE_ORDINE.matcher(testo);
        if (m.find()) {
            return m.group(1).toUpperCase(Locale.ROOT);
        }
        return null;
    }

    private static void popolaDaOrdineReale(DettaglioAvvisoOrdineBean out, OrdineBean ordine) {
        out.setOrdineRealeTrovato(true);
        String codice = ordine.getCodiceOrdine() != null ? ordine.getCodiceOrdine() : out.getCodiceOrdine();
        out.setCodiceOrdine(codice);

        String fase = OrdiniControl.calcolaFaseConsegna(ordine);
        String statoGrezzo = ordine.getStato() != null ? ordine.getStato().toUpperCase(Locale.ROOT) : "IN_ATTESA";
        String statoEffettivo = "APPROVATA".equals(statoGrezzo) ? fase : statoGrezzo;
        out.setStatoCodice(statoEffettivo);
        out.setStatoEtichetta(formattaEtichettaStato(statoEffettivo));

        if ("IN_ATTESA".equals(statoEffettivo)) {
            out.setTitoloPulito("Nuovo Ordine " + codice);
            out.setNotaInformativaFooter("Accedi alla sezione Ordini per confermare o rifiutare l'ordine.");
        } else if ("RIFIUTATA".equals(statoEffettivo)) {
            out.setTitoloPulito("Ordine " + codice);
            out.setNotaInformativaFooter("Questo ordine risulta annullato. Apri Gestione Ordini per i dettagli completi.");
        } else {
            out.setTitoloPulito("Ordine " + codice);
            out.setNotaInformativaFooter("Apri Gestione Ordini per consultare l'avanzamento o contattare il cliente.");
        }

        String nomeCli = ordine.getNomeCliente() != null ? ordine.getNomeCliente().trim() : "";
        String emailCli = ordine.getEmailCliente() != null ? ordine.getEmailCliente().trim() : "";
        if (!nomeCli.isEmpty() && !nomeCli.equalsIgnoreCase("Cliente")) {
            out.setNomeCliente(capitalizzaNomeSeTuttoMinuscolo(nomeCli));
        } else if (!emailCli.isEmpty()) {
            out.setNomeCliente(emailCli);
        }
        if (!emailCli.isEmpty()) {
            out.setEmailCliente(emailCli);
        }

        out.setPrezzoTotale(ordine.getPrezzoTotale());

        analizzaDatiConsegnaEPagamento(out, ordine.getDataRitiro());
        popolaArticoliOrdine(out, ordine);
    }

    private static void analizzaDatiConsegnaEPagamento(DettaglioAvvisoOrdineBean out, String rawDataRitiro) {
        String raw = rawDataRitiro != null ? rawDataRitiro.trim() : "";
        if (raw.isEmpty()) {
            out.setRitiroAlBanco(true);
            out.setModalitaLabel("Ritiro al banco");
            out.setMetodoPagamento("Al ritiro / Non specificato");
            return;
        }

        // 1. Estrae blocco pagamento online strutturato [Transazione Online: TRX-... • VISA •••• 5671]
        Matcher mTrx = PATTERN_TRX_BOX.matcher(raw);
        if (mTrx.find()) {
            String contenutoTrx = mTrx.group(1).trim();
            String[] partiTrx = contenutoTrx.split("\\s+•\\s+", 2);
            if (partiTrx.length > 0 && !partiTrx[0].trim().isEmpty()) {
                out.setCodiceTransazione(partiTrx[0].trim());
            }
            if (partiTrx.length > 1 && !partiTrx[1].trim().isEmpty()) {
                out.setDettaglioCartaMascherata(normalizzaCartaMascherata(partiTrx[1].trim()));
            }
            out.setMetodoPagamento("Pagamento online");
            out.setStatoPagamento("Pagato");
            raw = (raw.substring(0, mTrx.start()) + " " + raw.substring(mTrx.end())).trim();
        } else if (raw.toLowerCase(Locale.ITALIAN).contains("contanti")) {
            out.setMetodoPagamento("Contanti");
            out.setStatoPagamento("Alla consegna / al ritiro");
        } else if (raw.toLowerCase(Locale.ITALIAN).contains("pos") || raw.toLowerCase(Locale.ITALIAN).contains("carta")) {
            out.setMetodoPagamento("Carta / POS");
            out.setStatoPagamento("Al ritiro / alla consegna");
        } else {
            out.setMetodoPagamento("Non specificato");
        }

        String rawLower = raw.toLowerCase(Locale.ITALIAN);
        boolean asporto = rawLower.contains("banco") || rawLower.contains("ritiro in gelateria") || rawLower.contains("asporto");
        out.setRitiroAlBanco(asporto);
        out.setModalitaLabel(asporto ? "Ritiro al banco" : "Consegna a domicilio");

        // Rimuove prefisso testuale tipo "🛍️ Ritiro al Banco (Subito (~25-35 min)) • Indirizzo/Note: "
        String intestazione = raw;
        String corpoDettagli = "";
        int idxIndNote = raw.indexOf("• Indirizzo/Note:");
        if (idxIndNote >= 0) {
            intestazione = raw.substring(0, idxIndNote).trim();
            corpoDettagli = raw.substring(idxIndNote + "• Indirizzo/Note:".length()).trim();
        } else {
            corpoDettagli = raw;
        }

        // Eventuale orario nell'intestazione: "... (Subito (~25-35 min))"
        String orarioIntestazione = estraiOrarioTraParentesi(intestazione);
        if (orarioIntestazione != null && !orarioIntestazione.isBlank()) {
            out.setOrarioRichiesto(orarioIntestazione);
        }

        // Analizza segmenti separati da "•"
        List<String> noteExtra = new ArrayList<>();
        String[] segmenti = corpoDettagli.split("•");
        for (String seg : segmenti) {
            String s = pulisciEmojiIniziali(seg.trim());
            if (s.isEmpty()) {
                continue;
            }
            String sLower = s.toLowerCase(Locale.ITALIAN);
            if (sLower.startsWith("orario:")) {
                String val = s.substring("orario:".length()).trim();
                if (!val.isEmpty()) {
                    out.setOrarioRichiesto(val);
                }
            } else if (sLower.startsWith("tel:") || sLower.startsWith("telefono:")) {
                String val = s.substring(s.indexOf(':') + 1).trim();
                if (!val.isEmpty()) {
                    out.setTelefonoCliente(val);
                }
            } else if (sLower.startsWith("indirizzo:")) {
                String val = s.substring("indirizzo:".length()).trim();
                if (!val.isEmpty()) {
                    out.setLuogoOIndirizzo(val);
                }
            } else if (sLower.startsWith("ritiro:")) {
                String val = s.substring("ritiro:".length()).trim();
                if (!val.isEmpty()) {
                    out.setNominativoRitiro(val);
                }
            } else if (sLower.startsWith("note:")) {
                String val = s.substring("note:".length()).trim();
                if (!val.isEmpty()) {
                    noteExtra.add(val);
                }
            } else if (sLower.startsWith("ritiro in gelateria")) {
                out.setLuogoOIndirizzo(s);
            } else if (sLower.startsWith("consegna a domicilio") || sLower.startsWith("ritiro al banco")) {
                // Già catturato in modalitaLabel
            } else if (out.getLuogoOIndirizzo() == null && !asporto) {
                out.setLuogoOIndirizzo(s);
            } else {
                noteExtra.add(s);
            }
        }

        if (out.getLuogoOIndirizzo() == null || out.getLuogoOIndirizzo().isBlank()) {
            if (asporto) {
                out.setLuogoOIndirizzo("Gelateria Frostly (Banco)");
            }
        }
        if (!noteExtra.isEmpty()) {
            out.setNoteCliente(String.join(" • ", noteExtra));
        }
    }

    private static void popolaArticoliOrdine(DettaglioAvvisoOrdineBean out, OrdineBean ordine) {
        List<VaschettaItemBean> vaschette = ordine.getVaschette();
        if (vaschette != null && !vaschette.isEmpty()) {
            popolaArticoliDaOggettiStrutturati(out, vaschette);
            return;
        }
        popolaArticoliDaDescrizione(out, ordine.getDescrizioneCompleta(), ordine.getPrezzoTotale());
    }

    private static void popolaArticoliDaOggettiStrutturati(DettaglioAvvisoOrdineBean out,
                                                           List<VaschettaItemBean> vaschette) {
        Map<String, List<VaschettaItemBean>> gruppi = new LinkedHashMap<>();
        for (VaschettaItemBean v : vaschette) {
            if (v == null) continue;
            String chiave = costruisciChiaveRaggruppamento(v);
            gruppi.computeIfAbsent(chiave, k -> new ArrayList<>()).add(v);
        }

        for (List<VaschettaItemBean> gruppo : gruppi.values()) {
            VaschettaItemBean ref = gruppo.get(0);
            int quantitaTotale = 0;
            double subtotale = 0.0;
            boolean haPrezzi = true;
            for (VaschettaItemBean v : gruppo) {
                int q = Math.max(1, v.getQuantita());
                quantitaTotale += q;
                double p = v.ottieniPrezzo();
                if (p > 0.0) {
                    subtotale += p;
                } else {
                    haPrezzi = false;
                }
            }
            Double prezzoUnit = (haPrezzi && quantitaTotale > 0)
                    ? Math.round((subtotale / quantitaTotale) * 100.0) / 100.0
                    : null;
            Double subtotaleVal = haPrezzi ? Math.round(subtotale * 100.0) / 100.0 : null;

            RigaArticoloOrdine riga = costruisciRigaDaVaschetta(ref, quantitaTotale, prezzoUnit, subtotaleVal);
            out.aggiungiArticolo(riga);
        }
    }

    private static String costruisciChiaveRaggruppamento(VaschettaItemBean v) {
        String desc = v.ottieniDescrizione() != null ? v.ottieniDescrizione().trim() : "";
        String gusti = v.ottieniGusti() != null ? String.join(",", v.ottieniGusti()) : "";
        return desc + "|" + gusti + "|" + v.ottieniPesoKg() + "|" + v.haPanna() + "|" + v.haGranellaBronte()
                + "|" + Math.round(v.getPrezzoUnitario() * 100.0);
    }

    private static RigaArticoloOrdine costruisciRigaDaVaschetta(VaschettaItemBean v,
                                                                int quantita,
                                                                Double prezzoUnitario,
                                                                Double subtotaleRiga) {
        String desc = v.ottieniDescrizione() != null ? v.ottieniDescrizione().trim() : "";
        desc = desc.replaceFirst("^[Vv]aschetta #\\d+:\\s*", "").trim();

        List<String> varianti = new ArrayList<>();
        List<String> topping = new ArrayList<>();
        List<String> aggiunte = new ArrayList<>();
        List<String> esclusioni = new ArrayList<>();
        List<String> altre = new ArrayList<>();
        StringBuilder note = new StringBuilder();

        String nome;
        if (!desc.isEmpty()) {
            nome = estraiNomeEPersonalizzazioni(desc, varianti, topping, aggiunte, esclusioni, altre, note);
        } else if (v.ottieniPesoKg() > 0.0) {
            int grammi = (int) Math.round(v.ottieniPesoKg() * 1000.0);
            nome = "Vaschetta Gelato " + grammi + "g";
            if (v.ottieniGusti() != null && !v.ottieniGusti().isEmpty()) {
                varianti.addAll(v.ottieniGusti());
            }
        } else if (v.ottieniGusti() != null && !v.ottieniGusti().isEmpty()) {
            nome = String.join(", ", v.ottieniGusti());
        } else {
            nome = "Articolo Gelateria";
        }

        if (v.haPanna() && !contieneGia(aggiunte, "panna") && !desc.toLowerCase(Locale.ITALIAN).contains("panna")) {
            aggiunte.add("Panna montata");
        }
        if (v.haGranellaBronte() && !contieneGia(aggiunte, "granella") && !desc.toLowerCase(Locale.ITALIAN).contains("granella")) {
            aggiunte.add("Granella di pistacchio di Bronte");
        }

        return new RigaArticoloOrdine(quantita, nome, varianti, topping, aggiunte, esclusioni, altre,
                note.toString().trim(), prezzoUnitario, subtotaleRiga);
    }

    private static void popolaArticoliDaDescrizione(DettaglioAvvisoOrdineBean out,
                                                    String descrizioneCompleta,
                                                    double prezzoTotaleOrdine) {
        if (descrizioneCompleta == null || descrizioneCompleta.isBlank()) {
            return;
        }
        Map<String, Integer> raggruppati = new LinkedHashMap<>();
        String[] pezzi = descrizioneCompleta.split("\\s*\\|\\s*");
        for (String p : pezzi) {
            String cleaned = p.trim().replaceFirst("^[Vv]aschetta #\\d+:\\s*", "").trim();
            if (cleaned.isEmpty()) continue;
            int qty = 1;
            Matcher m = PATTERN_QUANTITA.matcher(cleaned);
            if (m.matches()) {
                qty = Integer.parseInt(m.group(1));
                cleaned = m.group(2).trim();
            }
            raggruppati.merge(cleaned, qty, Integer::sum);
        }

        boolean singoloArticolo = raggruppati.size() == 1 && prezzoTotaleOrdine > 0.0;
        for (Map.Entry<String, Integer> entry : raggruppati.entrySet()) {
            String testo = entry.getKey();
            int qty = entry.getValue();

            List<String> varianti = new ArrayList<>();
            List<String> topping = new ArrayList<>();
            List<String> aggiunte = new ArrayList<>();
            List<String> esclusioni = new ArrayList<>();
            List<String> altre = new ArrayList<>();
            StringBuilder note = new StringBuilder();

            String nome = estraiNomeEPersonalizzazioni(testo, varianti, topping, aggiunte, esclusioni, altre, note);

            Double prezzoUnit = null;
            Double subtotale = null;
            if (singoloArticolo) {
                subtotale = Math.round(prezzoTotaleOrdine * 100.0) / 100.0;
                prezzoUnit = Math.round((prezzoTotaleOrdine / qty) * 100.0) / 100.0;
            }

            out.aggiungiArticolo(new RigaArticoloOrdine(
                    qty, nome, varianti, topping, aggiunte, esclusioni, altre,
                    note.toString().trim(), prezzoUnit, subtotale));
        }
    }

    private static String estraiNomeEPersonalizzazioni(String testo,
                                                       List<String> varianti,
                                                       List<String> topping,
                                                       List<String> aggiunte,
                                                       List<String> esclusioni,
                                                       List<String> altre,
                                                       StringBuilder note) {
        String nome = testo.trim();
        String bloccoDettagli = "";

        int p1 = nome.indexOf('(');
        int p2 = nome.lastIndexOf(')');
        if (p1 > 0 && p2 > p1) {
            String base = nome.substring(0, p1).trim();
            bloccoDettagli = nome.substring(p1 + 1, p2).trim();
            String coda = nome.substring(p2 + 1).trim();
            nome = base;
            if (!coda.isEmpty()) {
                bloccoDettagli = bloccoDettagli.isEmpty() ? coda : (bloccoDettagli + " • " + coda);
            }
        } else if (nome.contains(" + ")) {
            String[] partiPlus = nome.split("\\s*\\+\\s*");
            nome = partiPlus[0].trim();
            for (int i = 1; i < partiPlus.length; i++) {
                String add = partiPlus[i].trim();
                if (!add.isEmpty()) {
                    aggiunte.add(add);
                }
            }
        }

        if (!bloccoDettagli.isEmpty()) {
            String[] token = bloccoDettagli.split("\\s*[•;]\\s*");
            for (String rawTok : token) {
                String tok = rawTok.trim();
                if (tok.isEmpty()) continue;
                String lower = tok.toLowerCase(Locale.ITALIAN);

                if (lower.startsWith("topping:") || lower.startsWith("topping ")) {
                    String val = tok.replaceFirst("(?i)^topping:?\\s*", "").trim();
                    if (!val.isEmpty()) topping.add(val);
                } else if (lower.startsWith("senza ") || lower.startsWith("no ") || lower.startsWith("esclus")) {
                    esclusioni.add(capitalizzaPrima(tok));
                } else if (lower.startsWith("+") || lower.startsWith("extra:") || lower.startsWith("extra ")
                        || lower.startsWith("con ")) {
                    String val = tok.replaceFirst("(?i)^(?:\\+|extra:?|con)\\s*", "").trim();
                    if (!val.isEmpty()) aggiunte.add(capitalizzaPrima(val));
                } else if (lower.startsWith("nota:") || lower.startsWith("note:")) {
                    String val = tok.substring(tok.indexOf(':') + 1).trim();
                    if (!val.isEmpty()) {
                        if (note.length() > 0) note.append(" • ");
                        note.append(val);
                    }
                } else if (lower.startsWith("gusti:") || lower.startsWith("gusto:") || lower.startsWith("variante:")) {
                    String val = tok.substring(tok.indexOf(':') + 1).trim();
                    if (!val.isEmpty()) varianti.add(val);
                } else if (nome.toLowerCase(Locale.ITALIAN).contains("vaschetta") && !lower.contains(":")) {
                    varianti.add(tok);
                } else {
                    altre.add(tok);
                }
            }
        }

        return nome;
    }

    private static boolean contieneGia(List<String> lista, String parolaChiave) {
        String key = parolaChiave.toLowerCase(Locale.ITALIAN);
        for (String s : lista) {
            if (s != null && s.toLowerCase(Locale.ITALIAN).contains(key)) {
                return true;
            }
        }
        return false;
    }

    public static String formattaEtichettaStato(String statoCodice) {
        if (statoCodice == null) return "Da confermare";
        return switch (statoCodice.toUpperCase(Locale.ROOT)) {
            case "IN_ATTESA" -> "Da confermare";
            case "APPROVATA" -> "Confermato";
            case "IN_PREPARAZIONE" -> "In preparazione";
            case "PRONTO_AL_BANCO" -> "Pronto al banco";
            case "IN_CONSEGNA" -> "In consegna";
            case "RITIRATO" -> "Ritirato";
            case "CONSEGNATO" -> "Consegnato";
            case "COMPLETATA" -> "Completato";
            case "RIFIUTATA" -> "Annullato";
            default -> statoCodice;
        };
    }

    public static String normalizzaCartaMascherata(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String pulita = mascheraEventualiNumeriCarta(raw.trim());
        // Compatta "VISA •••• •••• •••• 5671" in "VISA •••• 5671"
        return pulita.replaceAll("(?:[•*]\\s*){6,}", "•••• ");
    }

    public static String mascheraEventualiNumeriCarta(String testo) {
        if (testo == null || testo.isBlank()) return "";
        Matcher m = PATTERN_PAN_GREZZO.matcher(testo);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String digits = m.group().replaceAll("\\D", "");
            if (digits.length() >= 13 && digits.length() <= 19) {
                String last4 = digits.substring(digits.length() - 4);
                m.appendReplacement(sb, "•••• " + last4);
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String estraiOrarioTraParentesi(String testo) {
        if (testo == null) return null;
        int p1 = testo.indexOf('(');
        int p2 = testo.lastIndexOf(')');
        if (p1 >= 0 && p2 > p1) {
            String dentro = testo.substring(p1 + 1, p2).trim();
            if (!dentro.equalsIgnoreCase("Banco") && !dentro.isEmpty()) {
                return dentro;
            }
        }
        return null;
    }

    private static String pulisciEmojiIniziali(String s) {
        if (s == null) return "";
        int i = 0;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            if (Character.isLetterOrDigit(cp) || cp == '+' || cp == '-') {
                break;
            }
            i += Character.charCount(cp);
        }
        String res = s.substring(i).trim();
        return res.isEmpty() ? s.trim() : res;
    }

    private static String capitalizzaNomeSeTuttoMinuscolo(String nome) {
        if (nome == null || nome.isBlank()) return "";
        if (!nome.equals(nome.toLowerCase(Locale.ITALIAN))) {
            return nome;
        }
        String[] parole = nome.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parole) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private static String capitalizzaPrima(String s) {
        if (s == null || s.isBlank()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
