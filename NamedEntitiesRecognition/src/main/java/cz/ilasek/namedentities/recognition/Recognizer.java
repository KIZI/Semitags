package cz.ilasek.namedentities.recognition;

import java.util.List;

import cz.ilasek.nlp.ner.StanfordEntity;

public interface Recognizer {
    public List<StanfordEntity> recognize(String text);
}
