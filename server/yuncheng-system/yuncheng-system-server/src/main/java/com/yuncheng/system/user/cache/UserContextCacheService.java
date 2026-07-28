package com.yuncheng.system.user.cache;

import com.yuncheng.common.constant.CacheTtlConstants;
import com.yuncheng.common.context.CurrentUser;
import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisJsonStore;
import com.yuncheng.system.user.service.UserContextQueryService;
import java.util.Collection;
import org.springframework.stereotype.Service;

/** 在认证范围内读取和重建当前用户信息缓存。 */
@Service
public class UserContextCacheService {

    private final RedisJsonStore redisJsonStore;
    private final AuthRedisProperties redisProperties;
    private final UserContextQueryService userContextQueryService;

    public UserContextCacheService(
            RedisJsonStore redisJsonStore,
            AuthRedisProperties redisProperties,
            UserContextQueryService userContextQueryService
    ) {
        this.redisJsonStore = redisJsonStore;
        this.redisProperties = redisProperties;
        this.userContextQueryService = userContextQueryService;
    }

    public CurrentUser getForAuthentication(Long userId) {
        CurrentUser cached = redisJsonStore.get(cacheKey(userId), CurrentUser.class);
        if (cached != null) {
            return cached;
        }
        CurrentUser currentUser = userContextQueryService.loadEnabledUser(userId);
        if (currentUser != null) {
            write(userId, currentUser);
        }
        return currentUser;
    }

    public void delete(Long userId) {
        redisJsonStore.delete(cacheKey(userId));
    }

    public void deleteAll(Collection<Long> userIds) {
        redisJsonStore.delete(userIds.stream().map(this::cacheKey).toList());
    }

    private void write(Long userId, CurrentUser currentUser) {
        redisJsonStore.set(cacheKey(userId), currentUser, CacheTtlConstants.MEDIUM);
    }

    private String cacheKey(Long userId) {
        return redisProperties.cacheKey("user-info:" + userId);
    }
}
