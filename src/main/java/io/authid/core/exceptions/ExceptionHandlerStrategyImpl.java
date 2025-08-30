package io.authid.core.exceptions;

import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.authid.core.shared.utils.UniResponse;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerStrategyImpl {

  private final List<ExceptionHandlerStrategy> strategies;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<UniResponse<Object>> handleAllExceptions(Exception ex, Locale locale) {
    return strategies
      .stream()
      .filter(strategy -> strategy.canHandle(ex))
      .findFirst()
      .map(strategy -> strategy.handle(ex, locale))
      .orElseThrow(() -> new RuntimeException());
  }
}
