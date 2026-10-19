package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;
import module javafx.fxml;

import com.cherubinivocabulary.cherubinivocabulary.data.SearchHistory;
import com.cherubinivocabulary.cherubinivocabulary.data.Vocabulary;
import com.cherubinivocabulary.cherubinivocabulary.model.Abbreviation;
import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;
import com.cherubinivocabulary.cherubinivocabulary.model.MatchMode;
import com.cherubinivocabulary.cherubinivocabulary.model.SearchQuery;
import com.cherubinivocabulary.cherubinivocabulary.model.SearchResult;
import com.cherubinivocabulary.cherubinivocabulary.model.TextRange;
import com.cherubinivocabulary.cherubinivocabulary.search.VocabularySearch;
import com.cherubinivocabulary.cherubinivocabulary.ui.SearchSuggestions.Section;
import com.cherubinivocabulary.cherubinivocabulary.ui.SearchSuggestions.Suggestion;
import javafx.scene.control.Label;
import javafx.util.Duration;

public final class MainController {
    private static final SearchResult NOTHING_FOUND = new SearchResult(List.of(), List.of());
    private static final Duration TYPING_PAUSE = Duration.millis(150);

    private final Vocabulary vocabulary;
    private final SearchHistory history;
    private final ObservableList<Lemma> shownLemmas = FXCollections.observableArrayList();
    private final ObservableSet<Long> toPrint = FXCollections.observableSet();
    private final PauseTransition typingPause = new PauseTransition(TYPING_PAUSE);
    private String letter = LetterStrip.ALL;
    private SearchQuery query;
    private SearchResult result = NOTHING_FOUND;
    private int occurrence = MarkedText.NO_CURRENT_MATCH;

    @FXML private HBox searchBox;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private CheckBox containedSwitch;
    @FXML private ToggleButton abbreviationsToggle;
    @FXML private Label wordsWarning;
    @FXML private VBox browseHeader;
    @FXML private LetterStrip letterStrip;
    @FXML private Label listTitle;
    @FXML private Label listCount;
    @FXML private VBox searchHeader;
    @FXML private ToggleGroup resultTabs;
    @FXML private ToggleButton lemmasTab;
    @FXML private ToggleButton definitionsTab;
    @FXML private Label searchSummary;
    @FXML private ListView<Lemma> lemmaList;
    @FXML private HBox printBar;
    @FXML private Label printCount;
    @FXML private Button deselectButton;
    @FXML private Button printButton;
    @FXML private Button firstButton;
    @FXML private Button previousButton;
    @FXML private Label positionPrefix;
    @FXML private TextField positionField;
    @FXML private Label positionTotal;
    @FXML private Button nextButton;
    @FXML private Button lastButton;
    @FXML private HBox occurrenceBar;
    @FXML private Label occurrenceLabel;
    @FXML private Button previousOccurrenceButton;
    @FXML private Button nextOccurrenceButton;
    @FXML private CheckBox selectedToPrint;
    @FXML private Label lemmaNumber;
    @FXML private MarkedText lemmaTitle;
    @FXML private MarkedText definitionText;
    @FXML private VBox abbreviationsPane;
    @FXML private ToggleGroup abbreviationCategories;
    @FXML private ListView<Abbreviation> abbreviationList;
    @FXML private VBox abbreviationDetail;
    @FXML private Label abbreviationPosition;
    @FXML private Label abbreviationTitle;
    @FXML private Label abbreviationMeaning;
    @FXML private Button previousAbbreviationButton;
    @FXML private Button nextAbbreviationButton;
    @FXML private Label statusLabel;
    @FXML private Label toast;
    @FXML private SearchSuggestions suggestions;

    public MainController(Vocabulary vocabulary, SearchHistory history) {
        this.vocabulary = Objects.requireNonNull(vocabulary);
        this.history = Objects.requireNonNull(history);
    }

    @FXML
    private void initialize() {
        lemmaList.setItems(shownLemmas);
        lemmaList.setCellFactory(list -> new LemmaCell(list, () -> query, toPrint));
        lemmaList.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> showSelectedLemma());

        searchField.textProperty().addListener((_, _, text) -> {
            clearSearchButton.setVisible(!text.isBlank());
            typingPause.playFromStart();
        });
        searchField.focusedProperty().addListener((_, _, focused) -> {
            if (!focused) suggestions.hide();
        });
        searchField.setOnMouseClicked(_ -> showSuggestions());
        searchField.setOnKeyPressed(this::onSearchKey);
        typingPause.setOnFinished(_ -> {
            refresh();
            showSuggestions();
        });
        containedSwitch.selectedProperty().addListener((_, _, _) -> refresh());

