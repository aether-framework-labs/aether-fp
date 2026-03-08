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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A success-biased algebraic sum type representing the outcome of an operation that may either
 * succeed with a value of type {@code T} or fail with an error of type {@code E}.
 *
 * <p>{@code Result<T, E>} is a discriminated union that models exactly one of two exclusive states:
 * a {@link Success} carrying the successful outcome, or a {@link Failure} carrying the error that
 * describes why the operation failed. An instance is never both, never neither, and never
 * {@code null}.</p>
 *
 * <h2>Distinction from {@link Either}</h2>
 * <p>While {@link Either} is a general-purpose sum type with neutral left/right semantics,
 * {@code Result} is purpose-built for modeling operation outcomes. Its API uses explicit
 * success/failure vocabulary, provides dedicated recovery operations ({@link #recover(Function)},
 * {@link #recoverWith(Function)}), and can be converted to an {@link Either} via
 * {@link #toEither()} when a more general representation is needed. The success-biased design
 * means that functional composition naturally flows along the happy path while errors propagate
 * unchanged.</p>
 *
 * <h2>Semantics</h2>
 * <p>This type is success-biased. All primary transformation operations target the success value:</p>
 * <ul>
 *   <li>{@link #map(Function)} transforms the success value, leaving failures unchanged.</li>
 *   <li>{@link #flatMap(Function)} chains operations that may themselves fail.</li>
 *   <li>{@link #mapError(Function)} transforms the error value, leaving successes unchanged.</li>
 *   <li>{@link #recover(Function)} converts a failure into a success.</li>
 *   <li>{@link #recoverWith(Function)} converts a failure into a new {@code Result}.</li>
 *   <li>{@link #fold(Function, Function)} forces exhaustive handling of both cases.</li>
 * </ul>
 *
 * <h2>Null Policy</h2>
 * <p>Null values are strictly prohibited. All factory methods, all function parameters, and all
 * mapper return values are validated against {@code null}. Any attempt to create a {@code Result}
 * containing {@code null}, or to pass a {@code null} function argument, will result in a
 * {@link NullPointerException} being thrown immediately.</p>
 *
 * <h2>Immutability</h2>
 * <p>All instances of {@code Result} are immutable and therefore inherently thread-safe.
 * Every transformation operation returns a new instance; no existing state is ever modified.</p>
 *
 * <h2>Functional Composition</h2>
 * <p>The primary way to work with {@code Result} values is through functional composition.
 * Use {@link #map(Function)} and {@link #flatMap(Function)} for success-biased transformations,
 * {@link #mapError(Function)} for error transformations, {@link #recover(Function)} and
 * {@link #recoverWith(Function)} for error recovery, and {@link #fold(Function, Function)} for
 * exhaustive pattern matching that forces handling of both cases.</p>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Create result values
 * Result<Integer, String> success = Result.success(42);
 * Result<Integer, String> failure = Result.failure("not found");
 *
 * // Success-biased map - only transforms the success value
 * Result<String, String> mapped = success.map(n -> "value: " + n);
 * // mapped = Success[value: 42]
 *
 * // Failures propagate unchanged through map
 * Result<String, String> stillFailed = failure.map(n -> "value: " + n);
 * // stillFailed = Failure[not found]
 *
 * // Chain operations that may fail using flatMap
 * Result<Integer, String> parsed = Result.<String, String>success("123")
 *     .flatMap(s -> {
 *         try {
 *             return Result.success(Integer.parseInt(s));
 *         } catch (NumberFormatException e) {
 *             return Result.failure("invalid number: " + s);
 *         }
 *     });
 *
 * // Recover from failure
 * Result<Integer, String> recovered = failure.recover(err -> 0);
 * // recovered = Success[0]
 *
 * // Exhaustive pattern matching with fold
 * String result = success.fold(
 *     error -> "Error: " + error,
 *     value -> "Success: " + value
 * );
 * // result = "Success: 42"
 *
 * // Convert to Either for interop
 * Either<String, Integer> either = success.toEither();
 * // either = Right[42]
 * }</pre>
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error value
 * @author Erik Pförtner
 * @since 0.1.0
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    /**
     * Creates a successful {@code Result} containing the given value.
     *
     * <p>Use this factory method to construct a {@code Result} that represents the successful
     * outcome of an operation.</p>
     *
     * <pre>{@code
     * Result<Integer, String> ok = Result.success(42);
     * }</pre>
     *
     * @param value the success value, must not be {@code null}
     * @param <T>   the type of the success value
     * @param <E>   the type of the error value
     * @return a successful {@code Result} containing the specified value, never {@code null}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @NotNull
    static <T, E> Result<T, E> success(@NotNull final T value) {
        Objects.requireNonNull(value, "value must not be null");
        return new Success<>(value);
    }

    /**
     * Creates a failed {@code Result} containing the given error.
     *
     * <p>Use this factory method to construct a {@code Result} that represents the failed
     * outcome of an operation.</p>
     *
     * <pre>{@code
     * Result<Integer, String> err = Result.failure("not found");
     * }</pre>
     *
     * @param error the error value, must not be {@code null}
     * @param <T>   the type of the success value
     * @param <E>   the type of the error value
     * @return a failed {@code Result} containing the specified error, never {@code null}
     * @throws NullPointerException if {@code error} is {@code null}
     */
    @NotNull
    static <T, E> Result<T, E> failure(@NotNull final E error) {
        Objects.requireNonNull(error, "error must not be null");
        return new Failure<>(error);
    }

    /**
     * Returns {@code true} if this {@code Result} represents a successful outcome.
     *
     * <p>Exactly one of {@code isSuccess()} and {@link #isFailure()} will return {@code true}
     * for any given instance.</p>
     *
     * @return {@code true} if this is a {@link Success}, {@code false} if this is a {@link Failure}
     */
    boolean isSuccess();

    /**
     * Returns {@code true} if this {@code Result} represents a failed outcome.
     *
     * <p>Exactly one of {@link #isSuccess()} and {@code isFailure()} will return {@code true}
     * for any given instance.</p>
     *
     * @return {@code true} if this is a {@link Failure}, {@code false} if this is a {@link Success}
     */
    boolean isFailure();

    /**
     * Transforms the success value using the given mapping function.
     *
     * <p>If this is a {@link Success}, applies the mapper to the success value and returns a new
     * {@link Success} containing the result. If this is a {@link Failure}, returns this instance
     * unchanged with adjusted type parameters. This is the success-biased map operation.</p>
     *
     * <pre>{@code
     * Result<Integer, String> success = Result.success(42);
     * Result<String, String> mapped = success.map(n -> "Number: " + n);
     * // mapped = Success[Number: 42]
     *
     * Result<Integer, String> failure = Result.failure("error");
     * Result<String, String> stillFailed = failure.map(n -> "Number: " + n);
     * // stillFailed = Failure[error]
     * }</pre>
     *
     * @param mapper the mapping function to apply to the success value, must not be {@code null}
     *               and must not return {@code null}
     * @param <U>    the type of the new success value
     * @return a new {@code Result} with the transformed success value, or this failure unchanged;
     *         never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}, or if the mapper returns
     *                              {@code null}
     */
    @NotNull
    <U> Result<U, E> map(@NotNull final Function<? super T, ? extends U> mapper);

    /**
     * Transforms the success value using a function that itself returns a {@code Result}.
     *
     * <p>If this is a {@link Success}, applies the mapper to the success value and returns the
     * resulting {@code Result} directly (without wrapping it in another layer). If this is a
     * {@link Failure}, returns this instance unchanged with adjusted type parameters. This enables
     * chaining of operations that may each independently fail.</p>
     *
     * <pre>{@code
     * Result<String, String> input = Result.success("123");
     * Result<Integer, String> parsed = input.flatMap(s -> {
     *     try {
     *         return Result.success(Integer.parseInt(s));
     *     } catch (NumberFormatException e) {
     *         return Result.failure("invalid number: " + s);
     *     }
     * });
     * // parsed = Success[123]
     * }</pre>
     *
     * @param mapper the mapping function returning a {@code Result}, must not be {@code null} and
     *               must not return {@code null}
     * @param <U>    the type of the new success value
     * @return the result of applying the function if this is a success, or this failure unchanged;
     *         never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}, or if the mapper returns
     *                              {@code null}
     */
    @NotNull
    <U> Result<U, E> flatMap(@NotNull final Function<? super T, ? extends Result<U, E>> mapper);

    /**
     * Transforms the error value using the given mapping function.
     *
     * <p>If this is a {@link Failure}, applies the mapper to the error value and returns a new
     * {@link Failure} containing the result. If this is a {@link Success}, returns this instance
     * unchanged with adjusted type parameters.</p>
     *
     * <pre>{@code
     * Result<Integer, String> failure = Result.failure("not found");
     * Result<Integer, Integer> mapped = failure.mapError(String::length);
     * // mapped = Failure[9]
     *
     * Result<Integer, String> success = Result.success(42);
     * Result<Integer, Integer> stillSuccess = success.mapError(String::length);
     * // stillSuccess = Success[42]
     * }</pre>
     *
     * @param mapper the mapping function to apply to the error value, must not be {@code null} and
     *               must not return {@code null}
     * @param <X>    the type of the new error value
     * @return a new {@code Result} with the transformed error value, or this success unchanged;
     *         never {@code null}
     * @throws NullPointerException if {@code mapper} is {@code null}, or if the mapper returns
     *                              {@code null}
     */
    @NotNull
    <X> Result<T, X> mapError(@NotNull final Function<? super E, ? extends X> mapper);

    /**
     * Applies one of two functions depending on whether this is a success or failure, forcing
     * exhaustive handling of both cases.
     *
     * <p>Exactly one of the two provided functions will be applied. If this is a {@link Failure},
     * the {@code failureMapper} is applied to the error value. If this is a {@link Success}, the
     * {@code successMapper} is applied to the success value. Both functions must produce a result
     * of the same type {@code U}.</p>
     *
     * <pre>{@code
     * Result<Integer, String> result = Result.success(42);
     * String message = result.fold(
     *     error -> "Failed: " + error,
     *     value -> "Success: " + value
     * );
     * // message = "Success: 42"
     * }</pre>
     *
     * @param failureMapper the function to apply if this is a failure, must not be {@code null}
     * @param successMapper the function to apply if this is a success, must not be {@code null}
     * @param <U>           the common result type of both functions
     * @return the result of applying the appropriate function
     * @throws NullPointerException if {@code failureMapper} or {@code successMapper} is
     *                              {@code null}
     */
    <U> U fold(@NotNull final Function<? super E, ? extends U> failureMapper,
               @NotNull final Function<? super T, ? extends U> successMapper);

    /**
     * Attempts to recover from a failure by converting the error into a success value.
     *
     * <p>If this is a {@link Failure}, applies the recovery mapper to the error value and returns
     * a new {@link Success} containing the result. If this is a {@link Success}, returns this
     * instance unchanged. The recovery mapper must not return {@code null}.</p>
     *
     * <pre>{@code
     * Result<Integer, String> failure = Result.failure("not found");
     * Result<Integer, String> recovered = failure.recover(err -> 0);
     * // recovered = Success[0]
     *
     * Result<Integer, String> success = Result.success(42);
     * Result<Integer, String> unchanged = success.recover(err -> 0);
     * // unchanged = Success[42]
     * }</pre>
     *
     * @param recoveryMapper the function to convert the error into a success value, must not be
     *                       {@code null} and must not return {@code null}
     * @return a successful {@code Result} containing either the original success value or the
     *         recovered value; never {@code null}
     * @throws NullPointerException if {@code recoveryMapper} is {@code null}, or if the recovery
     *                              mapper returns {@code null}
     */
    @NotNull
    Result<T, E> recover(@NotNull final Function<? super E, ? extends T> recoveryMapper);

    /**
     * Attempts to recover from a failure by producing a new {@code Result} from the error.
     *
     * <p>If this is a {@link Failure}, applies the recovery mapper to the error value and returns
     * the resulting {@code Result} directly. If this is a {@link Success}, returns this instance
     * unchanged. Unlike {@link #recover(Function)}, this method allows the recovery operation
     * itself to fail by returning another {@link Failure}.</p>
     *
     * <pre>{@code
     * Result<Integer, String> failure = Result.failure("not found");
     * Result<Integer, String> recovered = failure.recoverWith(err ->
     *     err.equals("not found") ? Result.success(0) : Result.failure(err)
     * );
     * // recovered = Success[0]
     * }</pre>
     *
     * @param recoveryMapper the function to produce a new {@code Result} from the error, must not
     *                       be {@code null} and must not return {@code null}
     * @return the recovered {@code Result} or this success unchanged; never {@code null}
     * @throws NullPointerException if {@code recoveryMapper} is {@code null}, or if the recovery
     *                              mapper returns {@code null}
     */
    @NotNull
    Result<T, E> recoverWith(@NotNull final Function<? super E, ? extends Result<T, E>> recoveryMapper);

    /**
     * Executes the given consumer if this is a success.
     *
     * <p>This method is intended for observation and side effects such as logging or result
     * processing. The consumer is only invoked if this is a {@link Success}. The method returns
     * this same instance to allow fluent chaining. No state is mutated.</p>
     *
     * <pre>{@code
     * Result<Integer, String> result = Result.success(42);
     * result.ifSuccess(value -> System.out.println("Got: " + value))
     *       .ifFailure(error -> System.err.println("Error: " + error));
     * }</pre>
     *
     * @param consumer the consumer to execute with the success value, must not be {@code null}
     * @return this {@code Result} instance for method chaining, never {@code null}
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    @NotNull
    Result<T, E> ifSuccess(@NotNull final Consumer<? super T> consumer);

    /**
     * Executes the given consumer if this is a failure.
     *
     * <p>This method is intended for observation and side effects such as logging or error
     * reporting. The consumer is only invoked if this is a {@link Failure}. The method returns
     * this same instance to allow fluent chaining. No state is mutated.</p>
     *
     * <pre>{@code
     * Result<Integer, String> result = Result.failure("error");
     * result.ifFailure(error -> System.err.println("Error: " + error))
     *       .ifSuccess(value -> System.out.println("Got: " + value));
     * }</pre>
     *
     * @param consumer the consumer to execute with the error value, must not be {@code null}
     * @return this {@code Result} instance for method chaining, never {@code null}
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    @NotNull
    Result<T, E> ifFailure(@NotNull final Consumer<? super E> consumer);

    /**
     * Returns the success value if this is a {@link Success}, otherwise returns the provided
     * fallback value.
     *
     * <pre>{@code
     * Result<Integer, String> success = Result.success(42);
     * Integer value1 = success.getOrElse(0);  // 42
     *
     * Result<Integer, String> failure = Result.failure("error");
     * Integer value2 = failure.getOrElse(0);  // 0
     * }</pre>
     *
     * @param other the fallback value to return if this is a failure, must not be {@code null}
     * @return the success value if present, otherwise the provided fallback; never {@code null}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    @NotNull
    T getOrElse(@NotNull final T other);

    /**
     * Returns the success value if this is a {@link Success}, otherwise computes a fallback value
     * from the error using the provided function.
     *
     * <p>Unlike {@link #getOrElse(Object)}, this method allows the fallback to be derived from
     * the error value, enabling context-aware error recovery.</p>
     *
     * <pre>{@code
     * Result<String, Integer> failure = Result.failure(404);
     * String result = failure.getOrElseGet(code -> "Error code: " + code);
     * // result = "Error code: 404"
     * }</pre>
     *
     * @param fallback the function to compute a fallback from the error value, must not be
     *                 {@code null} and must not return {@code null}
     * @return the success value if present, otherwise the computed fallback; never {@code null}
     * @throws NullPointerException if {@code fallback} is {@code null}, or if the fallback
     *                              function returns {@code null}
     */
    @NotNull
    T getOrElseGet(@NotNull final Function<? super E, ? extends T> fallback);

    /**
     * Returns the success value if this is a {@link Success}, otherwise throws a runtime exception
     * created from the error value using the provided mapping function.
     *
     * <p>This method bridges the gap between functional error handling and Java's exception
     * mechanism. It is typically used at the boundary of a functional pipeline where exceptions
     * are the expected error propagation strategy.</p>
     *
     * <pre>{@code
     * Result<Integer, String> result = Result.failure("not found");
     * try {
     *     Integer value = result.getOrElseThrow(IllegalStateException::new);
     * } catch (IllegalStateException e) {
     *     // e.getMessage() == "not found"
     * }
     * }</pre>
     *
     * @param exceptionMapper the function to create a runtime exception from the error value,
     *                        must not be {@code null}
     * @return the success value if this is a success
     * @throws RuntimeException     the exception created from the error value if this is a failure
     * @throws NullPointerException if {@code exceptionMapper} is {@code null}
     */
    @NotNull
    T getOrElseThrow(@NotNull final Function<? super E, ? extends RuntimeException> exceptionMapper);

    /**
     * Converts this {@code Result} to an {@link Either}.
     *
     * <p>A {@link Success} is converted to an {@link Either.Right Either.Right} containing the
     * success value, and a {@link Failure} is converted to an {@link Either.Left Either.Left}
     * containing the error value. This allows seamless interoperability between the two sum
     * types.</p>
     *
     * <pre>{@code
     * Result<Integer, String> success = Result.success(42);
     * Either<String, Integer> right = success.toEither();
     * // right = Right[42]
     *
     * Result<Integer, String> failure = Result.failure("error");
     * Either<String, Integer> left = failure.toEither();
     * // left = Left[error]
     * }</pre>
     *
     * @return an {@link Either} representing this result, never {@code null}
     */
    @NotNull
    Either<E, T> toEither();

    /**
     * Implementation of {@link Result} representing a successful outcome.
     *
     * <p>This implementation carries the success value and is the target of all success-biased
     * operations. It is immutable and thread-safe.</p>
     *
     * <h2>Usage</h2>
     * <p>Instances should be created via the factory method {@link Result#success(Object)}
     * rather than directly constructing this class.</p>
     *
     * <h2>Behavior</h2>
     * <ul>
     *   <li>{@link #isSuccess()} always returns {@code true}</li>
     *   <li>{@link #isFailure()} always returns {@code false}</li>
     *   <li>{@link #map(Function)} applies the mapper to the success value</li>
     *   <li>{@link #flatMap(Function)} applies the mapper and returns its result</li>
     *   <li>{@link #mapError(Function)} returns this instance unchanged (with adjusted type
     *       parameters)</li>
     *   <li>{@link #fold(Function, Function)} applies the success mapper</li>
     *   <li>{@link #recover(Function)} returns this instance unchanged</li>
     *   <li>{@link #recoverWith(Function)} returns this instance unchanged</li>
     *   <li>{@link #ifSuccess(Consumer)} invokes the consumer with the success value</li>
     *   <li>{@link #ifFailure(Consumer)} does not invoke the consumer</li>
     *   <li>{@link #getOrElse(Object)} returns the success value</li>
     *   <li>{@link #getOrElseGet(Function)} returns the success value</li>
     *   <li>{@link #getOrElseThrow(Function)} returns the success value</li>
     *   <li>{@link #toEither()} returns {@code Either.right(value)}</li>
     * </ul>
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error value (phantom type parameter for this implementation)
     * @since 0.1.0
     */
    final class Success<T, E> implements Result<T, E> {

        /**
         * The success value held by this instance.
         */
        private final T value;

        /**
         * Constructs a new {@code Success} containing the specified value.
         *
         * <p>This constructor is intentionally private. Use the factory method
         * {@link Result#success(Object)} to create instances.</p>
         *
         * @param value the success value, must not be {@code null}
         * @throws NullPointerException if {@code value} is {@code null}
         */
        private Success(@NotNull final T value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code true} for {@code Success}
         */
        @Override
        public boolean isSuccess() {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code false} for {@code Success}
         */
        @Override
        public boolean isFailure() {
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, applies the mapper to the success value and returns a new
         * {@code Success} containing the result.</p>
         *
         * @param mapper the mapping function to apply to the success value, must not be
         *               {@code null}
         * @param <U>    the new success type
         * @return a new {@code Success} containing the mapped value, never {@code null}
         */
        @NotNull
        @Override
        public <U> Result<U, E> map(@NotNull final Function<? super T, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return Result.success(mapper.apply(this.value));
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, applies the mapper to the success value and returns the
         * resulting {@code Result} directly.</p>
         *
         * @param mapper the mapping function to apply to the success value, must not be
         *               {@code null}
         * @param <U>    the new success type
         * @return the result of applying the mapper to the success value, never {@code null}
         */
        @NotNull
        @Override
        public <U> Result<U, E> flatMap(
                @NotNull final Function<? super T, ? extends Result<U, E>> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return Objects.requireNonNull(mapper.apply(this.value),
                    "mapper must not return null");
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, the mapper is not applied and this instance is returned
         * unchanged with adjusted type parameters. The success value propagates through the
         * operation.</p>
         *
         * @param mapper the mapping function, must not be {@code null} (validated but not applied)
         * @param <X>    the new error type
         * @return this {@code Success} instance cast to the new type, never {@code null}
         */
        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <X> Result<T, X> mapError(@NotNull final Function<? super E, ? extends X> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return (Result<T, X>) this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, applies the {@code successMapper} to the success value.</p>
         *
         * @param failureMapper the function for failure values (validated but not applied)
         * @param successMapper the function to apply to the success value, must not be
         *                      {@code null}
         * @param <U>           the result type
         * @return the result of applying {@code successMapper} to the success value
         */
        @Override
        public <U> U fold(@NotNull final Function<? super E, ? extends U> failureMapper,
                          @NotNull final Function<? super T, ? extends U> successMapper) {
            Objects.requireNonNull(failureMapper, "failureMapper must not be null");
            Objects.requireNonNull(successMapper, "successMapper must not be null");
            return successMapper.apply(this.value);
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, returns this instance unchanged. Recovery is not needed
         * because the operation already succeeded.</p>
         *
         * @param recoveryMapper the recovery function (validated but not applied), must not be
         *                       {@code null}
         * @return this instance, never {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> recover(
                @NotNull final Function<? super E, ? extends T> recoveryMapper) {
            Objects.requireNonNull(recoveryMapper, "recoveryMapper must not be null");
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, returns this instance unchanged. Recovery is not needed
         * because the operation already succeeded.</p>
         *
         * @param recoveryMapper the recovery function (validated but not applied), must not be
         *                       {@code null}
         * @return this instance, never {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> recoverWith(
                @NotNull final Function<? super E, ? extends Result<T, E>> recoveryMapper) {
            Objects.requireNonNull(recoveryMapper, "recoveryMapper must not be null");
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, invokes the consumer with the success value.</p>
         *
         * @param consumer the consumer to invoke with the success value, must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> ifSuccess(@NotNull final Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            consumer.accept(this.value);
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, the consumer is not invoked.</p>
         *
         * @param consumer the consumer (validated but not invoked), must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> ifFailure(@NotNull final Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, returns the success value, ignoring the fallback.</p>
         *
         * @param other the fallback value (validated but not used), must not be {@code null}
         * @return the success value, never {@code null}
         */
        @NotNull
        @Override
        public T getOrElse(@NotNull final T other) {
            Objects.requireNonNull(other, "other must not be null");
            return this.value;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, returns the success value without invoking the fallback
         * function.</p>
         *
         * @param fallback the fallback function (validated but not invoked), must not be
         *                 {@code null}
         * @return the success value, never {@code null}
         */
        @NotNull
        @Override
        public T getOrElseGet(@NotNull final Function<? super E, ? extends T> fallback) {
            Objects.requireNonNull(fallback, "fallback must not be null");
            return this.value;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, returns the success value without throwing.</p>
         *
         * @param exceptionMapper the exception mapper (validated but not invoked), must not be
         *                        {@code null}
         * @return the success value, never {@code null}
         */
        @NotNull
        @Override
        public T getOrElseThrow(
                @NotNull final Function<? super E, ? extends RuntimeException> exceptionMapper) {
            Objects.requireNonNull(exceptionMapper, "exceptionMapper must not be null");
            return this.value;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Success}, returns an {@link Either.Right} containing the success
         * value.</p>
         *
         * @return a right-valued {@code Either} containing the success value, never {@code null}
         */
        @NotNull
        @Override
        public Either<E, T> toEither() {
            return Either.right(this.value);
        }

        /**
         * Compares this {@code Success} to another object for equality.
         *
         * <p>Two {@code Success} instances are considered equal if and only if they contain
         * equal values as determined by {@link Objects#equals(Object, Object)}.</p>
         *
         * @param obj the object to compare with
         * @return {@code true} if the other object is a {@code Success} with an equal value,
         *         {@code false} otherwise
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Success<?, ?> other)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

        /**
         * Returns the hash code for this {@code Success}.
         *
         * <p>The hash code is computed from the contained success value.</p>
         *
         * @return the hash code based on the success value
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }

        /**
         * Returns a string representation of this {@code Success}.
         *
         * <p>The format is {@code Success[value]}, where {@code value} is the string
         * representation of the contained success value.</p>
         *
         * @return a string in the format {@code Success[value]}, never {@code null}
         */
        @NotNull
        @Override
        public String toString() {
            return "Success[" + this.value + "]";
        }
    }

    /**
     * Implementation of {@link Result} representing a failed outcome.
     *
     * <p>This implementation carries the error value that describes why the operation failed.
     * It is immutable and thread-safe.</p>
     *
     * <h2>Usage</h2>
     * <p>Instances should be created via the factory method {@link Result#failure(Object)}
     * rather than directly constructing this class.</p>
     *
     * <h2>Behavior</h2>
     * <ul>
     *   <li>{@link #isSuccess()} always returns {@code false}</li>
     *   <li>{@link #isFailure()} always returns {@code true}</li>
     *   <li>{@link #map(Function)} returns this instance unchanged (with adjusted type
     *       parameters)</li>
     *   <li>{@link #flatMap(Function)} returns this instance unchanged (with adjusted type
     *       parameters)</li>
     *   <li>{@link #mapError(Function)} applies the mapper to the error value</li>
     *   <li>{@link #fold(Function, Function)} applies the failure mapper</li>
     *   <li>{@link #recover(Function)} applies the recovery mapper and returns a new
     *       {@code Success}</li>
     *   <li>{@link #recoverWith(Function)} applies the recovery mapper and returns its
     *       result</li>
     *   <li>{@link #ifSuccess(Consumer)} does not invoke the consumer</li>
     *   <li>{@link #ifFailure(Consumer)} invokes the consumer with the error value</li>
     *   <li>{@link #getOrElse(Object)} returns the provided fallback</li>
     *   <li>{@link #getOrElseGet(Function)} computes the fallback from the error value</li>
     *   <li>{@link #getOrElseThrow(Function)} throws the mapped exception</li>
     *   <li>{@link #toEither()} returns {@code Either.left(error)}</li>
     * </ul>
     *
     * @param <T> the type of the success value (phantom type parameter for this implementation)
     * @param <E> the type of the error value
     * @since 0.1.0
     */
    final class Failure<T, E> implements Result<T, E> {

        /**
         * The error value held by this instance.
         */
        private final E error;

        /**
         * Constructs a new {@code Failure} containing the specified error.
         *
         * <p>This constructor is intentionally private. Use the factory method
         * {@link Result#failure(Object)} to create instances.</p>
         *
         * @param error the error value, must not be {@code null}
         * @throws NullPointerException if {@code error} is {@code null}
         */
        private Failure(@NotNull final E error) {
            this.error = Objects.requireNonNull(error, "error must not be null");
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code false} for {@code Failure}
         */
        @Override
        public boolean isSuccess() {
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @return always {@code true} for {@code Failure}
         */
        @Override
        public boolean isFailure() {
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, the mapper is not applied and this instance is returned
         * unchanged with adjusted type parameters. The error value propagates through the
         * operation.</p>
         *
         * @param mapper the mapping function, must not be {@code null} (validated but not applied)
         * @param <U>    the new success type
         * @return this {@code Failure} instance cast to the new type, never {@code null}
         */
        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U, E> map(@NotNull final Function<? super T, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return (Result<U, E>) this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, the mapper is not applied and this instance is returned
         * unchanged with adjusted type parameters. The error value propagates through the
         * operation.</p>
         *
         * @param mapper the mapping function, must not be {@code null} (validated but not applied)
         * @param <U>    the new success type
         * @return this {@code Failure} instance cast to the new type, never {@code null}
         */
        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U, E> flatMap(
                @NotNull final Function<? super T, ? extends Result<U, E>> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return (Result<U, E>) this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, applies the mapper to the error value and returns a new
         * {@code Failure} containing the result.</p>
         *
         * @param mapper the mapping function to apply to the error value, must not be {@code null}
         * @param <X>    the new error type
         * @return a new {@code Failure} containing the mapped error, never {@code null}
         */
        @NotNull
        @Override
        public <X> Result<T, X> mapError(@NotNull final Function<? super E, ? extends X> mapper) {
            Objects.requireNonNull(mapper, "mapper must not be null");
            return Result.failure(mapper.apply(this.error));
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, applies the {@code failureMapper} to the error value.</p>
         *
         * @param failureMapper the function to apply to the error value, must not be {@code null}
         * @param successMapper the function for success values (validated but not applied)
         * @param <U>           the result type
         * @return the result of applying {@code failureMapper} to the error value
         */
        @Override
        public <U> U fold(@NotNull final Function<? super E, ? extends U> failureMapper,
                          @NotNull final Function<? super T, ? extends U> successMapper) {
            Objects.requireNonNull(failureMapper, "failureMapper must not be null");
            Objects.requireNonNull(successMapper, "successMapper must not be null");
            return failureMapper.apply(this.error);
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, applies the recovery mapper to the error value and returns
         * a new {@code Success} containing the result.</p>
         *
         * @param recoveryMapper the function to convert the error into a success value, must not
         *                       be {@code null}
         * @return a new {@code Success} containing the recovered value, never {@code null}
         * @throws NullPointerException if the recovery mapper returns {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> recover(
                @NotNull final Function<? super E, ? extends T> recoveryMapper) {
            Objects.requireNonNull(recoveryMapper, "recoveryMapper must not be null");
            return Result.success(recoveryMapper.apply(this.error));
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, applies the recovery mapper to the error value and returns
         * the resulting {@code Result} directly.</p>
         *
         * @param recoveryMapper the function to produce a new {@code Result} from the error, must
         *                       not be {@code null}
         * @return the result of applying the recovery mapper, never {@code null}
         * @throws NullPointerException if the recovery mapper returns {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> recoverWith(
                @NotNull final Function<? super E, ? extends Result<T, E>> recoveryMapper) {
            Objects.requireNonNull(recoveryMapper, "recoveryMapper must not be null");
            return Objects.requireNonNull(recoveryMapper.apply(this.error),
                    "recoveryMapper must not return null");
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, the consumer is not invoked.</p>
         *
         * @param consumer the consumer (validated but not invoked), must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> ifSuccess(@NotNull final Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, invokes the consumer with the error value.</p>
         *
         * @param consumer the consumer to invoke with the error value, must not be {@code null}
         * @return this instance for method chaining, never {@code null}
         */
        @NotNull
        @Override
        public Result<T, E> ifFailure(@NotNull final Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer, "consumer must not be null");
            consumer.accept(this.error);
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, always returns the provided fallback value.</p>
         *
         * @param other the fallback value to return, must not be {@code null}
         * @return the provided fallback value, never {@code null}
         */
        @NotNull
        @Override
        public T getOrElse(@NotNull final T other) {
            Objects.requireNonNull(other, "other must not be null");
            return other;
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, applies the fallback function to the error value and returns
         * the result.</p>
         *
         * @param fallback the function to compute the fallback from the error value, must not be
         *                 {@code null}
         * @return the computed fallback value, never {@code null}
         * @throws NullPointerException if the fallback function returns {@code null}
         */
        @NotNull
        @Override
        public T getOrElseGet(@NotNull final Function<? super E, ? extends T> fallback) {
            Objects.requireNonNull(fallback, "fallback must not be null");
            final T result = fallback.apply(this.error);
            return Objects.requireNonNull(result, "fallback function must not return null");
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, always throws the exception created from the error value.</p>
         *
         * @param exceptionMapper the function to create an exception from the error value, must
         *                        not be {@code null}
         * @return never returns normally for {@code Failure}
         * @throws RuntimeException the exception created from the error value
         */
        @NotNull
        @Override
        public T getOrElseThrow(
                @NotNull final Function<? super E, ? extends RuntimeException> exceptionMapper) {
            Objects.requireNonNull(exceptionMapper, "exceptionMapper must not be null");
            throw exceptionMapper.apply(this.error);
        }

        /**
         * {@inheritDoc}
         *
         * <p>For {@code Failure}, returns an {@link Either.Left} containing the error value.</p>
         *
         * @return a left-valued {@code Either} containing the error value, never {@code null}
         */
        @NotNull
        @Override
        public Either<E, T> toEither() {
            return Either.left(this.error);
        }

        /**
         * Compares this {@code Failure} to another object for equality.
         *
         * <p>Two {@code Failure} instances are considered equal if and only if they contain
         * equal error values as determined by {@link Objects#equals(Object, Object)}.</p>
         *
         * @param obj the object to compare with
         * @return {@code true} if the other object is a {@code Failure} with an equal error,
         *         {@code false} otherwise
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Failure<?, ?> other)) {
                return false;
            }
            return Objects.equals(this.error, other.error);
        }

        /**
         * Returns the hash code for this {@code Failure}.
         *
         * <p>The hash code is computed from the contained error value.</p>
         *
         * @return the hash code based on the error value
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.error);
        }

        /**
         * Returns a string representation of this {@code Failure}.
         *
         * <p>The format is {@code Failure[error]}, where {@code error} is the string
         * representation of the contained error value.</p>
         *
         * @return a string in the format {@code Failure[error]}, never {@code null}
         */
        @NotNull
        @Override
        public String toString() {
            return "Failure[" + this.error + "]";
        }
    }
}
