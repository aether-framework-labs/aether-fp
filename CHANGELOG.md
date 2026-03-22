# Changelog

All notable changes to this project will be documented in this file.

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-03-08

### Initial Release

First release of Aether FP, providing core functional programming primitives for Java 21+.

### Added

#### Core Types (`aether-fp-core`)

- **`Either<L, R>`** — Right-biased algebraic sum type
  - Sealed interface with `Left` and `Right` implementations
  - Right-biased `map`, `flatMap`, and left-side `mapLeft`
  - Exhaustive `fold` for pattern matching
  - `swap`, `getOrElse`, `getOrElseGet`, `getOrElseThrow`
  - Side-effect observation via `ifLeft` and `ifRight`
  - Value-based `equals`, `hashCode`, and `toString`

- **`Result<T, E>`** — Success-biased operation outcome type
  - Sealed interface with `Success` and `Failure` implementations
  - Success-biased `map`, `flatMap`, and error-side `mapError`
  - Error recovery via `recover` and `recoverWith`
  - Exhaustive `fold` for pattern matching
  - `getOrElse`, `getOrElseGet`, `getOrElseThrow`
  - Interoperability with `Either` via `toEither()`
  - Side-effect observation via `ifSuccess` and `ifFailure`
  - Value-based `equals`, `hashCode`, and `toString`

- **`Lazy<T>`** — Thread-safe memoized deferred computation
  - Double-checked locking for exactly-once successful evaluation
  - Lazy composition via `map` and `flatMap`
  - Non-forcing inspection via `isEvaluated`, `ifEvaluated`, `toOptional`
  - Supplier reference released after evaluation for GC
  - Value-based `equals` and `hashCode` (force evaluation)
  - Non-forcing `toString`

#### Build & Quality

- Maven multi-module project with `aether-fp-core` and `aether-fp-bom`
- Java 21+ with Maven 3.9.5+ enforced via maven-enforcer-plugin
- QA profile with JaCoCo (75%+ line coverage), SpotBugs, Checkstyle, OWASP Dependency-Check, CycloneDX SBOM
- Release profile with GPG signing and Maven Central publishing
- Google Java Style-based Checkstyle configuration (4-space indent)

#### Testing

- 325 unit tests covering all types, operations, null rejection, and edge cases
- Thread-safety tests for `Lazy` with concurrent evaluation
- Organized with JUnit 5 `@Nested` test classes and `@DisplayName`
- AssertJ fluent assertions

#### Documentation

- Comprehensive Javadoc on all public and internal APIs
- 31 markdown documentation files covering concepts, tutorials, how-to guides, and examples
- `package-info.java` with license header and overview

---

**Full Changelog:** [Initial Release](https://github.com/aether-framework-labs/aether-fp/commits/v0.1.0)
