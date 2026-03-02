package org.eckmo.functionnal.functional;

/**
 * Functional interface for processing users
 */
@FunctionalInterface
public interface UserProcessor {
    String process(Long userId, String email, String role);
}

