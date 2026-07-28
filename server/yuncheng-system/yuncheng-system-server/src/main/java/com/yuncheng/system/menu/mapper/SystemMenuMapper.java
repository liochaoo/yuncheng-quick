package com.yuncheng.system.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuncheng.system.menu.entity.SystemMenu;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 系统菜单数据库访问。 */
@Mapper
public interface SystemMenuMapper extends BaseMapper<SystemMenu> {

    List<SystemMenu> selectEffectiveRoutesByUserId(@Param("userId") Long userId);

    List<SystemMenu> selectAllEffectiveRoutes();

    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    List<String> selectAllPermissionCodes();

    List<Long> selectSubtreeIds(@Param("menuId") Long menuId);

    List<Long> selectRelatedRoleIds(@Param("menuIds") List<Long> menuIds);
}
