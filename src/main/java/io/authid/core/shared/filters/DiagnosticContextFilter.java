package io.authid.core.shared.filters;

import io.authid.core.shared.constants.DiagnosticContextConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.MDC;
import java.io.IOException;
import java.security.Principal;
import java.util.UUID;


@Component
@Order(1)
public class DiagnosticContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            // 1. Set Request Timestamp
            MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_TIMESTAMP, String.valueOf(startTime));

            // 2. Handle Trace ID (X-Trace-Id header)
            //    If provided by upstream, use it. Otherwise, generate a new one.
            String traceId = request.getHeader(DiagnosticContextConstant.HTTP_HEADER_TRACE_ID);
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }
            MDC.put(DiagnosticContextConstant.MDC_KEY_TRACE_ID, traceId);

            // 3. Handle Request ID (Unique for this specific service's request)
            //    Generate a new one for each request processed by this service instance.
            MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_ID, UUID.randomUUID().toString());

            // 4. Handle Correlation ID (X-Correlation-Id header)
            //    If provided by upstream (e.g., business process ID), use it. Otherwise, use traceId or generate.
            String correlationId = request.getHeader(DiagnosticContextConstant.HTTP_HEADER_CORRELATION_ID);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = traceId; // Fallback to traceId if no explicit correlationId
            }
            MDC.put(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID, correlationId);

            // 5. User ID (from authenticated principal)
            Principal principal = request.getUserPrincipal();
            if (principal != null) {
                MDC.put(DiagnosticContextConstant.MDC_KEY_USER_ID, principal.getName());
            }

            // 6. Client IP (handle X-Forwarded-For from proxies/load balancers)
            String clientIp = request.getHeader(DiagnosticContextConstant.HTTP_HEADER_FORWARDED_FOR);
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            }
            MDC.put(DiagnosticContextConstant.MDC_KEY_CLIENT_IP, clientIp);

            // 7. HTTP Method and Request URI
            MDC.put(DiagnosticContextConstant.MDC_KEY_HTTP_METHOD, request.getMethod());
            MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_URI, request.getRequestURI());

            // 8. User Agent (X-User-Agent header)
            String userAgent = request.getHeader(DiagnosticContextConstant.HTTP_HEADER_USER_AGENT);
            if (userAgent != null && !userAgent.isEmpty()) {
                MDC.put(DiagnosticContextConstant.MDC_KEY_USER_AGENT, userAgent);
            }

            // 9. Client/Tenant ID (X-Client-Id header)
            String clientId = request.getHeader(DiagnosticContextConstant.HTTP_HEADER_CLIENT_ID);
            if (clientId != null && !clientId.isEmpty()) {
                MDC.put(DiagnosticContextConstant.MDC_KEY_TENANT_ID, clientId);
            }

            // --- Proceed with the request chain ---
            filterChain.doFilter(request, response);
        } finally {
            // Calculate and set request duration before clearing MDC
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS, String.valueOf(duration));

            // Log completion of the request (optional, can be done by AOP too)
            // LoggerFactory.getLogger(TraceIdMdcFilter.class).info("Request processed.");

            // --- CRUCIAL: Clear all MDC keys to prevent memory leaks and context bleed ---
            MDC.remove(DiagnosticContextConstant.MDC_KEY_TRACE_ID);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_REQUEST_ID);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_USER_ID);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_CLIENT_IP);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_HTTP_METHOD);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_REQUEST_URI);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_USER_AGENT);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_TENANT_ID);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_REQUEST_TIMESTAMP);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS);
        }
    }
}
