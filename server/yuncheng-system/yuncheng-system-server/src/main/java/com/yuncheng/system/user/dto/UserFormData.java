package com.yuncheng.system.user.dto;

import java.util.List;

/** 用户编辑表单数据，联系方式保持原始内容。 */
public record UserFormData(
        String id,
        String username,
        String realName,
        String phone,
        String email,
        int sortOrder,
        boolean enabled,
        List<String> roleIds,
        List<String> orgIds,
        String primaryOrgId
) {
}
