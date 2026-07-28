package com.yuncheng.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.role.entity.SystemRole;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 系统角色数据库访问。 */
@Mapper
public interface SystemRoleMapper extends BaseMapper<SystemRole> {

    List<RoleUserCountRow> selectUserCounts(@Param("roleIds") Collection<Long> roleIds);
}
