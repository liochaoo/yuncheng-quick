import type { OrgType } from '#/api/common/organization';
import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const ORG_TYPE_OPTIONS = {
  ORGANIZATION: { label: '组织', type: 'primary' },
  DEPARTMENT: { label: '部门', type: 'success' },
  GROUP: { label: '小组', type: 'warning' },
} satisfies EnumTagOptions;

export const ORG_TYPE_SELECT_OPTIONS = [
  { label: '组织', value: 'ORGANIZATION' },
  { label: '部门', value: 'DEPARTMENT' },
  { label: '小组', value: 'GROUP' },
] as const satisfies Array<{ label: string; value: OrgType }>;

export function orgTypeLabel(type: OrgType) {
  return ORG_TYPE_OPTIONS[type]?.label ?? type;
}
