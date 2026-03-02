package org.eckmo.functionnal.functional;

/**
 * Functional interface for validating enrollments
 */
@FunctionalInterface
public interface EnrollmentValidator {
    boolean validate(Long userId, Long courseId, Double currentScore);
}

