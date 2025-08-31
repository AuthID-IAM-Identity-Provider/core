package io.authid.core.shared.utils;

import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import io.authid.core.shared.components.i18n.services.I18nService;
import io.authid.core.shared.constants.DiagnosticContextConstant;
import io.authid.core.shared.constants.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class UniResponseFactory {

    private final I18nService i18nService;
    private final Environment environment;
    private final Map<Class<? extends Throwable>, Function<Throwable, Map<String, Object>>> debugInfoHandlers;

    public UniResponseFactory(I18nService i18nService, Environment environment) {
        this.i18nService = i18nService;
        this.environment = environment;
        this.debugInfoHandlers = new HashMap<>();
        this.debugInfoHandlers.put(
            MethodArgumentNotValidException.class,
            throwable -> formatValidationException((MethodArgumentNotValidException) throwable)
        );
    }

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
        log.warn("Consider using the standardized error method signature: error(catalog, throwable, locale, ...)");
        return error(catalog, throwable, locale, args);
    }

    public ResponseEntity<UniResponse<Object>> validationError(ErrorCatalog catalog, Map<String, List<String>> fieldErrors, Throwable throwable, Locale locale) {
        UniError errorPayload = buildUniError(catalog, fieldErrors, throwable, locale);
        String message = i18nService.translate(catalog.getBaseMessageKey(), locale);
        return buildErrorResponse(catalog.getHttpStatus(), message, errorPayload);
    }

    public ResponseEntity<UniResponse<Object>> error(HttpStatus status, String code, String title, String message) {
        return error(status, code, title, message, null);
    }

    public ResponseEntity<UniResponse<Object>> error(HttpStatus status, String code, String title, String message, Throwable throwable) {
        UniError.UniErrorBuilder errorBuilder = UniError.builder()
            .code(code)
            .title(title)
            .cause(message)
            .debugInfo(buildDebugInfo(throwable));
        return buildErrorResponse(status, message, errorBuilder.build());
    }

    private <R> ResponseEntity<UniResponse<R>> buildSuccessResponse(HttpStatus status, String message, R data, UniPagination pagination, URI location) {
        UniMeta meta = buildMeta(pagination);
        UniResponse<R> body = UniResponse.success(message, data, meta);
        return location != null ? ResponseEntity.created(location).body(body) : ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<UniResponse<Object>> buildErrorResponse(HttpStatus status, String message, UniError errorPayload) {
        UniMeta meta = buildMeta(null);
        UniResponse<Object> body = UniResponse.error(message, errorPayload, meta);
        return ResponseEntity.status(status).body(body);
    }

    private UniError buildUniError(ErrorCatalog catalog, Map<String, List<String>> fieldErrors, Throwable throwable, Locale locale, Object... args) {
        String baseKey = catalog.getBaseMessageKey();
        return UniError.builder()
            .code(catalog.getCode())
            .category(catalog.getCategory())
            .module(catalog.getModule())
            .title(i18nService.translate(baseKey + ".title", locale, args))
            .cause(i18nService.translate(baseKey + ".cause", locale, args))
            .action(i18nService.translate(baseKey + ".action", locale, args))
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
                    .map(Long::parseLong).orElse(null)
            ).build();
    }

    private Map<String, Object> buildDebugInfo(Throwable throwable) {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains(ErrorConstants.PROFILE_PRODUCTION);
        if (isProduction || throwable == null) {
            return null;
        }
        Function<Throwable, Map<String, Object>> handler =
            debugInfoHandlers.getOrDefault(throwable.getClass(), this::formatGenericException);
        return handler.apply(throwable);
    }

    private Map<String, Object> formatValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> debugMap = new LinkedHashMap<>();
        debugMap.put("exceptionClass", ex.getClass().getName());
        debugMap.put("errorCount", ex.getErrorCount());
        List<Map<String, Object>> validationDetails = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldErrorDetail)
            .collect(Collectors.toList());
        debugMap.put("validationErrors", validationDetails);
        return debugMap;
    }

    private Map<String, Object> formatFieldErrorDetail(FieldError fieldError) {
        Map<String, Object> errorDetail = new LinkedHashMap<>();
        errorDetail.put("field", fieldError.getField());
        errorDetail.put("rejectedValue", fieldError.getRejectedValue());
        errorDetail.put("messageKey", fieldError.getDefaultMessage());
        errorDetail.put("validator", fieldError.getCode());
        return errorDetail;
    }

    private Map<String, Object> formatGenericException(Throwable throwable) {
        return Stream.of(
                new AbstractMap.SimpleImmutableEntry<>("exceptionClass", throwable.getClass().getName()),
                new AbstractMap.SimpleImmutableEntry<>("exceptionMessage", throwable.getMessage()),
                new AbstractMap.SimpleImmutableEntry<>("rootCause", ExceptionUtils.getRootCauseMessage(throwable))
            )
            .filter(entry -> entry.getValue() != null && !entry.getValue().toString().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}