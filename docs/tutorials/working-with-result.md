# Working with Result

This tutorial walks through every aspect of `Result<T, E>` — from creating values to error recovery and Either interop.

## Goal

Build a service that loads, validates, and transforms data with explicit error handling using `Result`. By the end, you'll understand how `Result` differs from `Either` in practice.

## Prerequisites

- Java 21
- Aether FP installed ([Installation](../getting-started/installation.md))
- Familiarity with [Either vs Result](../concepts/either-vs-result.md)

## Step 1: Creating Result Values

Use `success()` for the happy path and `failure()` for errors:

```java
import de.splatgames.aether.fp.types.Result;

Result<Integer, String> ok  = Result.success(42);
Result<Integer, String> err = Result.failure("not found");
```

Both values must be non-null.

## Step 2: Checking the Variant

```java
Result<Integer, String> ok = Result.success(42);

ok.isSuccess(); // true
ok.isFailure(); // false
```

As with `Either`, prefer `map`/`flatMap`/`fold` over explicit checks.

## Step 3: Transforming with map

`map` applies a function to the success value. Failures propagate unchanged:

```java
Result<Integer, String> ok  = Result.success(42);
Result<Integer, String> err = Result.failure("timeout");

Result<String, String> mappedOk  = ok.map(n -> "Value: " + n);
// Success[Value: 42]

Result<String, String> mappedErr = err.map(n -> "Value: " + n);
// Failure[timeout]
```

## Step 4: Chaining with flatMap

Use `flatMap` when each step can independently fail:

```java
static Result<User, String> findUser(String id) {
    // database lookup
}

static Result<User, String> validateActive(User user) {
    return user.isActive()
        ? Result.success(user)
        : Result.failure("User is inactive");
}

static Result<String, String> getEmail(User user) {
    return user.email() != null
        ? Result.success(user.email())
        : Result.failure("No email on file");
}

Result<String, String> email = findUser("alice-123")
    .flatMap(user -> validateActive(user))
    .flatMap(user -> getEmail(user));
```

If any step fails, subsequent steps are skipped and the failure propagates.

## Step 5: Handling Both Cases with fold

```java
Result<Integer, String> result = Result.success(42);

String output = result.fold(
    error   -> "Failed: " + error,
    success -> "Got: " + success
);
// output = "Got: 42"
```

Note that `fold` takes the failure handler first and the success handler second.

## Step 6: Transforming the Error with mapError

To change the error type without affecting the success:

```java
Result<Integer, String> result = Result.failure("timeout");

Result<Integer, AppError> typed = result.mapError(AppError::fromMessage);
// Failure[AppError[timeout]]
```

This is useful for converting between error representations as results flow through layers.

## Step 7: Recovering from Errors

This is where `Result` shines over `Either`. Two recovery methods are available:

### recover — convert failure to success

```java
Result<Integer, String> result = Result.failure("not found");

Result<Integer, String> recovered = result.recover(err -> 0);
// Success[0]
```

The recovery function receives the error and returns a success value. The result is always `Success`.

### recoverWith — convert failure to a new Result

```java
Result<Integer, String> result = Result.failure("primary timeout");

Result<Integer, String> recovered = result.recoverWith(
    err -> callBackupService()  // returns Result<Integer, String>
);
// Success[...] if backup succeeds, Failure[...] if backup also fails
```

Use `recoverWith` when the recovery itself can fail.

### Chaining recovery strategies

```java
Result<Config, String> config = loadFromFile()
    .recoverWith(err -> loadFromNetwork())
    .recoverWith(err -> loadFromDefaults());
```

## Step 8: Side Effects

```java
Result.success(42)
    .ifSuccess(v -> log.info("Got: {}", v))
    .ifFailure(e -> log.warn("Failed: {}", e));
```

Both return `this` for fluent chaining.

## Step 9: Extracting Values

At system boundaries, extract the success value:

```java
Result<Integer, String> result = Result.failure("error");

// With a default
int a = result.getOrElse(0);

// With a function-based fallback
int b = result.getOrElseGet(err -> err.length());

// Or throw
int c = result.getOrElseThrow(err -> new ServiceException(err));
```

## Step 10: Converting to Either

When you need interop with code that expects `Either`:

```java
Result<Integer, String> result = Result.success(42);
Either<String, Integer> either = result.toEither();
// Success → Right, Failure → Left
```

## Complete Example

An order processing service using `Result` throughout:

```java
import de.splatgames.aether.fp.types.Result;

public class OrderService {

    record Order(String id, String product, int quantity) {}
    record Receipt(String orderId, double total) {}

    static Result<Order, String> findOrder(String id) {
        if ("ORD-001".equals(id)) {
            return Result.success(new Order(id, "Widget", 3));
        }
        return Result.failure("Order not found: " + id);
    }

    static Result<Order, String> validateStock(Order order) {
        return order.quantity() <= 10
            ? Result.success(order)
            : Result.failure("Insufficient stock for " + order.product());
    }

    static Result<Receipt, String> processPayment(Order order) {
        double total = order.quantity() * 9.99;
        return Result.success(new Receipt(order.id(), total));
    }

    static Result<Receipt, String> processOrder(String orderId) {
        return findOrder(orderId)
            .flatMap(order -> validateStock(order))
            .flatMap(order -> processPayment(order));
    }

    public static void main(String[] args) {
        String output = processOrder("ORD-001").fold(
            error   -> "Order failed: " + error,
            receipt -> String.format("Order %s processed. Total: $%.2f",
                                    receipt.orderId(), receipt.total())
        );

        System.out.println(output);
        // Order ORD-001 processed. Total: $29.97
    }
}
```

## Next Steps

- [Working with Lazy](working-with-lazy.md) — Learn deferred computation
- [Recover from Errors](../how-to/recover-from-errors.md) — More recovery patterns
- [Replace Exceptions](../how-to/replace-exceptions.md) — Migrate from try/catch
