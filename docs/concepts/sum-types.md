# Sum Types

A sum type (also called a discriminated union or tagged union) is a type that holds exactly one value from a fixed set of possibilities. Aether FP's `Either` and `Result` are both sum types, implemented as Java 21 sealed interfaces.

## What Is a Sum Type?

In Java, you're already familiar with **product types** — classes and records that combine multiple values:

```java
// Product type: has a name AND an age
record Person(String name, int age) {}
```

A **sum type** is the dual: it holds one value OR another, never both:

```java
// Sum type: holds a Left OR a Right, never both
Either<String, Integer> value = Either.right(42);
```

The "sum" comes from counting possible values: if `L` has 3 possible values and `R` has 5, then `Either<L, R>` has 3 + 5 = 8 possible values. Product types multiply; sum types add.

## Sum Types in Java 21

Before Java 17, there was no clean way to express sum types. You'd use inheritance, but nothing prevented additional subclasses. Java 17+ sealed interfaces solve this:

```java
public sealed interface Either<L, R> permits Either.Left, Either.Right {
    // Exactly two implementations — guaranteed by the compiler
}
```

The `sealed` keyword means:

- **Exhaustive** — Only `Left` and `Right` can implement `Either`
- **Compiler-checked** — `switch` expressions can verify you handle all cases
- **Closed hierarchy** — No external code can add new variants

## Either as a Sum Type

`Either<L, R>` is the most general sum type: a value is either `Left<L>` or `Right<R>`.

```java
Either<String, Integer> success = Either.right(42);
Either<String, Integer> failure = Either.left("not found");

// Exhaustive handling with fold
String message = success.fold(
    left  -> "Error: " + left,
    right -> "Value: " + right
);
// message = "Value: 42"
```

By convention, `Left` represents the error/alternative case and `Right` represents the success/primary case. This is called **right-bias** — transformations like `map` and `flatMap` operate on the right value.

## Result as a Sum Type

`Result<T, E>` is a specialized sum type that makes the success/failure intent explicit:

```java
Result<Integer, String> ok = Result.success(42);
Result<Integer, String> err = Result.failure("timeout");

// Same exhaustive handling
String message = ok.fold(
    error   -> "Failed: " + error,
    success -> "Got: " + success
);
```

While `Either` uses neutral `Left`/`Right` vocabulary, `Result` uses `Success`/`Failure` — making your intent clearer when modeling operation outcomes.

## Pattern Matching with Switch

Because `Either` and `Result` are sealed, Java 21's `switch` expressions provide exhaustive type matching:

```java
Either<String, Integer> value = Either.right(42);

String output = switch (value) {
    case Either.Left<String, Integer> left ->
        left.fold(error -> "Error: " + error, r -> "");
    case Either.Right<String, Integer> right ->
        right.fold(l -> "", number -> "Value: " + number);
};
// The compiler ensures both cases are handled
```

Note: `Left` and `Right` are final classes (not records), so deconstruction patterns are not available. Use `fold` or other extraction methods to access the contained value. In practice, `fold` alone covers most use cases — `switch` is most useful for complex multi-line logic per variant.

See [Use Pattern Matching](../how-to/use-pattern-matching.md) for more details.

## Why Not Just Use Optional?

`Optional<T>` is a sum type too — it's either present or absent. But it can only tell you *whether* a value exists, not *why* it's missing:

| Type | Carries Success? | Carries Error Info? | Use Case |
|------|:---:|:---:|---|
| `Optional<T>` | Yes | No | Value may or may not exist |
| `Either<L, R>` | Yes | Yes | General-purpose disjunction |
| `Result<T, E>` | Yes | Yes | Operation outcomes |

When you need to know *what went wrong*, `Either` or `Result` gives you a typed error value instead of an empty container.

## The "Always One, Never Neither" Guarantee

Unlike nullable types or Optional, an `Either` or `Result` is never "empty":

```java
// Optional can be empty — no information about why
Optional<User> user = Optional.empty();

// Either always carries a value — Left tells you what went wrong
Either<String, User> user = Either.left("User not found");

// Result is even more explicit
Result<User, String> user = Result.failure("User not found");
```

This guarantee is enforced by the strict null policy: `null` values are never permitted, so every instance always contains meaningful data.

## Related

- [Biased Composition](biased-composition.md) — How map and flatMap work with sum types
- [Either vs Result](either-vs-result.md) — Choosing between the two sum types
- [Null Safety](null-safety.md) — The strict null-free contract
- [Use Pattern Matching](../how-to/use-pattern-matching.md) — Java 21 switch with sealed types
