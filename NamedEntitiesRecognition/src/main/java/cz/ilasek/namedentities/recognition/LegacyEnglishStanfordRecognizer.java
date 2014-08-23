package cz.ilasek.namedentities.recognition;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import cz.ilasek.nlp.ner.StanfordEntity;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

// TODO - rewrite to work similar like German and Dutch - english models are in the same location
/**
 * 
 * @deprecated Now using uniform access to Stanford models in {@link EnglishStanfordRecognizer}
 *
 */
public class LegacyEnglishStanfordRecognizer extends StanfordRecognizer {

    @Override
    public List<StanfordEntity> recognize(String text) {
        List<StanfordEntity> entities = new LinkedList<StanfordEntity>();
        
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
 
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and
        // has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);        
        
        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            Tree tree = sentence.get(TreeAnnotation.class);
            String category = "";
            entities = traverseTree(tree, entities, category);
        }
        
        return concatEntities(entities); 
    }
    
    private List<StanfordEntity> traverseTree(Tree tree, List<StanfordEntity> entities, String category) {
        Label label = tree.label();
        if (label instanceof CoreLabel) {
            CoreLabel coreLabel = (CoreLabel) label;
            if (coreLabel.category() != null)
                category += "-" + coreLabel.category();
            
            if ((coreLabel.ner() != null) && (!coreLabel.ner().equals("O"))) {
                StanfordEntity entity = new StanfordEntity();
                entity.setName(coreLabel.originalText());
                entity.setStart(coreLabel.beginPosition());
                entity.setLength(coreLabel.endPosition() - coreLabel.beginPosition());
                entity.setType(coreLabel.ner());
                entity.setCategory(category);
                
                entities.add(entity);
            }
        }
        List<Tree> childTrees = tree.getChildrenAsList();
        for (Tree childTree : childTrees) {
            traverseTree(childTree, entities, category);
        }
        return entities;
    }    

}
