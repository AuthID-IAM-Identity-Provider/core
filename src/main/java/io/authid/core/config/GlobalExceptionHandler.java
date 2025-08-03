package io.authid.core.config;

import io.authid.core.shared.components.exception.BaseApplicationException;
import io.authid.core.shared.utils.UniResponse;
import io.authid.core.shared.utils.UniResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}