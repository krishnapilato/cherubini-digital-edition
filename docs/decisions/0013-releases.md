# 0013. Keep only the latest release

**Status:** Accepted

## Context

Each desktop version is a complete, standalone package, and old ones have no reason to stay in circulation.

## Decision

- Publish through a `v*` tag.
- After a new release is confirmed, delete the previous release and its tag, so only the latest remains.
- Version numbers only go up, and are never reused for different code. The one exception: on 2026-11-09, 1.0.0 was reissued with the JSON loading and the view package, replacing 1.0.1, so that the definitive version is a single 1.0.0.

## Consequences

- People always find one clear download.
- Old versions can't be downloaded again, and the old tags are gone. Their code is still in the Git history.
