package com.yuncheng.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.role.entity.SystemUserRole;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户角色关系数据库访问。 */
@Mapper
public interface SystemUserRoleMapper extends BaseMapper<SystemUserRole> {

    List<RoleSummaryRow> selectRoleSummariesByUserIds(@Param("userIds") Collection<Long> userIds);

    List<String> selectRoleCodes(@Param("userId") Long userId);

    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectSystemRoleUserIds(@Param("userIds") Collection<Long> userIds);

    List<Long> selectUserIdsHavingOnlyRole(
            @Param("roleId") Long roleId,
            @Param("userIds") Collection<Long> userIds
    );

    int countSystemRolesByUserId(@Param("userId") Long userId);
}
