import type { MenuType } from '#/api/system/menu';
import type { RoleType } from '#/api/system/types';
import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const MENU_TYPE_TAG_OPTIONS = {
  CATALOG: { label: '目录', type: 'info' },
  MENU: { label: '页面', type: 'primary' },
  EMBEDDED: { label: '内嵌', type: 'warning' },
  LINK: { label: '链接', type: 'success' },
  BUTTON: { label: '按钮', type: 'danger' },
} satisfies EnumTagOptions;

export const ROLE_TYPE_TAG_OPTIONS = {
  SYSTEM: { label: '系统角色', type: 'warning' },
  CUSTOM: { label: '自定义', type: 'info' },
} satisfies EnumTagOptions;

export const CLIENT_TYPE_TAG_OPTIONS = {
  ANDROID: { label: 'Android', type: 'success' },
  HARMONYOS: { label: 'HarmonyOS', type: 'warning' },
  IOS: { label: 'iOS', type: 'info' },
  WEB: { label: 'Web', type: 'primary' },
  WECHAT_MINI_PROGRAM: { label: '微信小程序', type: 'success' },
} satisfies EnumTagOptions;

export const MENU_TYPE_SELECT_OPTIONS = Object.entries(
  MENU_TYPE_TAG_OPTIONS,
).map(([value, option]) => ({
  label: option.label,
  value: value as MenuType,
}));

export const ROLE_TYPE_SELECT_OPTIONS = Object.entries(
  ROLE_TYPE_TAG_OPTIONS,
).map(([value, option]) => ({
  label: option.label,
  value: value as RoleType,
}));

export const CLIENT_TYPE_SELECT_OPTIONS = Object.entries(
  CLIENT_TYPE_TAG_OPTIONS,
).map(([value, option]) => ({ label: option.label, value }));
