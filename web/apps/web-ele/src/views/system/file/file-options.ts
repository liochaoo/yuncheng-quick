import type { FileApi } from '#/api/core';
import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const FILE_ACCESS_TAG_OPTIONS = {
  PRIVATE: { label: '私有', type: 'info' },
  PUBLIC: { label: '公开', type: 'success' },
} satisfies EnumTagOptions;

export const FILE_ACCESS_SELECT_OPTIONS = Object.entries(
  FILE_ACCESS_TAG_OPTIONS,
).map(([value, option]) => ({
  label: option.label,
  value: value as FileApi.AccessType,
}));

export const FILE_POLICY_OPTIONS = [
  { label: '普通附件', value: 'attachment' },
  { label: '普通图片', value: 'image' },
  { label: '头像', value: 'avatar' },
  { label: '公开图片', value: 'public-image' },
];

export function filePolicyLabel(policyCode?: string) {
  return (
    FILE_POLICY_OPTIONS.find((item) => item.value === policyCode)?.label ??
    policyCode ??
    '-'
  );
}
