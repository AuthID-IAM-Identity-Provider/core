package io.authid.core.shared.components.database.configurations; // Lokasi yang bagus

import io.authid.core.shared.components.database.audit.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider") // Mengaktifkan JPA Auditing dan menunjuk ke bean auditorProvider
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // Mengembalikan instance dari AuditorAwareImpl yang sudah Anda miliki
        return new AuditorAwareImpl();
    }
}