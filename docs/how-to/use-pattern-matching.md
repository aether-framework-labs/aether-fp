# How to Use Pattern Matching

Because `Either` and `Result` are sealed interfaces, Java 21's `switch` expressions provide exhaustive, compiler-checked type discrimination.

## How It Works

`Either.Left`, `Either.Right`, `Result.Success`, and `Result.Failure` are final classes (not records), so you can match their types in a `switch` but cannot use deconstruction patterns. To access the contained value, use `fold`, `getOrElse`, or other extraction methods.

## Switch Expressions with Either

```java
Either<String, Integer> value = Either.right(42);

String output = switch (value) {
    case Either.Left<String, Integer> left ->
        left.fold(error -> "Error: " + error, r -> "");
    case Either.Right<String, Integer> right ->
        right.fold(l -> "", number -> "Value: " + number);
};
// output = "Value: 42"
```

The compiler ensures both cases are handled. Omitting one causes a compile error.

## Switch Expressions with Result

```java
Result<Integer, String> result = Result.success(42);

String output = switch (result) {
    case Result.Success<Integer, String> success ->
        success.fold(e -> "", value -> "Got: " + value);
    case Result.Failure<Integer, String> failure ->
        failure.fold(error -> "Failed: " + error, v -> "");
};
```

## instanceof Pattern Matching

For conditional checks rather than exhaustive handling:

```java
if (value instanceof Either.Right<String, Integer> right) {
    // We know it's Right — fold's right branch will execute
    right.ifRight(n -> System.out.println("Got number: " + n));
}
```

## Pattern Matching vs fold

Both achieve exhaustive handling. `fold` is usually the better choice:

| Approach | Best When |
|----------|-----------|
| `fold` | You want a single expression result — **preferred for most cases** |
| `switch` | You need multi-line statements, variable declarations, or side effects per case |

**fold** — concise, direct access to both values:

```java
String msg = value.fold(
    error  -> "Error: " + error,
    number -> "Value: " + number
);
```

**switch** — useful when you need complex logic per variant:

```java
switch (value) {
    case Either.Left<String, Integer> left -> left.ifLeft(error -> {
        log.warn("Encountered error: {}", error);
        metrics.increment("errors");
    });
    case Either.Right<String, Integer> right -> right.ifRight(number -> {
        log.info("Processed value: {}", number);
        cache.put("latest", number);
    });
}
```

## When Pattern Matching Shines

Pattern matching is most valuable when combined with **other sealed types** — the `switch` on the outer type branches into the inner sealed hierarchy:

```java
sealed interface AppError permits NotFound, Forbidden, Timeout {
    String message();
}

// Use fold for the outer Result, switch for the inner error type
String output = findUser(id).fold(
    error -> switch (error) {
        case NotFound nf   -> "Not found: " + nf.message();
        case Forbidden fb  -> "Forbidden: " + fb.message();
        case Timeout to    -> "Timeout: " + to.message();
    },
    user -> "Found: " + user.name()
);
```

This combines `fold` for the Result with `switch` for the sealed error hierarchy — each tool used where it fits best.

## Related

- [Sum Types](../concepts/sum-types.md) — Why sealed interfaces enable exhaustive matching
- [Working with Either](../tutorials/working-with-either.md) — Either from A to Z
- [Working with Result](../tutorials/working-with-result.md) — Result from A to Z
