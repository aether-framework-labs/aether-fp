# Configuration Loading Example

A complete example showing `Lazy` combined with `Result` to build a configuration loader that reads from a file lazily, parses it, and caches the result.

## Scenario

An application needs database configuration. The config file should be:

- **Loaded lazily** — only read when the first database call happens
- **Parsed safely** — missing keys or invalid values produce clear errors
- **Cached permanently** — subsequent accesses return the cached result instantly

## Implementation

```java
import de.splatgames.aether.fp.types.Lazy;
import de.splatgames.aether.fp.types.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigLoadingExample {

    record DatabaseConfig(String host, int port, String name, String user) {}

    // --- File reading (may fail) ---

    static Result<String, String> readFile(String path) {
        try {
            return Result.success(Files.readString(Path.of(path)));
        } catch (IOException e) {
            return Result.failure("Cannot read " + path + ": " + e.getMessage());
        }
    }

    // --- Parsing (may fail) ---

    static Result<Map<String, String>, String> parseProperties(String content) {
        try {
            Map<String, String> props = content.lines()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .map(line -> line.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                    parts -> parts[0].strip(),
                    parts -> parts[1].strip()
                ));
            return Result.success(props);
        } catch (Exception e) {
            return Result.failure("Parse error: " + e.getMessage());
        }
    }

    static Result<DatabaseConfig, String> buildConfig(Map<String, String> props) {
        String host = props.get("db.host");
        String portStr = props.get("db.port");
        String name = props.get("db.name");
        String user = props.get("db.user");

        if (host == null || portStr == null || name == null || user == null) {
            return Result.failure("Missing required config keys (db.host, db.port, db.name, db.user)");
        }

        try {
            int port = Integer.parseInt(portStr);
            return Result.success(new DatabaseConfig(host, port, name, user));
        } catch (NumberFormatException e) {
            return Result.failure("Invalid port number: " + portStr);
        }
    }

    // --- Lazy configuration loader ---

    static class AppConfig {
        private final Lazy<Result<DatabaseConfig, String>> dbConfig;

        AppConfig(String configPath) {
            this.dbConfig = Lazy.of(() ->
                readFile(configPath)
                    .flatMap(content -> parseProperties(content))
                    .flatMap(props -> buildConfig(props))
            );
        }

        Result<DatabaseConfig, String> getDatabaseConfig() {
            return dbConfig.get();
        }
    }

    // --- Entry point ---

    public static void main(String[] args) {
        AppConfig config = new AppConfig("database.properties");
        System.out.println("Config object created — file NOT read yet");

        // First access: reads file, parses, validates, caches
        Result<DatabaseConfig, String> result = config.getDatabaseConfig();

        String output = result.fold(
            error -> "Configuration error: " + error,
            cfg   -> String.format("Connecting to %s@%s:%d/%s",
                                   cfg.user(), cfg.host(), cfg.port(), cfg.name())
        );
        System.out.println(output);

        // Second access: returns cached Result instantly
        Result<DatabaseConfig, String> cached = config.getDatabaseConfig();
        System.out.println("Second access: " + cached.fold(
            error -> "still error",
            cfg   -> "still " + cfg.host()
        ));
    }
}
```

**Example `database.properties`:**

```properties
# Database configuration
db.host=localhost
db.port=5432
db.name=myapp
db.user=admin
```

**Output:**

```
Config object created — file NOT read yet
Connecting to admin@localhost:5432/myapp
Second access: still localhost
```

## Key Patterns Demonstrated

- **Lazy&lt;Result&lt;...&gt;&gt;** — the entire fallible pipeline is deferred and cached
- **Result pipeline** — `readFile` → `parseProperties` → `buildConfig` via `flatMap`
- **Single evaluation** — the file is read exactly once, even across multiple accesses
- **Error as value** — missing keys, parse errors, and I/O errors all flow as `Result.Failure`
- **Clean API** — callers get `Result<DatabaseConfig, String>` without knowing about Lazy internals

## Related

- [Working with Lazy](../tutorials/working-with-lazy.md) — Full Lazy tutorial
- [Combining Types](../tutorials/combining-types.md) — More patterns for using types together
- [Defer Expensive Computation](../how-to/defer-expensive-computation.md) — When to use Lazy
