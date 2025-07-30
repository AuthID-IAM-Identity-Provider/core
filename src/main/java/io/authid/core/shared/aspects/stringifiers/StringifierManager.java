// src/main/java/io/authid/core/shared/aspects/stringifiers/StringifierManager.java
package io.authid.core.shared.aspects.stringifiers;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Manages a list of ArgumentStringifier implementations and provides a facade
 * for stringifying arguments.
 */
public record StringifierManager(List<ArgumentStringifier> stringifiers) {

    public StringifierManager(List<ArgumentStringifier> stringifiers) {
        // Ensure default stringifier is last or explicitly handle fallback
        this.stringifiers = Objects.requireNonNull(stringifiers, "Stringifier list cannot be null");
    }

    /**
     * Converts the given argument into a log-friendly string representation
     * by delegating to the appropriate Stringifier.
     *
     * @param arg The argument object to stringify.
     * @return The string representation.
     */
    public String stringifyArg(Object arg) {
        if (arg == null) {
            return "null";
        }

        for (ArgumentStringifier stringifier : stringifiers) {
            if (stringifier.supports(arg)) {
                return stringifier.stringify(arg, this); // Pass 'this' for recursive calls
            }
        }
        // Fallback to default if no specific stringifier is found (should be handled by DefaultStringifier)
        return arg.toString();
    }

    // Static factory method to create a default manager with common stringifiers
    public static StringifierManager createDefault(int maxStringLength, int maxMapEntries, int maxCollectionElements, Pattern sensitiveKeyPattern) {
        return new StringifierManager(List.of(
                new ResponseEntityStringifier(),
                new UniResponseStringifier(),
                new UniPaginatedResultStringifier(),
                new UniPaginationStringifier(),
                new PageableStringifier(),
                new MapStringifier(maxMapEntries, sensitiveKeyPattern),
                new CollectionStringifier(maxCollectionElements),
                new StringStringifier(maxStringLength),
                new DefaultStringifier() // This must be last as it supports all types
        ));
    }
}