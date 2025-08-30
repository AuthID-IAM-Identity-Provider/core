package io.authid.core.exceptions;

import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;

import io.authid.core.shared.utils.UniResponse;

public interface ExceptionHandlerStrategy extends Ordered {
  /**
   * Check that handler can handle given exception.
   * 
   * @param throwable Exception thrown.
   * @return true if can be handle, false otherwise.
   */
  boolean canHandle(Throwable throwable);
  
  /**
   * Handle excecuting logic for error handling startegy.
   * 
   * @param throwable Exception thrown.
   * @param locale Locale from request.
   * @return Response.
   */
  ResponseEntity<UniResponse<Object>> handle(Throwable throwable, Locale locale);

  @Override
  int getOrder();
}
