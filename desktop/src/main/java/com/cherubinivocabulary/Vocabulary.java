package com.cherubinivocabulary;

import module java.base;
import module java.sql;

import org.sqlite.SQLiteConfig;

final class Vocabulary {
    private static final String QUERY = "SELECT id, lemma, def2 FROM cherubini_table ORDER BY id";

    private Vocabulary() {}

    static List<Lemma> load() {
        try (InputStream bundled = Vocabulary.class.getResourceAsStream("/cherubini.db")) {
            Path copy = Files.createTempFile("cherubini", ".db");
            copy.toFile().deleteOnExit();
            Files.copy(Objects.requireNonNull(bundled, "cherubini.db"), copy, StandardCopyOption.REPLACE_EXISTING);
            return read(copy);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile preparare il vocabolario", e);
        }
    }

    private static List<Lemma> read(Path database) {
        var config = new SQLiteConfig();
        config.setReadOnly(true);
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database, config.toProperties());
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(QUERY)) {
            List<Lemma> lemmas = new ArrayList<>();
            while (rows.next()) lemmas.add(Lemma.of(rows.getLong("id"), rows.getString("lemma"), rows.getString("def2")));
            return List.copyOf(lemmas);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossibile leggere il vocabolario", e);
        }
    }
}
