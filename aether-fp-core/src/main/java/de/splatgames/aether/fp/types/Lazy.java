/*
 * Copyright (c) 2026 Splatgames.de Software and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.splatgames.aether.fp.types;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A thread-safe, memoized container for deferred computation.
 *
 * <p>{@code Lazy<T>} wraps a {@link Supplier} and defers its evaluation until the value is
 * actually needed. Once evaluated successfully, the computed result is cached permanently and
 * all subsequent accesses return the same value without re-executing the supplier. This makes
 * {@code Lazy} a referentially transparent handle for expensive or side-effecting computations
 * that should execute at most once.</p>
 *
 * <h2>Distinction from {@link Supplier}</h2>
 * <p>A plain {@link Supplier} is re-evaluated on every call to {@link Supplier#get()} and provides
 * no caching, no thread-safety guarantees, and no compositional API. {@code Lazy<T>}, by contrast,
 * guarantees exactly-once successful evaluation, permanent memoization, safe concurrent access,
 * and functional composition through {@link #map(Function)} and {@link #flatMap(Function)}. It is
 * a value abstraction, not a function abstraction.</p>
 *
 * <h2>Thread Safety</h2>
 * <p>All operations on {@code Lazy} are thread-safe. Concurrent calls to {@link #get()} are
 * guaranteed to evaluate the underlying supplier at most once successfully. The implementation
 * uses double-checked locking with volatile fields to ensure safe publication of the computed
 * value across threads without unnecessary synchronization on the fast path.</p>
 *
 * <h2>Exactly-Once Successful Computation</h2>
 * <p>The supplier is invoked at most once successfully. If the supplier throws an exception during
 * evaluation, the exception propagates to the caller and the {@code Lazy} remains in an unevaluated
 * state — subsequent calls to {@link #get()} will retry the computation. Once the supplier returns
 * a non-null value, that value is cached permanently and the supplier reference is released to
 * allow garbage collection of any captured state.</p>
 *
 * <h2>Null Policy</h2>
 * <p>Null values are strictly prohibited. The factory method {@link #of(Supplier)} rejects a
 * {@code null} supplier, and the supplier must not return {@code null}. All function parameters
 * (mappers, consumers) are validated against {@code null}. Any violation results in a
 * {@link NullPointerException} being thrown immediately.</p>
 *
 * <h2>Equality and Hashing</h2>
 * <p>Two {@code Lazy} instances are considered equal if and only if they produce equal values.
 * Consequently, calling {@link #equals(Object)} or {@link #hashCode()} <strong>triggers
 * evaluation</strong> of this instance (and, for {@code equals}, of the other instance if it is
 * also a {@code Lazy}). This design ensures that equality is value-based and referentially
 * transparent, consistent with the semantics of a memoized value container. The
 * {@link #toString()} method, by contrast, does <strong>not</strong> trigger evaluation.</p>
 *
 * <h2>Functional Composition</h2>
 * <p>{@code Lazy} supports functional composition through {@link #map(Function)} and
 * {@link #flatMap(Function)}. Both operations preserve laziness: they return a new {@code Lazy}
 * that composes the transformation with the original computation without evaluating either.
 * Evaluation only occurs when the resulting {@code Lazy}'s value is actually requested.</p>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Defer an expensive computation
 * Lazy<String> lazy = Lazy.of(() -> expensiveComputation());
 * // lazy.isEvaluated() == false
 *
 * // Value is computed on first access
 * String value = lazy.get();
 * // lazy.isEvaluated() == true
 *
 * // Subsequent access returns the cached value
 * String same = lazy.get(); // no re-computation
 *
 * // Compose lazily — no evaluation happens here
 * Lazy<Integer> length = lazy.map(String::length);
 * // length.isEvaluated() == false
 *
 * // Chain lazy computations
 * Lazy<String> chained = Lazy.of(() -> "hello")
 *     .map(s -> s + " world")
 *     .flatMap(s -> Lazy.of(() -> s.toUpperCase()));
 * // Nothing evaluated yet — only computed when chained.get() is called
 *
 * // Non-forcing inspection
 * Optional<String> maybe = lazy.toOptional();
 * // Returns Optional.of(value) if already evaluated, Optional.empty() otherwise
 * }</pre>
 *
 * @param <T> the type of the lazily computed value
 * @author Erik Pförtner
 * @since 0.1.0
 */
public final class Lazy<T> {

    /**
     * The supplier that produces the lazily computed value.
     *
     * <p>This field is set to {@code null} after successful evaluation to allow garbage
     * collection of the supplier and any state it captures.</p>
     */
    private volatile Supplier<? extends T> supplier;

    /**
     * The memoized result of the computation, or {@code null} if not yet evaluated.
     *
     * <p>Because null values are forbidden, a non-null value reliably indicates that the
     * computation has completed successfully.</p>
     */
    private volatile T value;

