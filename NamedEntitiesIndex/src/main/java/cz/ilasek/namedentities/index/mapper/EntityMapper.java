package cz.ilasek.namedentities.index.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import cz.ilasek.namedentities.index.models.Entity;


public class EntityMapper implements RowMapper<Entity> {

    @Override
    public Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
        Entity entity = new Entity();
        entity.setId(rs.getLong("entity_id"));
        entity.setUri(rs.getString("uri"));
        
        return entity;
    }

}
