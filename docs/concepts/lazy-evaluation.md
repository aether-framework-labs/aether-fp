# Lazy Evaluation

`Lazy<T>` is a thread-safe, memoized container for deferred computation. It wraps a `Supplier` and guarantees that the computation runs at most once successfully, caching the result permanently.

## Eager vs Lazy

In Java, most expressions are eager — they evaluate immediately:

```java
// Both compute NOW, even if you only need one
String fast = computeFast();
String slow = computeSlow(); // always runs, even if unused
```

`Lazy` defers evaluation until the value is actually needed:

```java
Lazy<String> fast = Lazy.of(() -> computeFast());
Lazy<String> slow = Lazy.of(() -> computeSlow());

// Only computeFast() runs — computeSlow() is never called
String result = fast.get();
```

## How Lazy Differs from Supplier

A `Supplier<T>` re-evaluates every time you call `get()`. A `Lazy<T>` evaluates once and caches:

| Behavior | `Supplier<T>` | `Lazy<T>` |
|----------|:---:|:---:|
| Evaluation | Every call | First call only |
| Caching | No | Yes (permanent) |
| Thread-safe | No guarantee | Yes |
| Compositional API | No | `map`, `flatMap` |
| Null return | Allowed | Prohibited |

```java
Supplier<String> supplier = () -> expensiveCall();
supplier.get(); // calls expensiveCall()
supplier.get(); // calls expensiveCall() again

Lazy<String> lazy = Lazy.of(() -> expensiveCall());
lazy.get(); // calls expensiveCall()
lazy.get(); // returns cached result — no re-computation
```

## Memoization

After the first successful call to `get()`, the computed value is cached permanently and the supplier reference is released (allowing garbage collection of any captured state):

```java
Lazy<String> lazy = Lazy.of(() -> {
    System.out.println("Computing...");
    return "result";
});

lazy.isEvaluated(); // false
lazy.get();         // prints "Computing...", returns "result"
lazy.isEvaluated(); // true
lazy.get();         // returns "result" — no print, no re-computation
```

## Lazy Composition

`map` and `flatMap` create new `Lazy` values without triggering evaluation:

```java
Lazy<String> name = Lazy.of(() -> loadUserName());
Lazy<Integer> length = name.map(String::length);
Lazy<String> greeting = name.flatMap(n -> Lazy.of(() -> "Hello, " + n));

// Nothing has been evaluated yet — all three are unevaluated
name.isEvaluated();     // false
length.isEvaluated();   // false
greeting.isEvaluated(); // false

// Evaluating length triggers evaluation of name
int len = length.get(); // loadUserName() runs once
name.isEvaluated();     // true — was evaluated as a dependency
```

## Non-Forcing Inspection

Some operations let you observe a `Lazy` without triggering evaluation:

```java
Lazy<String> lazy = Lazy.of(() -> "hello");

lazy.isEvaluated();   // false — does NOT force evaluation
lazy.toOptional();    // Optional.empty() — does NOT force evaluation
lazy.toString();      // "Lazy[?]" — does NOT force evaluation

lazy.ifEvaluated(v -> System.out.println(v)); // nothing happens — not yet evaluated

lazy.get(); // NOW it evaluates

lazy.toOptional();    // Optional.of("hello")
lazy.toString();      // "Lazy[hello]"
lazy.ifEvaluated(v -> System.out.println(v)); // prints "hello"
```

**Warning:** `equals()` and `hashCode()` **do** force evaluation, because they need the actual value to compare:

```java
Lazy<String> a = Lazy.of(() -> "hello");
Lazy<String> b = Lazy.of(() -> "hello");

a.equals(b); // true — but both a and b are now evaluated
```

## Exception Handling

If the supplier throws an exception, the `Lazy` remains unevaluated and will retry on the next call:

```java
AtomicInteger attempts = new AtomicInteger(0);
Lazy<String> flaky = Lazy.of(() -> {
    if (attempts.incrementAndGet() < 3) {
        throw new RuntimeException("Not ready yet");
    }
    return "ready";
});

flaky.get(); // throws RuntimeException — attempt 1
flaky.get(); // throws RuntimeException — attempt 2
flaky.get(); // returns "ready" — attempt 3, now cached
flaky.get(); // returns "ready" — cached, no retry
```

## When to Use Lazy

- **Expensive initialization** — database connections, config file parsing, large computations
- **Conditional computation** — values that may never be needed
- **Lazy fields** — object fields that should only initialize on first access
- **Derived values** — computed from other lazy values via `map`/`flatMap`

## Related

- [Thread Safety](thread-safety.md) — How Lazy achieves thread-safe memoization
- [Defer Expensive Computation](../how-to/defer-expensive-computation.md) — Practical Lazy patterns
- [Working with Lazy](../tutorials/working-with-lazy.md) — Hands-on Lazy tutorial
