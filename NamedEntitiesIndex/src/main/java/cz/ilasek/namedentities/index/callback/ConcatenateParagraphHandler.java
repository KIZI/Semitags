package cz.ilasek.namedentities.index.callback;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.LimitExceededException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;

import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;

public class ConcatenateParagraphHandler implements RowCallbackHandler {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final String targetDir;
    
    private int count = 0;
    private long start;
    
    private EntityMentionsDao entityMentionsDao;
    
    public ConcatenateParagraphHandler(String targetDir) {
        this.targetDir = targetDir;
        start = System.currentTimeMillis();
    }
    
    @Autowired
    public void setEntityMentionsDao(EntityMentionsDao entityMentionsDao) {
        this.entityMentionsDao = entityMentionsDao;
    }
    
    @Override
    public void processRow(ResultSet rs) throws SQLException {
        while (rs.next()) {
            long entityId = rs.getLong("entity_id");
            logger.info("Processing entity: " + entityId + " -- Total count of processed entities " + count++);
            logger.info("Average time per entity: " + ((double)(System.currentTimeMillis() - start) / (1000 * count)) + " seconds.");
            try {
                ParagraphStoreHandler psh = new ParagraphStoreHandler(entityId, targetDir);
                entityMentionsDao.getFilteredEntityMentionParagraphs(entityId, psh, 100);
                psh.closeOutput();
            } catch (IOException e) {
                logger.error("Problem while ParagraphStoreHandler was saving the entity - entityId: " + entityId, e);
            }
        }
    }
}
