package view;

import bean.ScontrinoBean;
import java.util.Locale;

/**
 * Componente View dedicato esclusivamente alla formattazione HTML dello scontrino fiscale digitale
 * visualizzato sullo smartphone dopo la scansione del QR Code.
 */
public final class ScontrinoHtmlFormatter {

    private ScontrinoHtmlFormatter() {
    }

    public static String costruisciHtmlScontrino(ScontrinoBean s) {
        double totale = s.getImportoTotale();
        double imponibile = totale / 1.10;
        double iva = totale - imponibile;

        StringBuilder righeArticoli = new StringBuilder();
        String raw = s.getDettaglioProdotti() != null ? s.getDettaglioProdotti() : "";
        if (!raw.isBlank()) {
            for (String pezzo : raw.split("\\|")) {
                String p = pezzo.trim();
                if (!p.isEmpty()) {
                    righeArticoli.append("<div class='item-row'><span class='dot'>•</span><span>")
                            .append(escapeHtml(p))
                            .append("</span></div>");
                }
            }
        } else {
            righeArticoli.append("<div class='item-row'><span>Ordine Gelateria Frostly</span></div>");
        }

        String docNum = s.getCodiceOrdine() != null && !s.getCodiceOrdine().isBlank() ? s.getCodiceOrdine() : "ORD";

        return "<!DOCTYPE html><html lang='it'><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Scontrino Frostly — " + escapeHtml(docNum) + "</title>"
                + "<style>"
                + "body{margin:0;padding:20px 14px;background:#121114;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;display:flex;justify-content:center;}"
                + ".paper{width:100%;max-width:390px;background:#fffdf8;color:#1d1b18;border-radius:14px;box-shadow:0 16px 40px rgba(0,0,0,0.55);overflow:hidden;border:1px solid #d8c6ad;}"
                + ".top-band{background:#18161b;color:#f5ebe0;text-align:center;padding:22px 18px 18px;border-bottom:3px solid #baa487;}"
                + ".brand{font-family:Georgia,serif;font-size:24px;letter-spacing:4px;font-weight:700;margin:0;}"
                + ".sub{font-size:11px;letter-spacing:1.4px;color:#baa487;margin-top:5px;text-transform:uppercase;}"
                + ".addr{font-size:11.5px;color:#c7beb2;margin-top:8px;line-height:1.4;}"
                + ".body{padding:18px 20px;}"
                + ".badge-ok{background:#e6f6ef;color:#0f6b47;border:1px solid #10b981;border-radius:20px;padding:6px 12px;font-size:12px;font-weight:700;text-align:center;margin-bottom:14px;}"
                + ".meta{font-family:'Courier New',monospace;font-size:12px;color:#4a443c;border-top:1px dashed #c8bcaa;border-bottom:1px dashed #c8bcaa;padding:10px 0;margin-bottom:14px;}"
                + ".meta-row{display:flex;justify-content:space-between;gap:8px;margin:3px 0;}"
                + ".sec-title{font-size:11px;font-weight:700;letter-spacing:1px;color:#7a6852;text-transform:uppercase;margin:10px 0 8px;}"
                + ".item-row{display:flex;gap:8px;font-size:13.5px;padding:6px 0;border-bottom:1px dotted #e3dacd;line-height:1.35;}"
                + ".dot{color:#baa487;font-weight:bold;}"
                + ".totals{margin-top:14px;padding-top:10px;border-top:2px solid #1d1b18;font-family:'Courier New',monospace;}"
                + ".tot-sub{display:flex;justify-content:space-between;font-size:12.5px;color:#554e45;margin:3px 0;}"
                + ".tot-main{display:flex;justify-content:space-between;font-size:20px;font-weight:800;color:#141312;margin-top:8px;}"
                + ".pay-box{background:#f7f2ea;border:1px solid #e2d5c3;border-radius:10px;padding:11px 13px;margin-top:14px;font-size:12px;color:#3d3730;line-height:1.5;}"
                + ".footer{text-align:center;font-size:11.5px;color:#756b5e;padding:14px 18px 20px;border-top:1px dashed #c8bcaa;}"
                + ".btn-print{display:inline-block;margin-top:12px;background:#18161b;color:#f5ebe0;border:none;border-radius:10px;padding:10px 20px;font-size:13px;font-weight:700;cursor:pointer;}"
                + "@media print{body{background:#fff;padding:0;}.paper{box-shadow:none;border:none;}.btn-print{display:none;}}"
                + "</style></head><body>"
                + "<div class='paper'>"
                + "  <div class='top-band'>"
                + "    <div class='brand'>FROSTLY</div>"
                + "    <div class='sub'>Haute Gelateria &amp; Tea Salon • Dal 1984</div>"
                + "    <div class='addr'>Via del Corso 124, 00186 Roma (RM)<br>P.IVA 09482711004 • Tel. 06 555 1984</div>"
                + "  </div>"
                + "  <div class='body'>"
                + "    <div class='badge-ok'>✓ DOCUMENTO COMMERCIALE • PAGAMENTO VERIFICATO</div>"
                + "    <div class='meta'>"
                + "      <div class='meta-row'><span>DOC. N.</span><strong>" + escapeHtml(docNum) + "</strong></div>"
                + "      <div class='meta-row'><span>DATA E ORA</span><span>" + escapeHtml(nonNull(s.getDataOra())) + "</span></div>"
                + "      <div class='meta-row'><span>CLIENTE</span><span>" + escapeHtml(nonNull(s.getNomeCliente())) + "</span></div>"
                + "    </div>"
                + "    <div class='sec-title'>Articoli Acquistati</div>"
                + righeArticoli
                + "    <div class='totals'>"
                + "      <div class='tot-sub'><span>Imponibile</span><span>EUR " + String.format(Locale.US, "%.2f", imponibile) + "</span></div>"
                + "      <div class='tot-sub'><span>IVA 10% inclusa</span><span>EUR " + String.format(Locale.US, "%.2f", iva) + "</span></div>"
                + "      <div class='tot-main'><span>TOTALE</span><span>EUR " + String.format(Locale.US, "%.2f", totale) + "</span></div>"
                + "    </div>"
                + "    <div class='pay-box'>"
                + "      <strong>Pagamento Elettronico:</strong> " + escapeHtml(nonNull(s.getCircuito())) + " (" + escapeHtml(nonNull(s.getCartaMascherata())) + ")<br>"
                + "      <strong>Codice Autorizzazione:</strong> " + escapeHtml(nonNull(s.getCodiceTransazione())) + "<br>"
                + "      <strong>Ricezione:</strong> " + escapeHtml(nonNull(s.getModalitaConsegna()))
                + "    </div>"
                + "  </div>"
                + "  <div class='footer'>"
                + "    Grazie per aver scelto Frostly!<br>Conserva questo scontrino digitale.<br>"
                + "    <button class='btn-print' onclick='window.print()'>Stampa / Salva PDF</button>"
                + "  </div>"
                + "</div></body></html>";
    }

    public static String paginaNonTrovata() {
        return "<html><body style='font-family:sans-serif;text-align:center;padding:40px;'><h2>Scontrino non trovato</h2></body></html>";
    }

    private static String nonNull(String s) {
        return s != null ? s : "";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
