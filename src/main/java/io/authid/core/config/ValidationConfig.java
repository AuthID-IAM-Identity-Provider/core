package io.authid.core.config;

import io.authid.core.shared.components.i18n.configurations.CustomMessageInterpolator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import jakarta.validation.Validation;


@Configuration
public class ValidationConfig {
        
    private final AutowireCapableBeanFactory beanFactory;

    public ValidationConfig(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public Validator validator() {
        ConstraintValidatorFactory springConstraintValidatorFactory =
                new SpringConstraintValidatorFactory(beanFactory);

        MessageInterpolator defaultInterpolator = new ParameterMessageInterpolator();

        CustomMessageInterpolator customInterpolator = new CustomMessageInterpolator(defaultInterpolator);

        return Validation.buildDefaultValidatorFactory()
                .usingContext()
                .constraintValidatorFactory(springConstraintValidatorFactory) // <-- INI KUNCINYA
                .messageInterpolator(customInterpolator)
                .getValidator();
    }
}