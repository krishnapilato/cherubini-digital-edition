package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;

import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public final class LetterStrip extends HBox {
    public static final String ALL = "Tutte";

    private static final List<String> LETTERS = Stream.concat(Stream.of(ALL), "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(Character::toString)).toList();
    private static final int VISIBLE = 5;
    private static final double STEP = 44;
    private static final double WINDOW = VISIBLE * STEP - 4;

    private final ToggleGroup group = new ToggleGroup();
    private final HBox track = new HBox(4);
    private final Region thumb = new Region();
    private final StackPane indicator = new StackPane(thumb);
    private final Button previous = Icons.button("fas-chevron-left", "Lettere precedenti");
    private final Button next = Icons.button("fas-chevron-right", "Lettere successive");
    private final PauseTransition indicatorTimeout = new PauseTransition(Duration.millis(1200));
    private Consumer<String> onLetterSelected = _ -> { };
    private int first;

    public LetterStrip() {
        getStyleClass().add("letter-strip");
        setAlignment(Pos.CENTER);
        setSpacing(4);

        LETTERS.forEach(letter -> track.getChildren().add(letterButton(letter)));
        var window = new Pane(track);
        window.setMinSize(WINDOW, 36);
        window.setMaxSize(WINDOW, 36);
        window.setClip(new Rectangle(WINDOW, 36));
        window.setOnScroll(event -> scrollBy(event.getDeltaY() + event.getDeltaX() < 0 ? 1 : -1));

        thumb.getStyleClass().add("strip-thumb");
        thumb.setMaxWidth(WINDOW * VISIBLE / LETTERS.size());
        indicator.getStyleClass().add("strip-indicator");
        indicator.setAlignment(Pos.CENTER_LEFT);
        indicator.setMaxWidth(WINDOW);
        indicator.setOpacity(0);
        indicatorTimeout.setOnFinished(_ -> fade(indicator, 0));

        previous.setOnAction(_ -> scrollBy(-VISIBLE));
        next.setOnAction(_ -> scrollBy(VISIBLE));
        getChildren().addAll(previous, new VBox(6, window, indicator), next);

        group.selectedToggleProperty().addListener((_, before, selected) -> {
            if (selected == null) group.selectToggle(before);
            else onLetterSelected.accept((String) selected.getUserData());
        });
        refreshArrows();
    }

    public void setOnLetterSelected(Consumer<String> onLetterSelected) {
        this.onLetterSelected = Objects.requireNonNull(onLetterSelected);
    }

    public void setLemmaCounts(Map<String, Long> counts) {
        group.getToggles().forEach(toggle -> {
            var button = (ToggleButton) toggle;
            String letter = (String) button.getUserData();
            long count = ALL.equals(letter) ? counts.values().stream().mapToLong(Long::longValue).sum() : counts.getOrDefault(letter, 0L);
            button.setDisable(count == 0);
            button.setTooltip(new Tooltip(ALL.equals(letter) ? "Tutti i lemmi" : count == 0 ? letter + ": nessun lemma" : letter + ": " + count + " lemmi"));
        });
    }

    public void select(String letter) {
        group.getToggles().stream()
                .filter(toggle -> letter.equals(toggle.getUserData()))
                .findFirst()
                .ifPresent(group::selectToggle);
        int index = LETTERS.indexOf(letter);
        if (index < first || index >= first + VISIBLE) scrollBy(index - first - VISIBLE / 2);
    }

    private ToggleButton letterButton(String letter) {
        var button = new ToggleButton(letter);
        button.setUserData(letter);
        button.setToggleGroup(group);
        button.getStyleClass().add(ALL.equals(letter) ? "letter-all" : "letter");
        return button;
    }

    private void scrollBy(int delta) {
        first = Math.clamp(first + delta, 0, LETTERS.size() - VISIBLE);
        var slide = new TranslateTransition(Duration.millis(260), track);
        slide.setToX(-first * STEP);
        slide.setInterpolator(Interpolator.EASE_OUT);
        slide.play();
        thumb.setTranslateX(WINDOW * first / LETTERS.size());
        fade(indicator, 1);
        indicatorTimeout.playFromStart();
        refreshArrows();
    }

    private void refreshArrows() {
        previous.setDisable(first == 0);
        next.setDisable(first >= LETTERS.size() - VISIBLE);
    }

    private static void fade(Node node, double opacity) {
        var fade = new FadeTransition(Duration.millis(opacity > 0 ? 120 : 450), node);
        fade.setToValue(opacity);
        fade.play();
    }
}
