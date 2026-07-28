package com.yuncheng.system.api.role;

import java.util.Optional;

/** 对外提供的系统角色查询能力。 */
public interface SystemRoleQueryApi {

    Optional<SystemRoleInfo> findByCode(String roleCode);
}
