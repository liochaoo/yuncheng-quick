package com.yuncheng.system.user.dto;

import com.yuncheng.system.user.enums.PasswordSetupMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 新增用户请求。 */
public record UserCreateRequest(
        @NotBlank(message = "登录名不能为空") @Size(max = 50, message = "登录名不能超过 50 个字符") String username,
        @NotNull(message = "密码设置方式不能为空") PasswordSetupMode passwordMode,
        String password,
        @NotBlank(message = "姓名不能为空") @Size(max = 64, message = "姓名不能超过 64 个字符") String realName,
        @Size(max = 32, message = "手机号码不能超过 32 个字符") String phone,
        @Email(message = "电子邮箱格式不正确") @Size(max = 254, message = "电子邮箱不能超过 254 个字符") String email,
        Integer sortOrder,
        @NotEmpty(message = "至少需要选择一个角色")
        @Size(max = 100, message = "单次最多分配 100 个角色")
        List<@NotNull(message = "角色主键不能为空")
                @Positive(message = "角色主键必须为正数") Long> roleIds,
        @NotEmpty(message = "至少需要选择一个归属组织")
        @Size(max = 100, message = "单个用户最多归属 100 个组织")
        List<@NotNull(message = "组织主键不能为空")
                @Positive(message = "组织主键必须为正数") Long> orgIds,
        @NotNull(message = "主组织不能为空")
        @Positive(message = "主组织主键必须为正数")
        Long primaryOrgId
) {
}
