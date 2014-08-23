package cz.ilasek.namedentities.disambiguation;

import java.io.IOException;
import java.util.List;

import cz.ilasek.namedentities.models.Chunk;
import cz.ilasek.namedentities.models.EntityInText;
import cz.ilasek.namedentities.models.SVOTriple;
import cz.ilasek.namedentities.recognition.ONLPExtractor;

public class SentenceDisambiguation {
    
    private ONLPExtractor onlp = new ONLPExtractor();
    
    public EntityInText locateEntity(String entity, String text) {
        EntityInText entityInText = new EntityInText(entity);
        try {
            String[] sentences = onlp.detectSentences(text);
            for (String sentence : sentences) {
                if (sentence.contains(entity)) {
                    List<Chunk> chunks = onlp.getChunks(sentence);
                    List<SVOTriple> triples = onlp.getSVOTriples(chunks);
                    for (SVOTriple triple : triples) {
                        if (triple.getSubjectStr().contains(entity) ||
                                triple.getObjectStr().contains(entity) ||
                                triple.getVerbStr().contains(entity))
                            entityInText.addSvoTriple(triple);
                            
                    }
                }
            }
        } catch (IOException e) {
            // TODO log error
        }
        
        return entityInText;
    }

    public EntityInText coLocateEntity(String entity, String text) {
        EntityInText entityInText = new EntityInText(entity);
        try {
            String[] sentences = onlp.detectSentences(text);
            for (String sentence : sentences) {
                if (sentence.contains(entity)) {
                    List<Chunk> chunks = onlp.getChunks(sentence);
                    List<SVOTriple> triples = onlp.getSVOTriples(chunks);
                    for (SVOTriple triple : triples) {
                        entityInText.addSvoTriple(triple);
                    }
                }
            }
        } catch (IOException e) {
            // TODO log error
        }
        
        return entityInText;
    }    
    
}
