package cz.ilasek.namedentities.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.Ostermiller.util.CSVParser;

import cz.ilasek.namedentities.index.dao.redis.GenericRedisDao;
import cz.ilasek.namedentities.writer.WriterException;


public class CsvToRedis 
{
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
     */
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException, WriterException
    {
        if (args.length != 3) {
            System.err.println("Usaage: java -jar csvToRedis.jar inputFile redisHost redisDatabase");
            logger.error("Usaage: java -jar csvToRedis.jar inputFile redisHost redisDatabase");
            return;
        }

        String file = args[0];
        String redisHost = args[1];
        int redisDatabase = new Integer(args[2]);
        
        GenericRedisDao redisDao = new GenericRedisDao(redisHost, redisDatabase);
        CSVParser fileReader = new CSVParser(new FileInputStream(file));
        String[] line = fileReader.getLine();
        
        logger.info("Indexing file " + file);
        int i = 0;
        while ((line = fileReader.getLine()) != null) {
            redisDao.set(line[0], line[1]);
            
            if ((++i % 50000) == 0) {
                logger.info("Indexed " + i + " records");
            }
        }
        
        logger.info("Closing connection to Redis");
        redisDao.close();
        logger.info("Connection closed");
        logger.info("DONE");
    }
}
