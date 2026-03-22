# Working with Lazy

This tutorial walks through every aspect of `Lazy<T>` — from creating deferred values to composition and observation.

## Goal

Build a configuration system with deferred initialization using `Lazy`. By the end, you'll understand evaluation semantics, composition, and the non-forcing API.

## Prerequisites

- Java 21
- Aether FP installed ([Installation](../getting-started/installation.md))
- Familiarity with [Lazy Evaluation](../concepts/lazy-evaluation.md)

## Step 1: Creating Lazy Values

Wrap a `Supplier` with `Lazy.of()`:

```java
import de.splatgames.aether.fp.types.Lazy;

Lazy<String> greeting = Lazy.of(() -> {
    System.out.println("Computing greeting...");
    return "Hello, World!";
});
// Nothing printed yet — the supplier has not run
```

The supplier must not be `null` and must not return `null`.

## Step 2: Accessing the Value

Call `get()` to evaluate (or retrieve the cached result):

```java
String value = greeting.get();
// Prints: "Computing greeting..."
// value = "Hello, World!"

String again = greeting.get();
// Nothing printed — returns cached "Hello, World!"
```

The supplier runs exactly once. All subsequent calls return the cached value.

## Step 3: Checking Evaluation State

`isEvaluated()` tells you whether the value has been computed, without triggering evaluation:

```java
Lazy<String> lazy = Lazy.of(() -> "hello");

lazy.isEvaluated(); // false
lazy.get();
lazy.isEvaluated(); // true
```

## Step 4: Non-Forcing Inspection with toOptional

`toOptional()` returns the cached value as an `Optional`, or `Optional.empty()` if not yet evaluated:

```java
Lazy<String> lazy = Lazy.of(() -> "hello");

lazy.toOptional(); // Optional.empty() — does NOT trigger evaluation

lazy.get();

lazy.toOptional(); // Optional.of("hello")
```

## Step 5: Composing with map

`map` creates a new `Lazy` that transforms the value — without evaluating anything yet:

```java
Lazy<String> name = Lazy.of(() -> "Alice");
Lazy<String> upper = name.map(String::toUpperCase);
Lazy<Integer> length = name.map(String::length);

// All three are unevaluated
name.isEvaluated();   // false
upper.isEvaluated();  // false
length.isEvaluated(); // false

// Evaluating upper triggers evaluation of name
String result = upper.get(); // "ALICE"
name.isEvaluated(); // true — evaluated as a dependency
```

## Step 6: Chaining with flatMap

`flatMap` chains lazy computations where the next value depends on the previous:

```java
Lazy<String> userId = Lazy.of(() -> lookupCurrentUser());
Lazy<String> preference = userId.flatMap(
    id -> Lazy.of(() -> loadPreference(id))
);

// Neither evaluated yet
String pref = preference.get();
// lookupCurrentUser() runs, then loadPreference(id) runs
```

## Step 7: Observing with ifEvaluated

`ifEvaluated` runs a consumer only if the value is already cached — it never triggers evaluation:

```java
Lazy<String> lazy = Lazy.of(() -> "hello");

lazy.ifEvaluated(v -> System.out.println("Value: " + v));
// Nothing printed — not yet evaluated

lazy.get();

lazy.ifEvaluated(v -> System.out.println("Value: " + v));
// Prints: "Value: hello"
```

Returns `this` for fluent chaining.

## Step 8: Equality and Hashing

`equals()` and `hashCode()` **force evaluation** — they need the actual value:

```java
Lazy<Integer> a = Lazy.of(() -> 42);
Lazy<Integer> b = Lazy.of(() -> 42);

a.isEvaluated(); // false
a.equals(b);     // true — both are now evaluated
a.isEvaluated(); // true
```

`toString()` does NOT force evaluation:

```java
Lazy<Integer> lazy = Lazy.of(() -> 42);
lazy.toString(); // "Lazy[?]" — still unevaluated

lazy.get();
lazy.toString(); // "Lazy[42]"
```

## Complete Example

A configuration system that loads settings lazily:

```java
import de.splatgames.aether.fp.types.Lazy;

public class AppConfig {

    private final Lazy<String> databaseUrl;
    private final Lazy<Integer> maxConnections;
    private final Lazy<String> connectionString;

    public AppConfig() {
        this.databaseUrl = Lazy.of(() -> {
            System.out.println("Reading database URL from environment...");
            String url = System.getenv("DATABASE_URL");
            return url != null ? url : "jdbc:h2:mem:default";
        });

        this.maxConnections = Lazy.of(() -> {
            System.out.println("Reading max connections...");
            String max = System.getenv("MAX_CONNECTIONS");
            return max != null ? Integer.parseInt(max) : 10;
        });

        // Derived lazy value — computed from the above, but only when needed
        this.connectionString = databaseUrl.flatMap(
            url -> maxConnections.map(
                max -> url + "?maxPoolSize=" + max
            )
        );
    }

    public String getConnectionString() {
        return connectionString.get();
    }

    public static void main(String[] args) {
        AppConfig config = new AppConfig();

        // Nothing loaded yet
        System.out.println("Config created");

        // First access triggers both lazy evaluations
        System.out.println(config.getConnectionString());
    }
}
```

**Output:**

```
Config created
Reading database URL from environment...
Reading max connections...
jdbc:h2:mem:default?maxPoolSize=10
```

The database URL and max connections are only read when the connection string is first requested. Subsequent calls return the cached result.

## Next Steps

- [Combining Types](combining-types.md) — Use Lazy with Either and Result
- [Defer Expensive Computation](../how-to/defer-expensive-computation.md) — More Lazy patterns
- [Thread Safety](../concepts/thread-safety.md) — How Lazy handles concurrent access
