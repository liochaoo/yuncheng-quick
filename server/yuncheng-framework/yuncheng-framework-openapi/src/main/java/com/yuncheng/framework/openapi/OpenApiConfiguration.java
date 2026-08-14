package com.yuncheng.framework.openapi;

import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.security.constant.SecurityConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.ArrayList;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

/** 平台 OpenAPI 3.1 文档定义。 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String FALLBACK_VERSION = "1.0.0";

    @Bean
    OpenAPI platformOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("云程快速开发平台 API")
                        .version(applicationVersion())
                        .description("云程快速开发平台后端 HTTP 接口")
                        .contact(new Contact()
                                .name("码小白")
                                .url("https://github.com/liochaoo/yuncheng-quick"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.txt")))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    OperationCustomizer platformOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getSummary() == null || operation.getSummary().isBlank()) {
                OperationLog operationLog = handlerMethod.getMethodAnnotation(OperationLog.class);
                if (operationLog != null && !operationLog.value().isBlank()) {
                    operation.setSummary(operationLog.value());
                }
            }

            RequirePermission permission = handlerMethod.getMethodAnnotation(RequirePermission.class);
            if (permission != null) {
                operation.addExtension("x-permission-codes", List.of(permission.value()));
                operation.getResponses().addApiResponse(
                        "403",
                        new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description("没有相应功能权限")
                );
            }
            operation.getResponses().addApiResponse(
                    "401",
                    new io.swagger.v3.oas.models.responses.ApiResponse()
                            .description("未登录或登录状态已失效")
            );
            return operation;
        };
    }

    @Bean
    OpenApiCustomizer platformOpenApiCustomizer() {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
                boolean anonymous = SecurityConstants.ANONYMOUS_URLS.stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, path));
                if (anonymous) {
                    operation.setSecurity(new ArrayList<>());
                    operation.getResponses().remove("401");
                }
            }));
        };
    }

    private String applicationVersion() {
        String implementationVersion = OpenApiConfiguration.class
                .getPackage()
                .getImplementationVersion();
        return implementationVersion == null || implementationVersion.isBlank()
                ? FALLBACK_VERSION
                : implementationVersion;
    }
}
