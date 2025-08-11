package io.authid.core.shared.components.events;

import io.authid.core.shared.components.events.contracts.DomainEvent;

import java.util.List;

public final class EntityLifecycleEvents {

    public record BeforeFetchAllEvent() implements DomainEvent {
    }

    public record AfterFetchAllEvent<T>(List<T> entities) implements DomainEvent {
    }

    public record BeforeCreateEvent<C>(C createRequest) implements DomainEvent {
    }

    public record AfterCreateEvent<T>(T entity) implements DomainEvent {
    }

    public record BeforeFindEvent<T>(T entity) implements DomainEvent {
    }

    public record AfterFindEvent<T>(T entity) implements DomainEvent {
    }

    public record BeforeUpdateEvent<ID, U>(ID entityId, U updateRequest) implements DomainEvent {
    }

    public record AfterUpdateEvent<T>(T entity) implements DomainEvent {
    }

    public record BeforeDeleteEvent<T>(T entity) implements DomainEvent {
    }

    public record AfterDeleteEvent<T>(T deletedEntity) implements DomainEvent {
    }
}
