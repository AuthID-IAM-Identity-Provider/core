package io.authid.core.containers.user.transformer;

import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.containers.user.response.*;
import io.authid.core.shared.components.i18n.services.I18nService;
import io.authid.core.shared.rest.transformer.RestTransformer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class UserRestTransformer implements RestTransformer<
    UserEntity, 
    IndexUserResponse, 
    DetailUserResponse, 
    CreateUserResponse, 
    UpdateUserResponse, 
    DeleteUserResponse> {

    public UserRestTransformer(I18nService i18nService) {
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
        return DetailUserResponse.builder()
                .id(entity.getId().toString())
                .firstName(entity.getName())
                .email(entity.getEmail())
                .createdAt(String.valueOf(entity.getCreatedAt()))
                .isEmailVerified(entity.isVerified()) // Delegasi ke metode di UserEntity
                .isAccountLocked(entity.isAccountLocked()) // Delegasi ke metode di UserEntity
                .hasTwoFactorAuth(entity.hasTwoFactor()) // Delegasi ke metode di UserEntity
                .canLogin(entity.canLogin()) // Delegasi ke metode di UserEntity
                .lastLoginAt(String.valueOf(entity.getLastLoginAt())) // Raw, but often displayed here
                .lastLoginIp(entity.getLastLoginIp()) // Raw, be careful with exposure
                .loginCount(String.valueOf(entity.getLoginCount()))   // Raw, but useful for detail
                .failedLoginAttempts(String.valueOf(entity.getFailedLoginAttempts())) // Raw, but useful for detail (careful with exposure)
                .daysSinceLastLogin((String) calculateDaysSinceLastLogin(entity.getLastLoginAt())) // Computed
                .daysSinceCreated(String.valueOf(calculateDaysSinceCreated(entity.getCreatedAt())))
                .build();
    }

    // Helper method for computed field
    private Object calculateDaysSinceLastLogin(Instant lastLoginAt) {
        if (lastLoginAt == null) {
            return null; // Or some default like 0 if never logged in
        }
        Duration duration = Duration.between(lastLoginAt, Instant.now());
        return (int) duration.toDays();
    }

    private Object calculateDaysSinceCreated(Instant createdAt) {
        if (createdAt == null) {
            return null; // Or some default like 0 if never logged in
        }
        Duration duration = Duration.between(createdAt, Instant.now());
        return (int) duration.toDays();
    }

    @Override
    public CreateUserResponse toCreateResponse(UserEntity entity) {
        return CreateUserResponse.builder().build();
    }

    @Override
    public UpdateUserResponse toUpdateResponse(UserEntity entity) {
        return UpdateUserResponse.builder().build();
    }

    @Override
    public DeleteUserResponse toDeleteResponse(UserEntity entity) {
        return DeleteUserResponse.builder().build();
    }
}
