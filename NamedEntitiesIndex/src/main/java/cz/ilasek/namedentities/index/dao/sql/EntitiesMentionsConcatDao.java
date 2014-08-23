package cz.ilasek.namedentities.index.dao.sql;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager.Language;

@Repository
public class EntitiesMentionsConcatDao {
    private JdbcTemplate jdbcTemplate;
    
    private String entitiesMentionsConcatTable;
    
    public EntitiesMentionsConcatDao() {
        setLanguage(Language.ENGLISH);
    }
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public void setLanguage(Language language) {
        entitiesMentionsConcatTable = language.getLang() + "_entities_mentions_concat";
    }
    
    public int insertMentions(String mentionsText, long entityId) {
        int mentCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + entitiesMentionsConcatTable + " WHERE entity_id = ?",
                new Object[] {entityId});
        
        if (mentCount <= 0)
            return jdbcTemplate.update("INSERT INTO " + entitiesMentionsConcatTable + " (entity_id, mentions) VALUES (?, ?)", 
                    entityId, mentionsText);
        else
            return 0;
    }    
}
