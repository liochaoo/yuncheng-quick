package com.yuncheng.system.user.dto;

import java.util.List;

/** 用户组织归属的编辑数据。 */
public record UserOrgAssignment(
        List<String> orgIds,
        String primaryOrgId
) {
}
