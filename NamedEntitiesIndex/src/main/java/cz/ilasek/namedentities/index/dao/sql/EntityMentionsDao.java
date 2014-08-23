package cz.ilasek.namedentities.index.dao.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager.Language;
import cz.ilasek.namedentities.index.mapper.EntityMapper;
import cz.ilasek.namedentities.index.mapper.EntityMentionMapper;
import cz.ilasek.namedentities.index.models.Entity;
import cz.ilasek.namedentities.index.models.EntityMention;

@Repository
public class EntityMentionsDao {

    private JdbcTemplate jdbcTemplate;
    
    private String articlesTable;
    private String entitiesTable;
    private String cooccurrencesTable;
    private String entitiesMentionsTable;
    private String entitiesMentionsFilterTable;
    private String paragraphsTable;
    private String surfaceFormsTable;

    public EntityMentionsDao() {
        setLanguage(Language.ENGLISH);
    }
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setLanguage(Language language) {
        articlesTable = language.getLang() + "_articles";
        entitiesTable = language.getLang() + "_entities";
        cooccurrencesTable = language.getLang() + "_cooccurrences";
        entitiesMentionsTable = language.getLang() + "_entities_mentions";
        entitiesMentionsFilterTable = language.getLang() + "_entities_mentions_filter";
        paragraphsTable = language.getLang() + "_paragraphs";
        surfaceFormsTable = language.getLang() + "_surface_forms";
    }
    
    public int insertMention(String entityUri, String surfaceForm, long paragraphId) {
        long surfaceFormId = insertSurfaceForm(surfaceForm);
        long entityId = insertEntity(entityUri);
        
        int mentCount = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + entitiesMentionsTable + " WHERE entity_id = ? AND paragraph_id = ? AND surface_form_id = ?",
                new Object[] {entityId, paragraphId, surfaceFormId}); 
        
        if (mentCount <= 0)
            return jdbcTemplate.update("INSERT INTO " + entitiesMentionsTable + " (entity_id, paragraph_id, surface_form_id) VALUES (?, ?, ?)", 
                    entityId, paragraphId, surfaceFormId); 
        else
            return 0;
    }
    
    public long insertArticle(final String articleUrl) {
        Long articleId;
        try {
            articleId = jdbcTemplate.queryForObject("SELECT article_id FROM " + articlesTable + " WHERE url = ?", new Object[] {articleUrl}, Long.class);
        }
        catch(EmptyResultDataAccessException e) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement("INSERT INTO " + articlesTable + " (url) VALUES (?)", new String[] {"article_id"});
                    ps.setString(1, articleUrl);
                    return ps;
                }
            },
            keyHolder);
            articleId = keyHolder.getKey().longValue();        
        }
        
        return articleId;
    }
    
    public long insertParagraph(final String paragraphText, final long articleId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps =
                    connection.prepareStatement("INSERT INTO " + paragraphsTable + " (paragraph_text, article_id) VALUES (?, ?)", new String[] {"paragraph_id"});
                ps.setString(1, paragraphText);
                ps.setLong(2, articleId);
                return ps;
            }
        },
        keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public List<Entity> findCandidates(String entityName) {
        return jdbcTemplate.query("SELECT entity_id, uri FROM " + entitiesTable +
        		" JOIN " + entitiesMentionsFilterTable + " USING (entity_id)" +
        		" JOIN " + surfaceFormsTable + " USING(surface_form_id) " +
        		"WHERE surface_form LIKE ? GROUP BY entity_id", 
        		new Object[]{entityName}, new EntityMapper());
    }
    
    public List<String> getEntityMentionParagraphs(long entityId, int limit) {
        return jdbcTemplate.queryForList("SELECT paragraph_text FROM " + entitiesMentionsTable +
        		" JOIN " + paragraphsTable + " USING(paragraph_id) WHERE entity_id = ? LIMIT " + limit, 
        		new Object[] {entityId}, String.class);
    }
    
    public void getFilteredEntityMentionParagraphs(final long entityId, RowCallbackHandler rowCallbackHandler, final int limit) {
        jdbcTemplate.query(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement("SELECT paragraph_text FROM " + entitiesMentionsFilterTable + " emf" + 
                                " JOIN " + entitiesMentionsTable + " em" +
                                		" ON ((emf.entity_id = em.entity_id) AND (emf.surface_form_id = em.surface_form_id))" +
                                " JOIN " + paragraphsTable + " USING(paragraph_id) WHERE emf.entity_id = ?" +
                                " LIMIT " + limit);
                    ps.setLong(1, entityId);
                    ps.setFetchSize(Integer.MIN_VALUE);
                    
                    return ps;
                }
            }, 
            rowCallbackHandler
        );
    }    
    
    public List<EntityMention> getEntityMentions(long entityId, int limit) {
        return jdbcTemplate.query("SELECT entity_id, paragraph_text, paragraph_id, surface_form FROM " + entitiesMentionsTable +
                " JOIN " + paragraphsTable + " USING (paragraph_id)" +
                "JOIN " + surfaceFormsTable + " USING(surface_form_id) " +
                "WHERE entity_id = ? LIMIT " + limit, 
                new Object[]{entityId}, new EntityMentionMapper());
    }    
    
    public int getCoOccurrenceCount(long entityIdA, long entityIdB) {
        return jdbcTemplate.queryForInt("SELECT SUM(cooccurrence_count) FROM " + 
                cooccurrencesTable + " co WHERE entity_id = ? AND  cooccur_entity_id = ?", 
                new Object[] {entityIdA, entityIdB});
    }

    // Unfinished - this aggregation would probably not work, as it is difficult 
    // to exclude entities that are from the same group. 
