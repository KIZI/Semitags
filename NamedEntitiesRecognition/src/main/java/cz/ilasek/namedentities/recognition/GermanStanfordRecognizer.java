package cz.ilasek.namedentities.recognition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.ilasek.nlp.ner.StanfordEntity;

public class GermanStanfordRecognizer extends StanfordRecognizer {

    private final GermanNerModel nerModel;
    
    public enum GermanNerModel {
        NEWS_WIRE("de_hgc_175m_600.crf.ser"), OTHER_TEXTS("de_dewac_175m_600.crf.ser");

        private final String model;
        
        private static final Map<String, GermanNerModel> lookup = new HashMap<String, GermanNerModel>();
        static {
            for (GermanNerModel t : GermanNerModel.values())
                lookup.put(t.getModel(), t);
        }
        

        private GermanNerModel(String model) {
            this.model = model;
        }

        public String getModel() {
            return model;
        }
        
        public static GermanNerModel get(String model) {
            return lookup.get(model);
        }        
    }
    
    public GermanStanfordRecognizer(GermanNerModel nerModel) {
        this.nerModel = nerModel;
    }
    
    @Override
    public List<StanfordEntity> recognize(String text) {
        return concatEntities(extractEntities(text, modelDirectory + nerModel.getModel()));
    }

}
