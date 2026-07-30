import type { OrganizationNodeType } from '#/api/common/organization';
import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const ORGANIZATION_NODE_TYPE_OPTIONS = {
  DEPARTMENT: { label: '部门', type: 'success' },
  GROUP: { label: '小组', type: 'warning' },
  ORGANIZATION: { label: '组织', type: 'primary' },
} satisfies EnumTagOptions;

export const ORGANIZATION_NODE_TYPE_SELECT_OPTIONS = Object.entries(
  ORGANIZATION_NODE_TYPE_OPTIONS,
).map(([value, option]) => ({
  label: option.label,
  value: value as OrganizationNodeType,
}));

export function organizationNodeTypeLabel(type: OrganizationNodeType) {
  return ORGANIZATION_NODE_TYPE_OPTIONS[type]?.label ?? type;
}
