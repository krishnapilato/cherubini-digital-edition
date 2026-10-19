package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;

import com.cherubinivocabulary.cherubinivocabulary.model.TextRange;
import javafx.scene.shape.Path;

public final class MarkedText extends Region {
    public static final int NO_CURRENT_MATCH = -1;

    private final TextFlow flow = new TextFlow();
    private final Group marks = new Group();
    private String runStyle = "";
    private List<TextRange> matches = List.of();
    private int current = NO_CURRENT_MATCH;

    public MarkedText() {
        getStyleClass().add("marked-text");
        marks.setManaged(false);
        getChildren().addAll(marks, flow);
    }

    public String getRunStyle() {
        return runStyle;
    }

    public void setRunStyle(String runStyle) {
        this.runStyle = Objects.requireNonNull(runStyle);
    }

    public void setLineSpacing(double spacing) {
        flow.setLineSpacing(spacing);
    }

    public double getLineSpacing() {
        return flow.getLineSpacing();
    }

    public void setContent(String text, List<TextRange> matches, int current) {
        this.matches = List.copyOf(matches);
        this.current = current;
        flow.getChildren().setAll(runs(text));
        requestLayout();
    }

    public void setContent(String text) {
        setContent(text, List.of(), NO_CURRENT_MATCH);
    }

    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();
        double width = getWidth() - insets.getLeft() - insets.getRight();
        flow.resizeRelocate(insets.getLeft(), insets.getTop(), width, flow.prefHeight(width));
        flow.layout();

        marks.setLayoutX(insets.getLeft());
        marks.setLayoutY(insets.getTop());
        marks.getChildren().setAll(IntStream.range(0, matches.size()).mapToObj(this::mark).toList());
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets insets = getInsets();
        return insets.getLeft() + flow.prefWidth(-1) + insets.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets insets = getInsets();
        double available = width >= 0 ? width : getPrefWidth();
        double inner = available > 0 ? available - insets.getLeft() - insets.getRight() : -1;
        return insets.getTop() + flow.prefHeight(inner) + insets.getBottom();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    private Path mark(int index) {
        TextRange match = matches.get(index);
        var mark = new Path(flow.getRangeShape(match.start(), match.end(), false));
        mark.getStyleClass().add(index == current ? "mark-current" : "mark");
        return mark;
    }

    private List<Text> runs(String text) {
        List<Text> runs = new ArrayList<>();
        int cursor = 0;
        for (int i = 0; i < matches.size(); i++) {
            TextRange match = matches.get(i);
            if (match.start() > cursor) runs.add(run(text.substring(cursor, match.start())));
            Text marked = run(text.substring(match.start(), match.end()));
            marked.getStyleClass().add(i == current ? "match-current" : "match");
            runs.add(marked);
            cursor = match.end();
        }
        if (cursor < text.length()) runs.add(run(text.substring(cursor)));
        return runs;
    }

    private Text run(String content) {
        var run = new Text(content);
        if (!runStyle.isEmpty()) run.getStyleClass().add(runStyle);
        return run;
    }
}
