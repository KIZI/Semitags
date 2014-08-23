package cz.ilasek.namedentities.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Ostermiller.util.CSVPrinter;

import cz.ilasek.namedentities.index.models.EntitySurfaceFormMention;
import cz.ilasek.namedentities.set.EntitySet;
import cz.ilasek.namedentities.set.SurfaceFormSet;

public class CSVWriter implements Writer {
    
    private final Logger logger = LoggerFactory.getLogger(CSVWriter.class);
    
    public static final String ARTICLE_CSV_FILE = "article.csv";
    public static final String CONCATENATED_ARTICLE_FOLDER = "concat_articles";
    public static final String PARAGRAPH_CSV_FILE = "paragraph.csv";
    public static final String ENTITIES_MENTIONS_CSV_FILE = "entities_mentions.csv";
    public static final String ENTITIES_CSV_FILE = "entities.csv";
    public static final String SURFACE_FORMS_CSV_FILE = "surface_forms.csv";
    
    private final CSVPrinter articlePrinter;
    private final CSVPrinter paragraphPrinter;
    private final CSVPrinter entitiesMentionsPrinter;
    private final CSVPrinter entitiesPrinter;
    private final CSVPrinter surfaceFormsPrinter;
    
    private final String targetDirectory;
    
    public CSVWriter(String targetDirectory) throws IOException {
        articlePrinter = new CSVPrinter(new FileOutputStream(new File(targetDirectory + "/" + ARTICLE_CSV_FILE)));
        paragraphPrinter = new CSVPrinter(new FileOutputStream(new File(targetDirectory + "/" + PARAGRAPH_CSV_FILE)));
        entitiesMentionsPrinter = new CSVPrinter(new FileOutputStream(new File(targetDirectory + "/" + ENTITIES_MENTIONS_CSV_FILE)));
        entitiesPrinter =  new CSVPrinter(new FileOutputStream(new File(targetDirectory + "/" + ENTITIES_CSV_FILE)));
        surfaceFormsPrinter =  new CSVPrinter(new FileOutputStream(new File(targetDirectory + "/" + SURFACE_FORMS_CSV_FILE)));
        
        this.targetDirectory = targetDirectory;
        
        prepareHealdines();
    }
    
    private void prepareHealdines() throws IOException {
        articlePrinter.writeln(new String[]{"articleId", "articleTitle"});
        paragraphPrinter.writeln(new String[]{"paragraphUri", "text"});
        entitiesMentionsPrinter.writeln(new String[]{"entityUri", "surfaceFrom", "paragraphUri"});
        entitiesPrinter.writeln(new String[]{"entityUri"});
        surfaceFormsPrinter.writeln(new String[]{"surfaceForm"});
    }
    
    public void appendConcatenatedParagraph(String articleId, String paragraphText) throws WriterException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetDirectory + "/" + CONCATENATED_ARTICLE_FOLDER + "/" + articleId + ".txt", true);
            fos.write((paragraphText + "\n\n").getBytes());
            
        } catch (FileNotFoundException e) {
            throw new WriterException("Problem writing to the file " + targetDirectory + "/" + CONCATENATED_ARTICLE_FOLDER + "/" + articleId + ".txt", e);
        } catch (IOException e) {
            throw new WriterException("Problem writing to the file " + targetDirectory + "/" + CONCATENATED_ARTICLE_FOLDER + "/" + articleId + ".txt", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new WriterException("Problem closing file " + targetDirectory + "/" + CONCATENATED_ARTICLE_FOLDER + "/" + articleId + ".txt", e);
                }
            }
        }
    }

    @Override
    public void appendArticle(String articleId, String articleTitle) throws WriterException {
        try {
            articlePrinter.writeln(new String[]{articleId, articleTitle});
        } catch (IOException e) {
            throw new WriterException("Problem writing article " + articleId + " to file", e);
        }

    }

    @Override
    public void appenParagraph(String paragraphUri, String text) throws WriterException {
        try {
            paragraphPrinter.writeln(new String[]{paragraphUri, text});
        } catch (IOException e) {
            throw new WriterException("Problem writing paragraph " + paragraphUri + " to file", e);
        }

    }

    @Override
    public void appendEntitiesMentions(Set<EntitySurfaceFormMention> entitiyMentions, String paragraphUri) throws WriterException {
        for (EntitySurfaceFormMention entityMention : entitiyMentions) {
            try {
                entitiesMentionsPrinter.writeln(new String[]{entityMention.getEntityUri(), entityMention.getSurfaceForm(), paragraphUri});
            } catch (IOException e) {
                throw new WriterException("Problems writing entity mention " + entityMention.getEntityUri() + ", " 
                        + entityMention.getSurfaceForm() + ", " + paragraphUri, e);
            }
        }

    }

    @Override
    public void persistSurfaceForms(SurfaceFormSet surfaceForms) throws WriterException {
        for (String surfaceForm : surfaceForms.getSurfaceForms()) {
            try {
                surfaceFormsPrinter.writeln(surfaceForm);
            } catch (IOException e) {
                throw new WriterException("Problem writing surface form " + surfaceForm + " to the file", e);
            }
        }

    }

    @Override
    public void persistEntities(EntitySet entities) throws WriterException {
        for (String entityUri : entities.getEntities()) {
            try {
                entitiesPrinter.writeln(entityUri);
            } catch (IOException e) {
                throw new WriterException("Problem writing entity " + entityUri + " to the file", e);
            }
        }

    }
    
    @Override
    public void close() throws WriterException {
        logger.info("Closing CSV files for writing");
        try {
            articlePrinter.close();
            paragraphPrinter.close();
            entitiesMentionsPrinter.close();
            entitiesPrinter.close();
            surfaceFormsPrinter.close();
        } catch (IOException e) {
            throw new WriterException("Problem closing CSV writers", e);
        }
    }

    @Override
    public void appendEntityMention(String entityUri, String surfaceForm, String paragraphUri) throws WriterException {
        try {
            entitiesMentionsPrinter.writeln(new String[]{entityUri, surfaceForm, paragraphUri});
        } catch (IOException e) {
            throw new WriterException("Problems writing entity mention " + entityUri + ", " 
                    + surfaceForm + ", " + paragraphUri, e);
        }        
    }

    @Override
    public void appendEntity(String entityUri) throws WriterException {
        try {
            entitiesPrinter.writeln(new String[]{entityUri});
        } catch (IOException e) {
            throw new WriterException("Problem writing entity " + entityUri + " to the file", e);
        }    
    }

    @Override
    public void appendSurfaceForm(String surfaceForm) throws WriterException {
        try {
            surfaceFormsPrinter.writeln(new String[]{surfaceForm});
        } catch (IOException e) {
            throw new WriterException("Problem writing surface form " + surfaceForm + " to the file", e);
        }    
    }    
}
