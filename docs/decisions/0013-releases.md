# 0013. Keep only the latest release

**Status:** Accepted

## Context

Each desktop version is a complete, standalone package, and old ones have no reason to stay in circulation.

## Decision

- Publish through a `v*` tag.
- After a new release is confirmed, delete the previous release and its tag, so only the latest remains.
- Version numbers only go up, and are never reused for different code.

## Consequences

- People always find one clear download.
- Old versions can't be downloaded again, and the old tags are gone. Their code is still in the Git history.
