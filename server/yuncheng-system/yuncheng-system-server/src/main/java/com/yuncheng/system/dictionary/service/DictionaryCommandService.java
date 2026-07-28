package com.yuncheng.system.dictionary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.dictionary.dto.DictionaryCreateRequest;
import com.yuncheng.system.dictionary.dto.DictionaryOptionCreateRequest;
import com.yuncheng.system.dictionary.dto.DictionaryOptionUpdateRequest;
import com.yuncheng.system.dictionary.dto.DictionaryUpdateRequest;
import com.yuncheng.system.dictionary.entity.SystemDictionary;
import com.yuncheng.system.dictionary.entity.SystemDictionaryOption;
import com.yuncheng.system.dictionary.mapper.SystemDictionaryMapper;
import com.yuncheng.system.dictionary.mapper.SystemDictionaryOptionMapper;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 新增、编辑和删除数据字典及选项。 */
@Service
public class DictionaryCommandService {

    private static final Pattern DICTIONARY_CODE_PATTERN =
            Pattern.compile("[a-z][a-z0-9_-]{0,49}");
    private static final Pattern OPTION_VALUE_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,99}");

    private final SystemDictionaryMapper dictionaryMapper;
    private final SystemDictionaryOptionMapper optionMapper;
    private final DictionaryQueryService queryService;
    private final DictionaryUniquenessService uniquenessService;

    public DictionaryCommandService(
            SystemDictionaryMapper dictionaryMapper,
            SystemDictionaryOptionMapper optionMapper,
            DictionaryQueryService queryService,
            DictionaryUniquenessService uniquenessService
    ) {
        this.dictionaryMapper = dictionaryMapper;
        this.optionMapper = optionMapper;
        this.queryService = queryService;
        this.uniquenessService = uniquenessService;
    }

    @Transactional
    public Long create(DictionaryCreateRequest request) {
        String dictionaryCode = normalizeDictionaryCode(request.dictionaryCode());
        uniquenessService.requireDictionaryCodeAvailable(dictionaryCode);
        SystemDictionary dictionary = new SystemDictionary();
        dictionary.setDictionaryCode(dictionaryCode);
        dictionary.setDictionaryName(normalizeRequiredText(
                request.dictionaryName(),
                100,
                "字典名称"
        ));
        dictionary.setDescription(normalizeOptionalText(
                request.description(),
                500,
                "字典说明"
        ));
        dictionary.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        dictionaryMapper.insert(dictionary);
        return dictionary.getId();
    }

    @Transactional
    public void update(Long dictionaryId, DictionaryUpdateRequest request) {
        SystemDictionary dictionary = queryService.requireDictionary(dictionaryId);
        dictionary.setDictionaryName(normalizeRequiredText(
                request.dictionaryName(),
                100,
                "字典名称"
        ));
        dictionary.setDescription(normalizeOptionalText(
                request.description(),
                500,
                "字典说明"
        ));
        dictionary.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        dictionaryMapper.updateById(dictionary);
    }

    @Transactional
    public void delete(Long dictionaryId) {
        SystemDictionary dictionary = queryService.requireDictionary(dictionaryId);
        boolean hasOptions = optionMapper.exists(
                new LambdaQueryWrapper<SystemDictionaryOption>()
                        .eq(SystemDictionaryOption::getDictionaryId, dictionaryId)
        );
        if (hasOptions) {
            throw PlatformException.conflict("字典下仍有选项，不能删除");
        }
        dictionaryMapper.deleteById(dictionary.getId());
    }

    @Transactional
    public Long createOption(Long dictionaryId, DictionaryOptionCreateRequest request) {
        queryService.requireDictionary(dictionaryId);
        String optionValue = normalizeOptionValue(request.optionValue());
        uniquenessService.requireOptionValueAvailable(dictionaryId, optionValue);
        SystemDictionaryOption option = new SystemDictionaryOption();
        option.setDictionaryId(dictionaryId);
        option.setOptionValue(optionValue);
        option.setOptionLabel(normalizeRequiredText(
                request.optionLabel(),
                100,
                "选项标签"
        ));
        option.setDescription(normalizeOptionalText(
                request.description(),
                500,
                "选项说明"
        ));
        option.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        option.setEnabled(true);
        optionMapper.insert(option);
        return option.getId();
    }

    @Transactional
    public void updateOption(
            Long dictionaryId,
            Long optionId,
            DictionaryOptionUpdateRequest request
    ) {
        SystemDictionaryOption option = queryService.requireOption(dictionaryId, optionId);
        option.setOptionLabel(normalizeRequiredText(
                request.optionLabel(),
                100,
                "选项标签"
        ));
        option.setDescription(normalizeOptionalText(
                request.description(),
                500,
                "选项说明"
        ));
        option.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        optionMapper.updateById(option);
    }

    @Transactional
    public void changeOptionStatus(Long dictionaryId, Long optionId, boolean enabled) {
        SystemDictionaryOption option = queryService.requireOption(dictionaryId, optionId);
        option.setEnabled(enabled);
        optionMapper.updateById(option);
    }

    @Transactional
    public void deleteOption(Long dictionaryId, Long optionId) {
        SystemDictionaryOption option = queryService.requireOption(dictionaryId, optionId);
        optionMapper.deleteById(option.getId());
    }

    private String normalizeDictionaryCode(String value) {
        if (!StringUtils.hasText(value)) {
            throw PlatformException.badRequest("字典编码不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!DICTIONARY_CODE_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest(
                    "字典编码只能包含字母、数字、下划线和连字符，并以字母开头，长度不能超过 50 个字符"
            );
        }
        return normalized;
    }

    private String normalizeOptionValue(String value) {
        if (!StringUtils.hasText(value)) {
            throw PlatformException.badRequest("选项值不能为空");
        }
        String normalized = value.trim();
        if (!OPTION_VALUE_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest(
                    "选项值只能包含字母、数字、点、下划线、冒号和连字符，长度不能超过 100 个字符"
            );
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, int maxLength, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw PlatformException.badRequest(fieldName + "不能为空");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw PlatformException.badRequest(fieldName + "不能超过 " + maxLength + " 个字符");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, int maxLength, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw PlatformException.badRequest(fieldName + "不能超过 " + maxLength + " 个字符");
        }
        return normalized;
    }
}
