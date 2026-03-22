# Null Safety

Aether FP enforces a strict null-free contract across all types. No value, no function parameter, and no function return value may ever be `null`. This eliminates an entire class of bugs and makes every instance guaranteed to carry meaningful data.

## The Contract

Every entry point into Aether FP validates against `null`:

| What | Enforcement | Violation |
|------|-------------|-----------|
| Factory method values | `Objects.requireNonNull` | `NullPointerException` |
| Function parameters (mappers, consumers) | `Objects.requireNonNull` | `NullPointerException` |
| Function return values | `Objects.requireNonNull` | `NullPointerException` |
| Supplier return values (Lazy) | `Objects.requireNonNull` | `NullPointerException` |

```java
// All of these throw NullPointerException immediately:
Either.left(null);                          // null value
Either.right(42).map(null);                 // null mapper
Either.right(42).map(n -> null);            // null mapper result
Result.success(null);                       // null value
Lazy.of(null);                              // null supplier
Lazy.of(() -> null).get();                  // null supplier result
```

## @NotNull Annotations

All public method signatures use `@NotNull` from JetBrains Annotations. This gives you IDE support (warnings, inspections) before you even run your code:

```java
// Your IDE will warn you here — the parameter is annotated @NotNull
Either<String, Integer> bad = Either.right(null); // IDE warning + runtime NPE
```

The annotations are a compile-time dependency only. They have no effect at runtime and do not add to your deployment footprint.

## Why No Nulls?

Functional composition breaks down in the presence of `null`:

```java
// Without null safety, any step could silently produce null
Either.right(someValue)
    .map(this::transform)     // might return null?
    .flatMap(this::validate)  // NPE somewhere downstream?
    .map(this::format);       // which step caused it?
```

With Aether FP's strict policy, a `NullPointerException` is thrown at the exact point where `null` is introduced — not three steps later in an unrelated method.

## Replacing Null Patterns

The types themselves are the replacement for `null`:

**Before — null return:**
```java
User findUser(String id) {
    // returns null if not found
}

User user = findUser("123");
if (user == null) {
    // handle missing user
}
```

**After — Either:**
```java
Either<String, User> findUser(String id) {
    // returns Left with reason, or Right with user
}

findUser("123").fold(
    reason -> handleMissing(reason),
    user   -> handleFound(user)
);
```

**After — Result:**
```java
Result<User, String> findUser(String id) {
    // returns Success with user, or Failure with reason
}

findUser("123").fold(
    error -> handleError(error),
    user  -> handleFound(user)
);
```

Both approaches force the caller to handle the absence case — you can't accidentally forget to check for `null`.

## Related

- [Sum Types](sum-types.md) — How Either and Result model presence vs absence
- [Either vs Result](either-vs-result.md) — Choosing the right type for your use case
- [Replace Null Checks](../how-to/replace-null-checks.md) — Step-by-step migration guide
