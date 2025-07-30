package io.authid.core.shared.aspects.stringifiers;

import io.authid.core.shared.utils.UniPaginatedResult;

public class UniPaginatedResultStringifier implements ArgumentStringifier {
    @Override
    public boolean supports(Object arg) {
        return arg instanceof UniPaginatedResult;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        UniPaginatedResult<?> paginatedResult = (UniPaginatedResult<?>) arg;
        return "UniPaginatedResult(dataSize=" + (paginatedResult.getData() != null ? paginatedResult.getData().size() : 0) + ", pagination=" + manager.stringifyArg(paginatedResult.getPagination()) + ")";
    }
}
