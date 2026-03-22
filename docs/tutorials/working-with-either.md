# Working with Either

This tutorial walks through every aspect of `Either<L, R>` — from creating values to composing multi-step pipelines.

## Goal

Build a user input parser that validates and transforms form data using `Either` chains. By the end, you'll be comfortable with every method on `Either`.

## Prerequisites

- Java 21
- Aether FP installed ([Installation](../getting-started/installation.md))
- Familiarity with [Sum Types](../concepts/sum-types.md)

## Step 1: Creating Either Values

Use the static factory methods `left()` and `right()`:

```java
import de.splatgames.aether.fp.types.Either;

// Right = success / primary value
Either<String, Integer> success = Either.right(42);

// Left = error / alternative value
Either<String, Integer> failure = Either.left("invalid input");
```

Both sides must be non-null. Passing `null` throws `NullPointerException`.

## Step 2: Checking the Variant

```java
Either<String, Integer> value = Either.right(42);

value.isRight(); // true
value.isLeft();  // false
```

However, you rarely need these checks. Prefer `map`, `flatMap`, or `fold` instead — they handle both cases structurally.

## Step 3: Transforming with map

`map` applies a function to the right value. Left values pass through unchanged:

```java
Either<String, Integer> right = Either.right(42);
Either<String, Integer> left  = Either.left("error");

Either<String, String> mappedRight = right.map(n -> "Number: " + n);
// Right[Number: 42]

Either<String, String> mappedLeft = left.map(n -> "Number: " + n);
// Left[error] — mapper was never called
```

You can chain multiple `map` calls:

```java
Either<String, String> result = Either.<String, Integer>right(42)
    .map(n -> n * 2)
    .map(n -> n + 1)
    .map(n -> "Result: " + n);
// Right[Result: 85]
```

## Step 4: Chaining with flatMap

Use `flatMap` when the transformation itself can fail — i.e., it returns another `Either`:

```java
static Either<String, Integer> parseInt(String s) {
    try {
        return Either.right(Integer.parseInt(s));
    } catch (NumberFormatException e) {
        return Either.left("Not a number: " + s);
    }
}

static Either<String, Integer> validatePositive(int n) {
    return n > 0 ? Either.right(n) : Either.left("Must be positive: " + n);
}

Either<String, Integer> result = Either.<String, String>right("42")
    .flatMap(s -> parseInt(s))
    .flatMap(n -> validatePositive(n));
// Right[42]

Either<String, Integer> invalid = Either.<String, String>right("-5")
    .flatMap(s -> parseInt(s))
    .flatMap(n -> validatePositive(n));
// Left[Must be positive: -5]
```

If any step returns a `Left`, the rest of the chain is skipped.

## Step 5: Handling Both Cases with fold

`fold` is exhaustive — you must provide a handler for each side:

```java
Either<String, Integer> value = Either.right(42);

String message = value.fold(
    error  -> "Error: " + error,
    number -> "Got: " + number
);
// message = "Got: 42"
```

This is the primary way to "exit" the Either world and produce a concrete value.

## Step 6: Transforming the Left Side with mapLeft

To transform the error without touching the success:

```java
Either<String, Integer> value = Either.left("not found");

Either<Integer, Integer> coded = value.mapLeft(msg -> switch (msg) {
    case "not found" -> 404;
    case "forbidden" -> 403;
    default -> 500;
});
// Left[404]
```

This is useful for converting between error representations (e.g., string messages to error codes or typed error objects).

## Step 7: Swapping Sides

`swap()` flips Left and Right:

```java
Either<String, Integer> original = Either.right(42);
Either<Integer, String> swapped = original.swap();
// Left[42] — the right value is now on the left
```

This is useful when you need to process the "other" side with right-biased operations.

## Step 8: Side Effects with ifLeft and ifRight

For logging or observation without transforming:

```java
Either.right(42)
    .ifRight(v -> System.out.println("Success: " + v))
    .ifLeft(e -> System.out.println("Error: " + e));
// Prints: "Success: 42"
```

Both methods return `this`, so you can chain them fluently.

## Step 9: Extracting Values

Three ways to extract the right value at system boundaries:

```java
Either<String, Integer> value = Either.left("error");

// With a default value
int a = value.getOrElse(0);
// a = 0

// With a function-based fallback
int b = value.getOrElseGet(err -> err.length());
// b = 5

// Or throw an exception
int c = value.getOrElseThrow(err -> new IllegalStateException(err));
// throws IllegalStateException("error")
```

## Complete Example

A form field parser that validates name, age, and email:

```java
import de.splatgames.aether.fp.types.Either;

public class FormParser {

    record FormData(String name, int age, String email) {}

    static Either<String, String> validateName(String name) {
        if (name == null || name.isBlank()) {
            return Either.left("Name must not be blank");
        }
        return Either.right(name.strip());
    }

    static Either<String, Integer> validateAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            return age >= 18
                ? Either.right(age)
                : Either.left("Must be 18 or older");
        } catch (NumberFormatException e) {
            return Either.left("Invalid age: " + ageStr);
        }
    }

    static Either<String, String> validateEmail(String email) {
        if (email != null && email.contains("@")) {
            return Either.right(email.strip());
        }
        return Either.left("Invalid email: " + email);
    }

    static Either<String, FormData> parseForm(String name, String age, String email) {
        return validateName(name)
            .flatMap(validName -> validateAge(age)
                .flatMap(validAge -> validateEmail(email)
                    .map(validEmail -> new FormData(validName, validAge, validEmail))
                )
            );
    }

    public static void main(String[] args) {
        Either<String, FormData> result = parseForm("Alice", "30", "alice@example.com");

        String output = result.fold(
            error -> "Validation failed: " + error,
            form  -> "Welcome, " + form.name() + "!"
        );

        System.out.println(output);
        // Welcome, Alice!
    }
}
```

## Next Steps

- [Working with Result](working-with-result.md) — Learn the operation-outcome type
- [Chain Operations](../how-to/chain-operations.md) — More flatMap pipeline patterns
- [Use Pattern Matching](../how-to/use-pattern-matching.md) — Java 21 switch with Either
