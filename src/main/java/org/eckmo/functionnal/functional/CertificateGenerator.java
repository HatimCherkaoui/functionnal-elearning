package org.eckmo.functionnal.functional;

/**
 * Functional interface for generating certificates
 */
@FunctionalInterface
public interface CertificateGenerator {
    String generate(String userName, String courseName, String certificateCode);
}

