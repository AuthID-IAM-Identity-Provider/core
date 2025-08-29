package io.authid.core.shared.rest.services.commons.update;

import io.authid.core.shared.rest.contracts.RestCommandHandler;
import io.authid.core.shared.rest.contracts.hooks.RestServiceHooks;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateByIdCommandHandler<T, ID, C, U> implements RestCommandHandler<T, UpdateByIdCommand<T, ID, C, U>> {

    @Override
    public T handle(UpdateByIdCommand<T, ID, C, U> command) throws EntityNotFoundException {

        prepareCommand(command, command.hooks());

        T executedResult = command.repository()
            .findById(command.uuid())
            .map(entity -> {
                return runCommand(entity, command);
            })
            .orElseThrow(() -> {
                flushCommand();
                return command.hooks().onNotFound(command.uuid());
            });

        flushCommand();

        return executedResult;
    }

    private void prepareCommand(UpdateByIdCommand<T, ID, C, U> command, RestServiceHooks<T, ID, C, U> lifecycleHooks) {
        lifecycleHooks.beforeFindById(command.uuid());
        lifecycleHooks.onFindingById(command.uuid());
    }

    private T runCommand(T entity, UpdateByIdCommand<T, ID, C, U> command){
        command.hooks().afterFindById(entity);
        command.hooks().beforeUpdate(command.updateRequest());
        T updated = command.repository().save(command.hooks().onUpdating(entity));
        command.hooks().afterUpdate(updated);
        return updated;
    }

    private void flushCommand(){
    }
}