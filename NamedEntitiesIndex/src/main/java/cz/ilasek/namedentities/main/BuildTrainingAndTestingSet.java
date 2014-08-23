package cz.ilasek.namedentities.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.test.TrainingTestingBuilder;
import cz.ilasek.namedentities.writer.WriterException;


/**
 * Hello world!
 *
 */
public class BuildTrainingAndTestingSet 
{
    private static final Logger logger = LoggerFactory.getLogger(BuildTrainingAndTestingSet.class);
    
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException, WriterException
    {
        if (args.length != 4) {
            System.out.println("Usaage: java -jar buildTrainingTestingSet.jar  percentTestingArticles inputFolder trainingOutputFolder testingOutputFolder");
            return;
        }

        Double percentTesting = new Double(args[0]);
        String inputFolder = args[1];
        String outputTrainingFolder = args[2];
        String outputTestingFolder = args[3];
        
        logger.info("Started processing");

        TrainingTestingBuilder trainingTestingBuilder = new TrainingTestingBuilder(percentTesting, inputFolder, outputTrainingFolder, outputTestingFolder);
        trainingTestingBuilder.createTrainingTestingFiles();
        
        logger.info("Done");
    }
}
