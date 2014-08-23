package cz.ilasek.namedentities.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Ostermiller.util.CSVPrinter;
import com.google.gson.Gson;

import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;

public class SurfaceFormIndexBuilder {
    private static final int MINIMUM_OCCURRENCE_COUNT = 4;
    
    private static final String SURFACE_FORM_CSV_FILE = "surface_form_entities.csv";
    
    private static final int LOG_FREQUENCY = 50000;
    
    private static final Logger logger = LoggerFactory.getLogger(SurfaceFormIndexBuilder.class);
    
    private static final Gson gson = new Gson();
    
    private final CSVReader inputReader;
    
    private final CSVPrinter surfaceFormsPrinter;
    
    
    public SurfaceFormIndexBuilder(String inputFolder, String outputFolder) throws IOException {
        inputReader = new CSVReader(inputFolder);
        surfaceFormsPrinter = new CSVPrinter(new FileOutputStream(new File(outputFolder + "/" + SURFACE_FORM_CSV_FILE)));
        surfaceFormsPrinter.writeln(new String[]{"surfaceForm", "entitiesList"});
    }

    public void indexSurfaceForms() {
        Map<String, Map<String, Integer>> surfaceFormIndex = new HashMap<String, Map<String, Integer>>();
        EntityMentionInParagraph emip = null;
        
        int i = 0;
        try {
            while((emip = inputReader.readEntityMention()) != null) {
                Map<String, Integer> entities;
                if (surfaceFormIndex.containsKey(emip.getSurfaceForm())) {
                    entities = surfaceFormIndex.get(emip.getSurfaceForm());
                } else {
                    entities = new HashMap<String, Integer>();
                    surfaceFormIndex.put(emip.getSurfaceForm(), entities);
                }
                
                int occurrences;
                if (entities.containsKey(emip.getEntityUri())) {
                    occurrences = entities.get(emip.getEntityUri());
                } else {
                    occurrences = 0;
                }
                
                occurrences++;
                entities.put(emip.getEntityUri(), occurrences);
                
                if ((++i % LOG_FREQUENCY) == 0) {
                    logger.info("Processed " + i + " mentions.");
                }
            }
            
            logger.info("Indexed " + surfaceFormIndex.size() + " surface forms");
            logger.info("Persisting");
            
            i = 0;
            for (Entry<String, Map<String, Integer>> surfaceForm : surfaceFormIndex.entrySet()) {
                Set<String> frequentEntities = new HashSet<String>();
                
                for (Entry<String, Integer> entityOccurrences : surfaceForm.getValue().entrySet()) {
                    if (entityOccurrences.getValue() > MINIMUM_OCCURRENCE_COUNT) {
                        frequentEntities.add(entityOccurrences.getKey());
                    }
                }
                
                try {
                    surfaceFormsPrinter.writeln(new String[]{ surfaceForm.getKey(),  gson.toJson(frequentEntities)});
                } catch (IOException e) {
                    logger.error("Problem writing surface form " + surfaceForm.getKey() + " to output file", e);
                }
                
                if ((++i % LOG_FREQUENCY) == 0) {
                    logger.info("Processed " + i + " surface forms.");
                }
            }
            
            logger.info("Done persisting");
            
        } catch (ReaderException e) {
            logger.error("Problem reding entity mentions", e);
        }
    }
    
    public void close() {
        try {
            surfaceFormsPrinter.close();
        } catch (IOException e) {
            logger.error("Problem closing surface form printer", e);
        }
    }
    
}
