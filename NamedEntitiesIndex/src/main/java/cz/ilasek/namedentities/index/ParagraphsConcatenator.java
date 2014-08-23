package cz.ilasek.namedentities.index;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.ilasek.namedentities.index.callback.ConcatenateParagraphHandler;
import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;
import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager;
import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager.Language;

public class ParagraphsConcatenator {
    public static void main(String[] args) {
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[] {"appconfig.xml"});
        
        LanguageDaoManager languageDaoManager = context.getBean(LanguageDaoManager.class);
        languageDaoManager.setLanguage(Language.DUTCH);
        EntityMentionsDao entityMentionsDao = context.getBean(EntityMentionsDao.class);
        entityMentionsDao.processFilteredMentionedEntities(context.getBean(ConcatenateParagraphHandler.class));
    }
}