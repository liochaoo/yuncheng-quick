package com.yuncheng.system.role.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.enums.RoleType;
import org.springframework.stereotype.Service;

/** 判断当前操作者可以管理的角色范围。 */
@Service
public class RoleAccessService {

    private final CurrentUserContext currentUserContext;

    public RoleAccessService(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    public boolean isSuperAdmin() {
        return currentUserContext.getRoleCodes().contains(SystemRoleCodes.SUPER_ADMIN);
    }

    public void requireCanManage(SystemRole role) {
        if (role.getRoleType() == RoleType.SYSTEM && !isSuperAdmin()) {
            throw PlatformException.forbidden("系统角色只能由超级管理员操作");
        }
    }

    public void requireCanCreate(RoleType roleType) {
        if (roleType == RoleType.SYSTEM && !isSuperAdmin()) {
            throw PlatformException.forbidden("只有超级管理员可以创建系统角色");
        }
    }

    public void requireNotSuperAdminRole(SystemRole role, String operation) {
        if (SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            throw PlatformException.conflict("超级管理员角色不能" + operation);
        }
    }

    public void requireDeletable(SystemRole role) {
        if (SystemRoleCodes.NON_DELETABLE_ROLE_CODES.contains(role.getRoleCode())) {
            throw PlatformException.conflict("平台保留角色不能删除");
        }
    }
}
