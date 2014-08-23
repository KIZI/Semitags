package cz.ilasek.namedentities.index.dao.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LanguageDaoManager {
    public enum Language {
        GERMAN("de"), DUTCH("nl"), ENGLISH("en");

        private final String lang;
        

        private Language(String lang) {
            this.lang = lang;
        }
        
        public String getLang() {
            return lang;
        }
    }
    
    private Language language = Language.ENGLISH;
    
    private EntityMentionsDao entitiyMetionsDao;
    private EntitiesMentionsConcatDao entitiesMentionsConcatDao;

    @Autowired
    public void setEntitiyMetionsDao(EntityMentionsDao entitiyMetionsDao) {
        this.entitiyMetionsDao = entitiyMetionsDao;
        this.entitiyMetionsDao.setLanguage(language);
    }

    @Autowired
    public void setEntitiesMetionsConcatDao(EntitiesMentionsConcatDao entitiesMetionsConcatDao) {
        this.entitiesMentionsConcatDao = entitiesMetionsConcatDao;
        this.entitiesMentionsConcatDao.setLanguage(language);
    }    
    
    public void setLanguage(Language language) {
        this.language = language;
        entitiyMetionsDao.setLanguage(language);
        entitiesMentionsConcatDao.setLanguage(language);
    }
}
