package io.authid.core.shared.aspects.stringifiers;

/**
 * Fallback stringifier for any object not handled by specific stringifiers.
 * Relies on the object's toString() method.
 */
public class DefaultStringifier implements ArgumentStringifier {
    @Override
    public boolean supports(Object arg) {
        return true; // Supports all types
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        // Fallback: If no specific handler, try standard toString()
        // Ensure your DTOs/Entities have meaningful toString() implementations (e.g., using Lombok @Data)
        return arg.toString();
    }
}