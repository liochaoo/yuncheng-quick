package com.yuncheng.system.login.profile.dto;

/** 当前用户的归属组织路径摘要。 */
public record ProfileOrgResponse(
        String id,
        String fullPath
) {
}
