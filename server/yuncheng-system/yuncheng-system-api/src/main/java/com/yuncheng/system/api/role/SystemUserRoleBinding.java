package com.yuncheng.system.api.role;

/** 用户与角色的绑定关系。 */
public record SystemUserRoleBinding(Long userId, Long roleId) {
}
