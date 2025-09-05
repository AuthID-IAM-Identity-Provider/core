package io.authid.core.shared.rest.services.commons.create;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.jpa.repository.JpaRepository;

import io.authid.core.shared.rest.contracts.hooks.RestServiceHooks;
import io.authid.core.shared.rest.mapper.RestMapper;

public record CreateCommand<T, ID, C, U> (
  C createRequest,
  JpaRepository<T, ID> repository,
  RestMapper<T, C, U> mapper,
  RestServiceHooks<T, ID, C, U> hooks
){
}
