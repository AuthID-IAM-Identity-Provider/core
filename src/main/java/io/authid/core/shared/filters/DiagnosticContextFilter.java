package io.authid.core.shared.filters;

import io.authid.core.shared.constants.DiagnosticContextConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1) // Paling pertama dijalankan
public class DiagnosticContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            populateInitialMdc(request, startTime);
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS, String.valueOf(duration));

            MDC.clear();
        }
    }

    private void populateInitialMdc(HttpServletRequest request, long startTime) {
        // Timestamp
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_TIMESTAMP, String.valueOf(startTime));

        // Tracing & Request IDs
        String traceId = getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_TRACE_ID).orElseGet(() -> UUID.randomUUID().toString());
        MDC.put(DiagnosticContextConstant.MDC_KEY_TRACE_ID, traceId);
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_ID, UUID.randomUUID().toString());

        // Request Info
        MDC.put(DiagnosticContextConstant.MDC_KEY_HTTP_METHOD, request.getMethod());
        MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_URI, request.getRequestURI());
        MDC.put(DiagnosticContextConstant.MDC_KEY_CLIENT_IP, getClientIp(request));

        // Headers (jika ada)
        getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_CORRELATION_ID).ifPresent(val -> MDC.put(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID, val));
        getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_USER_AGENT).ifPresent(val -> MDC.put(DiagnosticContextConstant.MDC_KEY_USER_AGENT, val));
        getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_API_KEY).ifPresent(val -> MDC.put(DiagnosticContextConstant.MDC_KEY_CLIENT_ID, val));
    }

    private Optional<String> getHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return (value == null || value.trim().isEmpty()) ? Optional.empty() : Optional.of(value);
    }

    private String getClientIp(HttpServletRequest request) {
        return getHeaderValue(request, DiagnosticContextConstant.HTTP_HEADER_FORWARDED_FOR).orElse(request.getRemoteAddr());
    }
}