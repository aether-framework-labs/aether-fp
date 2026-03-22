# Either vs Result

Both `Either` and `Result` are sum types that carry a value on one side and an alternative on the other. Choosing between them depends on what you're modeling.

## The Core Difference

- **Either&lt;L, R&gt;** is a **general-purpose disjunction** — "this value is an L or an R"
- **Result&lt;T, E&gt;** is a **purpose-built outcome** — "this operation succeeded with T or failed with E"

The distinction is semantic, not structural. Under the hood they're both sealed interfaces with two variants. The difference lies in vocabulary and API surface.

## API Comparison

| Capability | Either | Result |
|------------|--------|--------|
| Factory methods | `left(value)`, `right(value)` | `success(value)`, `failure(error)` |
| Check variant | `isLeft()`, `isRight()` | `isSuccess()`, `isFailure()` |
| Map happy path | `map(fn)` | `map(fn)` |
| Chain operations | `flatMap(fn)` | `flatMap(fn)` |
| Map other side | `mapLeft(fn)` | `mapError(fn)` |
| Exhaustive handling | `fold(leftFn, rightFn)` | `fold(failureFn, successFn)` |
| Side effects | `ifLeft(c)`, `ifRight(c)` | `ifSuccess(c)`, `ifFailure(c)` |
| Extract with fallback | `getOrElse(v)` | `getOrElse(v)` |
| Extract with function | `getOrElseGet(fn)` | `getOrElseGet(fn)` |
| Extract or throw | `getOrElseThrow(fn)` | `getOrElseThrow(fn)` |
| Swap sides | `swap()` | — |
| Recover from error | — | `recover(fn)` |
| Recover with new Result | — | `recoverWith(fn)` |
| Convert to Either | — | `toEither()` |

Key differences highlighted:

- **Either** has `swap()` — because both sides are conceptually equal, swapping makes sense
- **Result** has `recover()` and `recoverWith()` — because recovering from failure is a first-class concern for operation outcomes
- **Result** has `toEither()` — to convert when you need the more general type

## Decision Guide

**Use Either when:**

- You're modeling a value that could be one of two types with no inherent success/failure meaning
- Both sides are equally "valid" (e.g., `Either<Text, Image>` for content that's text or image)
- You need `swap()` to flip perspectives
- You're building a generic utility that doesn't impose success/failure semantics

**Use Result when:**

- You're modeling an operation that can succeed or fail
- You want explicit `success`/`failure` vocabulary in your code
- You need recovery operations (`recover`, `recoverWith`)
- You're building a service or processing pipeline with clear happy/error paths

## Examples

### Either — general disjunction

```java
// A response is either cached data or fresh data
Either<CachedData, FreshData> response = fetchData(key);

// A configuration value is either an integer or a string
Either<Integer, String> configValue = parseConfig(key);
```

### Result — operation outcome

```java
// Parsing can succeed or fail
Result<Integer, ParseError> parsed = parseInput(raw);

// A network call can succeed or fail
Result<Response, NetworkError> response = httpClient.get(url);

// Recover from a failure
Result<Integer, ParseError> withDefault = parsed.recover(err -> 0);
```

### Converting between them

When you have a `Result` but need an `Either` (for example, to pass to a generic utility):

```java
Result<User, String> result = findUser(id);
Either<String, User> either = result.toEither();
// Success → Right, Failure → Left
```

## Common Mistake: Using Either for Operations

If you catch yourself writing `Either<ErrorType, SuccessType>`, consider whether `Result<SuccessType, ErrorType>` would be clearer:

```java
// Less clear — requires knowing the Left=error convention
Either<String, User> findUser(String id) { ... }

// More clear — intent is explicit in the type name
Result<User, String> findUser(String id) { ... }
```

The second version communicates intent through the type itself, not through convention.

## Related

- [Sum Types](sum-types.md) — The shared foundation of both types
- [Biased Composition](biased-composition.md) — How map and flatMap work on both types
- [Recover from Errors](../how-to/recover-from-errors.md) — Using Result's recovery API
