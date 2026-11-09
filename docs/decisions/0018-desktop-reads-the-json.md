# 0018. The desktop edition reads the JSON directly

**Status:** Accepted

## Context

The desktop edition used to turn `data/cherubini.json` into a SQLite database during the build. At every startup it copied that database to a temporary file, loaded the SQLite native library, read all the rows with a single query and deleted the copy. The search already ran in memory, so SQLite was only a file format: it brought a native library, the native-access flags, a build step and a temporary file, and gave nothing in return.

## Decision

- Bundle `data/cherubini.json` in the jar as it is, and read it at startup with the Jackson streaming parser (`jackson-core`).
- Use the streaming parser rather than data binding: the file is a flat array of flat objects, and reading it token by token loads far fewer classes at startup. `jackson-databind` stays a test dependency, for the shared search cases.

## Consequences

- No native code for the data, no temporary file and no build step to generate it. The jlink runtime no longer needs `java.sql`.
- Startup is a little faster (about 0.15 s less, measured headless with `--smoke-test`). The entries take about 2.8 MB in memory, as before.
- The desktop and web editions read the same file the same way.
