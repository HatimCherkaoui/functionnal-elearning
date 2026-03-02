package org.eckmo.functionnal.util;

import lombok.experimental.UtilityClass;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Functional utility class for common operations using functional programming patterns
 */
@UtilityClass
public class FunctionalUtils {

    /**
     * Filter a collection using a predicate
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Check if any element in collection matches predicate
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().anyMatch(predicate);
    }

    /**
     * Check if all elements in collection match predicate
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().allMatch(predicate);
    }

    /**
     * Count elements matching predicate
     */
    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream()
                .filter(predicate)
                .count();
    }

    /**
     * Convert between two types using a mapper function
     */
    public static <T, R> List<R> map(Collection<T> collection, java.util.function.Function<T, R> mapper) {
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Combine two predicates with AND logic
     */
    public static <T> Predicate<T> and(Predicate<T> p1, Predicate<T> p2) {
        return p1.and(p2);
    }

    /**
     * Combine two predicates with OR logic
     */
    public static <T> Predicate<T> or(Predicate<T> p1, Predicate<T> p2) {
        return p1.or(p2);
    }

    /**
     * Negate a predicate
     */
    public static <T> Predicate<T> negate(Predicate<T> predicate) {
        return predicate.negate();
    }

    /**
     * Partition collection into two based on predicate
     */
    public static <T> java.util.Map.Entry<List<T>, List<T>> partition(Collection<T> collection, Predicate<T> predicate) {
        List<T> matching = collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        List<T> nonMatching = collection.stream()
                .filter(predicate.negate())
                .collect(Collectors.toList());
        return java.util.Map.entry(matching, nonMatching);
    }

    /**
     * Find first element matching predicate
     */
    public static <T> java.util.Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream()
                .filter(predicate)
                .findFirst();
    }

    /**
     * Get distinct values from collection
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        return collection.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Sort collection using comparator
     */
    public static <T> List<T> sort(Collection<T> collection, java.util.Comparator<T> comparator) {
        return collection.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}

