package cz.ilasek.namedentities.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.builder.ParagraphCounter;
import cz.ilasek.namedentities.writer.WriterException;


public class CountParagraphs 
{
    private static final Logger logger = LoggerFactory.getLogger(ParagraphCounter.class);
    
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException, WriterException
    {
        if (args.length != 2) {
            System.err.println("Usaage: java -jar countCoOccurrences.jar inputFolder outputFolder");
            logger.error("Usaage: java -jar countCoOccurrences.jar inputFolder outputFolder");
            return;
        }

        String inputFolder = args[0];
        String outputFolder = args[1];
        
//        EntityCoOccurrencesDao entityCoOccurrencesDao = new EntityCoOccurrencesDao(language, "localhost");
//        EntityCoOccurrencesDao entityCoOccurrencesDao = new EntityCoOccurrencesDao(language, "ner2.lmcloud.vse.cz");
//        CoOccurrenceBuilder coOccurrenceBuilder = new CoOccurrenceBuilder(inputFolder, entityCoOccurrencesDao);
        
        ParagraphCounter paragraphCounter = new ParagraphCounter(inputFolder, outputFolder);
        
        logger.info("Started processing");
        paragraphCounter.countParagraphSizes();
        logger.info("Closing connection");
        paragraphCounter.close();
        logger.info("Done building cooccurrences");
    }
}
