package io.authid.core.config;

import io.authid.core.shared.components.i18n.configurations.CustomMessageInterpolator;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.validation.Validation;


@Configuration
public class ValidationConfig {

    @Bean
    public Validator validator() {
        MessageInterpolator defaultInterpolator = new ParameterMessageInterpolator();

        CustomMessageInterpolator customInterpolator = new CustomMessageInterpolator(defaultInterpolator);

        return Validation.buildDefaultValidatorFactory()
                .usingContext()
                .messageInterpolator(customInterpolator)
                .getValidator();
    }
}