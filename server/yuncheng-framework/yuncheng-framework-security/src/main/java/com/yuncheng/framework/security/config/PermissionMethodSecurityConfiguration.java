package com.yuncheng.framework.security.config;

import com.yuncheng.framework.security.authorization.PermissionAuthorizationManager;
import com.yuncheng.framework.security.authorization.PermissionCodeProvider;
import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;

/** 注册平台权限码的方法级授权能力。 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class PermissionMethodSecurityConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static AuthorizationManagerBeforeMethodInterceptor requirePermissionInterceptor(
            ObjectProvider<PermissionCodeProvider> permissionCodeProvider
    ) {
        return new AuthorizationManagerBeforeMethodInterceptor(
                AnnotationMatchingPointcut.forMethodAnnotation(RequirePermission.class),
                new PermissionAuthorizationManager(permissionCodeProvider::getObject)
        );
    }
}
