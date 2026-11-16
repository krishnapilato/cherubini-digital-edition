package com.cherubinivocabulary.view;

import module java.base;
import module javafx.controls;
import com.cherubinivocabulary.Lemma;
import javafx.scene.control.Label;

/**
 * The letters of the alphabet: as pills on the start page and as tiles beside the list.
 */
public final class LetterIndex {
    public static final String ALL = "*";
    private static final List<String> LETTERS = Stream.concat(Stream.of(ALL), "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(Character::toString)).toList();
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final List<Lemma> lemmas;
    private final Map<String, Long> counts;
    private final Map<String, Button> tiles = new HashMap<>();

    public LetterIndex(List<Lemma> lemmas) {
        this.lemmas = lemmas;
        this.counts = lemmas.stream().collect(Collectors.groupingBy(Lemma::initial, Collectors.counting()));
    }

    public void build(FlowPane pills, GridPane grid, Consumer<String> open) {
        for (var i = 0; i < LETTERS.size(); i++) {
            var key = LETTERS.get(i);
            var count = ALL.equals(key) ? lemmas.size() : counts.getOrDefault(key, 0L);
            var description = describe(key, count);

            var pill = new Button(key);
            pill.getStyleClass().add("pill");
            pill.setDisable(count == 0);
            pill.setTooltip(new Tooltip(description));
            pill.setOnAction(_ -> open.accept(key));
            pills.getChildren().add(pill);

            var name = new Label(key);
            name.getStyleClass().add("tile-letter");
            var amount = new Label(Ui.format(count));
            amount.getStyleClass().add("tile-count");
            var face = new VBox(name, amount);
            face.setAlignment(Pos.CENTER);
            var tile = new Button(null, face);
            tile.getStyleClass().add("tile");
            tile.setDisable(count == 0);
            tile.setTooltip(new Tooltip(description));
            tile.setOnAction(_ -> open.accept(key));
            tile.setMaxWidth(Double.MAX_VALUE);
            grid.add(tile, i % 2, i / 2);
            tiles.put(key, tile);
        }
        IntStream.range(0, 2).forEach(_ -> {
            var column = new ColumnConstraints();
            column.setPercentWidth(50);
            grid.getColumnConstraints().add(column);
        });
    }

    public void select(String letter) {
        tiles.forEach((key, tile) -> tile.pseudoClassStateChanged(SELECTED, key.equals(letter)));
    }

    public List<Lemma> lemmasOf(String letter) {
        return ALL.equals(letter) ? lemmas : lemmas.stream().filter(lemma -> lemma.initial().equals(letter)).toList();
    }

    public static String title(String letter) {
        return ALL.equals(letter) ? "Tutti i lemmi" : letter;
    }

    private static String describe(String key, long count) {
        if (ALL.equals(key)) return "Tutti i lemmi";
        return key + ": " + (count == 0 ? "nessun lemma" : Ui.quantity(count, "lemma", "lemmi"));
    }
}
