package io.authid.core.shared.utils;

import io.authid.core.shared.components.i18n.services.I18nService;
import io.authid.core.shared.constants.DiagnosticContextConstant;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniResponseFactory {

    private final I18nService i18nService;

    // ===================================================================================
    // # 2xx - Success Responses
    // ===================================================================================

    /**
     * Mengembalikan 200 OK dengan pesan default "Success".
     */
    public <R> ResponseEntity<UniResponse<R>> ok(R data) {
        return ok(data, "Success");
    }

    /**
     * Mengembalikan 200 OK dengan pesan custom.
     */
    public <R> ResponseEntity<UniResponse<R>> ok(R data, String message) {
        return custom(HttpStatus.OK, message, data);
    }

    /**
     * Mengembalikan 200 OK untuk data paginasi dengan pesan default "Success".
     */
    public <R> ResponseEntity<UniResponse<List<R>>> ok(UniPaginatedResult<R> paginatedData) {
        return ok(paginatedData, "Success");
    }

    /**
     * Mengembalikan 200 OK untuk data paginasi dengan pesan custom.
     */
    public <R> ResponseEntity<UniResponse<List<R>>> ok(UniPaginatedResult<R> paginatedData, String message) {
        UniMeta meta = withMeta(paginatedData.getPagination());
        UniResponse<List<R>> response = UniResponse.success(message, paginatedData.getData(), meta);
        return ResponseEntity.ok(response);
    }

    /**
     * Mengembalikan 201 Created dengan header 'Location' dan pesan default.
     */
    public <R> ResponseEntity<UniResponse<R>> created(URI location, R data) {
        return created(location, data, "Resource created successfully");
    }

    /**
     * Mengembalikan 201 Created dengan header 'Location' dan pesan custom.
     */
    public <R> ResponseEntity<UniResponse<R>> created(URI location, R data, String message) {
        UniResponse<R> response = UniResponse.success(message, data, withMeta(null));
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Mengembalikan 204 No Content.
     */
    public ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * [METODE GENERIK SUKSES]
     * Mengembalikan respons sukses dengan status HTTP dan pesan yang bisa ditentukan secara bebas.
     * Gunakan ini untuk status 2xx yang tidak umum (misal: 202 Accepted).
     */
    public <R> ResponseEntity<UniResponse<R>> custom(HttpStatus status, String message, R data) {
        UniResponse<R> response = UniResponse.success(message, data, withMeta(null));
        return ResponseEntity.status(status).body(response);
    }

    // ===================================================================================
    // # Error Responses (Struktur Baru yang Lebih Cerdas)
    // ===================================================================================

    /**
     * [METODE UTAMA ERROR]
     * Membuat respons error lengkap untuk error konseptual (non-validasi).
     */
    public ResponseEntity<UniResponse<Object>> error(ErrorCatalog catalog, Locale locale, Object... args) {
        // Panggil helper untuk membangun payload, pass null untuk fieldErrors
        UniError errorPayload = buildUniError(catalog, null, locale, args);
        String fullClassNamePrefix = catalog.getClass().getName() + ".";
        String baseKey = catalog.getBaseMessageKey();

        // Bangun respons akhir
        UniResponse<Object> response = UniResponse.error(
                i18nService.translate(fullClassNamePrefix + baseKey, locale, args),
                errorPayload,
                withMeta(null)
        );
        return ResponseEntity.status(catalog.getHttpStatus()).body(response);
    }

    /**
     * [METODE UTAMA VALIDATION ERROR]
     * Membuat respons error lengkap khusus untuk kegagalan validasi.
     */
    public ResponseEntity<UniResponse<Object>> validationError(ErrorCatalog catalog, Map<String, List<String>> fieldErrors,Locale locale) {
        // Panggil helper untuk membangun payload, sertakan fieldErrors
        UniError errorPayload = buildUniError(catalog, fieldErrors, locale);

        // Bangun respons akhir, gunakan HttpStatus dari catalog untuk konsistensi
        UniResponse<Object> response = UniResponse.error(i18nService.translate(catalog.getBaseMessageKey()), errorPayload, withMeta(null));
        return ResponseEntity.status(catalog.getHttpStatus()).body(response);
    }

    /**
     * [PRIVATE HELPER]
     * Pusat logika untuk membangun objek UniError dari ErrorCatalog.
     * Mengeliminasi duplikasi kode antara metode error dan validationError.
     */
    private UniError buildUniError(ErrorCatalog catalog,  Map<String, List<String>> fieldErrors, Locale locale, Object... args) {
        // ✨ KUNCI PERBAIKAN: Dapatkan prefix dari nama kelas enum
        String fullClassNamePrefix = catalog.getClass().getName() + ".";
        String baseKey = catalog.getBaseMessageKey();

        // Terjemahkan semua bagian menggunakan kunci yang lengkap
        String title = i18nService.translate(fullClassNamePrefix + baseKey + ".title", locale, args);
        String cause = i18nService.translate(fullClassNamePrefix + baseKey + ".cause", locale, args);
        String action = i18nService.translate(fullClassNamePrefix + baseKey + ".action", locale, args);

        // Bangun payload UniError dengan builder
        return UniError.builder()
                .code(catalog.getCode())
                .category(catalog.getCategory())
                .module(catalog.getModule())
                .title(title)
                .cause(cause)
                .action(action)
                .fieldErrors(fieldErrors) // Akan diabaikan oleh Jackson jika null/kosong
                .build();
    }


    // ===================================================================================
    // # Private Helpers
    // ===================================================================================

    private UniMeta withMeta(UniPagination pagination) {
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
