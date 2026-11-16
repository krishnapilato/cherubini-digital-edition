# 0010. Java code style

**Status:** Accepted

## Context

The desktop code should be compact and consistent. One suggestion was to use only interfaces and records, and another was to adopt Java preview features.

## Decision

- Use `var` for local and loop variables, and put single-statement `if`s on one line.
- Start sources with `import module java.base;`. Test sources don't import `javafx.*` modules, because IntelliJ compiles tests on the classpath.
- Use records for data (`Lemma`, `Search.Query`), and final classes for static utilities and services (`Help`, `Vocabulary`, `History`). An interface declares a type to implement, and a record would expose its state.
- Use no preview features: they need `--enable-preview` at every step, tie the classes to one JDK, and may not parse in PMD.
- Comment only non-obvious reasons.

## Consequences

- New Java features are adopted once final.
- Short code relies on clear names rather than comments.
