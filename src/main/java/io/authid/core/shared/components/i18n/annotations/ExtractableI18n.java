package io.authid.core.shared.components.i18n.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // Ditargetkan ke field enum
public @interface ExtractableI18n {
    String value();

    String[] suffixes() default {};

    KeyType type() default KeyType.GENERAL;
}