package cz.ilasek.namedentities.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.reader.ReaderException;
import cz.ilasek.namedentities.test.ArticlesTester;
import cz.ilasek.namedentities.writer.WriterException;

public class TestingArticles {
    private static final Logger logger = LoggerFactory.getLogger(CsvToRedis.class);
    
    /**
     * Reads the two column CSV file skips first line and then stores each line in redis as key (1. column) 
     * and value (2. column).
     * @param args
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws WriterException
     * @throws ReaderException 
     */
    public static void main( String[] args ) throws ReaderException, IOException
    {
        if (args.length != 1) {
            System.err.println("Usage: java -jar testingArticles.jar testingFolder");
            logger.error("Usage: java -jar testingArticles.jar testingFolder");
            return;
        }

        String testingDirectory = args[0];
        
        ArticlesTester articlesTester = new ArticlesTester(testingDirectory, testingDirectory + "/concat_articles");
        articlesTester.testArticles(10);
    }
}
