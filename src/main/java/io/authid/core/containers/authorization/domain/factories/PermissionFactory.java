package io.authid.core.containers.authorization.domain.factories;

import io.authid.core.containers.authorization.domain.entities.PermissionEntity;
import io.authid.core.containers.authorization.domain.entities.RoleEntity;
import io.authid.core.shared.components.database.factory.Factory;
import org.springframework.stereotype.Component;

@Component
public class PermissionFactory extends Factory<PermissionEntity> {

    @Override
    public PermissionEntity definition() {
        return null;
    }
}