        letterStrip.setOnLetterSelected(this::showLetter);
        letterStrip.select(LetterStrip.ALL);
        resultTabs.selectedToggleProperty().addListener((_, before, selected) -> {
            if (selected == null) resultTabs.selectToggle(before);
            else showResultList();
        });
        toPrint.addListener((SetChangeListener<Long>) _ -> refreshPrintState());
        positionField.setOnAction(_ -> jumpToTypedPosition());

        abbreviationsPane.visibleProperty().bind(abbreviationsToggle.selectedProperty());
        abbreviationsPane.managedProperty().bind(abbreviationsToggle.selectedProperty());
        abbreviationList.setCellFactory(_ -> new AbbreviationCell());
        abbreviationList.setPlaceholder(wrapping("Elenco da importare dal database originale."));
        abbreviationList.getSelectionModel().selectedIndexProperty().addListener((_, _, _) -> showSelectedAbbreviation());
        abbreviationCategories.selectedToggleProperty().addListener((_, before, selected) -> {
            if (selected == null) abbreviationCategories.selectToggle(before);
            else showAbbreviationCategory();
        });

        lemmaList.sceneProperty().addListener((_, _, scene) -> {
            if (scene != null) Platform.runLater(lemmaList::requestFocus);
            if (scene != null) scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F3), this::nextOccurrence);
        });
        vocabulary.stateProperty().addListener((_, _, state) -> showVocabularyState(state));
        showVocabularyState(vocabulary.getState());
        toast.setOpacity(0);
        refresh();
    }

    private void showVocabularyState(Vocabulary.State state) {
        statusLabel.setText(switch (state) {
            case Vocabulary.Loading _ -> "Apertura del vocabolario…";
            case Vocabulary.Ready(int count) -> String.format(Locale.ITALIAN, "%,d lemmi · sola consultazione", count);
            case Vocabulary.Failed(Throwable cause) -> "Impossibile aprire il vocabolario: " + cause.getMessage();
        });
        if (!(state instanceof Vocabulary.Ready)) return;
        letterStrip.setLemmaCounts(vocabulary.lemmas().stream().collect(Collectors.groupingBy(Lemma::initial, Collectors.counting())));
        abbreviationCategories.selectToggle(abbreviationCategories.getToggles().getFirst());
        refresh();
    }

    private void refresh() {
        List<String> words = words();
        wordsWarning.setVisible(words.size() > SearchQuery.MAX_TERMS);
        wordsWarning.setManaged(wordsWarning.isVisible());
        wordsWarning.setText("Si cercano al massimo quattro parole: «" + String.join(" ", words.stream().skip(SearchQuery.MAX_TERMS).toList()) + "» non viene considerato.");
        query = SearchQuery.of(words, containedSwitch.isSelected() ? MatchMode.CONTAINED : MatchMode.EXACT).orElse(null);
        if (query == null) showBrowsing();
        else showSearching();
    }

    private List<String> words() {
        return Arrays.stream(searchField.getText().strip().split("\\s+")).filter(word -> !word.isBlank()).toList();
    }

    private void showLetter(String chosen) {
        letter = chosen;
        if (query != null) searchField.clear();
        else showBrowsing();
    }

    private void showBrowsing() {
        result = NOTHING_FOUND;
        show(browseHeader, true);
        show(searchHeader, false);
        show(printBar, false);
        show(occurrenceBar, false);
        List<Lemma> lemmas = LetterStrip.ALL.equals(letter)
                ? vocabulary.lemmas()
                : vocabulary.lemmas().stream().filter(lemma -> lemma.initial().equals(letter)).toList();
        listTitle.setText(LetterStrip.ALL.equals(letter) ? "Tutti i lemmi" : letter);
        listCount.setText(String.format(Locale.ITALIAN, "%,d lemmi", lemmas.size()));
        lemmaList.setPlaceholder(new Label(vocabulary.getState() instanceof Vocabulary.Ready ? "Nessun lemma per questa lettera." : "Apertura del vocabolario…"));
        showLemmas(lemmas);
    }

    private void showSearching() {
        result = VocabularySearch.search(vocabulary.lemmas(), query);
        show(browseHeader, false);
        show(searchHeader, true);
        show(printBar, true);
        show(occurrenceBar, true);
        lemmasTab.setText("Lemmi  " + result.inLemmas().size());
        definitionsTab.setText("Definizioni  " + result.inDefinitions().size());
        lemmasTab.setDisable(result.inLemmas().isEmpty());
        definitionsTab.setDisable(result.inDefinitions().isEmpty());
        searchSummary.setText(query.describe());
        lemmaList.setPlaceholder(new Label(query.mode() == MatchMode.EXACT
                ? "Nessuna voce trovata. Prova «Anche parole contenute»."
                : "Nessuna voce trovata. Controlla l'ortografia."));
        if (resultTabs.getSelectedToggle() == lemmasTab && result.inLemmas().isEmpty()) resultTabs.selectToggle(definitionsTab);
        if (resultTabs.getSelectedToggle() == definitionsTab && result.inDefinitions().isEmpty()) resultTabs.selectToggle(lemmasTab);
        showResultList();
        refreshPrintState();
    }

    private void showResultList() {
        if (query == null) return;
        showLemmas(resultTabs.getSelectedToggle() == lemmasTab ? result.inLemmas() : result.inDefinitions());
    }

    private void showLemmas(List<Lemma> lemmas) {
        Lemma selected = lemmaList.getSelectionModel().getSelectedItem();
        shownLemmas.setAll(lemmas);
        int index = Math.max(0, shownLemmas.indexOf(selected));
        if (!shownLemmas.isEmpty()) select(index);
        showSelectedLemma();
        lemmaList.refresh();
    }

    private void onSearchKey(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER -> {
                rememberSearch();
                suggestions.hide();
            }
            case ESCAPE -> {
                if (suggestions.isShowing()) suggestions.hide();
                else searchField.clear();
            }
            case DOWN -> lemmaList.requestFocus();
            default -> { }
        }
    }

    private void rememberSearch() {
        if (query != null) history.record(query);
    }

    @FXML
    private void clearSearch() {
        searchField.clear();
        searchField.requestFocus();
    }

    private void showSuggestions() {
        if (!searchField.isFocused()) return;
        List<Section> sections = query == null
                ? List.of(new Section("Ricerche recenti", recentSearches()), new Section("Azioni", actions()))
                : List.of(new Section("Lemmi", lemmaSuggestions()), new Section("Definizioni", definitionSuggestion()), new Section("Azioni", matchingActions()));
        suggestions.show(searchBox, sections);
    }

    private List<Suggestion> recentSearches() {
        List<SearchQuery> recent = history.entries();
        return IntStream.range(0, Math.min(6, recent.size()))
                .mapToObj(i -> new Suggestion(String.join(" ", recent.get(i).terms()), String.valueOf(i + 1), () -> replay(recent.get(i))))
                .toList();
    }

    private List<Suggestion> lemmaSuggestions() {
        return result.inLemmas().stream()
                .limit(4)
                .map(lemma -> new Suggestion(lemma.lemma(), "N° " + lemma.id(), () -> openFromSuggestion(lemmasTab, lemma)))
                .toList();
    }

    private List<Suggestion> definitionSuggestion() {
        if (result.inDefinitions().isEmpty()) return List.of();
        int count = result.inDefinitions().size();
        return List.of(new Suggestion("Cerca «" + String.join(" ", query.terms()) + "» nelle definizioni", count + (count == 1 ? " voce" : " voci"), () -> openFromSuggestion(definitionsTab, null)));
    }

    private List<Suggestion> actions() {
        return List.of(
                new Suggestion("Apri le abbreviazioni", "", () -> abbreviationsToggle.setSelected(true)),
                new Suggestion("Mostra tutti i lemmi", "", () -> letterStrip.select(LetterStrip.ALL)),
                new Suggestion("Seleziona tutte le voci da stampare", "", this::selectAllToPrint),
                new Suggestion("Deseleziona tutte le voci", "", this::deselectAll),
                new Suggestion("Stampa le voci selezionate", toPrint.size() + "", this::print),
                new Suggestion(containedSwitch.isSelected() ? "Cerca solo parole esatte" : "Cerca anche parole contenute", "", () -> containedSwitch.setSelected(!containedSwitch.isSelected())),
                new Suggestion("Cancella la cronologia delle ricerche", "", history::clear));
    }

    private List<Suggestion> matchingActions() {
        String typed = String.join(" ", query.terms()).toLowerCase(Locale.ITALIAN);
        if (typed.length() < 3) return List.of();
        return actions().stream()
                .filter(action -> Arrays.stream(action.text().toLowerCase(Locale.ITALIAN).split(" ")).anyMatch(word -> word.startsWith(typed)))
                .toList();
    }

    private void replay(SearchQuery recent) {
        containedSwitch.setSelected(recent.mode() == MatchMode.CONTAINED);
        searchField.setText(String.join(" ", recent.terms()));
        searchField.positionCaret(searchField.getText().length());
        refresh();
        rememberSearch();
    }

    private void openFromSuggestion(Toggle tab, Lemma lemma) {
        resultTabs.selectToggle(tab);
        if (lemma != null) select(shownLemmas.indexOf(lemma));
        rememberSearch();
        lemmaList.requestFocus();
    }

    private void showSelectedLemma() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        int index = lemmaList.getSelectionModel().getSelectedIndex();
        int total = shownLemmas.size();
        occurrence = MarkedText.NO_CURRENT_MATCH;

        positionPrefix.setText(query == null || resultTabs.getSelectedToggle() == lemmasTab ? "Lemma" : "Voce");
        positionField.setText(index >= 0 ? String.valueOf(index + 1) : "");
        positionTotal.setText("di " + total);
        firstButton.setDisable(index <= 0);
        previousButton.setDisable(index <= 0);
        nextButton.setDisable(index < 0 || index >= total - 1);
        lastButton.setDisable(index < 0 || index >= total - 1);

        if (lemma == null) {
            lemmaNumber.setText("");
            lemmaTitle.setContent("Nessuna voce");
            definitionText.setContent("");
            return;
        }
        lemmaNumber.setText("N° " + lemma.id());
        lemmaTitle.setContent(lemma.lemma(), query == null ? List.of() : VocabularySearch.occurrences(lemma.lemma(), query), MarkedText.NO_CURRENT_MATCH);
        selectedToPrint.setSelected(toPrint.contains(lemma.id()));
        showDefinition(lemma);
        Animations.settle(definitionText);
        if (query != null && lemmaList.isFocused()) rememberSearch();
    }

    private void showDefinition(Lemma lemma) {
        List<TextRange> matches = definitionMatches(lemma);
        definitionText.setContent(lemma.definition().strip(), matches, occurrence);
        occurrenceLabel.setText(matches.isEmpty() ? "Nessuna occorrenza"
                : occurrence < 0 ? matches.size() + (matches.size() == 1 ? " occorrenza" : " occorrenze")
                : "Occorrenza " + (occurrence + 1) + " di " + matches.size());
        previousOccurrenceButton.setDisable(matches.isEmpty());
        nextOccurrenceButton.setDisable(matches.isEmpty());
    }

    private List<TextRange> definitionMatches(Lemma lemma) {
        return query == null ? List.of() : VocabularySearch.occurrences(lemma.definition().strip(), query);
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
        select(shownLemmas.size() - 1);
    }

    private void jumpToTypedPosition() {
        try {
            int position = Integer.parseInt(positionField.getText().strip());
            if (position < 1 || position > shownLemmas.size()) throw new NumberFormatException();
            select(position - 1);
        } catch (NumberFormatException _) {
            Animations.shake(positionField);
            showSelectedLemma();
        }
    }

    private void select(int index) {
        if (shownLemmas.isEmpty()) return;
        int target = Math.clamp(index, 0, shownLemmas.size() - 1);
        lemmaList.getSelectionModel().clearAndSelect(target);
        lemmaList.scrollTo(Math.max(0, target - 2));
    }

    @FXML
    private void nextOccurrence() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma == null || query == null) return;
        int count = definitionMatches(lemma).size();
        if (occurrence + 1 < count) {
            occurrence++;
            showDefinition(lemma);
            return;
        }
        int index = lemmaList.getSelectionModel().getSelectedIndex();
        OptionalInt following = IntStream.range(index + 1, shownLemmas.size())
                .filter(candidate -> !definitionMatches(shownLemmas.get(candidate)).isEmpty())
                .findFirst();
        if (following.isEmpty()) {
            Animations.shake(nextOccurrenceButton);
            return;
        }
        select(following.getAsInt());
        occurrence = 0;
        showDefinition(shownLemmas.get(following.getAsInt()));
    }

    @FXML
    private void previousOccurrence() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma == null || query == null) return;
        int count = definitionMatches(lemma).size();
        if (count == 0) return;
        occurrence = occurrence <= 0 ? count - 1 : occurrence - 1;
        showDefinition(lemma);
    }

    @FXML
    private void toggleSelectedToPrint() {
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        if (lemma == null) return;
        if (selectedToPrint.isSelected()) toPrint.add(lemma.id());
        else toPrint.remove(lemma.id());
    }

    @FXML
    private void selectAllToPrint() {
        shownLemmas.forEach(lemma -> toPrint.add(lemma.id()));
    }

    @FXML
    private void deselectAll() {
        toPrint.clear();
    }

    @FXML
    private void print() {
        List<Lemma> chosen = vocabulary.lemmas().stream().filter(lemma -> toPrint.contains(lemma.id())).toList();
        if (chosen.isEmpty()) {
            showToast("Nessuna voce da stampare: spunta «Da stampare» sui risultati.");
            return;
        }
        String heading = query == null ? "Voci scelte" : "Ricerca " + query.describe();
        showToast(switch (LemmaPrinter.print(heading, chosen)) {
            case PRINTED -> "Stampa inviata: " + chosen.size() + (chosen.size() == 1 ? " voce." : " voci.");
            case NO_PRINTER -> "Nessuna stampante disponibile.";
            case FAILED -> "Stampa non riuscita.";
        });
    }

    private void refreshPrintState() {
        int count = toPrint.size();
        printCount.setText(count + " da stampare");
        deselectButton.setDisable(count == 0);
        printButton.setDisable(count == 0);
        Lemma lemma = lemmaList.getSelectionModel().getSelectedItem();
        selectedToPrint.setSelected(lemma != null && toPrint.contains(lemma.id()));
        lemmaList.refresh();
    }

    private void showAbbreviationCategory() {
        Object category = abbreviationCategories.getSelectedToggle().getUserData();
        abbreviationList.getItems().setAll(vocabulary.abbreviations().stream().filter(abbreviation -> abbreviation.category().equals(category)).toList());
        abbreviationList.getSelectionModel().selectFirst();
        showSelectedAbbreviation();
    }

    private void showSelectedAbbreviation() {
        Abbreviation abbreviation = abbreviationList.getSelectionModel().getSelectedItem();
        int index = abbreviationList.getSelectionModel().getSelectedIndex();
        show(abbreviationDetail, abbreviation != null);
        if (abbreviation == null) return;
        abbreviationPosition.setText("Voce " + (index + 1) + " di " + abbreviationList.getItems().size());
        abbreviationTitle.setText(abbreviation.abbreviation());
        abbreviationMeaning.setText(abbreviation.meaning());
        previousAbbreviationButton.setDisable(index <= 0);
        nextAbbreviationButton.setDisable(index >= abbreviationList.getItems().size() - 1);
    }

    @FXML
    private void previousAbbreviation() {
        abbreviationList.getSelectionModel().selectPrevious();
    }

    @FXML
    private void nextAbbreviation() {
        abbreviationList.getSelectionModel().selectNext();
    }

    @FXML
    private void searchAbbreviation() {
        Abbreviation abbreviation = abbreviationList.getSelectionModel().getSelectedItem();
        if (abbreviation != null) replay(new SearchQuery(List.of(abbreviation.abbreviation()), MatchMode.CONTAINED));
    }

    @FXML
    private void closeAbbreviations() {
        abbreviationsToggle.setSelected(false);
    }

    private void showToast(String message) {
        toast.setText(message);
        var appear = new FadeTransition(Duration.millis(150), toast);
        appear.setToValue(1);
        var stay = new PauseTransition(Duration.seconds(2.6));
        var disappear = new FadeTransition(Duration.millis(400), toast);
        disappear.setToValue(0);
        new SequentialTransition(appear, stay, disappear).play();
    }

    private static Label wrapping(String text) {
        var label = new Label(text);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        return label;
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static final class AbbreviationCell extends ListCell<Abbreviation> {
        @Override
        protected void updateItem(Abbreviation abbreviation, boolean empty) {
            super.updateItem(abbreviation, empty);
            setText(empty || abbreviation == null ? null : abbreviation.abbreviation() + "  —  " + abbreviation.meaning());
        }
    }
}
