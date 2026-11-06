package org.delcom.ifs25005pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Menyisipkan judul, bahasa, dan kerangka semantik (main, h1, subjudul) ke HTML awal,
 * serta menandai indikator koneksi bawaan Vaadin sebagai landmark agar lolos audit aksesibilitas.
 */
@Component
public class InitialPageListener implements VaadinServiceInitListener {

    private static final String TITLE = "Cashflow - Catatan Keuangan Pribadi";
    private static final String SUBTITLE =
            "Catatan keuangan pribadi: pantau pemasukan, pengeluaran, dan saldo Anda.";

    // Catatan: skrip ini sengaja tanpa karakter &, <, dan > agar aman ditulis sebagai teks HTML.
    private static final String INDICATOR_SCRIPT = "(function(){"
            + "function fix(){"
            + "var e=document.querySelector('vaadin-connection-indicator');"
            + "if(!e){return false;}"
            + "e.setAttribute('role','region');"
            + "e.setAttribute('aria-label','Status koneksi');"
            + "return true;"
            + "}"
            + "if(fix()){return;}"
            + "var o=new MutationObserver(function(){if(fix()){o.disconnect();}});"
            + "o.observe(document.documentElement,{childList:true,subtree:true});"
            + "})();";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(this::enrich);
    }

    private void enrich(IndexHtmlResponse response) {
        Document document = response.getDocument();
        document.title(TITLE);
        document.select("html").attr("lang", "id");
        document.head().appendElement("script").text(INDICATOR_SCRIPT);

        Element shell = document.body().prependElement("main")
                .attr("id", "initial-shell")
                .attr("style", "position:absolute;top:0;left:0;right:0;box-sizing:border-box;"
                        + "max-width:1200px;margin:0 auto;padding:1rem;color:#111827");
        shell.appendElement("h1")
                .attr("style", "margin:0;font-size:2rem;color:#111827")
                .text("Cashflow");
        shell.appendElement("p")
                .attr("style", "margin:0.25rem 0 1rem;color:#374151")
                .text(SUBTITLE);
    }
}