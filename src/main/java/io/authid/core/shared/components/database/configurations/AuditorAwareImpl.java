package io.authid.core.shared.components.database.configurations;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        // Mengambil informasi otentikasi dari Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Jika tidak ada user yang login (misal proses sistem) atau user adalah anonim
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("system"); // Default value jika tidak ada user
        }

        // Mengembalikan username dari user yang sedang login
        return Optional.of(authentication.getName());
    }
}
