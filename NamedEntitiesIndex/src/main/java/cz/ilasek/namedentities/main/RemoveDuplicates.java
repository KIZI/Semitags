package cz.ilasek.namedentities.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.index.dao.sql.RedirectsDao;
import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;
import cz.ilasek.namedentities.writer.CSVWriter;
import cz.ilasek.namedentities.writer.WriterException;

public class RemoveDuplicates {
//    private static final Logger logger = LoggerFactory.getLogger(RemoveDuplicates.class);
    
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException, WriterException, ReaderException
    {
        if (args.length != 3) {
            System.out.println("Usaage: java -jar removeDuplicates.jar sourceDirectory targetDirectory wikiBaseUrl");
            return;
        }
        
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[] {"resources/appconfig.xml"});
        
        String sourceDirectory = args[0];
        String targetDirectory = args[1];
        String wikiBaseUrl = args[2];
        CSVReader csvReader = new CSVReader(sourceDirectory);
        CSVWriter csvWriter = new CSVWriter(targetDirectory);
        
        RedirectsDao redirectsDao = context.getBean(RedirectsDao.class);
        redirectsDao.init(csvReader, wikiBaseUrl);
        
        EntityMentionInParagraph emp;
        while ((emp = csvReader.readEntityMention()) != null) {
            if (redirectsDao.isRedirected(emp.getEntityUri())) {
                csvWriter.appendEntityMention(redirectsDao.getTarget(emp.getEntityUri()), emp.getSurfaceForm(), emp.getParagraphUri());
            } else {
                csvWriter.appendEntityMention(emp.getEntityUri(), emp.getSurfaceForm(), emp.getParagraphUri());
            }
        }
        
        String entity;
        while ((entity = csvReader.readEntity()) != null) {
          if (!redirectsDao.isRedirected(entity)) {
              csvWriter.appendEntity(entity);
          }            
//            if (redirectsDao.isRedirected(entity)) {
//                csvWriter.appendEntity(redirectsDao.getTarget(entity));
//            } else {
//                csvWriter.appendEntity(entity);
//            }
        }
        
        csvReader.close();
        csvWriter.close();
    }
}
