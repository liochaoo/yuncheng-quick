package com.yuncheng.system.api.role;

import java.util.Collection;

/** 用户角色关系原子操作接口。 */
public interface SystemUserRoleApi {

    boolean exists(Long userId, Long roleId);

    void bind(Long userId, Long roleId);

    void bindBatch(Collection<SystemUserRoleBinding> bindings);
}
