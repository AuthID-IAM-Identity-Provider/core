package io.authid.core.containers.user.validation.validator;

import io.authid.core.containers.user.repositories.UserRepository;
import io.authid.core.containers.user.validation.annotations.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private UserRepository userRepository;

    public UniqueEmailValidator(
        UserRepository userRepository       
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true;
        }
        return !userRepository.existsByEmail((email));
    }
}