package io.authid.core.shared.aspects.stringifiers;

import io.authid.core.shared.utils.UniResponse;

public class UniResponseStringifier implements ArgumentStringifier{

    @Override
    public boolean supports(Object arg) {
        return arg instanceof UniResponse;
    }

    @Override
    public String stringify(Object arg, StringifierManager manager) {
        UniResponse<?> uniResponse = (UniResponse<?>) arg;
        String dataSummary = "data=";
        if (uniResponse.getData() != null) {
            dataSummary += manager.stringifyArg(uniResponse.getData());
        } else {
            dataSummary += "null";
        }
        // Assuming getMeta() and getPagination() from your UniResponse structure
        return "UniResponse(status=" + uniResponse.isSuccess() + ", message='" + uniResponse.getMessage() + "', " + dataSummary + ", pagination=" + manager.stringifyArg(uniResponse.getMeta().getPagination()) + ")";
    }
}
