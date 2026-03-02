package org.eckmo.functionnal.functional;

/**
 * Functional interface for filtering courses
 */
@FunctionalInterface
public interface CourseFilter {
    boolean test(String title, Double price, String level);
}

