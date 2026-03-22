# Thread Safety

All Aether FP types are safe to use from multiple threads without external synchronization.

## Guarantees by Type

| Type | Mechanism | Thread-Safe? |
|------|-----------|:---:|
| `Either<L, R>` | Immutable — no mutable state | Yes |
| `Result<T, E>` | Immutable — no mutable state | Yes |
| `Lazy<T>` | Volatile fields + double-checked locking | Yes |

## Either and Result

`Either` and `Result` are immutable. Once created, their internal state never changes. Every transformation method (`map`, `flatMap`, `fold`, etc.) returns a new instance. This means:

- You can share instances freely between threads
- No synchronization is needed for reads
- No synchronization is needed for transformations (they don't mutate)

```java
// Safe to share between threads — value never changes
Either<String, Integer> shared = Either.right(42);

executor.submit(() -> shared.map(n -> n * 2));  // creates new Either
executor.submit(() -> shared.fold(l -> 0, r -> r)); // reads only
// shared is still Right[42] — untouched
```

## Lazy

`Lazy<T>` is the only mutable type — it transitions from unevaluated to evaluated. This transition is thread-safe:

- The `value` and `supplier` fields are `volatile`, ensuring visibility across threads
- First evaluation is protected by `synchronized` with double-checked locking
- After evaluation, `get()` reads the volatile `value` field directly — no lock needed

```java
// Safe to share — only one thread will execute the supplier
Lazy<Connection> connection = Lazy.of(() -> createConnection());

// Multiple threads call get() concurrently
executor.submit(() -> connection.get()); // may trigger evaluation
executor.submit(() -> connection.get()); // waits or reads cache
executor.submit(() -> connection.get()); // reads cache
```

The supplier executes at most once successfully, regardless of how many threads call `get()` concurrently.

## What Thread Safety Does NOT Cover

Thread safety applies to the Aether FP types themselves. The values *inside* them follow their own rules:

```java
// The Either is thread-safe, but the mutable list inside is NOT
Either<String, List<String>> risky = Either.right(new ArrayList<>());

// This is unsafe — two threads mutating the same ArrayList
executor.submit(() -> risky.ifRight(list -> list.add("a")));
executor.submit(() -> risky.ifRight(list -> list.add("b")));
```

For full thread safety, ensure the contained values are themselves immutable or properly synchronized.

## Related

- [Lazy Evaluation](lazy-evaluation.md) — How Lazy's memoization works
- [Sum Types](sum-types.md) — The immutable foundation of Either and Result
