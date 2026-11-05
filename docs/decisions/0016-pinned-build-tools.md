# 0016. Pinned build tools

**Status:** Accepted

## Context

CI used `node-version: latest` and the Maven installed on GitHub's runners. The same commit could then be built with different tools from one day to the next, and a new major Node.js release could break a push that changed nothing. Once the app is finished, changes should come only from bug fixes and dependency updates, not from the build machines.

## Decision

- Run Node.js 26 in CI and in the site build, with `check-latest`, so every 26.x update arrives and the major version stays fixed.
- Run Maven through the wrapper (`./mvnw`), which downloads the version written in `desktop/.mvn/wrapper/maven-wrapper.properties`, Maven 3.10.0, on every computer and in every workflow.
- Keep JDK 27 from Temurin through `setup-java`, as before.

## Consequences

- A PC, CI and a release build with the same tools.
- Moving to a new major Node.js or Maven version is a deliberate change in these files: Dependabot doesn't update `node-version`.
- The wrapper downloads Maven, about 10 MB, the first time it runs on a machine.
