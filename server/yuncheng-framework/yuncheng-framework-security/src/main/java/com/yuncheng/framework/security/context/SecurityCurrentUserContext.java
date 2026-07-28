package com.yuncheng.framework.security.context;

import com.yuncheng.common.context.CurrentUser;
import com.yuncheng.common.context.CurrentUserContext;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** 从 Spring Security 读取当前登录用户上下文。 */
@Component
public class SecurityCurrentUserContext implements CurrentUserContext {

    @Override
    public Optional<CurrentUser> findUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CurrentUserJwtAuthenticationToken currentAuthentication
                && authentication.isAuthenticated()) {
            return Optional.of(currentAuthentication.getCurrentUser());
        }
        return Optional.empty();
    }
}
