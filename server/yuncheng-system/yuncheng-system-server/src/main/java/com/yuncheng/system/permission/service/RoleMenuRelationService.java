package com.yuncheng.system.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.system.permission.entity.SystemRoleMenu;
import com.yuncheng.system.permission.mapper.SystemRoleMenuMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 维护角色与菜单权限的数据库关系。 */
@Service
public class RoleMenuRelationService {

    private static final int BATCH_SIZE = 500;

    private final SystemRoleMenuMapper roleMenuMapper;

    public RoleMenuRelationService(SystemRoleMenuMapper roleMenuMapper) {
        this.roleMenuMapper = roleMenuMapper;
    }

    public Set<Long> menuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SystemRoleMenu>()
                        .eq(SystemRoleMenu::getRoleId, roleId))
                .stream()
                .map(SystemRoleMenu::getMenuId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public long countByMenuIds(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return 0L;
        }
        return roleMenuMapper.selectCount(new LambdaQueryWrapper<SystemRoleMenu>()
                .in(SystemRoleMenu::getMenuId, menuIds));
    }

    public void replace(Long roleId, Collection<Long> menuIds) {
        Set<Long> existingIds = menuIdsByRoleId(roleId);
        Set<Long> targetIds = new LinkedHashSet<>(menuIds);
        Set<Long> removedIds = new LinkedHashSet<>(existingIds);
        removedIds.removeAll(targetIds);
        Set<Long> addedIds = new LinkedHashSet<>(targetIds);
        addedIds.removeAll(existingIds);
        if (!removedIds.isEmpty()) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SystemRoleMenu>()
                    .eq(SystemRoleMenu::getRoleId, roleId)
                    .in(SystemRoleMenu::getMenuId, removedIds));
        }
        if (!addedIds.isEmpty()) {
            roleMenuMapper.insert(addedIds.stream()
                    .map(menuId -> relation(roleId, menuId))
                    .toList(), BATCH_SIZE);
        }
    }

    /** 批量整体替换多个角色的菜单关系。 */
    public void replaceBatch(Map<Long, ? extends Collection<Long>> menuIdsByRoleId) {
        if (menuIdsByRoleId == null || menuIdsByRoleId.isEmpty()) {
            return;
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SystemRoleMenu>()
                .in(SystemRoleMenu::getRoleId, menuIdsByRoleId.keySet()));
        List<SystemRoleMenu> relations = new ArrayList<>();
        menuIdsByRoleId.forEach((roleId, menuIds) -> new LinkedHashSet<>(menuIds).forEach(
                menuId -> relations.add(relation(roleId, menuId))
        ));
        if (!relations.isEmpty()) {
            roleMenuMapper.insert(relations, BATCH_SIZE);
        }
    }

    public void deleteByRoleId(Long roleId) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SystemRoleMenu>()
                .eq(SystemRoleMenu::getRoleId, roleId));
    }

    public void deleteByMenuIds(Collection<Long> menuIds) {
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SystemRoleMenu>()
                    .in(SystemRoleMenu::getMenuId, menuIds));
        }
    }

    private SystemRoleMenu relation(Long roleId, Long menuId) {
        SystemRoleMenu relation = new SystemRoleMenu();
        relation.setRoleId(roleId);
        relation.setMenuId(menuId);
        return relation;
    }
}
