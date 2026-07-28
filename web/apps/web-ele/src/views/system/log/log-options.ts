import type { EnumTagOptions } from '#/components/display/enum-tag.types';

export const LOGIN_EVENT_TAG_OPTIONS = {
  LOGIN: { label: '登录', type: 'primary' },
  LOGOUT: { label: '退出', type: 'info' },
} satisfies EnumTagOptions;

export const LOGIN_EVENT_SELECT_OPTIONS = Object.entries(
  LOGIN_EVENT_TAG_OPTIONS,
).map(([value, option]) => ({ label: option.label, value }));

export const LOG_RESULT_TAG_OPTIONS = {
  false: { label: '失败', type: 'danger' },
  true: { label: '成功', type: 'success' },
} satisfies EnumTagOptions;

export const LOG_RESULT_SELECT_OPTIONS = [
  { label: '成功', value: true },
  { label: '失败', value: false },
];
