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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive test suite for {@link Result}.
 *
 * <p>Tests are organized by API area using nested test classes. Each area covers
 * happy paths, edge cases, null rejection, and behavioral contracts.</p>
 *
 * @since 0.1.0
 */
@DisplayName("Result")
class ResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("success() creates a success-valued Result")
        void success_withValidValue_createsSuccess() {
            final Result<String, Integer> result = Result.success("hello");

            assertThat(result).isInstanceOf(Result.Success.class);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
        }

        @Test
        @DisplayName("failure() creates a failure-valued Result")
        void failure_withValidValue_createsFailure() {
            final Result<String, Integer> result = Result.failure(404);

            assertThat(result).isInstanceOf(Result.Failure.class);
            assertThat(result.isFailure()).isTrue();
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("success(null) throws NullPointerException")
        void success_withNull_throwsNullPointerException() {
            assertThatThrownBy(() -> Result.success(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("failure(null) throws NullPointerException")
        void failure_withNull_throwsNullPointerException() {
            assertThatThrownBy(() -> Result.failure(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("State Methods")
    class StateMethods {

        @Test
        @DisplayName("isSuccess() returns true for Success")
        void isSuccess_onSuccess_returnsTrue() {
            final Result<String, String> result = Result.success("ok");

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess() returns false for Failure")
        void isSuccess_onFailure_returnsFalse() {
            final Result<String, String> result = Result.failure("err");

            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("isFailure() returns true for Failure")
        void isFailure_onFailure_returnsTrue() {
            final Result<String, String> result = Result.failure("err");

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("isFailure() returns false for Success")
        void isFailure_onSuccess_returnsFalse() {
            final Result<String, String> result = Result.success("ok");

            assertThat(result.isFailure()).isFalse();
        }
    }

    @Nested
    @DisplayName("map")
    class Map {

        @Test
        @DisplayName("map() on Success transforms the value")
        void map_onSuccess_transformsValue() {
            final Result<Integer, String> result = Result.success(21);

            final Result<Integer, String> mapped = result.map(v -> v * 2);

            assertThat(mapped.isSuccess()).isTrue();
            final Integer value = mapped.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(42);
        }

        @Test
        @DisplayName("map() on Failure keeps the failure unchanged")
        void map_onFailure_keepsFailureUnchanged() {
            final Result<Integer, String> result = Result.failure("error");

            final Result<String, String> mapped = result.map(v -> "mapped: " + v);

            assertThat(mapped.isFailure()).isTrue();
            final String error = mapped.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("error");
        }

        @Test
        @DisplayName("map() mapper is invoked for Success")
        void map_onSuccess_mapperIsInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.success("value");

            result.map(v -> {
                invoked.set(true);
                return v.toUpperCase();
            });

            assertThat(invoked).isTrue();
        }

        @Test
        @DisplayName("map() mapper is NOT invoked for Failure")
        void map_onFailure_mapperIsNotInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.failure("error");

            result.map(v -> {
                invoked.set(true);
                return v.toUpperCase();
            });

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("map() with null mapper throws NullPointerException")
        void map_withNullMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.map(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.map(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("map() with mapper returning null throws NullPointerException on Success")
        void map_onSuccess_mapperReturningNull_throwsNullPointerException() {
            final Result<String, String> result = Result.success("value");

            assertThatThrownBy(() -> result.map(v -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("flatMap")
    class FlatMap {

        @Test
        @DisplayName("flatMap() on Success returning Success yields Success")
        void flatMap_onSuccess_returningSuccess_yieldsSuccess() {
            final Result<String, String> result = Result.success("123");

            final Result<Integer, String> flatMapped = result.flatMap(
                    v -> Result.success(Integer.parseInt(v)));

            assertThat(flatMapped.isSuccess()).isTrue();
            final Integer value = flatMapped.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(123);
        }

        @Test
        @DisplayName("flatMap() on Success returning Failure yields Failure")
        void flatMap_onSuccess_returningFailure_yieldsFailure() {
            final Result<String, String> result = Result.success("bad");

            final Result<Integer, String> flatMapped = result.flatMap(
                    v -> Result.failure("parse error"));

            assertThat(flatMapped.isFailure()).isTrue();
            final String error = flatMapped.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("parse error");
        }

        @Test
        @DisplayName("flatMap() on Failure keeps the original failure unchanged")
        void flatMap_onFailure_keepsFailureUnchanged() {
            final Result<String, String> result = Result.failure("original");

            final Result<Integer, String> flatMapped = result.flatMap(
                    v -> Result.success(42));

            assertThat(flatMapped.isFailure()).isTrue();
            final String error = flatMapped.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("original");
        }

        @Test
        @DisplayName("flatMap() mapper is invoked for Success")
        void flatMap_onSuccess_mapperIsInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.success("value");

            result.flatMap(v -> {
                invoked.set(true);
                return Result.success(v);
            });

            assertThat(invoked).isTrue();
        }

        @Test
        @DisplayName("flatMap() mapper is NOT invoked for Failure")
        void flatMap_onFailure_mapperIsNotInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.failure("error");

            result.flatMap(v -> {
                invoked.set(true);
                return Result.success(v);
            });

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("flatMap() with null mapper throws NullPointerException")
        void flatMap_withNullMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.flatMap(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.flatMap(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("flatMap() with mapper returning null throws NullPointerException on Success")
        void flatMap_onSuccess_mapperReturningNull_throwsNullPointerException() {
            final Result<String, String> result = Result.success("value");

            assertThatThrownBy(() -> result.flatMap(v -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("mapError")
    class MapError {

        @Test
        @DisplayName("mapError() on Failure transforms the error value")
        void mapError_onFailure_transformsError() {
            final Result<String, String> result = Result.failure("not found");

            final Result<String, Integer> mapped = result.mapError(String::length);

            assertThat(mapped.isFailure()).isTrue();
            final Integer error = mapped.fold(e -> e, v -> null);
            assertThat(error).isEqualTo(9);
        }

        @Test
        @DisplayName("mapError() on Success keeps the success unchanged")
        void mapError_onSuccess_keepsSuccessUnchanged() {
            final Result<String, String> result = Result.success("hello");

            final Result<String, Integer> mapped = result.mapError(String::length);

            assertThat(mapped.isSuccess()).isTrue();
            final String value = mapped.fold(e -> null, v -> v);
            assertThat(value).isEqualTo("hello");
        }

        @Test
        @DisplayName("mapError() mapper is invoked for Failure")
        void mapError_onFailure_mapperIsInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.failure("error");

            result.mapError(e -> {
                invoked.set(true);
                return e.length();
            });

            assertThat(invoked).isTrue();
        }

        @Test
        @DisplayName("mapError() mapper is NOT invoked for Success")
        void mapError_onSuccess_mapperIsNotInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.success("value");

            result.mapError(e -> {
                invoked.set(true);
                return e.length();
            });

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("mapError() with null mapper throws NullPointerException")
        void mapError_withNullMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.mapError(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.mapError(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("mapError() with mapper returning null throws NullPointerException on Failure")
        void mapError_onFailure_mapperReturningNull_throwsNullPointerException() {
            final Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.mapError(e -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fold")
    class Fold {

        @Test
        @DisplayName("fold() on Success applies the success mapper")
        void fold_onSuccess_appliesSuccessMapper() {
            final Result<Integer, String> result = Result.success(42);

            final String folded = result.fold(
                    e -> "Error: " + e,
                    v -> "Value: " + v
            );

            assertThat(folded).isEqualTo("Value: 42");
        }

        @Test
        @DisplayName("fold() on Failure applies the failure mapper")
        void fold_onFailure_appliesFailureMapper() {
            final Result<Integer, String> result = Result.failure("not found");

            final String folded = result.fold(
                    e -> "Error: " + e,
                    v -> "Value: " + v
            );

            assertThat(folded).isEqualTo("Error: not found");
        }

        @Test
        @DisplayName("fold() on Success does NOT invoke failure mapper")
        void fold_onSuccess_doesNotInvokeFailureMapper() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<Integer, String> result = Result.success(1);

            result.fold(
                    e -> {
                        invoked.set(true);
                        return "failure";
                    },
                    v -> "success"
            );

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("fold() on Failure does NOT invoke success mapper")
        void fold_onFailure_doesNotInvokeSuccessMapper() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<Integer, String> result = Result.failure("err");

            result.fold(
                    e -> "failure",
                    v -> {
                        invoked.set(true);
                        return "success";
                    }
            );

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("fold() with null failure mapper throws NullPointerException")
        void fold_withNullFailureMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.fold(null, v -> v))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.fold(null, v -> v))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fold() with null success mapper throws NullPointerException")
        void fold_withNullSuccessMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.fold(e -> e, null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.fold(e -> e, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("recover")
    class Recover {

        @Test
        @DisplayName("recover() on Failure converts to Success")
        void recover_onFailure_convertsToSuccess() {
            final Result<Integer, String> result = Result.failure("not found");

            final Result<Integer, String> recovered = result.recover(e -> 0);

            assertThat(recovered.isSuccess()).isTrue();
            final Integer value = recovered.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(0);
        }

        @Test
        @DisplayName("recover() on Success leaves it unchanged")
        void recover_onSuccess_leavesUnchanged() {
            final Result<Integer, String> result = Result.success(42);

            final Result<Integer, String> recovered = result.recover(e -> 0);

            assertThat(recovered.isSuccess()).isTrue();
            assertThat(recovered).isSameAs(result);
            final Integer value = recovered.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(42);
        }

        @Test
        @DisplayName("recover() mapper is invoked for Failure")
        void recover_onFailure_mapperIsInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<Integer, String> result = Result.failure("error");

            result.recover(e -> {
                invoked.set(true);
                return 0;
            });

            assertThat(invoked).isTrue();
        }

        @Test
        @DisplayName("recover() mapper is NOT invoked for Success")
        void recover_onSuccess_mapperIsNotInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<Integer, String> result = Result.success(42);

            result.recover(e -> {
                invoked.set(true);
                return 0;
            });

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("recover() with null mapper throws NullPointerException")
        void recover_withNullMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.recover(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.recover(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("recover() with mapper returning null throws NullPointerException on Failure")
        void recover_onFailure_mapperReturningNull_throwsNullPointerException() {
            final Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.recover(e -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("recoverWith")
    class RecoverWith {

        @Test
        @DisplayName("recoverWith() on Failure returning Success yields Success")
        void recoverWith_onFailure_returningSuccess_yieldsSuccess() {
            final Result<Integer, String> result = Result.failure("not found");

            final Result<Integer, String> recovered = result.recoverWith(
                    e -> Result.success(0));

            assertThat(recovered.isSuccess()).isTrue();
            final Integer value = recovered.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(0);
        }

        @Test
        @DisplayName("recoverWith() on Failure returning Failure yields Failure")
        void recoverWith_onFailure_returningFailure_yieldsFailure() {
            final Result<Integer, String> result = Result.failure("original");

            final Result<Integer, String> recovered = result.recoverWith(
                    e -> Result.failure("recovery failed"));

            assertThat(recovered.isFailure()).isTrue();
            final String error = recovered.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("recovery failed");
        }

        @Test
        @DisplayName("recoverWith() on Success leaves it unchanged")
        void recoverWith_onSuccess_leavesUnchanged() {
            final Result<Integer, String> result = Result.success(42);

            final Result<Integer, String> recovered = result.recoverWith(
                    e -> Result.success(0));

            assertThat(recovered.isSuccess()).isTrue();
            assertThat(recovered).isSameAs(result);
        }

        @Test
        @DisplayName("recoverWith() mapper is invoked for Failure")
        void recoverWith_onFailure_mapperIsInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<Integer, String> result = Result.failure("error");

            result.recoverWith(e -> {
                invoked.set(true);
                return Result.success(0);
            });

            assertThat(invoked).isTrue();
        }

        @Test
        @DisplayName("recoverWith() mapper is NOT invoked for Success")
        void recoverWith_onSuccess_mapperIsNotInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<Integer, String> result = Result.success(42);

            result.recoverWith(e -> {
                invoked.set(true);
                return Result.success(0);
            });

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("recoverWith() with null mapper throws NullPointerException")
        void recoverWith_withNullMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.recoverWith(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.recoverWith(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("recoverWith() with mapper returning null throws NullPointerException on Failure")
        void recoverWith_onFailure_mapperReturningNull_throwsNullPointerException() {
            final Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.recoverWith(e -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Side Effect Methods")
    class SideEffects {

        @Test
        @DisplayName("ifSuccess() invokes consumer for Success")
        void ifSuccess_onSuccess_invokesConsumer() {
            final AtomicReference<String> captured = new AtomicReference<>();
            final Result<String, String> result = Result.success("hello");

            result.ifSuccess(captured::set);

            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("ifSuccess() does NOT invoke consumer for Failure")
        void ifSuccess_onFailure_doesNotInvokeConsumer() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.failure("error");

            result.ifSuccess(v -> invoked.set(true));

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("ifFailure() invokes consumer for Failure")
        void ifFailure_onFailure_invokesConsumer() {
            final AtomicReference<String> captured = new AtomicReference<>();
            final Result<String, String> result = Result.failure("error");

            result.ifFailure(captured::set);

            assertThat(captured.get()).isEqualTo("error");
        }

        @Test
        @DisplayName("ifFailure() does NOT invoke consumer for Success")
        void ifFailure_onSuccess_doesNotInvokeConsumer() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.success("value");

            result.ifFailure(e -> invoked.set(true));

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("ifSuccess() returns the same instance for fluent chaining")
        void ifSuccess_returnsSameInstance() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThat(success.ifSuccess(v -> { })).isSameAs(success);
            assertThat(failure.ifSuccess(v -> { })).isSameAs(failure);
        }

        @Test
        @DisplayName("ifFailure() returns the same instance for fluent chaining")
        void ifFailure_returnsSameInstance() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThat(success.ifFailure(e -> { })).isSameAs(success);
            assertThat(failure.ifFailure(e -> { })).isSameAs(failure);
        }

        @Test
        @DisplayName("ifSuccess() with null consumer throws NullPointerException")
        void ifSuccess_withNullConsumer_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.ifSuccess(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.ifSuccess(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ifFailure() with null consumer throws NullPointerException")
        void ifFailure_withNullConsumer_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.ifFailure(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.ifFailure(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fluent chaining of ifSuccess and ifFailure works correctly")
        void fluentChaining_ifSuccessAndIfFailure_worksCorrectly() {
            final AtomicReference<String> successCapture = new AtomicReference<>();
            final AtomicReference<String> failureCapture = new AtomicReference<>();

            final Result<String, String> success = Result.success("ok");
            success.ifSuccess(successCapture::set)
                    .ifFailure(failureCapture::set);

            assertThat(successCapture.get()).isEqualTo("ok");
            assertThat(failureCapture.get()).isNull();

            successCapture.set(null);
            final Result<String, String> failure = Result.failure("err");
            failure.ifSuccess(successCapture::set)
                    .ifFailure(failureCapture::set);

            assertThat(successCapture.get()).isNull();
            assertThat(failureCapture.get()).isEqualTo("err");
        }
    }

    @Nested
    @DisplayName("Extraction Methods")
    class ExtractionMethods {

        @Test
        @DisplayName("getOrElse() returns success value when Success")
        void getOrElse_onSuccess_returnsValue() {
            final Result<String, String> result = Result.success("hello");

            assertThat(result.getOrElse("default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("getOrElse() returns fallback when Failure")
        void getOrElse_onFailure_returnsFallback() {
            final Result<String, String> result = Result.failure("error");

            assertThat(result.getOrElse("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getOrElse() with null fallback throws NullPointerException")
        void getOrElse_withNullFallback_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.getOrElse(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.getOrElse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseGet() returns success value when Success")
        void getOrElseGet_onSuccess_returnsValue() {
            final Result<String, String> result = Result.success("hello");

            assertThat(result.getOrElseGet(e -> "computed")).isEqualTo("hello");
        }

        @Test
        @DisplayName("getOrElseGet() computes fallback when Failure")
        void getOrElseGet_onFailure_computesFallback() {
            final Result<String, String> result = Result.failure("error");

            assertThat(result.getOrElseGet(e -> "recovered from: " + e))
                    .isEqualTo("recovered from: error");
        }

        @Test
        @DisplayName("getOrElseGet() fallback function is NOT invoked for Success")
        void getOrElseGet_onSuccess_fallbackNotInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.success("hello");

            result.getOrElseGet(e -> {
                invoked.set(true);
                return "computed";
            });

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("getOrElseGet() fallback function is invoked for Failure")
        void getOrElseGet_onFailure_fallbackIsInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.failure("error");

            result.getOrElseGet(e -> {
                invoked.set(true);
                return "computed";
            });

            assertThat(invoked).isTrue();
        }

        @Test
        @DisplayName("getOrElseGet() with null function throws NullPointerException")
        void getOrElseGet_withNullFunction_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.getOrElseGet(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.getOrElseGet(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseGet() with function returning null throws NullPointerException on Failure")
        void getOrElseGet_onFailure_functionReturningNull_throwsNullPointerException() {
            final Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.getOrElseGet(e -> null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseThrow() returns success value when Success")
        void getOrElseThrow_onSuccess_returnsValue() {
            final Result<String, String> result = Result.success("hello");

            assertThat(result.getOrElseThrow(IllegalStateException::new)).isEqualTo("hello");
        }

        @Test
        @DisplayName("getOrElseThrow() throws mapped exception when Failure")
        void getOrElseThrow_onFailure_throwsMappedException() {
            final Result<String, String> result = Result.failure("not found");

            assertThatThrownBy(() -> result.getOrElseThrow(IllegalStateException::new))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("not found");
        }

        @Test
        @DisplayName("getOrElseThrow() exception mapper is NOT invoked for Success")
        void getOrElseThrow_onSuccess_mapperNotInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.success("hello");

            result.getOrElseThrow(e -> {
                invoked.set(true);
                return new IllegalStateException(e);
            });

            assertThat(invoked).isFalse();
        }

        @Test
        @DisplayName("getOrElseThrow() exception mapper is invoked for Failure")
        void getOrElseThrow_onFailure_mapperIsInvoked() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.getOrElseThrow(e -> {
                invoked.set(true);
                return new IllegalStateException(e);
            })).isInstanceOf(IllegalStateException.class);

            assertThat(invoked).isTrue();
        }

        @Test
        @DisplayName("getOrElseThrow() with null mapper throws NullPointerException")
        void getOrElseThrow_withNullMapper_throwsNullPointerException() {
            final Result<String, String> success = Result.success("value");
            final Result<String, String> failure = Result.failure("error");

            assertThatThrownBy(() -> success.getOrElseThrow(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> failure.getOrElseThrow(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("toEither Interoperability")
    class ToEither {

        @Test
        @DisplayName("toEither() on Success returns Either.Right with the success value")
        void toEither_onSuccess_returnsRight() {
            final Result<Integer, String> result = Result.success(42);

            final Either<String, Integer> either = result.toEither();

            assertThat(either).isInstanceOf(Either.Right.class);
            assertThat(either.isRight()).isTrue();
            final Integer value = either.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(42);
        }

        @Test
        @DisplayName("toEither() on Failure returns Either.Left with the error value")
        void toEither_onFailure_returnsLeft() {
            final Result<Integer, String> result = Result.failure("error");

            final Either<String, Integer> either = result.toEither();

            assertThat(either).isInstanceOf(Either.Left.class);
            assertThat(either.isLeft()).isTrue();
            final String error = either.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("error");
        }

        @Test
        @DisplayName("toEither() on Success preserves value through Either operations")
        void toEither_onSuccess_preservesValueThroughEitherOperations() {
            final Result<String, String> result = Result.success("hello");

            final Either<String, String> either = result.toEither();
            final Either<String, String> mapped = either.map(String::toUpperCase);

            assertThat(mapped.isRight()).isTrue();
            final String value = mapped.fold(e -> null, v -> v);
            assertThat(value).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("toEither() on Failure preserves error through Either operations")
        void toEither_onFailure_preservesErrorThroughEitherOperations() {
            final Result<String, String> result = Result.failure("oops");

            final Either<String, String> either = result.toEither();
            final Either<String, String> mapped = either.map(String::toUpperCase);

            assertThat(mapped.isLeft()).isTrue();
            final String error = mapped.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("oops");
        }
    }

    @Nested
    @DisplayName("Object Contract")
    class ObjectContract {

        @Test
        @DisplayName("Success equals is reflexive")
        void successEquals_isReflexive() {
            final Result<String, String> result = Result.success("hello");

            assertThat(result).isEqualTo(result);
        }

        @Test
        @DisplayName("Failure equals is reflexive")
        void failureEquals_isReflexive() {
            final Result<String, String> result = Result.failure("error");

            assertThat(result).isEqualTo(result);
        }

        @Test
        @DisplayName("Success equals is symmetric")
        void successEquals_isSymmetric() {
            final Result<String, String> a = Result.success("hello");
            final Result<String, String> b = Result.success("hello");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(a);
        }

        @Test
        @DisplayName("Failure equals is symmetric")
        void failureEquals_isSymmetric() {
            final Result<String, String> a = Result.failure("error");
            final Result<String, String> b = Result.failure("error");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(a);
        }

        @Test
        @DisplayName("Success equals is transitive")
        void successEquals_isTransitive() {
            final Result<String, String> a = Result.success("x");
            final Result<String, String> b = Result.success("x");
            final Result<String, String> c = Result.success("x");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(c);
            assertThat(a).isEqualTo(c);
        }

        @Test
        @DisplayName("Failure equals is transitive")
        void failureEquals_isTransitive() {
            final Result<String, String> a = Result.failure("x");
            final Result<String, String> b = Result.failure("x");
            final Result<String, String> c = Result.failure("x");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(c);
            assertThat(a).isEqualTo(c);
        }

        @Test
        @DisplayName("Success with different values are not equal")
        void successWithDifferentValues_areNotEqual() {
            final Result<String, String> a = Result.success("hello");
            final Result<String, String> b = Result.success("world");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Failure with different values are not equal")
        void failureWithDifferentValues_areNotEqual() {
            final Result<String, String> a = Result.failure("error1");
            final Result<String, String> b = Result.failure("error2");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Success and Failure with same inner value are not equal")
        void successAndFailure_withSameInnerValue_areNotEqual() {
            final Result<String, String> success = Result.success("same");
            final Result<String, String> failure = Result.failure("same");

            assertThat(success).isNotEqualTo(failure);
            assertThat(failure).isNotEqualTo(success);
        }

        @Test
        @DisplayName("Success is not equal to null or unrelated types")
        void success_isNotEqualToNullOrUnrelatedTypes() {
            final Result<String, String> result = Result.success("hello");

            assertThat(result).isNotEqualTo(null);
            assertThat(result).isNotEqualTo("hello");
            assertThat(result).isNotEqualTo(42);
        }

        @Test
        @DisplayName("Failure is not equal to null or unrelated types")
        void failure_isNotEqualToNullOrUnrelatedTypes() {
            final Result<String, String> result = Result.failure("error");

            assertThat(result).isNotEqualTo(null);
            assertThat(result).isNotEqualTo("error");
            assertThat(result).isNotEqualTo(42);
        }

        @Test
        @DisplayName("hashCode is consistent for equal Success instances")
        void hashCode_consistentForEqualSuccessInstances() {
            final Result<String, String> a = Result.success("hello");
            final Result<String, String> b = Result.success("hello");

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("hashCode is consistent for equal Failure instances")
        void hashCode_consistentForEqualFailureInstances() {
            final Result<String, String> a = Result.failure("error");
            final Result<String, String> b = Result.failure("error");

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString of Success has format Success[value]")
        void toString_ofSuccess_hasCorrectFormat() {
            final Result<Integer, String> result = Result.success(42);

            assertThat(result.toString()).isEqualTo("Success[42]");
        }

        @Test
        @DisplayName("toString of Failure has format Failure[error]")
        void toString_ofFailure_hasCorrectFormat() {
            final Result<Integer, String> result = Result.failure("not found");

            assertThat(result.toString()).isEqualTo("Failure[not found]");
        }

        @Test
        @DisplayName("toString of Success with string value includes the string")
        void toString_ofSuccessWithString_includesString() {
            final Result<String, String> result = Result.success("hello world");

            assertThat(result.toString()).isEqualTo("Success[hello world]");
        }

        @Test
        @DisplayName("toString of Failure with string error includes the string")
        void toString_ofFailureWithString_includesString() {
            final Result<String, String> result = Result.failure("something went wrong");

            assertThat(result.toString()).isEqualTo("Failure[something went wrong]");
        }
    }

    @Nested
    @DisplayName("Immutability and Stability")
    class ImmutabilityAndStability {

        @Test
        @DisplayName("map() on Failure preserves the original error value")
        void map_onFailure_preservesOriginalError() {
            final Result<Integer, String> failure = Result.failure("original");

            failure.map(v -> v * 2);
            failure.map(v -> "string");

            final String error = failure.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("original");
        }

        @Test
        @DisplayName("mapError() on Success preserves the original success value")
        void mapError_onSuccess_preservesOriginalValue() {
            final Result<String, String> success = Result.success("original");

            success.mapError(e -> 42);
            success.mapError(e -> "changed");

            final String value = success.fold(e -> null, v -> v);
            assertThat(value).isEqualTo("original");
        }

        @Test
        @DisplayName("recover() on Success preserves the original success value")
        void recover_onSuccess_preservesOriginalValue() {
            final Result<String, String> success = Result.success("original");

            success.recover(e -> "recovered");

            final String value = success.fold(e -> null, v -> v);
            assertThat(value).isEqualTo("original");
        }

        @Test
        @DisplayName("ifSuccess() does not mutate the Result")
        void ifSuccess_doesNotMutateResult() {
            final Result<String, String> success = Result.success("value");

            final Result<String, String> returned = success.ifSuccess(v -> { });

            assertThat(returned).isSameAs(success);
            assertThat(returned.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("ifFailure() does not mutate the Result")
        void ifFailure_doesNotMutateResult() {
            final Result<String, String> failure = Result.failure("error");

            final Result<String, String> returned = failure.ifFailure(e -> { });

            assertThat(returned).isSameAs(failure);
            assertThat(returned.isFailure()).isTrue();
        }

        @Test
        @DisplayName("repeated operations yield consistent results")
        void repeatedOperations_yieldConsistentResults() {
            final Result<Integer, String> success = Result.success(10);

            final Result<Integer, String> mapped1 = success.map(v -> v + 1);
            final Result<Integer, String> mapped2 = success.map(v -> v + 1);

            assertThat(mapped1).isEqualTo(mapped2);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Success with empty string is valid")
        void success_withEmptyString_isValid() {
            final Result<String, String> result = Result.success("");

            assertThat(result.isSuccess()).isTrue();
            final String value = result.fold(e -> null, v -> v);
            assertThat(value).isEmpty();
        }

        @Test
        @DisplayName("Failure with empty string is valid")
        void failure_withEmptyString_isValid() {
            final Result<String, String> result = Result.failure("");

            assertThat(result.isFailure()).isTrue();
            final String error = result.fold(e -> e, v -> null);
            assertThat(error).isEmpty();
        }

        @Test
        @DisplayName("Success with zero is valid")
        void success_withZero_isValid() {
            final Result<Integer, String> result = Result.success(0);

            assertThat(result.isSuccess()).isTrue();
            final Integer value = result.fold(e -> null, v -> v);
            assertThat(value).isZero();
        }

        @Test
        @DisplayName("Success with negative value is valid")
        void success_withNegativeValue_isValid() {
            final Result<Integer, String> result = Result.success(-1);

            assertThat(result.isSuccess()).isTrue();
            final Integer value = result.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(-1);
        }

        @Test
        @DisplayName("Failure with negative value is valid")
        void failure_withNegativeValue_isValid() {
            final Result<String, Integer> result = Result.failure(-404);

            assertThat(result.isFailure()).isTrue();
            final Integer error = result.fold(e -> e, v -> null);
            assertThat(error).isEqualTo(-404);
        }

        @Test
        @DisplayName("Success with long string is valid")
        void success_withLongString_isValid() {
            final String longValue = "x".repeat(10_000);
            final Result<String, String> result = Result.success(longValue);

            final String value = result.fold(e -> null, v -> v);
            assertThat(value).hasSize(10_000);
        }

        @Test
        @DisplayName("Identity map on Success returns equal result")
        void identityMap_onSuccess_returnsEqualResult() {
            final Result<String, String> result = Result.success("hello");

            final Result<String, String> mapped = result.map(v -> v);

            assertThat(mapped).isEqualTo(result);
        }

        @Test
        @DisplayName("Identity mapError on Failure returns equal result")
        void identityMapError_onFailure_returnsEqualResult() {
            final Result<String, String> result = Result.failure("error");

            final Result<String, String> mapped = result.mapError(e -> e);

            assertThat(mapped).isEqualTo(result);
        }

        @Test
        @DisplayName("Nested Result type is handled correctly")
        void nestedResultType_isHandledCorrectly() {
            final Result<Result<Integer, String>, String> outer =
                    Result.success(Result.success(42));

            assertThat(outer.isSuccess()).isTrue();
            final Result<Integer, String> inner = outer.fold(e -> null, v -> v);
            assertThat(inner).isNotNull();
            assertThat(inner.isSuccess()).isTrue();
            final Integer value = inner.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(42);
        }

        @Test
        @DisplayName("Success with structured error type works correctly")
        void success_withStructuredErrorType_worksCorrectly() {
            final Result<String, IllegalArgumentException> result =
                    Result.success("ok");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOrElse("fallback")).isEqualTo("ok");
        }

        @Test
        @DisplayName("Failure with structured error type works correctly")
        void failure_withStructuredErrorType_worksCorrectly() {
            final IllegalArgumentException error = new IllegalArgumentException("bad input");
            final Result<String, IllegalArgumentException> result = Result.failure(error);

            assertThat(result.isFailure()).isTrue();
            assertThatThrownBy(() -> result.getOrElseThrow(e -> e))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("bad input");
        }
    }

    @Nested
    @DisplayName("Chaining and Composition")
    class ChainingAndComposition {

        @Test
        @DisplayName("success -> map -> flatMap -> ifSuccess -> getOrElse")
        void successChain_mapFlatMapIfSuccessGetOrElse() {
            final AtomicReference<Integer> sideEffect = new AtomicReference<>();

            final Integer value = Result.<Integer, String>success(10)
                    .map(v -> v * 2)
                    .flatMap(v -> Result.success(v + 1))
                    .ifSuccess(sideEffect::set)
                    .getOrElse(0);

            assertThat(value).isEqualTo(21);
            assertThat(sideEffect.get()).isEqualTo(21);
        }

        @Test
        @DisplayName("failure propagates through map and flatMap chain")
        void failurePropagation_throughMapAndFlatMap() {
            final String result = Result.<Integer, String>failure("initial error")
                    .map(v -> v * 2)
                    .flatMap(v -> Result.success(v + 1))
                    .map(Object::toString)
                    .fold(
                            e -> e,
                            v -> "should not reach"
                    );

            assertThat(result).isEqualTo("initial error");
        }

        @Test
        @DisplayName("failure -> recover -> map yields transformed recovered value")
        void failureRecoverMap_yieldsTransformedRecoveredValue() {
            final Result<String, String> result = Result.<String, String>failure("error")
                    .recover(e -> "recovered")
                    .map(String::toUpperCase);

            assertThat(result.isSuccess()).isTrue();
            final String value = result.fold(e -> null, v -> v);
            assertThat(value).isEqualTo("RECOVERED");
        }

        @Test
        @DisplayName("failure -> recoverWith -> flatMap works correctly")
        void failureRecoverWithFlatMap_worksCorrectly() {
            final Result<Integer, String> result =
                    Result.<Integer, String>failure("not found")
                            .recoverWith(e -> Result.success(0))
                            .flatMap(v -> Result.success(v + 100));

            assertThat(result.isSuccess()).isTrue();
            final Integer value = result.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(100);
        }

        @Test
        @DisplayName("success path is untouched by recovery methods")
        void successPath_untouchedByRecoveryMethods() {
            final AtomicBoolean recoverInvoked = new AtomicBoolean(false);
            final AtomicBoolean recoverWithInvoked = new AtomicBoolean(false);

            final Integer value = Result.<Integer, String>success(42)
                    .recover(e -> {
                        recoverInvoked.set(true);
                        return 0;
                    })
                    .recoverWith(e -> {
                        recoverWithInvoked.set(true);
                        return Result.success(0);
                    })
                    .getOrElse(999);

            assertThat(value).isEqualTo(42);
            assertThat(recoverInvoked).isFalse();
            assertThat(recoverWithInvoked).isFalse();
        }

        @Test
        @DisplayName("toEither after chained operations preserves result")
        void toEither_afterChainedOperations_preservesResult() {
            final Either<String, String> either = Result.<Integer, String>success(5)
                    .map(v -> v * 3)
                    .map(v -> "value: " + v)
                    .toEither();

            assertThat(either.isRight()).isTrue();
            final String value = either.fold(e -> null, v -> v);
            assertThat(value).isEqualTo("value: 15");
        }

        @Test
        @DisplayName("toEither on failed chain preserves error")
        void toEither_onFailedChain_preservesError() {
            final Either<String, Integer> either = Result.<Integer, String>failure("err")
                    .map(v -> v + 1)
                    .mapError(String::toUpperCase)
                    .toEither();

            assertThat(either.isLeft()).isTrue();
            final String error = either.fold(e -> e, v -> null);
            assertThat(error).isEqualTo("ERR");
        }

        @Test
        @DisplayName("multiple successive recovery operations work correctly")
        void multipleRecoveries_workCorrectly() {
            final Result<Integer, String> result =
                    Result.<Integer, String>failure("error")
                            .recoverWith(e -> Result.failure("still failing"))
                            .recoverWith(e -> Result.failure("still failing again"))
                            .recover(e -> 99);

            assertThat(result.isSuccess()).isTrue();
            final Integer value = result.fold(e -> null, v -> v);
            assertThat(value).isEqualTo(99);
        }
    }
}
