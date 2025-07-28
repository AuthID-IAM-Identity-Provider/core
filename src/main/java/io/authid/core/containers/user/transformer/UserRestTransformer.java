package io.authid.core.containers.user.transformer;

import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.containers.user.response.*;
import io.authid.core.shared.components.i18n.extractors.I18n;
import io.authid.core.shared.components.i18n.services.I18nService;
import io.authid.core.shared.rest.transformer.RestTransformer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class UserRestTransformer implements RestTransformer<UserEntity, IndexUserResponse, DetailUserResponse, CreateUserResponse, UpdateUserResponse, DeleteUserResponse> {

    private final I18nService i18nService;

    public UserRestTransformer(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    public IndexUserResponse toIndex(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return IndexUserResponse
                .builder()
                .id(entity.getId().toString())
                .email(entity.getEmail())
                .firstName(entity.getName())
                .createdAt(entity.getCreatedAt().toString())
                .build();
    }

    @Override
    public DetailUserResponse toDetail(UserEntity entity) {
        return null;
    }

    @Override
    public CreateUserResponse toCreateResponse(UserEntity entity) {
        return null;
    }

    @Override
    public UpdateUserResponse toUpdateResponse(UserEntity entity) {
        return null;
    }

    @Override
    public DeleteUserResponse toDeleteResponse(UserEntity entity) {
        return null;
    }

    private Long getAccountAgeInDays(UserEntity entity) {
        if (entity.getCreatedAt() == null) {
            return null;
        }
        return Duration.between(entity.getCreatedAt(), Instant.now()).toDays();
    }

    private String getTimeSinceLastLogin(UserEntity entity) {
        if (entity.getLastLoginAt() == null) {
            return I18n.extract("last.login.never");
        }
        Duration duration = Duration.between(entity.getLastLoginAt(), Instant.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return I18n.extract("last.login.just.now");
        } else if (minutes < 60) {
            return minutes + I18n.extract("last.login.a.minutes.ago");
        } else if (minutes < 24 * 60) {
            long hours = duration.toHours();
            return hours + I18n.extract("last.login.a.hours.ago");
        } else {
            long days = duration.toDays();
            return days + I18n.extract("last.login.a.days.ago");
        }
    }
}
