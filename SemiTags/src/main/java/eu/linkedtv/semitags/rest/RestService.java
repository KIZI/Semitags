package eu.linkedtv.semitags.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cz.ilasek.namedentities.disambiguation.CandidatesGenerator;
import cz.ilasek.namedentities.disambiguation.Disambiguation;
import cz.ilasek.namedentities.models.DisambiguatedEntity;
import cz.ilasek.namedentities.recognition.DutchStanfordRecognizer;
import cz.ilasek.namedentities.recognition.EnglishStanfordRecognizer;
import cz.ilasek.namedentities.recognition.GermanStanfordRecognizer;
import cz.ilasek.namedentities.recognition.Recognizer;
import cz.ilasek.nlp.ner.StanfordEntity;
import eu.linkedtv.semitags.rest.model.NamedEntity;
import eu.linkedtv.semitags.rest.model.StanfordEntityRestable;

@Component
@Path("/v1")
public class RestService {
    
    private static final int REC_SENTENCES_COUNT = 10; 
    private static final double MAX_GRAPH_SCORE = 15000000d;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    // Recognizer
    @Autowired
    private GermanStanfordRecognizer germanRecognizer;
    
    @Autowired
    private DutchStanfordRecognizer dutchRecognizer;
    
    @Autowired
    private EnglishStanfordRecognizer englishRecognizer;
    
    // Candidates generator
    @Autowired
    @Qualifier("englishCandidatesGenerator")
    private CandidatesGenerator englishCandidatesGenerator;
    
    @Autowired
    @Qualifier("germanCandidatesGenerator")
    private CandidatesGenerator germanCandidatesGenerator;
    
    @Autowired
    @Qualifier("dutchCandidatesGenerator")
    private CandidatesGenerator dutchCandidatesGenerator;    

    // Disambiguation
    @Autowired
    private Disambiguation englishDisambiguation;
    
    @Autowired
    private Disambiguation germanDisambiguation;
    
