# 0009. Animations always on

**Status:** Accepted

## Context

With Windows "animation effects" turned off, the guide looked static, and the animations are part of how it explains the app.

## Decision

Keep animations always active. Don't gate them on `prefers-reduced-motion`.

## Consequences

- The guide and the interface look the same on every system.
- People who turn animations off in their system settings for comfort still see them. This is a known accessibility trade-off.
