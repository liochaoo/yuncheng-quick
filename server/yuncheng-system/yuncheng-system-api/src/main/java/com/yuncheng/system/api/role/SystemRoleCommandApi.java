package com.yuncheng.system.api.role;

import java.util.List;
import java.util.Map;

/** 对外提供的系统角色写入能力。 */
public interface SystemRoleCommandApi {

    Long create(SystemRoleCreateCommand command);

    /** 批量创建角色，返回以角色编码为键的角色主键。 */
    Map<String, Long> createBatch(List<SystemRoleCreateCommand> commands);
}
