# 0019. The window's pieces live in their own package

**Status:** Accepted

## Context

`MainController` had grown to over 800 lines: besides directing the window, it built the letter tiles, the recent-search chips, the list rows and the print panel, measured the excerpts and marked the matches. Each part was hard to find and to change on its own.

## Decision

- `MainController` keeps only the direction of the window: which page is shown, what the list holds, which entry is open, and the handlers named in `main-view.fxml`.
- Each piece of the interface is a class in `com.cherubinivocabulary.view`: `EntryView`, `LemmaCell` with `RowLayout`, `LetterIndex`, `RecentSearches`, `PrintSelection`, `Toast`, and the small helpers `Highlight` and `Ui`.
- The pieces receive the nodes from the controller; they don't load FXML or know about each other.
- The behaviour and the algorithms are unchanged: the split moves code, it doesn't rewrite it.

## Consequences

- The controller drops from about 820 to about 500 lines, a quarter of which are the fields injected from FXML, and each piece can be read on its own.
- `Search`, `Search.Range`, `Lemma.initial()` and the methods of `History` are public so the view package can use them. The module exports no package, so nothing outside the app sees them.
