package com.yuncheng.framework.openapi;

import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.security.authorization.PermissionCodeProvider;
import com.yuncheng.framework.security.web.SecurityResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/** 为 Springdoc 注册的原始文档端点补充平台权限码校验。 */
public class OpenApiAccessInterceptor implements HandlerInterceptor {

    private final CurrentUserContext currentUserContext;
    private final PermissionCodeProvider permissionCodeProvider;
    private final SecurityResponseWriter responseWriter;

    public OpenApiAccessInterceptor(
            CurrentUserContext currentUserContext,
            PermissionCodeProvider permissionCodeProvider,
            SecurityResponseWriter responseWriter
    ) {
        this.currentUserContext = currentUserContext;
        this.permissionCodeProvider = permissionCodeProvider;
        this.responseWriter = responseWriter;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        Long userId = currentUserContext.getUserId();
        Collection<String> permissionCodes = permissionCodeProvider.getPermissionCodes(userId);
        if (permissionCodes != null && permissionCodes.contains(OpenApiPermissionCodes.QUERY)) {
            return true;
        }
        responseWriter.write(
                response,
                HttpStatus.FORBIDDEN.value(),
                "没有访问接口文档的权限"
        );
        return false;
    }
}
