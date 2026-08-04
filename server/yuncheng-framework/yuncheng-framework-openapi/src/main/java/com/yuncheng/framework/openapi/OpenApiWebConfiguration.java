package com.yuncheng.framework.openapi;

import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.security.authorization.PermissionCodeProvider;
import com.yuncheng.framework.security.web.SecurityResponseWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 接口文档 Web 访问配置。 */
@Configuration(proxyBeanMethods = false)
public class OpenApiWebConfiguration implements WebMvcConfigurer {

    private final OpenApiAccessInterceptor accessInterceptor;

    public OpenApiWebConfiguration(
            CurrentUserContext currentUserContext,
            PermissionCodeProvider permissionCodeProvider,
            SecurityResponseWriter responseWriter
    ) {
        this.accessInterceptor = new OpenApiAccessInterceptor(
                currentUserContext,
                permissionCodeProvider,
                responseWriter
        );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor)
                .addPathPatterns(
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml"
                );
    }
}
