package com.yuncheng.system.dictionary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.dictionary.entity.SystemDictionary;
import org.apache.ibatis.annotations.Mapper;

/** 系统数据字典数据库访问。 */
@Mapper
public interface SystemDictionaryMapper extends BaseMapper<SystemDictionary> {
}
