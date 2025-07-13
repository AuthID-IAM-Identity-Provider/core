package io.authid.core.containers.authorization.domain.seeders;

import io.authid.core.shared.components.database.seeder.CallableSeeder;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationSeeder extends CallableSeeder {
    @Override
    public void run() {
        this.call(RoleSeeder.class);
    }
}
