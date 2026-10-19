DROP TABLE IF EXISTS cherubini_table;

CREATE TABLE cherubini_table (
    id            INTEGER PRIMARY KEY,
    lemma         TEXT NOT NULL,
    lemmaNo       TEXT,
    definizioneNo TEXT,
    def2          TEXT
);

.import --csv --skip 1 data/cherubini.csv cherubini_table

CREATE INDEX idx_cherubini_lemma ON cherubini_table (lemma COLLATE NOCASE);

VACUUM;
