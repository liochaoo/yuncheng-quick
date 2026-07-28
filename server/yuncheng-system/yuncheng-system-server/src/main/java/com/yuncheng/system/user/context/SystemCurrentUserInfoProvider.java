package com.yuncheng.system.user.context;

import com.yuncheng.common.context.CurrentUser;
import com.yuncheng.framework.security.context.CurrentUserInfoProvider;
import com.yuncheng.system.user.cache.UserContextCacheService;
import org.springframework.stereotype.Component;

/** 使用系统用户缓存为请求认证提供当前用户信息。 */
@Component
public class SystemCurrentUserInfoProvider implements CurrentUserInfoProvider {

    private final UserContextCacheService userContextCacheService;

    public SystemCurrentUserInfoProvider(UserContextCacheService userContextCacheService) {
        this.userContextCacheService = userContextCacheService;
    }

    @Override
    public CurrentUser loadForAuthentication(Long userId) {
        return userContextCacheService.getForAuthentication(userId);
    }
}
