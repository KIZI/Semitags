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

public class GenericCounter {
    private static final String PARAGRAPH_COUNTER_FILE = "paragraph_counter.csv";
    
    private static final int LOG_FREQUENCY = 50000;
    
    private static final Logger logger = LoggerFactory.getLogger(GenericCounter.class);
    
    private final CSVReader inputReader;
    
    private final CSVPrinter counterPrinter;
    
    private final int sizeToBeLogged;
    
    public GenericCounter(String inputFolder, String outputFolder, int sizeToBeLogged) throws IOException {
        inputReader = new CSVReader(inputFolder);
        counterPrinter = new CSVPrinter(new FileOutputStream(new File(outputFolder + "/" + PARAGRAPH_COUNTER_FILE)));
        counterPrinter.writeln(new String[]{"ContainsCount (Size)", "HowManyOfTheTotal"});
        this.sizeToBeLogged = sizeToBeLogged;
    }

    public void countParagraphSizes() {
        logger.info("Reading entities map");
        EntityMentionInParagraph emip = null;
        String lastRecord = null;
        String record = null;
        Map<Integer, Integer> sizes = new HashMap<Integer, Integer>();
        
        int i = 0;
        int recordSize = 0;
        try {
            while((emip = inputReader.readEntityMention()) != null) {
                recordSize++; 
                
                if (recordSize > sizeToBeLogged) {
                    logger.info("LARGEST PARAGRAPH " + emip.getParagraphUri());
                }
                
                if (!emip.getParagraphUri().equals(lastRecord)) {
                    storeParagraphSize(sizes, recordSize);
                    
                    recordSize = 0;
                    lastRecord = emip.getParagraphUri();
                }
                
                if ((++i % LOG_FREQUENCY) == 0) {
                    logger.info("Processed " + i + " mentions.");
                }
            }
            
            logger.info("Storing last paragraph of cooccurrences");
            storeParagraphSize(sizes, recordSize);
            logger.info("Entity cooccurrences indexed");
            
            persistParagraphSizes(sizes);
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
                counterPrinter.writeln(new String[]{ paragraphSize.getKey().toString(),  paragraphSize.getValue().toString()});
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
            counterPrinter.close();
        } catch (IOException e) {
            logger.error("Problem closing cooccurrence printer", e);
        }
    }
    
}
