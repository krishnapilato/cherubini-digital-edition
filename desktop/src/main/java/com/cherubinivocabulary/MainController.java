package com.cherubinivocabulary;

import module java.base;
import module javafx.controls;
import module javafx.fxml;
import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.ToggleSwitch;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.kordamp.ikonli.javafx.FontIcon;

public final class MainController {
    private static final String ALL = "*";
    private static final String SECONDARY = "secondary";
    private static final List<String> LETTERS = Stream.concat(Stream.of(ALL), "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(Character::toString)).toList();
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final int SNIPPET_LENGTH = 110;
    private static final int NO_OCCURRENCE = -1;

    private enum Mode {LENTE, ATLANTE, STRUMENTO}

    private final List<Lemma> lemmas;
    private final History history;
    private final HostServices host;
    private final Map<String, Long> counts;
    private final ObservableList<Lemma> shown = FXCollections.observableArrayList();
    private final ObservableSet<Long> toPrint = FXCollections.observableSet(new LinkedHashSet<>());
    private final StringProperty query = new SimpleStringProperty("");
    private final BooleanProperty contained = new SimpleBooleanProperty(true);
    private final PauseTransition typingPause = new PauseTransition(Duration.millis(120));
    private final StyleClassedTextArea definition = new StyleClassedTextArea();
    private final Map<String, Button> tiles = new HashMap<>();
    private Mode mode;
    private boolean browsing;
    private String letter = ALL;
    private Search.Query search = Search.Query.of("", false);
    private List<Lemma> inLemmas = List.of();
    private List<Lemma> inDefinitions = List.of();
    private int occurrence = NO_OCCURRENCE;

    @FXML
    private VBox lente;
    @FXML
    private StackPane root;
    @FXML
    private HBox lenteActions;
    @FXML
    private CustomTextField bigSearch;
    @FXML
    private ToggleSwitch bigContained;
    @FXML
    private FlowPane recentPane;
    @FXML
    private FlowPane letterPane;
    @FXML
    private Label lentePrintBadge;
    @FXML
    private BorderPane workspace;
    @FXML
    private CustomTextField barSearch;
    @FXML
    private Button clearButton;
    @FXML
    private ToggleSwitch barContained;
    @FXML
    private Label barPrintBadge;
    @FXML
    private Label wordsWarning;
    @FXML
    private ScrollPane lettersCard;
    @FXML
    private GridPane letterGrid;
    @FXML
    private HBox atlanteHeader;
    @FXML
    private Label listTitle;
    @FXML
    private Label listCount;
    @FXML
    private VBox searchHeader;
    @FXML
    private ToggleGroup resultTabs;
    @FXML
    private ToggleButton lemmasTab;
    @FXML
    private ToggleButton definitionsTab;
    @FXML
    private Label searchSummary;
    @FXML
    private ListView<Lemma> lemmaList;
    @FXML
    private Button firstButton;
    @FXML
    private Button previousButton;
    @FXML
    private Label positionPrefix;
    @FXML
    private TextField positionField;
    @FXML
    private Label positionTotal;
    @FXML
    private Button nextButton;
    @FXML
    private Button lastButton;
    @FXML
    private HBox occurrenceBox;
    @FXML
    private Label occurrenceLabel;
    @FXML
    private Button previousOccurrenceButton;
    @FXML
    private Button nextOccurrenceButton;
    @FXML
    private Label lemmaNumber;
    @FXML
    private TextFlow lemmaTitle;
    @FXML
    private StackPane definitionHolder;
    @FXML
    private CheckBox selectedToPrint;
    @FXML
    private Label readingHint;
    @FXML
    private Label statusLabel;
    @FXML
    private Label toast;
    @FXML
    private ModalPane modal;
    @FXML
    private VBox abbreviationsPanel;
    @FXML
    private ToggleGroup abbreviationCategories;
    @FXML
    private Label abbreviationsMessage;
    @FXML
    private VBox printPanel;
    @FXML
    private Label printSubtitle;
    @FXML
    private ListView<Lemma> printList;
    @FXML
    private Button selectAllFoundButton;
    @FXML
    private Button clearPrintButton;
    @FXML
    private Button printNowButton;

    public MainController(List<Lemma> lemmas, History history, HostServices host) {
        this.lemmas = lemmas;
        this.history = history;
        this.host = host;
        this.counts = lemmas.stream().collect(Collectors.groupingBy(Lemma::initial, Collectors.counting()));
    }

    @FXML
    private void initialize() {
        bigSearch.textProperty().bindBidirectional(query);
        barSearch.textProperty().bindBidirectional(query);
        bigContained.selectedProperty().bindBidirectional(contained);
        barContained.selectedProperty().bindBidirectional(contained);
        query.addListener((_, _, _) -> typingPause.playFromStart());
        typingPause.setOnFinished(_ -> refresh());
        contained.addListener((_, _, _) -> refresh());
        bigSearch.setOnKeyPressed(this::onSearchKey);
        barSearch.setOnKeyPressed(this::onSearchKey);
        clearButton.visibleProperty().bind(query.isNotEmpty());

        definition.setEditable(false);
        definition.setWrapText(true);
        definition.getStyleClass().add("definition");
        definitionHolder.getChildren().add(new VirtualizedScrollPane<>(definition));

        lemmaList.setItems(shown);
        lemmaList.setCellFactory(_ -> new LemmaCell());
        lemmaList.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> showSelected());
        resultTabs.selectedToggleProperty().addListener((_, before, now) -> {
            if (now == null) resultTabs.selectToggle(before);
            else showResults();
        });
        positionField.setOnAction(_ -> jumpToTypedPosition());

        toPrint.addListener((SetChangeListener<Long>) _ -> refreshPrint());
        printList.setCellFactory(_ -> new PrintCell());
        printList.setPlaceholder(wrapped("Nessuna voce scelta. Spunta «Da stampare» sulle voci trovate o nella voce aperta."));
        abbreviationCategories.selectedToggleProperty().addListener((_, before, now) -> {
            if (now == null) abbreviationCategories.selectToggle(before);
            else showAbbreviationCategory();
        });
        modal.setAlignment(Pos.TOP_RIGHT);
        modal.usePredefinedTransitionFactories(Side.RIGHT);

        lemmaList.sceneProperty().addListener((_, _, scene) -> {
            if (scene != null) scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F3), this::nextOccurrence);
        });
        statusLabel.setText(format(lemmas.size()) + " voci · sola consultazione");
        buildLetters();
        refreshRecent();
        refreshPrint();
        showAbbreviationCategory();
        refresh();
        // JavaFX would focus the search field, which hides its placeholder: the page takes the focus instead.
        Platform.runLater(root::requestFocus);
        // As on a web page, clicking outside a text field takes the focus away from it.
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!(event.getTarget() instanceof Node target && insideTextField(target))) root.requestFocus();
        });
    }

    private static boolean insideTextField(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current instanceof TextInputControl) return true;
        }
        return false;
    }

    private void refresh() {
        search = Search.Query.of(query.get(), !contained.get());
        Mode next = nextMode();
        switchTo(next);
        if (next == Mode.ATLANTE) showLetter();
        else if (next == Mode.STRUMENTO) showSearch();
    }

    private Mode nextMode() {
        if (!search.isEmpty()) return Mode.STRUMENTO;
        return browsing ? Mode.ATLANTE : Mode.LENTE;
    }

    private void switchTo(Mode next) {
        if (next == mode) return;
        Mode before = mode;
        mode = next;
        show(lente, next == Mode.LENTE);
        show(lenteActions, next == Mode.LENTE);
        show(workspace, next != Mode.LENTE);
        show(lettersCard, next == Mode.ATLANTE);
        show(atlanteHeader, next == Mode.ATLANTE);
        show(searchHeader, next == Mode.STRUMENTO);
        show(occurrenceBox, next == Mode.STRUMENTO);
        show(wordsWarning, false);
        readingHint.setText(next == Mode.STRUMENTO ? "F3 · occorrenza successiva" : "Frecce · voce precedente e successiva");
        fadeIn(next == Mode.LENTE ? lente : workspace);
        if (before == Mode.LENTE && next == Mode.STRUMENTO) focusAtEnd(barSearch);
        if (next == Mode.LENTE && before != null) focusAtEnd(bigSearch);
    }

    private void showLetter() {
        List<Lemma> list = ALL.equals(letter) ? lemmas : lemmas.stream().filter(lemma -> lemma.initial().equals(letter)).toList();
        listTitle.setText(ALL.equals(letter) ? "Tutti i lemmi" : letter);
        listCount.setText(format(list.size()) + " lemmi");
        tiles.forEach((key, tile) -> tile.pseudoClassStateChanged(SELECTED, key.equals(letter)));
        lemmaList.setPlaceholder(wrapped("Nessun lemma per questa lettera."));
        showList(list);
    }

    private void showSearch() {
        inLemmas = lemmas.stream().filter(lemma -> Search.matches(lemma.searchableLemma(), search)).toList();
        inDefinitions = lemmas.stream().filter(lemma -> Search.matches(lemma.searchableDefinition(), search)).toList();
        lemmasTab.setText("Lemmi   " + format(inLemmas.size()));
        definitionsTab.setText("Definizioni   " + format(inDefinitions.size()));
        lemmasTab.setDisable(inLemmas.isEmpty());
        definitionsTab.setDisable(inDefinitions.isEmpty());
        searchSummary.setText(search.describe());
        wordsWarning.setText("Si cercano al massimo quattro parole: «" + String.join(" ", search.ignored()) + "» non viene considerato.");
        show(wordsWarning, !search.ignored().isEmpty());
        lemmaList.setPlaceholder(wrapped(search.exact() ? "Nessuna voce trovata. Prova ad attivare «Parole contenute»." : "Nessuna voce trovata. Controlla l'ortografia o usa meno parole."));
        if (lemmasTab.isSelected() && inLemmas.isEmpty() && !inDefinitions.isEmpty())
            resultTabs.selectToggle(definitionsTab);
        if (definitionsTab.isSelected() && inDefinitions.isEmpty() && !inLemmas.isEmpty())
            resultTabs.selectToggle(lemmasTab);
        showResults();
    }

    private void showResults() {
        if (mode == Mode.STRUMENTO) showList(lemmasTab.isSelected() ? inLemmas : inDefinitions);
    }

    private void showList(List<Lemma> list) {
        Lemma selected = lemmaList.getSelectionModel().getSelectedItem();
        shown.setAll(list);
        if (!shown.isEmpty()) select(Math.max(0, shown.indexOf(selected)));
        showSelected();
        lemmaList.refresh();
        refreshPrint();
    }

    private void showSelected() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        int index = lemmaList.getSelectionModel().getSelectedIndex();
        int total = shown.size();
        occurrence = NO_OCCURRENCE;
        positionPrefix.setText(mode == Mode.STRUMENTO ? "Voce" : "Lemma");
        positionField.setText(index >= 0 ? String.valueOf(index + 1) : "");
        positionTotal.setText("di " + format(total));
        firstButton.setDisable(index <= 0);
        previousButton.setDisable(index <= 0);
        nextButton.setDisable(index < 0 || index >= total - 1);
        lastButton.setDisable(index < 0 || index >= total - 1);
        selectedToPrint.setDisable(lemma == null);
        if (lemma == null) {
            lemmaNumber.setText("");
            lemmaTitle.getChildren().setAll(new Text("Nessuna voce"));
            definition.replaceText("");
            occurrenceLabel.setText("");
            return;
        }
        lemmaNumber.setText("N° " + lemma.id() + " · lettera " + lemma.initial());
        lemmaTitle.getChildren().setAll(texts(lemma.lemma(), matchesIn(lemma.searchableLemma())));
        selectedToPrint.setSelected(toPrint.contains(lemma.id()));
        showDefinition(lemma);
        if (mode == Mode.STRUMENTO && lemmaList.isFocused()) remember();
    }

    private void showDefinition(Lemma lemma) {
        List<Search.Range> ranges = matchesIn(lemma.searchableDefinition());
        definition.replaceText(lemma.definition());
        if (!lemma.definition().isEmpty())
            definition.setStyleSpans(0, highlighted(lemma.definition().length(), ranges));
        if (occurrence >= 0) {
            definition.moveTo(ranges.get(occurrence).start());
            definition.requestFollowCaret();
        } else {
            definition.moveTo(0);
            definition.showParagraphAtTop(0);
        }
        occurrenceLabel.setText(occurrenceText(ranges.size()));
        previousOccurrenceButton.setDisable(ranges.isEmpty());
        nextOccurrenceButton.setDisable(ranges.isEmpty());
    }

    private String occurrenceText(int count) {
        if (count == 0) return "Nessuna occorrenza";
        if (occurrence < 0) return quantity(count, "occorrenza", "occorrenze");
        return "Occorrenza " + (occurrence + 1) + " di " + count;
    }

    private static String quantity(long count, String one, String many) {
        return count + " " + (count == 1 ? one : many);
    }

    private List<Search.Range> matchesIn(String searchableText) {
        return mode == Mode.STRUMENTO ? Search.find(searchableText, search) : List.of();
    }

    @FXML
    private void nextOccurrence() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma == null || mode != Mode.STRUMENTO) return;
        int count = matchesIn(lemma.searchableDefinition()).size();
        if (occurrence + 1 < count) {
            occurrence++;
            showDefinition(lemma);
            return;
        }
        int index = lemmaList.getSelectionModel().getSelectedIndex();
        OptionalInt following = IntStream.range(index + 1, shown.size())
                .filter(candidate -> !matchesIn(shown.get(candidate).searchableDefinition()).isEmpty())
                .findFirst();
        if (following.isEmpty()) return;
        select(following.getAsInt());
        occurrence = 0;
        showDefinition(shown.get(following.getAsInt()));
    }

    @FXML
    private void previousOccurrence() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma == null) return;
        int count = matchesIn(lemma.searchableDefinition()).size();
        if (count == 0) return;
        occurrence = occurrence <= 0 ? count - 1 : occurrence - 1;
        showDefinition(lemma);
    }

    @FXML
    private void showFirst() {
        select(0);
    }

    @FXML
    private void showPrevious() {
        select(lemmaList.getSelectionModel().getSelectedIndex() - 1);
    }

    @FXML
    private void showNext() {
        select(lemmaList.getSelectionModel().getSelectedIndex() + 1);
    }

    @FXML
    private void showLast() {
        select(shown.size() - 1);
    }

    private void jumpToTypedPosition() {
        int position = typedPosition();
        if (position >= 1 && position <= shown.size()) select(position - 1);
        else showSelected();
    }

    private int typedPosition() {
        try {
            return Integer.parseInt(positionField.getText().strip());
        } catch (NumberFormatException _) {
            return 0;
        }
    }

    private void select(int index) {
        if (shown.isEmpty()) return;
        int target = Math.clamp(index, 0, shown.size() - 1);
        lemmaList.getSelectionModel().clearAndSelect(target);
        lemmaList.scrollTo(Math.max(0, target - 2));
    }

    private void onSearchKey(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.ENTER) remember();
        else if (code == KeyCode.ESCAPE) query.set("");
        else if (code == KeyCode.DOWN && mode != Mode.LENTE) lemmaList.requestFocus();
    }

    private void remember() {
        if (search.isEmpty()) return;
        history.remember(query.get());
        refreshRecent();
    }

    @FXML
    private void clearSearch() {
        query.set("");
    }

    @FXML
    private void goHome() {
        browsing = false;
        query.set("");
        refresh();
    }

    private void openLetter(String chosen) {
        letter = chosen;
        browsing = true;
        query.set("");
        refresh();
    }

    private void buildLetters() {
        for (int i = 0; i < LETTERS.size(); i++) {
            String key = LETTERS.get(i);
            long count = ALL.equals(key) ? lemmas.size() : counts.getOrDefault(key, 0L);
            String description = describeLetter(key, count);

            var pill = new Button(key);
            pill.getStyleClass().add("pill");
            pill.setDisable(count == 0);
            pill.setTooltip(new Tooltip(description));
            pill.setOnAction(_ -> openLetter(key));
            letterPane.getChildren().add(pill);

            var name = new Label(key);
            name.getStyleClass().add("tile-letter");
            var amount = new Label(format(count));
            amount.getStyleClass().add("tile-count");
            var face = new VBox(name, amount);
            face.setAlignment(Pos.CENTER);
            var tile = new Button(null, face);
            tile.getStyleClass().add("tile");
            tile.setDisable(count == 0);
            tile.setTooltip(new Tooltip(description));
            tile.setOnAction(_ -> openLetter(key));
            tile.setMaxWidth(Double.MAX_VALUE);
            letterGrid.add(tile, i % 2, i / 2);
            tiles.put(key, tile);
        }
        IntStream.range(0, 2).forEach(_ -> {
            var column = new ColumnConstraints();
            column.setPercentWidth(50);
            letterGrid.getColumnConstraints().add(column);
        });
    }

    private static String describeLetter(String key, long count) {
        if (ALL.equals(key)) return "Tutti i lemmi";
        return key + ": " + (count == 0 ? "nessun lemma" : format(count) + " lemmi");
    }

    private void refreshRecent() {
        List<String> recent = history.recent();
        recentPane.getChildren().clear();
        show(recentPane, !recent.isEmpty());
        if (recent.isEmpty()) return;
        var label = new Label("Recenti");
        label.getStyleClass().add(SECONDARY);
        recentPane.getChildren().add(label);
        recent.forEach(text -> {
            var repeat = new Button(text, new FontIcon("fth-clock"));
            repeat.getStyleClass().add("chip-search");
            repeat.setOnAction(_ -> {
                query.set(text);
                refresh();
                remember();
            });
            var remove = new Button(null, new FontIcon("fth-x"));
            remove.getStyleClass().add("chip-remove");
            remove.setTooltip(new Tooltip("Togli dalle recenti"));
            remove.setAccessibleText("Togli «" + text + "» dalle recenti");
            var chip = new HBox(repeat, remove);
            chip.getStyleClass().add("chip");
            chip.setAlignment(Pos.CENTER_LEFT);
            remove.setOnAction(_ -> {
                var fade = new FadeTransition(Duration.millis(180), chip);
                fade.setToValue(0);
                var shrink = new ScaleTransition(Duration.millis(180), chip);
                shrink.setToX(0.6);
                shrink.setToY(0.6);
                var vanish = new ParallelTransition(fade, shrink);
                vanish.setOnFinished(_ -> {
                    history.forget(text);
                    refreshRecent();
                });
                vanish.play();
            });
            recentPane.getChildren().add(chip);
        });
    }

    @FXML
    private void toggleSelectedToPrint() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma != null) togglePrint(lemma);
    }

    private void togglePrint(Lemma lemma) {
        if (!toPrint.remove(lemma.id())) toPrint.add(lemma.id());
    }

    private void refreshPrint() {
        int count = toPrint.size();
        List<Lemma> chosen = lemmas.stream().filter(lemma -> toPrint.contains(lemma.id())).toList();
        printList.getItems().setAll(chosen);
        printSubtitle.setText(count == 0 ? "Nessuna voce scelta" : quantity(count, "voce scelta", "voci scelte"));
        printNowButton.setText(count == 0 ? "Stampa" : "Stampa " + quantity(count, "voce", "voci"));
        printNowButton.setDisable(count == 0);
        clearPrintButton.setDisable(count == 0);
        selectAllFoundButton.setDisable(mode != Mode.STRUMENTO || shown.isEmpty());
        Stream.of(lentePrintBadge, barPrintBadge).forEach(badge -> {
            badge.setText(String.valueOf(count));
            show(badge, count > 0);
        });
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        selectedToPrint.setSelected(lemma != null && toPrint.contains(lemma.id()));
        lemmaList.refresh();
    }

    @FXML
    private void selectAllFound() {
        shown.forEach(lemma -> toPrint.add(lemma.id()));
    }

    @FXML
    private void clearPrint() {
        toPrint.clear();
    }

    @FXML
    private void print() {
        List<Lemma> chosen = lemmas.stream().filter(lemma -> toPrint.contains(lemma.id())).toList();
        if (chosen.isEmpty()) return;
        closePanel();
        showToast(Printing.print(chosen, lemmaList.getScene().getWindow()));
    }

    @FXML
    private void openAbbreviations() {
        modal.show(abbreviationsPanel);
    }

    @FXML
    private void openPrint() {
        refreshPrint();
        modal.show(printPanel);
    }

    @FXML
    private void closePanel() {
        modal.hide(true);
    }

    @FXML
    private void openHelp() {
        Help.open(host);
    }

    private void showAbbreviationCategory() {
        Object category = abbreviationCategories.getSelectedToggle().getUserData();
        abbreviationsMessage.setText("Le abbreviazioni del Cherubini (" + category + ") compariranno qui con la loro spiegazione e la ricerca nel vocabolario.");
    }

    private void showToast(String message) {
        toast.setText(message);
        toast.setOpacity(0);
        toast.setVisible(true);
        var appear = new FadeTransition(Duration.millis(160), toast);
        appear.setToValue(1);
        var disappear = new FadeTransition(Duration.millis(400), toast);
        disappear.setToValue(0);
        var animation = new SequentialTransition(appear, new PauseTransition(Duration.seconds(2.6)), disappear);
        animation.setOnFinished(_ -> toast.setVisible(false));
        animation.play();
    }

    private static List<Text> texts(String text, List<Search.Range> ranges) {
        List<Text> parts = new ArrayList<>();
        int cursor = 0;
        for (Search.Range range : ranges) {
            if (range.start() > cursor) parts.add(new Text(text.substring(cursor, range.start())));
            var match = new Text(text.substring(range.start(), range.end()));
            match.getStyleClass().add("match");
            parts.add(match);
            cursor = range.end();
        }
        if (cursor < text.length()) parts.add(new Text(text.substring(cursor)));
        return parts;
    }

    private StyleSpans<Collection<String>> highlighted(int length, List<Search.Range> ranges) {
        var spans = new StyleSpansBuilder<Collection<String>>();
        int cursor = 0;
        for (int i = 0; i < ranges.size(); i++) {
            Search.Range range = ranges.get(i);
            spans.add(List.of(), range.start() - cursor);
            spans.add(List.of(i == occurrence ? "hit-current" : "hit"), range.end() - range.start());
            cursor = range.end();
        }
        spans.add(List.of(), length - cursor);
        return spans.create();
    }

    private static Label wrapped(String text) {
        var label = new Label(text);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.getStyleClass().add(SECONDARY);
        return label;
    }

    private static void focusAtEnd(TextField field) {
        Platform.runLater(() -> {
            field.requestFocus();
            field.end();
        });
    }

    private static void fadeIn(Node node) {
        node.setOpacity(0);
        var fade = new FadeTransition(Duration.millis(220), node);
        fade.setToValue(1);
        fade.play();
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static String format(long number) {
        return String.format(Locale.ITALIAN, "%,d", number);
    }

    private final class LemmaCell extends ListCell<Lemma> {
        private final CheckBox printBox = new CheckBox();
        private final TextFlow title = new TextFlow();
        private final TextFlow snippet = new TextFlow();
        private final HBox content;

        LemmaCell() {
            title.getStyleClass().add("list-lemma");
            snippet.getStyleClass().add("list-snippet");
            title.prefWidthProperty().bind(lemmaList.widthProperty().subtract(100));
            snippet.prefWidthProperty().bind(lemmaList.widthProperty().subtract(100));
            printBox.setOnAction(_ -> togglePrint(getItem()));
            printBox.setTooltip(new Tooltip("Da stampare"));
            content = new HBox(10, printBox, new VBox(2, title, snippet));
        }

        @Override
        protected void updateItem(Lemma lemma, boolean empty) {
            super.updateItem(lemma, empty);
            if (empty || lemma == null) {
                setGraphic(null);
                return;
            }
            show(printBox, mode == Mode.STRUMENTO);
            printBox.setSelected(toPrint.contains(lemma.id()));
            title.getChildren().setAll(texts(lemma.lemma(), matchesIn(lemma.searchableLemma())));
            List<Search.Range> ranges = matchesIn(lemma.searchableDefinition());
            int start = ranges.isEmpty() ? 0 : Math.max(0, ranges.getFirst().start() - 24);
            int end = Math.min(lemma.definition().length(), start + SNIPPET_LENGTH);
            List<Search.Range> visible = ranges.stream()
                    .filter(range -> range.start() >= start && range.end() <= end)
                    .map(range -> new Search.Range(range.start() - start, range.end() - start))
                    .toList();
            List<Text> parts = new ArrayList<>(texts(lemma.definition().substring(start, end), visible));
            if (start > 0) parts.addFirst(new Text("…"));
            if (end < lemma.definition().length()) parts.add(new Text("…"));
            snippet.getChildren().setAll(parts);
            setGraphic(content);
        }
    }

    private final class PrintCell extends ListCell<Lemma> {
        @Override
        protected void updateItem(Lemma lemma, boolean empty) {
            super.updateItem(lemma, empty);
            if (empty || lemma == null) {
                setGraphic(null);
                return;
            }
            var name = new Label(lemma.lemma());
            name.getStyleClass().add("print-lemma");
            var number = new Label("N° " + lemma.id());
            number.getStyleClass().addAll(SECONDARY, "small");
            var texts = new VBox(name, number);
            HBox.setHgrow(texts, Priority.ALWAYS);
            var remove = new Button(null, new FontIcon("fth-x"));
            remove.getStyleClass().addAll("flat", "button-icon");
            remove.setTooltip(new Tooltip("Togli dalla stampa"));
            remove.setOnAction(_ -> toPrint.remove(lemma.id()));
            var row = new HBox(10, texts, remove);
            row.setAlignment(Pos.CENTER_LEFT);
            setGraphic(row);
        }
    }
}
