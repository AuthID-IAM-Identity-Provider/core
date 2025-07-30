package io.authid.core.shared.constants;

public final class DiagnosticContextConstant {
    public static final String HTTP_HEADER_TRACE_ID = "X-Trace-Id"; // Custom trace ID header
    public static final String HTTP_HEADER_REQUEST_ID = "X-Request-Id"; // Custom request ID header (less common, usually traceId covers it)
    public static final String HTTP_HEADER_CORRELATION_ID = "X-Correlation-Id"; // Custom correlation ID header
    public static final String HTTP_HEADER_USER_AGENT = "User-Agent";
    public static final String HTTP_HEADER_FORWARDED_FOR = "X-Forwarded-For";
    public static final String HTTP_HEADER_CLIENT_ID = "X-Client-Id"; // For client/tenant ID
    public static final String HTTP_HEADER_TENANT_ID = "X-Tenant-Id"; // For tenant ID
    public static final String HTTP_HEADER_ID = "X-Tenant-Id"; // For tenant ID


    public static final String MDC_KEY_TRACE_ID = "traceId";
    public static final String MDC_KEY_REQUEST_ID = "requestId"; // Specific for current service's request
    public static final String MDC_KEY_CORRELATION_ID = "correlationId"; // Business process ID
    public static final String MDC_KEY_USER_ID = "userId";
    public static final String MDC_KEY_CLIENT_IP = "clientIp";
    public static final String MDC_KEY_HTTP_METHOD = "httpMethod";
    public static final String MDC_KEY_REQUEST_URI = "requestUri";
    public static final String MDC_KEY_SPAN_ID = "spanId"; // Used if integrating with distributed tracing frameworks (OpenTelemetry/Sleuth)
    public static final String MDC_KEY_TENANT_ID = "tenantId";
    public static final String MDC_KEY_OPERATION_NAME = "operationName";
    public static final String MDC_KEY_REQUEST_TIMESTAMP = "requestTimestamp"; // Time request started processing in this service (ms)
    public static final String MDC_KEY_REQUEST_DURATION_MS = "requestDurationMs"; // Total duration of the request in this service (ms)
    public static final String MDC_KEY_USER_AGENT = "userAgent";

    // New: MDC Keys for Class Hierarchy Info
    public static final String MDC_KEY_SUPERCLASS_NAME = "superClass"; // Add this
    public static final String MDC_KEY_SUBCLASS_NAME = "subClass";     // Add this
}
