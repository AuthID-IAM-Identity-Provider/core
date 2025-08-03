package io.authid.core.shared.utils;

import io.authid.core.shared.constants.DiagnosticContextConstant;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import java.util.UUID;

public class UniResponseFactory {
    // Private constructor untuk mencegah instansiasi kelas utilitas.
    private UniResponseFactory() {}

    // ===================================================================================
    // # 2xx - Success Responses
    // ===================================================================================

    /**
     * Mengembalikan 200 OK dengan pesan default "Success".
     */
    public static <R> ResponseEntity<UniResponse<R>> ok(R data) {
        return ok(data, "Success");
    }

    /**
     * Mengembalikan 200 OK dengan pesan custom.
     */
    public static <R> ResponseEntity<UniResponse<R>> ok(R data, String message) {
        return custom(HttpStatus.OK, message, data);
    }

    /**
     * Mengembalikan 200 OK untuk data paginasi dengan pesan default "Success".
     */
    public static <R> ResponseEntity<UniResponse<List<R>>> ok(UniPaginatedResult<R> paginatedData) {
        return ok(paginatedData, "Success");
    }

    /**
     * Mengembalikan 200 OK untuk data paginasi dengan pesan custom.
     */
    public static <R> ResponseEntity<UniResponse<List<R>>> ok(UniPaginatedResult<R> paginatedData, String message) {
        UniMeta meta = withMeta(paginatedData.getPagination());
        UniResponse<List<R>> response = UniResponse.success(HttpStatus.OK.value(), message, paginatedData.getData(), meta);
        return ResponseEntity.ok(response);
    }

    /**
     * Mengembalikan 201 Created dengan header 'Location' dan pesan default.
     */
    public static <R> ResponseEntity<UniResponse<R>> created(URI location, R data) {
        return created(location, data, "Resource created successfully");
    }

    /**
     * Mengembalikan 201 Created dengan header 'Location' dan pesan custom.
     */
    public static <R> ResponseEntity<UniResponse<R>> created(URI location, R data, String message) {
        UniResponse<R> response = UniResponse.success(HttpStatus.CREATED.value(), message, data, withMeta(null));
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Mengembalikan 204 No Content.
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * [METODE GENERIK SUKSES]
     * Mengembalikan respons sukses dengan status HTTP dan pesan yang bisa ditentukan secara bebas.
     * Gunakan ini untuk status 2xx yang tidak umum (misal: 202 Accepted).
     */
    public static <R> ResponseEntity<UniResponse<R>> custom(HttpStatus status, String message, R data) {
        UniResponse<R> response = UniResponse.success(status.value(), message, data, withMeta(null));
        return ResponseEntity.status(status).body(response);
    }

    // ===================================================================================
    // # Error Responses (Struktur Baru yang Lebih Cerdas)
    // ===================================================================================

    /**
     * [METODE UTAMA ERROR]
     * Membuat respons error lengkap untuk error konseptual (non-validasi).
     */
    public static ResponseEntity<UniResponse<Object>> error(ErrorCatalog catalog, MessageSource messageSource, Locale locale, Object... args) {
        // Terjemahkan pesan judul
        String title = messageSource.getMessage(catalog.getBaseMessageKey() + ".title", args, catalog.getCode(), locale);

        // Panggil helper untuk membangun payload, pass null untuk fieldErrors
        UniError errorPayload = buildUniError(catalog, null, messageSource, locale, args);

        // Bangun respons akhir
        UniResponse<Object> response = UniResponse.error(catalog.getHttpStatus().value(), title, errorPayload, withMeta(null));
        return ResponseEntity.status(catalog.getHttpStatus()).body(response);
    }

    /**
     * [METODE UTAMA VALIDATION ERROR]
     * Membuat respons error lengkap khusus untuk kegagalan validasi.
     */
    public static ResponseEntity<UniResponse<Object>> validationError(ErrorCatalog catalog, List<FieldErrorDetail> fieldErrors, MessageSource messageSource, Locale locale) {
        // Terjemahkan pesan judul
        String topLevelMessage = messageSource.getMessage(catalog.getBaseMessageKey() + ".title", null, "Validation Failed", locale);

        // Panggil helper untuk membangun payload, sertakan fieldErrors
        UniError errorPayload = buildUniError(catalog, fieldErrors, messageSource, locale);

        // Bangun respons akhir, gunakan HttpStatus dari catalog untuk konsistensi
        UniResponse<Object> response = UniResponse.error(catalog.getHttpStatus().value(), topLevelMessage, errorPayload, withMeta(null));
        return ResponseEntity.status(catalog.getHttpStatus()).body(response);
    }

    /**
     * [PRIVATE HELPER]
     * Pusat logika untuk membangun objek UniError dari ErrorCatalog.
     * Mengeliminasi duplikasi kode antara metode error dan validationError.
     */
    private static UniError buildUniError(ErrorCatalog catalog, List<FieldErrorDetail> fieldErrors, MessageSource messageSource, Locale locale, Object... args) {
        // Terjemahkan pesan cause dan action
        String cause = messageSource.getMessage(catalog.getBaseMessageKey() + ".cause", args, "", locale);
        String action = messageSource.getMessage(catalog.getBaseMessageKey() + ".action", args, "", locale);

        // Bangun payload UniError dengan builder
        return UniError.builder()
                .code(catalog.getCode())
                .category(catalog.getCategory())
                .module(catalog.getModule())
                .cause(cause)
                .action(action)
                .fieldErrors(fieldErrors) // Akan diabaikan oleh Jackson jika null/kosong
                .build();
    }


    // ===================================================================================
    // # Private Helpers
    // ===================================================================================

    private static UniMeta withMeta(UniPagination pagination) {
        return UniMeta.builder()
            .requestId(MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_ID)) // Ambil dari MDC
            .traceId(MDC.get(DiagnosticContextConstant.MDC_KEY_TRACE_ID))     // Ambil dari MDC
            .correlationId(MDC.get(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID)) // Ambil dari MDC
            .userId(MDC.get(DiagnosticContextConstant.MDC_KEY_USER_ID))         // Ambil dari MDC
            .clientIp(MDC.get(DiagnosticContextConstant.MDC_KEY_CLIENT_IP))     // Ambil dari MDC
            .tenantId(MDC.get(DiagnosticContextConstant.MDC_KEY_TENANT_ID))     // Ambil dari MDC
            .operationName(MDC.get(DiagnosticContextConstant.MDC_KEY_OPERATION_NAME)) // Ambil dari MDC
            .requestDurationMs(MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS) != null ?
                    Long.parseLong(MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS)) : null) // Convert to Long
            // Pastikan sessionId juga diset jika ada di MDC
            .sessionId(MDC.get("sessionIdKeyAnda")) // Ganti dengan MDC key yang relevan jika ada
            .timestamp(Instant.now())
            .pagination(pagination)
            .build();
    }
}
