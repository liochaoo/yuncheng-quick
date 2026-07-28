package com.yuncheng.framework.security.authorization;

import java.util.Collection;

/** 为安全框架提供当前用户的有效权限码。 */
public interface PermissionCodeProvider {

    Collection<String> getPermissionCodes(Long userId);
}
