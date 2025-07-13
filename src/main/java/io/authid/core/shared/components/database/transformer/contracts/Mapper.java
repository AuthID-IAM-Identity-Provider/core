package io.authid.core.shared.components.database.transformer.contracts;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface Mapper<E, R> {

    R toResource(E entity);

    E toEntity(R resource);

    default List<R> toResourceList(Collection<E> entities) {
        if(entities == null){
            return List.of();
        }

        return entities.stream().map(this::toResource).collect(Collectors.toList());
    }

    default List<E> toEntityList(Collection<R> resources){
        if (resources == null) {
            return List.of();
        }
        return resources.stream().map(this::toEntity).collect(Collectors.toList());
    }
}