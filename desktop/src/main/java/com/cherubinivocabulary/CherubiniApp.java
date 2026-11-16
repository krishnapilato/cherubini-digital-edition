package com.cherubinivocabulary;

import module java.base;
import module javafx.fxml;
import module javafx.graphics;
import atlantafx.base.theme.PrimerLight;

public class CherubiniApp extends Application {
    static final String SMOKE_TEST = "--smoke-test";

    private static final AtomicBoolean SMOKE_TEST_PASSED = new AtomicBoolean();

    // The smoke test must fail loudly on any problem; a reader gets a calm message instead.
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void start(Stage stage) throws IOException {
        var smokeTest = getParameters().getRaw().contains(SMOKE_TEST);
        if (!smokeTest) Problems.reportUnexpected();
        try {
            stage.setTitle("Vocabolario Cherubini");
            stage.getIcons().add(new Image(resource("icon.png")));
            stage.setMinWidth(900);
            stage.setMinHeight(560);
            stage.setScene(scene(new History(), getHostServices()));
            stage.show();
        } catch (IOException | RuntimeException e) {
            if (smokeTest) throw e;
            Problems.reportStartupFailure(e);
            Platform.exit();
            return;
        }
        if (smokeTest) Platform.runLater(() -> finishTheSmokeTest(stage));
    }

    static boolean passedTheSmokeTest() {
        return SMOKE_TEST_PASSED.get();
    }

    private static void finishTheSmokeTest(Stage stage) {
        // Letter buttons are enabled only when the vocabulary contains entries.
        var hasEntries = stage.getScene().getRoot().lookupAll("#letterPane .pill").stream().anyMatch(node -> !node.isDisabled());
        SMOKE_TEST_PASSED.set(stage.isShowing() && hasEntries);
        Platform.exit();
    }

    static Scene scene(History history, HostServices host) throws IOException {
        List.of("Regular", "Medium", "SemiBold").forEach(weight -> Font.loadFont(resource("fonts/IBMPlexSans-" + weight + ".ttf"), 14));
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

    // Written by Maven from the version in pom.xml, so the footer never needs editing by hand.
    static String version() {
        try (var in = Objects.requireNonNull(CherubiniApp.class.getResourceAsStream("version.properties"), "version.properties")) {
            var properties = new Properties();
            properties.load(in);
            return properties.getProperty("version");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the version.", e);
        }
    }
}
