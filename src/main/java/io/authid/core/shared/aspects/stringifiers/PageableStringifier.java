package io.authid.core.shared.aspects.stringifiers;

import org.springframework.data.domain.Pageable;

public class PageableStringifier implements ArgumentStringifier {
    @Override
    public boolean supports(Object arg) {
        return arg instanceof Pageable;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        Pageable pageable = (Pageable) arg;
        return "Pageable(page=" + pageable.getPageNumber() + ", size=" + pageable.getPageSize() + ", sort=" + pageable.getSort() + ")";
    }
}