# 0003. A "research instrument" interface

**Status:** Accepted

## Context

The original program's purpose is surfacing technical terms hidden inside definitions. The redesign should honour it while being more functional and attractive. Rejected directions: warm paper cards, a minimal tabbed layout, a replica of Access 2000, and heavy 3D gradients.

## Decision

- The interface is a neutral, luminous, precise search instrument, with one green accent from the icon and red only for found words.
- Three modes: *Lente* (a large centered search as the opening view), *Atlante* (browsing by letter) and *Strumento* (search results).
- It looks the same everywhere: no imitation of Material, Fluent or Apple styles, no dark theme, and a bundled font (IBM Plex Sans).
- Secondary tools appear in panels only when needed.
- No filters by trade.

## Consequences

- Consistent look on every system, at the cost of bundling the font.
- No dark mode for those who prefer it.
