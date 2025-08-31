package io.authid.core.exceptions.handlers;

import io.authid.core.exceptions.ExceptionCatalogMapping;
import io.authid.core.exceptions.ExceptionHandlerStrategy;
import io.authid.core.exceptions.enums.SystemErrorCatalog;
import io.authid.core.shared.components.i18n.services.I18nService;
import io.authid.core.shared.constants.DiagnosticContextConstant;
import io.authid.core.shared.constants.ErrorConstants;
import io.authid.core.shared.utils.UniResponse;
import io.authid.core.shared.utils.UniResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Arrays;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultExceptionHandler implements ExceptionHandlerStrategy {

    private final UniResponseFactory responseFactory;
    private final Environment environment;
    private final I18nService i18nService;

    /**
     * Data class lokal untuk membungkus konteks pesan error secara rapi.
     */
    private record MessageContext(String key, Object[] args) {}

    @Override
    public boolean canHandle(Throwable throwable) {
        return true;
    }

    @Override
    public ResponseEntity<UniResponse<Object>> handle(Throwable throwable, Locale locale){
        return ExceptionCatalogMapping.getMappedCatalog(throwable)
            .map(catalog -> handleMappedException(catalog, throwable, locale))
            .orElseGet(() -> handleGenericServerError(throwable, locale));
    }

    public ResponseEntity<UniResponse<Object>> handleGenericServerError(Throwable throwable, Locale locale) {
        final String traceId = MDC.get(DiagnosticContextConstant.MDC_KEY_TRACE_ID);
        final String requestId = MDC.get(DiagnosticContextConstant.MDC_KEY_REQUEST_ID);
        final String correlationId = MDC.get(DiagnosticContextConstant.MDC_KEY_CORRELATION_ID);
        log.warn("An unexpected internal server error occurred: handleGenericServerError",
            kv("requesId", requestId),
            kv("traceId", traceId),
            kv("correlationId", correlationId),
            kv("errorClass", throwable.getClass().getName()),
            kv("errorMessage", throwable.getMessage()),
            kv("originalException", throwable)
        );

        final MessageContext messageContext = Arrays.asList(environment.getActiveProfiles()).contains(ErrorConstants.PROFILE_PRODUCTION)
                ? new MessageContext(ErrorConstants.KEY_INTERNAL_ERROR_MESSAGE_PROD, new Object[]{})
                : new MessageContext(ErrorConstants.KEY_INTERNAL_ERROR_MESSAGE_DEV, new Object[]{throwable.getMessage()});

        final String title = i18nService.translate(ErrorConstants.KEY_INTERNAL_ERROR_TITLE, locale);
        final String message = i18nService.translate(messageContext.key(), locale, messageContext.args());

        return responseFactory.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorConstants.CODE_INTERNAL_SERVER_ERROR,
                title,
                message,
                throwable
        );
    }
    
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    private ResponseEntity<UniResponse<Object>> handleMappedException(SystemErrorCatalog catalog, Throwable throwable, Locale locale) {
        log.warn("Mapped exception found", 
            kv("errorClass", throwable.getClass().getSimpleName()), 
            kv("errorCode", catalog.getCode()),
            kv("errorMessage", throwable.getMessage()),
            kv("originalException", throwable)
        );
        return responseFactory.error(catalog, locale, throwable);
    }   
}