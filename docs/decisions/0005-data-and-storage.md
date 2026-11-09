# 0005. One JSON source, no data leaves the device

**Status:** Accepted; recent searches on the web changed by [0017](0017-recent-searches-persist-on-the-web.md), desktop loading changed by [0018](0018-desktop-reads-the-json.md)

## Context

Both editions need the same 2,307 entries, and consulting a vocabulary shouldn't require accounts or tracking.

## Decision

- `data/cherubini.json` is the only source.
- **Desktop:** the JSON is bundled in the jar and read at startup ([0018](0018-desktop-reads-the-json.md)). Entries stay in memory. Recent searches are saved in the system preferences.
- **Web:** the JSON stays outside the JavaScript and is kept in `sessionStorage`, along with the browsing state. Closing the tab erases it all.

## Consequences

- Data has one place to change, and nothing is copied to the temporary folder to read it.
- No data leaves the device.
- On the web, recent searches last only as long as the tab.
