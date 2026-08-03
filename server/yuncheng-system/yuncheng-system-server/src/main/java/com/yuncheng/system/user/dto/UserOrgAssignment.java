package com.yuncheng.system.user.dto;

import java.util.List;

/** 用户归属组织的编辑数据。 */
public record UserOrgAssignment(
        List<String> orgIds,
        String primaryOrgId
) {
}
