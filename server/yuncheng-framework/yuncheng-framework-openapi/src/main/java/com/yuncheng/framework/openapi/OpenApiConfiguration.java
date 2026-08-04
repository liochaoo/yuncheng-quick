package com.yuncheng.framework.openapi;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.security.constant.SecurityConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

/** 平台 OpenAPI 3.1 文档定义。 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String FALLBACK_VERSION = "0.1.0-alpha.1";

    private static final Map<String, String> CONTROLLER_TAGS = Map.ofEntries(
            Map.entry("AuthController", "Web 登录认证"),
            Map.entry("CaptchaController", "图形验证码"),
            Map.entry("CurrentUserController", "当前用户"),
            Map.entry("DictionaryManagementController", "字典管理"),
            Map.entry("DictionaryOptionController", "数据字典"),
            Map.entry("ExperienceConfigController", "体验环境"),
            Map.entry("FileController", "通用文件"),
            Map.entry("MenuController", "菜单管理"),
            Map.entry("MobileAuthController", "移动端认证"),
            Map.entry("OnlineSessionController", "在线会话"),
            Map.entry("OpenApiController", "接口文档"),
            Map.entry("OrgManagementController", "组织管理"),
            Map.entry("OrgOptionController", "组织选项"),
            Map.entry("PasswordRecoveryController", "密码找回"),
            Map.entry("PermissionController", "权限管理"),
            Map.entry("ProfileController", "个人中心"),
            Map.entry("PublicFileController", "公开文件"),
            Map.entry("RegistrationController", "用户注册"),
            Map.entry("RoleController", "角色管理"),
            Map.entry("SecurityPolicyController", "登录安全策略"),
            Map.entry("SecurityPolicyManagementController", "安全策略管理"),
            Map.entry("SystemFileController", "系统文件管理"),
            Map.entry("SystemLogController", "系统日志"),
            Map.entry("UserController", "用户管理")
    );

    private static final Map<String, String> METHOD_SUMMARIES = Map.ofEntries(
            Map.entry("accessCodes", "查询当前用户权限码"),
            Map.entry("addUsers", "添加角色用户"),
            Map.entry("associate", "关联业务文件"),
            Map.entry("batchDelete", "批量删除"),
            Map.entry("batchKickout", "批量强制下线"),
            Map.entry("candidateUsers", "分页查询待选角色用户"),
            Map.entry("changeAvatar", "修改头像"),
            Map.entry("changeEmail", "修改电子邮箱"),
            Map.entry("changeOptionStatus", "变更字典选项状态"),
            Map.entry("changePassword", "修改密码"),
            Map.entry("changeStatus", "变更启用状态"),
            Map.entry("check", "校验图形验证码"),
            Map.entry("checkOptionUniqueness", "校验字典选项唯一性"),
            Map.entry("checkUniqueness", "校验唯一性"),
            Map.entry("children", "查询下级节点"),
            Map.entry("clean", "清理日志"),
            Map.entry("cleanPolicy", "查询日志清理策略"),
            Map.entry("clearCache", "清空权限缓存"),
            Map.entry("config", "查询接口文档状态"),
            Map.entry("create", "新增"),
            Map.entry("createOption", "新增字典选项"),
            Map.entry("delete", "删除"),
            Map.entry("deleteAvatar", "删除头像"),
            Map.entry("deleteOption", "删除字典选项"),
            Map.entry("deletionImpact", "查询删除影响"),
            Map.entry("detail", "查询详情"),
            Map.entry("download", "下载文件"),
            Map.entry("formData", "查询编辑表单数据"),
            Map.entry("get", "查询"),
            Map.entry("item", "查询组织选项"),
            Map.entry("items", "批量查询组织选项"),
            Map.entry("kickout", "强制下线"),
            Map.entry("list", "查询列表"),
            Map.entry("login", "登录"),
            Map.entry("loginDetail", "查询登录日志详情"),
            Map.entry("loginPage", "分页查询登录日志"),
            Map.entry("logout", "退出登录"),
            Map.entry("menuTree", "查询权限菜单树"),
            Map.entry("menus", "查询当前用户菜单"),
            Map.entry("move", "移动组织"),
            Map.entry("moveImpact", "查询组织移动影响"),
            Map.entry("optionDetail", "查询字典选项详情"),
            Map.entry("operationDetail", "查询操作日志详情"),
            Map.entry("operationPage", "分页查询操作日志"),
            Map.entry("options", "查询选项"),
            Map.entry("optionsByIds", "按主键查询角色选项"),
            Map.entry("page", "分页查询"),
            Map.entry("pageOptions", "分页查询字典选项"),
            Map.entry("preview", "预览文件"),
            Map.entry("profile", "查询个人资料"),
            Map.entry("refresh", "刷新访问令牌"),
            Map.entry("register", "注册用户"),
            Map.entry("removeAssociation", "解除业务文件关联"),
            Map.entry("removeUsers", "移除角色用户"),
            Map.entry("resetPassword", "重置密码"),
            Map.entry("rolePermission", "查询角色权限"),
            Map.entry("save", "保存"),
            Map.entry("search", "搜索"),
            Map.entry("sendEmailCode", "发送邮箱验证码"),
            Map.entry("tree", "查询树"),
            Map.entry("unlock", "解除登录锁定"),
            Map.entry("update", "编辑"),
            Map.entry("updateOption", "编辑字典选项"),
            Map.entry("updateAvatar", "更新头像"),
            Map.entry("updateEmail", "修改电子邮箱"),
            Map.entry("updatePassword", "修改密码"),
            Map.entry("updateProfile", "修改个人资料"),
            Map.entry("upload", "上传文件"),
            Map.entry("userInfo", "查询当前用户信息"),
            Map.entry("users", "分页查询角色用户")
    );

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
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String tag = CONTROLLER_TAGS.get(controllerName);
            if (tag != null) {
                operation.setTags(List.of(tag));
            }

            Method method = handlerMethod.getMethod();
            String summary = operationLogSummary(method);
            if (summary == null) {
                summary = METHOD_SUMMARIES.get(method.getName());
            }
            if (summary != null) {
                operation.setSummary(summary);
            }

            RequirePermission permission = method.getAnnotation(RequirePermission.class);
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

    private String operationLogSummary(Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            if (!annotation.annotationType().getName()
                    .equals("com.yuncheng.framework.log.annotation.OperationLog")) {
                continue;
            }
            try {
                Object value = annotation.annotationType().getMethod("value").invoke(annotation);
                return value instanceof String text && !text.isBlank() ? text : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
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
