package io.authid.core.config;

import io.authid.core.shared.components.exception.BaseApplicationException;
import io.authid.core.shared.components.exception.ResourceNotFoundErrorException;
import io.authid.core.shared.constants.DiagnosticContextConstant;
import io.authid.core.shared.enums.SystemErrorCatalog;
import io.authid.core.shared.utils.UniResponse;
import io.authid.core.shared.utils.UniResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final UniResponseFactory responseFactory;

    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<UniResponse<Object>> handleApplicationException(BaseApplicationException ex, Locale locale) {
        log.info("getArgs :  {}", Arrays.toString(ex.getArgs()));
        return responseFactory.error(ex.getErrorCatalog(), locale, ex.getArgs());
    }

    @ExceptionHandler(ResourceNotFoundErrorException.class)
    public ResponseEntity<UniResponse<Object>> handleResourceNotFoundException(ResourceNotFoundErrorException ex, Locale locale){
        log.info("Given id : {} is not found", ex.getArgs());
        return responseFactory.error(ex.getErrorCatalog(), locale, ex.getArgs());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<UniResponse<Object>> handleNoResourceFoundException(NoResourceFoundException ex, Locale locale) {
        log.warn("No resource found for URI: {}", ex.getResourcePath());
        return responseFactory.error(SystemErrorCatalog.ROUTE_NOT_FOUND, locale, new Object[]{ex.getResourcePath(), ex.getMessage(), ex.getRootCause()});
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<UniResponse<Object>> handleNullPoinEntity(NullPointerException ex, Locale locale) {
        log.warn("Null Pointer Exception: {}", ex.getCause());
        return responseFactory.error(SystemErrorCatalog.NULL_POINTER_EXCEPTION, locale, new Object[]{ex.getCause(), ex.getMessage()});
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UniResponse<Object>> handleAllUncaughtExceptions(Exception ex, Locale locale) {
        log.error("An unexpected internal service error occurred: {}", ex.getMessage(), ex);
        return responseFactory.error(
                SystemErrorCatalog.INTERNAL_SERVER_ERROR,
                locale,
                new Object[]{ex.getMessage()},
                MDC.get(DiagnosticContextConstant.HTTP_HEADER_TRACE_ID)
        );
    }
}