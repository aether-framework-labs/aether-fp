# How to Chain Operations

This guide shows how to build pipelines using `flatMap` where each step can fail independently.

## The Basic Pattern

Use `map` when a transformation always succeeds. Use `flatMap` when it can fail:

```java
// map: always succeeds
Result<String, String> upper = Result.<String, String>success("hello")
    .map(String::toUpperCase);
// Success[HELLO]

// flatMap: may fail
Result<Integer, String> parsed = Result.<String, String>success("42")
    .flatMap(s -> safeParseInt(s));
// Success[42] or Failure[...]
```

## Multi-Step Pipeline

Chain multiple fallible operations into a single pipeline:

```java
Result<Receipt, String> processOrder(String rawInput) {
    return parseOrderId(rawInput)          // Result<String, String>
        .flatMap(id -> findOrder(id))      // Result<Order, String>
        .flatMap(order -> validateStock(order))  // Result<Order, String>
        .flatMap(order -> charge(order))    // Result<Receipt, String>
        .map(receipt -> applyFormatting(receipt)); // always succeeds
}
```

If any step fails, the rest is skipped:

```
parseOrderId("ORD-1") → Success["ORD-1"]
  → findOrder("ORD-1")  → Success[Order{...}]
    → validateStock(...)   → Failure["out of stock"]
      → charge(...)          → skipped
        → applyFormatting(...)  → skipped
= Failure["out of stock"]
```

## Either Chains

The same pattern works with `Either`:

```java
Either<String, FormData> parseForm(String name, String ageStr, String email) {
    return validateName(name)
        .flatMap(validName -> validateAge(ageStr)
            .flatMap(validAge -> validateEmail(email)
                .map(validEmail -> new FormData(validName, validAge, validEmail))
            )
        );
}
```

## Mixing map and flatMap

Use `map` for transformations that can't fail within a `flatMap` chain:

```java
findUser(id)
    .map(user -> user.email())              // extract email (can't fail)
    .flatMap(email -> sendNotification(email))  // send (can fail)
    .map(response -> response.statusCode());    // extract status (can't fail)
```

## Transforming Errors Along the Way

Use `mapError` (Result) or `mapLeft` (Either) to enrich errors as they propagate:

```java
findUser(id)
    .mapError(err -> "User lookup failed: " + err)
    .flatMap(user -> findOrder(user.lastOrderId())
        .mapError(err -> "Order lookup failed for user " + user.id() + ": " + err)
    );
```

## Common Mistakes

| Mistake | Solution |
|---------|----------|
| Using `map` when the function returns a Result/Either | Use `flatMap` — `map` would produce nested types like `Result<Result<...>, ...>` |
| Creating deeply nested flatMaps | Extract each step into a named method for readability |
| Ignoring the final result | Always terminate with `fold`, `getOrElse`, or `getOrElseThrow` |

## Related

- [Replace Exceptions](replace-exceptions.md) — Migrating from try/catch
- [Recover from Errors](recover-from-errors.md) — Handling failures mid-chain
- [Biased Composition](../concepts/biased-composition.md) — The theory behind pipelines
