package com.yuncheng.system.user.service;

import com.yuncheng.common.context.CurrentUser;
import com.yuncheng.system.role.service.UserRoleService;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import org.springframework.stereotype.Service;

/** 直接从数据库加载认证所需的用户上下文。 */
@Service
public class UserContextQueryService {

    private final SystemUserMapper userMapper;
    private final UserRoleService userRoleService;

    public UserContextQueryService(SystemUserMapper userMapper, UserRoleService userRoleService) {
        this.userMapper = userMapper;
        this.userRoleService = userRoleService;
    }

    public CurrentUser loadEnabledUser(Long userId) {
        SystemUser user = userId == null ? null : userMapper.selectById(userId);
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            return null;
        }
        return new CurrentUser(
                user.getId(), user.getUsername(), user.getRealName(), user.getAvatar(),
                userRoleService.roleCodes(userId)
        );
    }
}
