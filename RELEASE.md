# 🚀 **Aether FP v0.1.0 — Initial Release**

The first release of Aether FP, providing core functional programming primitives for Java 21+.

---

## 🎯 Highlights in v0.1.0

- ✅ **`Either<L, R>`** — Right-biased algebraic sum type with exhaustive pattern matching
- ✅ **`Result<T, E>`** — Success-biased operation outcome with error recovery
- ✅ **`Lazy<T>`** — Thread-safe memoized deferred computation
- ✅ **Zero runtime dependencies** — Only JetBrains Annotations at compile time
- ✅ **325 unit tests** — Comprehensive coverage including thread-safety tests
- ✅ **Comprehensive documentation** — 31 markdown files covering concepts, tutorials, and examples

---

## 📦 Installation

> [!TIP]
> All Aether artifacts are available on **Maven Central** — no extra repository required.

### Maven

```xml
<dependency>
  <groupId>de.splatgames.aether.fp.labs</groupId>
  <artifactId>aether-fp-core</artifactId>
  <version>0.1.0</version>
</dependency>
```

**Using the BOM**

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

### Gradle (Groovy)

```groovy
dependencies {
  implementation 'de.splatgames.aether.fp.labs:aether-fp-core:0.1.0'
  // Or with BOM:
  implementation platform('de.splatgames.aether.fp.labs:aether-fp-bom:0.1.0')
  implementation 'de.splatgames.aether.fp.labs:aether-fp-core'
}
```

### Gradle (Kotlin)

```kotlin
dependencies {
  implementation("de.splatgames.aether.fp.labs:aether-fp-core:0.1.0")
  // Or with BOM:
  implementation(platform("de.splatgames.aether.fp.labs:aether-fp-bom:0.1.0"))
  implementation("de.splatgames.aether.fp.labs:aether-fp-core")
}
```

---

## 📋 Module Overview

| Module | Description |
|--------|-------------|
| `aether-fp-core` | Core FP types: `Either`, `Result`, `Lazy` |
| `aether-fp-bom` | Bill of Materials for version management |

---

## 📝 Changelog

**New in 0.1.0**

- `Either<L, R>` — Right-biased sum type with `map`, `flatMap`, `mapLeft`, `fold`, `swap`, `getOrElse`, `getOrElseThrow`
- `Result<T, E>` — Success-biased outcome with `map`, `flatMap`, `mapError`, `fold`, `recover`, `recoverWith`, `toEither`
- `Lazy<T>` — Thread-safe memoized computation with `map`, `flatMap`, `isEvaluated`, `ifEvaluated`, `toOptional`
- 325 unit tests with JUnit 5 and AssertJ
- QA profile with JaCoCo (75%+), SpotBugs, Checkstyle, OWASP Dependency-Check, CycloneDX SBOM
- Comprehensive documentation (31 markdown files)

**Full Changelog:** [Initial Release](https://github.com/aether-framework-labs/aether-fp/commits/v0.1.0)

---

## 🗺️ Roadmap

### v1.0.0 (next)

- **Stable Release** — Production-ready with semantic versioning guarantees
- **GroupId migration** — Remove `.labs` suffix from groupId
- **Additional types** — `Option`, `Try`, `Validation` under consideration
- **Performance benchmarks** — Published benchmark suite

---

## 📜 License

**MIT** — see `LICENSE`.
