package org.eckmo.functionnal.functional;

/**
 * Custom functional interface for filtering courses
 * Demonstrates the use of functional programming patterns
 */
@FunctionalInterface
public interface FunctionalInterfaces {
    boolean test(String title, Double price, String level);
}


