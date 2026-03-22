# Quick Reference

A cheat sheet for all Aether FP types. For detailed explanations, follow the links to the relevant documentation.

## Import

```java
import de.splatgames.aether.fp.types.Either;
import de.splatgames.aether.fp.types.Result;
import de.splatgames.aether.fp.types.Lazy;
```

---

## Either&lt;L, R&gt;

Right-biased sum type. `Left` = error/alternative, `Right` = success/primary.

| Method | Signature | Description |
|--------|-----------|-------------|
| `left` | `static <L, R> Either<L, R> left(L value)` | Create a Left |
| `right` | `static <L, R> Either<L, R> right(R value)` | Create a Right |
| `isLeft` | `boolean isLeft()` | Test for Left |
| `isRight` | `boolean isRight()` | Test for Right |
| `map` | `<U> Either<L, U> map(Function<R, U> fn)` | Transform the right value |
| `flatMap` | `<U> Either<L, U> flatMap(Function<R, Either<L, U>> fn)` | Chain with a function that returns Either |
| `mapLeft` | `<T> Either<T, R> mapLeft(Function<L, T> fn)` | Transform the left value |
| `fold` | `<U> U fold(Function<L, U> leftFn, Function<R, U> rightFn)` | Handle both cases exhaustively |
| `swap` | `Either<R, L> swap()` | Swap left and right |
| `ifLeft` | `Either<L, R> ifLeft(Consumer<L> c)` | Side effect on Left |
| `ifRight` | `Either<L, R> ifRight(Consumer<R> c)` | Side effect on Right |
| `getOrElse` | `R getOrElse(R other)` | Right value or default |
| `getOrElseGet` | `R getOrElseGet(Function<L, R> fn)` | Right value or computed fallback |
| `getOrElseThrow` | `R getOrElseThrow(Function<L, RuntimeException> fn)` | Right value or throw |

---

## Result&lt;T, E&gt;

Success-biased outcome type. `Success` = happy path, `Failure` = error.

| Method | Signature | Description |
|--------|-----------|-------------|
| `success` | `static <T, E> Result<T, E> success(T value)` | Create a Success |
| `failure` | `static <T, E> Result<T, E> failure(E error)` | Create a Failure |
| `isSuccess` | `boolean isSuccess()` | Test for Success |
| `isFailure` | `boolean isFailure()` | Test for Failure |
| `map` | `<U> Result<U, E> map(Function<T, U> fn)` | Transform the success value |
| `flatMap` | `<U> Result<U, E> flatMap(Function<T, Result<U, E>> fn)` | Chain with a function that returns Result |
| `mapError` | `<X> Result<T, X> mapError(Function<E, X> fn)` | Transform the error value |
| `fold` | `<U> U fold(Function<E, U> failureFn, Function<T, U> successFn)` | Handle both cases exhaustively |
| `recover` | `Result<T, E> recover(Function<E, T> fn)` | Convert Failure to Success |
| `recoverWith` | `Result<T, E> recoverWith(Function<E, Result<T, E>> fn)` | Convert Failure to new Result |
| `ifSuccess` | `Result<T, E> ifSuccess(Consumer<T> c)` | Side effect on Success |
| `ifFailure` | `Result<T, E> ifFailure(Consumer<E> c)` | Side effect on Failure |
| `getOrElse` | `T getOrElse(T other)` | Success value or default |
| `getOrElseGet` | `T getOrElseGet(Function<E, T> fn)` | Success value or computed fallback |
| `getOrElseThrow` | `T getOrElseThrow(Function<E, RuntimeException> fn)` | Success value or throw |
| `toEither` | `Either<E, T> toEither()` | Convert to Either (Success→Right, Failure→Left) |

---

## Lazy&lt;T&gt;

Thread-safe, memoized deferred computation.

| Method | Signature | Forces Evaluation? | Description |
|--------|-----------|:---:|-------------|
| `of` | `static <T> Lazy<T> of(Supplier<T> supplier)` | No | Create unevaluated Lazy |
| `get` | `T get()` | Yes | Get or compute value |
| `isEvaluated` | `boolean isEvaluated()` | No | Check if already computed |
| `map` | `<U> Lazy<U> map(Function<T, U> fn)` | No | Lazy transformation |
| `flatMap` | `<U> Lazy<U> flatMap(Function<T, Lazy<U>> fn)` | No | Lazy composition |
| `ifEvaluated` | `Lazy<T> ifEvaluated(Consumer<T> c)` | No | Side effect if already computed |
| `toOptional` | `Optional<T> toOptional()` | No | Optional.of if evaluated, empty if not |
| `equals` | `boolean equals(Object obj)` | **Yes** | Value-based equality |
| `hashCode` | `int hashCode()` | **Yes** | Hash from computed value |
| `toString` | `String toString()` | No | `Lazy[value]` or `Lazy[?]` |

---

## Common Patterns

```java
// Either: validate and transform
Either<String, Integer> result = Either.<String, String>right("42")
    .flatMap(s -> parseInteger(s))
    .map(n -> n * 2);

// Result: operation pipeline with recovery
Result<Data, String> data = loadPrimary()
    .recoverWith(err -> loadBackup())
    .map(raw -> transform(raw));

// Lazy: deferred initialization
Lazy<Config> config = Lazy.of(() -> loadConfig());
Lazy<String> url = config.map(Config::databaseUrl);

// Result to Either
Either<String, Data> either = result.toEither();

// Exhaustive handling
String output = result.fold(
    error -> "Failed: " + error,
    value -> "Got: " + value
);
```

## Which Type Should I Use?

| Situation | Type |
|-----------|------|
| Value is one of two types (no success/failure meaning) | `Either<L, R>` |
| Operation that can succeed or fail | `Result<T, E>` |
| Expensive computation that should run at most once | `Lazy<T>` |
| Need error recovery (fallbacks) | `Result<T, E>` |
| Need to swap perspectives | `Either<L, R>` |
| Defer and cache any computation | `Lazy<T>` |
