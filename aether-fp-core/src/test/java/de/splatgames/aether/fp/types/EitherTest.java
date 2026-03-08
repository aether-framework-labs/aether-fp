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
 * Comprehensive test suite for {@link Either}.
 *
 * <p>Tests are organized by API area using nested test classes. Each area covers
 * happy paths, edge cases, null rejection, and behavioral contracts.</p>
 *
 * @since 0.1.0
 */
@DisplayName("Either")
class EitherTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("left() creates a left-valued Either")
        void left_withValidValue_createsLeft() {
            final Either<String, Integer> either = Either.left("error");

            assertThat(either).isInstanceOf(Either.Left.class);
            assertThat(either.isLeft()).isTrue();
            assertThat(either.isRight()).isFalse();
        }

        @Test
        @DisplayName("right() creates a right-valued Either")
        void right_withValidValue_createsRight() {
            final Either<String, Integer> either = Either.right(42);

            assertThat(either).isInstanceOf(Either.Right.class);
            assertThat(either.isRight()).isTrue();
            assertThat(either.isLeft()).isFalse();
        }

        @Test
        @DisplayName("left(null) throws NullPointerException")
        void left_withNull_throwsNullPointerException() {
            assertThatThrownBy(() -> Either.left(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("right(null) throws NullPointerException")
        void right_withNull_throwsNullPointerException() {
            assertThatThrownBy(() -> Either.right(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("left() preserves the provided value")
        void left_withValue_preservesValue() {
            final Either<String, Integer> either = Either.left("preserved");

            final String result = either.fold(l -> l, r -> "wrong");
            assertThat(result).isEqualTo("preserved");
        }

        @Test
        @DisplayName("right() preserves the provided value")
        void right_withValue_preservesValue() {
            final Either<String, Integer> either = Either.right(99);

            final Integer result = either.fold(l -> -1, r -> r);
            assertThat(result).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("State Inspection")
    class StateInspection {

        @Test
        @DisplayName("isLeft() and isRight() are always opposite on Left")
        void left_stateFlags_areOpposite() {
            final Either<String, Integer> left = Either.left("x");

            assertThat(left.isLeft()).isTrue();
            assertThat(left.isRight()).isFalse();
            assertThat(left.isLeft()).isNotEqualTo(left.isRight());
        }

        @Test
        @DisplayName("isLeft() and isRight() are always opposite on Right")
        void right_stateFlags_areOpposite() {
            final Either<String, Integer> right = Either.right(1);

            assertThat(right.isRight()).isTrue();
            assertThat(right.isLeft()).isFalse();
            assertThat(right.isLeft()).isNotEqualTo(right.isRight());
        }

        @Test
        @DisplayName("state flags are stable across repeated calls")
        void stateFlags_repeatedCalls_returnConsistentResults() {
            final Either<String, Integer> left = Either.left("stable");
            final Either<String, Integer> right = Either.right(42);

            for (int i = 0; i < 5; i++) {
                assertThat(left.isLeft()).isTrue();
                assertThat(left.isRight()).isFalse();
                assertThat(right.isRight()).isTrue();
                assertThat(right.isLeft()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("map()")
    class Map {

        @Test
        @DisplayName("map on Right transforms the value")
        void map_onRight_transformsValue() {
            final Either<String, Integer> right = Either.right(10);

            final Either<String, String> result = right.map(n -> "val:" + n);

            assertThat(result.isRight()).isTrue();
            final String folded = result.fold(l -> "fail", r -> r);
            assertThat(folded).isEqualTo("val:10");
        }

        @Test
        @DisplayName("map on Left keeps the Left unchanged")
        void map_onLeft_keepsLeftUnchanged() {
            final Either<String, Integer> left = Either.left("error");

            final Either<String, String> result = left.map(n -> "val:" + n);

            assertThat(result.isLeft()).isTrue();
            final String folded = result.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("error");
        }

        @Test
        @DisplayName("map on Left does not invoke the mapper")
        void map_onLeft_doesNotInvokeMapper() {
            final Either<String, Integer> left = Either.left("error");
            final AtomicBoolean invoked = new AtomicBoolean(false);

            left.map(n -> {
                invoked.set(true);
                return "mapped";
            });

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("map on Right invokes the mapper exactly once")
        void map_onRight_invokesMapper() {
            final Either<String, Integer> right = Either.right(5);
            final AtomicBoolean invoked = new AtomicBoolean(false);

            right.map(n -> {
                invoked.set(true);
                return n * 2;
            });

            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("map with null mapper throws NullPointerException")
        void map_withNullMapper_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.map(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("map with null mapper on Left also throws NullPointerException")
        void map_withNullMapperOnLeft_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("error");

            assertThatThrownBy(() -> left.map(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("map with mapper returning null throws NullPointerException")
        void map_mapperReturningNull_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.map(n -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class FlatMap {

        @Test
        @DisplayName("flatMap on Right to another Right")
        void flatMap_onRight_toRight() {
            final Either<String, Integer> right = Either.right(10);

            final Either<String, String> result = right.flatMap(n -> Either.right("v:" + n));

            assertThat(result.isRight()).isTrue();
            final String folded = result.fold(l -> "fail", r -> r);
            assertThat(folded).isEqualTo("v:10");
        }

        @Test
        @DisplayName("flatMap on Right to a Left")
        void flatMap_onRight_toLeft() {
            final Either<String, Integer> right = Either.right(10);

            final Either<String, String> result = right.flatMap(n -> Either.left("failed at " + n));

            assertThat(result.isLeft()).isTrue();
            final String folded = result.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("failed at 10");
        }

        @Test
        @DisplayName("flatMap on Left keeps the original Left unchanged")
        void flatMap_onLeft_keepsLeftUnchanged() {
            final Either<String, Integer> left = Either.left("original error");

            final Either<String, String> result = left.flatMap(n -> Either.right("should not happen"));

            assertThat(result.isLeft()).isTrue();
            final String folded = result.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("original error");
        }

        @Test
        @DisplayName("flatMap on Left does not invoke the mapper")
        void flatMap_onLeft_doesNotInvokeMapper() {
            final Either<String, Integer> left = Either.left("error");
            final AtomicBoolean invoked = new AtomicBoolean(false);

            left.flatMap(n -> {
                invoked.set(true);
                return Either.right("mapped");
            });

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("flatMap on Right invokes the mapper")
        void flatMap_onRight_invokesMapper() {
            final Either<String, Integer> right = Either.right(5);
            final AtomicBoolean invoked = new AtomicBoolean(false);

            right.flatMap(n -> {
                invoked.set(true);
                return Either.right(n);
            });

            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("flatMap with null mapper throws NullPointerException")
        void flatMap_withNullMapper_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.flatMap(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("flatMap with null mapper on Left also throws NullPointerException")
        void flatMap_withNullMapperOnLeft_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("error");

            assertThatThrownBy(() -> left.flatMap(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("flatMap with mapper returning null throws NullPointerException")
        void flatMap_mapperReturningNull_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.flatMap(n -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("mapLeft()")
    class MapLeft {

        @Test
        @DisplayName("mapLeft on Left transforms the left value")
        void mapLeft_onLeft_transformsValue() {
            final Either<String, Integer> left = Either.left("error");

            final Either<Integer, Integer> result = left.mapLeft(String::length);

            assertThat(result.isLeft()).isTrue();
            final Integer folded = result.fold(l -> l, r -> -1);
            assertThat(folded).isEqualTo(5);
        }

        @Test
        @DisplayName("mapLeft on Right keeps the Right unchanged")
        void mapLeft_onRight_keepsRightUnchanged() {
            final Either<String, Integer> right = Either.right(42);

            final Either<Integer, Integer> result = right.mapLeft(String::length);

            assertThat(result.isRight()).isTrue();
            final Integer folded = result.fold(l -> -1, r -> r);
            assertThat(folded).isEqualTo(42);
        }

        @Test
        @DisplayName("mapLeft on Right does not invoke the mapper")
        void mapLeft_onRight_doesNotInvokeMapper() {
            final Either<String, Integer> right = Either.right(42);
            final AtomicBoolean invoked = new AtomicBoolean(false);

            right.mapLeft(l -> {
                invoked.set(true);
                return 0;
            });

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("mapLeft on Left invokes the mapper")
        void mapLeft_onLeft_invokesMapper() {
            final Either<String, Integer> left = Either.left("err");
            final AtomicBoolean invoked = new AtomicBoolean(false);

            left.mapLeft(l -> {
                invoked.set(true);
                return l.toUpperCase();
            });

            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("mapLeft with null mapper throws NullPointerException")
        void mapLeft_withNullMapper_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("error");

            assertThatThrownBy(() -> left.mapLeft(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("mapLeft with null mapper on Right also throws NullPointerException")
        void mapLeft_withNullMapperOnRight_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.mapLeft(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("mapLeft with mapper returning null throws NullPointerException")
        void mapLeft_mapperReturningNull_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("error");

            assertThatThrownBy(() -> left.mapLeft(l -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fold()")
    class Fold {

        @Test
        @DisplayName("fold on Left uses leftMapper")
        void fold_onLeft_usesLeftMapper() {
            final Either<String, Integer> left = Either.left("problem");

            final String result = left.fold(l -> "L:" + l, r -> "R:" + r);

            assertThat(result).isEqualTo("L:problem");
        }

        @Test
        @DisplayName("fold on Right uses rightMapper")
        void fold_onRight_usesRightMapper() {
            final Either<String, Integer> right = Either.right(7);

            final String result = right.fold(l -> "L:" + l, r -> "R:" + r);

            assertThat(result).isEqualTo("R:7");
        }

        @Test
        @DisplayName("fold on Left does not invoke rightMapper")
        void fold_onLeft_doesNotInvokeRightMapper() {
            final Either<String, Integer> left = Either.left("err");
            final AtomicBoolean rightInvoked = new AtomicBoolean(false);

            left.fold(l -> "left", r -> {
                rightInvoked.set(true);
                return "right";
            });

            assertThat(rightInvoked.get()).isFalse();
        }

        @Test
        @DisplayName("fold on Right does not invoke leftMapper")
        void fold_onRight_doesNotInvokeLeftMapper() {
            final Either<String, Integer> right = Either.right(1);
            final AtomicBoolean leftInvoked = new AtomicBoolean(false);

            right.fold(l -> {
                leftInvoked.set(true);
                return "left";
            }, r -> "right");

            assertThat(leftInvoked.get()).isFalse();
        }

        @Test
        @DisplayName("fold with null leftMapper throws NullPointerException")
        void fold_withNullLeftMapper_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.fold(null, r -> "ok"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fold with null rightMapper throws NullPointerException")
        void fold_withNullRightMapper_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.fold(l -> "ok", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fold with both null mappers throws NullPointerException")
        void fold_withBothNullMappers_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.fold(null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fold returns the mapper result directly")
        void fold_returnsMapperResultDirectly() {
            final Either<String, Integer> right = Either.right(100);

            final Integer result = right.fold(l -> -1, r -> r + 1);

            assertThat(result).isEqualTo(101);
        }
    }

    @Nested
    @DisplayName("swap()")
    class Swap {

        @Test
        @DisplayName("swap on Left produces a Right with the same value")
        void swap_onLeft_producesRight() {
            final Either<String, Integer> left = Either.left("hello");

            final Either<Integer, String> swapped = left.swap();

            assertThat(swapped.isRight()).isTrue();
            final String folded = swapped.fold(l -> "fail", r -> r);
            assertThat(folded).isEqualTo("hello");
        }

        @Test
        @DisplayName("swap on Right produces a Left with the same value")
        void swap_onRight_producesLeft() {
            final Either<String, Integer> right = Either.right(42);

            final Either<Integer, String> swapped = right.swap();

            assertThat(swapped.isLeft()).isTrue();
            final Integer folded = swapped.fold(l -> l, r -> -1);
            assertThat(folded).isEqualTo(42);
        }

        @Test
        @DisplayName("double swap on Left returns semantically equal value")
        void swap_doubleSwapOnLeft_returnsSemanticallyEqual() {
            final Either<String, Integer> left = Either.left("roundtrip");

            final Either<String, Integer> doubleSwapped = left.swap().swap();

            assertThat(doubleSwapped).isEqualTo(left);
            assertThat(doubleSwapped.isLeft()).isTrue();
        }

        @Test
        @DisplayName("double swap on Right returns semantically equal value")
        void swap_doubleSwapOnRight_returnsSemanticallyEqual() {
            final Either<String, Integer> right = Either.right(99);

            final Either<String, Integer> doubleSwapped = right.swap().swap();

            assertThat(doubleSwapped).isEqualTo(right);
            assertThat(doubleSwapped.isRight()).isTrue();
        }

        @Test
        @DisplayName("swap correctly flips state flags")
        void swap_flipsStateFlags() {
            final Either<String, Integer> left = Either.left("x");

            final Either<Integer, String> swapped = left.swap();

            assertThat(left.isLeft()).isTrue();
            assertThat(swapped.isRight()).isTrue();
            assertThat(swapped.isLeft()).isFalse();
        }
    }

    @Nested
    @DisplayName("Side-Effect Methods (ifLeft / ifRight)")
    class SideEffects {

        @Test
        @DisplayName("ifLeft on Left executes the consumer")
        void ifLeft_onLeft_executesConsumer() {
            final Either<String, Integer> left = Either.left("captured");
            final AtomicReference<String> captured = new AtomicReference<>();

            left.ifLeft(captured::set);

            assertThat(captured.get()).isEqualTo("captured");
        }

        @Test
        @DisplayName("ifLeft on Right does not execute the consumer")
        void ifLeft_onRight_doesNotExecuteConsumer() {
            final Either<String, Integer> right = Either.right(1);
            final AtomicBoolean invoked = new AtomicBoolean(false);

            right.ifLeft(l -> invoked.set(true));

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("ifRight on Right executes the consumer")
        void ifRight_onRight_executesConsumer() {
            final Either<String, Integer> right = Either.right(42);
            final AtomicReference<Integer> captured = new AtomicReference<>();

            right.ifRight(captured::set);

            assertThat(captured.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("ifRight on Left does not execute the consumer")
        void ifRight_onLeft_doesNotExecuteConsumer() {
            final Either<String, Integer> left = Either.left("err");
            final AtomicBoolean invoked = new AtomicBoolean(false);

            left.ifRight(r -> invoked.set(true));

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("ifLeft returns the same instance for fluent chaining")
        void ifLeft_returnsSameInstance() {
            final Either<String, Integer> left = Either.left("err");

            final Either<String, Integer> returned = left.ifLeft(l -> {});

            assertThat(returned).isSameAs(left);
        }

        @Test
        @DisplayName("ifRight returns the same instance for fluent chaining")
        void ifRight_returnsSameInstance() {
            final Either<String, Integer> right = Either.right(1);

            final Either<String, Integer> returned = right.ifRight(r -> {});

            assertThat(returned).isSameAs(right);
        }

        @Test
        @DisplayName("ifLeft on Right still returns the same instance")
        void ifLeft_onRight_returnsSameInstance() {
            final Either<String, Integer> right = Either.right(1);

            final Either<String, Integer> returned = right.ifLeft(l -> {});

            assertThat(returned).isSameAs(right);
        }

        @Test
        @DisplayName("ifRight on Left still returns the same instance")
        void ifRight_onLeft_returnsSameInstance() {
            final Either<String, Integer> left = Either.left("err");

            final Either<String, Integer> returned = left.ifRight(r -> {});

            assertThat(returned).isSameAs(left);
        }

        @Test
        @DisplayName("ifLeft with null consumer throws NullPointerException")
        void ifLeft_withNullConsumer_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.ifLeft(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ifLeft with null consumer on Right throws NullPointerException")
        void ifLeft_withNullConsumerOnRight_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.ifLeft(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ifRight with null consumer throws NullPointerException")
        void ifRight_withNullConsumer_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(1);

            assertThatThrownBy(() -> right.ifRight(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ifRight with null consumer on Left throws NullPointerException")
        void ifRight_withNullConsumerOnLeft_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.ifRight(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Extraction Methods")
    class Extraction {

        @Test
        @DisplayName("getOrElse on Right returns the right value")
        void getOrElse_onRight_returnsRightValue() {
            final Either<String, Integer> right = Either.right(42);

            assertThat(right.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("getOrElse on Left returns the fallback value")
        void getOrElse_onLeft_returnsFallback() {
            final Either<String, Integer> left = Either.left("err");

            assertThat(left.getOrElse(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("getOrElse with null fallback throws NullPointerException")
        void getOrElse_withNullFallback_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.getOrElse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElse with null fallback on Right still throws NullPointerException")
        void getOrElse_withNullFallbackOnRight_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(42);

            assertThatThrownBy(() -> right.getOrElse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseGet on Right returns the right value")
        void getOrElseGet_onRight_returnsRightValue() {
            final Either<String, Integer> right = Either.right(42);

            assertThat(right.getOrElseGet(l -> 0)).isEqualTo(42);
        }

        @Test
        @DisplayName("getOrElseGet on Left computes fallback from left value")
        void getOrElseGet_onLeft_computesFallback() {
            final Either<String, Integer> left = Either.left("err");

            final Integer result = left.getOrElseGet(String::length);
            assertThat(result).isEqualTo(3);
        }

        @Test
        @DisplayName("getOrElseGet on Right does not invoke the fallback function")
        void getOrElseGet_onRight_doesNotInvokeFallback() {
            final Either<String, Integer> right = Either.right(42);
            final AtomicBoolean invoked = new AtomicBoolean(false);

            right.getOrElseGet(l -> {
                invoked.set(true);
                return 0;
            });

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("getOrElseGet on Left invokes the fallback function")
        void getOrElseGet_onLeft_invokesFallback() {
            final Either<String, Integer> left = Either.left("err");
            final AtomicBoolean invoked = new AtomicBoolean(false);

            left.getOrElseGet(l -> {
                invoked.set(true);
                return 0;
            });

            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("getOrElseGet with null fallback function throws NullPointerException")
        void getOrElseGet_withNullFallback_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.getOrElseGet(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseGet with null fallback function on Right throws NullPointerException")
        void getOrElseGet_withNullFallbackOnRight_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(42);

            assertThatThrownBy(() -> right.getOrElseGet(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseGet with fallback returning null throws NullPointerException")
        void getOrElseGet_fallbackReturningNull_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.getOrElseGet(l -> null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseThrow on Right returns the right value")
        void getOrElseThrow_onRight_returnsRightValue() {
            final Either<String, Integer> right = Either.right(42);

            assertThat(right.getOrElseThrow(IllegalStateException::new)).isEqualTo(42);
        }

        @Test
        @DisplayName("getOrElseThrow on Left throws the mapped exception")
        void getOrElseThrow_onLeft_throwsMappedException() {
            final Either<String, Integer> left = Either.left("boom");

            assertThatThrownBy(() -> left.getOrElseThrow(IllegalStateException::new))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("boom");
        }

        @Test
        @DisplayName("getOrElseThrow on Right does not invoke the exception mapper")
        void getOrElseThrow_onRight_doesNotInvokeMapper() {
            final Either<String, Integer> right = Either.right(42);
            final AtomicBoolean invoked = new AtomicBoolean(false);

            right.getOrElseThrow(l -> {
                invoked.set(true);
                return new IllegalStateException(l);
            });

            assertThat(invoked.get()).isFalse();
        }

        @Test
        @DisplayName("getOrElseThrow on Left invokes the exception mapper")
        void getOrElseThrow_onLeft_invokesMapper() {
            final Either<String, Integer> left = Either.left("err");
            final AtomicBoolean invoked = new AtomicBoolean(false);

            assertThatThrownBy(() -> left.getOrElseThrow(l -> {
                invoked.set(true);
                return new IllegalStateException(l);
            })).isInstanceOf(IllegalStateException.class);

            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("getOrElseThrow with null exception mapper throws NullPointerException")
        void getOrElseThrow_withNullMapper_throwsNullPointerException() {
            final Either<String, Integer> left = Either.left("err");

            assertThatThrownBy(() -> left.getOrElseThrow(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrElseThrow with null exception mapper on Right throws NullPointerException")
        void getOrElseThrow_withNullMapperOnRight_throwsNullPointerException() {
            final Either<String, Integer> right = Either.right(42);

            assertThatThrownBy(() -> right.getOrElseThrow(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Object Contract (equals / hashCode / toString)")
    class ObjectContract {

        @Test
        @DisplayName("Left equals itself (reflexive)")
        void left_equals_reflexive() {
            final Either<String, Integer> left = Either.left("x");

            assertThat(left).isEqualTo(left);
        }

        @Test
        @DisplayName("Right equals itself (reflexive)")
        void right_equals_reflexive() {
            final Either<String, Integer> right = Either.right(1);

            assertThat(right).isEqualTo(right);
        }

        @Test
        @DisplayName("Two Lefts with the same value are equal (symmetric)")
        void left_equals_symmetric() {
            final Either<String, Integer> a = Either.left("hello");
            final Either<String, Integer> b = Either.left("hello");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(a);
        }

        @Test
        @DisplayName("Two Rights with the same value are equal (symmetric)")
        void right_equals_symmetric() {
            final Either<String, Integer> a = Either.right(42);
            final Either<String, Integer> b = Either.right(42);

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(a);
        }

        @Test
        @DisplayName("Three Lefts with the same value satisfy transitivity")
        void left_equals_transitive() {
            final Either<String, Integer> a = Either.left("t");
            final Either<String, Integer> b = Either.left("t");
            final Either<String, Integer> c = Either.left("t");

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(c);
            assertThat(a).isEqualTo(c);
        }

        @Test
        @DisplayName("Left with different value is not equal")
        void left_notEquals_differentValue() {
            final Either<String, Integer> a = Either.left("a");
            final Either<String, Integer> b = Either.left("b");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Right with different value is not equal")
        void right_notEquals_differentValue() {
            final Either<String, Integer> a = Either.right(1);
            final Either<String, Integer> b = Either.right(2);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Left and Right with equal inner values are not equal")
        void left_notEquals_rightWithSameInnerValue() {
            final Either<String, String> left = Either.left("same");
            final Either<String, String> right = Either.right("same");

            assertThat(left).isNotEqualTo(right);
            assertThat(right).isNotEqualTo(left);
        }

        @Test
        @DisplayName("Left is not equal to null")
        void left_notEquals_null() {
            final Either<String, Integer> left = Either.left("x");

            assertThat(left).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Right is not equal to null")
        void right_notEquals_null() {
            final Either<String, Integer> right = Either.right(1);

            assertThat(right).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Left is not equal to an arbitrary object")
        void left_notEquals_arbitraryObject() {
            final Either<String, Integer> left = Either.left("x");

            assertThat(left).isNotEqualTo("x");
        }

        @Test
        @DisplayName("Right is not equal to an arbitrary object")
        void right_notEquals_arbitraryObject() {
            final Either<String, Integer> right = Either.right(42);

            assertThat(right).isNotEqualTo(42);
        }

        @Test
        @DisplayName("Equal Lefts have the same hashCode")
        void left_hashCode_consistentForEqualObjects() {
            final Either<String, Integer> a = Either.left("hello");
            final Either<String, Integer> b = Either.left("hello");

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("Equal Rights have the same hashCode")
        void right_hashCode_consistentForEqualObjects() {
            final Either<String, Integer> a = Either.right(42);
            final Either<String, Integer> b = Either.right(42);

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("hashCode is stable across multiple calls")
        void hashCode_isStable() {
            final Either<String, Integer> left = Either.left("stable");
            final int hash = left.hashCode();

            assertThat(left.hashCode()).isEqualTo(hash);
            assertThat(left.hashCode()).isEqualTo(hash);
        }

        @Test
        @DisplayName("Left toString contains 'Left' and the value")
        void left_toString_format() {
            final Either<String, Integer> left = Either.left("error");

            assertThat(left.toString()).isEqualTo("Left[error]");
        }

        @Test
        @DisplayName("Right toString contains 'Right' and the value")
        void right_toString_format() {
            final Either<String, Integer> right = Either.right(42);

            assertThat(right.toString()).isEqualTo("Right[42]");
        }

        @Test
        @DisplayName("Left toString with integer value")
        void left_toString_withInteger() {
            final Either<Integer, String> left = Either.left(404);

            assertThat(left.toString()).isEqualTo("Left[404]");
        }

        @Test
        @DisplayName("Right toString with string value")
        void right_toString_withString() {
            final Either<Integer, String> right = Either.right("success");

            assertThat(right.toString()).isEqualTo("Right[success]");
        }
    }

    @Nested
    @DisplayName("Immutability and Stability")
    class Immutability {

        @Test
        @DisplayName("map on Left preserves the original left value")
        void map_onLeft_preservesOriginalValue() {
            final Either<String, Integer> left = Either.left("original");

            left.map(n -> n * 2);

            final String folded = left.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("original");
        }

        @Test
        @DisplayName("mapLeft on Right preserves the original right value")
        void mapLeft_onRight_preservesOriginalValue() {
            final Either<String, Integer> right = Either.right(42);

            right.mapLeft(String::toUpperCase);

            final Integer folded = right.fold(l -> -1, r -> r);
            assertThat(folded).isEqualTo(42);
        }

        @Test
        @DisplayName("map produces a new instance, original unchanged")
        void map_producesNewInstance() {
            final Either<String, Integer> right = Either.right(10);

            final Either<String, Integer> mapped = right.map(n -> n + 1);

            final Integer originalFolded = right.fold(l -> -1, r -> r);
            final Integer mappedFolded = mapped.fold(l -> -1, r -> r);
            assertThat(originalFolded).isEqualTo(10);
            assertThat(mappedFolded).isEqualTo(11);
            assertThat(mapped).isNotSameAs(right);
        }

        @Test
        @DisplayName("mapLeft produces a new instance, original unchanged")
        void mapLeft_producesNewInstance() {
            final Either<String, Integer> left = Either.left("lower");

            final Either<String, Integer> mapped = left.mapLeft(String::toUpperCase);

            final String originalFolded = left.fold(l -> l, r -> "fail");
            final String mappedFolded = mapped.fold(l -> l, r -> "fail");
            assertThat(originalFolded).isEqualTo("lower");
            assertThat(mappedFolded).isEqualTo("LOWER");
        }

        @Test
        @DisplayName("swap does not mutate the original instance")
        void swap_doesNotMutateOriginal() {
            final Either<String, Integer> right = Either.right(42);

            right.swap();

            assertThat(right.isRight()).isTrue();
            final Integer folded = right.fold(l -> -1, r -> r);
            assertThat(folded).isEqualTo(42);
        }

        @Test
        @DisplayName("ifLeft does not mutate the contained value")
        void ifLeft_doesNotMutateValue() {
            final Either<String, Integer> left = Either.left("value");

            left.ifLeft(l -> {
                // side effect only - no mutation possible on the Either itself
            });

            final String folded = left.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("value");
        }

        @Test
        @DisplayName("ifRight does not mutate the contained value")
        void ifRight_doesNotMutateValue() {
            final Either<String, Integer> right = Either.right(7);

            right.ifRight(r -> {
                // side effect only
            });

            final Integer folded = right.fold(l -> -1, r -> r);
            assertThat(folded).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Left with empty string")
        void left_emptyString() {
            final Either<String, Integer> left = Either.left("");

            assertThat(left.isLeft()).isTrue();
            final String folded = left.fold(l -> l, r -> "fail");
            assertThat(folded).isEmpty();
            assertThat(left.toString()).isEqualTo("Left[]");
        }

        @Test
        @DisplayName("Right with empty string")
        void right_emptyString() {
            final Either<Integer, String> right = Either.right("");

            assertThat(right.isRight()).isTrue();
            final String folded = right.fold(l -> "fail", r -> r);
            assertThat(folded).isEmpty();
            assertThat(right.toString()).isEqualTo("Right[]");
        }

        @Test
        @DisplayName("Right with zero value")
        void right_zeroValue() {
            final Either<String, Integer> right = Either.right(0);

            assertThat(right.isRight()).isTrue();
            assertThat(right.getOrElse(99)).isEqualTo(0);
        }

        @Test
        @DisplayName("Right with negative value")
        void right_negativeValue() {
            final Either<String, Integer> right = Either.right(-1);

            assertThat(right.isRight()).isTrue();
            assertThat(right.getOrElse(99)).isEqualTo(-1);
        }

        @Test
        @DisplayName("Left with long string")
        void left_longString() {
            final String longValue = "x".repeat(10_000);
            final Either<String, Integer> left = Either.left(longValue);

            assertThat(left.isLeft()).isTrue();
            final Integer folded = left.fold(String::length, r -> -1);
            assertThat(folded).isEqualTo(10_000);
        }

        @Test
        @DisplayName("Nested Either as a value")
        void nestedEither() {
            final Either<String, Either<String, Integer>> nested = Either.right(Either.right(42));

            assertThat(nested.isRight()).isTrue();

            final Either<String, Integer> inner = nested.fold(Either::left, r -> r);
            assertThat(inner.isRight()).isTrue();
            final Integer folded = inner.fold(l -> -1, r -> r);
            assertThat(folded).isEqualTo(42);
        }

        @Test
        @DisplayName("Nested Either with inner Left")
        void nestedEither_innerLeft() {
            final Either<String, Either<String, Integer>> nested = Either.right(Either.left("inner error"));

            final Either<String, Integer> inner = nested.fold(Either::left, r -> r);
            assertThat(inner.isLeft()).isTrue();
            final String folded = inner.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("inner error");
        }

        @Test
        @DisplayName("Repeated swap calls (4x) return to original")
        void repeatedSwap_returnsToOriginal() {
            final Either<String, Integer> original = Either.right(42);

            final Either<String, Integer> result = original.swap().swap().swap().swap();

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("map with identity function preserves equality")
        void map_identityFunction_preservesEquality() {
            final Either<String, Integer> right = Either.right(42);

            final Either<String, Integer> mapped = right.map(r -> r);

            assertThat(mapped).isEqualTo(right);
        }

        @Test
        @DisplayName("flatMap with right-wrapping function preserves equality")
        void flatMap_rightWrapping_preservesEquality() {
            final Either<String, Integer> right = Either.right(42);

            final Either<String, Integer> result = right.flatMap(Either::right);

            assertThat(result).isEqualTo(right);
        }

        @Test
        @DisplayName("Left with Integer.MAX_VALUE")
        void left_maxIntegerValue() {
            final Either<Integer, String> left = Either.left(Integer.MAX_VALUE);

            assertThat(left.isLeft()).isTrue();
            final Integer folded = left.fold(l -> l, r -> -1);
            assertThat(folded).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Right with Integer.MIN_VALUE")
        void right_minIntegerValue() {
            final Either<String, Integer> right = Either.right(Integer.MIN_VALUE);

            assertThat(right.isRight()).isTrue();
            assertThat(right.getOrElse(0)).isEqualTo(Integer.MIN_VALUE);
        }
    }

    @Nested
    @DisplayName("Chaining and Composition")
    class ChainingAndComposition {

        @Test
        @DisplayName("right -> map -> flatMap -> ifRight -> getOrElse pipeline")
        void rightPipeline_fullChain() {
            final AtomicReference<String> observed = new AtomicReference<>();

            final String result = Either.<String, Integer>right(10)
                    .map(n -> n * 2)
                    .flatMap(n -> n > 15 ? Either.right("big:" + n) : Either.left("too small"))
                    .ifRight(observed::set)
                    .getOrElse("fallback");

            assertThat(result).isEqualTo("big:20");
            assertThat(observed.get()).isEqualTo("big:20");
        }

        @Test
        @DisplayName("left propagation through multiple right-biased operations")
        void leftPropagation_throughChain() {
            final AtomicBoolean anyMapperInvoked = new AtomicBoolean(false);

            final Either<String, String> result = Either.<String, Integer>left("initial error")
                    .map(n -> {
                        anyMapperInvoked.set(true);
                        return n * 2;
                    })
                    .flatMap(n -> {
                        anyMapperInvoked.set(true);
                        return Either.right("value:" + n);
                    })
                    .map(s -> {
                        anyMapperInvoked.set(true);
                        return s.toUpperCase();
                    });

            assertThat(result.isLeft()).isTrue();
            final String folded = result.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("initial error");
            assertThat(anyMapperInvoked.get()).isFalse();
        }

        @Test
        @DisplayName("left recovery through getOrElseGet")
        void leftRecovery_throughGetOrElseGet() {
            final Integer result = Either.<String, Integer>left("not found")
                    .map(n -> n * 2)
                    .getOrElseGet(String::length);

            assertThat(result).isEqualTo(9);
        }

        @Test
        @DisplayName("left recovery through getOrElse")
        void leftRecovery_throughGetOrElse() {
            final Integer result = Either.<String, Integer>left("error")
                    .map(n -> n + 1)
                    .getOrElse(-1);

            assertThat(result).isEqualTo(-1);
        }

        @Test
        @DisplayName("right -> flatMap short-circuits to left mid-chain")
        void rightToLeft_midChain() {
            final Either<String, Integer> result = Either.<String, Integer>right(5)
                    .flatMap(n -> Either.<String, Integer>left("failed at " + n))
                    .map(n -> n * 100);

            assertThat(result.isLeft()).isTrue();
            final String folded = result.fold(l -> l, r -> "fail");
            assertThat(folded).isEqualTo("failed at 5");
        }

        @Test
        @DisplayName("mapLeft transforms error while preserving left state through chain")
        void mapLeft_transformsError_preservingLeftState() {
            final String result = Either.<String, Integer>left("raw error")
                    .map(n -> n * 2)
                    .mapLeft(String::toUpperCase)
                    .fold(l -> l, r -> "fail");

            assertThat(result).isEqualTo("RAW ERROR");
        }

        @Test
        @DisplayName("ifLeft and ifRight chained on both sides")
        void ifLeftAndIfRight_chainedOnBothSides() {
            final AtomicReference<String> leftCapture = new AtomicReference<>();
            final AtomicReference<Integer> rightCapture = new AtomicReference<>();

            Either.<String, Integer>left("err")
                    .ifLeft(leftCapture::set)
                    .ifRight(rightCapture::set);

            assertThat(leftCapture.get()).isEqualTo("err");
            assertThat(rightCapture.get()).isNull();

            leftCapture.set(null);

            Either.<String, Integer>right(42)
                    .ifLeft(leftCapture::set)
                    .ifRight(rightCapture::set);

            assertThat(leftCapture.get()).isNull();
            assertThat(rightCapture.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("complex multi-step pipeline with type changes")
        void complexPipeline_withTypeChanges() {
            final Either<Integer, String> result = Either.<String, Integer>right(42)
                    .map(n -> n + 8)
                    .flatMap(n -> n == 50
                            ? Either.right("fifty")
                            : Either.left("not fifty"))
                    .mapLeft(String::length);

            assertThat(result.isRight()).isTrue();
            final String folded = result.fold(l -> "fail:" + l, r -> r);
            assertThat(folded).isEqualTo("fifty");
        }

        @Test
        @DisplayName("swap in the middle of a pipeline")
        void swap_inPipeline() {
            // After swap: Right[42] becomes Left[42], map doesn't apply (right-biased), fold uses leftMapper
            final String result = Either.<String, Integer>right(42)
                    .swap()
                    .map(s -> s + " swapped")
                    .fold(l -> "got left: " + l, r -> "got right: " + r);

            assertThat(result).isEqualTo("got left: 42");
        }
    }
}
