package org.delcom.ifs25005pbo2026springbootcashflow.framework.view;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Memberi masa cache panjang pada berkas JavaScript/CSS hasil build Vaadin.
 * Nama berkas di /VAADIN/build/ memuat hash isi, sehingga aman disimpan lama di browser.
 */
@Component
public class StaticCacheFilter extends OncePerRequestFilter {

    private static final String BUILD_PATH = "/VAADIN/build/";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String LONG_CACHE = "public, max-age=31536000, immutable";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(BUILD_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletResponse wrapped = new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                super.setHeader(name, CACHE_CONTROL.equalsIgnoreCase(name) ? LONG_CACHE : value);
            }

            @Override
            public void addHeader(String name, String value) {
                if (CACHE_CONTROL.equalsIgnoreCase(name)) {
                    super.setHeader(name, LONG_CACHE);
                } else {
                    super.addHeader(name, value);
                }
            }
        };
        wrapped.setHeader(CACHE_CONTROL, LONG_CACHE);
        filterChain.doFilter(request, wrapped);
    }
}