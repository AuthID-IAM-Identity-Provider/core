package io.authid.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
public class AuthIdCoreApplication {
        public static void main(String[] args) {
                SpringApplication.run(AuthIdCoreApplication.class, args);
        }
}
