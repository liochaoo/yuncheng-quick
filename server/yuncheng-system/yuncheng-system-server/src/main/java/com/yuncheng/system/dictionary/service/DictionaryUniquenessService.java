package com.yuncheng.system.dictionary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.dictionary.entity.SystemDictionary;
import com.yuncheng.system.dictionary.entity.SystemDictionaryOption;
import com.yuncheng.system.dictionary.mapper.SystemDictionaryMapper;
import com.yuncheng.system.dictionary.mapper.SystemDictionaryOptionMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** 数据字典及选项唯一性校验。 */
@Service
public class DictionaryUniquenessService {

    private final SystemDictionaryMapper dictionaryMapper;
    private final SystemDictionaryOptionMapper optionMapper;

    public DictionaryUniquenessService(
            SystemDictionaryMapper dictionaryMapper,
            SystemDictionaryOptionMapper optionMapper
    ) {
        this.dictionaryMapper = dictionaryMapper;
        this.optionMapper = optionMapper;
    }

    public boolean isDictionaryCodeAvailable(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return dictionaryMapper.selectCount(new LambdaQueryWrapper<SystemDictionary>()
                .eq(SystemDictionary::getDictionaryCode, normalized)) == 0;
    }

    public void requireDictionaryCodeAvailable(String value) {
        if (!isDictionaryCodeAvailable(value)) {
            throw PlatformException.conflict("字典编码已存在");
        }
    }

    public boolean isOptionValueAvailable(Long dictionaryId, String value) {
        String normalized = value == null ? "" : value.trim();
        return optionMapper.selectCount(new LambdaQueryWrapper<SystemDictionaryOption>()
                .eq(SystemDictionaryOption::getDictionaryId, dictionaryId)
                .eq(SystemDictionaryOption::getOptionValue, normalized)) == 0;
    }

    public void requireOptionValueAvailable(Long dictionaryId, String value) {
        if (!isOptionValueAvailable(dictionaryId, value)) {
            throw PlatformException.conflict("当前字典下的选项值已存在");
        }
    }
}
