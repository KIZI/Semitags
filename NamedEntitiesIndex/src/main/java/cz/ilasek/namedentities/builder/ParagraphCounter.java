package cz.ilasek.namedentities.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Ostermiller.util.CSVPrinter;

import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;

public class ParagraphCounter {
    private static final String PARAGRAPH_COUNTER_FILE = "paragraph_counter.csv";
    
    private static final int LOG_FREQUENCY = 50000;
    
    private static final Logger logger = LoggerFactory.getLogger(ParagraphCounter.class);
    
    private final CSVReader inputReader;
    
    private final CSVPrinter paragraphCounterPrinter;
    
    public ParagraphCounter(String inputFolder, String outputFolder) throws IOException {
        inputReader = new CSVReader(inputFolder);
        paragraphCounterPrinter = new CSVPrinter(new FileOutputStream(new File(outputFolder + "/" + PARAGRAPH_COUNTER_FILE)));
        paragraphCounterPrinter.writeln(new String[]{"LinksCount", "ParagraphCount"});        
    }

    public void countParagraphSizes() {
        logger.info("Reading entities map");
        EntityMentionInParagraph emip = null;
        String lastParagraphUri = null;
        Map<Integer, Integer> paragraphSizes = new HashMap<Integer, Integer>();
        
        int i = 0;
        int paragraphSize = 0;
        try {
            while((emip = inputReader.readEntityMention()) != null) {
                paragraphSize++;
                
                if (paragraphSize > 24650)
                    logger.info("LARGEST PARAGRAPH " + emip.getParagraphUri());
                
                if (!emip.getParagraphUri().equals(lastParagraphUri)) {
                    storeParagraphSize(paragraphSizes, paragraphSize);
                    
                    paragraphSize = 0;
                    lastParagraphUri = emip.getParagraphUri();
                }
                
                if ((++i % LOG_FREQUENCY) == 0) {
                    logger.info("Processed " + i + " mentions.");
                }
            }
            
            logger.info("Storing last paragraph of cooccurrences");
            storeParagraphSize(paragraphSizes, paragraphSize);
            logger.info("Entity cooccurrences indexed");
            
            persistParagraphSizes(paragraphSizes);
            logger.info("Entity cooccurrences persisted");
        } catch (ReaderException e) {
            logger.error("Problem reading entity mentions", e);
        }
    }

    private void persistParagraphSizes(Map<Integer, Integer> paragraphSizes) {
        int i = 0;
        
        logger.info("Persisting " + paragraphSizes.size() + " cooccurrences");
        
        for (Entry<Integer, Integer> paragraphSize : paragraphSizes.entrySet()) {
            
            try {
                paragraphCounterPrinter.writeln(new String[]{ paragraphSize.getKey().toString(),  paragraphSize.getValue().toString()});
            } catch (IOException e) {
                logger.error("Problem writing cooccurrence " + paragraphSize.getKey() + " to output file", e);
            }
            
            if ((++i % LOG_FREQUENCY) == 0) {
                logger.info("Processed " + i + " cooccurrences.");
            }
        }        
    }
    
    private void storeParagraphSize(Map<Integer, Integer> paragraphSizes, int paragraphSize) {
        int count = 0;
        if (paragraphSizes.containsKey(paragraphSize)) {
            count = paragraphSizes.get(paragraphSize);
        }
        
        count++;
        
        paragraphSizes.put(paragraphSize, count);
    }
    
    public void close() {
        try {
            paragraphCounterPrinter.close();
        } catch (IOException e) {
            logger.error("Problem closing cooccurrence printer", e);
        }
    }
    
}
