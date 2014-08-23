package cz.ilasek.namedentities.recognition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import cz.ilasek.namedentities.disambiguation.Disambiguator;
import cz.ilasek.namedentities.disambiguation.SentenceDisambiguation;
import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;
import cz.ilasek.namedentities.index.dao.sql.SentenceOccurrencesDao;
import cz.ilasek.namedentities.index.models.EntityMention;
import cz.ilasek.namedentities.models.DisambiguatedEntity;
import cz.ilasek.namedentities.models.EntityInText;
import cz.ilasek.namedentities.models.SVOTriple;

public class RecognitionTester {
    private final Disambiguator disambiguator;
    private final EntityMentionsDao entityMentionsDao;
    private final SentenceOccurrencesDao sentenceOccurrencesDao;
    private final Recognizer recognizer;
    private final SentenceDisambiguation sd;
    
    public RecognitionTester(Disambiguator disambiguator, EntityMentionsDao entityMetionsDao, SentenceOccurrencesDao sentenceOccurrencesDao) {
        recognizer = new LegacyEnglishStanfordRecognizer();
        this.disambiguator = disambiguator;
        this.entityMentionsDao = entityMetionsDao;
        this.sentenceOccurrencesDao = sentenceOccurrencesDao;
        sd = new SentenceDisambiguation();        
    }
    
    public void testSentenceDisambiguation() throws IOException {
        String text = loadArticle("article1.txt");
        
        Set<String> entityNames = disambiguator.concatEntities(recognizer.recognize(text));
        
        int groupId = 1;
        for (String entityName : entityNames) {
            EntityInText eInText = sd.coLocateEntity(entityName, text);
//            System.out.println("Disambiguating: " + eInText.getEntityName());
            
            List<DisambiguatedEntity> candidates = disambiguator.getCandidates(entityName, groupId);
            if (candidates.size() > 0) {
                for (DisambiguatedEntity candidate : candidates) {
                    for (SVOTriple svoTriple : eInText.getSvoTriples()) {
                        System.out.println(svoTriple.getSubjectStr() + " .. " + svoTriple.getVerbStr() + " .. " + svoTriple.getObjectStr());
//                        candidate.addIntScore(sentenceOccurrencesDao.getSimilarOccurencesCount(candidate.getEntityId(), 
//                                svoTriple.getSubjectStr(), svoTriple.getVerbStr(), svoTriple.getObjectStr()));
                    }
                }
                
//                DisambiguatedEntity bestCandidate = candidates.get(0);
//                for (DisambiguatedEntity candidate : candidates) {
////                    System.out.println("Candidate: " + candidate.getUri() + " ... " + candidate.getIntScore());
//                    if (candidate.getIntScore() > bestCandidate.getIntScore())
//                        bestCandidate = candidate;
//                }
////                System.out.println("=============================================");
//                System.out.println(entityName + " ... " + bestCandidate.getUri());
            } else {
//                System.out.println("No candidate found");
            }
//            System.out.println("=============================================");
            groupId++;
        }        
    }
    
    public void testDisambiguation() throws IOException {
        String text = loadArticle("article1.txt");
        
        Set<String> entities = disambiguator.concatEntities(recognizer.recognize(text));
        
        for (String entity : entities) {
            EntityInText eInText = sd.coLocateEntity(entity, text);
            System.out.println(eInText.getEntityName());
            for (SVOTriple svoTriple : eInText.getSvoTriples()) {
                System.out.println(svoTriple.getSubjectStr() + " - " + svoTriple.getVerbStr() + " - " + svoTriple.getObjectStr());
            }
            System.out.println("=============================================");
            System.out.println("=============================================");
        }
        
// ///////////////////////////////////////////////////////////        
//        List<DisambiguatedEntity> allCandidates = recognizer.getSpotDisambCandidates(
//                recognizer.identifyEntities(text), text);
//        allCandidates = recognizer.graphDisambiguation(allCandidates);
//        allCandidates = disambiguator.coOccurrenceDisambiguation(allCandidates);
        
//        Map<Integer, DisambiguatedEntity> results = getResultsByGraphScore(allCandidates);
//        Map<Integer, DisambiguatedEntity> results = getResultsByStringScore(allCandidates);

//        System.out.println("Results");
//        System.out.println("============================");
//        System.out.println("============================");        
//        for (Entry<Integer, DisambiguatedEntity> e : results.entrySet()) {
//            System.out.println(e.getValue().getName() + " ... " + e.getValue().getUri());
//        }        
    }
    
    public void indexOccurrences() throws IOException {
        for (int i = 1; i <= 1; i++) {
            System.out.println("Loading article " + i);
            String text = loadArticle("article" + i + ".txt");
            
            Set<String> entities = disambiguator.concatEntities(recognizer.recognize(text));        
            
            List<DisambiguatedEntity> allCandidates = disambiguator.getCandidates(entities);
            System.out.println("Candidates count: " + allCandidates.size());
            
            for (DisambiguatedEntity candidate : allCandidates) {
                System.out.println("Indexing candidate: " + candidate.getUri());
                
                List<EntityMention> entityMentions = entityMentionsDao.getEntityMentions(candidate.getEntityId(), 100);
                // TODO delete candidate sentence Occurrences
                for (EntityMention entityMention : entityMentions) {
                    EntityInText locatedEntity = sd.coLocateEntity(entityMention.getSurfaceForm(), entityMention.getParagraph());
                    for (SVOTriple svoTriple : locatedEntity.getSvoTriples()) {
                        try {
                            sentenceOccurrencesDao.insertSentenceOccurrence(
                                    entityMention.getEntityId(), entityMention.getParagraphId(), 
                                    svoTriple.getSubjectStr(), svoTriple.getVerbStr(), svoTriple.getObjectStr());
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    private static String loadArticle(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        
        return sb.toString();
    }    
}
