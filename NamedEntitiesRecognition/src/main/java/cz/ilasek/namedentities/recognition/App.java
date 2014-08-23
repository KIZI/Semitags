package cz.ilasek.namedentities.recognition;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.ilasek.namedentities.models.DisambiguatedEntity;

public class App 
{
    
    public App() {

    }
    
    public static void main( String[] args ) throws IOException
    {
//        String text = "Dat is de beste garantie voor een meer welvarende toekomst van Griekenland in de eurozone.";
//        TokenNameFinderModel tnfm = new TokenNameFinderModel(App.class.getResourceAsStream("/opennlp/model/nl_ner_all.bin"));
//        NameFinderME nfm = new NameFinderME(tnfm);
//        String[] tokens = text.split("[^\\p{L}\\p{N}]");
//        ArrayList<String> filteredTokens = new ArrayList<String>();
//        for (String token : tokens) {
//            if (token.length() > 0) {
//                filteredTokens.add(token);
//                System.out.println(token);
//            }
//        }
//        filteredTokens.add(".");
//        System.out.println("================");
//        
//        String[] ftkns = filteredTokens.toArray(new String[filteredTokens.size()]);
//        Span[] nes = nfm.find(ftkns);
//        System.out.println(nes.length);
//        for (Span ne : nes) {
//            System.out.println(ne.getType());
//            for (int i = ne.getStart(); i < ne.getEnd(); i++) {
//                System.out.print(ftkns[i] + " ");
//            }
//            System.out.println();
//        }
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[] {"nerrecconfig.xml"});
        RecognitionTester rt = context.getBean(RecognitionTester.class);
        rt.testSentenceDisambiguation();
//        rt.indexOccurrences();
    }
    
    private static Map<Integer, DisambiguatedEntity> getResultsByGraphScore(List<DisambiguatedEntity> allCandidates) {
        Map<Integer, DisambiguatedEntity> results = new HashMap<Integer, DisambiguatedEntity>();
        System.out.println("============================");      
        System.out.println("Filtering results...");
        System.out.println("============================");      
        for (DisambiguatedEntity de : allCandidates) {
            System.out.println(de.toString());
            double bestScore = 0;
            DisambiguatedEntity bestEntity = results.get(de.getGroupId());
            if (bestEntity != null)
                bestScore = bestEntity.getGraphScore();
            
            if (de.getGraphScore() > bestScore)
                results.put(de.getGroupId(), de);
        }
        System.out.println("============================");      
        
        return results;
    }
    
    
    private static Map<Integer, DisambiguatedEntity> getResultsByStringScore(List<DisambiguatedEntity> allCandidates) {
        Map<Integer, DisambiguatedEntity> results = new HashMap<Integer, DisambiguatedEntity>();
        for (DisambiguatedEntity de : allCandidates) {
            double bestScore = 0;
            DisambiguatedEntity bestEntity = results.get(de.getGroupId());
            if (bestEntity != null)
                bestScore = bestEntity.getStringScore();
            
            if (de.getStringScore() > bestScore)
                results.put(de.getGroupId(), de);
        }
        
        return results;
    }    
    
    private static Map<Integer, DisambiguatedEntity> getResultsCoOcuurenceAndStringScore(List<DisambiguatedEntity> allCandidates) {
        Map<Integer, DisambiguatedEntity> results = new HashMap<Integer, DisambiguatedEntity>();
        float bestStringScore = 0;
        double bestGraphScore = 0;
        for (DisambiguatedEntity de : allCandidates) {
            if (de.getGraphScore() > bestGraphScore)
                bestGraphScore = de.getGraphScore();
            if (de.getStringScore() > bestStringScore)
                bestStringScore = de.getStringScore();
        }
        
        for (DisambiguatedEntity de : allCandidates) {
            de.setStringScore(de.getStringScore() / bestStringScore);
            de.setGraphScore(de.getGraphScore() / bestGraphScore);
        }

        for (DisambiguatedEntity de : allCandidates) {
            double bestScore = 0;
            DisambiguatedEntity bestEntity = results.get(de.getGroupId());
            if (bestEntity != null)
                bestScore = bestEntity.getStringScore();
            
            if (de.getStringScore() > bestScore)
                results.put(de.getGroupId(), de);
        }        
        
        return results;
    }       
}
