package com.cherubinivocabulary;

import module java.base;
import module javafx.controls;
import atlantafx.base.controls.ModalPane;
import javafx.scene.control.Label;
import javafx.scene.robot.Robot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Drives the real window with the JavaFX robot: clicks and key presses go through the same
 * event handling as a user's. The JavaFX headless platform (see the surefire configuration)
 * lets it run without a screen, in CI too.
 */
class InterfaceTest {
    private static final long TIMEOUT_SECONDS = 5;
    private static final long STILL_NANOS = TimeUnit.MILLISECONDS.toNanos(300);
    private static final String FERRO = "ferro";

    private Stage stage;
    private Robot robot;

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        var started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        assertEquals(true, started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "JavaFX did not start");
        // Every test closes its window: JavaFX must keep running for the next one.
        Platform.setImplicitExit(false);
    }

    @BeforeEach
    void openTheWindow() {
        onFx(() -> {
            stage = new Stage();
            stage.setScene(CherubiniApp.scene(new History(new MemoryPreferences()), null));
            stage.show();
            // The headless screen is 1000 × 1000: the whole window must fit on it.
            stage.setX(0);
            stage.setY(0);
            stage.setWidth(1000);
            robot = new Robot();
            return stage;
        });
        waitUntil("the start page is shown", () -> node("lente").isVisible());
    }

    @AfterEach
    void closeTheWindow() {
        run(stage::close);
    }

    @Test
    void startsWithoutFocusOnTheSearchSoThePlaceholderIsVisible() {
        assertFalse(onFx(() -> node("bigSearch").isFocused()));
        assertFalse(onFx(() -> stage.getScene().getFocusOwner() instanceof TextInputControl));
    }

    @Test
    void clickingOutsideTheSearchTakesTheFocusAway() {
        click(node("bigSearch"));
        waitUntil("the search is focused", () -> node("bigSearch").isFocused());
        click(onFx(() -> stage.getScene().lookup(".lente-title")));
        waitUntil("the search loses the focus", () -> !node("bigSearch").isFocused());
    }

    @Test
    void typingSearchesTheDefinitions() {
        search(FERRO);
        assertEquals("Definizioni   97", onFx(() -> node("definitionsTab", ToggleButton.class).getText()));
        assertEquals("Anèll", selectedLemma());
    }

    @Test
    void browsesALetterFromTheStartPage() {
        click(onFx(() -> stage.getScene().getRoot().lookupAll(".pill").stream()
                .filter(pill -> "J".equals(((Button) pill).getText()))
                .findFirst()
                .orElseThrow()));
        waitUntil("the letter J is open", () -> "J".equals(node("listTitle", Label.class).getText()));
        assertEquals(1, onFx(() -> lemmaList().getItems().size()));
        assertEquals("Jùre(De)", selectedLemma());
    }

    @Test
    void f3GoesToTheNextOccurrenceAndThenToTheNextEntry() {
        search(FERRO);
        // Anèll has a single occurrence: the first F3 reaches it, the second moves on.
        press(KeyCode.F3);
        assertEquals("Anèll", selectedLemma());
        press(KeyCode.F3);
        waitUntil("the next entry is open", () -> "Bocchètta".equals(lemmaList().getSelectionModel().getSelectedItem().lemma()));
    }

    @Test
    void escapeClearsTheSearchAndGoesBackToTheStartPage() {
        search(FERRO);
        press(KeyCode.ESCAPE);
        waitUntil("the start page is shown again", () -> node("lente").isVisible());
    }

    @Test
    void enterSavesARecentSearchWhichCanBeRemoved() {
        search(FERRO);
        press(KeyCode.ENTER);
        press(KeyCode.ESCAPE);
        waitUntil("the start page shows the recent search", () -> node("lente").isVisible() && node("recentPane").isVisible());
        click(onFx(() -> node("recentPane").lookup(".chip-remove")));
        waitUntil("the recent search is removed", () -> !node("recentPane").isVisible());
    }

    @Test
    void thePrintPanelOpensAndCloses() {
        click(onFx(() -> ((Pane) node("lenteActions")).getChildren().stream()
                .filter(child -> child instanceof Button button && "Stampa".equals(button.getText()))
                .findFirst()
                .orElseThrow()));
        waitUntil("the print panel is open", () -> node("modal", ModalPane.class).isDisplay());
        click(onFx(() -> node("printPanel").lookup(".button-icon")));
        waitUntil("the print panel is closed", () -> !node("modal", ModalPane.class).isDisplay());
    }

    private void search(String text) {
        click(node("bigSearch"));
        type(text);
        waitUntil("the results are shown", () -> node("workspace").isVisible() && lemmaList().getSelectionModel().getSelectedItem() != null);
    }

    private String selectedLemma() {
        return onFx(() -> lemmaList().getSelectionModel().getSelectedItem().lemma());
    }

    @SuppressWarnings("unchecked")
    private ListView<Lemma> lemmaList() {
        return (ListView<Lemma>) node("lemmaList", ListView.class);
    }

    private Node node(String id) {
        return node(id, Node.class);
    }

    private <T extends Node> T node(String id, Class<T> type) {
        return onFx(() -> type.cast(stage.getScene().lookup("#" + id)));
    }

    /**
     * Clicks the centre of the target once it is shown inside the window and its entrance
     * animation is over: on a slow machine two checks in a row can fall between two frames,
     * so the target must stay still for a real stretch of time.
     */
    private void click(Node target) {
        var last = new AtomicReference<Bounds>();
        var stillSince = new AtomicLong();
        waitUntil("the target is shown and stays still", () -> {
            Bounds bounds = target.localToScreen(target.getBoundsInLocal());
            if (!shown(target) || bounds == null || !insideTheWindow(bounds)) return false;
            long now = System.nanoTime();
            if (!bounds.equals(last.getAndSet(bounds))) stillSince.set(now);
            return now - stillSince.get() >= STILL_NANOS;
        });
        run(() -> {
            Bounds bounds = target.localToScreen(target.getBoundsInLocal());
            robot.mouseMove(bounds.getCenterX(), bounds.getCenterY());
            robot.mouseClick(MouseButton.PRIMARY);
        });
    }

    private boolean insideTheWindow(Bounds bounds) {
        return new BoundingBox(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()).contains(bounds);
    }

    private static boolean shown(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (!current.isVisible() || current.getOpacity() < 1) return false;
        }
        return true;
    }

    private void type(String text) {
        run(() -> text.chars().forEach(character -> robot.keyType(KeyCode.getKeyCode(Character.toString(Character.toUpperCase(character))))));
    }

    private void press(KeyCode key) {
        run(() -> robot.keyType(key));
    }

    private static void waitUntil(String what, BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (!onFx(condition::getAsBoolean)) {
            if (System.nanoTime() > deadline) fail("Not reached in time: " + what);
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20));
        }
    }

    private static void run(Runnable action) {
        onFx(Executors.callable(action));
    }

    /** Runs on the JavaFX thread, which owns the window, and waits for the result. */
    private static <T> T onFx(Callable<T> action) {
        var task = new FutureTask<>(action);
        // Already on the JavaFX thread (inside another onFx), waiting for it would never end.
        if (Platform.isFxApplicationThread()) task.run();
        else Platform.runLater(task);
        try {
            return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new AssertionError(e);
        }
    }
}
