package com.yuncheng.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.user.entity.SystemUserPasswordHistory;
import org.apache.ibatis.annotations.Mapper;

/** 用户密码历史数据库访问。 */
@Mapper
public interface SystemUserPasswordHistoryMapper extends BaseMapper<SystemUserPasswordHistory> {
}
