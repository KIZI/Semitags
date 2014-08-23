package cz.ilasek.namedentities.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Ostermiller.util.CSVParser;

import cz.ilasek.namedentities.index.models.Article;
import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.index.models.Paragraph;
import cz.ilasek.namedentities.writer.CSVWriter;

public class CSVReader implements Reader {
    
    private final Logger logger = LoggerFactory.getLogger(CSVReader.class);

    private final CSVParser articleParser;
    private final CSVParser paragraphParser;
    private final CSVParser entitiesMentionsParser;
    private final CSVParser entitiesParser;
    private final CSVParser surfaceFormsParser;
    
    public CSVReader(String sourceDirectory) throws IOException {
        articleParser = new CSVParser(new FileInputStream(new File(sourceDirectory + "/" + CSVWriter.ARTICLE_CSV_FILE)));
        paragraphParser = new CSVParser(new FileInputStream(new File(sourceDirectory + "/" + CSVWriter.PARAGRAPH_CSV_FILE)));
        entitiesMentionsParser = new CSVParser(new FileInputStream(new File(sourceDirectory + "/" + CSVWriter.ENTITIES_MENTIONS_CSV_FILE)));
        entitiesParser =  new CSVParser(new FileInputStream(new File(sourceDirectory + "/" + CSVWriter.ENTITIES_CSV_FILE)));
        surfaceFormsParser =  new CSVParser(new FileInputStream(new File(sourceDirectory + "/" + CSVWriter.SURFACE_FORMS_CSV_FILE)));
        
        skipHealdines();
    }
    
    private void skipHealdines() throws IOException {
        articleParser.getLine();
        paragraphParser.getLine();
        entitiesMentionsParser.getLine();
        entitiesParser.getLine();
        surfaceFormsParser.getLine();
    }    
    
    @Override
    public Article readArticle() throws ReaderException {
        try {
            String[] line = articleParser.getLine();
            
            if (line == null) {
                return null;
            } else {
                return new Article(line[0], line[1]);
            }
        } catch (IOException e) {
            throw new ReaderException("Problem reading article", e);
        }
    }

    @Override
    public Paragraph readParagraph() throws ReaderException {
        try {
            String[] line = paragraphParser.getLine();
            
            if (line == null) {
                return null;
            } else {
                return new Paragraph(line[0], line[1]);
            }
        } catch (IOException e) {
            throw new ReaderException("Problem reading paragraph", e);
        }
    }

    @Override
    public EntityMentionInParagraph readEntityMention() throws ReaderException {
        try {
            String[] line = entitiesMentionsParser.getLine();
            
            if (line == null) {
                return null;
            } else {
                return new EntityMentionInParagraph(line[0], line[1], line[2]);
            }
        } catch (IOException e) {
            throw new ReaderException("Problem reading entitiy mention", e);
        }
    }

    @Override
    public String readSurfaceForm() throws ReaderException {
        try {
            String[] line = surfaceFormsParser.getLine();
            
            if (line == null) {
                return null;
            } else {
                return line[0];
            }
        } catch (IOException e) {
            throw new ReaderException("Problem reading article", e);
        }
    }

    @Override
    public String readEntity() throws ReaderException {
        try {
            String[] line = entitiesParser.getLine();
            
            if (line == null) {
                return null;
            } else {
                return line[0];
            }
        } catch (IOException e) {
            throw new ReaderException("Problem reading article", e);
        }
    }

    @Override
    public void close() throws ReaderException {
        logger.info("Closing CSV files for reading");
        try {
            articleParser.close();
            paragraphParser.close();
            entitiesMentionsParser.close();
            entitiesParser.close();
            surfaceFormsParser.close();
        } catch (IOException e) {
            throw new ReaderException("Problem closing CSV readers", e);
        }
    }

}
