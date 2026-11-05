# 0005. One JSON source, no data leaves the device

**Status:** Accepted

## Context

Both editions need the same 2,307 entries, and consulting a vocabulary shouldn't require accounts or tracking.

## Decision

- `data/cherubini.json` is the only source. The SQLite database is generated during the Maven build and isn't in the repository.
- **Desktop:** SQLite needs a real file, so the bundled database is copied to a temporary file only while reading, then deleted. Entries stay in memory. Recent searches are saved in the system preferences.
- **Web:** the JSON stays outside the JavaScript and is kept in `sessionStorage`, along with the browsing state. Closing the tab erases it all.

## Consequences

- Data has one place to change, and temporary copies don't pile up.
- No data leaves the device.
- On the web, recent searches last only as long as the tab.
