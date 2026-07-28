package com.yuncheng.system.permission.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.system.menu.mapper.SystemMenuMapper;
import com.yuncheng.system.role.service.UserRoleService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 直接从数据库查询用户的有效权限码。 */
@Service
public class SystemPermissionService {

    private final SystemMenuMapper menuMapper;
    private final UserRoleService userRoleService;

    public SystemPermissionService(
            SystemMenuMapper menuMapper,
            UserRoleService userRoleService
    ) {
        this.menuMapper = menuMapper;
        this.userRoleService = userRoleService;
    }

    public List<String> getPermissionCodes(Long userId) {
        List<String> permissionCodes;
        if (userRoleService.roleCodes(userId).contains(SystemRoleCodes.SUPER_ADMIN)) {
            permissionCodes = menuMapper.selectAllPermissionCodes();
        } else {
            permissionCodes = menuMapper.selectPermissionCodesByUserId(userId);
        }
        return List.copyOf(permissionCodes);
    }
}
