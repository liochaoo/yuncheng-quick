package com.yuncheng.framework.redis.store;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 统一使用 JSON 保存和读取 Redis 对象。 */
@Component
public class RedisJsonStore {

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public RedisJsonStore(StringRedisTemplate redisTemplate, JsonMapper jsonMapper) {
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    public <T> T get(String key, Class<T> type) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try {
            return jsonMapper.readValue(value, type);
        } catch (Exception exception) {
            throw new IllegalStateException("读取 Redis JSON 数据失败", exception);
        }
    }

    public <T> T getAndDelete(String key, Class<T> type) {
        String value = redisTemplate.opsForValue().getAndDelete(key);
        if (value == null) {
            return null;
        }
        try {
            return jsonMapper.readValue(value, type);
        } catch (Exception exception) {
            throw new IllegalStateException("读取 Redis JSON 数据失败", exception);
        }
    }

    public <T> List<T> getList(String key, Class<T> elementType) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try {
            return jsonMapper.readValue(
                    value,
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, elementType)
            );
        } catch (Exception exception) {
            throw new IllegalStateException("读取 Redis JSON 数据失败", exception);
        }
    }

    public <T> List<T> multiGet(List<String> keys, Class<T> type) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) {
            return Collections.nCopies(keys.size(), null);
        }
        try {
            return values.stream()
                    .map(value -> value == null ? null : read(value, type))
                    .toList();
        } catch (Exception exception) {
            throw new IllegalStateException("批量读取 Redis JSON 数据失败", exception);
        }
    }

    public void set(String key, Object value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Redis Key 的 TTL 必须大于 0");
        }
        try {
            redisTemplate.opsForValue().set(key, jsonMapper.writeValueAsString(value), ttl);
        } catch (Exception exception) {
            throw new IllegalStateException("写入 Redis JSON 数据失败", exception);
        }
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.unlink(keys);
    }

    private <T> T read(String value, Class<T> type) {
        try {
            return jsonMapper.readValue(value, type);
        } catch (Exception exception) {
            throw new IllegalStateException("读取 Redis JSON 数据失败", exception);
        }
    }
}