//    public int getCoOccurrenceCounts(long entityIdA, long entityIdB) {
//        return jdbcTemplate.queryForInt("SELECT SUM(cooccurrence_count) FROM " + 
//                entitiesMentionsFilterTable + " em1 " +
//                "JOIN " + cooccurrencesTable + " co ON (em1.entity_id = co.entity_id) " +
//                "JOIN " + entitiesMentionsFilterTable + " em2 ON (co.cooccur_entity_id = em2.entity_id) " +
//                "WHERE (em1.entity_id != co.cooccur_entity_id) AND (em1.surface_form_id != )", 
//                new Object[] {entityIdA, entityIdB});
//    }    
    
    public int getOccurrenceCount(long entityId) {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + entitiesMentionsTable + " em " +
                "WHERE em.entity_id = ?", 
                new Object[] {entityId});
    }        
    
    private long insertEntity(final String entityUri) {
        Long entityId;
        try {
            entityId = jdbcTemplate.queryForObject("SELECT entity_id FROM " + entitiesTable + " WHERE uri = ?", new Object[] {entityUri}, Long.class);
        }
        catch(EmptyResultDataAccessException e) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement("REPLACE INTO " + entitiesTable + " (uri) VALUES(?)", new String[] {"entity_id"});
                    ps.setString(1, entityUri);
                    return ps;
                }
            },
            keyHolder);
            
            entityId = keyHolder.getKey().longValue();
        }
        
        return entityId;
    }
    
    private long insertSurfaceForm(final String surfaceForm) {
        Long surfaceFormId;
        try {
            surfaceFormId = jdbcTemplate.queryForObject("SELECT surface_form_id FROM " + surfaceFormsTable + " WHERE surface_form = ?", new Object[] {surfaceForm}, Long.class);
        } catch (EmptyResultDataAccessException e) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement("REPLACE INTO " + surfaceFormsTable + " (surface_form) VALUES(?)", new String[] {"surface_form_id"});
                    ps.setString(1, surfaceForm);
                    return ps;
                }
            },
            keyHolder);
            
            surfaceFormId = keyHolder.getKey().longValue();
        }
        
        return surfaceFormId;
    }
    
    public void processFilteredMentionedEntities(RowCallbackHandler rowCallbackHandler) {
        jdbcTemplate.query(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement("SELECT DISTINCT entity_id FROM " + entitiesMentionsFilterTable);
                    ps.setFetchSize(Integer.MIN_VALUE);
                    return ps;
                }
            }, 
            rowCallbackHandler
        );
    }
}
