package cz.ilasek.namedentities.index.dao.redis;

import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager.Language;

public class EntityCoOccurrencesDao {
    
    private static final String SEPARATOR = "#*@#";

    private String lang = "en";
    
    private final GenericRedisDao coOccurrencesRedisDao;

    public EntityCoOccurrencesDao(String lang, String redisHost, int redisDatabase) {
        this.lang = lang;
        coOccurrencesRedisDao = new GenericRedisDao(redisHost, redisDatabase);
    }
    
    public EntityCoOccurrencesDao(Language language, String redisHost, int redisDatabase) {
        this(language.getLang(), redisHost, redisDatabase);
    }
    

    public Integer getCoOccurrenceCount(String entityAUri, String entityBUri) {
        return getCoOccurrenceCount(buildKey(lang, entityAUri, entityBUri));
    }


    private Integer getCoOccurrenceCount(String key) {
        Integer coOccurrenceCount = null;
        String value = coOccurrencesRedisDao.get(key);
        
        if (value == null) {
            coOccurrenceCount = 0;
        } else {
            coOccurrenceCount = new Integer(value);
        }
        
        return coOccurrenceCount;
    }
    
    public void incrementCoOccurrenceCount(String entityAUri, String entityBUri) {
        String key = buildKey(lang, entityAUri, entityBUri);
        Integer coOccurrenceCount = getCoOccurrenceCount(key);

        coOccurrenceCount++;
        coOccurrencesRedisDao.set(key, coOccurrenceCount.toString());
    }

    public static String buildKey(String lang, String entityAUri, String entityBUri) {
        if (entityAUri.compareTo(entityBUri) > 0) {
            String tmpUri = entityAUri;
            entityAUri = entityBUri;
            entityBUri = tmpUri;
        }
        
        return lang + SEPARATOR + entityAUri + SEPARATOR + entityBUri;
    }
    
    public void close() {
        coOccurrencesRedisDao.close();
    }

}
