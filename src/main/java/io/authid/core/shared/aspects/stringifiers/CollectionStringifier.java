package io.authid.core.shared.aspects.stringifiers;

import java.util.Collection;
import java.util.stream.Collectors;

public record CollectionStringifier(int maxElementsToLog) implements ArgumentStringifier {

    @Override
    public boolean supports(Object arg) {
        return arg instanceof Collection;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        Collection<?> collection = (Collection<?>) arg;
        String content;
        if (collection.size() <= maxElementsToLog) {
            content = collection.stream()
                    .map(manager::stringifyArg)
                    .collect(Collectors.joining(", "));
        } else {
            content = collection.stream().limit(maxElementsToLog)
                    .map(manager::stringifyArg)
                    .collect(Collectors.joining(", ")) + ", ...";
        }
        return collection.getClass().getSimpleName() + "(size=" + collection.size() + ", elements=[" + content + "])";
    }
}