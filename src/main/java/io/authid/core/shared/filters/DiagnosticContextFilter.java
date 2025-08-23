package io.authid.core.shared.filters;

import io.authid.core.shared.constants.DiagnosticContextConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.Optional;

@Component
@Order(1) // Paling pertama dijalankan
@Slf4j
public class DiagnosticContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis(); // Mulai pengukuran durasi

        try {
            // 1. Inisialisasi MDC dengan konteks diagnostik dari request
            initializeMdcContext(request, startTime);

            // 2. Set header respons untuk tracing dan keamanan umum
            // Ini dilakukan di awal untuk memastikan header hadir terlepas dari alur selanjutnya
            setResponseHeaders(response);

            // 3. Lanjutkan rantai filter untuk memproses request
            filterChain.doFilter(request, response);

        } finally {
            // 4. Hitung durasi permintaan dan tambahkan ke MDC sebelum membersihkan
            updateMdcWithDuration(startTime);

            // 5. Bersihkan MDC untuk mencegah kontaminasi konteks antar thread/request
            MDC.clear();
        }
    }

    /**
     * Menginisialisasi Mapped Diagnostic Context (MDC) dengan informasi dari permintaan HTTP.
     * Metode ini bertanggung jawab untuk menyiapkan konteks logging untuk seluruh durasi permintaan.
     *
     * @param request HttpServletRequest saat ini
     * @param startTime Waktu mulai pemrosesan permintaan
     */
    private void initializeMdcContext(HttpServletRequest request, long startTime) {
        // Timestamp
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_TIMESTAMP, String.valueOf(startTime));

        // Tracing & Request IDs: Mengambil dari header atau generate baru, lalu masukkan ke MDC
        String traceId = getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_TRACE_ID, UUID.randomUUID().toString());
        String requestId = getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_REQUEST_ID, UUID.randomUUID().toString());
        String correlationId = getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_CORRELATION_ID, UUID.randomUUID().toString());

        MDC.put(DiagnosticContextConstant.MDC_KEY_TRACE_ID, traceId);
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_ID, requestId);
        MDC.put(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID, correlationId);

        // Request Info: Metode, URI, IP Klien, User-Agent
        MDC.put(DiagnosticContextConstant.MDC_KEY_HTTP_METHOD, request.getMethod());
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_URI, request.getRequestURI());
        MDC.put(DiagnosticContextConstant.MDC_KEY_CLIENT_IP, getClientIp(request));
        getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_USER_AGENT).ifPresent(val -> MDC.put(DiagnosticContextConstant.MDC_KEY_USER_AGENT, val));

        // API Key/Client ID (jika ada)
        getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_API_KEY).ifPresent(val -> MDC.put(DiagnosticContextConstant.MDC_KEY_CLIENT_ID, val));
    }

    /**
     * Menyetel header respons HTTP yang relevan untuk tracing, korelasi, dan keamanan umum.
     * Nilai-nilai untuk tracing diambil dari MDC yang sudah diinisialisasi.
     *
     * @param response HttpServletResponse yang akan dimodifikasi
     */
    private void setResponseHeaders(HttpServletResponse response) {
        // Header Tracing & Korelasi (diambil dari MDC)
        // Pastikan MDC telah diisi oleh initializeMdcContext() sebelumnya
        setResponseHeaderFromMdc(response, DiagnosticContextConstant.HTTP_HEADER_TRACE_ID, DiagnosticContextConstant.MDC_KEY_TRACE_ID);
        setResponseHeaderFromMdc(response, DiagnosticContextConstant.HTTP_HEADER_REQUEST_ID, DiagnosticContextConstant.MDC_KEY_REQUEST_ID);
        setResponseHeaderFromMdc(response, DiagnosticContextConstant.HTTP_HEADER_CORRELATION_ID, DiagnosticContextConstant.MDC_KEY_CORRELATION_ID);

        // Header Keamanan Umum (hardcoded atau dari konfigurasi)
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        response.setHeader("Referrer-Policy", "no-referrer-when-downgrade");

        log.debug("Response headers set by DiagnosticContextFilter.");
    }

    /**
     * Memperbarui MDC dengan durasi total permintaan.
     * @param startTime Waktu mulai permintaan.
     */
    private void updateMdcWithDuration(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS, String.valueOf(duration));
    }

    /**
     * Mendapatkan nilai header dari request, mengembalikan Optional.
     * @param request HttpServletRequest
     * @param headerName Nama header
     * @return Optional yang berisi nilai header jika ada dan tidak kosong
     */
    private Optional<String> getHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return Optional.ofNullable(value).filter(v -> !v.trim().isEmpty());
    }

    /**
     * Mendapatkan nilai header dari request, atau nilai default jika tidak ada/kosong.
     * @param request HttpServletRequest
     * @param headerName Nama header
     * @param defaultValue Nilai default jika header tidak ditemukan atau kosong
     * @return Nilai header atau nilai default
     */
    private String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        return getHeaderValue(request, headerName).orElse(defaultValue);
    }

    /**
     * Mengambil nilai dari MDC dan menyetelnya sebagai header respons.
     * @param response HttpServletResponse
     * @param httpHeaderName Nama header HTTP yang akan disetel
     * @param mdcKey Kunci MDC tempat nilai disimpan
     */
    private void setResponseHeaderFromMdc(HttpServletResponse response, String httpHeaderName, String mdcKey) {
        Optional.ofNullable(MDC.get(mdcKey))
                .filter(value -> !value.isEmpty())
                .ifPresent(value -> response.setHeader(httpHeaderName, value));
    }

    /**
     * Mendapatkan alamat IP klien. Mengutamakan X-Forwarded-For jika ada.
     * @param request HttpServletRequest
     * @return Alamat IP klien
     */
    private String getClientIp(HttpServletRequest request) {
        return getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_FORWARDED_FOR, request.getRemoteAddr());
    }
}
