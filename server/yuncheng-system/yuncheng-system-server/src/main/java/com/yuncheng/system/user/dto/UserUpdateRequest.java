package com.yuncheng.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 编辑用户请求；登录名创建后不允许修改，头像由用户个人中心维护。 */
public record UserUpdateRequest(
        @NotBlank(message = "姓名不能为空") @Size(max = 64, message = "姓名不能超过 64 个字符") String realName,
        @Size(max = 32, message = "手机号码不能超过 32 个字符") String phone,
        @Email(message = "电子邮箱格式不正确") @Size(max = 254, message = "电子邮箱不能超过 254 个字符") String email,
        Integer sortOrder,
        @NotEmpty(message = "至少需要选择一个角色")
        @Size(max = 100, message = "单次最多分配 100 个角色")
        List<@NotNull(message = "角色主键不能为空")
                @Positive(message = "角色主键必须为正数") Long> roleIds
) {
}
