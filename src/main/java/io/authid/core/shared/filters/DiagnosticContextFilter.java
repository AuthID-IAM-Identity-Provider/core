package io.authid.core.shared.filters;

import io.authid.core.shared.constants.DiagnosticContextConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Memastikan filter ini berjalan paling awal
@Slf4j
public class DiagnosticContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        
        try {
            // Tahap 1: Setup Konteks
            initializeMdcContext(request);
            addResponseHeaders(response);

            // Tahap 2: Proses Request
            filterChain.doFilter(request, response);

        } finally {
            // Tahap 3: Finalisasi & Pembersihan
            updateMdcWithDuration(startTime);
            
            // Log akses ringkas setelah request selesai dan durasi dihitung
            logAccess(); 
            
            MDC.clear();
        }
    }

    /**
     * Menginisialisasi Mapped Diagnostic Context (MDC) dengan data dari request.
     */
    private void initializeMdcContext(HttpServletRequest request) {
        Map<String, String> contextMap = Map.of(
            DiagnosticContextConstant.MDC_KEY_TRACE_ID, getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_TRACE_ID, UUID.randomUUID().toString()),
            DiagnosticContextConstant.MDC_KEY_REQUEST_ID, getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_REQUEST_ID, UUID.randomUUID().toString()),
            DiagnosticContextConstant.MDC_KEY_HTTP_METHOD, request.getMethod(),
            DiagnosticContextConstant.MDC_KEY_REQUEST_URI, request.getRequestURI(),
            DiagnosticContextConstant.MDC_KEY_CLIENT_IP, getClientIp(request),
            DiagnosticContextConstant.MDC_KEY_CORRELATION_ID, getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_CORRELATION_ID, UUID.randomUUID().toString())
        );
        
        // Menambahkan semua entri yang pasti ada
        MDC.setContextMap(contextMap);

        // Menambahkan entri opsional hanya jika ada nilainya
        Stream.of(
            Map.entry(DiagnosticContextConstant.MDC_KEY_USER_AGENT, getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_USER_AGENT)),
            Map.entry(DiagnosticContextConstant.MDC_KEY_CLIENT_ID, getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_API_KEY))
        ).forEach(entry -> entry.getValue().ifPresent(value -> MDC.put(entry.getKey(), value)));
    }

    /**
     * Menambahkan header ke response, baik untuk tracing maupun keamanan.
     */
    private void addResponseHeaders(HttpServletResponse response) {
        response.setHeader(DiagnosticContextConstant.HTTP_HEADER_TRACE_ID, MDC.get(DiagnosticContextConstant.MDC_KEY_TRACE_ID));
        response.setHeader(DiagnosticContextConstant.HTTP_HEADER_REQUEST_ID, MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_ID));
        response.setHeader(DiagnosticContextConstant.HTTP_HEADER_CORRELATION_ID, MDC.get(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
    }

    /**
     * Memperbarui MDC dengan durasi total request.
     */
    private void updateMdcWithDuration(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS, String.valueOf(duration));
    }
    
    /**
     * Mencatat log akses ringkas setelah semua informasi MDC terkumpul.
     */
    private void logAccess() {
        log.info("Request processed",
            kv("method", MDC.get(DiagnosticContextConstant.MDC_KEY_HTTP_METHOD)),
            kv("uri", MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_URI)),
            kv("duration_ms", Long.parseLong(MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS)))
        );
    }
    
    // --- Helper Methods ---

    private String getHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        return getHeaderValue(request, headerName).orElse(defaultValue);
    }
    
    private Optional<String> getHeaderValue(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName)).filter(v -> !v.trim().isEmpty());
    }
    
    private String getClientIp(HttpServletRequest request) {
        return getHeaderOrDefault(request, DiagnosticContextConstant.HTTP_HEADER_FORWARDED_FOR, request.getRemoteAddr());
    }
}