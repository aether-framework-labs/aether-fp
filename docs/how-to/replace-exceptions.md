# How to Replace Exceptions with Result

This guide shows how to migrate from exception-based error handling to `Result`-based error handling.

## Before: Exceptions

```java
public User findUser(String id) throws UserNotFoundException {
    User user = database.get(id);
    if (user == null) {
        throw new UserNotFoundException("No user with id: " + id);
    }
    return user;
}

// Caller must handle the exception
try {
    User user = findUser("123");
    System.out.println(user.name());
} catch (UserNotFoundException e) {
    System.out.println("Not found: " + e.getMessage());
}
```

## After: Result

```java
import de.splatgames.aether.fp.types.Result;

public Result<User, String> findUser(String id) {
    User user = database.get(id);
    if (user == null) {
        return Result.failure("No user with id: " + id);
    }
    return Result.success(user);
}

// Caller handles both cases explicitly
findUser("123").fold(
    error -> "Not found: " + error,
    user  -> user.name()
);
```

## Wrapping Existing Throwing Code

When you can't change a method that throws, wrap it:

```java
static Result<Integer, String> safeParseInt(String s) {
    try {
        return Result.success(Integer.parseInt(s));
    } catch (NumberFormatException e) {
        return Result.failure("Invalid number: " + s);
    }
}
```

## Chaining Fallible Operations

Replace nested try/catch with `flatMap`:

**Before:**
```java
try {
    User user = findUser(id);
    try {
        Order order = findOrder(user.lastOrderId());
        try {
            Receipt receipt = processRefund(order);
            return receipt.toString();
        } catch (RefundException e) {
            return "Refund failed: " + e.getMessage();
        }
    } catch (OrderNotFoundException e) {
        return "Order not found: " + e.getMessage();
    }
} catch (UserNotFoundException e) {
    return "User not found: " + e.getMessage();
}
```

**After:**
```java
String output = findUser(id)
    .flatMap(user -> findOrder(user.lastOrderId()))
    .flatMap(order -> processRefund(order))
    .fold(
        error   -> error,
        receipt -> receipt.toString()
    );
```

## Converting Back to Exceptions at Boundaries

At framework boundaries (e.g., HTTP handlers, CLI entry points), you may need to convert back to exceptions:

```java
User user = findUser(id)
    .getOrElseThrow(error -> new ResponseStatusException(404, error));
```

## Typed Error Objects

For richer error information, use a sealed error type instead of strings:

```java
sealed interface AppError permits NotFound, ValidationFailed, Timeout {
    String message();
}
record NotFound(String message) implements AppError {}
record ValidationFailed(String message) implements AppError {}
record Timeout(String message) implements AppError {}

Result<User, AppError> findUser(String id) {
    // ...
    return Result.failure(new NotFound("No user: " + id));
}
```

## Common Mistakes

| Mistake | Solution |
|---------|----------|
| Catching and re-wrapping all exceptions generically | Only wrap exceptions that represent expected failures |
| Using `Result` for programming errors (NPE, ArrayIndexOutOfBounds) | Let bugs crash — `Result` is for domain errors |
| Ignoring the error side | Always handle via `fold` or `getOrElseThrow` |

## Related

- [Working with Result](../tutorials/working-with-result.md) — Full Result tutorial
- [Chain Operations](chain-operations.md) — More flatMap patterns
- [Recover from Errors](recover-from-errors.md) — Recovery strategies
