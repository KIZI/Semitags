package cz.ilasek.namedentities.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.builder.SurfaceFormIndexBuilder;
import cz.ilasek.namedentities.writer.WriterException;


public class IndexSurfaceForms 
{
    private static final Logger logger = LoggerFactory.getLogger(IndexSurfaceForms.class);
    
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException, WriterException
    {
        if (args.length != 2) {
            System.err.println("Usaage: java -jar indexSurfaceForms.jar inputFolder outputFolder");
            logger.error("Usaage: java -jar indexSurfaceForms.jar inputFolder outputFolder");
            return;
        }

        String inputFolder = args[0];
        String outputFolder = args[1];
        
        SurfaceFormIndexBuilder surfaceFormIndexBuilder = new SurfaceFormIndexBuilder(inputFolder, outputFolder);
        
        logger.info("Started processing");
        surfaceFormIndexBuilder.indexSurfaceForms();
        logger.info("Done building surface form index");
        logger.info("Closing index builder");
        surfaceFormIndexBuilder.close();
        logger.info("Connection closed. DONE.");
    }
}
