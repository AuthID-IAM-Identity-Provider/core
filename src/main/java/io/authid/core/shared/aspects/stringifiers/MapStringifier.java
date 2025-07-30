// src/main/java/io/authid/core/shared/aspects/stringifiers/MapStringifier.java
package io.authid.core.shared.aspects.stringifiers;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record MapStringifier(int maxEntriesToLog, Pattern sensitiveKeyPattern) implements ArgumentStringifier {

    @Override
    public boolean supports(Object arg) {
        return arg instanceof Map;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        Map<?, ?> map = (Map<?, ?>) arg;
        String content;
        if (map.size() <= maxEntriesToLog) {
            content = map.entrySet().stream()
                    .map(entry -> {
                        String key = manager.stringifyArg(entry.getKey());
                        String value = manager.stringifyArg(entry.getValue());
                        return maskSensitiveString(key, value); // Masking applies here
                    })
                    .collect(Collectors.joining(", "));
        } else {
            content = map.entrySet().stream().limit(maxEntriesToLog)
                    .map(entry -> {
                        String key = manager.stringifyArg(entry.getKey());
                        String value = manager.stringifyArg(entry.getValue());
                        return maskSensitiveString(key, value); // Masking applies here
                    })
                    .collect(Collectors.joining(", ")) + ", ...";
        }
        return map.getClass().getSimpleName() + "(" + content + ")";
    }

    private String maskSensitiveString(String key, String value) {
        if (value == null || key == null) {
            return value;
        }
        if (sensitiveKeyPattern.matcher(key).find()) {
            return "[SENSITIVE_DATA]";
        }
        return value;
    }
}