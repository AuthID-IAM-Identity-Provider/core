package io.authid.core.config;

import io.authid.core.shared.components.exception.GlobalTranslatableException;
import io.authid.core.shared.components.i18n.services.I18nService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final I18nService i18nService;

    @ExceptionHandler(GlobalTranslatableException.class)
    public ResponseEntity<Map<String, String>> handleTranslatableException(GlobalTranslatableException ex, Locale locale) {
        String translatedMessage = i18nService.translate(ex.getMessage(), locale, ex.getArgs());

        Map<String, String> response = Map.of(
                "errorCode", ex.getMessage(),
                "message", translatedMessage
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}