package com.cherubinivocabulary;

import module java.base;
import module javafx.controls;
import module javafx.fxml;
import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.ToggleSwitch;
import com.cherubinivocabulary.view.EntryView;
import com.cherubinivocabulary.view.LemmaCell;
import com.cherubinivocabulary.view.LetterIndex;
import com.cherubinivocabulary.view.PrintSelection;
import com.cherubinivocabulary.view.RecentSearches;
import com.cherubinivocabulary.view.RowLayout;
import com.cherubinivocabulary.view.Toast;
import com.cherubinivocabulary.view.Ui;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

/**
 * Directs the window: which page is shown, what the list holds and which entry is open. The
 * pieces of the interface live in the view package.
 */
public final class MainController {
    private enum Mode {LENTE, ATLANTE, STRUMENTO}

    private final List<Lemma> lemmas;
    private final History history;
    private final HostServices host;
    private final LetterIndex letters;
    private final ObservableList<Lemma> shown = FXCollections.observableArrayList();
    private final StringProperty query = new SimpleStringProperty("");
    private final BooleanProperty contained = new SimpleBooleanProperty(true);
    private final PauseTransition typingPause = new PauseTransition(Duration.millis(120));
    private Mode mode;
    private boolean browsing;
    private String letter = LetterIndex.ALL;
    private Search.Query search = Search.Query.of("", false);
    private List<Lemma> inLemmas = List.of();
    private List<Lemma> inDefinitions = List.of();
    private RecentSearches recent;
    private PrintSelection toPrint;
    private EntryView entry;

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
    private HBox readingToolbar;
    @FXML
    private HBox readingFooter;
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
        this.letters = new LetterIndex(lemmas);
    }

    @FXML
    private void initialize() {
        bindSearch();
        entry = new EntryView(definitionHolder, lemmaNumber, lemmaTitle, occurrenceLabel, List.of(previousOccurrenceButton, nextOccurrenceButton));
        toPrint = new PrintSelection(lemmas, printList, printSubtitle, printNowButton, clearPrintButton, List.of(lentePrintBadge, barPrintBadge));
        toPrint.onChange(this::refreshPrint);
        recent = new RecentSearches(recentPane, history, this::repeat);
        setUpList();
        modal.setAlignment(Pos.TOP_RIGHT);
        modal.usePredefinedTransitionFactories(Side.RIGHT);

        lemmaList.sceneProperty().addListener((_, _, scene) -> {
            if (scene == null) return;
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F3), this::nextOccurrence);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F3, KeyCombination.SHIFT_DOWN), this::previousOccurrence);
        });
        statusLabel.setText(Ui.format(lemmas.size()) + " voci · sola consultazione");
        letters.build(letterPane, letterGrid, this::openLetter);
        recent.refresh();
        refreshPrint();
        refresh();
        // JavaFX would focus the search field, which hides its placeholder: the page takes the focus instead.
        Platform.runLater(root::requestFocus);
        // As on a web page, clicking outside a text field takes the focus away from it.
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!(event.getTarget() instanceof Node target && Ui.insideTextField(target))) root.requestFocus();
        });
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::onArrowKey);
    }

    private void bindSearch() {
        bigSearch.textProperty().bindBidirectional(query);
        barSearch.textProperty().bindBidirectional(query);
        bigContained.selectedProperty().bindBidirectional(contained);
        barContained.selectedProperty().bindBidirectional(contained);
        query.addListener((_, _, _) -> typingPause.playFromStart());
        typingPause.setOnFinished(_ -> refresh());
        contained.addListener((_, _, _) -> refresh());
        bigSearch.setOnKeyPressed(this::onSearchKey);
        barSearch.setOnKeyPressed(this::onSearchKey);
        bigSearch.setTextFormatter(new TextFormatter<>(MainController::withinMaxLength));
        barSearch.setTextFormatter(new TextFormatter<>(MainController::withinMaxLength));
        clearButton.visibleProperty().bind(query.isNotEmpty());
    }

    private void setUpList() {
        var layout = new RowLayout(lemmaList);
        lemmaList.setItems(shown);
        lemmaList.setFixedCellSize(layout.cellHeight());
        lemmaList.setCellFactory(list -> new LemmaCell(list, layout, toPrint, this::matchesIn, () -> mode == Mode.STRUMENTO));
        // The excerpts are cut to the width of the list.
        lemmaList.widthProperty().addListener((_, _, _) -> lemmaList.refresh());
        lemmaList.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> showSelected());
        resultTabs.selectedToggleProperty().addListener((_, before, now) -> {
            if (now == null) resultTabs.selectToggle(before);
            else showResults(true);
        });
        positionField.setOnAction(_ -> jumpToTypedPosition());
    }

    // As the hint under the entry says, the arrows move to the previous or next entry from
    // anywhere; the list and the text fields keep their own handling.
    private void onArrowKey(KeyEvent event) {
        var step = switch (event.getCode()) {
            case UP -> -1;
            case DOWN -> 1;
            default -> 0;
        };
        if (step == 0 || mode == Mode.LENTE || modal.isDisplay() || lemmaList.isFocused()) return;
        var focused = root.getScene().getFocusOwner();
        if (focused != null && Ui.insideTextField(focused)) return;
        event.consume();
        select(lemmaList.getSelectionModel().getSelectedIndex() + step);
    }

    // As maxlength on the web: what goes past the limit is left out, without a message.
    private static TextFormatter.Change withinMaxLength(TextFormatter.Change change) {
        var excess = change.getControlNewText().length() - Search.MAX_LENGTH;
        var text = change.getText();
        if (excess > 0) change.setText(text.substring(0, Math.max(0, text.length() - excess)));
        return change;
    }

    private void refresh() {
        search = Search.Query.of(query.get(), !contained.get());
        var next = nextMode();
        switchTo(next);
        if (next == Mode.ATLANTE) showLetter();
        else if (next == Mode.STRUMENTO) showSearch();
    }

    private Mode nextMode() {
        if (search.hasWords()) return Mode.STRUMENTO;
        return browsing ? Mode.ATLANTE : Mode.LENTE;
    }

    private void switchTo(Mode next) {
        if (next == mode) return;
        var before = mode;
        mode = next;
        // focusAtEnd runs later, once the new mode is on screen.
        if (before == Mode.LENTE && next == Mode.STRUMENTO) Ui.focusAtEnd(barSearch);
        if (next == Mode.LENTE && before != null) Ui.focusAtEnd(bigSearch);
        Ui.show(lente, next == Mode.LENTE);
        Ui.show(lenteActions, next == Mode.LENTE);
        Ui.show(workspace, next != Mode.LENTE);
        Ui.show(lettersCard, next == Mode.ATLANTE);
        Ui.show(atlanteHeader, next == Mode.ATLANTE);
        Ui.show(searchHeader, next == Mode.STRUMENTO);
        Ui.show(occurrenceBox, next == Mode.STRUMENTO);
        Ui.show(wordsWarning, false);
        readingHint.setText(next == Mode.STRUMENTO ? "F3 · occorrenza successiva" : "Frecce · voce precedente e successiva");
        Ui.fadeIn(next == Mode.LENTE ? lente : workspace);
    }

    private void showLetter() {
        var list = letters.lemmasOf(letter);
        listTitle.setText(LetterIndex.title(letter));
        listCount.setText(Ui.quantity(list.size(), "lemma", "lemmi"));
        letters.select(letter);
        lemmaList.setPlaceholder(Ui.wrapped("Nessun lemma per questa lettera."));
        showList(list, false);
    }

    private void showSearch() {
        inLemmas = lemmas.stream().filter(lemma -> Search.matches(lemma.searchableLemma(), search)).toList();
        inDefinitions = lemmas.stream().filter(lemma -> Search.matches(lemma.searchableDefinition(), search)).toList();
        lemmasTab.setText("Lemmi   " + Ui.format(inLemmas.size()));
        definitionsTab.setText("Definizioni   " + Ui.format(inDefinitions.size()));
        lemmasTab.setDisable(inLemmas.isEmpty());
        definitionsTab.setDisable(inDefinitions.isEmpty());
        searchSummary.setText(search.describe());
        wordsWarning.setText("Si cercano al massimo quattro parole: «" + String.join(" ", search.ignored()) + "» non viene considerato.");
        Ui.show(wordsWarning, !search.ignored().isEmpty());
        lemmaList.setPlaceholder(search.exact() ? containedOffer() : Ui.wrapped("Nessuna voce trovata. Controlla l'ortografia o usa meno parole."));
        if (lemmasTab.isSelected() && inLemmas.isEmpty() && !inDefinitions.isEmpty())
            resultTabs.selectToggle(definitionsTab);
        if (definitionsTab.isSelected() && inDefinitions.isEmpty() && !inLemmas.isEmpty())
            resultTabs.selectToggle(lemmasTab);
        showResults(false);
    }

    private Node containedOffer() {
        var accept = new Button("Attiva «Parole contenute»");
        accept.setId("containedOffer");
        accept.getStyleClass().add("accent");
        accept.setOnAction(_ -> contained.set(true));
        var offer = new VBox(10, Ui.wrapped("Nessuna voce trovata con le parole esatte."), accept);
        offer.setAlignment(Pos.CENTER);
        return offer;
    }

    private void showResults(boolean keepSelected) {
        if (mode == Mode.STRUMENTO) showList(lemmasTab.isSelected() ? inLemmas : inDefinitions, keepSelected);
    }

    // A new search or letter starts from its first entry; switching between lemmas and
    // definitions keeps the open entry when the other tab has it too.
    private void showList(List<Lemma> list, boolean keepSelected) {
        var selected = lemmaList.getSelectionModel().getSelectedItem();
        shown.setAll(list);
        if (!shown.isEmpty()) select(keepSelected ? Math.max(0, shown.indexOf(selected)) : 0);
        showSelected();
        lemmaList.refresh();
        refreshPrint();
    }

    private void showSelected() {
        var index = lemmaList.getSelectionModel().getSelectedIndex();
        var total = shown.size();
        positionPrefix.setText(mode == Mode.STRUMENTO ? "Voce" : "Lemma");
        positionField.setText(index >= 0 ? String.valueOf(index + 1) : "");
        positionTotal.setText("di " + Ui.format(total));
        firstButton.setDisable(index <= 0);
        previousButton.setDisable(index <= 0);
        nextButton.setDisable(index < 0 || index >= total - 1);
        lastButton.setDisable(index < 0 || index >= total - 1);
        // Without results there is nothing to move through, print or highlight.
        var lemma = lemmaList.getSelectionModel().getSelectedItem();
        Ui.show(readingToolbar, lemma != null);
        Ui.show(readingFooter, lemma != null);
        if (lemma == null) {
            entry.clear();
            return;
        }
        entry.show(lemma, matchesIn(lemma.searchableLemma()), matchesIn(lemma.searchableDefinition()));
        selectedToPrint.setSelected(toPrint.contains(lemma));
        if (mode == Mode.STRUMENTO && lemmaList.isFocused()) remember();
    }

    private List<Search.Range> matchesIn(String searchableText) {
        return mode == Mode.STRUMENTO ? Search.find(searchableText, search) : List.of();
    }

    @FXML
    private void nextOccurrence() {
        var lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma == null || mode != Mode.STRUMENTO) return;
        var ranges = matchesIn(lemma.searchableDefinition());
        if (entry.currentOccurrence() + 1 < ranges.size()) {
            entry.showDefinition(lemma, ranges, entry.currentOccurrence() + 1);
            return;
        }
        var index = lemmaList.getSelectionModel().getSelectedIndex();
        var following = IntStream.range(index + 1, shown.size())
                .filter(candidate -> !matchesIn(shown.get(candidate).searchableDefinition()).isEmpty())
                .findFirst();
        if (following.isEmpty()) return;
        select(following.getAsInt());
        var next = shown.get(following.getAsInt());
        entry.showDefinition(next, matchesIn(next.searchableDefinition()), 0);
    }

    @FXML
    private void previousOccurrence() {
        var lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma == null) return;
        var ranges = matchesIn(lemma.searchableDefinition());
        if (ranges.isEmpty()) return;
        entry.showDefinition(lemma, ranges, entry.currentOccurrence() <= 0 ? ranges.size() - 1 : entry.currentOccurrence() - 1);
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
        var position = typedPosition();
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
        var target = Math.clamp(index, 0, shown.size() - 1);
        lemmaList.getSelectionModel().clearAndSelect(target);
        lemmaList.scrollTo(Math.max(0, target - 2));
    }

    private void onSearchKey(KeyEvent event) {
        var code = event.getCode();
        if (code == KeyCode.ENTER) remember();
        else if (code == KeyCode.ESCAPE) query.set("");
        else if (code == KeyCode.DOWN && mode != Mode.LENTE) lemmaList.requestFocus();
    }

    private void remember() {
        // The typed text rather than the last search: Enter can come before the search starts.
        if (Search.Query.of(query.get(), !contained.get()).hasWords()) recent.remember(query.get());
    }

    private void repeat(String recentSearch) {
        query.set(recentSearch);
        refresh();
        remember();
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

    @FXML
    private void toggleSelectedToPrint() {
        var lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma != null) toPrint.toggle(lemma);
    }

    private void refreshPrint() {
        toPrint.refresh();
        selectAllFoundButton.setDisable(mode != Mode.STRUMENTO || shown.isEmpty());
        var lemma = lemmaList.getSelectionModel().getSelectedItem();
        selectedToPrint.setSelected(lemma != null && toPrint.contains(lemma));
        lemmaList.refresh();
    }

    @FXML
    private void selectAllFound() {
        toPrint.addAll(shown);
    }

    @FXML
    private void clearPrint() {
        toPrint.clear();
    }

    @FXML
    private void print() {
        var chosen = toPrint.chosenLemmas();
        if (chosen.isEmpty()) return;
        closePanel();
        Toast.show(toast, Printing.print(chosen, lemmaList.getScene().getWindow()));
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
}
