package com.yuncheng.system.dictionary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.dictionary.cache.DictionaryOptionCacheService;
import com.yuncheng.system.dictionary.dto.DictionaryDetail;
import com.yuncheng.system.dictionary.dto.DictionaryListItem;
import com.yuncheng.system.dictionary.dto.DictionaryOptionDetail;
import com.yuncheng.system.dictionary.dto.DictionaryOptionItem;
import com.yuncheng.system.dictionary.dto.DictionaryOptionListItem;
import com.yuncheng.system.dictionary.dto.DictionaryOptionPageQuery;
import com.yuncheng.system.dictionary.dto.DictionaryPageQuery;
import com.yuncheng.system.dictionary.entity.SystemDictionary;
import com.yuncheng.system.dictionary.entity.SystemDictionaryOption;
import com.yuncheng.system.dictionary.mapper.SystemDictionaryMapper;
import com.yuncheng.system.dictionary.mapper.SystemDictionaryOptionMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 查询数据字典、管理选项和普通业务消费选项。 */
@Service
public class DictionaryQueryService {

    private final SystemDictionaryMapper dictionaryMapper;
    private final SystemDictionaryOptionMapper optionMapper;
    private final DictionaryOptionCacheService optionCacheService;

    public DictionaryQueryService(
            SystemDictionaryMapper dictionaryMapper,
            SystemDictionaryOptionMapper optionMapper,
            DictionaryOptionCacheService optionCacheService
    ) {
        this.dictionaryMapper = dictionaryMapper;
        this.optionMapper = optionMapper;
        this.optionCacheService = optionCacheService;
    }

    public PageResult<DictionaryListItem> page(DictionaryPageQuery query) {
        String keyword = normalizedText(query.getKeyword());
        String codeKeyword = normalizedCode(keyword);
        IPage<SystemDictionary> page = dictionaryMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<SystemDictionary>()
                        .and(
                                StringUtils.hasText(keyword),
                                nested -> nested
                                        .like(SystemDictionary::getDictionaryCode, codeKeyword)
                                        .or()
                                        .like(SystemDictionary::getDictionaryName, keyword)
                        )
                        .orderByAsc(SystemDictionary::getSortOrder, SystemDictionary::getId)
        );
        List<DictionaryListItem> items = page.getRecords().stream()
                .map(this::toListItem)
                .toList();
        return PageResult.of(items, page.getTotal(), query);
    }

    public DictionaryDetail detail(Long dictionaryId) {
        SystemDictionary dictionary = requireDictionary(dictionaryId);
        return new DictionaryDetail(
                id(dictionary.getId()),
                dictionary.getDictionaryCode(),
                dictionary.getDictionaryName(),
                dictionary.getDescription(),
                dictionary.getSortOrder(),
                dictionary.getCreatedAt(),
                id(dictionary.getCreatedBy()),
                dictionary.getUpdatedAt(),
                id(dictionary.getUpdatedBy())
        );
    }

    public PageResult<DictionaryOptionListItem> pageOptions(
            Long dictionaryId,
            DictionaryOptionPageQuery query
    ) {
        requireDictionary(dictionaryId);
        String keyword = normalizedText(query.getKeyword());
        IPage<SystemDictionaryOption> page = optionMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<SystemDictionaryOption>()
                        .eq(SystemDictionaryOption::getDictionaryId, dictionaryId)
                        .and(
                                StringUtils.hasText(keyword),
                                nested -> nested
                                        .like(SystemDictionaryOption::getOptionValue, keyword)
                                        .or()
                                        .like(SystemDictionaryOption::getOptionLabel, keyword)
                        )
                        .eq(
                                query.getEnabled() != null,
                                SystemDictionaryOption::getEnabled,
                                query.getEnabled()
                        )
                        .orderByAsc(
                                SystemDictionaryOption::getSortOrder,
                                SystemDictionaryOption::getId
                        )
        );
        List<DictionaryOptionListItem> items = page.getRecords().stream()
                .map(this::toOptionListItem)
                .toList();
        return PageResult.of(items, page.getTotal(), query);
    }

    public DictionaryOptionDetail optionDetail(Long dictionaryId, Long optionId) {
        SystemDictionaryOption option = requireOption(dictionaryId, optionId);
        return new DictionaryOptionDetail(
                id(option.getId()),
                id(option.getDictionaryId()),
                option.getOptionValue(),
                option.getOptionLabel(),
                option.getDescription(),
                option.getSortOrder(),
                Boolean.TRUE.equals(option.getEnabled()),
                option.getCreatedAt(),
                id(option.getCreatedBy()),
                option.getUpdatedAt(),
                id(option.getUpdatedBy())
        );
    }

    public List<DictionaryOptionItem> listForConsumption(String dictionaryCode) {
        String normalizedCode = normalizedCode(dictionaryCode);
        return optionCacheService.getOrLoad(
                normalizedCode,
                () -> loadOptionsForConsumption(requireDictionaryByCode(normalizedCode).getId())
        );
    }

    public SystemDictionary requireDictionary(Long dictionaryId) {
        SystemDictionary dictionary = dictionaryId == null
                ? null
                : dictionaryMapper.selectById(dictionaryId);
        if (dictionary == null) {
            throw PlatformException.notFound("数据字典不存在");
        }
        return dictionary;
    }

    public SystemDictionaryOption requireOption(Long dictionaryId, Long optionId) {
        SystemDictionaryOption option = optionId == null ? null : optionMapper.selectById(optionId);
        if (option == null || !option.getDictionaryId().equals(dictionaryId)) {
            throw PlatformException.notFound("字典选项不存在");
        }
        return option;
    }

    private SystemDictionary requireDictionaryByCode(String dictionaryCode) {
        String normalized = normalizedCode(dictionaryCode);
        SystemDictionary dictionary = dictionaryMapper.selectOne(
                new LambdaQueryWrapper<SystemDictionary>()
                        .eq(SystemDictionary::getDictionaryCode, normalized)
        );
        if (dictionary == null) {
            throw PlatformException.notFound("数据字典不存在");
        }
        return dictionary;
    }

    private List<DictionaryOptionItem> loadOptionsForConsumption(Long dictionaryId) {
        return optionMapper.selectList(new LambdaQueryWrapper<SystemDictionaryOption>()
                        .eq(SystemDictionaryOption::getDictionaryId, dictionaryId)
                        .orderByAsc(
                                SystemDictionaryOption::getSortOrder,
                                SystemDictionaryOption::getId
                        ))
                .stream()
                .map(option -> new DictionaryOptionItem(
                        option.getOptionValue(),
                        option.getOptionLabel(),
                        Boolean.TRUE.equals(option.getEnabled())
                ))
                .toList();
    }

    private DictionaryListItem toListItem(SystemDictionary dictionary) {
        return new DictionaryListItem(
                id(dictionary.getId()),
                dictionary.getDictionaryCode(),
                dictionary.getDictionaryName(),
                dictionary.getDescription(),
                dictionary.getSortOrder(),
                dictionary.getCreatedAt(),
                dictionary.getUpdatedAt()
        );
    }

    private DictionaryOptionListItem toOptionListItem(SystemDictionaryOption option) {
        return new DictionaryOptionListItem(
                id(option.getId()),
                id(option.getDictionaryId()),
                option.getOptionValue(),
                option.getOptionLabel(),
                option.getDescription(),
                option.getSortOrder(),
                Boolean.TRUE.equals(option.getEnabled()),
                option.getCreatedAt(),
                option.getUpdatedAt()
        );
    }

    private String normalizedCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String normalizedText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String id(Long value) {
        return value == null ? null : value.toString();
    }
}
