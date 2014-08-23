package cz.ilasek.namedentities.index.dao.sql;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SentenceOccurrencesDao {
    private JdbcTemplate jdbcTemplate;
    private String sentenceOccurrencesTable = "nl_sentence_occurrences";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public int insertSentenceOccurrence(long entityId, long paragraphId, String subject, String verb, String object) 
    {
        subject = (subject.equals("")) ? null : subject;
        verb = (verb.equals("")) ? null : verb;
        object = (object.equals("")) ? null : object;
        
        int mentCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + sentenceOccurrencesTable + " WHERE entity_id = ? AND paragraph_id = ? " +
                    "AND subject = ? AND object = ? AND verb = ?",
                new Object[] {entityId, paragraphId, subject, verb, object}); 
        
        if (mentCount <= 0)
            return jdbcTemplate.update("INSERT INTO " + sentenceOccurrencesTable + " (entity_id, paragraph_id, subject, verb, object) VALUES (?, ?, ?, ?, ?)", 
                    entityId, paragraphId, subject, verb, object); 
        else
            return 0;
    }
    
    public int getSimilarOccurencesCount(long entityId, String subject, String verb, String object) {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + sentenceOccurrencesTable +
                " WHERE (entity_id = ?) AND ((subject LIKE ?) OR (verb LIKE ?) OR (object LIKE ?))", 
                new Object[] {entityId, "%" + subject + "%", "%" + verb + "%", "%" + object + "%"});
    }
}
