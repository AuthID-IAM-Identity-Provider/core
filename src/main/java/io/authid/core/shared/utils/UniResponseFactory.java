package io.authid.core.shared.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Instant;
import java.util.List;
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
    // # 4xx & 5xx - Error Responses
    // ===================================================================================

    /**
     * Mengembalikan 400 Bad Request.
     */
    public static ResponseEntity<UniResponse<Object>> badRequest(Object errorDetails) {
        return error(HttpStatus.BAD_REQUEST, "Bad Request", errorDetails);
    }

    /**
     * Mengembalikan 404 Not Found.
     */
    public static ResponseEntity<UniResponse<Object>> notFound(Object errorDetails) {
        return error(HttpStatus.NOT_FOUND, "Resource Not Found", errorDetails);
    }

    /**
     * [METODE GENERIK ERROR]
     * Mengembalikan respons error dengan status HTTP, pesan, dan detail error yang bisa ditentukan secara bebas.
     * Gunakan ini untuk semua status 4xx dan 5xx (misal: 403, 409, 503).
     */
    public static ResponseEntity<UniResponse<Object>> error(HttpStatus status, String message, Object errorDetails) {
        UniResponse<Object> response = UniResponse.error(status.value(), message, errorDetails, withMeta(null));
        return ResponseEntity.status(status).body(response);
    }

    // ===================================================================================
    // # Private Helpers
    // ===================================================================================

    private static UniMeta withMeta(UniPagination pagination) {
        return UniMeta.builder()
                .timestamp(Instant.now())
                .pagination(pagination)
                .requestId(UUID.randomUUID().toString())
                .build();
    }
}
