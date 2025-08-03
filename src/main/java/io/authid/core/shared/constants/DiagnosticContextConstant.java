package io.authid.core.shared.constants;

/**
 * Menyimpan konstanta untuk header HTTP dan kunci Mapped Diagnostic Context (MDC)
 * yang digunakan di seluruh aplikasi untuk tujuan logging, tracing, dan audit.
 */
public final class DiagnosticContextConstant {

    // --- HTTP Headers ---

    /**
     * ID unik yang melacak seluruh perjalanan sebuah permintaan melalui berbagai layanan.
     * Standar untuk distributed tracing.
     */
    public static final String HTTP_HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * ID unik untuk satu permintaan HTTP spesifik yang diterima oleh layanan ini.
     * Berguna untuk debugging hop-by-hop.
     */
    public static final String HTTP_HEADER_REQUEST_ID = "X-Request-Id";

    /**
     * ID yang menghubungkan beberapa permintaan yang merupakan bagian dari satu proses bisnis yang sama.
     */
    public static final String HTTP_HEADER_CORRELATION_ID = "X-Correlation-Id";

    /**
     * Header standar untuk mengidentifikasi alamat IP asli klien ketika melewati proxy.
     */
    public static final String HTTP_HEADER_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Header standar yang berisi informasi tentang aplikasi klien (browser, perangkat, dll).
     */
    public static final String HTTP_HEADER_USER_AGENT = "User-Agent";

    /**
     * Kunci publik yang mengidentifikasi aplikasi klien yang memanggil API.
     */
    public static final String HTTP_HEADER_API_KEY = "X-Api-Key";

    /**
     * Tanda tangan digital (HMAC) untuk memverifikasi integritas dan keaslian permintaan.
     */
    public static final String HTTP_HEADER_API_SIGNATURE = "X-Signature";

    /**
     * Header untuk menentukan versi/algoritma tanda tangan yang digunakan oleh klien.
     */
    public static final String HTTP_HEADER_API_ALGORITHM = "X-Signature-Algorithm";


    // --- MDC (Mapped Diagnostic Context) Keys ---

    // Kunci untuk Tracing & Korelasi
    /**
     * Kunci MDC untuk `traceId`, merefleksikan `X-Trace-Id` untuk korelasi log di seluruh layanan.
     */
    public static final String MDC_KEY_TRACE_ID = "traceId";
    /**
     * Kunci MDC untuk `requestId`, merefleksikan `X-Request-Id` untuk identifikasi unik per hop.
     */
    public static final String MDC_KEY_REQUEST_ID = "requestId";
    /**
     * Kunci MDC untuk `correlationId`, mengidentifikasi proses bisnis yang berjalan lintas permintaan.
     */
    public static final String MDC_KEY_CORRELATION_ID = "correlationId";
    /**
     * Kunci MDC untuk `spanId`, digunakan oleh framework distributed tracing (OpenTelemetry, Sleuth).
     */
    public static final String MDC_KEY_SPAN_ID = "spanId";

    // Kunci untuk Konteks Request
    /**
     * Kunci MDC untuk metode HTTP (GET, POST, dll).
     */
    public static final String MDC_KEY_HTTP_METHOD = "httpMethod";
    /**
     * Kunci MDC untuk URI permintaan yang diminta klien.
     */
    public static final String MDC_KEY_REQUEST_URI = "requestUri";
    /**
     * Kunci MDC untuk alamat IP klien yang melakukan permintaan.
     */
    public static final String MDC_KEY_CLIENT_IP = "clientIp";
    /**
     * Kunci MDC untuk User-Agent klien.
     */
    public static final String MDC_KEY_USER_AGENT = "userAgent";

    // Kunci untuk Konteks Bisnis & Pengguna
    /**
     * Kunci MDC untuk ID pengguna yang terotentikasi.
     */
    public static final String MDC_KEY_USER_ID = "userId";
    /**
     * Kunci MDC untuk ID tenant dalam arsitektur multi-tenant.
     */
    public static final String MDC_KEY_TENANT_ID = "tenantId";
    /**
     * Kunci MDC untuk ID aplikasi klien (diambil dari Api Key).
     */
    public static final String MDC_KEY_CLIENT_ID = "clientId";

    // Kunci untuk Konteks Operasi & Kinerja
    /**
     * Kunci MDC untuk nama operasi atau method yang sedang berjalan.
     */
    public static final String MDC_KEY_OPERATION_NAME = "operationName";
    /**
     * Kunci MDC untuk waktu (epoch ms) saat permintaan mulai diproses.
     */
    public static final String MDC_KEY_REQUEST_TIMESTAMP = "requestTimestamp";
    /**
     * Kunci MDC untuk durasi total pemrosesan permintaan dalam milidetik.
     */
    public static final String MDC_KEY_REQUEST_DURATION_MS = "requestDurationMs";

    // Kunci untuk Konteks Debugging Lanjutan
    /**
     * Kunci MDC untuk nama kelas utama yang menjadi konteks log.
     */
    public static final String MDC_KEY_CONTEXT_CLASS = "contextClass";
    /**
     * Kunci MDC untuk nama sub-kelas jika konteks yang lebih spesifik diperlukan.
     */
    public static final String MDC_KEY_CONTEXT_SUBCLASS = "contextSubclass";
}