package com.yuncheng.system.menu.cache;

import com.yuncheng.common.constant.CacheTtlConstants;
import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisJsonStore;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.menu.dto.MenuRoute;
import com.yuncheng.system.menu.service.MenuRouteService;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 读取、写入和删除用户菜单缓存。 */
@Service
public class MenuCacheService {

    private static final Logger log = LoggerFactory.getLogger(MenuCacheService.class);

    private final RedisJsonStore redisJsonStore;
    private final AuthRedisProperties redisProperties;
    private final MenuRouteService menuRouteService;

    public MenuCacheService(
            RedisJsonStore redisJsonStore,
            AuthRedisProperties redisProperties,
            MenuRouteService menuRouteService
    ) {
        this.redisJsonStore = redisJsonStore;
        this.redisProperties = redisProperties;
        this.menuRouteService = menuRouteService;
    }

    public List<MenuRoute> getUserMenus(Long userId) {
        try {
            String key = menusKey(userId);
            List<MenuRoute> cached = redisJsonStore.getList(key, MenuRoute.class);
            if (cached != null) {
                return List.copyOf(cached);
            }
            List<MenuRoute> menus = menuRouteService.getUserMenus(userId);
            redisJsonStore.set(key, menus, CacheTtlConstants.MEDIUM);
            return menus;
        } catch (RuntimeException exception) {
            log.error("加载用户菜单失败，userId={}", userId, exception);
            throw PlatformException.serviceUnavailable("菜单服务暂时不可用");
        }
    }

    public void delete(Long userId) {
        redisJsonStore.delete(menusKey(userId));
    }

    public void deleteAll(Collection<Long> userIds) {
        redisJsonStore.delete(userIds.stream().map(this::menusKey).toList());
    }

    private String menusKey(Long userId) {
        return redisProperties.cacheKey("menus:" + userId);
    }
}
