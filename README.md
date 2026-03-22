<div align="center">

<h1>Aether FP</h1>

<p><strong>Functional programming primitives for Java 21+.</strong></p>

<p>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License: MIT"></a>
  <a href="https://openjdk.org/projects/jdk/21/"><img src="https://img.shields.io/badge/Java-21%2B-orange" alt="Java 21+"></a>
  <a href="https://github.com/aether-framework-labs/aether-fp/actions/workflows/ci-pr.yml"><img src="https://github.com/aether-framework-labs/aether-fp/actions/workflows/ci-pr.yml/badge.svg" alt="CI"></a>
  <img src="https://img.shields.io/badge/coverage-75%25%2B-brightgreen" alt="Coverage">
</p>

</div>

## Overview

Aether FP provides **type-safe, immutable functional programming primitives** for Java 21 and beyond. It offers algebraic sum types and deferred computation containers that leverage modern Java features like **sealed interfaces**, **pattern matching**, and **records**.

All types are **immutable and thread-safe**, enforce a **strict null prohibition policy**, and compose naturally through `map`, `flatMap`, and `fold` operations. The library has **zero runtime dependencies** beyond JetBrains Annotations.

> **Note:** Version < 1.0.0 indicates Aether Labs / experimental status with production-grade quality standards.

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Installation](#-installation)
- [Core Types](#-core-types)
- [Modules](#-modules)
- [Documentation](#-documentation)
- [Building from Source](#-building-from-source)
- [Contributing](#-contributing)
- [Security](#-security)
- [License](#-license)

## 🚀 Quick Start

### 1. Add the dependency

```xml
<dependency>
    <groupId>de.splatgames.aether.fp.labs</groupId>
    <artifactId>aether-fp-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

<details>
<summary>Gradle (Groovy / Kotlin)</summary>

```groovy
// Groovy
implementation 'de.splatgames.aether.fp.labs:aether-fp-core:0.1.0'
```

```kotlin
// Kotlin
implementation("de.splatgames.aether.fp.labs:aether-fp-core:0.1.0")
```

</details>

### 2. Use functional types

```java
import de.splatgames.aether.fp.types.Either;
import de.splatgames.aether.fp.types.Result;
import de.splatgames.aether.fp.types.Lazy;

// Either: right-biased sum type
Either<String, Integer> parsed = Either.right("42")
    .map(Integer::parseInt);

// Result: success-biased operation outcome
Result<User, String> user = findUser(id)
    .map(User::activate)
    .recover(err -> User.guest());

// Lazy: thread-safe memoized computation
Lazy<Config> config = Lazy.of(() -> loadExpensiveConfig());
// Computed only on first access, cached forever
```

## 📦 Installation

**Maven**

```xml
<dependency>
    <groupId>de.splatgames.aether.fp.labs</groupId>
    <artifactId>aether-fp-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

<details>
<summary>Gradle (Groovy / Kotlin)</summary>

```groovy
// Groovy
implementation 'de.splatgames.aether.fp.labs:aether-fp-core:0.1.0'
```

```kotlin
// Kotlin
implementation("de.splatgames.aether.fp.labs:aether-fp-core:0.1.0")
```

</details>

### Using the BOM

The Bill of Materials ensures consistent versions across all Aether FP modules.

**Maven**

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
    <!-- No version needed -->
    <dependency>
        <groupId>de.splatgames.aether.fp.labs</groupId>
        <artifactId>aether-fp-core</artifactId>
    </dependency>
</dependencies>
```

<details>
<summary>Gradle (Groovy / Kotlin)</summary>

```groovy
// Groovy
dependencies {
    implementation platform('de.splatgames.aether.fp.labs:aether-fp-bom:0.1.0')
    implementation 'de.splatgames.aether.fp.labs:aether-fp-core'
}
```

```kotlin
// Kotlin
dependencies {
    implementation(platform("de.splatgames.aether.fp.labs:aether-fp-bom:0.1.0"))
    implementation("de.splatgames.aether.fp.labs:aether-fp-core")
}
```

</details>

## 🧩 Core Types

### `Either<L, R>` — Right-Biased Sum Type

A discriminated union representing exactly one of two possible values. By convention, left represents the error/alternative case and right represents the success/primary case.

```java
Either<String, Integer> success = Either.right(42);
Either<String, Integer> failure = Either.left("not found");

// Right-biased map
Either<String, String> mapped = success.map(n -> "value: " + n);
// mapped = Right[value: 42]

// Chain with flatMap
Either<String, Integer> parsed = Either.<String, String>right("123")
    .flatMap(s -> {
        try {
            return Either.right(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Either.left("invalid number: " + s);
        }
    });

// Exhaustive pattern matching
String result = success.fold(
    error -> "Error: " + error,
    value -> "Success: " + value
);
```

**Key operations:** `map`, `flatMap`, `mapLeft`, `fold`, `swap`, `getOrElse`, `getOrElseGet`, `getOrElseThrow`, `ifLeft`, `ifRight`

### `Result<T, E>` — Success-Biased Operation Outcome

Purpose-built for modeling operations that may succeed or fail. Provides dedicated recovery operations and converts to `Either` for interoperability.

```java
Result<Integer, String> success = Result.success(42);
Result<Integer, String> failure = Result.failure("not found");

// Success-biased composition
Result<String, String> mapped = success.map(n -> "value: " + n);

// Error recovery
Result<Integer, String> recovered = failure.recover(err -> 0);

// Convert to Either
Either<String, Integer> either = success.toEither();
```

**Key operations:** `map`, `flatMap`, `mapError`, `fold`, `recover`, `recoverWith`, `getOrElse`, `getOrElseGet`, `getOrElseThrow`, `toEither`, `ifSuccess`, `ifFailure`

### `Lazy<T>` — Thread-Safe Memoized Computation

Wraps a `Supplier` and defers evaluation until the value is first accessed. Once evaluated, the result is cached permanently. Thread-safe via double-checked locking.

```java
Lazy<String> lazy = Lazy.of(() -> expensiveComputation());
// lazy.isEvaluated() == false

String value = lazy.get(); // evaluates once
String same = lazy.get();  // returns cached value

// Compose lazily — no evaluation
Lazy<Integer> length = lazy.map(String::length);
```

**Key operations:** `get`, `map`, `flatMap`, `isEvaluated`, `ifEvaluated`, `toOptional`

## 📦 Modules

| Module | Description |
|--------|-------------|
| `aether-fp-core` | Core FP types: `Either`, `Result`, `Lazy` |
| `aether-fp-bom` | Bill of Materials for version management |

## 📖 Documentation

Comprehensive documentation is available in the [`docs/`](docs/) directory:

- 📘 **[Getting Started](docs/getting-started/)** — Installation and quick start guide
- 💡 **[Concepts](docs/concepts/)** — Sum types, null safety, biased composition, lazy evaluation, thread safety
- 🎓 **[Tutorials](docs/tutorials/)** — Working with Either, Result, Lazy, and combining types
- 🔧 **[How-To Guides](docs/how-to/)** — Replace exceptions, replace null checks, chain operations, recover from errors
- 📝 **[Examples](docs/examples/)** — Validation pipelines, service layer patterns, configuration loading
- 📚 **[Appendix](docs/appendix/)** — Glossary and quick reference

## 🔨 Building from Source

**Prerequisites:**
- Java 21+
- Maven 3.9.5+

```bash
# Build (skip tests)
mvn clean install -DskipTests

# Build with tests
mvn clean install

# QA profile (JaCoCo, SpotBugs, Checkstyle, OWASP, CycloneDX)
mvn verify -Pqa

# Integration tests
mvn verify -Pit
```

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

Please note that this project is released with a [Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.

## 🔒 Security

For security concerns, please see our [Security Policy](SECURITY.md).

**Do not report security vulnerabilities in public issues.** See the security policy for responsible disclosure instructions.

## 📜 License

**MIT** — see [LICENSE](LICENSE).

Copyright (c) 2026 Splatgames.de Software and Contributors
