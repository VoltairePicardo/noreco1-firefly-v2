# Lessons Learned

## findOne() → findById() conversion

**Rule:** Always put `.orElse(null)` AFTER `findById(argument)`, never inside the argument.

**Wrong:**
```java
repo.findById(entity.getId().orElse(null))   // getId() is Integer, not Optional
```

**Correct:**
```java
repo.findById(entity.getId()).orElse(null)
```

**Why:** `findById()` returns `Optional<T>`. The `.orElse(null)` unwraps the Optional result — it does NOT belong inside the argument. `getId()` on a JPA entity returns a plain `Integer`, not `Optional<Integer>`, so calling `.orElse(null)` on it is a compile error.

**How to apply:** When replacing `findOne(x)` → `findById(x).orElse(null)`, check each occurrence individually. Do not use bulk scripts — verify argument types and ensure `.orElse(null)` is chained on the return value, not inside the parentheses.
