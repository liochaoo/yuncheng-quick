import type { AvailabilityResult } from '#/api/system/types';

import { requestClient } from '#/api/request';

export type MenuType = 'BUTTON' | 'CATALOG' | 'EMBEDDED' | 'LINK' | 'MENU';
export type MenuUniqueField =
  | 'MENU_NAME'
  | 'PERMISSION_CODE'
  | 'ROUTE_NAME'
  | 'ROUTE_PATH';

export interface MenuItem {
  activeIcon?: null | string;
  activePath?: null | string;
  affixTab: boolean;
  affixTabOrder?: null | number;
  badge?: null | string;
  badgeType?: null | string;
  badgeVariants?: null | string;
  children?: MenuItem[];
  componentPath?: null | string;
  createdAt: string;
  createdBy: string;
  fullPathKey: boolean;
  hideChildrenInMenu: boolean;
  hideInBreadcrumb: boolean;
  hideInMenu: boolean;
  hideInTab: boolean;
  icon?: null | string;
  id: string;
  iframeSrc?: null | string;
  keepAlive: boolean;
  link?: null | string;
  maxNumOfOpenTab?: null | number;
  menuName: string;
  menuType: MenuType;
  noBasicLayout: boolean;
  openInNewWindow: boolean;
  parentId?: null | string;
  permissionCode?: null | string;
  query?: null | Record<string, string>;
  redirect?: null | string;
  routeName?: null | string;
  routePath?: null | string;
  sortOrder: number;
  updatedAt: string;
  updatedBy: string;
}

export interface MenuDetail {
  menu: MenuItem;
  parentName?: null | string;
}

export interface MenuSaveRequest {
  activeIcon?: string;
  activePath?: string;
  affixTab: boolean;
  affixTabOrder?: number;
  badge?: string;
  badgeType?: string;
  badgeVariants?: string;
  componentPath?: string;
  fullPathKey: boolean;
  hideChildrenInMenu: boolean;
  hideInBreadcrumb: boolean;
  hideInMenu: boolean;
  hideInTab: boolean;
  icon?: string;
  iframeSrc?: string;
  keepAlive: boolean;
  link?: string;
  maxNumOfOpenTab?: number;
  menuName: string;
  menuType: MenuType;
  noBasicLayout: boolean;
  openInNewWindow: boolean;
  parentId?: string;
  permissionCode?: string;
  query?: Record<string, string>;
  redirect?: string;
  routeName?: string;
  routePath?: string;
  sortOrder: number;
}

export interface MenuDeleteImpact {
  menuCount: number;
  roleRelationCount: number;
}

interface MenuUniquenessCheckRequest {
  field: MenuUniqueField;
  id?: string;
  parentId?: string;
  value: string;
}

export async function getMenuTreeApi() {
  return requestClient.get<MenuItem[]>('/system/menus');
}

export async function getMenuDetailApi(id: string) {
  return requestClient.get<MenuDetail>(`/system/menus/${id}`);
}

export async function createMenuApi(data: MenuSaveRequest) {
  return requestClient.post<string>('/system/menus', data);
}

export async function updateMenuApi(id: string, data: MenuSaveRequest) {
  return requestClient.put<null>(`/system/menus/${id}`, data);
}

export async function checkMenuUniquenessApi(data: MenuUniquenessCheckRequest) {
  return requestClient.post<AvailabilityResult>(
    '/system/menus/uniqueness-check',
    data,
  );
}

export async function getMenuDeleteImpactApi(id: string) {
  return requestClient.get<MenuDeleteImpact>(
    `/system/menus/${id}/deletion-impact`,
  );
}

export async function deleteMenuApi(id: string) {
  return requestClient.delete<null>(`/system/menus/${id}`);
}
