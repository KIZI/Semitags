package cz.ilasek.namedentities.recognition;

import java.util.List;

import cz.ilasek.nlp.ner.StanfordEntity;

public class EnglishStanfordRecognizer extends StanfordRecognizer {
    private static final String NER_MODEL = "en_all.3class.distsim.crf.ser";
    
    @Override
    public List<StanfordEntity> recognize(String text) {
        return concatEntities(extractEntities(text, modelDirectory + NER_MODEL));
    }
}
