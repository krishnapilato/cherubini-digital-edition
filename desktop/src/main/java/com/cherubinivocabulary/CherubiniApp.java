package com.cherubinivocabulary;

import module java.base;
import module javafx.fxml;
import module javafx.graphics;
import atlantafx.base.theme.PrimerLight;
import javafx.scene.control.Labeled;

public class CherubiniApp extends Application {
    /** Opens the window, checks that it shows the vocabulary and quits: see Launcher. */
    static final String SMOKE_TEST = "--smoke-test";

    private static final List<String> FONT_WEIGHTS = List.of("Regular", "Medium", "SemiBold");
    private static final AtomicBoolean SMOKE_TEST_PASSED = new AtomicBoolean();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Vocabolario Cherubini");
        stage.getIcons().add(new Image(resource("icon.png")));
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(scene(new History(), getHostServices()));
        stage.show();
        if (getParameters().getRaw().contains(SMOKE_TEST)) Platform.runLater(() -> finishTheSmokeTest(stage));
    }

    static boolean passedTheSmokeTest() {
        return SMOKE_TEST_PASSED.get();
    }

    private static void finishTheSmokeTest(Stage stage) {
        String status = stage.getScene().lookup("#statusLabel") instanceof Labeled label ? label.getText() : "";
        SMOKE_TEST_PASSED.set(stage.isShowing() && status.endsWith(" voci · sola consultazione") && !status.startsWith("0 "));
        Platform.exit();
    }

    /** The whole interface, shared with the interface tests. */
    static Scene scene(History history, HostServices host) throws IOException {
        FONT_WEIGHTS.forEach(weight -> Font.loadFont(resource("fonts/IBMPlexSans-" + weight + ".ttf"), 14));
        setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        var loader = new FXMLLoader(CherubiniApp.class.getResource("main-view.fxml"));
        loader.setControllerFactory(_ -> new MainController(Vocabulary.load(), history, host));
        var scene = new Scene(loader.load(), 1080, 640);
        scene.getStylesheets().add(resource("app.css"));
        scene.getPreferences().setColorScheme(ColorScheme.LIGHT);
        return scene;
    }

    static String resource(String name) {
        return Objects.requireNonNull(CherubiniApp.class.getResource(name), name).toExternalForm();
    }
}