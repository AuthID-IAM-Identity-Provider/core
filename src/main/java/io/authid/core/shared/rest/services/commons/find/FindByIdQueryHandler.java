package io.authid.core.shared.rest.services.commons.find;

import io.authid.core.shared.rest.contracts.RestQueryHandler;
import io.authid.core.shared.rest.contracts.hooks.commons.FetchByIdHooks;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindByIdQueryHandler<T, ID> implements RestQueryHandler<T, FindByIdQuery<T, ID>> {

    @Override
    public T handle(FindByIdQuery<T, ID> query) throws EntityNotFoundException {

        FetchByIdHooks<T, ID> hooks = query.hooks();

        hooks.beforeFindById(query.uuid());

        return query.repository()
            .findOne(
                (root, q, cb) -> cb.equal(root.get("id"), query.uuid())
            )
            .map(entity -> {
                hooks.afterFindById(entity);
                return entity;
            })
            .orElseThrow(() -> hooks.onNotFound(query.uuid()));
    }
}