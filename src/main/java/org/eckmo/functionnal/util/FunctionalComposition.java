package org.eckmo.functionnal.util;

import lombok.experimental.UtilityClass;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Functional utilities for null-safe operations and composition
 */
@UtilityClass
public class FunctionalComposition {

    /**
     * Compose two functions: f(g(x))
     */
    public static <A, B, C> Function<A, C> compose(Function<B, C> f, Function<A, B> g) {
        return x -> f.apply(g.apply(x));
    }

    /**
     * Pipe two functions: g(f(x))
     */
    public static <A, B, C> Function<A, C> pipe(Function<A, B> f, Function<B, C> g) {
        return x -> g.apply(f.apply(x));
    }

    /**
     * Safe execution with null handling
     */
    public static <T, R> R safeApply(T value, Function<T, R> function, R defaultValue) {
        try {
            return value != null ? function.apply(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Lazy initialization with supplier
     */
    public static <T> T lazy(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * Memoize a supplier (cache result)
     */
    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<T>() {
            private T value;
            private boolean initialized = false;

            @Override
            public T get() {
                if (!initialized) {
                    value = supplier.get();
                    initialized = true;
                }
                return value;
            }
        };
    }

    /**
     * Curry a function that takes two parameters
     */
    public static <A, B, R> Function<A, Function<B, R>> curry(java.util.function.BiFunction<A, B, R> f) {
        return a -> b -> f.apply(a, b);
    }

    /**
     * Uncurry a function
     */
    public static <A, B, R> java.util.function.BiFunction<A, B, R> uncurry(Function<A, Function<B, R>> f) {
        return (a, b) -> f.apply(a).apply(b);
    }
}

