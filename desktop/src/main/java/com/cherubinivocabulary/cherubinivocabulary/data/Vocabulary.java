package com.cherubinivocabulary.cherubinivocabulary.data;

import module java.base;
import module javafx.graphics;

import com.cherubinivocabulary.cherubinivocabulary.model.Abbreviation;
import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;

public final class Vocabulary {

    public sealed interface State permits Loading, Ready, Failed { }

    public record Loading() implements State { }

    public record Ready(int lemmaCount) implements State { }

    public record Failed(Throwable cause) implements State { }

    private record Snapshot(List<Lemma> lemmas, List<Abbreviation> abbreviations) { }

    private final LemmaRepository lemmaRepository;
    private final AbbreviationRepository abbreviationRepository;
    private final ObservableList<Lemma> lemmas = FXCollections.observableArrayList();
    private final ObservableList<Abbreviation> abbreviations = FXCollections.observableArrayList();
    private final ObservableList<Lemma> readOnlyLemmas = FXCollections.unmodifiableObservableList(lemmas);
    private final ObservableList<Abbreviation> readOnlyAbbreviations = FXCollections.unmodifiableObservableList(abbreviations);
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(this, "state", new Loading());

    public Vocabulary(LemmaRepository lemmaRepository, AbbreviationRepository abbreviationRepository) {
        this.lemmaRepository = Objects.requireNonNull(lemmaRepository);
        this.abbreviationRepository = Objects.requireNonNull(abbreviationRepository);
    }

    public void load() {
        state.set(new Loading());
        Task<Snapshot> task = new Task<>() {
            @Override
            protected Snapshot call() {
                return new Snapshot(lemmaRepository.findAll(), abbreviationRepository.findAll());
            }
        };
        task.setOnSucceeded(_ -> {
            Snapshot snapshot = task.getValue();
            abbreviations.setAll(snapshot.abbreviations());
            lemmas.setAll(snapshot.lemmas());
            state.set(new Ready(snapshot.lemmas().size()));
        });
        task.setOnFailed(_ -> state.set(new Failed(task.getException())));
        Thread.ofVirtual().name("vocabulary-loader").start(task);
    }

    public ObservableList<Lemma> lemmas() {
        return readOnlyLemmas;
    }

    public ObservableList<Abbreviation> abbreviations() {
        return readOnlyAbbreviations;
    }

    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    public State getState() {
        return state.get();
    }
}
