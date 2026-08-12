package com.yuncheng.system.user.dto;

import java.util.List;

/** 用户交换工作簿使用的组织编码归属。 */
public record UserOrgCodeAssignment(
        String primaryOrgCode,
        List<String> otherOrgCodes
) {
}
