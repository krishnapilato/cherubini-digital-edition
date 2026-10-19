package com.cherubinivocabulary.cherubinivocabulary.data;

import module java.base;

import com.cherubinivocabulary.cherubinivocabulary.model.Abbreviation;

public interface AbbreviationRepository {
    List<Abbreviation> findAll();
}
