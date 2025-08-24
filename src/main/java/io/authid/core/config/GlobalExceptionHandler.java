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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UniResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, Locale locale) {
        // Mengelompokkan semua error berdasarkan nama field
        Map<String, List<String>> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField, // Kunci Map adalah nama field
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList()) // Value adalah List dari pesan error
                ));

        // Panggil factory untuk membuat respons
        return responseFactory.validationError(SystemErrorCatalog.VALIDATION_ERROR, validationErrors, locale);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<UniResponse<Object>> handleNullPointerException(NullPointerException ex, Locale locale) {
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