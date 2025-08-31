// File: io/authid/core/exceptions/strategy/ValidationExceptionHandlerStrategy.java
package io.authid.core.exceptions.handlers;

import io.authid.core.exceptions.ExceptionHandlerStrategy;
import io.authid.core.exceptions.enums.SystemErrorCatalog;
import io.authid.core.shared.utils.UniResponse;
import io.authid.core.shared.utils.UniResponseFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component // Tandai sebagai Spring Bean agar bisa di-inject
@RequiredArgsConstructor
public class ValidationExceptionHandler implements ExceptionHandlerStrategy {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    private final UniResponseFactory responseFactory;

    @Override
    public boolean canHandle(Throwable throwable) {
        return throwable instanceof MethodArgumentNotValidException;
    }

    @Override
    public ResponseEntity<UniResponse<Object>> handle(Throwable throwable, Locale locale) {
        MethodArgumentNotValidException ex = (MethodArgumentNotValidException) throwable;
        Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
          .collect(Collectors.groupingBy(
                  FieldError::getField,
                  Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
          ));
        return responseFactory.validationError(SystemErrorCatalog.VALIDATION_ERROR, fieldErrors, throwable, locale);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}