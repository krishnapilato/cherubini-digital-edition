package com.cherubinivocabulary;

import module java.base;
import module javafx.fxml;
import module javafx.graphics;
import atlantafx.base.theme.PrimerLight;

public class CherubiniApp extends Application {
    private static final List<String> FONT_WEIGHTS = List.of("Regular", "Medium", "SemiBold");

    @Override
    public void start(Stage stage) throws IOException {
        FONT_WEIGHTS.forEach(weight -> Font.loadFont(resource("fonts/IBMPlexSans-" + weight + ".ttf"), 14));
        setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        var loader = new FXMLLoader(CherubiniApp.class.getResource("main-view.fxml"));
        loader.setControllerFactory(_ -> new MainController(Vocabulary.load(), new History(), getHostServices()));
        var scene = new Scene(loader.load(), 1080, 640);
        scene.getStylesheets().add(resource("app.css"));
        scene.getPreferences().setColorScheme(ColorScheme.LIGHT);

        stage.setTitle("Vocabolario Cherubini");
        stage.getIcons().add(new Image(resource("icon.png")));
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    static String resource(String name) {
        return Objects.requireNonNull(CherubiniApp.class.getResource(name), name).toExternalForm();
    }
}