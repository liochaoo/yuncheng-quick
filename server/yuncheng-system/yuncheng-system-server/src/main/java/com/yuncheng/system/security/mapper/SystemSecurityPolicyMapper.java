package com.yuncheng.system.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.security.entity.SystemSecurityPolicy;
import org.apache.ibatis.annotations.Mapper;

/** 安全策略数据库访问。 */
@Mapper
public interface SystemSecurityPolicyMapper extends BaseMapper<SystemSecurityPolicy> {
}
