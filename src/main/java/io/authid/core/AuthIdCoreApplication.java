package io.authid.core;

import io.authid.core.shared.components.database.configurations.AuditorAwareImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

@SpringBootApplication
@EntityScan(basePackages = {
		"io.authid.core.containers",
		"io.authid.core.containers.user.entities",
})
public class AuthIdCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthIdCoreApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
		return new AuditorAwareImpl();
	}

}
