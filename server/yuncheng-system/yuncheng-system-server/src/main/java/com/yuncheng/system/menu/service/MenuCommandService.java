package com.yuncheng.system.menu.service;

import com.yuncheng.system.menu.dto.MenuDeleteImpact;
import com.yuncheng.system.menu.dto.MenuSaveRequest;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.enums.MenuType;
import com.yuncheng.system.menu.mapper.SystemMenuMapper;
import com.yuncheng.system.menu.cache.MenuAuthorizationCacheInvalidator;
import com.yuncheng.system.permission.service.RoleMenuRelationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 新增、编辑和删除菜单。 */
@Service
public class MenuCommandService {

    private final SystemMenuMapper menuMapper;
    private final MenuQueryService menuQueryService;
    private final MenuValidationService validationService;
    private final RoleMenuRelationService roleMenuRelationService;
    private final MenuAuthorizationCacheInvalidator cacheInvalidator;

    public MenuCommandService(
            SystemMenuMapper menuMapper,
            MenuQueryService menuQueryService,
            MenuValidationService validationService,
            RoleMenuRelationService roleMenuRelationService,
            MenuAuthorizationCacheInvalidator cacheInvalidator
    ) {
        this.menuMapper = menuMapper;
        this.menuQueryService = menuQueryService;
        this.validationService = validationService;
        this.roleMenuRelationService = roleMenuRelationService;
        this.cacheInvalidator = cacheInvalidator;
    }

    @Transactional
    public Long create(MenuSaveRequest request) {
        SystemMenu menu = validationService.prepare(request, null);
        menuMapper.insert(menu);
        cacheInvalidator.clearAfterCommit(List.of(menu.getId()));
        return menu.getId();
    }

    @Transactional
    public void update(Long menuId, MenuSaveRequest request) {
        SystemMenu existing = menuQueryService.requireMenu(menuId);
        List<Long> changedMenuIds = menuMapper.selectSubtreeIds(menuId);
        SystemMenu updated = validationService.prepare(request, existing);
        cacheInvalidator.clearAfterCommit(changedMenuIds);
        menuMapper.updateById(updated);
        if (updated.getMenuType() == MenuType.CATALOG) {
            roleMenuRelationService.deleteByMenuIds(List.of(menuId));
        }
    }

    public MenuDeleteImpact deletionImpact(Long menuId) {
        menuQueryService.requireMenu(menuId);
        List<Long> menuIds = menuMapper.selectSubtreeIds(menuId);
        return new MenuDeleteImpact(
                menuIds.size(),
                roleMenuRelationService.countByMenuIds(menuIds)
        );
    }

    @Transactional
    public void delete(Long menuId) {
        menuQueryService.requireMenu(menuId);
        List<Long> menuIds = menuMapper.selectSubtreeIds(menuId);
        cacheInvalidator.clearAfterCommit(menuIds);
        roleMenuRelationService.deleteByMenuIds(menuIds);
        for (Long id : menuIds) {
            menuMapper.deleteById(id);
        }
    }
}
