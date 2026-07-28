package com.yuncheng.framework.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.framework.file.entity.PlatformFile;
import org.apache.ibatis.annotations.Mapper;

/** 平台文件数据库访问。 */
@Mapper
public interface PlatformFileMapper extends BaseMapper<PlatformFile> {
}
