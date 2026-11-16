# 0011. Printing through the system dialog

**Status:** Accepted

## Context

The original program printed directly, but the default printer can be a virtual one, and the print service may be stopped.

## Decision

- Always show the system print dialog, and report missing printers with a clear message.
- Paginate the entries in the app, with a heading and page number.
- Keep printing on the JavaFX thread, without a background thread.

## Consequences

- People choose the printer themselves, and the code stays simple.
- The window doesn't respond while the system prints. This is barely noticeable for a few entries, but a very large job on a slow driver could block it for a while. Moving it to a background thread would add complexity, and it can't be fully tested without a printer. Revisit this if someone actually hits it.
