# 0001. Two editions with shared sources

**Status:** Accepted

## Context

*Voci tecniche*, Stefano Pilato's program, runs on Microsoft Access 2000. People need to reach the vocabulary without Access, including without installing anything or having admin rights.

## Decision

Provide a web edition (React) and a desktop edition (JavaFX) with the same features. They share `data/cherubini.json`, `guide/` and `tests/search-cases.json`. Each keeps its own interface code, native to its platform.

## Consequences

- The web needs no installation; the desktop works offline as a standalone app.
- Every feature and fix has to be made twice, and the search logic exists in two languages (see [0004](0004-search-written-twice-with-shared-cases.md)).
