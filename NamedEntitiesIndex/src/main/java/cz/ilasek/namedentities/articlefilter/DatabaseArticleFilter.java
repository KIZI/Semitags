package cz.ilasek.namedentities.articlefilter;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.model.WikiModel;

import org.xml.sax.SAXException;

import cz.ilasek.namedentities.index.InternalStoreConverter;
import cz.ilasek.namedentities.index.Link;
import cz.ilasek.namedentities.index.Paragraph;
import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;

public class DatabaseArticleFilter implements IArticleFilter {
    
    private final WikiModel wikiModel;
    private final EntityMentionsDao entityMetionsDao;
    
    private int count = 1;
    
    public DatabaseArticleFilter(EntityMentionsDao entityMetionsDao) {
        wikiModel = new WikiModel("http://nl.wikipedia.org/wiki/${image}", "http://nl.wikipedia.org/wiki/${title}");
        this.entityMetionsDao = entityMetionsDao;
    }

    @Override
    public void process(WikiArticle article, Siteinfo siteinfo)
            throws SAXException {
        InternalStoreConverter isc = new InternalStoreConverter();
        wikiModel.render(isc, article.getText());
        System.out.println((count++) + " ... Parsing article " + article.getTitle());
        long articleId = entityMetionsDao.insertArticle(article.getTitle());
        for (Paragraph paragraph : isc.getParagraphs()) {
            if (!paragraph.getText().trim().equals("")) {
                long paragraphId = entityMetionsDao.insertParagraph(paragraph.getText(), articleId);
                if (paragraph.hasLinks()) {
    //                System.out.println(paragraph.getText());
    //                System.out.println("=========");
                    for (Link link : paragraph.getLinks()) {
                        entityMetionsDao.insertMention(link.getTarget(), link.getSurfaceForm(), paragraphId);
    //                    System.out.println(link.getSurfaceForm() + " => " + link.getTarget());
                    }
    //                System.out.println("===============");
    //                System.out.println("===============");
                }
            }
        }
    }
}
