package spring.gemfire.showcase.vector.search.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class ClearCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public ClearCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Clears all keys managed by both Redis templates using FLUSHDB.
     */
    public void clearCache() {
        log.info("Clearing all cache entries...");

        flushTemplateDatabase(redisTemplate, "vectorStoreTemplate");

        log.info("All cache entries cleared successfully.");
    }

    /**
     * Flushes the entire database associated with the provided RedisTemplate connection.
     */
    private void flushTemplateDatabase(RedisTemplate<String, Object> template, String templateName) {
        template.execute((RedisCallback<Void>) connection -> {
            log.info("Executing FLUSHDB for {}", templateName);
            connection.serverCommands().flushDb();
            return null;
        });
    }

    /**
     * Alternative: Clears keys matching a specific pattern (e.g. "vectorStore:*")
     * without wiping the entire Redis database.
     */
    public void clearCacheByPattern(String pattern) {
        log.info("Clearing keys matching pattern: {}", pattern);
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long count = redisTemplate.delete(keys);
            log.info("Deleted {} keys for pattern: {}", count, pattern);
        }
    }
}