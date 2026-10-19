package com.cherubinivocabulary.cherubinivocabulary.data;

import module java.base;
import module java.sql;

import org.sqlite.SQLiteConfig;

public final class SqliteDatabase {

    private static final String BUNDLED_DATABASE = "/cherubini.db";

    private final String url;

    private SqliteDatabase(Path file) {
        this.url = "jdbc:sqlite:" + file.toAbsolutePath();
    }

    public static SqliteDatabase fromBundledCopy(Path localCopy) {
        try (InputStream bundled = SqliteDatabase.class.getResourceAsStream(BUNDLED_DATABASE)) {
            if (bundled == null) throw new IllegalStateException("Missing resource " + BUNDLED_DATABASE);
            Files.createDirectories(localCopy.getParent());
            Files.copy(bundled, localCopy, StandardCopyOption.REPLACE_EXISTING);
            return new SqliteDatabase(localCopy);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot prepare the local vocabulary database", e);
        }
    }

    public Connection connect() throws SQLException {
        var config = new SQLiteConfig();
        config.setReadOnly(true);
        return DriverManager.getConnection(url, config.toProperties());
    }

    public boolean hasTable(String name) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Cannot inspect " + url, e);
        }
    }

    @Override
    public String toString() {
        return url;
    }
}
