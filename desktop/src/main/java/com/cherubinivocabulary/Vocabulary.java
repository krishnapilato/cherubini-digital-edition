package com.cherubinivocabulary;

import module java.base;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

final class Vocabulary {
    private static final String SOURCE = "/cherubini.json";

    private Vocabulary() {}

    // The streaming parser rather than data binding: the file is a flat array of flat objects, and
    // reading it token by token loads far fewer classes at startup.
    static List<Lemma> load() {
        try (var in = Objects.requireNonNull(Vocabulary.class.getResourceAsStream(SOURCE), SOURCE);
             var parser = new JsonFactory().createParser(ObjectReadContext.empty(), in)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) throw new IllegalStateException("The vocabulary is not a JSON array.");
            var lemmas = new ArrayList<Lemma>();
            while (parser.nextToken() == JsonToken.START_OBJECT) lemmas.add(entry(parser));
            lemmas.sort(Comparator.comparingLong(Lemma::id));
            return List.copyOf(lemmas);
        } catch (IOException | JacksonException e) {
            throw new IllegalStateException("Failed to read the vocabulary.", e);
        }
    }

    private static Lemma entry(JsonParser parser) {
        var fields = new HashMap<String, String>();
        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            var name = parser.currentName();
            parser.nextToken();
            fields.put(name, parser.getValueAsString());
        }
        if (fields.get("id") == null || fields.get("lemma") == null) throw new IllegalStateException("An entry of the vocabulary has no id or lemma.");
        return Lemma.of(Long.parseLong(fields.get("id")), fields.get("lemma"), fields.get("def2"));
    }
}
