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
    private String message;
    private UniMeta meta;
    private T data;
    private UniError errors;

    public static <T> UniResponse<T> success(String message, T data, UniMeta meta) {
        UniResponse<T> response = new UniResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setMeta(meta);
        return response;
    }

    public static <T> UniResponse<T> error(String message, UniError errors, UniMeta meta) {
        UniResponse<T> response = new UniResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrors(errors);
        response.setMeta(meta);
        return response;
    }
}