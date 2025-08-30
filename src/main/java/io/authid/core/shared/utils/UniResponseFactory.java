package io.authid.core.shared.utils;

import io.authid.core.shared.components.i18n.services.I18nService;
import io.authid.core.shared.constants.DiagnosticContextConstant;
import io.authid.core.shared.constants.ErrorConstants;
import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UniResponseFactory {

    private final I18nService i18nService;
    private final Environment environment;

    public <R> ResponseEntity<UniResponse<R>> ok(R data) {
        return buildSuccessResponse(HttpStatus.OK, "Success", data, null, null);
    }

    public <R> ResponseEntity<UniResponse<List<R>>> ok(UniPaginatedResult<R> paginatedData) {
        return buildSuccessResponse(HttpStatus.OK, "Success", paginatedData.getData(), paginatedData.getPagination(), null);
    }

    public <R> ResponseEntity<UniResponse<R>> created(URI location, R data) {
        return buildSuccessResponse(HttpStatus.CREATED, "Resource created successfully", data, null, location);
    }

    public ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<UniResponse<Object>> error(ErrorCatalog catalog, Throwable throwable, Locale locale, Object... args) {
        UniError errorPayload = buildUniError(catalog, null, throwable, locale, args);
        String message = i18nService.translate(catalog.getBaseMessageKey(), locale, args);
        return buildErrorResponse(catalog.getHttpStatus(), message, errorPayload);
    }

    public ResponseEntity<UniResponse<Object>> error(ErrorCatalog catalog, Locale locale, Throwable throwable, Object... args) {
        UniError errorPayload = buildUniError(catalog, null, throwable, locale, args); // Teruskan throwable
        String message = i18nService.translate(catalog.getBaseMessageKey(), locale, args);
        return buildErrorResponse(catalog.getHttpStatus(), message, errorPayload);
    }

    public ResponseEntity<UniResponse<Object>> error(HttpStatus status, String code, String title, String message) {
        UniError errorPayload = UniError.builder()
                .code(code)
                .title(title)
                .cause(message)
                .build();
        return buildErrorResponse(status, message, errorPayload);
    }

    public ResponseEntity<UniResponse<Object>> error(HttpStatus status, String code, String title, String message, Throwable throwable) {
        String originalExceptionMessage = Optional.ofNullable(throwable)
            .map(Throwable::getMessage)
            .orElse("No specific exception message available.");
        UniError errorPayload = UniError.builder()
                .code(code)
                .title(title)
                .cause(originalExceptionMessage)
                .debugInfo(buildDebugInfo(throwable)) // Tambahkan info debug
                .build();
        return buildErrorResponse(status, message, errorPayload);
    }

    public ResponseEntity<UniResponse<Object>> validationError(ErrorCatalog catalog, Map<String, List<String>> fieldErrors, Throwable throwable, Locale locale) {
        UniError errorPayload = buildUniError(catalog, fieldErrors, throwable, locale);
        String message = i18nService.translate(catalog.getBaseMessageKey(), locale);
        return buildErrorResponse(catalog.getHttpStatus(), message, errorPayload);
    }

    private <R> ResponseEntity<UniResponse<R>> buildSuccessResponse(HttpStatus status, String message, R data, UniPagination pagination, URI location) {
        UniMeta meta = buildMeta(pagination);
        UniResponse<R> body = UniResponse.success(message, data, meta);

        if (location != null) {
            return ResponseEntity.created(location).body(body);
        }
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<UniResponse<Object>> buildErrorResponse(HttpStatus status, String message, UniError errorPayload) {
        UniMeta meta = buildMeta(null);
        UniResponse<Object> body = UniResponse.error(message, errorPayload, meta);
        return ResponseEntity.status(status).body(body);
    }
    
    private UniError buildUniError(ErrorCatalog catalog, Map<String, List<String>> fieldErrors, Throwable throwable, Locale locale, Object... args) {
        String baseKey = catalog.getBaseMessageKey();
        
        // Terjemahkan semua bagian yang relevan
        String title = i18nService.translate(baseKey + ".title", locale, args);
        String cause = i18nService.translate(baseKey + ".cause", locale, args);
        String action = i18nService.translate(baseKey + ".action", locale, args);

        return UniError.builder()
                .code(catalog.getCode())
                .category(catalog.getCategory())
                .module(catalog.getModule())
                .title(title)
                .cause(cause)
                .action(action)
                .fieldErrors(fieldErrors)
                .debugInfo(buildDebugInfo(throwable))
                .build();
    }

    private UniMeta buildMeta(UniPagination pagination) {
        return UniMeta.builder()
                .requestId(MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_ID))
                .traceId(MDC.get(DiagnosticContextConstant.MDC_KEY_TRACE_ID))
                .correlationId(MDC.get(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID))
                .timestamp(Instant.now())
                .pagination(pagination)
                .requestDurationMs(
                    Optional.ofNullable(MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_DURATION_MS))
                            .map(Long::parseLong)
                            .orElse(null)
                )
                .build();
    }

    private Map<String, Object> buildDebugInfo(Throwable throwable) {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains(ErrorConstants.PROFILE_PRODUCTION);

        if (isProduction || throwable == null) {
            return null;
        }
        
        return Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("exceptionClass", throwable.getClass().getName()),
                new AbstractMap.SimpleImmutableEntry<>("exceptionMessage", throwable.getMessage()),
                new AbstractMap.SimpleImmutableEntry<>("rootCause", ExceptionUtils.getRootCauseMessage(throwable))
            )
            .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty()) // Hanya ambil entri yang nilainya valid
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}