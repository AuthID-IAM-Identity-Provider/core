package io.authid.core.shared.components.database.seeder;

import io.authid.core.containers.authorization.domain.seeders.AuthorizationSeeder;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder extends CallableSeeder {

    @Transactional
    @Override
    public void run() {
        System.out.println("🌱 Memulai proses database seeding...");

        this.call(
                AuthorizationSeeder.class
        );

        System.out.println("✅ Proses database seeding berhasil diselesaikan.");
    }
}
