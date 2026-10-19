package com.cherubinivocabulary.cherubinivocabulary.data;

import module java.base;

import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;

public interface LemmaRepository {
    List<Lemma> findAll();
}
