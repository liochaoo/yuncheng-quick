package com.yuncheng.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.user.entity.SystemUserOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户组织归属关系数据库访问。 */
@Mapper
public interface SystemUserOrgMapper extends BaseMapper<SystemUserOrg> {

    long countDistinctUsersInSubtree(@Param("pathIds") String pathIds);

    long countRelationsInSubtree(@Param("pathIds") String pathIds);
}
