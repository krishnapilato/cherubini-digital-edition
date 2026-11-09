# 0008. Real-app tests and release gates

**Status:** Accepted

## Context

Unit tests can't show that a packaged app really opens, and a broken release would reach people who can't diagnose it.

## Decision

- Test the real interface: Playwright on the production web build, failing on console errors and warnings, and the JavaFX robot on the desktop, on the headless platform.
- Run CI on every push and pull request.
- Run the same checks before deploying the site or creating a release.
- Start the Windows package with `--smoke-test` before release. The app must show the vocabulary, or the release stops.
- Use Dependabot for weekly updates and security alerts.

## Consequences

- A release can't be published with a package that fails to start.
- These tests don't cover real screens or real printers, so those still need a manual try.
