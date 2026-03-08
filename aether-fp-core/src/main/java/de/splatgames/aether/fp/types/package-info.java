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

/**
 * Core algebraic data types for functional programming in Java.
 *
 * <p>This package contains the fundamental types that form the building blocks of typed functional
 * programming. These include algebraic sum types implemented as sealed interfaces with exhaustive
 * permitted implementations, as well as deferred computation types that provide memoized lazy
 * evaluation.</p>
 *
 * <h2>Available Types</h2>
 * <ul>
 *   <li>{@link de.splatgames.aether.fp.types.Either} — a right-biased sum type representing
 *       exactly one of two possible values</li>
 *   <li>{@link de.splatgames.aether.fp.types.Result} — a success-biased sum type representing
 *       the outcome of an operation that may succeed or fail</li>
 *   <li>{@link de.splatgames.aether.fp.types.Lazy} — a thread-safe memoized container for
 *       deferred computation</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li>All types are immutable and thread-safe.</li>
 *   <li>Null values are never permitted — all factory methods and functional parameters
 *       reject {@code null} with a {@link java.lang.NullPointerException}.</li>
 *   <li>Types are designed for functional composition through {@code map}, {@code flatMap},
 *       and {@code fold} operations.</li>
 * </ul>
 *
 * @since 0.1.0
 */
package de.splatgames.aether.fp.types;
