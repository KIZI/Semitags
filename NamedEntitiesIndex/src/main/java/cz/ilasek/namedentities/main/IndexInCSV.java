package cz.ilasek.namedentities.main;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.WikiXMLParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.articlefilter.WriterArticleFilter;
import cz.ilasek.namedentities.set.EntitySet;
import cz.ilasek.namedentities.set.SurfaceFormSet;
import cz.ilasek.namedentities.writer.CSVWriter;
import cz.ilasek.namedentities.writer.WriterException;


/**
 * Hello world!
 *
 */
public class IndexInCSV 
{
    private static final Logger logger = LoggerFactory.getLogger(IndexInCSV.class);
    
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException, WriterException
    {
        if (args.length != 3) {
            System.out.println("Usaage: java -jar indexInCsv.jar dumpDirectory wikiBaseUrl targetDirectory");
        }
        
        String dumpDirectory = args[0];
        String wikiBaseUrl = args[1];
        String targetDirectory = args[2];
        
        File directory = new File(dumpDirectory);
        CSVWriter csvWriter = new CSVWriter(targetDirectory);
        
        for (String file : directory.list()) {
            logger.info("Reading: " + directory.getAbsolutePath() + "/" + file);
            try {
                InputStream in = new FileInputStream(directory.getAbsolutePath() + "/" + file);
                
                
                IArticleFilter handler = new WriterArticleFilter(csvWriter, EntitySet.getInstance(), SurfaceFormSet.getInstance(), wikiBaseUrl);
                WikiXMLParser wxp = new WikiXMLParser(in, handler);
                wxp.parse();
            } catch (FileNotFoundException e) {
                logger.warn("Skipping file " + file + " - File not found", e);
            }
        }
        
//        csvWriter.persistEntities(EntitySet.getInstance());
//        csvWriter.persistSurfaceForms(SurfaceFormSet.getInstance());
        
        csvWriter.close();
    }
}
