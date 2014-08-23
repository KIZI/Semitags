package cz.ilasek.namedentities.index.dao.redis;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SurfaceFormsDao {
    
    private static final Gson gson = new Gson();

    private final GenericRedisDao surfaceFormsRedisDao;
    
    public SurfaceFormsDao(String redisHost, int redisDatabase) {
        surfaceFormsRedisDao = new GenericRedisDao(redisHost, redisDatabase);
    }
    
    public void persistSurfaceForm(String surfaceForm, Set<String> entities) {
        surfaceFormsRedisDao.set(surfaceForm, gson.toJson(entities));
    }
    
    public Set<String> getEntities(String surfaceForm) {
        Set<String> entities = new HashSet<String>();
        String entitiesSerialized = surfaceFormsRedisDao.get(surfaceForm);
        
        if (entitiesSerialized != null) {
            entities = gson.fromJson(entitiesSerialized, new TypeToken<Set<String>>() {}.getType());
        }
        
        return entities;
    }    

    public void close() {
        surfaceFormsRedisDao.close();
    }

}
