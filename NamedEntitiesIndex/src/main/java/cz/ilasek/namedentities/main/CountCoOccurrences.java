package cz.ilasek.namedentities.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.builder.CoOccurrenceInMemoryBuilder;
import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager.Language;
import cz.ilasek.namedentities.writer.WriterException;


public class CountCoOccurrences 
{
    private static final Logger logger = LoggerFactory.getLogger(CountCoOccurrences.class);
    
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException, WriterException
    {
        if (args.length != 3) {
            System.err.println("Usaage: java -jar countCoOccurrences.jar inputFolder outputFolder language");
            logger.error("Usaage: java -jar countCoOccurrences.jar inputFolder outputFolder language");
            return;
        }

        String inputFolder = args[0];
        String outputFolder = args[1];
        String lang = args[2];
        
        Language language;
        if (lang.equals("en")) {
            language = Language.ENGLISH;            
        } else if (lang.equals("de")) {
            language = Language.GERMAN;            
        } else if (lang.equals("nl")) {
            language = Language.DUTCH;            
        } else {
            System.err.println("Only en, de or nl are supported as language options - provided " + lang);
            logger.error("Only en, de or nl are supported as language options - provided " + lang);
            
            return;
        }

//        EntityCoOccurrencesDao entityCoOccurrencesDao = new EntityCoOccurrencesDao(language, "localhost");
//        EntityCoOccurrencesDao entityCoOccurrencesDao = new EntityCoOccurrencesDao(language, "ner2.lmcloud.vse.cz");
//        CoOccurrenceBuilder coOccurrenceBuilder = new CoOccurrenceBuilder(inputFolder, entityCoOccurrencesDao);
        
        CoOccurrenceInMemoryBuilder coOccurrenceInMemoryBuilder = new CoOccurrenceInMemoryBuilder(inputFolder, outputFolder, language);
        
        logger.info("Started processing");
        coOccurrenceInMemoryBuilder.countCoOccurrences();
        logger.info("Closing connection");
        coOccurrenceInMemoryBuilder.close();
        logger.info("Done building cooccurrences");
    }
}
