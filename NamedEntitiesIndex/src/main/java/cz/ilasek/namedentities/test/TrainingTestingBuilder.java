package cz.ilasek.namedentities.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.ilasek.namedentities.index.models.Article;
import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.index.models.Paragraph;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;
import cz.ilasek.namedentities.writer.CSVWriter;
import cz.ilasek.namedentities.writer.WriterException;

public class TrainingTestingBuilder {
    private static final double RANDOM_MAX_BOUND = 100d;

    private static final Logger logger = LoggerFactory.getLogger(TrainingTestingBuilder.class);
    
    private final Random random = new Random(1L);
    
    private final CSVReader inputReader;
    private final CSVWriter trainingWriter;
    private final CSVWriter testingWriter;
    private final Double percentTesting;
    
    public TrainingTestingBuilder(Double percentTesting, String inputFolder, String outputTrainingFolder, String outputTestingFolder) throws IOException {
        this.percentTesting = percentTesting;

        inputReader = new CSVReader(inputFolder);
        trainingWriter = new CSVWriter(outputTrainingFolder);
        testingWriter = new CSVWriter(outputTestingFolder);
    }
    
    public void createTrainingTestingFiles() {
        logger.info("Started processing articles");
        Set<String> testingArticles = writeArticles();
        logger.info("Started processing paragraphs");
        Set<String> testingParagraphs = writeParagraphs(testingArticles);
        logger.info("Started processing entity mentions");
        writeEntityMentions(testingParagraphs);
        logger.info("DONE processing entity mentions");
    }
    
    private Set<String> writeArticles() {
        Set<String> testingArticles = new HashSet<String>();
        
        Article article = null;
        boolean finished = false;
        
        while (!finished) {
            try {
                while ((article = inputReader.readArticle()) != null) {
                    if ((random.nextDouble() * RANDOM_MAX_BOUND) <= percentTesting) {
                        try {
                            testingWriter.appendArticle(article.getArticleId(), article.getArticleTitle());
                            testingArticles.add(article.getArticleId());
                        } catch (WriterException e) {
                            logger.error("Problem writing article " + article.getArticleId() + " " + article.getArticleTitle(), e);
                        }
                    } else {
                        try {
                            trainingWriter.appendArticle(article.getArticleId(), article.getArticleTitle());
                        } catch (WriterException e) {
                            logger.error("Problem writing article " + article.getArticleId() + " " + article.getArticleTitle(), e);
                        }
                    }
                }
                
                finished = true;
            } catch (ReaderException e) {
                if (article != null) {
                    logger.error("Problem reading article " + article.getArticleId() + " " + article.getArticleTitle(), e);
                } else {
                    logger.error("Problem reading article", e);
                }
            }
        }
        
        return testingArticles;
    }
    
    private Set<String> writeParagraphs(Set<String> testingArticles) {
        Set<String> testingParagraphs = new HashSet<String>();
        
        Paragraph paragraph = null;
        boolean finished = false;
        
        while (!finished) {
            try {
                while ((paragraph = inputReader.readParagraph()) != null) {
                    if (testingArticles.contains(paragraph.getArticleId())) {
                        try {
                            testingWriter.appendConcatenatedParagraph(paragraph.getArticleId(), paragraph.getText());
                            
                            testingParagraphs.add(paragraph.getParagraphUri());
                        } catch (WriterException e) {
                            logger.error("Problem writing paragraph " + paragraph.getParagraphUri(), e);
                        }
                    } else {
                        try {
                            trainingWriter.appenParagraph(paragraph.getParagraphUri(), paragraph.getText());
                        } catch (WriterException e) {
                            logger.error("Problem writing paragraph " + paragraph.getParagraphUri(), e);
                        }
                    }
                }
                
                finished = true;
            } catch (ReaderException e) {
                if (paragraph != null) {
                    logger.error("Problem reading paragraph " + paragraph.getParagraphUri(), e);
                } else {
                    logger.error("Problem reading paragraph", e);
                }
            }
        }
        
        return testingParagraphs;   
    }
    
    private void writeEntityMentions(Set<String> testingParagraphs) {
        EntityMentionInParagraph entityMention = null;
        boolean finished = false;
        
        while (!finished) {
            try {
                while ((entityMention = inputReader.readEntityMention()) != null) {
                    if (testingParagraphs.contains(entityMention.getParagraphUri())) {
                        try {
                            testingWriter.appendEntityMention(entityMention.getEntityUri(), entityMention.getSurfaceForm(), entityMention.getParagraphUri());
                        } catch (WriterException e) {
                            logger.error("Problem writing entity mention " + entityMention.getEntityUri() + " ... " + entityMention.getParagraphUri(), e);
                        }
                    } else {
                        try {
                            trainingWriter.appendEntityMention(entityMention.getEntityUri(), entityMention.getSurfaceForm(), entityMention.getParagraphUri());
                        } catch (WriterException e) {
                            logger.error("Problem writing entity mention " + entityMention.getEntityUri() + " ... " + entityMention.getParagraphUri(), e);
                        }
                    }
                }
                
                finished = true;
            } catch (ReaderException e) {
                if (entityMention != null) {
                    logger.error("Problem reading entity mention " + entityMention.getEntityUri() + " ... " + entityMention.getParagraphUri(), e);
                } else {
                    logger.error("Problem reading entity mention", e);
                }
            }
        }
    }
}
