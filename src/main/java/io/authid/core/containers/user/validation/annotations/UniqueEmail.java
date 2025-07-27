package io.authid.core.containers.user.validation.annotations; // Atau di package lain yang sesuai

import io.authid.core.containers.user.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "Email already exists.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}