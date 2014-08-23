package cz.ilasek.namedentities.articlefilter;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.model.WikiModel;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.index.InternalStoreConverter;
import cz.ilasek.namedentities.index.Link;
import cz.ilasek.namedentities.index.Paragraph;
import cz.ilasek.namedentities.index.models.EntitySurfaceFormMention;
import cz.ilasek.namedentities.set.EntitySet;
import cz.ilasek.namedentities.set.SurfaceFormSet;
import cz.ilasek.namedentities.writer.Writer;
import cz.ilasek.namedentities.writer.WriterException;

public class WriterArticleFilter implements IArticleFilter {
    
    private static final int LOG_EVERY_X_ARTICLES = 10000;
    
    private static final Logger logger = LoggerFactory.getLogger(WriterArticleFilter.class);
    
    private final WikiModel wikiModel;
    private final Writer writer;
    private final EntitySet entitySet;
    private final SurfaceFormSet surfaceFormSet;
    
    private int count = 1;
    
    /**
     * 
     * @param wikiBaseUrl For example http://en.wikipedia.org/wiki/. Must end with the slash (/).
     */
    public WriterArticleFilter(Writer writer, EntitySet entitySet, SurfaceFormSet surfaceFormSet, String wikiBaseUrl) {
        wikiModel = new WikiModel(wikiBaseUrl + "${image}", wikiBaseUrl + "${title}");
        this.writer = writer;
        this.entitySet = entitySet;
        this.surfaceFormSet = surfaceFormSet;
    }

    @Override
    public void process(WikiArticle article, Siteinfo siteinfo)
            throws SAXException 
    {
        InternalStoreConverter isc = new InternalStoreConverter();
        wikiModel.render(isc, article.getText());
        
        if (((++count) % LOG_EVERY_X_ARTICLES) == 0) {
            logger.info(count + " ... Parsing article " + article.getTitle());
        }
        
        if (!article.getTitle().contains(":") && !article.getTitle().contains("(disambiguation")) {
            String articleId = article.getId();
            
            try {
                writer.appendArticle(articleId, article.getTitle());
            } catch (WriterException e) {
                logger.error("Problem writing article " + articleId, e);
            }
            int paragraphCount = 0;
            
            for (Paragraph paragraph : isc.getParagraphs()) {
                if (!StringUtils.isBlank(paragraph.getText())) {
                    paragraphCount++;
                    String paragraphId = articleId + "#" + paragraphCount;
                    try {
                        writer.appenParagraph(paragraphId, paragraph.getText());
                    } catch (WriterException e) {
                        logger.error("Problem writing paragraph " + paragraphId, e);
                    }
                    if (paragraph.hasLinks()) {
                        Set<EntitySurfaceFormMention> entityMentions = new HashSet<EntitySurfaceFormMention>();
                        
                        for (Link link : paragraph.getLinks()) {
                            if (link.getTarget().startsWith("http://") && (link.getTarget().indexOf(":", 7) == -1)) {
                                try {
                                    writer.appendEntity(link.getTarget());
                                } catch (WriterException e) {
                                    logger.error("Problem appending entity " + link.getTarget(), e);
                                }
//                                entitySet.addEntity(link.getTarget());
                                try {
                                    writer.appendSurfaceForm(link.getSurfaceForm());
                                } catch (WriterException e) {
                                    logger.error("Problem appending surface form " + link.getSurfaceForm(), e);
                                }
//                                surfaceFormSet.addSurfaceForm(link.getSurfaceForm());
                                entityMentions.add(new EntitySurfaceFormMention(link.getTarget(), link.getSurfaceForm()));
                            }
                        }
                        try {
                            writer.appendEntitiesMentions(entityMentions, paragraphId);
                        } catch (WriterException e) {
                            logger.error("Problem appending entitiy mentions from paragraph " + paragraphId, e);
                        }
                    }
                }
            }
        }
    }

    public EntitySet getEntitySet() {
        return entitySet;
    }

    public SurfaceFormSet getSurfaceFormSet() {
        return surfaceFormSet;
    }
}
