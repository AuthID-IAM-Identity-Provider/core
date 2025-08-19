package io.authid.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@EntityScan(basePackages = {
        "io.authid.core", // This should cover all entities within io.authid.core and its sub-packages
})
@EnableJpaRepositories(basePackages = {
        "io.authid.core" // This will cover all repositories in io.authid.core and its sub-packages
})
@CommandScan
public class AuthIdCoreApplication {

        public static void main(String[] args) {
                SpringApplication.run(AuthIdCoreApplication.class, args);
        }
}
