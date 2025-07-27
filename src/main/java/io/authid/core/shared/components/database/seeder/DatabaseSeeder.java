package io.authid.core.shared.components.database.seeder;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder extends CallableSeeder {

    @Transactional
    @Override
    public void run() {
        System.out.println("🌱 Memulai proses database seeding...");
        System.out.println("✅ Proses database seeding berhasil diselesaikan.");
    }
}
