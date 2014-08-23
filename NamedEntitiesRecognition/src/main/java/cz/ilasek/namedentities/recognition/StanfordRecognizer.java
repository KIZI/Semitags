package cz.ilasek.namedentities.recognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.ilasek.nlp.ner.StanfordEntity;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;

public abstract class StanfordRecognizer implements Recognizer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected String modelDirectory;
    
    public void setModelDirectory(String modelDirectory) {
        this.modelDirectory = modelDirectory;
        
        if (!modelDirectory.substring(modelDirectory.length() - 1).equals("/"))
            this.modelDirectory += "/";
    }
    
    protected List<StanfordEntity> concatEntities(List<StanfordEntity> entities) {
        List<StanfordEntity> concatEntities = new LinkedList<StanfordEntity>();
        
        StanfordEntity prevEntity = null;
        StanfordEntity currentEntity = null;
        
        for (StanfordEntity entity : entities) {
            if (!entity.getType().equals("NUMBER") && !entity.getType().equals("DATE") && !entity.getType().equals("DURATION") 
                    && !entity.getType().equals("SET") && !entity.getType().equals("PERCENT") && !entity.getType().equals("MONEY")
                    && !entity.getType().equals("ORDINAL")) {
                if ((prevEntity != null) && (entity.getStart() == (prevEntity.getStart() + prevEntity.getLength() + 1))) {
                    currentEntity.setName(currentEntity.getName() + " " + entity.getName());
                    currentEntity.setLength(currentEntity.getName().length());
                } else {
                    if (prevEntity != null)
                        concatEntities.add(currentEntity);
                    currentEntity = new StanfordEntity();
                    currentEntity.setName(entity.getName());
                    currentEntity.setStart(entity.getStart());
                    currentEntity.setType(entity.getType());
                }
                prevEntity = entity;
            }
        }
        if (currentEntity != null)
            concatEntities.add(currentEntity);
        
        return concatEntities;
    }
    
    /**
     * 
     * @param text
     * @param nerModel Path to Stanford CRF model resource for NER. 
     * @return
     */
    protected List<StanfordEntity> extractEntities(String text, String nerModel) {
        List<StanfordEntity> entities = new LinkedList<StanfordEntity>();
        try {
            InputStream is = new FileInputStream(new File(nerModel));

            AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(is);
            List<List<CoreLabel>> out = classifier.classify(text);
            for (List<CoreLabel> sentence : out) {
                for (CoreLabel coreLabel : sentence) {
                    String ner = coreLabel.get(AnswerAnnotation.class);
                    if ((ner != null) && (!ner.equals("O"))) {
                        StanfordEntity entity = new StanfordEntity();
                        entity.setName(coreLabel.originalText());
                        entity.setStart(coreLabel.beginPosition());
                        entity.setLength(coreLabel.endPosition() - coreLabel.beginPosition());
                        entity.setType(ner);
                        entity.setCategory(coreLabel.category());
                        
                        entities.add(entity);
                    }
                }
            }
        } catch (ClassCastException e) {
            logger.error("Problems creating Stanford model " + nerModel, e);
        } catch (IOException e) {
            logger.error("Problems creating Stanford model " + nerModel, e);
        } catch (ClassNotFoundException e) {
            logger.error("Problems creating Stanford model " + nerModel, e);
        }

        return concatEntities(entities);
    }
}
