package cz.ilasek.namedentities.recognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;
import cz.ilasek.nlp.ner.StanfordEntity;

public class DutchONLPRecognizer implements Recognizer {
    
    private static final String MODEL_FILE = "nl_ner_all.bin";
    
    private String modelDirectory;
    
    public void setModelDirectory(String modelDirectory) {
        this.modelDirectory = modelDirectory;
        
        if (!modelDirectory.substring(modelDirectory.length() - 1).equals("/"))
            this.modelDirectory += "/";
    }

    @Override
    public List<StanfordEntity> recognize(String text) {
        List<StanfordEntity> entities = new LinkedList<StanfordEntity>();

        TokenNameFinderModel tnfm;
        try {
            tnfm = new TokenNameFinderModel(
                    new FileInputStream(new File(modelDirectory + MODEL_FILE)));
        } catch (InvalidFormatException e) {
            throw new RecognitionException("Problems loading Recognizer model " + MODEL_FILE, e);
        } catch (IOException e) {
            throw new RecognitionException("Problems loading Recognizer model " + MODEL_FILE, e);
        }
        
        NameFinderME nfm = new NameFinderME(tnfm);
        String[] tokens = text.split("[^\\p{L}\\p{N}\\.\\,]");
        ArrayList<String> filteredTokens = new ArrayList<String>();
        for (String token : tokens) {
            if (token.length() > 0) {
                filteredTokens.add(token);
            }
        }

        String[] ftkns = filteredTokens.toArray(new String[filteredTokens
                .size()]);
        Span[] nes = nfm.find(ftkns);
        
        for (Span ne : nes) {
            StanfordEntity se = new StanfordEntity();
            se.setType(ne.getType());
            String name = "";
            for (int i = ne.getStart(); i < ne.getEnd(); i++) {
                name += ftkns[i];
                if (i < (ne.getEnd() - 1)) {
                    name += " ";
                }
            }
            se.setName(name);
            se.setLength(name.length());
            se.setStart(ne.getStart());
            entities.add(se);
        }

        return entities;
    }

}
