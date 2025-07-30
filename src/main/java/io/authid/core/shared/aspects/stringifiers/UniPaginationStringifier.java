package io.authid.core.shared.aspects.stringifiers;

import io.authid.core.shared.utils.UniPagination; // Pastikan import ini sesuai

public class UniPaginationStringifier implements ArgumentStringifier {
    @Override
    public boolean supports(Object arg) {
        return arg instanceof UniPagination;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        UniPagination pagination = (UniPagination) arg;
        return "UniPagination(type=" + pagination.getType() + ", page=" + pagination.getPage() + ", perPage=" + pagination.getPerPage() + ", totalItems=" + pagination.getTotalItems() + ", hasMore=" + pagination.getHasMore() + ", nextCursor='" + pagination.getNextCursor() + "')";
    }
}