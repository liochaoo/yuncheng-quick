package com.yuncheng.framework.log.aop;

import com.yuncheng.common.context.CurrentUser;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.log.OperationLogApi;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.framework.log.command.OperationLogCommand;
import com.yuncheng.framework.web.client.ClientRequestInfo;
import com.yuncheng.framework.web.client.ClientRequestInfoResolver;
import com.yuncheng.framework.web.constant.WebConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 根据注解统一采集业务写操作日志，并位于事务拦截器外侧判断最终执行结果。 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    private final OperationLogApi operationLogApi;
    private final OperationParameterSanitizer parameterSanitizer;
    private final CurrentUserContext currentUserContext;
    private final ClientRequestInfoResolver requestInfoResolver;

    public OperationLogAspect(
            OperationLogApi operationLogApi,
            OperationParameterSanitizer parameterSanitizer,
            CurrentUserContext currentUserContext,
            ClientRequestInfoResolver requestInfoResolver
    ) {
        this.operationLogApi = operationLogApi;
        this.parameterSanitizer = parameterSanitizer;
        this.currentUserContext = currentUserContext;
        this.requestInfoResolver = requestInfoResolver;
    }

    @Around("@annotation(operationLog)")
    public Object record(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startedAt = System.nanoTime();
        OperationContext context = createContextSafely(joinPoint, operationLog);
        try {
            Object result = joinPoint.proceed();
            submitSafely(context, true, null, elapsedMillis(startedAt));
            return result;
        } catch (Throwable throwable) {
            submitSafely(context, false, throwable.getMessage(), elapsedMillis(startedAt));
            throw throwable;
        }
    }

    private OperationContext createContextSafely(
            ProceedingJoinPoint joinPoint,
            OperationLog operationLog
    ) {
        try {
            return createContext(joinPoint, operationLog);
        } catch (RuntimeException exception) {
            log.error("采集操作日志上下文失败，action={}", operationLog.value(), exception);
            return null;
        }
    }

    private OperationContext createContext(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        HttpServletRequest request = currentRequest();
        ClientRequestInfo requestInfo = request == null ? null : requestInfoResolver.resolve(request);
        CurrentUser user = currentUserContext.findUser().orElse(null);
        return new OperationContext(
                operationLog.value(),
                signature.getDeclaringTypeName(),
                signature.getName(),
                request == null ? null : request.getMethod(),
                request == null ? null : request.getRequestURI(),
                parameterSanitizer.serialize(signature.getParameterNames(), joinPoint.getArgs()),
                user,
                requestInfo,
                MDC.get(WebConstants.TRACE_ID_MDC_KEY),
                Instant.now()
        );
    }

    private void submitSafely(
            OperationContext context,
            boolean success,
            String errorMessage,
            long durationMillis
    ) {
        if (context == null) {
            return;
        }
        try {
            submit(context, success, errorMessage, durationMillis);
        } catch (RuntimeException exception) {
            log.error("提交操作日志失败，action={}", context.action(), exception);
        }
    }

    private void submit(
            OperationContext context,
            boolean success,
            String errorMessage,
            long durationMillis
    ) {
        CurrentUser user = context.user();
        ClientRequestInfo requestInfo = context.requestInfo();
        operationLogApi.record(new OperationLogCommand(
                context.action(),
                context.className(),
                context.methodName(),
                context.httpMethod(),
                context.requestPath(),
                context.requestParams(),
                success,
                truncate(errorMessage),
                durationMillis,
                user == null ? null : user.userId(),
                user == null ? null : user.username(),
                user == null ? null : user.realName(),
                requestInfo == null ? null : requestInfo.ip(),
                requestInfo == null ? null : requestInfo.userAgent(),
                context.traceId(),
                context.occurredAt()
        ));
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private record OperationContext(
            String action,
            String className,
            String methodName,
            String httpMethod,
            String requestPath,
            String requestParams,
            CurrentUser user,
            ClientRequestInfo requestInfo,
            String traceId,
            Instant occurredAt
    ) {
    }
}
