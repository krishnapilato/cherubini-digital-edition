# 0015. Content Security Policy on the web edition

**Status:** Accepted

## Context

The site has no server, accounts or user content, and the code never inserts HTML, so the current risk is low. A Content Security Policy is a second line of defence: if a vulnerability ever got in, for example through a compromised dependency, the browser would refuse injected scripts and sending data elsewhere. GitHub Pages can't send HTTP headers.

## Decision

- Add the policy as a `<meta>` tag to the built site and to the published copy of the guide: everything from the site itself, and no plugins, `<base>` changes or form submissions.
- Allow the one inline script, the vocabulary preload, by its SHA-256 hash, computed during the build.
- Leave the policy out of the development server, which injects inline scripts of its own, and out of the guide's source, because the desktop opens it from `file://`, where `'self'` is unreliable.
- Add `<meta name="referrer" content="no-referrer">`, so links to other sites don't reveal the page they come from.

## Consequences

- A blocked resource is a console error, so the Playwright tests fail if a change breaks the policy.
- A `<meta>` policy can't use `frame-ancestors`, so other sites can still embed the page.
- A new external resource or inline script needs the policy updated first.
