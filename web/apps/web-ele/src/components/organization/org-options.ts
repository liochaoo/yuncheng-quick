import type { OrgType } from '#/api/common/organization';
import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const ORG_TYPE_OPTIONS = {
  DEPARTMENT: { label: '部门', type: 'success' },
  GROUP: { label: '小组', type: 'warning' },
  ORGANIZATION: { label: '组织', type: 'primary' },
} satisfies EnumTagOptions;

export const ORG_TYPE_SELECT_OPTIONS = Object.entries(ORG_TYPE_OPTIONS).map(
  ([value, option]) => ({
    label: option.label,
    value: value as OrgType,
  }),
);

export function orgTypeLabel(type: OrgType) {
  return ORG_TYPE_OPTIONS[type]?.label ?? type;
}
