import module java.base;
import module java.sql;

void main(String[] args) throws IOException, SQLException {
    Path json = Path.of(args[0]);
    Path database = Path.of(args[1]);
    Files.createDirectories(database.getParent());
    Files.deleteIfExists(database);

    try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
         Statement statement = connection.createStatement()) {
        statement.execute("""
                CREATE TABLE cherubini_table (
                    id            INTEGER PRIMARY KEY,
                    lemma         TEXT NOT NULL,
                    lemmaNo       TEXT,
                    definizioneNo TEXT,
                    def2          TEXT
                )""");
        try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO cherubini_table (id, lemma, lemmaNo, definizioneNo, def2)
                SELECT value ->> 'id', value ->> 'lemma', value ->> 'lemmaNo', value ->> 'definizioneNo', value ->> 'def2'
                FROM json_each(?)""")) {
            insert.setString(1, Files.readString(json));
            if (insert.executeUpdate() == 0) throw new IllegalStateException("Nessuna voce in " + json);
        }
        statement.execute("VACUUM");
    }
}
