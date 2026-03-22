# Getting Started

Everything you need to start using Aether FP.

## Prerequisites

- **Java 21** or later
- **Maven 3.9.5+** or Gradle

## Learning Path

1. [Installation](installation.md) — Add Aether FP to your project
2. [Quick Start](quick-start.md) — Hands-on introduction in 5 minutes

## Key Types Preview

| Type | Purpose | Example |
|------|---------|---------|
| `Either<L, R>` | One of two values (right-biased) | `Either.right(42)` |
| `Result<T, E>` | Operation outcome (success-biased) | `Result.success("ok")` |
| `Lazy<T>` | Deferred, memoized computation | `Lazy.of(() -> compute())` |

## Next Steps

After completing the quick start:

- [Core Concepts](../concepts/index.md) — Understand the ideas behind the types
- [Tutorials](../tutorials/index.md) — Deep dives into each type
- [Quick Reference](../appendix/quick-reference.md) — Cheat sheet for all methods
