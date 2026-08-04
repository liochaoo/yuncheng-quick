import type { RoleType } from '#/api/system/types';
import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const ROLE_TYPE_TAG_OPTIONS = {
  SYSTEM: { label: '系统角色', type: 'warning' },
  CUSTOM: { label: '自定义', type: 'info' },
} satisfies EnumTagOptions;

export const ROLE_TYPE_SELECT_OPTIONS = Object.entries(
  ROLE_TYPE_TAG_OPTIONS,
).map(([value, option]) => ({
  label: option.label,
  value: value as RoleType,
}));
