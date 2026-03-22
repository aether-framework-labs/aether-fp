# Service Layer Example

A complete example showing an exception-free service layer using `Result` throughout. Every operation returns a `Result` instead of throwing exceptions.

## Scenario

A `UserService` that supports finding, creating, and updating users. Errors are represented as typed values, not exceptions.

## Implementation

```java
import de.splatgames.aether.fp.types.Result;

public class ServiceLayerExample {

    // --- Domain types ---

    record User(String id, String name, String email, boolean active) {}

    sealed interface UserError permits UserNotFound, DuplicateEmail, InvalidInput {
        String message();
    }
    record UserNotFound(String message) implements UserError {}
    record DuplicateEmail(String message) implements UserError {}
    record InvalidInput(String message) implements UserError {}

    // --- Repository (simulated) ---

    static final java.util.Map<String, User> USERS = new java.util.HashMap<>(java.util.Map.of(
        "u-001", new User("u-001", "Alice", "alice@example.com", true),
        "u-002", new User("u-002", "Bob", "bob@example.com", false)
    ));

    // --- Service methods ---

    static Result<User, UserError> findUser(String id) {
        User user = USERS.get(id);
        if (user == null) {
            return Result.failure(new UserNotFound("No user with id: " + id));
        }
        return Result.success(user);
    }

    static Result<User, UserError> requireActive(User user) {
        if (!user.active()) {
            return Result.failure(new InvalidInput("User " + user.id() + " is inactive"));
        }
        return Result.success(user);
    }

    static Result<User, UserError> updateEmail(User user, String newEmail) {
        boolean taken = USERS.values().stream()
            .anyMatch(u -> u.email().equals(newEmail) && !u.id().equals(user.id()));
        if (taken) {
            return Result.failure(new DuplicateEmail("Email already in use: " + newEmail));
        }
        User updated = new User(user.id(), user.name(), newEmail, user.active());
        USERS.put(updated.id(), updated);
        return Result.success(updated);
    }

    // --- Pipeline: find, validate, update ---

    static Result<User, UserError> changeEmail(String userId, String newEmail) {
        return findUser(userId)
            .flatMap(user -> requireActive(user))
            .flatMap(user -> updateEmail(user, newEmail));
    }

    // --- HTTP-style response conversion ---

    record Response(int status, String body) {}

    static Response toResponse(Result<User, UserError> result) {
        return result.fold(
            error -> switch (error) {
                case UserNotFound e  -> new Response(404, e.message());
                case DuplicateEmail e -> new Response(409, e.message());
                case InvalidInput e  -> new Response(400, e.message());
            },
            user -> new Response(200, "Updated: " + user.email())
        );
    }

    // --- Entry point ---

    public static void main(String[] args) {
        // Success case
        Response r1 = toResponse(changeEmail("u-001", "newalice@example.com"));
        System.out.println(r1.status() + ": " + r1.body());
        // 200: Updated: newalice@example.com

        // User not found
        Response r2 = toResponse(changeEmail("u-999", "x@example.com"));
        System.out.println(r2.status() + ": " + r2.body());
        // 404: No user with id: u-999

        // Inactive user
        Response r3 = toResponse(changeEmail("u-002", "newbob@example.com"));
        System.out.println(r3.status() + ": " + r3.body());
        // 400: User u-002 is inactive

        // Duplicate email
        Response r4 = toResponse(changeEmail("u-001", "bob@example.com"));
        System.out.println(r4.status() + ": " + r4.body());
        // 409: Email already in use: bob@example.com
    }
}
```

## Key Patterns Demonstrated

- **Typed errors** — a `sealed interface UserError` with pattern matching in `toResponse`
- **flatMap pipeline** — find, validate, and update chained without try/catch
- **Boundary conversion** — `toResponse` translates `Result` to HTTP-style status codes
- **No exceptions** — the entire flow is exception-free; errors are values
- **Exhaustive error handling** — the `switch` expression covers all `UserError` variants

## Related

- [Working with Result](../tutorials/working-with-result.md) — Full Result tutorial
- [Replace Exceptions](../how-to/replace-exceptions.md) — Migrating from try/catch
- [Validation Pipeline Example](validation-pipeline-example.md) — Either for validation
