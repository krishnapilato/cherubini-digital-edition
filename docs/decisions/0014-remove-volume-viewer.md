# 0014. Remove the volume viewer

**Status:** Accepted

## Context

An earlier version added *Sfoglia il volume*, which showed the scanned vocabulary from Google Books: in a dialog on the web, and in a browser window from the desktop. The next step would have been searching inside the scan and listing the pages that contain a word.

Google offers no official way to do that. The Books API only searches across books, the Embedded Viewer can only highlight a word on the pages already shown, and the old page-level search API was retired in 2011. Getting the pages would mean scraping Google Books through a server of our own. Even the viewer alone loads Google on every visit and needs an internet connection.

## Decision

Remove *Sfoglia il volume* from both editions, along with its tests and styles.

## Consequences

- No part of the app contacts a third party again, in line with [0005](0005-data-and-storage.md), and the desktop works fully offline.
- People can't browse the original scans from the app.
- If page search returns, it should come from our own word-to-pages index built from the public-domain text, not from a proxy to Google.
