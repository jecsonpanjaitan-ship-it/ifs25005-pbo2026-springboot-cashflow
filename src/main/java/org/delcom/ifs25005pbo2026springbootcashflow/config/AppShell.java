package org.delcom.ifs25005pbo2026springbootcashflow.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.server.AppShellSettings;

/**
 * Konfigurasi halaman global (app shell): meta deskripsi untuk SEO dan favicon.
 */
@Meta(name = "description",
        content = "Cashflow: aplikasi pencatat keuangan pribadi untuk memantau pemasukan, pengeluaran, dan saldo.")
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "icons/favicon.svg", "any");
    }
}
