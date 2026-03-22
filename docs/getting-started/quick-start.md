# Quick Start

Get up and running with Aether FP in 5 minutes. This guide walks through a realistic example using all three core types.

## The Scenario

You're building a user registration system. You need to:

1. **Validate** an email address (may fail) — `Either`
2. **Register** the user (may fail) — `Result`
3. **Load** a welcome template lazily (expensive) — `Lazy`

## Prerequisites

- Java 21 or later
- Aether FP installed ([Installation](installation.md))

## Step 1: Validate with Either

`Either<L, R>` models a value that is one of two types. By convention, `Left` is the error case and `Right` is the success case.

```java
import de.splatgames.aether.fp.types.Either;

static Either<String, String> validateEmail(String input) {
    if (input != null && input.contains("@")) {
        return Either.right(input);
    }
    return Either.left("Invalid email: " + input);
}

Either<String, String> valid = validateEmail("alice@example.com");
// valid = Right[alice@example.com]

Either<String, String> invalid = validateEmail("not-an-email");
// invalid = Left[Invalid email: not-an-email]
```

## Step 2: Register with Result

`Result<T, E>` is purpose-built for operation outcomes. It adds recovery operations on top of the Either pattern.

```java
import de.splatgames.aether.fp.types.Result;

static Result<String, String> registerUser(String email) {
    if (email.equals("taken@example.com")) {
        return Result.failure("Email already registered");
    }
    return Result.success("User registered: " + email);
}

Result<String, String> registered = registerUser("alice@example.com");
// registered = Success[User registered: alice@example.com]
```

## Step 3: Defer with Lazy

`Lazy<T>` wraps an expensive computation and evaluates it only once, on first access.

```java
import de.splatgames.aether.fp.types.Lazy;

Lazy<String> welcomeTemplate = Lazy.of(() -> {
    System.out.println("Loading template...");
    return "Welcome, %s!";
});

// Nothing has been loaded yet
System.out.println(welcomeTemplate.isEvaluated()); // false
```

## Step 4: Put It All Together

```java
import de.splatgames.aether.fp.types.Either;
import de.splatgames.aether.fp.types.Result;
import de.splatgames.aether.fp.types.Lazy;

public class Registration {

    static Either<String, String> validateEmail(String input) {
        if (input != null && input.contains("@")) {
            return Either.right(input);
        }
        return Either.left("Invalid email: " + input);
    }

    static Result<String, String> registerUser(String email) {
        if (email.equals("taken@example.com")) {
            return Result.failure("Email already registered");
        }
        return Result.success(email);
    }

    public static void main(String[] args) {
        // Lazy welcome template — loaded only if registration succeeds
        Lazy<String> template = Lazy.of(() -> {
            System.out.println("Loading welcome template...");
            return "Welcome, %s!";
        });

        // Validate, then register, then greet
        String output = validateEmail("alice@example.com")
            .fold(
                error -> "Validation failed: " + error,
                email -> registerUser(email).fold(
                    error -> "Registration failed: " + error,
                    user -> String.format(template.get(), user)
                )
            );

        System.out.println(output);
    }
}
```

**Output:**

```
Loading welcome template...
Welcome, alice@example.com!
```

## What Just Happened?

1. `validateEmail` returned `Right["alice@example.com"]` — validation passed
2. `fold` on the Either extracted the email and called `registerUser`
3. `registerUser` returned `Success["alice@example.com"]` — registration succeeded
4. `template.get()` loaded the template on first access and cached it
5. The welcome message was formatted and printed

If the email had been invalid, the `Left` would have short-circuited the entire chain — no registration attempted, no template loaded.

## Key Points

- **Either** is for general "one of two" values, typically validation
- **Result** is for operation outcomes with explicit success/failure semantics
- **Lazy** defers computation until first access, then caches permanently
- All three types are **immutable**, **thread-safe**, and **null-free**

## Next Steps

- [Sum Types](../concepts/sum-types.md) — Understand the theory behind Either and Result
- [Working with Either](../tutorials/working-with-either.md) — Complete Either tutorial
- [Working with Result](../tutorials/working-with-result.md) — Complete Result tutorial
- [Working with Lazy](../tutorials/working-with-lazy.md) — Complete Lazy tutorial
