package io.authid.core.shared.components.events.contracts;

@FunctionalInterface
public interface DomainEventListener<E extends DomainEvent> {
    void handle(E event);
}
