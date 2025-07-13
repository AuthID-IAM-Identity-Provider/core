package io.authid.core.containers.authorization.domain.seeders;

import io.authid.core.containers.authorization.domain.factories.RoleFactory;
import io.authid.core.shared.components.database.seeder.DatabaseSeeder;
import io.authid.core.shared.components.database.seeder.Seeder;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder extends DatabaseSeeder {

    @Override
    public void run() {
        RoleFactory factory = new RoleFactory();
        factory.count(10).make();
    }
}
