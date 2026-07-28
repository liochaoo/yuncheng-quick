package com.yuncheng.system.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 批量下线登录会话请求。 */
public record OnlineSessionIdsRequest(
        @NotEmpty(message = "请选择需要下线的会话")
        @Size(max = 100, message = "单次最多下线 100 个会话")
        List<
                @NotBlank(message = "会话标识不能为空")
                @Size(max = 64, message = "会话标识不能超过 64 个字符")
                String> sessionIds
) {
}
