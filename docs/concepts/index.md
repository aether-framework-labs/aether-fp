# Core Concepts

Aether FP is built on a small set of fundamental ideas. Understanding these concepts will help you use the library effectively and make the right design choices.

## Overview

```
                    ┌────────────────────────┐
                    │     Sum Types          │
                    │ (sealed interfaces)    │
                    └──────────┬─────────────┘
                               │
                 ┌─────────────┼─────────────┐
                 ▼             ▼             ▼
          ┌──────────┐  ┌──────────┐  ┌──────────┐
          │  Either  │  │  Result  │  │   Lazy   │
          │ <L, R>   │  │ <T, E>   │  │  <T>     │
          └──────────┘  └──────────┘  └──────────┘
           Right-biased  Success-biased  Memoized
           Left | Right  Success | Failure  Deferred
```

## Concepts

### Foundation

| Concept | Description |
|---------|-------------|
| [Sum Types](sum-types.md) | Algebraic types that hold exactly one of two values |
| [Null Safety](null-safety.md) | Strict null prohibition across all types |

### Composition

| Concept | Description |
|---------|-------------|
| [Biased Composition](biased-composition.md) | How map and flatMap flow through pipelines |
| [Lazy Evaluation](lazy-evaluation.md) | Deferred computation and memoization |

### Design Decisions

| Concept | Description |
|---------|-------------|
| [Either vs Result](either-vs-result.md) | When to use which sum type |
| [Thread Safety](thread-safety.md) | Concurrency guarantees across all types |

## Recommended Reading Order

1. [Sum Types](sum-types.md) — the foundation everything builds on
2. [Null Safety](null-safety.md) — the contract every type enforces
3. [Biased Composition](biased-composition.md) — the key to working with Either and Result
4. [Either vs Result](either-vs-result.md) — choosing the right type
5. [Lazy Evaluation](lazy-evaluation.md) — deferred computation
6. [Thread Safety](thread-safety.md) — concurrency guarantees

## Next Steps

After reading the concepts:

- [Tutorials](../tutorials/index.md) — Hands-on walkthroughs of each type
- [How-To Guides](../how-to/index.md) — Task-oriented recipes
