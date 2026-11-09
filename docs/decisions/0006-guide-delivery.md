# 0006. One guide, opened differently per edition

**Status:** Accepted

## Context

The guide is a self-contained animated page, shared by both editions.

## Decision

- **Web:** open it in a near full-screen modal containing an iframe, with the app blurred behind it.
- **Desktop:** copy it to a fixed temporary folder, `cherubini-guida`, and open it in the system browser. The same folder is reused every time.

## Consequences

- One guide to maintain, without rewriting it for each interface.
- The desktop depends on a default browser being available.
