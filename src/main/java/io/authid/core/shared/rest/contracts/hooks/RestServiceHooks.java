package io.authid.core.shared.rest.contracts.hooks;

import io.authid.core.shared.rest.contracts.hooks.commons.*;

public interface RestServiceHooks<T, ID, C, U> extends
    CreateHooks<T, C>,
    UpdateHooks<T, ID, U>,
    DeleteByIdHooks<T, ID>,
    FetchByIdHooks<T, ID>,
    FetchAllHooks<T> {
}
