# Validation Pipeline Example

A complete example showing input validation using `Either` chains. Each validation step returns an `Either<String, T>`, and `flatMap` short-circuits on the first failure.

## Scenario

A registration form with three fields that must all pass validation before creating an account.

| Field | Rules |
|-------|-------|
| Username | 3-20 characters, alphanumeric only |
| Email | Must contain `@` |
| Password | At least 8 characters, at least one digit |

## Implementation

```java
import de.splatgames.aether.fp.types.Either;

public class ValidationPipeline {

    record Registration(String username, String email, String password) {}

    // --- Individual validators ---

    static Either<String, String> validateUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) {
            return Either.left("Username must be 3-20 characters");
        }
        if (!username.matches("[a-zA-Z0-9]+")) {
            return Either.left("Username must be alphanumeric");
        }
        return Either.right(username);
    }

    static Either<String, String> validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            return Either.left("Invalid email address");
        }
        return Either.right(email.strip().toLowerCase());
    }

    static Either<String, String> validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return Either.left("Password must be at least 8 characters");
        }
        if (!password.matches(".*\\d.*")) {
            return Either.left("Password must contain at least one digit");
        }
        return Either.right(password);
    }

    // --- Pipeline ---

    static Either<String, Registration> validate(
            String username, String email, String password) {
        return validateUsername(username)
            .flatMap(validUser -> validateEmail(email)
                .flatMap(validEmail -> validatePassword(password)
                    .map(validPass -> new Registration(validUser, validEmail, validPass))
                )
            );
    }

    // --- Entry point ---

    public static void main(String[] args) {
        // Valid input
        Either<String, Registration> valid = validate("alice", "alice@example.com", "secret42");
        System.out.println(valid.fold(
            error -> "Error: " + error,
            reg   -> "Registered: " + reg.username() + " (" + reg.email() + ")"
        ));
        // Registered: alice (alice@example.com)

        // Invalid username
        Either<String, Registration> badUser = validate("ab", "alice@example.com", "secret42");
        System.out.println(badUser.fold(
            error -> "Error: " + error,
            reg   -> "Registered: " + reg.username()
        ));
        // Error: Username must be 3-20 characters

        // Valid username, invalid password
        Either<String, Registration> badPass = validate("alice", "alice@example.com", "short");
        System.out.println(badPass.fold(
            error -> "Error: " + error,
            reg   -> "Registered: " + reg.username()
        ));
        // Error: Password must be at least 8 characters
    }
}
```

## Key Patterns Demonstrated

- **Validators as functions** — each returns `Either<String, T>`, composable via `flatMap`
- **Short-circuit semantics** — first failure stops the pipeline
- **Data normalization** — `validateEmail` strips and lowercases while validating
- **Record construction** — the final `map` assembles validated fields into a record
- **Exhaustive output** — `fold` ensures both success and failure paths are handled

## Related

- [Working with Either](../tutorials/working-with-either.md) — Full Either tutorial
- [Chain Operations](../how-to/chain-operations.md) — More flatMap patterns
- [Service Layer Example](service-layer-example.md) — Result in a service context
