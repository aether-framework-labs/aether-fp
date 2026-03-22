# Glossary

Definitions of functional programming terms as they apply to Aether FP.

---

**Algebraic Data Type (ADT)**
A composite type formed by combining other types. Sum types (Either, Result) and product types (records, classes) are the two fundamental forms. "Algebraic" refers to the mathematical operations — sum (or) and product (and) — used to combine types.

**Biased**
A sum type is biased when its transformation methods (map, flatMap) operate on one side by default. Either is *right-biased* (operations target the right value). Result is *success-biased* (operations target the success value). The other side propagates unchanged.

**Deferred Evaluation**
A computation strategy where expressions are not evaluated when they are bound, but only when their value is first needed. Lazy implements deferred evaluation with permanent memoization.

**Discriminated Union**
Another name for a sum type. "Discriminated" means each variant carries a tag (its type) that distinguishes it from the others. In Java 21, sealed interfaces provide the discrimination mechanism.

**Exhaustive Handling**
A guarantee that every possible variant of a sum type is handled. Achieved through `fold` (which requires both function arguments) or through `switch` expressions on sealed types (which the compiler checks for completeness).

**flatMap**
A composition operation that applies a function returning a wrapped type and "flattens" the result. For Either: `flatMap(fn)` applies `fn` to the right value, where `fn` returns an `Either`. For Lazy: `flatMap(fn)` applies `fn` to the value, where `fn` returns a `Lazy`.

**fold**
An operation that reduces a sum type to a single value by providing a handler for each variant. `either.fold(leftFn, rightFn)` calls `leftFn` if Left, `rightFn` if Right.

**Immutability**
The property of an object whose state cannot be modified after creation. All Aether FP types are immutable — every transformation returns a new instance.

**Lazy Evaluation**
See *Deferred Evaluation*. In Aether FP, `Lazy<T>` combines deferred evaluation with memoization — the computation runs at most once.

**map**
A transformation operation that applies a function to the value inside a container, producing a new container. For Either: `map(fn)` transforms the right value. For Lazy: `map(fn)` creates a new Lazy with the transformed computation.

**Memoization**
Caching the result of a computation so that subsequent accesses return the cached value without re-computing. `Lazy<T>` memoizes permanently after the first successful evaluation.

**Null Safety**
A design principle where null values are never accepted or produced. Aether FP enforces this with runtime checks (`Objects.requireNonNull`) and compile-time annotations (`@NotNull`).

**Pattern Matching**
A language feature for inspecting the structure of a value and extracting its components. Java 21 supports pattern matching in `switch` expressions and `instanceof` checks. Combined with sealed interfaces, it provides exhaustive handling.

**Right-Biased**
An Either type where `map` and `flatMap` operate on the Right value. By convention, Right represents the success or primary case, and Left represents the error or alternative case.

**Sealed Interface**
A Java 17+ language feature that restricts which classes can implement an interface. `sealed interface Either permits Left, Right` guarantees that only `Left` and `Right` exist, enabling exhaustive pattern matching.

**Short-Circuit Evaluation**
In a chain of `flatMap` calls, once a Left (or Failure) is encountered, subsequent operations are skipped and the error propagates directly to the end of the chain. This is analogous to how exceptions skip code after a throw.

**Success-Biased**
A Result type where `map` and `flatMap` operate on the Success value. Failures propagate unchanged through the pipeline.

**Sum Type**
A type that holds exactly one value from a fixed set of possibilities. `Either<L, R>` is a sum of `L` and `R`. `Result<T, E>` is a sum of `T` and `E`. In Java, implemented using sealed interfaces.

**Thread Safety**
The property of code that can be safely executed by multiple threads concurrently. Either and Result achieve this through immutability. Lazy achieves this through volatile fields and double-checked locking.
