import type { MenuItem, MenuSaveRequest, MenuType } from '#/api/system/menu';

export interface QueryItem {
  id: number;
  key: string;
  value: string;
}

let queryItemSequence = 0;

export function createQueryItem(key = '', value = ''): QueryItem {
  return { id: ++queryItemSequence, key, value };
}

export interface MenuFormModel {
  activeIcon: string;
  activePath: string;
  affixTab: boolean;
  affixTabOrder?: number;
  badge: string;
  badgeType?: string;
  badgeVariants?: string;
  componentPath: string;
  fullPathKey: boolean;
  hideChildrenInMenu: boolean;
  hideInBreadcrumb: boolean;
  hideInMenu: boolean;
  hideInTab: boolean;
  icon: string;
  iframeSrc: string;
  keepAlive: boolean;
  link: string;
  maxNumOfOpenTab?: number;
  menuName: string;
  menuType: MenuType;
  noBasicLayout: boolean;
  openInNewWindow: boolean;
  parentId?: string;
  permissionCode: string;
  queryItems: QueryItem[];
  redirect: string;
  routeName: string;
  routePath: string;
  sortOrder: number;
}

export function createDefaultMenuForm(): MenuFormModel {
  return {
    activeIcon: '',
    activePath: '',
    affixTab: false,
    affixTabOrder: undefined,
    badge: '',
    badgeType: undefined,
    badgeVariants: undefined,
    componentPath: '',
    fullPathKey: true,
    hideChildrenInMenu: false,
    hideInBreadcrumb: false,
    hideInMenu: false,
    hideInTab: false,
    icon: '',
    iframeSrc: '',
    keepAlive: false,
    link: '',
    maxNumOfOpenTab: undefined,
    menuName: '',
    menuType: 'CATALOG',
    noBasicLayout: false,
    openInNewWindow: false,
    parentId: undefined,
    permissionCode: '',
    queryItems: [],
    redirect: '',
    routeName: '',
    routePath: '',
    sortOrder: 0,
  };
}

function queryValue(value: unknown) {
  return typeof value === 'string' ? value : '';
}

/** 把详情响应转换为可编辑的表单模型。 */
export function fillMenuForm(model: MenuFormModel, detail: MenuItem) {
  model.activeIcon = detail.activeIcon ?? '';
  model.activePath = detail.activePath ?? '';
  model.affixTab = detail.affixTab;
  model.affixTabOrder = detail.affixTabOrder ?? undefined;
  model.badge = detail.badge ?? '';
  model.badgeType = detail.badgeType ?? undefined;
  model.badgeVariants = detail.badgeVariants ?? undefined;
  model.componentPath = detail.componentPath ?? '';
  model.fullPathKey = detail.fullPathKey;
  model.hideChildrenInMenu = detail.hideChildrenInMenu;
  model.hideInBreadcrumb = detail.hideInBreadcrumb;
  model.hideInMenu = detail.hideInMenu;
  model.hideInTab = detail.hideInTab;
  model.icon = detail.icon ?? '';
  model.iframeSrc = detail.iframeSrc ?? '';
  model.keepAlive = detail.keepAlive;
  model.link = detail.link ?? '';
  model.maxNumOfOpenTab = detail.maxNumOfOpenTab ?? undefined;
  model.menuName = detail.menuName;
  model.menuType = detail.menuType;
  model.noBasicLayout = detail.noBasicLayout;
  model.openInNewWindow = detail.openInNewWindow;
  model.parentId = detail.parentId ?? undefined;
  model.permissionCode = detail.permissionCode ?? '';
  model.queryItems = Object.entries(detail.query ?? {}).map(([key, value]) => ({
    ...createQueryItem(key, queryValue(value)),
  }));
  model.redirect = detail.redirect ?? '';
  model.routeName = detail.routeName ?? '';
  model.routePath = detail.routePath ?? '';
  model.sortOrder = detail.sortOrder;
}

function optional(value: string) {
  return value.trim() || undefined;
}

function queryObject(items: QueryItem[]) {
  const result: Record<string, string> = {};
  for (const item of items) {
    const key = item.key.trim();
    if (key) result[key] = item.value;
  }
  return Object.keys(result).length > 0 ? result : undefined;
}

/** 只提交当前菜单类型实际使用的字段，切换类型后不会带出隐藏字段。 */
export function buildMenuSaveRequest(model: MenuFormModel): MenuSaveRequest {
  const base: MenuSaveRequest = {
    affixTab: false,
    fullPathKey: true,
    hideChildrenInMenu: false,
    hideInBreadcrumb: false,
    hideInMenu: false,
    hideInTab: false,
    keepAlive: false,
    menuName: model.menuName.trim(),
    menuType: model.menuType,
    noBasicLayout: false,
    openInNewWindow: false,
    parentId: model.parentId,
    sortOrder: model.sortOrder ?? 0,
  };

  if (model.menuType === 'BUTTON') {
    return { ...base, permissionCode: optional(model.permissionCode) };
  }

  const supportsActivePath =
    model.menuType === 'EMBEDDED' || model.menuType === 'MENU';
  const supportsAffixTab = supportsActivePath;
  const supportsChildVisibility =
    model.menuType === 'CATALOG' || model.menuType === 'MENU';
  const supportsPageOptions = supportsActivePath;
  const supportsRedirect = supportsChildVisibility;
  const supportsTabVisibility = model.menuType !== 'LINK';

  Object.assign(base, {
    activeIcon: optional(model.activeIcon),
    activePath: supportsActivePath ? optional(model.activePath) : undefined,
    affixTab: supportsAffixTab && model.affixTab,
    affixTabOrder:
      supportsAffixTab && model.affixTab ? model.affixTabOrder : undefined,
    badge: model.badgeType === 'normal' ? optional(model.badge) : undefined,
    badgeType: model.badgeType,
    badgeVariants: model.badgeType ? model.badgeVariants : undefined,
    fullPathKey: model.fullPathKey,
    hideChildrenInMenu: supportsChildVisibility && model.hideChildrenInMenu,
    hideInBreadcrumb: supportsTabVisibility && model.hideInBreadcrumb,
    hideInMenu: model.hideInMenu,
    hideInTab: supportsTabVisibility && model.hideInTab,
    icon: optional(model.icon),
    keepAlive: model.menuType === 'MENU' && model.keepAlive,
    maxNumOfOpenTab: supportsPageOptions ? model.maxNumOfOpenTab : undefined,
    noBasicLayout: supportsPageOptions && model.noBasicLayout,
    openInNewWindow: model.menuType === 'LINK' && model.openInNewWindow,
    query: queryObject(model.queryItems),
    redirect: supportsRedirect ? optional(model.redirect) : undefined,
    routeName: optional(model.routeName),
    routePath: optional(model.routePath),
  });

  switch (model.menuType) {
    case 'EMBEDDED': {
      base.iframeSrc = optional(model.iframeSrc);
      break;
    }
    case 'LINK': {
      base.link = optional(model.link);
      break;
    }
    case 'MENU': {
      base.componentPath = optional(model.componentPath);
      base.permissionCode = optional(model.permissionCode);
      break;
    }
    // BUTTON 已在上方返回，CATALOG 没有类型专属字段。
  }
  return base;
}

export function queryItemsError(items: QueryItem[]) {
  const keys = new Set<string>();
  for (const item of items) {
    const key = item.key.trim();
    if (!key && item.value.trim()) {
      return '路由查询参数存在未填写参数名的数据';
    }
    if (key && keys.has(key)) {
      return `路由查询参数【${key}】重复`;
    }
    if (key) keys.add(key);
  }
}
