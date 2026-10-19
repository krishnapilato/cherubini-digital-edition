package com.cherubinivocabulary.cherubinivocabulary;

import module java.base;
import module javafx.controls;
import module javafx.fxml;

import com.cherubinivocabulary.cherubinivocabulary.data.SearchHistory;
import com.cherubinivocabulary.cherubinivocabulary.data.SqliteAbbreviationRepository;
import com.cherubinivocabulary.cherubinivocabulary.data.SqliteDatabase;
import com.cherubinivocabulary.cherubinivocabulary.data.SqliteLemmaRepository;
import com.cherubinivocabulary.cherubinivocabulary.data.Vocabulary;
import com.cherubinivocabulary.cherubinivocabulary.ui.MainController;
import java.nio.file.Path;

public class CherubiniApplication extends Application {
    private static final Path LOCAL_DATABASE = Path.of(System.getProperty("user.home"), ".cherubini", "cherubini.db");

    @Override
    public void start(Stage stage) throws IOException {
        var database = SqliteDatabase.fromBundledCopy(LOCAL_DATABASE);
        var vocabulary = new Vocabulary(new SqliteLemmaRepository(database), new SqliteAbbreviationRepository(database));
        var history = SearchHistory.forCurrentUser();

        var loader = new FXMLLoader(CherubiniApplication.class.getResource("main-view.fxml"));
        loader.setControllerFactory(type -> {
            if (type == MainController.class) return new MainController(vocabulary, history);
            throw new IllegalArgumentException("No controller registered for " + type);
        });

        var scene = new Scene(loader.load(), 1000, 500);
        scene.getStylesheets().add(Objects.requireNonNull(CherubiniApplication.class.getResource("css/theme.css")).toExternalForm());

        stage.setTitle("Vocabolario milanese–italiano · Francesco Cherubini");
        stage.getIcons().setAll(icons());
        stage.setMinWidth(860);
        stage.setMinHeight(460);
        stage.setScene(scene);
        stage.show();

        vocabulary.load();
    }

    private static List<Image> icons() {
        return IntStream.of(16, 32, 48, 64, 128, 256)
                .mapToObj(size -> new Image(Objects.requireNonNull(CherubiniApplication.class.getResource("icons/icon-" + size + ".png")).toExternalForm()))
                .toList();
    }
}
