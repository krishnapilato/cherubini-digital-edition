package com.cherubinivocabulary.cherubinivocabulary.data;

import module java.base;
import module java.sql;

import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;

public final class SqliteLemmaRepository implements LemmaRepository {

    private static final String FIND_ALL = "SELECT id, lemma, lemmaNo, definizioneNo, def2 FROM cherubini_table ORDER BY id";

    private final SqliteDatabase database;

    public SqliteLemmaRepository(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public List<Lemma> findAll() {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rows = statement.executeQuery()) {
            List<Lemma> lemmas = new ArrayList<>();
            while (rows.next()) {
                lemmas.add(new Lemma(
                        rows.getLong("id"),
                        rows.getString("lemma"),
                        rows.getString("lemmaNo"),
                        rows.getString("definizioneNo"),
                        rows.getString("def2")));
            }
            return List.copyOf(lemmas);
        } catch (SQLException e) {
            throw new DataAccessException("Cannot read the vocabulary from " + database, e);
        }
    }
}
