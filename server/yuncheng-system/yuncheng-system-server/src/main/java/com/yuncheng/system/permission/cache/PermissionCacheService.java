package com.yuncheng.system.permission.cache;

import com.yuncheng.common.constant.CacheTtlConstants;
import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisJsonStore;
import com.yuncheng.framework.security.authorization.PermissionCodeProvider;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.permission.service.SystemPermissionService;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 读取、写入和删除用户权限码缓存。 */
@Service
public class PermissionCacheService implements PermissionCodeProvider {

    private static final Logger log = LoggerFactory.getLogger(PermissionCacheService.class);

    private final RedisJsonStore redisJsonStore;
    private final AuthRedisProperties redisProperties;
    private final SystemPermissionService permissionService;

    public PermissionCacheService(
            RedisJsonStore redisJsonStore,
            AuthRedisProperties redisProperties,
            SystemPermissionService permissionService
    ) {
        this.redisJsonStore = redisJsonStore;
        this.redisProperties = redisProperties;
        this.permissionService = permissionService;
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        try {
            String key = permissionCodesKey(userId);
            List<String> cached = redisJsonStore.getList(key, String.class);
            if (cached != null) {
                return List.copyOf(cached);
            }
            List<String> permissionCodes = permissionService.getPermissionCodes(userId);
            redisJsonStore.set(key, permissionCodes, CacheTtlConstants.SHORT);
            return permissionCodes;
        } catch (RuntimeException exception) {
            log.error("加载用户权限码失败，userId={}", userId, exception);
            throw PlatformException.serviceUnavailable("权限服务暂时不可用");
        }
    }

    public void delete(Long userId) {
        redisJsonStore.delete(permissionCodesKey(userId));
    }

    public void deleteAll(Collection<Long> userIds) {
        redisJsonStore.delete(userIds.stream().map(this::permissionCodesKey).toList());
    }

    private String permissionCodesKey(Long userId) {
        return redisProperties.cacheKey("permission-codes:" + userId);
    }
}
