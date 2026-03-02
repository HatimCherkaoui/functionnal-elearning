package org.eckmo.functionnal;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptHashGenerator {

    @Test
    void generateAdminHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);

        System.out.println("======================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("======================================");
        System.out.println();

        // Test the existing hash
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iJSNvZs3obQIaLHnGCbXCNMQLDlC";
        System.out.println("Testing existing hash:");
        System.out.println("Existing: " + existingHash);
        System.out.println("Matches 'admin123': " + encoder.matches(password, existingHash));
        System.out.println();

        // Generate a few more to choose from
        System.out.println("Alternative hashes for 'admin123':");
        for (int i = 0; i < 3; i++) {
            System.out.println("Hash " + (i+1) + ": " + encoder.encode(password));
        }
    }
}

