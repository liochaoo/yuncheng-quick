package com.yuncheng.system.api.permission;

import com.yuncheng.system.api.role.SystemRoleType;
import java.util.List;

/** 对外提供的角色可授权菜单权限查询能力。 */
public interface SystemPermissionQueryApi {

    List<Long> findAssignableMenuIds(SystemRoleType roleType);
}