    @Autowired
    private Disambiguation dutchDisambiguation;    
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_XML)
    @Path("/ner")
    public List<StanfordEntityRestable> ner(MultivaluedMap<String, String> formParams) {
        List<String> languages = formParams.get("language");
        String language = "de";
        if (languages.size() > 0)
            language = languages.get(0);
        
        List<String> texts = formParams.get("text");
        
        Recognizer recognizer;
        
        if (language.equals("nl")) {
            recognizer = dutchRecognizer;
        }
        else if (language.equals("de")) {
            recognizer = germanRecognizer;
        } else {
            recognizer = englishRecognizer;
        }    
        
        if (texts.size() > 0) {
            List<StanfordEntityRestable> response = new LinkedList<StanfordEntityRestable>();
            List<StanfordEntity> recEntities = recognizer.recognize(texts.get(0));
            
            for (StanfordEntity entity : recEntities) {
                response.add(new StanfordEntityRestable(entity));
            }
        
            return response;
        } else {
            return null;
        }
    }    
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_XML)
    @Path("/recognize")
    public List<NamedEntity> recognize(MultivaluedMap<String, String> formParams) {
        List<String> languages = formParams.get("language");
        String language = "de";
        if (languages.size() > 0)
            language = languages.get(0);
        
        List<String> texts = formParams.get("text");
        
        Recognizer recognizer;
        CandidatesGenerator candidatesGenerator;
        Disambiguation disambiguation;
        
        if (language.equals("nl")) {
            recognizer = dutchRecognizer;
            candidatesGenerator = dutchCandidatesGenerator;
            disambiguation = dutchDisambiguation;
        }
        else if (language.equals("de")) {
            recognizer = germanRecognizer;
            candidatesGenerator = germanCandidatesGenerator;
            disambiguation = germanDisambiguation;
        } else {
            recognizer = englishRecognizer;
            candidatesGenerator = englishCandidatesGenerator;
            disambiguation = englishDisambiguation;
        }        
        
        if (texts.size() > 0) {
            Map<String, NamedEntity> namedEntities = chunkAndExtractEntities(language, texts.get(0), recognizer, candidatesGenerator, disambiguation);
            
            List<NamedEntity> response = new LinkedList<NamedEntity>();
            for (Entry<String, NamedEntity> neEntry : namedEntities.entrySet()) {
                response.add(neEntry.getValue());
            }
            
            return response;
        } else {
            return null;
        }
    }

    private Map<String, NamedEntity> chunkAndExtractEntities(String language, String originalText,
            Recognizer recognizer, CandidatesGenerator candidatesGenerator, Disambiguation disambiguation) {
        Map<String, NamedEntity> namedEntities = new HashMap<String, NamedEntity>();
        String[] sentences = originalText.split("\\.");
        logger.debug("Input text split into " + sentences.length + " sentences.");
        
        StringBuilder recText = new StringBuilder();
        int offset = 0;
        for (int i = 0; i < sentences.length; i++) {
            recText.append(sentences[i] + ".");
        
            if (((i % REC_SENTENCES_COUNT) == 0) && (i > 0)) {
                logger.debug("Extracting entities up to sentence " + i);
                namedEntities = extractEntities(
                        recText.toString(), namedEntities, offset, recognizer, candidatesGenerator, disambiguation);
                offset += recText.length();
                recText = new StringBuilder();
            }
        }
        
        if (recText.length() > 0) { 
            logger.debug("Extracting entities from remaining text - lnegth: " + recText.length());
            namedEntities = extractEntities(
                    recText.toString(), namedEntities, offset, recognizer, candidatesGenerator, disambiguation);
        }
        
        return namedEntities;
    }
    
    private Map<String, NamedEntity> extractEntities(String originalText, Map<String, NamedEntity> namedEntities, int offset,
            Recognizer recognizer, CandidatesGenerator candidatesGenerator, Disambiguation disambiguation) {
        String recText = originalText;
    
        recText = recText.replaceAll("[^\\p{L}\\p{N}\\n\\.\\,-]", " ");
        
        Collection<DisambiguatedEntity> disambiguatedEntites = disambiguateEntities(
                generateCandidates(recognizeEntities(recText, recognizer), candidatesGenerator), 
                recText, disambiguation);
        
        for (DisambiguatedEntity disambiguatedEntity : disambiguatedEntites) {
            namedEntities.put(
                    disambiguatedEntity.getUri(), 
                    mapEntity(namedEntities.get(disambiguatedEntity.getUri()), disambiguatedEntity, originalText, offset));
        }
        
        return namedEntities;
    }
    
    private List<StanfordEntity> recognizeEntities(String recText, Recognizer recognizer) {
        return recognizer.recognize(recText);
    }
    
    private List<DisambiguatedEntity> generateCandidates(List<StanfordEntity> identifiedEntities, CandidatesGenerator candidatesGenerator) {
        List<DisambiguatedEntity> candidates = new LinkedList<DisambiguatedEntity>();
        
        int groupId = 1;
        for (StanfordEntity identifiedEntity : identifiedEntities) {
            List<DisambiguatedEntity> generatedCandidates = candidatesGenerator.getCandidates(identifiedEntity.getName(), groupId++);
            
            for (DisambiguatedEntity candidate : generatedCandidates) {
                candidate.setType(identifiedEntity.getType());
                candidates.add(candidate);
            }
        }
        
        return candidates;
    }
    
    private Collection<DisambiguatedEntity> disambiguateEntities(List<DisambiguatedEntity> candidates, String recText, Disambiguation disambiguation) {
        return disambiguation.listDisambiguatedEntities(candidates, recText);
    }
    
    private NamedEntity mapEntity(NamedEntity namedEntity, DisambiguatedEntity entity, String originalText, int offset) {
        if (namedEntity == null) {
            namedEntity = new NamedEntity();
            namedEntity.setName(entity.getName());
        }
        
        int index = 0;
        while ((index = originalText.indexOf(entity.getName(), index)) >= 0) {
            namedEntity.addOccurrence(offset + index, offset + index + entity.getName().length() - 1);
            index = index + 1;
        }
        namedEntity.setConfidence(entity.getGraphScore() / MAX_GRAPH_SCORE);
        namedEntity.setType(mapType(entity.getType()));
        namedEntity.setDbpediaUri(entity.getDBpediaUri());
        namedEntity.setWikipediaUri(entity.getUri());
        
        return namedEntity;
    }
    
    private static String mapType(String type) {
        if (type.substring(0, 2).equals("I-") || type.substring(0, 2).equals("B-")) {
            if (type.substring(2, 5).equals("PER"))
                return "person";
            else if (type.substring(2, 5).equals("ORG"))
                return "organization";
            else if (type.substring(2, 5).equals("LOC"))
                return "location";
            else
                return "miscellaneous";
        } else
            return type;
    }
    
    
}
