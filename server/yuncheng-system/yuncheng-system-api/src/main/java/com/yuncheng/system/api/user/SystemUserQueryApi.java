package com.yuncheng.system.api.user;

import java.util.Optional;

/** 对外提供的系统用户查询能力。 */
public interface SystemUserQueryApi {

    Optional<SystemUserInfo> findById(Long userId);

    Optional<SystemUserInfo> findByUsername(String username);
}
