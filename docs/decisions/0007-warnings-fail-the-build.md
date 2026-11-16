# 0007. Warnings fail the build

**Status:** Accepted

## Context

Unused code and warnings tend to pile up unless something stops them. The checks should run during normal work, not only in a separate review.

## Decision

- **Desktop:** `javac -Xlint:all -Werror`, and PMD on both main and test sources once the tests are compiled (`process-test-classes`), with the curated rules in `desktop/pmd-ruleset.xml`.
- **Web:** TypeScript in strict mode, and ESLint with the SonarJS and jsx-a11y rules and `--max-warnings 0`. Prettier checks the formatting, with lines up to 120 characters.

## Consequences

- Code with a warning doesn't build, on a PC or in CI.
- Some valid choices are ruled out: PMD rejects `volatile`, so the code uses `AtomicBoolean`.
- Every tool update can bring new reports to resolve.
