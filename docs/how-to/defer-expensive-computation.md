# How to Defer Expensive Computation

This guide shows when and how to use `Lazy<T>` to defer initialization and cache results.

## Lazy Fields

Replace eagerly initialized fields with `Lazy` when the computation is expensive and may not always be needed:

**Before:**
```java
public class ReportGenerator {
    private final DataSource dataSource;
    private final List<Record> allRecords; // loaded eagerly — always pays the cost

    public ReportGenerator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.allRecords = dataSource.loadAll(); // expensive, even if never used
    }
}
```

**After:**
```java
import de.splatgames.aether.fp.types.Lazy;

public class ReportGenerator {
    private final Lazy<List<Record>> allRecords;

    public ReportGenerator(DataSource dataSource) {
        this.allRecords = Lazy.of(() -> dataSource.loadAll());
        // Nothing loaded yet
    }

    public List<Record> getRecords() {
        return allRecords.get(); // loads on first call, cached thereafter
    }
}
```

## Derived Lazy Values

Use `map` to build derived values that stay lazy until needed:

```java
Lazy<List<Record>> records = Lazy.of(() -> dataSource.loadAll());
Lazy<Integer> count = records.map(List::size);
Lazy<Map<String, List<Record>>> grouped = records.map(
    recs -> recs.stream().collect(Collectors.groupingBy(Record::category))
);

// All three are unevaluated until accessed
int total = count.get(); // triggers records.loadAll(), then computes size
```

## Conditional Initialization

When a value is only needed under certain conditions:

```java
public class FeatureService {
    private final Lazy<ExpensiveIndex> searchIndex;

    public FeatureService(Config config) {
        this.searchIndex = Lazy.of(() -> buildSearchIndex());
    }

    public String search(String query) {
        return searchIndex.get().query(query); // built on first search
    }
}
```

If `search()` is never called, the index is never built.

## Thread-Safe Singletons

`Lazy` is a natural fit for thread-safe lazy singletons:

```java
public class ConnectionPool {
    private static final Lazy<ConnectionPool> INSTANCE =
        Lazy.of(() -> new ConnectionPool(loadConfig()));

    public static ConnectionPool getInstance() {
        return INSTANCE.get();
    }

    private ConnectionPool(Config config) {
        // expensive initialization
    }
}
```

Multiple threads calling `getInstance()` concurrently are safe — only one will initialize the pool.

## When NOT to Use Lazy

- **Cheap computations** — the overhead of `Lazy` isn't worth it for trivial operations
- **Computations that should fail fast** — if you want to detect errors at startup, evaluate eagerly
- **Frequently changing values** — `Lazy` caches permanently; it's not a cache with expiration

## Related

- [Lazy Evaluation](../concepts/lazy-evaluation.md) — How Lazy works internally
- [Working with Lazy](../tutorials/working-with-lazy.md) — Complete Lazy tutorial
- [Thread Safety](../concepts/thread-safety.md) — Lazy's concurrency guarantees
