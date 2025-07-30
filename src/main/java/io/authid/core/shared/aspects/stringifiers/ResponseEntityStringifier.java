package io.authid.core.shared.aspects.stringifiers;

import org.springframework.http.ResponseEntity;

public class ResponseEntityStringifier implements ArgumentStringifier {
    @Override
    public boolean supports(Object arg) {
        return arg instanceof ResponseEntity;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) arg;
        return "ResponseEntity(status=" + responseEntity.getStatusCode() + ", body=" + manager.stringifyArg(responseEntity.getBody()) + ")";
    }
}