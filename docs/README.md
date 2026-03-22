# Aether FP Documentation

Welcome to the official documentation for **Aether FP**, a functional programming primitives library for Java 21.

## What is Aether FP?

Aether FP provides immutable, thread-safe algebraic data types that bring functional programming patterns to Java. Built on Java 21 sealed interfaces, it offers type-safe composition, exhaustive pattern matching, and a strict null-free API.

### Key Features

- **Either&lt;L, R&gt;** — Right-biased sum type for modeling one of two possible values
- **Result&lt;T, E&gt;** — Success-biased outcome type with built-in error recovery
- **Lazy&lt;T&gt;** — Thread-safe, memoized deferred computation
- **Sealed Interfaces** — Exhaustive pattern matching with Java 21 `switch` expressions
- **Null Safety** — Strict null prohibition with `@NotNull` annotations throughout
- **Zero Dependencies** — Only JetBrains Annotations at compile time

---

## Quick Navigation

### Getting Started

New to Aether FP? Start here:

- [Installation](getting-started/installation.md) — Add the library to your project
- [Quick Start](getting-started/quick-start.md) — Get up and running in 5 minutes

### Core Concepts

Understand the fundamental ideas:

- [Sum Types](concepts/sum-types.md) — Algebraic sum types in Java 21
- [Biased Composition](concepts/biased-composition.md) — How map and flatMap flow through pipelines
- [Lazy Evaluation](concepts/lazy-evaluation.md) — Deferred computation and memoization
- [Null Safety](concepts/null-safety.md) — The strict null-free contract
- [Either vs Result](concepts/either-vs-result.md) — When to use which type
- [Thread Safety](concepts/thread-safety.md) — Concurrency guarantees

### Tutorials

Step-by-step learning guides:

- [Working with Either](tutorials/working-with-either.md) — Deep dive into Either from A to Z
- [Working with Result](tutorials/working-with-result.md) — Error handling with Result
- [Working with Lazy](tutorials/working-with-lazy.md) — Deferred computation patterns
- [Combining Types](tutorials/combining-types.md) — Using all three types together

### How-To Guides

Practical task-oriented recipes:

- [Replace Exceptions](how-to/replace-exceptions.md) — Migrate from try/catch to Result
- [Replace Null Checks](how-to/replace-null-checks.md) — Migrate from null to Either/Result
- [Chain Operations](how-to/chain-operations.md) — Build flatMap pipelines
- [Recover from Errors](how-to/recover-from-errors.md) — Use recover and recoverWith
- [Defer Expensive Computation](how-to/defer-expensive-computation.md) — Use Lazy for performance
- [Convert to Optional](how-to/convert-to-optional.md) — Bridge to java.util.Optional
- [Use Pattern Matching](how-to/use-pattern-matching.md) — Java 21 switch with sealed types

### Examples

Working code examples:

- [Validation Pipeline](examples/validation-pipeline-example.md) — Input validation with Either chains
- [Service Layer](examples/service-layer-example.md) — Exception-free service with Result
- [Configuration Loading](examples/configuration-loading-example.md) — Lazy + Result config loader

### Reference

- [Glossary](appendix/glossary.md) — FP terminology definitions
- [Quick Reference](appendix/quick-reference.md) — Cheat sheet for all types

---

## Quick Example

```java
import de.splatgames.aether.fp.types.Either;
import de.splatgames.aether.fp.types.Result;
import de.splatgames.aether.fp.types.Lazy;

// Either: model one of two possible values
Either<String, Integer> parsed = Either.right(42);
String message = parsed.fold(
    error -> "Failed: " + error,
    value -> "Got: " + value
);
// message = "Got: 42"

// Result: model operation outcomes with recovery
Result<Integer, String> result = Result.<Integer, String>failure("timeout")
    .recover(err -> -1);
// result = Success[-1]

// Lazy: defer expensive work until needed
Lazy<String> config = Lazy.of(() -> loadConfig());
// loadConfig() has not been called yet
String value = config.get(); // now it runs, result is cached
```

---

## Modules

| Module           | Description                              |
|------------------|------------------------------------------|
| `aether-fp-core` | Core FP types (Either, Result, Lazy)     |
| `aether-fp-bom`  | Bill of Materials for version management |

## Requirements

- **Java 21** or later
- **Maven 3.9.5+** or Gradle

## License

MIT (c) Splatgames.de Software and Contributors
