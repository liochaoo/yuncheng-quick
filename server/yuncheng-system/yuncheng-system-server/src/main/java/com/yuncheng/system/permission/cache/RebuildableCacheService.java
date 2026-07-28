package com.yuncheng.system.permission.cache;

import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisKeyCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 清理可以从数据源重新构建的缓存，不操作运行时数据。 */
@Service
public class RebuildableCacheService {

    private static final Logger log = LoggerFactory.getLogger(RebuildableCacheService.class);

    private final RedisKeyCleaner redisKeyCleaner;
    private final AuthRedisProperties redisProperties;

    public RebuildableCacheService(
            RedisKeyCleaner redisKeyCleaner,
            AuthRedisProperties redisProperties
    ) {
        this.redisKeyCleaner = redisKeyCleaner;
        this.redisProperties = redisProperties;
    }

    public long clearAll() {
        long deleted = redisKeyCleaner.deleteByPrefix(redisProperties.getKeyPrefix() + ":cache:");
        log.info("可重建缓存清理完成，清理数量={}", deleted);
        return deleted;
    }
}
