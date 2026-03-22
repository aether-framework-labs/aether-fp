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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A right-biased algebraic sum type representing exactly one of two possible values: a {@link Left} or a
 * {@link Right}.
 *
 * <p>{@code Either<L, R>} is a discriminated union that models a value which is either of type {@code L}
 * or of type {@code R}, but never both and never neither. This makes it fundamentally different from a container that
 * may or may not hold a value - an {@code Either} always holds exactly one value, and its type encodes which side that
 * value belongs to.</p>
 *
 * <h2>Semantics</h2>
 * <p>By convention in functional programming, the left side represents the alternative or error case,
 * while the right side represents the success or primary case. This convention is reflected in the right-biased design
 * of this type:</p>
 * <ul>
 *   <li>{@link #map(Function)} operates exclusively on the right value.</li>
 *   <li>{@link #flatMap(Function)} operates exclusively on the right value.</li>
 *   <li>Left values propagate unchanged through right-biased operations.</li>
 * </ul>
 *
 * <h2>Null Policy</h2>
 * <p>Null values are strictly prohibited. All factory methods, all function parameters, and all mapper
 * return values are validated against {@code null}. Any attempt to create an {@code Either} containing
 * {@code null}, or to pass a {@code null} function argument, will result in a {@link NullPointerException}
 * being thrown immediately.</p>
 *
 * <h2>Immutability</h2>
 * <p>All instances of {@code Either} are immutable and therefore inherently thread-safe.
 * Every transformation operation returns a new instance; no existing state is ever modified.</p>
 *
 * <h2>Functional Composition</h2>
 * <p>The primary way to work with {@code Either} values is through functional composition.
 * Use {@link #map(Function)} and {@link #flatMap(Function)} for right-biased transformations,
 * {@link #mapLeft(Function)} for left-side transformations, and {@link #fold(Function, Function)}
 * for exhaustive pattern matching that forces handling of both cases.</p>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Create either values
 * Either<String, Integer> success = Either.right(42);
 * Either<String, Integer> failure = Either.left("not found");
 *
 * // Right-biased map - only transforms the right value
 * Either<String, String> mapped = success.map(n -> "value: " + n);
 * // mapped = Right[value: 42]
 *
 * // Left values propagate unchanged through map
 * Either<String, String> stillLeft = failure.map(n -> "value: " + n);
 * // stillLeft = Left[not found]
 *
 * // Chain operations that may fail using flatMap
 * Either<String, Integer> parsed = Either.<String, String>right("123")
 *     .flatMap(s -> {
 *         try {
 *             return Either.right(Integer.parseInt(s));
 *         } catch (NumberFormatException e) {
 *             return Either.left("invalid number: " + s);
 *         }
 *     });
 *
 * // Exhaustive pattern matching with fold
 * String result = success.fold(
 *     error -> "Error: " + error,
 *     value -> "Success: " + value
 * );
 * // result = "Success: 42"
 *
 * // Extract with fallback
 * Integer value = failure.getOrElse(0);  // 0
 * }</pre>
 *
 * @param <L> the type of the left value, conventionally the error or alternative type
 * @param <R> the type of the right value, conventionally the success or primary type
 * @author Erik Pförtner
 * @since 0.1.0
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    /**
     * Creates a left-valued {@code Either}.
     *
     * <p>By convention, the left side represents an error, failure, or alternative result.
     * Use this factory method to construct an {@code Either} that carries a left value.</p>
     *
     * <pre>{@code
     * Either<String, Integer> error = Either.left("not found");
     * }</pre>
     *
     * @param value the left value, must not be {@code null}
     * @param <L>   the type of the left value
     * @param <R>   the type of the right value
     * @return a left-valued {@code Either} containing the specified value, never {@code null}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @NotNull
    static <L, R> Either<L, R> left(@NotNull final L value) {
        Objects.requireNonNull(value, "value must not be null");
        return new Left<>(value);
    }

    /**
     * Creates a right-valued {@code Either}.
     *
     * <p>By convention, the right side represents a success or primary result.
     * Use this factory method to construct an {@code Either} that carries a right value.</p>
     *
     * <pre>{@code
     * Either<String, Integer> success = Either.right(42);
     * }</pre>
     *
     * @param value the right value, must not be {@code null}
     * @param <L>   the type of the left value
     * @param <R>   the type of the right value
     * @return a right-valued {@code Either} containing the specified value, never {@code null}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @NotNull
    static <L, R> Either<L, R> right(@NotNull final R value) {
        Objects.requireNonNull(value, "value must not be null");
        return new Right<>(value);
    }

    /**
     * Returns {@code true} if this {@code Either} contains a left value.
     *
     * <p>Exactly one of {@code  isLeft()} and {@link #isRight()} will return {@code true}
     * for any given instance.</p>
     *
     * @return {@code true} if this is a {@link Left}, {@code false} if this is a {@link Right}
     */
    boolean isLeft();

    /**
     * Returns {@code true} if this {@code Either} contains a right value.
     *
     * <p>Exactly one of {@link #isLeft()} and {@code isRight()} will return {@code true}
     * for any given instance.</p>
     *
     * @return {@code true} if this is a {@link Right}, {@code false} if this is a {@link Left}
     */
    boolean isRight();

    /**
     * Transforms the right value using the given mapping function.
     *
     * <p>If this is a {@link Right}, applies the mapper to the right value and returns a new
     * {@link Right} containing the result. If this is a {@link Left}, returns this instance unchanged with adjusted
     * type parameters. This is the right-biased map operation.</p>
     *
     * <pre>{@code
     * Either<String, Integer> right = Either.right(42);
     * Either<String, String> mapped = right.map(n -> "Number: " + n);
     * // mapped = Right[Number: 42]
     *
     * Either<String, Integer> left = Either.left("error");
     * Either<String, String> stillLeft = left.map(n -> "Number: " + n);
     * // stillLeft = Left[error]
     * }</pre>
     *
     * @param mapper the mapping function to apply to the right value, must not be {@code null} and must not return
     *               {@code null}
     * @param <U>    the type of the new right value
     * @return a new {@code Either} with the transformed right value, or this left unchanged; never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}, or if the mapper returns {@code null}
     */
    @NotNull
    <U> Either<L, U> map(@NotNull final Function<? super R, ? extends U> mapper);

    /**
     * Transforms the right value using a function that itself returns an {@code Either}.
     *
     * <p>If this is a {@link Right}, applies the mapper to the right value and returns the
     * resulting {@code Either} directly (without wrapping it in another layer). If this is a {@link Left}, returns this
     * instance unchanged with adjusted type parameters. This enables chaining of operations that may each independently
     * fail.</p>
     *
     * <pre>{@code
     * Either<String, String> input = Either.right("123");
     * Either<String, Integer> parsed = input.flatMap(s -> {
     *     try {
     *         return Either.right(Integer.parseInt(s));
     *     } catch (NumberFormatException e) {
     *         return Either.left("invalid number: " + s);
     *     }
     * });
     * // parsed = Right[123]
     * }</pre>
     *
     * @param mapper the mapping function returning an {@code Either}, must not be {@code null} and must not return
     *               {@code null}
     * @param <U>    the type of the new right value
     * @return the result of applying the function if this is a right, or this left unchanged; never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}, or if the mapper returns {@code null}
     */
    @NotNull
    <U> Either<L, U> flatMap(@NotNull final Function<? super R, ? extends Either<L, U>> mapper);

    /**
     * Transforms the left value using the given mapping function.
     *
     * <p>If this is a {@link Left}, applies the mapper to the left value and returns a new
     * {@link Left} containing the result. If this is a {@link Right}, returns this instance unchanged with adjusted
     * type parameters.</p>
     *
     * <pre>{@code
     * Either<String, Integer> left = Either.left("error");
     * Either<String, Integer> mappedLeft = left.mapLeft(String::toUpperCase);
     * // mappedLeft = Left[ERROR]
     *
     * Either<String, Integer> right = Either.right(42);
     * Either<String, Integer> stillRight = right.mapLeft(String::toUpperCase);
     * // stillRight = Right[42]
     * }</pre>
     *
     * @param mapper the mapping function to apply to the left value, must not be {@code null} and must not return
     *               {@code null}
     * @param <T>    the type of the new left value
     * @return a new {@code Either} with the transformed left value, or this right unchanged; never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}, or if the mapper returns {@code null}
     */
    @NotNull
    <T> Either<T, R> mapLeft(@NotNull final Function<? super L, ? extends T> mapper);

    /**
     * Applies one of two functions depending on whether this is a left or right value, forcing exhaustive handling of
     * both cases.
     *
     * <p>Exactly one of the two provided functions will be applied. If this is a {@link Left},
     * the {@code leftMapper} is applied to the left value. If this is a {@link Right}, the {@code rightMapper} is
     * applied to the right value. Both functions must produce a result of the same type {@code U}.</p>
     *
     * <pre>{@code
     * Either<String, Integer> either = Either.right(42);
     * String result = either.fold(
     *     error -> "Failed: " + error,
     *     value -> "Success: " + value
     * );
     * // result = "Success: 42"
     * }</pre>
     *
     * @param leftMapper  the function to apply if this is a left value, must not be {@code null}
     * @param rightMapper the function to apply if this is a right value, must not be {@code null}
     * @param <U>         the common result type of both functions
     * @return the result of applying the appropriate function
     * @throws NullPointerException if {@code leftMapper} or {@code rightMapper} is {@code null}
     */
    <U> U fold(@NotNull final Function<? super L, ? extends U> leftMapper,
               @NotNull final Function<? super R, ? extends U> rightMapper);

    /**
     * Returns a new {@code Either} with the left and right sides swapped.
     *
     * <p>A {@link Left} becomes a {@link Right} and a {@link Right} becomes a {@link Left}.
     * The contained value is preserved; only its positional semantics change.</p>
     *
     * <pre>{@code
     * Either<String, Integer> right = Either.right(42);
     * Either<Integer, String> swapped = right.swap();
     * // swapped = Left[42]
     *
     * Either<String, Integer> left = Either.left("error");
     * Either<Integer, String> swappedLeft = left.swap();
     * // swappedLeft = Right[error]
     * }</pre>
     *
     * @return a new {@code Either} with left and right values swapped, never {@code null}
     */
    @NotNull
    Either<R, L> swap();

    /**
     * Executes the given consumer if this is a left value.
     *
     * <p>This method is intended for observation and side effects such as logging or error
     * reporting. The consumer is only invoked if this is a {@link Left}. The method returns this same instance to allow
     * fluent chaining. No state is mutated.</p>
     *
     * <pre>{@code
     * Either<String, Integer> result = Either.left("error");
     * result.ifLeft(error -> System.err.println("Error: " + error))
     *       .ifRight(value -> System.out.println("Value: " + value));
     * }</pre>
     *
     * @param consumer the consumer to execute with the left value, must not be {@code null}
     * @return this {@code Either} instance for method chaining, never {@code null}
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    @NotNull
    Either<L, R> ifLeft(@NotNull final Consumer<? super L> consumer);

    /**
     * Executes the given consumer if this is a right value.
     *
     * <p>This method is intended for observation and side effects such as logging or
     * result processing. The consumer is only invoked if this is a {@link Right}. The method returns this same instance
     * to allow fluent chaining. No state is mutated.</p>
     *
     * <pre>{@code
     * Either<String, Integer> result = Either.right(42);
     * result.ifRight(value -> System.out.println("Value: " + value))
     *       .ifLeft(error -> System.err.println("Error: " + error));
     * }</pre>
     *
     * @param consumer the consumer to execute with the right value, must not be {@code null}
     * @return this {@code Either} instance for method chaining, never {@code null}
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    @NotNull
    Either<L, R> ifRight(@NotNull final Consumer<? super R> consumer);

    /**
     * Returns the right value if this is a {@link Right}, otherwise returns the provided fallback value.
     *
     * <pre>{@code
     * Either<String, Integer> right = Either.right(42);
     * Integer value1 = right.getOrElse(0);  // 42
     *
     * Either<String, Integer> left = Either.left("error");
     * Integer value2 = left.getOrElse(0);  // 0
     * }</pre>
     *
     * @param other the fallback value to return if this is a left, must not be {@code null}
     * @return the right value if present, otherwise the provided fallback; never {@code null}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    @NotNull
    R getOrElse(@NotNull final R other);

    /**
     * Returns the right value if this is a {@link Right}, otherwise computes a fallback value from the left value using
     * the provided function.
     *
     * <p>Unlike {@link #getOrElse(Object)}, this method allows the fallback to be derived
     * from the left value, enabling context-aware error recovery.</p>
     *
     * <pre>{@code
     * Either<Integer, String> left = Either.left(404);
     * String result = left.getOrElseGet(code -> "Error code: " + code);
     * // result = "Error code: 404"
     * }</pre>
     *
     * @param fallback the function to compute a fallback from the left value, must not be {@code null} and must not
     *                 return {@code null}
     * @return the right value if present, otherwise the computed fallback; never {@code null}
     * @throws NullPointerException if {@code fallback} is {@code null}, or if the fallback function returns
     *                              {@code null}
     */
    @NotNull
    R getOrElseGet(@NotNull final Function<? super L, ? extends R> fallback);

    /**
     * Returns the right value if this is a {@link Right}, otherwise throws a runtime exception created from the left
     * value using the provided mapping function.
     *
     * <p>This method bridges the gap between functional error handling and Java's exception
     * mechanism. It is typically used at the boundary of a functional pipeline where exceptions are the expected error
     * propagation strategy.</p>
     *
     * <pre>{@code
     * Either<String, Integer> result = Either.left("not found");
     * try {
     *     Integer value = result.getOrElseThrow(IllegalStateException::new);
     * } catch (IllegalStateException e) {
     *     // e.getMessage() == "not found"
     * }
     * }</pre>
     *
     * @param exceptionMapper the function to create a runtime exception from the left value, must not be {@code null}
     * @return the right value if this is a right
     * @throws RuntimeException     the exception created from the left value if this is a left
     * @throws NullPointerException if {@code exceptionMapper} is {@code null}
     */
    @NotNull
    R getOrElseThrow(@NotNull final Function<? super L, ? extends RuntimeException> exceptionMapper);

    /**
     * Implementation of {@link Either} representing a left value.
     *
     * <p>By convention, the left case represents an error, failure, or alternative result.
     * This implementation is immutable and thread-safe.</p>
     *
     * <h2>Usage</h2>
     * <p>Instances should be created via the factory method {@link Either#left(Object)}
     * rather than directly constructing this class.</p>
     *
     * <h2>Behavior</h2>
     * <ul>
     *   <li>{@link #isLeft()} always returns {@code true}</li>
     *   <li>{@link #isRight()} always returns {@code false}</li>
     *   <li>{@link #map(Function)} returns this instance unchanged (with adjusted type parameters)</li>
     *   <li>{@link #flatMap(Function)} returns this instance unchanged (with adjusted type parameters)</li>
     *   <li>{@link #mapLeft(Function)} applies the mapper to the left value</li>
     *   <li>{@link #fold(Function, Function)} applies the left mapper</li>
     *   <li>{@link #ifLeft(Consumer)} invokes the consumer with the left value</li>
     *   <li>{@link #ifRight(Consumer)} does not invoke the consumer</li>
     *   <li>{@link #getOrElse(Object)} returns the provided fallback</li>
     *   <li>{@link #getOrElseGet(Function)} computes the fallback from the left value</li>
     *   <li>{@link #getOrElseThrow(Function)} throws the mapped exception</li>
     * </ul>
     *
     * @param <L> the type of the left value
     * @param <R> the type of the right value (phantom type parameter for this implementation)
     * @since 0.1.0
     */
    final class Left<L, R> implements Either<L, R> {

        /**
         * The left value held by this instance.
         */
        private final L value;

        /**
         * Constructs a new {@code Left} containing the specified value.
         *
         * <p>This constructor is intentionally private. Use the factory method
         * {@link Either#left(Object)} to create instances.</p>
         *
         * @param value the left value, must not be {@code null}
         * @throws NullPointerException if {@code value} is {@code null}
         */
        private Left(@NotNull final L value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code true} for {@code Left}
         */
        @Override
        public boolean isLeft() {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code false} for {@code Left}
         */
        @Override
        public boolean isRight() {
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, the mapper is not applied and this instance is returned unchanged
         * with adjusted type parameters. The left value propagates through the operation.</p>
         *
         * @param mapper the mapping function, must not be {@code null} (validated but not applied)
         * @param <U>    the new right type
         * @return this {@code Left} instance cast to the new type, never {@code null}
         */
        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <U> Either<L, U> map(@NotNull final Function<? super R, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return (Either<L, U>) this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, the mapper is not applied and this instance is returned unchanged
         * with adjusted type parameters. The left value propagates through the operation.</p>
         *
         * @param mapper the mapping function, must not be {@code null} (validated but not applied)
         * @param <U>    the new right type
         * @return this {@code Left} instance cast to the new type, never {@code null}
         */
        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <U> Either<L, U> flatMap(@NotNull final Function<? super R, ? extends Either<L, U>> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return (Either<L, U>) this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, applies the mapper to the left value and returns a new
         * {@code Left} containing the result.</p>
         *
         * @param mapper the mapping function to apply to the left value, must not be {@code null}
         * @param <T>    the new left type
         * @return a new {@code Left} containing the mapped value, never {@code null}
         */
        @NotNull
        @Override
        public <T> Either<T, R> mapLeft(@NotNull final Function<? super L, ? extends T> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return Either.left(mapper.apply(this.value));
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, applies the {@code leftMapper} to the left value.</p>
         *
         * @param leftMapper  the function to apply to the left value, must not be {@code null}
         * @param rightMapper the function for right values (validated but not applied)
         * @param <U>         the result type
         * @return the result of applying {@code leftMapper} to the left value
         */
        @Override
        public <U> U fold(@NotNull final Function<? super L, ? extends U> leftMapper,
                          @NotNull final Function<? super R, ? extends U> rightMapper) {
            Objects.requireNonNull(leftMapper, "leftMapper must not be null");
            Objects.requireNonNull(rightMapper, "rightMapper must not be null");
            return leftMapper.apply(this.value);
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, returns a {@link Right} containing the left value.</p>
         *
         * @return a {@code Right} containing this left's value, never {@code null}
         */
        @NotNull
        @Override
        public Either<R, L> swap() {
            return Either.right(this.value);
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, invokes the consumer with the left value.</p>
         *
         * @param consumer the consumer to invoke with the left value, must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Either<L, R> ifLeft(@NotNull final Consumer<? super L> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            consumer.accept(this.value);
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, the consumer is not invoked.</p>
         *
         * @param consumer the consumer (validated but not invoked), must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Either<L, R> ifRight(@NotNull final Consumer<? super R> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, always returns the provided fallback value.</p>
         *
         * @param other the fallback value to return, must not be {@code null}
         * @return the provided fallback value, never {@code null}
         */
        @NotNull
        @Override
        public R getOrElse(@NotNull final R other) {
            Objects.requireNonNull(other, "other must not be null");
            return other;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, applies the fallback function to the left value and returns
         * the result.</p>
         *
         * @param fallback the function to compute the fallback from the left value, must not be {@code null}
         * @return the computed fallback value, never {@code null}
         * @throws NullPointerException if the fallback function returns {@code null}
         */
        @NotNull
        @Override
        public R getOrElseGet(@NotNull final Function<? super L, ? extends R> fallback) {
            Objects.requireNonNull(fallback, "fallback must not be null");
            final R result = fallback.apply(this.value);
            return Objects.requireNonNull(result, "fallback function must not return null");
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Left}, always throws the exception created from the left value.</p>
         *
         * @param exceptionMapper the function to create an exception from the left value, must not be {@code null}
         * @return never returns normally for {@code Left}
         * @throws RuntimeException the exception created from the left value
         */
        @NotNull
        @Override
        public R getOrElseThrow(@NotNull final Function<? super L, ? extends RuntimeException> exceptionMapper) {
            Objects.requireNonNull(exceptionMapper, "exceptionMapper must not be null");
            throw exceptionMapper.apply(this.value);
        }

        /**
         * Compares this {@code Left} to another object for equality.
         *
         * <p>Two {@code Left} instances are considered equal if and only if they contain
         * equal values as determined by {@link Objects#equals(Object, Object)}.</p>
         *
         * @param obj the object to compare with, may be {@code null}
         * @return {@code true} if the other object is a {@code Left} with an equal value, {@code false} otherwise
         */
        @Override
        public boolean equals(@Nullable final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Left<?, ?> other)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

        /**
         * Returns the hash code for this {@code Left}.
         *
         * <p>The hash code is computed from the contained left value, combined with a type
         * discriminator to reduce hash collisions between {@code Left} and {@code Right}
         * instances containing equal values.</p>
         *
         * @return the hash code based on the left value
         */
        @Override
        public int hashCode() {
            return Objects.hash(0, this.value);
        }

        /**
         * Returns a string representation of this {@code Left}.
         *
         * <p>The format is {@code Left[value]}, where {@code value} is the string
         * representation of the contained left value.</p>
         *
         * @return a string in the format {@code Left[value]}, never {@code null}
         */
        @NotNull
        @Override
        public String toString() {
            return "Left[" + this.value + "]";
        }
    }

    /**
     * Implementation of {@link Either} representing a right value.
     *
     * <p>By convention, the right case represents a success or primary result.
     * This implementation is immutable and thread-safe.</p>
     *
     * <h2>Usage</h2>
     * <p>Instances should be created via the factory method {@link Either#right(Object)}
     * rather than directly constructing this class.</p>
     *
     * <h2>Behavior</h2>
     * <ul>
     *   <li>{@link #isLeft()} always returns {@code false}</li>
     *   <li>{@link #isRight()} always returns {@code true}</li>
     *   <li>{@link #map(Function)} applies the mapper to the right value</li>
     *   <li>{@link #flatMap(Function)} applies the mapper and returns its result</li>
     *   <li>{@link #mapLeft(Function)} returns this instance unchanged (with adjusted type parameters)</li>
     *   <li>{@link #fold(Function, Function)} applies the right mapper</li>
     *   <li>{@link #ifLeft(Consumer)} does not invoke the consumer</li>
     *   <li>{@link #ifRight(Consumer)} invokes the consumer with the right value</li>
     *   <li>{@link #getOrElse(Object)} returns the right value</li>
     *   <li>{@link #getOrElseGet(Function)} returns the right value</li>
     *   <li>{@link #getOrElseThrow(Function)} returns the right value</li>
     * </ul>
     *
     * @param <L> the type of the left value (phantom type parameter for this implementation)
     * @param <R> the type of the right value
     * @since 0.1.0
     */
    final class Right<L, R> implements Either<L, R> {

        /**
         * The right value held by this instance.
         */
        private final R value;

        /**
         * Constructs a new {@code Right} containing the specified value.
         *
         * <p>This constructor is intentionally private. Use the factory method
         * {@link Either#right(Object)} to create instances.</p>
         *
         * @param value the right value, must not be {@code null}
         * @throws NullPointerException if {@code value} is {@code null}
         */
        private Right(@NotNull final R value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code false} for {@code Right}
         */
        @Override
        public boolean isLeft() {
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code true} for {@code Right}
         */
        @Override
        public boolean isRight() {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, applies the mapper to the right value and returns a new
         * {@code Right} containing the result.</p>
         *
         * @param mapper the mapping function to apply to the right value, must not be {@code null}
         * @param <U>    the new right type
         * @return a new {@code Right} containing the mapped value, never {@code null}
         */
        @NotNull
        @Override
        public <U> Either<L, U> map(@NotNull final Function<? super R, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return Either.right(mapper.apply(this.value));
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, applies the mapper to the right value and returns the
         * resulting {@code Either} directly.</p>
         *
         * @param mapper the mapping function to apply to the right value, must not be {@code null}
         * @param <U>    the new right type
         * @return the result of applying the mapper to the right value, never {@code null}
         */
        @NotNull
        @Override
        public <U> Either<L, U> flatMap(@NotNull final Function<? super R, ? extends Either<L, U>> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return Objects.requireNonNull(mapper.apply(this.value), "mapper must not return null");
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, the mapper is not applied and this instance is returned unchanged
         * with adjusted type parameters.</p>
         *
         * @param mapper the mapping function, must not be {@code null} (validated but not applied)
         * @param <T>    the new left type
         * @return this {@code Right} instance cast to the new type, never {@code null}
         */
        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <T> Either<T, R> mapLeft(@NotNull final Function<? super L, ? extends T> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return (Either<T, R>) this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, applies the {@code rightMapper} to the right value.</p>
         *
         * @param leftMapper  the function for left values (validated but not applied)
         * @param rightMapper the function to apply to the right value, must not be {@code null}
         * @param <U>         the result type
         * @return the result of applying {@code rightMapper} to the right value
         */
        @Override
        public <U> U fold(@NotNull final Function<? super L, ? extends U> leftMapper,
                          @NotNull final Function<? super R, ? extends U> rightMapper) {
            Objects.requireNonNull(leftMapper, "leftMapper must not be null");
            Objects.requireNonNull(rightMapper, "rightMapper must not be null");
            return rightMapper.apply(this.value);
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, returns a {@link Left} containing the right value.</p>
         *
         * @return a {@code Left} containing this right's value, never {@code null}
         */
        @NotNull
        @Override
        public Either<R, L> swap() {
            return Either.left(this.value);
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, the consumer is not invoked.</p>
         *
         * @param consumer the consumer (validated but not invoked), must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Either<L, R> ifLeft(@NotNull final Consumer<? super L> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, invokes the consumer with the right value.</p>
         *
         * @param consumer the consumer to invoke with the right value, must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Either<L, R> ifRight(@NotNull final Consumer<? super R> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            consumer.accept(this.value);
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, returns the right value, ignoring the fallback.</p>
         *
         * @param other the fallback value (validated but not used), must not be {@code null}
         * @return the right value, never {@code null}
         */
        @NotNull
        @Override
        public R getOrElse(@NotNull final R other) {
            Objects.requireNonNull(other, "other must not be null");
            return this.value;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, returns the right value without invoking the fallback function.</p>
         *
         * @param fallback the fallback function (validated but not invoked), must not be {@code null}
         * @return the right value, never {@code null}
         */
        @NotNull
        @Override
        public R getOrElseGet(@NotNull final Function<? super L, ? extends R> fallback) {
            Objects.requireNonNull(fallback, "fallback must not be null");
            return this.value;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Right}, returns the right value without throwing.</p>
         *
         * @param exceptionMapper the exception mapper (validated but not invoked), must not be {@code null}
         * @return the right value, never {@code null}
         */
        @NotNull
        @Override
        public R getOrElseThrow(@NotNull final Function<? super L, ? extends RuntimeException> exceptionMapper) {
            Objects.requireNonNull(exceptionMapper, "exceptionMapper must not be null");
            return this.value;
        }

        /**
         * Compares this {@code Right} to another object for equality.
         *
         * <p>Two {@code Right} instances are considered equal if and only if they contain
         * equal values as determined by {@link Objects#equals(Object, Object)}.</p>
         *
         * @param obj the object to compare with, may be {@code null}
         * @return {@code true} if the other object is a {@code Right} with an equal value, {@code false} otherwise
         */
        @Override
        public boolean equals(@Nullable final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Right<?, ?> other)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

        /**
         * Returns the hash code for this {@code Right}.
         *
         * <p>The hash code is computed from the contained right value, combined with a type
         * discriminator to reduce hash collisions between {@code Left} and {@code Right}
         * instances containing equal values.</p>
         *
         * @return the hash code based on the right value
         */
        @Override
        public int hashCode() {
            return Objects.hash(1, this.value);
        }

        /**
         * Returns a string representation of this {@code Right}.
         *
         * <p>The format is {@code Right[value]}, where {@code value} is the string
         * representation of the contained right value.</p>
         *
         * @return a string in the format {@code Right[value]}, never {@code null}
         */
        @NotNull
        @Override
        public String toString() {
            return "Right[" + this.value + "]";
        }
    }
}
