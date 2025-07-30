package io.authid.core.shared.aspects.stringifiers;

/**
 * Interface for converting an argument object into a log-friendly string.
 */
public interface ArgumentStringifier {

    /**
     * Checks if this stringifier can handle the given argument type.
     * @param arg The argument object.
     * @return true if this stringifier can process the argument, false otherwise.
     */
    boolean supports(Object arg);

    /**
     * Converts the argument into a log-friendly string representation.
     * This method might recursively call the StringifierManager for nested objects.
     * @param arg The argument object.
     * @param manager The StringifierManager to handle nested object stringification.
     * @return The string representation of the argument.
     */
    String stringify(Object arg, StringifierManager manager);
}