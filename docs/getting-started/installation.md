# Installation

This guide covers how to add Aether FP to your project using Maven or Gradle.

## Modules Overview

Aether FP provides two modules:

| Module           | Purpose                          | When to Use                           |
|------------------|----------------------------------|---------------------------------------|
| `aether-fp-core` | Core FP types                    | Always needed                         |
| `aether-fp-bom`  | Bill of Materials                | Recommended for multi-module projects |

## Maven

### Basic Setup

Add the core dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>de.splatgames.aether.fp.labs</groupId>
    <artifactId>aether-fp-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Using the BOM (Recommended)

The Bill of Materials ensures consistent versions across modules:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>de.splatgames.aether.fp.labs</groupId>
            <artifactId>aether-fp-bom</artifactId>
            <version>0.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- No version needed when using BOM -->
    <dependency>
        <groupId>de.splatgames.aether.fp.labs</groupId>
        <artifactId>aether-fp-core</artifactId>
    </dependency>
</dependencies>
```

---

## Gradle

### Groovy DSL

```groovy
dependencies {
    implementation 'de.splatgames.aether.fp.labs:aether-fp-core:0.1.0'
}
```

### Kotlin DSL

```kotlin
dependencies {
    implementation("de.splatgames.aether.fp.labs:aether-fp-core:0.1.0")
}
```

### Using the BOM

**Groovy DSL:**

```groovy
dependencies {
    implementation platform('de.splatgames.aether.fp.labs:aether-fp-bom:0.1.0')

    // No version needed
    implementation 'de.splatgames.aether.fp.labs:aether-fp-core'
}
```

**Kotlin DSL:**

```kotlin
dependencies {
    implementation(platform("de.splatgames.aether.fp.labs:aether-fp-bom:0.1.0"))

    // No version needed
    implementation("de.splatgames.aether.fp.labs:aether-fp-core")
}
```

---

## Verifying Installation

Create a simple program to verify everything works:

```java
import de.splatgames.aether.fp.types.Either;
import de.splatgames.aether.fp.types.Result;
import de.splatgames.aether.fp.types.Lazy;

public class VerifyInstallation {
    public static void main(String[] args) {
        Either<String, Integer> either = Either.right(42);
        Result<String, String> result = Result.success("hello");
        Lazy<String> lazy = Lazy.of(() -> "world");

        System.out.println(either);  // Right[42]
        System.out.println(result);  // Success[hello]
        System.out.println(lazy);    // Lazy[?]
        System.out.println(lazy.get()); // world
        System.out.println(lazy);    // Lazy[world]

        System.out.println("Aether FP installed successfully!");
    }
}
```

Expected output:

```
Right[42]
Success[hello]
Lazy[?]
world
Lazy[world]
Aether FP installed successfully!
```

---

## Transitive Dependencies

Aether FP has minimal dependencies:

| Dependency            | Scope    | Purpose                 |
|-----------------------|----------|-------------------------|
| JetBrains Annotations | Compile  | `@NotNull`, `@Nullable` |

No additional runtime dependencies are required.

---

## Next Steps

- [Quick Start](quick-start.md) — Get a working example in 5 minutes
- [Sum Types](../concepts/sum-types.md) — Understand the core concept behind Either and Result
