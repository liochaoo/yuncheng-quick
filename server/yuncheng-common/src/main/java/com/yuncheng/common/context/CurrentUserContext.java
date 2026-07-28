package com.yuncheng.common.context;

import java.util.List;
import java.util.Optional;

/** 统一读取当前执行线程中的登录用户上下文。 */
public interface CurrentUserContext {

    Optional<CurrentUser> findUser();

    default CurrentUser getUser() {
        return findUser().orElseThrow(() -> new IllegalStateException("当前登录用户上下文不存在"));
    }

    default Optional<Long> findUserId() {
        return findUser().map(CurrentUser::userId);
    }

    default Long getUserId() {
        return getUser().userId();
    }

    default String getUsername() {
        return getUser().username();
    }

    default String getRealName() {
        return getUser().realName();
    }

    default List<String> getRoleCodes() {
        return getUser().roleCodes();
    }
}
