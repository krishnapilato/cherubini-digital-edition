package com.cherubinivocabulary.cherubinivocabulary.data;

import module java.base;
import module java.sql;

import com.cherubinivocabulary.cherubinivocabulary.model.Abbreviation;

public final class SqliteAbbreviationRepository implements AbbreviationRepository {
    private static final String TABLE = "abbreviazioni";

    private static final String FIND_ALL = """
            SELECT categoria, abbreviazione, significato
            FROM abbreviazioni
            ORDER BY rowid
            """;

    private final SqliteDatabase database;

    public SqliteAbbreviationRepository(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public List<Abbreviation> findAll() {
        if (!database.hasTable(TABLE)) return List.of();
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rows = statement.executeQuery()) {
            List<Abbreviation> abbreviations = new ArrayList<>();
            while (rows.next()) {
                abbreviations.add(new Abbreviation(
                        rows.getString("categoria"),
                        rows.getString("abbreviazione"),
                        rows.getString("significato")));
            }
            return List.copyOf(abbreviations);
        } catch (SQLException e) {
            throw new DataAccessException("Cannot read the abbreviations from " + database, e);
        }
    }
}
