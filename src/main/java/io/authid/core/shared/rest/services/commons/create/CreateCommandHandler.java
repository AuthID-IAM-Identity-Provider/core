package io.authid.core.shared.rest.services.commons.create;

import io.authid.core.shared.rest.contracts.RestCommandHandler;

public class CreateCommandHandler<T, ID, C, U> implements RestCommandHandler<T, CreateCommand<T, ID, C, U>> {
  @Override
  public T handle(CreateCommand<T, ID, C, U> command) {

    command.hooks().beforeCreate(command.createRequest());

    command.mapper().create(command.createRequest());

    T save = command.repository().save(
      command.hooks().onCreating(
        command.createRequest()
      )
    );

    command.hooks().afterCreate(save);

    return save;
  }
}
