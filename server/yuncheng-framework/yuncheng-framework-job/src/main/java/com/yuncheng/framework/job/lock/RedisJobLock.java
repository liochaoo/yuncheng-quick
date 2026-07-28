package com.yuncheng.framework.job.lock;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 使用唯一令牌获取和释放定时任务 Redis 锁。 */
@Component
public class RedisJobLock {

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final String applicationName;

    public RedisJobLock(
            StringRedisTemplate redisTemplate,
            @Qualifier("springApplicationName") String applicationName
    ) {
        this.redisTemplate = redisTemplate;
        this.applicationName = applicationName;
    }

    public String tryLock(String jobName, Duration ttl) {
        validate(jobName, ttl);
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key(jobName), token, ttl);
        return Boolean.TRUE.equals(acquired) ? token : null;
    }

    public void unlock(String jobName, String token) {
        redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key(jobName)), token);
    }

    private String key(String jobName) {
        return applicationName + ":runtime:job-lock:" + jobName;
    }

    private void validate(String jobName, Duration ttl) {
        if (!StringUtils.hasText(jobName)) {
            throw new IllegalArgumentException("定时任务名称不能为空");
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("定时任务锁时长必须大于 0");
        }
    }
}
