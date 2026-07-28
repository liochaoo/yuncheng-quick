package com.yuncheng.system.api.permission;

import java.util.Collection;

/** 对外提供的角色菜单权限写入能力。 */
public interface SystemRolePermissionCommandApi {

    void replaceBatch(Collection<SystemRolePermissionCommand> commands);
}
