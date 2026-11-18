# 0020. The same tools and every check, everywhere

**Status:** Accepted; changes the Node.js and JDK choice of [0016](0016-pinned-build-tools.md) and extends [0007](0007-warnings-fail-the-build.md)

## Context

A broken site on GitHub Pages or a faulty desktop package must never be published. CI picked the newest Node.js 26 and the newest Temurin JDK 27, so it could build with tools that had never run on the development computer, and `npm i` could install versions other than the ones in the lockfile. Nothing caught a dependency that was declared but no longer used, like SQLite before [0018](0018-desktop-reads-the-json.md). The project also always moves to the latest versions of its tools and packages.

## Decision

- CI and the release use exactly the development computer's tools: Node.js from `web/.node-version`, Oracle JDK 27.0.1.1 downloaded from Oracle and handed to `setup-java`, Maven from the wrapper. They are raised by hand, to the latest version, together with the computer; `./mvnw -v` prints them in every desktop log.
- Workflows install with `npm ci`, so they build exactly what `package-lock.json` holds.
- `npm run check` is the single gate of the web edition: Knip, unit tests, type check, lint, formatting, build and interface tests. `npm run dev` runs it first and doesn't start if anything fails; CI and the site deployment run the same command.
- Unused or undeclared dependencies fail the build: Knip on the web, `dependency:analyze-only` on the desktop, next to PMD. A dependency used in a way the analysis can't see is excepted in `web/knip.jsonc` or `desktop/pom.xml`, with the reason next to it.
- Size budgets fail too, about 10% above the sizes when they were set: the web's JavaScript and CSS, gzipped, through size-limit in `npm run check`; the desktop app on disk and its zip, checked by the release (68 MB and 49 MB, from 62.2 MB and 44.7 MB in 1.0.2). The vocabulary data doesn't change, so it has no budget.

## Consequences

- What passes on the computer passes in CI, and what is published is what was tested.
- Starting the development server takes about 20 seconds more.
- New Node.js and JDK versions don't arrive by themselves: `web/.node-version` and the `java-version` in the workflows have to be raised.
- When an update makes either edition heavier than its budget, for example a new JDK, raising `.size-limit.json` or the numbers in the release workflow is a deliberate choice, not something that happens unnoticed.
