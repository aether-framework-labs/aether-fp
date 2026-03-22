# How to Recover from Errors

`Result` provides two recovery methods that `Either` does not: `recover` and `recoverWith`. This guide shows when and how to use each.

## recover — Provide a Fallback Value

`recover` converts a `Failure` into a `Success` by computing a fallback value from the error:

```java
Result<Integer, String> result = Result.failure("not found");

Result<Integer, String> recovered = result.recover(err -> 0);
// Success[0]
```

If the `Result` is already a `Success`, `recover` has no effect:

```java
Result<Integer, String> ok = Result.success(42);
Result<Integer, String> same = ok.recover(err -> 0);
// Success[42] — recovery was not needed
```

## recoverWith — Try an Alternative Operation

`recoverWith` converts a `Failure` into a new `Result`. The alternative operation can itself fail:

```java
Result<Config, String> config = loadFromCache()
    .recoverWith(err -> loadFromDisk());
// If cache misses, tries disk. If disk fails too, the disk error propagates.
```

## Chaining Recovery Strategies

Build a fallback chain by chaining `recoverWith`:

```java
Result<Config, String> config = loadFromPrimary()
    .recoverWith(err -> loadFromReplica())
    .recoverWith(err -> loadDefaults());
```

Each fallback is only tried if the previous one failed.

## Selective Recovery

Recover from specific errors only:

```java
Result<User, AppError> result = findUser(id)
    .recoverWith(error -> switch (error) {
        case NotFound nf -> createDefaultUser(id);  // recover
        case Timeout t   -> Result.failure(t);       // don't recover, propagate
    });
```

## Recovery in a Pipeline

Insert recovery at any point in a `flatMap` chain:

```java
loadConfig()
    .recover(err -> Config.defaults())        // use defaults if config fails
    .flatMap(cfg -> connectToDatabase(cfg))    // continues with the recovered value
    .flatMap(conn -> runQuery(conn));
```

## Common Mistakes

| Mistake | Solution |
|---------|----------|
| Using `recover` when the fallback can also fail | Use `recoverWith` — it returns a `Result` |
| Swallowing all errors silently | Log the original error inside the recovery function |
| Recovering from programming errors | Let bugs crash — only recover from domain errors |

## Related

- [Replace Exceptions](replace-exceptions.md) — Migrating from try/catch to Result
- [Chain Operations](chain-operations.md) — Building flatMap pipelines
- [Either vs Result](../concepts/either-vs-result.md) — Why recovery is on Result, not Either
