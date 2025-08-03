package io.authid.core.shared.utils;

import lombok.*;

import java.time.Instant;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniMeta {
    private String requestId;      // From MDC_KEY_REQUEST_ID
    private String traceId;        // From MDC_KEY_TRACE_ID
    private String correlationId;  // From MDC_KEY_CORRELATION_ID (New)
    private String userId;         // From MDC_KEY_USER_ID (New)
    private String clientIp;       // From MDC_KEY_CLIENT_IP (New)
    private String tenantId;       // From MDC_KEY_TENANT_ID (New, if applicable)
    private String operationName;  // From MDC_KEY_OPERATION_NAME (New)
    private Long requestDurationMs; // From MDC_KEY_REQUEST_DURATION_MS (New)

    // Optional, if sessionId is a distinct concept from userId for your system
    private String sessionId;

    private Instant timestamp;     // From MDC_KEY_REQUEST_TIMESTAMP (or just current time of response generation)
    private UniPagination pagination; // Domain-specific, not from MDC_KEY
}
