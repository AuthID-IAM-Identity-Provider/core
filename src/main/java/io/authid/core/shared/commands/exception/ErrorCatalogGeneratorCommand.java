package io.authid.core.shared.commands.exception;

import io.authid.core.shared.generators.ErrorCatalogGenerator;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

@Command(command = "generate error catalog", alias = "gen-err-cat", description = "Commands for generating error catalaog  operations")
@Component
public class ErrorCatalogGeneratorCommand {
    private final ErrorCatalogGenerator errorCatalogGenerator;

    private static final String LOCK_KEY = "error_catalog_generation_lock"; // Key untuk lock

    public ErrorCatalogGeneratorCommand(ErrorCatalogGenerator errorCatalogGenerator) {
        this.errorCatalogGenerator = errorCatalogGenerator;
    }

    // --- PSEUDO-CODE UNTUK SERVIS LOCK ---
    // Di sini kita akan menggunakan pseudo-code lock yang sama dengan generator
    // Anda bisa memindahkan ini ke util atau menggunakan @Service jika servis lock nyata ada
    private boolean acquireLock() {
        System.out.println("Attempting to acquire lock for: " + ErrorCatalogGeneratorCommand.LOCK_KEY + "...");
        // Implementasi nyata: Redis lock, database lock, etc.
        // Untuk demo, selalu sukses.
        return true;
    }

    private void releaseLock() {
        System.out.println("Releasing lock for: " + ErrorCatalogGeneratorCommand.LOCK_KEY + ".");
        // Implementasi nyata: melepaskan lock
    }
    // --- AKHIR PSEUDO-CODE UNTUK SERVIS LOCK ---

    @Command(command = "generate", alias = "gen", description = "Generates the Error Catalog enum(s) from Excel master data.") // Untuk Spring Shell 3.x Command annotation
    public String generateErrorCatalog() {

        System.out.println("Executing generate-error-catalog command...");

        // --- AKUISISI LOCK ---
        if (!acquireLock()) {
            return "Generation process cannot start: Another process is currently running or lock could not be acquired.";
        }

        try {
            // Panggil metode generate dari generator service
            errorCatalogGenerator.generate();
            return "Error Catalog generation completed successfully!";
        } catch (IllegalArgumentException e) {
            System.err.println("Validation Error: " + e.getMessage());
            return "Generation failed due to validation error: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during generation: " + e.getMessage());
            e.printStackTrace();
            return "Generation failed due to an unexpected error. Check logs for details: " + e.getMessage();
        } finally {
            // --- MELEPASKAN LOCK ---
            releaseLock();
        }
    }
}
