package com.yuncheng.system.api.user;

import java.util.Map;

/** 对外提供的系统用户写入能力。 */
public interface SystemUserCommandApi {

    Long create(SystemUserCreateCommand command);

    /** 批量创建用户，返回以登录名为键的用户主键。 */
    Map<String, Long> createBatch(SystemUserBatchCreateCommand command);
}
