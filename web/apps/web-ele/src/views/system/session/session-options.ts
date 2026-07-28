import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const SESSION_STATUS_TAG_OPTIONS = {
  false: { label: '在线', type: 'success' },
  true: { label: '当前', type: 'primary' },
} satisfies EnumTagOptions;
