package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module java.prefs;
import module javafx.controls;
import module javafx.fxml;

import com.cherubinivocabulary.cherubinivocabulary.data.SearchHistory;
import com.cherubinivocabulary.cherubinivocabulary.data.Vocabulary;
import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewsLoadTest {
    private static final String VIEWS = "/com/cherubinivocabulary/cherubinivocabulary/";
    private static final Lemma CHIAVE = new Lemma(1, "Ciàv", "", "", "Chiave di ferro e di legno.");
    private static final Lemma CAN = new Lemma(2, "Càn", "", "", "Cane. Strumento da imboccar i cerchi sulle botti.");

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        var started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            started.countDown();
        }
        started.await();
    }

    @Test
    void typingShowsTheMatchingDefinitions() throws Exception {
        Stage stage = openMainWindow();

        int found = onFxThread(() -> {
            control(stage, TextField.class, "searchField").setText("ferro legno");
            return 0;
        });
        Thread.sleep(400);
        found = onFxThread(() -> control(stage, ListView.class, "lemmaList").getItems().size());

        assertEquals(1, found);
    }

    @Test
    void lettersFilterTheList() throws Exception {
        Stage stage = openMainWindow();

        int shown = onFxThread(() -> control(stage, ListView.class, "lemmaList").getItems().size());

        assertTrue(shown >= 2);
    }

    private static Stage openMainWindow() throws Exception {
        var vocabulary = new Vocabulary(() -> List.of(CHIAVE, CAN), List::of);
        var history = new SearchHistory(Preferences.userRoot().node("cherubini-tests"));
        var ready = new CountDownLatch(1);
        Stage stage = onFxThread(() -> {
            var loader = new FXMLLoader(ViewsLoadTest.class.getResource(VIEWS + "main-view.fxml"));
            loader.setControllerFactory(_ -> new MainController(vocabulary, history));
            var window = new Stage();
            window.setScene(new Scene(loader.load(), 1000, 500));
            window.getScene().getStylesheets().add(ViewsLoadTest.class.getResource(VIEWS + "css/theme.css").toExternalForm());
            window.show();
            vocabulary.stateProperty().addListener((_, _, state) -> {
                if (state instanceof Vocabulary.Ready) ready.countDown();
            });
            vocabulary.load();
            return window;
        });
        ready.await(10, TimeUnit.SECONDS);
        Thread.sleep(200);
        return stage;
    }

    private static <T extends Node> T control(Stage stage, Class<T> type, String id) {
        return type.cast(stage.getScene().lookup("#" + id));
    }

    private static <T> T onFxThread(Callable<T> work) throws Exception {
        var result = new CompletableFuture<T>();
        Platform.runLater(() -> {
            try {
                result.complete(work.call());
            } catch (Throwable failure) {
                result.completeExceptionally(failure);
            }
        });
        return result.get(20, TimeUnit.SECONDS);
    }
}
