package cz.ilasek.namedentities.index.dao.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class GenericRedisDao {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericRedisDao.class);

    private Jedis jedis;
    
    private final String redisHost;
    private final int redisDatabase;

    public GenericRedisDao(String redisHost, int redisDatabase) {
        this.redisHost = redisHost;
        this.redisDatabase = redisDatabase;
        reconnect();
    }
    
    private void reconnect() {
        if (jedis != null) {
            jedis.disconnect();
        }
        jedis = new Jedis(redisHost);
        jedis.select(redisDatabase);
        jedis.connect();
    }
    
    public void set(String key, String value) {
        boolean processed = false;
        do {
            try {
                jedis.set(key, value);
                processed = true;
            } catch (JedisConnectionException e) {
                logger.error("Lost connection to jedis - reocnnecting...");
                try {
                    reconnect();
                    logger.info("Reconnected...");
                } catch (Exception e1) {
                    logger.error("Problems reconnecting to Redis");
                }
            }
        } while (!processed);
    }
    
    public String get(String key) {
        boolean processed = false;
        String value = null;
        
        do {
            try {
                value = jedis.get(key);
                
                processed = true;
            } catch (JedisConnectionException e) {
                logger.error("Lost connection to jedis - reocnnecting...");
                try {
                    reconnect();
                    logger.info("Reconnected...");
                } catch (Exception e1) {
                    logger.error("Problems reconnecting to Redis");
                }
            }
        } while(!processed);
        
        return value;
    }

    public void close() {
        jedis.disconnect();
    }

}
