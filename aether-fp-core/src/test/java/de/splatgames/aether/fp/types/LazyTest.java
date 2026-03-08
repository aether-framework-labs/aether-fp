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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive test suite for {@link Lazy}.
 *
 * <p>Tests are organized by API area using nested test classes. Each area covers
 * happy paths, edge cases, null rejection, memoization, deferred evaluation,
 * thread-safety, and behavioral contracts.</p>
 *
 * @since 0.1.0
 */
@DisplayName("Lazy")
class LazyTest {

    @Nested
    @DisplayName("Factory Method")
    class FactoryMethod {

        @Test
        @DisplayName("of() creates a Lazy from a valid supplier")
        void of_withValidSupplier_createsLazy() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy).isNotNull();
        }

        @Test
        @DisplayName("of(null) throws NullPointerException")
        void of_withNull_throwsNullPointerException() {
            assertThatThrownBy(() -> Lazy.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("of() does not evaluate the supplier eagerly")
        void of_doesNotEvaluateSupplierEagerly() {
            final AtomicBoolean invoked = new AtomicBoolean(false);

            Lazy.of(() -> {
                invoked.set(true);
                return "value";
            });

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("of() returns an unevaluated Lazy")
        void of_returnsUnevaluatedLazy() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy.isEvaluated()).isFalse();
        }
    }

    @Nested
    @DisplayName("get() and Evaluation Semantics")
    class GetAndEvaluation {

        @Test
        @DisplayName("get() evaluates the supplier on first access")
        void get_evaluatesSupplierOnFirstAccess() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "computed";
            });

            assertThat(invoked.get()).isFalse();

            lazy.get();

            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("get() returns the computed value")
        void get_returnsComputedValue() {
            final Lazy<Integer> lazy = Lazy.of(() -> 42);

            assertThat(lazy.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("get() marks the instance as evaluated")
        void get_marksInstanceAsEvaluated() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            lazy.get();

            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("repeated get() calls return the same value")
        void get_repeatedCalls_returnsSameValue() {
            final Lazy<String> lazy = Lazy.of(() -> "stable");

            final String first = lazy.get();
            final String second = lazy.get();
            final String third = lazy.get();

            assertThat(first).isEqualTo("stable");
            assertThat(second).isEqualTo("stable");
            assertThat(third).isEqualTo("stable");
        }

        @Test
        @DisplayName("get() with supplier returning null throws NullPointerException")
        void get_supplierReturnsNull_throwsNullPointerException() {
            final Lazy<String> lazy = Lazy.of(() -> null);

            assertThatThrownBy(lazy::get)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("get() retries after supplier throws an exception")
        void get_supplierThrows_retriesOnNextCall() {
            final AtomicInteger counter = new AtomicInteger(0);
            final Lazy<String> lazy = Lazy.of(() -> {
                final int attempt = counter.incrementAndGet();
                if (attempt == 1) {
                    throw new RuntimeException("first attempt fails");
                }
                return "success";
            });

            assertThatThrownBy(lazy::get)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("first attempt fails");
            assertThat(lazy.isEvaluated()).isFalse();

            final String result = lazy.get();

            assertThat(result).isEqualTo("success");
            assertThat(lazy.isEvaluated()).isTrue();
            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("get() does not retry after successful evaluation")
        void get_afterSuccess_doesNotRetry() {
            final AtomicInteger counter = new AtomicInteger(0);
            final Lazy<String> lazy = Lazy.of(() -> {
                counter.incrementAndGet();
                return "value";
            });

            lazy.get();
            lazy.get();
            lazy.get();

            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Memoization")
    class Memoization {

        @Test
        @DisplayName("supplier is executed exactly once after repeated get() calls")
        void get_repeatedCalls_supplierExecutedOnce() {
            final AtomicInteger counter = new AtomicInteger(0);
            final Lazy<String> lazy = Lazy.of(() -> {
                counter.incrementAndGet();
                return "memoized";
            });

            for (int i = 0; i < 100; i++) {
                lazy.get();
            }

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("memoized value is the same reference across accesses")
        void get_memoizedValue_isSameReference() {
            final Lazy<List<String>> lazy = Lazy.of(ArrayList::new);

            final List<String> first = lazy.get();
            final List<String> second = lazy.get();

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("map-derived Lazy memoizes its own computation")
        void map_derivedLazy_memoizesComputation() {
            final AtomicInteger mapperCounter = new AtomicInteger(0);
            final Lazy<String> base = Lazy.of(() -> "hello");
            final Lazy<Integer> mapped = base.map(s -> {
                mapperCounter.incrementAndGet();
                return s.length();
            });

            mapped.get();
            mapped.get();
            mapped.get();

            assertThat(mapperCounter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("flatMap-derived Lazy memoizes its own computation")
        void flatMap_derivedLazy_memoizesComputation() {
            final AtomicInteger mapperCounter = new AtomicInteger(0);
            final Lazy<String> base = Lazy.of(() -> "hello");
            final Lazy<String> flatMapped = base.flatMap(s -> {
                mapperCounter.incrementAndGet();
                return Lazy.of(s::toUpperCase);
            });

            flatMapped.get();
            flatMapped.get();
            flatMapped.get();

            assertThat(mapperCounter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("base supplier is invoked at most once even through map chains")
        void mapChain_baseSupplierInvokedOnce() {
            final AtomicInteger baseCounter = new AtomicInteger(0);
            final Lazy<String> base = Lazy.of(() -> {
                baseCounter.incrementAndGet();
                return "root";
            });

            final Lazy<Integer> mapped = base.map(String::length).map(n -> n * 2);

            mapped.get();
            mapped.get();

            assertThat(baseCounter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("isEvaluated()")
    class IsEvaluated {

        @Test
        @DisplayName("returns false before evaluation")
        void isEvaluated_beforeEvaluation_returnsFalse() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        @DisplayName("returns true after successful evaluation")
        void isEvaluated_afterEvaluation_returnsTrue() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            lazy.get();

            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("calling isEvaluated() does not trigger evaluation")
        void isEvaluated_doesNotTriggerEvaluation() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "value";
            });

            lazy.isEvaluated();
            lazy.isEvaluated();
            lazy.isEvaluated();

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("repeated calls return stable results")
        void isEvaluated_repeatedCalls_stableResults() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy.isEvaluated()).isFalse();
            assertThat(lazy.isEvaluated()).isFalse();

            lazy.get();

            assertThat(lazy.isEvaluated()).isTrue();
            assertThat(lazy.isEvaluated()).isTrue();
        }
    }

    @Nested
    @DisplayName("map()")
    class Map {

        @Test
        @DisplayName("map() returns a new Lazy without evaluating the original")
        void map_doesNotEvaluateOriginal() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "hello";
            });

            final Lazy<Integer> mapped = lazy.map(String::length);

            assertThat(invoked.get()).isFalse();
            assertThat(mapped.isEvaluated()).isFalse();
            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        @DisplayName("mapped Lazy evaluates correctly when requested")
        void map_evaluatesCorrectlyWhenRequested() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<Integer> mapped = lazy.map(String::length);

            assertThat(mapped.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("mapper is invoked only when evaluation happens")
        void map_mapperInvokedOnlyOnEvaluation() {
            final AtomicBoolean mapperInvoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<String> mapped = lazy.map(s -> {
                mapperInvoked.set(true);
                return s.toUpperCase();
            });

            assertThat(mapperInvoked.get()).isFalse();

            mapped.get();

            assertThat(mapperInvoked.get()).isTrue();
        }

        @Test
        @DisplayName("mapper is invoked exactly once under memoized semantics")
        void map_mapperInvokedExactlyOnce() {
            final AtomicInteger counter = new AtomicInteger(0);
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<Integer> mapped = lazy.map(s -> {
                counter.incrementAndGet();
                return s.length();
            });

            mapped.get();
            mapped.get();
            mapped.get();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("map(null) throws NullPointerException")
        void map_withNullMapper_throwsNullPointerException() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThatThrownBy(() -> lazy.map(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("mapper returning null throws NullPointerException at evaluation time")
        void map_mapperReturnsNull_throwsNullPointerException() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<Object> mapped = lazy.map(s -> null);

            assertThatThrownBy(mapped::get)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("chained maps preserve laziness")
        void map_chainedMaps_preserveLaziness() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "hello";
            });

            final Lazy<String> chain = lazy
                    .map(String::toUpperCase)
                    .map(s -> s + "!")
                    .map(s -> s + s);

            assertThat(invoked.get()).isFalse();
            assertThat(chain.isEvaluated()).isFalse();

            final String result = chain.get();

            assertThat(result).isEqualTo("HELLO!HELLO!");
            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("map produces a new Lazy instance")
        void map_producesNewInstance() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<String> mapped = lazy.map(s -> s);

            assertThat(mapped).isNotSameAs(lazy);
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class FlatMap {

        @Test
        @DisplayName("flatMap() returns a new Lazy without evaluating the original")
        void flatMap_doesNotEvaluateOriginal() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "hello";
            });

            final Lazy<String> flatMapped = lazy.flatMap(s -> Lazy.of(s::toUpperCase));

            assertThat(invoked.get()).isFalse();
            assertThat(flatMapped.isEvaluated()).isFalse();
            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        @DisplayName("flatMapped Lazy evaluates correctly when requested")
        void flatMap_evaluatesCorrectlyWhenRequested() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<String> flatMapped = lazy.flatMap(
                    s -> Lazy.of(s::toUpperCase)
            );

            assertThat(flatMapped.get()).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("mapper is invoked only when evaluation happens")
        void flatMap_mapperInvokedOnlyOnEvaluation() {
            final AtomicBoolean mapperInvoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<String> flatMapped = lazy.flatMap(s -> {
                mapperInvoked.set(true);
                return Lazy.of(s::toUpperCase);
            });

            assertThat(mapperInvoked.get()).isFalse();

            flatMapped.get();

            assertThat(mapperInvoked.get()).isTrue();
        }

        @Test
        @DisplayName("mapper is invoked exactly once under memoized semantics")
        void flatMap_mapperInvokedExactlyOnce() {
            final AtomicInteger counter = new AtomicInteger(0);
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<String> flatMapped = lazy.flatMap(s -> {
                counter.incrementAndGet();
                return Lazy.of(s::toUpperCase);
            });

            flatMapped.get();
            flatMapped.get();
            flatMapped.get();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("flatMap(null) throws NullPointerException")
        void flatMap_withNullMapper_throwsNullPointerException() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThatThrownBy(() -> lazy.flatMap(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("mapper returning null throws NullPointerException at evaluation time")
        void flatMap_mapperReturnsNull_throwsNullPointerException() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<Object> flatMapped = lazy.flatMap(s -> null);

            assertThatThrownBy(flatMapped::get)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("chained flatMaps preserve laziness")
        void flatMap_chainedFlatMaps_preserveLaziness() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "hello";
            });

            final Lazy<String> chain = lazy
                    .flatMap(s -> Lazy.of(s::toUpperCase))
                    .flatMap(s -> Lazy.of(() -> s + " WORLD"));

            assertThat(invoked.get()).isFalse();
            assertThat(chain.isEvaluated()).isFalse();

            final String result = chain.get();

            assertThat(result).isEqualTo("HELLO WORLD");
            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("flatMap produces a new Lazy instance")
        void flatMap_producesNewInstance() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final Lazy<String> flatMapped = lazy.flatMap(s -> Lazy.of(() -> s));

            assertThat(flatMapped).isNotSameAs(lazy);
        }
    }

    @Nested
    @DisplayName("ifEvaluated()")
    class IfEvaluated {

        @Test
        @DisplayName("does not invoke consumer before evaluation")
        void ifEvaluated_beforeEvaluation_doesNotInvokeConsumer() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final AtomicBoolean invoked = new AtomicBoolean(false);

            lazy.ifEvaluated(v -> invoked.set(true));

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("invokes consumer after evaluation with cached value")
        void ifEvaluated_afterEvaluation_invokesConsumerWithCachedValue() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            final AtomicReference<String> captured = new AtomicReference<>();

            lazy.get();
            lazy.ifEvaluated(captured::set);

            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("does not trigger evaluation on its own")
        void ifEvaluated_doesNotTriggerEvaluation() {
            final AtomicBoolean supplierInvoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                supplierInvoked.set(true);
                return "value";
            });

            lazy.ifEvaluated(v -> {});

            assertThat(supplierInvoked.get()).isFalse();
            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        @DisplayName("returns the same instance for fluent chaining")
        void ifEvaluated_returnsSameInstance() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            final Lazy<String> returned = lazy.ifEvaluated(v -> {});

            assertThat(returned).isSameAs(lazy);
        }

        @Test
        @DisplayName("returns the same instance after evaluation")
        void ifEvaluated_afterEvaluation_returnsSameInstance() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");
            lazy.get();

            final Lazy<String> returned = lazy.ifEvaluated(v -> {});

            assertThat(returned).isSameAs(lazy);
        }

        @Test
        @DisplayName("ifEvaluated(null) throws NullPointerException")
        void ifEvaluated_withNullConsumer_throwsNullPointerException() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThatThrownBy(() -> lazy.ifEvaluated(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("ifEvaluated(null) throws even before evaluation")
        void ifEvaluated_withNullConsumer_throwsEvenBeforeEvaluation() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThatThrownBy(() -> lazy.ifEvaluated(null))
                    .isInstanceOf(NullPointerException.class);

            assertThat(lazy.isEvaluated()).isFalse();
        }
    }

    @Nested
    @DisplayName("toOptional()")
    class ToOptional {

        @Test
        @DisplayName("returns Optional.empty() before evaluation")
        void toOptional_beforeEvaluation_returnsEmpty() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy.toOptional()).isEmpty();
        }

        @Test
        @DisplayName("does not trigger evaluation")
        void toOptional_doesNotTriggerEvaluation() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "value";
            });

            lazy.toOptional();

            assertThat(invoked.get()).isFalse();
            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        @DisplayName("returns Optional.of(value) after evaluation")
        void toOptional_afterEvaluation_returnsPresent() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            lazy.get();

            assertThat(lazy.toOptional()).isPresent().contains("hello");
        }

        @Test
        @DisplayName("returns cached value after evaluation")
        void toOptional_afterEvaluation_returnsCachedValue() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            lazy.get();
            final Optional<String> first = lazy.toOptional();
            final Optional<String> second = lazy.toOptional();

            assertThat(first).contains("hello");
            assertThat(second).contains("hello");
        }

        @Test
        @DisplayName("repeated calls before evaluation remain empty")
        void toOptional_repeatedCallsBeforeEvaluation_remainEmpty() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy.toOptional()).isEmpty();
            assertThat(lazy.toOptional()).isEmpty();
            assertThat(lazy.toOptional()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Object Contract (equals / hashCode / toString)")
    class ObjectContract {

        @Test
        @DisplayName("equals is reflexive")
        void equals_reflexive() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy).isEqualTo(lazy);
        }

        @Test
        @DisplayName("equals is symmetric for equal values")
        void equals_symmetric() {
            final Lazy<String> a = Lazy.of(() -> "hello");
            final Lazy<String> b = Lazy.of(() -> "hello");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(a);
        }

        @Test
        @DisplayName("equals is transitive")
        void equals_transitive() {
            final Lazy<String> a = Lazy.of(() -> "hello");
            final Lazy<String> b = Lazy.of(() -> "hello");
            final Lazy<String> c = Lazy.of(() -> "hello");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(c);
            assertThat(a).isEqualTo(c);
        }

        @Test
        @DisplayName("different values are not equal")
        void equals_differentValues_notEqual() {
            final Lazy<String> a = Lazy.of(() -> "hello");
            final Lazy<String> b = Lazy.of(() -> "world");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals with null returns false")
        void equals_withNull_returnsFalse() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals with different type returns false")
        void equals_withDifferentType_returnsFalse() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy.equals("hello")).isFalse();
        }

        @Test
        @DisplayName("equals triggers evaluation")
        void equals_triggersEvaluation() {
            final AtomicBoolean aInvoked = new AtomicBoolean(false);
            final AtomicBoolean bInvoked = new AtomicBoolean(false);
            final Lazy<String> a = Lazy.of(() -> {
                aInvoked.set(true);
                return "hello";
            });
            final Lazy<String> b = Lazy.of(() -> {
                bInvoked.set(true);
                return "hello";
            });

            a.equals(b);

            assertThat(aInvoked.get()).isTrue();
            assertThat(bInvoked.get()).isTrue();
            assertThat(a.isEvaluated()).isTrue();
            assertThat(b.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("hashCode is consistent for equal Lazy instances")
        void hashCode_consistentForEqualInstances() {
            final Lazy<String> a = Lazy.of(() -> "hello");
            final Lazy<String> b = Lazy.of(() -> "hello");

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("hashCode triggers evaluation")
        void hashCode_triggersEvaluation() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "hello";
            });

            lazy.hashCode();

            assertThat(invoked.get()).isTrue();
            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("hashCode is stable across repeated calls")
        void hashCode_stableAcrossRepeatedCalls() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            final int first = lazy.hashCode();
            final int second = lazy.hashCode();

            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("toString returns Lazy[?] before evaluation")
        void toString_beforeEvaluation_returnsPlaceholder() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            assertThat(lazy.toString()).isEqualTo("Lazy[?]");
        }

        @Test
        @DisplayName("toString returns Lazy[value] after evaluation")
        void toString_afterEvaluation_returnsValue() {
            final Lazy<String> lazy = Lazy.of(() -> "hello");

            lazy.get();

            assertThat(lazy.toString()).isEqualTo("Lazy[hello]");
        }

        @Test
        @DisplayName("toString does not trigger evaluation")
        void toString_doesNotTriggerEvaluation() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "hello";
            });

            lazy.toString();

            assertThat(invoked.get()).isFalse();
            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        @DisplayName("toString with integer value")
        void toString_withIntegerValue() {
            final Lazy<Integer> lazy = Lazy.of(() -> 42);

            lazy.get();

            assertThat(lazy.toString()).isEqualTo("Lazy[42]");
        }

        @Test
        @DisplayName("equals between two lazies with same value, one pre-evaluated")
        void equals_onePreEvaluated_equal() {
            final Lazy<String> a = Lazy.of(() -> "hello");
            final Lazy<String> b = Lazy.of(() -> "hello");

            a.get();

            assertThat(a).isEqualTo(b);
        }
    }

    @Nested
    @DisplayName("Concurrency")
    class Concurrency {

        @Test
        @DisplayName("concurrent get() calls all receive the correct value")
        void get_concurrent_allReceiveCorrectValue() throws Exception {
            final int threadCount = 64;
            final Lazy<String> lazy = Lazy.of(() -> "concurrent-value");
            final CountDownLatch startLatch = new CountDownLatch(1);
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
                final List<Future<String>> futures = new ArrayList<>();
                for (int i = 0; i < threadCount; i++) {
                    futures.add(executor.submit(() -> {
                        startLatch.await();
                        return lazy.get();
                    }));
                }

                startLatch.countDown();

                for (final Future<String> future : futures) {
                    assertThat(future.get()).isEqualTo("concurrent-value");
                }
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("concurrent get() calls invoke supplier exactly once")
        void get_concurrent_supplierInvokedExactlyOnce() throws Exception {
            final int threadCount = 64;
            final AtomicInteger counter = new AtomicInteger(0);
            final Lazy<String> lazy = Lazy.of(() -> {
                counter.incrementAndGet();
                return "value";
            });
            final CountDownLatch startLatch = new CountDownLatch(1);
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
                final List<Future<String>> futures = new ArrayList<>();
                for (int i = 0; i < threadCount; i++) {
                    futures.add(executor.submit(() -> {
                        startLatch.await();
                        return lazy.get();
                    }));
                }

                startLatch.countDown();

                for (final Future<String> future : futures) {
                    future.get();
                }

                assertThat(counter.get()).isEqualTo(1);
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("concurrent get() calls all observe the same reference")
        void get_concurrent_allObserveSameReference() throws Exception {
            final int threadCount = 32;
            final Lazy<List<String>> lazy = Lazy.of(ArrayList::new);
            final CountDownLatch startLatch = new CountDownLatch(1);
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
                final List<Future<List<String>>> futures = new ArrayList<>();
                for (int i = 0; i < threadCount; i++) {
                    futures.add(executor.submit(() -> {
                        startLatch.await();
                        return lazy.get();
                    }));
                }

                startLatch.countDown();

                final List<String> reference = futures.get(0).get();
                for (final Future<List<String>> future : futures) {
                    assertThat(future.get()).isSameAs(reference);
                }
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("repeated concurrent bursts preserve exactly-once semantics")
        void get_repeatedConcurrentBursts_preserveExactlyOnce() throws Exception {
            for (int iteration = 0; iteration < 10; iteration++) {
                final int threadCount = 32;
                final AtomicInteger counter = new AtomicInteger(0);
                final Lazy<String> lazy = Lazy.of(() -> {
                    counter.incrementAndGet();
                    return "burst";
                });
                final CountDownLatch startLatch = new CountDownLatch(1);
                final ExecutorService executor =
                        Executors.newFixedThreadPool(threadCount);

                try {
                    final List<Future<String>> futures = new ArrayList<>();
                    for (int i = 0; i < threadCount; i++) {
                        futures.add(executor.submit(() -> {
                            startLatch.await();
                            return lazy.get();
                        }));
                    }

                    startLatch.countDown();

                    for (final Future<String> future : futures) {
                        assertThat(future.get()).isEqualTo("burst");
                    }

                    assertThat(counter.get()).isEqualTo(1);
                } finally {
                    executor.shutdown();
                }
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Lazy with empty string value")
        void lazy_emptyString() {
            final Lazy<String> lazy = Lazy.of(() -> "");

            assertThat(lazy.get()).isEmpty();
            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("Lazy with zero integer value")
        void lazy_zeroIntegerValue() {
            final Lazy<Integer> lazy = Lazy.of(() -> 0);

            assertThat(lazy.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Lazy with negative integer value")
        void lazy_negativeIntegerValue() {
            final Lazy<Integer> lazy = Lazy.of(() -> -42);

            assertThat(lazy.get()).isEqualTo(-42);
        }

        @Test
        @DisplayName("Lazy with Integer.MAX_VALUE")
        void lazy_maxIntegerValue() {
            final Lazy<Integer> lazy = Lazy.of(() -> Integer.MAX_VALUE);

            assertThat(lazy.get()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Lazy with Integer.MIN_VALUE")
        void lazy_minIntegerValue() {
            final Lazy<Integer> lazy = Lazy.of(() -> Integer.MIN_VALUE);

            assertThat(lazy.get()).isEqualTo(Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("Lazy with long string value")
        void lazy_longStringValue() {
            final String longString = "a".repeat(10_000);
            final Lazy<String> lazy = Lazy.of(() -> longString);

            assertThat(lazy.get()).hasSize(10_000);
            assertThat(lazy.get()).isEqualTo(longString);
        }

        @Test
        @DisplayName("nested Lazy evaluation")
        void lazy_nestedLazyEvaluation() {
            final Lazy<Lazy<String>> outer = Lazy.of(
                    () -> Lazy.of(() -> "nested")
            );

            assertThat(outer.isEvaluated()).isFalse();

            final Lazy<String> inner = outer.get();

            assertThat(outer.isEvaluated()).isTrue();
            assertThat(inner.isEvaluated()).isFalse();

            assertThat(inner.get()).isEqualTo("nested");
            assertThat(inner.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("Lazy that is never evaluated has no side effects")
        void lazy_neverEvaluated_noSideEffects() {
            final AtomicBoolean invoked = new AtomicBoolean(false);
            final Lazy<String> lazy = Lazy.of(() -> {
                invoked.set(true);
                return "never";
            });

            lazy.isEvaluated();
            lazy.toOptional();
            lazy.ifEvaluated(v -> {});
            lazy.toString();

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("multiple inspection calls before evaluation do not cause issues")
        void lazy_multipleInspectionsBeforeEvaluation() {
            final Lazy<String> lazy = Lazy.of(() -> "inspected");

            for (int i = 0; i < 100; i++) {
                assertThat(lazy.isEvaluated()).isFalse();
                assertThat(lazy.toOptional()).isEmpty();
                assertThat(lazy.toString()).isEqualTo("Lazy[?]");
            }

            assertThat(lazy.get()).isEqualTo("inspected");
            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("Lazy with boolean value")
        void lazy_booleanValue() {
            final Lazy<Boolean> lazyTrue = Lazy.of(() -> Boolean.TRUE);
            final Lazy<Boolean> lazyFalse = Lazy.of(() -> Boolean.FALSE);

            assertThat(lazyTrue.get()).isTrue();
            assertThat(lazyFalse.get()).isFalse();
        }

        @Test
        @DisplayName("toString after evaluation with empty string")
        void toString_emptyStringValue() {
            final Lazy<String> lazy = Lazy.of(() -> "");

            lazy.get();

            assertThat(lazy.toString()).isEqualTo("Lazy[]");
        }

        @Test
        @DisplayName("equals between Lazy instances with different types but equal toString")
        void equals_differentComputations_sameValue() {
            final Lazy<String> a = Lazy.of(() -> "he" + "llo");
            final Lazy<String> b = Lazy.of(() -> "hel" + "lo");

            assertThat(a).isEqualTo(b);
        }
    }

    @Nested
    @DisplayName("Chaining and Composition")
    class ChainingAndComposition {

        @Test
        @DisplayName("of -> map -> map -> get evaluates correctly")
        void mapChain_evaluatesCorrectly() {
            final Lazy<String> result = Lazy.of(() -> "hello")
                    .map(String::toUpperCase)
                    .map(s -> s + "!");

            assertThat(result.get()).isEqualTo("HELLO!");
        }

        @Test
        @DisplayName("of -> flatMap -> map -> get evaluates correctly")
        void flatMapThenMap_evaluatesCorrectly() {
            final Lazy<Integer> result = Lazy.of(() -> "hello")
                    .flatMap(s -> Lazy.of(() -> s + " world"))
                    .map(String::length);

            assertThat(result.get()).isEqualTo(11);
        }

        @Test
        @DisplayName("of -> map -> flatMap -> get evaluates correctly")
        void mapThenFlatMap_evaluatesCorrectly() {
            final Lazy<String> result = Lazy.of(() -> "hello")
                    .map(String::length)
                    .flatMap(n -> Lazy.of(() -> "length=" + n));

            assertThat(result.get()).isEqualTo("length=5");
        }

        @Test
        @DisplayName("entire chain remains deferred until terminal access")
        void chain_remainsDeferredUntilTerminalAccess() {
            final AtomicInteger baseCounter = new AtomicInteger(0);
            final AtomicInteger mapCounter = new AtomicInteger(0);
            final AtomicInteger flatMapCounter = new AtomicInteger(0);

            final Lazy<String> chain = Lazy.of(() -> {
                        baseCounter.incrementAndGet();
                        return "base";
                    })
                    .map(s -> {
                        mapCounter.incrementAndGet();
                        return s.toUpperCase();
                    })
                    .flatMap(s -> {
                        flatMapCounter.incrementAndGet();
                        return Lazy.of(() -> s + "!");
                    });

            assertThat(baseCounter.get()).isZero();
            assertThat(mapCounter.get()).isZero();
            assertThat(flatMapCounter.get()).isZero();
            assertThat(chain.isEvaluated()).isFalse();

            final String result = chain.get();

            assertThat(result).isEqualTo("BASE!");
            assertThat(baseCounter.get()).isEqualTo(1);
            assertThat(mapCounter.get()).isEqualTo(1);
            assertThat(flatMapCounter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("chain is memoized after first evaluation")
        void chain_memoizedAfterFirstEvaluation() {
            final AtomicInteger counter = new AtomicInteger(0);

            final Lazy<String> chain = Lazy.of(() -> {
                        counter.incrementAndGet();
                        return "base";
                    })
                    .map(String::toUpperCase)
                    .map(s -> s + "!");

            chain.get();
            chain.get();
            chain.get();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("inspection before and after evaluation in chain")
        void chain_inspectionBeforeAndAfterEvaluation() {
            final Lazy<String> base = Lazy.of(() -> "hello");
            final Lazy<Integer> mapped = base.map(String::length);

            assertThat(base.isEvaluated()).isFalse();
            assertThat(mapped.isEvaluated()).isFalse();
            assertThat(base.toOptional()).isEmpty();
            assertThat(mapped.toOptional()).isEmpty();
            assertThat(base.toString()).isEqualTo("Lazy[?]");
            assertThat(mapped.toString()).isEqualTo("Lazy[?]");

            final int result = mapped.get();

            assertThat(result).isEqualTo(5);
            assertThat(base.isEvaluated()).isTrue();
            assertThat(mapped.isEvaluated()).isTrue();
            assertThat(base.toOptional()).contains("hello");
            assertThat(mapped.toOptional()).contains(5);
            assertThat(base.toString()).isEqualTo("Lazy[hello]");
            assertThat(mapped.toString()).isEqualTo("Lazy[5]");
        }

        @Test
        @DisplayName("deep map chain evaluates correctly")
        void deepMapChain_evaluatesCorrectly() {
            Lazy<Integer> lazy = Lazy.of(() -> 1);

            for (int i = 0; i < 20; i++) {
                lazy = lazy.map(n -> n + 1);
            }

            assertThat(lazy.get()).isEqualTo(21);
        }

        @Test
        @DisplayName("deep flatMap chain evaluates correctly")
        void deepFlatMapChain_evaluatesCorrectly() {
            Lazy<Integer> lazy = Lazy.of(() -> 1);

            for (int i = 0; i < 20; i++) {
                lazy = lazy.flatMap(n -> Lazy.of(() -> n + 1));
            }

            assertThat(lazy.get()).isEqualTo(21);
        }

        @Test
        @DisplayName("mixed chain with ifEvaluated observation")
        void mixedChain_withIfEvaluatedObservation() {
            final AtomicReference<String> observed = new AtomicReference<>();

            final Lazy<String> lazy = Lazy.of(() -> "hello")
                    .map(String::toUpperCase);

            lazy.ifEvaluated(observed::set);
            assertThat(observed.get()).isNull();

            lazy.get();

            lazy.ifEvaluated(observed::set);
            assertThat(observed.get()).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("flatMap with identity produces equal Lazy")
        void flatMap_identity_producesEqualLazy() {
            final Lazy<String> original = Lazy.of(() -> "hello");
            final Lazy<String> identity = original.flatMap(s -> Lazy.of(() -> s));

            assertThat(identity.get()).isEqualTo(original.get());
        }

        @Test
        @DisplayName("map with identity produces equal Lazy")
        void map_identity_producesEqualLazy() {
            final Lazy<String> original = Lazy.of(() -> "hello");
            final Lazy<String> identity = original.map(s -> s);

            assertThat(identity.get()).isEqualTo(original.get());
        }
    }
}
