/** 普通文本的统一空值显示。 */
export function formatEmptyValue(value: unknown): number | string {
  if (value === null || value === undefined || value === '') return '-';
  return typeof value === 'number' ? value : String(value);
}
