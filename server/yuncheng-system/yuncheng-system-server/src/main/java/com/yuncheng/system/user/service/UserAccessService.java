package com.yuncheng.system.user.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.role.service.RoleAccessService;
import com.yuncheng.system.role.service.UserRoleService;
import com.yuncheng.system.user.entity.SystemUser;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

/** 判断当前操作者可以管理的用户范围。 */
@Service
public class UserAccessService {

    private final UserRoleService userRoleService;
    private final RoleAccessService roleAccessService;

    public UserAccessService(UserRoleService userRoleService, RoleAccessService roleAccessService) {
        this.userRoleService = userRoleService;
        this.roleAccessService = roleAccessService;
    }

    public boolean canManage(Long userId) {
        return roleAccessService.isSuperAdmin() || !userRoleService.hasSystemRole(userId);
    }

    public void requireCanManage(Long userId) {
        if (!canManage(userId)) {
            throw PlatformException.forbidden("包含系统内置角色的用户只能由超级管理员操作");
        }
    }

    public void requireCanManage(Collection<SystemUser> users) {
        if (roleAccessService.isSuperAdmin() || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(SystemUser::getId).toList();
        if (!userRoleService.systemRoleUserIds(userIds).isEmpty()) {
            throw PlatformException.forbidden("包含系统内置角色的用户只能由超级管理员操作");
        }
    }
}
