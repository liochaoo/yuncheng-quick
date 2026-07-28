package com.yuncheng.system.dictionary.controller;

import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.dictionary.dto.DictionaryOptionItem;
import com.yuncheng.system.dictionary.service.DictionaryQueryService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 已登录业务模块消费数据字典选项的公共接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/dictionaries")
public class DictionaryOptionController {

    private final DictionaryQueryService queryService;

    public DictionaryOptionController(DictionaryQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{dictionaryCode}/options")
    public ApiResponse<List<DictionaryOptionItem>> options(
            @PathVariable
            @Size(max = 50, message = "字典编码不能超过 50 个字符")
            @Pattern(
                    regexp = "[A-Za-z][A-Za-z0-9_-]*",
                    message = "字典编码格式不正确"
            )
            String dictionaryCode
    ) {
        return ApiResponse.success(queryService.listForConsumption(dictionaryCode));
    }
}
