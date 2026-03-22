# How to Convert to Optional

This guide shows how to bridge between Aether FP types and `java.util.Optional`.

## Either to Optional

Use `fold` to collapse into an `Optional`, discarding the left value:

```java
import java.util.Optional;
import de.splatgames.aether.fp.types.Either;

Either<String, User> result = findUser("123");

Optional<User> user = result.fold(
    left  -> Optional.empty(),
    right -> Optional.of(right)
);
```

## Result to Optional

Same approach with `fold`:

```java
import de.splatgames.aether.fp.types.Result;

Result<User, String> result = findUser("123");

Optional<User> user = result.fold(
    error   -> Optional.empty(),
    success -> Optional.of(success)
);
```

## Lazy to Optional (Non-Forcing)

`Lazy` has a built-in `toOptional()` method that does **not** trigger evaluation:

```java
import de.splatgames.aether.fp.types.Lazy;

Lazy<String> lazy = Lazy.of(() -> "hello");

lazy.toOptional(); // Optional.empty() — not yet evaluated
lazy.get();
lazy.toOptional(); // Optional.of("hello")
```

This is useful for checking whether a lazy value has been computed without forcing it.

## When to Use Optional at Boundaries

Use `Optional` when your API consumers expect it (e.g., framework integrations, Stream operations):

```java
// Internal: use Result for rich error information
Result<User, String> findUserInternal(String id) { ... }

// Public API: expose Optional for framework compatibility
public Optional<User> findUser(String id) {
    return findUserInternal(id).fold(
        error   -> Optional.empty(),
        success -> Optional.of(success)
    );
}
```

Keep `Either`/`Result` internally where error information matters; convert to `Optional` at boundaries where only presence/absence matters.

## Related

- [Null Safety](../concepts/null-safety.md) — Why Either/Result carry more information than Optional
- [Replace Null Checks](replace-null-checks.md) — Migrating from null to Either/Result
