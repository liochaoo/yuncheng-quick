package com.yuncheng.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.system.user.dto.UserPageQuery;
import com.yuncheng.system.user.entity.SystemUser;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 系统用户数据库访问。 */
@Mapper
public interface SystemUserMapper extends BaseMapper<SystemUser> {

    SystemUser selectByIdForUpdate(@Param("id") Long id);

    int updateLoginFailureState(
            @Param("id") Long id,
            @Param("failedCount") int failedCount,
            @Param("windowStartedAt") Instant windowStartedAt,
            @Param("lockedUntil") Instant lockedUntil
    );

    int clearLoginFailureState(@Param("id") Long id);

    IPage<SystemUser> selectUserPage(Page<SystemUser> page, @Param("query") UserPageQuery query);

    IPage<SystemUser> selectRoleUserPage(
            Page<SystemUser> page,
            @Param("roleId") Long roleId,
            @Param("username") String username,
            @Param("realName") String realName,
            @Param("assigned") boolean assigned,
            @Param("excludeSystemRoleUsers") boolean excludeSystemRoleUsers
    );

    List<Long> selectIdsByRolesAfter(
            @Param("roleIds") Collection<Long> roleIds,
            @Param("roleCodes") Collection<String> roleCodes,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );

    long countEnabledUsersByRoleCode(
            @Param("roleCode") String roleCode,
            @Param("excludedUserId") Long excludedUserId
    );
}
