package io.authid.core.containers.authorization.domain.factories;

import io.authid.core.containers.authorization.domain.entities.RoleEntity;
import io.authid.core.shared.components.database.factory.Factory;
import org.springframework.stereotype.Component;

@Component
public class RoleFactory extends Factory<RoleEntity> {

    @Override
    public RoleEntity definition() {
        return null;
    }

    public RoleFactory asAdmin() {
        return (RoleFactory) this.state(role -> {
            role.setName("Administrator");
            role.setCode("ADMIN");
            return role;
        });
    }
}
