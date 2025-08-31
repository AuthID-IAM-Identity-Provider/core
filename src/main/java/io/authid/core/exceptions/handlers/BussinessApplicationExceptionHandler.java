package io.authid.core.exceptions.handlers;

import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.authid.core.exceptions.ExceptionHandlerStrategy;
import io.authid.core.shared.components.exception.BussinessApplicationException;
import io.authid.core.shared.utils.UniResponse;
import io.authid.core.shared.utils.UniResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
public class BussinessApplicationExceptionHandler implements ExceptionHandlerStrategy {
  
  private final UniResponseFactory responseFactory;

  @Override
  public boolean canHandle(Throwable throwable) {
    return throwable instanceof BussinessApplicationException;
  }

  @Override
  public ResponseEntity<UniResponse<Object>> handle(Throwable throwable, Locale locale) {
    BussinessApplicationException ex = (BussinessApplicationException) throwable;

    log.warn("Business Application exception occurred: handle", 
      kv("errorClass", ex.getClass().getSimpleName()),
      kv("errorMessage", ex.getMessage()),
      kv("errorCatalog", ex.getErrorCatalog()),
      kv("errorCode", ex.getErrorCatalog().getCode())
    );

    return responseFactory.error(
      ex.getErrorCatalog(), 
      throwable, 
      locale, 
      ex.getArgs()
    );
  }

  @Override
  public int getOrder() {
    return 10;
  }
}
