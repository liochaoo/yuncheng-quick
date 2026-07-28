package com.yuncheng.system.api.user;

import java.util.List;

/** 批量创建系统用户参数，同一批用户使用同一个初始密码。 */
public record SystemUserBatchCreateCommand(
        String password,
        List<SystemUserBatchCreateItem> users
) {
}
