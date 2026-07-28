import type { MenuType } from '#/api/system/menu';

/** 判断上级节点能否直接包含指定类型的下级节点。 */
export function menuTypeAllowsChild(parentType: MenuType, childType: MenuType) {
  if (childType === 'BUTTON') return parentType !== 'BUTTON';
  return parentType === 'CATALOG' || parentType === 'MENU';
}

/** 按钮不能继续添加下级，其他节点都可以直接挂载按钮。 */
export function menuTypeAllowsChildren(menuType: MenuType) {
  return menuType !== 'BUTTON';
}

/** 为“添加下级”提供最符合常见使用方式的默认类型。 */
export function defaultChildMenuType(parentType: MenuType): MenuType {
  return parentType === 'CATALOG' ? 'MENU' : 'BUTTON';
}