    /**
     * The lock object used for synchronization during the first evaluation.
     */
    private final Object lock = new Object();

    /**
     * Creates a new {@code Lazy} instance with the given supplier.
     *
     * <p>This constructor is intentionally private. Use the factory method {@link #of(Supplier)}
     * to create instances.</p>
     *
     * @param supplier the supplier that produces the lazily computed value, must not be
     *                 {@code null}
     */
    private Lazy(@NotNull final Supplier<? extends T> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
    }

    /**
     * Creates a new {@code Lazy} that will evaluate the given supplier on first access.
     *
     * <p>The supplier is not invoked during this call. Evaluation is deferred until the value
     * is actually requested via {@link #get()}, or indirectly through operations that force
     * evaluation such as {@link #equals(Object)} or {@link #hashCode()}.</p>
     *
     * <pre>{@code
     * Lazy<String> lazy = Lazy.of(() -> expensiveComputation());
     * // The supplier has not been called yet
     * }</pre>
     *
     * @param supplier the supplier that produces the value, must not be {@code null} and must
     *                 not return {@code null} when eventually evaluated
     * @param <T>      the type of the lazily computed value
     * @return a new unevaluated {@code Lazy} wrapping the given supplier, never {@code null}
     * @throws NullPointerException if {@code supplier} is {@code null}
     */
    @NotNull
    public static <T> Lazy<T> of(@NotNull final Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Returns the computed value, evaluating the supplier on first access.
     *
     * <p>If the value has not been computed yet, the supplier is invoked and its result is
     * cached permanently. All subsequent calls return the cached value without re-executing the
     * supplier. If the supplier throws an exception, the exception propagates to the caller and
     * the {@code Lazy} remains unevaluated — the next call to {@code get()} will retry.</p>
     *
     * <p>This method is thread-safe. Concurrent calls are guaranteed to evaluate the supplier at
     * most once successfully. The implementation uses double-checked locking to avoid
     * synchronization overhead on the fast path after the value has been computed.</p>
     *
     * <pre>{@code
     * Lazy<Integer> lazy = Lazy.of(() -> 42);
     * int value = lazy.get(); // evaluates the supplier, returns 42
     * int same = lazy.get();  // returns cached 42, no re-evaluation
     * }</pre>
     *
     * @return the computed value, never {@code null}
     * @throws NullPointerException if the supplier returns {@code null}
     */
    @NotNull
    public T get() {
        T result = this.value;
        if (result == null) {
            synchronized (this.lock) {
                result = this.value;
                if (result == null) {
                    result = Objects.requireNonNull(
                        this.supplier.get(),
                        "supplier must not return null"
                    );
                    this.value = result;
                    this.supplier = null;
                }
            }
        }
        return result;
    }

    /**
     * Returns whether the lazy value has already been computed and memoized.
     *
     * <p>This is a non-forcing inspection method. It does not trigger evaluation of the
     * underlying supplier and simply reflects the current internal state.</p>
     *
     * <pre>{@code
     * Lazy<String> lazy = Lazy.of(() -> "hello");
     * lazy.isEvaluated(); // false
     * lazy.get();
     * lazy.isEvaluated(); // true
     * }</pre>
     *
     * @return {@code true} if the value has been computed, {@code false} otherwise
     */
    public boolean isEvaluated() {
        return this.value != null;
    }

    /**
     * Transforms the lazily computed value using the given mapping function.
     *
     * <p>Returns a new {@code Lazy} whose value, when requested, is the result of applying the
     * mapper to this {@code Lazy}'s value. This operation is itself lazy — neither this
     * {@code Lazy} nor the returned {@code Lazy} is evaluated by calling this method. Evaluation
     * only occurs when the resulting {@code Lazy}'s value is accessed.</p>
     *
     * <pre>{@code
     * Lazy<String> lazy = Lazy.of(() -> "hello");
     * Lazy<Integer> length = lazy.map(String::length);
     * // Neither lazy nor length has been evaluated yet
     *
     * int len = length.get();
     * // Now both are evaluated: lazy computed "hello", length computed 5
     * }</pre>
     *
     * @param mapper the mapping function to apply to the computed value, must not be {@code null}
     *               and must not return {@code null}
     * @param <U>    the type of the new lazily computed value
     * @return a new {@code Lazy} with the lazily transformed value, never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}
     */
    @NotNull
    public <U> Lazy<U> map(@NotNull final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return Lazy.of(() -> Objects.requireNonNull(
            mapper.apply(this.get()),
            "mapper must not return null"
        ));
    }

    /**
     * Transforms the lazily computed value using a function that itself returns a {@code Lazy}.
     *
     * <p>Returns a new {@code Lazy} whose value, when requested, is obtained by applying the
     * mapper to this {@code Lazy}'s value to produce an intermediate {@code Lazy}, and then
     * evaluating that intermediate {@code Lazy}. This enables chaining of lazy computations
     * without nesting. Like {@link #map(Function)}, this operation is itself lazy — no evaluation
     * occurs until the resulting {@code Lazy}'s value is accessed.</p>
     *
     * <pre>{@code
     * Lazy<String> lazy = Lazy.of(() -> "hello");
     * Lazy<String> upper = lazy.flatMap(s -> Lazy.of(() -> s.toUpperCase()));
     * // Nothing evaluated yet
     *
     * String result = upper.get();
     * // result = "HELLO"
     * }</pre>
     *
     * @param mapper the mapping function returning a {@code Lazy}, must not be {@code null} and
     *               must not return {@code null}
     * @param <U>    the type of the new lazily computed value
     * @return a new {@code Lazy} with the lazily composed value, never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}
     */
    @NotNull
    public <U> Lazy<U> flatMap(@NotNull final Function<? super T, ? extends Lazy<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return Lazy.of(() -> Objects.requireNonNull(
            mapper.apply(this.get()),
            "mapper must not return null"
        ).get());
    }

    /**
     * Invokes the given consumer with the cached value if this {@code Lazy} has already been
     * evaluated.
     *
     * <p>If the value has been computed, the consumer is called with the memoized value. If the
     * value has not been computed yet, the consumer is not invoked and no evaluation is triggered.
     * This is a non-forcing observation method suitable for logging, debugging, or conditional
     * side effects.</p>
     *
     * <pre>{@code
     * Lazy<String> lazy = Lazy.of(() -> "hello");
     * lazy.ifEvaluated(v -> System.out.println(v)); // nothing happens
     *
     * lazy.get(); // evaluates
     * lazy.ifEvaluated(v -> System.out.println(v)); // prints "hello"
     * }</pre>
     *
     * @param consumer the consumer to invoke with the cached value if already evaluated, must
     *                 not be {@code null}
     * @return this {@code Lazy} instance for fluent chaining, never {@code null}
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    @NotNull
    public Lazy<T> ifEvaluated(@NotNull final Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        final T snapshot = this.value;
        if (snapshot != null) {
            consumer.accept(snapshot);
        }
        return this;
    }

    /**
     * Returns an {@link Optional} containing the cached value if this {@code Lazy} has already
     * been evaluated, or an empty {@link Optional} if it has not.
     *
     * <p>This is a non-forcing inspection method. It does not trigger evaluation of the
     * underlying supplier. Because null values are forbidden, an evaluated value always results
     * in {@link Optional#of(Object)}, never {@link Optional#ofNullable(Object)}.</p>
     *
     * <pre>{@code
     * Lazy<String> lazy = Lazy.of(() -> "hello");
     * lazy.toOptional(); // Optional.empty()
     *
     * lazy.get(); // evaluates
     * lazy.toOptional(); // Optional.of("hello")
     * }</pre>
     *
     * @return an {@link Optional} containing the cached value if evaluated, or
     *         {@link Optional#empty()} otherwise; never {@code null}
     */
    @NotNull
    public Optional<T> toOptional() {
        final T snapshot = this.value;
        return snapshot != null ? Optional.of(snapshot) : Optional.empty();
    }

    /**
     * Compares this {@code Lazy} with the specified object for equality.
     *
     * <p>Two {@code Lazy} instances are considered equal if and only if they produce equal
     * values. This means that calling this method <strong>triggers evaluation</strong> of this
     * instance. If the other object is also a {@code Lazy}, it will be evaluated as well.</p>
     *
     * <p>A {@code Lazy} is never equal to {@code null} or to an object of a different type.</p>
     *
     * @param obj the object to compare with, may be {@code null}
     * @return {@code true} if the other object is a {@code Lazy} with an equal computed value,
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Lazy<?> other)) {
            return false;
        }
        return Objects.equals(this.get(), other.get());
    }

    /**
     * Returns a hash code value for this {@code Lazy}, computed from its value.
     *
     * <p>Because the hash code is derived from the computed value, calling this method
     * <strong>triggers evaluation</strong> if the value has not yet been computed. The hash code
     * is consistent with {@link #equals(Object)}: two {@code Lazy} instances that are equal will
     * produce the same hash code.</p>
     *
     * @return the hash code computed from the lazily evaluated value
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.get());
    }

    /**
     * Returns a string representation of this {@code Lazy}.
     *
     * <p>If the value has already been evaluated, the format is {@code Lazy[value]}, where
     * {@code value} is the string representation of the cached value. If the value has not yet
     * been evaluated, the format is {@code Lazy[?]}. This method does <strong>not</strong>
     * trigger evaluation.</p>
     *
     * @return a string representation of this {@code Lazy}, never {@code null}
     */
    @NotNull
    @Override
    public String toString() {
        final T snapshot = this.value;
        return snapshot != null ? "Lazy[" + snapshot + "]" : "Lazy[?]";
    }
}
