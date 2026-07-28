package com.yuncheng.framework.redis.store;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 使用非阻塞扫描按命名空间批量清理 Redis Key。 */
@Component
public class RedisKeyCleaner {

    private static final int DELETE_BATCH_SIZE = 500;

    private final StringRedisTemplate redisTemplate;

    public RedisKeyCleaner(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long deleteByPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Redis Key 前缀不能为空");
        }
        ScanOptions options = ScanOptions.scanOptions()
                .match(prefix + "*")
                .count(DELETE_BATCH_SIZE)
                .build();
        List<String> keys = new ArrayList<>(DELETE_BATCH_SIZE);
        long deleted = 0;
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
                if (keys.size() >= DELETE_BATCH_SIZE) {
                    deleted += unlink(keys);
                }
            }
        }
        return deleted + unlink(keys);
    }

    private long unlink(List<String> keys) {
        if (keys.isEmpty()) {
            return 0;
        }
        Long deleted = redisTemplate.unlink(List.copyOf(keys));
        keys.clear();
        return deleted == null ? 0 : deleted;
    }
}
