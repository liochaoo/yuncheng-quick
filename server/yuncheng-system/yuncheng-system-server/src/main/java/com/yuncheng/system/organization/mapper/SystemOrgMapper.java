package com.yuncheng.system.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.organization.dto.OrgNameConflict;
import com.yuncheng.system.organization.entity.SystemOrg;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 系统组织数据库访问。 */
@Mapper
public interface SystemOrgMapper extends BaseMapper<SystemOrg> {

    SystemOrg selectByIdForUpdate(@Param("orgId") Long orgId);

    List<SystemOrg> selectSubtreeForUpdate(@Param("pathIds") String pathIds);

    Integer selectMaxDepthByPathIds(@Param("pathIds") String pathIds);

    List<OrgNameConflict> selectSiblingNameConflicts(
            @Param("candidatesJson") String candidatesJson
    );

    int updateSubtreeAfterRename(
            @Param("orgId") Long orgId,
            @Param("pathIds") String pathIds,
            @Param("oldFullPath") String oldFullPath,
            @Param("newFullPath") String newFullPath,
            @Param("orgCode") String orgCode,
            @Param("orgName") String orgName,
            @Param("sortOrder") Integer sortOrder,
            @Param("description") String description,
            @Param("updatedAt") Instant updatedAt,
            @Param("updatedBy") Long updatedBy
    );

    int updateSubtreeAfterMove(
            @Param("orgId") Long orgId,
            @Param("oldPathIds") String oldPathIds,
            @Param("newPathIds") String newPathIds,
            @Param("oldFullPath") String oldFullPath,
            @Param("newFullPath") String newFullPath,
            @Param("newParentId") Long newParentId,
            @Param("depthDelta") int depthDelta,
            @Param("updatedAt") Instant updatedAt,
            @Param("updatedBy") Long updatedBy
    );
}
