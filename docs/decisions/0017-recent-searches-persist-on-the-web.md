# 0017. Recent searches persist on the web

**Status:** Accepted

## Context

[Decision 0005](0005-data-and-storage.md) kept the whole web state in `sessionStorage`, recent searches included, so they vanished with the tab. The desktop keeps them in the system preferences, so the two editions behaved differently, and recent searches are meant to be found again at the next visit.

## Decision

- Keep recent searches in `localStorage`, in their own store (`cherubini.recent`), as the desktop does.
- Leave the rest of the browsing state (search, letter, open entry, entries to print) in `sessionStorage`.
- «Ricomincia da capo» on the error page clears the recent searches too, so a damaged saved value can't keep breaking the page.

## Consequences

- Both editions remember the same six searches across visits.
- On a shared computer the next person sees the recent searches; each one can be removed with its X. They still never leave the browser.
- Node has no `localStorage` without a backing file, so the unit tests give it an in-memory one.
