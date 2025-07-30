package io.authid.core.shared.aspects.stringifiers;

public record StringStringifier(int maxLengthToLog) implements ArgumentStringifier {

    @Override
    public boolean supports(Object arg) {
        return arg instanceof String;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        String value = (String) arg;
        if (value != null && value.length() > maxLengthToLog) {
            return value.substring(0, maxLengthToLog) + "...[TRUNCATED]";
        }
        return value;
    }
}