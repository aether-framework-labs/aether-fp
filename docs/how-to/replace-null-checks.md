# How to Replace Null Checks

This guide shows how to migrate from `null` return values and `if (x == null)` checks to `Either` or `Result`.

## Before: Null Returns

```java
User findUser(String id) {
    return database.get(id); // returns null if not found
}

User user = findUser("123");
if (user == null) {
    System.out.println("User not found");
} else {
    System.out.println("Found: " + user.name());
}
```

The problem: nothing in the type system forces callers to check for `null`. Forgetting the check leads to `NullPointerException` at runtime.

## After: Either

Use `Either` when both outcomes are equally valid (not necessarily an "error"):

```java
import de.splatgames.aether.fp.types.Either;

Either<String, User> findUser(String id) {
    User user = database.get(id);
    if (user == null) {
        return Either.left("No user with id: " + id);
    }
    return Either.right(user);
}

String output = findUser("123").fold(
    reason -> "Not found: " + reason,
    user   -> "Found: " + user.name()
);
```

## After: Result

Use `Result` when the absence represents an operation failure:

```java
import de.splatgames.aether.fp.types.Result;

Result<User, String> findUser(String id) {
    User user = database.get(id);
    if (user == null) {
        return Result.failure("No user with id: " + id);
    }
    return Result.success(user);
}

findUser("123")
    .ifSuccess(user -> System.out.println("Found: " + user.name()))
    .ifFailure(err -> System.out.println("Not found: " + err));
```

## Compared to Optional

`Optional` only tells you whether a value exists. `Either` and `Result` tell you *why* it's missing:

```java
// Optional — no information about why it's empty
Optional<User> user = Optional.ofNullable(database.get(id));

// Either — carries a reason
Either<String, User> user = findUser(id);
// Left["No user with id: 123"]

// Result — carries an error
Result<User, String> user = findUser(id);
// Failure["No user with id: 123"]
```

Use `Optional` when you genuinely don't care why the value is absent. Use `Either`/`Result` when the reason matters.

## Chaining Null-Safe Operations

Replace chains of null checks with `flatMap`:

**Before:**
```java
String city = null;
User user = findUser(id);
if (user != null) {
    Address address = user.getAddress();
    if (address != null) {
        city = address.getCity();
    }
}
return city != null ? city : "Unknown";
```

**After:**
```java
String city = findUser(id)
    .flatMap(user -> getAddress(user))
    .flatMap(addr -> getCity(addr))
    .getOrElse("Unknown");
```

## Related

- [Null Safety](../concepts/null-safety.md) — The strict null-free contract
- [Either vs Result](../concepts/either-vs-result.md) — Choosing the right type
- [Chain Operations](chain-operations.md) — Building flatMap pipelines
