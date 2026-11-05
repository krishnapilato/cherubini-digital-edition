# 0004. Search written twice, with shared cases

**Status:** Accepted

## Context

Each edition has its own language, so the search can't be one piece of code. Yet the same query must give the same results in both.

## Decision

- Implement the search in TypeScript and in Java, with the same rules: up to four words, all of which must match; accents and capitals ignored; contained words by default, exact words as an option.
- Put the expected behaviour in `tests/search-cases.json`, read by both Vitest and JUnit, including searches on the real vocabulary.
- A word made only of accents is skipped, because it would fold to spaces and match every space in the text.

## Consequences

- A difference between the editions fails the tests. This already found real bugs, such as the handling of non-breaking spaces.
- Each rule change touches two implementations and the shared cases.
