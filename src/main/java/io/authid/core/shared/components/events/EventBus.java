package io.authid.core.shared.components.events;

import io.authid.core.shared.components.events.contracts.DomainEvent;
import io.authid.core.shared.components.events.contracts.DomainEventListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBus {

    @Getter
    private final Map<Class<? extends DomainEvent>, List<DomainEventListener<?>>> listeners = new HashMap<>();

    /**
     * Dispatch given event
     *
     * @param event The dispatched event class
     */
    public void dispatch(DomainEvent event) {
        Optional.ofNullable(listeners.get(event.getClass()))
            .ifPresent(listeners -> listeners
                .forEach(listener -> {
                    try {
                        DomainEventListener<DomainEvent> typedListener = (DomainEventListener<DomainEvent>) listener;
                        typedListener.handle(event);
                    } catch (Exception e) {
                        log.error("Error occurred while handling event", e);
                    }
                })
            );
    }

    /**
     * Listen for any future event
     *
     * @param eventClass The class of event.
     * @param action     The consumer
     * @param <E>        The event
     */
    public <E extends DomainEvent> void listen(Class<E> eventClass, Consumer<E> action) {
        DomainEventListener<E> listener = action::accept;
        this.listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
    }
}
