package com.yuncheng.framework.security.context;

import com.yuncheng.common.context.CurrentUser;

/** 为请求认证提供当前用户信息，由用户业务模块实现。 */
public interface CurrentUserInfoProvider {

    CurrentUser loadForAuthentication(Long userId);
}
