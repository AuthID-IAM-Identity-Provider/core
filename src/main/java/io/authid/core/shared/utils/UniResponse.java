package io.authid.core.shared.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private UniMeta meta;
    private T data;
    private Object errors;

    public static <T> UniResponse<T> success(int code, String message, T data, UniMeta meta) {
        UniResponse<T> response = new UniResponse<>();
        response.setSuccess(true);
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        response.setMeta(meta);
        return response;
    }

    public static <T> UniResponse<T> error(int code, String message, Object errors, UniMeta meta) {
        UniResponse<T> response = new UniResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setErrors(errors);
        response.setMeta(meta);
        return response;
    }
}