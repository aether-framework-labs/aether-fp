# Biased Composition

`Either` is **right-biased** and `Result` is **success-biased**. This means that `map`, `flatMap`, and other transformation methods operate on one side — the "happy path" — while the other side propagates unchanged. This is the key mechanism that makes functional error handling work.

## What "Biased" Means

When you call `map` on an `Either`, only the `Right` value is transformed. A `Left` value passes through untouched:

```java
Either<String, Integer> right = Either.right(42);
Either<String, Integer> left  = Either.left("error");

Either<String, String> mappedRight = right.map(n -> "value: " + n);
// mappedRight = Right[value: 42]  — mapper was applied

Either<String, String> mappedLeft = left.map(n -> "value: " + n);
// mappedLeft = Left[error]  — mapper was NOT called
```

The same applies to `Result` with its success bias:

```java
Result<Integer, String> ok  = Result.success(42);
Result<Integer, String> err = Result.failure("timeout");

Result<String, String> mappedOk  = ok.map(n -> "value: " + n);
// mappedOk = Success[value: 42]

Result<String, String> mappedErr = err.map(n -> "value: " + n);
// mappedErr = Failure[timeout]  — mapper was NOT called
```

## map vs flatMap

Both operate on the biased side, but they differ in what the mapper returns:

| Operation | Mapper Returns | Use When |
|-----------|----------------|----------|
| `map` | A plain value `U` | The transformation always succeeds |
| `flatMap` | An `Either<L, U>` or `Result<U, E>` | The transformation itself can fail |

**map** — transform a value that's already on the happy path:

```java
Either.right(42)
    .map(n -> n * 2)           // Right[84]
    .map(n -> "result: " + n); // Right[result: 84]
```

**flatMap** — chain operations where each step can fail:

```java
Either.<String, String>right("42")
    .flatMap(s -> parseInteger(s))   // Right[42] or Left["not a number"]
    .flatMap(n -> validateRange(n)); // Right[42] or Left["out of range"]
```

If any step produces a `Left` (or `Failure`), all subsequent steps are skipped:

```
Right["42"] → flatMap(parseInteger) → Right[42] → flatMap(validateRange) → Right[42]
Right["abc"] → flatMap(parseInteger) → Left["not a number"] → skipped → Left["not a number"]
```

## The Pipeline Pattern

This short-circuit behavior creates a natural pipeline where errors propagate automatically:

```java
Result<Order, String> processOrder(String orderId) {
    return findOrder(orderId)              // Result<Order, String>
        .flatMap(order -> validateStock(order))  // may fail
        .flatMap(order -> calculateTotal(order)) // may fail
        .map(order -> applyDiscount(order));     // always succeeds
}
```

If `findOrder` fails, `validateStock` and `calculateTotal` are never called. The failure propagates directly to the caller.

## Transforming the Other Side

To transform the left/error side without affecting the right/success side:

**Either** — use `mapLeft`:

```java
Either<String, Integer> result = Either.left("not found");
Either<Integer, Integer> coded = result.mapLeft(msg -> 404);
// coded = Left[404]
```

**Result** — use `mapError`:

```java
Result<Integer, String> result = Result.failure("timeout");
Result<Integer, AppError> typed = result.mapError(AppError::new);
// typed = Failure[AppError[timeout]]
```

## fold — Handle Both Sides

When you need to produce a single value from either side, use `fold`:

```java
Either<String, Integer> value = Either.right(42);

String output = value.fold(
    left  -> "Error: " + left,
    right -> "Value: " + right
);
// output = "Value: 42"
```

`fold` is exhaustive — you must provide handlers for both cases. This makes it impossible to forget the error path.

## Side Effects with ifLeft / ifRight / ifSuccess / ifFailure

For observation without transformation:

```java
Either.right(42)
    .ifRight(v -> log.info("Got value: {}", v))
    .ifLeft(e -> log.warn("Got error: {}", e));

Result.success(42)
    .ifSuccess(v -> log.info("Succeeded: {}", v))
    .ifFailure(e -> log.warn("Failed: {}", e));
```

These methods return `this` for fluent chaining and only invoke the consumer when the matching side is present.

## Related

- [Sum Types](sum-types.md) — The foundation that biased composition builds on
- [Either vs Result](either-vs-result.md) — Choosing the right biased type
- [Chain Operations](../how-to/chain-operations.md) — Practical flatMap pipeline recipes
- [Working with Either](../tutorials/working-with-either.md) — Hands-on Either tutorial
