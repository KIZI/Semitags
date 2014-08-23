package cz.ilasek.namedentities.recognition;

import java.util.List;

import cz.ilasek.nlp.ner.StanfordEntity;

public class DutchStanfordRecognizer extends StanfordRecognizer {
    private static final String NER_MODEL = "nl_ned-model.ser";
    
    @Override
    public List<StanfordEntity> recognize(String text) {
        return concatEntities(extractEntities(text, modelDirectory + NER_MODEL));
    }
}
