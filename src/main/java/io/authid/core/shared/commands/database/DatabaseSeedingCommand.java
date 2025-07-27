package io.authid.core.shared.commands.database;

import io.authid.core.shared.components.database.seeder.DatabaseSeeder;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

@Command(command = "database-seeding", alias = "db-seeding", description = "Commands for database seeding operations")
@Component
public class DatabaseSeedingCommand {
    private final DatabaseSeeder databaseSeeder;

    public DatabaseSeedingCommand(DatabaseSeeder databaseSeeder) {
        this.databaseSeeder = databaseSeeder;
    }

    @Command(command = "seed", alias = "s", description = "Seeds the database with initial data")
    public void seed() {
        this.databaseSeeder.run();
    }
}
