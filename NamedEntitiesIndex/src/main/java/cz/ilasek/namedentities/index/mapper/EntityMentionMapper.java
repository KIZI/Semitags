package cz.ilasek.namedentities.index.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import cz.ilasek.namedentities.index.models.EntityMention;

public class EntityMentionMapper implements RowMapper<EntityMention> {
    @Override
    public EntityMention mapRow(ResultSet rs, int rowNum) throws SQLException {
        EntityMention entityMention = new EntityMention();
        entityMention.setEntityId(rs.getLong("entity_id"));
        entityMention.setParagraph(rs.getString("paragraph_text"));
        entityMention.setParagraphId(rs.getLong("paragraph_id"));
        entityMention.setSurfaceForm(rs.getString("surface_form"));
        
        return entityMention;
    }
}
