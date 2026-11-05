# 0002. A portable, unsigned desktop package for Windows only

**Status:** Accepted

## Context

The original *Voci tecniche* ran only on Windows, through Microsoft Access: it needed the Access Runtime installed and a restart before it could be used. Today the desktop app is used by two people, both on Windows 11, and the project isn't meant to reach a wider audience. Nobody involved has a Mac or Linux to try a package on. The project is free, so it has no budget for signing certificates.

Packages for macOS and Linux were built at first, but they could only be checked by starting them headless in CI, and their build had parts that tend to break on their own: a macOS Intel runner near its end of life, a separate JDK for it, and an AppImage tool downloaded unpinned.

## Decision

- Ship one portable package, for Windows 10 and 11: a jlink runtime and a jpackage app-image in a zip, with no installer.
- Build it on Windows through GitHub Actions, with only the Windows native libraries of JavaFX and SQLite.
- Point every other system, phones included, to the web edition, which has the same features.
- Don't sign the package. The release notes explain the first-launch warning, and `SHA256SUMS.txt` lets people verify their download.

## Consequences

- Nothing to install, Java is included, and the one package that ships is the one its users actually run.
- Windows SmartScreen warns on first launch.
- There is no automatic update check, so people download new versions themselves.
- The code stays cross-platform: Java and JavaFX have nothing specific to Windows, so another system could come back with one more build in the release workflow.
