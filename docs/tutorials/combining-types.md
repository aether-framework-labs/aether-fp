# Combining Types

This tutorial shows how `Either`, `Result`, and `Lazy` work together in a real application. Each type has a clear role, and combining them gives you a powerful toolkit for building robust, efficient code.

## Goal

Build a resource loader that:

1. **Lazily** loads a configuration file (expensive I/O)
2. **Parses** the content with `Result` (may fail)
3. **Validates** the parsed values with `Either` (may fail)
4. Caches everything so subsequent accesses are instant

## Prerequisites

- Completed the individual tutorials for [Either](working-with-either.md), [Result](working-with-result.md), and [Lazy](working-with-lazy.md)

## The Pattern

Each type handles a different concern:

| Type | Role |
|------|------|
| `Lazy<T>` | Defer expensive work, cache results |
| `Result<T, E>` | Model operations that may fail |
| `Either<L, R>` | Model values that could be one of two types |

## Step 1: Define the Data

```java
import de.splatgames.aether.fp.types.Either;
import de.splatgames.aether.fp.types.Result;
import de.splatgames.aether.fp.types.Lazy;

record DatabaseConfig(String host, int port, String database) {}
```

## Step 2: Wrap Fallible I/O in Result

File reading can fail, so we return a `Result`:

```java
static Result<String, String> readFile(String path) {
    try {
        return Result.success(java.nio.file.Files.readString(java.nio.file.Path.of(path)));
    } catch (Exception e) {
        return Result.failure("Failed to read " + path + ": " + e.getMessage());
    }
}
```

## Step 3: Parse with Result

Parsing the file content can also fail:

```java
static Result<DatabaseConfig, String> parseConfig(String content) {
    String[] lines = content.split("\n");
    if (lines.length < 3) {
        return Result.failure("Expected at least 3 lines in config");
    }
    try {
        String host = lines[0].split("=")[1].strip();
        int port = Integer.parseInt(lines[1].split("=")[1].strip());
        String database = lines[2].split("=")[1].strip();
        return Result.success(new DatabaseConfig(host, port, database));
    } catch (Exception e) {
        return Result.failure("Parse error: " + e.getMessage());
    }
}
```

## Step 4: Validate with Either

Validation is a good fit for `Either` — it's about checking constraints, not modeling operation success/failure:

```java
static Either<String, DatabaseConfig> validateConfig(DatabaseConfig config) {
    if (config.port() < 1 || config.port() > 65535) {
        return Either.left("Invalid port: " + config.port());
    }
    if (config.host().isBlank()) {
        return Either.left("Host must not be blank");
    }
    if (config.database().isBlank()) {
        return Either.left("Database name must not be blank");
    }
    return Either.right(config);
}
```

## Step 5: Combine with Lazy

The full pipeline is wrapped in a `Lazy` so it only runs once:

```java
public class ResourceLoader {

    private final Lazy<Either<String, DatabaseConfig>> config;

    public ResourceLoader(String configPath) {
        this.config = Lazy.of(() ->
            readFile(configPath)
                .flatMap(content -> parseConfig(content))
                .toEither()
                .flatMap(cfg -> validateConfig(cfg))
        );
    }

    public Either<String, DatabaseConfig> getConfig() {
        return config.get();
    }
}
```

Here's how the types flow:

```
readFile(path)           → Result<String, String>
  .flatMap(parseConfig)  → Result<DatabaseConfig, String>
  .toEither()            → Either<String, DatabaseConfig>
  .flatMap(validateConfig) → Either<String, DatabaseConfig>
```

The `toEither()` bridge converts from `Result` to `Either` so we can chain with `validateConfig` which returns an `Either`.

## Step 6: Use It

```java
public static void main(String[] args) {
    ResourceLoader loader = new ResourceLoader("config.properties");

    // First access — reads file, parses, validates, caches
    Either<String, DatabaseConfig> result = loader.getConfig();

    String output = result.fold(
        error  -> "Configuration error: " + error,
        config -> String.format("Connecting to %s:%d/%s",
                                config.host(), config.port(), config.database())
    );

    System.out.println(output);

    // Second access — returns cached result instantly
    Either<String, DatabaseConfig> cached = loader.getConfig();
}
```

## Alternative: Lazy + Result Without Either

If you don't need the validation step (or if validation also returns `Result`), you can stay entirely in `Result`:

```java
private final Lazy<Result<DatabaseConfig, String>> config;

public ResourceLoader(String configPath) {
    this.config = Lazy.of(() ->
        readFile(configPath)
            .flatMap(content -> parseConfig(content))
            .flatMap(cfg -> validatePort(cfg))
    );
}
```

Choose the type that best fits your API boundaries.

## Key Takeaways

1. **Lazy** wraps the entire pipeline — the expensive work runs once and is cached
2. **Result** models individual operations that can fail (I/O, parsing)
3. **Either** models validation decisions or general disjunctions
4. **toEither()** bridges from Result to Either when crossing API boundaries
5. Types compose naturally — `Lazy<Result<...>>` or `Lazy<Either<...>>` are both valid patterns

## Related

- [Either vs Result](../concepts/either-vs-result.md) — When to use which type
- [Configuration Loading Example](../examples/configuration-loading-example.md) — Full working example
- [Service Layer Example](../examples/service-layer-example.md) — Result in a service context
