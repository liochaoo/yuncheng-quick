package com.yuncheng.system.user.dto;

import java.util.List;

/** 已完成工作簿校验、等待事务写入的导入用户。 */
public record UserImportItem(
        int rowNumber,
        String username,
        String realName,
        String phone,
        String email,
        Long primaryOrgId,
        List<Long> orgIds,
        List<Long> roleIds,
        boolean enabled,
        int sortOrder
) {
}
