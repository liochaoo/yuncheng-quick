package com.yuncheng.common.constant;

import java.util.Set;

/** 平台保留角色编码。 */
public final class SystemRoleCodes {

    public static final String SUPER_ADMIN = "super-admin";
    public static final String DEFAULT_USER = "default";
    public static final Set<String> NON_DELETABLE_ROLE_CODES = Set.of(SUPER_ADMIN, DEFAULT_USER);

    private SystemRoleCodes() {
    }
}
