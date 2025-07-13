package io.authid.core.shared.components.database.factory;

import net.datafaker.Faker;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Factory<T> {
    private final Faker faker = new Faker();
    private int count = 1;
    private final List<Function<T, T>> states = new ArrayList<>();
    private final List<Consumer<T>> afterMakingCallbacks = new ArrayList<>();
    private final List<Consumer<T>> afterCreatingCallbacks = new ArrayList<>();
    public abstract T definition();

    protected Factory<T> afterMaking(Consumer<T> callback) {
        this.afterMakingCallbacks.add(callback);
        return this;
    }

    protected Factory<T> afterCreating(Consumer<T> callback) {
        this.afterCreatingCallbacks.add(callback);
        return this;
    }


    public Factory<T> count(int count) {
        this.count = count;
        return this;
    }

    protected Factory<T> state(Function<T, T> state) {
        this.states.add(state);
        return this;
    }

    public Object make() {
        List<T> entities = IntStream.range(0, this.count)
                .mapToObj(i -> {
                    T entity = this.definition();
                    for (Function<T, T> state : states) {
                        entity = state.apply(entity);
                    }
                    return entity;
                })
                .collect(Collectors.toList());

        entities.forEach(entity -> afterMakingCallbacks.forEach(callback -> callback.accept(entity)));
        return this.count == 1 ? entities.getFirst() : entities;
    }

    public Object create(JpaRepository<T, ?> repository) {
        Object madeObjects = this.make();
        List<T> entitiesToSave = (this.count == 1)
                ? Collections.singletonList((T) madeObjects)
                : (List<T>) madeObjects;

        repository.saveAll(entitiesToSave);

        entitiesToSave.forEach(entity -> afterCreatingCallbacks.forEach(callback -> callback.accept(entity)));

        return madeObjects;
    }


    public Factory<T> with(T overrides) {
        return this.state(entity -> {
            BeanUtils.copyProperties(overrides, entity, getNullPropertyNames(overrides));
            return entity;
        });
    }

    private static String[] getNullPropertyNames(Object source) {
        return new String[]{};
    }

    @SafeVarargs
    protected final <V> Factory<T> sequence(BiConsumer<T, V> setter, V... values) {
        final AtomicInteger sequenceCounter = new AtomicInteger(0);

        return this.state(entity -> {
            int index = sequenceCounter.getAndIncrement() % values.length;
            V nextValue = values[index];
            setter.accept(entity, nextValue);
            return entity;
        });
    }
}