package com.cherubinivocabulary;

import module java.base;
import module java.sql;

import org.sqlite.SQLiteConfig;

final class Vocabulary {
    private static final String QUERY = "SELECT id, lemma, def2 FROM cherubini_table ORDER BY id";

    private Vocabulary() {}

    static List<Lemma> load() {
        try (var in = Vocabulary.class.getResourceAsStream("/cherubini.db")) {
            var copy = Files.createTempFile("cherubini", ".db");
            try {
                Files.copy(Objects.requireNonNull(in, "cherubini.db"), copy, StandardCopyOption.REPLACE_EXISTING);
                return read(copy);
            } finally {
                Files.deleteIfExists(copy);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile preparare il vocabolario", e);
        }
    }

    private static List<Lemma> read(Path db) {
        var config = new SQLiteConfig();
        config.setReadOnly(true);
        try (var conn = DriverManager.getConnection("jdbc:sqlite:" + db, config.toProperties());
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(QUERY)) {

            var lemmas = new ArrayList<Lemma>();
            while (rs.next()) lemmas.add(Lemma.of(rs.getLong(1), rs.getString(2), rs.getString(3)));
            return List.copyOf(lemmas);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossibile leggere il vocabolario", e);
        }
    }
}
