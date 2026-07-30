package com.yuncheng.system.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.organization.entity.SystemOrganizationNode;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 系统组织节点数据库访问。 */
@Mapper
public interface SystemOrganizationNodeMapper extends BaseMapper<SystemOrganizationNode> {

    SystemOrganizationNode selectByIdForUpdate(@Param("nodeId") Long nodeId);

    List<SystemOrganizationNode> selectSubtreeForUpdate(@Param("pathIds") String pathIds);

    int updateSubtreeAfterEdit(
            @Param("nodeId") Long nodeId,
            @Param("pathIds") String pathIds,
            @Param("oldFullPath") String oldFullPath,
            @Param("newFullPath") String newFullPath,
            @Param("nodeCode") String nodeCode,
            @Param("nodeName") String nodeName,
            @Param("sortOrder") Integer sortOrder,
            @Param("description") String description,
            @Param("updatedAt") Instant updatedAt,
            @Param("updatedBy") Long updatedBy
    );

    int updateSubtreeAfterMove(
            @Param("nodeId") Long nodeId,
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
