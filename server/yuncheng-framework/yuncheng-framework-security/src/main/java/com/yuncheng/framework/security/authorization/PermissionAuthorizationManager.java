package com.yuncheng.framework.security.authorization;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.security.context.CurrentUserJwtAuthenticationToken;
import com.yuncheng.framework.web.exception.PlatformException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;

/** 根据方法声明的权限码完成授权判断。 */
public class PermissionAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    private static final Logger log = LoggerFactory.getLogger(PermissionAuthorizationManager.class);

    private final Supplier<PermissionCodeProvider> permissionCodeProviderSupplier;

    public PermissionAuthorizationManager(
            Supplier<PermissionCodeProvider> permissionCodeProviderSupplier
    ) {
        this.permissionCodeProviderSupplier = permissionCodeProviderSupplier;
    }

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authenticationSupplier,
            MethodInvocation methodInvocation
    ) {
        RequirePermission annotation = findAnnotation(methodInvocation);
        if (annotation == null) {
            return new AuthorizationDecision(true);
        }
        Set<String> requiredCodes = validCodes(annotation.value());
        if (requiredCodes.isEmpty()) {
            return new AuthorizationDecision(false);
        }
        Authentication authentication = authenticationSupplier.get();
        if (!(authentication instanceof CurrentUserJwtAuthenticationToken currentAuthentication)) {
            return new AuthorizationDecision(false);
        }
        Long userId = currentAuthentication.getCurrentUser().userId();
        Collection<String> permissionCodes;
        try {
            permissionCodes = permissionCodeProviderSupplier.get()
                    .getPermissionCodes(userId);
        } catch (PlatformException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("加载用户权限码失败，userId={}", userId, exception);
            throw PlatformException.serviceUnavailable("权限服务暂时不可用");
        }
        Set<String> userCodes = permissionCodes == null
                ? Set.of()
                : new HashSet<>(permissionCodes);
        boolean granted = requiredCodes.stream().anyMatch(userCodes::contains);
        return new AuthorizationDecision(granted);
    }

    private RequirePermission findAnnotation(MethodInvocation methodInvocation) {
        Method method = AopUtils.getMostSpecificMethod(
                methodInvocation.getMethod(),
                methodInvocation.getThis().getClass()
        );
        return AnnotatedElementUtils.findMergedAnnotation(method, RequirePermission.class);
    }

    private Set<String> validCodes(String[] codes) {
        Set<String> validCodes = new HashSet<>();
        for (String code : codes) {
            if (code != null && !code.isBlank()) {
                validCodes.add(code);
            }
        }
        return validCodes;
    }
}
