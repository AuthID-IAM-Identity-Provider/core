package io.authid.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Konfigurasi utama untuk Spring Security.
 * Kelas ini mendefinisikan aturan keamanan untuk endpoint HTTP.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Nonaktifkan CSRF karena biasanya tidak relevan untuk API stateless.
                .csrf(csrf -> csrf.disable())

                // Konfigurasi aturan otorisasi untuk setiap request.
                .authorizeHttpRequests(auth -> auth
                        // PENTING: Izinkan semua request ke /api/test/** tanpa perlu autentikasi.
                        .requestMatchers("/api/**").permitAll()

                        // Aturan lain bisa ditambahkan di sini, contoh:
                        // .requestMatchers("/api/auth/**").permitAll()

                        // Amankan semua endpoint lain, harus diautentikasi.
                        .anyRequest().authenticated()
                )

                // Konfigurasi manajemen sesi. Untuk API, biasanya stateless.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
